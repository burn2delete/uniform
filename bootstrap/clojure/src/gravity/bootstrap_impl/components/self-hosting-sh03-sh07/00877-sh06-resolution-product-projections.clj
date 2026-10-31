

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

(defn
 sh06-resolution-sh02-descriptor
 [source-path binding summary]
 (gravity.resolution/envelope-descriptor
  source-path
  binding
  summary
  (resolution-stage-operations)))

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

(defn
 sh06-resolution-boundary-keys-for
 [boundary]
 (gravity.resolution/boundary-keys-for
  boundary
  (resolution-stage-operations)))
