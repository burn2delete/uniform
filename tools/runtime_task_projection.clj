(ns runtime-task-projection
  "Runtime task authority with document-relative evidence provenance."
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(load-file "tools/task_projection.clj")
(load-file "tools/task_projection_links.clj")

(def task-path "specs/runtime-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/runtime.md")
(def semantic-ids
  #{"runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"
    "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"
    "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
    "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"
    "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"})
(def link-pattern task-projection-links/link-pattern)
(def record-pattern
  #"(?s)^- \[([ x])\] `(runtime/remaining-tasks-and-evidence/[a-z-]+)` ([^\n|]+)\n\nStatus: ([^\n|]+)\n\nEvidence baseline: ([^\n|]+)\n\nClosing evidence: ([^\n|]+)$")

(defn fail [message] (task-projection/fail message))

(defn relative-target [document target]
  (task-projection-links/relative-target "Runtime" document target))

(defn rebase-links [field source destination]
  (task-projection-links/rebase-links "Runtime" field source destination))

(defn validate-field [field]
  (task-projection-links/validate-field "Runtime" task-path field))

(defn records [text]
  (let [rows (task-projection/records
               {:component "Runtime" :task-marker "runtime-implementation-tasks"
                :record-pattern record-pattern :semantic-ids semantic-ids
                :record-keys [:id :title :status :evidence :acceptance]
                :title-id-prefix "runtime/remaining-tasks-and-evidence/"
                :title-id-error "Runtime task ID and preserved title disagree"
                :validate-field validate-field} text)]
    (task-projection-links/validate-targets "Runtime" task-path rows)))

(defn table [rows]
  (let [all-open? (every? #(= "Open" (:status %)) rows)]
    (str (if all-open?
           "\nAll tasks are open; source evidence is a starting point, not acceptance.\n\n| Task | Existing evidence | Required closing evidence |\n| --- | --- | --- |\n"
           "\nImplementation task statuses are shown below; source evidence is a starting point, not acceptance.\n\n| Task | Status | Existing evidence | Required closing evidence |\n| --- | --- | --- | --- |\n")
         (apply str
           (for [{:keys [title status evidence acceptance]} rows]
             (str "| " (rebase-links title task-path roadmap-path) " | "
                  (when-not all-open? (str (rebase-links status task-path roadmap-path) " | "))
                  (rebase-links evidence task-path roadmap-path) " | "
                  (rebase-links acceptance task-path roadmap-path) " |\n"))))))

(defn project [task-text roadmap-text]
  (let [rows (records task-text)
        {:keys [prefix suffix]} (task-projection/region roadmap-text "runtime-task-projection")]
    (str prefix (table rows) suffix)))

(defn check-projection [task-text roadmap-text]
  (when-not (= roadmap-text (project task-text roadmap-text))
    (fail "Stale Runtime projection; edit tasks.md, then run --write"))
  true)

(defn run [args]
  (when-not (#{["--check"] ["--write"]} (vec args))
    (fail "Usage: clojure -M tools/runtime_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "Runtime task projection validated; no implementation acceptance inferred.")))

(when (seq *command-line-args*) (run *command-line-args*))
