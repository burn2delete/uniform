

(def ^:private c2-pass-cache-compiler-contract
  {:implementation :gravity-stage0-clojure-bootstrap
   :implementation-contract-version 1
   :c2-artifact-schema-version 1
   :c2-artifact-identity-version 1
   :canonical-reader :gravity-sh03-reader
   :adapter-contract :gravity/sh03-to-c2-reader-products-v2
   :clojure-adapter-residual? true
   :self-hosted? false
   :release-authority? false})

(def ^:private c2-pass-cache-pass-contract
  {:name :c2-reader-document-coverage
   :input :source-bytes
   :output :reader-document-proof
   :requires [:source-unit :reader-policy]
   :preserves [:source-spans :raw-literal-facts
               :reader-origin :trivia :diagnostics]
   :emits [:source-unit-record :token-stream :form-tree
           :syntax-seed-stream :reader-source-map
           :literal-decoding-records
           :reader-extension-invocation-records
           :reader-diagnostics :incremental-reader-hash]
   :rejects c2-reader-diagnostic-ids})

(def ^:private c2-pass-cache-boundary-contract
  {:slice :SH-03
   :owner :gravity-source
   :adapter-contract :gravity/sh03-to-c2-reader-products-v2
   :plan-binding-contract :exact-current-sh03-plan-binding
   :semantic-value-table-contract
   :authenticated-reader-product-identity-projection
   :authenticated-envelope-contract
   {:stage :c2-reader
    :artifact-kind :gravity/sh03-reader-products
    :verification :fresh-sh02-descriptor-envelope-reconstruction}
   :target-source-reread? false
   :uncredited-source-models
   {:status :not-executed
    :entrypoints sh03-reader-uncredited-source-model-entrypoints
    :self-hosting-credit? false
    :seed-retirement-credit? false
    :release-credit? false}
   :clojure-adapter-residual? true
   :self-hosted? false})

(def ^:private c2-pass-cache-sh03-semantic-binding-fields
  [:artifact :status :semantic-authority :compiled-by :executed-by
   :generic-bridge-residual? :self-hosted?
   :source-byte-count :source-content-hash :plan-id
   :plan-semantic-hash :functions-semantic-hash :function-count
   :function-names-hash :function-shapes-hash
   :entrypoint-semantic-hash :verifier-semantic-hash
   :builtin-functions-hash :instruction-summary])

(defn- c2-pass-cache-current-binding!
  [source-path]
  (let [exact-sh03-binding
        (dissoc (sh03-reader-current-binding! source-path) :plan)
        sh03-semantic-binding
        (select-keys exact-sh03-binding
                     c2-pass-cache-sh03-semantic-binding-fields)
        sh03-binding-id
        (c2-pass-cache/canonical-content-id
         {:domain :gravity/c2-pass-cache-sh03-binding-v1
          :binding sh03-semantic-binding})
        compiler-input
        (assoc c2-pass-cache-compiler-contract
               :sh03-binding-id sh03-binding-id
               :sh03-binding sh03-semantic-binding)
        compiler-id
        (c2-pass-cache/canonical-content-id
         {:domain :gravity/c2-pass-cache-compiler-binding-v1
          :compiler compiler-input})
        pass-contract-id
        (c2-pass-cache/canonical-content-id
         {:domain :gravity/c2-pass-cache-pass-contract-v1
          :pass c2-pass-cache-pass-contract})
        plan-binding-id
        (c2-pass-cache/canonical-content-id
         {:domain :gravity/c2-pass-cache-exact-sh03-plan-binding-v1
          :binding exact-sh03-binding})
        semantic-value-table-contract-id
        (c2-pass-cache/canonical-content-id
         {:domain :gravity/c2-pass-cache-semantic-value-table-contract-v1
          :contract
          (:semantic-value-table-contract c2-pass-cache-boundary-contract)})
        authenticated-envelope-contract-id
        (c2-pass-cache/canonical-content-id
         {:domain :gravity/c2-pass-cache-authenticated-envelope-contract-v1
          :contract
          (:authenticated-envelope-contract c2-pass-cache-boundary-contract)})
        boundary-binding-base
        (assoc c2-pass-cache-boundary-contract
               :plan-binding-id plan-binding-id
               :semantic-value-table-contract-id
               semantic-value-table-contract-id
               :authenticated-envelope-contract-id
               authenticated-envelope-contract-id)
        boundary-binding
        (assoc boundary-binding-base
               :identity
               (c2-pass-cache/canonical-content-id
                {:domain :gravity/c2-pass-cache-boundary-binding-v1
                 :binding boundary-binding-base}))
        compiler-binding (assoc compiler-input :compiler-id compiler-id)
        pass-binding {:pass :c2-reader
                      :pass-contract c2-pass-cache-pass-contract
                      :pass-contract-id pass-contract-id}
        entry-binding
        {:artifact :gravity/c2-pass-cache-producer-binding
         :schema-version 1
         :compiler-id compiler-id
         :pass-contract-id pass-contract-id
         :sh03-binding-id sh03-binding-id
         :boundary-binding-id (:identity boundary-binding)
         :exact-sh03-plan-binding exact-sh03-binding
         :adapter-contract :gravity/sh03-to-c2-reader-products-v2
         :clojure-adapter-residual? true
         :self-hosted? false
         :release-authority? false}]
    {:compiler-binding compiler-binding
     :pass-binding pass-binding
     :boundary-binding boundary-binding
     :entry-binding entry-binding
     :exact-sh03-binding exact-sh03-binding}))

(defn- c2-pass-cache-key-context!
  [source-path snapshot current-binding]
  (let [source-bytes (:bytes snapshot)
        source-text
        (sh03-reader-strict-source-text! source-path source-path source-bytes)
        project-context (reader-project-context-for-source source-path)
        source-unit (c2-source-unit-record source-path source-text
                                           standard-reader-options
                                           project-context)
        dependency-binding
        {:dependencies :not-consumed-at-c2
         :project-root-id (:project-root-id project-context)
         :identity
         (c2-pass-cache/canonical-content-id
          {:domain :gravity/c2-pass-cache-dependency-binding-v1
           :stage :c2-reader
           :project-root-id (:project-root-id project-context)
           :dependencies :not-consumed-at-c2})}
        build-effect-input
        {:ambient-authority :denied
         :registered-tags (:registered-tags standard-reader-policy)
         :build-effects #{}}
        build-effect-binding
        (assoc build-effect-input
               :identity
               (c2-pass-cache/canonical-content-id
                {:domain :gravity/c2-pass-cache-build-effect-binding-v1
                 :binding build-effect-input}))
        capability-input {:capabilities #{}
                          :ambient-authority :denied}
        capability-binding
        (assoc capability-input
               :identity
               (c2-pass-cache/canonical-content-id
                {:domain :gravity/c2-pass-cache-capability-binding-v1
                 :binding capability-input}))
        facet-input {:facets (:enabled-features standard-reader-options)}
        facet-binding
        (assoc facet-input
               :identity
               (c2-pass-cache/canonical-content-id
                {:domain :gravity/c2-pass-cache-facet-binding-v1
                 :binding facet-input}))
        key
        (c2-pass-cache/cache-key
         {:source-unit
          (select-keys source-unit
                       [:source-id :bytes-hash :reader-options
                        :identity-inputs])
          :source-snapshot
          (select-keys snapshot
                       [:artifact :schema-version :byte-count :bytes-hash
                        :maximum-source-bytes])
          :reader-policy
          {:reader-options standard-reader-options
           :extension-policy (:extension-policy standard-reader-options)
           :standard-reader-policy-id
           (reader-canonical-hash standard-reader-policy)}
          :project-binding
          {:project-root-id (:project-root-id project-context)
           :project-relative-path (:project-relative-path project-context)}
          :compiler-binding (:compiler-binding current-binding)
          :pass-binding (:pass-binding current-binding)
          :dependency-binding dependency-binding
          :build-effect-binding build-effect-binding
          :capability-binding capability-binding
          :facet-binding facet-binding
          :profile-binding {:applicability :not-applicable-at-c2}
          :target-binding {:applicability :not-applicable-at-c2}
          :boundary-binding (:boundary-binding current-binding)
          :path-provenance
          {:canonical-path (:canonical-path snapshot)
           :supplied-path (str source-path)}})]
    {:key key
     :source-text source-text
     :source-unit source-unit
    :project-context project-context}))

(defn- c2-pass-cache-boundary-projection-id
  [artifact]
  (c2-pass-cache/canonical-content-id
   {:domain :gravity/c2-pass-cache-artifact-boundary-projection-v1
    :gravity-reader-boundary (:gravity-reader-boundary artifact)}))