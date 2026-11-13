;; Scoped foreground production entrypoint. No provider doubles, model doubles,
;; planner proposals, campaign registration, GitHub writes or schedules.
;; The subprocess capture is ingested, not rerun or relabeled as a configured
;; sandbox command. Its exact offline launcher remains part of the receipt.
(require '[clojure.edn :as edn] '[clojure.java.io :as io]
         '[clojure.java.shell :as shell] '[clojure.string :as str]
         '[seedless.main :as main] '[seedless.workflow :as workflow]
         '[seedless.compiler-observation :as compiler]
         '[seedless.compiler-diagnosis :as diagnosis]
         '[simplify.runtime :as rt])

(def root "/workspace/scratch/0ca27623ede8/uniform-c7-routing")
(def controller-root "/workspace/scratch/bb81fa003696/uniform")
(def artifacts "/workspace/scratch/bb81fa003696/foreground-frontier")
(def capture-dir (str artifacts "/offline-full-route"))
(def state-directory (str artifacts "/production-diagnosis-state"))

(defn git-read [cwd & argv]
  (let [result (apply shell/sh "git" (concat argv [:dir cwd]))]
    (when-not (zero? (:exit result)) (throw (ex-info "Read-only Git query failed" result)))
    (str/trim (:out result))))

(when-not (and (= "" (git-read root "status" "--porcelain"))
               (= "" (git-read controller-root "status" "--porcelain")))
  (throw (ex-info "Foreground diagnosis requires frozen compiler and controller revisions" {})))

(def revision (str/trim (slurp (io/file capture-dir "execution-revision"))))
(def actual-launcher
  ["java" "-Xmx2g" "-cp"
   "/workspace/scratch/0ca27623ede8/toolchains/clojure-1.12.0.1530/runtime/libexec/clojure-tools-1.12.0.1530.jar:bootstrap/clojure/src:tools:bootstrap/clojure/test"
   "clojure.main" "-m" "gravity.compiler-probe"])
(def receipt
  {:exitCode (Long/parseLong (str/trim (slurp (io/file capture-dir "exit-code"))))
   :stdout (slurp (io/file capture-dir "observation.edn"))
   :stderr (slurp (io/file capture-dir "probe.stderr"))
   :capture-truncated false
   :capture-provenance
   {:kind :fresh-foreground-subprocess :actual-argv actual-launcher :cwd root
    :source-revision revision
    :started-at (str/trim (slurp (io/file capture-dir "started-at")))
    :finished-at (str/trim (slurp (io/file capture-dir "finished-at")))
    :watchdog {:argv ["timeout" "--signal=TERM" "7200"] :seconds 7200}
    :configured-cli-executed false :collector-ingestion-does-not-rerun true}})
(def candidates (edn/read-string (slurp (io/file artifacts "current-candidates.edn"))))
(def config (assoc (main/config! (str root "/tools/seedless/config.edn")) :state-dir state-directory))
(def ctx (main/context! config))

(main/with-current-lock
 ctx
 (fn []
   (rt/event! ctx :foreground-production-entrypoint
              {:controller-revision (git-read controller-root "rev-parse" "HEAD")
               :controller-tree (git-read controller-root "rev-parse" "HEAD^{tree}")
               :compiler-revision revision
               :actual-launcher actual-launcher :ingest-existing-fresh-receipt true
               :campaign-started false :provider-writes false :candidate-count (count candidates)})
   (let [observation (compiler/observe!
                      (assoc-in ctx [:config :check-timeout-ms] (:compiler-observation-timeout-ms config))
                      root revision
                      (fn [argv]
                        (when-not (= compiler/command argv)
                          (throw (ex-info "Collector changed the declared route" {:argv argv})))
                        receipt))
         eligible (diagnosis/investigable? root observation compiler/command)
         result (workflow/diagnose! ctx root observation candidates)
         state @(:state ctx)
         summary {:controller-revision (git-read controller-root "rev-parse" "HEAD")
                  :compiler-revision revision :input-id (:input-id observation)
                  :actual-launcher actual-launcher
                  :collector-status (:status observation)
                  :actual-diagnostic (get-in observation [:failure :diagnostic])
                  :investigation-eligible eligible :result-status (:status result)
                  :diagnosis (:diagnosis result) :jobs (:jobs state)
                  :model-turn-completed (boolean (some #(= :completed (:status %)) (vals (:jobs state))))
                  :campaign-started false :provider-writes false :state-directory state-directory}]
     (spit (io/file artifacts "production-diagnosis-result.edn") (pr-str summary))
     (prn (select-keys summary [:controller-revision :compiler-revision :input-id :collector-status
                                :investigation-eligible :result-status :model-turn-completed
                                :campaign-started :provider-writes :state-directory])))))
(shutdown-agents)
