(load-file "tools/types_task_projection.clj")
(require '[clojure.test :refer [deftest is run-tests]] '[clojure.string :as str])

(def tasks (slurp types-task-projection/task-path))
(def roadmap (slurp types-task-projection/roadmap-path))
(def status "Open; partial source evidence above is not closure")
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest deterministic-compatible-projection
  (is (= ["types/current-evidence-and-remaining-work/types1-solve-core-constraints" "types/current-evidence-and-remaining-work/types2-check-declared-abstractions" "types/current-evidence-and-remaining-work/types3-make-dynamic-boundaries-explicit" "types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts" "types/current-evidence-and-remaining-work/types5-integrate-typed-core"]
         (mapv :id (types-task-projection/records tasks))))
  (is (types-task-projection/check-projection tasks roadmap))
  (is (= roadmap (types-task-projection/project tasks
                  (types-task-projection/project tasks roadmap))))
  (let [with-history (str tasks "\n- [x] T999 Historical record only.\n")]
    (is (= roadmap (types-task-projection/project with-history roadmap))))
  (let [changed (str/replace-first tasks status "Accepted; owner receipt pending semantic review")
        checked (str/replace-first changed "- [ ] `types/current-evidence-and-remaining-work/types1-solve-core-constraints`" "- [x] `types/current-evidence-and-remaining-work/types1-solve-core-constraints`")]
    (is (rejects? #(types-task-projection/records changed)))
    (is (= 5 (count (types-task-projection/records checked))))))

(deftest missing-duplicate-and-malformed-records
  (doseq [bad [(str/replace tasks "`types/current-evidence-and-remaining-work/types1-solve-core-constraints`" "`types/current-evidence-and-remaining-work/types6-unknown`")
               (str/replace tasks "`types/current-evidence-and-remaining-work/types2-check-declared-abstractions`" "`types/current-evidence-and-remaining-work/types1-solve-core-constraints`")
               (str/replace-first tasks #"(?s)- \[ \] `types/current-evidence-and-remaining-work/types1-solve-core-constraints`.*?(?=\n\n- \[)" "")
               (str/replace-first tasks "Current evidence/status:" "State:")
               (str/replace-first tasks "- [ ] `types/current-evidence-and-remaining-work/types1-solve-core-constraints`" "- [x] `types/current-evidence-and-remaining-work/types1-solve-core-constraints`")
               (str/replace-first tasks "- [ ] `types/current-evidence-and-remaining-work/types1-solve-core-constraints`" "- [ ] `types/current-evidence-and-remaining-work/types2-check-declared-abstractions`")
               (str/replace-first tasks "types/1: Solve" "types/2: Solve")
               (str/replace-first tasks "Solve core constraints" "Solve | core constraints")]]
    (is (rejects? #(types-task-projection/records bad)))))

(deftest stale-projection-and-region-controls
  (is (rejects? #(types-task-projection/check-projection tasks
                  (str/replace roadmap status "Accepted; source evidence"))))
  (is (rejects? #(types-task-projection/check-projection
                  (str/replace-first tasks status "Partial; fresh evidence") roadmap)))
  (doseq [marker ["types-task-projection"]
          text [(str/replace roadmap (str "<!-- " marker ":end -->") "")
                (str roadmap "<!-- " marker ":start -->")
                (str "<!-- " marker ":end -->\n<!-- " marker ":start -->")]]
    (is (rejects? #(types-task-projection/project tasks text))))
  (doseq [text [(str/replace tasks "<!-- types-implementation-tasks:end -->" "")
                (str tasks "<!-- types-implementation-tasks:start -->")
                "<!-- types-implementation-tasks:end -->\n<!-- types-implementation-tasks:start -->"]]
    (is (rejects? #(types-task-projection/records text))))
  (let [changed (str/replace-first tasks status "Partial; fresh evidence")
        projected (types-task-projection/project changed roadmap)
        old-region (types-task-projection/region roadmap "types-task-projection")
        new-region (types-task-projection/region projected "types-task-projection")]
    (is (= (:prefix old-region) (:prefix new-region)))
    (is (= (:suffix old-region) (:suffix new-region)))
    (is (types-task-projection/check-projection changed projected))))

(deftest context-independent-receipt-links
  (let [receipt "[receipt](https://github.com/burn2delete/uniform/commit/88f621ee7633be6965f34d33a43a361cbdd26157)"
        with-link (str/replace-first tasks status (str "Partial; " receipt))
        projected (types-task-projection/project with-link roadmap)]
    (is (str/includes? projected receipt))
    (is (types-task-projection/check-projection with-link projected)))
  (doseq [link ["[receipt](evidence.md#source-and-scope)" "[receipt](#source-and-scope)"
                "[receipt](/evidence.md)" "[receipt](//example.com/evidence)"
                "[receipt](http://example.com/evidence)" "[receipt][source]" "[receipt][]"
                "[receipt]" "![image](https://example.com/evidence)"
                "<a href=\"evidence.md\">receipt</a>" "<evidence.md>"
                "<https://example.com/evidence>" "[receipt](https://example.com/evidence \"title\")"
                "[receipt](https://user@example.com/evidence)"]]
    (is (rejects? #(types-task-projection/project
                    (str/replace-first tasks status (str "Partial; " link)) roadmap)) link)))

(let [result (run-tests)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
