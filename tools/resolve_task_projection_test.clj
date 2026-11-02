(load-file "tools/resolve_task_projection.clj")
(require '[clojure.test :refer [deftest is run-tests]] '[clojure.string :as str])

(def tasks (slurp resolve-task-projection/task-path))
(def roadmap (slurp resolve-task-projection/roadmap-path))
(def status "Open; partial source evidence above is not closure")
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest deterministic-compatible-projection
  (is (= ["resolve/current-evidence-and-remaining-work/resolve1-resolve-lexical-positions" "resolve/current-evidence-and-remaining-work/resolve2-validate-module-boundaries" "resolve/current-evidence-and-remaining-work/resolve3-check-graph-legality" "resolve/current-evidence-and-remaining-work/resolve4-publish-stable-indexes" "resolve/current-evidence-and-remaining-work/resolve5-integrate-binding-consumers"]
         (mapv :id (resolve-task-projection/records tasks))))
  (is (resolve-task-projection/check-projection tasks roadmap))
  (is (= roadmap (resolve-task-projection/project tasks
                  (resolve-task-projection/project tasks roadmap))))
  (let [with-history (str tasks "\n- [x] T999 Historical record only.\n")]
    (is (= roadmap (resolve-task-projection/project with-history roadmap))))
  (let [changed (str/replace-first tasks status "Accepted; owner receipt pending semantic review")
        checked (str/replace-first changed "- [ ] `resolve/current-evidence-and-remaining-work/resolve1-resolve-lexical-positions`" "- [x] `resolve/current-evidence-and-remaining-work/resolve1-resolve-lexical-positions`")]
    (is (rejects? #(resolve-task-projection/records changed)))
    (is (= 5 (count (resolve-task-projection/records checked))))))

(deftest missing-duplicate-and-malformed-records
  (doseq [bad [(str/replace tasks "`resolve/current-evidence-and-remaining-work/resolve1-resolve-lexical-positions`" "`resolve/current-evidence-and-remaining-work/resolve6-unknown`")
               (str/replace tasks "`resolve/current-evidence-and-remaining-work/resolve2-validate-module-boundaries`" "`resolve/current-evidence-and-remaining-work/resolve1-resolve-lexical-positions`")
               (str/replace-first tasks #"(?s)- \[ \] `resolve/current-evidence-and-remaining-work/resolve1-resolve-lexical-positions`.*?(?=\n\n- \[)" "")
               (str/replace-first tasks "Current evidence/status:" "State:")
               (str/replace-first tasks "- [ ] `resolve/current-evidence-and-remaining-work/resolve1-resolve-lexical-positions`" "- [x] `resolve/current-evidence-and-remaining-work/resolve1-resolve-lexical-positions`")
               (str/replace-first tasks "- [ ] `resolve/current-evidence-and-remaining-work/resolve1-resolve-lexical-positions`" "- [ ] `resolve/current-evidence-and-remaining-work/resolve2-validate-module-boundaries`")
               (str/replace-first tasks "resolve/1: Resolve" "resolve/2: Resolve")
               (str/replace-first tasks "Resolve lexical positions" "Resolve | lexical positions")]]
    (is (rejects? #(resolve-task-projection/records bad)))))

(deftest stale-projection-and-region-controls
  (is (rejects? #(resolve-task-projection/check-projection tasks
                  (str/replace roadmap status "Accepted; source evidence"))))
  (is (rejects? #(resolve-task-projection/check-projection
                  (str/replace-first tasks status "Partial; fresh evidence") roadmap)))
  (doseq [marker ["resolve-task-projection"]
          text [(str/replace roadmap (str "<!-- " marker ":end -->") "")
                (str roadmap "<!-- " marker ":start -->")
                (str "<!-- " marker ":end -->\n<!-- " marker ":start -->")]]
    (is (rejects? #(resolve-task-projection/project tasks text))))
  (doseq [text [(str/replace tasks "<!-- resolve-implementation-tasks:end -->" "")
                (str tasks "<!-- resolve-implementation-tasks:start -->")
                "<!-- resolve-implementation-tasks:end -->\n<!-- resolve-implementation-tasks:start -->"]]
    (is (rejects? #(resolve-task-projection/records text))))
  (let [changed (str/replace-first tasks status "Partial; fresh evidence")
        projected (resolve-task-projection/project changed roadmap)
        old-region (resolve-task-projection/region roadmap "resolve-task-projection")
        new-region (resolve-task-projection/region projected "resolve-task-projection")]
    (is (= (:prefix old-region) (:prefix new-region)))
    (is (= (:suffix old-region) (:suffix new-region)))
    (is (resolve-task-projection/check-projection changed projected))))

(deftest context-independent-receipt-links
  (let [receipt "[receipt](https://github.com/burn2delete/uniform/commit/023ec47d86004fc829ae7dc5753b715ca4a653dc)"
        with-link (str/replace-first tasks status (str "Partial; " receipt))
        projected (resolve-task-projection/project with-link roadmap)]
    (is (str/includes? projected receipt))
    (is (resolve-task-projection/check-projection with-link projected)))
  (doseq [link ["[receipt](evidence.md#source-and-scope)" "[receipt](#source-and-scope)"
                "[receipt](/evidence.md)" "[receipt](//example.com/evidence)"
                "[receipt](http://example.com/evidence)" "[receipt][source]" "[receipt][]"
                "[receipt]" "![image](https://example.com/evidence)"
                "<a href=\"evidence.md\">receipt</a>" "<evidence.md>"
                "<https://example.com/evidence>" "[receipt](https://example.com/evidence \"title\")"
                "[receipt](https://user@example.com/evidence)"]]
    (is (rejects? #(resolve-task-projection/project
                    (str/replace-first tasks status (str "Partial; " link)) roadmap)) link)))

(let [result (run-tests)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
