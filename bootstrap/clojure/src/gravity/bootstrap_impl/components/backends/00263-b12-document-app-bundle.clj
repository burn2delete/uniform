

(def b12-document-app-bundle
  (str
   "{:bundle-id \"org.gravity.stage0\"\n"
   " :platform :ios\n"
   " :architecture :arm64\n"
   " :entrypoint :GravityStage0App\n"
   " :ui-bridge :swiftui\n"
   " :status :complete}\n"))

(def b12-document-permission-manifest-fixture
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

(defn b12-document-bundle-structurally-valid?
  [text]
  (and (str/includes? text "org.gravity.stage0")
       (str/includes? text ":platform :ios")
       (str/includes? text ":architecture :arm64")
       (str/includes? text ":entrypoint :GravityStage0App")))

(defn b12-document-permission-structurally-valid?
  [text]
  (and (str/includes? text ":capability :network/request")
       (str/includes? text ":runtime-request :declared")
       (str/includes? text ":denial-policy :return-error")
       (str/includes? text ":hidden-background-work :rejected")))

(defn b12-document-diagnostic-stream
  [source-path input-id]
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
            :primary {:span (source-span source-path index)
                      :syntax-id (str "b12-document-syntax-" index)
                      :artifact input-id}
            :profile :hosted
            :target :ios
            :domain-anchor (b12-document-domain-anchor id)
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
            :missing-policy (b12-document-missing-policy id)
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
         b12-document-diagnostic-ids
         (range))
   :status :complete})