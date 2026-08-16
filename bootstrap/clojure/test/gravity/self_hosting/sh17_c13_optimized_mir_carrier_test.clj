(ns gravity.self-hosting.sh17-c13-optimized-mir-carrier-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

;; This file is deliberately a carrier test, rather than another source
;; coverage test.  The packet builders below are the authenticated public
;; ingress.  C13 is inspected as the carrier that C14 actually receives and
;; C14 is exercised through a freshly compiled Gravity plan.

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh17_c13_optimized_mir_carrier_test.clj")]
    (when-not resource
      (throw (ex-info "SH-17 C13 carrier test source is not on the classpath"
                      {:id "SH17-C13-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Gravity repository root could not be located"
                        {:id "SH17-C13-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(def ^:private c14-source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c14_target_lowering_architecture.gravity")

(def ^:private c14-plan
  (delay
    (let [source-path (path c14-source-relative-path)
          source-text (slurp source-path)
          emitter
          (:emitter
           (bootstrap/c-backend-stage2-plan-emitter-source-rule!
            source-path :llvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan
       emitter source-path source-text))))

(def ^:private c13-envelope-keys
  ;; This set is copied from C14's c14-c13-envelope-valid? contract.  Keep it
  ;; explicit: C14 source is authoritative for the C13 carrier envelope.
  #{:artifact :schema-version :status :input :optimized-mir
    :pass-contract :decision-record :invalidation-ledger
    :residual-check-report :verifier-replay :semantic-identity
    :target-instruction-selection :diagnostics :semantic-authority
    :execution-tcb :clojure-seed-boundary? :self-hosted?
    :source-rule :actual-path-provenance :semantic-id :artifact-id
    :actual-path-binding-id})

(def ^:private seal-keys
  #{:source-rule :actual-path-provenance :semantic-id :artifact-id
    :actual-path-binding-id})

(def ^:private targets
  ;; W1 closes one exact target. Other backend families remain distinct and
  ;; cannot serve as evidence for this Linux LLVM carrier.
  [:llvm])

(def ^:private canonical-target :llvm-x86_64-linux)

(def ^:private packet-builders
  {:llvm bootstrap/p15-s23-stage2-c13-c14-b1-packet-from-c11!})

(def ^:private packet-verifiers
  {:llvm bootstrap/p15-s23-stage2-c13-c14-b1-verification-report})

(def ^:private c14-builder-functions
  {:llvm 'c14-build-bounded-llvm-lowering-record})

(def ^:private c14-policy-keys
  ;; C14's three public policy constructors intentionally share one exact
  ;; envelope. The target policy narrows the values in that envelope.
  #{:expected-c11-artifact-id :expected-c13-artifact-id
    :profile :profile-contract :target :source-target-selection
    :abi :runtime :providers :effects :capabilities :safety :proofs
    :contract-bindings :required-evidence :source-map
    :expected-source-map-binding :dependencies :target-policy
    :unsupported-surface :proof-target-metadata :fact-bindings
    :supported-operation-ids :c11-verifier :c11-verifier-id :request-id})

;; These nested keysets are part of the authenticated carrier schema, rather
;; than incidental implementation detail.  Keeping them here makes the
;; positive test prove that C13 emitted the complete replay/closure record and
;; makes every extra or missing field a fail-closed regression.
(def ^:private c13-input-keys
  #{:kind :c11-artifact-id :c11-mir-id :module-id :source-core
    :verifier-report-id :verifier-status :semantic-replay-parity
    :pass-execution-record-id})

(def ^:private c13-pass-contract-keys
  #{:pass-id :family :version :input-ir :output-ir :required-analyses
    :preconditions :preserves :invalidates :regenerates :proof-obligations
    :profile-constraints :target-assumptions :effect-ordering-policy
    :safety-policy :domain-policy :maximum-operation-count :emits})

(def ^:private c13-decision-record-keys
  #{:artifact :pass-id :decision-id :input-mir :output-mir :decision
    :changed? :changed-operations :reason :preserved :invalidated
    :proofs-used :residual-checks :source-map :verifier-result})

(def ^:private c13-invalidation-ledger-keys
  #{:artifact :pass-id :decision-id :input-mir :output-mir
    :analysis-invalidated :facts-invalidated :facts-regenerated
    :proofs-invalidated :certificates-invalidated :runtime-checks-restored
    :passes-to-rerun :caches-cleared :diagnostics-affected :profile :target})

(def ^:private c13-residual-check-keys
  #{:artifact :status :retained-runtime-checks :elided-runtime-checks
    :open-proof-obligations})

(def ^:private c13-verifier-replay-keys
  #{:artifact :required? :c11-artifact-id :verifier-report-id :c11-mir-id
    :expected-input-module-id :expected-output-module-id :fact-bindings
    :semantic-identity-required? :result})

(def ^:private c13-semantic-identity-keys
  #{:c11-input-mir-id :c11-output-mir-id :input-module-id
    :output-module-id :fact-bindings :operation-order :operation-count
    :maximum-operation-count :effect-order-graph :unchanged?})

(def ^:private c13-source-rule-keys
  #{:artifact :owner :source-content-hash :source-byte-count
    :plan-semantic-hash :functions-semantic-hash :builder-function
    :builder-semantic-hash :function-shapes :semantic-authority
    :compiled-by :executed-by :self-hosted?})

(def ^:private c13-path-provenance-keys
  #{:source :c11-source :c13-source})

(def ^:private c11-report-keys
  #{:artifact :status :mir-id :checked-core-artifact-id
    :checked-core-ingress :checked-core-request-binding-provenance
    :source-rule :semantic-replay-parity :invocation-audit
    :execution-count :live-invocation-claim? :execution-tcb
    :independent-verifier :b1-preflight :actual-path-context})

(def ^:private final-envelope-missing-facts
  {:llvm :bounded-c13-c14-b1-final-envelope})

(def ^:private final-identity-missing-facts
  {:llvm :recomputable-c13-c14-b1-final-identities})

(def ^:private contextual-reconstruction-missing-facts
  {:llvm :contextual-fresh-c13-c14-b1-reconstruction})

(def ^:private source-values
  {:llvm "42"})

(defn- closed-pure-source
  ([backend]
   (closed-pure-source backend (get source-values backend)))
  ([backend value]
   (str
    "(ns checked.sh17 (:profile :hosted) (:target :jvm) "
    "(:safety :safe) (:effects #{}) (:capabilities #{}) (:exports [main]))\n"
    "(defn main [] " value ")\n")))

(defn- build-upstream
  ([backend source-path]
   (build-upstream backend source-path (closed-pure-source backend)))
  ([backend source-path source]
    (binding [bootstrap/*p15-s23-c11-mir-diagnostic-context*
              {:requested-target canonical-target}
              bootstrap/*additional-bootstrap-targets*
              bootstrap/stage2-runtime-derived-source-targets]
      (let [context
            (bootstrap/p15-s23-stage2-gravity-checked-core-context
             source-path source canonical-target)
            checked-core
            (bootstrap/p15-s23-stage2-gravity-checked-core-source-artifact
             context)
            c11
            (bootstrap/p15-s23-stage2-c11-mir-artifact
             checked-core context)
            c11-report
            (bootstrap/p15-s23-stage2-c11-mir-verification-report
             c11 checked-core context)
            packet
            ((get packet-builders backend) c11 checked-core context)
            c13 (:c13 packet)
            policy
            (bootstrap/p15-s23-c14-policy
             c11 checked-core c11-report c13 (:source-rule c13))
            raw-c14
            (bootstrap/p15-s23-stage2-runtime-execute-function
             {:engine :gravity-sh17-c13-optimized-mir-carrier-test
              :compiler-artifact-plan? true}
             @c14-plan (get c14-builder-functions backend)
             [c13 policy])
            report ((get packet-verifiers backend) packet checked-core context)]
        {:backend backend :source source :source-path source-path
         :context context :checked-core checked-core :c11 c11
         :c11-report c11-report :packet packet :c13 c13
         :c14 (:c14 packet) :b1 (:b1 packet)
         :policy policy :raw-c14 raw-c14 :report report}))))

(def ^:private prepared
  (delay
    (into {}
          (map (fn [backend]
                 [backend
                  (build-upstream
                   backend
                   (str "sh17-c13-carrier-" (name backend) ".gravity"))])
               targets))))

(defn- carrier
  [backend]
  (get @prepared backend))

(def ^:private alternate-linux-carrier
  (delay
    (build-upstream
     :llvm "sh17-c13-carrier-linux-alternate.gravity"
     (closed-pure-source :llvm "43"))))

(defn- raw-c14-from-packet
  [packet]
  (apply dissoc (:c14 packet) seal-keys))

(defn- invoke-c14
  [backend optimized policy]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh17-c13-optimized-mir-carrier-test
    :compiler-artifact-plan? true}
   @c14-plan (get c14-builder-functions backend) [optimized policy]))

(defn- packet-diagnostic
  [backend packet checked-core context]
  ;; The public verifier intentionally throws ExceptionInfo for a rejected
  ;; packet.  Catch only that owned diagnostic type; a host error must fail the
  ;; test rather than being mistaken for a rejection.
  (try
    {:unexpected ((get packet-verifiers backend)
                  packet checked-core context)}
    (catch clojure.lang.ExceptionInfo exception
      (ex-data exception))))

(defn- rehash-packet
  [packet]
  (let [semantic-id (bootstrap/p15-s23-c13-c14-b1-semantic-id packet)
        artifact-id
        (bootstrap/p15-s23-c11-mir-digest
         {:kind (:kind packet) :schema-version (:schema-version packet)
          :semantic-id semantic-id})
        path-binding-id
        (bootstrap/p15-s23-c13-c14-b1-actual-path-binding-id
         semantic-id (:actual-path-provenance packet))]
    (assoc packet :semantic-id semantic-id :artifact-id artifact-id
           :actual-path-binding-id path-binding-id)))

(defn- maps-in
  [value]
  (tree-seq coll?
           (fn [item]
             (if (map? item)
               (mapcat identity item)
               item))
           value))

(defn- assert-claim-boundary
  [value]
  (doseq [item (filter map? (maps-in value))]
    (when (contains? item :clojure-seed-boundary?)
      (is (true? (:clojure-seed-boundary? item)) item))
    (doseq [claim [:self-hosted? :whole-c13? :whole-c14? :whole-b1?
                   :whole-b2? :whole-b3? :whole-b4? :public? :release?]]
      (when (contains? item claim)
        (is (false? (get item claim)) [claim item])))
    (when (contains? item :semantic-authority)
      (is (= :gravity-source (:semantic-authority item)) item))
    (is (not (contains? item :authority)) item)
    (is (not (contains? item :unsupported-authority)) item)
    (is (not (contains? item :ambient-authority)) item)))

(defn- assert-fresh-c11-report
  [{:keys [c11 checked-core c11-report context]}]
  (is (= c11-report-keys (set (keys c11-report))))
  (is (= :gravity/c11-mir-verification-report (:artifact c11-report)))
  (is (= :passed (:status c11-report)))
  (is (= (:mir-id c11) (:mir-id c11-report)))
  (is (= (:artifact-id checked-core)
         (:checked-core-artifact-id c11-report)))
  (is (= :passed (:semantic-replay-parity c11-report)))
  (is (= :clojure-stage0-rule-runner (:execution-tcb c11-report)))
  (is (= :not-available (:invocation-audit c11-report)))
  (is (= :not-claimed (:execution-count c11-report)))
  (is (false? (:live-invocation-claim? c11-report)))
  (is (= (:source-path context)
         (get-in c11-report [:actual-path-context :source])))
  (is (= :passed
         (get-in c11-report [:independent-verifier :status])))
  (is (true?
       (get-in c11-report [:independent-verifier :module-shape-valid?])))
  (is (true?
       (get-in c11-report [:independent-verifier :type-facts-valid?])))
  (is (true?
       (get-in c11-report [:independent-verifier :target-independent?]))))

(defn- assert-c13-carrier
  [{:keys [source-path c11 c13]}]
  (let [mir (:mir-module c11)
        source-rule (:source-rule c13)
        input (:input c13)
        pass-contract (:pass-contract c13)
        decision-record (:decision-record c13)
        invalidation-ledger (:invalidation-ledger c13)
        residual-check-report (:residual-check-report c13)
        verifier-replay (:verifier-replay c13)
        semantic-identity (:semantic-identity c13)
        actual-path-provenance (:actual-path-provenance c13)
        fact-bindings
        {:type
         (bootstrap/p15-s23-c13-c14-b1-content-binding (:type-table mir))
         :effect
         (bootstrap/p15-s23-c13-c14-b1-content-binding (:effect-table mir))
         :ownership
         (bootstrap/p15-s23-c13-c14-b1-content-binding (:ownership-table mir))
         :capability
         (bootstrap/p15-s23-c13-c14-b1-content-binding
          (:capability-table mir))
         :safety
         (bootstrap/p15-s23-c13-c14-b1-content-binding (:safety-table mir))
         :runtime-checks
         (bootstrap/p15-s23-c13-c14-b1-content-binding
          (:runtime-check-table mir))
         :proofs
         (bootstrap/p15-s23-c13-c14-b1-content-binding
          (:proof-certificate-table mir))
         :source-map
         (bootstrap/p15-s23-c13-c14-b1-content-binding (:source-map mir))}
        operation-order
        (mapv :op-id
              (bootstrap/p15-s23-c11-mir-operation-sequence mir))
        semantic-id
        (bootstrap/p15-s23-c11-mir-digest
         {:kind (:artifact c13)
          :record
          (bootstrap/p15-s23-c13-c14-b1-stage-semantic-input c13)})
        artifact-id
        (bootstrap/p15-s23-c11-mir-digest
         {:kind (:artifact c13) :schema-version (:schema-version c13)
          :semantic-id semantic-id})]
    (is (= 22 (count c13-envelope-keys)))
    (is (= c13-envelope-keys (set (keys c13))))
    (is (= c13-input-keys (set (keys input))))
    (is (= c13-pass-contract-keys (set (keys pass-contract))))
    (is (= c13-decision-record-keys (set (keys decision-record))))
    (is (= c13-invalidation-ledger-keys
           (set (keys invalidation-ledger))))
    (is (= c13-residual-check-keys
           (set (keys residual-check-report))))
    (is (= c13-verifier-replay-keys
           (set (keys verifier-replay))))
    (is (= c13-semantic-identity-keys
           (set (keys semantic-identity))))
    (is (= c13-source-rule-keys
           (set (keys source-rule))))
    (is (= c13-path-provenance-keys
           (set (keys actual-path-provenance))))
    (is (= 1 (:schema-version c13)))
    (is (= :accepted (:status c13)))
    (is (= :gravity-source (:semantic-authority c13)))
    (is (= :clojure-stage0-rule-runner (:execution-tcb c13)))
    (is (true? (:clojure-seed-boundary? c13)))
    (is (false? (:self-hosted? c13)))
    (is (= :forbidden (:target-instruction-selection c13)))
    (is (= [] (:diagnostics c13)))
    (is (= (:mir-id c11) (:c11-mir-id input)))
    (is (= (:artifact-id c11) (:c11-artifact-id input)))
    (is (= (:module-id mir) (:module-id input)))
    (is (= (:source-core mir) (:source-core input)))
    (is (= :passed (:verifier-status input)))
    (is (= :passed (:semantic-replay-parity input)))
    (is (= :hosted (first (:profile-constraints pass-contract))))
    (is (= :c13-bounded-identity (:pass-id pass-contract)))
    (is (= 1 (:version pass-contract)))
    (is (= :retain-input-unchanged (:decision decision-record)))
    (is (false? (:changed? decision-record)))
    (is (= :passed (:verifier-result decision-record)))
    (is (= :complete (:status residual-check-report)))
    (is (= true (:required? verifier-replay)))
    (is (= true (:semantic-identity-required? verifier-replay)))
    (is (= :passed (:result verifier-replay)))
    (is (= true (:unchanged? semantic-identity)))
    (is (= :hosted (:profile invalidation-ledger)))
    ;; C13 itself remains target-independent, but its request is required to
    ;; carry the one canonical Linux identity selected at checked-core ingress.
    (is (= canonical-target (:target invalidation-ledger)))
    (is (= (:mir-id c11) (:input-mir decision-record)))
    (is (= (:mir-id c11) (:output-mir decision-record)))
    (is (= (:mir-id c11) (:c11-mir-id verifier-replay)))
    (is (= (:mir-id c11) (:c11-input-mir-id semantic-identity)))
    (is (= (:mir-id c11) (:c11-output-mir-id semantic-identity)))
    (is (= (:mir-module c11) (:optimized-mir c13)))
    (is (= fact-bindings
           (:fact-bindings semantic-identity)
           (get-in decision-record [:preserved :fact-bindings])
           (:fact-bindings verifier-replay)))
    (is (= operation-order
           (:operation-order semantic-identity)
           (get-in decision-record [:preserved :operation-order])))
    (is (= (:effect-order-graph mir)
           (:effect-order-graph semantic-identity)
           (get-in decision-record [:preserved :effect-order-graph])))
    (is (= (:runtime-check-table mir)
           (:retained-runtime-checks residual-check-report)
           (:residual-checks decision-record)))
    (is (= [] (:elided-runtime-checks residual-check-report)))
    (is (= [] (:open-proof-obligations residual-check-report)))
    (is (= [] (:facts-invalidated invalidation-ledger)))
    (is (= [] (:analysis-invalidated invalidation-ledger)))
    (is (= [] (:proofs-invalidated invalidation-ledger)))
    (is (= semantic-id (:semantic-id c13)))
    (is (= artifact-id (:artifact-id c13)))
    (is (= (:actual-path-binding-id c13)
           (bootstrap/p15-s23-c13-c14-b1-actual-path-binding-id
            (:semantic-id c13) (:actual-path-provenance c13))))
    (is (= :gravity/pinned-gravity-source-rule (:artifact source-rule)))
    (is (= :gravity.compiler/c13-mir-optimization (:owner source-rule)))
    (is (= bootstrap/p15-s23-c13-source-byte-count
           (:source-byte-count source-rule)))
    (is (= bootstrap/p15-s23-c13-expected-source-content-hash
           (:source-content-hash source-rule)))
    (is (= bootstrap/p15-s23-c13-expected-plan-semantic-hash
           (:plan-semantic-hash source-rule)))
    (is (= bootstrap/p15-s23-c13-expected-functions-semantic-hash
           (:functions-semantic-hash source-rule)))
    (is (= bootstrap/p15-s23-c13-builder-function
           (:builder-function source-rule)))
    (is (= bootstrap/p15-s23-c13-expected-builder-semantic-hash
           (:builder-semantic-hash source-rule)))
    (is (= bootstrap/p15-s23-c13-required-functions
           (:function-shapes source-rule)))
    (is (= :gravity-source (:semantic-authority source-rule)))
    (is (= false (:self-hosted? source-rule)))
    (is (= source-path (:source actual-path-provenance)))
    (is (= (get-in c11 [:provenance :actual-paths :c11-source])
           (:c11-source actual-path-provenance)))
    (is (string? (:c13-source actual-path-provenance)))
    (is (= canonical-target
           (:target (get-in c13 [:invalidation-ledger]))))))

(defn- assert-c14-raw
  [backend {:keys [packet c13 policy raw-c14 c11]}]
  (let [expected (raw-c14-from-packet packet)
        request (:request raw-c14)
        expected-backend :gravity.backend/llvm]
    (is (= :llvm backend))
    (is (= c14-policy-keys (set (keys policy))))
    (is (= expected raw-c14))
    (is (= :accepted (:status raw-c14)))
    (is (= :gravity/c14-bounded-llvm-lowering-record
           (:artifact raw-c14)))
    (is (= :accepted (:status request)))
    (is (= expected-backend (get-in raw-c14 [:eligibility :backend])))
    (is (= expected-backend (get-in request [:target :backend])))
    (is (= (:artifact-id c13) (get-in request [:input :artifact-id])))
    (is (= (:semantic-id c13) (get-in request [:input :id])))
    (is (= (:artifact-id c13) (get-in request [:input :optimization-report :artifact-id])))
    (is (= (:request-id policy) (:request-id request)))
    (is (= :hosted (:profile policy) (:profile request)))
    (is (= (bootstrap/p15-s23-b3-llvm-expected-profile-contract)
           (:profile-contract policy)
           (:profile-contract request)))
    (is (= (:target policy) (:target request) (get-in raw-c14 [:eligibility :target])))
    (is (= canonical-target
           (get-in policy [:target :canonical-target])
           (get-in request [:target :canonical-target])
           (get-in raw-c14 [:eligibility :target :canonical-target])))
    (is (= :llvm (get-in policy [:target :request])))
    (is (= :llvm (get-in request [:target :request])))
    (is (= "x86_64-unknown-linux-gnu"
           (get-in policy [:target :triple])
           (get-in request [:target :triple])))
    (is (= :elf (get-in policy [:target :object-format])))
    (is (= :x86_64 (get-in policy [:target :architecture])))
    (is (= (assoc (bootstrap/p15-s23-b3-llvm-expected-abi-contract)
                  :return-type :i32)
           (:abi policy)
           (:abi request)
           (get-in raw-c14 [:abi-layout :abi])))
    (is (= (bootstrap/p15-s23-b3-llvm-expected-runtime-contract)
           (:runtime policy)
           (:runtime request)
           (get-in raw-c14 [:runtime-provider :runtime])))
    (is (= (bootstrap/p15-s23-b3-llvm-expected-provider-contract)
           (:providers policy)
           (:providers request)
           (get-in raw-c14 [:runtime-provider :providers])))
    (is (= (:providers policy) (:providers request)
           (get-in raw-c14 [:eligibility :required-providers])))
    (is (= (:effects policy) (:effects request)))
    (is (= (:capabilities policy) (:capabilities request)))
    (is (= (:safety policy) (:safety request)))
    (is (= (:source-map policy) (:source-map request)))
    (is (= (:dependencies policy)
           (:dependency-provenance raw-c14)))
    (is (= :gravity-source (:semantic-authority raw-c14)))
    (is (= [] (:diagnostics raw-c14)))
    (is (not (contains? raw-c14 :authority)))
    (is (not (contains? request :authority)))
    (let [c11-link
          (or (get-in request [:input :c11-artifact-id])
              (get-in raw-c14 [:bounded-lowering-payload :c11-artifact-id]))]
      (if c11-link
        (is (= (:artifact-id c11) c11-link))
        ;; The LLVM C14 request intentionally carries the C11 id through the
        ;; C13 input record rather than repeating it in the request input.
        (is (= (:artifact-id c11)
               (get-in c13 [:input :c11-artifact-id])))))))

(defn- assert-b1-carrier
  [{:keys [c13 c14 b1 policy]}]
  (let [target (:target policy)
        expected-abi (assoc
                      (bootstrap/p15-s23-b3-llvm-expected-abi-contract)
                      :return-type :i32)]
    (is (= :gravity/b1-verified-backend-input-packet (:artifact b1)))
    (is (= 1 (:schema-version b1)))
    (is (= :accepted-for-bounded-llvm (:status b1)))
    (is (= (:artifact-id c13) (get-in b1 [:input :artifact-id])))
    (is (= target (:target b1)
           (get-in b1 [:eligibility :target])
           (get-in b1 [:c14-eligibility :target])))
    (is (= :llvm-x86_64-linux (:canonical-target target)))
    (is (= "x86_64-unknown-linux-gnu" (:triple target)))
    (is (= :elf (:object-format target)))
    (is (= :x86_64 (:architecture target)))
    (is (= expected-abi (:abi b1)))
    (is (= (:runtime policy) (:runtime b1)))
    (is (= (:providers policy) (:providers b1)))
    (is (= #{} (:effects b1)))
    (is (= #{} (:capabilities b1)))
    (is (= (:safety policy) (:safety b1)))
    (is (= [:llvm-ir :elf-x86_64-object :elf-x86_64-executable]
           (get-in b1 [:backend-manifest :emits])))
    (is (= (get-in c14 [:request :request-id])
           (get-in b1 [:backend-manifest :c14-request-id])))
    (is (= (:artifact-id c14)
           (get-in b1 [:backend-manifest :c14-artifact-id])))
    (is (= :gravity-source (:semantic-authority b1)))
    (is (= :clojure-stage0-rule-runner (:execution-tcb b1)))
    (is (true? (:clojure-seed-boundary? b1)))
    (is (false? (:self-hosted? b1)))
    (is (= [] (:diagnostics b1)))
    (is (not (contains? b1 :authority)))
    (is (not (contains? b1 :public?)))))

(defn- assert-contextual-report
  [{:keys [packet c11 report]}]
  (is (= :gravity/c13-c14-b1-contextual-verification-report
         (:artifact report)))
  (is (= 1 (:schema-version report)))
  (is (= :passed (:status report)))
  (is (= [:passed :passed :passed :passed]
         ((juxt :c11 :c13 :c14 :b1) report)))
  (is (= (:artifact-id packet) (:packet-id report)))
  (is (= (:semantic-id packet) (:semantic-id report)))
  (is (= (:mir-id c11) (:fresh-c11-mir-id report)))
  (is (= (get-in packet [:c13 :artifact-id]) (:c13-artifact-id report)))
  (is (= (get-in packet [:c14 :request :request-id])
         (:c14-request-id report)))
  (is (= (get-in packet [:c14 :artifact-id]) (:c14-artifact-id report)))
  (is (= (get-in packet [:b1 :artifact-id]) (:b1-artifact-id report)))
  (is (= :passed (:gravity-source-replay report)))
  (is (= :passed (:independent-reconstruction report)))
  (is (false? (:self-hosted? report)))
  (is (= (:report-id report)
         (bootstrap/p15-s23-c11-mir-digest
         {:kind :gravity/c13-c14-b1-contextual-verification-report
           :schema-version 1
           :report (dissoc report :report-id)}))))

(defn- assert-packet-envelope
  [backend {:keys [packet c11 checked-core]}]
  (is (= :llvm backend))
  (is (= bootstrap/p15-s23-c13-c14-b1-final-packet-keys
         (set (keys packet))))
  (is (= :gravity/p15-s23-c13-c14-b1-authenticated-packet
         (:kind packet)))
  (is (= :accepted-for-bounded-llvm (:status packet)))
  (is (= :gravity-source (:semantic-authority packet)))
  (is (= :clojure-stage0-independent-reconstruction
         (:verification-tcb packet)))
  (is (= (:artifact-id c11) (get-in packet [:c11 :artifact-id])))
  (is (= (:mir-id c11) (get-in packet [:c11 :mir-id])))
  (is (= (:artifact-id checked-core)
         (get-in packet [:c11 :checked-core-artifact-id])))
  (is (= [] (:diagnostics packet)))
  (is (= bootstrap/p15-s23-c13-c14-b1-final-packet-scope
         (:scope packet)))
  (is (= :passed (get-in packet [:c11 :verifier-record :status])))
  (is (= :passed (get-in packet [:c11 :verifier-record :semantic-replay-parity])))
  (is (= :clojure-stage0-rule-runner
         (get-in packet [:c11 :verifier-record :execution-tcb])))
  (is (not (contains? (get-in packet [:c11 :verifier-record])
                      :live-invocation-claim?))))

(deftest sh17-c13-optimized-mir-carrier-is-authenticated-for-linux-llvm
  (let [rows (mapv (fn [backend] [backend (carrier backend)]) targets)]
    (is (= #{:gravity/c13-bounded-identity-optimized-mir}
           (set (map (comp :artifact :c13) (map second rows)))))
    (doseq [[backend row] rows]
      (testing (name backend)
        (assert-packet-envelope backend row)
        (assert-fresh-c11-report row)
        (assert-c13-carrier row)
        (assert-c14-raw backend row)
        (assert-b1-carrier row)
        (assert-contextual-report row)
        (assert-claim-boundary row)
        (is (= (:raw-c14 row)
               (invoke-c14 backend (:c13 row) (:policy row))))
        (is (true?
             (bootstrap/p15-s23-stage2-c13-c14-b1-authentic?
              (:packet row) (:checked-core row) (:context row))))))))

(deftest sh17-c13-carrier-identities-ignore-actual-checkout-paths
  (let [left (build-upstream :llvm "sh17-c13-carrier-left.gravity")
        right (build-upstream :llvm "sh17-c13-carrier-right.gravity")
        left-c13 (:c13 left) right-c13 (:c13 right)
        left-packet (:packet left) right-packet (:packet right)]
    (is (= (:source left) (:source right)))
    (is (= (:semantic-id left-c13) (:semantic-id right-c13)))
    (is (= (:artifact-id left-c13) (:artifact-id right-c13)))
    (is (= (:semantic-id left-packet) (:semantic-id right-packet)))
    (is (= (:artifact-id left-packet) (:artifact-id right-packet)))
    (is (not= (get-in left-c13 [:actual-path-provenance :source])
              (get-in right-c13 [:actual-path-provenance :source])))
    (is (not= (:actual-path-binding-id left-c13)
              (:actual-path-binding-id right-c13)))
    (is (not= (:actual-path-binding-id left-packet)
              (:actual-path-binding-id right-packet)))
    (is (not (contains?
              (bootstrap/p15-s23-c13-c14-b1-stage-semantic-input left-c13)
              :actual-path-provenance)))
    (assert-claim-boundary left)
    (assert-claim-boundary right)))

(defn- c14-rejection
  [backend optimized policy]
  (invoke-c14 backend optimized policy))

(defn- assert-c14-rejection
  [result diagnostic missing-fact]
  (is (= [:rejected diagnostic missing-fact
          [diagnostic]]
         ((juxt :status :diagnostic :missing-fact :diagnostics) result))
      result)
  (is (not (contains? result :authority))))

(defn- assert-c14-input-rejection
  [result]
  (assert-c14-rejection result "C14-INPUT" :verified-optimized-mir))

(deftest sh17-c13-c14-carrier-mutations-fail-closed
  (doseq [backend targets]
    (let [{:keys [c13 policy]} (carrier backend)
          hostile-c13
          [[:key-set-injection (assoc c13 :hostile-key true)]
           [:key-set-removal (dissoc c13 :optimized-mir)]
           [:artifact (assoc c13 :artifact :gravity/substituted)]
           [:status (assoc c13 :status :rejected)]
           [:authority (assoc c13 :semantic-authority :untrusted)]
           [:source-rule (assoc-in c13 [:source-rule :owner]
                                   :gravity/attacker)]
           [:source-identity (assoc-in c13 [:source-rule :source-content-hash]
                                       "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")]
           [:pass-contract (assoc-in c13 [:pass-contract :maximum-operation-count]
                                     1)]
           [:artifact-id (assoc c13 :artifact-id
                                "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")]
           [:c11-verifier (assoc-in c13 [:verifier-replay :c11-mir-id]
                                    "sha256:dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd")]
           [:c11-replay (assoc-in c13 [:verifier-replay :result] :rejected)]
           [:fact-bindings (assoc-in c13 [:semantic-identity :fact-bindings :type :content-id]
                                     "sha256:eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")]
           [:runtime-checks (assoc-in c13 [:residual-check-report :retained-runtime-checks]
                                      [{:hostile true}])]
           [:residual-status (assoc-in c13 [:residual-check-report :status]
                                       :rejected)]
           [:elided-runtime-checks
            (assoc-in c13 [:residual-check-report :elided-runtime-checks]
                      [:hostile-check])]
           [:effect-order (assoc-in c13 [:semantic-identity :effect-order-graph]
                                    {:hostile true})]
           [:operation-order (update-in c13 [:semantic-identity :operation-order]
                                        #(conj % "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"))]
           [:mir-substitution (assoc c13 :optimized-mir
                                     (assoc (:optimized-mir c13)
                                            :module-id "sha256:abababababababababababababababababababababababababababababababab"))]
           [:decision (assoc-in c13 [:decision-record :output-mir]
                                "sha256:1212121212121212121212121212121212121212121212121212121212121212")]
           [:ledger (assoc-in c13 [:invalidation-ledger :facts-invalidated]
                              [:hostile-fact])]
           [:ledger-analysis
            (assoc-in c13 [:invalidation-ledger :analysis-invalidated]
                      [:hostile-analysis])]
           [:ledger-target (assoc-in c13 [:invalidation-ledger :target]
                                     :hostile-target)]
           [:replay-input-module
            (assoc-in c13 [:verifier-replay :expected-input-module-id]
                      "sha256:3434343434343434343434343434343434343434343434343434343434343434")]]]
      (testing (str (name backend) " C13 mutation")
        (doseq [[label hostile] hostile-c13]
          (testing (name label)
            (assert-c14-input-rejection
             (c14-rejection backend hostile policy)))))
      (let [hostile-policies
            [[:policy-key-injection (assoc policy :hostile-key true)]
             [:policy-key-removal (dissoc policy :request-id)]
             [:policy-c11-id (assoc policy :expected-c11-artifact-id
                                    "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")]
             [:policy-c13-id (assoc policy :expected-c13-artifact-id
                                    "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")]
             [:policy-target (assoc-in policy [:target :canonical-target]
                                       :hostile-non-linux-target)]
             [:policy-triple (assoc-in policy [:target :triple]
                                       "hostile-unsupported-triple")]
             [:policy-profile (assoc policy :profile :native)]
             [:policy-abi (assoc-in policy [:abi :architecture] :hostile-arch)]
             [:policy-runtime (assoc-in policy [:runtime :platform-runtime-providers]
                                        [:unsupported/process-startup])]
             [:policy-providers (assoc-in policy [:providers :platform]
                                          [:unsupported/process-startup])]
             [:policy-effects (assoc policy :effects #{:io})]
             [:policy-capabilities (assoc policy :capabilities #{:filesystem})]
             [:policy-safety (assoc-in policy [:safety :unsafe-islands] 1)]
             [:policy-source-map (assoc-in policy [:source-map :preserved?] false)]
             [:policy-target-policy (assoc-in policy [:target-policy :public?] true)]
             [:policy-c11-verifier-report
              (assoc-in policy [:c11-verifier :status] :rejected)]
             [:policy-facts (assoc-in policy [:fact-bindings :type :content-id]
                                      "sha256:cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc")]
             [:policy-proofs (assoc-in policy [:proofs :capability :content-id]
                                       "sha256:dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd")]
             [:policy-dependencies (assoc-in policy [:dependencies :source-core]
                                             "sha256:eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")]
             ]]
        (doseq [[label hostile-policy] hostile-policies]
          (testing (str (name backend) " " (name label))
            (let [result (c14-rejection backend c13 hostile-policy)
                  ;; Policy tampering reaches the C14 policy/surface gate,
                  ;; whereas the two expected-id substitutions fail at the
                  ;; authenticated optimized-MIR ingress.  Keep those
                  ;; diagnostics distinct and stable.
                  [diagnostic missing-fact]
                  (cond
                    (= :policy-profile label)
                    ["C14-PROFILE" :hosted-profile-eligibility]

                    (contains? #{:policy-c11-id :policy-c13-id
                                 :policy-facts} label)
                    ["C14-INPUT" :verified-optimized-mir]

                    (contains? #{:policy-key-injection :policy-key-removal
                                 :policy-target :policy-triple :policy-abi
                                 :policy-runtime :policy-providers
                                 :policy-effects :policy-capabilities
                                 :policy-safety :policy-source-map
                                 :policy-target-policy
                                 :policy-c11-verifier-report
                                 :policy-proofs
                                 :policy-dependencies} label)
                    ["C14-TARGET" :explicit-bounded-llvm-target]

                    :else
                    ["C14-UNSUPPORTED" :bounded-llvm-mir-surface])]
              (assert-c14-rejection result diagnostic missing-fact))))))))

(deftest sh17-full-packet-stage-substitution-and-context-replay-fail-closed
  (doseq [backend targets]
    (let [{:keys [packet checked-core context]} (carrier backend)
          other-packet (:packet @alternate-linux-carrier)
          preflight-cases
          [[:packet-key-set-injection (assoc packet :hostile-key true)
            "B1-METADATA"
            (get final-envelope-missing-facts backend)]
           [:packet-key-set-removal (dissoc packet :c13)
            "B1-METADATA"
            (get final-envelope-missing-facts backend)]
           [:packet-artifact (assoc packet :kind :gravity/substituted)
            "B1-METADATA"
            (get final-envelope-missing-facts backend)]
           [:packet-status (assoc packet :status :rejected)
            "B1-METADATA"
            (get final-envelope-missing-facts backend)]
           [:packet-authority (assoc packet :semantic-authority :untrusted)
            "B1-METADATA"
            (get final-envelope-missing-facts backend)]
           [:packet-semantic-id (assoc packet :semantic-id
                                       "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
            "B1-METADATA"
            (get final-identity-missing-facts backend)]
           [:packet-artifact-id (assoc packet :artifact-id
                                       "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")
            "B1-METADATA"
            (get final-identity-missing-facts backend)]
           [:packet-path-binding-id (assoc packet :actual-path-binding-id
                                           "sha256:cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc")
            "B1-METADATA"
            (get final-identity-missing-facts backend)]
           [:packet-path-provenance
            (rehash-packet
             (assoc-in packet [:actual-path-provenance :source]
                       "attacker/source.gravity"))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:c13-path-provenance
            (rehash-packet
             (assoc-in packet [:c13 :actual-path-provenance :source]
                       "attacker/c13-source.gravity"))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:c13-semantic-id
            (rehash-packet
             (assoc-in packet [:c13 :semantic-id]
                       "sha256:abababababababababababababababababababababababababababababababab"))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:c13-path-binding-id
            (rehash-packet
             (assoc-in packet [:c13 :actual-path-binding-id]
                       "sha256:cdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcd"))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:c14-request-id
            (rehash-packet
             (assoc-in packet [:c14 :request :request-id]
                       "sha256:efefefefefefefefefefefefefefefefefefefefefefefefefefefefefefefef"))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:c13-stage-substitution (rehash-packet
                                     (assoc packet :c13 (:c13 other-packet)))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:c14-stage-substitution (rehash-packet
                                     (assoc packet :c14 (:c14 other-packet)))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:b1-stage-substitution (rehash-packet
                                    (assoc packet :b1 (:b1 other-packet)))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:c11-verifier-replay
            (rehash-packet
             (assoc-in packet [:c13 :verifier-replay :result] :rejected))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:c11-verifier-report
            (rehash-packet
             (assoc-in packet [:c11 :verifier-record :status] :rejected))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:fact-bindings
            (rehash-packet
             (assoc-in packet [:c13 :semantic-identity :fact-bindings :type :content-id]
                       "sha256:dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:runtime-checks
            (rehash-packet
             (assoc-in packet [:c13 :residual-check-report :retained-runtime-checks]
                       [{:hostile true}]))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:operation-effect-order
            (rehash-packet
             (assoc-in packet [:c13 :semantic-identity :effect-order-graph]
                       {:hostile true}))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:optimized-mir-substitution
            (rehash-packet
             (assoc-in packet [:c13 :optimized-mir]
                       (:optimized-mir (:c13 other-packet))))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:decision-ledger
            (rehash-packet
             (assoc-in packet [:c13 :decision-record :output-mir]
                       "sha256:eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]]]
      (doseq [[label hostile expected-id expected-missing] preflight-cases]
        (testing (str (name backend) " " (name label))
          (let [data (packet-diagnostic backend hostile checked-core context)]
            (is (nil? (:unexpected data)) data)
            (is (= expected-id (:id data)) data)
            (is (= expected-id (:rule data)) data)
            (is (= expected-missing (:missing-fact data)) data)
            (is (= :rejected (:fallback-status data)) data)
            (is (= (:diagnostic-id data)
                   (bootstrap/c15-stable-diagnostic-id data)) data)
            (is (false?
                 (bootstrap/p15-s23-stage2-c13-c14-b1-authentic?
                  hostile checked-core context)))))))))
