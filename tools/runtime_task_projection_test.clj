(load-file "tools/runtime_task_projection.clj")
(require '[clojure.test :refer [deftest is run-tests]] '[clojure.string :as str]
         '[clojure.java.io :as io])
(def tasks (slurp runtime-task-projection/task-path))
(def roadmap (slurp runtime-task-projection/roadmap-path))
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest canonical-records-and-generated-boundaries
  (let [rows (runtime-task-projection/records tasks)]
    (is (= 5 (count rows)))
    (is (= runtime-task-projection/semantic-ids (set (map :id rows))))
    (is (every? #(= "Open" (:status %)) rows))
    (is (runtime-task-projection/check-projection tasks roadmap))
    (is (= roadmap (runtime-task-projection/project tasks roadmap)))
    (is (= roadmap (runtime-task-projection/project (str tasks "\n- [x] T999 Historical only\n") roadmap))))
  (let [changed (str/replace-first tasks "Status: Open" "Status: Partial; source evidence")
        projected (runtime-task-projection/project changed roadmap)]
    (is (rejects? #(runtime-task-projection/check-projection changed roadmap)))
    (is (str/includes? projected "| Task | Status | Existing evidence | Required closing evidence |"))
    (is (str/includes? projected "Partial; source evidence"))
    (doseq [part [:prefix :suffix]]
      (is (= (part (task-projection/region roadmap "runtime-task-projection"))
             (part (task-projection/region projected "runtime-task-projection"))))))
  (let [accepted (-> tasks (str/replace-first "- [ ]" "- [x]")
                     (str/replace-first "Status: Open" "Status: Accepted; [receipt](https://example.com/review)"))]
    (is (= 5 (count (runtime-task-projection/records accepted))))
    (is (not (str/includes? (runtime-task-projection/project accepted roadmap) "All tasks are open;")))))

(deftest identity-and-structure-rejections
  (doseq [bad [(str/replace-first tasks "- [ ]" "- [x]")
               (str/replace-first tasks "Status: Open" "Status: Accepted; receipt")
               (str/replace-first tasks "Evidence baseline:" "Evidence:")
               (str/replace-first tasks "Implement explicit family selection" "Implement unsupported hidden selection")
               (str/replace-first tasks "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection" "runtime/unknown")
               (str/replace tasks "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup" "runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection")
               (str/replace tasks "<!-- runtime-implementation-tasks:end -->" "")
               (str tasks "<!-- runtime-implementation-tasks:start -->")
               "<!-- runtime-implementation-tasks:end -->\n<!-- runtime-implementation-tasks:start -->"]]
    (is (rejects? #(runtime-task-projection/records bad))))
  (doseq [bad [(str/replace roadmap "<!-- runtime-task-projection:end -->" "")
               (str roadmap "<!-- runtime-task-projection:start -->")
               "<!-- runtime-task-projection:end -->\n<!-- runtime-task-projection:start -->"]]
    (is (rejects? #(runtime-task-projection/project tasks bad)))))

(deftest relative-link-provenance
  (doseq [row (runtime-task-projection/records tasks)]
    (let [projected (runtime-task-projection/rebase-links (:evidence row)
                       runtime-task-projection/task-path runtime-task-projection/roadmap-path)
          sources (re-seq runtime-task-projection/link-pattern (:evidence row))
          destinations (re-seq runtime-task-projection/link-pattern projected)]
      (is (= (count sources) (count destinations)))
      (doseq [[[_ _ target] [_ _ destination]] (map vector sources destinations)]
        (is (= (runtime-task-projection/relative-target runtime-task-projection/task-path target)
               (runtime-task-projection/relative-target runtime-task-projection/roadmap-path destination))))))
  (is (str/includes? tasks "(../../docs/phase-08-runtime-architecture/README.md)"))
  (is (str/includes? roadmap "(../phase-08-runtime-architecture/README.md)"))
  (doseq [link ["[x](../../missing-file.md)" "[x](../../../outside.md)" "[x](/etc/passwd)"
                "[x](//example.com/path)" "[x](http://example.com/path)" "[x](#anchor)"
                "[x](tasks.md#missing)" "[x](tasks.md?query)" "[x](%2e%2e/path)"
                "![x](tasks.md)" "[x][ref]" "<a>" "[x](https://user@example.com/a)"]]
    (is (rejects? #(runtime-task-projection/project
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
  (invoke-script root "tools/runtime_task_projection.clj" args))

(deftest fixed-cli-boundary-and-symlink-escape
  (let [temp (.toFile (java.nio.file.Files/createTempDirectory "runtime-projection-" (make-array java.nio.file.attribute.FileAttribute 0)))
        root (io/file temp "checkout")
        task-file (io/file root runtime-task-projection/task-path)
        roadmap-path runtime-task-projection/roadmap-path
        sentinel (io/file temp "outside.md")]
    (try
      (doseq [path (concat ["tools/task_projection.clj" "tools/task_projection_links.clj" "tools/runtime_task_projection.clj"
                           runtime-task-projection/task-path roadmap-path]
                    (for [row (runtime-task-projection/records tasks)
                          [_ _ target] (re-seq runtime-task-projection/link-pattern (:evidence row))]
                      (str (runtime-task-projection/relative-target runtime-task-projection/task-path target))))]
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
      (let [link (io/file root "specs/runtime-workflow/escape.md")]
        (java.nio.file.Files/createSymbolicLink (.toPath link) (.toPath sentinel) (make-array java.nio.file.attribute.FileAttribute 0))
        (spit task-file (str/replace-first tasks "Status: Open" "Status: Partial; [escape](escape.md)"))
        (let [before (snapshot root)]
          (is (not= 0 (:exit (invoke root ["--write"]))))
          (is (= before (snapshot root)))))
      (is (= "Outside write boundary" (slurp sentinel)))
      (finally (doseq [f (reverse (file-seq temp))] (io/delete-file f))))))

(let [r (run-tests)] (when (pos? (+ (:fail r) (:error r))) (System/exit 1)))
