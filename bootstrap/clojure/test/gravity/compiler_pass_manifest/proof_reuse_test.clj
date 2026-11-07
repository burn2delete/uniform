(ns gravity.compiler-pass-manifest.proof-reuse-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.compiler-pass-manifest :as manifest]))

(def source-path "compiler-passes.gravity")

(defn profile-manifest [overrides]
  {:profile :native :target :jvm
   :metadata {:compiler {:passes overrides}}})

(defn construct [profile]
  (manifest/compiler-pass-source-artifact-from-upstream
   source-path
   {:kind :gravity/stage0-math-conformance-artifact
    :profile-manifest profile}))

(defn validate [profile]
  (manifest/compiler-pass-validate-incremental!
   source-path profile (manifest/compiler-pass-suite profile)))

(defn outcome [f]
  (try
    {:value (f)}
    (catch clojure.lang.ExceptionInfo exception
      {:diagnostic (ex-data exception)
       :message (.getMessage exception)})))

(def fresh-record
  {:proof-id :proof/fixture :claim :bounds-preserved
   :inputs ["sha256:fixture-mir"] :profile :native :target :jvm
   :status :fresh :reuse :accepted
   :invalidation-conditions [:source-change :target-change]
   :provenance {:provider :fixture/provider :source "fixture.gravity"}
   :extension {:retained [1 2 3]}})

(deftest default-proof-reuse-unchanged
  (let [profile (profile-manifest {})
        records manifest/compiler-pass-default-proof-reuse-records
        artifact (construct profile)]
    (is (= :complete (validate profile)))
    (is (= records (:proof-reuse-records artifact)))
    (is (= profile (:profile-manifest artifact)))))

(deftest fresh-accepted-record-preserved
  (let [profile (profile-manifest {:proof-reuse-records [fresh-record]})
        artifact (construct profile)]
    (is (= :complete (validate profile)))
    (is (= [fresh-record] (:proof-reuse-records artifact)))
    (is (= profile (:profile-manifest artifact)))))

(deftest nonaccepted-record-unchanged
  (let [records [(assoc fresh-record :reuse :rejected :status :stale)
                 (dissoc (assoc fresh-record :reuse :speculative) :status)
                 (assoc fresh-record :reuse nil :status :unknown)]
        profile (profile-manifest {:proof-reuse-records records})]
    (is (= :complete (validate profile)))
    (is (= records (:proof-reuse-records (construct profile))))))

(def rejected-records
  [["accepted-stale-proof" (assoc fresh-record :status :stale)]
   ["accepted-missing-status" (dissoc fresh-record :status)]
   ["accepted-nil-status" (assoc fresh-record :status nil)]
   ["accepted-unknown-status" (assoc fresh-record :status :unknown)]
   ["accepted-secret-marker-status"
    (assoc fresh-record :status {:secret "proof-status-secret-marker"})]])

(deftest accepted-reuse-requires-explicit-freshness
  (doseq [[fixture record] rejected-records]
    (testing fixture
      (let [profile (profile-manifest {:proof-reuse-records [record]})
            direct (outcome #(validate profile))
            constructor (outcome #(construct profile))
            diagnostic (:diagnostic direct)]
        (is (= "C16-PROOF" (:id diagnostic)))
        (is (= :accepted-proof-requires-fresh-status (:reason diagnostic)))
        (is (= 0 (:record-index diagnostic)))
        (is (= {:source source-path} (:source-span diagnostic)))
        (is (= :native (:profile diagnostic)))
        (is (= :jvm (:target diagnostic)))
        (is (= direct
               (outcome #(validate (profile-manifest
                                    {:proof-reuse-records
                                     [(dissoc fresh-record :status)]})))))
        (is (= direct constructor))
        (is (not (contains? constructor :value)) "No artifact on refusal")
        (is (not (contains? diagnostic :status)))
        (is (not (str/includes? (pr-str direct) "proof-status-secret-marker")))))))

(deftest deterministic-first-refusal
  (let [first-invalid (dissoc fresh-record :status)
        profile (profile-manifest
                 {:proof-reuse-records
                  [fresh-record first-invalid (assoc fresh-record :status :stale)]})
        expected (outcome #(validate profile))]
    (is (= "C16-PROOF" (get-in expected [:diagnostic :id])))
    (is (= 1 (get-in expected [:diagnostic :record-index])))
    (is (= expected (outcome #(validate profile))))
    (is (= expected (outcome #(construct profile))))))

(deftest cache-refusals-precede-proof-refusal
  (doseq [[expected overrides]
          [["C16-KEY" {:cache-keys [{}] :cache-entries [{}]}]
           ["C16-ENTRY" {:cache-entries [{}]}]]]
    (testing expected
      (let [profile (profile-manifest
                     (assoc overrides :proof-reuse-records
                            [(dissoc fresh-record :status)]))
            direct (outcome #(validate profile))
            constructor (outcome #(construct profile))]
        (is (= expected (get-in direct [:diagnostic :id])))
        (is (= direct constructor))
        (is (not (contains? constructor :value)))))))
