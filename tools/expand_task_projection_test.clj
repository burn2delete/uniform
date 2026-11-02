(load-file "tools/expand_task_projection.clj")
(require '[clojure.test :refer [deftest is run-tests]] '[clojure.string :as str])

(def tasks (slurp expand-task-projection/task-path))
(def roadmap (slurp expand-task-projection/roadmap-path))
(def status "Open; partial source evidence above is not closure")
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest deterministic-compatible-projection
  (is (= ["expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros" "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety" "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects" "expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination" "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"]
         (mapv :id (expand-task-projection/records tasks))))
  (is (expand-task-projection/check-projection tasks roadmap))
  (is (= roadmap (expand-task-projection/project tasks
                  (expand-task-projection/project tasks roadmap))))
  (let [with-history (str tasks "\n- [x] T999 Historical record only.\n")]
    (is (= roadmap (expand-task-projection/project with-history roadmap))))
  (let [changed (str/replace-first tasks status "Accepted; owner receipt pending semantic review")
        checked (str/replace-first changed "- [ ] `expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros`" "- [x] `expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros`")]
    (is (rejects? #(expand-task-projection/records changed)))
    (is (= 5 (count (expand-task-projection/records checked))))))

(deftest missing-duplicate-and-malformed-records
  (doseq [bad [(str/replace tasks "`expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros`" "`expand/current-evidence-and-remaining-work/expand6-unknown`")
               (str/replace tasks "`expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety`" "`expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros`")
               (str/replace-first tasks #"(?s)- \[ \] `expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros`.*?(?=\n\n- \[)" "")
               (str/replace-first tasks "Status:" "State:")
               (str/replace-first tasks "- [ ] `expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros`" "- [x] `expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros`")
               (str/replace-first tasks "- [ ] `expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros`" "- [ ] `expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety`")
               (str/replace-first tasks "expand/1: Execute" "expand/2: Execute")
               (str/replace-first tasks "Execute syntax macros" "Execute | syntax macros")]]
    (is (rejects? #(expand-task-projection/records bad)))))

(deftest stale-projection-and-region-controls
  (is (rejects? #(expand-task-projection/check-projection tasks
                  (str/replace roadmap status "Accepted; source evidence"))))
  (is (rejects? #(expand-task-projection/check-projection
                  (str/replace-first tasks status "Partial; fresh evidence") roadmap)))
  (doseq [marker ["expand-task-projection"]
          text [(str/replace roadmap (str "<!-- " marker ":end -->") "")
                (str roadmap "<!-- " marker ":start -->")
                (str "<!-- " marker ":end -->\n<!-- " marker ":start -->")]]
    (is (rejects? #(expand-task-projection/project tasks text))))
  (doseq [text [(str/replace tasks "<!-- expand-implementation-tasks:end -->" "")
                (str tasks "<!-- expand-implementation-tasks:start -->")
                "<!-- expand-implementation-tasks:end -->\n<!-- expand-implementation-tasks:start -->"]]
    (is (rejects? #(expand-task-projection/records text))))
  (let [changed (str/replace-first tasks status "Partial; fresh evidence")
        projected (expand-task-projection/project changed roadmap)
        old-region (expand-task-projection/region roadmap "expand-task-projection")
        new-region (expand-task-projection/region projected "expand-task-projection")]
    (is (= (:prefix old-region) (:prefix new-region)))
    (is (= (:suffix old-region) (:suffix new-region)))
    (is (expand-task-projection/check-projection changed projected))))

(deftest context-independent-receipt-links
  (let [receipt "[receipt](https://github.com/burn2delete/uniform/commit/023ec47d86004fc829ae7dc5753b715ca4a653dc)"
        with-link (str/replace-first tasks status (str "Partial; " receipt))
        projected (expand-task-projection/project with-link roadmap)]
    (is (str/includes? projected receipt))
    (is (expand-task-projection/check-projection with-link projected)))
  (doseq [link ["[receipt](evidence.md#source-and-scope)" "[receipt](#source-and-scope)"
                "[receipt](/evidence.md)" "[receipt](//example.com/evidence)"
                "[receipt](http://example.com/evidence)" "[receipt][source]" "[receipt][]"
                "[receipt]" "![image](https://example.com/evidence)"
                "<a href=\"evidence.md\">receipt</a>" "<evidence.md>"
                "<https://example.com/evidence>" "[receipt](https://example.com/evidence \"title\")"
                "[receipt](https://user@example.com/evidence)"]]
    (is (rejects? #(expand-task-projection/project
                    (str/replace-first tasks status (str "Partial; " link)) roadmap)) link)))

(let [result (run-tests)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
