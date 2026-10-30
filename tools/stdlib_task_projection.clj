(ns stdlib-task-projection
  "Standard Library task authority with document-relative evidence provenance."
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(load-file "tools/task_projection.clj")
(load-file "tools/task_projection_links.clj")

(def task-path "specs/stdlib-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/stdlib.md")
(def semantic-ids
  #{"stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations"
    "stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis"
    "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis"
    "stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters"
    "stdlib/remaining-tasks-and-evidence/implement-query-and-transaction-adapters"
    "stdlib/remaining-tasks-and-evidence/implement-durable-workflow-adapters"
    "stdlib/remaining-tasks-and-evidence/implement-ai-provider-and-policy-adapters"
    "stdlib/remaining-tasks-and-evidence/implement-compiler-and-syntax-adapters"
    "stdlib/remaining-tasks-and-evidence/implement-cryptographic-adapters"
    "stdlib/remaining-tasks-and-evidence/implement-ui-and-event-adapters"
    "stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests"})
(def link-pattern task-projection-links/link-pattern)
(def record-pattern
  #"(?s)^- \[([ x])\] `(stdlib/remaining-tasks-and-evidence/[a-z-]+)` ([^\n|]+)\n\nStatus: ([^\n|]+)\n\nEvidence baseline: ([^\n|]+)\n\nClosing evidence: ([^\n|]+)$")

(defn fail [message] (task-projection/fail message))

(defn relative-parts [target]
  (let [parts (str/split target #"#" -1)]
    (when-not (and (<= (count parts) 2) (seq (first parts))
                   (or (= 1 (count parts))
                       (re-matches #"[a-z0-9]+(?:-[a-z0-9]+)*" (second parts))))
      (fail "Standard Library relative evidence requires a file and a valid optional heading"))
    parts))

(defn relative-target [document target]
  (task-projection-links/relative-target "Standard Library" document (first (relative-parts target))))

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
  (task-projection-links/validate-field "Standard Library" task-path (file-only-links field)))

(defn validate-targets [rows]
  ;; Check every file's existence and realpath containment before reading any
  ;; heading. Shared link policies and other component wrappers are unchanged.
  (task-projection-links/validate-targets "Standard Library" task-path
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
        (fail "Standard Library evidence heading does not exist"))))
  rows)

(defn records [text]
  (let [rows (task-projection/records
               {:component "Standard Library" :task-marker "stdlib-implementation-tasks"
                :record-pattern record-pattern :semantic-ids semantic-ids
                :record-keys [:id :title :status :evidence :acceptance]
                :validate-field validate-field} text)]
    (doseq [{:keys [id title]} rows]
      (when-not (if (= id "stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters")
                  (= title "Complete schema and serialization adapters")
                  (= id (str "stdlib/remaining-tasks-and-evidence/" (-> title str/lower-case (str/replace #"[^a-z0-9 -]" "") (str/replace #" +" "-")))))
        (fail "Standard Library task ID and declared leaf title disagree")))
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

(defn project [task-text roadmap-text]
  (let [rows (records task-text)
        {:keys [prefix suffix]} (task-projection/region roadmap-text "stdlib-task-projection")]
    (str prefix (table rows) suffix)))

(defn check-projection [task-text roadmap-text]
  (when-not (= roadmap-text (project task-text roadmap-text))
    (fail "Stale Standard Library projection; edit tasks.md, then run --write"))
  true)

(defn run [args]
  (when-not (#{["--check"] ["--write"]} (vec args))
    (fail "Usage: clojure -M tools/stdlib_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "Standard Library task projection validated; no implementation acceptance inferred.")))

(when (seq *command-line-args*) (run *command-line-args*))
