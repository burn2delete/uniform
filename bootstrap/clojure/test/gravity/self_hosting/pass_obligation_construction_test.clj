(ns gravity.self-hosting.pass-obligation-construction-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private source-path
  "language/compiler/src/gravity/compiler/c18_compiler_verification_pass_correctness.gravity")

(def ^:private plan
  (delay
    (let [path (.getCanonicalPath (io/file source-path))
          emitter (:emitter
                   (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan
       emitter path (slurp path :encoding "UTF-8")))))

(defn- invoke [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh16-pass-obligation-development :compiler-artifact-plan? true}
   @plan function arguments))

(deftest current-uniform-constructors-preserve-supplied-record-fields
  ;; C18/D9/BOOT4: construction retains supplied data. Named evidence and
  ;; checkers do not establish proof acceptance, migration or release authority.
  (doseq [variant [:first :second]]
    (let [risk-fields
          {:pass variant :pass-version (name variant) :stage :mir
           :risk :high :reason #{:removes-runtime-checks}
           :affected-profiles #{:native} :affected-targets #{:jvm}
           :minimum-evidence {:checker variant
                              :provenance {:input [variant "source"]}}
           :release-gate :required}
          risk (invoke 'build-c18-pass-risk
                       (mapv risk-fields
                             [:pass :pass-version :stage :risk :reason
                              :affected-profiles :affected-targets
                              :minimum-evidence :release-gate]))
          obligation-fields
          {:obligation-id variant :pass-id variant :pass-version (name variant)
           :claim {:property :same-effects :function variant}
           :input-artifact {:id "input" :provenance {:source variant}}
           :output-artifact {:id "output" :provenance {:pass variant}}
           :properties #{:same-effects :same-safety-outcomes}
           :assumptions [{:kind :profile-legality :profile :native}]
           :checker variant
           :required-evidence {:certificate variant :inputs ["input" "output"]}
           :profile :native :target :jvm}
          obligation
          (invoke 'build-c18-proof-obligation
                  (mapv obligation-fields
                        [:obligation-id :pass-id :pass-version :claim
                         :input-artifact :output-artifact :properties
                         :assumptions :checker :required-evidence :profile :target]))]
      (is (= risk-fields (dissoc risk :artifact :contract)))
      (is (= :gravity/pass-risk (:artifact risk)))
      (is (map? (:contract risk)))
      (is (= :gravity/c18-pass-risk-contract (get-in risk [:contract :artifact])))
      (is (= obligation-fields (dissoc obligation :artifact :status :contract)))
      (is (= :gravity/pass-proof-obligation (:artifact obligation)))
      (is (= :pending (:status obligation)))
      (is (map? (:contract obligation)))
      (is (= :gravity/c18-pass-proof-obligation-contract
             (get-in obligation [:contract :artifact]))))))
