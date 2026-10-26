(load-file "tools/diag_task_projection.clj")
(require '[clojure.test :refer [deftest is run-tests]] '[clojure.string :as str]
         '[clojure.java.io :as io])
(def tasks (slurp diag-task-projection/task-path))
(def roadmap (slurp diag-task-projection/roadmap-path))
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest canonical-records-and-generated-boundaries
  (let [rows (diag-task-projection/records tasks)]
    (is (= 4 (count rows)))
    (is (= diag-task-projection/semantic-ids (set (map :id rows))))
    (is (every? #(= "Open" (:status %)) rows))
    (is (diag-task-projection/check-projection tasks roadmap))
    (is (= roadmap (diag-task-projection/project tasks roadmap)))
    (is (= roadmap (diag-task-projection/project (str tasks "\n- [x] T999 Historical only\n") roadmap))))
  (let [changed (str/replace-first tasks "Status: Open" "Status: Partial; source evidence")
        projected (diag-task-projection/project changed roadmap)]
    (is (rejects? #(diag-task-projection/check-projection changed roadmap)))
    (is (str/includes? projected "| Task | Status | Evidence baseline | Closing evidence |"))
    (is (str/includes? projected "Partial; source evidence"))
    (doseq [part [:prefix :suffix]]
      (is (= (part (task-projection/region roadmap "diag-task-projection"))
             (part (task-projection/region projected "diag-task-projection"))))))
  (let [accepted (-> tasks (str/replace-first "- [ ]" "- [x]")
                     (str/replace-first "Status: Open" "Status: Accepted; [receipt](https://example.com/review)"))]
    (is (= 4 (count (diag-task-projection/records accepted))))
    (is (not (str/includes? (diag-task-projection/project accepted roadmap) "Task (all open)")))))

(deftest identity-and-structure-rejections
  (doseq [bad [(str/replace-first tasks "- [ ]" "- [x]")
               (str/replace-first tasks "Status: Open" "Status: Accepted; receipt")
               (str/replace-first tasks "Evidence baseline:" "Evidence:")
               (str/replace-first tasks "Reconcile stage carriers" "Reconcile stage rules")
               (str/replace-first tasks "diag/remaining-tasks-and-evidence/reconcile-stage-carriers-with-canonical-record-schema" "diag/unknown")
               (str/replace tasks "diag/remaining-tasks-and-evidence/preserve-primaryrelatedgenerated-origins-and-stable-order" "diag/remaining-tasks-and-evidence/reconcile-stage-carriers-with-canonical-record-schema")
               (str/replace tasks "<!-- diag-implementation-tasks:end -->" "")
               (str tasks "<!-- diag-implementation-tasks:start -->")
               "<!-- diag-implementation-tasks:end -->\n<!-- diag-implementation-tasks:start -->"]]
    (is (rejects? #(diag-task-projection/records bad))))
  (doseq [bad [(str/replace roadmap "<!-- diag-task-projection:end -->" "")
               (str roadmap "<!-- diag-task-projection:start -->")
               "<!-- diag-task-projection:end -->\n<!-- diag-task-projection:start -->"]]
    (is (rejects? #(diag-task-projection/project tasks bad)))))

(deftest relative-link-provenance
  (doseq [row (diag-task-projection/records tasks)]
    (let [projected (diag-task-projection/rebase-links (:evidence row)
                       diag-task-projection/task-path diag-task-projection/roadmap-path)
          sources (re-seq diag-task-projection/link-pattern (:evidence row))
          destinations (re-seq diag-task-projection/link-pattern projected)]
      (is (= (count sources) (count destinations)))
      (doseq [[[_ _ target] [_ _ destination]] (map vector sources destinations)]
        (is (= (diag-task-projection/relative-target diag-task-projection/task-path target)
               (diag-task-projection/relative-target diag-task-projection/roadmap-path destination))))))
  (is (str/includes? tasks "(../../docs/phase-13-tooling-and-developer-experience/README.md)"))
  (is (str/includes? roadmap "(../phase-13-tooling-and-developer-experience/README.md)"))
  (doseq [link ["[x](../../missing-file.md)" "[x](../../../outside.md)" "[x](/etc/passwd)"
                "[x](//example.com/path)" "[x](http://example.com/path)" "[x](#anchor)"
                "[x](tasks.md#missing)" "[x](tasks.md?query)" "[x](%2e%2e/path)"
                "![x](tasks.md)" "[x][ref]" "<a>" "[x](https://user@example.com/a)"]]
    (is (rejects? #(diag-task-projection/project
                    (str/replace-first tasks "Status: Open" (str "Status: Partial; " link)) roadmap)) link)))

(defn snapshot [root]
  (into {} (for [f (file-seq root) :when (.isFile f)]
             [(str (.relativize (.toPath root) (.toPath f))) (slurp f)])))
(defn invoke-script [root script args]
  (let [p (.start (doto (ProcessBuilder. ^java.util.List
                         (into [(str (io/file (System/getProperty "java.home") "bin/java"))
                                "-Xmx128m" "-cp" (System/getProperty "java.class.path")
                                "clojure.main" script] args))
                   (.directory root) (.redirectErrorStream true)))
        output (slurp (.getInputStream p))]
    {:exit (.waitFor p) :output output}))
(defn invoke [root args]
  (invoke-script root "tools/diag_task_projection.clj" args))

(deftest fixed-cli-boundary-and-symlink-escape
  (let [temp (.toFile (java.nio.file.Files/createTempDirectory "diag-projection-" (make-array java.nio.file.attribute.FileAttribute 0)))
        root (io/file temp "checkout")
        task-file (io/file root diag-task-projection/task-path)
        roadmap-path diag-task-projection/roadmap-path
        sentinel (io/file temp "outside.md")]
    (try
      (doseq [path (concat ["tools/task_projection.clj" "tools/task_projection_links.clj" "tools/diag_task_projection.clj"
                           diag-task-projection/task-path roadmap-path]
                    (for [row (diag-task-projection/records tasks)
                          [_ _ target] (re-seq diag-task-projection/link-pattern (:evidence row))]
                      (str (diag-task-projection/relative-target diag-task-projection/task-path target))))]
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
      (let [link (io/file root "specs/diag-workflow/escape.md")]
        (java.nio.file.Files/createSymbolicLink (.toPath link) (.toPath sentinel) (make-array java.nio.file.attribute.FileAttribute 0))
        (spit task-file (str/replace-first tasks "Status: Open" "Status: Partial; [escape](escape.md)"))
        (let [before (snapshot root)]
          (is (not= 0 (:exit (invoke root ["--write"]))))
          (is (= before (snapshot root)))))
      (is (= "Outside write boundary" (slurp sentinel)))
      (finally (doseq [f (reverse (file-seq temp))] (io/delete-file f))))))

(let [r (run-tests)] (when (pos? (+ (:fail r) (:error r))) (System/exit 1)))
