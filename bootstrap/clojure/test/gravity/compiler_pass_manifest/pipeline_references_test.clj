(ns gravity.compiler-pass-manifest.pipeline-references-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.compiler-pass-manifest :as manifest]))

(def ^:private source-path "pipeline-references.gravity")
(def ^:private stages manifest/compiler-pass-default-stage-order)
(def ^:private contracts manifest/compiler-pass-default-contracts)
(def ^:private references (mapv :pass contracts))
(def ^:private default-pipeline
  {:artifact :gravity/compiler-pipeline
   :pipeline-id "sha256:stage0-compiler-pipeline"
   :compiler :gravity-stage0-clojure-bootstrap
   :source-root "sha256:stage0-source-root"
   :profile :meta
   :target {:backend :jvm :triple "stage0"}
   :stages stages
   :pass-contracts references
   :evidence [:types :effects :ownership :capabilities :safety :proofs
              :diagnostics]
   :diagnostics "sha256:stage0-compiler-diagnostics"
   :artifact-graph "sha256:stage0-artifact-graph"})

(defn- profile-manifest [passes]
  {:profile :meta :target :jvm
   :metadata {:compiler {:passes passes}}})

(defn- construct [passes]
  (manifest/compiler-pass-source-artifact-from-upstream
   source-path
   {:kind :gravity/stage0-math-conformance-artifact
    :profile-manifest (profile-manifest passes)}))

(defn- validate [passes]
  (let [profile (profile-manifest passes)]
    (manifest/compiler-pass-validate-pipeline!
     source-path profile (manifest/compiler-pass-suite profile))))

(defn- outcome [thunk]
  (try
    {:result (thunk)}
    (catch clojure.lang.ExceptionInfo exception
      {:diagnostic (ex-data exception)
       :message (.getMessage exception)})))

(deftest consistent-references-preserve-manifests
  (doseq [[fixture passes expected]
          [[:default-manifest-preserved {} default-pipeline]
           [:reordered-contract-references
            {:pipeline-manifest-overrides {:pass-contracts (vec (reverse references))}}
            (assoc default-pipeline :pass-contracts (vec (reverse references)))]
           [:reordered-registry-with-matching-references
            {:contracts (vec (reverse contracts))
             :pipeline-manifest-overrides {:pass-contracts references}}
            default-pipeline]
           [:additional-manifest-metadata-preserved
            {:pipeline-manifest-overrides
             {:notes {:origin "compatibility-fixture" :ai/human-review :pending}
              :profile :native :target {:backend :llvm :triple "fixture"}}}
            (assoc default-pipeline
                   :notes {:origin "compatibility-fixture" :ai/human-review :pending}
                   :profile :native :target {:backend :llvm :triple "fixture"})]]]
    (testing (name fixture)
      (is (= :complete (validate passes)))
      (let [artifact (construct passes)]
        (is (= :gravity/stage0-pass-contract-manifest-artifact (:kind artifact)))
        (is (= expected (:pipeline-manifest artifact)))
        (is (= stages (:pipeline-stage-order artifact)))
        (is (= (get passes :contracts contracts) (:pass-contract-registry artifact)))
        (is (= (profile-manifest passes) (:profile-manifest artifact)))
        (is (= :gravity/stage0-math-conformance-artifact
               (:upstream-artifact-kind artifact)))
        (is (str/starts-with? (:upstream-artifact-hash artifact) "sha256:"))))))

(defn- assert-reference-refusal [passes field reason]
  (let [results [(outcome #(validate passes)) (outcome #(construct passes))]]
    (doseq [result results]
      (is (not (contains? result :result)) "Refusal must return no artifact or success")
      (let [diagnostic (:diagnostic result)]
        (is (= "C1-MANIFEST" (:id diagnostic)))
        (is (= field (:field diagnostic)))
        (is (= reason (:reason diagnostic)))
        (is (= {:source source-path} (:source-span diagnostic)))
        (is (= :meta (:profile diagnostic)))
        (is (= {:backend :jvm :triple "stage0"} (:target diagnostic)))
        (is (= :pipeline-manifest (:stage diagnostic)))
        (is (seq (:remediation diagnostic)))))
    (is (= (first results) (second results)) "Public and constructor diagnostics agree")
    results))

(deftest inconsistent-references-reject-before-artifact-emission
  (doseq [[fixture overrides field reason]
          [[:omitted-stage-reference {:stages (subvec stages 1)}
            :stages :stage-order-mismatch]
           [:reordered-stage-reference {:stages (vec (reverse stages))}
            :stages :stage-order-mismatch]
           [:foreign-stage-reference {:stages (assoc stages 0 :foreign-stage)}
            :stages :stage-order-mismatch]
           [:omitted-contract-reference {:pass-contracts (subvec references 1)}
            :pass-contracts :missing-reference]
           [:foreign-contract-reference {:pass-contracts (assoc references 0 :foreign-pass)}
            :pass-contracts :foreign-reference]
           [:duplicate-contract-reference {:pass-contracts (conj references (first references))}
            :pass-contracts :duplicate-reference]
           [:duplicate-stage-reference {:stages (conj stages (first stages))}
            :stages :stage-order-mismatch]
           [:duplicate-contract-replacing-reference
            {:pass-contracts (assoc references 1 (first references))}
            :pass-contracts :duplicate-reference]
           [:empty-stage-references {:stages []} :stages :stage-order-mismatch]
           [:empty-contract-references {:pass-contracts []}
            :pass-contracts :missing-reference]]]
    (testing (name fixture)
      (assert-reference-refusal {:pipeline-manifest-overrides overrides} field reason))))

(deftest malformed-secret-marker-references-are-not-disclosed
  (let [secret "PRIVATE-REFERENCE-PAYLOAD"
        marker {:secret secret}]
    (doseq [field [:stages :pass-contracts]
            value [secret marker #{secret} (list secret) [marker] [secret]]]
      (testing (str (name field) " malformed reference shape")
        (let [reason (if (vector? value)
                       (if (= :stages field) :stage-order-mismatch :foreign-reference)
                       :expected-vector)
              results (assert-reference-refusal
                       {:pipeline-manifest-overrides {field value}} field reason)]
          (doseq [result results]
            (is (not (str/includes? (pr-str result) secret)))
            (is (not (contains? (:diagnostic result) :actual-outcome)))))))))

(deftest references-cover-the-supplied-registry
  ;; Registry membership is broader than stage order; do not substitute defaults.
  (let [extra (assoc (first contracts) :pass :fixture-pass)
        passes {:contracts (conj contracts extra)
                :pipeline-manifest-overrides {:pass-contracts references}}]
    (assert-reference-refusal passes :pass-contracts :missing-reference)
    (is (= :complete
           (validate (assoc-in passes [:pipeline-manifest-overrides :pass-contracts]
                               (conj references :fixture-pass)))))))

(deftest earlier-refusals-keep-precedence
  (let [bad-references {:stages [:foreign-stage] :pass-contracts [:foreign-pass]}]
    (doseq [[fixture changes expected-id missing-fields]
            [[:stage-order {:stage-order (vec (reverse stages))} "C1-PIPELINE" nil]
             [:missing-stage-contract {:contracts (subvec contracts 1)}
              "C1-PASS-CONTRACT" [:contract]]
             [:missing-contract-field {:contract-overrides [{:pass :read-source :output nil}]}
              "C1-PASS-CONTRACT" [:output]]
             [:evidence-drop {:contract-overrides [{:pass :read-source
                                                    :invalidates [:source-spans]
                                                    :regenerates [] :emits []}]}
              "C1-EVIDENCE-DROP" [:source-spans]]
             [:unchecked-backend {:contract-overrides [{:pass :lower-target :input :raw-source}]}
              "C1-UNCHECKED-BACKEND" nil]
             [:missing-manifest-field
              {:pipeline-manifest (dissoc (merge default-pipeline bad-references) :compiler)}
              "C1-MANIFEST" [:compiler]]]]
      (testing (name fixture)
        (let [passes (merge {:pipeline-manifest-overrides bad-references} changes)]
          (doseq [result [(outcome #(validate passes)) (outcome #(construct passes))]]
            (is (not (contains? result :result)))
            (is (= expected-id (get-in result [:diagnostic :id])))
            (is (= missing-fields (get-in result [:diagnostic :missing-fields])))
            (is (nil? (get-in result [:diagnostic :field])))))))
    (testing "Stage references precede contract references"
      (assert-reference-refusal {:pipeline-manifest-overrides bad-references}
                                :stages :stage-order-mismatch))))
