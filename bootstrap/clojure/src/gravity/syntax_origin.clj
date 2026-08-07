(ns gravity.syntax-origin
  "Hosted stage0 construction of reader-derived syntax origin chains.

  This leaf preserves the Clojure seed's origin-record projection only. It is
  not canonical C3 syntax authority and does not own source authentication,
  macro provenance, hygiene semantics, diagnostic policy, or orchestration.")

(def ^:private namespace-contract
  {:namespace 'gravity.syntax-origin
   :contract-boundary :hosted-reader-derived-syntax-origin-projection
   :public-api
   {'c3-origin-chain
    {:arglists '([seed source-unit])
     :returns :ordered-hosted-syntax-origin-chain}}
   :artifact-inputs [:hosted-syntax-seed :hosted-source-unit-record]
   :artifact-outputs [:hosted-reader-derived-syntax-origin-chain]
   :ownership
   {:owns [:hosted-reader-derived-syntax-origin-projection]
    :does-not-own [:canonical-c3-syntax-object-authority
                   :source-reading
                   :source-authentication
                   :macro-expansion
                   :macro-provenance
                   :hygiene-semantics
                   :diagnostic-construction
                   :bootstrap-orchestration
                   :self-hosted-authority]}
   :dependency-direction
   {:requires ['clojure.core]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-c3-authority? false
   :self-hosted? false})

(defn c3-origin-chain
  "Project a hosted syntax seed and source-unit record into the stage0 origin
  chain shape, preserving reader abbreviation order and source references."
  [seed source-unit]
  (let [source-entry {:kind :source
                      :producer {:kind :reader
                                 :name 'gravity.stage0/reader
                                 :version "stage0"}
                      :source-id (get source-unit :source-id)
                      :span (:span seed)
                      :input-syntax-ids []
                      :reason :source-read
                      :build-effects []}
        generated (mapv (fn [origin]
                          {:kind :generated
                           :producer {:kind :reader
                                      :name 'gravity.stage0/reader-abbreviation
                                      :version "stage0"}
                           :inputs [(:syntax-id seed)]
                           :generated-span (str "generated:reader:"
                                                (name (or (:reader-abbreviation origin)
                                                          :abbreviation))
                                                ":"
                                                (get-in seed [:span :form-index]))
                           :source-span (:from origin)
                           :reason (:reader-abbreviation origin)
                           :build-effects []})
                        (:generated-origin seed))]
    (vec (cons source-entry generated))))
