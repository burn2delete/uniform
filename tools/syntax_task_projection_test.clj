(load-file "tools/syntax_task_projection.clj")
(require '[clojure.test :refer [deftest is run-tests]] '[clojure.string :as str])
(def tasks (slurp syntax-task-projection/task-path))
(def roadmap (slurp syntax-task-projection/roadmap-path))
(def first-id "syntax/current-evidence-and-remaining-work/syntax1-construct-canonical-syntax")
(def last-id "syntax/current-evidence-and-remaining-work/syntax5-integrate-consumers")
(def status "Open; partial source evidence above is not closure")
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))
(deftest compatible-projection
  (is (= 5 (count (syntax-task-projection/records tasks))))
  (is (syntax-task-projection/check-projection tasks roadmap))
  (is (= roadmap (syntax-task-projection/project tasks (syntax-task-projection/project tasks roadmap))))
  (is (str/includes? roadmap "Reader -> Syntax -> Expand")))
(deftest malformed-records
  (doseq [bad [(str/replace tasks first-id "syntax/unknown")
               (str/replace tasks last-id first-id)
               (str/replace-first tasks #"(?s)- \[ \] `syntax/[^`]+`.*?(?=\n\n- \[)" "")
               (str/replace-first tasks "Current evidence/status:" "Status:")
               (str/replace-first tasks "- [ ] `syntax/" "- [x] `syntax/")]]
    (is (rejects? #(syntax-task-projection/records bad)))))
(deftest stale-and-region-controls
  (is (rejects? #(syntax-task-projection/check-projection tasks (str/replace roadmap status "Accepted; not evidenced"))))
  (is (rejects? #(syntax-task-projection/check-projection (str/replace tasks status "Partial; new evidence") roadmap)))
  (doseq [bad [(str/replace roadmap "<!-- syntax-task-projection:end -->" "")
               (str roadmap "<!-- syntax-task-projection:start -->")]]
    (is (rejects? #(syntax-task-projection/project tasks bad)))))
(deftest context-independent-links-and-arrows
  (let [receipt "[receipt](https://github.com/burn2delete/uniform/commit/023ec47d86004fc829ae7dc5753b715ca4a653dc)"
        with-link (str/replace-first tasks status (str "Open; " receipt))
        projected (syntax-task-projection/project with-link roadmap)]
    (is (str/includes? projected receipt))
    (is (syntax-task-projection/check-projection with-link projected)))
  (doseq [bad ["[receipt](evidence.md#source-and-scope)" "[receipt](#source)"
               "[receipt](/evidence.md)" "[receipt](//example.com/evidence)"
               "[receipt](http://example.com/evidence)" "[receipt][source]" "[receipt][]"
               "![image](https://example.com/evidence)" "<a href=\"evidence.md\">receipt</a>"
               "<https://example.com/evidence>" "[receipt](https://example.com/evidence \"title\")"
               "[receipt](https://user@example.com/evidence)" "> hidden quote"]]
    (is (rejects? #(syntax-task-projection/project (str/replace-first tasks status bad) roadmap)) bad)))
(let [result (run-tests)] (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
