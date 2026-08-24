(ns gravity.self-hosting.sh01-incremental-check
  "Changed-path development check backed by the existing SH-01 planner."
  (:gen-class)
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [gravity.self-hosting.sh01-impact-test-planner :as planner]
            [gravity.self-hosting.sh01-parallel-test-runner :as runner]))

(defn invalidation-explanation
  "Returns the planner selections in a compact, inspectable form."
  [plan]
  (let [direct (set (:direct-slices plan))
        affected (set (:affected-slices plan))]
    (array-map
     :schema :gravity/sh01-incremental-check-explanation-v1
     :authority :non-authoritative
     :non-authoritative? true
     :authoritative? false
     :changed-paths (:changed-paths plan)
     :path-invalidations
     (mapv
      (fn [{:keys [path classification slices development-selection
                   development-invalidation-reason
                   development-test-namespaces]}]
        (array-map
         :path path
         :classification classification
         :governance-slices slices
         :invalidates-slices slices
         :development-selection development-selection
         :development-invalidation-reason development-invalidation-reason
         :selected-test-namespaces
         (vec (or development-test-namespaces []))
         :effect (if (= :unrelated classification)
                   :ignored
                   :selected)))
      (:classifications plan))
     :direct-slices (vec (sort direct))
     :dependant-slices (vec (sort (set/difference affected direct)))
     :affected-slices (vec (sort affected))
     :execution-direct-slices (:execution-direct-slices plan)
     :execution-affected-slices (:execution-affected-slices plan)
     :selected-namespaces (:namespaces plan)
     :ignored-paths (:ignored-paths plan)
     :change-discovery (:change-discovery plan)
     :selection-rule
     "Governance slices retain SH-01 ownership closure; reviewed component and dedicated test paths select exact development tests, while other owned paths retain conservative slice closure.")))

(defn build-check-plan
  "Builds an explicitly non-authoritative plan for changed paths."
  ([changed-paths]
   (build-check-plan changed-paths nil))
  ([changed-paths change-discovery]
   (cond->
    (assoc
     (planner/build-plan
      {:changed-paths changed-paths
       :expand-dependants? true})
     :authority :non-authoritative
     :non-authoritative? true
     :authoritative? false)
     change-discovery
     (assoc :change-discovery change-discovery))))

(defn execute-check
  "Executes exactly the namespaces selected by an SH-01 development plan."
  [plan]
  (let [explanation (invalidation-explanation plan)
        execution
        (if (seq (:namespaces plan))
          (runner/execute-plan plan)
          {:schema :gravity/sh01-parallel-test-report-v1
           :plan-schema (:schema plan)
           :authority :non-authoritative
           :non-authoritative? true
           :authoritative? false
           :status :passed
           :ok? true
           :exit-code 0
           :complete? true
           :reason :no-selected-work
           :results []})]
    (array-map
     :schema :gravity/sh01-incremental-check-report-v1
     :authority :non-authoritative
     :non-authoritative? true
     :authoritative? false
     :status (:status execution)
     :ok? (:ok? execution)
     :exit-code (:exit-code execution)
     :plan plan
     :invalidation-explanation explanation
     :execution execution)))

(defn run-check
  "Plans and runs the non-authoritative development work for changed paths."
  ([]
   (run-check (planner/changed-paths)))
  ([changed-paths]
   (execute-check (build-check-plan changed-paths))))

(defn run-check-from-base
  "Discovers and checks committed plus working changes from an explicit base."
  [base-ref]
  (let [discovery (planner/change-discovery base-ref)]
    (execute-check
     (build-check-plan (:changed-paths discovery) discovery))))

(defn- parse-arguments
  [arguments]
  (let [arguments (vec arguments)]
    (cond
      (empty? arguments) {:base-ref nil}

      (and (= 2 (count arguments))
           (= "--base-ref" (first arguments))
           (string? (second arguments))
           (not (str/blank? (second arguments))))
      {:base-ref (second arguments)}

      :else
      (throw
       (ex-info
        "incremental-check accepts only --base-ref REF"
        {:id "SH01-INCREMENTAL-USAGE"
         :arguments arguments})))))

(defn- cleanup!
  []
  (flush)
  (.flush *err*)
  (.flush System/out)
  (.flush System/err)
  (shutdown-agents))

(defn -main
  [& arguments]
  (let [result
        (try
          (let [{:keys [base-ref]} (parse-arguments arguments)
                discovery (planner/change-discovery base-ref)
                plan (build-check-plan (:changed-paths discovery) discovery)]
              (println "SH-01 incremental developer check: non-authoritative")
              (prn (invalidation-explanation plan))
              (let [report (execute-check plan)]
                (prn report)
                report))
          (catch clojure.lang.ExceptionInfo exception
            (binding [*out* *err*]
              (println (.getMessage exception))
              (prn (ex-data exception)))
            {:exit-code 2 :error (ex-data exception)})
          (finally
            (cleanup!)))]
    (when (pos? (long (:exit-code result)))
      (System/exit (:exit-code result)))
    result))
