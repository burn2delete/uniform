(ns gravity.self-hosting.sh11-authenticated-safety-integration-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh08-authenticated-type-integration-test]
            [gravity.self-hosting.sh09-authenticated-effect-integration-test]
            [gravity.self-hosting.sh10-authenticated-ownership-integration-test]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh11_authenticated_safety_integration_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-11 authenticated integration test is not on the classpath"
        {:id "SH11-AUTH-TEST-SOURCE"})))
    (loop [candidate
           (.getParent
            (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH11-AUTH-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path [relative]
  (str (.resolve @root relative)))

(defn- compile-plan [relative]
  (let [source-path (path relative)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private envelope-source
  "bootstrap/gravity/src/gravity/compiler/authenticated_envelope.gravity")

(def ^:private c10-source
  "bootstrap/gravity/src/gravity/compiler/c10_safety_analysis_pipeline.gravity")

(def ^:private bridge-source
  (str
   "bootstrap/clojure/fixtures/self-hosting/sh-11/"
   "authenticated_safety_bridge.gravity"))

(def ^:private envelope-plan
  (delay (compile-plan envelope-source)))

(def ^:private c10-plan
  (delay (compile-plan c10-source)))

(def ^:private bridge-plan
  (delay (compile-plan bridge-source)))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh11-authenticated-safety-integration
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- resolved-value [namespace name]
  (or
   (some-> (ns-resolve namespace name) var-get)
   (throw
    (ex-info
     "Required authenticated integration helper is unavailable"
     {:id "SH11-AUTH-HELPER"
      :namespace namespace
      :name name}))))

(defn- sh08-value [name]
  (resolved-value
   'gravity.self-hosting.sh08-authenticated-type-integration-test
   name))

(defn- sh09-value [name]
  (resolved-value
   'gravity.self-hosting.sh09-authenticated-effect-integration-test
   name))

(defn- sh10-value [name]
  (resolved-value
   'gravity.self-hosting.sh10-authenticated-ownership-integration-test
   name))

(defn- sh08-call [name & arguments]
  (apply (sh08-value name) arguments))

(defn- sh09-call [name & arguments]
  (apply (sh09-value name) arguments))

(defn- sh10-call [name & arguments]
  (apply (sh10-value name) arguments))

(defn- canonical-id [value]
  (bootstrap/p15-s23-c6c10-canonical-digest
   "<sh11-authenticated-safety-integration>" value))

(defn- identity-id [domain preimage]
  (canonical-id {:domain domain :semantic-input preimage}))

(defn- source-hash [source-text]
  (str "sha256:" (bootstrap/sha256-hex source-text)))

(defn- byte-count [source-text]
  (alength
   (.getBytes
    source-text java.nio.charset.StandardCharsets/UTF_8)))

(defn- plan-semantic-id [plan]
  (bootstrap/p15-s23-c11-mir-digest
   (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input
    plan)))

(defn- function-shapes [plan]
  (into
   (sorted-map)
   (map
    (fn [[name function]]
      [name (select-keys function [:arity :params])]))
   (:functions plan)))

(def ^:private envelope-bounds
  {:maximum-semantic-projections 64
   :maximum-fact-transitions 64
   :maximum-identity-subjects 64
   :maximum-lineage-records 32
   :maximum-reference-nodes 128
   :maximum-reference-edges 128
   :maximum-reference-depth 64
   :maximum-logical-source-path-code-units 128
   :maximum-reference-id-code-units 128
   :maximum-digest-requests 2048
   :maximum-carrier-nodes 65536
   :maximum-carrier-depth 64
   :maximum-container-width 128
   :maximum-scalar-bytes 65536
   :maximum-integer-bits 256})

(defn- bridge-source-revision []
  (let [source-text (slurp (path bridge-source))
        plan @bridge-plan
        builder 'sh11-build-authenticated-safety-request]
    {:owner :sh-safety
     :source-language :gravity
     :logical-source-path
     "self-hosting/sh-11/authenticated-safety-bridge"
     :source-content-hash (source-hash source-text)
     :source-byte-count (byte-count source-text)
     :plan-semantic-hash (plan-semantic-id plan)
     :functions-semantic-hash
     (bootstrap/p15-s23-c11-mir-digest (:functions plan))
     :builder-function builder
     :builder-semantic-hash
     (bootstrap/p15-s23-c11-mir-digest
      (get (:functions plan) builder))
     :function-shapes (function-shapes plan)}))

(defn- authenticated-sh08-products [extension]
  {:effect
   (sh09-call 'authenticated-sh08-products extension)
   :ownership
   (sh10-call 'authenticated-sh08-products extension)})

(def ^:private sh08-gravity
  (delay (authenticated-sh08-products ".gravity")))

(def ^:private sh08-qst
  (delay (authenticated-sh08-products ".qst")))

(defn- effect-products [sh08-products actual-path]
  (let [prepared
        (sh09-call
         'prepared-request sh08-products :pure actual-path)
        run (sh09-call 'run-bridge prepared)
        verification
        (sh09-call
         'invoke
         (sh09-value 'bridge-plan)
         'sh09-verify-authenticated-effect-result
         [(:request prepared)
          (:c8-request run)
          (:effect-result run)
          (:effect-verification run)
          (:result run)])]
    {:effected-core (:result run)
     :verification verification}))

(defn- ownership-products [sh08-products actual-path]
  (let [prepared
        (sh10-call
         'prepared-request
         sh08-products :initialized-read actual-path)
        run (sh10-call 'run-bridge prepared)
        verification
        (sh10-call
         'invoke
         (sh10-value 'bridge-plan)
         'sh10-verify-authenticated-ownership-result
         [(:request prepared)
          (:c9-request run)
          (:ownership-result run)
          (:ownership-verification run)
          (:result run)])]
    {:owned-core (:result run)
     :verification verification}))

(defn- upstream-products [sh08-products actual-path]
  {:effect
   (effect-products
    (:effect sh08-products)
    (str actual-path "/effect"))
   :ownership
   (ownership-products
    (:ownership sh08-products)
    (str actual-path "/ownership"))})

(defn- fact-links [upstream]
  (let [effected (get-in upstream [:effect :effected-core])
        owned (get-in upstream [:ownership :owned-core])
        effect-preserves
        (get-in effected [:effect-result :preserves])
        ownership-preserves
        (get-in owned [:ownership-result :preserves])
        raw-span (:source-span effect-preserves)
        raw-origins (:origin-chain effect-preserves)
        normalized-origins
        (if (seq raw-origins)
          (mapv :origin-id raw-origins)
          [(:core-node-id effect-preserves)])
        effect-id (:artifact-id effected)
        ownership-id (:artifact-id owned)]
    {:core-node-id
     (:core-node-id effect-preserves)
     :type-fact-id
     (:type-fact-id ownership-preserves)
     :effect-fact-id
     (:effect-fact-id ownership-preserves)
     :capability-proof-id
     (:capability-proof-id ownership-preserves)
     :ownership-fact-id
     ownership-id
     :source-span
     {:source-id (:core-node-id effect-preserves)
      :start-byte (:byte-start raw-span)
      :end-byte (:byte-end raw-span)}
     :origin-chain normalized-origins
     :raw-source-span raw-span
     :raw-origin-chain raw-origins
     :profile (:profile effect-preserves)
     :target (:target effect-preserves)
     :runtime-check-id
     (canonical-id
      {:domain :gravity/sh11-runtime-check-v1
       :typed-artifact-id (:typed-artifact-id effected)})
     :runtime-support-id
     (canonical-id
      {:domain :gravity/sh11-runtime-support-v1
       :effect-artifact-id effect-id})
     :runtime-provider-id
     (canonical-id
      {:domain :gravity/sh11-runtime-provider-v1
       :effect-artifact-id effect-id})
     :unsafe-audit-id
     (canonical-id
      {:domain :gravity/sh11-unsafe-audit-v1
       :ownership-artifact-id ownership-id})
     :unsafe-review-id
     (canonical-id
      {:domain :gravity/sh11-unsafe-review-v1
       :ownership-artifact-id ownership-id})}))

(defn- overflow-facts [operation links]
  {:numeric-mode
   (if (= operation :unsafe-overflow)
     :unsafe-unchecked
     :proof-required)
   :proof-id (:type-fact-id links)
   :operator :add
   :left (if (= operation :unsafe-overflow)
           2147483647
           20)
   :right (if (= operation :unsafe-overflow) 1 22)
   :bit-width 32
   :signedness :signed
   :result-min -2147483648
   :result-max 2147483647
   :arbitrary-precision-supported false})

(defn- division-facts [links]
  {:numeric-mode :checked
   :proof-id (:type-fact-id links)
   :operator :divide
   :dividend 42
   :divisor 0
   :bit-width 32
   :signedness :signed
   :result-min -2147483648
   :result-max 2147483647})

(defn- runtime-support [links]
  {:artifact :gravity/sh11-runtime-check-target-support
   :support-id (:runtime-support-id links)
   :provider-id (:runtime-provider-id links)
   :profile (:profile links)
   :target (:target links)
   :conditions #{:valid-divisor-and-quotient}
   :failure-behaviors #{:error/numeric}
   :effects #{:error/raise}
   :performance-classes #{:constant-time-branch}
   :evidence #{:target-conformance :failure-path-test}})

(defn- runtime-check [links]
  {:artifact :gravity/sh11-runtime-safety-check
   :check-id (:runtime-check-id links)
   :condition :valid-divisor-and-quotient
   :predicate
   {:expression :divisor-nonzero-and-quotient-representable
    :operation-id :runtime-division
    :kind :division
    :operands
    {:operator :divide
     :dividend 42
     :divisor 0
     :minimum -2147483648
     :maximum 2147483647}}
   :emitted-location :before-operation
   :profile (:profile links)
   :target (:target links)
   :failure-behavior :error/numeric
   :effects-introduced #{:error/raise}
   :performance-class :constant-time-branch
   :invalidation-conditions
   [:operand-range-change :numeric-mode-change]
   :target-support-id (:runtime-support-id links)
   :provider-id (:runtime-provider-id links)})

(defn- unsafe-audit [links]
  {:artifact :gravity/sh11-unsafe-island-audit
   :audit-id (:unsafe-audit-id links)
   :operation-id :unsafe-overflow
   :operation :numeric-overflow
   :owner "self-hosting-safety"
   :reason :explicit-machine-width-overflow
   :source-span (:source-span links)
   :generated-origin-chain (:origin-chain links)
   :profile (:profile links)
   :target (:target links)
   :safety-mode :systems
   :effects #{}
   :capabilities #{}
   :preconditions #{:caller-proves-range}
   :postconditions #{:machine-width-result}
   :invariants #{:explicit-unsafe-mode :lineage-preserved}
   :evidence #{:authenticated-effect :authenticated-ownership}
   :safe-wrapper "gravity.numeric/unchecked-add"
   :review
   {:state :approved
    :review-id (:unsafe-review-id links)
    :source-id (:core-node-id links)
    :policy :required}
   :policy
   {:safety-mode :systems
    :package-unsafe-approved true
    :empty-effects-approved true
    :empty-capabilities-approved true}
   :re-review :on-target-or-wrapper-change})

(defn- safety-operation [operation links]
  (let [division?
        (contains?
         #{:runtime-division :unresolved-division}
         operation)
        runtime? (= operation :runtime-division)
        unsafe? (= operation :unsafe-overflow)]
    {:artifact :gravity/sh11-normalized-safety-operation
     :operation-id operation
     :kind (if division? :division :numeric-overflow)
     :core-node-id (:core-node-id links)
     :source-span (:source-span links)
     :origin-chain (:origin-chain links)
     :profile (:profile links)
     :target (:target links)
     :safety-mode (if unsafe? :systems :safe)
     :facts
     (if division?
       (division-facts links)
       (overflow-facts operation links))
     :runtime-check-support
     (when runtime? (runtime-support links))
     :runtime-check
     (when runtime? (runtime-check links))
     :unsafe-audit
     (when unsafe? (unsafe-audit links))
     :type-fact-id (:type-fact-id links)
     :effect-fact-id (:effect-fact-id links)
     :capability-proof-id (:capability-proof-id links)
     :ownership-fact-id (:ownership-fact-id links)}))

(defn- c10-products [safety-operation]
  (let [request safety-operation
        result
        (invoke
         c10-plan 'sh11-classify-operation [request])
        verification
        (invoke
         c10-plan
         'sh11-verify-safety-result [request result])]
    {:safety-request request
     :safety-result result
     :safety-verification verification}))

(defn- fact-transition [name value evidence-id]
  {:name name
   :disposition :preserved
   :input value
   :output value
   :input-count (count value)
   :output-count (count value)
   :evidence-ids [evidence-id]})

(defn- descriptor
  [upstream safety-operation c10 operation links actual-path]
  (let [effected (get-in upstream [:effect :effected-core])
        owned (get-in upstream [:ownership :owned-core])
        effect-identity
        {:effect-artifact-id (:artifact-id effected)}
        ownership-identity
        {:ownership-artifact-id (:artifact-id owned)}
        safety-operation-id
        (identity-id
         :gravity/sh11-safety-operation-v1
         safety-operation)
        safety-artifact-id
        (identity-id
         :gravity/sh11-safety-result-v3
         {:result (:safety-result c10)
          :verification (:safety-verification c10)})
        safety-identity
        {:safety-result-id safety-artifact-id}
        effect-subject-id
        (identity-id
         :gravity/sh11-effected-core-v1 effect-identity)
        ownership-subject-id
        (identity-id
         :gravity/sh11-owned-core-v1 ownership-identity)
        safety-subject-id safety-artifact-id
        evidence-id
        (canonical-id
         {:domain
          :gravity/sh11-authenticated-safety-evidence-v1
          :effect-subject-id effect-subject-id
          :ownership-subject-id ownership-subject-id
          :safety-subject-id safety-subject-id
          :operation operation})
        fact-names
        [:type :effect :capability :ownership :safety]
        fact-values
        {:type {:fact-id (:type-fact-id links)}
         :effect {:fact-id (:effect-fact-id links)}
         :capability
         {:proof-id (:capability-proof-id links)}
         :ownership
         {:fact-id (:ownership-fact-id links)}
         :safety {:result-id safety-subject-id}}
        facts
        (mapv
         #(fact-transition % (get fact-values %) evidence-id)
         fact-names)]
    {:artifact
     :gravity/private-authenticated-envelope-descriptor
     :schema-version 1
     :stage :sh11-authenticated-safety-bridge
     :artifact-kind
     :gravity/sh11-authenticated-safety-boundary
     :source-revision (bridge-source-revision)
     :projection-contract
     {:contract-kind
      :gravity/sh11-authenticated-safety-boundary-contract
      :contract-version 1
      :profile :meta
      :target :jvm
       :required-semantic-projections
      [:sh09-effected-core
       :sh10-owned-core
       :sh11-safety-operation
       :sh11-safety-result]
      :required-fact-families fact-names
      :required-identity-subjects
      [:sh09-effected-core
       :sh10-owned-core
       :sh11-safety-operation
       :sh11-safety-result]}
     :semantic-projections
     [{:name :sh09-effected-core
       :role :verified-effect-identity
       :entry-count (count effect-identity)
       :value effect-identity}
      {:name :sh10-owned-core
       :role :verified-ownership-identity
       :entry-count (count ownership-identity)
       :value ownership-identity}
      {:name :sh11-safety-operation
       :role :source-bound-normalized-safety-operation
       :entry-count 1
       :value
       {:safety-operation-id safety-operation-id}}
      {:name :sh11-safety-result
       :role :fresh-safety-result-identity
       :entry-count (count safety-identity)
       :value safety-identity}]
     :fact-transitions facts
     :effect-capability-relation
     {:effect-facts
      {:declared
       (set
        (get-in
         effected [:effect-result :direct-effects]))
       :observed
       (set
        (get-in
         effected [:effect-result :direct-effects]))}
      :capability-facts
      {:required #{}
       :granted #{}}
      :capability-proof-facts
      {:proof-ids [evidence-id]}
      :effect-order []
      :provider-selections []
      :grant-scopes []}
     :proof-composite
     {:proof-records
      [{:proof-id evidence-id
        :status :host-replayed
        :effect-verification-status
        (get-in upstream [:effect :verification :status])
        :ownership-verification-status
        (get-in
         upstream [:ownership :verification :status])
        :c10-verification-status
        (:status (:safety-verification c10))}]
      :proof-certificate-table
      {evidence-id
       {:status :host-replayed
        :effect-verification-status
        (get-in upstream [:effect :verification :status])
        :ownership-verification-status
        (get-in
         upstream [:ownership :verification :status])
        :c10-verification-status
        (:status (:safety-verification c10))}}
      :proof-summary {:required 3 :checked 3}
      :proof-usage
      [{:proof-id evidence-id
        :used-by :authenticated-safety-boundary}]}
     :preservation
     {:requires fact-names
      :preserves fact-names
      :invalidates []
      :regenerates []
      :residual-checks
      [:same-typed-lineage
       :fresh-effect-verification
       :fresh-ownership-verification
       :fresh-c10-result
       :fresh-c10-verification]}
     :identity-subjects
     [{:name :sh09-effected-core
       :domain :gravity/sh11-effected-core-v1
       :preimage effect-identity
       :observed-id effect-subject-id}
      {:name :sh10-owned-core
       :domain :gravity/sh11-owned-core-v1
       :preimage ownership-identity
       :observed-id ownership-subject-id}
      {:name :sh11-safety-operation
       :domain :gravity/sh11-safety-operation-v1
       :preimage safety-operation
       :observed-id safety-operation-id}
      {:name :sh11-safety-result
       :domain :gravity/sh11-safety-result-v3
       :preimage
       {:result (:safety-result c10)
        :verification (:safety-verification c10)}
       :observed-id safety-subject-id}]
     :lineage
     [{:stage :sh09-effect-checking
       :artifact-kind
       :gravity/sh09-authenticated-effected-core
       :semantic-id effect-subject-id
       :artifact-id (:artifact-id effected)
       :verification-id evidence-id
       :relation :verified-upstream}
      {:stage :sh10-ownership-checking
       :artifact-kind
       :gravity/sh10-authenticated-owned-core
       :semantic-id ownership-subject-id
       :artifact-id (:artifact-id owned)
       :verification-id evidence-id
       :relation :verified-upstream}
      {:stage :sh11-safety-analysis
       :artifact-kind
       :gravity/sh11-normalized-safety-operation
       :semantic-id safety-operation-id
       :artifact-id safety-operation-id
       :verification-id evidence-id
       :relation :source-bound-input}
      {:stage :sh11-safety-analysis
       :artifact-kind
       :gravity/sh11-safety-classification-result
       :semantic-id safety-subject-id
       :artifact-id safety-artifact-id
       :verification-id evidence-id
       :relation :freshly-recomputed}]
     :reference-closure
     {:root-id "sh11-safety-bridge"
      :node-ids
      ["sh11-safety-bridge"
       "sh09-effected-core"
       "sh10-owned-core"
       "sh11-safety-operation"
       "sh11-safety-result"]
      :edges
      [{:from "sh11-safety-bridge"
        :role :consumes :to "sh09-effected-core"}
       {:from "sh11-safety-bridge"
        :role :consumes :to "sh10-owned-core"}
       {:from "sh11-safety-bridge"
        :role :consumes :to "sh11-safety-operation"}
       {:from "sh11-safety-bridge"
        :role :produces :to "sh11-safety-result"}]
      :fact-reference-ids
      [(:type-fact-id links)
       (:effect-fact-id links)
       (:capability-proof-id links)
       (:ownership-fact-id links)]
      :origin-reference-ids (:origin-chain links)
      :proof-reference-ids [evidence-id]
      :runtime-check-reference-ids
      (if (= operation :runtime-division)
        [(:runtime-check-id links)]
        [])
      :observed-node-count 5
      :observed-edge-count 4
      :observed-maximum-depth 1}
     :actual-path-provenance
     {:source-path actual-path
      :workspace-root (str @root)
      :invocation-root (System/getProperty "user.dir")}
     :bounds envelope-bounds}))

(defn- authenticated-envelope
  [descriptor effect-verification ownership-verification]
  (let [raw
        (invoke
         envelope-plan
         'authenticated-envelope-build-template
         [descriptor])
        replay
        (invoke
         envelope-plan
         'authenticated-envelope-verify-template
         [descriptor
          (:artifact-template raw)
          (:digest-requests raw)])
        sealed (sh08-call 'seal-builder-result! raw)]
    {:envelope (:template sealed)
     :template-replay replay
     :verification
     {:artifact
      :gravity/sh11-envelope-contextual-verification
      :status :contextual-verification-passed
      :artifact-template (:template sealed)
      :semantic-envelope-root (:semantic-root sealed)
      :provenance-binding-root (:provenance-root sealed)
      :identity-checks (:identity-checks sealed)
      :identity-enforcement :passed
      :eligible-for-contextual-acceptance? true
      :host-digest-resolution :passed
      :identity-subject-equality :passed
      :fresh-envelope-reconstruction :passed
      :effect-verification effect-verification
      :ownership-verification ownership-verification}
     :semantic-root (:semantic-root sealed)
     :provenance-root (:provenance-root sealed)}))

(defn- request
  [upstream safety-operation c10 operation descriptor
   authenticated actual-path]
  (let [safety-operation-id
        (identity-id
         :gravity/sh11-safety-operation-v1
         safety-operation)
        safety-artifact-id
        (identity-id
         :gravity/sh11-safety-result-v3
         {:result (:safety-result c10)
          :verification (:safety-verification c10)})]
  {:artifact :gravity/sh11-authenticated-safety-bridge-request
   :schema-version 1
   :operation operation
   :effected-core
   (get-in upstream [:effect :effected-core])
   :effect-verification
   (get-in upstream [:effect :verification])
   :owned-core
   (get-in upstream [:ownership :owned-core])
   :ownership-verification
   (get-in upstream [:ownership :verification])
   :safety-operation safety-operation
   :expected-safety-operation-id safety-operation-id
   :expected-safety-result (:safety-result c10)
   :expected-safety-verification
   (:safety-verification c10)
   :expected-safety-artifact-id
   safety-artifact-id
   :envelope-descriptor descriptor
   :envelope (:envelope authenticated)
   :template-replay (:template-replay authenticated)
   :envelope-verification (:verification authenticated)
   :actual-source-path actual-path}))

(defn- prepared-request
  [sh08-products operation actual-path]
  (let [upstream (upstream-products sh08-products actual-path)
        links (fact-links upstream)
        safety-operation
        (safety-operation operation links)
        c10 (c10-products safety-operation)
        descriptor
        (descriptor
         upstream safety-operation c10 operation links
         actual-path)
        authenticated
        (authenticated-envelope
         descriptor
         (get-in upstream [:effect :verification])
         (get-in upstream [:ownership :verification]))]
    {:request
     (request
      upstream safety-operation c10 operation descriptor
      authenticated actual-path)
     :upstream upstream
     :links links
     :safety-operation safety-operation
     :c10 c10
     :descriptor descriptor
     :authenticated authenticated}))

(defn- run-bridge [prepared]
  (let [request (:request prepared)
        c10-request
        (invoke
         bridge-plan
         'sh11-build-authenticated-safety-request
         [request])
        fresh-result
        (invoke
         c10-plan
         'sh11-classify-operation
         [(:safety-request c10-request)])
        fresh-verification
        (invoke
         c10-plan
         'sh11-verify-safety-result
         [(:safety-request c10-request) fresh-result])
        result
        (invoke
         bridge-plan
         'sh11-bind-authenticated-safety-result
         [request c10-request fresh-result fresh-verification])]
    {:c10-request c10-request
     :safety-result fresh-result
     :safety-verification fresh-verification
     :result result}))

(deftest sh11-authenticated-safety-bridge-compiles-with-narrow-api
  (doseq [plan [envelope-plan c10-plan bridge-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan
           (:kind @plan))))
  (let [functions (:functions @bridge-plan)]
    (is (= {:arity 0 :params []}
           (select-keys
            (get
             functions
             'sh11-authenticated-safety-bridge-policy)
            [:arity :params])))
    (is (= {:arity 1 :params ['request]}
           (select-keys
            (get
             functions
             'sh11-build-authenticated-safety-request)
            [:arity :params])))
    (is
     (=
      {:arity 4
       :params
       ['request 'c10-request
        'safety-result 'safety-verification]}
      (select-keys
       (get
        functions
        'sh11-bind-authenticated-safety-result)
       [:arity :params])))
    (is
     (=
      {:arity 5
       :params
       ['request 'c10-request 'safety-result
        'safety-verification 'candidate]}
      (select-keys
       (get
        functions
        'sh11-verify-authenticated-safety-result)
       [:arity :params])))))

(deftest sh11-classifies-all-four-safe1-outcomes
  (doseq [[extension sh08-products]
          [[".gravity" @sh08-gravity]
           [".qst" @sh08-qst]]]
    (doseq [[operation expected-outcome expected-status
             expected-rule]
            [[:proven-overflow :proven-safe :accepted nil]
             [:runtime-division :runtime-checked :rejected
              "STD11-BRIDGE-LINEAGE"]
             [:unresolved-division :rejected :rejected
              "STD11-BRIDGE-SAFETY"]
             [:unsafe-overflow :unsafe-island :accepted nil]]]
      (let [prepared
            (prepared-request
             sh08-products operation
             (str "/checkout-a/safety-input" extension))
            run (run-bridge prepared)
            result (:result run)]
        (is (= :accepted (:status (:c10-request run)))
            [extension operation])
        (is (= (get-in prepared [:request :safety-operation])
               (:safety-request (:c10-request run)))
            [extension operation])
        (is (= (get-in
                prepared
                [:request :expected-safety-verification])
               (:expected-safety-verification
                (:c10-request run)))
            [extension operation])
        (is (= expected-outcome
               (:outcome (:safety-result run)))
            [extension operation])
        (is (= :passed
               (:status (:safety-verification run)))
            [extension operation])
        (is (= expected-status (:status result))
            [extension operation])
        (if (= expected-status :accepted)
          (do
            (is (= expected-outcome (:outcome result)))
            (is (= 1
                   (count
                    (get-in
                     result
                     [:safety-result :outcomes])))))
          (do
            (is (= expected-rule
                   (get-in result [:diagnostics 0 :rule])))
            (if (= operation :unresolved-division)
              (is (= "C10-NUMERIC"
                     (get-in
                      result
                      [:diagnostics 0 :facts
                       :upstream-diagnostics 0 :rule])))
              (is
               (= :safety-effect-capability-mismatch
                  (get-in
                   result
                   [:diagnostics 0 :facts :reason]))))))
        (is
         (= :passed
            (:status
             (invoke
              bridge-plan
              'sh11-verify-authenticated-safety-result
              [(:request prepared)
               (:c10-request run)
               (:safety-result run)
               (:safety-verification run)
               result]))))))))

(deftest sh11-rejects-cross-lineage-and-product-alteration
  (let [prepared
        (prepared-request
         @sh08-gravity :proven-overflow
         "/checkout-a/safety-input.gravity")
        request (:request prepared)
        accepted (run-bridge prepared)
        foreign-id
        (str "sha256:" (apply str (repeat 64 "f")))
        foreign-owned
        (assoc (:owned-core request)
               :typed-artifact-id foreign-id)
        cross-lineage
        (-> request
            (assoc :owned-core foreign-owned)
            (assoc
             :ownership-verification
             (-> (:ownership-verification request)
                 (assoc :expected foreign-owned)
                 (assoc :candidate foreign-owned))))
        altered-effect
        (assoc-in
         request [:effected-core :artifact-id] foreign-id)
        altered-envelope
        (assoc-in
         request [:envelope :semantic-envelope-id]
         (str "sha256:" (apply str (repeat 64 "e"))))
        altered-operation
        (assoc-in
         request [:safety-operation :core-node-id]
         foreign-id)
        altered-operation-facts
        (assoc-in
         request [:safety-operation :facts :left] 21)
        altered-verification
        (assoc
         (:expected-safety-verification request)
         :operation-id :different-operation)
        request-with-altered-verification
        (assoc
         request
         :expected-safety-verification
         altered-verification)
        altered-safety
        (assoc (:safety-result accepted)
               :outcome :runtime-checked)
        altered-result
        (assoc (:result accepted) :status :rejected)]
    (doseq [[label candidate expected-rule]
            [[:lineage cross-lineage
              "STD11-BRIDGE-LINEAGE"]
             [:effect altered-effect
              "STD11-BRIDGE-LINEAGE"]
             [:operation altered-operation
              "STD11-BRIDGE-LINEAGE"]
             [:operation-facts altered-operation-facts
              "STD11-BRIDGE-ENVELOPE"]
             [:verification request-with-altered-verification
              "STD11-BRIDGE-ENVELOPE"]
             [:envelope altered-envelope
              "STD11-BRIDGE-ENVELOPE"]]]
      (testing (name label)
        (let [result
              (invoke
               bridge-plan
               'sh11-build-authenticated-safety-request
               [candidate])]
          (is (= :rejected (:status result)))
          (is (= expected-rule
                 (get-in result [:diagnostics 0 :rule]))))))
    (let [safety-rejection
          (invoke
           bridge-plan
           'sh11-bind-authenticated-safety-result
           [request
            (:c10-request accepted)
            altered-safety
            (:safety-verification accepted)])
          result-rejection
          (invoke
           bridge-plan
           'sh11-verify-authenticated-safety-result
           [request
            (:c10-request accepted)
            (:safety-result accepted)
            (:safety-verification accepted)
            altered-result])]
      (is (= :rejected (:status safety-rejection)))
      (is (= "STD11-BRIDGE-VERIFY"
             (get-in
              safety-rejection
              [:diagnostics 0 :rule])))
      (is (= :rejected (:status result-rejection))))))

(deftest sh11-identity-is-path-neutral-with-separate-provenance
  (let [sh08-products @sh08-gravity
        prepared-a
        (prepared-request
         sh08-products :proven-overflow
         "/checkout-a/safety-input.gravity")
        prepared-b
        (prepared-request
         sh08-products :proven-overflow
         "/checkout-b/safety-input.gravity")
        result-a (:result (run-bridge prepared-a))
        result-b (:result (run-bridge prepared-b))]
    (is (= (get-in
            prepared-a [:authenticated :semantic-root])
           (get-in
            prepared-b [:authenticated :semantic-root])))
    (is
     (not=
      (get-in
       prepared-a [:authenticated :provenance-root])
      (get-in
       prepared-b [:authenticated :provenance-root])))
    (is (= (:identity-input result-a)
           (:identity-input result-b)))
    (is (= (:artifact-id result-a)
           (:artifact-id result-b)))
    (is (not= (:provenance result-a)
              (:provenance result-b)))))

(deftest sh11-authenticated-safety-bridge-fails-closed-on-malformed-input
  (doseq [value
          [nil
           {}
           {:artifact
            :gravity/sh11-authenticated-safety-bridge-request}
           {:artifact
            :gravity/sh11-authenticated-safety-bridge-request
            :schema-version 1
            :unexpected true}]]
    (let [result
          (invoke
           bridge-plan
           'sh11-build-authenticated-safety-request
           [value])]
      (is (= :rejected (:status result)))
      (is (= "STD11-BRIDGE-SCHEMA"
             (get-in result [:diagnostics 0 :rule])))))
  (let [over-depth
        (reduce (fn [value _] [value])
                :leaf
                (range 130))
        result
        (invoke
         bridge-plan
         'sh11-build-authenticated-safety-request
         [over-depth])]
    (is (= :rejected (:status result)))
    (is (= "STD11-BRIDGE-SCHEMA"
           (get-in result [:diagnostics 0 :rule])))
    (is (= :request-carrier-bound
           (get-in
            result
            [:diagnostics 0 :facts :reason])))))
