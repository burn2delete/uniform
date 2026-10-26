(ns backends-task-projection
  "Project existing keyed backend-family task paragraphs from one editable ledger."
  (:require [clojure.string :as str]))

(load-file "tools/task_projection.clj")
(load-file "tools/task_projection_links.clj")

(def task-path "specs/backends-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/backends.md")
(def family-order
  ["backend-interface"
   "backend-c"
   "backend-llvm"
   "backend-wasm"
   "backend-jvm"
   "backend-js"
   "backend-mlir"
   "backend-gpu"
   "backend-hdl"
   "backend-workflow"
   "backend-ai"
   "backend-query"
   "backend-mobile"
   "backend-emission"
   "backend-conformance"])
(def family-headings
  {"backend-interface" "Backend Interface and ABI"
   "backend-c" "C"
   "backend-llvm" "LLVM"
   "backend-wasm" "Wasm"
   "backend-jvm" "JVM"
   "backend-js" "JavaScript and TypeScript"
   "backend-mlir" "MLIR"
   "backend-gpu" "GPU Backend"
   "backend-hdl" "HDL"
   "backend-workflow" "Workflow Backend"
   "backend-ai" "AI Artifacts"
   "backend-query" "Query and Relational"
   "backend-mobile" "Mobile"
   "backend-emission" "Artifact Emission Contract"
   "backend-conformance" "Backend Conformance"})
(def semantic-ids (set family-order))
(def record-pattern
  #"(?s)^- \[([ x])\] `(backend-[a-z-]+)` ([^\n|]+)\n\nStatus: ([^\n|]+)\n\nProjected task block:\n(Remaining tasks: .+)$")
(def open-summary "All remaining tasks below are open.")
(def mixed-summary "Non-Open implementation task statuses are shown in the projected family blocks.")

(defn fail [message] (task-projection/fail message))
(defn region [text marker] (task-projection/region text marker))
(defn validate-field [field]
  (task-projection-links/validate-field "Backends" task-path field))

(defn records [text]
  (let [rows (task-projection/records
               {:component "Backends" :task-marker "backends-implementation-tasks"
                :record-pattern record-pattern :semantic-ids semantic-ids
                :record-keys [:id :title :status :projection]
                :validate-field validate-field} text)]
    (doseq [{:keys [id title projection]} rows]
      (when-not (= (get family-headings id) title)
        (fail "Backend family key and original heading disagree"))
      (when-not (str/includes? projection "Yes/no:")
        (fail "Backend family task block is missing its acceptance questions")))
    (task-projection-links/validate-targets "Backends" task-path rows)))

(defn rebase [field]
  (task-projection-links/rebase-links "Backends" field task-path roadmap-path))

(defn project [task-text roadmap-text]
  (let [rows (records task-text)
        by-id (into {} (map (juxt :id identity) rows))
        {:keys [prefix body suffix]} (region roadmap-text "backends-open-status-projection")
        previous (some #(when (str/starts-with? (str/trim body) %) %)
                       [open-summary mixed-summary])]
    (when-not previous (fail "Malformed Backends status summary"))
    (let [summary (if (every? #(= "Open" (:status %)) rows) open-summary mixed-summary)
          banner (str prefix (str/replace-first body (re-pattern (java.util.regex.Pattern/quote previous)) summary) suffix)]
      (reduce (fn [text id]
                (let [{:keys [status projection]} (get by-id id)
                      {:keys [prefix suffix]} (region text (str "backends-task-projection/" id))]
                  (str prefix "\n"
                       (when-not (= "Open" status)
                         (str "Implementation status: " (rebase status) "\n\n"))
                       (rebase projection) "\n" suffix)))
              banner family-order))))

(defn check-projection [task-text roadmap-text]
  (when-not (= roadmap-text (project task-text roadmap-text))
    (fail "Stale Backends projection; edit tasks.md, then run --write"))
  true)

(defn run [args]
  (when-not (#{["--check"] ["--write"]} (vec args))
    (fail "Usage: clojure -M tools/backends_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "Backends family task projection validated; no backend acceptance inferred.")))

(when (seq *command-line-args*) (run *command-line-args*))
