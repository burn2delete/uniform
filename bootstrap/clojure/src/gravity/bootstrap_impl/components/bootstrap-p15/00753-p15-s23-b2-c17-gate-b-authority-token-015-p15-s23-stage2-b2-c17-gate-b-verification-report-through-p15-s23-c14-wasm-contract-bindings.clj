(let [p15-s23-b2-c17-gate-b-authority-token (nth __gravity_bootstrap_lexical_values_131964 0)
      p15-s23-b2-c17-gate-b-tool-state (nth __gravity_bootstrap_lexical_values_131964 1)]
(defn p15-s23-stage2-b2-c17-gate-b-verification-report
  "Contextually reauthenticate Gate A, the pinned hosted-C17 toolchain, and any
  published seven-file bundle.  Integrity without checked context is not
  authenticity."
  [artifact checked-core context]
  (let [source-path (p15-s23-c11-ingress-source-path context)]
    (try
      (p15-s23-b2-c17-gate-b-integrity-preflight! source-path artifact)
      (let [fresh-c11
            (p15-s23-stage2-c11-mir-artifact checked-core context)
            fresh-gate-a
            (p15-s23-stage2-b2-c17-artifact-from-c11!
             fresh-c11 checked-core context)
            fresh-contextual
            (p15-s23-stage2-b2-c17-verification-report
             fresh-gate-a checked-core context)
            _
            (when-not
             (and (= fresh-gate-a (:gate-a-artifact artifact))
                  (= fresh-contextual
                     (:gate-a-contextual-report artifact)))
              (p15-s23-c-backend-fail!
               "B13-PROVENANCE" source-path artifact
               {:missing-fact :fresh-contextual-c17-gate-a-parity}))
            publication-intent?
            (get-in artifact [:toolchain-evidence :publication-intent?])
            fresh-transaction
            (p15-s23-b2-c17-gate-b-toolchain-transaction!
             p15-s23-b2-c17-gate-b-authority-token
             fresh-gate-a source-path publication-intent?)
            expected
            (p15-s23-b2-c17-gate-b-final-record
             fresh-gate-a fresh-contextual fresh-transaction
             (:publication-receipt artifact))
            _
            (p15-s23-b2-c17-gate-b-integrity-preflight!
             source-path expected)
            _
            (when-not (= expected artifact)
              (p15-s23-c-backend-fail!
               "B13-EVIDENCE" source-path artifact
               {:missing-fact
                :fresh-pinned-toolchain-and-final-c17-record-parity}))
            publication
            (p15-s23-b2-c17-gate-b-verify-via-provider!
             p15-s23-b2-c17-gate-b-authority-token artifact
             fresh-transaction source-path)
            base
            {:artifact :gravity/b2-c17-gate-b-contextual-verification-report
             :schema-version 1 :status :passed
             :artifact-id (:artifact-id artifact)
             :semantic-id (:semantic-id artifact)
             :gate-a-artifact-id (:artifact-id fresh-gate-a)
             :gate-a-contextual-report-id (:report-id fresh-contextual)
             :fresh-c11 :passed :fresh-gate-a :passed
             :fresh-gate-a-contextual-verification :passed
             :pinned-toolchain-replay :passed
             :strict-final-record-reconstruction :passed
             :publication publication
             :whole-b2? false :public? false :release? false
             :self-hosted? false :seed-boundary? true}]
        (assoc base :report-id
               (p15-s23-c11-mir-digest
                {:kind :gravity/b2-c17-gate-b-contextual-verification-report
                 :schema-version 1 :report base})))
      (catch InterruptedException interrupted
        (.interrupt (Thread/currentThread))
        (throw interrupted))
      (catch StackOverflowError _
        (p15-s23-c-backend-fail!
         "B13-SCHEMA" source-path {}
         {:missing-fact :bounded-hostile-c17-gate-b-verifier-stack}))
      (catch clojure.lang.ExceptionInfo exception
        (p15-s23-c-backend-contain-exception!
         source-path :contained-c17-gate-b-verifier-diagnostic exception))
      (catch Exception exception
        (p15-s23-c-backend-contain-exception!
         source-path :contained-c17-gate-b-verifier-host-failure exception)))))

(defn p15-s23-stage2-b2-c17-gate-b-verify!
  [artifact checked-core context]
  (let [report
        (p15-s23-stage2-b2-c17-gate-b-verification-report
         artifact checked-core context)]
    (when-not (= :passed (:status report))
      (p15-s23-c-backend-fail!
       "B13-EVIDENCE" (p15-s23-c11-ingress-source-path context) artifact
       {:missing-fact :contextual-c17-gate-b-verification-status}))
    :passed))

(defn p15-s23-stage2-b2-c17-gate-b-authentic?
  ([artifact] false)
  ([artifact checked-core context]
   (try
     (= :passed
        (p15-s23-stage2-b2-c17-gate-b-verify!
         artifact checked-core context))
     (catch InterruptedException interrupted
       (.interrupt (Thread/currentThread))
       (throw interrupted))
     (catch StackOverflowError _ false)
     (catch Exception _ false))))



(def p15-s23-c14-wasm-required-evidence
  [:authenticated-c11-replay
   :independent-lowering-reconstruction
   :gravity-b4-byte-reconstruction
   :independent-raw-module-parser
   :pinned-node-differential-result
   :content-hash-and-provenance])

(def p15-s23-c14-wasm-unsupported-surface
  [:strings :quote :str :println :runtime-checks :effects
   :program-capabilities :domain-anchors :multiple-functions
   :multiple-conditionals :non-scalar-types
   :integer-outside-signed-i32 :imports :memory :tables :globals
   :components :wit :wasi :simd :atomics])

(defn p15-s23-c14-wasm-target-contract []
  {:request :wasm
   :triple "wasm32-unknown-unknown"
   :architecture :wasm32
   :object-format :webassembly
   :backend :gravity.backend/wasm
   :target-kind :core-module
   :memory-width :wasm32
   :features #{}
   :imports []
   :runtime-helpers []
   :profile-eligibility [:hosted]
   :tier :experimental
   :exposure :internal
   :source-declaration-target :jvm
   :requested-lowering-target :wasm
   :selection :explicit-bootstrap-seed-target-override
   :reason :checked-core-seed-contract
   :direct-source-declared-wasm? false})

(defn p15-s23-c14-wasm-source-target-selection []
  {:source-declaration-target :jvm
   :requested-lowering-target :wasm
   :selection :explicit-bootstrap-seed-target-override
   :reason :checked-core-seed-contract
   :direct-source-declared-wasm? false})

(defn p15-s23-c14-wasm-abi []
  {:calling-convention :wasm-core
   :parameters [] :result :i32
   :integer-representation :signed-i32
   :boolean-representation :i32-0-or-1
   :nil-representation :i32-zero
   :memory-model :none :multi-value? false})

(defn p15-s23-c14-wasm-runtime []
  {:runtime :none :helpers [] :imports [] :hidden-services? false})

(defn p15-s23-c14-wasm-target-policy []
  {:scope :bounded-pure-scalar-forwarding-do-let-if-integer-comparisons
   :maximum-operation-count 127
   :target-kind :core-module
   :whole-c14? false :whole-b1? false :whole-b4? false
   :public? false :release? false :self-hosted? false})

(defn p15-s23-c14-wasm-contract-bindings
  [c11-artifact checked-core c11-report c13-record dependencies]
  (let [fact-bindings
        (get-in c13-record [:semantic-identity :fact-bindings])]
    (assoc
     (p15-s23-b3-llvm-contract-bindings
      c11-artifact checked-core c11-report)
     :target
     (p15-s23-c13-c14-b1-content-binding
      (p15-s23-c14-wasm-target-contract))
     :abi
     (p15-s23-c13-c14-b1-content-binding
      (p15-s23-c14-wasm-abi))
     :runtime
     (p15-s23-c13-c14-b1-content-binding
      (p15-s23-c14-wasm-runtime))
     :providers (p15-s23-c13-c14-b1-content-binding [])
     :dependencies
     (p15-s23-c13-c14-b1-content-binding dependencies)
     :type (:type fact-bindings)
     :ownership (:ownership fact-bindings)
     :c13-optimization
     (p15-s23-c13-c14-b1-content-binding
      {:artifact-id (:artifact-id c13-record)
       :semantic-id (:semantic-id c13-record)
       :decision-id (get-in c13-record [:decision-record :decision-id])
       :verifier-result (get-in c13-record [:verifier-replay :result])})))))
