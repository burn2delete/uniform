(ns gravity.self-hosting.pass-obligation-construction-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
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

(def ^:private risk-contract
  {:artifact :gravity/c18-pass-risk-contract
   :risk-levels [:low :medium :high :critical]
   :required-fields [:pass-id :pass-version :stage :risk :reason :input-ir
                     :output-ir :affected-profiles :affected-targets
                     :changed-fact-families :minimum-evidence :stronger-evidence
                     :release-gate :diagnostic-policy]
   :risk-rules
   {:low [:metadata-only-or-reporting :verifier-checked]
    :medium [:changes-ir-shape :semantics-preserved-by-verifier]
    :high [:changes-optimization-checks-effects-ownership-target-metadata-or-safety]
    :critical [:trusted-semantic-base :safety-gate :proof-checker
               :target-lowering-for-safety-critical-profile]}
   :minimum-evidence-by-risk
   {:low [:verifier-report]
    :medium [:verifier-report :golden-fixture]
    :high [:translation-validation :property-or-differential-test]
    :critical [:mechanized-proof-or-checked-certificate
               :translation-validation :conformance-fixtures]}
   :forbidden [:unclassified-pass :risk-classification-without-reason
               :high-risk-pass-with-optional-release-gate
               :risk-derived-from-human-wording]})

(def ^:private obligation-contract
  {:artifact :gravity/c18-pass-proof-obligation-contract
   :required-fields [:obligation-id :pass-id :pass-version :stage :claim
                     :input-artifact :output-artifact :changed-functions
                     :properties :assumptions :checker :required-evidence
                     :available-evidence :source-spans :origin-chain
                     :profile :target :status]
   :properties [:same-observable-result :same-effects :same-capabilities
                :same-safety-outcomes :same-profile-legality
                :source-origin-preserved :diagnostic-compatibility-preserved
                :artifact-provenance-preserved]
   :statuses [:pending :accepted :rejected :stale :blocked]
   :rules [:obligation-created-before-risky-transform-release
           :assumptions-are-structured :checker-is-named
           :stale-inputs-reject-obligation :missing-evidence-emits-c18-evidence]
   :forbidden [:proof-obligation-hidden-in-prose
               :unchecked-certificate-as-evidence :benchmark-as-correctness-proof
               :eml-tree-identity-as-equivalence]})

(def ^:private risk-argument-fields
  [:pass :pass-version :stage :risk :reason :affected-profiles :affected-targets
   :minimum-evidence :release-gate])

(def ^:private obligation-argument-fields
  [:obligation-id :pass-id :pass-version :claim :input-artifact :output-artifact
   :properties :assumptions :checker :required-evidence :profile :target])

(defn- risk-fields [variant]
  (if (= :first variant)
    {:pass :check-elision :pass-version "first" :stage :mir
     :risk :high :reason #{:removes-runtime-checks}
     :affected-profiles #{:native} :affected-targets #{:jvm}
     :minimum-evidence {:checker :named-only :status :accepted
                        :provenance {:input ["first" {:source :original}]}}
     :release-gate :required}
    {:pass :target-lowering :pass-version "second" :stage :target-lowering
     :risk :critical :reason #{:trusted-semantic-base :target-metadata}
     :affected-profiles #{:kernel :firmware} :affected-targets #{:native}
     :minimum-evidence {:checker :another-name :status :rejected
                        :provenance {:input ["second" {:source :changed}]}}
     :release-gate :blocked}))

(defn- obligation-fields [variant]
  (if (= :first variant)
    {:obligation-id :first-obligation :pass-id :check-elision :pass-version "first"
     :claim {:property :same-effects :function :first}
     :input-artifact {:id "first-input" :provenance {:source [:first :input]}}
     :output-artifact {:id "first-output" :provenance {:pass [:first :output]}}
     :properties #{:same-effects :same-safety-outcomes}
     :assumptions [{:kind :profile-legality :profile :native}]
     :checker 'proof/accepted-checker
     :required-evidence {:certificate :first :status :accepted
                         :release-status :passed
                         :provenance {:inputs ["first-input" "first-output"]}}
     :profile :native :target :jvm}
    {:obligation-id :second-obligation :pass-id :target-lowering :pass-version "second"
     :claim {:property :same-capabilities :function :second}
     :input-artifact {:id "second-input" :provenance {:source [:second :input]}}
     :output-artifact {:id "second-output" :provenance {:pass [:second :output]}}
     :properties #{:same-capabilities :artifact-provenance-preserved}
     :assumptions [{:kind :target-legality :target :native}]
     :checker 'proof/another-checker
     :required-evidence {:certificate :second :status :rejected
                         :release-status :blocked
                         :provenance {:inputs ["second-input" "second-output"]}}
     :profile :kernel :target :native}))

(def ^:private constructors
  [{:function 'build-c18-pass-risk
    :argument-fields risk-argument-fields
    :fields risk-fields
    :fixed-fields {:artifact :gravity/pass-risk :contract risk-contract}
    :template '(pass-id pass-version stage risk reason affected-profiles
                affected-targets minimum-evidence release-gate)
    :contract-symbol 'c18-pass-risk-contract}
   {:function 'build-c18-proof-obligation
    :argument-fields obligation-argument-fields
    :fields obligation-fields
    :fixed-fields {:artifact :gravity/pass-proof-obligation
                   :status :pending :contract obligation-contract}
    :template '(obligation-id pass-id pass-version claim input-artifact
                output-artifact properties assumptions checker required-evidence
                profile target)
    :contract-symbol 'c18-pass-proof-obligation-contract}])

(deftest current-uniform-constructors-preserve-supplied-record-fields
  ;; C18/D9/BOOT4: construction retains supplied data. Named evidence and
  ;; checkers do not establish proof acceptance, migration or release authority.
  (doseq [{:keys [function argument-fields fields fixed-fields template contract-symbol]}
          constructors
          variant [:first :second]]
    (testing (str function " " variant)
      (let [supplied (fields variant)
            expected (merge supplied fixed-fields)
            product (invoke function (mapv supplied argument-fields))
            ;; Regression fixture for the old quoted constructor body.
            symbol-template (merge fixed-fields (zipmap argument-fields template)
                                   {:contract contract-symbol})]
        (is (= expected product))
        (is (not= expected symbol-template))
        (is (not= symbol-template product))
        (is (= (:contract fixed-fields) (:contract product)))
        (is (not-any? #(contains? product %)
                      [:result :proof-accepted? :release-status :release-artifacts]))
        (when (= 'build-c18-proof-obligation function)
          (is (= :pending (:status product))))))))

(deftest every-constructor-argument-independently-changes-its-product-field
  (doseq [{:keys [function argument-fields fields fixed-fields]} constructors]
    (let [original (fields :first)
          alternate (fields :second)
          product (invoke function (mapv original argument-fields))]
      (doseq [field argument-fields]
        (testing (str function " argument " field)
          (let [changed (assoc original field (get alternate field))
                changed-product (invoke function (mapv changed argument-fields))]
            (is (not= (get original field) (get alternate field)))
            (is (= (merge changed fixed-fields) changed-product))
            (is (not= product changed-product))
            (is (= (dissoc product field) (dissoc changed-product field)))))))))

(deftest constructors-reject-wrong-arities-with-stable-diagnostics
  (doseq [{:keys [function argument-fields fields]} constructors]
    (let [arguments (mapv (fields :first) argument-fields)
          arity (count argument-fields)]
      (doseq [wrong-arguments [[] (pop arguments) (conj arguments :extra)]]
        (testing (str function " arity " (count wrong-arguments))
          (let [failure (try
                          (invoke function wrong-arguments)
                          nil
                          (catch clojure.lang.ExceptionInfo e (ex-data e)))]
            (is (= "L2-FUNCTION-ARITY" (:id failure)))
            (is (= function (:function failure)))
            (is (= arity (:expected-arity failure)))
            (is (= (count wrong-arguments) (:actual-arity failure)))))))))
