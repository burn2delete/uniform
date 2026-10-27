(ns resolve-task-projection
  "Resolve's Spec Kit ledger is editable; the roadmap table is generated."
  (:require [clojure.string :as str]))

(def task-path "specs/resolve-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/resolve.md")
(def semantic-ids
  #{"resolve/current-evidence-and-remaining-work/resolve1-resolve-lexical-positions"
    "resolve/current-evidence-and-remaining-work/resolve2-validate-module-boundaries"
    "resolve/current-evidence-and-remaining-work/resolve3-check-graph-legality"
    "resolve/current-evidence-and-remaining-work/resolve4-publish-stable-indexes"
    "resolve/current-evidence-and-remaining-work/resolve5-integrate-binding-consumers"})

(load-file "tools/task_projection.clj")

(def record-pattern
  #"(?s)^- \[([ x])\] `(resolve/current-evidence-and-remaining-work/resolve[1-5]-[a-z-]+)` ([^\n|]+)\n\nCurrent evidence/status: ([^\n|]+)\n\nDone when \(yes/no\): ([^\n|]+)$")

(defn- options []
  {:component "Resolve"
   :task-marker "resolve-implementation-tasks"
   :projection-marker "resolve-task-projection"
   :record-pattern record-pattern
   :semantic-ids semantic-ids
   :table-header "\n| Task | Status | Concrete exit evidence |\n| --- | --- | --- |\n"
   :title-id-prefix "resolve/current-evidence-and-remaining-work/"
   :title-id-error "Resolve task ID and preserved title disagree"})

(defn fail [message] (task-projection/fail message))
(defn region [text marker] (task-projection/region text marker))
(defn validate-links [field] (task-projection/validate-links (options) field))
(defn records [text] (task-projection/records (options) text))
(defn table [rows] (task-projection/table (:table-header (options)) rows))
(defn project [task-text roadmap-text]
  (task-projection/project (options) task-text roadmap-text))
(defn check-projection [task-text roadmap-text]
  (task-projection/check-projection (options) task-text roadmap-text))

(defn run [args]
  (when-not (#{["--check"] ["--write"]} (vec args))
    (fail "Usage: clojure -M tools/resolve_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "Resolve task projection validated; no implementation acceptance inferred.")))

;; load-file from the focused test must not perform a write or CLI action.
(when (seq *command-line-args*) (run *command-line-args*))
