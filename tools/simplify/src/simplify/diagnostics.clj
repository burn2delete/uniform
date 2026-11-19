(ns simplify.diagnostics
  "Observations from saved campaign records. No controller lock or dispatch."
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [simplify.github :as gh]
            [simplify.runtime :as rt]))

(defn coordination-snapshot [config]
  (let [common (rt/common-git-directory (:repo-dir config))
        path (:coordination-file (rt/coordination common config))
        registry (rt/read-edn path)]
    (when-not (and (= 1 (:schema registry)) (map? (:writers registry))
                   (or (nil? (:restrictions registry)) (map? (:restrictions registry))))
      (rt/fail! "Cannot inspect shared coordination state" {:file path}))
    registry))

(defn restart-check [config state]
  (let [registry (coordination-snapshot config)
        local (:restricted-actions state {})
        shared (get-in registry [:restrictions :api] {})
        repo (str "repos/" (:repository config))]
    (when-not (every? map? [local shared])
      (rt/fail! "Cannot inspect recorded GitHub restrictions" {}))
    (let [blocked (vec (for [[step method endpoint] [[:lookup-project-owner "POST" "graphql"]
                                                    [:read-project-repository "GET" repo]
                                                    [:list-project-labels "GET" (str repo "/labels?per_page=100&page=1")]]
                             :let [key (rt/digest (pr-str [method endpoint]))
                                   sources (cond-> []
                                             (contains? local key) (conj :campaign)
                                             (or (get-in registry [:restrictions :api-denial key])
                                                 (and (contains? shared key)
                                                      (not (gh/settled-status-limit? (get shared key))))) (conj :shared))]
                             :when (seq sources)]
                         {:step step :method method :endpoint endpoint :key key :sources sources}))]
      {:scope :required-github-project-setup :campaign (:campaign config)
       :status (if (seq blocked) :blocked :no-recorded-blocker)
       :blockers blocked})))

(defn report-restart! [config state]
  (let [report (restart-check config state)]
    (prn report)
    (when (= :blocked (:status report))
      (rt/fail! "GitHub project setup is held" {}))))

(defn timeout-progress [result]
  (when (= 124 (:exitCode result))
    (let [frames (keep (fn [line]
                         (when (str/starts-with? line "{:schema :gravity/development-test-progress-v2,")
                           (try
                             (let [frame (edn/read-string line)]
                               (when (and (= :gravity/development-test-progress-v2 (:schema frame))
                                          (= :non-authoritative (:authority frame))
                                          (= :test-var (:phase frame))
                                          (string? (:test-var frame))
                                          (nat-int? (:elapsed-ns frame))
                                          (#{:enter :return :throw} (:edge frame)))
                                 (select-keys frame [:test-var :edge :elapsed-ns])))
                             (catch Exception _ nil))))
                       (str/split-lines (or (:stderr result) "")))]
      {:elapsed-ms (:elapsed-ms result)
       :capture-truncated (:capture-truncated result)
       :last-observed-test (last frames)
       :last-completed-test (last (filter #(= :return (:edge %)) frames))})))

(defn holds [state registry]
  (for [task (sort-by :key (vals (:tasks state)))
        :when (#{:attention :ready :repair} (:status task))
        :let [details (get-in task [:error :details])
              output (:output details)
              paths (distinct (concat (:paths task) (:blocking-paths output)
                                      (when (string? (:path details)) [(:path details)])))
              conflicts (for [path paths
                              writer (sort-by :id (vals (:writers registry)))
                              owned-path (:paths writer)
                              :when (rt/overlap? [path] [owned-path])]
                          {:path path :owned-path owned-path
                           :owner (select-keys writer [:key :campaign :state-dir :phase :status])})
              waiting (filterv #(not= :done (get-in state [:tasks % :status])) (:dependencies task))]
        :when (or (= :attention (:status task)) (seq conflicts) (seq waiting))]
    {:finding (:key task) :issue (get-in task [:issue :number]) :status (:status task)
     :reason (or (:summary output) (get-in task [:error :message]))
     :reported-blocking-paths (or (:blocking-paths output) (when (:path details) [(:path details)]))
     :writer-conflicts (vec conflicts) :waiting-on waiting
     :timeouts (vec (for [failure (concat (:failed-checks task) [(:error task)])
                          :let [details (:details failure)
                                progress (or (:timeout-progress details) (timeout-progress (:result details)))]
                          :when progress]
                      (assoc progress :candidate (:candidate failure) :command (:command details))))}))
