(ns gravity.macro-expansion.engine
  (:require [gravity.digest :as digest]
            [gravity.macro-expansion.operations :as operations]
            [gravity.macro-expansion.policy :as policy]))

(defn- expansion-digests
  [input output ops]
  (let [hash-operation (operations/operation ops :sha256-hex digest/sha256-hex)
        hash (if (var? hash-operation) @hash-operation hash-operation)]
    {:input-hash (str "sha256:" (hash (pr-str input)))
     :output-hash (str "sha256:" (hash (pr-str output)))}))

(def ^:private ^:dynamic *expansion-digest-context* nil)

(defn- expansion-digest-context
  [input output ops]
  (let [hash-operation (operations/operation ops :sha256-hex digest/sha256-hex)]
    {:input input
     :output output
     :hash-op (if (var? hash-operation) @hash-operation hash-operation)
     :digests (volatile! nil)}))

(defn- expansion-digests-for-context
  [input output ops]
  (let [hash-operation (operations/operation ops :sha256-hex digest/sha256-hex)
        hash-op (if (var? hash-operation) @hash-operation hash-operation)
        context *expansion-digest-context*]
    (if (and (map? context)
             (= input (:input context))
             (= output (:output context))
             (identical? hash-op (:hash-op context)))
      (or @(:digests context)
          (let [digests (expansion-digests input output ops)]
            (vreset! (:digests context) digests)
            digests))
      (expansion-digests input output ops))))

(defn- generated-origin-with-digests
  [macro syntax digests]
  (when-not (:omit-generated-origin? macro)
    [{:from (:span syntax)
      :macro (:identity macro)
      :macro-version (:version macro)
      :input-hash (:input-hash digests)
      :output-hash (:output-hash digests)}]))

(defn generated-origin
  [macro syntax input output ops]
  (when-not (:omit-generated-origin? macro)
    (generated-origin-with-digests
     macro syntax (expansion-digests-for-context input output ops))))

(defn macro-call
  [registry form]
  (when (seq? form)
    (get registry (first form))))

(declare expand-syntax-object)

(defn expand-child-form
  [registry module syntax form trace depth ops]
  (let [expand-syntax
        (operations/operation
         ops :expand-syntax-object
         (fn [r m syn tr d]
           (expand-syntax-object r m syn tr d ops)))]
    (:form (expand-syntax registry module (assoc syntax :form form)
                          trace depth))))

(defn expand-form-children
  [registry module syntax form trace depth ops]
  (let [expand-child
        (operations/operation
         ops :expand-child-form
         (fn [r m syn value tr d]
           (expand-child-form r m syn value tr d ops)))]
    (cond
      (seq? form)
      (apply list (map #(expand-child registry module syntax % trace depth)
                       form))
      (vector? form)
      (vec (map #(expand-child registry module syntax % trace depth) form))
      (map? form)
      (into {} (map (fn [[k v]]
                      [(expand-child registry module syntax k trace depth)
                       (expand-child registry module syntax v trace depth)])
                    form))
      (set? form)
      (set (map #(expand-child registry module syntax % trace depth) form))
      :else form)))

(defn- trace-record-with-digests
  [_module macro syntax generated-origin depth digests]
  {:macro (:identity macro)
   :macro-version (:version macro)
   :macro-namespace (:macro-namespace macro)
   :caller-namespace (:namespace syntax)
   :caller-profile (:profile syntax)
   :depth depth
   :call-span (:span syntax)
   :input-syntax-id (:syntax-id syntax)
   :input-hash (:input-hash digests)
   :output-hash (:output-hash digests)
   :build-effects (vec (sort-by str (or (:uses-build-effects macro)
                                        (:build-effects macro)
                                        #{})))
   :generated-origin generated-origin
   :generated-spans [(str "generated:" (:identity macro) ":"
                          (get-in syntax [:span :form-index]))]
   :hygiene-policy (:hygiene-policy macro)
   :hygiene-marks (:hygiene syntax)
   :metadata (:metadata syntax)
   :diagnostics []})

(defn trace-record
  [module macro syntax input output generated-origin depth ops]
  (trace-record-with-digests
   module macro syntax generated-origin depth
   (expansion-digests-for-context input output ops)))

(defn distinct-by-pr-str
  [values]
  (vec (vals (reduce (fn [acc value]
                       (assoc acc (pr-str value) value))
                     {}
                     values))))

(defn expand-syntax-object
  [registry module initial-syntax trace initial-depth ops]
  (let [form-op (operations/operation ops :form-op? operations/form-op?)
        macro-call-op (operations/operation ops :macro-call macro-call)
        expand-macro
        (operations/operation
         ops :expand-macro-form
         (fn [m mac args span]
           (policy/expand-macro-form m mac args span ops)))
        origin-record-op (get ops :expansion-generated-origin)
        trace-record-op (get ops :expansion-trace-record)
        expand-children
        (operations/operation
         ops :expand-form-children
         (fn [r m syn value tr depth]
           (expand-form-children r m syn value tr depth ops)))]
    (loop [syntax initial-syntax
           depth initial-depth
           seen-inputs #{}]
      (let [form (:form syntax)]
        (cond
          (form-op 'defmacro form)
          (assoc syntax :phase :macro-definition)

          (macro-call-op registry form)
          (let [input-key (binding [*print-meta* true
                                    *print-readably* true
                                    *print-length* nil
                                    *print-level* nil]
                            (pr-str form))]
            (when (contains? seen-inputs input-key)
              (operations/fail!
               ops "L4-EXPANSION-DEPTH"
               "macro expansion repeated a non-progressing form"
               {:source-span (:span syntax)
                :form form
                :depth depth
                :reason :recursive-expansion-cycle
                :remediation
                "Change the recursive macro so each expansion makes semantic progress."}))
            (let [macro (macro-call-op registry form)
                  input form
                  output (expand-macro module macro (vec (rest form))
                                       (:span syntax))
                  digest-context (expansion-digest-context input output ops)
                  [origin digests]
                  (binding [*expansion-digest-context* digest-context]
                    (if origin-record-op
                      [(origin-record-op macro syntax input output) nil]
                      (let [digests (when-not (:omit-generated-origin? macro)
                                      (expansion-digests-for-context
                                       input output ops))]
                        [(generated-origin-with-digests macro syntax digests)
                         digests])))]
              (when (empty? origin)
                (operations/fail!
                 ops "L4-PROVENANCE-MISSING"
                 "macro expansion output lacks generated-origin metadata"
                 {:source-span (:span syntax)
                  :macro (:identity macro)
                  :remediation
                  "Attach generated-origin metadata linking output syntax to the macro call site."}))
              (let [record
                    (binding [*expansion-digest-context* digest-context]
                      (let [digests (or digests
                                        (when (nil? trace-record-op)
                                          (expansion-digests-for-context
                                           input output ops)))]
                        (if trace-record-op
                          (trace-record-op module macro syntax input output origin
                                           depth)
                          (trace-record-with-digests
                           module macro syntax origin depth digests))))]
                (swap! trace conj record)
                (recur (-> syntax
                           (assoc :form output
                                  :origin :generated
                                  :phase :macro-expanded
                                  :generated-origin
                                  (vec (concat (:generated-origin syntax) origin))
                                  :macro-namespace (:macro-namespace macro))
                           (update :hygiene conj
                                   {:macro (:identity macro)
                                    :policy (:hygiene-policy macro)}))
                       (inc depth)
                       (conj seen-inputs input-key)))))

          (coll? form)
          (assoc syntax
                 :form (expand-children registry module syntax form trace depth)
                 :phase :macro-expanded)

          :else
          (assoc syntax :phase :macro-expanded))))))
