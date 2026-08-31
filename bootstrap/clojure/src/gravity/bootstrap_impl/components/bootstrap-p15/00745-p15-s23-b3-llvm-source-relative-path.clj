

;; ---------------------------------------------------------------------------
;; Verified C11 MIR -> Gravity-authored bounded LLVM (FL-P07-T01 slice)
;; ---------------------------------------------------------------------------

(def p15-s23-b3-llvm-source-relative-path
  "bootstrap/gravity/src/gravity/backend/b3_llvm_backend_design.gravity")

(def p15-s23-b3-llvm-builder-function
  'b3-build-bounded-llvm-x86_64-linux)

(def p15-s23-b3-llvm-source-byte-count 91077)
(def p15-s23-b3-llvm-expected-source-content-hash
  "sha256:3faf6a748485547abd6bd8da917a2214a7a8ba1cc9b3038bbbd3cdadd0562300")
(def p15-s23-b3-llvm-expected-plan-semantic-hash
  "sha256:ac34b56b83ad0bb876de5574c6550ed46e8e4ab1ced457d2ab3736a3a5e1c68e")
(def p15-s23-b3-llvm-expected-functions-semantic-hash
  "sha256:6bd78c2937ee13900571daf202a2c2096ec268d8a2b2b4a391774076cacb37d8")
(def p15-s23-b3-llvm-expected-builder-semantic-hash
  "sha256:b8ce9d861759de62aa16237cc8c596be1451ae1d06d7e2424d07da48881d351e")

(def p15-s23-b3-llvm-expected-file-magic-content
  {:status :not-applicable
   :format :elf
   :architecture :x86_64})

(def p15-s23-b3-llvm-required-functions
  {'b3-build-bounded-llvm-x86_64-linux {:arity 1 :params ['mir]}
   'b3-bounded-llvm-x86_64-linux-policy-record {:arity 0 :params []}
   'b3-block-order {:arity 2 :params ['mir 'function]}
   'b3-collect-block-operations
   {:arity 3 :params ['blocks 'block-order 'result]}
   'b3-index-operations {:arity 3 :params ['operations 'index 'result]}
   'b3-first-unsupported-operation
   {:arity 4 :params ['operations 'operation-index 'block-labels 'values]}
   'b3-operation-unsupported-reason
   {:arity 5
    :params ['operation 'operations 'operation-index 'block-labels 'values]}
   'b3-constant-payload-supported? {:arity 1 :params ['operation]}
   'b3-semantic-result
   {:arity 3 :params ['function 'block-order 'operations]}
   'b3-build-operation-records
   {:arity 5
    :params ['operations 'all-operations 'operation-index
             'block-labels 'result]}
   'b3-build-block-records
   {:arity 7
    :params ['mir 'function 'block-order 'block-labels 'records
             'operation-index 'result]}
   'b3-operation-instruction
   {:arity 4 :params ['operation 'operations 'operation-index 'block-labels]}
   'b3-blocks-text {:arity 2 :params ['blocks 'result]}})

(def p15-s23-b3-llvm-policy
  {:artifact :gravity/b3-bounded-llvm-x86_64-linux-policy
   :schema-version 1
   :owner :gravity.backend/b3-llvm
   :backend :llvm
   :profile :hosted
   :tier :experimental
   :exposure :internal
   :opt-in-required? true
   :backend-status :partial-bounded-executable-slice
   :source-declaration-target :jvm
   :requested-lowering-target :llvm-x86_64-linux
   :target-selection
   {:source-target :jvm
    :requested-target :llvm-x86_64-linux
    :selection :explicit-canonical-linux-target
    :reason :checked-core-seed-contract}
   :direct-source-declared-llvm? false
   :full-target-selection-credit? false
   :source-to-native-operational-status
   :upstream-fresh-replay-performance-residual
   :fresh-replay-latency-slo :not-established
   :identity-path-neutrality
   :checkout-temp-output-and-effective-tool-installation
   :tool-launcher-path-policy :pinned-system-launchers
   :runtime-status :linux-hosted-llvm-no-jvm-or-clojure-fallback
   :canonical-target :llvm-x86_64-linux
   :target :llvm-x86_64-linux
   :target-triple "x86_64-unknown-linux-gnu"
   :data-layout
   "e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-i128:128-f80:128-n8:16:32:64-S128"
   :cpu "generic"
   :features ""
   :calling-convention :sysv-amd64
   :abi :sysv-amd64
   :object-format :elf
   :architecture :x86_64
   :os :linux
   :relocation-model :pic
   :code-model :small
   :optimization-level :O0
   :minimum-os-version :not-applicable
   :unwind-strategy :dwarf-cfi
   :sanitizers []
   :instrumentation []
   :gravity-runtime-providers []
   :platform-runtime-providers
   [:linux/process-startup :linux/elf-loader :linux/glibc-2.36]
   :platform-runtime-provider-status :requires-elf-x86_64-verification
   :build-runtime-providers
   [:llvm-clang-20.1.8 :llvm-llc-20.1.8 :llvm-opt-20.1.8
    :llvm-as-20.1.8 :llvm-dis-20.1.8 :llvm-readobj-20.1.8
    :llvm-objdump-20.1.8 :llvm-lld-20.1.8
    :linux-x86_64-process-loader]
   :requested-vs-emitted-binding :requires-canonical-linux-target
   :no-host-fallback? true
   :gravity-exception-unwind :none
   :platform-unwind-metadata :dwarf-cfi
   :tls-model :not-applicable
   :supported-profiles #{:hosted}
   :supported-operation-families
   [:scalar-i64-bool-nil :forwarding :truthiness :single-conditional
    :signed-i64-integer-comparisons]
   :unsupported-profiles
   [:hardware :firmware :kernel :native :distributed :ai :meta :gpu
    :formal]
   :unsupported-targets [:darwin :mach-o :arm64 :aarch64
                         :llvm-arm64-darwin :llvm-aarch64-linux
                         :llvm-x86_64-darwin :llvm]
   :required-evidence
   [:authenticated-c11-replay :independent-lowering-reconstruction
    :llvm-tool-acceptance :elf-x86_64-object :elf-x86_64-executable
    :differential-process-result :content-hash-and-provenance]
   :raw-lowering-kind :gravity/b3-bounded-llvm-x86_64-linux-lowering
   :final-artifact-kind
   :gravity/p15-s23-b3-authenticated-llvm-x86_64-linux-artifact
   :emission-kind :gravity/b3-llvm-x86_64-linux-elf-emission
   :executable-carrier-distinct? true
   :unsupported-surface
   [:strings :quote :str :println :runtime-checks :effects
    :program-capabilities :domain-anchors :multiple-functions
    :multiple-conditionals :non-scalar-types
    :integer-outside-signed-i64 :process-result-outside-0-to-255
    :jvm-fallback :clojure-fallback]
   :claims {:clojure-seed-boundary? true
            :public? false
            :release? false
            :self-hosted? false
            :whole-b3? false}
   :whole-b3? false
   :public? false
   :release? false
   :self-hosted? false
   :clojure-seed-boundary? true})

(def p15-s23-b3-llvm-source-lowering-policy
  ;; The pinned Gravity B3 source owns MIR -> LLVM decisions through the
  ;; compiler/tool observations listed here.  JDK FFM and renamex_np belong to
  ;; the later host finalization/publication boundary and must not be backfilled
  ;; into the Gravity-authored lowering result.
  (assoc p15-s23-b3-llvm-policy
         :build-runtime-providers
         [:llvm-clang-20.1.8 :llvm-llc-20.1.8 :llvm-opt-20.1.8
          :llvm-as-20.1.8 :llvm-dis-20.1.8 :llvm-readobj-20.1.8
          :llvm-objdump-20.1.8 :llvm-lld-20.1.8
          :linux-x86_64-process-loader]))

(def p15-s23-b3-llvm-diagnostic-rules
  #{"C13-CONTRACT" "C13-PRESERVE" "C13-INVALIDATE" "C13-PROOF"
    "C13-CHECK-ELISION" "C13-EFFECT" "C13-SAFETY" "C13-DOMAIN"
    "C13-NONDETERMINISM" "C13-VERIFY"
    "C14-INPUT" "C14-PROFILE" "C14-TARGET" "C14-ABI"
    "C14-RUNTIME" "C14-PROVIDER" "C14-PROOF-METADATA"
    "C14-CAPABILITY" "C14-UNSUPPORTED" "C14-MANIFEST"
    "B1-INPUT" "B1-PROFILE" "B1-TARGET" "B1-ABI" "B1-RUNTIME"
    "B1-PROOF" "B1-CAPABILITY" "B1-UNSUPPORTED" "B1-METADATA"
    "B3-TARGET" "B3-METADATA"
    "B3-UB" "B3-RUNTIME" "B3-PASS" "B3-ABI" "B3-MANIFEST" "B13-HASH"
    "B14-DIFFERENTIAL"})

(defn p15-s23-b3-llvm-diagnostic-stage
  [id]
  (cond
    (str/starts-with? id "C13-") :c13-mir-optimization
    (str/starts-with? id "C14-") :c14-target-lowering
    (str/starts-with? id "B1-") :b1-backend-interface
    (= id "B13-HASH") :b13-artifact-emission
    (= id "B14-DIFFERENTIAL") :b14-backend-conformance
    :else :b3-llvm-backend))

(defn p15-s23-b3-llvm-diagnostic-message
  [id]
  (get {"C13-CONTRACT" "C13 optimization pass contract is invalid"
        "C13-PRESERVE" "C13 optimization preservation evidence is incomplete"
        "C13-INVALIDATE" "C13 invalidation ledger is incomplete"
        "C13-PROOF" "C13 optimization proof closure is incomplete"
        "C13-CHECK-ELISION" "C13 runtime check accounting is incomplete"
        "C13-EFFECT" "C13 effect ordering preservation failed"
        "C13-SAFETY" "C13 safety preservation failed"
        "C13-DOMAIN" "C13 domain anchor preservation failed"
        "C13-NONDETERMINISM" "C13 deterministic replay failed"
        "C13-VERIFY" "C13 optimized MIR verification failed"
        "C14-INPUT" "C14 lowering input is unverified or stale"
        "C14-PROFILE" "C14 profile is ineligible for the requested backend"
        "C14-TARGET" "C14 target contract is unsupported or incomplete"
        "C14-ABI" "C14 ABI or layout contract is incomplete"
        "C14-RUNTIME" "C14 runtime contract is incomplete"
        "C14-PROVIDER" "C14 provider contract is incomplete"
        "C14-PROOF-METADATA" "C14 target metadata lacks proof authority"
        "C14-CAPABILITY" "C14 effect or capability authority is incomplete"
        "C14-UNSUPPORTED" "C14 input is outside the bounded lowering surface"
        "C14-MANIFEST" "C14 lowering manifest is incomplete"
        "B1-INPUT" "LLVM backend input is unverified or incomplete"
        "B1-PROFILE" "B1 profile contract is ineligible"
        "B1-TARGET" "B1 target or backend manifest is incomplete"
        "B1-ABI" "B1 ABI contract is incomplete"
        "B1-RUNTIME" "B1 runtime contract is incomplete"
        "B1-PROOF" "B1 proof closure is incomplete"
        "B1-CAPABILITY" "B1 authority closure is incomplete"
        "B1-UNSUPPORTED" "verified MIR is outside the bounded LLVM slice"
        "B1-METADATA" "B1 metadata or provenance is incomplete"
        "B3-TARGET" "pinned LLVM target or toolchain contract failed"
        "B3-METADATA" "LLVM metadata is not proof-authorized"
        "B3-UB" "LLVM lowering would introduce undefined behavior"
        "B3-RUNTIME" "delegated Linux runtime provider contract failed"
        "B3-PASS" "LLVM emission or verification pass failed"
        "B3-ABI" "LLVM ABI or ELF contract failed"
        "B3-MANIFEST" "LLVM artifact manifest is incomplete"
        "B13-HASH" "emitted LLVM artifact hash did not verify"
        "B14-DIFFERENTIAL" "LLVM process result differs from reference"
        }
       id "bounded LLVM backend failure"))