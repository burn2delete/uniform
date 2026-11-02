(load-file "tools/backends_task_projection.clj")
(require '[clojure.test :refer [deftest is run-tests]]
         '[clojure.string :as str] '[clojure.java.io :as io])
(def tasks (slurp backends-task-projection/task-path))
(def roadmap (slurp backends-task-projection/roadmap-path))
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest literal-family-identity-and-deterministic-projection
  (let [rows (backends-task-projection/records tasks)]
    (is (= 15 (count rows)))
    (is (= backends-task-projection/family-order (mapv :id rows)))
    (is (every? #(= "Open" (:status %)) rows))
    (is (backends-task-projection/check-projection tasks roadmap))
    (is (= roadmap (backends-task-projection/project tasks roadmap)))
    (is (= roadmap (backends-task-projection/project
                    (str tasks "\n- [x] T999 Historical only.\n") roadmap)))
    (doseq [{:keys [id title projection]} rows]
      (is (= title (get backends-task-projection/family-headings id)))
      (is (str/starts-with? projection "Remaining tasks:"))
      (is (str/includes? projection "Yes/no:")))))

(deftest explicit-status-and-unchanged-release-boundaries
  (let [changed (str/replace-first tasks "Status: Open" "Status: Partial; evidence pending")
        projected (backends-task-projection/project changed roadmap)]
    (is (rejects? #(backends-task-projection/check-projection changed roadmap)))
    (is (str/includes? projected "Implementation status: Partial; evidence pending"))
    (is (str/includes? projected backends-task-projection/mixed-summary))
    (is (not (str/includes? projected backends-task-projection/open-summary)))
    (is (backends-task-projection/check-projection changed projected))
    (is (= projected (backends-task-projection/project changed projected)))
    (is (str/includes? projected "selected staged **Linux LLVM** release candidate remains unchanged"))
    (is (str/includes? projected "release acceptance is **open**")))
  (let [inconsistent (str/replace-first tasks "Status: Open" "Status: Accepted; [receipt](https://example.com/receipt)")
        accepted (str/replace-first inconsistent "- [ ]" "- [x]")]
    (is (rejects? #(backends-task-projection/records inconsistent)))
    (is (= 15 (count (backends-task-projection/records accepted))))
    (is (str/includes? (backends-task-projection/project accepted roadmap)
                      "Implementation status: Accepted; [receipt](https://example.com/receipt)"))))

(deftest malformed-identities-and-region-rejection
  (doseq [bad [(str/replace-first tasks "`backend-interface`" "`backend-unknown`")
               (str/replace tasks "`backend-c`" "`backend-interface`")
               (str/replace-first tasks "`backend-interface` Backend Interface and ABI" "`backend-interface` Changed heading")
               (str/replace-first tasks "- [ ]" "- [x]")
               (str/replace-first tasks "Status:" "State:")
               (str/replace-first tasks "Projected task block:" "Projection:")
               (str/replace-first tasks "Yes/no:" "Questions:")
               (str/replace tasks "<!-- backends-implementation-tasks:end -->" "")
               (str tasks "<!-- backends-implementation-tasks:start -->")
               "<!-- backends-implementation-tasks:end -->\n<!-- backends-implementation-tasks:start -->"]]
    (is (rejects? #(backends-task-projection/records bad))))
  (doseq [marker (cons "backends-open-status-projection"
                      (map #(str "backends-task-projection/" %) backends-task-projection/family-order))]
    (doseq [bad [(str/replace roadmap (str "<!-- " marker ":end -->") "")
                 (str roadmap "<!-- " marker ":start -->")
                 (str "<!-- " marker ":end -->\n<!-- " marker ":start -->")]]
      (is (rejects? #(backends-task-projection/project tasks bad)))))
  (is (rejects? #(backends-task-projection/project tasks
                  (str/replace roadmap backends-task-projection/open-summary "Unrecognized summary.")))))

(deftest document-relative-evidence-and-strict-markdown
  (doseq [{:keys [projection]} (backends-task-projection/records tasks)]
    (let [rebased (backends-task-projection/rebase projection)
          old (re-seq task-projection-links/link-pattern projection)
          new (re-seq task-projection-links/link-pattern rebased)]
      (is (= projection rebased))
      (is (= 1 (count old)))
      (is (= (count old) (count new)))
      (doseq [[[_ _ source] [_ _ destination]] (map vector old new)]
        (is (= (task-projection-links/relative-target "Backends" backends-task-projection/task-path source)
               (task-projection-links/relative-target "Backends" backends-task-projection/roadmap-path destination))))))
  (doseq [link ["[x](../../missing-file.md)" "[x](../../../outside.md)" "[x](/etc/passwd)"
                "[x](//example.com/path)" "[x](http://example.com/path)" "[x](#anchor)"
                "[x](tasks.md#anchor)" "[x](tasks.md?query)" "[x](%2e%2e/path)"
                "![x](https://example.com/a)" "[x][ref]" "<a>" "Backends -> Lower"
                "[x](https://user@example.com/a)"]]
    (is (rejects? #(backends-task-projection/project
                    (str/replace-first tasks "Status: Open" (str "Status: Partial; " link)) roadmap)) link)))

(defn snapshot [root]
  (into {} (for [f (file-seq root) :when (.isFile f)]
             [(str (.relativize (.toPath root) (.toPath f))) (slurp f)])))
(defn invoke [root script args]
  (let [process (.start (doto (ProcessBuilder. ^java.util.List
                               (into [(str (io/file (System/getProperty "java.home") "bin/java"))
                                      "-Xmx128m" "-cp" (System/getProperty "java.class.path")
                                      "clojure.main" script] args))
                         (.directory root) (.redirectErrorStream true)))
        output (slurp (.getInputStream process))]
    {:exit (.waitFor process) :output output}))

(deftest fixed-write-boundary-and-symlink-rejection
  (let [temp (.toFile (java.nio.file.Files/createTempDirectory "backends-projection-" (make-array java.nio.file.attribute.FileAttribute 0)))
        root (io/file temp "checkout") script "tools/backends_task_projection.clj"
        task-file (io/file root backends-task-projection/task-path)
        target backends-task-projection/roadmap-path sentinel (io/file temp "outside.md")]
    (try
      (doseq [path (concat [script "tools/task_projection.clj" "tools/task_projection_links.clj"
                           backends-task-projection/task-path target]
                    (for [row (backends-task-projection/records tasks)
                          [_ _ href] (re-seq task-projection-links/link-pattern (:projection row))]
                      (str (task-projection-links/relative-target "Backends" backends-task-projection/task-path href))))]
        (io/make-parents (io/file root path)) (spit (io/file root path) (slurp path)))
      (spit sentinel "Outside write boundary")
      (let [before (snapshot root)]
        (is (= 0 (:exit (invoke root script ["--check"]))))
        (is (= 0 (:exit (invoke root script []))))
        (is (= 0 (:exit (invoke root "tools/task_projection_links.clj" ["--write" (.getPath sentinel)]))))
        (is (= before (snapshot root)))
        (doseq [args [["--write" (.getPath sentinel)] ["--output" (.getPath sentinel)]]]
          (is (not= 0 (:exit (invoke root script args)))))
        (is (= before (snapshot root))))
      (spit task-file (str/replace-first tasks "Status: Open" "Status: Partial; CLI fixture"))
      (let [before (snapshot root)]
        (is (not= 0 (:exit (invoke root script ["--check"]))))
        (is (= before (snapshot root)))
        (is (= 0 (:exit (invoke root script ["--write"]))))
        (is (not= (get before target) (get (snapshot root) target)))
        (is (= (dissoc before target) (dissoc (snapshot root) target)))
        (is (= 0 (:exit (invoke root script ["--check"])))))
      (let [link (io/file root "specs/backends-workflow/escape.md")]
        (java.nio.file.Files/createSymbolicLink (.toPath link) (.toPath sentinel) (make-array java.nio.file.attribute.FileAttribute 0))
        (spit task-file (str/replace-first tasks "Status: Open" "Status: Partial; [escape](escape.md)"))
        (let [before (snapshot root)]
          (is (not= 0 (:exit (invoke root script ["--write"]))))
          (is (= before (snapshot root)))))
      (is (= "Outside write boundary" (slurp sentinel)))
      (finally (doseq [f (reverse (file-seq temp))] (io/delete-file f))))))

(let [result (run-tests)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
