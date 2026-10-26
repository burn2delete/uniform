(load-file "tools/reader_task_projection.clj")
(require '[clojure.test :refer [deftest is run-tests]] '[clojure.string :as str])

(def tasks (slurp reader-task-projection/task-path))
(def roadmap (slurp reader-task-projection/roadmap-path))
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest deterministic-compatible-projection
  (is (= 7 (count (reader-task-projection/records tasks))))
  (is (reader-task-projection/check-projection tasks roadmap))
  (is (= roadmap (reader-task-projection/project tasks
                  (reader-task-projection/project tasks roadmap)))))

(deftest missing-duplicate-and-malformed-records
  (let [id "reader/finite-remaining-work/contract-coverage-reconciliation"]
    (is (rejects? #(reader-task-projection/records (str/replace tasks id "reader/absent"))))
    (is (rejects? #(reader-task-projection/records
                    (str/replace tasks "reader/finite-remaining-work/fresh-syntax-handoff" id))))
    (is (rejects? #(reader-task-projection/records
                    (str/replace-first tasks #"(?s)- \[ \] `reader/finite-remaining-work/contract-coverage-reconciliation`.*?(?=\n\n- \[)" ""))))
    (is (rejects? #(reader-task-projection/records
                    (str/replace-first tasks "Current evidence/status:" "Status:"))))
    (is (rejects? #(reader-task-projection/records
                    (str/replace-first tasks "- [ ] `reader/" "- [x] `reader/"))))))

(deftest stale-projection-and-region-controls
  (is (rejects? #(reader-task-projection/check-projection tasks
                  (str/replace roadmap "| Open; source leaves" "| Accepted; source leaves"))))
  (is (rejects? #(reader-task-projection/check-projection
                  (str/replace tasks "Partial Reader and projection sources." "Partial; new evidence.") roadmap)))
  (doseq [text [(str/replace roadmap "<!-- reader-task-projection:end -->" "")
                (str roadmap "<!-- reader-task-projection:start -->")]]
    (is (rejects? #(reader-task-projection/project tasks text)))))

(deftest context-independent-receipt-links
  (let [receipt "[receipt](https://github.com/burn2delete/uniform/commit/c40d74aded3a019ff2bdeb1f4050cf95dbb11c1e)"
        with-link (str/replace tasks "Partial Reader and projection sources."
                              (str "Partial; " receipt "."))
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
                    (str/replace tasks "Partial Reader and projection sources."
                                 (str "Partial; " link ".")) roadmap)) link)))

(let [result (run-tests)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
