(load-file "tools/mir_task_projection.clj")
(require '[clojure.test :refer [deftest is run-tests]] '[clojure.string :as str] '[clojure.java.io])

(def tasks (slurp mir-task-projection/task-path))
(def roadmap (slurp mir-task-projection/roadmap-path))
(def status "Open; partial source evidence above is not closure")
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest deterministic-compatible-projection
  (is (= ["mir/current-evidence-and-remaining-work/mir1-construct-semantic-mir" "mir/current-evidence-and-remaining-work/mir2-attach-fact-tables" "mir/current-evidence-and-remaining-work/mir3-verify-graph-and-legality" "mir/current-evidence-and-remaining-work/mir4-serialize-stable-modules" "mir/current-evidence-and-remaining-work/mir5-gate-downstream-consumption"]
         (mapv :id (mir-task-projection/records tasks))))
  (is (mir-task-projection/check-projection tasks roadmap))
  (is (= roadmap (mir-task-projection/project tasks
                  (mir-task-projection/project tasks roadmap))))
  (let [with-history (str tasks "\n- [x] T999 Historical record only.\n")]
    (is (= roadmap (mir-task-projection/project with-history roadmap))))
  (let [changed (str/replace-first tasks status "Accepted; owner receipt pending semantic review")
        checked (str/replace-first changed "- [ ] `mir/current-evidence-and-remaining-work/mir1-construct-semantic-mir`" "- [x] `mir/current-evidence-and-remaining-work/mir1-construct-semantic-mir`")]
    (is (rejects? #(mir-task-projection/records changed)))
    (is (= 5 (count (mir-task-projection/records checked))))))

(deftest missing-duplicate-and-malformed-records
  (doseq [bad [(str/replace tasks "`mir/current-evidence-and-remaining-work/mir1-construct-semantic-mir`" "`mir/current-evidence-and-remaining-work/mir6-unknown`")
               (str/replace tasks "`mir/current-evidence-and-remaining-work/mir2-attach-fact-tables`" "`mir/current-evidence-and-remaining-work/mir1-construct-semantic-mir`")
               (str/replace-first tasks #"(?s)- \[ \] `mir/current-evidence-and-remaining-work/mir1-construct-semantic-mir`.*?(?=\n\n- \[)" "")
               (str/replace-first tasks "Current evidence/status:" "State:")
               (str/replace-first tasks "- [ ] `mir/current-evidence-and-remaining-work/mir1-construct-semantic-mir`" "- [x] `mir/current-evidence-and-remaining-work/mir1-construct-semantic-mir`")
               (str/replace-first tasks "- [ ] `mir/current-evidence-and-remaining-work/mir1-construct-semantic-mir`" "- [ ] `mir/current-evidence-and-remaining-work/mir2-attach-fact-tables`")
               (str/replace-first tasks "mir/1: Construct" "mir/2: Construct")
               (str/replace-first tasks "Construct semantic MIR" "Construct | semantic MIR")]]
    (is (rejects? #(mir-task-projection/records bad)))))

(deftest stale-projection-and-region-controls
  (is (rejects? #(mir-task-projection/check-projection tasks
                  (str/replace roadmap status "Accepted; source evidence"))))
  (is (rejects? #(mir-task-projection/check-projection
                  (str/replace-first tasks status "Partial; fresh evidence") roadmap)))
  (doseq [marker ["mir-task-projection"]
          text [(str/replace roadmap (str "<!-- " marker ":end -->") "")
                (str roadmap "<!-- " marker ":start -->")
                (str "<!-- " marker ":end -->\n<!-- " marker ":start -->")]]
    (is (rejects? #(mir-task-projection/project tasks text))))
  (doseq [text [(str/replace tasks "<!-- mir-implementation-tasks:end -->" "")
                (str tasks "<!-- mir-implementation-tasks:start -->")
                "<!-- mir-implementation-tasks:end -->\n<!-- mir-implementation-tasks:start -->"]]
    (is (rejects? #(mir-task-projection/records text))))
  (let [changed (str/replace-first tasks status "Partial; fresh evidence")
        projected (mir-task-projection/project changed roadmap)
        old-region (mir-task-projection/region roadmap "mir-task-projection")
        new-region (mir-task-projection/region projected "mir-task-projection")]
    (is (= (:prefix old-region) (:prefix new-region)))
    (is (= (:suffix old-region) (:suffix new-region)))
    (is (mir-task-projection/check-projection changed projected))))

(deftest context-independent-receipt-links
  (let [receipt "[receipt](https://github.com/burn2delete/uniform/commit/d5d8e15c773929337e1d9646da4f32af2d461e17)"
        with-link (str/replace-first tasks status (str "Partial; " receipt))
        projected (mir-task-projection/project with-link roadmap)]
    (is (str/includes? projected receipt))
    (is (mir-task-projection/check-projection with-link projected)))
  (doseq [link ["[receipt](evidence.md#source-and-scope)" "[receipt](#source-and-scope)"
                "[receipt](/evidence.md)" "[receipt](//example.com/evidence)"
                "[receipt](http://example.com/evidence)" "[receipt][source]" "[receipt][]"
                "[receipt]" "![image](https://example.com/evidence)"
                "<a href=\"evidence.md\">receipt</a>" "<evidence.md>"
                "<https://example.com/evidence>" "[receipt](https://example.com/evidence \"title\")"
                "[receipt](https://user@example.com/evidence)"]]
    (is (rejects? #(mir-task-projection/project
                    (str/replace-first tasks status (str "Partial; " link)) roadmap)) link)))


(defn snapshot [root]
  (into {} (for [f (file-seq root) :when (.isFile f)]
             [(str (.relativize (.toPath root) (.toPath f))) (slurp f)])))
(defn invoke [root script args]
  (let [p (.start (doto (ProcessBuilder. ^java.util.List
                         (into [(str (clojure.java.io/file (System/getProperty "java.home") "bin/java"))
                                "-Xmx128m" "-cp" (System/getProperty "java.class.path")
                                "clojure.main" script] args))
                   (.directory root) (.redirectErrorStream true)))
        output (slurp (.getInputStream p))]
    {:exit (.waitFor p) :output output}))

(deftest fixed-path-cli-controls
  (let [temp (.toFile (java.nio.file.Files/createTempDirectory "mir-projection-" (make-array java.nio.file.attribute.FileAttribute 0)))
        root (clojure.java.io/file temp "checkout")
        script "tools/mir_task_projection.clj"
        task-file (clojure.java.io/file root mir-task-projection/task-path)
        target mir-task-projection/roadmap-path
        sentinel (clojure.java.io/file temp "outside.md")]
    (try
      (doseq [path [script "tools/task_projection.clj" mir-task-projection/task-path target]]
        (clojure.java.io/make-parents (clojure.java.io/file root path))
        (spit (clojure.java.io/file root path) (slurp path)))
      (spit sentinel "Outside fixed write boundary")
      (let [before (snapshot root)]
        (is (= 0 (:exit (invoke root script ["--check"]))))
        (is (= 0 (:exit (invoke root script []))))
        (is (= 0 (:exit (invoke root "tools/task_projection.clj" ["--write" (.getPath sentinel)]))))
        (is (= before (snapshot root)))
        (doseq [args [["--write" (.getPath sentinel)] ["--output" (.getPath sentinel)]]]
          (is (not= 0 (:exit (invoke root script args)))))
        (is (= before (snapshot root))))
      (spit task-file (str/replace-first tasks status "Partial; CLI fixture"))
      (let [before (snapshot root)]
        (is (not= 0 (:exit (invoke root script ["--check"]))))
        (is (= before (snapshot root)))
        (is (= 0 (:exit (invoke root script ["--write"]))))
        (is (not= (get before target) (get (snapshot root) target)))
        (is (= (dissoc before target) (dissoc (snapshot root) target)))
        (is (= 0 (:exit (invoke root script ["--check"])))))
      (is (= "Outside fixed write boundary" (slurp sentinel)))
      (finally (doseq [f (reverse (file-seq temp))] (clojure.java.io/delete-file f))))))

(let [result (run-tests)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
