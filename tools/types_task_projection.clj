(ns types-task-projection
  "Types's Spec Kit ledger is editable; the roadmap table is generated."
  (:require [clojure.string :as str]))

(def task-path "specs/types-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/types.md")
(def semantic-ids
  #{"types/current-evidence-and-remaining-work/types1-solve-core-constraints"
    "types/current-evidence-and-remaining-work/types2-check-declared-abstractions"
    "types/current-evidence-and-remaining-work/types3-make-dynamic-boundaries-explicit"
    "types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts"
    "types/current-evidence-and-remaining-work/types5-integrate-typed-core"})

(load-file "tools/task_projection.clj")

(def record-pattern
  #"(?s)^- \[([ x])\] `(types/current-evidence-and-remaining-work/types[1-5]-[a-z-]+)` ([^\n|]+)\n\nCurrent evidence/status: ([^\n|]+)\n\nDone when \(yes/no\): ([^\n|]+)$")

(defn- options []
  {:component "Types"
   :task-marker "types-implementation-tasks"
   :projection-marker "types-task-projection"
   :record-pattern record-pattern
   :semantic-ids semantic-ids
   :table-header "\n| Task | Status | Concrete exit evidence |\n| --- | --- | --- |\n"
   :title-id-prefix "types/current-evidence-and-remaining-work/"
   :title-id-error "Types task ID and preserved title disagree"})

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
    (fail "Usage: clojure -M tools/types_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "Types task projection validated; no implementation acceptance inferred.")))

;; load-file from the focused test must not perform a write or CLI action.
(when (seq *command-line-args*) (run *command-line-args*))
