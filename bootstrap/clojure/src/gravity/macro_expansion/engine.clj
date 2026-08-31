(ns gravity.macro-expansion.engine
  (:require [gravity.digest :as digest]
            [gravity.macro-expansion.operations :as operations]
            [gravity.macro-expansion.policy :as policy]))

(defn generated-origin
  [macro syntax input output ops]
  (let [hash (operations/operation ops :sha256-hex digest/sha256-hex)]
    (when-not (:omit-generated-origin? macro)
      [{:from (:span syntax)
        :macro (:identity macro)
        :macro-version (:version macro)
        :input-hash (str "sha256:" (hash (pr-str input)))
        :output-hash (str "sha256:" (hash (pr-str output)))}])))

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

(defn trace-record
  [module macro syntax input output generated-origin depth ops]
  (let [hash (operations/operation ops :sha256-hex digest/sha256-hex)]
    {:macro (:identity macro)
     :macro-version (:version macro)
     :macro-namespace (:macro-namespace macro)
     :caller-namespace (:namespace syntax)
     :caller-profile (:profile syntax)
     :depth depth
     :call-span (:span syntax)
     :input-syntax-id (:syntax-id syntax)
     :input-hash (str "sha256:" (hash (pr-str input)))
     :output-hash (str "sha256:" (hash (pr-str output)))
     :build-effects (vec (sort-by str (or (:uses-build-effects macro)
                                           (:build-effects macro)
                                           #{})))
     :generated-origin generated-origin
     :generated-spans [(str "generated:" (:identity macro) ":"
                            (get-in syntax [:span :form-index]))]
     :hygiene-policy (:hygiene-policy macro)
     :hygiene-marks (:hygiene syntax)
     :metadata (:metadata syntax)
     :diagnostics []}))

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
        origin-record
        (operations/operation
         ops :expansion-generated-origin
         (fn [mac syn input output]
           (generated-origin mac syn input output ops)))
        trace-record-op
        (operations/operation
         ops :expansion-trace-record
         (fn [m mac syn input output origin depth]
           (trace-record m mac syn input output origin depth ops)))
        expand-children
        (operations/operation
         ops :expand-form-children
         (fn [r m syn value tr depth]
           (expand-form-children r m syn value tr depth ops)))
        max-depth (:max-macro-expansion-depth ops 16)]
    (loop [syntax initial-syntax
           depth initial-depth]
      (let [form (:form syntax)]
        (cond
          (form-op 'defmacro form)
          (assoc syntax :phase :macro-definition)

          (macro-call-op registry form)
          (do
            (when (>= depth max-depth)
              (operations/fail!
               ops "L4-EXPANSION-DEPTH"
               "macro expansion exceeded the configured depth limit"
               {:source-span (:span syntax)
                :form form
                :depth depth
                :limit max-depth
                :remediation
                "Stop recursive expansion or raise the project expansion limit with evidence."}))
            (let [macro (macro-call-op registry form)
                  input form
                  output (expand-macro module macro (vec (rest form))
                                       (:span syntax))
                  origin (origin-record macro syntax input output)]
              (when (empty? origin)
                (operations/fail!
                 ops "L4-PROVENANCE-MISSING"
                 "macro expansion output lacks generated-origin metadata"
                 {:source-span (:span syntax)
                  :macro (:identity macro)
                  :remediation
                  "Attach generated-origin metadata linking output syntax to the macro call site."}))
              (swap! trace conj
                     (trace-record-op module macro syntax input output origin
                                      depth))
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
                     (inc depth))))

          (coll? form)
          (assoc syntax
                 :form (expand-children registry module syntax form trace depth)
                 :phase :macro-expanded)

          :else
          (assoc syntax :phase :macro-expanded))))))
