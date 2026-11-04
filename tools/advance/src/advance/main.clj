(ns advance.main
  (:require [advance.catalog :as catalog]
            [advance.github :as project]
            [advance.workflow :as workflow]
            [simplify.coordination :as coordination]
            [simplify.main :as base]
            [simplify.runtime :as rt])
  (:gen-class))

(def defaults
  (merge base/defaults
    {:harness-id "advance" :coordinator-lock "advance-harness.lock"
     :tracking-ref "refs/advance/main" :branch-prefix "codex/advance-"
     :project-title "Uniform Implementation" :campaign "Uniform project completion"
     :state-dir "../../target/validation/advance" :worktree-root "../../../uniform-advance-worktrees"
     :early-draft true :repair-github-review true :wait-for-ci true :priorities [] :work-label "implementation"
     :labels [["implementation" "0e8a16" "Spec Kit implementation tracking"]
              ["implementation:campaign" "1d76db" "Project completion objective"]
              ["implementation:task" "bfdadc" "Repository-owned implementation outcome"]
              ["implementation:increment" "c5def5" "Independently reviewable implementation increment"]]
     :project-description "Spec Kit implementation: select, draft, implement, review, verify, merge, repeat."
     :project-readme "# Uniform Implementation\n\nRepository specs/**/tasks.md retain requirements and completion authority. Parent issues mirror current outcomes; child issues are independently useful increments. A merged child does not imply parent completion.\n\nPriority controls selection order. Control = Paused or Blocked holds dispatch and integration for that issue and its children; on the campaign it holds the whole campaign. Active workers finish. Status reports execution, separately from Control.\n\nThe controller owns the full merge loop. It shares persistent path claims and a main-write lock with tools/simplify. Interrupted, restricted and uncertain work retains its identity and claims for inspection.\n\nAn empty actionable queue means waiting. Completion requires all current tasks accepted, no unclassified/missing outcomes, successful actual release checks, and independent assessment of the canonical language, self-hosting and seedless-release evidence on still-current main.\n\n"
     :project-views [["All work" "TABLE_LAYOUT" "label:implementation"]
                     ["Workflow" "BOARD_LAYOUT" "label:implementation"]
                     ["Outcomes" "TABLE_LAYOUT" "label:implementation:task"]
                     ["Attention" "TABLE_LAYOUT" "label:implementation status:Attention"]]
     :worker-instructions workflow/instructions
     :review-focus "Check the selected source task, declared increment, task/evidence updates, actual dependencies and completion nonclaims. "
     :completion-checks [["clojure" "-M" "tools/validate_gravity_docs.clj"]
                         ["clojure" "-M" "tools/validate_full_language_roadmap.clj"]
                         ["clojure" "-M:gravity" "self-host" "verify"]]}))

(defn dispatch! [ctx command args]
  (case command
    "catalog" (println (pr-str (catalog/index (catalog/discover (get-in ctx [:config :repo-dir])))))
    "setup" (rt/with-lock ctx #(do (coordination/register! ctx)
                                 (try (project/setup! ctx) (workflow/ensure-campaign! ctx)
                                      (finally (coordination/unregister! ctx)))))
    "run" (rt/with-lock ctx #(when-not (= :complete (:status @(:state ctx))) (workflow/campaign! ctx)))
    "resume" (rt/with-lock ctx #(when-not (= :complete (:status @(:state ctx)))
                                 (rt/write-edn! (:control-file ctx) {:paused false}) (workflow/campaign! ctx)))
    (base/dispatch! ctx command args)))

(defn -main [& [command config-path & args]]
  (try
    (when-not config-path (rt/fail! "Usage: clojure -M:run doctor|catalog|setup|run|resume|pause|status|retry|reconcile CONFIG.edn [ARGS]" {}))
    (let [ctx (assoc (base/context! (base/config! config-path defaults)) :before-implement workflow/validate-source! :command command)]
      (.addShutdownHook (Runtime/getRuntime)
        (Thread. ^Runnable
          (fn [] (base/shutdown! ctx))))
      (rt/event! ctx :startup {:command command :config (dissoc (:config ctx) :worker-instructions)})
      (try (dispatch! ctx command args)
           (catch Exception e
             (rt/event! ctx :fatal {:message (.getMessage e) :data (ex-data e)})
             (throw e))))
    (catch Exception e
      (binding [*out* *err*] (println (.getMessage e)))
      (shutdown-agents)
      (System/exit 1)))
  (shutdown-agents))
