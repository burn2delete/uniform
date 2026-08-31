(ns gravity.compiler-pass-manifest-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.compiler-pass-manifest :as manifest]))

(def upstream-artifact
  {:kind :gravity/stage0-math-conformance-artifact
   :profile-manifest {:metadata {}}})

(defn diagnostic-id
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo exception
      (:id (ex-data exception)))))

(deftest compiler-pass-manifest-is-a-bootstrap-free-stage-boundary
  (is (nil? (find-ns 'gravity.bootstrap)))
  (let [contract (manifest/compiler-pass-manifest-contract)
        publics (ns-publics 'gravity.compiler-pass-manifest)
        artifact (manifest/compiler-pass-source-artifact-from-upstream
                  "compiler-passes.gravity" upstream-artifact)]
    (is (= 'gravity.compiler-pass-manifest (:namespace contract)))
    (is (= :stage0-compiler-pass-manifest
           (:contract-boundary contract)))
    (is (some #{'gravity.bootstrap}
              (get-in contract [:dependency-direction :forbids])))
    (is (true? (:compatibility-only? contract)))
    (is (false? (:canonical-authority? contract)))
    (is (false? (:self-hosted? contract)))
    (is (= (set (keys publics)) (set (keys (:public-api contract)))))
    (doseq [[name spec] (:public-api contract) :when (:arglists spec)]
      (is (= (:arglists spec) (:arglists (meta (get publics name)))) name))
    (is (nil? (get (ns-aliases 'gravity.compiler-pass-manifest)
                   'bootstrap)))
    (is (= :gravity/stage0-pass-contract-manifest-artifact
           (:kind artifact)))
    (is (= ["C1" "C15" "C16" "C17" "C18"]
           (:document-set artifact)))
    (is (= manifest/compiler-pass-default-stage-order
           (:pipeline-stage-order artifact)))
    (is (= 19 (count (:pass-contract-registry artifact))))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (= manifest/compiler-pass-diagnostic-ids
           (get-in artifact [:compiler-pass-results
                             :required-diagnostic-ids])))))

(deftest stage-validators-reject-invalid-boundary-evidence
  (let [suite (manifest/compiler-pass-suite {})
        source-path "compiler-passes.gravity"
        profile-manifest {}]
    (doseq [[expected-id validate]
            [["C1-PASS-CONTRACT"
              #(manifest/compiler-pass-validate-pipeline!
                source-path profile-manifest
                (update suite :contracts
                        (fn [contracts]
                          (assoc contracts 0 (dissoc (first contracts)
                                                     :output)))))]
             ["C1-EVIDENCE-DROP"
              #(manifest/compiler-pass-validate-pipeline!
                source-path profile-manifest
                (update suite :contracts
                        (fn [contracts]
                          (assoc contracts 0
                                 (assoc (first contracts)
                                        :invalidates [:source-spans]
                                        :regenerates []
                                        :emits [])))))]
             ["C16-PROOF"
              #(manifest/compiler-pass-validate-incremental!
                source-path profile-manifest
                (assoc suite :proof-reuse-records
                       [{:proof-id :stale :status :stale :reuse :accepted}]))]
             ["C17-CAPABILITY"
              #(manifest/compiler-pass-validate-plugins!
                source-path profile-manifest
                (assoc-in suite [:plugin-manifest :requested-scopes]
                          #{:write-release-artifact}))]
             ["C18-EVIDENCE"
              #(manifest/compiler-pass-validate-verification!
                source-path profile-manifest
                (assoc suite :risk-classification
                       [{:pass :read-source
                         :risk :critical
                         :minimum-evidence #{:required}
                         :available-evidence #{}
                         :release-gate :required}]))]]]
      (testing expected-id
        (is (= expected-id (diagnostic-id validate)))))))
