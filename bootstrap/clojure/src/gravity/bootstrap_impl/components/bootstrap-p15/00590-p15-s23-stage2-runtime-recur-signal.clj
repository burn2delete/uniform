

(defn p15-s23-stage2-runtime-recur-signal
  [values]
  (P15S23Stage2RuntimeRecurSignal. (vec values)))

(defn- p15-s23-stage2-runtime-map-entry
  [value requested-key]
  (when (map? value)
    (if (sorted? value)
      ;; A sorted map can use a comparator that rejects a heterogeneous lookup
      ;; key before equality is considered. Runtime records may legitimately
      ;; be inspected for keyword control keys even when their own keys use a
      ;; different comparable type, so retain the equality scan for that case.
      (some (fn [entry]
              (when (= requested-key (key entry)) entry))
            value)
      ;; Ordinary runtime records are persistent hash/array maps. `find`
      ;; preserves their key equality and nil-value behavior without scanning
      ;; every entry for every interpreted instruction.
      (find value requested-key))))

(defn- p15-s23-stage2-runtime-recur-values
  [value]
  (when (instance? P15S23Stage2RuntimeRecurSignal value)
    (.-values ^P15S23Stage2RuntimeRecurSignal value)))

(defn p15-s23-stage2-runtime-recur-signal?
  [value]
  (boolean
   (and (instance? P15S23Stage2RuntimeRecurSignal value)
        (vector? (p15-s23-stage2-runtime-recur-values value)))))

(defn p15-s23-stage2-runtime-recur-fail!
  [plan target-arity actual-arity reason]
  (fail! "L2-RECUR-TARGET"
         "recur has no compatible loop or function target"
         {:source-span {:source (get-in plan [:source :path])}
          :target-arity target-arity
          :actual-arity actual-arity
          :reason reason
          :remediation
          "Use recur only in tail position inside a compatible loop or function with matching arity."}))

(defn p15-s23-stage2-runtime-nontail-value!
  [plan value reason]
  (when (p15-s23-stage2-runtime-recur-signal? value)
    (p15-s23-stage2-runtime-recur-fail!
     plan nil (count (p15-s23-stage2-runtime-recur-values value)) reason))
  value)

(defn- p15-s23-stage2-runtime-execute-value
  [runtime plan env instruction reason]
  (p15-s23-stage2-runtime-nontail-value!
   plan
   (p15-s23-stage2-runtime-execute-instruction
    runtime plan env instruction)
   reason))

(defn- p15-s23-stage2-runtime-bind-values
  [base names values]
  ;; Function and loop binders are normally tiny. Avoid `zipmap`'s transient
  ;; map plus `merge`'s seq traversal for the common fixed-arity cases.
  (case (count names)
    0 base
    1 (assoc base (nth names 0) (nth values 0))
    2 (assoc (assoc base
                    (nth names 0) (nth values 0))
             (nth names 1) (nth values 1))
    3 (assoc (assoc (assoc base
                          (nth names 0) (nth values 0))
                   (nth names 1) (nth values 1))
             (nth names 2) (nth values 2))
    (merge base (zipmap names values))))

(defn p15-s23-stage2-runtime-execute-values
  [runtime plan env instructions reason]
  ;; Builtin and function calls overwhelmingly carry zero to three arguments.
  ;; `mapv` allocates a transient vector root/tail even for those tiny
  ;; collections, which accounted for most allocation in a live B47 profile.
  ;; Preserve the generic path for larger carriers.
  (case (count instructions)
    0 []
    1 [(p15-s23-stage2-runtime-execute-value
        runtime plan env (nth instructions 0) reason)]
    2 [(p15-s23-stage2-runtime-execute-value
        runtime plan env (nth instructions 0) reason)
       (p15-s23-stage2-runtime-execute-value
        runtime plan env (nth instructions 1) reason)]
    3 [(p15-s23-stage2-runtime-execute-value
        runtime plan env (nth instructions 0) reason)
       (p15-s23-stage2-runtime-execute-value
        runtime plan env (nth instructions 1) reason)
       (p15-s23-stage2-runtime-execute-value
        runtime plan env (nth instructions 2) reason)]
    (mapv (fn [instruction]
            (p15-s23-stage2-runtime-execute-value
             runtime plan env instruction reason))
          instructions)))

(defn- p15-s23-stage2-runtime-execute-map-entries
  "Evaluate map-literal entries without allocating an intermediate pair.

  Compiler plans represent entries as a vector of {:key ... :value ...}
  records. The old `into {}`/`map` path evaluated each record into a fresh
  two-element vector before conjoining it into the transient map. Large
  compiler artifacts contain many map literals, so that short-lived carrier
  became a measurable allocation and sequence-traversal hot path. Keep the
  same left-to-right key/value evaluation and last-key-wins semantics while
  associating directly into one transient map. The sequence branch preserves
  the public helper's established behavior for direct list callers.
  "
  [runtime plan env entries]
  (if (vector? entries)
    (let [entry-count (count entries)]
      (loop [index 0
             result (transient {})]
        (if (< index entry-count)
          (let [{:keys [key value]} (nth entries index)
                evaluated-key
                (p15-s23-stage2-runtime-nontail-value!
                 plan
                 (p15-s23-stage2-runtime-execute-instruction
                  runtime plan env key)
                 :recur-inside-map-key)
                evaluated-value
                (p15-s23-stage2-runtime-nontail-value!
                 plan
                 (p15-s23-stage2-runtime-execute-instruction
                  runtime plan env value)
                 :recur-inside-map-value)]
            (recur (inc index)
                   (assoc! result evaluated-key evaluated-value)))
          (persistent! result))))
    (loop [remaining (seq entries)
           result (transient {})]
      (if (seq remaining)
        (let [{:keys [key value]} (first remaining)
              evaluated-key
              (p15-s23-stage2-runtime-nontail-value!
               plan
               (p15-s23-stage2-runtime-execute-instruction
                runtime plan env key)
               :recur-inside-map-key)
              evaluated-value
              (p15-s23-stage2-runtime-nontail-value!
               plan
               (p15-s23-stage2-runtime-execute-instruction
                runtime plan env value)
               :recur-inside-map-value)]
          (recur (next remaining)
                 (assoc! result evaluated-key evaluated-value)))
        (persistent! result)))))

(defn p15-s23-stage2-runtime-execute-instructions
  [runtime plan env instructions]
  (if (vector? instructions)
    ;; Compiler plans use vectors. Indexing avoids constructing a chunked seq
    ;; and calling `next` for every interpreted function body.
    (let [instruction-count (count instructions)]
      (loop [index 0
             result nil]
        (if (< index instruction-count)
          (let [value (p15-s23-stage2-runtime-execute-instruction
                       runtime plan env (nth instructions index))
                more? (< (inc index) instruction-count)]
            (if (and (p15-s23-stage2-runtime-recur-signal? value) more?)
              (p15-s23-stage2-runtime-recur-fail!
               plan nil (count (p15-s23-stage2-runtime-recur-values value))
               :non-tail-sequential-position)
              (recur (inc index) value)))
          result)))
    ;; Retain the established behavior for direct callers that supply lists.
    (loop [remaining (seq instructions)
           result nil]
      (if-let [instruction (first remaining)]
        (let [value (p15-s23-stage2-runtime-execute-instruction
                     runtime plan env instruction)
              more (next remaining)]
          (if (and (p15-s23-stage2-runtime-recur-signal? value) more)
            (p15-s23-stage2-runtime-recur-fail!
             plan nil (count (p15-s23-stage2-runtime-recur-values value))
             :non-tail-sequential-position)
            (recur more value)))
        result))))

(defn- p15-s23-stage2-runtime-execute-generic-builtin-call
  [runtime plan env instruction function]
  (let [args (p15-s23-stage2-runtime-execute-values
              runtime plan env (:args instruction)
              :recur-inside-builtin-argument)]
    (if (and (= 'str function)
             (:runtime-artifact-plan runtime))
      (case (count args)
        1 (p15-s23-stage2-runtime-artifact-invoke
           runtime p15-s23-stage2-runtime-artifact-function args)
        2 (p15-s23-stage2-runtime-artifact-invoke
           runtime p15-s23-stage2-runtime-artifact-concat-function args)
        (p15-s23-stage2-runtime-fail-call-arity!
         "L2-BUILTIN-ARITY" plan function args "1 or 2"))
      (p15-s23-stage2-runtime-invoke-builtin plan function args))))