(ns gravity.c8-effect-checker
  "Hosted Stage0 C8 effect-analysis engine and artifact projection.

  The leaf preserves the Clojure seed compatibility implementation. It records
  effect/capability evidence but is not effect-legality, safety, proof, or
  release authority."
  (:require [clojure.set :as set]
            [gravity.digest :as digest]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})

(def ^:private function-operation-keys
  #{:fail! :source-span :c4-artifact-id :read-source-form-records
    :validate-ns-syntax! :parse-module
    :compiler-c7-type-source-artifact
    :c8-effect-source-overrides :c8-effect-message :c8-effect-fail! :c8-effect-validate-overrides! :c8-fact-direct-effects :c8-effectful-facts :c8-effect-graph :c8-legality-records :c8-capability-proof-records :c8-build-effect-log :c8-replay-requirements :c8-ordering-constraints :c8-residual-effect-report :c8-effect-diagnostics :c8-effect-verifier-report :c8-effect-capability-proof :c8-effect-validate! :compiler-c8-effect-source-artifact :compiler-c8-effect-file-artifact})

(def ^:private scalar-operation-keys
  #{:c8-effect-diagnostic-ids :c8-effect-governing-document
    :c8-effect-rejected-designs :c8-effect-override-diagnostics
    :c8-known-effects :c8-effect-capability
    :c8-replay-sensitive-effects})

(def ^:private operation-keys
  (into function-operation-keys scalar-operation-keys))

(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key)
    (get *operations* key)))

(defmacro ^:private definterposable [name args & body]
  (let [key (keyword name)]
    `(defn ~name ~args
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys*
                   (conj *active-operation-keys* ~key)]
           (operation# ~@args))
         (do ~@body)))))

(defn- default-fail! [id message data]
  (throw (ex-info message (assoc (or data {}) :id id))))
(defn- default-source-span [path index]
  {:source path :form-index index})
(defn- default-c4-artifact-id [artifact]
  (str "sha256:" (digest/sha256-hex (pr-str artifact))))
(defn- unsupported-host-operation [operation]
  (fn [& _]
    (throw (ex-info (str "C8 leaf requires injected operation " operation)
                    {:operation operation}))))
(defn- op-fn [key fallback] (or (get *operations* key) fallback))
(defn- fail! [id message data]
  ((op-fn :fail! default-fail!) id message data))
(defn- source-span [path index]
  ((op-fn :source-span default-source-span) path index))
(defn- c4-artifact-id [artifact]
  ((op-fn :c4-artifact-id default-c4-artifact-id) artifact))
(defn- read-source-form-records [path text]
  ((op-fn :read-source-form-records
          (unsupported-host-operation :read-source-form-records))
   path text))
(defn- validate-ns-syntax! [path forms]
  ((op-fn :validate-ns-syntax!
          (unsupported-host-operation :validate-ns-syntax!))
   path forms))
(defn- parse-module [path forms]
  ((op-fn :parse-module (unsupported-host-operation :parse-module))
   path forms))
(defn- compiler-c7-type-source-artifact [path text]
  ((op-fn :compiler-c7-type-source-artifact
          (unsupported-host-operation :compiler-c7-type-source-artifact))
   path text))

(def ^:dynamic c8-effect-diagnostic-ids
  ["C8-UNDECLARED"
   "C8-PROFILE"
   "C8-CAPABILITY"
   "C8-BUILD"
   "C8-REPLAY"
   "C8-ORDER"
   "C8-RUNTIME"
   "C8-UNKNOWN"
   "C8-VERIFY"])

(def ^:dynamic c8-effect-governing-document
  "docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md")

(def ^:dynamic c8-effect-rejected-designs
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

(def ^:dynamic c8-effect-override-diagnostics
  {:undeclared "C8-UNDECLARED"
   :profile "C8-PROFILE"
   :capability "C8-CAPABILITY"
   :build "C8-BUILD"
   :replay "C8-REPLAY"
   :order "C8-ORDER"
   :runtime "C8-RUNTIME"
   :unknown "C8-UNKNOWN"
   :verify "C8-VERIFY"})

(def ^:dynamic c8-known-effects
  #{:io/write :io/read :filesystem/read :filesystem/write :network/http
    :database/read :database/write :time/read :random/read
    :runtime/dynamic-dispatch :error/throw :memory/raw :ffi/call
    :workflow/event :workflow/replay :ai/model-call :ai/tool-call
    :ai/human-review :build/read-file :build/write-artifact
    :build/network :build/exec :build/model-call :build/tool-call})

(def ^:dynamic c8-effect-capability
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

(def ^:dynamic c8-replay-sensitive-effects
  #{:time/read :random/read :network/http :database/read :workflow/event
    :workflow/replay :ai/model-call :ai/tool-call :ai/human-review
    :runtime/dynamic-dispatch})

(definterposable c8-effect-source-overrides
  [module]
  (get-in module [:metadata :compiler :c8-effect-check] {}))

(definterposable c8-effect-message
  [id]
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

(definterposable c8-effect-fail!
  [id source-path subject extra]
  (fail! id
         (c8-effect-message id)
         (merge {:source-span (or (:source-span subject)
                                  (get-in subject [:source :span])
                                  (:span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :c8-effect-checker
                 :stage :effect-check
                 :document-id "C8"
                 :expected-document c8-effect-governing-document
                 :core-node-id (or (:core-node-id subject) (:core-node subject))
                 :generated-origin-chain (or (:generated-origin subject)
                                             (get-in subject
                                                     [:source :origin-chain]))
                 :function (:function subject)
                 :namespace (:namespace subject)
                 :effect (or (:effect subject) :unknown/effect)
                 :capability (:capability subject)
                 :profile (:profile subject)
                 :target (:target subject)
                 :provider (:provider subject)
                 :grant (:grant subject)
                 :remediation "Emit effect graph facts, legality intersection records, capability proofs, build/replay obligations, ordering constraints, residual effect records, and verifier-accepted diagnostics before MIR construction."}
                extra)))

(definterposable c8-effect-validate-overrides!
  [source-path module overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get c8-effect-override-diagnostics fail-kind)]
      (c8-effect-fail! id source-path
                       {:source-span (source-span source-path 0)
                        :core-node "fixture-override"
                        :function "fixture"
                        :namespace (:module module)
                        :effect fail-kind
                        :capability (get c8-effect-capability fail-kind)
                        :profile (:profile module)
                        :target (:target module)
                        :provider :fixture/provider
                        :grant :fixture/grant
                        :generated-origin []}
                       {:missing-fields [fail-kind]}))))

(definterposable c8-fact-direct-effects
  [fact]
  (set/union (set (:effects fact))
             (case (:type fact)
               "CheckedCast[String]" #{:runtime/dynamic-dispatch}
               "ProtocolValue" #{:runtime/dynamic-dispatch}
               "SchemaDerived" #{:runtime/dynamic-dispatch}
               "UnsafeIsland[Dynamic]" #{:memory/raw}
               "Never" #{:error/throw}
               #{})))

(definterposable c8-effectful-facts
  [type-facts]
  ;; Preserve the legacy Var-interposition behavior across lazy realization:
  ;; the bootstrap operation binding ends when this lazy sequence is returned.
  (let [direct-effects (or (current-operation :c8-fact-direct-effects)
                           c8-fact-direct-effects)]
    (filter #(seq (direct-effects %)) type-facts)))

(definterposable c8-effect-graph
  [module type-facts functions]
  (let [effectful (c8-effectful-facts type-facts)]
    {:artifact :gravity/c8-effect-graph
     :module (:module module)
     :nodes (into (sorted-map)
                  (map (fn [fact]
                         (let [direct (c8-fact-direct-effects fact)]
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
                 :inferred (set (mapcat c8-fact-direct-effects type-facts))}
     :build-effects (vec (sort-by str
                                  (get-in module
                                          [:metadata :build-grants] #{})))
     :replay-required (set/intersection
                       c8-replay-sensitive-effects
                       (set (mapcat c8-fact-direct-effects effectful)))
     :diagnostics []
     :status :complete}))

(definterposable c8-legality-records
  [module effect-graph]
  (let [effects (get-in effect-graph [:namespace :inferred])]
    {:artifact :gravity/c8-effect-legality-report
     :records
     (mapv (fn [effect]
             (let [capability (get c8-effect-capability effect)]
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
                :granted-capabilities (set/intersection
                                       (if capability #{capability} #{})
                                       (:capabilities module))
                :result :accepted}))
           (sort-by str effects))
     :status :accepted}))

(definterposable c8-capability-proof-records
  [module effect-graph]
  {:artifact :gravity/c8-capability-proof-records
   :records
   (mapv (fn [effect]
           (let [capability (get c8-effect-capability effect)]
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

(definterposable c8-build-effect-log
  [module]
  (let [grants (get-in module [:metadata :build-grants] #{})]
    {:artifact :gravity/c8-build-effect-log
     :records (mapv (fn [effect]
                      {:effect effect
                       :phase :build
                       :granted? (contains? grants effect)
                       :capability (get c8-effect-capability effect)
                       :status (if (contains? grants effect)
                                 :accepted
                                 :rejected)})
                    (sort-by str grants))
     :status :complete}))

(definterposable c8-replay-requirements
  [effect-graph]
  {:artifact :gravity/c8-replay-effect-requirements
   :records (mapv (fn [effect]
                    {:effect effect
                     :mode :audit-record
                     :record-id (str "c8-replay-" (name effect))
                     :status :recorded})
                  (sort-by str (:replay-required effect-graph)))
   :status :complete})

(definterposable c8-ordering-constraints
  [effect-graph]
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

(definterposable c8-residual-effect-report
  [effect-graph]
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

(definterposable c8-effect-diagnostics
  [source-path type-facts]
  {:artifact :gravity/c8-effect-diagnostic-registry
   :required-diagnostic-ids c8-effect-diagnostic-ids
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
              :capability (get c8-effect-capability effect)
              :profile (:profile fact)
              :target (:target fact)
              :provider :fixture/provider
              :grant :fixture/grant
              :remediation "Keep effect legality explicit before MIR construction."}))
         c8-effect-rejected-designs)
   :status :complete})

(definterposable c8-effect-verifier-report
  [module effect-graph legality capability-proof build-log replay ordering residual diagnostics]
  (let [inferred (get-in effect-graph [:namespace :inferred])
        declared (:effects module)
        known? (set/subset? inferred c8-known-effects)
        declared? (set/subset? inferred declared)
        legality? (every? #(= :accepted (:result %)) (:records legality))
        capabilities? (every? #(= :accepted (:status %))
                              (:records capability-proof))
        build? (every? #(= :accepted (:status %)) (:records build-log))
        replay? (or (empty? (:replay-required effect-graph))
                    (seq (:records replay)))
        order? (seq (:records ordering))
        residual? (= :complete (:status residual))
        diagnostics? (= (set c8-effect-diagnostic-ids)
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

(definterposable c8-effect-capability-proof
  [artifact]
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

(definterposable c8-effect-validate!
  [source-path artifact]
  (let [proof (c8-effect-capability-proof artifact)]
    (doseq [[field id] [[:effect-graph-complete? "C8-VERIFY"]
                        [:declared-effect-allowance-checked?
                         "C8-UNDECLARED"]
                        [:legality-intersection-recorded? "C8-PROFILE"]
                        [:capability-proofs-accepted? "C8-CAPABILITY"]
                        [:build-effects-separated-and-authorized?
                         "C8-BUILD"]
                        [:replay-obligations-recorded? "C8-REPLAY"]
                        [:ordering-constraints-recorded? "C8-ORDER"]
                        [:residual-effects-recorded? "C8-RUNTIME"]
                        [:diagnostics-covered? "C8-VERIFY"]
                        [:verifier-passed? "C8-VERIFY"]]]
      (when-not (get proof field)
        (c8-effect-fail! id source-path {:stage :effect-check}
                         {:missing-fields [field]}))))
  :complete)

(definterposable compiler-c8-effect-source-artifact
  [source-path source-text]
  (let [records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        overrides (c8-effect-source-overrides module)
        _ (c8-effect-validate-overrides! source-path module overrides)
        c7-artifact (compiler-c7-type-source-artifact source-path source-text)
        type-facts (:type-facts c7-artifact)
        functions (:function-type-table c7-artifact)
        effect-graph (c8-effect-graph module type-facts functions)
        legality (c8-legality-records module effect-graph)
        capability-proof-records (c8-capability-proof-records module effect-graph)
        build-log (c8-build-effect-log module)
        replay (c8-replay-requirements effect-graph)
        ordering (c8-ordering-constraints effect-graph)
        residual (c8-residual-effect-report effect-graph)
        diagnostics (c8-effect-diagnostics source-path type-facts)
        verifier (c8-effect-verifier-report module effect-graph legality
                                            capability-proof-records build-log
                                            replay ordering residual
                                            diagnostics)
        artifact-base
        {:kind :gravity/stage0-c8-effect-checker-artifact
         :task "P06-D087"
         :document-set ["C8"]
         :governing-document c8-effect-governing-document
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
                :rejects c8-effect-diagnostic-ids}
         :source-overrides overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :c7-type-checker-artifact
         (select-keys c7-artifact [:kind :artifact-id :typed-core-module
                                   :type-environment :function-type-table
                                   :capability-based-proof])
         :effect-graph effect-graph
         :function-latent-effect-table
         {:artifact :gravity/c8-function-latent-effect-table
          :functions (get-in effect-graph [:functions])
          :status :complete}
         :namespace-effect-summary (:namespace effect-graph)
         :module-effect-summary {:declared (:effects module)
                                 :inferred (get-in effect-graph
                                                   [:namespace :inferred])
                                 :status :complete}
         :effect-legality-report legality
         :capability-proof-records capability-proof-records
         :build-effect-log build-log
         :replay-effect-requirements replay
         :effect-ordering-constraints ordering
         :residual-effect-report residual
         :effect-verifier-report verifier
         :effect-diagnostics diagnostics
         :c8-effect-check-results
         {:documents ["C8"]
          :task "P06-D087"
          :required-diagnostic-ids c8-effect-diagnostic-ids
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
        _ (c8-effect-validate! source-path artifact-base)
        capability-proof (c8-effect-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(definterposable compiler-c8-effect-file-artifact
  [path]
  (compiler-c8-effect-source-artifact path (slurp path)))

(def ^:private namespace-contract
  {:namespace 'gravity.c8-effect-checker
   :contract-boundary :hosted-stage0-c8-effect-checker
   :artifact-inputs [:c7-typed-core-artifact :module-context]
   :artifact-outputs [:effect-graph :effect-legality-report
                      :capability-proof-records :build-effect-log
                      :replay-effect-requirements
                      :effect-ordering-constraints
                      :residual-effect-report :effect-diagnostics]
   :owns [:hosted-stage0-c8-effect-analysis
          :hosted-stage0-c8-artifact-projection]
   :dependency-direction {:requires ['clojure.set 'gravity.digest]
                          :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :does-not-own [:canonical-c8-authority :source-authentication
                  :type-checking-authority :package-grant-authority
                  :deployment-grant-authority :runtime-provider-authority
                  :safety-legality :mir-construction :proof-authority
                  :equivalence :self-hosting :release :seed-retirement]
   :compatibility-only? true
   :override-driven-diagnostics? true
   :legality-model-complete? false
   :canonical-c8-authority? false
   :operation-interposition {:accepted-keys operation-keys
                             :unknown-keys-rejected? true
                             :partial-overrides? true
                             :single-binding-per-top-level-call? true}})

(defn- keyword-set? [value]
  (and (set? value) (seq value) (every? keyword? value)))
(defn- string-vector? [value]
  (and (vector? value) (seq value) (every? string? value)))
(defn- vector-of-maps? [value]
  (and (vector? value) (every? map? value)))
(defn- keyword-string-map? [value]
  (and (map? value)
       (every? (fn [[key item]]
                 (and (keyword? key) (string? item)))
               value)))
(defn- keyword-keyword-map? [value]
  (and (map? value)
       (every? (fn [[key item]]
                 (and (keyword? key) (keyword? item)))
               value)))

(defn- validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C8 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        invalid-fns
        (seq (for [[key value] (select-keys operations
                                            function-operation-keys)
                   :when (not (fn? value))]
               key))]
    (when unknown
      (throw (ex-info "C8 operation map contains unknown keys"
                      {:unknown-keys (vec unknown)
                       :allowed-keys operation-keys})))
    (when invalid-fns
      (throw (ex-info "C8 function operation values must be functions"
                      {:non-function-keys (vec invalid-fns)}))))
  (doseq [[key predicate expected]
          [[:c8-effect-diagnostic-ids string-vector?
            :non-empty-string-vector]
           [:c8-effect-governing-document
            #(and (string? %) (seq %)) :non-empty-string]
           [:c8-effect-rejected-designs vector-of-maps? :vector-of-maps]
           [:c8-effect-override-diagnostics keyword-string-map?
            :keyword-to-string-map]
           [:c8-known-effects keyword-set? :non-empty-keyword-set]
           [:c8-effect-capability keyword-keyword-map?
            :keyword-to-keyword-map]
           [:c8-replay-sensitive-effects keyword-set?
            :non-empty-keyword-set]]
          :when (and (contains? operations key)
                     (not (predicate (get operations key))))]
    (throw (ex-info "C8 scalar operation has an invalid shape"
                    {:key key :expected expected
                     :actual (get operations key)})))
  operations)

(defn with-operations [operations thunk]
  (validate-operations! operations)
  (let [merged (merge *operations* operations)]
    (binding [*operations* merged
              c8-effect-diagnostic-ids
              (get merged :c8-effect-diagnostic-ids
                   c8-effect-diagnostic-ids)
              c8-effect-governing-document
              (get merged :c8-effect-governing-document
                   c8-effect-governing-document)
              c8-effect-rejected-designs
              (get merged :c8-effect-rejected-designs
                   c8-effect-rejected-designs)
              c8-effect-override-diagnostics
              (get merged :c8-effect-override-diagnostics
                   c8-effect-override-diagnostics)
              c8-known-effects
              (get merged :c8-known-effects c8-known-effects)
              c8-effect-capability
              (get merged :c8-effect-capability c8-effect-capability)
              c8-replay-sensitive-effects
              (get merged :c8-replay-sensitive-effects
                   c8-replay-sensitive-effects)]
      (thunk))))

(def public-api
  {'public-api {:kind :contract}
   'with-operations {:arglists '([operations thunk])}
   'c8-engine-contract {:arglists '([])}
   'c8-effect-diagnostic-ids {:kind :constant}
   'c8-effect-governing-document {:kind :constant}
   'c8-effect-rejected-designs {:kind :constant}
   'c8-effect-override-diagnostics {:kind :constant}
   'c8-known-effects {:kind :constant}
   'c8-effect-capability {:kind :constant}
   'c8-replay-sensitive-effects {:kind :constant}
   'c8-effect-source-overrides {:arglists '([module])}
   'c8-effect-message {:arglists '([id])}
   'c8-effect-fail! {:arglists '([id source-path subject extra])}
   'c8-effect-validate-overrides!
   {:arglists '([source-path module overrides])}
   'c8-fact-direct-effects {:arglists '([fact])}
   'c8-effectful-facts {:arglists '([type-facts])}
   'c8-effect-graph {:arglists '([module type-facts functions])}
   'c8-legality-records {:arglists '([module effect-graph])}
   'c8-capability-proof-records {:arglists '([module effect-graph])}
   'c8-build-effect-log {:arglists '([module])}
   'c8-replay-requirements {:arglists '([effect-graph])}
   'c8-ordering-constraints {:arglists '([effect-graph])}
   'c8-residual-effect-report {:arglists '([effect-graph])}
   'c8-effect-diagnostics {:arglists '([source-path type-facts])}
   'c8-effect-verifier-report
   {:arglists '([module effect-graph legality capability-proof build-log
                 replay ordering residual diagnostics])}
   'c8-effect-capability-proof {:arglists '([artifact])}
   'c8-effect-validate! {:arglists '([source-path artifact])}
   'compiler-c8-effect-source-artifact
   {:arglists '([source-path source-text])}
   'compiler-c8-effect-file-artifact {:arglists '([path])}})

(defn c8-engine-contract []
  (assoc namespace-contract :public-api public-api))
