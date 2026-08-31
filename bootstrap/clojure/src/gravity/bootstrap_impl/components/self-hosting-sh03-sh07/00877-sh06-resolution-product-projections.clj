

(defn sh06-resolution-product-projections
  [analysis]
  (let [compatibility-resolutions
        (mapv #(assoc % :resolution-kind
                      (sh06-resolution-order-compatibility
                       (:resolution-order %)))
              (:resolution-table analysis))]
    {:namespace-analysis
     (assoc analysis :artifact :gravity/namespace-analysis)
     :binding-table
     {:artifact :gravity/c5-binding-table
      :status :complete
      :bindings (:binding-table analysis)
      :resolution-table compatibility-resolutions}
     :alias-table
     {:artifact :gravity/c5-alias-table
      :status :complete :aliases (:alias-table analysis)}
     :import-export-table
     {:artifact :gravity/c5-import-export-table
      :status :complete
      :imports (:dependency-records analysis)
      :exports (:exports analysis)}
     :lexical-scope-graph
     {:artifact :gravity/c5-lexical-scope-graph
      :status :complete :scopes (:lexical-scope-graph analysis)}
     :dependency-graph
     (assoc (:dependency-graph analysis)
            :artifact :gravity/c5-module-dependency-graph
            :status :complete)
     :cross-profile-edge-report
     {:artifact :gravity/c5-cross-profile-edge-report
      :status :complete
      :records (:cross-profile-edge-report analysis)}
     :incremental-invalidation-keys
     {:artifact :gravity/c5-incremental-invalidation-keys
      :status :stable
      :inputs (:incremental-invalidation-inputs analysis)
      :invalidation-id
      (reader-canonical-hash
       {:domain :gravity/sh06-invalidation-identity-v1
        :inputs (:incremental-invalidation-inputs analysis)})}
     :resolution-table compatibility-resolutions}))

(defn sh06-resolution-identity-chunks
  [values]
  (mapv
   (fn [chunk-index chunk]
     {:chunk-index chunk-index
      :entry-count (count chunk)
      :chunk-id
      (reader-canonical-hash
       {:domain :gravity/sh06-resolution-identity-chunk-v1
        :chunk-index chunk-index :entries (vec chunk)})})
   (range)
   (partition-all 16 values)))

(defn sh06-resolution-artifact-identity-input
  [artifact]
  {:domain :gravity/sh06-resolution-artifact-v1
   :kind (:kind artifact)
   :slice (:slice artifact)
   :task (:task artifact)
   :adapter-contract
   (get-in artifact [:gravity-resolution-boundary :adapter-contract])
   :analysis-artifact-id
   (get-in artifact [:gravity-resolution-boundary :resolved-analysis
                     :artifact-id])
   :source-revision-id
   (get-in artifact [:gravity-resolution-boundary
                     :authenticated-resolution-request :module
                     :source-revision-id])
   :upstream-artifact-id
   (get-in artifact [:sh05-macro-artifact :artifact-id])
   :upstream-syntax-stream-id
   (get-in artifact [:sh05-macro-artifact :expanded-syntax-stream-id])
   :upstream-trace-id
   (get-in artifact [:sh05-macro-artifact :macro-expansion-trace-id])
   :binding-id-chunks
   (sh06-resolution-identity-chunks
    (mapv :binding-id (get-in artifact [:binding-table :bindings])))
   :resolution-chunks
   (sh06-resolution-identity-chunks
    (mapv #(dissoc % :source-span) (:resolution-table artifact)))
   :dependency-graph-id
   (reader-canonical-hash
    {:domain :gravity/sh06-dependency-graph-v1
     :nodes (get-in artifact [:dependency-graph :nodes])
     :edges (mapv #(dissoc % :source-span)
                  (get-in artifact [:dependency-graph :edges]))})
   :invalidation-id
   (get-in artifact [:incremental-invalidation-keys :invalidation-id])})

(defn sh06-resolution-artifact-id
  [artifact]
  (reader-canonical-hash
   (sh06-resolution-artifact-identity-input artifact)))

(defn sh06-resolution-envelope-summary
  [artifact]
  {:slice :SH-06
   :artifact-id (:artifact-id artifact)
   :analysis-artifact-id
   (get-in artifact [:gravity-resolution-boundary :resolved-analysis
                     :artifact-id])
   :upstream-artifact-id (get-in artifact [:sh05-macro-artifact :artifact-id])
   :source-revision-id
   (get-in artifact [:gravity-resolution-boundary
                     :authenticated-resolution-request :module
                     :source-revision-id])
   :binding-count (count (get-in artifact [:binding-table :bindings]))
   :reference-count (count (:resolution-table artifact))
   :dependency-edge-count (count (get-in artifact [:dependency-graph :edges]))
   :binding-id-chunks
   (sh06-resolution-identity-chunks
    (mapv :binding-id (get-in artifact [:binding-table :bindings])))
   :resolution-chunks
   (sh06-resolution-identity-chunks
    (mapv #(dissoc % :source-span) (:resolution-table artifact)))})

(defn sh06-resolution-sh02-descriptor
  [source-path binding summary]
  (let [projection-name :resolution-product-identities
        fact-name :resolution-product-binding
        identity-name :resolution-result
        identity-domain :gravity/sh06-resolution-result-identity-v1
        evidence-id
        (p15-s23-c6c10-canonical-digest
         source-path
         {:domain :gravity/sh06-resolution-envelope-evidence-v1
          :summary summary
          :plan-semantic-hash (:plan-semantic-hash binding)})
        identity-preimage {:summary summary}
        observed-id
        (p15-s23-c6c10-canonical-digest
         source-path {:domain identity-domain
                      :semantic-input identity-preimage})
        fact-value {:family fact-name :entries [summary]}
        sealed-artifact-id
        (p15-s23-c6c10-canonical-digest
         source-path {:resolution-result (:artifact-id summary)})]
    {:artifact :gravity/private-authenticated-envelope-descriptor
     :schema-version 1
     :stage sh06-resolution-envelope-stage
     :artifact-kind sh06-resolution-sealed-artifact-kind
     :source-revision
     {:owner :sh06-resolution
      :source-language :gravity
      :logical-source-path sh06-resolution-source-relative-path
      :source-content-hash (:source-content-hash binding)
      :source-byte-count (:source-byte-count binding)
      :plan-semantic-hash (:plan-semantic-hash binding)
      :functions-semantic-hash (:functions-semantic-hash binding)
      :builder-function 'sh06-build-resolution-template
      :builder-semantic-hash
      (get sh06-resolution-public-function-hashes
           'sh06-build-resolution-template)
      :function-shapes sh06-resolution-public-function-shapes}
     :projection-contract
     {:contract-kind :gravity/sh06-resolution-product-envelope-contract
      :contract-version 1 :profile :meta :target :jvm
      :required-semantic-projections [projection-name]
      :required-fact-families [fact-name]
      :required-identity-subjects [identity-name]}
     :semantic-projections
     [{:name projection-name :role :complete-resolution-product-identities
       :entry-count (count summary) :value summary}]
     :fact-transitions
     [{:name fact-name :disposition :preserved
       :input fact-value :output fact-value
       :input-count (count fact-value)
       :output-count (count fact-value)
       :evidence-ids [evidence-id]}]
     :effect-capability-relation
     {:effect-facts {:declared #{} :observed #{}}
      :capability-facts {:required #{} :granted #{}}
      :capability-proof-facts {:proof-ids [evidence-id]}
      :effect-order [] :provider-selections [] :grant-scopes []}
     :proof-composite
     {:proof-records [{:proof-id evidence-id :status :checked}]
      :proof-certificate-table {evidence-id {:status :checked}}
      :proof-summary {:required 1 :checked 1}
      :proof-usage [{:proof-id evidence-id :used-by :resolution-products}]}
     :preservation
     {:requires [fact-name] :preserves [fact-name]
      :invalidates [] :regenerates []
      :residual-checks [:identity-subject-equality
                        :digest-graph-reachability]}
     :identity-subjects
     [{:name identity-name :domain identity-domain
       :preimage identity-preimage :observed-id observed-id}]
     :lineage
     [{:stage :sh06-resolution
       :artifact-kind :gravity/sh06-resolution-artifact
       :semantic-id (:artifact-id summary)
       :artifact-id sealed-artifact-id
       :verification-id evidence-id
       :relation :produced-from-gravity-resolution}]
     :reference-closure
     {:root-id "sh06-resolution-result"
      :node-ids ["sh06-resolution-result"]
      :edges [] :fact-reference-ids [evidence-id]
      :origin-reference-ids [] :proof-reference-ids [evidence-id]
      :runtime-check-reference-ids [] :observed-node-count 1
      :observed-edge-count 0 :observed-maximum-depth 0}
     :actual-path-provenance
     {:source-path source-path
      :workspace-root (System/getProperty "user.dir")
      :invocation-root (System/getProperty "user.dir")}
     :bounds p15-s23-sh02-authenticated-envelope-bounds}))

(def sh06-resolution-artifact-keys
  #{:kind :status :slice :task :document-set :governing-document
    :artifact-id :sh05-macro-artifact :gravity-resolution-boundary
    :provenance :pass :execution-boundary :capability-based-proof
    :diagnostics :namespace-analysis :binding-table :alias-table
    :import-export-table :lexical-scope-graph :dependency-graph
    :cross-profile-edge-report :incremental-invalidation-keys
    :resolution-table})

(def sh06-resolution-boundary-keys
  #{:slice :owner :adapter-contract :plan-binding
    :authenticated-resolution-request :raw-template-result :raw-analysis
    :resolved-analysis :digest-requests :resolved-digests
    :template-verification :resolved-verification
    :authenticated-envelope-descriptor :authenticated-envelope
    :target-source-reread? :clojure-adapter-residual? :self-hosted?})