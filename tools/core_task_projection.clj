(ns core-task-projection
  "Core's Spec Kit ledger is editable; the roadmap table is generated."
  (:require [clojure.string :as str]))

(def task-path "specs/core-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/core.md")
(def semantic-ids
  #{"core/current-evidence-and-remaining-work/core1-lower-core-forms"
    "core/current-evidence-and-remaining-work/core2-preserve-binding-structure"
    "core/current-evidence-and-remaining-work/core3-desugar-without-semantic-loss"
    "core/current-evidence-and-remaining-work/core4-verify-and-serialize"
    "core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses"})

(load-file "tools/task_projection.clj")

(def record-pattern
  #"(?s)^- \[([ x])\] `(core/current-evidence-and-remaining-work/core[1-5]-[a-z-]+)` ([^\n|]+)\n\nCurrent evidence/status: ([^\n|]+)\n\nDone when \(yes/no\): ([^\n|]+)$")

(defn- options []
  {:component "Core"
   :task-marker "core-implementation-tasks"
   :projection-marker "core-task-projection"
   :record-pattern record-pattern
   :semantic-ids semantic-ids
   :table-header "\n| Task | Status | Concrete exit evidence |\n| --- | --- | --- |\n"
   :title-id-prefix "core/current-evidence-and-remaining-work/"
   :title-id-error "Core task ID and preserved title disagree"
   :allow-bang? true})

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
    (fail "Usage: clojure -M tools/core_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "Core task projection validated; no implementation acceptance inferred.")))

;; load-file from the focused test must not perform a write or CLI action.
(when (seq *command-line-args*) (run *command-line-args*))
