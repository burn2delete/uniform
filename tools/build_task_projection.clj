(ns build-task-projection
  "Build task authority with document-relative evidence provenance."
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(load-file "tools/task_projection.clj")
(load-file "tools/task_projection_links.clj")

(def task-path "specs/build-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/build.md")
(def semantic-ids
  #{"build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution"
    "build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects"
    "build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation"
    "build/remaining-tasks-and-evidence/produce-and-verify-packageprovenancesigning-records"
    "build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix"})
(def link-pattern task-projection-links/link-pattern)
(def record-pattern
  #"(?s)^- \[([ x])\] `(build/remaining-tasks-and-evidence/[a-z-]+)` ([^\n|]+)\n\nStatus: ([^\n|]+)\n\nEvidence baseline: ([^\n|]+)\n\nClosing evidence: ([^\n|]+)$")

(defn fail [message] (task-projection/fail message))

(defn relative-target [document target]
  (task-projection-links/relative-target "Build" document target))

(defn rebase-links [field source destination]
  (task-projection-links/rebase-links "Build" field source destination))

(defn validate-field [field]
  (task-projection-links/validate-field "Build" task-path field))

(defn records [text]
  (let [rows (task-projection/records
               {:component "Build" :task-marker "build-implementation-tasks"
                :record-pattern record-pattern :semantic-ids semantic-ids
                :record-keys [:id :title :status :evidence :acceptance]
                :title-id-prefix "build/remaining-tasks-and-evidence/"
                :title-id-error "Build task ID and preserved title disagree"
                :validate-field validate-field} text)]
    (task-projection-links/validate-targets "Build" task-path rows)))

(defn table [rows]
  (let [all-open? (every? #(= "Open" (:status %)) rows)]
    ;; The original header carries the open status. On a later status update,
    ;; expose status explicitly instead of retaining a false all-open label.
    (str (if all-open?
           "\n| Task (all open) | Evidence baseline | Closing evidence |\n| --- | --- | --- |\n"
           "\n| Task | Status | Evidence baseline | Closing evidence |\n| --- | --- | --- | --- |\n")
         (apply str
           (for [{:keys [title status evidence acceptance]} rows]
             (str "| " (rebase-links title task-path roadmap-path) " | "
                  (when-not all-open? (str (rebase-links status task-path roadmap-path) " | "))
                  (rebase-links evidence task-path roadmap-path) " | "
                  (rebase-links acceptance task-path roadmap-path) " |\n"))))))

(defn project [task-text roadmap-text]
  (let [rows (records task-text)
        {:keys [prefix suffix]} (task-projection/region roadmap-text "build-task-projection")]
    (str prefix (table rows) suffix)))

(defn check-projection [task-text roadmap-text]
  (when-not (= roadmap-text (project task-text roadmap-text))
    (fail "Stale Build projection; edit tasks.md, then run --write"))
  true)

(defn run [args]
  (when-not (#{["--check"] ["--write"]} (vec args))
    (fail "Usage: clojure -M tools/build_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "Build task projection validated; no implementation acceptance inferred.")))

(when (seq *command-line-args*) (run *command-line-args*))
