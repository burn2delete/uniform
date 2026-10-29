(load-file "tools/release_task_projection.clj")
(require '[clojure.test :refer [deftest is run-tests]] '[clojure.string :as str]
         '[clojure.java.io :as io] '[clojure.java.shell :as shell])
(def tasks (slurp release-task-projection/task-path))
(def roadmap (slurp release-task-projection/roadmap-path))
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest canonical-records-and-generated-boundaries
  (let [rows (release-task-projection/records tasks)]
    (is (= 17 (count rows)))
    (is (= release-task-projection/semantic-ids (set (map :id rows))))
    (is (every? #(= "Open" (:status %)) rows))
    (is (release-task-projection/check-projection tasks roadmap))
    (is (= roadmap (release-task-projection/project tasks roadmap)))
    (is (= roadmap (release-task-projection/project (str tasks "\n- [x] T999 Historical only\n") roadmap))))
  (let [changed (str/replace-first tasks "Status: Open" "Status: Partial; source evidence")
        projected (release-task-projection/project changed roadmap)]
    (is (rejects? #(release-task-projection/check-projection changed roadmap)))
    (is (str/includes? projected "| Task | Status | Evidence baseline | Closing evidence |"))
    (is (str/includes? projected "Partial; source evidence"))
    (doseq [part [:prefix :suffix]]
      (is (= (part (task-projection/region roadmap "release-task-projection"))
             (part (task-projection/region projected "release-task-projection"))))))
  (let [accepted (-> tasks (str/replace-first "- [ ]" "- [x]")
                     (str/replace-first "Status: Open" "Status: Accepted; [receipt](https://example.com/review)"))]
    (is (= 17 (count (release-task-projection/records accepted))))
    (is (not (str/includes? (release-task-projection/project accepted roadmap) "Task (all open)")))))

(deftest identity-and-structure-rejections
  (doseq [bad [(str/replace-first tasks "- [ ]" "- [x]")
               (str/replace-first tasks "Status: Open" "Status: Accepted; receipt")
               (str/replace-first tasks "Evidence baseline:" "Evidence:")
               (str/replace-first tasks "Bind each claimed component to exact implementation and conformance" "Unsupported renamed task")
               (str/replace-first tasks "release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance" "release/unknown")
               (str/replace tasks "release/remaining-tasks-and-evidence/complete-stage-compatibilityequivalence-across-the-claimed-compiler" "release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance")
               (str/replace tasks "<!-- release-implementation-tasks:end -->" "")
               (str tasks "<!-- release-implementation-tasks:start -->")
               "<!-- release-implementation-tasks:end -->\n<!-- release-implementation-tasks:start -->"]]
    (is (rejects? #(release-task-projection/records bad))))
  (doseq [bad [(str/replace roadmap "<!-- release-task-projection:end -->" "")
               (str roadmap "<!-- release-task-projection:start -->")
               "<!-- release-task-projection:end -->\n<!-- release-task-projection:start -->"]]
    (is (rejects? #(release-task-projection/project tasks bad)))))

(deftest rebuild-executor-remains-in-existing-third-record
  (let [rows (release-task-projection/records tasks) row (nth rows 2)]
    (is (= 17 (count rows)))
    (is (= "release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution" (:id row)))
    (is (= "Open" (:status row)))
    (doseq [required ["prior Gravity compiler process" "complete declared next-stage" "accepted and rejected compiler inputs"
                      "without Clojure" "logs/lineage" "public check/run/compile" "absent seed boundaries"
                      "SH27 equivalence" "supplied rebuild records or plans do not prove execution"]]
      (is (str/includes? (:acceptance row) required)))))

(deftest governance-consumers-reference-existing-open-record
  (let [rows (release-task-projection/records tasks)
        row (first rows)
        plan (slurp "specs/release-workflow/plan.md")
        historical (shell/sh "git" "show"
                            "f1cb35013c9a132cda4557f7806616b19af0f3ed:specs/release-workflow/tasks.md")
        _ (is (zero? (:exit historical)))
        pinned-row (with-redefs [release-task-projection/semantic-ids (set (map :id (take 5 rows)))]
                     (first (release-task-projection/records (:out historical))))]
    (is (= 17 (count rows)))
    (is (= "release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance" (:id row)))
    (is (= "Open" (:status row)))
    (is (= (dissoc pinned-row :acceptance) (dissoc row :acceptance)))
    (is (str/starts-with? (:acceptance row) (:acceptance pinned-row)))
    (doseq [required ["all 240 original contracts" "missing producer/checker/consumer interface"
                      "an accountability link does not establish an implementation interface"
                      "bind actual component evidence" "does not block ordinary semantic development"]]
      (is (str/includes? (:acceptance row) required)))
    (is (str/includes? plan (:id row)))
    (doseq [required ["Ownerless changes" "Missing required sections" "Accidental experimental use"
                      "removal before the approved window" "on/off fixtures" "expiry, rollback"
                      "broader evolution workflow" "FL-P17-T01" "does not amend task acceptance"]]
      (is (str/includes? plan required)))
    (doseq [id ["231-gov1-language-evolution-process" "236-gov6-rfc-process"
                "237-gov7-experimental-feature-policy" "238-gov8-deprecation-and-stabilization-policy"]]
      (is (str/includes? plan (str "../../docs/phase-17-governance-and-evolution/" id ".md#conformance-criteria"))))
    (is (str/includes? plan "no candidate requirement gains implementation credit"))
    (is (release-task-projection/check-projection tasks roadmap))))

(deftest relative-link-provenance
  (doseq [row (release-task-projection/records tasks)]
    (let [projected (release-task-projection/rebase-links (:evidence row)
                       release-task-projection/task-path release-task-projection/roadmap-path)
          sources (re-seq release-task-projection/link-pattern (:evidence row))
          destinations (re-seq release-task-projection/link-pattern projected)]
      (is (= (count sources) (count destinations)))
      (doseq [[[_ _ target] [_ _ destination]] (map vector sources destinations)]
        (is (= (release-task-projection/relative-target release-task-projection/task-path target)
               (release-task-projection/relative-target release-task-projection/roadmap-path destination))))))
  (is (str/includes? tasks "(../../docs/implementation-roadmap.md)"))
  (is (str/includes? roadmap "(../implementation-roadmap.md)"))
  (doseq [link ["[x](../../missing-file.md)" "[x](../../../outside.md)" "[x](/etc/passwd)"
                "[x](//example.com/path)" "[x](http://example.com/path)" "[x](#anchor)"
                "[x](tasks.md#missing)" "[x](tasks.md?query)" "[x](%2e%2e/path)"
                "![x](tasks.md)" "[x][ref]" "<a>" "[x](https://user@example.com/a)"]]
    (is (rejects? #(release-task-projection/project
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
  (invoke-script root "tools/release_task_projection.clj" args))

(deftest fixed-cli-boundary-and-symlink-escape
  (let [temp (.toFile (java.nio.file.Files/createTempDirectory "release-projection-" (make-array java.nio.file.attribute.FileAttribute 0)))
        root (io/file temp "checkout")
        task-file (io/file root release-task-projection/task-path)
        roadmap-path release-task-projection/roadmap-path
        sentinel (io/file temp "outside.md")]
    (try
      (doseq [path (concat ["tools/task_projection.clj" "tools/task_projection_links.clj" "tools/release_task_projection.clj"
                           release-task-projection/task-path roadmap-path]
                    (for [row (release-task-projection/records tasks)
                          [_ _ target] (re-seq release-task-projection/link-pattern (:evidence row))]
                      (str (release-task-projection/relative-target release-task-projection/task-path target))))]
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
      (let [link (io/file root "specs/release-workflow/escape.md")]
        (java.nio.file.Files/createSymbolicLink (.toPath link) (.toPath sentinel) (make-array java.nio.file.attribute.FileAttribute 0))
        (spit task-file (str/replace-first tasks "Status: Open" "Status: Partial; [escape](escape.md)"))
        (let [before (snapshot root)]
          (is (not= 0 (:exit (invoke root ["--write"]))))
          (is (= before (snapshot root)))))
      (is (= "Outside write boundary" (slurp sentinel)))
      (finally (doseq [f (reverse (file-seq temp))] (io/delete-file f))))))

(let [r (run-tests)] (when (pos? (+ (:fail r) (:error r))) (System/exit 1)))
