(ns gravity.self-hosting.sh16-checked-mir-identity-bridge-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh12-checked-i64-add-producer-test :as add]
            [gravity.self-hosting.sh12-checked-i64-sub-producer-test :as sub]
            [gravity.self-hosting.sh13-mir-integer-add-bridge-test :as execution]
            [gravity.self-hosting.sh13-branch-join-execution-test]
            [gravity.self-hosting.sh17-c13-optimized-mir-carrier-test]))

;; C13/C18/TEST2: comparison of supplied records cannot authenticate invocation,
;; freshness, arbitrary MIR validity or admission. Actual composition is separate.
(defn- existing [namespace name] (var-get (ns-resolve namespace name)))
(defn- path [relative] (str (.resolve @(add/existing 'root) relative)))
(defn- compile-plan [relative]
  (let [file (path relative)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! file :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter file (slurp file))))

(def ^:private plans
  {:c13 (delay (compile-plan "bootstrap/gravity/src/gravity/compiler/c13_mir_optimization_passes.gravity"))
   :c14 (delay (compile-plan "bootstrap/gravity/src/gravity/compiler/c14_target_lowering_architecture.gravity"))
   :b1 (delay (compile-plan "bootstrap/gravity/src/gravity/backend/b1_backend_interface_specification.gravity"))})

(defn- call [stage name arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :sh16-original-mir-identity-comparison :compiler-artifact-plan? true}
   @(get plans stage) name arguments))

(defn- build [mir evidence]
  (call :c13 'c13-build-bounded-identity-optimized-mir [mir evidence]))

(defn- evidence [mir report]
  {:verification-class :original-c11-reconstruction
   :reconstructed-mir mir :reconstruction-report report})

(defn- supplied []
  (let [core (str "sha256:" (apply str (repeat 64 "a"))) entry (str core ":mir:entry")
        mir {:artifact :gravity/mir-module :source-core core :module-id 'supplied
             :profile :hosted :target-request :jvm :verification-status :pending
             :functions {'main {:params ["left" "right"] :entry entry
                                :blocks {entry {:instructions [{:op-id "sub" :operands ["token" "left" "right"]}]
                                                :terminator {:kind :return :operands ["sub"]}}}}}
             :runtime-check-table {"check" {:failure :error/numeric :numeric-mode :checked}}
             :pass-contract {:pass-id :c11-build-checked-i64-sub}
             :pass-execution-record {:pass-id :c11-build-checked-i64-sub
                                     :status :constructed-unverified :record-id :pending-independent-verifier}
             :b1-preflight {:status :pending-c11-verifier :backend-credit? false}}
        report {:artifact :gravity/sh12-branch-join-mir-report :schema-version 1
                :scope :closed-i64-parameter-linear :phase :verification :status :passed
                :checked-core-artifact-id core :mir-module-id 'supplied
                :checks [:single-function-linear :blocks-terminators-and-cfg-edges
                         :entry-parameter-and-local-dominance :type-fact-preservation
                         :effect-order-preservation :ownership-capability-and-profile-preservation
                         :safety-outcome-preservation :source-origin-preservation]
                :preserves [:type-facts :effect-facts :ownership-facts :capability-facts :safety-facts
                            :profile-target-facts :source-spans :source-origin-map :generated-origin-chains
                            :control-flow-edges :data-flow-edges :dominance-facts]
                :diagnostics [] :nonclaims [:generic-c11-verifier :independent-acceptance
                                            :optimizer-credit :backend-credit :self-hosted?]}]
    [mir (evidence mir report)]))

(defn- rejected [mir request]
  (let [result (build mir request)]
    (is (= :rejected (:status result)))
    (is (= ["C13-VERIFY"] (:diagnostics result)))
    (is (true? (nil? (:optimized-mir result))))))

(deftest sh16-identity-comparison-pending-result-development-kernel
  (let [[mir request] (supplied) result (build mir request)]
    (is (= :constructed-unverified (:status result)))
    (is (= [:pending :not-admitted :not-established :not-established]
           ((juxt :verification-status :admission-status :semantic-replay-parity :freshness-status) result)))
    (is (= mir (:optimized-mir result)))
    (is (= (:reconstruction-report request) (:reconstruction-report result)))
    (is (= [] (get-in result [:decision-record :proofs-used])))
    (is (= :not-established (get-in result [:decision-record :verifier-result])))
    (is (false? (get-in result [:decision-record :changed?])))
    (is (every? empty? (vals (:invalidation-ledger result))))
    (is (= (:runtime-check-table mir) (get-in result [:residual-check-report :retained-runtime-checks])))
    (is (nil? (:verifier-replay result)))
    (is (nil? (:source-rule result)))
    (is (= [true false] ((juxt :clojure-seed-boundary? :self-hosted?) result)))
    (is (= result (build mir request)))))

(deftest sh16-identity-comparison-malformed-and-complete-field-mutations-development-kernel
  (let [[mir request] (supplied)]
    (doseq [candidate (concat (map #(dissoc request %) (keys request))
                             [(assoc request :extra true) (assoc request :verification-class :unknown)
                              (assoc request :verification-class nil)
                              (assoc-in request [:reconstruction-report :status] :rejected)
                              (assoc-in request [:reconstruction-report :checked-core-artifact-id] :stale)
                              (assoc-in request [:reconstruction-report :mir-module-id] 'wrong)
                              (assoc-in request [:reconstruction-report :scope] :single-conditional-branch-join)
                              (assoc-in request [:reconstruction-report :checks] [])
                              (assoc-in request [:reconstruction-report :nonclaims] [])
                              (assoc-in request [:reconstruction-report :independent-acceptance] true)])]
      (rejected mir candidate))
    (doseq [field (keys (:reconstruction-report request))]
      (rejected mir (update request :reconstruction-report dissoc field)))
    (let [entry (get-in mir [:functions 'main :entry]) block [:functions 'main :blocks entry]
          malformed [(dissoc mir :source-core) (assoc mir :source-core :bad)
                     (assoc mir :source-core [:bad]) (dissoc mir :functions)
                     (assoc mir :functions :bad) (assoc mir :functions {'main :bad})
                     (update-in mir [:functions 'main] dissoc :blocks)
                     (assoc-in mir [:functions 'main :blocks] :bad)
                     (assoc-in mir block :bad) (update-in mir block dissoc :instructions)
                     (assoc-in mir (conj block :instructions) :bad)
                     (assoc-in mir (conj block :instructions) [:bad])]]
      (doseq [candidate malformed]
        (rejected candidate request)
        (rejected mir (assoc request :reconstructed-mir candidate))))
    (doseq [[field value] [[:type-table {:changed true}] [:effect-table {:changed true}]
                           [:ownership-table {}] [:capability-table {}] [:safety-table {}]
                           [:runtime-check-table {}] [:proof-certificate-table {}] [:source-map {}]
                           [:data-flow-graph {}] [:control-flow-graph {}] [:effect-order-graph {}]
                           [:pass-contract {}] [:pass-execution-record {}] [:physical-provenance {:changed true}]
                           [:profile :native] [:target-request :llvm-x86_64-linux] [:extra :ignored?]]]
      (rejected (assoc mir field value) request)
      (rejected mir (assoc-in request [:reconstructed-mir field] value)))))

(deftest sh16-identity-comparison-bounds-and-coherent-data-nonclaims-development-kernel
  (let [[mir request] (supplied)
        nest (fn [n] (nth (iterate vector :payload) n))]
    (let [width-control (vec (range 1024)) widened (assoc mir :width-control width-control)
          result (build widened (assoc request :reconstructed-mir widened))]
      (is (= {:status :accepted}
             (call :c13 'sh16-c13-evidence-preflight [width-control])))
      (is (= :constructed-unverified (:status result)))
      (is (= widened (:optimized-mir result))))
    (doseq [payload [(Object.) (nest 257) (vec (repeat 1025 0))
                     (vec (repeat 1024 (vec (repeat 64 0)))) (apply str (repeat 4097 "x"))]]
      (rejected (assoc mir :opaque payload) request)
      (rejected mir (assoc-in request [:reconstructed-mir :opaque] payload))
      (rejected mir (assoc-in request [:reconstruction-report :opaque] payload)))
    (doseq [placement [:input :reconstruction :report]]
      (let [callbacks (atom {:hash 0 :equals 0 :string 0})
            opaque (proxy [Object] []
                     (hashCode [] (swap! callbacks update :hash inc) 1231)
                     (equals [_] (swap! callbacks update :equals inc) false)
                     (toString [] (swap! callbacks update :string inc) "opaque"))
            input (if (= placement :input) (assoc mir :opaque opaque) mir)
            candidate (case placement
                        :input request
                        :reconstruction (assoc-in request [:reconstructed-mir :opaque] opaque)
                        :report (assoc-in request [:reconstruction-report :opaque] opaque))]
        (rejected input candidate)
        (is (false? (call :c13 'sh16-c13-evidence-scalar? [opaque])))
        (is (false? (call :c13 'sh16-structural-scalar-valid? [opaque])))
        (is (= {:hash 0 :equals 0 :string 0} @callbacks))))
    ;; Only built-in persistent carriers; no custom Map callback guarantee.
    (doseq [[make-map class-name] [[array-map "clojure.lang.PersistentArrayMap"]
                                  [hash-map "clojure.lang.PersistentHashMap"]]
            placement [:input :reconstruction :report]]
      (let [callbacks (atom {:hash 0 :equals 0 :string 0})
            opaque (proxy [Object] []
                     (hashCode [] (swap! callbacks update :hash inc) 1231)
                     (equals [_] (swap! callbacks update :equals inc) false)
                     (toString [] (swap! callbacks update :string inc) "opaque"))
            carrier (make-map opaque :payload)
            input (if (= placement :input) (assoc mir :opaque-key-map carrier) mir)
            candidate (case placement
                        :input request
                        :reconstruction (assoc-in request [:reconstructed-mir :opaque-key-map] carrier)
                        :report (assoc-in request [:reconstruction-report :opaque-key-map] carrier))]
        (reset! callbacks {:hash 0 :equals 0 :string 0})
        (is (= class-name (.getName (class carrier))))
        (rejected input candidate)
        (is (= {:hash 0 :equals 0 :string 0} @callbacks))))
    (doseq [make-map [array-map hash-map]]
      (let [carrier (make-map :a 1 :b false)]
        (is (= (vec (mapcat identity carrier)) (call :c13 'sh16-c13-evidence-children [carrier])))
        (is (= {:status :accepted}
               (call :c13 'sh16-c13-evidence-preflight [carrier])))))
    ;; Coherent caller-forged records can match; the result claims no validation.
    (let [forged (assoc mir :opaque :coherently-forged) result (build forged (assoc request :reconstructed-mir forged))]
      (is (= :constructed-unverified (:status result)))
      (is (= forged (:optimized-mir result)))
      (is (= :not-established (:freshness-status result)))
      (is (some #{:authentication} (:nonclaims result))))))

(deftest sh16-identity-comparison-keeps-public-gates-and-legacy-records-development-kernel
  (let [[mir request] (supplied) result (build mir request)
        shape (existing 'gravity.self-hosting.sh17-c13-optimized-mir-carrier-test 'development-comparison-input)
        legacy (shape 2 3 false)
        before (pr-str legacy)
        expected (bootstrap/p15-s23-c13-expected-record (:mir legacy) (:evidence legacy))
        actual (build (:mir legacy) (:evidence legacy))
        repeated (build (:mir legacy) (:evidence legacy))]
    (is (false? (call :c14 'c14-c13-envelope-valid? [result])))
    (is (false? (call :c14 'c14-bounded-optimized-input-valid? [result {}])))
    (is (false? (call :b1 'b1-bounded-c14-input-valid? [result])))
    (is (false? (call :c14 'c14-c13-envelope-valid? [(assoc result :status :accepted)])))
    (is (false? (call :b1 'b1-bounded-c14-input-valid? [(assoc result :status :accepted)])))
    (is (= expected actual))
    (is (= actual repeated))
    (is (= before (pr-str legacy)))
    (is (= :rejected (:status (build mir {}))))
    (rejected (:mir legacy) (assoc (:evidence legacy) :verification-class :unknown))))

(def ^:private original-pairs
  (delay
    {:add @add/current-pair :sub @sub/current-pair
     :generic ((existing 'gravity.self-hosting.sh13-branch-join-execution-test 'current-parameter-pair)
                (path "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/i64_parameter_source.gravity")
                nil (path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))}))

(deftest sh16-identity-comparison-original-producer-verifier-and-execution-composition
  ;; Fresh original producers and consumers, separately authorized composition.
  (doseq [[family pair] @original-pairs]
    (let [core (:core pair) mir (add/invoke 'sh12-build-branch-join-mir core)
          reconstruction (add/invoke 'sh12-build-branch-join-mir core)
          before (add/invoke 'sh12-verify-branch-join-mir core mir)
          report (add/invoke 'sh12-verify-branch-join-mir core reconstruction)
          result (build mir (evidence reconstruction report)) output (:optimized-mir result)]
      (is (= :passed (:status before) (:status report)))
      (is (= :constructed-unverified (:status result)))
      (is (= mir output))
      (is (= report (:reconstruction-report result)))
      (is (= :passed (:status (add/invoke 'sh12-verify-branch-join-mir core output))))
      (doseq [arguments (case family :add [[7 3] [Long/MAX_VALUE 1]]
                         :sub [[7 3] [-1 Long/MIN_VALUE] [Long/MIN_VALUE 1]]
                         :generic [[0] [Long/MIN_VALUE]])]
        (let [original (execution/run core mir arguments) retained (execution/run core output arguments)]
          (is (= original retained))
          (is (= :passed (:status (execution/verify core mir arguments original))))
          (is (= :passed (:status (execution/verify core output arguments retained)))))))))

(deftest sh16-identity-comparison-original-mutants-remain-c11-rejections
  (doseq [[family pair] @original-pairs]
    (let [core (:core pair) mir (add/invoke 'sh12-build-branch-join-mir core)
          report (add/invoke 'sh12-verify-branch-join-mir core mir)
          entry (get-in mir [:functions 'main :entry]) op-path [:functions 'main :blocks entry :instructions]
          operations (get-in mir op-path)
          mutations (concat [(assoc-in mir [:pass-contract :pass-id] :foreign)
                             (assoc-in mir (conj op-path 0 :effects) #{:io/write})
                             (assoc-in mir (conj op-path 0 :source) {:changed true})]
                      (when (#{:add :sub} family)
                        [(assoc-in mir (conj op-path 3 :operands) (vec (rest (:operands (get operations 3)))))
                         (assoc mir :runtime-check-table {})
                         (assoc-in mir op-path (assoc operations 2 (get operations 3) 3 (get operations 2)))]))]
      (doseq [candidate mutations]
        (rejected candidate (evidence mir report))
        (let [original (add/invoke 'sh12-verify-branch-join-mir core candidate)
              coherent (build candidate (evidence candidate report))
              returned (:optimized-mir coherent)
              execution (execution/run core candidate (if (= family :generic) [1] [7 3]))]
          (is (= :rejected (:status original)))
          (is (= :constructed-unverified (:status coherent)))
          (is (= candidate returned))
          (is (= :rejected (:status (add/invoke 'sh12-verify-branch-join-mir core returned))))
          (is (= :rejected (:status execution)))
          (is (nil? (:execution execution))))))))
