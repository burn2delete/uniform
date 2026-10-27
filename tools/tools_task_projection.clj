(ns tools-task-projection
  "Developer Tools task authority with document-relative evidence provenance."
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(load-file "tools/task_projection.clj")
(load-file "tools/task_projection_links.clj")

(def task-path "specs/tools-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/tools.md")
(def semantic-ids
  #{"tools/finite-service-outcomes-and-remaining-work/format-formatter-maintainer"
    "tools/finite-service-outcomes-and-remaining-work/lint-analysis-tool-maintainer"
    "tools/finite-service-outcomes-and-remaining-work/language-server-editor-maintainer"
    "tools/finite-service-outcomes-and-remaining-work/debug-debugger-maintainer"
    "tools/finite-service-outcomes-and-remaining-work/repl-interactive-runtime-tooling-maintainer"
    "tools/finite-service-outcomes-and-remaining-work/ir-explorer-compiler-tool-maintainer"
    "tools/finite-service-outcomes-and-remaining-work/profile-performance-tool-maintainer"
    "tools/finite-service-outcomes-and-remaining-work/docs-documentation-tool-maintainer"
    "tools/finite-service-outcomes-and-remaining-work/dev-server-development-session-maintainer"
    "tools/finite-service-outcomes-and-remaining-work/registry-view-package-tool-maintainer"
    "tools/finite-service-outcomes-and-remaining-work/safety-explorer-safety-tool-maintainer"
    "tools/finite-service-outcomes-and-remaining-work/ai-development-ai-tool-maintainer"})
(def link-pattern task-projection-links/link-pattern)
(def record-pattern
  #"(?s)^- \[([ x])\] `(tools/finite-service-outcomes-and-remaining-work/[a-z-]+)` ([^\n|]+)\n\nStatus: ([^\n|]+)\n\nRemaining implementation tasks: ([^\n|]+)\n\nClosing evidence and governing contract: ([^\n|]+)$")

(defn fail [message] (task-projection/fail message))

(defn relative-target [document target]
  (task-projection-links/relative-target "Developer Tools" document target))

(defn rebase-links [field source destination]
  (task-projection-links/rebase-links "Developer Tools" field source destination))

(defn validate-field [field]
  (task-projection-links/validate-field "Developer Tools" task-path field))

(defn records [text]
  (let [rows (task-projection/records
               {:component "Developer Tools" :task-marker "tools-implementation-tasks"
                :record-pattern record-pattern :semantic-ids semantic-ids
                :record-keys [:id :title :status :implementation :acceptance]
                :title-id-prefix "tools/finite-service-outcomes-and-remaining-work/"
                :title-id-error "Developer Tools task ID and preserved title disagree"
                :validate-field validate-field} text)]
    (task-projection-links/validate-targets "Developer Tools" task-path rows)))

(def open-summary "Every row is open. The concrete implementation tasks in each row close only\nwith actual service execution and the listed yes/no evidence, not record flags.\nSpecialist roles are accountability recommendations, not ownership transfers.")
(defn table [rows]
  (let [all-open? (every? #(= "Open" (:status %)) rows)]
    (str "\n" (if all-open? open-summary
                 (str/replace-first open-summary "Every row is open." "Implementation task statuses are shown below."))
         (if all-open?
           "\n\n| Named outcome / specialist role | Remaining implementation tasks | Yes/no closing evidence and governing contract |\n| --- | --- | --- |\n"
           "\n\n| Named outcome / specialist role | Status | Remaining implementation tasks | Yes/no closing evidence and governing contract |\n| --- | --- | --- | --- |\n")
         (apply str (for [{:keys [title status implementation acceptance]} rows]
           (str "| " (rebase-links title task-path roadmap-path) " | "
                (when-not all-open? (str (rebase-links status task-path roadmap-path) " | "))
                (rebase-links implementation task-path roadmap-path) " | "
                (rebase-links acceptance task-path roadmap-path) " |\n"))))))

(defn project [task-text roadmap-text]
  (let [rows (records task-text)
        {:keys [prefix suffix]} (task-projection/region roadmap-text "tools-task-projection")]
    (str prefix (table rows) suffix)))

(defn check-projection [task-text roadmap-text]
  (when-not (= roadmap-text (project task-text roadmap-text))
    (fail "Stale Developer Tools projection; edit tasks.md, then run --write"))
  true)

(defn run [args]
  (when-not (#{["--check"] ["--write"]} (vec args))
    (fail "Usage: clojure -M tools/tools_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "Developer Tools task projection validated; no implementation acceptance inferred.")))

(when (seq *command-line-args*) (run *command-line-args*))
