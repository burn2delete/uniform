(ns emit-task-projection
  "Emit's Spec Kit ledger is editable; the roadmap table is generated."
  (:require [clojure.string :as str]))

(def task-path "specs/emit-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/emit.md")
(def semantic-ids
  #{"emit/current-evidence-and-remaining-work/emit1-reconcile-selected-target-contracts"
    "emit/current-evidence-and-remaining-work/emit2-emit-real-outputs"
    "emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes"
    "emit/current-evidence-and-remaining-work/emit4-establish-conformance"
    "emit/current-evidence-and-remaining-work/emit5-integrate-artifact-consumers"})

(load-file "tools/task_projection.clj")

(def record-pattern
  #"(?s)^- \[([ x])\] `(emit/current-evidence-and-remaining-work/emit[1-5]-[a-z-]+)` ([^\n|]+)\n\nCurrent evidence/status: ([^\n|]+)\n\nDone when \(yes/no\): ([^\n|]+)$")

(defn- options []
  {:component "Emit"
   :task-marker "emit-implementation-tasks"
   :projection-marker "emit-task-projection"
   :record-pattern record-pattern
   :semantic-ids semantic-ids
   :table-header "\n| Task | Status | Concrete exit evidence |\n| --- | --- | --- |\n"
   :title-id-prefix "emit/current-evidence-and-remaining-work/"
   :title-id-error "Emit task ID and preserved title disagree"})

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
    (fail "Usage: clojure -M tools/emit_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "Emit task projection validated; no implementation acceptance inferred.")))

;; load-file from the focused test must not perform a write or CLI action.
(when (seq *command-line-args*) (run *command-line-args*))
