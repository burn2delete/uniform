(ns gravity.package.pipeline-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.build.package-policy :as policy]
            [gravity.build.package-policy-test :as policy-test]
            [gravity.build.package-selection :as selection]
            [gravity.build.package-selection-test :as selection-test]
            [gravity.digest :as digest]
            [gravity.package :as package]))

(deftest package-owned-api-retains-both-build-compatibility-entry-points
  (let [request selection-test/request]
    (doseq [input [request (assoc request :allowed-capabilities #{})
                   (assoc-in request [:context :visibility] :private)
                   (assoc-in request [:candidates 0 :yank-status] :yanked)]]
      (is (= (selection/select-exact input) (package/select-exact input))))
    (doseq [input [policy-test/request (dissoc policy-test/request :policy)
                   (assoc-in policy-test/request [:record :revocation-status] :revoked)]]
      (is (= (policy/decide input) (package/decide-policy input))))
    (is (= 1 (count (first (:arglists (meta #'policy/decide))))))
    (is (= 1 (count (first (:arglists (meta #'selection/select-exact))))))
    (is (= #{'decide-policy 'select-exact} (set (keys (ns-publics 'gravity.package)))))
    (is (= #{'clojure.set 'clojure.walk} (set (map ns-name (vals (ns-aliases 'gravity.package))))))
    (let [contract (:gravity/namespace-contract (meta #'package/select-exact))]
      (is (= contract (:gravity/namespace-contract (meta #'package/decide-policy))))
      (is (= :supplied-package-policy-and-direct-exact-selection (:contract-boundary contract)))
      (is (= :non-admitting (:authority contract)))
      (is (some #{:signature-verification} (:unsupported contract)))
      (is (some #{:transitive-resolution} (:unsupported contract)))
      (is (= :before-metadata-reporting (get-in contract [:diagnostic-ownership :private-registry-denial]))))))

(deftest package-selection-keeps-late-policy-interposition
  (let [calls (atom [])
        denied {:artifact :gravity/package-policy-decision :policy-decision :reject
                :diagnostics [{:diagnostic "PKG5006" :policy-rule :supplied-refusal}]}
        callback (fn [value] (swap! calls conj value) denied)]
    (with-redefs [policy/decide callback]
      (let [result (selection/select-exact selection-test/request)]
        (is (= :rejected (:status result)))
        (is (= [] (:selections result)))
        (is (= (:diagnostics denied) (:diagnostics result)))))
    (with-redefs [package/decide-policy callback]
      (is (= :rejected (:status (package/select-exact selection-test/request)))))
    (is (= :rejected (:status (package/select-exact callback selection-test/request))))
    (is (= 3 (count @calls)))
    (is (apply = @calls))
    (is (= selection-test/package-record (:record (first @calls))))
    (is (= [] (:locked-records (first @calls))))))

(deftest real-file-provenance-and-supplied-status-survive-selection-and-rejection
  (let [relative "bootstrap/gravity/src/gravity/compiler/c15_compiler_diagnostics.gravity"
        path (.getCanonicalPath (io/file relative)) text (slurp path :encoding "UTF-8")
        hash (str "sha256:" (digest/sha256-hex text))
        ;; These are unsigned development metadata with an unbound variant,
        ;; not published package records or a compiled JVM artifact.
        record (assoc selection-test/package-record :package "development/c15-source"
                       :content-hash hash :provenance (str "development-file:" relative)
                       :signature-status :unverified :provenance-status :unverified
                       :variants [{:profile :hosted :target :jvm-21
                                   :artifact "fixture:unbound-c15-package-variant"}])
        request (assoc selection-test/request :candidates [record] :locked-records [record record]
                        :constraints [(assoc selection-test/constraint-record :package (:package record))])
        before (pr-str request) result (package/select-exact request)
        rejected (package/select-exact (assoc request :allowed-capabilities #{}))]
    (is (= :selected (:status result)))
    (is (= record (get-in result [:selections 0 :selected])))
    (is (= [record record] (:locked-records result) (:locked-records rejected)))
    (is (= before (pr-str request)))
    (is (= result (edn/read-string (pr-str result))))
    (is (= hash (get-in result [:selections 0 :selected :content-hash])))
    (is (= :unverified (get-in result [:selections 0 :selected :signature-status])))
    (is (= :non-admitting (:authority result) (:authority rejected)))
    (is (= :not-evaluated (:admission result) (:admission rejected)))
    (is (= "PKG5002" (get-in rejected [:diagnostics 0 :diagnostic])))
    (is (= [] (:selections rejected)))
    (is (= #{} (:granted-capabilities result) (:granted-capabilities rejected)))))
