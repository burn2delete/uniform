(ns gravity.self-hosting.sh01-incremental-check
  "Changed-path development check backed by the existing SH-01 planner."
  (:gen-class)
  (:require [clojure.set :as set]
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
      (fn [{:keys [path classification slices]}]
        (array-map
         :path path
         :classification classification
         :invalidates-slices slices
         :effect (if (= :unrelated classification)
                   :ignored
                   :selected)))
      (:classifications plan))
     :direct-slices (vec (sort direct))
     :dependant-slices (vec (sort (set/difference affected direct)))
     :affected-slices (vec (sort affected))
     :selected-namespaces (:namespaces plan)
     :ignored-paths (:ignored-paths plan)
     :selection-rule
     "Changed paths select their SH-01 owners; affected slices include the existing downstream dependency closure.")))

(defn build-check-plan
  "Builds an explicitly non-authoritative plan for changed paths."
  [changed-paths]
  (assoc
   (planner/build-plan
    {:changed-paths changed-paths
     :expand-dependants? true})
   :authority :non-authoritative
   :non-authoritative? true
   :authoritative? false))

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
          (if (seq arguments)
            (do
              (binding [*out* *err*]
                (println "incremental-check accepts no arguments"))
              {:exit-code 2})
            (let [plan (build-check-plan (planner/changed-paths))]
              (println "SH-01 incremental developer check: non-authoritative")
              (prn (invalidation-explanation plan))
              (let [report (execute-check plan)]
                (prn report)
                report)))
          (finally
            (cleanup!)))]
    (when (pos? (long (:exit-code result)))
      (System/exit (:exit-code result)))
    result))
