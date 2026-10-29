(ns task-baseline-test
  (:require [clojure.test :refer [deftest is run-tests]]))
(binding [*command-line-args* nil] (load-file "tools/task_baseline.clj"))
(require '[task-baseline :as baseline] '[sh-crosswalk])
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))
(deftest scoped-status-and-false-acceptance
  (is (= :accepted (:state (baseline/status true "Status: Accepted; exact scoped receipt"))))
  (is (= :unassessed (:state (baseline/status false "Status: Extension records/projected views exist"))))
  (is (rejects? #(baseline/status false "Status: Accepted; invented")))
  (is (rejects? #(baseline/status true "Status: Open"))))
(deftest canonical-identity-and-unweighted-counting
  (let [tasks [{:id "a" :status {:state :accepted}} {:id "b" :status {:state :open}}
               {:id "c" :status {:state :partial}} {:id "d" :status {:state :unassessed}}]
        p (baseline/progress tasks)]
    (is (= [1 4 3] (mapv p [:accepted-complete-in-declared-task-scope :total :remaining])))
    (is (= {:numerator 1 :denominator 4} (:unweighted-ratio p)))
    (is (rejects? #(baseline/progress (conj tasks (first tasks)))))))
(deftest stale-source-refuses-before-counting
  (with-redefs [clojure.core/slurp (constantly "changed")
                sh-crosswalk/git-source (fn [_ _] "pinned")]
    (is (rejects? #(baseline/pinned-text "source" "task.md")))))
(let [r (run-tests)] (shutdown-agents) (when (pos? (+ (:fail r) (:error r))) (System/exit 1)))
