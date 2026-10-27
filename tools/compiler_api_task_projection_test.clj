(load-file "tools/compiler_api_task_projection.clj")
(require '[clojure.test :refer [deftest is run-tests]]
         '[clojure.string :as str] '[clojure.java.io :as io])
(def tasks (slurp compiler-api-task-projection/task-path))
(def roadmap (slurp compiler-api-task-projection/roadmap-path))
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest exact-outcomes-and-status-boundaries
  (let [rows (compiler-api-task-projection/records tasks)]
    (is (= ["pipeline" "plugins" "pass-evidence" "development-boundaries"] (mapv :id rows)))
    (is (every? #(= "Open" (:status %)) rows))
    (is (compiler-api-task-projection/check-projection tasks roadmap))
    (is (= roadmap (compiler-api-task-projection/project tasks roadmap)))
    (is (= roadmap (compiler-api-task-projection/project
                     (str tasks "\n- [x] T999 Historical only\n") roadmap))))
  (let [changed (str/replace-first tasks "Status: Open" "Status: Partial; source evidence")
        projected (compiler-api-task-projection/project changed roadmap)]
    (is (rejects? #(compiler-api-task-projection/check-projection changed roadmap)))
    (is (str/includes? projected "Implementation task status: Partial; source evidence"))
    (is (= 2 (count (re-seq #"All tasks open" projected))))
    (is (str/includes? projected "all outcomes below remain open"))
    (is (= projected (compiler-api-task-projection/project changed projected))))
  (let [accepted (-> tasks (str/replace-first "- [ ]" "- [x]")
                     (str/replace-first "Status: Open" "Status: Accepted; [review](https://example.com/review)"))]
    (is (= 4 (count (compiler-api-task-projection/records accepted))))
    (is (str/includes? (compiler-api-task-projection/project accepted roadmap)
                      "all outcomes below remain open"))))

(deftest reject-identity-structure-and-unportable-fields
  (doseq [bad [(str/replace-first tasks "- [ ]" "- [x]")
               (str/replace-first tasks "Status: Open" "Status: Accepted; receipt")
               (str/replace-first tasks "`pipeline`" "`unknown`")
               (str/replace-first tasks "`plugins`" "`pipeline`")
               (str/replace-first tasks "`pipeline` Built-in" "`pipeline` Renamed")
               (str/replace-first tasks "Yes/no acceptance:" "Closing questions:")
               (str/replace-first tasks "| Task | Concrete closing evidence |" "| Task | Evidence |")
               (str/replace-first tasks #"(?m)^\| Stabilize public[^\n]+\n" "")
               (str/replace-first tasks "All tasks open" "All tasks done")
               (str/replace tasks "<!-- compiler-api-implementation-tasks:end -->" "")
               (str tasks "<!-- compiler-api-implementation-tasks:start -->")
               "<!-- compiler-api-implementation-tasks:end -->\n<!-- compiler-api-implementation-tasks:start -->"]]
    (is (rejects? #(compiler-api-task-projection/records bad))))
  (doseq [id compiler-api-task-projection/outcome-order]
    (doseq [bad [(str/replace roadmap (str "<!-- compiler-api-task-projection/" id ":end -->") "")
                 (str roadmap "<!-- compiler-api-task-projection/" id ":start -->")
                 (str "<!-- compiler-api-task-projection/" id ":end -->\n<!-- compiler-api-task-projection/" id ":start -->")]]
      (is (rejects? #(compiler-api-task-projection/project tasks bad)))))
  (doseq [link ["[x](../evidence.md)" "[x](../../missing.md)" "[x](/etc/passwd)"
                "[x](http://example.com/a)" "[x](https://user@example.com/a)"
                "[x](#anchor)" "![x](https://example.com/a)" "[x][ref]" "<a>"]]
    (is (rejects? #(compiler-api-task-projection/records
                    (str/replace-first tasks "Status: Open" (str "Status: Partial; " link)))))))

(deftest independent-regions-retain-all-other-text
  (doseq [id compiler-api-task-projection/outcome-order]
    (let [marker (str "compiler-api-task-projection/" id)
          before (task-projection/region roadmap marker)
          changed (str/replace-first tasks "Status: Open" "Status: Partial; source evidence")
          after (task-projection/region (compiler-api-task-projection/project changed roadmap) marker)]
      (when (= "pipeline" id)
        (is (= (:prefix before) (:prefix after)))
        (is (= (:suffix before) (:suffix after))))))
  (is (str/includes? tasks "## Historical workflow migration record"))
  (is (str/includes? tasks "- [x] T001 [US1]")))

(deftest new-development-boundary-is-one-six-facet-outcome
  (let [row (last (compiler-api-task-projection/records tasks))
        changed (str/replace tasks "Current acceptance unassessed" "Current acceptance accepted")]
    (is (= "development-boundaries" (:id row)))
    (is (= "Open" (:status row)))
    (is (str/includes? (:projection row) "historically completed implementation"))
    (is (str/includes? (:projection row) "cbc55cd1477deca08a9c042c91db98005833021a"))
    (is (rejects? #(compiler-api-task-projection/records changed)))
    (doseq [bad [(str/replace tasks #"(?m)^\| Verify representative ownership[^\n]+\n" "")
                 (str/replace tasks "| Publish semantic module and API ownership |"
                   "| Extra outcome | Extra evidence |\n| Publish semantic module and API ownership |")]]
      (is (rejects? #(compiler-api-task-projection/records bad))))))

(defn snapshot [root]
  (into {} (for [f (file-seq root) :when (.isFile f)]
             [(str (.relativize (.toPath root) (.toPath f))) (slurp f)])))
(defn invoke [root script args]
  (let [p (.start (doto (ProcessBuilder. ^java.util.List
                         (into [(str (io/file (System/getProperty "java.home") "bin/java"))
                                "-Xmx128m" "-cp" (System/getProperty "java.class.path")
                                "clojure.main" script] args))
                   (.directory root) (.redirectErrorStream true)))
        output (slurp (.getInputStream p))]
    {:exit (.waitFor p) :output output}))

(deftest fixed-write-cli-boundaries
  (let [root (.toFile (java.nio.file.Files/createTempDirectory "compiler-api-projection-"
                       (make-array java.nio.file.attribute.FileAttribute 0)))
        script "tools/compiler_api_task_projection.clj"
        target compiler-api-task-projection/roadmap-path
        task-file (io/file root compiler-api-task-projection/task-path)]
    (try
      (doseq [p [script "tools/task_projection.clj" compiler-api-task-projection/task-path target]]
        (io/make-parents (io/file root p)) (spit (io/file root p) (slurp p)))
      (let [before (snapshot root)]
        (is (= 0 (:exit (invoke root script ["--check"]))))
        (is (= before (snapshot root)))
        (is (= 0 (:exit (invoke root script []))))
        (is (= 0 (:exit (invoke root "tools/task_projection.clj" ["--write" "outside.md"]))))
        (is (= before (snapshot root)))
        (doseq [args [["--write" "outside.md"] ["--output" "outside.md"] ["--check" "--write"]]]
          (is (not= 0 (:exit (invoke root script args)))))
        (is (= before (snapshot root))))
      (spit task-file (str/replace-first tasks "Status: Open" "Status: Partial; source evidence"))
      (let [stale (snapshot root)]
        (is (not= 0 (:exit (invoke root script ["--check"]))))
        (is (= stale (snapshot root)))
        (is (= 0 (:exit (invoke root script ["--write"]))))
        (is (= #{target} (set (for [[p text] (snapshot root) :when (not= text (get stale p))] p))))
        (is (= 0 (:exit (invoke root script ["--check"])))))
      (spit task-file (str/replace-first tasks "`pipeline`" "`unknown`"))
      (let [invalid (snapshot root)]
        (is (not= 0 (:exit (invoke root script ["--write"]))))
        (is (= invalid (snapshot root))))
      (finally (doseq [f (reverse (file-seq root))] (.delete f))))))

(let [result (run-tests)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
