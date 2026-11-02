(ns gravity.effects
  "Existing hosted seed effect/graph builders; Gravity retains effect semantics."
  (:require [clojure.set :as set]))

(def ^:private namespace-contract
  '{:namespace gravity.effects
    :contract-boundary :hosted-seed-effect-data-and-native-reports
    :governing-contracts [D1 D2 D3 D8 D9 L6 L15 C1 C7 C8 C15 BOOT8 TEST2]
    :public-api
    {
     native-diagnostic-ids
     {:api-kind :data :arglists nil :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     native-governing-document
     {:api-kind :data :arglists nil :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     native-rejected-designs
     {:api-kind :data :arglists nil :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     native-override-diagnostics
     {:api-kind :data :arglists nil :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     native-known-effects
     {:api-kind :data :arglists nil :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     native-effect-capability
     {:api-kind :data :arglists nil :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     native-replay-sensitive-effects
     {:api-kind :data :arglists nil :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     native-effect-message
     {:api-kind :function :arglists ([id]) :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     native-source-overrides
     {:arglists ([module]) :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     native-diagnostic-fail!
     {:arglists ([fail-operation effect-message source-span governing-document id source-path subject extra]) :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     validate-native-overrides!
     {:arglists ([effect-fail! source-span override-diagnostics effect-capability source-path module overrides]) :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     native-diagnostics
     {:arglists ([source-span diagnostic-ids rejected-designs effect-capability source-path type-facts]) :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     assemble-source-artifact
     {:arglists ([ops source-path source-text]) :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     legality-records
     {:arglists ([effect-capability module effect-graph]) :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     capability-proof-records
     {:arglists ([effect-capability module effect-graph]) :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     build-effect-log
     {:arglists ([effect-capability module]) :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     replay-requirements
     {:arglists ([effect-graph]) :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     ordering-constraints
     {:arglists ([effect-graph]) :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     residual-effect-report
     {:arglists ([effect-graph]) :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     native-verifier-report
     {:arglists ([known-effects diagnostic-ids module effect-graph legality capability-proof build-log replay ordering residual diagnostics])
      :admission :none :seed-compatibility-data-only? true :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     native-capability-proof
     {:arglists ([artifact]) :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     validate-native-report!
     {:arglists ([capability-proof effect-fail! source-path artifact]) :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false :trusted-callbacks? true}
     fact-direct-effects
     {:arglists ([fact]) :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     effectful-facts
     {:arglists ([direct-effects type-facts]) :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     effect-graph
     {:arglists ([effectful-facts direct-effects replay-sensitive-effects module type-facts functions])
      :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}}
    :dependency-direction
    {:requires [clojure.set] :consumed-by [gravity.c8-effect-checker.facts gravity.c8-effect-checker.verification gravity.c8-effect-checker.evidence gravity.c8-effect-checker.artifact gravity.c8-effect-checker.diagnostics gravity.c8-effect-checker.catalog]
     :forbids [gravity.bootstrap gravity.cli gravity.release gravity.c8-effect-checker]}
    :artifact-inputs [:native-diagnostic-subject-extra-and-catalog-data :explicit-message-span-and-failure-callbacks :source-path-and-text-with-explicit-owner-operations :explicit-effect-capability-catalog :native-seed-type-facts :native-function-records :native-module-declarations
                      :explicit-classifier-and-selection-callbacks :replay-sensitive-effect-catalog :native-effect-catalog-and-diagnostic-ids :native-legality-capability-build-replay-ordering-residual-and-diagnostic-records :trusted-projection-and-failure-callbacks]
    :artifact-outputs [:native-catalog-defaults-and-message-text :native-override-data :owner-failure-return-or-exception :native-fixture-diagnostic-registry :hosted-seed-effect-artifact-and-borrowed-owner-identity :native-legality-capability-build-replay-ordering-and-residual-records :seed-direct-effect-sets :lazy-seed-effectful-fact-view :seed-effect-graph-data :native-verifier-report :legacy-capability-projection :complete-marker-or-native-report-refusal]
    :diagnostic-ownership
    {:projection [:native-catalog-defaults :native-message-text :native-payload-fields :fixture-override-selection :fixture-registry-data]
     :stage [C8-VERIFY C8-UNDECLARED C8-PROFILE C8-CAPABILITY C8-BUILD C8-REPLAY C8-ORDER C8-RUNTIME] :upstream [:type-function-and-source-data]
     :existing-c8-owner [:effect-rules :legality :capabilities :replay :ordering :source-admission-and-diagnostic-publication]}
    :legacy-status-labels-authoritative? false
    :legacy-build-grants-prove-performed-effects? false
    :unsupported [:canonical-effect-registration :canonical-c15-diagnostic-schema :diagnostic-identity-binding :redaction-and-safe-publication :native-ingress-authentication :callback-validation :canonical-c8-legality
                  :pure-native-graph-ordering-gap :canonical-diagnostic-coverage :untrusted-report-authentication :call-effect-fixed-point :latent-body-inference :handlers-and-continuations
                  :performed-build-effects :package-deployment-provider-authority :profile-and-safety-legality
                  :canonical-full-source-pipeline :authority-admission :migration :release :self-hosting :seed-retirement]})

(def native-diagnostic-ids
  ["C8-UNDECLARED"
   "C8-PROFILE"
   "C8-CAPABILITY"
   "C8-BUILD"
   "C8-REPLAY"
   "C8-ORDER"
   "C8-RUNTIME"
   "C8-UNKNOWN"
   "C8-VERIFY"])

(def native-governing-document
  "docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md")

(def native-rejected-designs
  [{:diagnostic "C8-UNDECLARED"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c8-undeclared.gravity"
    :rejected-design :inferred-effect-outside-declaration}
   {:diagnostic "C8-PROFILE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c8-profile.gravity"
    :rejected-design :profile-rejects-effect}
   {:diagnostic "C8-CAPABILITY"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c8-capability.gravity"
    :rejected-design :missing-capability-grant}
   {:diagnostic "C8-BUILD"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c8-build.gravity"
    :rejected-design :ungranted-build-effect}
   {:diagnostic "C8-REPLAY"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c8-replay.gravity"
    :rejected-design :replay-sensitive-effect-without-obligation}
   {:diagnostic "C8-ORDER"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c8-order.gravity"
    :rejected-design :missing-effect-ordering}
   {:diagnostic "C8-RUNTIME"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c8-runtime.gravity"
    :rejected-design :no-runtime-provider-support}
   {:diagnostic "C8-UNKNOWN"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c8-unknown.gravity"
    :rejected-design :unregistered-effect-name}
   {:diagnostic "C8-VERIFY"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c8-verify.gravity"
    :rejected-design :malformed-effect-artifact}])

(def native-override-diagnostics
  {:undeclared "C8-UNDECLARED"
   :profile "C8-PROFILE"
   :capability "C8-CAPABILITY"
   :build "C8-BUILD"
   :replay "C8-REPLAY"
   :order "C8-ORDER"
   :runtime "C8-RUNTIME"
   :unknown "C8-UNKNOWN"
   :verify "C8-VERIFY"})

(def native-known-effects
  #{:io/write :io/read :filesystem/read :filesystem/write :network/http
    :database/read :database/write :time/read :random/read
    :runtime/dynamic-dispatch :error/throw :memory/raw :ffi/call
    :workflow/event :workflow/replay :ai/model-call :ai/tool-call
    :ai/human-review :build/read-file :build/write-artifact
    :build/network :build/exec :build/model-call :build/tool-call})

(def native-effect-capability
  {:io/write :io/stdout
   :filesystem/read :fs/read
   :filesystem/write :fs/write
   :network/http :http/client
   :database/read :db/read
   :database/write :db/write
   :memory/raw :memory/raw
   :ffi/call :ffi/call
   :workflow/event :workflow/event
   :ai/model-call :model/call
   :ai/tool-call :tool/invoke
   :ai/human-review :ai/human-review
   :build/read-file :fs/read
   :build/write-artifact :artifact/write
   :build/network :http/client
   :build/exec :process/exec
   :build/model-call :model/call
   :build/tool-call :tool/invoke})

(def native-replay-sensitive-effects
  #{:time/read :random/read :network/http :database/read :workflow/event
    :workflow/replay :ai/model-call :ai/tool-call :ai/human-review
    :runtime/dynamic-dispatch})

(defn native-effect-message [id]
  (case id
    "C8-UNDECLARED" "inferred effects exceed the declared effect allowance"
    "C8-PROFILE" "active profile rejects the inferred effect"
    "C8-CAPABILITY" "effect lacks a required capability grant"
    "C8-BUILD" "build effect lacks a build grant"
    "C8-REPLAY" "replay-sensitive effect lacks replay or audit obligation"
    "C8-ORDER" "effect ordering constraints are missing"
    "C8-RUNTIME" "no legal runtime or provider supports the effect"
    "C8-UNKNOWN" "effect name is unregistered"
    "C8-VERIFY" "effect verifier rejected the artifact"
    "Effect checking failed"))

(defn fact-direct-effects [fact]
  (set/union (set (:effects fact))
             (case (:type fact)
               "CheckedCast[String]" #{:runtime/dynamic-dispatch}
               "ProtocolValue" #{:runtime/dynamic-dispatch}
               "SchemaDerived" #{:runtime/dynamic-dispatch}
               "UnsafeIsland[Dynamic]" #{:memory/raw}
               "Never" #{:error/throw}
               #{})))

(defn effectful-facts [direct-effects type-facts]
  (filter #(seq (direct-effects %)) type-facts))

(defn effect-graph
  [effectful-facts direct-effects replay-sensitive-effects
   module type-facts functions]
  (let [effectful (effectful-facts type-facts)]
    {:artifact :gravity/c8-effect-graph
     :module (:module module)
     :nodes (into (sorted-map)
                  (map (fn [fact]
                         (let [direct (direct-effects fact)]
                           [(:core-node fact)
                            {:direct direct
                             :latent #{}
                             :transitive direct
                             :ordering (if (seq direct) :sequence :pure)
                             :source (:source fact)}]))
                       type-facts))
     :functions (into (sorted-map)
                      (map (fn [fn-record]
                             [(:fn-id fn-record)
                              {:declared (set (:latent-effects fn-record))
                               :inferred (set (:latent-effects fn-record))
                               :latent (set (:latent-effects fn-record))
                               :throws (:throws fn-record)}])
                           (:functions functions)))
     :namespace {:declared (:effects module)
                 :inferred (set (mapcat direct-effects type-facts))}
     :build-effects (vec (sort-by str
                                  (get-in module
                                          [:metadata :build-grants] #{})))
     :replay-required
     (set/intersection replay-sensitive-effects
                       (set (mapcat direct-effects effectful)))
     :diagnostics []
     :status :complete}))

(defn native-source-overrides [module]
  (get-in module [:metadata :compiler :c8-effect-check] {}))

(defn native-diagnostic-fail!
  [fail-operation effect-message source-span governing-document
   id source-path subject extra]
  (fail-operation
   id
   (effect-message id)
   (merge {:source-span (or (:source-span subject)
                            (get-in subject [:source :span])
                            (:span subject)
                            (source-span source-path 0))
           :diagnostic-family :c8-effect-checker
           :stage :effect-check
           :document-id "C8"
           :expected-document governing-document
           :core-node-id (or (:core-node-id subject) (:core-node subject))
           :generated-origin-chain (or (:generated-origin subject)
                                       (get-in subject [:source :origin-chain]))
           :function (:function subject)
           :namespace (:namespace subject)
           :effect (or (:effect subject) :unknown/effect)
           :capability (:capability subject)
           :profile (:profile subject)
           :target (:target subject)
           :provider (:provider subject)
           :grant (:grant subject)
           :remediation
           "Emit effect graph facts, legality intersection records, capability proofs, build/replay obligations, ordering constraints, residual effect records, and verifier-accepted diagnostics before MIR construction."}
          extra)))

(defn validate-native-overrides!
  [effect-fail! source-span override-diagnostics effect-capability
   source-path module overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get override-diagnostics fail-kind)]
      (effect-fail! id source-path
                    {:source-span (source-span source-path 0)
                     :core-node "fixture-override"
                     :function "fixture"
                     :namespace (:module module)
                     :effect fail-kind
                     :capability (get effect-capability fail-kind)
                     :profile (:profile module)
                     :target (:target module)
                     :provider :fixture/provider
                     :grant :fixture/grant
                     :generated-origin []}
                    {:missing-fields [fail-kind]}))))

(defn native-diagnostics
  [source-span diagnostic-ids rejected-designs effect-capability
   source-path type-facts]
  {:artifact :gravity/c8-effect-diagnostic-registry
   :required-diagnostic-ids diagnostic-ids
   :diagnostics
   (mapv (fn [design]
           (let [fact (first type-facts)
                 effect (keyword "fixture" (:diagnostic design))]
             {:diagnostic (:diagnostic design)
              :fixture (:fixture design)
              :core-node-id (:core-node fact)
              :source-span (get-in fact [:source :span]
                                   (source-span source-path 0))
              :generated-origin-chain (get-in fact [:source :origin-chain])
              :function :fixture
              :namespace :fixture
              :effect effect
              :capability (get effect-capability effect)
              :profile (:profile fact)
              :target (:target fact)
              :provider :fixture/provider
              :grant :fixture/grant
              :remediation
              "Keep effect legality explicit before MIR construction."}))
         rejected-designs)
   :status :complete})

(defn assemble-source-artifact
  [ops source-path source-text]
  (let [{:keys [read-source-form-records validate-ns-syntax! parse-module
                effect-source-overrides effect-validate-overrides!
                c7-type-source-artifact effect-graph legality-records
                capability-proof-records build-effect-log replay-requirements
                ordering-constraints residual-effect-report effect-diagnostics
                effect-verifier-report effect-validate! effect-capability-proof
                artifact-id diagnostic-ids governing-document]} ops
        records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        overrides (effect-source-overrides module)
        _ (effect-validate-overrides! source-path module overrides)
        c7-artifact (c7-type-source-artifact source-path source-text)
        type-facts (:type-facts c7-artifact)
        functions (:function-type-table c7-artifact)
        effect-graph-value (effect-graph module type-facts functions)
        legality (legality-records module effect-graph-value)
        capability-records (capability-proof-records module effect-graph-value)
        build-log (build-effect-log module)
        replay (replay-requirements effect-graph-value)
        ordering (ordering-constraints effect-graph-value)
        residual (residual-effect-report effect-graph-value)
        diagnostics (effect-diagnostics source-path type-facts)
        verifier (effect-verifier-report module effect-graph-value legality
                                         capability-records build-log replay
                                         ordering residual diagnostics)
        artifact-base
        {:kind :gravity/stage0-c8-effect-checker-artifact
         :task "P06-D087"
         :document-set ["C8"]
         :governing-document governing-document
         :pass {:name :c8-effect-checker
                :input :typed-core
                :output :effected-core
                :requires [:typed-core-module :type-facts :function-types
                           :profile :capabilities :build-grants]
                :preserves [:source-spans :generated-origin :types
                            :profile :target :capabilities]
                :emits [:effect-graph :function-latent-effect-table
                        :namespace-effect-summary :module-effect-summary
                        :capability-proof-records :build-effect-log
                        :replay-effect-requirements
                        :effect-ordering-constraints
                        :residual-effect-report
                        :effect-diagnostics]
                :rejects diagnostic-ids}
         :source-overrides overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :c7-type-checker-artifact
         (select-keys c7-artifact [:kind :artifact-id :typed-core-module
                                   :type-environment :function-type-table
                                   :capability-based-proof])
         :effect-graph effect-graph-value
         :function-latent-effect-table
         {:artifact :gravity/c8-function-latent-effect-table
          :functions (get-in effect-graph-value [:functions])
          :status :complete}
         :namespace-effect-summary (:namespace effect-graph-value)
         :module-effect-summary
         {:declared (:effects module)
          :inferred (get-in effect-graph-value [:namespace :inferred])
          :status :complete}
         :effect-legality-report legality
         :capability-proof-records capability-records
         :build-effect-log build-log
         :replay-effect-requirements replay
         :effect-ordering-constraints ordering
         :residual-effect-report residual
         :effect-verifier-report verifier
         :effect-diagnostics diagnostics
         :c8-effect-check-results
         {:documents ["C8"]
          :task "P06-D087"
          :required-diagnostic-ids diagnostic-ids
          :effect-graph-status :complete
          :function-latent-status :complete
          :namespace-summary-status :complete
          :module-summary-status :complete
          :capability-proof-status :accepted
          :build-effect-status :complete
          :replay-status :complete
          :ordering-status :complete
          :residual-status :complete
          :verifier-status (:status verifier)
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (effect-validate! source-path artifact-base)
        capability-proof (effect-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (artifact-id (assoc artifact-base
                                            :capability-based-proof
                                            capability-proof)))))

(defn legality-records [effect-capability module effect-graph]
  (let [effects (get-in effect-graph [:namespace :inferred])]
    {:artifact :gravity/c8-effect-legality-report
     :records
     (mapv (fn [effect]
             (let [capability (get effect-capability effect)]
               {:effect effect
                :source :namespace
                :allowed-by {:function true
                             :namespace (contains? (:effects module) effect)
                             :profile true
                             :package true
                             :deployment true
                             :runtime true
                             :safety true}
                :required-capabilities (if capability #{capability} #{})
                :granted-capabilities
                (set/intersection (if capability #{capability} #{})
                                  (:capabilities module))
                :result :accepted}))
           (sort-by str effects))
     :status :accepted}))

(defn capability-proof-records [effect-capability module effect-graph]
  {:artifact :gravity/c8-capability-proof-records
   :records
   (mapv (fn [effect]
           (let [capability (get effect-capability effect)]
             {:artifact :gravity/capability-proof
              :effect effect
              :source :namespace
              :capability capability
              :grant (when capability
                       {:grant/id (keyword "stage0" (name capability))
                        :scope :namespace
                        :principal (:module module)
                        :phase :runtime})
              :provider (keyword "gravity.runtime" (name effect))
              :profile (:profile module)
              :target (:target module)
              :status (if (or (nil? capability)
                              (contains? (:capabilities module) capability))
                        :accepted
                        :rejected)}))
         (sort-by str (get-in effect-graph [:namespace :inferred])))
   :status :complete})

(defn build-effect-log [effect-capability module]
  (let [grants (get-in module [:metadata :build-grants] #{})]
    {:artifact :gravity/c8-build-effect-log
     :records (mapv (fn [effect]
                      {:effect effect
                       :phase :build
                       :granted? (contains? grants effect)
                       :capability (get effect-capability effect)
                       :status (if (contains? grants effect)
                                 :accepted
                                 :rejected)})
                    (sort-by str grants))
     :status :complete}))

(defn replay-requirements [effect-graph]
  {:artifact :gravity/c8-replay-effect-requirements
   :records (mapv (fn [effect]
                    {:effect effect
                     :mode :audit-record
                     :record-id (str "c8-replay-" (name effect))
                     :status :recorded})
                  (sort-by str (:replay-required effect-graph)))
   :status :complete})

(defn ordering-constraints [effect-graph]
  {:artifact :gravity/c8-effect-ordering-constraints
   :records
   (mapv (fn [[node-id node]]
           {:constraint-id (str "c8-order-" node-id)
            :core-node node-id
            :effects (:direct node)
            :ordering (:ordering node)
            :preserves [:sequence :no-duplicate :no-eliminate]
            :status :recorded})
         (filter (fn [[_ node]] (seq (:direct node)))
                 (:nodes effect-graph)))
   :status :complete})

(defn residual-effect-report [effect-graph]
  (let [effects (get-in effect-graph [:namespace :inferred])
        residuals (set/intersection effects
                                    #{:runtime/dynamic-dispatch :error/throw
                                      :memory/raw})]
    {:artifact :gravity/c8-residual-effect-report
     :records (mapv (fn [effect]
                      {:effect effect
                       :reason :preserved-for-runtime-or-safety
                       :mir-preservation :required
                       :status :recorded})
                    (sort-by str residuals))
     :status :complete}))

(defn native-verifier-report
  [known-effects diagnostic-ids module effect-graph legality capability-proof
   build-log replay ordering residual diagnostics]
  (let [inferred (get-in effect-graph [:namespace :inferred])
        declared (:effects module)
        known? (set/subset? inferred known-effects)
        declared? (set/subset? inferred declared)
        legality? (every? #(= :accepted (:result %)) (:records legality))
        capabilities? (every? #(= :accepted (:status %))
                              (:records capability-proof))
        build? (every? #(= :accepted (:status %)) (:records build-log))
        replay? (or (empty? (:replay-required effect-graph))
                    (seq (:records replay)))
        order? (seq (:records ordering))
        residual? (= :complete (:status residual))
        diagnostics? (= (set diagnostic-ids)
                        (set (map :diagnostic (:diagnostics diagnostics))))]
    {:artifact :gravity/c8-effect-verifier-report
     :every-effectful-node-recorded? (boolean (seq (:nodes effect-graph)))
     :known-effects? known?
     :declarations-cover-inferred-effects? declared?
     :legality-intersections-accepted? legality?
     :capability-proofs-accepted? capabilities?
     :build-effects-authorized? build?
     :replay-obligations-recorded? (boolean replay?)
     :ordering-constraints-recorded? (boolean order?)
     :residual-effects-recorded? residual?
     :diagnostics-covered? diagnostics?
     :status (if (and known? declared? legality? capabilities? build?
                      replay? order? residual? diagnostics?)
               :passed
               :failed)}))

(defn native-capability-proof [artifact]
  (let [verifier (:effect-verifier-report artifact)]
    {:effect-graph-complete?
     (:every-effectful-node-recorded? verifier)
     :declared-effect-allowance-checked?
     (:declarations-cover-inferred-effects? verifier)
     :legality-intersection-recorded?
     (:legality-intersections-accepted? verifier)
     :capability-proofs-accepted?
     (:capability-proofs-accepted? verifier)
     :build-effects-separated-and-authorized?
     (:build-effects-authorized? verifier)
     :replay-obligations-recorded?
     (:replay-obligations-recorded? verifier)
     :ordering-constraints-recorded?
     (:ordering-constraints-recorded? verifier)
     :residual-effects-recorded?
     (:residual-effects-recorded? verifier)
     :diagnostics-covered?
     (:diagnostics-covered? verifier)
     :verifier-passed?
     (= :passed (:status verifier))
     :status :complete}))

(defn validate-native-report! [capability-proof effect-fail! source-path artifact]
  (let [proof (capability-proof artifact)]
    (doseq [[field id] [[:effect-graph-complete? "C8-VERIFY"]
                        [:declared-effect-allowance-checked? "C8-UNDECLARED"]
                        [:legality-intersection-recorded? "C8-PROFILE"]
                        [:capability-proofs-accepted? "C8-CAPABILITY"]
                        [:build-effects-separated-and-authorized? "C8-BUILD"]
                        [:replay-obligations-recorded? "C8-REPLAY"]
                        [:ordering-constraints-recorded? "C8-ORDER"]
                        [:residual-effects-recorded? "C8-RUNTIME"]
                        [:diagnostics-covered? "C8-VERIFY"]
                        [:verifier-passed? "C8-VERIFY"]]]
      (when-not (get proof field)
        (effect-fail! id source-path {:stage :effect-check}
                      {:missing-fields [field]}))))
  :complete)

(doseq [operation [#'native-diagnostic-ids #'native-governing-document #'native-rejected-designs #'native-override-diagnostics #'native-known-effects #'native-effect-capability #'native-replay-sensitive-effects #'native-effect-message #'native-source-overrides #'native-diagnostic-fail! #'validate-native-overrides! #'native-diagnostics #'assemble-source-artifact #'legality-records #'capability-proof-records #'build-effect-log #'replay-requirements #'ordering-constraints #'residual-effect-report #'native-verifier-report #'native-capability-proof #'validate-native-report! #'fact-direct-effects #'effectful-facts #'effect-graph]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
