

(defn assert-namespace-effect-and-capability!
  [source-path module inferred-effects]
  (module-analysis-call
   :assert-namespace-effect-and-capability!
   module-analysis/assert-namespace-effect-and-capability!
   source-path module inferred-effects))

(defn parse-module
  [source-path forms]
  (module-analysis-call
   :parse-module module-analysis/parse-module source-path forms))

(defn uses-println?
  [form]
  (module-analysis-call
   :uses-println? module-analysis/uses-println? form))

(defn validate-module-effects!
  [module]
  (module-analysis-call
   :validate-module-effects! module-analysis/validate-module-effects! module))

(defn module-source-artifact-from-records
  [source-path source-text records]
  (module-analysis-call
   :module-source-artifact-from-records
   module-analysis/module-source-artifact-from-records
   source-path source-text records))

(defn module-source-artifact
  [source-path source-text]
  (module-source-artifact-from-records
   source-path source-text
   (read-source-form-records source-path source-text)))

(defn form-op?
  [op form]
  (and (seq? form) (= op (first form))))

(defn contains-form-op?
  [op form]
  (cond
    (form-op? op form) true
    (seq? form) (some #(contains-form-op? op %) form)
    (coll? form) (some #(contains-form-op? op %) form)
    :else false))