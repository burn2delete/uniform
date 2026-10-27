(load-file "tools/reader_task_projection.clj")
(require '[clojure.test :refer [deftest is run-tests]] '[clojure.string :as str])

(def tasks (slurp reader-task-projection/task-path))
(def roadmap (slurp reader-task-projection/roadmap-path))
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(defn replace-fixture [text match replacement]
  (let [changed (str/replace-first text match replacement)]
    ;; A missing fixture anchor must not masquerade as a projector rejection.
    (when (= text changed)
      (throw (AssertionError. "Fixture mutation did not change its input")))
    changed))

(defn with-status-evidence [text evidence]
  ;; Keep the real status/checkbox pairing; evidence prose may change freely.
  (replace-fixture text #"(?m)^Current evidence/status: .+$"
                   #(str % " " evidence)))

(deftest fixture-mutations-must-change-input
  (is (= "after" (replace-fixture "before" "before" "after")))
  (is (thrown? AssertionError (replace-fixture "before" "absent" "after")))
  (is (thrown? AssertionError (replace-fixture "before" "before" "before")))
  (is (thrown? AssertionError (with-status-evidence "no status field" "evidence"))))

(deftest deterministic-compatible-projection
  (is (= 7 (count (reader-task-projection/records tasks))))
  (is (reader-task-projection/check-projection tasks roadmap))
  (is (= roadmap (reader-task-projection/project tasks
                  (reader-task-projection/project tasks roadmap)))))

(deftest missing-duplicate-and-malformed-records
  (let [id "reader/finite-remaining-work/contract-coverage-reconciliation"]
    (is (rejects? #(reader-task-projection/records (replace-fixture tasks id "reader/absent"))))
    (is (rejects? #(reader-task-projection/records
                    (replace-fixture tasks "reader/finite-remaining-work/fresh-syntax-handoff" id))))
    (is (rejects? #(reader-task-projection/records
                    (replace-fixture tasks #"(?s)- \[[ x]\] `reader/finite-remaining-work/contract-coverage-reconciliation`.*?(?=\n\n- \[)" ""))))
    (is (rejects? #(reader-task-projection/records
                    (replace-fixture tasks "Current evidence/status:" "Status:"))))
    (is (rejects? #(reader-task-projection/records
                    (replace-fixture tasks #"- \[([ x])\] `reader/"
                      (fn [[_ checked]]
                        (str "- [" (if (= checked "x") " " "x") "] `reader/"))))))))

(deftest stale-projection-and-region-controls
  (let [changed (with-status-evidence tasks "Fixture evidence update.")
        projected (reader-task-projection/project changed roadmap)]
    (is (not= roadmap projected))
    (is (rejects? #(reader-task-projection/check-projection tasks projected)))
    (is (rejects? #(reader-task-projection/check-projection changed roadmap))))
  (doseq [text [(replace-fixture roadmap "<!-- reader-task-projection:end -->" "")
                (str roadmap "<!-- reader-task-projection:start -->")]]
    (is (rejects? #(reader-task-projection/project tasks text)))))

(deftest context-independent-receipt-links
  (let [receipt "[receipt](https://github.com/burn2delete/uniform/commit/c40d74aded3a019ff2bdeb1f4050cf95dbb11c1e)"
        with-link (with-status-evidence tasks (str "Fixture evidence: " receipt "."))
        projected (reader-task-projection/project with-link roadmap)]
    (is (str/includes? with-link receipt))
    (is (str/includes? projected receipt))
    (is (reader-task-projection/check-projection with-link projected)))
  (doseq [link ["[receipt](evidence.md#source-and-scope)"
                "[receipt](#source-and-scope)"
                "[receipt](/evidence.md)"
                "[receipt](//example.com/evidence)"
                "[receipt](http://example.com/evidence)"
                "[receipt][source]" "[receipt][]" "[receipt]"
                "![image](https://example.com/evidence)"
                "<a href=\"evidence.md\">receipt</a>"
                "<evidence.md>" "<https://example.com/evidence>"
                "[receipt](https://example.com/evidence \"title\")"
                "[receipt](https://user@example.com/evidence)"]]
    (is (rejects? #(reader-task-projection/project
                    (with-status-evidence tasks (str "Fixture evidence: " link ".")) roadmap)) link)))

(let [result (run-tests)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
