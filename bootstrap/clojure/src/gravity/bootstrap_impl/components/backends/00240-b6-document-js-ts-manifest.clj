

(defn b6-document-js-ts-manifest
  [input-id]
  (let [js-hash (c4-artifact-id b6-document-js-source)
        ts-hash (c4-artifact-id b6-document-ts-declarations)
        source-map-hash (c4-artifact-id b6-document-source-map)
        package-hash (c4-artifact-id b6-document-package-json)]
    {:artifact :gravity/js-ts-backend-manifest
     :backend :gravity.backend/js-ts
     :target {:runtime :browser
              :ecmascript :es2022
              :module-format :esm}
     :emits #{:javascript :typescript-declarations :source-map
              :package-manifest}
     :requires #{:runtime-provider-manifest :capability-manifest
                 :async-effect-map :numeric-representation-map}
     :rejects #{:ambient-global-access :dynamic-eval
                :unchecked-nullish-flow :untyped-package-import
                :lossy-number-lowering :prototype-mutation}
     :runtime-and-module-target-record
     {:runtime :browser
      :ecmascript :es2022
      :module-format :esm
      :bundler-policy :explicit-side-effect-free-package
      :package-boundary :exports-map
      :tree-shaking-assumptions :side-effects-false
      :source-map-strategy :external-v3
      :worker-thread-event-loop-model :browser-event-loop
      :package-manager-lock-metadata :stage0-none
      :supported-builtins [:Promise :BigInt :Int32Array :Object.freeze
                           :globalThis :Error :Number.isFinite]
      :polyfills []
      :status :pinned}
     :javascript-module-artifacts
     [{:path "gravity-stage0.mjs"
       :content b6-document-js-source
       :hash js-hash
       :module-format :esm
       :status :complete}]
     :typescript-declaration-files
     [{:path "gravity-stage0.d.ts"
       :content b6-document-ts-declarations
       :hash ts-hash
       :status :complete}]
     :source-maps-and-generated-origin-maps
     [{:path "gravity-stage0.mjs.map"
       :content b6-document-source-map
       :hash source-map-hash
       :source input-id
       :generated-origin-chain [:mir :c14-target-lowering
                                :b1-interface :b6-js-ts-backend]
       :status :complete}]
     :package-metadata
     {:path "package.json"
      :content b6-document-package-json
      :hash package-hash
      :package-name "@gravity/stage0-js-ts"
      :type :module
      :side-effects false
      :status :complete}
     :value-and-type-representation-record
     {:representations [{:gravity-type :I64
                         :javascript "bigint"
                         :typescript "bigint"
                         :nullish :not-allowed
                         :numeric-representation :BigInt
                         :mutability :immutable
                         :equality :strict
                         :serialization-schema :string-at-json-boundary
                         :taint-policy :validated}
                        {:gravity-type :F64
                         :javascript "number"
                         :typescript "number"
                         :nullish :not-allowed
                         :numeric-representation :Number
                         :boundary-validation :finite
                         :serialization-schema :number-when-finite
                         :taint-policy :validated}
                        {:gravity-type :Packet
                         :javascript "frozen object"
                         :typescript "Readonly<Record<string, unknown>>"
                         :nullish :option-wrapper
                         :object-layout :prototype-opaque-frozen
                         :serialization-schema :phase10-schema
                         :taint-policy :validated}]
      :typescript-declarations-are-api-evidence-not-safety-source true
      :runtime-validation-required-for-untrusted-input true
      :status :complete}
     :capability-manifest
     {:host-globals [{:symbol "globalThis"
                      :effects #{}
                      :capabilities #{}
                      :schema :opaque-host-global
                      :trust :opaque
                      :taint :validated-wrapper
                      :browser true
                      :node true
                      :edge true
                      :tree-shaking :safe}]
      :package-imports []
      :dynamic-import-policy :effectful-denied-by-default
      :status :declared}
     :package-dependency-manifest
     {:dependencies []
      :imports []
      :accepted-fixtures [{:package "@gravity/stage0"
                           :typed-wrapper :generated
                           :integrity :package-lock-or-sri-required
                           :status :accepted}]
      :rejected-fixtures [{:package "untyped-side-effectful-package"
                           :diagnostic "B6-IMPORT"
                           :status :rejected}]
      :side-effects :none
      :version-integrity-policy :locked-or-rejected
      :status :complete}
     :async-effect-boundary-map
     {:promise-creation []
      :awaiting [{:symbol "translatePromise"
                  :effect :async/await
                  :capability nil
                  :rejected-promise-policy :translate}]
      :timers []
      :dom-ui-events []
      :network-fetch []
      :filesystem-process []
      :storage []
      :worker-messages []
      :model-tool-calls []
      :cancellation-behavior :declared-none
      :scheduler-event-loop-assumptions :browser-event-loop
      :replay-nondeterminism-policy :recorded
      :source-map-preservation :required
      :status :complete}
     :nullish-and-exception-translation-map
     {:nullish {:javascript-null :Option
                :javascript-undefined :Option
                :safe-gravity-entry :rejected-without-adapter
                :opaque-host-values :allowed-behind-wrapper}
      :exceptions {:throwable :gravity-panic
                   :rejected-promise :gravity-error
                   :event-callback-failure :effect-channel
                   :host-logging-only :rejected}
      :status :checked}
     :numeric-representation-manifest
     {:I64 :BigInt
      :F64 :Number
      :packed-I32 :Int32Array
      :overflow-narrowing-shift-boundaries :checked-helper
      :schema-stable-large-json-values :string
      :NaN :checked-boundary
      :signed-zero :manifested
      :precision-loss :rejected
      :bigint-number-mixing :rejected
      :json-serialization-loss :schema-checked
      :target-intrinsics []
      :status :complete}
     :dynamic-code-and-prototype-policy
     {:eval :rejected
      :function-constructor :rejected
      :dynamic-code-loading :policy-required
      :prototype-mutation :rejected
      :object-layout-assumptions :frozen-record-or-schema
      :status :complete}
     :ui-component-binding-metadata
     {:enabled false
      :components []
      :props-schema :not-applicable
      :state-effect-hooks []
      :dom-event-capabilities []
      :style-asset-dependencies []
      :hydration-boundary :not-applicable
      :source-map-hot-reload-identity :recorded
      :taint-policy :not-applicable
      :status :not-applicable}
     :source-debug-map
     {:source input-id
      :generated-origin-chain [:mir :c14-target-lowering
                               :b1-interface :b6-js-ts-backend]
      :generated-files ["gravity-stage0.mjs"
                        "gravity-stage0.d.ts"
                        "gravity-stage0.mjs.map"
                        "package.json"]
      :status :preserved}
     :node-syntax-and-runtime-record
     {:declared-check-command "node --check /tmp/gravity-p07-b6-js-ts/gravity-stage0.mjs"
      :declared-runtime-command "node -e \"import('/tmp/gravity-p07-b6-js-ts/gravity-stage0.mjs').then(m => console.log(String(m.gravityEntry(7n))))\""
      :proof-artifact "docs/artifacts/phase-07/reports/p07-d103-b6-js-ts-backend-report.md"
      :status :requires-proof-command}
     :typescript-declaration-check-record
     {:declared-command "tsc --noEmit --strict --target ES2022 --module ES2022 /tmp/gravity-p07-b6-js-ts/gravity-stage0.d.ts"
      :proof-artifact "docs/artifacts/phase-07/reports/p07-d103-b6-js-ts-backend-report.md"
      :status :requires-proof-command}
     :input-artifact input-id
     :status :complete}))