(ns evolution-task-projection
  "Project the declared governance artifact producers from their task authority."
  (:require [clojure.string :as str]))

(load-file "tools/task_projection.clj")
(load-file "tools/task_projection_links.clj")

(def task-path "specs/evolution-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/evolution.md")
(def semantic-ids
  #{"evolution/change-review" "evolution/experiment-lifecycle"
    "evolution/library-admission" "evolution/namespace-decision"
    "evolution/registry-yank" "evolution/rfc-review"
    "evolution/security-response" "evolution/security-review"
    "evolution/stabilization-decision" "evolution/target-support-admission"})
(def record-pattern
  #"(?s)^- \[([ x])\] `(evolution/[a-z-]+)` ([^\n|]+)\n\nStatus: ([^\n|]+)\n\nSource requirement: ([^\n|]+)\n\nRequired outputs: ([^\n|]+)\n\nClosing evidence: ([^\n|]+)\n\nRequired rejection evidence: ([^\n|]+)\n\nDependencies: ([^\n|]+)\n\nExact source criteria: ([^\n|]+)$")

(defn fail [message] (task-projection/fail message))
(defn records [text]
  (let [rows (task-projection/records
               {:component "Evolution" :task-marker "evolution-implementation-tasks"
                :record-pattern record-pattern :semantic-ids semantic-ids
                :record-keys [:id :title :status :requirement :outputs :acceptance :rejections :dependencies :facets]
                :validate-field #(task-projection-links/validate-field "Evolution" task-path %)} text)]
    (task-projection-links/validate-targets "Evolution" task-path rows)))

(defn project [task-text roadmap-text]
  (let [rows (records task-text)
        {:keys [prefix suffix]} (task-projection/region roadmap-text "evolution-task-projection")]
    (str prefix "\n| Task | Status | Required outputs | Closing evidence | Rejection evidence | Dependencies | Source requirement |\n"
         "| --- | --- | --- | --- | --- | --- | --- |\n"
         (apply str (for [row rows]
                      (str "| " (str/join " | "
                                          (map #(task-projection-links/rebase-links "Evolution" (get row %) task-path roadmap-path)
                                               [:title :status :outputs :acceptance :rejections :dependencies :requirement])) " |\n")))
         suffix)))

(defn check-projection [tasks roadmap]
  (when-not (= roadmap (project tasks roadmap)) (fail "Stale Evolution projection; edit tasks.md, then run --write"))
  true)

(defn run [args]
  (when-not (#{["--check"] ["--write"]} (vec args))
    (fail "Usage: clojure -M tools/evolution_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args)) (spit roadmap-path (project tasks roadmap)) (check-projection tasks roadmap))
    (println "Evolution task projection validated; governance implementation acceptance is unchanged.")))

(when (seq *command-line-args*) (run *command-line-args*))
