

(defn
 sh06-resolution-product-projections
 [analysis]
 (gravity.resolution/product-projections
  analysis
  (resolution-stage-operations)))

(defn
 sh06-resolution-identity-chunks
 [values]
 (gravity.resolution/identity-chunks
  values
  (resolution-stage-operations)))

(defn-
 sh06-resolution-product-identity-chunks
 [artifact]
 (gravity.resolution/product-identity-chunks
  artifact
  (resolution-stage-operations)))

(defn-
 sh06-resolution-artifact-identity-input-with-chunks
 [artifact chunks]
 (gravity.resolution/artifact-identity-input-with-chunks
  artifact
  chunks
  (resolution-stage-operations)))

(defn
 sh06-resolution-artifact-identity-input
 [artifact]
 (gravity.resolution/artifact-identity-input
  artifact
  (resolution-stage-operations)))

(defn
 sh06-resolution-artifact-id
 [artifact]
 (gravity.resolution/artifact-id
  artifact
  (resolution-stage-operations)))

(defn-
 sh06-resolution-envelope-summary-with-chunks
 [artifact chunks]
 (gravity.resolution/envelope-summary-with-chunks artifact chunks))

(defn
 sh06-resolution-envelope-summary
 [artifact]
 (gravity.resolution/envelope-summary
  artifact
  (resolution-stage-operations)))

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
     :schema-version 2
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
      (get (:public-function-hashes binding)
           'sh06-build-resolution-template)
      :function-shapes (:public-function-shapes binding)}
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
      :invocation-root (System/getProperty "user.dir")}}))

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

(defn sh06-resolution-boundary-keys-for [boundary]
  (if (= :gravity/sh06-ordinary-module-resolution-request
         (get-in boundary [:authenticated-resolution-request :artifact]))
    (conj sh06-resolution-boundary-keys :module-inputs)
    sh06-resolution-boundary-keys))
