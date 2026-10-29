(ns release-task-projection
  "Release task authority with document-relative evidence provenance."
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(load-file "tools/task_projection.clj")
(load-file "tools/task_projection_links.clj")

(def task-path "specs/release-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/release.md")
(def semantic-ids
  #{"release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance"
    "release/remaining-tasks-and-evidence/complete-stage-compatibilityequivalence-across-the-claimed-compiler"
    "release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution"
    "release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom"
    "release/remaining-tasks-and-evidence/obtain-independent-acceptance-for-the-exact-release-candidate"
    "release/remaining-tasks-and-evidence/execute-language-conformance-fixtures"
    "release/remaining-tasks-and-evidence/execute-compiler-preservation-fixtures"
    "release/remaining-tasks-and-evidence/execute-runtime-conformance-fixtures"
    "release/remaining-tasks-and-evidence/execute-profile-compliance-fixtures"
    "release/remaining-tasks-and-evidence/execute-safety-conformance-fixtures"
    "release/remaining-tasks-and-evidence/execute-library-conformance-fixtures"
    "release/remaining-tasks-and-evidence/execute-agent-and-workflow-evaluations"
    "release/remaining-tasks-and-evidence/execute-fuzz-and-property-fixtures"
    "release/remaining-tasks-and-evidence/execute-differential-conformance-fixtures"
    "release/remaining-tasks-and-evidence/execute-formal-evidence-checks"
    "release/remaining-tasks-and-evidence/execute-performance-regression-fixtures"
    "release/remaining-tasks-and-evidence/execute-staged-compiler-validation"
    "release/remaining-tasks-and-evidence/implement-declared-seed-compiler-and-native-observations"})
(def link-pattern task-projection-links/link-pattern)
(def record-pattern
  #"(?s)^- \[([ x])\] `(release/remaining-tasks-and-evidence/[a-z-]+)` ([^\n|]+)\n\nStatus: ([^\n|]+)\n\nEvidence baseline: ([^\n|]+)\n\nClosing evidence: ([^\n|]+)$")

(defn fail [message] (task-projection/fail message))

(defn relative-target [document target]
  (task-projection-links/relative-target "Release" document target))

(defn rebase-links [field source destination]
  (task-projection-links/rebase-links "Release" field source destination))

(defn validate-field [field]
  (task-projection-links/validate-field "Release" task-path field))

(defn records [text]
  (let [rows (task-projection/records
               {:component "Release" :task-marker "release-implementation-tasks"
                :record-pattern record-pattern :semantic-ids semantic-ids
                :record-keys [:id :title :status :evidence :acceptance]
                :title-id-prefix "release/remaining-tasks-and-evidence/"
                :title-id-error "Release task ID and preserved title disagree"
                :validate-field validate-field} text)]
    (task-projection-links/validate-targets "Release" task-path rows)))

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
        {:keys [prefix suffix]} (task-projection/region roadmap-text "release-task-projection")]
    (str prefix (table rows) suffix)))

(defn check-projection [task-text roadmap-text]
  (when-not (= roadmap-text (project task-text roadmap-text))
    (fail "Stale Release projection; edit tasks.md, then run --write"))
  true)

(defn run [args]
  (when-not (#{["--check"] ["--write"]} (vec args))
    (fail "Usage: clojure -M tools/release_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "Release task projection validated; no implementation acceptance inferred.")))

(when (seq *command-line-args*) (run *command-line-args*))
