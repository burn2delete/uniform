(ns gravity.syntax-object-stream
  "Hosted stage0 projection from reader records to syntax records.

  This leaf preserves the Clojure seed's record shape and ordering only. It is
  not the canonical C2 reader or C3 syntax-object authority and does not own
  source authentication, macro expansion, or hygiene semantics.")

(def ^:private namespace-contract
  {:namespace 'gravity.syntax-object-stream
   :contract-boundary :hosted-reader-to-syntax-record-projection
   :public-api
   {'syntax-object-stream
    {:arglists '([source-path form-records]
                 [source-path form-records module-context])
     :returns :ordered-hosted-syntax-records}}
   :artifact-inputs [:source-path :reader-form-records :module-context]
   :artifact-outputs [:hosted-syntax-record-stream]
   :ownership
   {:owns [:hosted-reader-to-syntax-record-projection]
    :does-not-own [:canonical-c2-reader-authority
                   :canonical-c3-syntax-object-authority
                   :source-reading
                   :source-authentication
                   :macro-expansion
                   :hygiene-semantics
                   :diagnostic-construction
                   :bootstrap-orchestration]}
   :dependency-direction
   {:requires ['clojure.core]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :test-owner
   'gravity.syntax-object-stream-test/syntax-object-stream-contract-is-narrow-and-nonauthoritative
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-c2-authority? false
   :canonical-c3-authority? false
   :self-hosted? false})

(defn syntax-object-stream
  "Project reader form records into the hosted stage0 syntax-record shape.

  `source-path` is retained for bootstrap API compatibility but intentionally
  does not affect this projection. The two-argument form uses nil module
  context, preserving always-present namespace and profile keys with nil
  values. A form-id key is copied only when it is present on the input record,
  including when its value is nil."
  ([source-path form-records]
   (syntax-object-stream source-path form-records nil))
  ([source-path form-records module-context]
   (mapv (fn [idx {:keys [form span metadata reader-origin generated-origin]
                   :as record}]
           (cond->
            {:syntax-id (str "stage0-syntax-" idx)
             :form form
             :span span
             :origin :source
             :reader-origin reader-origin
             :generated-origin generated-origin
             :namespace (:module module-context)
             :phase :read
             :profile (:profile module-context)
             :hygiene []
             :metadata metadata}
             (contains? record :form-id) (assoc :form-id (:form-id record))))
         (range)
         form-records)))
