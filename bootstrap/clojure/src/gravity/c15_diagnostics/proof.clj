(ns gravity.c15-diagnostics.proof
  (:require [gravity.diagnostics :as stage]
            [gravity.c15-diagnostics.records :as records]
            [gravity.c15-diagnostics.validation :as validation]))
(defn capability-proof [configuration artifact]
  (stage/native-capability-proof configuration artifact
    {:stable-id (fn [] records/stable-id) :context-valid? (fn [] validation/context-valid?)
     :primary-valid? (fn [] validation/primary-valid?)}))
