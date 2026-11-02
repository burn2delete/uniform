(ns cli-task-projection
  "CLI task authority with document-relative evidence provenance."
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(load-file "tools/task_projection.clj")
(load-file "tools/task_projection_links.clj")

(def task-path "specs/cli-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/cli.md")
(def semantic-ids
  #{"cli/remaining-tasks-and-evidence/reconcile-supported-commandoptions-with-normative-families"
    "cli/remaining-tasks-and-evidence/connect-checkruncompilebuildtestpackageverify-to-real-services"
    "cli/remaining-tasks-and-evidence/connect-replformatlintdocinspectprofileauditairegistryexplain-services"
    "cli/remaining-tasks-and-evidence/normalize-json-text-exit-codes-and-redaction"
    "cli/remaining-tasks-and-evidence/package-and-verify-user-entrypoints"})
(def link-pattern task-projection-links/link-pattern)
(def record-pattern
  #"(?s)^- \[([ x])\] `(cli/remaining-tasks-and-evidence/[a-z-]+)` ([^\n|]+)\n\nStatus: ([^\n|]+)\n\nEvidence baseline: ([^\n|]+)\n\nClosing evidence: ([^\n|]+)$")

(defn fail [message] (task-projection/fail message))

(defn relative-parts [target]
  (let [parts (str/split target #"#" -1)]
    (when-not (and (<= (count parts) 2) (seq (first parts))
                   (or (= 1 (count parts))
                       (re-matches #"[a-z0-9]+(?:-[a-z0-9]+)*" (second parts))))
      (fail "CLI relative evidence requires a file and a valid optional heading"))
    parts))

(defn relative-target [document target]
  (task-projection-links/relative-target "CLI" document (first (relative-parts target))))

(defn file-only-links [field]
  (str/replace field link-pattern
    (fn [[whole label target]]
      (if (str/starts-with? target "https:") whole
        (let [[file] (relative-parts target)]
          ;; Lexical policy, including query/escape rejection, stays shared.
          (relative-target task-path target)
          (str "[" label "](" file ")"))))))

(defn rebase-links [field source destination]
  (str/replace field link-pattern
    (fn [[whole label target]]
      (if (str/starts-with? target "https:") whole
        (let [[_ fragment] (relative-parts target)
              path (relative-target source target)
              rebased (.relativize (.getParent (.toPath (io/file destination))) path)]
          (str "[" label "](" rebased (when fragment (str "#" fragment)) ")"))))))

(defn validate-field [field]
  (task-projection-links/validate-field "CLI" task-path (file-only-links field)))

(defn validate-targets [rows]
  ;; Check every file's existence and realpath containment before reading any
  ;; heading. Shared link policies and other component wrappers are unchanged.
  (task-projection-links/validate-targets "CLI" task-path
    (mapv (fn [row] (into {} (map (fn [[k value]]
                                  [k (if (= :id k) value (file-only-links value))]) row))) rows))
  (doseq [row rows field (vals (dissoc row :id)) [_ _ target] (re-seq link-pattern field)
          :when (not (str/starts-with? target "https:"))
          :let [[_ fragment] (relative-parts target)] :when fragment]
    (let [headings (set (for [[_ heading] (re-seq #"(?m)^#+ (.+)$"
                                                  (slurp (str (relative-target task-path target))))]
                          (-> heading str/lower-case
                              (str/replace #"[^a-z0-9 -]" "")
                              (str/replace #" +" "-"))))]
      (when-not (contains? headings fragment)
        (fail "CLI evidence heading does not exist"))))
  rows)

(defn records [text]
  (let [rows (task-projection/records
               {:component "CLI" :task-marker "cli-implementation-tasks"
                :record-pattern record-pattern :semantic-ids semantic-ids
                :record-keys [:id :title :status :evidence :acceptance]
                :title-id-prefix "cli/remaining-tasks-and-evidence/"
                :title-id-error "CLI task ID and preserved title disagree"
                :validate-field validate-field} text)]
    (validate-targets rows)))

(defn table [rows]
  (let [all-open? (every? #(= "Open" (:status %)) rows)]
    ;; The original header carries the open status. On a later status update,
    ;; expose status explicitly instead of retaining a false all-open label.
    (str (if all-open?
           "\n| Task (all open) | Evidence baseline | Closing evidence |\n| --- | --- | --- |\n"
           "\n| Task | Status | Evidence baseline | Closing evidence |\n| --- | --- | --- | --- |\n")
         (apply str
           (for [{:keys [title status evidence acceptance]} rows]
             (str "| " (rebase-links title task-path roadmap-path) " | "
                  (when-not all-open? (str (rebase-links status task-path roadmap-path) " | "))
                  (rebase-links evidence task-path roadmap-path) " | "
                  (rebase-links acceptance task-path roadmap-path) " |\n"))))))

(defn selected-scope [task-text]
  (let [body (str/trim (:body (task-projection/region task-text "cli-compiler-commands-scope")))]
    (when-not (and (str/starts-with? body "`cli/compiler-commands`")
                   (= ["1" "2" "3"] (mapv second (re-seq #"(?m)^([0-9]+)\. " body)))
                   (str/includes? body "Acceptance is yes/no:")
                   (str/includes? body "wider CLI/service outcomes"))
      (fail "CLI selected-command scope requires its original identity, three closure steps and acceptance"))
    (validate-field body)
    (validate-targets [{:id "cli/compiler-commands" :scope body}])
    body))

(defn project [task-text roadmap-text]
  (let [rows (records task-text)
        scope (selected-scope task-text)
        {task-prefix :prefix task-suffix :suffix}
        (task-projection/region roadmap-text "cli-task-projection")
        table-view (str task-prefix (table rows) task-suffix)
        {:keys [prefix suffix]}
        (task-projection/region table-view "cli-compiler-commands-projection")]
    (str prefix "\n" (rebase-links scope task-path roadmap-path) "\n" suffix)))

(defn check-projection [task-text roadmap-text]
  (when-not (= roadmap-text (project task-text roadmap-text))
    (fail "Stale CLI projection; edit tasks.md, then run --write"))
  true)

(defn run [args]
  (when-not (#{["--check"] ["--write"]} (vec args))
    (fail "Usage: clojure -M tools/cli_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "CLI task projection validated; no implementation acceptance inferred.")))

(when (seq *command-line-args*) (run *command-line-args*))
