(ns expand-task-projection
  "Expand's Spec Kit ledger is editable; the roadmap table is generated."
  (:require [clojure.string :as str]))

(def task-path "specs/expand-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/expand.md")
(def semantic-ids
  #{"expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros" "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety" "expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination" "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs" "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"})

(load-file "tools/task_projection.clj")

(def record-pattern
  #"(?s)^- \[([ x])\] `(expand/current-evidence-and-remaining-work/expand[1-5]-[a-z-]+)` ([^\n|]+)\n\nStatus: ([^\n|]+)\n\nConcrete exit evidence: ([^\n|]+)$")

(defn- options []
  {:component "Expand"
   :task-marker "expand-implementation-tasks"
   :projection-marker "expand-task-projection"
   :record-pattern record-pattern
   :semantic-ids semantic-ids
   :table-header "\n| Task | Status | Concrete exit evidence |\n| --- | --- | --- |\n"
   :title-id-prefix "expand/current-evidence-and-remaining-work/"
   :title-id-error "Expand task ID and preserved title label disagree"})

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
    (fail "Usage: clojure -M tools/expand_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "Expand task projection validated; no implementation acceptance inferred.")))

;; load-file from the focused test must not perform a write or CLI action.
(when (seq *command-line-args*) (run *command-line-args*))
