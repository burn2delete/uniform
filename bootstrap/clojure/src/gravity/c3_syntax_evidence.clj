(ns gravity.c3-syntax-evidence
  "Hosted Stage0 projections for C3 syntax schema and evidence ledgers.

  This leaf owns only deterministic projections over an already constructed
  hosted syntax stream. It does not construct or authenticate canonical C3
  syntax objects, establish syntax identity, validate C2 reader products, or
  grant proof, self-hosting, or release authority.")

(def ^:private namespace-contract
  {:namespace 'gravity.c3-syntax-evidence
   :contract-boundary :hosted-c3-syntax-evidence-projection
   :public-api
   {'c3-required-form-kinds {:kind :constant}
    'c3-syntax-schema {:arglists '([] [required-form-kinds])}
    'c3-hygiene-context-map {:arglists '([syntax-stream])}
    'c3-origin-chain-graph {:arglists '([syntax-stream])}
    'c3-metadata-ledger {:arglists '([syntax-stream])}
    'c3-fact-ledger {:arglists '([syntax-stream])}
    'c3-generated-syntax-report {:arglists '([syntax-stream])}}
   :artifact-inputs [:hosted-c3-syntax-stream]
   :artifact-outputs [:hosted-c3-syntax-object-schema
                      :hosted-c3-hygiene-context-map
                      :hosted-c3-origin-chain-graph
                      :hosted-c3-metadata-ledger
                      :hosted-c3-fact-ledger
                      :hosted-c3-generated-syntax-report]
   :ownership
   {:owns [:hosted-c3-syntax-evidence-projection]
    :does-not-own [:canonical-c3-syntax-object-authority
                   :c2-reader-product-authentication
                   :syntax-object-construction
                   :syntax-identity
                   :syntax-serialization
                   :syntax-validation
                   :hygiene-authority
                   :macro-expansion
                   :diagnostic-policy
                   :proof-authority
                   :self-hosted-authority
                   :release-authority]}
   :dependency-direction
   {:requires ['clojure.core]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-c3-authority? false
   :self-hosted? false
   :release-authority? false})

(def c3-required-form-kinds
  [:list :vector :map :set :symbol :keyword :string :character :integer
   :ratio :decimal :boolean :nil :tagged-literal :metadata-wrapper
   :abbreviation-expansion :generated-form])

(defn c3-syntax-schema
  ([]
   (c3-syntax-schema c3-required-form-kinds))
  ([required-form-kinds]
   {:artifact :gravity/syntax-object-schema
    :required-fields [:artifact :syntax/id :form :span :source :namespace
                      :phase :profile :metadata :hygiene :origin :facts
                      :reader-binding :reader-source-revision :ownership
                      :version :prior-syntax-ids :immutable?]
    :form-kinds required-form-kinds
    :identity :content-derived
    :mutation :immutable
    :fact-policy :versioned-and-invalidated}))

(defn c3-hygiene-context-map
  [syntax-stream]
  {:artifact :gravity/hygiene-context-map
   :contexts
   (mapv (fn [syntax]
           {:syntax-id (:syntax/id syntax)
            :marks (get-in syntax [:hygiene :marks])
            :lexical-scopes (get-in syntax [:hygiene :lexical-scopes])
            :renames (get-in syntax [:hygiene :renames])
            :captures (get-in syntax [:hygiene :captures])
            :introduced-identifiers (get-in syntax
                                             [:hygiene :introduced-identifiers])
            :macro-definition-namespace
            (get-in syntax [:hygiene :macro-definition-namespace])
            :macro-call-site-namespace
            (get-in syntax [:hygiene :macro-call-site-namespace])})
         syntax-stream)
   :status :complete})

(defn c3-origin-chain-graph
  [syntax-stream]
  {:artifact :gravity/syntax-origin-chain-graph
   :nodes (mapv (fn [syntax]
                  {:syntax-id (:syntax/id syntax)
                   :origin (:origin syntax)
                   :prior-syntax-ids (:prior-syntax-ids syntax)})
                syntax-stream)
   :status :complete})

(defn c3-metadata-ledger
  [syntax-stream]
  (let [source-metadata (vec (keep (fn [syntax]
                                     (when (and (not= :generated-form
                                                      (get-in syntax
                                                              [:form :kind]))
                                                (seq (:metadata syntax)))
                                       {:syntax-id (:syntax/id syntax)
                                        :action :preserved
                                        :metadata (:metadata syntax)}))
                                   syntax-stream))
        generated (first (filter #(= :generated-form (get-in % [:form :kind]))
                                 syntax-stream))]
    {:artifact :gravity/syntax-metadata-ledger
     :source-metadata source-metadata
     :explicit-changes
     (if generated
       [{:syntax-id (:syntax/id generated)
         :action :explicit-change
         :producer 'compiler.c3/with-capture-demo
         :metadata (:metadata generated)}]
       [])
     :status :complete}))

(defn c3-fact-ledger
  [syntax-stream]
  (let [target (first syntax-stream)
        generated (first (filter #(= :generated-form (get-in % [:form :kind]))
                                 syntax-stream))]
    {:artifact :gravity/syntax-fact-ledger
     :attached [{:syntax-id (:syntax/id target)
                 :fact :declared-profile
                 :value (:profile target)
                 :producer :syntax-object-model
                 :version 1
                 :invalidation-conditions [:macro-expansion
                                           :metadata-change
                                           :namespace-change]}]
     :invalidated [{:syntax-id (:syntax/id target)
                    :successor-syntax-id (:syntax/id generated)
                    :fact :declared-profile
                    :stale-version 1
                    :new-version 2
                    :reason :syntax-transformation
                    :replacement-fact :declared-profile}]
     :status :complete}))

(defn c3-generated-syntax-report
  [syntax-stream]
  {:artifact :gravity/generated-syntax-report
   :generated
   (mapv (fn [syntax]
           {:syntax-id (:syntax/id syntax)
            :producer (get-in syntax [:origin 0 :producer])
            :input-syntax-ids (or (get-in syntax
                                          [:origin 0 :input-syntax-ids])
                                  (get-in syntax [:origin 0 :inputs]))
            :expansion-step 1
            :generated-span (or (get-in syntax [:origin 0 :span])
                                (get-in syntax
                                        [:origin 0 :generated-span]))
            :caller-profile (:profile syntax)
            :hygiene (:hygiene syntax)})
         (filter #(= :generated-form (get-in % [:form :kind])) syntax-stream))
   :status :complete})
