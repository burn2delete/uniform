(load-file "tools/platform_task_projection.clj")
(require '[clojure.test :refer [deftest is run-tests]] '[clojure.string :as str]
         '[clojure.java.io :as io])
(def tasks (slurp platform-task-projection/task-path))
(def roadmap (slurp platform-task-projection/roadmap-path))
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest canonical-records-and-generated-boundaries
  (let [rows (platform-task-projection/records tasks)]
    (is (= 4 (count rows)))
    (is (= (set platform-task-projection/project-order) (set (map :id rows))))
    (is (every? #(= "Open" (:status %)) rows))
    (is (platform-task-projection/check-projection tasks roadmap))
    (is (= roadmap (platform-task-projection/project tasks roadmap)))
    (is (= roadmap (platform-task-projection/project (str tasks "\n- [x] T999 Historical only\n") roadmap))))
  (let [changed (str/replace-first tasks "Status: Open" "Status: Partial; source evidence")
        projected (platform-task-projection/project changed roadmap)]
    (is (rejects? #(platform-task-projection/check-projection changed roadmap)))
    (is (str/includes? projected "| Project / key | Status | Declared completion and remaining work | Source basis |"))
    (is (str/includes? projected "Partial; source evidence"))
    (doseq [part [:prefix :suffix]]
      (is (= (part (task-projection/region roadmap "platform-task-projection"))
             (part (task-projection/region projected "platform-task-projection"))))))
  (let [accepted (-> tasks (str/replace-first "- [ ]" "- [x]")
                     (str/replace-first "Status: Open" "Status: Accepted; [receipt](https://example.com/review)"))]
    (is (= 4 (count (platform-task-projection/records accepted))))
    (is (not (str/includes? (platform-task-projection/project accepted roadmap) "All projects are proposed and open.")))))

(deftest identity-and-structure-rejections
  (doseq [bad [(str/replace-first tasks "- [ ]" "- [x]")
               (str/replace-first tasks "Status: Open" "Status: Accepted; receipt")
               (str/replace-first tasks "Declared completion and remaining work:" "Evidence:")
               (str/replace-first tasks "Hardware / `hardware`" "Unsupported renamed project")
               (str/replace-first tasks "hardware" "platform/unknown")
               (str/replace tasks "firmware" "hardware")
               (str/replace tasks "<!-- platform-implementation-tasks:end -->" "")
               (str tasks "<!-- platform-implementation-tasks:start -->")
               "<!-- platform-implementation-tasks:end -->\n<!-- platform-implementation-tasks:start -->"]]
    (is (rejects? #(platform-task-projection/records bad))))
  (doseq [bad [(str/replace roadmap "<!-- platform-task-projection:end -->" "")
               (str roadmap "<!-- platform-task-projection:start -->")
               "<!-- platform-task-projection:end -->\n<!-- platform-task-projection:start -->"]]
    (is (rejects? #(platform-task-projection/project tasks bad)))))

(deftest relative-link-provenance
  (doseq [row (platform-task-projection/records tasks)]
    (let [projected (platform-task-projection/rebase-links (:evidence row)
                       platform-task-projection/task-path platform-task-projection/roadmap-path)
          sources (re-seq platform-task-projection/link-pattern (:evidence row))
          destinations (re-seq platform-task-projection/link-pattern projected)]
      (is (= (count sources) (count destinations)))
      (doseq [[[_ _ target] [_ _ destination]] (map vector sources destinations)]
        (is (= (platform-task-projection/relative-target platform-task-projection/task-path target)
               (platform-task-projection/relative-target platform-task-projection/roadmap-path destination))))))
  (is (str/includes? tasks "(../../docs/phase-09-domain-specific-computing-coverage/124-dom1-hardware-computing-domain-specification.md)"))
  (is (str/includes? roadmap "(../phase-09-domain-specific-computing-coverage/124-dom1-hardware-computing-domain-specification.md)"))
  (doseq [link ["[x](../../missing-file.md)" "[x](../../../outside.md)" "[x](/etc/passwd)"
                "[x](//example.com/path)" "[x](http://example.com/path)" "[x](#anchor)"
                "[x](tasks.md#missing)" "[x](tasks.md?query)" "[x](%2e%2e/path)"
                "![x](tasks.md)" "[x][ref]" "<a>" "[x](https://user@example.com/a)"]]
    (is (rejects? #(platform-task-projection/project
                    (str/replace-first tasks "Status: Open" (str "Status: Partial; " link)) roadmap)) link)))

(defn snapshot [root]
  (into {} (for [f (file-seq root) :when (.isFile f)]
             [(str (.relativize (.toPath root) (.toPath f))) (slurp f)])))

(deftest whole-product-scope-is-independent
  (let [scope (platform-task-projection/product-scope tasks)
        rows (platform-task-projection/records tasks)]
    (is (= 4 (count rows)))
    (is (str/includes? scope "semantic key `platform-product`."))
    (is (str/includes? scope "Each task is open."))
    (is (str/includes? (str/replace scope #"\s+" " ")
                      "no fabrication order, device purchase or external service action is authorized")))
  (let [changed (str/replace tasks "The full goal also includes" "The full goal also includes explicitly")
        projected (platform-task-projection/project changed roadmap)
        old-table (:body (task-projection/region roadmap "platform-task-projection"))
        new-table (:body (task-projection/region projected "platform-task-projection"))]
    (is (rejects? #(platform-task-projection/check-projection changed roadmap)))
    (is (str/includes? projected "The full goal also includes explicitly"))
    (is (= old-table new-table)))
  (doseq [bad [(str/replace tasks "<!-- platform-product-scope:end -->" "")
               (str tasks "<!-- platform-product-scope:start -->")
               (str/replace tasks "semantic key `platform-product`." "semantic key `unknown`.")
               (str/replace-first tasks #"(?m)^\| ISA and hardware implementation[^\n]+\n" "")
               (str/replace tasks "After these records are adopted, remaining product tasks are:" "Conditional work:")
               (str/replace tasks "External prerequisites are not language features:" "Dependencies:")
               (str/replace tasks "Product acceptance is yes/no:" "Product questions:")]]
    (is (rejects? #(platform-task-projection/project bad roadmap))))
  (doseq [bad [(str/replace roadmap "<!-- platform-product-projection:end -->" "")
               (str roadmap "<!-- platform-product-projection:start -->")]]
    (is (rejects? #(platform-task-projection/project tasks bad)))))
(defn invoke-script [root script args]
  (let [p (.start (doto (ProcessBuilder. ^java.util.List
                         (into [(str (io/file (System/getProperty "java.home") "bin/java"))
                                "-Xmx128m" "-cp" (System/getProperty "java.class.path")
                                "clojure.main" script] args))
                   (.directory root) (.redirectErrorStream true)))
        output (slurp (.getInputStream p))]
    {:exit (.waitFor p) :output output}))
(defn invoke [root args]
  (invoke-script root "tools/platform_task_projection.clj" args))

(deftest fixed-cli-boundary-and-symlink-escape
  (let [temp (.toFile (java.nio.file.Files/createTempDirectory "platform-projection-" (make-array java.nio.file.attribute.FileAttribute 0)))
        root (io/file temp "checkout")
        task-file (io/file root platform-task-projection/task-path)
        roadmap-path platform-task-projection/roadmap-path
        sentinel (io/file temp "outside.md")]
    (try
      (doseq [path (concat ["tools/task_projection.clj" "tools/task_projection_links.clj" "tools/platform_task_projection.clj"
                           platform-task-projection/task-path roadmap-path]
                    (for [row (platform-task-projection/records tasks)
                          [_ _ target] (re-seq platform-task-projection/link-pattern (:evidence row))]
                      (str (platform-task-projection/relative-target platform-task-projection/task-path target))))]
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
      (let [link (io/file root "specs/platform-workflow/escape.md")]
        (java.nio.file.Files/createSymbolicLink (.toPath link) (.toPath sentinel) (make-array java.nio.file.attribute.FileAttribute 0))
        (spit task-file (str/replace-first tasks "Status: Open" "Status: Partial; [escape](escape.md)"))
        (let [before (snapshot root)]
          (is (not= 0 (:exit (invoke root ["--write"]))))
          (is (= before (snapshot root)))))
      (is (= "Outside write boundary" (slurp sentinel)))
      (finally (doseq [f (reverse (file-seq temp))] (io/delete-file f))))))

(let [r (run-tests)] (when (pos? (+ (:fail r) (:error r))) (System/exit 1)))
