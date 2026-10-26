(ns legality-task-projection
  "Legality task authority with document-relative evidence provenance."
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(load-file "tools/task_projection.clj")

(def task-path "specs/legality-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/legality.md")
(def semantic-ids
  #{"legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
    "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
    "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
    "legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution"
    "legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface"})
(def link-pattern #"\[([^\[\]<>!\\\n]+)\]\(([^\s()<>\"`\\]+)\)")
(def record-pattern
  #"(?s)^- \[([ x])\] `(legality/remaining-tasks-and-evidence/[a-z-]+)` ([^\n|]+)\n\nStatus: ([^\n|]+)\n\nEvidence baseline: ([^\n|]+)\n\nClosing evidence: ([^\n|]+)$")

(defn fail [message] (task-projection/fail message))

(defn relative-target [document target]
  ;; Pure lexical resolution. File existence and symlink containment are checked
  ;; separately before any projection can be written.
  (let [uri (try (java.net.URI. target) (catch java.net.URISyntaxException _ nil))]
    (when-not (and uri (nil? (.getScheme uri)) (nil? (.getAuthority uri))
                   (nil? (.getFragment uri)) (nil? (.getQuery uri))
                   (not (str/starts-with? target "/"))
                   (not (str/includes? target "%")) (seq (.getPath uri)))
      (fail "Legality relative links must name repository files without fragments or queries"))
    (let [path (.normalize (.resolve (.getParent (.toPath (io/file document))) target))]
      (when (or (.isAbsolute path) (.startsWith path ".."))
        (fail "Legality link escapes repository"))
      path)))

(defn rebase-links [field source destination]
  (str/replace field link-pattern
    (fn [[whole label target]]
      (if (str/starts-with? target "https:")
        whole
        (let [path (relative-target source target)
              rebased (.relativize (.getParent (.toPath (io/file destination))) path)]
          (str "[" label "](" rebased ")"))))))

(defn validate-field [field]
  ;; Reuse the accepted Markdown grammar after validating relative path syntax.
  (task-projection/validate-links {:component "Legality"}
    (str/replace field link-pattern
      (fn [[whole _ target]]
        (if (str/starts-with? target "https:") whole
          (do (relative-target task-path target) ""))))))

(defn records [text]
  (let [rows (task-projection/records
               {:component "Legality" :task-marker "legality-implementation-tasks"
                :record-pattern record-pattern :semantic-ids semantic-ids
                :record-keys [:id :title :status :evidence :acceptance]
                :title-id-prefix "legality/remaining-tasks-and-evidence/"
                :title-id-error "Legality task ID and preserved title disagree"
                :validate-field validate-field} text)
        root (.toRealPath (.toPath (io/file ".")) (make-array java.nio.file.LinkOption 0))]
    (doseq [row rows field (vals (dissoc row :id)) [_ _ target] (re-seq link-pattern field)
            :when (not (str/starts-with? target "https:"))]
      (let [path (relative-target task-path target)
            file (io/file (str path))]
        (when-not (.isFile file) (fail "Legality link target is not an existing file"))
        (when-not (.startsWith (.toRealPath (.toPath file) (make-array java.nio.file.LinkOption 0)) root)
          (fail "Legality link target resolves outside repository"))))
    rows))

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
        {:keys [prefix suffix]} (task-projection/region roadmap-text "legality-task-projection")]
    (str prefix (table rows) suffix)))

(defn check-projection [task-text roadmap-text]
  (when-not (= roadmap-text (project task-text roadmap-text))
    (fail "Stale Legality projection; edit tasks.md, then run --write"))
  true)

(defn run [args]
  (when-not (#{["--check"] ["--write"]} (vec args))
    (fail "Usage: clojure -M tools/legality_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "Legality task projection validated; no implementation acceptance inferred.")))

(when (seq *command-line-args*) (run *command-line-args*))
