(ns gravity.c4-macro-evidence.policy
  "Operation and namespace policy for hosted C4 macro evidence."
  (:require [gravity.digest :as digest]))

(def operation-keys
  #{:sha256-hex :artifact-id-of :max-macro-expansion-depth})

(def namespace-contract
  {:namespace 'gravity.c4-macro-evidence
   :contract-boundary :hosted-c4-macro-evidence-projection
   :public-api
   {'c4-macro-environment {:arglists '([macro-artifact]
                                       [macro-artifact operations])}
    'c4-expansion-input {:arglists '([module c3-artifact macro-artifact]
                                     [module c3-artifact macro-artifact operations])}
    'c4-expanded-syntax-stream {:arglists '([macro-artifact]
                                            [macro-artifact operations])}
    'c4-trace-records {:arglists '([macro-artifact])}
    'c4-hygiene-capture-records {:arglists '([trace-records])}
    'c4-build-effect-log {:arglists '([module trace-records])}
    'c4-macro-safety-declarations {:arglists '([macro-environment])}
    'c4-generated-origin-source-map {:arglists '([trace-records expanded-stream])}
    'c4-expansion-cache-key {:arglists '([expansion-input trace-records]
                                         [expansion-input trace-records operations])}
    'c4-trace-replay-report {:arglists '([trace-records cache-key])}
    'c4-macro-safety-report {:arglists '([trace-records safety-declarations])}}
   :operation-interposition
   {:accepted-keys operation-keys
    :partial-overrides? true
    :unknown-keys-rejected? true}
   :artifact-inputs [:hosted-c3-syntax-artifact :hosted-macro-artifact]
   :artifact-outputs [:hosted-c4-macro-environment
                      :hosted-c4-expansion-input
                      :hosted-c4-expanded-syntax-stream
                      :hosted-c4-expansion-trace
                      :hosted-c4-hygiene-capture-records
                      :hosted-c4-build-effect-log
                      :hosted-c4-macro-safety-records
                      :hosted-c4-generated-origin-map
                      :hosted-c4-expansion-cache-key
                      :hosted-c4-trace-replay-report]
   :ownership
   {:owns [:hosted-c4-macro-evidence-projection]
    :does-not-own [:canonical-c4-macro-expansion-authority
                   :macro-execution
                   :c3-input-authentication
                   :hygiene-authority
                   :build-effect-authorization
                   :trace-replay-execution
                   :cache-storage
                   :cache-hit-validation
                   :diagnostic-policy
                   :proof-authority
                   :self-hosted-authority
                   :release-authority]}
   :dependency-direction
   {:requires ['clojure.core 'clojure.set 'clojure.string 'gravity.digest]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-c4-authority? false
   :self-hosted? false
   :release-authority? false})

(def default-operations
  {:sha256-hex digest/sha256-hex
   :artifact-id-of (fn [value]
                     (str "sha256:" (digest/sha256-hex (pr-str value))))
   :max-macro-expansion-depth 16})

(defn operations [overrides]
  (when-not (map? overrides)
    (throw (ex-info "C4 macro evidence operations must be a map"
                    {:operations overrides})))
  (let [unknown (seq (remove operation-keys (keys overrides)))
        merged (merge default-operations overrides)]
    (when unknown
      (throw (ex-info "C4 macro evidence operations contain unknown keys"
                      {:unknown-keys (vec unknown)})))
    (doseq [key [:sha256-hex :artifact-id-of]]
      (when-not (fn? (get merged key))
        (throw (ex-info "C4 macro evidence function operation is invalid"
                        {:operation key :value (get merged key)}))))
    (when-not (and (integer? (:max-macro-expansion-depth merged))
                   (pos? (:max-macro-expansion-depth merged)))
      (throw (ex-info "C4 macro expansion depth must be a positive integer"
                      {:operation :max-macro-expansion-depth
                       :value (:max-macro-expansion-depth merged)})))
    merged))
