(load-file "tools/core_task_projection.clj")
(require '[clojure.test :refer [deftest is run-tests]] '[clojure.string :as str])

(def tasks (slurp core-task-projection/task-path))
(def roadmap (slurp core-task-projection/roadmap-path))
(def status "Open; partial source evidence above is not closure")
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest deterministic-compatible-projection
  (is (str/includes? (core-task-projection/project tasks roadmap) "var, set!, try/throw"))
  (is (= ["core/current-evidence-and-remaining-work/core1-lower-core-forms" "core/current-evidence-and-remaining-work/core2-preserve-binding-structure" "core/current-evidence-and-remaining-work/core3-desugar-without-semantic-loss" "core/current-evidence-and-remaining-work/core4-verify-and-serialize" "core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses"]
         (mapv :id (core-task-projection/records tasks))))
  (is (core-task-projection/check-projection tasks roadmap))
  (is (= roadmap (core-task-projection/project tasks
                  (core-task-projection/project tasks roadmap))))
  (let [with-history (str tasks "\n- [x] T999 Historical record only.\n")]
    (is (= roadmap (core-task-projection/project with-history roadmap))))
  (let [changed (str/replace-first tasks status "Accepted; owner receipt pending semantic review")
        checked (str/replace-first changed "- [ ] `core/current-evidence-and-remaining-work/core1-lower-core-forms`" "- [x] `core/current-evidence-and-remaining-work/core1-lower-core-forms`")]
    (is (rejects? #(core-task-projection/records changed)))
    (is (= 5 (count (core-task-projection/records checked))))))

(deftest missing-duplicate-and-malformed-records
  (doseq [bad [(str/replace tasks "`core/current-evidence-and-remaining-work/core1-lower-core-forms`" "`core/current-evidence-and-remaining-work/core6-unknown`")
               (str/replace tasks "`core/current-evidence-and-remaining-work/core2-preserve-binding-structure`" "`core/current-evidence-and-remaining-work/core1-lower-core-forms`")
               (str/replace-first tasks #"(?s)- \[ \] `core/current-evidence-and-remaining-work/core1-lower-core-forms`.*?(?=\n\n- \[)" "")
               (str/replace-first tasks "Current evidence/status:" "State:")
               (str/replace-first tasks "- [ ] `core/current-evidence-and-remaining-work/core1-lower-core-forms`" "- [x] `core/current-evidence-and-remaining-work/core1-lower-core-forms`")
               (str/replace-first tasks "- [ ] `core/current-evidence-and-remaining-work/core1-lower-core-forms`" "- [ ] `core/current-evidence-and-remaining-work/core2-preserve-binding-structure`")
               (str/replace-first tasks "core/1: Lower" "core/2: Lower")
               (str/replace-first tasks "Lower core forms" "Lower | core forms")]]
    (is (rejects? #(core-task-projection/records bad)))))

(deftest stale-projection-and-region-controls
  (is (rejects? #(core-task-projection/check-projection tasks
                  (str/replace roadmap status "Accepted; source evidence"))))
  (is (rejects? #(core-task-projection/check-projection
                  (str/replace-first tasks status "Partial; fresh evidence") roadmap)))
  (doseq [marker ["core-task-projection"]
          text [(str/replace roadmap (str "<!-- " marker ":end -->") "")
                (str roadmap "<!-- " marker ":start -->")
                (str "<!-- " marker ":end -->\n<!-- " marker ":start -->")]]
    (is (rejects? #(core-task-projection/project tasks text))))
  (doseq [text [(str/replace tasks "<!-- core-implementation-tasks:end -->" "")
                (str tasks "<!-- core-implementation-tasks:start -->")
                "<!-- core-implementation-tasks:end -->\n<!-- core-implementation-tasks:start -->"]]
    (is (rejects? #(core-task-projection/records text))))
  (let [changed (str/replace-first tasks status "Partial; fresh evidence")
        projected (core-task-projection/project changed roadmap)
        old-region (core-task-projection/region roadmap "core-task-projection")
        new-region (core-task-projection/region projected "core-task-projection")]
    (is (= (:prefix old-region) (:prefix new-region)))
    (is (= (:suffix old-region) (:suffix new-region)))
    (is (core-task-projection/check-projection changed projected))))

(deftest context-independent-receipt-links
  (let [receipt "[receipt](https://github.com/burn2delete/uniform/commit/31193e634ec99b2cd5ebfd90304e323840fcf7b9)"
        with-link (str/replace-first tasks status (str "Partial; " receipt))
        projected (core-task-projection/project with-link roadmap)]
    (is (str/includes? projected receipt))
    (is (core-task-projection/check-projection with-link projected)))
  (doseq [link ["[receipt](evidence.md#source-and-scope)" "[receipt](#source-and-scope)"
                "[receipt](/evidence.md)" "[receipt](//example.com/evidence)"
                "[receipt](http://example.com/evidence)" "[receipt][source]" "[receipt][]"
                "[receipt]" "![image](https://example.com/evidence)"
                "<a href=\"evidence.md\">receipt</a>" "<evidence.md>"
                "<https://example.com/evidence>" "[receipt](https://example.com/evidence \"title\")"
                "[receipt](https://user@example.com/evidence)"]]
    (is (rejects? #(core-task-projection/project
                    (str/replace-first tasks status (str "Partial; " link)) roadmap)) link)))

(let [result (run-tests)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
