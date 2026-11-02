(ns gravity.pass-execution.config
  "Closed schemas, bounds, authority ordering, and facade contract data.")

(def maximum-depth 64)
(def maximum-nodes 16384)
(def maximum-canonical-bytes (* 4 1024 1024))
(def maximum-integer-bits (* 3 maximum-canonical-bytes))
(def maximum-evidence-records 4096)
(def maximum-dag-receipts 1024)
(def sha256-pattern #"sha256:[0-9a-f]{64}")

(def pass-contract-fields
  #{:pass :version :order :input :output :requires :preserves :invalidates
    :regenerates :replacement-evidence :emits :effects :capabilities :profiles
    :required-evidence :verifier-required? :authority-ceiling})

(def semantic-binding-fields
  #{:compiler-id :capability-policy-id :facet-set-id :provider-manifest-id
    :package-lock-id :diagnostic-schema-id})

(def provenance-fields #{:provenance-id :source-path :metadata})
(def request-authority-fields #{:input-authorities :claimed-level :scope})
(def external-root-fields #{:kind :facts})

(def execution-request-fields
  #{:stage :contract :producer-binding-id :input-artifact-ids :input-facts
    :external-root-inputs :semantic-bindings :dependency-graph-id
    :build-effect-replay-id :profile-id :target-id :policy-ids :provenance
    :diagnostic-stream-id :execution-mode :authority})

(def execute-operation-fields
  #{:produce! :validate-output! :artifact-id-of :verifier-reports
    :evidence-records})

(def receipt-validation-operation-fields
  #{:validate-diagnostic-stream! :validate-verifier-report!
    :validate-evidence-record!})

(def verifier-report-fields #{:verifier-id :stage :artifact-id :status})
(def evidence-record-fields
  #{:evidence-id :kind :status :artifact-id :authority-level})

(def receipt-authority-fields
  #{:input-authorities :claimed-level :effective-level :ceiling :scope
    :authority-contribution? :aggregate-authoritative?})

(def receipt-fields
  #{:artifact :schema-version :receipt-id :stage :pass-contract-id
    :producer-binding-id :input-artifact-ids :output-artifact-id
    :external-root-inputs :input-facts :output-facts :requires :preserves
    :invalidates :regenerates :replacement-evidence :effects
    :semantic-bindings :dependency-graph-id :build-effect-replay-id :profile-id
    :target-id :policy-ids :provenance :diagnostic-stream-id :verifier-reports
    :evidence-records :execution-mode :authority})

(def authority-rank
  {:none 0 :non-authoritative 1 :reviewed 2 :authoritative 3})

(def evidence-dag-fields
  #{:artifact :schema-version :root-receipt-id :receipts :contracts :edges
    :authority :evidence-root-id})

(def evidence-dag-authority-fields
  #{:effective-level :authority-contribution? :aggregate-authoritative?})

(def public-api
  {'pass-execution-contract {:arglists '([])}
   'canonical-pass-contract {:arglists '([contract])}
   'pass-contract-id {:arglists '([contract])}
   'validate-pass-contract! {:arglists '([contract])}
   'execute-pass! {:arglists '([request operations])}
   'validate-execution-receipt! {:arglists '([receipt contract operations])}
   'compose-evidence-dag {:arglists '([receipts contracts])}
   'evidence-root {:arglists '([dag])}})

(def namespace-contract
  {:namespace 'gravity.pass-execution
   :contract-boundary :hosted-stage0-pass-execution-receipts-v1
   :public-api public-api
   :owns [:bounded-canonical-pass-contract-identity :pass-execution-receipts
          :fact-flow-validation :evidence-dag-composition
          :authority-monotonicity]
   :dependency-direction
   {:requires ['clojure.core 'clojure.set 'gravity.digest]
    :forbids ['gravity.bootstrap 'gravity.c2-pass-cache
              'gravity.c16-incremental]}
   :compatibility-only? true
   :authoritative? false
   :cache-storage? false
   :pass-implementation? false
   :proof-authority? false
   :release-authority? false
   :self-hosting-authority? false
   :aggregate-authority? false
   :digest-is-signature? false
   :semantic-ordering
   {:integers :type-sensitive-integral-tags
    :input-artifact-ids :lexical-sha256
    :policy-ids :lexical-sha256
    :pass-order [:declared-order :pass-contract-id]}})
