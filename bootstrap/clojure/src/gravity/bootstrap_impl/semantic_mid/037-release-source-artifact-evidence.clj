(defn- semantic-mid-release-source-artifact-evidence
  [{:keys [source-path stage1-bootstrap-artifact stage1-records trace-value
           comparison self-hosted-runtime core-bootstrap-runtime
           core-bootstrap-builtins compiler-driver runtime-entrypoint
           runtime-image boot-chain diverse-verification release-attestation
           source-runtime character-stream token-classifier token-realizer
           token-automaton token-automaton-executor form-builder
           form-builder-executor token-stream]}]
  {:stage1-bootstrap-source-artifact stage1-bootstrap-artifact
   :stage1-reader-release-attestation-seed-retirement release-attestation
   :stage1-reader-diverse-bootstrap-verification diverse-verification
   :stage1-reader-verified-boot-chain boot-chain
   :stage1-reader-runtime-image runtime-image
   :stage1-reader-runtime-entrypoint runtime-entrypoint
   :stage1-reader-compiler-driver compiler-driver
   :stage1-reader-core-bootstrap-runtime core-bootstrap-runtime
   :stage1-reader-core-bootstrap-builtins core-bootstrap-builtins
   :stage1-reader-self-hosted-runtime self-hosted-runtime
   :stage1-reader-source-runtime source-runtime
   :stage1-reader-character-stream character-stream
   :stage1-reader-token-classifier token-classifier
   :stage1-reader-token-realizer token-realizer
   :stage1-reader-token-automaton token-automaton
   :stage1-reader-token-automaton-executor token-automaton-executor
   :stage1-reader-form-builder form-builder
   :stage1-reader-form-builder-executor form-builder-executor
   :stage1-reader-token-stream token-stream
   :stage1-reader-records stage1-records
   :stage1-reader-release-attestation-seed-retirement-trace
   (dissoc trace-value :character-stream :token-stream
           :token-classifier :token-realizer :token-automaton
           :token-automaton-executor :form-builder :form-builder-executor
           :source-runtime :self-hosted-reader-runtime
           :core-bootstrap-runtime :core-bootstrap-builtins
           :compiler-driver :runtime-entrypoint :runtime-image
           :verified-boot-chain :diverse-bootstrap-verification
           :release-attestation-seed-retirement)
   :stage0-comparison comparison
   :accepted-stage1-reader-release-attestation-seed-retirement-fixtures
   [{:fixture
     "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
     :status :accepted
     :comparison comparison
     :character-count (:character-count character-stream)
     :token-count (:token-count token-stream)
     :form-count (count stage1-records)
     :release-attestation-seed-retirement-id
     (:release-attestation-seed-retirement-id release-attestation)}]
   :rejected-stage1-reader-release-attestation-seed-retirement-fixtures
   stage1-reader-release-attestation-seed-retirement-rejected-fixture-records
   :stage1-reader-release-attestation-seed-retirement-diagnostic-stream
   (stage1-reader-release-attestation-seed-retirement-diagnostic-stream
    source-path
    (:release-attestation-seed-retirement-id release-attestation))
   :stage1-reader-release-attestation-seed-retirement-results
   {:accepted-fixtures 1
    :rejected-fixtures
    (count stage1-reader-release-attestation-seed-retirement-rejected-fixture-records)
    :diagnostic-count
    (+ (count stage1-reader-release-attestation-seed-retirement-diagnostic-ids)
       (dec (count stage1-reader-execution-diagnostic-ids)))
    :character-count (:character-count character-stream)
    :token-count (:token-count token-stream)
    :form-count (count stage1-records)
    :status :complete}
   :diagnostics []})

(defn- semantic-mid-release-source-artifact-base
  [context]
  (merge (semantic-mid-release-source-artifact-identity context)
         (semantic-mid-release-source-artifact-evidence context)))
