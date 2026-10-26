(ns task-projection-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.string :as str]
            [clojure.java.io :as io]))

(load-file "tools/task_projection.clj")

(def options
  {:component "Fixture"
   :task-marker "fixture-tasks"
   :projection-marker "fixture-table"
   :record-pattern #"(?s)^- \[([ x])\] `(fixture/[a-z-]+)` ([^\n|]+)\n\nStatus: ([^\n|]+)\n\nDone: ([^\n|]+)$"
   :semantic-ids #{"fixture/first" "fixture/second"}
   :table-header "\n| Task | Status | Done |\n| --- | --- | --- |\n"})
(def tasks
  (str "Historical introduction.\n<!-- fixture-tasks:start -->\n\n"
       "- [ ] `fixture/first` First\n\nStatus: Open; first evidence\n\nDone: First outcome\n\n"
       "- [ ] `fixture/second` Second\n\nStatus: Open; second evidence\n\nDone: Second outcome\n\n"
       "<!-- fixture-tasks:end -->\n- [x] T999 Historical migration only.\n"))
(def roadmap "Before.\n<!-- fixture-table:start -->\nold table\n<!-- fixture-table:end -->\nAfter.\n")
(defn rejects? [f] (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest generated-region-and-history-boundaries
  (let [generated (task-projection/project options tasks roadmap)
        old (task-projection/region roadmap "fixture-table")
        new (task-projection/region generated "fixture-table")]
    (is (= (:prefix old) (:prefix new)))
    (is (= (:suffix old) (:suffix new)))
    (is (= 2 (count (task-projection/records options tasks))))
    (is (= generated (task-projection/project options tasks generated)))
    (is (task-projection/check-projection options tasks generated))
    (is (rejects? #(task-projection/check-projection options tasks roadmap))))
  (doseq [text ["<!-- fixture-tasks:start -->"
                "<!-- fixture-tasks:end -->\n<!-- fixture-tasks:start -->"
                (str tasks "<!-- fixture-tasks:start -->")]]
    (is (rejects? #(task-projection/records options text))))
  (doseq [text ["<!-- fixture-table:start -->"
                "<!-- fixture-table:end -->\n<!-- fixture-table:start -->"
                (str roadmap "<!-- fixture-table:end -->")]]
    (is (rejects? #(task-projection/project options tasks text)))))

(deftest record-identities-and-explicit-acceptance
  (doseq [text [(str/replace tasks "`fixture/second`" "`fixture/first`")
                (str/replace tasks "`fixture/second`" "`fixture/unknown`")
                (str/replace-first tasks "Status:" "State:")
                (str/replace-first tasks "- [ ]" "- [x]")
                (str/replace-first tasks "Open; first evidence" "Accepted; receipt")
                (str/replace tasks "`fixture/second` Second" "`fixture/second` First")]]
    (is (rejects? #(task-projection/records options text))))
  (let [accepted (-> tasks (str/replace-first "- [ ]" "- [x]")
                    (str/replace-first "Open; first evidence" "Accepted; receipt"))
        renamed (str/replace tasks "`fixture/first` First" "`fixture/first` Revised")]
    (is (= 2 (count (task-projection/records options accepted))))
    ;; Reader does not impose the other components' title-derived-ID rule.
    (is (= 2 (count (task-projection/records options renamed))))
    (is (rejects? #(task-projection/records
                    (assoc options :title-id-prefix "fixture/" :title-id-error "Title mismatch") renamed)))))

(deftest link-policies-remain-distinct
  (is (nil? (task-projection/validate-links options "[receipt](https://example.com/evidence)")))
  (is (rejects? #(task-projection/validate-links options "set!")))
  (is (nil? (task-projection/validate-links (assoc options :allow-bang? true) "set!")))
  (doseq [profile [options (assoc options :allow-bang? true)]
          field ["[receipt](evidence.md)" "[receipt](#heading)"
                 "[receipt](/evidence.md)" "[receipt](//example.com/evidence)"
                 "[receipt](http://example.com/evidence)" "[receipt][ref]"
                 "[receipt](https://user@example.com/evidence)"
                 "![image](https://example.com/evidence)" "<a>receipt</a>"
                 "Reader -> Core"]]
    (is (rejects? #(task-projection/validate-links profile field)) field)))

(defn invoke-cli [root script args]
  (let [java (str (io/file (System/getProperty "java.home") "bin" "java"))
        command (into [java "-Xmx128m" "-cp" (System/getProperty "java.class.path")
                       "clojure.main" script] args)
        builder (doto (ProcessBuilder. ^java.util.List command)
                  (.directory root) (.redirectErrorStream true))
        process (.start builder)
        output (slurp (.getInputStream process))]
    {:exit (.waitFor process) :output output}))

(defn snapshot [root]
  (into (sorted-map)
        (for [file (file-seq root) :when (.isFile file)]
          [(str (.relativize (.toPath root) (.toPath file))) (slurp file)])))

(deftest wrappers-retain-fixed-path-cli-writes
  (doseq [component ["reader" "expand" "resolve" "core" "types" "effects"]]
    (let [temp (.toFile (java.nio.file.Files/createTempDirectory
                         "task-projection-test-" (make-array java.nio.file.attribute.FileAttribute 0)))
          root (io/file temp "checkout")
          script (str "tools/" component "_task_projection.clj")
          task-path (str "specs/" component "-workflow/tasks.md")
          roadmap-path (str "docs/component-roadmaps/" component ".md")
          sentinel (io/file temp "outside.md")]
      (try
        (.mkdirs root)
        (doseq [path [script "tools/task_projection.clj" task-path roadmap-path]]
          (io/make-parents (io/file root path))
          (spit (io/file root path) (slurp path)))
        (spit sentinel "Outside the fixed write boundary.\n")
        (let [before (snapshot root)
              checked (invoke-cli root script ["--check"])]
          (is (= 0 (:exit checked)) (str component " " (:output checked)))
          (is (= before (snapshot root))))
        (let [target (io/file root task-path)]
          (spit target (str/replace-first (slurp target) #"Current evidence/status: [^\n]+|Status: [^\n]+"
                                         (if (= component "expand")
                                           "Status: Partial; CLI fixture"
                                           "Current evidence/status: Partial; CLI fixture"))))
        (let [before (snapshot root)
              stale (invoke-cli root script ["--check"])
              written (invoke-cli root script ["--write"])
              after (snapshot root)]
          (is (not= 0 (:exit stale)))
          (is (= 0 (:exit written)) (str component " " (:output written)))
          (is (not= (get before roadmap-path) (get after roadmap-path)))
          (is (= (dissoc before roadmap-path) (dissoc after roadmap-path)))
          (is (= 0 (:exit (invoke-cli root script ["--check"])))))
        (let [before (snapshot root)]
          (is (not= 0 (:exit (invoke-cli root script ["--write" (.getPath sentinel)]))))
          (is (not= 0 (:exit (invoke-cli root script ["--output" (.getPath sentinel)]))))
          (is (= before (snapshot root)))
          (is (= "Outside the fixed write boundary.\n" (slurp sentinel)))
          ;; The shared implementation loads without interpreting CLI arguments.
          (is (= 0 (:exit (invoke-cli root "tools/task_projection.clj" ["--write" (.getPath sentinel)]))))
          (is (= before (snapshot root)))
          (is (= "Outside the fixed write boundary.\n" (slurp sentinel))))
        (finally
          (doseq [file (reverse (file-seq temp))] (io/delete-file file)))))))

(let [result (run-tests)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
