(load-file "tools/build_task_projection.clj")
(require '[clojure.test :refer [deftest is run-tests]] '[clojure.string :as str] '[clojure.edn :as edn]
         '[clojure.java.io :as io] '[clojure.java.shell :as shell])
(def tasks (slurp build-task-projection/task-path))
(def roadmap (slurp build-task-projection/roadmap-path))
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(defn interface-sha [text]
  (format "%064x" (java.math.BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-256") (.getBytes text "UTF-8")))))
(defn check-interface [a task-text source-text]
  (let [before (:before a) after (:after a) ref (:task-reference after)
        ids #{"facet/ai-build-tools/pkg5/1694-1763" "facet/ai-build-tools/pkg5/2238-2273"
              "facet/ai-build-tools/pkg5/3538-3616" "facet/ai-build-tools/pkg5/4019-4066"}
        source-bytes (.getBytes source-text "UTF-8")
        row (first (build-task-projection/records task-text))]
    (when-not (and (= [:task-reference :task-gap] (:allowed-fields a))
                   (= "behavior/pkg5/yank-revocation-admission" (:requirement-id a) (:id before) (:id after))
                   (= (dissoc before :task-reference :task-gap) (dissoc after :task-reference :task-gap))
                   (= (:task-gap before) (get-in after [:task-gap :prior-gap]))
                   (= 4 (count (:source-facets a)))
                   (= ids (set (:source-facet-ids before)) (set (map :id (:source-facets a))))
                   (= "build-implementation-tasks" (:marker ref))
                   (= build-task-projection/task-path (:path ref))
                   (= (:id row) (:id ref)) (= "Open" (:status row))
                   (= (:sha256 ref) (interface-sha task-text))
                   (= (get-in a [:source-document :sha256]) (interface-sha source-text)))
      (throw (ex-info "Changed task-interface identity, fields or source hash" {})))
    (doseq [f (:source-facets a)]
      (when-not (= (:raw-sha256 f)
                   (interface-sha (String. (java.util.Arrays/copyOfRange source-bytes (:start-byte f) (:end-byte f)) "UTF-8")))
        (throw (ex-info "Changed source facet span" {}))))
    (doseq [term ["yank/revocation policy" "signature/revocation status" "historical rebuild"
                  "PKG5006" "without silently replacing or erasing locked history"]]
      (when-not (str/includes? (:acceptance row) term)
        (throw (ex-info "Missing yank/revocation acceptance predicate" {}))))
    true))

(deftest exact-yank-interface-and-field-conservation
  (let [a (edn/read-string (slurp "specs/build-workflow/yank-interface.edn"))
        source (slurp (get-in a [:source-document :path]))
        historical (shell/sh "git" "show" (str (:task-source-commit a) ":" build-task-projection/task-path))
        _ (is (zero? (:exit historical)))
        pinned-tasks (:out historical)
        prior-row (first (build-task-projection/records pinned-tasks))
        current-row (first (build-task-projection/records tasks))]
    ;; This immutable receipt binds its original complete file, not every later
    ;; task amendment. Preserve the actual current yank criterion separately.
    (is (check-interface a pinned-tasks source))
    (is (= (select-keys prior-row [:id :title :status :evidence])
           (select-keys current-row [:id :title :status :evidence])))
    (is (str/starts-with? (:acceptance current-row) (:acceptance prior-row)))
    (is (= :unassessed (get-in a [:conservation :implementation-acceptance])))
    (doseq [bad [(assoc a :allowed-fields [:task-reference :task-gap :atomicity])
                 (assoc-in a [:after :atomicity] :accepted)
                 (assoc-in a [:after :id] "invented")
                 (assoc-in a [:after :task-reference :marker] "other-tasks")
                 (assoc-in a [:after :task-reference :id] "build/unknown")
                 (update a :source-facets conj (first (:source-facets a)))
                 (assoc-in a [:after :task-reference :sha256] (apply str (repeat 64 "0")))
                 (assoc-in a [:source-facets 0 :raw-sha256] (apply str (repeat 64 "0")))]]
      (is (rejects? #(check-interface bad pinned-tasks source))))
    (is (rejects? #(check-interface a (str pinned-tasks "\nchanged") source)))
    (is (rejects? #(check-interface a pinned-tasks (str source "\nchanged"))))))

(deftest canonical-records-and-generated-boundaries
  (let [rows (build-task-projection/records tasks)]
    (is (= 5 (count rows)))
    (is (= build-task-projection/semantic-ids (set (map :id rows))))
    (is (every? #(= "Open" (:status %)) rows))
    (is (build-task-projection/check-projection tasks roadmap))
    (is (= roadmap (build-task-projection/project tasks roadmap)))
    (is (= roadmap (build-task-projection/project (str tasks "\n- [x] T999 Historical only\n") roadmap))))
  (let [changed (str/replace-first tasks "Status: Open" "Status: Partial; source evidence")
        projected (build-task-projection/project changed roadmap)]
    (is (rejects? #(build-task-projection/check-projection changed roadmap)))
    (is (str/includes? projected "| Task | Status | Evidence baseline | Closing evidence |"))
    (is (str/includes? projected "Partial; source evidence"))
    (doseq [part [:prefix :suffix]]
      (is (= (part (task-projection/region roadmap "build-task-projection"))
             (part (task-projection/region projected "build-task-projection"))))))
  (let [accepted (-> tasks (str/replace-first "- [ ]" "- [x]")
                     (str/replace-first "Status: Open" "Status: Accepted; [receipt](https://example.com/review)"))]
    (is (= 5 (count (build-task-projection/records accepted))))
    (is (not (str/includes? (build-task-projection/project accepted roadmap) "Task (all open)")))))

(deftest identity-and-structure-rejections
  (doseq [bad [(str/replace-first tasks "- [ ]" "- [x]")
               (str/replace-first tasks "Status: Open" "Status: Accepted; receipt")
               (str/replace-first tasks "Evidence baseline:" "Evidence:")
               (str/replace-first tasks "Implement project/lock normalization and constraint resolution" "Unsupported renamed task")
               (str/replace-first tasks "build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution" "build/unknown")
               (str/replace tasks "build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects" "build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution")
               (str/replace tasks "<!-- build-implementation-tasks:end -->" "")
               (str tasks "<!-- build-implementation-tasks:start -->")
               "<!-- build-implementation-tasks:end -->\n<!-- build-implementation-tasks:start -->"]]
    (is (rejects? #(build-task-projection/records bad))))
  (doseq [bad [(str/replace roadmap "<!-- build-task-projection:end -->" "")
               (str roadmap "<!-- build-task-projection:start -->")
               "<!-- build-task-projection:end -->\n<!-- build-task-projection:start -->"]]
    (is (rejects? #(build-task-projection/project tasks bad)))))

(deftest relative-link-provenance
  (doseq [row (build-task-projection/records tasks)]
    (let [projected (build-task-projection/rebase-links (:evidence row)
                       build-task-projection/task-path build-task-projection/roadmap-path)
          sources (re-seq build-task-projection/link-pattern (:evidence row))
          destinations (re-seq build-task-projection/link-pattern projected)]
      (is (= (count sources) (count destinations)))
      (doseq [[[_ _ target] [_ _ destination]] (map vector sources destinations)]
        (is (= (build-task-projection/relative-target build-task-projection/task-path target)
               (build-task-projection/relative-target build-task-projection/roadmap-path destination))))))
  (is (str/includes? tasks "(../../docs/phase-12-build-package-and-artifact-system/166-pkg2-build-system-architecture.md)"))
  (is (str/includes? roadmap "(../phase-12-build-package-and-artifact-system/166-pkg2-build-system-architecture.md)"))
  (doseq [link ["[x](../../missing-file.md)" "[x](../../../outside.md)" "[x](/etc/passwd)"
                "[x](//example.com/path)" "[x](http://example.com/path)" "[x](#anchor)"
                "[x](tasks.md#missing)" "[x](tasks.md?query)" "[x](%2e%2e/path)"
                "![x](tasks.md)" "[x][ref]" "<a>" "[x](https://user@example.com/a)"]]
    (is (rejects? #(build-task-projection/project
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
  (invoke-script root "tools/build_task_projection.clj" args))

(deftest fixed-cli-boundary-and-symlink-escape
  (let [temp (.toFile (java.nio.file.Files/createTempDirectory "build-projection-" (make-array java.nio.file.attribute.FileAttribute 0)))
        root (io/file temp "checkout")
        task-file (io/file root build-task-projection/task-path)
        roadmap-path build-task-projection/roadmap-path
        sentinel (io/file temp "outside.md")]
    (try
      (doseq [path (concat ["tools/task_projection.clj" "tools/task_projection_links.clj" "tools/build_task_projection.clj"
                           build-task-projection/task-path roadmap-path]
                    (for [row (build-task-projection/records tasks)
                          field [(:evidence row) (:acceptance row)]
                          [_ _ target] (re-seq build-task-projection/link-pattern field)]
                      (str (build-task-projection/relative-target build-task-projection/task-path target))))]
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
      (let [link (io/file root "specs/build-workflow/escape.md")]
        (java.nio.file.Files/createSymbolicLink (.toPath link) (.toPath sentinel) (make-array java.nio.file.attribute.FileAttribute 0))
        (spit task-file (str/replace-first tasks "Status: Open" "Status: Partial; [escape](escape.md)"))
        (let [before (snapshot root)]
          (is (not= 0 (:exit (invoke root ["--write"]))))
          (is (= before (snapshot root)))))
      (is (= "Outside write boundary" (slurp sentinel)))
      (finally (doseq [f (reverse (file-seq temp))] (io/delete-file f))))))

(let [r (run-tests)] (when (pos? (+ (:fail r) (:error r))) (System/exit 1)))
