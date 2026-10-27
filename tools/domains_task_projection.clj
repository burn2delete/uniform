(ns domains-task-projection
  "Project seven existing domain outcome/task blocks from one editable ledger."
  (:require [clojure.string :as str]))

(load-file "tools/task_projection.clj")
(load-file "tools/task_projection_links.clj")

(def task-path "specs/domains-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/domains.md")
(def outcome-order ["math" "schema" "query" "workflow" "ai" "gpu" "ui"])
(def outcome-headings {"math" "Math" "schema" "Schema" "query" "Query" "workflow" "Workflow"
                       "ai" "AI" "gpu" "GPU" "ui" "UI"})
(def record-pattern
  #"(?s)^- \[([ x])\] `([a-z]+)` ([^\n|]+)\n\nStatus: ([^\n|]+)\n\nProjected task block:\n(Outcome: .+)$")
(def open-tasks-pattern #"Remaining(?:\s+finite)?\s+tasks,\s+all open:")
(def link-pattern task-projection-links/link-pattern)
(defn fail [message] (task-projection/fail message))
(defn relative-target [document target]
  (task-projection-links/relative-target "Domains" document target))
(defn rebase-links [field source destination]
  (task-projection-links/rebase-links "Domains" field source destination))
(defn validate-field [field]
  (task-projection-links/validate-field "Domains" task-path field))

(defn records [text]
  (let [rows (task-projection/records
               {:component "Domains" :task-marker "domains-implementation-tasks"
                :record-pattern record-pattern :semantic-ids (set outcome-order)
                :record-keys [:id :title :status :projection]
                :validate-field validate-field} text)]
    (doseq [{:keys [id title projection]} rows]
      (when-not (and (= (get outcome-headings id) title)
                     (= 1 (count (re-seq open-tasks-pattern projection)))
                     (= 1 (count (re-seq #"Yes/no acceptance:" projection)))
                     (str/includes? projection "Integration:")
                     (str/includes? projection "Decision owner:"))
        (fail "Domains requires each original key/heading and complete task/acceptance/integration block")))
    (task-projection-links/validate-targets "Domains" task-path rows)))

(defn project [task-text roadmap-text]
  (let [by-id (into {} (map (juxt :id identity) (records task-text)))]
    (reduce (fn [text id]
              (let [{:keys [status projection]} (get by-id id)
                    {:keys [prefix suffix]}
                    (task-projection/region text (str "domains-task-projection/" id))
                    block (if (= "Open" status) projection
                            (str/replace-first projection open-tasks-pattern
                              (fn [_] (str "Remaining tasks (implementation status: " status "):"))))]
                (str prefix "\n" (rebase-links block task-path roadmap-path) "\n" suffix)))
            roadmap-text outcome-order)))

(defn check-projection [task-text roadmap-text]
  (when-not (= roadmap-text (project task-text roadmap-text))
    (fail "Stale Domains projection; edit tasks.md, then run --write"))
  true)

(defn run [args]
  (when-not (#{["--check"] ["--write"]} (vec args))
    (fail "Usage: clojure -M tools/domains_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "Domains task projection validated; no domain implementation acceptance inferred.")))

(when (seq *command-line-args*) (run *command-line-args*))
