(load-file "tools/lower_task_projection.clj")
(require '[clojure.test :refer [deftest is run-tests]] '[clojure.string :as str] '[clojure.java.io])

(def tasks (slurp lower-task-projection/task-path))
(def roadmap (slurp lower-task-projection/roadmap-path))
(def status "Open; partial source evidence above is not closure")
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest deterministic-compatible-projection
  (is (= ["lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs" "lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout" "lower/current-evidence-and-remaining-work/lower3-select-explicit-providers" "lower/current-evidence-and-remaining-work/lower4-preserve-proofs-and-origins" "lower/current-evidence-and-remaining-work/lower5-integrate-target-emission"]
         (mapv :id (lower-task-projection/records tasks))))
  (is (lower-task-projection/check-projection tasks roadmap))
  (is (= roadmap (lower-task-projection/project tasks
                  (lower-task-projection/project tasks roadmap))))
  (let [with-history (str tasks "\n- [x] T999 Historical record only.\n")]
    (is (= roadmap (lower-task-projection/project with-history roadmap))))
  (let [changed (str/replace-first tasks status "Accepted; owner receipt pending semantic review")
        checked (str/replace-first changed "- [ ] `lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs`" "- [x] `lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs`")]
    (is (rejects? #(lower-task-projection/records changed)))
    (is (= 5 (count (lower-task-projection/records checked))))))

(deftest missing-duplicate-and-malformed-records
  (doseq [bad [(str/replace tasks "`lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs`" "`lower/current-evidence-and-remaining-work/lower6-unknown`")
               (str/replace tasks "`lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout`" "`lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs`")
               (str/replace-first tasks #"(?s)- \[ \] `lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs`.*?(?=\n\n- \[)" "")
               (str/replace-first tasks "Current evidence/status:" "State:")
               (str/replace-first tasks "- [ ] `lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs`" "- [x] `lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs`")
               (str/replace-first tasks "- [ ] `lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs`" "- [ ] `lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout`")
               (str/replace-first tasks "lower/1: Validate" "lower/2: Validate")
               (str/replace-first tasks "Validate lowering inputs" "Validate | lowering inputs")]]
    (is (rejects? #(lower-task-projection/records bad)))))

(deftest stale-projection-and-region-controls
  (is (rejects? #(lower-task-projection/check-projection tasks
                  (str/replace roadmap status "Accepted; source evidence"))))
  (is (rejects? #(lower-task-projection/check-projection
                  (str/replace-first tasks status "Partial; fresh evidence") roadmap)))
  (doseq [marker ["lower-task-projection"]
          text [(str/replace roadmap (str "<!-- " marker ":end -->") "")
                (str roadmap "<!-- " marker ":start -->")
                (str "<!-- " marker ":end -->\n<!-- " marker ":start -->")]]
    (is (rejects? #(lower-task-projection/project tasks text))))
  (doseq [text [(str/replace tasks "<!-- lower-implementation-tasks:end -->" "")
                (str tasks "<!-- lower-implementation-tasks:start -->")
                "<!-- lower-implementation-tasks:end -->\n<!-- lower-implementation-tasks:start -->"]]
    (is (rejects? #(lower-task-projection/records text))))
  (let [changed (str/replace-first tasks status "Partial; fresh evidence")
        projected (lower-task-projection/project changed roadmap)
        old-region (lower-task-projection/region roadmap "lower-task-projection")
        new-region (lower-task-projection/region projected "lower-task-projection")]
    (is (= (:prefix old-region) (:prefix new-region)))
    (is (= (:suffix old-region) (:suffix new-region)))
    (is (lower-task-projection/check-projection changed projected))))

(deftest context-independent-receipt-links
  (let [receipt "[receipt](https://github.com/burn2delete/uniform/commit/d5d8e15c773929337e1d9646da4f32af2d461e17)"
        with-link (str/replace-first tasks status (str "Partial; " receipt))
        projected (lower-task-projection/project with-link roadmap)]
    (is (str/includes? projected receipt))
    (is (lower-task-projection/check-projection with-link projected)))
  (doseq [link ["[receipt](evidence.md#source-and-scope)" "[receipt](#source-and-scope)"
                "[receipt](/evidence.md)" "[receipt](//example.com/evidence)"
                "[receipt](http://example.com/evidence)" "[receipt][source]" "[receipt][]"
                "[receipt]" "![image](https://example.com/evidence)"
                "<a href=\"evidence.md\">receipt</a>" "<evidence.md>"
                "<https://example.com/evidence>" "[receipt](https://example.com/evidence \"title\")"
                "[receipt](https://user@example.com/evidence)"]]
    (is (rejects? #(lower-task-projection/project
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
  (let [temp (.toFile (java.nio.file.Files/createTempDirectory "lower-projection-" (make-array java.nio.file.attribute.FileAttribute 0)))
        root (clojure.java.io/file temp "checkout")
        script "tools/lower_task_projection.clj"
        task-file (clojure.java.io/file root lower-task-projection/task-path)
        target lower-task-projection/roadmap-path
        sentinel (clojure.java.io/file temp "outside.md")]
    (try
      (doseq [path [script "tools/task_projection.clj" lower-task-projection/task-path target]]
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

(deftest strict-shared-policy-retained
  (is (rejects? #(lower-task-projection/validate-links "Lower -> Emit")))
  (is (rejects? #(task-projection/validate-links {:component "Fixture"} "Lower -> Emit"))))

(let [result (run-tests)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
