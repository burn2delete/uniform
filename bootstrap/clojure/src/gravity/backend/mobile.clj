(ns gravity.backend.mobile
  "Native B12 diagnostic lookup data; no mobile or input admission authority.")

(def ^:private namespace-contract
  '{:authenticates-inputs? false,
 :public-api
 {source-overrides {:arglists ([module]), :admission :none},
  missing-policy {:arglists ([id]), :admission :none},
  domain-anchor {:arglists ([id]), :admission :none}},
 :supported
 [:existing-native-projection
  :borrowed-identity
  :compatibility-var-interposition],
 :dependency-direction
 {:requires [clojure.core],
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
  :seed-retirement],
 :artifact-inputs [:native-module-metadata :native-b12-diagnostic-id],
 :namespace gravity.backend.mobile,
 :diagnostic-ownership
 {:stage [:native-override-projection :native-rule-context-defaults],
  :existing-owner
  [:b12-refusal-decisions
   :failure-construction
   :diagnostic-stream
   :source-admission
   :publication]},
 :artifact-outputs
 [:borrowed-source-overrides
  :native-missing-policy
  :native-domain-anchor],
 :issues-certificates? false,
 :contract-boundary :native-b12-diagnostic-policy-data})

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

(doseq [operation [#'source-overrides #'missing-policy #'domain-anchor]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
