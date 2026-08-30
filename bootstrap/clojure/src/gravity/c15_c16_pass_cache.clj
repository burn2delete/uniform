(ns gravity.c15-c16-pass-cache
  "Non-authoritative generic-v2 cache integration for the adjacent C15 and
  C16 Stage0 passes.

  Callers supply the real pass producers, validators, and artifact identity
  function.  This namespace owns only the exact C16 request projection, cache
  orchestration, and composition of the two producer receipts.  It cannot
  establish compiler, diagnostic, proof, release, or self-hosting authority."
  (:require [clojure.edn :as edn]
            [gravity.pass-cache :as pass-cache]
            [gravity.pass-execution :as pass-execution]
            [gravity.c15-c16-pass-cache.request :as request-builder]))

(def ^:private sha256-pattern #"sha256:[0-9a-f]{64}")

(def ^:private context-fields
  #{:c14-artifact-id :semantic-bindings :dependency-graph-id
    :build-effect-replay-id :profile-id :target-id :policy-ids :provenance
    :diagnostic-stream-ids :producer-binding-ids :validation-binding-ids
    :authority-scope})

(def ^:private operation-fields
  #{:produce-c15! :validate-c15! :produce-c16! :validate-c16!
    :artifact-id-of})

(def ^:private maximum-envelope-characters (* 8 1024 1024))
(def ^:private envelope-fields
  #{:artifact :schema-version :stage :artifact-id :payload-edn})

(def ^:private stage-binding-fields #{:c15 :c16})

(def ^:private c15-input-facts
  #{:source-spans :origin-chain :profile-context :target-context
    :lowering-artifact :provenance :proofs :unstructured-diagnostics})

(def ^:private c15-output-facts
  #{:source-spans :origin-chain :profile-context :target-context
    :lowering-artifact :provenance :proofs :diagnostic-stream})

(def ^:private c15-pass-contract
  {:pass :c15-compiler-diagnostics
   :version "stage0-c15-cache-v1"
   :order 15
   :input :gravity/stage0-c14-target-lowering-artifact
   :output :gravity/stage0-c15-compiler-diagnostics-artifact
   :requires #{:lowering-artifact :source-spans :profile-context
               :target-context :unstructured-diagnostics}
   :preserves #{:source-spans :origin-chain :profile-context :target-context
                :lowering-artifact :provenance :proofs}
   :invalidates #{:unstructured-diagnostics}
   :regenerates #{:diagnostic-stream}
   :replacement-evidence
   {:unstructured-diagnostics :diagnostic-schema}
   :emits #{:diagnostic-stream :diagnostic-schema :diagnostic-catalog
            :diagnostic-verification-report}
   :effects #{}
   :capabilities #{}
   :profiles #{:hosted}
   :required-evidence #{}
   :verifier-required? false
   :authority-ceiling :none})

(def ^:private c16-pass-contract
  {:pass :c16-incremental-compilation
   :version "stage0-c16-cache-v1"
   :order 16
   :input :gravity/stage0-c15-compiler-diagnostics-artifact
   :output :gravity/stage0-c16-incremental-compilation-artifact
   :requires #{:diagnostic-stream :source-spans :profile-context
               :target-context :provenance :proofs}
   :preserves c15-output-facts
   :invalidates #{}
   :regenerates #{:cache-key-schema :invalidation-trace
                  :revalidation-report}
   :replacement-evidence {}
   :emits #{:incremental-dependency-graph :cache-key-schema
            :cache-entry-manifest :invalidation-trace
            :artifact-reuse-report :revalidation-report}
   :effects #{}
   :capabilities #{}
   :profiles #{:hosted}
   :required-evidence #{}
   :verifier-required? false
   :authority-ceiling :none})

(def ^:private public-api
  {'c15-c16-pass-cache-contract {:arglists '([])}
   'c15-stage-request {:arglists '([context])}
   'c16-stage-request {:arglists '([context c15-artifact-id])}
   'lookup-or-compute! {:arglists '([store context operations])}})

(def ^:private namespace-contract
  {:namespace 'gravity.c15-c16-pass-cache
   :contract-boundary :hosted-stage0-c15-c16-generic-v2-cache-integration
   :public-api public-api
   :owns [:exact-c15-c16-c16-invalidator-projection
          :adjacent-pass-cache-orchestration
          :c15-to-c16-receipt-edge
          :two-pass-evidence-root]
   :does-not-own [:c15-pass-semantics :c16-pass-semantics
                  :artifact-identity-policy :diagnostic-policy
                  :compiler-authority :proof-authority :release-authority
                  :equivalence-authority :self-hosting-authority
                  :cache-storage-implementation]
   :dependency-direction
   {:requires ['clojure.core 'clojure.edn
               'gravity.pass-cache 'gravity.pass-execution]
    :forbids ['gravity.bootstrap 'gravity.c15-diagnostics
              'gravity.c16-incremental]}
   :authority {:ceiling :none
               :local-development-only? true
               :speculative-only? true
               :authoritative? false
               :proof? false
               :release? false
               :equivalence? false
               :self-hosting? false}
   :pass-contracts [c15-pass-contract c16-pass-contract]
   :c16-invalidator-fields
   [:c14-artifact-id :compiler-id :capability-policy-id :facet-set-id
    :provider-manifest-id :package-lock-id :diagnostic-schema-id
    :dependency-graph-id :build-effect-replay-id :profile-id :target-id
    :policy-ids :provenance-id :producer-binding-id
    :validation-binding-id :authority-scope]})

(defn- fail!
  [id message data]
  (throw (ex-info message
                  (merge {:id id
                          :stage :c15-c16-pass-cache
                          :release-authority? false
                          :proof-authority? false
                          :self-hosting-authority? false}
                         data))))

(defn- sha256-id?
  [value]
  (and (string? value) (boolean (re-matches sha256-pattern value))))

(defn- require-sha256!
  [field value]
  (when-not (sha256-id? value)
    (fail! "C16-KEY" "C15/C16 cache identity must be lowercase SHA-256"
           {:field field :value value}))
  value)

(defn- exact-map!
  [value expected field]
  (when-not (and (map? value) (= expected (set (keys value))))
    (fail! "C16-KEY" "C15/C16 cache input has unknown or missing fields"
           {:field field :expected expected
            :observed (when (map? value) (set (keys value)))}))
  value)

(defn- validate-stage-bindings!
  [field bindings]
  (exact-map! bindings stage-binding-fields field)
  (doseq [[stage value] bindings]
    (require-sha256! [field stage] value))
  bindings)

(defn- validate-context!
  [context]
  (exact-map! context context-fields :context)
  (require-sha256! :c14-artifact-id (:c14-artifact-id context))
  (exact-map! (:semantic-bindings context)
              #{:compiler-id :capability-policy-id :facet-set-id
                :provider-manifest-id :package-lock-id :diagnostic-schema-id}
              :semantic-bindings)
  (doseq [[field value] (:semantic-bindings context)]
    (require-sha256! field value))
  (doseq [field [:dependency-graph-id :build-effect-replay-id
                 :profile-id :target-id]]
    (require-sha256! field (get context field)))
  (when-not (and (vector? (:policy-ids context))
                 (= (:policy-ids context) (vec (sort (:policy-ids context))))
                 (= (count (:policy-ids context))
                    (count (distinct (:policy-ids context))))
                 (every? sha256-id? (:policy-ids context)))
    (fail! "C16-KEY" "C15/C16 policy identities must be sorted and unique"
           {:policy-ids (:policy-ids context)}))
  (validate-stage-bindings! :diagnostic-stream-ids
                            (:diagnostic-stream-ids context))
  (validate-stage-bindings! :producer-binding-ids
                            (:producer-binding-ids context))
  (validate-stage-bindings! :validation-binding-ids
                            (:validation-binding-ids context))
  (when-not (keyword? (:authority-scope context))
    (fail! "C16-POLICY" "C15/C16 authority scope must be a keyword"
           {:authority-scope (:authority-scope context)}))
  ;; The generic key validator owns the exact provenance schema and canonical
  ;; bounds.  It is intentionally exercised below before a request is exposed.
  context)

(defn c15-stage-request
  "Build and validate the exact C15 generic-pass request."
  [context]
  (request-builder/build {:validate-context! validate-context!
                          :require-sha256! require-sha256!
                          :stage-cache-key pass-cache/stage-cache-key}
                         context :c15 c15-pass-contract
                         (:c14-artifact-id context) c15-input-facts true))

(defn c16-stage-request
  "Build and validate the exact C16 request consuming one C15 artifact.

  The C15 input is deliberately not an external root: when both receipts are
  composed, it must resolve to the C15 producer and form a typed internal edge."
  [context c15-artifact-id]
  (request-builder/build {:validate-context! validate-context!
                          :require-sha256! require-sha256!
                          :stage-cache-key pass-cache/stage-cache-key}
                         context :c16 c16-pass-contract c15-artifact-id
                         c15-output-facts false))

(defn- validate-operations!
  [operations]
  (exact-map! operations operation-fields :operations)
  (doseq [[field operation] operations]
    (when-not (fn? operation)
      (fail! "C16-ENTRY" "C15/C16 cache operation must be a function"
             {:field field})))
  operations)

(defn- envelope!
  [stage artifact operations]
  (let [artifact-id ((:artifact-id-of operations) artifact)
        _ (require-sha256! :artifact-id artifact-id)
        payload (pr-str artifact)]
    (when (> (count payload) maximum-envelope-characters)
      (fail! "C16-ENTRY" "C15/C16 artifact envelope exceeds its local bound"
             {:stage stage
              :maximum-characters maximum-envelope-characters}))
    {:artifact :gravity/c15-c16-pass-cache-envelope
     :schema-version 1
     :stage stage
     :artifact-id artifact-id
     :payload-edn payload}))

(defn- decode-envelope!
  [stage envelope validate operations]
  (when-not (and (map? envelope)
                 (= envelope-fields (set (keys envelope)))
                 (= :gravity/c15-c16-pass-cache-envelope
                    (:artifact envelope))
                 (= 1 (:schema-version envelope))
                 (= stage (:stage envelope))
                 (sha256-id? (:artifact-id envelope))
                 (string? (:payload-edn envelope))
                 (<= (count (:payload-edn envelope))
                     maximum-envelope-characters))
    (fail! "C16-ENTRY" "C15/C16 cache envelope is malformed"
           {:stage stage}))
  (let [artifact
        (try
          (edn/read-string
           {:readers {}
            :default (fn [tag _]
                       (fail! "C16-ENTRY"
                              "C15/C16 cache envelope contains an unknown tag"
                              {:stage stage :tag tag}))}
           (:payload-edn envelope))
          (catch clojure.lang.ExceptionInfo error (throw error))
          (catch Throwable error
            (fail! "C16-ENTRY" "C15/C16 cache envelope EDN is malformed"
                   {:stage stage
                    :host-error (.getName (class error))})))
        observed ((:artifact-id-of operations) artifact)]
    (when-not (= observed (:artifact-id envelope))
      (fail! "C16-STALE" "C15/C16 envelope artifact identity is stale"
             {:stage stage :expected (:artifact-id envelope)
              :observed observed}))
    (validate artifact)
    artifact))

(defn- stage-cache-operations
  [context stage produce validate operations]
  {:produce! (fn [_] (envelope! stage (produce) operations))
   :validate-output!
   (fn [envelope _ _]
     (decode-envelope! stage envelope validate operations)
     envelope)
   :artifact-id-of
   (fn [envelope]
     (require-sha256! :artifact-id (:artifact-id envelope)))
   :validation-binding-id (get-in context [:validation-binding-ids stage])
   :verifier-reports (fn [& _] [])
   :evidence-records
   (fn [envelope _ _]
     (if (= :c15 stage)
       [{:evidence-id (get-in context [:diagnostic-stream-ids :c15])
         :kind :diagnostic-schema
         :status :accepted
         :artifact-id (:artifact-id envelope)
         :authority-level :none}]
       []))
   :validate-diagnostic-stream!
   (fn [stream-id receipt]
     (when-not (and (= stream-id (get-in context [:diagnostic-stream-ids stage]))
                    (= stream-id (:diagnostic-stream-id receipt)))
       (fail! "C16-DIAGNOSTIC"
              "C15/C16 receipt diagnostic stream binding is stale"
              {:pass stage})))
   :validate-verifier-report!
   (fn [& _]
     (fail! "C18-EVIDENCE"
            "C15/C16 compatibility cache admits no verifier reports" {}))
   :validate-evidence-record!
   (fn [record receipt]
     (when-not
      (and (= :c15 stage)
           (= {:evidence-id (get-in context [:diagnostic-stream-ids :c15])
               :kind :diagnostic-schema
               :status :accepted
               :artifact-id (:output-artifact-id receipt)
               :authority-level :none}
              record))
       (fail! "C18-EVIDENCE"
              "C15 replacement evidence differs from its current binding"
              {:pass stage})))})

(defn lookup-or-compute!
  "Reuse or execute the adjacent C15 and C16 passes and compose their receipts.

  The returned evidence DAG is explicitly non-authoritative.  C16 consumes the
  exact C15 artifact id, so a changed C15 output invalidates the C16 key and the
  composed DAG contains one real, typed internal edge."
  [store context operations]
  (let [context (validate-context! context)
        operations (validate-operations! operations)
        c15-request (c15-stage-request context)
        c15-key (pass-cache/stage-cache-key c15-request)
        c15-operations
        (stage-cache-operations context :c15 (:produce-c15! operations)
                                (:validate-c15! operations) operations)
        c15-result
        (pass-cache/lookup-or-compute! store c15-key c15-request
                                       c15-operations)
        c15-artifact
        (decode-envelope! :c15 (:artifact c15-result)
                          (:validate-c15! operations) operations)
        c15-artifact-id ((:artifact-id-of operations) c15-artifact)
        c16-request (c16-stage-request context c15-artifact-id)
        c16-key (pass-cache/stage-cache-key c16-request)
        c16-operations
        (stage-cache-operations
         context :c16
         #((:produce-c16! operations) c15-artifact)
         (:validate-c16! operations) operations)
        c16-result
        (pass-cache/lookup-or-compute! store c16-key c16-request
                                       c16-operations)
        receipts [(:producer-receipt c15-result)
                  (:producer-receipt c16-result)]
        contracts [c15-pass-contract c16-pass-contract]
        evidence-dag (pass-execution/compose-evidence-dag receipts contracts)]
    {:artifact :gravity/c15-c16-pass-cache-result
     :schema-version 1
     :c15-artifact c15-artifact
     :c16-artifact
     (decode-envelope! :c16 (:artifact c16-result)
                       (:validate-c16! operations) operations)
     :c15-cache-evidence (:cache-evidence c15-result)
     :c16-cache-evidence (:cache-evidence c16-result)
     :c15-producer-receipt (first receipts)
     :c16-producer-receipt (second receipts)
     :evidence-dag evidence-dag
     :evidence-root-id (pass-execution/evidence-root evidence-dag)
     :authority :none
     :release-authority? false
     :proof-authority? false
     :self-hosting-authority? false}))

(defn c15-c16-pass-cache-contract
  "Return the exact non-authoritative adapter contract."
  []
  namespace-contract)
