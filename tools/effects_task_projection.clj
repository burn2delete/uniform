(ns effects-task-projection
  "Effects's Spec Kit ledger is editable; the roadmap table is generated."
  (:require [clojure.string :as str]))

(def task-path "specs/effects-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/effects.md")
(def semantic-ids
  #{"effects/current-evidence-and-remaining-work/effects1-infer-direct-effects"
    "effects/current-evidence-and-remaining-work/effects2-propagate-latent-effects"
    "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
    "effects/current-evidence-and-remaining-work/effects4-record-replay-and-invalidation"
    "effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers"})

(load-file "tools/task_projection.clj")

(def record-pattern
  #"(?s)^- \[([ x])\] `(effects/current-evidence-and-remaining-work/effects[1-5]-[a-z-]+)` ([^\n|]+)\n\nCurrent evidence/status: ([^\n|]+)\n\nDone when \(yes/no\): ([^\n|]+)$")

(defn- options []
  {:component "Effects"
   :task-marker "effects-implementation-tasks"
   :projection-marker "effects-task-projection"
   :record-pattern record-pattern
   :semantic-ids semantic-ids
   :table-header "\n| Task | Status | Concrete exit evidence |\n| --- | --- | --- |\n"
   :title-id-prefix "effects/current-evidence-and-remaining-work/"
   :title-id-error "Effects task ID and preserved title disagree"})

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
    (fail "Usage: clojure -M tools/effects_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "Effects task projection validated; no implementation acceptance inferred.")))

;; load-file from the focused test must not perform a write or CLI action.
(when (seq *command-line-args*) (run *command-line-args*))
