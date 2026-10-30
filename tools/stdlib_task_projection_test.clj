(load-file "tools/stdlib_task_projection.clj")
(require '[clojure.test :refer [deftest is run-tests]] '[clojure.string :as str]
         '[clojure.java.io :as io])
(def tasks (slurp stdlib-task-projection/task-path))
(def roadmap (slurp stdlib-task-projection/roadmap-path))
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest canonical-records-and-generated-boundaries
  (let [rows (stdlib-task-projection/records tasks)]
    (is (= 11 (count rows)))
    (is (= stdlib-task-projection/semantic-ids (set (map :id rows))))
    (is (every? #(= "Open" (:status %)) rows))
    (is (stdlib-task-projection/check-projection tasks roadmap))
    (is (= roadmap (stdlib-task-projection/project tasks roadmap)))
    (is (= roadmap (stdlib-task-projection/project (str tasks "\n- [x] T999 Historical only\n") roadmap))))
  (let [changed (str/replace-first tasks "Status: Open" "Status: Partial; source evidence")
        projected (stdlib-task-projection/project changed roadmap)]
    (is (rejects? #(stdlib-task-projection/check-projection changed roadmap)))
    (is (str/includes? projected "| Task | Status | Evidence baseline | Closing evidence |"))
    (is (str/includes? projected "Partial; source evidence"))
    (doseq [part [:prefix :suffix]]
      (is (= (part (task-projection/region roadmap "stdlib-task-projection"))
             (part (task-projection/region projected "stdlib-task-projection"))))))
  (let [accepted (-> tasks (str/replace-first "- [ ]" "- [x]")
                     (str/replace-first "Status: Open" "Status: Accepted; [receipt](https://example.com/review)"))]
    (is (= 11 (count (stdlib-task-projection/records accepted))))
    (is (not (str/includes? (stdlib-task-projection/project accepted roadmap) "Task (all open)")))))

(deftest identity-and-structure-rejections
  (doseq [bad [(str/replace-first tasks "- [ ]" "- [x]")
               (str/replace-first tasks "Status: Open" "Status: Accepted; receipt")
               (str/replace-first tasks "Evidence baseline:" "Evidence:")
               (str/replace-first tasks "Reconcile export inventory" "Reconcile unsupported export list")
               (str/replace-first tasks "stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations" "stdlib/unknown")
               (str/replace tasks "stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis" "stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations")
               (str/replace tasks "<!-- stdlib-implementation-tasks:end -->" "")
               (str tasks "<!-- stdlib-implementation-tasks:start -->")
               "<!-- stdlib-implementation-tasks:end -->\n<!-- stdlib-implementation-tasks:start -->"]]
    (is (rejects? #(stdlib-task-projection/records bad))))
  (doseq [bad [(str/replace roadmap "<!-- stdlib-task-projection:end -->" "")
               (str roadmap "<!-- stdlib-task-projection:start -->")
               "<!-- stdlib-task-projection:end -->\n<!-- stdlib-task-projection:start -->"]]
    (is (rejects? #(stdlib-task-projection/project tasks bad)))))

(deftest relative-link-provenance
  (doseq [row (stdlib-task-projection/records tasks)]
    (let [projected (stdlib-task-projection/rebase-links (:evidence row)
                       stdlib-task-projection/task-path stdlib-task-projection/roadmap-path)
          sources (re-seq stdlib-task-projection/link-pattern (:evidence row))
          destinations (re-seq stdlib-task-projection/link-pattern projected)]
      (is (= (count sources) (count destinations)))
      (doseq [[[_ _ target] [_ _ destination]] (map vector sources destinations)]
        (is (= (stdlib-task-projection/relative-target stdlib-task-projection/task-path target)
               (stdlib-task-projection/relative-target stdlib-task-projection/roadmap-path destination))))))
  (is (str/includes? tasks "(../../docs/phase-16-standard-library/README.md)"))
  (is (str/includes? roadmap "(../phase-16-standard-library/README.md)"))
  (doseq [link ["[x](../../missing-file.md)" "[x](../../../outside.md)" "[x](/etc/passwd)"
                "[x](//example.com/path)" "[x](http://example.com/path)" "[x](#anchor)"
                "[x](tasks.md#missing)" "[x](tasks.md?query)" "[x](%2e%2e/path)"
                "![x](tasks.md)" "[x][ref]" "<a>" "[x](https://user@example.com/a)"]]
    (is (rejects? #(stdlib-task-projection/project
                    (str/replace-first tasks "Status: Open" (str "Status: Partial; " link)) roadmap)) link)))

(defn snapshot [root]
  (into {} (for [f (file-seq root) :when (.isFile f)]
             [(str (.relativize (.toPath root) (.toPath f))) (slurp f)])))

(deftest file-and-heading-provenance
  (let [source "[Platform outcome](../../docs/component-roadmaps/platform.md#release-outcome)"
        projected (stdlib-task-projection/rebase-links source
                    stdlib-task-projection/task-path stdlib-task-projection/roadmap-path)]
    (is (= "[Platform outcome](platform.md#release-outcome)" projected))
    (is (= "release-outcome" (second (stdlib-task-projection/relative-parts
                                      "../../docs/component-roadmaps/platform.md#release-outcome"))))
    (is (= (stdlib-task-projection/relative-target stdlib-task-projection/task-path
             "../../docs/component-roadmaps/platform.md#release-outcome")
           (stdlib-task-projection/relative-target stdlib-task-projection/roadmap-path
             "platform.md#release-outcome")))
    (is (str/includes? roadmap "`stdlib/compiler-support`")))
  (doseq [target ["../../docs/component-roadmaps/platform.md#missing-heading"
                  "../../docs/component-roadmaps/platform.md#"
                  "../../docs/component-roadmaps/platform.md#release-outcome#extra"
                  "../../docs/component-roadmaps/platform.md?x=y#release-outcome"
                  "../../docs/component-roadmaps/platform.md#%72elease-outcome"
                  "../../docs/component-roadmaps/platform.md#release-outcome?query"
                  "#release-outcome" "../../../outside.md#release-outcome"]]
    (is (rejects? #(stdlib-task-projection/records
                    (str/replace tasks "../../docs/component-roadmaps/platform.md#release-outcome" target)))))
  (is (rejects? #(task-projection-links/relative-target "Unchanged shared policy"
                   stdlib-task-projection/task-path "../../docs/component-roadmaps/platform.md#release-outcome"))))
(defn invoke-script [root script args]
  (let [p (.start (doto (ProcessBuilder. ^java.util.List
                         (into [(str (io/file (System/getProperty "java.home") "bin/java"))
                                "-Xmx128m" "-cp" (System/getProperty "java.class.path")
                                "clojure.main" script] args))
                   (.directory root) (.redirectErrorStream true)))
        output (slurp (.getInputStream p))]
    {:exit (.waitFor p) :output output}))
(defn invoke [root args]
  (invoke-script root "tools/stdlib_task_projection.clj" args))

(deftest fixed-cli-boundary-and-symlink-escape
  (let [temp (.toFile (java.nio.file.Files/createTempDirectory "stdlib-projection-" (make-array java.nio.file.attribute.FileAttribute 0)))
        root (io/file temp "checkout")
        task-file (io/file root stdlib-task-projection/task-path)
        roadmap-path stdlib-task-projection/roadmap-path
        sentinel (io/file temp "outside.md")]
    (try
      (doseq [path (concat ["tools/task_projection.clj" "tools/task_projection_links.clj" "tools/stdlib_task_projection.clj"
                           stdlib-task-projection/task-path roadmap-path]
                    (for [row (stdlib-task-projection/records tasks)
                          [_ _ target] (re-seq stdlib-task-projection/link-pattern (:evidence row))]
                      (str (stdlib-task-projection/relative-target stdlib-task-projection/task-path target))))]
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
      (let [link (io/file root "specs/stdlib-workflow/escape.md")]
        (java.nio.file.Files/createSymbolicLink (.toPath link) (.toPath sentinel) (make-array java.nio.file.attribute.FileAttribute 0))
        (spit task-file (str/replace-first tasks "Status: Open" "Status: Partial; [escape](escape.md)"))
        (let [before (snapshot root)]
          (is (not= 0 (:exit (invoke root ["--write"]))))
          (is (= before (snapshot root)))))
      (is (= "Outside write boundary" (slurp sentinel)))
      (finally (doseq [f (reverse (file-seq temp))] (io/delete-file f))))))

(deftest grouped-adapter-id-is-one-schema-leaf
  (let [rows (into {} (map (juxt :id identity) (stdlib-task-projection/records tasks)))
        legacy "stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters"
        schema (rows legacy)]
    (is (= "Complete schema and serialization adapters" (:title schema)))
    (is (str/includes? (:acceptance schema) "Own the STD10 public library boundary"))
    (is (not (str/includes? (:acceptance schema) "STD11")))
    (is (str/includes? (:acceptance (rows "stdlib/remaining-tasks-and-evidence/implement-query-and-transaction-adapters")) "commit, rollback, retry"))
    (is (rejects? #(stdlib-task-projection/records
                    (str/replace tasks "Complete schema and serialization adapters" "Complete schema/query/workflow/AI/meta/crypto/UI adapters"))))
    (is (rejects? #(stdlib-task-projection/records
                    (str/replace tasks "`stdlib/remaining-tasks-and-evidence/implement-ai-provider-and-policy-adapters`" "`stdlib/remaining-tasks-and-evidence/implement-unknown-adapters`"))))))

(let [r (run-tests)] (when (pos? (+ (:fail r) (:error r))) (System/exit 1)))
