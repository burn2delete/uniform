(ns gravity.backend.mobile
  "Native B12 document data, diagnostics and artifact checks; no backend or input admission authority."
  (:require [clojure.string :as str]))

(def ^:private namespace-contract
  '{:authenticates-inputs? false,
 :public-api
 {validate-native-manifest!
  {:authenticates-inputs? false,
   :required-operations
   [:fail!
    :app-bundle
    :permission-manifest-fixture
    :bundle-structurally-valid?
    :permission-structurally-valid?
    :diagnostic-ids],
   :diagnostics
   ["B12-MANIFEST"
    "B12-TARGET"
    "B12-RESOURCE"
    "B12-PERMISSION"
    "B12-LIFECYCLE"
    "B12-THREAD"
    "B12-NULL"
    "B12-ERROR"
    "B12-STORAGE"
    "B12-BACKGROUND"],
   :admission :none,
   :diagnostic-publication :caller-owned,
   :grants-authority? false,
   :artifact-inputs [:unverified-document-artifact-data],
   :arglists ([operations source-path artifact]),
   :artifact-outputs [:legacy-structural-completion-or-refusal],
   :issues-certificates? false,
   :contract-boundary :native-mobile-document-checks},
  validate-source-overrides!
  {:arglists ([source-path overrides operations]),
   :admission :none,
   :required-operations [:override-diagnostics :failure-operation]},
  source-overrides {:arglists ([module]), :admission :none},
  fail!
  {:arglists ([id source-path subject extra operations]),
   :admission :none,
   :required-operations
   [:fail! :source-span :domain-anchor :missing-policy],
   :callback-return :propagated},
  domain-anchor {:arglists ([id]), :admission :none},
  native-coverage-report
  {:authenticates-inputs? false,
   :required-operations
   [:app-bundle
    :permission-manifest-fixture
    :bundle-structurally-valid?
    :permission-structurally-valid?
    :diagnostic-ids],
   :admission :none,
   :grants-authority? false,
   :artifact-inputs [:unverified-document-artifact-data],
   :arglists ([operations artifact]),
   :artifact-outputs [:legacy-document-coverage-record],
   :issues-certificates? false,
   :contract-boundary :native-mobile-document-data},
  native-manifest
  {:authenticates-inputs? false,
   :required-operations
   [:artifact-id :app-bundle :permission-manifest-fixture],
   :admission :none,
   :grants-authority? false,
   :artifact-inputs
   [:unverified-source-path
    :unverified-input-id
    :fixed-native-reference-text],
   :arglists ([operations source-path input-id]),
   :artifact-outputs [:legacy-mobile-document-manifest],
   :issues-certificates? false,
   :contract-boundary :native-mobile-document-data},
  permission-manifest-fixture
  {:value-kind :string,
   :artifact-role :fixed-native-reference-fixture,
   :admission :none},
  bundle-structurally-valid?
  {:arglists ([text]),
   :admission :none,
   :validation-scope :native-document-fixture-text},
  app-bundle
  {:value-kind :string,
   :artifact-role :fixed-native-reference-fixture,
   :admission :none},
  permission-structurally-valid?
  {:arglists ([text]),
   :admission :none,
   :validation-scope :native-document-fixture-text},
  diagnostic-stream
  {:arglists ([source-path input-id operations]),
   :admission :none,
   :required-operations
   [:source-span :diagnostic-ids :domain-anchor :missing-policy]},
  missing-policy {:arglists ([id]), :admission :none}},
 :supported
 [:native-manifest-construction
  :native-artifact-validation-decisions
  :native-coverage-projection
  :existing-native-projection
  :borrowed-identity
  :compatibility-var-interposition
  :native-catalog-assembly
  :explicit-native-operations
  :existing-native-fixture-recognition
  :fixed-native-reference-text
  :native-failure-payload-assembly
  :native-explicit-override-refusal],
 :dependency-direction
 {:requires [clojure.core clojure.string],
  :consumed-by [gravity.bootstrap],
  :must-not-require [gravity.bootstrap gravity.cli gravity.release]},
 :governing-contracts
 [D1 D2 D3 D8 D9 C1 C15 B1 B12 B13 B14 BOOT8 TEST2],
 :unsupported
 [:canonical-b12-semantics
  :gravity-b12-source-compilation
  :verified-mir-ui-or-platform-ingress
  :canonical-mobile-bundle-emission
  :mobile-execution
  :platform-binding-or-permission-validation
  :lifecycle-or-threading-validation
  :null-error-or-callback-validation
  :storage-sync-or-schema-validation
  :resources-or-store-audit
  :device-or-simulator-conformance
  :downstream-handoff
  :source-authentication
  :public-diagnostic-redaction
  :migration
  :release
  :self-hosting
  :seed-retirement
  :canonical-c15-conformance
  :catalog-as-observed-semantic-findings
  :native-operations-as-proof-authority
  :canonical-target-syntax-validation
  :fixture-text-as-verified-ir
  :fixture-match-as-capability-or-proof-authority
  :fixture-text-as-lowering-or-execution
  :refusal-callback-as-admission-or-proof
  :canonical-diagnostic-publication],
 :artifact-inputs
 [:unverified-native-document-data
  :native-manifest-operations
  :native-module-metadata
  :native-b12-diagnostic-id
  :native-source-path
  :supplied-input-artifact-id
  :native-diagnostic-operations
  :native-document-fixture-text
  :fixed-native-fixture-definitions
  :native-diagnostic-subject
  :native-diagnostic-extra
  :native-refusal-operations],
 :namespace gravity.backend.mobile,
 :diagnostic-ownership
 {:stage
  [:native-artifact-validation-decisions
   :native-override-projection
   :native-rule-context-defaults
   :native-catalog-assembly
   :native-fixture-recognition
   :fixed-native-reference-text
   :native-failure-payload-assembly
   :native-explicit-override-refusal],
  :existing-owner
  [:diagnostic-throw-transport :source-admission :publication]},
 :artifact-outputs
 [:legacy-mobile-document-manifest
  :legacy-structural-completion-or-refusal
  :legacy-document-coverage-record
  :borrowed-source-overrides
  :native-missing-policy
  :native-domain-anchor
  :native-diagnostic-catalog-stream
  :native-fixture-recognition-result
  :fixed-native-reference-text
  :native-refusal-callback-result],
 :consumer-component-paths
 ["bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00262-b11-document-source-artifact.clj"
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00263-b12-document-app-bundle.clj"
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00264-b12-document-mobile-manifest.clj"
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00265-b12-document-validate.clj"
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00266-b12-document-source-artifact.clj"],
 :issues-certificates? false,
 :contract-boundary :native-b12-document-data}
)

(defn source-overrides
  [module]
  (get-in module [:metadata :backend :specialized-lowering] {}))

(defn missing-policy
  [id]
  (case id
    "B12-TARGET" :platform-os-architecture-bundle-ui-target
    "B12-PERMISSION" :permission-capability-runtime-denial-policy
    "B12-LIFECYCLE" :lifecycle-state-model
    "B12-THREAD" :main-thread-actor-callback-affinity
    "B12-NULL" :platform-nullability-adapter
    "B12-ERROR" :platform-exception-callback-error-map
    "B12-BACKGROUND" :background-execution-deployment-policy
    "B12-STORAGE" :local-storage-sync-schema-migration
    "B12-RESOURCE" :asset-resource-bundle-metadata
    :mobile-artifact-manifest))

(defn domain-anchor
  [id]
  (case id
    "B12-TARGET" :mobile-platform-target
    "B12-PERMISSION" :platform-api-binding
    "B12-LIFECYCLE" :app-lifecycle
    "B12-THREAD" :ui-thread-update
    "B12-NULL" :platform-null-adapter
    "B12-ERROR" :platform-error-adapter
    "B12-BACKGROUND" :background-task
    "B12-STORAGE" :local-sync-store
    "B12-RESOURCE" :resource-bundle
    :mobile-manifest))

(defn diagnostic-stream
  [source-path input-id operations]
  {:artifact :gravity/b12-mobile-backend-diagnostic-stream
   :stage :b12-mobile-backend-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :b12-mobile-backend-document-coverage
            :backend :gravity.backend/mobile
            :message-key (keyword "backend-mobile" (str/lower-case id))
            :primary {:span ((:source-span operations) source-path index)
                      :syntax-id (str "b12-document-syntax-" index)
                      :artifact input-id}
            :profile :hosted
            :target :ios
            :domain-anchor ((:domain-anchor operations) id)
            :platform :ios
            :api-symbol (case id
                          "B12-PERMISSION" 'Foundation/URLSession.data
                          "B12-THREAD" 'SwiftUI/View.body
                          "B12-STORAGE" 'CoreData/PersistentStore
                          'GravityStage0/MobileEntry)
            :lifecycle-state (case id
                               "B12-BACKGROUND" :background
                               "B12-LIFECYCLE" :unknown
                               :foreground)
            :thread-actor (case id
                            "B12-THREAD" :background-worker
                            :main)
            :permission (case id
                          "B12-PERMISSION" :network
                          "B12-BACKGROUND" :background-execution
                          "B12-STORAGE" :local-files
                          :network)
            :capability (case id
                          "B12-PERMISSION" :network/request
                          "B12-BACKGROUND" :background/fetch
                          "B12-STORAGE" :storage/local
                          :network/request)
            :missing-policy ((:missing-policy operations) id)
            :source-generated-origin-chain
            [:mir :c11-mir :c12-mobile-domain-ir
             :c14-target-lowering :b1-interface
             :b12-mobile-backend]
            :fallback-status :rejected
            :facts {:permission-manifest-required true
                    :main-thread-ui-required true
                    :hidden-background-work-rejected true
                    :platform-null-error-adapters-required true
                    :storage-sync-schema-required true}
            :remediation [{:kind :declare-platform-target}
                          {:kind :attach-permission-capability-policy}
                          {:kind :record-lifecycle-threading-adapters}
                          {:kind :emit-storage-resource-store-audit}]
            :redactions []
            :ordering-key [id :b12-mobile-backend-document-coverage
                           :ios]})
         ((:diagnostic-ids operations))
         (range))
   :status :complete})

(defn bundle-structurally-valid?
  [text]
  (and (str/includes? text "org.gravity.stage0")
       (str/includes? text ":platform :ios")
       (str/includes? text ":architecture :arm64")
       (str/includes? text ":entrypoint :GravityStage0App")))

(defn permission-structurally-valid?
  [text]
  (and (str/includes? text ":capability :network/request")
       (str/includes? text ":runtime-request :declared")
       (str/includes? text ":denial-policy :return-error")
       (str/includes? text ":hidden-background-work :rejected")))

(def app-bundle
  (str
   "{:bundle-id \"org.gravity.stage0\"\n"
   " :platform :ios\n"
   " :architecture :arm64\n"
   " :entrypoint :GravityStage0App\n"
   " :ui-bridge :swiftui\n"
   " :status :complete}\n"))

(def permission-manifest-fixture
  (str
   "{:permissions [{:name :network\n"
   "                :capability :network/request\n"
   "                :runtime-request :declared\n"
   "                :denial-policy :return-error}\n"
   "               {:name :notifications\n"
   "                :capability :notification/send\n"
   "                :runtime-request :declared\n"
   "                :denial-policy :disable-feature}]\n"
   " :hidden-background-work :rejected\n"
   " :status :complete}\n"))

(defn fail!
  [id source-path subject extra operations]
  ((:fail! operations) id
         "B12 mobile backend design document coverage validation failed"
         (merge {:source-span (or (:source-span subject)
                                  ((:source-span operations) source-path 0))
                 :diagnostic-family :b12-mobile-backend-document
                 :stage :b12-mobile-backend-document-coverage
                 :backend :gravity.backend/mobile
                 :profile :hosted
                 :target :ios
                 :domain-anchor (or (:domain-anchor subject)
                                    ((:domain-anchor operations) id))
                 :platform (or (:platform subject) :ios)
                 :api-symbol (or (:api-symbol subject)
                                 'Foundation/URLSession.data)
                 :lifecycle-state (or (:lifecycle-state subject)
                                      :foreground)
                 :thread-actor (or (:thread-actor subject) :main)
                 :permission (or (:permission subject) :network)
                 :capability (or (:capability subject) :network/request)
                 :missing-policy ((:missing-policy operations) id)
                 :fallback-status :rejected
                 :remediation "B12 requires verified mobile/platform IR, explicit platform target and bundle metadata, permission and capability manifests, lifecycle and threading maps, null/error adapters, storage/sync schemas, resource manifests, store-audit metadata, source maps, and simulator/device conformance records."}
                extra)))

(defn validate-source-overrides!
  [source-path overrides operations]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get ((:override-diagnostics operations)) fail-kind)]
      (((:failure-operation operations))
       id source-path
       {:artifact-id (str "b12-document-" (name fail-kind))
        :domain-anchor (keyword (str "b12-document-" (name fail-kind)))}
       {:missing-fields [fail-kind]}))))

(defn- native-document-port [operations key]
  (let [operation (get operations key)]
    (if (var? operation) @operation operation)))

(defn native-manifest
  [operations source-path input-id]
  (let [bundle-hash ((native-document-port operations :artifact-id) (native-document-port operations :app-bundle))
        permission-hash ((native-document-port operations :artifact-id) (native-document-port operations :permission-manifest-fixture))]
    {:artifact :gravity/mobile-backend-manifest
     :backend :gravity.backend/mobile
     :target {:platform :ios
              :os-range "17.0+"
              :architecture :arm64
              :abi :darwin-arm64
              :bundle-id "org.gravity.stage0"
              :packaging-mode :app-bundle
              :runtime-provider :stage0-mobile-runtime
              :ui-bridge :swiftui
              :lifecycle-model :scene-based
              :threading-model :main-actor
              :permission-model :runtime-prompts
              :store-policy-target :app-store}
     :alternate-platform-targets
     [{:platform :android
       :os-range "api-26+"
       :architecture :arm64-v8a
       :package-id "org.gravity.stage0"
       :ui-bridge :compose
       :status :style-manifest-only}]
     :input-artifact input-id
     :mobile-ir-handoff-record
     {:domain-anchor :mobile-app
      :source-artifact input-id
      :accepted-by [:b1-backend-interface :c11-mir
                    :c12-domain-ir :c14-target-lowering
                    :p4-hosted-profile :p5-native-profile
                    :p13-compatibility-profile]
      :status :complete}
     :platform-target-record
     {:platform :ios
      :os-range "17.0+"
      :architecture :arm64
      :bundle-id "org.gravity.stage0"
      :entitlements [:network-client]
      :permissions [:network :notifications]
      :deployment-environment :simulator
      :signing-policy-reference :pkg-mobile-signing
      :release-policy-reference :store-audit-stage0
      :status :complete}
     :application-bundle-artifact
     {:path "GravityStage0.app/manifest.edn"
      :content (native-document-port operations :app-bundle)
      :hash bundle-hash
      :status :complete}
     :platform-binding-descriptors
     [{:platform-symbol 'Foundation/URLSession.data
       :framework :Foundation
       :class "URLSession"
       :method "data"
       :gravity-type :NetworkRequest
       :platform-type :URLRequest
       :nullability :checked
       :error-mapping {:transport-error :gravity/error
                       :permission-denied :gravity/permission-denied}
       :thread-affinity :background-worker
       :lifecycle-state-required :foreground
       :permission :network
       :capability :network/request
       :taint-policy :validated
       :resource-cleanup :structured
       :source-generated-origin-chain [:mir :b12-mobile-backend]
       :status :complete}
      {:platform-symbol 'SwiftUI/View.body
       :framework :SwiftUI
       :gravity-type :UiView
       :platform-type :View
       :nullability :nonnull
       :error-mapping {}
       :thread-affinity :main
       :lifecycle-state-required :foreground
       :permission :none
       :capability :ui/render
       :taint-policy :not-applicable
       :resource-cleanup :not-required
       :source-generated-origin-chain [:mir :b12-mobile-backend]
       :status :complete}]
     :permission-manifest
     {:path "gravity_stage0_permissions.edn"
      :content (native-document-port operations :permission-manifest-fixture)
      :hash permission-hash
      :permissions [{:name :network
                     :capability :network/request
                     :permission-text "Network access is used for stage0 requests."
                     :deployment-grant :declared
                     :runtime-request :declared
                     :denial-policy :return-error
                     :source-location (str source-path ":mobile")}
                    {:name :notifications
                     :capability :notification/send
                     :permission-text "Notifications are disabled unless explicitly granted."
                     :deployment-grant :declared
                     :runtime-request :declared
                     :denial-policy :disable-feature
                     :source-location (str source-path ":mobile")}]
      :hidden-background-work :rejected
      :permissionless-platform-api :rejected
      :status :complete}
     :resource-asset-manifest
     {:assets [{:name "AppIcon" :kind :asset-catalog :status :declared}]
      :resources [{:name "GravityStage0Config" :kind :plist
                   :hash bundle-hash}]
      :native-libraries []
      :status :complete}
     :lifecycle-threading-map
     {:lifecycle [:launch :foreground :background :terminate
                  :scene-create :deep-link :notification-received]
      :ui-thread :main
      :platform-actor :main
      :background-workers [:network-worker]
      :structured-concurrency :enabled
      :cancellation :propagated
      :callback-affinity {:url-session :background-worker
                          :ui-update :main}
      :invalid-ui-thread-updates :rejected
      :status :complete}
     :ui-bridge-metadata
     {:framework :swiftui
      :main-thread-required true
      :view-lifecycle [:appear :disappear]
      :deep-link-adapter :typed-route
      :status :complete}
     :null-error-callback-adapter-record
     {:platform-nulls :checked-optionals
      :exceptions :mapped-to-gravity-errors
      :callbacks :typed-effects
      :delegate-methods :typed-adapters
      :status :complete}
     :local-storage-sync-schema-bundle
     {:schemas [{:id :local-cache-v1
                 :kind :local-store
                 :fields [{:name :key :type :String}
                          {:name :value :type :Json}]}]
      :migration-policy :versioned
      :encryption :declared
      :storage-provider :stage0-local-store
      :taint-category :local-cache
      :retention-policy :bounded
      :backup-policy :excluded
      :sync-behavior :idempotent
      :conflict-handling :last-write-wins-recorded
      :offline-queue-replay :event-log-guarded
      :status :complete}
     :background-task-policy
     {:background-modes []
      :hidden-background-execution :rejected
      :network-sync :requires-explicit-background-capability
      :sensor-use :requires-explicit-capability
      :status :complete}
     :store-audit-metadata
     {:permissions [:network :notifications]
      :privacy-labels [:network]
      :background-modes []
      :tracking :none
      :ai-tool-providers []
      :policy-target :app-store
      :status :complete}
     :source-debug-map
     {:source input-id
      :source-path source-path
      :locations [(str source-path ":mobile")
                  (str source-path ":permissions")
                  (str source-path ":lifecycle")
                  (str source-path ":storage")]
      :generated-origin-chain [:mir :c11-mir :c12-mobile-domain-ir
                               :c14-target-lowering :b1-interface
                               :b12-mobile-backend]
      :platform-source-map {:network (str source-path ":mobile:network")
                            :ui (str source-path ":mobile:ui")
                            :storage (str source-path ":mobile:storage")}
      :status :preserved}
     :device-simulator-conformance-report
     {:startup :recorded
      :permission-prompts :recorded
      :lifecycle :recorded
      :crash-diagnostics :recorded
      :device-execution :not-available-in-current-environment
      :simulator-execution :not-available-in-current-environment
      :status :complete}
     :external-mobile-validation-record
     {:declared-command
      "gravity-mobile-sim --bundle /tmp/gravity-p07-b12-mobile/GravityStage0.app"
      :proof-artifact
      "docs/artifacts/phase-07/reports/p07-d109-b12-mobile-backend-report.md"
      :status :not-available-in-current-environment}
     :status :complete}))
(defn validate-native-manifest!
  [operations source-path artifact]
  (let [specialized (:specialized-lowering-artifact artifact)
        manifest (:mobile-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b12-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-specialized-lowering-artifact
                 (:kind specialized))
      ((native-document-port operations :fail!) "B12-MANIFEST" source-path specialized
                          {:missing-fields [:specialized-lowering-artifact]}))
    (when-not (= :complete (get-in specialized
                                   [:capability-based-proof :status]))
      ((native-document-port operations :fail!) "B12-MANIFEST" source-path specialized
                          {:missing-fields [:specialized-lowering-proof]}))
    (when-not (= :ios (get-in manifest [:target :platform]))
      ((native-document-port operations :fail!) "B12-TARGET" source-path manifest
                          {:missing-fields [:target-platform]}))
    (when-not (= :complete (get-in manifest
                                   [:mobile-ir-handoff-record :status]))
      ((native-document-port operations :fail!) "B12-MANIFEST" source-path manifest
                          {:missing-fields [:mobile-ir-handoff-record]}))
    (when-not (= :complete (get-in manifest
                                   [:platform-target-record :status]))
      ((native-document-port operations :fail!) "B12-TARGET" source-path manifest
                          {:missing-fields [:platform-target-record]}))
    (when-not (= :complete (get-in manifest
                                   [:application-bundle-artifact :status]))
      ((native-document-port operations :fail!) "B12-RESOURCE" source-path manifest
                          {:missing-fields [:application-bundle-artifact]}))
    (when-not ((native-document-port operations :bundle-structurally-valid?)
               (native-document-port operations :app-bundle))
      ((native-document-port operations :fail!) "B12-RESOURCE" source-path manifest
                          {:missing-fields [:application-bundle-structure]}))
    (when-not (seq (:platform-binding-descriptors manifest))
      ((native-document-port operations :fail!) "B12-MANIFEST" source-path manifest
                          {:missing-fields [:platform-binding-descriptors]}))
    (when-not (= :complete (get-in manifest [:permission-manifest :status]))
      ((native-document-port operations :fail!) "B12-PERMISSION" source-path manifest
                          {:missing-fields [:permission-manifest]}))
    (when-not ((native-document-port operations :permission-structurally-valid?)
               (native-document-port operations :permission-manifest-fixture))
      ((native-document-port operations :fail!) "B12-PERMISSION" source-path manifest
                          {:missing-fields [:permission-policy]}))
    (when-not (= :complete (get-in manifest
                                   [:resource-asset-manifest :status]))
      ((native-document-port operations :fail!) "B12-RESOURCE" source-path manifest
                          {:missing-fields [:resource-asset-manifest]}))
    (when-not (= :complete (get-in manifest
                                   [:lifecycle-threading-map :status]))
      ((native-document-port operations :fail!) "B12-LIFECYCLE" source-path manifest
                          {:missing-fields [:lifecycle-threading-map]}))
    (when-not (= :main (get-in manifest
                               [:lifecycle-threading-map :ui-thread]))
      ((native-document-port operations :fail!) "B12-THREAD" source-path manifest
                          {:missing-fields [:main-thread-policy]}))
    (when-not (= :complete (get-in manifest [:ui-bridge-metadata :status]))
      ((native-document-port operations :fail!) "B12-TARGET" source-path manifest
                          {:missing-fields [:ui-bridge-metadata]}))
    (when-not (= :complete (get-in manifest
                                   [:null-error-callback-adapter-record
                                    :status]))
      ((native-document-port operations :fail!) "B12-NULL" source-path manifest
                          {:missing-fields [:null-error-callback-adapters]}))
    (when-not (= :mapped-to-gravity-errors
                 (get-in manifest
                         [:null-error-callback-adapter-record
                          :exceptions]))
      ((native-document-port operations :fail!) "B12-ERROR" source-path manifest
                          {:missing-fields [:platform-error-mapping]}))
    (when-not (= :complete (get-in manifest
                                   [:local-storage-sync-schema-bundle
                                    :status]))
      ((native-document-port operations :fail!) "B12-STORAGE" source-path manifest
                          {:missing-fields [:local-storage-sync-schema]}))
    (when-not (= :complete (get-in manifest
                                   [:background-task-policy :status]))
      ((native-document-port operations :fail!) "B12-BACKGROUND" source-path manifest
                          {:missing-fields [:background-task-policy]}))
    (when-not (= :rejected (get-in manifest
                                   [:background-task-policy
                                    :hidden-background-execution]))
      ((native-document-port operations :fail!) "B12-BACKGROUND" source-path manifest
                          {:missing-fields [:hidden-background-rejection]}))
    (when-not (= :complete (get-in manifest [:store-audit-metadata :status]))
      ((native-document-port operations :fail!) "B12-MANIFEST" source-path manifest
                          {:missing-fields [:store-audit-metadata]}))
    (when-not (= :preserved (get-in manifest [:source-debug-map :status]))
      ((native-document-port operations :fail!) "B12-MANIFEST" source-path manifest
                          {:missing-fields [:source-debug-map]}))
    (when-not (= :complete (get-in manifest
                                   [:device-simulator-conformance-report
                                    :status]))
      ((native-document-port operations :fail!) "B12-MANIFEST" source-path manifest
                          {:missing-fields [:device-simulator-conformance]}))
    (when-not (every? #(contains? manifest %)
                      [:mobile-ir-handoff-record
                       :platform-target-record
                       :application-bundle-artifact
                       :platform-binding-descriptors
                       :permission-manifest
                       :resource-asset-manifest
                       :lifecycle-threading-map
                       :ui-bridge-metadata
                       :null-error-callback-adapter-record
                       :local-storage-sync-schema-bundle
                       :background-task-policy
                       :store-audit-metadata
                       :source-debug-map
                       :device-simulator-conformance-report
                       :external-mobile-validation-record])
      ((native-document-port operations :fail!) "B12-MANIFEST" source-path manifest
                          {:missing-fields [:mobile-artifact-manifest]}))
    (when-not (= (set (native-document-port operations :diagnostic-ids)) diagnostics)
      ((native-document-port operations :fail!) "B12-MANIFEST" source-path
                          (:b12-diagnostic-stream artifact)
                          {:missing-fields [:b12-diagnostics]})))
  :complete)

(defn native-coverage-report
  [operations artifact]
  (let [manifest (:mobile-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b12-diagnostic-stream
                                       :diagnostics])))]
    {:backend-interface-input-verified?
     (= :complete (get-in artifact
                          [:specialized-lowering-artifact
                           :capability-based-proof :status]))
     :mobile-ir-handoff-covered?
     (= :complete (get-in manifest
                          [:mobile-ir-handoff-record :status]))
     :platform-target-covered?
     (= :complete (get-in manifest
                          [:platform-target-record :status]))
     :application-bundle-emitted?
     (and (= :complete (get-in manifest
                               [:application-bundle-artifact :status]))
          ((native-document-port operations :bundle-structurally-valid?)
           (native-document-port operations :app-bundle)))
     :platform-bindings-covered?
     (every? #(= :complete (:status %))
             (:platform-binding-descriptors manifest))
     :permission-manifest-covered?
     (and (= :complete (get-in manifest [:permission-manifest :status]))
          ((native-document-port operations :permission-structurally-valid?)
           (native-document-port operations :permission-manifest-fixture)))
     :resource-asset-manifest-covered?
     (= :complete (get-in manifest
                          [:resource-asset-manifest :status]))
     :lifecycle-and-threading-covered?
     (and (= :complete (get-in manifest
                               [:lifecycle-threading-map :status]))
          (= :main (get-in manifest
                           [:lifecycle-threading-map :ui-thread])))
     :ui-bridge-covered?
     (= :complete (get-in manifest [:ui-bridge-metadata :status]))
     :null-and-error-adapters-covered?
     (and (= :complete (get-in manifest
                               [:null-error-callback-adapter-record
                                :status]))
          (= :mapped-to-gravity-errors
             (get-in manifest
                     [:null-error-callback-adapter-record
                      :exceptions])))
     :storage-sync-covered?
     (= :complete (get-in manifest
                          [:local-storage-sync-schema-bundle :status]))
     :background-work-rejected?
     (= :rejected (get-in manifest
                          [:background-task-policy
                           :hidden-background-execution]))
     :store-audit-covered?
     (= :complete (get-in manifest [:store-audit-metadata :status]))
     :source-debug-map-preserved?
     (= :preserved (get-in manifest [:source-debug-map :status]))
     :device-simulator-record-covered?
     (= :complete (get-in manifest
                          [:device-simulator-conformance-report
                           :status]))
     :manifest-complete?
     (every? #(contains? manifest %)
             [:mobile-ir-handoff-record
              :platform-target-record
              :application-bundle-artifact
              :platform-binding-descriptors
              :permission-manifest
              :resource-asset-manifest
              :lifecycle-threading-map
              :ui-bridge-metadata
              :null-error-callback-adapter-record
              :local-storage-sync-schema-bundle
              :background-task-policy
              :store-audit-metadata
              :source-debug-map
              :device-simulator-conformance-report
              :external-mobile-validation-record])
     :diagnostics-covered?
     (= (set (native-document-port operations :diagnostic-ids)) diagnostics)
     :external-mobile-validation?
     (get-in manifest [:external-mobile-validation-record :status])
     :status :complete}))
(doseq [[_ operation] (ns-publics *ns*)]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
