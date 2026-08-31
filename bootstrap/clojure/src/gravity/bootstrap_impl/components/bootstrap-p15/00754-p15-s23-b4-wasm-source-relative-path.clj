

;; ---------------------------------------------------------------------------
;; Verified C11 MIR -> authenticated bounded raw Wasm32 (FL-P07-T02 slice)
;; ---------------------------------------------------------------------------

(def p15-s23-b4-wasm-source-relative-path
  "bootstrap/gravity/src/gravity/backend/b4_wasm_backend_design.gravity")
(def p15-s23-b4-wasm-builder-function 'b4-build-bounded-wasm32-core)
(def p15-s23-b4-wasm-source-byte-count 118633)
(def p15-s23-b4-wasm-expected-source-content-hash
  "sha256:96e2fa6a41118edacd026f878cc6c817a5d65f06973ccee24974b62a3b3d7488")
(def p15-s23-b4-wasm-expected-plan-semantic-hash
  "sha256:ee8a72102449defce04f89dadcb3cd9f211b4db24edecd08e5291b21a6f8c960")
(def p15-s23-b4-wasm-expected-functions-semantic-hash
  "sha256:9fdfa4a165210e613138f038ae74edb9f3170ce8ddff2396ebb9da21978488d9")
(def p15-s23-b4-wasm-expected-builder-semantic-hash
  "sha256:f3e7c33fc18167ea5c3ae5da1aa8005cf7939feaaf60e9be5c488266cf1c16a9")
(def p15-s23-b4-wasm-required-functions
  {'b4-build-bounded-wasm32-core {:arity 1 :params ['b1-packet]}
   'b4-lowercase-hex? {:arity 1 :params ['characters]}
   'b4-sha256-id? {:arity 1 :params ['value]}
   'b4-content-binding-valid? {:arity 1 :params ['binding]}
   'b4-content-bindings-valid?
   {:arity 2 :params ['remaining 'bindings]}
   'b4-operation-order-matches?
   {:arity 2 :params ['operations 'operation-order]}
   'b4-operation-containers-valid?
   {:arity 2 :params ['mir 'function]}
   'b4-c14-verifier-valid?
   {:arity 2 :params ['verifier 'mir]}
   'b4-b1-mir-input-projections-valid?
   {:arity 1 :params ['packet]}
   'b4-c11-source-rule-valid? {:arity 1 :params ['rule]}
   'b4-c13-source-rule-valid? {:arity 1 :params ['rule]}
   'b4-proof-record-valid?
   {:arity 4 :params ['proofs 'fact-bindings 'bindings 'mir]}
   'b4-fact-bindings-valid?
   {:arity 4 :params ['packet 'mir 'fact-bindings 'bindings]}
   'b4-dependencies-valid?
   {:arity 3 :params ['dependencies 'input 'mir]}
   'b4-b1-nested-carriers-valid? {:arity 1 :params ['packet]}
   'b4-b1-static-report-projections-valid?
   {:arity 1 :params ['packet]}
   'b4-b1-static-contract-valid? {:arity 1 :params ['packet]}
   'b4-b1-seal-valid? {:arity 1 :params ['packet]}
   'b4-b1-packet-valid? {:arity 1 :params ['packet]}
   'b4-block-order {:arity 2 :params ['mir 'function]}
   'b4-block-labels {:arity 1 :params ['block-order]}
   'b4-reference-allowed?
   {:arity 5 :params ['operation 'reference-id 'operations
                      'operation-index 'block-labels]}
   'b4-u32-leb {:arity 1 :params ['value]}
   'b4-s32-leb {:arity 1 :params ['value]}
   'b4-operation-reason
   {:arity 4 :params ['operation 'operations 'operation-index
                      'block-labels]}
   'b4-comparison-wasm-opcode {:arity 1 :params ['opcode]}
   'b4-function-instructions
   {:arity 4 :params ['function 'block-order 'operations 'operation-index]}
   'b4-evaluate-operations
   {:arity 3 :params ['remaining 'operations 'values]}})

(def p15-s23-b4-wasm-node-path
  "/Users/matt/.nvm/versions/node/v20.9.0/bin/node")
(def p15-s23-b4-wasm-node-version "v20.9.0")
(def p15-s23-b4-wasm-node-architecture "arm64")
(def p15-s23-b4-wasm-node-byte-count 93278736)
(def p15-s23-b4-wasm-node-content-hash
  "sha256:a54ba15c721a9f5b62f84e845e914be0bc48c7bb62cf62de86be6583865495a5")
(def p15-s23-b4-wasm-node-timeout-ms 10000)
(def p15-s23-b4-wasm-max-module-bytes 65536)
(def p15-s23-b4-wasm-max-tool-output-bytes 4096)
(def p15-s23-b4-wasm-bounded-feature-policy
  {:wasm-version :core-v1
   :embedding-model :standalone-core-module
   :memory-width :wasm32
   :memory-count 0 :initial-memory-pages 0 :maximum-memory-pages 0
   :memory-growth-permission :forbidden
   :table-count 0 :table-representation :absent :global-count 0
   :reference-type-support :disabled
   :exception-handling-support :disabled
   :gc-proposal-support :disabled
   :simd-support :disabled :relaxed-simd-support :disabled
   :atomics-support :disabled :shared-memory-support :disabled
   :atomic-and-shared-memory-support :disabled
   :component-model-abi-version :not-applicable
   :wasi-preview-profile :none
   :wasi-preview-or-profile :none
   :async-component-abi-version :not-applicable
   :canonical-abi-adapter-support :disabled
   :resource-handle-support :disabled :resource-borrow-support :disabled
   :resource-handle-and-borrow-support :disabled
   :async-function-support :disabled :stream-support :disabled
   :future-support :disabled
   :async-func-stream-future-support :disabled
   :completion-strategy :not-applicable
   :cancellation-strategy :not-applicable
   :backpressure-strategy :not-applicable
   :completion-cancellation-backpressure-strategy :not-applicable
   :import-namespace :none
   :deterministic-or-replay-required-mode :deterministic-pure
   :enabled-features #{}
   :rejected-features
   #{:threads :tail-calls :multiple-memories :memory64}})
(def p15-s23-b4-wasm-node-script
  (str
   "'use strict';\n"
   "(async()=>{try{\n"
   "if(process.version!==\"v20.9.0\"||process.arch!==\"arm64\")process.exit(71);\n"
   "const b=Buffer.from(process.argv[1],\"base64\");\n"
   "const expected=Number(process.argv[2]);\n"
   "if(!WebAssembly.validate(b))process.exit(72);\n"
   "const m=await WebAssembly.compile(b);\n"
   "if(JSON.stringify(WebAssembly.Module.imports(m))!==\"[]\")process.exit(73);\n"
   "if(JSON.stringify(WebAssembly.Module.exports(m))!==\"[{\\\"name\\\":\\\"main\\\",\\\"kind\\\":\\\"function\\\"}]\")process.exit(74);\n"
   "const i=await WebAssembly.instantiate(m,{});\n"
   "if(JSON.stringify(Reflect.ownKeys(i.exports))!==\"[\\\"main\\\"]\")process.exit(75);\n"
   "const a=i.exports.main(),c=i.exports.main();\n"
   "if(typeof a!==\"number\"||a!==expected||c!==expected)process.exit(76);\n"
   "process.stdout.write(\"B4NODE1:\"+String(a)+\"\\n\");\n"
   "}catch(_){process.exit(77);}})();\n"))
(def p15-s23-b4-wasm-node-script-hash
  "sha256:079ca5e52a1db233e31028c3890fae19ddb85d8859fc1925f0b0846bebb16c08")

(def p15-s23-b4-wasm-diagnostic-rules
  #{"C13-VERIFY" "C14-INPUT" "C14-PROFILE" "C14-TARGET"
    "C14-UNSUPPORTED" "C14-MANIFEST"
    "B1-INPUT" "B1-TARGET" "B1-UNSUPPORTED" "B1-METADATA"
    "B4-TARGET" "B4-IMPORT"
    "B4-EXPORT" "B4-MEMORY" "B4-MANIFEST" "B13-HASH"
    "B14-DIFFERENTIAL"})

(defn p15-s23-b4-wasm-diagnostic-stage [id]
  (cond
    (str/starts-with? id "C13-") :c13-mir-optimization
    (str/starts-with? id "C14-") :c14-target-lowering
    (str/starts-with? id "B1-") :b1-backend-interface
    (= id "B13-HASH") :b13-artifact-emission
    (= id "B14-DIFFERENTIAL") :b14-backend-conformance
    :else :b4-wasm-backend))

(defn p15-s23-b4-wasm-diagnostic-message [id]
  (get {"B1-INPUT" "Wasm backend input is unverified or incomplete"
        "C13-VERIFY" "Wasm optimization replay did not verify"
        "C14-INPUT" "Wasm lowering input is unverified or stale"
        "C14-PROFILE" "profile is ineligible for bounded Wasm lowering"
        "C14-TARGET" "bounded Wasm32 core target contract failed"
        "C14-UNSUPPORTED" "input is outside the bounded Wasm32 lowering surface"
        "C14-MANIFEST" "bounded Wasm lowering manifest is incomplete"
        "B1-TARGET" "Wasm backend manifest is incomplete"
        "B1-METADATA" "Wasm backend metadata or provenance is incomplete"
        "B1-UNSUPPORTED" "verified MIR is outside the bounded Wasm slice"
        "B4-TARGET" "pinned Wasm target or Node contract failed"
        "B4-IMPORT" "bounded Wasm slice forbids imports"
        "B4-EXPORT" "bounded Wasm export contract failed"
        "B4-MEMORY" "bounded Wasm slice forbids memory and tables"
        "B4-MANIFEST" "raw Wasm module or artifact manifest is invalid"
        "B13-HASH" "raw Wasm content identity failed"
        "B14-DIFFERENTIAL" "Wasm result differs from authoritative MIR"}
       id "bounded Wasm backend failure"))

(defn p15-s23-b4-wasm-safe-facts [facts]
  (into (sorted-map)
        (keep (fn [key]
                (when (contains? facts key)
                  [key (p15-s23-c11-mir-safe-diagnostic-scalar
                        (get facts key))])))
        [:missing-fact :operation-id :opcode :observed-type
         :source-operation :c11-mir-id
         :expected-type :requested-target :tool-step :exit-code
         :expected-result :observed-result :byte-count :expected-byte-count
         :content-hash :expected-hash :timed-out? :node-start-count
         :invocation-local-start-count :captured-descendant-count
         :captured-process-set-reaped?]))

(defn p15-s23-b4-wasm-diagnostic-record [id source-path subject facts]
  (let [id (if (contains? p15-s23-b4-wasm-diagnostic-rules id)
             id "B4-MANIFEST")
        source-path (p15-s23-c11-mir-safe-source-path source-path)
        facts (let [safe (p15-s23-b4-wasm-safe-facts facts)]
                (if (seq safe) safe {:missing-fact :bounded-wasm-failure}))
        anchor (or (:artifact-id subject)
                   (p15-s23-c11-mir-digest
                    {:kind :gravity/b4-diagnostic :rule id :facts facts}))
        primary {:span (p15-s23-c11-mir-span-with-source source-path subject)
                 :syntax-id (or (:syntax-id subject) :not-applicable)
                 :core-node-id (or (:op-id subject) :not-applicable)
                 :mir-operation-id (or (:op-id subject) :not-applicable)
                 :origin-id (or (get-in subject [:source :origin-id])
                                :not-applicable)
                 :artifact anchor}
        base {:artifact :gravity/diagnostic
              :rule id :severity :error
              :stage (p15-s23-b4-wasm-diagnostic-stage id)
              :message-key (keyword "diagnostic" (str/lower-case id))
              :primary primary :related []
              :origin-chain (if (= :not-applicable (:origin-id primary))
                              [] [(:origin-id primary)])
              :profile :hosted :target :wasm
              :involved-artifacts [anchor]
              :facts facts
              :remediation [{:kind :repair-bounded-wasm-input
                             :from-stage :verified-c11-mir
                             :required-evidence
                             [:authenticated-c11-replay
                              :independent-raw-module-verification
                              :node-differential-execution]}]
              :redactions [{:kind :host-details :status :redacted
                            :policy :hashes-and-bounded-counts-only}]
              :lifecycle :active}
        diagnostic-id (c15-stable-diagnostic-id base)]
    (assoc base :diagnostic-id diagnostic-id
           :ordering-key [id (:stage base) anchor diagnostic-id])))

(defn p15-s23-b4-wasm-throw-record! [record]
  (let [message (p15-s23-b4-wasm-diagnostic-message (:rule record))]
    (throw (ex-info message
                    (merge record {:id (:rule record) :message message
                                   :bootstrap-stage :stage0
                                   :source-span (get-in record [:primary :span])
                                   :missing-fact
                                   (get-in record [:facts :missing-fact])
                                   :fallback-status :rejected})))))