(ns gravity.self-hosting.sh07-authoritative-coverage-census-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh07-authoritative-runner :as runner]))

(defn- fixture
  []
  (let [source-revision
        "sha256:0000000000000000000000000000000000000000000000000000000000000000"
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
    "c7-types" artifact request core source-binding source-binding
    :source-bound-derived)))

(defn- matching-contract
  [value]
  {:schema-version 2
   :coverage-census-policy :source-bound-derived
   :authority-claims
   {:counts-precommitted? false
    :independent-count-oracle? false
    :unsupported-claims [:exact-authentic-coverage :aggregate :release]
    :aggregate-authoritative? false
    :release-authoritative? false
    :attestation-required true
    :attestation-schema :gravity/sh07-source-bound-attestation-v1}
   :authoritative-coverage-census
   {:schema-version 2
    :policy :source-bound-derived
    :counts-precommitted? false
    :independent-count-oracle? false
    :unsupported-claims [:exact-authentic-coverage :aggregate :release]
   :module-expectations
    {:c7-types
     {:module-namespace (:module-namespace value)
      :source-binding (:source-binding value)}}}})

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
    (is (= :individual-source-bound-derived
           (:authority-scope value)))
    (is (false? (:aggregate-authoritative? value)))
    (is (false? (:counts-precommitted? value)))
    (is (false? (:independent-count-oracle? value)))
    (is (= [:exact-authentic-coverage :aggregate :release]
           (:unsupported-claims value)))
    (is (runner/authoritative-coverage-census-valid?
         (matching-contract value) "c7-types" value))))

(deftest proof-contract-binds-current-source-bound-modules
  (let [contract
        (edn/read-string
         (slurp
          (io/resource "gravity/self_hosting/sh07_proof_contract.edn")))
        c7-expectation
        (get-in contract
                [:authoritative-coverage-census
                 :module-expectations :c7-types])
        c8-expectation
        (get-in contract
                [:authoritative-coverage-census
                 :module-expectations :c8-effects])
        source-contracts (runner/module-source-contracts)]
    (is (= :source-bound-derived
           (:coverage-census-policy contract)))
    (is (= {:source-byte-count 210220
            :source-bytes-sha256
            "sha256:78a100be4fff12d3f4225e1eb4ef305188ee7227c7c087c3ef35d154fe88dab4"}
           (:source-binding c7-expectation)))
    (is (not (contains? c7-expectation :request-counts)))
    (is (not (contains? c7-expectation :core-counts)))
    (is (= {:source-path
            "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity"
            :source-binding (:source-binding c7-expectation)}
           (get source-contracts "c7-types")))
    (is (= {:source-byte-count 80761
            :source-bytes-sha256
            "sha256:ff072574ed4bd6feaa8714e2f221b64d633fe2cd601d55de2b0df1eff4983a70"}
           (:source-binding c8-expectation)))
    (is (not (contains? c8-expectation :request-counts)))
    (is (not (contains? c8-expectation :core-counts)))
    (is (= {:source-path
            "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity"
            :source-binding (:source-binding c8-expectation)}
           (get source-contracts "c8-effects")))))

(deftest source-contract-mismatch-stops-before-authoritative-proof
  (let [source-reader
        (or (ns-resolve 'gravity.self-hosting.sh07-authoritative-runner
                        'source-bytes-sha256)
            (throw (ex-info "source reader seam is absent" {})))
        proof-var
        (or (ns-resolve 'gravity.bootstrap 'sh07-core-file-proof-transaction)
            (throw (ex-info "proof transaction seam is absent" {})))
        proof-called? (atom false)]
    (doseq [selection ["c7-types" "all"]]
      (reset! proof-called? false)
      (let [failure
            (try
              (with-redefs-fn
                {source-reader
                 (fn [_]
                   {:byte-count 1
                    :sha256 (str "sha256:" (apply str (repeat 64 "0")))})
                 proof-var
                 (fn [_]
                   (reset! proof-called? true)
                   (throw (ex-info "proof must not run" {})))}
                #(runner/run-authoritative selection))
              nil
              (catch clojure.lang.ExceptionInfo error error))]
        (is (= (if (= selection "all")
                 "SH07-COVERAGE-CENSUS-MODULE-MISSING"
                 "SH07-AUTHORITATIVE-SOURCE-MISMATCH")
               (:id (ex-data failure))))
        (is (false? @proof-called?))))))

(deftest exact-precommitted-mode-remains-strict-and-explicit
  (let [value (runner/authoritative-coverage-census
               "c7-types" (:artifact (fixture)) (:request (fixture))
               (:core (fixture)) (:source-binding (fixture))
               (:source-binding (fixture)) :exact-precommitted)
        contract
        {:schema-version 2
         :coverage-census-policy :exact-precommitted
         :authority-claims {:counts-precommitted? true
                            :independent-count-oracle? true
                            :unsupported-claims []
                            :aggregate-authoritative? false
                            :release-authoritative? false}
         :authoritative-coverage-census
         {:schema-version 2 :policy :exact-precommitted
          :counts-precommitted? true :independent-count-oracle? true
          :module-expectations
          {:c7-types {:module-namespace (:module-namespace value)
                      :source-binding (:source-binding value)
                      :request-counts (:request-counts value)
                      :core-counts (:core-counts value)}}}}]
    (is (runner/authoritative-coverage-census-valid?
         contract "c7-types" value))
    (is (= ::failed
           (try
             (runner/validate-coverage-census-contract!
              (assoc-in contract
                        [:authoritative-coverage-census
                         :module-expectations :c7-types]
                        (dissoc (get-in contract
                                        [:authoritative-coverage-census
                                         :module-expectations :c7-types])
                                :request-counts)))
             ::unexpected
             (catch clojure.lang.ExceptionInfo _ ::failed))))
    (is (= ::failed
           (try
             (runner/validate-coverage-census-contract!
              (assoc contract :coverage-census-policy :unknown))
             ::unexpected
             (catch clojure.lang.ExceptionInfo _ ::failed))))))

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
    (testing "a coherent census cannot substitute different source bytes"
      (is (not
           (runner/authoritative-coverage-census-valid?
            (assoc-in contract
                      [:authoritative-coverage-census :module-expectations
                       :c7-types :source-binding :source-byte-count]
                      43)
            "c7-types" valid))))
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
