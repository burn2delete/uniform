(ns gravity.c14-lowering.validation
  (:require [gravity.lowering :as lowering]
            [gravity.c14-lowering.operations :as operations]
            [gravity.optimization-lowering :as shared]))

(defn source-overrides [module]
  (lowering/native-source-overrides module))

(defn source-overrides-artifact [overrides]
  (lowering/native-source-overrides-artifact overrides))

(defn diagnostic-catalog [configuration source-path input-id source-span]
  (lowering/native-diagnostic-catalog configuration source-path input-id source-span))

(defn- perf-present? [value]
  (operations/invoke :perf-present?
                     (fn [candidate] (and (some? candidate) (not (and (coll? candidate) (empty? candidate)))))
                     value))
(defn- fail! [id path artifact subject extra]
  (operations/invoke :optimization-lowering-fail! shared/optimization-lowering-fail!
                     id path artifact subject extra))
(defn validate! [configuration source-path artifact]
  (lowering/native-validate! configuration source-path artifact
    {:invoke (fn [] operations/invoke)
     :validate-overrides (fn [] shared/optimization-lowering-validate-overrides!)
     :fail! (fn [] fail!) :perf-present? (fn [] perf-present?)}))

(defn capability-proof [configuration artifact]
  (lowering/native-capability-proof configuration artifact
    {:perf-present? (fn [] perf-present?)}))
