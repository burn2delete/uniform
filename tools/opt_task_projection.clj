(ns opt-task-projection
  "Opt's Spec Kit ledger is editable; the roadmap table is generated."
  (:require [clojure.string :as str]))

(def task-path "specs/opt-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/opt.md")
(def semantic-ids
  #{"opt/current-evidence-and-remaining-work/opt1-execute-pass-contracts"
    "opt/current-evidence-and-remaining-work/opt2-implement-structural-families"
    "opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors"
    "opt/current-evidence-and-remaining-work/opt4-verify-and-regenerate"
    "opt/current-evidence-and-remaining-work/opt5-integrate-reproducible-pipeline"})

(load-file "tools/task_projection.clj")

(def record-pattern
  #"(?s)^- \[([ x])\] `(opt/current-evidence-and-remaining-work/opt[1-5]-[a-z-]+)` ([^\n|]+)\n\nCurrent evidence/status: ([^\n|]+)\n\nDone when \(yes/no\): ([^\n|]+)$")

(defn validate-links [field]
  ;; Opt's existing prose arrow is text, not Markdown. Validate a masked copy;
  ;; records and generated cells keep their original bytes.
  (task-projection/validate-links {:component "Opt"}
    (str/replace field " -> " " to ")))

(defn- options []
  {:component "Opt"
   :validate-field validate-links
   :task-marker "opt-implementation-tasks"
   :projection-marker "opt-task-projection"
   :record-pattern record-pattern
   :semantic-ids semantic-ids
   :table-header "\n| Task | Status | Concrete exit evidence |\n| --- | --- | --- |\n"
   :title-id-prefix "opt/current-evidence-and-remaining-work/"
   :title-id-error "Opt task ID and preserved title disagree"})

(defn fail [message] (task-projection/fail message))
(defn region [text marker] (task-projection/region text marker))
(defn records [text] (task-projection/records (options) text))
(defn table [rows] (task-projection/table (:table-header (options)) rows))
(defn project [task-text roadmap-text]
  (task-projection/project (options) task-text roadmap-text))
(defn check-projection [task-text roadmap-text]
  (task-projection/check-projection (options) task-text roadmap-text))

(defn run [args]
  (when-not (#{["--check"] ["--write"]} (vec args))
    (fail "Usage: clojure -M tools/opt_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "Opt task projection validated; no implementation acceptance inferred.")))

;; load-file from the focused test must not perform a write or CLI action.
(when (seq *command-line-args*) (run *command-line-args*))
