(ns gravity.backend.mobile
  "Native B12 diagnostic context, catalog and fixture data; no backend or input admission authority."
  (:require [clojure.string :as str]))

(def ^:private namespace-contract
  '{:authenticates-inputs? false,
 :public-api
 {source-overrides {:arglists ([module]), :admission :none},
  missing-policy {:arglists ([id]), :admission :none},
  domain-anchor {:arglists ([id]), :admission :none},
  diagnostic-stream
  {:arglists ([source-path input-id operations]),
   :admission :none,
   :required-operations
   [:source-span :diagnostic-ids :domain-anchor :missing-policy]},
  bundle-structurally-valid?
  {:arglists ([text]),
   :admission :none,
   :validation-scope :native-document-fixture-text},
  permission-structurally-valid?
  {:arglists ([text]),
   :admission :none,
   :validation-scope :native-document-fixture-text}},
 :supported
 [:existing-native-projection
  :borrowed-identity
  :compatibility-var-interposition
  :native-catalog-assembly
  :explicit-native-operations
  :existing-native-fixture-recognition],
 :dependency-direction
 {:requires [clojure.core clojure.string],
  :consumed-by [gravity.bootstrap],
  :forbids [gravity.bootstrap gravity.cli gravity.release]},
 :governing-contracts
 [D1 D2 D3 D8 D9 C1 C15 B1 B12 B13 B14 BOOT8 TEST2],
 :unsupported
 [:canonical-b12-semantics
  :gravity-b12-source-compilation
  :verified-mir-ui-or-platform-ingress
  :mobile-bundle-emission
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
  :fixture-match-as-capability-or-proof-authority],
 :artifact-inputs
 [:native-module-metadata
  :native-b12-diagnostic-id
  :native-source-path
  :supplied-input-artifact-id
  :native-diagnostic-operations
  :native-document-fixture-text],
 :namespace gravity.backend.mobile,
 :diagnostic-ownership
 {:stage
  [:native-override-projection
   :native-rule-context-defaults
   :native-catalog-assembly
   :native-fixture-recognition],
  :existing-owner
  [:b12-refusal-decisions
   :failure-construction
   :source-admission
   :publication]},
 :artifact-outputs
 [:borrowed-source-overrides
  :native-missing-policy
  :native-domain-anchor
  :native-diagnostic-catalog-stream
  :native-fixture-recognition-result],
 :consumer-component-paths
 ["bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00262-b11-document-source-artifact.clj"
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00263-b12-document-app-bundle.clj"],
 :issues-certificates? false,
 :contract-boundary
 :native-b12-document-context-catalog-and-fixture-data})

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

(doseq [operation [#'source-overrides #'missing-policy #'domain-anchor #'diagnostic-stream #'bundle-structurally-valid? #'permission-structurally-valid?]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
