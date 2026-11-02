(ns reader-task-projection
  "Reader's Spec Kit ledger is editable; the roadmap table is generated."
  (:require [clojure.string :as str]))

(def task-path "specs/reader-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/reader.md")
(def semantic-ids
  #{"reader/finite-remaining-work/contract-coverage-reconciliation"
    "reader/finite-remaining-work/decode-and-source-identity-closure"
    "reader/finite-remaining-work/lexical-products-and-origins-closure"
    "reader/finite-remaining-work/extension-policy-closure"
    "reader/finite-remaining-work/authentication-and-quota-removal-closure"
    "reader/finite-remaining-work/fresh-syntax-handoff"
    "reader/finite-remaining-work/component-release-evidence"})

(load-file "tools/task_projection.clj")

(def record-pattern
  #"(?s)^- \[([ x])\] `(reader/finite-remaining-work/[a-z-]+)` ([^\n|]+)\n\nCurrent evidence/status: ([^\n|]+)\n\nDone when \(yes/no\): ([^\n|]+)$")

(defn- options []
  {:component "Reader"
   :task-marker "reader-implementation-tasks"
   :projection-marker "reader-task-projection"
   :record-pattern record-pattern
   :semantic-ids semantic-ids
   :table-header "\n| Task | Current evidence/status | Done when (yes/no) |\n| --- | --- | --- |\n"})

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
    (fail "Usage: clojure -M tools/reader_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "Reader task projection validated; no implementation acceptance inferred.")))

;; load-file from the focused test must not perform a write or CLI action.
(when (seq *command-line-args*) (run *command-line-args*))
