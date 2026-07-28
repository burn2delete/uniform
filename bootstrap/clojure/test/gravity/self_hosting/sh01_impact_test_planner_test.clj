(ns gravity.self-hosting.sh01-impact-test-planner-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh01-impact-test-planner :as planner]))

(deftest backlog-dependency-expansion-is-transitive
  (let [dependencies (planner/backlog-dependencies)
        affected (planner/downstream-closure dependencies #{"SH-06"})]
    (is (= #{"SH-05"} (get dependencies "SH-06")))
    (is (contains? affected "SH-06"))
    (is (contains? affected "SH-07"))
    (is (contains? affected "SH-29"))
    (is (not (contains? affected "SH-05")))))

(deftest backlog-dependency-table-fails-closed
  (let [dependencies (planner/backlog-dependencies)
        exception-data
        (fn [entries]
          (try
            (planner/validate-backlog-dependency-entries entries)
            nil
            (catch clojure.lang.ExceptionInfo exception
              (ex-data exception))))]
    (is (= "SH01-IMPACT-BACKLOG"
           (:id
            (exception-data
             (vec (dissoc dependencies "SH-29"))))))
    (is (= ["SH-99"]
           (:unknown-dependencies
            (exception-data
             (vec
              (assoc dependencies "SH-00" #{"SH-99"}))))))
    (is (= ["SH-00"]
           (:duplicate-slices
            (exception-data
             (conj (vec dependencies) ["SH-00" #{}])))))))

(deftest slice-plan-selects-only-owned-dedicated-tests
  (let [plan
        (planner/build-plan
         {:direct-slices #{"SH-01"}
          :expand-dependants? false})]
    (is (= :gravity/sh01-impact-test-plan-v1 (:schema plan)))
    (is (= ["SH-01"] (:affected-slices plan)))
    (is (some
         #{'gravity.self-hosting.sh01-ownership-test}
         (:namespaces plan)))
    (is (some
         #{'gravity.self-hosting.sh01-impact-test-planner-test}
         (:namespaces plan)))
    (is (every?
         #(= "SH-01" (:slice %))
         (:shards plan)))))

(deftest changed-leaf-test-expands-to-dependent-slices
  (let [plan
        (planner/build-plan
         {:changed-paths
          ["bootstrap/clojure/test/gravity/self_hosting/sh06_resolution_test.clj"]
          :expand-dependants? true})]
    (is (= ["SH-06"] (:direct-slices plan)))
    (is (contains? (set (:affected-slices plan)) "SH-07"))
    (is (contains? (set (:affected-slices plan)) "SH-29"))
    (is (some
         #{'gravity.self-hosting.sh06-resolution-adapter-test}
         (:namespaces plan)))
    (is (some
         #{'gravity.self-hosting.sh07-checked-core-test}
         (:namespaces plan)))))

(deftest coordinator-path-selects-the-conservative-full-plan
  (let [plan
        (planner/build-plan
         {:changed-paths ["bootstrap/clojure/src/gravity/bootstrap.clj"]
          :expand-dependants? true})]
    (is (= 30 (count (:direct-slices plan))))
    (is (= 30 (count (:affected-slices plan))))
    (is (= :coordinator
           (get-in plan [:classifications 0 :classification])))))

(deftest unrelated-paths-are-reported-but-do-not-select-tests
  (let [plan
        (planner/build-plan
         {:changed-paths ["README.md"]
          :expand-dependants? true})]
    (is (empty? (:affected-slices plan)))
    (is (empty? (:namespaces plan)))
    (is (= ["README.md"] (:ignored-paths plan)))))

(deftest changed-path-plans-are-canonical
  (let [first-path
        "bootstrap/clojure/test/gravity/self_hosting/sh06_resolution_test.clj"
        second-path
        "bootstrap/clojure/test/gravity/self_hosting/sh07_checked_core_test.clj"
        plan
        (planner/build-plan
         {:changed-paths [second-path first-path second-path]
          :expand-dependants? false})]
    (is (= [first-path second-path] (:changed-paths plan)))
    (is (= [first-path second-path]
           (mapv :path (:classifications plan))))
    (is (every? vector? (map :slices (:classifications plan))))
    (is (= [["SH-06"] ["SH-07"]]
           (mapv :slices (:classifications plan))))))

(deftest unowned-self-hosting-paths-fail-closed
  (let [exception
        (try
          (planner/build-plan
           {:changed-paths
            ["bootstrap/clojure/test/gravity/self_hosting/unowned_test.clj"]
            :expand-dependants? true})
          nil
          (catch clojure.lang.ExceptionInfo exception exception))]
    (is (= "SH01-IMPACT-UNOWNED" (:id (ex-data exception))))
    (is (= ["bootstrap/clojure/test/gravity/self_hosting/unowned_test.clj"]
           (:paths (ex-data exception))))))

(deftest out-of-range-dedicated-paths-fail-closed
  (doseq [relative
          ["bootstrap/clojure/test/gravity/self_hosting/sh99_unknown_test.clj"
           "bootstrap/clojure/fixtures/self-hosting/sh-99/accepted/unknown.gravity"]]
    (let [exception
          (try
            (planner/build-plan
             {:changed-paths [relative]
              :expand-dependants? true})
            nil
            (catch clojure.lang.ExceptionInfo exception exception))]
      (is (= "SH01-IMPACT-SLICE" (:id (ex-data exception))) relative)
      (is (= ["SH-99"] (:slices (ex-data exception))) relative))))

(deftest sh07-shards-carry-a-resource-class
  (let [plan
        (planner/build-plan
         {:direct-slices #{"SH-07"}
          :expand-dependants? false})]
    (testing "the planner exposes scheduling data without running tests"
      (is (seq (:shards plan)))
      (is (every?
           #(= :memory-heavy (:resource-class %))
           (:shards plan))))))

(deftest unknown-slices-fail-closed
  (let [exception
        (try
          (planner/build-plan {:direct-slices #{"SH-30"}})
          nil
          (catch clojure.lang.ExceptionInfo exception exception))]
    (is (= "SH01-IMPACT-SLICE" (:id (ex-data exception))))
    (is (= ["SH-30"] (:slices (ex-data exception))))))
