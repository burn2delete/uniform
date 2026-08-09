(ns gravity.c17-c18-pass-cache
  "Non-authoritative generic-v2 cache continuation for C17 and C18.

  This adapter accepts the validated C15->C16 cache result, runs or reuses the
  real C17 and C18 producers, and recomposes all four producer receipts into
  one typed evidence DAG.  It owns no plugin, verifier, proof, release, or
  self-hosting authority."
  (:require [clojure.edn :as edn]
            [gravity.pass-cache :as pass-cache]
            [gravity.pass-execution :as pass-execution]))

(def ^:private sha256-pattern #"sha256:[0-9a-f]{64}")
(def ^:private maximum-envelope-characters (* 8 1024 1024))
(def ^:private envelope-fields
  #{:artifact :schema-version :stage :artifact-id :payload-edn})

(def ^:private context-fields
  #{:semantic-bindings :dependency-graph-id :build-effect-replay-id
    :profile-id :target-id :policy-ids :provenance :diagnostic-stream-ids
    :producer-binding-ids :validation-binding-ids :authority-scope})
(def ^:private operation-fields
  #{:produce-c17! :validate-c17! :produce-c18! :validate-c18!
    :artifact-id-of})
(def ^:private stage-binding-fields #{:c17 :c18})

(def ^:private c15-output-facts
  #{:source-spans :origin-chain :profile-context :target-context
    :lowering-artifact :provenance :proofs :diagnostic-stream})
(def ^:private c16-output-facts
  (into c15-output-facts
        #{:cache-key-schema :invalidation-trace :revalidation-report}))
(def ^:private c17-output-facts
  (into c16-output-facts
        #{:plugin-manifest :plugin-api-compatibility :plugin-grants
          :plugin-pass-contracts :plugin-execution-trace
          :plugin-output-verification}))
(def ^:private c18-output-facts
  (into c17-output-facts
        #{:pass-risk-classification :pass-evidence-records
          :translation-validation :compiler-trust-report
          :release-gate-report :counterexample-regressions}))

(def ^:private c17-pass-contract
  {:pass :c17-compiler-plugin
   :version "stage0-c17-cache-v1"
   :order 17
   :input :gravity/stage0-c16-incremental-compilation-artifact
   :output :gravity/stage0-c17-compiler-plugin-artifact
   :requires #{:cache-key-schema :revalidation-report
               :diagnostic-stream :profile-context :target-context}
   :preserves c16-output-facts
   :invalidates #{}
   :regenerates #{:plugin-manifest :plugin-api-compatibility :plugin-grants
                  :plugin-pass-contracts :plugin-execution-trace
                  :plugin-output-verification}
   :replacement-evidence {}
   :emits #{:plugin-manifest :plugin-api-compatibility :plugin-grants
            :plugin-pass-contracts :plugin-execution-trace
            :plugin-output-verification}
   :effects #{}
   :capabilities #{}
   :profiles #{:hosted}
   :required-evidence #{}
   :verifier-required? false
   :authority-ceiling :none})

(def ^:private c18-pass-contract
  {:pass :c18-compiler-verification
   :version "stage0-c18-cache-v1"
   :order 18
   :input :gravity/stage0-c17-compiler-plugin-artifact
   :output :gravity/stage0-c18-compiler-verification-artifact
   :requires #{:plugin-manifest :plugin-pass-contracts
               :plugin-execution-trace :plugin-output-verification}
   :preserves c17-output-facts
   :invalidates #{}
   :regenerates #{:pass-risk-classification :pass-evidence-records
                  :translation-validation :compiler-trust-report
                  :release-gate-report :counterexample-regressions}
   :replacement-evidence {}
   :emits #{:pass-risk-classification :pass-evidence-records
            :translation-validation :compiler-trust-report
            :release-gate-report :counterexample-regressions}
   :effects #{}
   :capabilities #{}
   :profiles #{:hosted}
   :required-evidence #{}
   :verifier-required? false
   :authority-ceiling :none})

(def ^:private public-api
  {'c17-c18-pass-cache-contract {:arglists '([])}
   'c17-stage-request {:arglists '([context upstream-result])}
   'c18-stage-request {:arglists '([context c17-receipt])}
   'lookup-or-compute! {:arglists '([store upstream-result context operations])}})

(def ^:private namespace-contract
  {:namespace 'gravity.c17-c18-pass-cache
   :contract-boundary :hosted-stage0-c17-c18-generic-v2-cache-integration
   :public-api public-api
   :owns [:exact-c17-c18-c16-invalidator-projection
          :upstream-evidence-root-revalidation
          :c16-to-c17-to-c18-receipt-edges
          :four-pass-evidence-root]
   :does-not-own [:c17-pass-semantics :c18-pass-semantics
                  :plugin-loading :sandbox-enforcement
                  :artifact-identity-policy :proof-checking-authority
                  :translation-validation-authority :release-gate-authority
                  :release-authority :self-hosting-authority]
   :dependency-direction
   {:requires ['clojure.core 'clojure.edn
               'gravity.pass-cache 'gravity.pass-execution]
    :forbids ['gravity.bootstrap 'gravity.c17-plugin
              'gravity.c18-verification]}
   :authority {:ceiling :none
               :local-development-only? true
               :speculative-only? true
               :authoritative? false
               :proof? false
               :release? false
               :self-hosting? false}
   :pass-contracts [c17-pass-contract c18-pass-contract]})

(defn- fail!
  [id message data]
  (throw (ex-info message
                  (merge {:id id :stage :c17-c18-pass-cache
                          :authority :none
                          :release-authority? false
                          :proof-authority? false
                          :self-hosting-authority? false}
                         data))))

(defn- sha256-id? [value]
  (and (string? value) (boolean (re-matches sha256-pattern value))))

(defn- require-sha256! [field value]
  (when-not (sha256-id? value)
    (fail! "C16-KEY" "C17/C18 cache identity must be lowercase SHA-256"
           {:field field :value value}))
  value)

(defn- exact-map! [value expected field]
  (when-not (and (map? value) (= expected (set (keys value))))
    (fail! "C16-KEY" "C17/C18 cache input has unknown or missing fields"
           {:field field :expected expected
            :observed (when (map? value) (set (keys value)))}))
  value)

(defn- validate-stage-bindings! [field bindings]
  (exact-map! bindings stage-binding-fields field)
  (doseq [[stage value] bindings]
    (require-sha256! [field stage] value))
  bindings)

(defn- validate-context! [context]
  (exact-map! context context-fields :context)
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
    (fail! "C16-KEY" "C17/C18 policy identities must be sorted and unique"
           {:policy-ids (:policy-ids context)}))
  (validate-stage-bindings! :diagnostic-stream-ids
                            (:diagnostic-stream-ids context))
  (validate-stage-bindings! :producer-binding-ids
                            (:producer-binding-ids context))
  (validate-stage-bindings! :validation-binding-ids
                            (:validation-binding-ids context))
  (when-not (keyword? (:authority-scope context))
    (fail! "C16-POLICY" "C17/C18 authority scope must be a keyword"
           {:authority-scope (:authority-scope context)}))
  context)

(defn- validate-upstream! [upstream-result]
  (let [expected
        #{:artifact :schema-version :c15-artifact :c16-artifact
          :c15-cache-evidence :c16-cache-evidence :c15-producer-receipt
          :c16-producer-receipt :evidence-dag :evidence-root-id :authority
          :release-authority? :proof-authority? :self-hosting-authority?}]
    (exact-map! upstream-result expected :upstream-result)
    (let [dag (:evidence-dag upstream-result)
          observed-root (pass-execution/evidence-root dag)
          receipts (:receipts dag)
          contracts (:contracts dag)
          c16-artifact (:c16-artifact upstream-result)]
      (when-not
       (and (= :gravity/c15-c16-pass-cache-result (:artifact upstream-result))
            (= 1 (:schema-version upstream-result))
            (= :none (:authority upstream-result))
            (false? (:release-authority? upstream-result))
            (false? (:proof-authority? upstream-result))
            (false? (:self-hosting-authority? upstream-result))
            (= observed-root (:evidence-root-id upstream-result))
            (= 2 (count receipts))
            (= 2 (count contracts))
            (= 1 (count (:edges dag)))
            (= (:c15-producer-receipt upstream-result) (first receipts))
            (= (:c16-producer-receipt upstream-result) (second receipts))
            (= (:artifact-id c16-artifact)
               (:output-artifact-id (second receipts)))
            (= c16-output-facts (:output-facts (second receipts)))
            (= :none (get-in dag [:authority :effective-level])))
        (fail! "C18-EVIDENCE"
               "C17/C18 cache upstream evidence boundary is stale"
               {:evidence-root-id (:evidence-root-id upstream-result)})))
    upstream-result))

(defn- request
  [context stage-key contract input-id input-facts]
  (validate-context! context)
  (require-sha256! :input-artifact-id input-id)
  (let [request
        {:stage (:pass contract)
         :contract contract
         :producer-binding-id (get-in context [:producer-binding-ids stage-key])
         :input-artifact-ids [input-id]
         :input-facts input-facts
         :external-root-inputs {}
         :semantic-bindings (:semantic-bindings context)
         :dependency-graph-id (:dependency-graph-id context)
         :build-effect-replay-id (:build-effect-replay-id context)
         :profile-id (:profile-id context)
         :target-id (:target-id context)
         :policy-ids (:policy-ids context)
         :provenance (:provenance context)
         :diagnostic-stream-id (get-in context [:diagnostic-stream-ids stage-key])
         :execution-mode :executed
         :authority {:input-authorities {input-id :none}
                     :claimed-level :none
                     :scope (:authority-scope context)}}]
    (pass-cache/stage-cache-key request)
    request))

(defn c17-stage-request
  "Build the exact C17 request from a validated C15->C16 result."
  [context upstream-result]
  (let [upstream-result (validate-upstream! upstream-result)
        receipt (:c16-producer-receipt upstream-result)]
    (request context :c17 c17-pass-contract
             (:output-artifact-id receipt) (:output-facts receipt))))

(defn c18-stage-request
  "Build the exact C18 request consuming one current C17 receipt."
  [context c17-receipt]
  (let [context (validate-context! context)]
    (when-not (and (map? c17-receipt)
                 (= :gravity/pass-execution-receipt
                    (:artifact c17-receipt))
                 (= 1 (:schema-version c17-receipt))
                 (= :c17-compiler-plugin (:stage c17-receipt))
                 (sha256-id? (:receipt-id c17-receipt))
                 (sha256-id? (:output-artifact-id c17-receipt))
                 (= c17-output-facts (:output-facts c17-receipt))
                 (= :none (get-in c17-receipt
                                  [:authority :effective-level])))
      (fail! "C18-EVIDENCE" "C17 producer receipt boundary is malformed" {}))
    (pass-execution/validate-execution-receipt!
     c17-receipt c17-pass-contract
     {:validate-diagnostic-stream!
      (fn [stream-id receipt]
        (when-not (and (= stream-id (get-in context
                                             [:diagnostic-stream-ids :c17]))
                       (= stream-id (:diagnostic-stream-id receipt)))
          (fail! "C16-DIAGNOSTIC"
                 "C17 receipt diagnostic stream binding is stale" {})))
      :validate-verifier-report!
      (fn [& _]
        (fail! "C18-EVIDENCE" "C17 receipt admits no verifier report" {}))
      :validate-evidence-record!
      (fn [& _]
        (fail! "C18-EVIDENCE" "C17 receipt admits no evidence record" {}))})
    (request context :c18 c18-pass-contract
             (:output-artifact-id c17-receipt) (:output-facts c17-receipt))))

(defn- validate-operations! [operations]
  (exact-map! operations operation-fields :operations)
  (doseq [[field operation] operations]
    (when-not (fn? operation)
      (fail! "C16-ENTRY" "C17/C18 cache operation must be a function"
             {:field field})))
  operations)

(defn- envelope! [stage artifact operations]
  (let [artifact-id ((:artifact-id-of operations) artifact)
        _ (require-sha256! :artifact-id artifact-id)
        payload (pr-str artifact)]
    (when (> (count payload) maximum-envelope-characters)
      (fail! "C16-ENTRY" "C17/C18 artifact envelope exceeds its local bound"
             {:stage stage :maximum-characters maximum-envelope-characters}))
    {:artifact :gravity/c17-c18-pass-cache-envelope
     :schema-version 1 :stage stage :artifact-id artifact-id
     :payload-edn payload}))

(defn- decode-envelope! [stage envelope validate operations]
  (when-not (and (map? envelope)
                 (= envelope-fields (set (keys envelope)))
                 (= :gravity/c17-c18-pass-cache-envelope (:artifact envelope))
                 (= 1 (:schema-version envelope))
                 (= stage (:stage envelope))
                 (sha256-id? (:artifact-id envelope))
                 (string? (:payload-edn envelope))
                 (<= (count (:payload-edn envelope))
                     maximum-envelope-characters))
    (fail! "C16-ENTRY" "C17/C18 cache envelope is malformed" {:stage stage}))
  (let [artifact
        (try
          (edn/read-string
           {:readers {}
            :default (fn [tag _]
                       (fail! "C16-ENTRY"
                              "C17/C18 cache envelope contains an unknown tag"
                              {:stage stage :tag tag}))}
           (:payload-edn envelope))
          (catch clojure.lang.ExceptionInfo error (throw error))
          (catch Throwable error
            (fail! "C16-ENTRY" "C17/C18 cache envelope EDN is malformed"
                   {:stage stage :host-error (.getName (class error))})))
        observed ((:artifact-id-of operations) artifact)]
    (when-not (= observed (:artifact-id envelope))
      (fail! "C16-STALE" "C17/C18 envelope artifact identity is stale"
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
   (fn [envelope] (require-sha256! :artifact-id (:artifact-id envelope)))
   :validation-binding-id (get-in context [:validation-binding-ids stage])
   :verifier-reports (fn [& _] [])
   :evidence-records (fn [& _] [])
   :validate-diagnostic-stream!
   (fn [stream-id receipt]
     (when-not (and (= stream-id (get-in context [:diagnostic-stream-ids stage]))
                    (= stream-id (:diagnostic-stream-id receipt)))
       (fail! "C16-DIAGNOSTIC"
              "C17/C18 receipt diagnostic stream binding is stale"
              {:pass stage})))
   :validate-verifier-report!
   (fn [& _]
     (fail! "C18-EVIDENCE" "C17/C18 cache admits no receipt verifier report" {}))
   :validate-evidence-record!
   (fn [& _]
     (fail! "C18-EVIDENCE" "C17/C18 cache admits no receipt evidence record" {}))})

(defn lookup-or-compute!
  "Run or reuse C17/C18 and compose them with the validated upstream DAG."
  [store upstream-result context operations]
  (let [upstream-result (validate-upstream! upstream-result)
        context (validate-context! context)
        operations (validate-operations! operations)
        c16-artifact (:c16-artifact upstream-result)
        c17-request (c17-stage-request context upstream-result)
        c17-key (pass-cache/stage-cache-key c17-request)
        c17-operations
        (stage-cache-operations
         context :c17 #((:produce-c17! operations) c16-artifact)
         (:validate-c17! operations) operations)
        c17-result
        (pass-cache/lookup-or-compute! store c17-key c17-request c17-operations)
        c17-artifact
        (decode-envelope! :c17 (:artifact c17-result)
                          (:validate-c17! operations) operations)
        c17-receipt (:producer-receipt c17-result)
        c18-request (c18-stage-request context c17-receipt)
        c18-key (pass-cache/stage-cache-key c18-request)
        c18-operations
        (stage-cache-operations
         context :c18 #((:produce-c18! operations) c17-artifact)
         (:validate-c18! operations) operations)
        c18-result
        (pass-cache/lookup-or-compute! store c18-key c18-request c18-operations)
        c18-artifact
        (decode-envelope! :c18 (:artifact c18-result)
                          (:validate-c18! operations) operations)
        upstream-dag (:evidence-dag upstream-result)
        receipts (into (vec (:receipts upstream-dag))
                       [c17-receipt (:producer-receipt c18-result)])
        contracts (into (vec (:contracts upstream-dag))
                        [c17-pass-contract c18-pass-contract])
        evidence-dag (pass-execution/compose-evidence-dag receipts contracts)]
    {:artifact :gravity/c17-c18-pass-cache-result
     :schema-version 1
     :c17-artifact c17-artifact
     :c18-artifact c18-artifact
     :c17-cache-evidence (:cache-evidence c17-result)
     :c18-cache-evidence (:cache-evidence c18-result)
     :c17-producer-receipt c17-receipt
     :c18-producer-receipt (:producer-receipt c18-result)
     :evidence-dag evidence-dag
     :evidence-root-id (pass-execution/evidence-root evidence-dag)
     :authority :none
     :release-authority? false
     :proof-authority? false
     :self-hosting-authority? false}))

(defn c17-c18-pass-cache-contract
  "Return the exact non-authoritative downstream adapter contract."
  []
  namespace-contract)
