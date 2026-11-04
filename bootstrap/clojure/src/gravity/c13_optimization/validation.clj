(ns gravity.c13-optimization.validation
  (:require [gravity.optimization :as optimization]
            [gravity.c13-optimization.operations :as operations]
            [gravity.optimization-lowering :as shared]))

(defn source-overrides [module]
  (optimization/native-source-overrides module))

(defn source-overrides-artifact [overrides]
  (optimization/native-source-overrides-artifact overrides))

(defn diagnostic-catalog [configuration source-path source-span]
  (optimization/native-diagnostic-catalog configuration source-path source-span))

(defn- fail! [id source-path artifact subject evidence]
  (operations/invoke :optimization-lowering-fail! shared/optimization-lowering-fail!
                     id source-path artifact subject evidence))

(defn- perf-present? [value]
  (operations/invoke :perf-present?
                     (fn [candidate]
                       (and (some? candidate)
                            (not (and (coll? candidate) (empty? candidate)))))
                     value))

(defn validate! [configuration source-path artifact]
  (optimization/native-validate! configuration source-path artifact
    {:invoke (fn [] operations/invoke)
     :validate-overrides (fn [] shared/optimization-lowering-validate-overrides!)
     :fail! (fn [] fail!) :perf-present? (fn [] perf-present?)}))

(defn capability-proof [configuration artifact]
  (optimization/native-capability-proof configuration artifact))
