(ns auth-task-projection
  "Auth task authority with document-relative evidence provenance."
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(load-file "tools/task_projection.clj")
(load-file "tools/task_projection_links.clj")

(def task-path "specs/auth-workflow/tasks.md")
(def roadmap-path "docs/component-roadmaps/auth.md")
(def semantic-ids
  #{"auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
    "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
    "auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks"
    "auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"
    "auth/remaining-tasks-and-evidence/publish-conformance-and-remaining-trust-assumptions"})
(def link-pattern task-projection-links/link-pattern)
(def record-pattern
  #"(?s)^- \[([ x])\] `(auth/remaining-tasks-and-evidence/[a-z-]+)` ([^\n|]+)\n\nStatus: ([^\n|]+)\n\nEvidence baseline: ([^\n|]+)\n\nClosing evidence: ([^\n|]+)$")

(defn fail [message] (task-projection/fail message))

(defn relative-target [document target]
  (task-projection-links/relative-target "Auth" document target))

(defn rebase-links [field source destination]
  (task-projection-links/rebase-links "Auth" field source destination))

(defn validate-field [field]
  (task-projection-links/validate-field "Auth" task-path field))

(defn records [text]
  (let [rows (task-projection/records
               {:component "Auth" :task-marker "auth-implementation-tasks"
                :record-pattern record-pattern :semantic-ids semantic-ids
                :record-keys [:id :title :status :evidence :acceptance]
                :title-id-prefix "auth/remaining-tasks-and-evidence/"
                :title-id-error "Auth task ID and preserved title disagree"
                :validate-field validate-field} text)]
    (task-projection-links/validate-targets "Auth" task-path rows)))

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
        {:keys [prefix suffix]} (task-projection/region roadmap-text "auth-task-projection")]
    (str prefix (table rows) suffix)))

(defn check-projection [task-text roadmap-text]
  (when-not (= roadmap-text (project task-text roadmap-text))
    (fail "Stale Auth projection; edit tasks.md, then run --write"))
  true)

(defn run [args]
  (when-not (#{["--check"] ["--write"]} (vec args))
    (fail "Usage: clojure -M tools/auth_task_projection.clj --check|--write"))
  (let [tasks (slurp task-path) roadmap (slurp roadmap-path)]
    (if (= ["--write"] (vec args))
      (spit roadmap-path (project tasks roadmap))
      (check-projection tasks roadmap))
    (println "Auth task projection validated; no implementation acceptance inferred.")))

(when (seq *command-line-args*) (run *command-line-args*))
