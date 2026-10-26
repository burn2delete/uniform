(load-file "tools/effects_task_projection.clj")
(require '[clojure.test :refer [deftest is run-tests]] '[clojure.string :as str])

(def tasks (slurp effects-task-projection/task-path))
(def roadmap (slurp effects-task-projection/roadmap-path))
(def status "Open; partial source evidence above is not closure")
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest deterministic-compatible-projection
  (is (= ["effects/current-evidence-and-remaining-work/effects1-infer-direct-effects" "effects/current-evidence-and-remaining-work/effects2-propagate-latent-effects" "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering" "effects/current-evidence-and-remaining-work/effects4-record-replay-and-invalidation" "effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers"]
         (mapv :id (effects-task-projection/records tasks))))
  (is (effects-task-projection/check-projection tasks roadmap))
  (is (= roadmap (effects-task-projection/project tasks
                  (effects-task-projection/project tasks roadmap))))
  (let [with-history (str tasks "\n- [x] T999 Historical record only.\n")]
    (is (= roadmap (effects-task-projection/project with-history roadmap))))
  (let [changed (str/replace-first tasks status "Accepted; owner receipt pending semantic review")
        checked (str/replace-first changed "- [ ] `effects/current-evidence-and-remaining-work/effects1-infer-direct-effects`" "- [x] `effects/current-evidence-and-remaining-work/effects1-infer-direct-effects`")]
    (is (rejects? #(effects-task-projection/records changed)))
    (is (= 5 (count (effects-task-projection/records checked))))))

(deftest missing-duplicate-and-malformed-records
  (doseq [bad [(str/replace tasks "`effects/current-evidence-and-remaining-work/effects1-infer-direct-effects`" "`effects/current-evidence-and-remaining-work/effects6-unknown`")
               (str/replace tasks "`effects/current-evidence-and-remaining-work/effects2-propagate-latent-effects`" "`effects/current-evidence-and-remaining-work/effects1-infer-direct-effects`")
               (str/replace-first tasks #"(?s)- \[ \] `effects/current-evidence-and-remaining-work/effects1-infer-direct-effects`.*?(?=\n\n- \[)" "")
               (str/replace-first tasks "Current evidence/status:" "State:")
               (str/replace-first tasks "- [ ] `effects/current-evidence-and-remaining-work/effects1-infer-direct-effects`" "- [x] `effects/current-evidence-and-remaining-work/effects1-infer-direct-effects`")
               (str/replace-first tasks "- [ ] `effects/current-evidence-and-remaining-work/effects1-infer-direct-effects`" "- [ ] `effects/current-evidence-and-remaining-work/effects2-propagate-latent-effects`")
               (str/replace-first tasks "effects/1: Infer" "effects/2: Infer")
               (str/replace-first tasks "Infer direct effects" "Infer | direct effects")]]
    (is (rejects? #(effects-task-projection/records bad)))))

(deftest stale-projection-and-region-controls
  (is (rejects? #(effects-task-projection/check-projection tasks
                  (str/replace roadmap status "Accepted; source evidence"))))
  (is (rejects? #(effects-task-projection/check-projection
                  (str/replace-first tasks status "Partial; fresh evidence") roadmap)))
  (doseq [marker ["effects-task-projection"]
          text [(str/replace roadmap (str "<!-- " marker ":end -->") "")
                (str roadmap "<!-- " marker ":start -->")
                (str "<!-- " marker ":end -->\n<!-- " marker ":start -->")]]
    (is (rejects? #(effects-task-projection/project tasks text))))
  (doseq [text [(str/replace tasks "<!-- effects-implementation-tasks:end -->" "")
                (str tasks "<!-- effects-implementation-tasks:start -->")
                "<!-- effects-implementation-tasks:end -->\n<!-- effects-implementation-tasks:start -->"]]
    (is (rejects? #(effects-task-projection/records text))))
  (let [changed (str/replace-first tasks status "Partial; fresh evidence")
        projected (effects-task-projection/project changed roadmap)
        old-region (effects-task-projection/region roadmap "effects-task-projection")
        new-region (effects-task-projection/region projected "effects-task-projection")]
    (is (= (:prefix old-region) (:prefix new-region)))
    (is (= (:suffix old-region) (:suffix new-region)))
    (is (effects-task-projection/check-projection changed projected))))

(deftest context-independent-receipt-links
  (let [receipt "[receipt](https://github.com/burn2delete/uniform/commit/fcb23737be00f646a74200fc26faaa8908f77164)"
        with-link (str/replace-first tasks status (str "Partial; " receipt))
        projected (effects-task-projection/project with-link roadmap)]
    (is (str/includes? projected receipt))
    (is (effects-task-projection/check-projection with-link projected)))
  (doseq [link ["[receipt](evidence.md#source-and-scope)" "[receipt](#source-and-scope)"
                "[receipt](/evidence.md)" "[receipt](//example.com/evidence)"
                "[receipt](http://example.com/evidence)" "[receipt][source]" "[receipt][]"
                "[receipt]" "![image](https://example.com/evidence)"
                "<a href=\"evidence.md\">receipt</a>" "<evidence.md>"
                "<https://example.com/evidence>" "[receipt](https://example.com/evidence \"title\")"
                "[receipt](https://user@example.com/evidence)"]]
    (is (rejects? #(effects-task-projection/project
                    (str/replace-first tasks status (str "Partial; " link)) roadmap)) link)))

(let [result (run-tests)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
