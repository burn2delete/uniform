(load-file "tools/cli_task_projection.clj")
(require '[clojure.test :refer [deftest is run-tests]] '[clojure.string :as str]
         '[clojure.java.io :as io])
(def tasks (slurp cli-task-projection/task-path))
(def roadmap (slurp cli-task-projection/roadmap-path))
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest canonical-records-and-generated-boundaries
  (let [rows (cli-task-projection/records tasks)]
    (is (= 5 (count rows)))
    (is (= cli-task-projection/semantic-ids (set (map :id rows))))
    (is (every? #(= "Open" (:status %)) rows))
    (is (cli-task-projection/check-projection tasks roadmap))
    (is (= roadmap (cli-task-projection/project tasks roadmap)))
    (is (= roadmap (cli-task-projection/project (str tasks "\n- [x] T999 Historical only\n") roadmap))))
  (let [changed (str/replace-first tasks "Status: Open" "Status: Partial; source evidence")
        projected (cli-task-projection/project changed roadmap)]
    (is (rejects? #(cli-task-projection/check-projection changed roadmap)))
    (is (str/includes? projected "| Task | Status | Evidence baseline | Closing evidence |"))
    (is (str/includes? projected "Partial; source evidence"))
    (doseq [part [:prefix :suffix]]
      (is (= (part (task-projection/region roadmap "cli-task-projection"))
             (part (task-projection/region projected "cli-task-projection"))))))
  (let [accepted (-> tasks (str/replace-first "- [ ]" "- [x]")
                     (str/replace-first "Status: Open" "Status: Accepted; [receipt](https://example.com/review)"))]
    (is (= 5 (count (cli-task-projection/records accepted))))
    (is (not (str/includes? (cli-task-projection/project accepted roadmap) "Task (all open)")))))

(deftest identity-and-structure-rejections
  (doseq [bad [(str/replace-first tasks "- [ ]" "- [x]")
               (str/replace-first tasks "Status: Open" "Status: Accepted; receipt")
               (str/replace-first tasks "Evidence baseline:" "Evidence:")
               (str/replace-first tasks "Reconcile supported command/options with normative families" "Unsupported renamed task")
               (str/replace-first tasks "cli/remaining-tasks-and-evidence/reconcile-supported-commandoptions-with-normative-families" "cli/unknown")
               (str/replace tasks "cli/remaining-tasks-and-evidence/connect-checkruncompilebuildtestpackageverify-to-real-services" "cli/remaining-tasks-and-evidence/reconcile-supported-commandoptions-with-normative-families")
               (str/replace tasks "<!-- cli-implementation-tasks:end -->" "")
               (str tasks "<!-- cli-implementation-tasks:start -->")
               "<!-- cli-implementation-tasks:end -->\n<!-- cli-implementation-tasks:start -->"]]
    (is (rejects? #(cli-task-projection/records bad))))
  (doseq [bad [(str/replace roadmap "<!-- cli-task-projection:end -->" "")
               (str roadmap "<!-- cli-task-projection:start -->")
               "<!-- cli-task-projection:end -->\n<!-- cli-task-projection:start -->"]]
    (is (rejects? #(cli-task-projection/project tasks bad)))))

(deftest relative-link-provenance
  (doseq [row (cli-task-projection/records tasks)]
    (let [projected (cli-task-projection/rebase-links (:evidence row)
                       cli-task-projection/task-path cli-task-projection/roadmap-path)
          sources (re-seq cli-task-projection/link-pattern (:evidence row))
          destinations (re-seq cli-task-projection/link-pattern projected)]
      (is (= (count sources) (count destinations)))
      (doseq [[[_ _ target] [_ _ destination]] (map vector sources destinations)]
        (is (= (cli-task-projection/relative-target cli-task-projection/task-path target)
               (cli-task-projection/relative-target cli-task-projection/roadmap-path destination))))))
  (is (str/includes? tasks "(../../docs/phase-13-tooling-and-developer-experience/README.md)"))
  (is (str/includes? roadmap "(../phase-13-tooling-and-developer-experience/README.md)"))
  (doseq [link ["[x](../../missing-file.md)" "[x](../../../outside.md)" "[x](/etc/passwd)"
                "[x](//example.com/path)" "[x](http://example.com/path)" "[x](#anchor)"
                "[x](tasks.md#missing)" "[x](tasks.md?query)" "[x](%2e%2e/path)"
                "![x](tasks.md)" "[x][ref]" "<a>" "[x](https://user@example.com/a)"]]
    (is (rejects? #(cli-task-projection/project
                    (str/replace-first tasks "Status: Open" (str "Status: Partial; " link)) roadmap)) link)))

(defn snapshot [root]
  (into {} (for [f (file-seq root) :when (.isFile f)]
             [(str (.relativize (.toPath root) (.toPath f))) (slurp f)])))

(deftest selected-command-authority-and-fragments
  (let [scope (cli-task-projection/selected-scope tasks)
        projected (cli-task-projection/rebase-links scope cli-task-projection/task-path cli-task-projection/roadmap-path)]
    (is (= 5 (count (cli-task-projection/records tasks))))
    (is (= 2 (count (re-seq cli-task-projection/link-pattern scope))))
    (doseq [[[_ _ source] [_ _ target]] (map vector (re-seq cli-task-projection/link-pattern scope)
                                          (re-seq cli-task-projection/link-pattern projected))]
      (is (= (cli-task-projection/relative-target cli-task-projection/task-path source)
             (cli-task-projection/relative-target cli-task-projection/roadmap-path target)))
      (is (= (second (cli-task-projection/relative-parts source))
             (second (cli-task-projection/relative-parts target)))))
    (is (str/includes? projected "(tools.md#release-outcome)")))
  (let [changed (str/replace-first tasks "finite CLI slice required" "finite audited CLI slice required")
        projected (cli-task-projection/project changed roadmap)]
    (is (rejects? #(cli-task-projection/check-projection changed roadmap)))
    (is (str/includes? projected "finite audited CLI slice required"))
    (is (= (:body (task-projection/region roadmap "cli-task-projection"))
           (:body (task-projection/region projected "cli-task-projection")))))
  (doseq [bad [(str/replace tasks "<!-- cli-compiler-commands-scope:end -->" "")
               (str tasks "<!-- cli-compiler-commands-scope:start -->")
               (str/replace tasks "`cli/compiler-commands`" "`cli/unknown`")
               (str/replace tasks "3. Verify JSON/text consistency" "4. Verify JSON/text consistency")
               (str/replace tasks "#final-phase-18-completion-gate" "#missing-heading")
               (str/replace tasks "#final-phase-18-completion-gate" "#")
               (str/replace tasks "#final-phase-18-completion-gate" "#gate#extra")
               (str/replace tasks "#final-phase-18-completion-gate" "#%67ate")
               (str/replace tasks "#final-phase-18-completion-gate" "#gate?query")]]
    (is (rejects? #(cli-task-projection/project bad roadmap))))
  (doseq [bad [(str/replace roadmap "<!-- cli-compiler-commands-projection:end -->" "")
               (str roadmap "<!-- cli-compiler-commands-projection:start -->")]]
    (is (rejects? #(cli-task-projection/project tasks bad))))
  (is (rejects? #(task-projection-links/relative-target "Unchanged shared policy"
                   cli-task-projection/task-path "../../docs/component-roadmaps/tools.md#release-outcome"))))
(defn invoke-script [root script args]
  (let [p (.start (doto (ProcessBuilder. ^java.util.List
                         (into [(str (io/file (System/getProperty "java.home") "bin/java"))
                                "-Xmx128m" "-cp" (System/getProperty "java.class.path")
                                "clojure.main" script] args))
                   (.directory root) (.redirectErrorStream true)))
        output (slurp (.getInputStream p))]
    {:exit (.waitFor p) :output output}))
(defn invoke [root args]
  (invoke-script root "tools/cli_task_projection.clj" args))

(deftest fixed-cli-boundary-and-symlink-escape
  (let [temp (.toFile (java.nio.file.Files/createTempDirectory "cli-projection-" (make-array java.nio.file.attribute.FileAttribute 0)))
        root (io/file temp "checkout")
        task-file (io/file root cli-task-projection/task-path)
        roadmap-path cli-task-projection/roadmap-path
        sentinel (io/file temp "outside.md")]
    (try
      (doseq [path (concat ["tools/task_projection.clj" "tools/task_projection_links.clj" "tools/cli_task_projection.clj"
                           cli-task-projection/task-path roadmap-path]
                    (for [row (cli-task-projection/records tasks)
                          [_ _ target] (re-seq cli-task-projection/link-pattern (:evidence row))]
                      (str (cli-task-projection/relative-target cli-task-projection/task-path target)))
                    (for [[_ _ target] (re-seq cli-task-projection/link-pattern (cli-task-projection/selected-scope tasks))]
                      (str (cli-task-projection/relative-target cli-task-projection/task-path target))))]
        (io/make-parents (io/file root path)) (spit (io/file root path) (slurp path)))
      (spit sentinel "Outside write boundary")
      (let [before (snapshot root)]
        (is (= 0 (:exit (invoke root ["--check"]))))
        (is (= before (snapshot root)))
        (is (= 0 (:exit (invoke root []))))
        (is (= 0 (:exit (invoke-script root "tools/task_projection_links.clj"
                             ["--write" (.getPath sentinel)]))))
        (is (= before (snapshot root)))
        (doseq [args [["--write" (.getPath sentinel)] ["--output" (.getPath sentinel)]]]
          (is (not= 0 (:exit (invoke root args)))))
        (is (= before (snapshot root))))
      (spit task-file (str/replace-first tasks "Status: Open" "Status: Partial; fixture"))
      (let [before (snapshot root)]
        (is (not= 0 (:exit (invoke root ["--check"]))))
        (is (= 0 (:exit (invoke root ["--write"]))))
        (is (= (dissoc before roadmap-path) (dissoc (snapshot root) roadmap-path)))
        (is (not= (get before roadmap-path) (get (snapshot root) roadmap-path)))
        (is (= 0 (:exit (invoke root ["--check"])))))
      (let [link (io/file root "specs/cli-workflow/escape.md")]
        (java.nio.file.Files/createSymbolicLink (.toPath link) (.toPath sentinel) (make-array java.nio.file.attribute.FileAttribute 0))
        (spit task-file (str/replace-first tasks "Status: Open" "Status: Partial; [escape](escape.md)"))
        (let [before (snapshot root)]
          (is (not= 0 (:exit (invoke root ["--write"]))))
          (is (= before (snapshot root)))))
      (is (= "Outside write boundary" (slurp sentinel)))
      (finally (doseq [f (reverse (file-seq temp))] (io/delete-file f))))))

(let [r (run-tests)] (when (pos? (+ (:fail r) (:error r))) (System/exit 1)))
