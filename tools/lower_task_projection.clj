(ns lower-task-projection
  "Lower's Spec Kit ledger is editable; the roadmap table is generated."
  (:require [clojure.string :as str]))

(def task-path "specs/lower-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/lower.md")
(def semantic-ids
  #{"lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs"
    "lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout"
    "lower/current-evidence-and-remaining-work/lower3-select-explicit-providers"
    "lower/current-evidence-and-remaining-work/lower4-preserve-proofs-and-origins"
    "lower/current-evidence-and-remaining-work/lower5-integrate-target-emission"})

(load-file "tools/task_projection.clj")

(def record-pattern
  #"(?s)^- \[([ x])\] `(lower/current-evidence-and-remaining-work/lower[1-5]-[a-z-]+)` ([^\n|]+)\n\nCurrent evidence/status: ([^\n|]+)\n\nDone when \(yes/no\): ([^\n|]+)$")

(defn- options []
  {:component "Lower"
   :task-marker "lower-implementation-tasks"
   :projection-marker "lower-task-projection"
   :record-pattern record-pattern
   :semantic-ids semantic-ids
   :table-header "\n| Task | Status | Concrete exit evidence |\n| --- | --- | --- |\n"
   :title-id-prefix "lower/current-evidence-and-remaining-work/"
   :title-id-error "Lower task ID and preserved title disagree"})

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
    (fail "Usage: clojure -M tools/lower_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "Lower task projection validated; no implementation acceptance inferred.")))

;; load-file from the focused test must not perform a write or CLI action.
(when (seq *command-line-args*) (run *command-line-args*))
