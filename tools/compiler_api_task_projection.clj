(ns compiler-api-task-projection
  "Project Compiler API outcomes and the owner-reviewed SH01 authority addition."
  (:require [clojure.string :as str]))

(load-file "tools/task_projection.clj")

(def task-path "specs/compiler-api-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/compiler-api.md")
(def outcome-order ["pipeline" "plugins" "pass-evidence" "domain-ir-boundaries" "development-boundaries"])
(def outcome-headings
  {"pipeline" "Built-in compiler pipeline outcome"
   "plugins" "Plugin Host: finite remaining tasks"
   "pass-evidence" "Pass Evidence: finite remaining tasks"
   "domain-ir-boundaries" "Domain IR registration and transition boundaries"
   "development-boundaries" "Compiler development ownership and verification boundaries"})
(def record-pattern
  #"(?s)^- \[([ x])\] `(pipeline|plugins|pass-evidence|domain-ir-boundaries|development-boundaries)` ([^\n|]+)\n\nStatus: ([^\n|]+)\n\nProjected task block:\n(Owner role: .+)$")
(defn fail [message] (task-projection/fail message))
(defn records [text]
  (let [rows (task-projection/records
               {:component "Compiler API" :task-marker "compiler-api-implementation-tasks"
                :record-pattern record-pattern :semantic-ids (set outcome-order)
                :record-keys [:id :title :status :projection]} text)]
    (doseq [{:keys [id title projection]} rows]
      (when-not (= (get outcome-headings id) title)
        (fail "Compiler API outcome key and original heading disagree"))
      (when-not (and (= 1 (count (re-seq (if (= id "development-boundaries")
                                          #"Current acceptance unassessed" #"All tasks open") projection)))
                     (= 1 (count (re-seq #"(?m)^Yes/no acceptance: " projection)))
                     (str/includes? projection "\n| Task | Concrete closing evidence |\n| --- | --- |\n")
                     (= (if (= id "development-boundaries") 6 5) (count (re-seq #"(?m)^\| [^|\n]+ \| [^|\n]+ \|$"
                                         (str/replace projection
                                           "| Task | Concrete closing evidence |\n| --- | --- |\n" "")))))
        (fail "Compiler API outcome requires its exact original rows and acceptance block")))
    rows))

(defn project [task-text roadmap-text]
  (let [by-id (into {} (map (juxt :id identity) (records task-text)))]
    (reduce (fn [text id]
              (let [{:keys [status projection]} (get by-id id)
                    {:keys [prefix suffix]}
                    (task-projection/region text (str "compiler-api-task-projection/" id))
                    block (if (= "Open" status) projection
                            (str/replace-first projection (if (= id "development-boundaries") "Current acceptance unassessed" "All tasks open")
                              (str "Implementation task status: " status)))]
                (str prefix "\n" block "\n" suffix)))
            roadmap-text outcome-order)))

(defn check-projection [task-text roadmap-text]
  (when-not (= roadmap-text (project task-text roadmap-text))
    (fail "Stale Compiler API projection; edit tasks.md, then run --write"))
  true)

(defn run [args]
  (when-not (#{["--check"] ["--write"]} (vec args))
    (fail "Usage: clojure -M tools/compiler_api_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "Compiler API task projection validated; no implementation acceptance inferred.")))

(when (seq *command-line-args*) (run *command-line-args*))
