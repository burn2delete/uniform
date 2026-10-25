(ns gravity.self-hosting.sh15-authenticated-envelope-rejection-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh02-authenticated-envelope-test]
            [gravity.self-hosting.sh15-absent-layout-location-test]))

(def ^:private compile-plan
  #'gravity.self-hosting.sh15-absent-layout-location-test/compile-plan)
(def ^:private c15-path
  "bootstrap/gravity/src/gravity/compiler/c15_compiler_diagnostics.gravity")
(def ^:private c15-plan (delay (compile-plan c15-path)))
(def ^:private invoke-envelope
  #'gravity.self-hosting.sh02-authenticated-envelope-test/invoke-envelope)
(def ^:private valid-descriptor
  (delay (#'gravity.self-hosting.sh02-authenticated-envelope-test/descriptor
          (#'gravity.self-hosting.sh02-authenticated-envelope-test/fixture-case
           "accepted/envelope-comparison.gravity"))))
(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh15-envelope-preservation-development :compiler-artifact-plan? true}
   plan function arguments))
(defn- c15 [function arguments] (invoke @c15-plan function arguments))
(defn- build [descriptor]
  (invoke-envelope 'authenticated-envelope-build-template [descriptor]))
(defn- preserve [result] (c15 'sh15-preserve-envelope-rejection [result]))
(defn- verify [original candidate]
  (c15 'sh15-verify-envelope-rejection [original candidate]))
(defn- reason [result] (get-in result [:diagnostics 0 :facts :reason]))

(deftest actual-envelope-validation-rejections-retain-all-original-data
  (doseq [[descriptor expected]
          [[nil :descriptor-shape] [{} :descriptor-shape]
           [(assoc @valid-descriptor :artifact :unexpected) :artifact]
           [(update @valid-descriptor :schema-version inc) :schema-version]
           [(assoc @valid-descriptor :stage nil) :stage]
           [(assoc @valid-descriptor :artifact-kind nil) :artifact-kind]]]
    (let [original (build descriptor) before (pr-str original)
          diagnostic (first (:diagnostics original)) candidate (preserve original)
          fresh (compile-plan c15-path)]
      (is (= :rejected (:status original)))
      (is (= expected (:reason diagnostic)))
      (is (= :validation (:phase diagnostic)))
      (is (= :preserved (:status candidate)))
      (is (= original (:source-result candidate)))
      (is (= (:diagnostics original) (:diagnostics candidate)))
      (is (= before (pr-str original)))
      (is (= :passed (:status (verify original candidate))))
      (is (= candidate (invoke fresh 'sh15-preserve-envelope-rejection [original])))
      (is (= :passed (:status (invoke fresh 'sh15-verify-envelope-rejection
                                     [original candidate]))))
      (is (= :none (:semantic-authority candidate)))
      (is (= :coordinator-mapping-required (:diagnostic-id-status candidate)))
      (is (= :external-unverified-preservation-input (:source-status candidate)))
      (is (every? (set (:nonclaims candidate))
                  [:upstream-semantic-authentication :normative-diagnostic-id
                   :canonical-c15-record :public-safe-projection]))
      (is (not-any? #(contains? diagnostic %)
                    [:diagnostic-id :primary :span :origin-chain :profile :target])))))

(deftest actual-envelope-replay-rejections-retain-the-original-phase-and-reason
  (let [descriptor @valid-descriptor accepted (build descriptor)]
    (is (= :accepted (:status accepted)))
    (is (= :invalid-envelope-rejection (reason (preserve accepted))))
    (doseq [[template requests expected]
            [[(assoc (:artifact-template accepted) :tampered true)
              (:digest-requests accepted) :artifact-template-replay]
             [(:artifact-template accepted) [] :digest-request-replay]]]
      (let [original (invoke-envelope 'authenticated-envelope-verify-template
                                      [descriptor template requests])
            candidate (preserve original)]
        (is (= :rejected (:status original)))
        (is (= expected (get-in original [:diagnostics 0 :reason])))
        (is (= :verification (get-in candidate [:diagnostics 0 :phase])))
        (is (= original (:source-result candidate)))
        (is (= (:claim-boundary original) (get-in candidate [:source-result :claim-boundary])))
        (is (= (:containment original) (get-in candidate [:source-result :containment])))
        (is (= :passed (:status (verify original candidate))))))))

(deftest fresh-original-comparison-rejects-loss-and-coherent-substitution
  (let [original (build {}) candidate (preserve original)
        alternate (build (assoc @valid-descriptor :stage nil))
        fresh (compile-plan c15-path)]
    (doseq [mutation [(dissoc candidate :source-result)
                      (assoc candidate :diagnostics [])
                      (assoc-in candidate [:source-result :fatal?] false)
                      (assoc-in candidate [:diagnostics 0 :reason] :different)
                      (assoc candidate :diagnostic-id-status :resolved)
                      (assoc candidate :semantic-authority :compiler)
                      (preserve alternate)
                      (-> candidate
                          (assoc-in [:source-result :diagnostics 0 :reason] :different)
                          (assoc-in [:diagnostics 0 :reason] :different))]]
      (let [result (invoke fresh 'sh15-verify-envelope-rejection [original mutation])]
        (is (= :rejected (:status result)))
        (is (= :candidate-substitution (reason result)))))
    ;; Self-consistency is preservation evidence, never producer authentication.
    (let [supplied (assoc-in original [:diagnostics 0 :reason] :different)
          supplied-candidate (preserve supplied)]
      (is (= :passed (:status (verify supplied supplied-candidate))))
      (is (some #{:upstream-semantic-authentication} (:nonclaims supplied-candidate))))))

(deftest malformed-carriers-are-rejected-without-inspecting-opaque-values
  (let [original (build {}) calls (atom 0)
        opaque (reify Object (toString [_] (swap! calls inc)
                               (throw (ex-info "private" {}))))]
    (doseq [input [opaque (assoc original :diagnostics (list (first (:diagnostics original))))
                   (assoc original :diagnostics [])
                   (update-in original [:diagnostics 0] dissoc :stage)
                   (assoc-in original [:diagnostics 0 :phase] :invented)
                   (assoc-in original [:containment :downstream-artifacts-forbidden] false)
                   (assoc original :claim-boundary [])
                   (assoc original :extra true)]]
      (let [result (preserve input)]
        (is (= :rejected (:status result)))
        (is (= :invalid-envelope-rejection (reason result)))
        (is (nil? (get-in result [:diagnostics 0 :offending-diagnostic-id])))
        (is (nil? (get-in result [:diagnostics 0 :artifact-id])))))
    (is (= :unsupported-candidate-carrier (reason (verify original opaque))))
    (is (= 0 @calls))))
