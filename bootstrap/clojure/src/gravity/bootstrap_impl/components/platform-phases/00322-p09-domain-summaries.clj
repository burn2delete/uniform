

(def p09-domain-summaries
  {"DOM1" {:domain :hardware :profiles #{:hardware :formal} :backends #{:hdl}
           :runtime-services #{:no-runtime}
           :schemas #{:interface-schema}
           :capabilities #{:hardware-interface}
           :effects #{:hardware/simulate}
           :artifacts #{:hdl-module :testbench :timing-constraints :interface-schema :simulation-trace}
           :examples #{:counter :fifo :uart :cdc-synchronizer :bus-peripheral}
           :rejects #{:heap-allocation :unbounded-loop :unproven-cdc :implicit-width-truncation}
           :dependencies #{"P8" "B9" "R2" "SAFE8" "SAFE9" "SAFE10" "SAFE15"}}
   "DOM2" {:domain :firmware :profiles #{:firmware} :backends #{:c :llvm}
           :runtime-services #{:no-runtime :minimal-native :memory-runtime}
           :schemas #{:mmio-register-schema :interrupt-vector}
           :capabilities #{:mmio/access}
           :effects #{:firmware/mmio :firmware/interrupt}
           :artifacts #{:firmware-image :linker-map :interrupt-table :memory-budget :mmio-audit}
           :examples #{:boot-handler :timer-isr :sensor-loop :uart-driver}
           :rejects #{:hidden-allocation :host-io :unchecked-mmio :unbounded-interrupt-handler}
           :dependencies #{"P6" "B2" "B3" "B13" "R2" "R3" "R5" "SAFE2" "SAFE5" "SAFE8" "SAFE9" "SAFE10"}}
   "DOM3" {:domain :kernel :profiles #{:kernel} :backends #{:llvm :c}
           :runtime-services #{:no-runtime :minimal-native :memory-runtime :concurrency-runtime}
           :schemas #{:syscall-schema :kernel-abi}
           :capabilities #{:kernel/privileged}
           :effects #{:kernel/raw-memory :kernel/syscall}
           :artifacts #{:kernel-object :syscall-table :interrupt-table :unsafe-audit :emulator-smoke}
           :examples #{:page-table-manager :scheduler-primitive :syscall-handler :kernel-allocator}
           :rejects #{:ambient-authority :unchecked-raw-memory :gc-assumption :unbounded-allocation}
           :dependencies #{"P7" "B2" "B3" "B13" "R2" "R3" "R5" "R6" "R10" "R11" "SAFE2" "SAFE5" "SAFE8" "SAFE10" "SAFE15"}}
   "DOM4" {:domain :drivers :profiles #{:firmware :kernel :native :hosted}
           :backends #{:c :llvm :mobile :gpu}
           :runtime-services #{:minimal-native :memory-runtime :concurrency-runtime :ffi-runtime :capability-runtime}
           :schemas #{:register-schema :device-adapter-schema}
           :capabilities #{:device/access}
           :effects #{:device/mmio :device/dma :device/interrupt}
           :artifacts #{:register-schema :mmio-audit :dma-lifetime-proof :interrupt-contract :device-tests}
           :examples #{:uart :spi :i2c :gpio :block-device :gpu-handle}
           :rejects #{:unchecked-register-access :dma-lifetime-escape :unbounded-interrupt-work :ungranted-device-access}
           :dependencies #{"P4" "P5" "P6" "P7" "B2" "B3" "B8" "B12" "B13" "R2" "R3" "R5" "R6" "R10" "R11" "SAFE2" "SAFE5" "SAFE8" "SAFE10" "SAFE15"}}
   "DOM5" {:domain :high-performance-native :profiles #{:native :gpu}
           :backends #{:llvm :c :gpu}
           :runtime-services #{:minimal-native :memory-runtime :concurrency-runtime :ffi-runtime}
           :schemas #{:layout-manifest :abi-manifest}
           :capabilities #{:native/ffi}
           :effects #{:native/call}
           :artifacts #{:native-library :benchmark-report :layout-manifest :optimization-certificates :safety-proof-bundle}
           :examples #{:simd-sum :parser :storage-loop :physics-kernel :safe-ffi-wrapper}
           :rejects #{:implicit-ub :proofless-check-elision :benchmark-without-context :unchecked-fast-math}
           :dependencies #{"P5" "P11" "PERF1" "PERF10" "MATH5" "MATH8" "B2" "B3" "B8" "B13" "R3" "R5" "R6" "R10" "SAFE2" "SAFE7" "SAFE8" "SAFE9" "SAFE10" "SAFE15"}}
   "DOM6" {:domain :web-ui :profiles #{:hosted} :backends #{:javascript-typescript :wasm}
           :runtime-services #{:managed-runtime :repl-runtime :capability-runtime :observability-runtime}
           :schemas #{:component-schema :route-schema :api-schema}
           :capabilities #{:browser/dom :browser/network}
           :effects #{:browser/dom :network/fetch}
           :artifacts #{:js-bundle :typescript-declarations :typed-components :api-client :browser-capability-manifest}
           :examples #{:component :form :typed-fetch :offline-cache}
           :rejects #{:ambient-dom-access :unsafe-html-sink :schema-drift :untyped-package-import}
           :dependencies #{"P4" "B4" "B6" "R4" "R9" "R11" "R12" "SAFE10" "SAFE11" "SCHEMA"}}
   "DOM7" {:domain :mobile :profiles #{:hosted :native} :backends #{:mobile :javascript-typescript :llvm}
           :runtime-services #{:managed-runtime :memory-runtime :capability-runtime :observability-runtime}
           :schemas #{:offline-schema :platform-binding-schema}
           :capabilities #{:mobile/permission :storage/local}
           :effects #{:mobile/platform-api :storage/local}
           :artifacts #{:app-bundle :permission-manifest :platform-bindings :offline-schema :device-tests}
           :examples #{:screen :camera-capture :offline-sync :push-handler}
           :rejects #{:platform-api-without-permission :hidden-background-work :unchecked-platform-null :lifecycle-unsafe-ui}
           :dependencies #{"P4" "P5" "P13" "B12" "R4" "R5" "R11" "R12" "SAFE10" "SAFE11" "SCHEMA"}}
   "DOM8" {:domain :backend-services :profiles #{:hosted :native :distributed}
           :backends #{:llvm :jvm :javascript-typescript :workflow-graph}
           :runtime-services #{:managed-runtime :concurrency-runtime :distributed-runtime :capability-runtime :observability-runtime}
           :schemas #{:api-schema :config-schema :route-schema}
           :capabilities #{:database/query :network/server :secrets/read}
           :effects #{:network/server :database/query :secrets/read}
           :artifacts #{:service-binary :api-spec :config-schema :capability-manifest :worker-manifest}
           :examples #{:typed-route :crud-service :background-job :message-consumer}
           :rejects #{:schema-less-route :unauthorized-io :secret-leak :untyped-external-input}
           :dependencies #{"P4" "P5" "P9" "P13" "B3" "B5" "B6" "B10" "B11" "B13" "R4" "R6" "R7" "R11" "R12" "SAFE10" "SAFE11" "SCHEMA"}}
   "DOM9" {:domain :distributed-systems :profiles #{:distributed}
           :backends #{:workflow-graph :jvm :javascript-typescript}
           :runtime-services #{:concurrency-runtime :distributed-runtime :ai-runtime :capability-runtime :observability-runtime}
           :schemas #{:message-schema :event-log-schema :state-schema :crdt-schema}
           :capabilities #{:network/call :workflow/replay :model/call}
           :effects #{:network/call :workflow/timer :workflow/replay}
           :artifacts #{:actor-manifest :workflow-graph :event-log-schema :crdt-manifest :coordination-analysis :sync-manifest :conflict-semantics :convergence-evidence :replay-trace :service-topology}
           :examples #{:cart-actor :checkout-workflow :saga :message-consumer :local-first-document :crdt-counter :offline-sync}
           :rejects #{:unrecorded-nondeterminism :schema-less-message :unsafe-event-log-upgrade :non-idempotent-replay :invalid-crdt-merge :unproven-convergence :unclassified-coordination :implicit-conflict-policy}
           :dependencies #{"P9" "B10" "R6" "R7" "R8" "R11" "R12" "SAFE10" "SAFE11" "SAFE13" "SCHEMA"}}
   "DOM10" {:domain :database-storage :profiles #{:hosted :native :distributed}
            :backends #{:query-relational :llvm :c}
            :runtime-services #{:memory-runtime :concurrency-runtime :distributed-runtime :capability-runtime :observability-runtime}
            :schemas #{:database-schema :migration-schema :storage-layout-schema}
            :capabilities #{:database/query :filesystem/write}
            :effects #{:database/query :filesystem/write}
            :artifacts #{:migration :query-plan :prepared-bindings :storage-layout :crash-recovery-fixture}
            :examples #{:schema-migration :typed-query :btree-page :wal-replay}
            :rejects #{:tainted-string-query :data-loss-without-policy :layout-without-binary-schema :unsafe-durability-claim}
            :dependencies #{"B11" "P4" "P5" "P9" "R5" "R6" "R7" "R11" "R12" "SAFE10" "SAFE11" "SCHEMA"}}
   "DOM11" {:domain :data-analytics :profiles #{:hosted :native :distributed}
            :backends #{:query-relational :llvm :gpu :workflow-graph}
            :runtime-services #{:distributed-runtime :capability-runtime :observability-runtime}
            :schemas #{:dataset-schema :query-schema}
            :capabilities #{:data/read :database/query}
            :effects #{:data/read :database/query}
            :artifacts #{:query-plan :lineage :schema-report :analytics-kernel}
            :examples #{:typed-dataframe :etl-pipeline :stream-window :gpu-aggregate}
            :rejects #{:schema-drift :unbounded-materialization :lineage-loss :unauthorized-data-source}
            :dependencies #{"B8" "B10" "B11" "B13" "P4" "P5" "P9" "SAFE10" "SAFE11" "R7" "SCHEMA" "MATH" "PERF"}}
   "DOM12" {:domain :scientific-numeric :profiles #{:core :native :gpu :formal}
            :backends #{:llvm :gpu}
            :runtime-services #{:memory-runtime :ffi-runtime}
            :schemas #{:numeric-domain-schema :provider-boundary-schema}
            :capabilities #{:numeric/provider}
            :effects #{:numeric/provider-call}
            :artifacts #{:efir-graph :eml-expression :math-certificate :numeric-conformance :benchmark-report}
            :examples #{:oscillator :simulation-step :activation-function :symbolic-rewrite}
            :rejects #{:uncertified-approximation :proofless-fast-math :domain-gap :invalid-symbolic-equality}
            :dependencies #{"MATH" "P5" "P11" "P12" "B3" "B7" "B8" "R5" "R10" "PERF" "SAFE9" "SAFE15"}}
   "DOM13" {:domain :gpu-accelerator :profiles #{:gpu :native}
            :backends #{:gpu :mlir :llvm}
            :runtime-services #{:memory-runtime :concurrency-runtime :capability-runtime}
            :schemas #{:host-device-boundary-schema :launch-schema}
            :capabilities #{:gpu/launch :gpu/memory}
            :effects #{:gpu/launch :gpu/transfer}
            :artifacts #{:kernel-binary :host-adapter :launch-descriptor :device-memory-manifest :numeric-certificate}
            :examples #{:reduction :image-filter :tensor-map :particle-update}
            :rejects #{:host-effect-in-kernel :implicit-transfer :unsynchronized-device-state :uncertified-fast-math}
            :dependencies #{"P11" "B8" "B7" "R5" "R6" "R11" "PERF8" "MATH" "SAFE8" "SAFE9"}}
   "DOM14" {:domain :game-simulation :profiles #{:native :gpu :hosted}
            :backends #{:llvm :gpu :javascript-typescript :mobile}
            :runtime-services #{:memory-runtime :concurrency-runtime :repl-runtime :capability-runtime :observability-runtime}
            :schemas #{:asset-schema :plugin-schema :replay-schema}
            :capabilities #{:plugin/run :asset/load}
            :effects #{:game/input :plugin/run}
            :artifacts #{:simulation-loop :asset-schema :replay-trace :performance-budget :plugin-capabilities}
            :examples #{:entity-update :physics-step :input-system :deterministic-replay}
            :rejects #{:frame-allocation :unrecorded-gameplay-random :ambient-plugin-authority :asset-schema-mismatch}
            :dependencies #{"P4" "P5" "P11" "B3" "B6" "B8" "B12" "B13" "R5" "R6" "R9" "R11" "R12" "MATH" "SCHEMA"}}
   "DOM15" {:domain :security-crypto :profiles #{:core :native :hosted :formal}
            :backends #{:llvm :c :wasm}
            :runtime-services #{:ffi-runtime :capability-runtime :observability-runtime :ai-runtime}
            :schemas #{:secret-taint-schema :webauthn-passkey-schema :private-computation-schema}
            :capabilities #{:crypto/random :crypto/key :webauthn/credential :private-compute/run}
            :effects #{:crypto/sign :crypto/decrypt :webauthn/assert :private-compute/evaluate}
            :artifacts #{:constant-time-report :test-vectors :fuzz-fixtures :taint-policy :provider-audit :private-compute-report :webauthn-passkey-policy :credential-ceremony-transcript :leakage-diagnostics}
            :examples #{:password-hash :signature :encrypted-storage :protocol-parser :passkey-login :webauthn-registration :fhe-evaluation :mpc-aggregation}
            :rejects #{:secret-log :insecure-random :unreviewed-custom-crypto :timing-branch-on-secret :implicit-decrypt :webauthn-origin-mismatch :passkey-policy-mismatch :noise-budget-exceeded :unaudited-reveal}
            :dependencies #{"SAFE10" "SAFE11" "SAFE13" "R11" "R12" "B2" "B3" "B4" "R10" "B13" "TEST" "FORMAL" "PACKAGE" "GOV"}}
   "DOM16" {:domain :blockchain-smart-contracts :profiles #{:core :formal :distributed}
            :backends #{:wasm :workflow-graph :query-relational}
            :runtime-services #{:distributed-runtime :capability-runtime}
            :schemas #{:chain-abi :state-schema :event-schema :user-operation-schema :authorization-schema}
            :capabilities #{:chain/state-write :account/validate}
            :effects #{:chain/transaction :chain/index}
            :artifacts #{:contract-wasm :chain-abi :state-schema :gas-report :determinism-proof :ordering-report :account-validation :user-operation-schema :ethereum-account-abstraction-profile :erc-4337-entrypoint-binding :eip-7702-authorization-schema :erc-7579-module-manifest :wallet-client-binding}
            :examples #{:token-transfer :escrow :governance-vote :indexer :programmable-account :erc-4337-wallet :eip-7702-delegation :erc-7579-modular-account}
            :rejects #{:nondeterministic-contract-effect :unchecked-overflow :abi-upgrade-without-migration :unauthorized-state-mutation :order-sensitive-contract-without-assumption :account-validation-without-replay-domain :account-abstraction-profile-mismatch}
            :dependencies #{"P2" "P9" "P12" "P13" "B4" "B10" "B11" "B13" "SAFE9" "SAFE10" "SAFE11" "FORMAL" "TEST"}}
   "DOM17" {:domain :compiler-tooling :profiles #{:meta :hosted :native}
            :backends #{:jvm :javascript-typescript :llvm}
            :runtime-services #{:repl-runtime :capability-runtime :observability-runtime}
            :schemas #{:syntax-schema :artifact-schema :diagnostic-schema}
            :capabilities #{:compiler/plugin}
            :effects #{:compiler/analyze :compiler/generate}
            :artifacts #{:syntax-tools :pass-manifest :mir-inspector :diagnostic-fixtures :plugin-manifest}
            :examples #{:macro-expander :formatter :mir-pass :lsp-query}
            :rejects #{:metadata-loss :unchecked-generated-code :plugin-effect-without-grant :nonhygienic-macro}
            :dependencies #{"C1" "C18" "P3" "P4" "P13" "R9" "R11" "R12" "TEST" "BOOTSTRAP"}}
   "DOM18" {:domain :ai-agentic :profiles #{:ai :distributed :hosted}
            :backends #{:workflow-graph :javascript-typescript :jvm}
            :runtime-services #{:ai-runtime :distributed-runtime :capability-runtime :observability-runtime}
            :schemas #{:tool-schema :structured-output-schema :memory-schema}
            :capabilities #{:model/call :tool/call :human-review/request}
            :effects #{:ai/model-call :ai/tool-call :ai/memory}
            :artifacts #{:agent-manifest :tool-policy :prompt-hashes :eval-report :replay-log}
            :examples #{:support-agent :data-extractor :code-reviewer :human-review-workflow}
            :rejects #{:tool-without-human-review :schema-less-output :prompt-policy-escalation :unvalidated-generated-code}
            :dependencies #{"P10" "B10" "R8" "R11" "SAFE10" "SAFE11" "SAFE12" "SAFE13" "P9" "R7"}}
   "DOM19" {:domain :formal-verification :profiles #{:formal :core}
            :backends #{:proof-kernel :mlir :zkvm}
            :runtime-services #{:no-runtime}
            :schemas #{:claim-schema :zk-relation-schema :public-private-input-schema}
            :capabilities #{:proof/check :zk/prove}
            :effects #{:proof/provider-call :zk/prove}
            :artifacts #{:proof-object :assumption-manifest :certificate :counterexample :proof-check-report :zk-relation :zk-privacy-facet-manifest :witness-record :setup-trust-manifest :prover-verifier-cost :recursive-proof-chain}
            :examples #{:bounds-proof :region-escape-proof :mir-equivalence :math-certificate :zk-circuit-proof :ivc-chain}
            :rejects #{:claim-without-proof :stale-certificate :solver-assumption-hidden :eml-equality-without-proof :zk-proof-without-relation :hidden-setup-trust}
            :dependencies #{"P12" "MATH" "C18" "SAFE15" "TEST" "B7" "B13" "PACKAGE"}}
   "DOM20" {:domain :scripting-shell-automation :profiles #{:hosted :meta}
            :backends #{:javascript-typescript :jvm :wasm}
            :runtime-services #{:repl-runtime :capability-runtime :observability-runtime}
            :schemas #{:argument-schema :command-schema :path-schema}
            :capabilities #{:filesystem/root :process/exec :human-review/request}
            :effects #{:filesystem/read :filesystem/write :process/exec}
            :artifacts #{:script-package :argument-schema :command-schema :capability-policy :task-log}
            :examples #{:file-transform :deploy-script :repo-maintenance :data-import}
            :rejects #{:shell-injection :ambient-filesystem :unhermetic-build-action :destructive-command-without-human-review}
            :dependencies #{"P4" "P3" "SAFE10" "SAFE11" "R9" "R11" "R12" "PACKAGE"}}
   "DOM21" {:domain :low-code-visual-workflow :profiles #{:distributed :ai :hosted}
            :backends #{:workflow-graph :javascript-typescript}
            :runtime-services #{:distributed-runtime :ai-runtime :capability-runtime :observability-runtime}
            :schemas #{:node-schema :edge-schema :workflow-schema}
            :capabilities #{:tool/call :model/call :human-review/request}
            :effects #{:workflow/step :ai/tool-call}
            :artifacts #{:visual-graph :schema-bindings :generated-source-map :human-review-policy :replay-trace}
            :examples #{:support-flow :etl-pipeline :human-review-flow :ai-tool-chain}
            :rejects #{:untyped-edge :hidden-effect :visual-authority :diagnostic-without-node-map}
            :dependencies #{"B10" "R7" "R8" "PHASE11" "SCHEMA" "SAFE10" "SAFE11" "SAFE12" "TOOLING"}}})

(def p09-domain-rejected-diagnostics
  {"DOM1" "DOM1-WIDTH"
   "DOM2" "DOM2-MMIO"
   "DOM3" "DOM3-RAW"
   "DOM4" "DOM4-DMA"
   "DOM5" "DOM5-OPTIMIZATION"
   "DOM6" "DOM6-TAINT"
   "DOM7" "DOM7-PERMISSION"
   "DOM8" "DOM8-SCHEMA"
   "DOM9" "DOM9-CONVERGENCE"
   "DOM10" "DOM10-QUERY"
   "DOM11" "DOM11-LINEAGE"
   "DOM12" "DOM12-CERTIFICATE"
   "DOM13" "DOM13-HOST-EFFECT"
   "DOM14" "DOM14-DETERMINISM"
   "DOM15" "DOM15-BOUNDARY"
   "DOM16" "DOM16-AA-PROFILE"
   "DOM17" "DOM17-METADATA"
   "DOM18" "DOM18-TOOL"
   "DOM19" "DOM19-ZK-SETUP"
   "DOM20" "DOM20-TAINT"
   "DOM21" "DOM21-EDGE"})

(def p09-domain-diagnostic-ids
  (vec
   (distinct
    (concat
     (mapcat
      (fn [document]
        (map (comp first val)
             (sort-by (comp name key) (p09-domain-contracts document))))
      p09-domain-documents)
     ["P09-MANIFEST" "P09-ACCEPTED" "P09-REJECTED"
      "P09-CLAIM" "P09-CONFORMANCE"]))))