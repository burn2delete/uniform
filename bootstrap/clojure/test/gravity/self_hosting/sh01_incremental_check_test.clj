(ns gravity.self-hosting.sh01-incremental-check-test
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh01-impact-test-planner :as planner]
            [gravity.self-hosting.sh01-incremental-check :as check]
            [gravity.self-hosting.sh01-parallel-test-runner :as runner]))

(def ^:private selected-plan
  {:schema :gravity/sh01-impact-test-plan-v1
   :changed-paths ["bootstrap/clojure/test/gravity/self_hosting/sh06_resolution_test.clj"]
   :classifications
   [{:path "bootstrap/clojure/test/gravity/self_hosting/sh06_resolution_test.clj"
     :classification :dedicated
     :slices ["SH-06"]}]
   :direct-slices ["SH-06"]
   :affected-slices ["SH-06" "SH-07"]
   :namespaces ['gravity.self-hosting.sh06-resolution-test
                'gravity.self-hosting.sh07-checked-core-test]
   :shards
   [{:namespace 'gravity.self-hosting.sh06-resolution-test
     :slice "SH-06"
     :resource-class :normal}
    {:namespace 'gravity.self-hosting.sh07-checked-core-test
     :slice "SH-07"
     :resource-class :memory-heavy}]
   :ignored-paths []})

(deftest changed-paths-use-the-existing-plan-and-run-only-selected-work
  (let [request (atom nil)
        executed (atom nil)]
    (with-redefs
      [planner/changed-paths
       (constantly (:changed-paths selected-plan))
       planner/build-plan
       (fn [value]
         (reset! request value)
         selected-plan)
       runner/execute-plan
       (fn [plan options]
         (reset! executed {:plan plan :options options})
         {:authority :non-authoritative
          :authoritative? false
          :status :passed
          :ok? true
          :exit-code 0})]
      (let [report (check/run-check)]
        (is (= {:changed-paths (:changed-paths selected-plan)
                :expand-dependants? true}
               @request))
        (is (= (:namespaces selected-plan)
               (get-in @executed [:plan :namespaces])))
        (is (= :non-authoritative (get-in @executed [:plan :authority])))
        (is (false? (get-in @executed [:plan :authoritative?])))
        (is (= {:development-loop? true} (:options @executed)))
        (is (= ["SH-07"]
               (get-in report
                       [:invalidation-explanation :dependant-slices])))
        (is (= (:namespaces selected-plan)
               (get-in report
                       [:invalidation-explanation :selected-namespaces])))
        (is (= :non-authoritative (:authority report)))
        (is (false? (:authoritative? report)))))))

(deftest unrelated-changes-emit-a-no-work-explanation
  (let [executed? (atom false)
        plan (assoc selected-plan
                    :changed-paths ["README.md"]
                    :classifications
                    [{:path "README.md"
                      :classification :unrelated
                      :slices []}]
                    :direct-slices []
                    :affected-slices []
                    :namespaces []
                    :shards []
                    :ignored-paths ["README.md"])]
    (with-redefs [planner/build-plan (constantly plan)
                  runner/execute-plan
                  (fn [_]
                    (reset! executed? true)
                    nil)]
      (let [report (check/run-check ["README.md"])]
        (is (false? @executed?))
        (is (= :no-selected-work
               (get-in report [:execution :reason])))
        (is (= :ignored
               (get-in report
                       [:invalidation-explanation
                        :path-invalidations 0 :effect])))
        (is (= 0 (:exit-code report)))))))

(deftest unowned-relevant-paths-fail-closed-before-execution
  (let [executed? (atom false)
        exception
        (with-redefs [runner/execute-plan
                      (fn [_]
                        (reset! executed? true)
                        nil)]
          (try
            (check/run-check
             ["bootstrap/clojure/test/gravity/self_hosting/unowned_test.clj"])
            nil
            (catch clojure.lang.ExceptionInfo exception
              exception)))]
    (is (= "SH01-IMPACT-UNOWNED" (:id (ex-data exception))))
    (is (= ["bootstrap/clojure/test/gravity/self_hosting/unowned_test.clj"]
           (:paths (ex-data exception))))
    (is (false? @executed?))))

(deftest incremental-alias-does-not-redefine-the-full-test-gate
  (let [aliases (:aliases (edn/read-string (slurp "deps.edn")))]
    (is (= ["-m" "gravity.self-hosting-test-runner"]
           (get-in aliases [:test :main-opts])))
    (is (= ["-m" "gravity.self-hosting.sh01-incremental-check"]
           (get-in aliases [:incremental-check :main-opts])))))

(deftest base-aware-run-records-discovery-and-forwards-its-union
  (let [discovery
        {:schema :gravity/sh01-change-discovery-v1
         :authority :non-authoritative
         :authoritative? false
         :base-ref "main"
         :base-commit "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
         :merge-base "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
         :committed-paths ["committed.clj"]
         :tracked-working-paths ["tracked.clj"]
         :untracked-paths ["untracked.clj"]
         :changed-paths ["committed.clj" "tracked.clj" "untracked.clj"]}
        planned (atom nil)]
    (with-redefs [planner/change-discovery (constantly discovery)
                  check/build-check-plan
                  (fn [paths metadata]
                    (reset! planned [paths metadata])
                    (assoc selected-plan :change-discovery metadata))
                  check/execute-check identity]
      (let [plan (check/run-check-from-base "main")]
        (is (= [(:changed-paths discovery) discovery] @planned))
        (is (= discovery (:change-discovery plan)))
        (is (= discovery
               (:change-discovery (check/invalidation-explanation plan))))))))

(deftest incremental-cli-accepts-only-one-explicit-base-ref
  (let [parse (ns-resolve 'gravity.self-hosting.sh01-incremental-check
                          'parse-arguments)]
    (is (= {:base-ref nil} (parse [])))
    (is (= {:base-ref "main"} (parse ["--base-ref" "main"])))
    (doseq [arguments [["main"] ["--base-ref"]
                       ["--base-ref" ""] ["--base-ref" "main" "extra"]]]
      (let [exception
            (try
              (parse arguments)
              nil
              (catch clojure.lang.ExceptionInfo exception exception))]
        (is (= "SH01-INCREMENTAL-USAGE" (:id (ex-data exception)))
            arguments)))))
