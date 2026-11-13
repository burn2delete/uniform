(ns simplify.main
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [simplify.codex :as codex]
            [simplify.coordination :as coordination]
            [simplify.github :as gh]
            [simplify.runtime :as rt]
            [simplify.workflow :as workflow])
  (:gen-class))

(def defaults
  {:repository "burn2delete/uniform" :repo-dir "../.." :remote "origin" :base-branch "main"
   :project-title "Uniform Simplify" :campaign "Uniform simplification"
   :state-dir "../../target/validation/simplify" :worktree-root "../../../uniform-simplify-worktrees"
   :scope ["."] :parallelism 3 :poll-ms 15000 :check-timeout-ms 1200000
   :repair-github-review true :wait-for-ci true
   :codex-command ["codex" "app-server" "--listen" "stdio://"]
   :check-executables #{"clojure"}
   :checks [["clojure" "-M:test" "--namespace" "gravity.self-hosting.sh01-language-boundary-test"]]})

(defn config!
  ([path] (config! path defaults))
  ([path configuration-defaults]
  (let [file (io/file (rt/canonical path))
        parent (.getParentFile file)
        raw (merge configuration-defaults (rt/read-edn file))
        config (reduce (fn [m key]
                         (let [value (io/file (get m key))]
                           (assoc m key (rt/canonical (if (.isAbsolute value) value (io/file parent (str value)))))))
                       raw [:repo-dir :state-dir :worktree-root])]
    (when-not (and (pos-int? (:parallelism config)) (pos-int? (:poll-ms config))
                   (vector? (:scope config)) (seq (:scope config))
                   (seq (:checks config)) (seq (:codex-command config))
                   (re-matches #"[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+" (:repository config)))
      (rt/fail! "Invalid harness configuration" {}))
    (when (workflow/covers? (:repo-dir config) (:worktree-root config))
      (rt/fail! "Worker worktrees must be outside the source checkout" {}))
    config)))

(defn read-state! [config path]
  (let [state (if (.exists (io/file path)) (rt/read-edn path)
                  {:schema 1 :repository (:repository config) :campaign (:campaign config)
                   :status :new :tasks {} :effects {} :jobs {} :rounds {}})]
    (when-not (and (= 1 (:schema state)) (= (:repository config) (:repository state))
                   (= (:campaign config) (:campaign state)))
      (rt/fail! "State belongs to another campaign or schema" {}))
    state))

(defn context! [config]
  (let [directory (io/file (:state-dir config))
        _ (.mkdirs directory)
        path (str (io/file directory "state.edn"))
        ctx {:config config :state-dir (str directory) :state-file path :control-file (str (io/file directory "control.edn"))
             :log-file (str (io/file directory "events.jsonl")) :log-lock (Object.) :processes (atom #{})
             :coordinator-owner (atom false) :read-state #(read-state! config path)
             :state (atom (read-state! config path))}]
    (merge ctx (rt/coordination (rt/common-git-directory (:repo-dir config)) config))))

(defn with-current-state [ctx f]
  (rt/with-lock ctx
                #(do (when-not (:read-state ctx) (reset! (:state ctx) (read-state! (:config ctx) (:state-file ctx))))
                     (when (:coordination-file ctx) (rt/import-state-coordination! ctx))
                     (f))))

(defn shutdown! [ctx]
  ;; Compatibility entry point for Advance; never enumerate descendants or save a snapshot.
  (doseq [process @(:processes ctx)] (.destroy ^Process process))
  (rt/event! ctx :controller-shutdown {:descendant-coverage :unknown :inspection-required true}))

(defn controller! [ctx f]
  (with-current-state
    ctx
    #(let [hook (Thread. ^Runnable (fn [] (shutdown! ctx)))]
       (.addShutdownHook (Runtime/getRuntime) hook)
       (try (f)
            (catch Exception e
              ;; This callback still owns the state lock; contenders cannot write here.
              (rt/update! ctx assoc :status :attention)
              (throw e))
            (finally
              (try (.removeShutdownHook (Runtime/getRuntime) hook)
                   (catch IllegalStateException _ nil)))))))

(defn- dependency-command [argv]
  ;; Inspect the exact configured CLI aliases without invoking their main-opts.
  ;; Do not guess at another launcher or silently omit an unsupported command.
  (let [[executable & arguments] argv
        [jvm-options remaining] (split-with #(and (string? %) (str/starts-with? % "-J")) arguments)
        mode (first remaining)]
    (when-not (and (= "clojure" (.getName (io/file (or executable ""))))
                   (string? mode) (re-matches #"-M(?::[^\s]+)?" mode))
      (rt/fail! "Readiness cannot inspect this configured check's dependencies"
                {:command argv :supported-launcher "clojure [-J...] -M[:aliases]"}))
    ;; tools.deps only warns for undeclared aliases. Force resolution so a
    ;; cached classpath cannot hide that warning from the readiness check.
    (into [executable "-Sforce" "-Spath"] (concat jvm-options [mode]))))

(defn- doctor-sandbox! [ctx record! skipped!]
  (let [attempt (try {:result (let [result (codex/command! ctx "doctor-sandbox" (get-in ctx [:config :repo-dir]) ["git" "--version"])]
                               (when (:capture-truncated result)
                                 (rt/fail! "Sandbox readiness output is incomplete" {:result result :method "command/exec"}))
                               result)}
                     (catch Exception e {:error e}))
        error (:error attempt)
        details (when error (ex-data error))
        initialized? (or (nil? error) (= "command/exec" (:method details)) (contains? details :result))
        failure (when error {:message (.getMessage ^Exception error) :details (rt/redact details)})]
    (record! (merge {:check :app-server-initialize :status (if initialized? :passed :blocked)}
                    (when-not initialized? failure)))
    (if initialized?
      (record! (merge {:check :sandbox-command :status (if error :blocked :passed)}
                      (select-keys attempt [:result]) failure))
      (skipped! :sandbox-command :app-server-initialize))))

(defn doctor! [ctx]
  (let [{:keys [repo-dir repository remote base-branch checks completion-checks compiler-observation-command]}
        (:config ctx)
        results (atom [])
        record! (fn [result]
                  (swap! results conj result)
                  (rt/event! ctx :doctor-check result)
                  result)
        check! (fn [id operation]
                 (record! (try {:check id :status :passed :result (operation)}
                               (catch Exception e
                                 {:check id :status :blocked :message (.getMessage e)
                                  :details (rt/redact (ex-data e))}))))
        skipped! (fn [id prerequisite]
                   (record! {:check id :status :skipped :prerequisite prerequisite}))
        passed? #(= :passed (:status %))
        configured (vec (distinct (concat (when compiler-observation-command [compiler-observation-command])
                                          checks completion-checks)))]
    (doseq [[id argv] [[:git-version ["git" "--version"]]
                       [:github-cli-version ["gh" "--version"]]
                       [:codex-version ["codex" "--version"]]
                       [:codex-login ["codex" "login" "status"]]]]
      (check! id #(rt/run! ctx repo-dir argv)))
    (check! :git-remote
            #(let [result (rt/git ctx repo-dir "ls-remote" "--exit-code" (or remote "origin")
                                 (str "refs/heads/" (or base-branch "main")))]
               (when (str/blank? result) (rt/fail! "Configured Git base is not readable" {}))
               result))
    (if (passed? (check! :github-login #(rt/run! ctx repo-dir ["gh" "auth" "status" "--hostname" "github.com"])))
      (do
        (check! :github-repository
                #(let [result (gh/api ctx (gh/repo-path ctx nil))]
                   (when-not (true? (get-in result [:permissions :push]))
                     (rt/fail! "GitHub login lacks observed repository push permission"
                               {:repository repository :permissions (:permissions result)}))
                   {:repository (:full_name result) :push-permission true}))
        (check! :github-projects
                #(let [owner (gh/owner-info ctx)]
                   {:owner (:id owner) :visible-projects (count (gh/projects ctx (:id owner)))})))
      (do (skipped! :github-repository :github-login)
          (skipped! :github-projects :github-login)))
    (if (passed? (doctor-sandbox! ctx record! skipped!))
      (let [dependency-probes (check! :configured-dependencies #(mapv dependency-command configured))]
        (when (passed? dependency-probes)
          (let [probes (vec (distinct (:result dependency-probes)))
                executables (distinct (map first probes))]
            (doseq [executable executables]
              (let [runtime (check! [:clojure-runtime executable]
                                    #(let [result (codex/command! ctx "doctor-clojure" repo-dir
                                                                 [executable "-M" "-e" "(println :uniform/doctor-ready)"])]
                                       (when-not (and (not (:capture-truncated result))
                                                       (= ":uniform/doctor-ready" (str/trim (:stdout result))))
                                         (rt/fail! "Clojure readiness expression did not return its complete result"
                                                   {:result result}))
                                       result))]
                (doseq [argv (filter #(= executable (first %)) probes)]
                  (if (passed? runtime)
                    (check! [:clojure-dependencies argv]
                            #(let [result (codex/command! ctx (str "doctor-dependencies-" (rt/digest (pr-str argv))) repo-dir argv)]
                               (when (re-find #"(?m)^WARNING: Specified aliases are undeclared and are not being used:" (:stderr result))
                                 (rt/fail! "Configured Clojure aliases are undeclared" {:command argv :result result}))
                               (when (or (:capture-truncated result) (str/blank? (:stdout result)))
                                 (rt/fail! "Clojure dependency classpath is unavailable or incomplete" {:result result}))
                               result))
                    (skipped! [:clojure-dependencies argv] [:clojure-runtime executable]))))))))
      (skipped! :configured-dependencies :sandbox-command))
    (let [report {:repository repository :status (if (every? passed? @results) :ready :blocked)
                  :checks @results
                  :note "Readiness only: no model turn, repository source edit, GitHub mutation or campaign started. Delivery writes still require their normal checks."}]
      (if (= :ready (:status report))
        (do (rt/event! ctx :doctor-ok report) report)
        (do (rt/event! ctx :doctor-blocked report)
            (rt/fail! "Readiness checks are blocked; inspect doctor-check events" report))))))

(defn reconcile-result! [ctx effect-id result]
  (let [effect (get-in @(:state ctx) [:effects effect-id])
        main? (str/starts-with? effect-id "merge/")
        unapplied? (= :not-applied (:outcome result))
        inspection (when main?
                     (:inspection (rt/main-merge-state ctx effect-id)))
        inspection-digest (when (= :not-applied (get-in inspection [:inspection :outcome]))
                            (rt/digest (pr-str inspection)))]
    (when-not (and (or (#{:pending :uncertain} (:status effect))
                      (and (#{:done :not-applied} (:status effect)) inspection-digest
                           (not= inspection-digest (:main-inspection-digest effect)))
                      (and (nil? effect) (some #{effect-id} (vals (:tracking-intents @(:state ctx))))))
                   (string? (:evidence result))
                   (not (str/blank? (:evidence result))) (contains? result :result)
                   (contains? #{nil :applied :not-applied} (:outcome result))
                   (or (not= :not-applied (:outcome result)) (nil? (:result result))))
      (rt/fail! "Reconciliation needs an uncertain effect and inspected external result with evidence" {}))
    (when (and main? unapplied? (nil? inspection-digest))
      (rt/fail! "Main not-applied recovery requires the accepted shared inspection first" {:effect effect-id}))
    (rt/update! ctx assoc-in [:effects effect-id]
                (cond-> {:status (if (and unapplied? (not main?)) :not-applied :done)
                         :result (:result result) :reconciliation (:evidence result) :prior effect :previous effect}
                  (:model-not-started result) (assoc :inspection result)
                  inspection-digest (assoc :main-inspection-digest inspection-digest)))
    (rt/event! ctx :effect-reconciled {:effect effect-id :evidence (:evidence result)})))

(defn reconcile! [ctx effect-id result-file]
  (reconcile-result! ctx effect-id (rt/read-edn result-file)))

(defn retry! [ctx key]
  (let [t (workflow/task ctx key)
        intent (workflow/main-intent t)
        effect (get-in @(:state ctx) [:effects (:id intent)])
        {:keys [pending inspection]} (rt/main-merge-state ctx (:id intent))
        not-applied? (= :not-applied (get-in inspection [:inspection :outcome]))
        digest (when not-applied? (rt/digest (pr-str inspection)))]
    (when-not (= :attention (:status t)) (rt/fail! "Only an inspected Attention finding can be retried" {:finding key}))
    (when (or (true? (get-in t [:error :details :restricted])) (= :restricted (:status effect)))
      (rt/fail! "Restricted actions are retained as denied history and cannot be retried" {:finding key}))
    (when pending (rt/fail! "Own main mutation needs inspected reconciliation before retry" {:finding key :effect (:id intent)}))
    (when (or (#{:pending :uncertain} (:status effect))
              (#{:pending :uncertain :restricted}
                (get-in @(:state ctx) [:effects (get-in t [:error :details :effect]) :status])))
      (rt/fail! "Current external effect needs inspected reconciliation before retry" {:finding key}))
    (when not-applied?
      (let [key (rt/digest (pr-str ["gh-pr-merge" (get-in ctx [:config :repository])]))]
        (when (or (get-in @(:state ctx) [:restricted-commands key]) (rt/shared-restriction ctx :commands key))
          (rt/fail! "Repository merge action remains restricted; no successor intent is permitted" {:restricted true})))
      (when-not (and (= :done (:status effect)) (= digest (:main-inspection-digest effect))
                     (= (select-keys intent [:id :base :candidate :tree :method])
                        (select-keys (:intent inspection) [:id :base :candidate :tree :method])))
        (rt/fail! "Inspected not-applied merge needs matching local reconciliation before retry" {:finding key}))
      (let [next-intent (assoc intent :id (str (:id intent) "/inspection/" digest))]
        (when (get-in @(:state ctx) [:effects (:id next-intent)])
          (rt/fail! "Inspected successor effect already exists; inspect its actual outcome" {:finding key}))
        (workflow/task! ctx key :merge-intent next-intent)))
    (workflow/task! ctx key :feedback (:error t)
                    :status (cond (:merge-verification t) :finalizing
                                  (or (:merged t) not-applied? (#{:ci :merge} (:resume-status t))) :ci
                                  :else :repair))
    (println "Finding queued at its inspected recovery phase. Use resume to continue.")))

(defn dispatch! [ctx command args]
  (case command
    "doctor" (with-current-state ctx #(doctor! ctx))
    "status" (println (pr-str @(:state ctx)))
    "pause" (do (rt/write-edn! (:control-file ctx) {:paused true}) (println "Dispatch and merges will pause at the next scheduler boundary; active workers finish."))
    "setup" (with-current-state ctx #(do (coordination/register! ctx)
                                        (try (gh/setup! ctx) (gh/status! ctx (workflow/ensure-campaign! ctx) "Proposed")
                                             (finally (coordination/unregister! ctx)))))
    "coordinate" (with-current-state ctx #(println "Preserved state registered with shared coordination; no workers or GitHub writes."))
    "run" (controller! ctx #(when-not (= :complete (:status @(:state ctx))) (workflow/campaign! ctx)))
    "resume" (controller! ctx #(when-not (= :complete (:status @(:state ctx)))
                                 (rt/write-edn! (:control-file ctx) {:paused false}) (workflow/campaign! ctx)))
    "retry" (with-current-state ctx #(retry! ctx (first args)))
    "recover-authored" (with-current-state ctx #(workflow/recover-authored! ctx (first args) (second args)))
    "recover-dependency" (with-current-state ctx #(workflow/recover-dependency! ctx (first args) (second args)))
    "recover-review-checks" (with-current-state ctx #(workflow/recover-review-checks! ctx (first args) (second args)))
    "recover-fresh-source" (with-current-state ctx #(workflow/recover-fresh-source! ctx (first args) (second args)))
    "queue-recovery" (println (pr-str (workflow/queue-recovery! ctx (keyword (first args)) (second args) (nth args 2 nil))))
    "reconcile" (with-current-state ctx #(reconcile! ctx (first args) (second args)))
    "reconcile-main" (with-current-state ctx #(rt/reconcile-main-merge! ctx (first args) (rt/read-edn (second args))))
    "release-writer" (with-current-state ctx #(rt/inspect-release-paths! ctx (rt/read-edn (first args))
                                                                        (:evidence (rt/read-edn (second args)))))
    (rt/fail! "Usage: clojure -M:run doctor|setup|coordinate|run|resume|pause|status|retry|recover-authored|recover-dependency|recover-fresh-source|recover-review-checks|queue-recovery|reconcile|reconcile-main|release-writer CONFIG.edn [KEY FILE]" {})))

(defn -main [& [command config-path & args]]
  (try
    (when-not config-path (rt/fail! "Provide a command and a configuration path; see README.md" {}))
    (let [ctx (cond-> (assoc (context! (config! config-path)) :command command)
                (= "queue-recovery" command) (dissoc :log-file))]
      (rt/event! ctx :startup {:command command :config (dissoc (:config ctx) :model)})
      (try (dispatch! ctx command args)
           (catch Exception e
             (rt/event! ctx :fatal {:message (.getMessage e) :data (ex-data e)})
             (throw e))))
    (catch Exception e
      (binding [*out* *err*] (println (.getMessage e)))
      (shutdown-agents)
      (System/exit 1)))
  (shutdown-agents))
