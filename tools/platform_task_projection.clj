(ns platform-task-projection
  "Project existing target projects and full-product scope from one task ledger."
  (:require [clojure.string :as str]))

(load-file "tools/task_projection.clj")
(load-file "tools/task_projection_links.clj")
(def task-path "specs/platform-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/platform.md")
(def project-order ["hardware" "firmware" "kernel" "devices"])
(def project-titles {"hardware" "Hardware / `hardware`" "firmware" "Firmware / `firmware`"
                     "kernel" "Kernel / `kernel`" "devices" "Devices / `devices`"})
(def record-pattern
  #"(?s)^- \[([ x])\] `([a-z]+)` ([^\n|]+)\n\nStatus: ([^\n|]+)\n\nDeclared completion and remaining work: ([^\n|]+)\n\nSource basis: ([^\n|]+)$")
(def link-pattern task-projection-links/link-pattern)
(defn fail [message] (task-projection/fail message))
(defn relative-target [document target]
  (task-projection-links/relative-target "Platform" document target))
(defn rebase-links [field source destination]
  (task-projection-links/rebase-links "Platform" field source destination))
(defn validate-field [field]
  (task-projection-links/validate-field "Platform" task-path field))

(defn records [text]
  (let [rows (task-projection/records
               {:component "Platform" :task-marker "platform-implementation-tasks"
                :record-pattern record-pattern :semantic-ids (set project-order)
                :record-keys [:id :title :status :work :evidence]
                :validate-field validate-field} text)]
    (doseq [{:keys [id title]} rows]
      (when-not (= title (get project-titles id))
        (fail "Platform literal key and original project/key cell disagree")))
    (task-projection-links/validate-targets "Platform" task-path rows)))

(defn product-scope [text]
  (let [body (str/trim (:body (task-projection/region text "platform-product-scope")))]
    (when-not (and (str/starts-with? body "The full goal also includes")
                   (str/includes? body "semantic key `platform-product`.")
                   (str/includes? body "| Decision record (open) | Recommendation for review | Accountable role and required output |")
                   (= 6 (count (re-seq #"(?m)^\| [^|\n]+ \| [^|\n]+ \| [^|\n]+ \|$" body)))
                   (str/includes? body "After these records are adopted, remaining product tasks are:")
                   (str/includes? body "External prerequisites are not language features:")
                   (str/includes? body "Product acceptance is yes/no:"))
      (fail "Platform product scope requires its original identity, four decisions, conditional work and acceptance"))
    (validate-field body)
    (task-projection-links/validate-targets "Platform" task-path [{:id "platform-product" :scope body}])
    body))

(def open-summary "All projects are proposed and open. Their owner is the platform maintainer until\nindividual target ownership is explicitly assigned, not inferred from a file.")
(defn table [rows]
  (let [all-open? (every? #(= "Open" (:status %)) rows)]
    (str "\n" (if all-open? open-summary
                 (str/replace-first open-summary "All projects are proposed and open."
                   "Project implementation statuses are shown below."))
         (if all-open?
           "\n\n| Project / key | Declared completion and remaining work | Source basis |\n| --- | --- | --- |\n"
           "\n\n| Project / key | Status | Declared completion and remaining work | Source basis |\n| --- | --- | --- | --- |\n")
         (apply str (for [{:keys [title status work evidence]} rows]
           (str "| " (rebase-links title task-path roadmap-path) " | "
                (when-not all-open? (str (rebase-links status task-path roadmap-path) " | "))
                (rebase-links work task-path roadmap-path) " | "
                (rebase-links evidence task-path roadmap-path) " |\n"))))))

(defn project [task-text roadmap-text]
  (let [rows (records task-text) scope (product-scope task-text)
        {table-prefix :prefix table-suffix :suffix}
        (task-projection/region roadmap-text "platform-task-projection")
        table-view (str table-prefix (table rows) table-suffix)
        {:keys [prefix suffix]} (task-projection/region table-view "platform-product-projection")]
    (str prefix "\n" (rebase-links scope task-path roadmap-path) "\n" suffix)))

(defn check-projection [task-text roadmap-text]
  (when-not (= roadmap-text (project task-text roadmap-text))
    (fail "Stale Platform projection; edit tasks.md, then run --write"))
  true)
(defn run [args]
  (when-not (#{["--check"] ["--write"]} (vec args))
    (fail "Usage: clojure -M tools/platform_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "Platform task projection validated; no target or product acceptance inferred.")))
(when (seq *command-line-args*) (run *command-line-args*))
