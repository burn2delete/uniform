(ns gravity.lowering.pipeline-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.lowering :as lowering]))

;; These handles are deliberately not authenticated artifact identities.
;; The raw reconstruction API transports caller-owned data; its legacy status
;; labels cannot admit these fixtures to the canonical C14 pipeline.
(defn- fixture [target]
  {:optimized {:artifact :gravity/c13-bounded-identity-optimized-mir
               :artifact-id "fixture:unbound-c13" :semantic-id "fixture:unbound-mir"
               :optimized-mir {:source-map {"op" {:source "caller.qst" :start-byte 7 :end-byte 9}}}
               :decision-record {:decision-id "fixture:decision"}
               :verifier-replay {:result :unverified-fixture}
               :semantic-identity {:operation-order ["op"]}}
   :policy {:request-id "fixture:unbound-request" :expected-c11-artifact-id "fixture:c11"
            :profile :hosted :profile-contract {:name :hosted}
            :target {:backend target} :abi {:calling-convention :fixture}
            :runtime nil :providers [] :effects #{} :capabilities #{}
            :safety {:fixture :unverified} :proofs [] :contract-bindings {}
            :required-evidence [] :source-map {:id "fixture:source-map" :preserved? true}
            :dependencies {:source "caller.qst" :origin-chain [{:kind :source :syntax-id "fixture:syntax"}]}
            :fact-bindings {:type :fixture-type :ownership :fixture-ownership :safety :fixture-safety}
            :proof-target-metadata {:entries [] :proofless-metadata-rejected? true}
            :target-policy {:fixture true} :unsupported-surface [:fixture-unsupported]
            :c11-verifier nil :c11-verifier-id nil}})

(def ^:private builders
  {:llvm [lowering/llvm-record bootstrap/p15-s23-c14-expected-record]
   :c [lowering/c-record bootstrap/p15-s23-c14-c-expected-record]
   :wasm [lowering/wasm-record bootstrap/p15-s23-c14-wasm-expected-record]})

(defn- assert-record! [target optimized policy expected raw]
  (case target
    :llvm (lowering/assert-llvm-reconstruction!
            #'bootstrap/p15-s23-c11-mir-require-strict-structure! #'bootstrap/p15-s23-b3-llvm-fail!
            "caller.qst" optimized policy expected raw)
    :c (lowering/assert-c-reconstruction!
         #'bootstrap/p15-s23-c11-mir-require-strict-structure! #'bootstrap/p15-s23-b3-llvm-fail!
         "caller.qst" {:artifact-id (:expected-c11-artifact-id policy)} optimized policy expected raw)
    :wasm (lowering/assert-wasm-reconstruction!
            #'bootstrap/p15-s23-c11-mir-require-strict-structure! #'bootstrap/p15-s23-b4-wasm-fail!
            "caller.qst" expected raw)))

(defn- rejection [thunk]
  (try (thunk) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))

(deftest contracts-identify-the-raw-data-and-exact-comparison-boundaries
  (let [publics (ns-publics 'gravity.lowering)
        contract (:gravity/namespace-contract (meta #'lowering/llvm-record))]
    (is (= 'gravity.lowering (:namespace contract)))
    (is (= (set (keys publics)) (set (keys (:public-api contract)))))
    (is (false? (:authenticates-inputs? contract)))
    (is (false? (:issues-certificates? contract)))
    (is (false? (:legacy-status-labels-authoritative? contract)))
    (is (= [] (get-in contract [:dependency-direction :requires])))
    (is (some #{:canonical-c11-c13-admission} (:unsupported contract)))
    (doseq [[name operation] publics]
      (is (= contract (:gravity/namespace-contract (meta operation))))
      (is (= (:arglists (meta operation)) (get-in contract [:public-api name :arglists])))
      (is (= :none (get-in contract [:public-api name :admission]))))))

(deftest raw-records-preserve-request-facts-provenance-and-compatibility
  (doseq [[target [build compatibility]] builders
          :let [{:keys [optimized policy]} (fixture target)
                record (build optimized policy)]]
    (is (= record (compatibility optimized policy)))
    (is (= (:request-id policy) (get-in record [:request :request-id])))
    (is (= (:artifact-id optimized) (get-in record [:request :input :artifact-id])))
    (is (= (:semantic-id optimized) (get-in record [:request :input :id])))
    (is (= (:target policy) (get-in record [:request :target]) (get-in record [:eligibility :target])))
    (is (identical? (:source-map policy) (get-in record [:request :source-map])))
    (is (= (:dependencies policy) (:dependency-provenance record)))
    (is (= (get-in policy [:fact-bindings :type]) (get-in record [:abi-layout :type-facts])))
    (is (= (get-in policy [:fact-bindings :ownership]) (get-in record [:abi-layout :ownership-facts])))
    (is (= (:proofs policy) (get-in record [:proof-target-metadata :proofs])))
    (is (nil? (assert-record! target optimized policy record record)))
    (when (#{:c :wasm} target)
      (is (= (:optimized-mir optimized) (get-in record [:bounded-lowering-payload :mir])))
      (is (= (:fact-bindings policy) (get-in record [:bounded-lowering-payload :fact-bindings]))))))

(deftest real-strict-comparison-rejects-serialized-record-drift
  (doseq [[target [build]] builders
          :let [{:keys [optimized policy]} (fixture target) record (build optimized policy)]]
    (doseq [path [[:request :request-id] [:request :source-map :id] [:dependency-provenance :source]]]
      (let [error (rejection #(assert-record! target optimized policy record (assoc-in record path "changed")))]
        (is (= "C11-VERIFY" (:id error)))
        (is (keyword? (:missing-fact error)))))))

(deftest coherent-records-still-reject-wrong-upstream-context
  (let [{:keys [optimized policy]} (fixture :llvm)
        record (lowering/llvm-record optimized policy)
        error (rejection #(assert-record! :llvm (assoc optimized :artifact-id "wrong-c13") policy record record))]
    (is (= "C14-MANIFEST" (:id error)))
    (is (= :exact-c13-bound-c14-reconstruction (:missing-fact error))))
  (let [{:keys [optimized policy]} (fixture :c)
        record (lowering/c-record optimized policy)
        error (rejection #(lowering/assert-c-reconstruction!
                            #'bootstrap/p15-s23-c11-mir-require-strict-structure! #'bootstrap/p15-s23-b3-llvm-fail!
                            "caller.qst" {:artifact-id "wrong-c11"} optimized policy record record))]
    (is (= "C14-MANIFEST" (:id error)))
    (is (= :exact-c13-bound-c14-c-reconstruction (:missing-fact error)))))

(defn- diagnostic-port [& _] :before)

(deftest diagnostic-var-is-read-after-the-strict-comparison-callback
  (let [{:keys [optimized policy]} (fixture :llvm)
        record (lowering/llvm-record optimized policy)
        events (atom [])]
    (with-redefs [diagnostic-port (fn [& _] :before)]
      (is (= :after
             (lowering/assert-llvm-reconstruction!
               (fn [& args]
                 (swap! events conj args)
                 (alter-var-root #'diagnostic-port (constantly (fn [& _] :after))))
               #'diagnostic-port "caller.qst" (assoc optimized :artifact-id "wrong-c13") policy record record))))
    (is (= [["caller.qst" record record :independent-c14-lowering-reconstruction]] @events))))

(def ^:private gravity-source "bootstrap/gravity/src/gravity/compiler/c14_target_lowering_architecture.gravity")
(def ^:private gravity-plan
  (delay
    (let [path (.getCanonicalPath (io/file gravity-source))
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :llvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path)))))

(defn- source-functions []
  (binding [*read-eval* false]
    (with-open [r (java.io.PushbackReader. (io/reader gravity-source))]
      (loop [names #{}]
        (let [form (read {:eof ::eof} r)]
          (if (= ::eof form) names
            (recur (if (and (seq? form) (= 'defn (first form))) (conj names (second form)) names))))))))

(deftest complete-current-gravity-source-compiles-and-rejects-invalid-input
  (is (= :gravity/stage2-compiler-artifact-plan (:kind @gravity-plan)))
  (is (= (source-functions) (set (keys (:functions @gravity-plan)))))
  (doseq [[target function] [[:llvm 'c14-build-bounded-llvm-lowering-record]
                            [:c 'c14-build-bounded-c-lowering-record]
                            [:wasm 'c14-build-bounded-wasm-lowering-record]]
          :let [{:keys [optimized policy]} (fixture target)]
          arguments [[nil {}] [optimized policy]]]
    (let [result (bootstrap/p15-s23-stage2-runtime-execute-function
                   {:engine :gravity-lowering-development :compiler-artifact-plan? true}
                   @gravity-plan function arguments)]
      (is (= :rejected (:status result)))
      (is (= ["C14-INPUT"] (:diagnostics result))))))
