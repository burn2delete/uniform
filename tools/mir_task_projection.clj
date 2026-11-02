(ns mir-task-projection
  "MIR's Spec Kit ledger is editable; the roadmap table is generated."
  (:require [clojure.string :as str]))

(def task-path "specs/mir-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/mir.md")
(def semantic-ids
  #{"mir/current-evidence-and-remaining-work/mir1-construct-semantic-mir"
    "mir/current-evidence-and-remaining-work/mir2-attach-fact-tables"
    "mir/current-evidence-and-remaining-work/mir3-verify-graph-and-legality"
    "mir/current-evidence-and-remaining-work/mir4-serialize-stable-modules"
    "mir/current-evidence-and-remaining-work/mir5-gate-downstream-consumption"})

(load-file "tools/task_projection.clj")

(def record-pattern
  #"(?s)^- \[([ x])\] `(mir/current-evidence-and-remaining-work/mir[1-5]-[a-z-]+)` ([^\n|]+)\n\nCurrent evidence/status: ([^\n|]+)\n\nDone when \(yes/no\): ([^\n|]+)$")

(defn- options []
  {:component "MIR"
   :task-marker "mir-implementation-tasks"
   :projection-marker "mir-task-projection"
   :record-pattern record-pattern
   :semantic-ids semantic-ids
   :table-header "\n| Task | Status | Concrete exit evidence |\n| --- | --- | --- |\n"
   :title-id-prefix "mir/current-evidence-and-remaining-work/"
   :title-id-error "MIR task ID and preserved title disagree"})

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
    (fail "Usage: clojure -M tools/mir_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "MIR task projection validated; no implementation acceptance inferred.")))

;; load-file from the focused test must not perform a write or CLI action.
(when (seq *command-line-args*) (run *command-line-args*))
