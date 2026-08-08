(ns gravity.c17-plugin
  "Hosted Stage0 C17 plugin/pass API schema and evidence projection."
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [gravity.compiler-verification-shared :as shared]
            [gravity.digest :as digest]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private function-operation-keys
  #{:fail! :source-span :sha256-hex :c4-artifact-id
    :read-source-form-records :validate-ns-syntax! :parse-module
    :compiler-c16-incremental-source-artifact
    :c17-plugin-source-overrides :c17-plugin-fail!
    :c17-plugin-validate-source-overrides! :c17-plugin-diagnostic-stream
    :c17-plugin-validate! :c17-plugin-capability-proof
    :compiler-c17-plugin-source-artifact
    :compiler-c17-plugin-file-artifact})
(def ^:private scalar-operation-keys
  #{:compiler-verification-diagnostic-messages
    :compiler-verification-override-diagnostics
    :c17-plugin-governing-document :c17-plugin-diagnostic-ids
    :c17-plugin-manifest-required-fields
    :c17-plugin-pass-contract-required-fields
    :c17-plugin-cache-key-required-fields})
(def ^:private operation-keys
  (into function-operation-keys scalar-operation-keys))
(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key)
    (get *operations* key)))
(defmacro ^:private definterposable [name args & body]
  (let [key (keyword name)]
    `(defn ~name ~args
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys* (conj *active-operation-keys* ~key)]
           (operation# ~@args))
         (do ~@body)))))
(defn- unsupported [key]
  (fn [& _]
    (throw (ex-info (str "C17 leaf requires injected operation " key)
                    {:operation key}))))
(defn- op [key fallback] (or (get *operations* key) fallback))
(defn- fail! [id message data]
  ((op :fail! (fn [rule text payload]
                (throw (ex-info text (assoc (or payload {}) :id rule)))))
   id message data))
(defn- source-span [path index]
  ((op :source-span (fn [p i] {:source p :form-index i})) path index))
(defn- sha256-hex [value]
  ((op :sha256-hex digest/sha256-hex) value))
(defn- c4-artifact-id [value]
  ((op :c4-artifact-id
       (fn [candidate]
         (str "sha256:" (digest/sha256-hex (pr-str candidate)))))
   value))
(defn- read-source-form-records [path text]
  ((op :read-source-form-records (unsupported :read-source-form-records))
   path text))
(defn- validate-ns-syntax! [path forms]
  ((op :validate-ns-syntax! (unsupported :validate-ns-syntax!)) path forms))
(defn- parse-module [path forms]
  ((op :parse-module (unsupported :parse-module)) path forms))
(defn- compiler-c16-incremental-source-artifact [path text]
  ((op :compiler-c16-incremental-source-artifact
       (unsupported :compiler-c16-incremental-source-artifact))
   path text))
(def ^:private ^:dynamic compiler-verification-diagnostic-messages
  shared/compiler-verification-diagnostic-messages)
(def ^:private ^:dynamic compiler-verification-override-diagnostics
  shared/compiler-verification-override-diagnostics)

(def ^:dynamic c17-plugin-governing-document
  "docs/phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md")

(def ^:dynamic c17-plugin-diagnostic-ids
  ["C17-MANIFEST"
   "C17-API"
   "C17-CAPABILITY"
   "C17-BUILD-EFFECT"
   "C17-SANDBOX"
   "C17-PASS-CONTRACT"
   "C17-OUTPUT"
   "C17-DOMAIN"
   "C17-FACET"
   "C17-TRUST"])

(def ^:dynamic c17-plugin-manifest-required-fields
  [:artifact :plugin :package :api-version :compiler-compatibility :trust
   :profile :build-effects :capabilities :capability-scopes :passes
   :domains :facets :emits :conformance])

(def ^:dynamic c17-plugin-pass-contract-required-fields
  [:input :output :requires :preserves :invalidates :regenerates
   :proof-obligations :emits])

(def ^:dynamic c17-plugin-cache-key-required-fields
  [:artifact :plugin-package :plugin-version :manifest :grants
   :dependencies :replay-record :pass])

(definterposable c17-plugin-source-overrides
  [module]
  (or (get-in module [:metadata :compiler :c17-plugin])
      (get-in module [:metadata :compiler :verification])
      {}))

(definterposable c17-plugin-fail!
  [id source-path subject extra]
  (fail! id
         (get compiler-verification-diagnostic-messages id
              "compiler plugin/pass API validation failed")
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :compiler-plugin-api
                 :stage (or (:stage subject) :c17-compiler-plugin)
                 :plugin-id (:plugin-id subject)
                 :package-id (:package-id subject)
                 :version (:version subject)
                 :pass-id (:pass-id subject)
                 :manifest-entry (:manifest-entry subject)
                 :requested-capability (:requested-capability subject)
                 :trust-level (:trust-level subject)
                 :compiler-api-version (:compiler-api-version subject)
                 :artifact-id (:artifact-id subject)
                 :remediation "Load plugins through a versioned manifest, explicit trust grant, scoped compiler capabilities, sandboxed build effects, verifier-checked outputs, cache-key integration, and C17 diagnostics."}
                extra)))

(definterposable c17-plugin-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (let [[id subject-kind] (get compiler-verification-override-diagnostics
                                 fail-kind)]
      (when (contains? (set c17-plugin-diagnostic-ids) id)
        (c17-plugin-fail!
         id source-path
         {:stage subject-kind
          :plugin-id 'gravity.plugins.stage0/loop-fuser
          :package-id 'gravity/stage0-loop-fuser
          :version "0.1.0"
          :pass-id subject-kind
          :manifest-entry fail-kind
          :requested-capability :compiler/ir-transform
          :trust-level :sandboxed
          :compiler-api-version "1"
          :artifact-id (str "c17-plugin-artifact-" (name fail-kind))}
         {:missing-fields [fail-kind]})))))

(definterposable c17-plugin-diagnostic-stream
  [source-path plugin-manifest input-id]
  {:artifact :gravity/c17-plugin-diagnostic-stream
   :stage :c17-compiler-plugin
   :input-artifact input-id
   :ordering-key [:rule :plugin :pass]
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity (if (= id "C17-TRUST") :error :warning)
            :stage :c17-compiler-plugin
            :message-key (keyword "plugin"
                                  (str/lower-case
                                   (str/replace id #"_" "-")))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "c17-plugin-syntax-" index)
                      :artifact input-id}
            :plugin-id (:plugin plugin-manifest)
            :package-id (get-in plugin-manifest [:package :name])
            :version (get-in plugin-manifest [:package :version])
            :pass-id (get (:passes plugin-manifest) 0)
            :manifest-entry (keyword (str/lower-case
                                      (subs id 4)))
            :requested-capability :compiler/ir-transform
            :trust-level (:trust plugin-manifest)
            :compiler-api-version (:api-version plugin-manifest)
            :source-or-artifact-id input-id
            :facts {:manifest-hash (:manifest-hash plugin-manifest)
                    :capability-scopes
                    (get-in plugin-manifest
                            [:capability-scopes :compiler/ir-transform])
                    :rule id}
            :remediation [{:kind :repair-plugin-manifest}
                          {:kind :rerun-plugin-verifier}]
            :redactions []
            :ordering-key [id (:plugin plugin-manifest)
                           (get (:passes plugin-manifest) 0)]})
         c17-plugin-diagnostic-ids
         (range))
   :status :complete})

(definterposable c17-plugin-validate!
  [source-path artifact]
  (let [manifest (:plugin-manifest artifact)
        manifest-fields (set (keys manifest))
        pass-contract-fields (set c17-plugin-pass-contract-required-fields)
        diagnostics (get-in artifact [:plugin-diagnostic-stream
                                      :diagnostics])
        diagnostic-ids (set (map :diagnostic diagnostics))
        trust-levels (set (map :trust (:trust-grants artifact)))]
    (when-not (set/subset? (set c17-plugin-manifest-required-fields)
                           manifest-fields)
      (c17-plugin-fail! "C17-MANIFEST" source-path manifest
                        {:missing-fields
                         (vec (remove manifest-fields
                                      c17-plugin-manifest-required-fields))}))
    (when-not (= :compatible (get-in artifact
                                     [:api-compatibility-report :status]))
      (c17-plugin-fail! "C17-API" source-path
                        (:api-compatibility-report artifact)
                        {:missing-fields [:api-compatibility-report]}))
    (when-not (and (contains? (:capabilities manifest)
                              :compiler/ir-transform)
                   (set/subset?
                    #{:read-mir :write-mir :register-pass :emit-artifacts}
                    (get-in manifest
                            [:capability-scopes :compiler/ir-transform])))
      (c17-plugin-fail! "C17-CAPABILITY" source-path manifest
                        {:missing-fields [:capability-scopes]}))
    (when-not (= :denied-ungranted-effects
                 (get-in artifact
                         [:hermetic-build-effect-report :status]))
      (c17-plugin-fail! "C17-BUILD-EFFECT" source-path
                        (:hermetic-build-effect-report artifact)
                        {:missing-fields [:hermetic-build-effect-report]}))
    (when-not (and (contains? trust-levels :sandboxed)
                   (contains? trust-levels :trusted-package)
                   (every? #(contains? #{:sandboxed :granted} (:status %))
                           (:trust-grants artifact)))
      (c17-plugin-fail! "C17-SANDBOX" source-path
                        (first (:trust-grants artifact))
                        {:missing-fields [:trust-grants]}))
    (doseq [registration (:plugin-pass-registration-records artifact)]
      (when-not (set/subset? pass-contract-fields
                             (set (keys (:contract registration))))
        (c17-plugin-fail! "C17-PASS-CONTRACT" source-path registration
                          {:missing-fields
                           (vec (remove (set (keys (:contract registration)))
                                        pass-contract-fields))})))
    (when-not (every? #(= :passed (:verifier-result %))
                      (:plugin-output-artifacts artifact))
      (c17-plugin-fail! "C17-OUTPUT" source-path
                        (first (:plugin-output-artifacts artifact))
                        {:missing-fields [:verifier-result]}))
    (when-not (every? #(and (:schema %) (:verifier %)
                            (seq (:supported-profiles %))
                            (seq (:lowering-paths %)))
                      (:domain-registration-records artifact))
      (c17-plugin-fail! "C17-DOMAIN" source-path
                        (first (:domain-registration-records artifact))
                        {:missing-fields [:schema :verifier
                                          :supported-profiles
                                          :lowering-paths]}))
    (when-not (every? #(and (:schema %) (:verifier %)
                            (seq (:supported-profiles %))
                            (seq (:conformance-fixtures %)))
                      (:facet-registration-records artifact))
      (c17-plugin-fail! "C17-FACET" source-path
                        (first (:facet-registration-records artifact))
                        {:missing-fields [:schema :verifier
                                          :supported-profiles
                                          :conformance-fixtures]}))
    (when-not (= :accepted (get-in artifact
                                   [:package-trust-report :status]))
      (c17-plugin-fail! "C17-TRUST" source-path
                        (:package-trust-report artifact)
                        {:missing-fields [:package-trust-report]}))
    (when-not (= (set c17-plugin-diagnostic-ids) diagnostic-ids)
      (c17-plugin-fail! "C17-MANIFEST" source-path
                        (:plugin-diagnostic-stream artifact)
                        {:missing-fields [:plugin-diagnostics]})))
  :complete)

(definterposable c17-plugin-capability-proof
  [artifact]
  (let [manifest (:plugin-manifest artifact)
        trust-levels (set (map :trust (:trust-grants artifact)))
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:plugin-diagnostic-stream
                                       :diagnostics])))]
    {:c16-incremental-input-verified?
     (= :complete (get-in artifact
                          [:c16-incremental-artifact
                           :capability-based-proof :status]))
     :manifest-loaded?
     (and (= :gravity/compiler-plugin (:artifact manifest))
          (set/subset? (set c17-plugin-manifest-required-fields)
                       (set (keys manifest))))
     :api-compatible?
     (= :compatible (get-in artifact [:api-compatibility-report :status]))
     :sandbox-and-trust-grants?
     (and (contains? trust-levels :sandboxed)
          (contains? trust-levels :trusted-package)
          (every? #(contains? #{:sandboxed :granted} (:status %))
                  (:trust-grants artifact)))
     :capabilities-scoped?
     (and (contains? (:capabilities manifest) :compiler/ir-transform)
          (set/subset?
           #{:read-mir :write-mir :register-pass :emit-artifacts}
           (get-in manifest
                   [:capability-scopes :compiler/ir-transform])))
     :build-effect-denial-covered?
     (= :denied-ungranted-effects
        (get-in artifact [:hermetic-build-effect-report :status]))
     :pass-contracts-registered?
     (every? #(and (= :registered (:status %))
                   (set/subset?
                    (set c17-plugin-pass-contract-required-fields)
                    (set (keys (:contract %)))))
             (:plugin-pass-registration-records artifact))
     :output-artifacts-verified?
     (every? #(= :passed (:verifier-result %))
             (:plugin-output-artifacts artifact))
     :domain-and-facet-registrations-verified?
     (and (every? #(= :registered (:status %))
                  (:domain-registration-records artifact))
          (every? #(= :registered (:status %))
                  (:facet-registration-records artifact)))
     :execution-trace-cache-key-integrated?
     (and (every? #(and (= :passed (:verifier-result %))
                        (:cache-key %))
                  (:plugin-execution-traces artifact))
          (every? #(set/subset?
                    (set c17-plugin-cache-key-required-fields)
                    (set (keys %)))
                  (:plugin-cache-keys artifact)))
     :diagnostics-covered?
     (= (set c17-plugin-diagnostic-ids) diagnostics)
     :conformance-passed?
     (= :passed (get-in artifact [:plugin-conformance-results :status]))
     :status :complete}))

(definterposable compiler-c17-plugin-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (c17-plugin-source-overrides module)
        _ (c17-plugin-validate-source-overrides! source-path
                                                 source-overrides)
        incremental-artifact (compiler-c16-incremental-source-artifact
                              source-path source-text)
        input-id (:artifact-id incremental-artifact)
        manifest-base
        {:artifact :gravity/compiler-plugin
         :plugin 'gravity.plugins.stage0/loop-fuser
         :package {:name 'gravity/stage0-loop-fuser
                   :version "0.1.0"
                   :signature "sha256:c17-stage0-loop-fuser"}
         :api-version "1"
         :compiler-compatibility {:min "0.1.0" :max-exclusive "0.2.0"}
         :trust :sandboxed
         :profile :meta
         :build-effects #{}
         :capabilities #{:compiler/ir-transform :compiler/diagnostics}
         :capability-scopes {:compiler/ir-transform
                             #{:read-mir :write-mir :register-pass
                               :emit-artifacts}
                             :compiler/diagnostics #{:emit-diagnostics}}
         :passes [:fuse-adjacent-loops :emit-plugin-diagnostics]
         :domains [:stage0-loop-domain]
         :facets [:stage0-loop-fusion]
         :emits #{:optimization-decision-log :verifier-report
                  :diagnostic-stream}
         :conformance [:compiler-c17-plugin-fixtures]
         :status :accepted}
        manifest (assoc manifest-base
                        :manifest-hash
                        (str "sha256:" (sha256-hex (pr-str manifest-base))))
        sandbox-grant
        {:artifact :gravity/plugin-sandbox-grant
         :plugin (:plugin manifest)
         :package (get-in manifest [:package :name])
         :trust :sandboxed
         :status :sandboxed
         :capabilities (:capabilities manifest)
         :capability-scopes (:capability-scopes manifest)
         :build-effects (:build-effects manifest)
         :denied-authority [:filesystem/write :network/http
                            :process/spawn :environment/read
                            :compiler/hidden-state-mutation]}
        trusted-grant
        {:artifact :gravity/plugin-trust-grant
         :plugin 'gravity.plugins.stage0/proof-provider
         :package 'gravity/stage0-proof-provider
         :trust :trusted-package
         :status :granted
         :signature-status :verified
         :capabilities #{:compiler/proof-provider}
         :capability-scopes {:compiler/proof-provider
                             #{:request-proof :provide-proof}}}
        grant-hash (str "sha256:"
                        (sha256-hex
                         (pr-str [sandbox-grant trusted-grant])))
        pass-contracts
        [{:input :gravity/mir
          :output :gravity/mir
          :requires #{:dominators :effect-graph}
          :preserves #{:types :source-origins :safety-outcomes}
          :invalidates #{:dominators :loop-analysis}
          :regenerates #{:effect-ordering}
          :proof-obligations #{:effect-order-preserved}
          :emits #{:optimization-decision-log :verifier-report}}
         {:input :gravity/diagnostic-stream
          :output :gravity/diagnostic-stream
          :requires #{:diagnostic-schema :source-spans}
          :preserves #{:diagnostic-ids :source-origins}
          :invalidates #{}
          :regenerates #{}
          :proof-obligations #{:diagnostic-order-stable}
          :emits #{:diagnostic-stream}}]
        pass-registration-records
        (mapv (fn [pass contract]
                {:artifact :gravity/plugin-pass-registration
                 :plugin (:plugin manifest)
                 :pass pass
                 :contract contract
                 :api-version (:api-version manifest)
                 :capabilities #{:compiler/ir-transform}
                 :status :registered})
              (:passes manifest)
              pass-contracts)
        domain-registration-records
        [{:artifact :gravity/plugin-domain-ir-registration
          :plugin (:plugin manifest)
          :domain :stage0-loop-domain
          :schema :gravity.loop-domain/schema-v1
          :verifier :gravity.loop-domain/verify
          :supported-profiles #{:hosted :native}
          :effects #{}
          :capabilities #{:compiler/ir-transform}
          :lowering-paths [:mir :target-lowering]
          :diagnostics ["C17-DOMAIN"]
          :conformance-fixtures [:stage0-loop-domain-fixture]
          :status :registered}]
        facet-registration-records
        [{:artifact :gravity/plugin-facet-registration
          :plugin (:plugin manifest)
          :facet :stage0-loop-fusion
          :schema :gravity.loop-fusion/schema-v1
          :verifier :gravity.loop-fusion/verify
          :supported-profiles #{:hosted :native}
          :effects #{}
          :capabilities #{:compiler/ir-transform}
          :lowering-paths [:mir :target-lowering]
          :diagnostics ["C17-FACET"]
          :conformance-fixtures [:stage0-loop-fusion-fixture]
          :status :registered}]
        plugin-cache-keys
        (mapv (fn [registration]
                {:artifact :gravity/plugin-cache-key
                 :plugin-package (get-in manifest [:package :name])
                 :plugin-version (get-in manifest [:package :version])
                 :manifest (:manifest-hash manifest)
                 :grants grant-hash
                 :dependencies [input-id
                                (str "sha256:"
                                     (sha256-hex
                                      (pr-str (:contract registration))))]
                 :replay-record "sha256:c17-plugin-replay"
                 :pass (:pass registration)})
              pass-registration-records)
        output-artifacts
        [{:artifact :gravity/plugin-output-artifact
          :kind :optimization-decision-log
          :plugin (:plugin manifest)
          :pass :fuse-adjacent-loops
          :artifact-id "sha256:c17-loop-fuser-decisions"
          :verifier-result :passed
          :status :verified}
         {:artifact :gravity/plugin-output-artifact
          :kind :diagnostic-stream
          :plugin (:plugin manifest)
          :pass :emit-plugin-diagnostics
          :artifact-id "sha256:c17-plugin-diagnostics"
          :verifier-result :passed
          :status :verified}]
        execution-traces
        (mapv (fn [registration cache-key output]
                {:artifact :gravity/plugin-execution
                 :plugin (:plugin manifest)
                 :pass (:pass registration)
                 :input input-id
                 :output (:artifact-id output)
                 :grants grant-hash
                 :build-effects []
                 :decisions [(:artifact-id output)]
                 :diagnostics []
                 :verifier-result :passed
                 :sandbox-result :passed
                 :cache-key (str "sha256:"
                                 (sha256-hex (pr-str cache-key)))})
              pass-registration-records
              plugin-cache-keys
              output-artifacts)
        diagnostic-stream (c17-plugin-diagnostic-stream source-path
                                                        manifest
                                                        input-id)
        artifact-base
        {:kind :gravity/stage0-c17-compiler-plugin-artifact
         :task "P06-D096"
         :document-set ["C17"]
         :governing-document c17-plugin-governing-document
         :pass {:name :c17-compiler-plugin-api
                :input :incremental-compilation-artifact
                :output :compiler-plugin-artifact
                :requires [:c16-incremental-compilation
                           :plugin-manifest :api-version
                           :compiler-capability-grants
                           :build-effect-policy :sandbox-policy
                           :pass-contracts :verifiers]
                :preserves [:source-spans :profile :target :diagnostics
                            :proofs :cache-keys :incremental-replay]
                :emits [:plugin-manifest :api-compatibility-report
                        :sandbox-grant :trust-grants
                        :plugin-pass-registration-records
                        :domain-registration-records
                        :facet-registration-records
                        :plugin-execution-traces
                        :plugin-output-artifacts
                        :plugin-diagnostic-stream
                        :plugin-cache-keys
                        :plugin-conformance-results]
                :rejects c17-plugin-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :c16-incremental-artifact
         (select-keys incremental-artifact
                      [:kind :task :artifact-id :governing-document
                       :capability-based-proof])
         :incremental-artifact-kind (:kind incremental-artifact)
         :incremental-artifact-hash input-id
         :plugin-manifest manifest
         :api-compatibility-report
         {:artifact :gravity/plugin-api-compatibility-report
          :plugin (:plugin manifest)
          :api-version (:api-version manifest)
          :compiler-compatibility (:compiler-compatibility manifest)
          :status :compatible}
         :sandbox-grant sandbox-grant
         :trust-grants [sandbox-grant trusted-grant]
         :package-trust-report
         {:artifact :gravity/plugin-package-trust-report
          :plugin (:plugin manifest)
          :package (get-in manifest [:package :name])
          :signature-status :verified
          :policy-status :accepted
          :status :accepted}
         :hermetic-build-effect-report
         {:artifact :gravity/plugin-build-effect-denial
          :plugin (:plugin manifest)
          :mode :hermetic
          :requested #{:network/http :process/spawn}
          :granted (:build-effects manifest)
          :denied #{:network/http :process/spawn}
          :diagnostic "C17-BUILD-EFFECT"
          :status :denied-ungranted-effects}
         :plugin-pass-registration-records pass-registration-records
         :domain-registration-records domain-registration-records
         :facet-registration-records facet-registration-records
         :plugin-cache-keys plugin-cache-keys
         :plugin-output-artifacts output-artifacts
         :plugin-execution-traces execution-traces
         :plugin-diagnostic-stream diagnostic-stream
         :plugin-conformance-results
         {:artifact :gravity/plugin-conformance-results
          :task "P06-D096"
          :fixtures [:manifest-loading :api-compatibility
                     :sandboxed-execution :trusted-execution
                     :capability-scope :pass-contract
                     :output-verifier-failure :domain-registration
                     :facet-registration :build-effect-denial
                     :execution-trace-cache-key :diagnostics]
          :status :passed}
         :c17-plugin-results
         {:documents ["C17"]
          :task "P06-D096"
          :required-diagnostic-ids c17-plugin-diagnostic-ids
          :c16-input-status :complete
          :manifest-status :complete
          :api-status :complete
          :sandbox-status :complete
          :trust-status :complete
          :capability-status :complete
          :build-effect-status :complete
          :pass-contract-status :complete
          :output-verifier-status :complete
          :domain-registration-status :complete
          :facet-registration-status :complete
          :execution-trace-status :complete
          :cache-key-status :complete
          :diagnostic-status :complete
          :conformance-status :complete
          :status :complete}
         :diagnostics []}
        _ (c17-plugin-validate! source-path artifact-base)
        capability-proof (c17-plugin-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(definterposable compiler-c17-plugin-file-artifact
  [path]
  (compiler-c17-plugin-source-artifact path (slurp path)))

(def ^:private namespace-contract
  {:contract-boundary :hosted-stage0-c17-plugin-evidence
   :dependency-direction
   {:requires ['clojure.set 'clojure.string
               'gravity.compiler-verification-shared 'gravity.digest]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :owns [:hosted-stage0-c17-plugin-schema
          :hosted-stage0-c17-plugin-evidence]
   :does-not-own [:canonical-c17-authority :source-authentication
                  :plugin-discovery :plugin-loading :plugin-execution
                  :sandbox-enforcement :package-trust-authority
                  :compiler-capability-grants :build-effect-authority
                  :signature-verification :proof-authority
                  :equivalence :self-hosting :release :seed-retirement]
   :compatibility-only? true
   :plugin-runtime-implementation? false
   :plugin-model-complete? false
   :canonical-c17-authority? false
   :operation-interposition
   {:accepted-keys operation-keys
    :unknown-keys-rejected? true
    :partial-overrides? true
    :single-binding-per-top-level-call? true}})
(defn- string-vector? [value]
  (and (vector? value) (seq value) (every? string? value)))
(defn- keyword-vector? [value]
  (and (vector? value) (seq value) (every? keyword? value)))
(defn- string-map? [value]
  (and (map? value)
       (every? (fn [[key entry]] (and (string? key) (string? entry))) value)))
(defn- override-map? [value]
  (and (map? value)
       (every? (fn [[key entry]]
                 (and (keyword? key) (vector? entry) (= 2 (count entry))
                      (string? (first entry)) (keyword? (second entry))))
               value)))
(defn- validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C17 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        invalid (seq (for [[key value]
                           (select-keys operations function-operation-keys)
                           :when (not (fn? value))]
                       key))]
    (when unknown
      (throw (ex-info "C17 operation map contains unknown keys"
                      {:unknown-keys (vec unknown)})))
    (when invalid
      (throw (ex-info "C17 function operations must be functions"
                      {:non-function-keys (vec invalid)}))))
  (doseq [[key predicate]
          [[:compiler-verification-diagnostic-messages string-map?]
           [:compiler-verification-override-diagnostics override-map?]
           [:c17-plugin-governing-document #(and (string? %) (seq %))]
           [:c17-plugin-diagnostic-ids string-vector?]
           [:c17-plugin-manifest-required-fields keyword-vector?]
           [:c17-plugin-pass-contract-required-fields keyword-vector?]
           [:c17-plugin-cache-key-required-fields keyword-vector?]]
          :when (and (contains? operations key)
                     (not (predicate (get operations key))))]
    (throw (ex-info "C17 scalar operation has invalid shape" {:key key})))
  operations)
(defn with-operations [operations thunk]
  (validate-operations! operations)
  (let [merged (merge *operations* operations)]
    (binding [*operations* merged
              compiler-verification-diagnostic-messages
              (get merged :compiler-verification-diagnostic-messages
                   compiler-verification-diagnostic-messages)
              compiler-verification-override-diagnostics
              (get merged :compiler-verification-override-diagnostics
                   compiler-verification-override-diagnostics)
              c17-plugin-governing-document
              (get merged :c17-plugin-governing-document
                   c17-plugin-governing-document)
              c17-plugin-diagnostic-ids
              (get merged :c17-plugin-diagnostic-ids
                   c17-plugin-diagnostic-ids)
              c17-plugin-manifest-required-fields
              (get merged :c17-plugin-manifest-required-fields
                   c17-plugin-manifest-required-fields)
              c17-plugin-pass-contract-required-fields
              (get merged :c17-plugin-pass-contract-required-fields
                   c17-plugin-pass-contract-required-fields)
              c17-plugin-cache-key-required-fields
              (get merged :c17-plugin-cache-key-required-fields
                   c17-plugin-cache-key-required-fields)]
      (thunk))))
(def public-api
  {'public-api {:kind :contract}
   'with-operations {:arglists '([operations thunk])}
   'c17-engine-contract {:arglists '([])}
   'c17-plugin-governing-document {:kind :constant}
   'c17-plugin-diagnostic-ids {:kind :constant}
   'c17-plugin-manifest-required-fields {:kind :constant}
   'c17-plugin-pass-contract-required-fields {:kind :constant}
   'c17-plugin-cache-key-required-fields {:kind :constant}
   'c17-plugin-source-overrides {:arglists '([module])}
   'c17-plugin-fail! {:arglists '([id source-path subject extra])}
   'c17-plugin-validate-source-overrides!
   {:arglists '([source-path overrides])}
   'c17-plugin-diagnostic-stream
   {:arglists '([source-path plugin-manifest input-id])}
   'c17-plugin-validate! {:arglists '([source-path artifact])}
   'c17-plugin-capability-proof {:arglists '([artifact])}
   'compiler-c17-plugin-source-artifact
   {:arglists '([source-path source-text])}
   'compiler-c17-plugin-file-artifact {:arglists '([path])}})
(defn c17-engine-contract []
  (assoc namespace-contract :public-api public-api))
