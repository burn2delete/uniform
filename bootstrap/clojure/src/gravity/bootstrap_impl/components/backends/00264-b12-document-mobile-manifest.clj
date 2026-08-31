

(defn b12-document-mobile-manifest
  [source-path input-id]
  (let [bundle-hash (c4-artifact-id b12-document-app-bundle)
        permission-hash (c4-artifact-id b12-document-permission-manifest-fixture)]
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
      :content b12-document-app-bundle
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
      :content b12-document-permission-manifest-fixture
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