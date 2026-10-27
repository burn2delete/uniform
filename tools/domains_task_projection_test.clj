(load-file "tools/domains_task_projection.clj")
(require '[clojure.test :refer [deftest is run-tests]]
         '[clojure.string :as str] '[clojure.java.io :as io])
(def tasks (slurp domains-task-projection/task-path))
(def roadmap (slurp domains-task-projection/roadmap-path))
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest literal-projects-and-complete-blocks
  (let [rows (domains-task-projection/records tasks)]
    (is (= 7 (count rows)))
    (is (= domains-task-projection/outcome-order (mapv :id rows)))
    (is (every? #(= "Open" (:status %)) rows))
    (is (domains-task-projection/check-projection tasks roadmap))
    (is (= roadmap (domains-task-projection/project tasks roadmap)))
    (is (= roadmap (domains-task-projection/project (str tasks "\n- [x] T999 History only\n") roadmap)))
    (doseq [{:keys [id title projection]} rows]
      (is (= title (get domains-task-projection/outcome-headings id)))
      (is (str/starts-with? projection "Outcome:"))
      (is (str/includes? projection "Yes/no acceptance:")))
    (doseq [row (take 2 rows)]
      (is (str/includes? (:projection row) "\n\nYes/no acceptance:")))))

(deftest explicit-status-keeps-aggregate-open
  (let [changed (str/replace-first tasks "Status: Open" "Status: Partial; source evidence")
        projected (domains-task-projection/project changed roadmap)]
    (is (rejects? #(domains-task-projection/check-projection changed roadmap)))
    (is (str/includes? projected "Remaining tasks (implementation status: Partial; source evidence):"))
    (is (= projected (domains-task-projection/project changed projected)))
    (is (str/includes? projected "The aggregate outcome is open")))
  (let [accepted (-> tasks (str/replace-first "- [ ]" "- [x]")
                     (str/replace-first "Status: Open" "Status: Accepted; [review](https://example.com/review)"))]
    (is (= 7 (count (domains-task-projection/records accepted))))
    (is (str/includes? (domains-task-projection/project accepted roadmap)
                      "The aggregate outcome is open"))))

(deftest malformed-identities-and-regions
  (doseq [bad [(str/replace-first tasks "`math` Math" "`unknown` Math")
               (str/replace tasks "`schema` Schema" "`math` Schema")
               (str/replace-first tasks "`math` Math" "`math` Changed")
               (str/replace-first tasks "- [ ]" "- [x]")
               (str/replace-first tasks "Projected task block:" "Projection:")
               (str/replace-first tasks "Yes/no acceptance:" "Questions:")
               (str/replace-first tasks "Integration:" "Consumers:")
               (str/replace-first tasks "Decision owner:" "Decision role:")
               (str/replace tasks "<!-- domains-implementation-tasks:end -->" "")
               (str tasks "<!-- domains-implementation-tasks:start -->")
               "<!-- domains-implementation-tasks:end -->\n<!-- domains-implementation-tasks:start -->"]]
    (is (rejects? #(domains-task-projection/records bad))))
  (doseq [id domains-task-projection/outcome-order
          :let [marker (str "domains-task-projection/" id)]]
    (doseq [bad [(str/replace roadmap (str "<!-- " marker ":end -->") "")
                 (str roadmap "<!-- " marker ":start -->")
                 (str "<!-- " marker ":end -->\n<!-- " marker ":start -->")]]
      (is (rejects? #(domains-task-projection/project tasks bad))))))

(deftest relative-math-link-and-strict-markdown
  (let [rows (domains-task-projection/records tasks)]
    (is (= 1 (count (mapcat #(re-seq domains-task-projection/link-pattern (:projection %)) rows))))
    (doseq [{:keys [projection]} rows
            [[_ _ source] [_ _ target]] (map vector
              (re-seq domains-task-projection/link-pattern projection)
              (re-seq domains-task-projection/link-pattern
                (domains-task-projection/rebase-links projection domains-task-projection/task-path domains-task-projection/roadmap-path)))]
      (is (= (domains-task-projection/relative-target domains-task-projection/task-path source)
             (domains-task-projection/relative-target domains-task-projection/roadmap-path target)))))
  (doseq [link ["[x](../../missing.md)" "[x](../../../outside.md)" "[x](/etc/passwd)"
                "[x](http://example.com/a)" "[x](https://user@example.com/a)"
                "[x](#anchor)" "[x](tasks.md#anchor)" "[x](tasks.md?query)"
                "[x](%2e%2e/path)" "![x](tasks.md)" "[x][ref]" "<a>"]]
    (is (rejects? #(domains-task-projection/project
                    (str/replace-first tasks "Status: Open" (str "Status: Partial; " link)) roadmap)))))
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
  (let [temp (.toFile (java.nio.file.Files/createTempDirectory "domains-projection-" (make-array java.nio.file.attribute.FileAttribute 0)))
        root (io/file temp "checkout") script "tools/domains_task_projection.clj"
        task-file (io/file root domains-task-projection/task-path)
        target domains-task-projection/roadmap-path sentinel (io/file temp "outside.md")]
    (try
      (doseq [path (concat [script "tools/task_projection.clj" "tools/task_projection_links.clj"
                           domains-task-projection/task-path target]
                    (for [row (domains-task-projection/records tasks)
                          [_ _ href] (re-seq task-projection-links/link-pattern (:projection row))]
                      (str (task-projection-links/relative-target "Domains" domains-task-projection/task-path href))))]
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
      (let [link (io/file root "specs/domains-workflow/escape.md")]
        (java.nio.file.Files/createSymbolicLink (.toPath link) (.toPath sentinel) (make-array java.nio.file.attribute.FileAttribute 0))
        (spit task-file (str/replace-first tasks "Status: Open" "Status: Partial; [escape](escape.md)"))
        (let [before (snapshot root)]
          (is (not= 0 (:exit (invoke root script ["--write"]))))
          (is (= before (snapshot root)))))
      (is (= "Outside write boundary" (slurp sentinel)))
      (finally (doseq [f (reverse (file-seq temp))] (io/delete-file f))))))

(let [result (run-tests)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
