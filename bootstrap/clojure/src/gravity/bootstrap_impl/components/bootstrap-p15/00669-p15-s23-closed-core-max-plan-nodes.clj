

;; ---------------------------------------------------------------------------
;; P15-S23 closed checked-core ingress (C6-C10 bounded migration slice)
;;
;; This is deliberately not MIR yet.  It authenticates the exact target-neutral
;; stage2 packet against trusted source bytes, rebuilds the authoritative C2/C3
;; products from those bytes, and emits a content-addressed checked-core/fact
;; sidecar for the entrypoint-only closed runtime language.  Raw C2/C3 ids and
;; actual paths are provenance, never semantic identity inputs.

(def p15-s23-closed-core-max-plan-nodes 128)
(def p15-s23-closed-core-max-plan-depth 128)
(def p15-s23-closed-core-max-derived-nodes 384)
(def p15-s23-closed-core-max-source-bytes (* 1024 1024))
(def p15-s23-closed-core-max-artifact-scalar-bytes (* 8 1024 1024))
(def p15-s23-closed-core-max-integer-bits 256)
(def p15-s23-closed-core-max-serialized-values
  (* p15-s23-closed-core-max-derived-nodes
     (inc p15-s23-closed-core-max-plan-depth)
     8))

(defn p15-s23-closed-core-bounded-utf8-count
  [^String value maximum-bytes]
  (let [length (.length value)]
    (if (> length maximum-bytes)
      {:status :over-limit :bytes (inc maximum-bytes)}
      (loop [idx 0
             bytes 0]
        (if (< idx length)
          (let [code (int (.charAt value idx))]
            (cond
              (<= 0xD800 code 0xDBFF)
              (if (and (< (inc idx) length)
                       (let [low (int (.charAt value (inc idx)))]
                         (<= 0xDC00 low 0xDFFF)))
                (let [next-bytes (+ bytes 4)]
                  (if (> next-bytes maximum-bytes)
                    {:status :over-limit :bytes next-bytes}
                    (recur (+ idx 2) next-bytes)))
                {:status :invalid-surrogate :index idx :bytes bytes})

              (<= 0xDC00 code 0xDFFF)
              {:status :invalid-surrogate :index idx :bytes bytes}

              :else
              (let [width (cond
                            (<= code 0x7F) 1
                            (<= code 0x7FF) 2
                            :else 3)
                    next-bytes (+ bytes width)]
                (if (> next-bytes maximum-bytes)
                  {:status :over-limit :bytes next-bytes}
                  (recur (inc idx) next-bytes)))))
          {:status :valid :bytes bytes})))))

(def p15-s23-closed-core-allowed-operations
  #{:literal :quote :local :do :if :let})

(def p15-s23-closed-core-recognized-plan-operations
  (conj p15-s23-closed-core-allowed-operations :builtin-call :println))

(def p15-s23-closed-core-recognized-core-operations
  (conj p15-s23-closed-core-allowed-operations :str :println))

(def p15-s23-closed-core-allowed-safety-outcomes
  #{:proven-safe :runtime-checked})

(def p15-s23-closed-core-known-effects
  #{:io/write :memory/allocate})

(def p15-s23-closed-core-known-capabilities
  #{:io/stdout :memory/allocator})

(declare p15-s23-closed-core-fail!)

(def p15-s23-closed-core-registered-effects
  ;; Registration and inference are separate.  The global L6 registry is the
  ;; recognition boundary, with the canon-confirmed legacy-only panic label
  ;; removed; this slice infers only the two labels above.
  (disj (set (keys effect-registry)) :panic/fail))

(def p15-s23-closed-core-registered-capabilities
  (set/union (set (keys provider-specs))
             (set (vals c8-effect-capability))
             p15-s23-closed-core-known-capabilities))

(defn p15-s23-closed-core-observed-plan-operations
  "Return the operations reachable in one concrete closed plan.

  This is deliberately distinct from the generic runtime validator's
  supported-operation inventory: an accepted pure artifact may use the
  runtime only as a seed-comparison oracle and must not imply that latent
  effectful executor branches conform to C8."
  [plan]
  (loop [pending
         (vec (mapcat :instructions (vals (:functions plan))))
         operations #{}
         visited 0]
    (if-let [instruction (peek pending)]
      (let [pending (pop pending)
            visited (inc visited)]
        (when-not (and (map? instruction)
                       (keyword? (:op instruction))
                       (<= visited p15-s23-closed-core-max-plan-nodes))
          (p15-s23-closed-core-fail!
           "C6-VERIFY" (or (get-in plan [:source :path]) "<closed-plan>")
           instruction
           {:missing-fact :bounded-concrete-plan-operation-inventory
            :observed-plan-nodes visited
            :maximum-plan-nodes p15-s23-closed-core-max-plan-nodes}))
        (recur (into pending (c-backend-instruction-children instruction))
               (conj operations (:op instruction))
               visited))
      operations)))

(defn p15-s23-closed-core-preflight-effect-requirements
  "Infer the recognized C8 residual requirements from a concrete plan
  without executing it or loading the runtime module."
  [plan]
  (loop [pending (vec (mapcat :instructions (vals (:functions plan))))
         effects #{}
         capabilities #{}
         visited 0]
    (if-let [instruction (peek pending)]
      (let [pending (pop pending)
            visited (inc visited)
            operation (:op instruction)
            str-call? (and (= :builtin-call operation)
                           (= 'str (:function instruction)))
            effects (cond-> effects
                      str-call? (conj :memory/allocate)
                      (= :println operation) (conj :io/write))
            capabilities (cond-> capabilities
                           str-call? (conj :memory/allocator)
                           (= :println operation) (conj :io/stdout))]
        (when-not (and (map? instruction)
                       (keyword? operation)
                       (<= visited p15-s23-closed-core-max-plan-nodes))
          (p15-s23-closed-core-fail!
           "C6-VERIFY" (or (get-in plan [:source :path]) "<closed-plan>")
           instruction
           {:missing-fact :bounded-preflight-effect-requirement-inventory
            :observed-plan-nodes visited
            :maximum-plan-nodes p15-s23-closed-core-max-plan-nodes}))
        (recur (into pending (c-backend-instruction-children instruction))
               effects capabilities visited))
      {:required-effects effects
       :required-capabilities capabilities})))

(defn p15-s23-closed-core-scope-contract
  ([]
  {:accepted-pure-operations
   (vec (sort p15-s23-closed-core-allowed-operations))
   :structural-node-kinds [:binding :function :truthiness]
   :recognized-but-rejected-pending-runtime-module [:println :str]
   :module {:profile :hosted
            :safety :safe
            :source-target :jvm
            :top-level-shape [:ns :single-zero-arity-main]
            :exports [#{} #{'main}]
            :requires :empty
            :imports :empty
            :providers :empty
            :metadata {}
            :doc nil}
   :literal-and-quote-types
   [:nil :string :bool :integer :char :keyword :symbol]
   :required-effects #{}
   :required-capabilities #{}
   :recognized-effectful-residual-labels
   p15-s23-closed-core-known-effects
   :recognized-but-uncredited-capabilities
   p15-s23-closed-core-known-capabilities
   :bounds {:maximum-plan-nodes p15-s23-closed-core-max-plan-nodes
            :maximum-plan-depth p15-s23-closed-core-max-plan-depth
            :maximum-derived-nodes p15-s23-closed-core-max-derived-nodes
            :maximum-source-bytes p15-s23-closed-core-max-source-bytes
            :maximum-artifact-scalar-bytes
            p15-s23-closed-core-max-artifact-scalar-bytes
            :maximum-integer-bits p15-s23-closed-core-max-integer-bits
            :maximum-serialized-values
            p15-s23-closed-core-max-serialized-values}
   :entrypoint-only? true
   :complete-for-pure-closed-slice? true
   :whole-language? false
   :mir-derived? false
   :c11-claimed? false
   :target-lowering-credit? false
   :release-credit? false
   :self-hosted? false})
  ([mode]
   (p15-s23-closed-core-scope-contract
    mode {:required-effects #{} :required-capabilities #{}}))
  ([mode {:keys [required-effects required-capabilities]}]
   (if (= :effectful-reference mode)
     (-> (p15-s23-closed-core-scope-contract)
         (dissoc :recognized-but-uncredited-capabilities)
         (assoc :recognized-but-rejected-pending-runtime-module []
                :required-effects required-effects
                :required-capabilities required-capabilities
                :accepted-reference-effects
                p15-s23-closed-core-known-effects
                :accepted-reference-capabilities
                p15-s23-closed-core-known-capabilities
                :complete-for-pure-closed-slice? false
                :accepted-reference-operations [:println :str]
                :reference-interpreter? true
                :single-authoritative-construction-invocation? true
                :verification-replay-is-separate-runtime-evaluation? true
                :complete-for-pinned-r1-r11-verification-replay-slice? true
                :r1-whole-conformance? false
                :r11-whole-conformance? false
                :deployment-runtime? false
                :live-external-io? false))
     (p15-s23-closed-core-scope-contract))))