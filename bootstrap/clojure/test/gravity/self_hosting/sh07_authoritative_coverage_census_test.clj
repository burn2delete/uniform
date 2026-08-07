(ns gravity.self-hosting.sh07-authoritative-coverage-census-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh07-authoritative-runner :as runner]))

(def ^:private measured-c7-request-counts
  {:fragment-count 142 :root-form-count 142 :form-count 12759
   :binding-count 1114 :local-binding-count 852 :resolution-count 5221})

(def ^:private measured-c7-core-counts
  {:core-node-count 10405 :definition-count 142 :call-count 2069
   :reference-count 4117 :keyword-lookup-count 0
   :core-form-frequencies
   {:let 98 :fn 137 :call 2069 :if 566 :recur 85 :loop 73
    :reference 4117 :quote 3 :collection-literal 263 :literal 2852
    :def 142}})

(defn- fixture
  []
  (let [source-revision "sha256:fixture-source"
        request
        {:schema-version 15
         :scope :sh07-b15-keyword-map-lookup
         :lineage {:source-revision-id source-revision}
         :module {:namespace 'gravity.compiler.c7-type-checker-engine
                  :source-revision-id source-revision}
         :top-level-form-ids [:root-1 :root-2]
         :forms [{:form-id :form-1} {:form-id :form-2} {:form-id :form-3}]
         :binding-table
         [{:namespace 'gravity.compiler.c7-type-checker-engine}
          {:namespace 'gravity.compiler.c7-type-checker-engine}
          {:namespace 'gravity.other}]
         :resolution-table [{:id 1} {:id 2}]
         :fragment-manifest
         [{:root-form-ids [:root-1] :form-ids [:form-1 :form-2]}
          {:root-form-ids [:root-2] :form-ids [:form-3]}]}
        core
        {:fragment-coverage
         {:covered-root-form-ids [:root-1 :root-2]
          :covered-form-ids [:form-1 :form-2 :form-3]}
         :nodes [{:core-form :literal} {:core-form :reference}
                 {:core-form :literal}]
         :definitions [{}]
         :calls [{} {}]
         :reference-uses [{}]
         :keyword-lookups []}
        artifact
        {:artifact-id "sha256:fixture-artifact"
         :task "SH-07-B45"
         :status :accepted
         :sh06-resolution-artifact {:status :accepted}
         :gravity-core-boundary {:target-source-reread? false}}
        source-binding {:byte-count 42 :sha256 source-revision}]
    {:artifact artifact :request request :core core
     :source-binding source-binding}))

(defn- census
  ([] (census (fixture)))
  ([{:keys [artifact request core source-binding]}]
   (runner/authoritative-coverage-census
    "c7-types" artifact request core source-binding source-binding)))

(defn- matching-contract
  [value]
  {:authoritative-coverage-census
   {:schema-version 1
    :module-expectations
    {:c7-types
     {:module-namespace (:module-namespace value)
      :request-counts (:request-counts value)
      :core-counts (:core-counts value)}}}})

(deftest compact-census-projects-existing-request-and-core-exactly
  (let [value (census)]
    (is (= {:fragment-count 2 :root-form-count 2 :form-count 3
            :binding-count 3 :local-binding-count 2 :resolution-count 2}
           (:request-counts value)))
    (is (= {:core-node-count 3 :definition-count 1 :call-count 2
            :reference-count 1 :keyword-lookup-count 0
            :core-form-frequencies {:literal 2 :reference 1}}
           (:core-counts value)))
    (is (every? true? (vals (:integrity value))))
    (is (= :individual-existing-runner-output-only
           (:authority-scope value)))
    (is (false? (:aggregate-authoritative? value)))
    (is (runner/authoritative-coverage-census-valid?
         (matching-contract value) "c7-types" value))))

(deftest proof-contract-binds-the-measured-c7-census
  (let [contract
        (edn/read-string
         (slurp
          (io/resource "gravity/self_hosting/sh07_proof_contract.edn")))
        expectation
        (get-in contract
                [:authoritative-coverage-census
                 :module-expectations :c7-types])]
    (is (= measured-c7-request-counts (:request-counts expectation)))
    (is (= measured-c7-core-counts (:core-counts expectation)))))

(deftest census-fails-closed-on-order-source-or-hash-drift
  (let [{:keys [artifact request core source-binding] :as values} (fixture)
        wrong-order
        (census (assoc values :core
                       (assoc-in core
                                 [:fragment-coverage :covered-form-ids]
                                 [:form-2 :form-1 :form-3])))
        changed-source
        (runner/authoritative-coverage-census
         "c7-types" artifact request core source-binding
         (assoc source-binding :byte-count 43))
        valid (census)
        contract (matching-contract valid)]
    (testing "ordering and source mutation are explicit failed integrity facts"
      (is (false? (get-in wrong-order [:integrity :form-id-order-exact?])))
      (is (false? (get-in changed-source
                          [:integrity :source-snapshot-stable?])))
      (is (not (runner/authoritative-coverage-census-valid?
                contract "c7-types" wrong-order)))
      (is (not (runner/authoritative-coverage-census-valid?
                contract "c7-types" changed-source))))
    (testing "the compact hash and exact C7 expectations are binding"
      (is (not (runner/authoritative-coverage-census-valid?
                contract "c7-types"
                (assoc valid :census-hash "sha256:altered"))))
      (is (not (runner/authoritative-coverage-census-valid?
                (assoc-in contract
                          [:authoritative-coverage-census
                           :module-expectations :c7-types
                           :request-counts :form-count]
                          4)
                "c7-types" valid))))))
