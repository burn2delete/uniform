(ns gravity.self-hosting.sh11-overflow-cast-safety-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh11-division-bounds-safety-test]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh11_overflow_cast_safety_test.clj")]
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "repository root not found"
                        {:id "SH11-OVERFLOW-CAST-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path [relative] (str (.resolve @root relative)))

(defn- division-var [name]
  (var-get
   (ns-resolve
    'gravity.self-hosting.sh11-division-bounds-safety-test name)))

(defn- adapter-var [name]
  (var-get
   (ns-resolve
    'gravity.self-hosting.sh11-c9-safety-adapter-test name)))

(defn- invoke-c10 [function arguments]
  ((division-var 'invoke-c10) function arguments))

(defn- digest [ordinal] ((division-var 'digest) ordinal))
(defn- compile-fixture [relative] ((division-var 'compile-fixture) relative))

(defn- invoke-fixture [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh11-overflow-cast-fixture-test
    :compiler-artifact-plan? true}
   plan function arguments))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-11/overflow-cast")
(defn- fixture-path [family basename extension]
  (str fixture-root "/" family "/" basename extension))
(def ^:private accepted-gravity-path
  (fixture-path "accepted" "overflow-cast-safety" ".gravity"))
(def ^:private accepted-qst-path
  (fixture-path "accepted" "overflow-cast-safety" ".qst"))
(def ^:private rejected-gravity-path
  (fixture-path "rejected" "invalid-overflow-cast-safety" ".gravity"))
(def ^:private rejected-qst-path
  (fixture-path "rejected" "invalid-overflow-cast-safety" ".qst"))
(def ^:private accepted-gravity-plan
  (delay (compile-fixture accepted-gravity-path)))
(def ^:private accepted-qst-plan
  (delay (compile-fixture accepted-qst-path)))
(def ^:private rejected-gravity-plan
  (delay (compile-fixture rejected-gravity-path)))
(def ^:private rejected-qst-plan
  (delay (compile-fixture rejected-qst-path)))

(defn- prepared-two-integer-c9 []
  ((division-var 'prepared-two-integer-c9)))
(defn- operand-binding [role request result identity]
  ((division-var 'operand-binding) role request result identity))
(defn- operand-reference [binding]
  ((division-var 'operand-reference) binding))

(defn- operation-inputs [prepared operator]
  (let [bound (:bound prepared)
        requests (get-in bound [:ownership-core :ownership-requests])
        results (get-in bound [:ownership-core :ownership-results])
        identities (:fact-identities bound)
        roles (if (= operator :checked-narrowing) [:value] [:left :right])
        bindings (mapv operand-binding roles requests results identities)
        first-binding (first bindings)]
    {:source
     {:core-node-id (digest (case operator :add 801 :multiply 802 803))
      :source-span (:source-span first-binding)
      :origin-chain (:origin-chain first-binding)
      :profile (:profile first-binding)
      :target (:target first-binding)
      :type-fact-id (:type-fact-id first-binding)
      :effect-fact-id (:effect-fact-id first-binding)
      :capability-proof-id (:capability-proof-id first-binding)
      :ownership-fact-id (:ownership-fact-id first-binding)}
     :bindings bindings
     :operands (mapv operand-reference bindings)}))

(defn- typed-function [operator]
  (case operator
    :add 'sh11-authenticated-typed-add-operation
    :multiply 'sh11-authenticated-typed-multiply-operation
    'sh11-authenticated-typed-cast-operation))

(defn- invalidations [operator]
  (case operator
    :add [:operand-value-change :ownership-fact-change
          :numeric-mode-change :operator-change :integer-width-change
          :signedness-change :profile-change :target-change]
    :multiply [:operand-value-change :ownership-fact-change
               :numeric-mode-change :operator-change :integer-width-change
               :signedness-change :profile-change :target-change]
    [:operand-value-change :ownership-fact-change
     :numeric-mode-change :operator-change
     :source-type-change :target-type-change
     :source-signedness-change :target-signedness-change
     :profile-change :target-change]))

(defn- resolved [request ordinal]
  {:request request :digest (digest ordinal)})

(defn- runtime-support [plan]
  (invoke-fixture @plan 'sh11-runtime-target-support []))

(defn- accepted-operation [plan prepared operator ordinal]
  (let [inputs (operation-inputs prepared operator)
        typed (invoke-fixture
               @plan (typed-function operator)
               [(:source inputs) (:operands inputs)])
        typed-resolution
        (resolved typed (case operator :add 811 :multiply 812 813))
        support (runtime-support plan)
        support-resolution (resolved support 850)
        descriptor
        (invoke-fixture
         @plan 'sh11-authenticated-checked-operation
         [typed typed-resolution support support-resolution
          (invalidations operator)])]
    {:inputs inputs
     :typed typed
     :typed-resolution typed-resolution
     :support support
     :support-resolution support-resolution
     :descriptor descriptor}))

(defn- template [prepared descriptor]
  (invoke-c10
   'sh11-authenticated-overflow-cast-check-request
   [(:bound prepared) (:verification prepared) descriptor]))
(defn- classify [prepared descriptor resolution]
  (invoke-c10
   'sh11-classify-authenticated-overflow-cast
   [(:bound prepared) (:verification prepared) descriptor resolution]))
(defn- verify-result [prepared descriptor resolution candidate]
  (invoke-c10
   'sh11-verify-authenticated-overflow-cast-result
   [(:bound prepared) (:verification prepared)
    descriptor resolution candidate]))
(defn- diagnostic [result] (first (:diagnostics result)))

(defn- rewrite-typed [descriptor changes ordinal]
  (let [typed (merge (:typed-operation descriptor) changes)]
    (-> descriptor
        (merge (select-keys
                typed
                [:operation-id :kind :operator :core-node-id :operands
                 :numeric-contract :source-span :origin-chain :profile
                 :target :type-fact-id :effect-fact-id
                 :capability-proof-id :ownership-fact-id]))
        (assoc :typed-operation typed)
        (assoc :typed-operation-resolution (resolved typed ordinal)))))

(deftest sh11-overflow-cast-api-fixtures-policy-and-parity-are-closed
  (let [policy (invoke-c10 'sh11-authenticated-overflow-cast-policy [])
        functions (:functions @(adapter-var 'c10-plan))]
    (is (= #{:numeric-overflow :numeric-cast} (:operation-kinds policy)))
    (is (= #{:add :multiply} (:overflow-operators policy)))
    (is (= #{:runtime-checked :rejected} (:implemented-outcomes policy)))
    (is (= #{:proven-safe :runtime-checked :rejected :unsafe-island}
           (:outcomes policy)))
    (is (some #{:sh11-completion} (:nonclaims policy)))
    (is (some #{:performance-class-authority} (:nonclaims policy)))
    (is (not (some #{:operation-core-node-authenticated-by-c9}
                   (:nonclaims policy))))
    (doseq [[operator semantic]
            [[:add [:operator-change :signedness-change]]
             [:multiply [:operator-change :signedness-change]]
             [:checked-narrowing
              [:operator-change :source-signedness-change
               :target-signedness-change]]]]
      (doseq [invalidation semantic]
        (is (some #{invalidation}
                  (get-in policy [:invalidation-conditions operator])))))
    (doseq [[function arity]
            [['sh11-authenticated-overflow-cast-policy 0]
             ['sh11-authenticated-overflow-cast-check-request 3]
             ['sh11-classify-authenticated-overflow-cast 4]
             ['sh11-verify-authenticated-overflow-cast-result 5]]]
      (is (= arity (get-in functions [function :arity])) function))
    (doseq [[gravity qst]
            [[accepted-gravity-path accepted-qst-path]
             [rejected-gravity-path rejected-qst-path]]]
      (is (java.util.Arrays/equals
           (java.nio.file.Files/readAllBytes
            (java.nio.file.Path/of (path gravity) (make-array String 0)))
           (java.nio.file.Files/readAllBytes
            (java.nio.file.Path/of (path qst) (make-array String 0))))))
    (doseq [plan [accepted-gravity-plan accepted-qst-plan
                  rejected-gravity-plan rejected-qst-plan]]
      (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))))

(deftest sh11-runtime-support-executes-real-boundary-guards-and-failures
  (let [gravity-support (runtime-support accepted-gravity-plan)
        qst-support (runtime-support accepted-qst-plan)
        expected
        (invoke-c10 'sh11-overflow-cast-expected-execution-evidence [])
        evidence (:execution-evidence gravity-support)]
    (is (= gravity-support qst-support))
    (is (= expected evidence))
    (is (= 26 (count evidence)))
    (is (= #{:pass :fail} (set (map :guard-result evidence))))
    (is (= #{:not-taken :error/numeric-raised}
           (set (map :failure-path evidence))))
    (is (= #{:signed :unsigned}
           (set (map #(or (get-in % [:numeric-contract :signedness])
                          (get-in % [:numeric-contract :target-signedness]))
                     evidence))))
    (is (= #{:add :multiply :checked-narrowing}
           (set (map :operator evidence))))
    (doseq [case-id
            [:signed-add-cancellation-invalid-operands
             :unsigned-multiply-cancellation-invalid-operand
             :signed-cast-source-outside]]
      (let [case (first (filter #(= case-id (:case-id %)) evidence))]
        (is (= :fail (:guard-result case)))
        (is (= :error/numeric-raised (:failure-path case)))
        (is (some false? (map :member (:operand-membership case))))))
    (let [cancellation
          (first
           (filter
            #(= :signed-add-cancellation-invalid-operands (:case-id %))
            evidence))]
      (is (true? (:result-membership cancellation))))))

(deftest sh11-checked-add-multiply-and-cast-bind-exact-operation-and-support
  (let [prepared (prepared-two-integer-c9)]
    (doseq [[operator condition expression rule ordinal]
            [[:add :overflow
              :operands-in-declared-width-and-checked-add-result-within-width
              :SAFE9-OVERFLOW 811]
             [:multiply :overflow
              :operands-in-declared-width-and-checked-multiply-result-within-width
              :SAFE9-OVERFLOW 812]
             [:checked-narrowing :representable-cast
              :value-in-source-width-and-representable-in-target-width
              :SAFE9-NARROW 813]]]
      (let [{:keys [inputs typed support descriptor]}
            (accepted-operation accepted-gravity-plan prepared operator ordinal)
            qst (accepted-operation accepted-qst-plan prepared operator ordinal)
            request-template (template prepared descriptor)
            check-resolution (resolved (:check-request request-template)
                                       (+ ordinal 100))
            result (classify prepared descriptor check-resolution)
            outcome (first (:outcomes result))
            runtime-check (first (:runtime-checks result))]
        (testing (name operator)
          (is (= descriptor (:descriptor qst)))
          (is (= :accepted (:status request-template)) (pr-str request-template))
          (is (= :accepted (:status result)) (pr-str result))
          (is (= :runtime-checked (:outcome result)))
          (is (= [runtime-check] (:residual-checks result)))
          (is (= condition (:condition runtime-check)))
          (is (= expression (get-in runtime-check
                                    [:guard-proof :predicate :expression])))
          (is (= (:operator typed)
                 (get-in runtime-check [:guard-proof :predicate :operator])))
          (is (= (:core-node-id typed)
                 (get-in runtime-check [:guard-proof :core-node-id])))
          (is (= (:numeric-contract typed)
                 (get-in runtime-check
                         [:guard-proof :predicate :numeric-contract])))
          (is (= [:operand-source-membership
                  :result-target-membership]
                 (get-in runtime-check
                         [:guard-proof :predicate :guard-components])))
          (is (= (:bindings inputs)
                 (get-in runtime-check [:guard-proof :predicate :operands])))
          (is (true? (get-in runtime-check
                             [:guard-proof :guards-exact-operation])))
          (is (= (:execution-evidence support)
                 (:support-evidence runtime-check)))
          (is (= (:provider support) (:provider-id runtime-check)))
          (is (= rule (:specialized-safe-rule outcome)))
          (is (= (:source-span typed)
                 (get-in result [:provenance :operation-source :source-span])))
          (is (= :passed
                 (:status
                  (verify-result prepared descriptor check-resolution result)))))))))

(deftest sh11-adversarial-operation-provider-staleness-and-provenance-fail-closed
  (let [prepared (prepared-two-integer-c9)
        add (accepted-operation accepted-gravity-plan prepared :add 901)
        descriptor (:descriptor add)
        accepted-template (template prepared descriptor)
        check-resolution (resolved (:check-request accepted-template) 951)
        forged-typed (-> (:typed-operation descriptor)
                         (assoc :operation-id :forged-add)
                         (assoc :operator :multiply)
                         (assoc :core-node-id (digest 902))
                         (assoc :numeric-contract
                                {:bit-width 16 :signedness :unsigned}))
        coordinated-stale
        (-> descriptor
            (merge (select-keys forged-typed
                                [:operation-id :operator :core-node-id
                                 :numeric-contract]))
            (assoc :typed-operation forged-typed))
        coordinated-forgery
        (rewrite-typed
         descriptor
         {:operation-id :forged-operation
          :operator :multiply
          :core-node-id (digest 999)
          :numeric-contract {:bit-width 16 :signedness :unsigned}}
         999)
        provider-forgery
        (-> descriptor
            (assoc-in [:target-support :provider] :attacker/provider)
            (assoc :provider-id :attacker/provider))
        arbitrary-support-label
        (assoc descriptor :target-support-id (digest 903))
        coordinated-support-forgery
        (-> descriptor
            (assoc :target-support-id (digest 903))
            (assoc-in [:target-support-resolution :digest] (digest 903)))
        forged-support-evidence
        (-> descriptor
            (assoc-in [:target-support :execution-evidence 0 :guard-result]
                      :fail)
            (assoc-in [:support-evidence 0 :guard-result] :fail))
        forged-performance-class
        (assoc descriptor :performance-class :forged)
        forged-result-type
        (rewrite-typed descriptor {:result-type :gravity.type/bool} 908)
        forged-effect-contract
        (rewrite-typed descriptor
                       {:effect-contract {:failure-behavior :abort
                                          :effects #{}}}
                       909)
        swapped-operands
        (rewrite-typed descriptor
                       {:operands (vec (reverse (:operands descriptor)))}
                       811)
        multiply-descriptor
        (:descriptor
         (accepted-operation accepted-gravity-plan prepared :multiply 904))
        stale-operator
        multiply-descriptor
        stale-signedness
        multiply-descriptor
        widening-cast-base
        (:descriptor
         (accepted-operation accepted-gravity-plan prepared
                             :checked-narrowing 906))
        widening-cast
        (rewrite-typed widening-cast-base
                       {:operation-id :authenticated-incompatible-cast
                        :core-node-id (digest 804)
                        :numeric-contract
                        {:source-width 8 :target-width 16
                         :source-signedness :signed
                         :target-signedness :signed}}
                       814)
        malformed
        (invoke-fixture
         @rejected-gravity-plan 'sh11-malformed-operation
         [(:typed-operation descriptor)])
        substituted-envelope
        (assoc-in descriptor [:typed-operation :source-span]
                  {:source "forged.gravity" :start-byte 90 :end-byte 99})
        coordinated-provenance-forgery
        (rewrite-typed
         descriptor
         {:source-span
          {:source "forged.gravity" :start-byte 90 :end-byte 99}
          :origin-chain [{:generated-by :attacker}]}
         811)
        cases
        [[(template prepared coordinated-stale)
          "C10-PROOF" :untrusted-or-stale-typed-numeric-operation]
         [(template prepared coordinated-forgery)
          "C10-PROOF" :untrusted-or-stale-typed-numeric-operation]
         [(template prepared provider-forgery)
          "C10-CHECK" :invalid-authenticated-runtime-check-policy]
         [(template prepared arbitrary-support-label)
          "C10-CHECK" :invalid-authenticated-runtime-check-policy]
         [(template prepared coordinated-support-forgery)
          "C10-CHECK" :invalid-authenticated-runtime-check-policy]
         [(template prepared forged-support-evidence)
          "C10-CHECK" :invalid-authenticated-runtime-check-policy]
         [(template prepared forged-performance-class)
          "C10-CHECK" :invalid-authenticated-runtime-check-policy]
         [(template prepared forged-result-type)
          "C10-PROOF" :untrusted-or-stale-typed-numeric-operation]
         [(template prepared forged-effect-contract)
          "C10-PROOF" :untrusted-or-stale-typed-numeric-operation]
         [(template prepared swapped-operands)
          "C10-PROOF" :operation-operands-not-bound-to-c9-facts]
         [(classify prepared stale-operator check-resolution)
          "C10-CHECK" :substituted-authenticated-residual-check]
         [(classify prepared stale-signedness check-resolution)
          "C10-CHECK" :substituted-authenticated-residual-check]
         [(template prepared widening-cast)
          "C10-NUMERIC" :incompatible-overflow-or-cast-contract]
         [(template prepared malformed)
          "C10-NO-OUTCOME" :malformed-authenticated-overflow-cast-operation]
         [(template prepared substituted-envelope)
          "C10-PROOF" :untrusted-or-stale-typed-numeric-operation]
         [(template prepared coordinated-provenance-forgery)
          "C10-PROOF" :operation-operands-not-bound-to-c9-facts]]]
    (doseq [[result id reason] cases]
      (let [d (diagnostic result)]
        (is (= :rejected (:status result)) (pr-str result))
        (is (= id (:diagnostic-id d)))
        (is (= reason (:reason d)))
        (is (= (get-in add [:inputs :source :source-span]) (:source-span d)))
        (is (= (get-in add [:inputs :source :origin-chain])
               (:generated-origin-chain d)))
        (is (contains? #{:authenticated-typed-operation
                         :authenticated-c9-operand}
                       (:provenance-source d)))
        (is (vector? (:untrusted-fields d)))))
    (let [d (diagnostic (template prepared coordinated-provenance-forgery))]
      (is (= :authenticated-c9-operand (:provenance-source d)))
      (is (seq (:untrusted-fields d))))))

(deftest sh11-proof-unsafe-erasure-fifth-outcome-and-result-substitution-reject
  (let [prepared (prepared-two-integer-c9)
        multiply (accepted-operation accepted-gravity-plan prepared
                                     :multiply 1001)
        descriptor (:descriptor multiply)
        args [(:typed-operation descriptor)
              (:typed-operation-resolution descriptor)
              (:target-support descriptor)
              (:target-support-resolution descriptor)
              (:invalidation-conditions descriptor)]
        unproved (invoke-fixture @rejected-gravity-plan
                                'sh11-unproved-operation args)
        unsafe (invoke-fixture @rejected-qst-plan
                              'sh11-unsafe-operation args)
        rejection-cases
        [[(template prepared unproved) "C10-PROOF"
          :numeric-proof-not-authenticated-by-ownership-facts]
         [(template prepared unsafe) "C10-UNSAFE"
          :authenticated-unsafe-overflow-cast-not-supported]
         [(classify prepared descriptor nil) "C10-CHECK"
          :missing-authenticated-residual-check]]]
    (doseq [[result id reason] rejection-cases]
      (is (= :rejected (:status result)))
      (is (= id (:diagnostic-id (diagnostic result))))
      (is (= reason (:reason (diagnostic result)))))
    (let [request-template (template prepared descriptor)
          check-resolution (resolved (:check-request request-template) 1051)
          result (classify prepared descriptor check-resolution)
          erased (-> result
                     (assoc :runtime-checks [])
                     (assoc :residual-checks [])
                     (assoc-in [:outcomes 0 :runtime-check] nil))
          fifth (-> result
                    (assoc :outcome :deferred-to-backend)
                    (assoc-in [:outcomes 0 :outcome] :deferred-to-backend))
          duplicated (assoc result :outcomes
                            (conj (:outcomes result)
                                  (first (:outcomes result))))
          substituted (assoc-in result [:preserves :operator] :add)
          malformed (dissoc result :residual-checks)
          candidates
          [[erased "C10-OPTIMIZATION" :authenticated-residual-check-substitution]
           [fifth "C10-NO-OUTCOME" :illegal-outcome]
           [duplicated "C10-NO-OUTCOME" :outcome-count-mismatch]
           [substituted "C10-OPTIMIZATION"
            :authenticated-residual-check-substitution]
           [malformed "C10-NO-OUTCOME" :candidate-structural-bound]]]
      (doseq [[candidate id reason] candidates]
        (let [verification
              (verify-result prepared descriptor check-resolution candidate)]
          (is (= :rejected (:status verification)))
          (is (= id (:diagnostic-id (diagnostic verification))))
          (is (= reason (:reason (diagnostic verification)))))))))
