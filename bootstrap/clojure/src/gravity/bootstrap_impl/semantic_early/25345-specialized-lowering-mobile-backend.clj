; Semantic decomposition of HEAD reader line 25345.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-specialized-lowering-mobile-backend
 [source-path state]
 (let
  [{:keys [mobile-manifest mobile-bundle]} state]
  (assoc
   {}
   :mobile-backend
   {:local-storage-sync-schema-bundle
    {:schemas [:local-cache],
     :migration-policy :versioned,
     :sync-policy :idempotent,
     :status :complete},
    :platform-target-record
    {:platform :ios,
     :os-range "stage0",
     :architecture :arm64,
     :bundle-id "org.gravity.stage0",
     :status :complete},
    :permission-manifest
    {:permissions
     [{:name :network,
       :capability :network/request,
       :runtime-request :declared,
       :denial-policy :return-error}],
     :status :complete},
    :status :complete,
    :device-simulator-conformance-report
    {:startup :recorded,
     :permission-prompts :recorded,
     :lifecycle :recorded,
     :status :complete},
    :lifecycle-threading-map
    {:lifecycle [:launch :foreground :background],
     :ui-thread :main,
     :callbacks :declared,
     :status :complete},
    :artifact :gravity/mobile-backend-manifest,
    :platform-binding-descriptors
    [{:symbol :network/request,
      :framework :Foundation,
      :permission :network,
      :capability :network/request,
      :thread-affinity :background}],
    :store-audit-metadata
    {:permissions [:network], :background-modes [], :status :complete},
    :application-bundle-artifact
    {:content mobile-bundle,
     :hash (:content-hash mobile-manifest),
     :status :complete},
    :backend :gravity.backend/mobile,
    :ui-bridge-metadata
    {:framework :stage0-ui,
     :main-thread-required true,
     :status :complete},
    :resource-asset-manifest
    {:assets [], :resources [], :status :complete}})))
