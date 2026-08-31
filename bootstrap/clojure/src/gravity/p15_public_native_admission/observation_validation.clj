(ns gravity.p15-public-native-admission.observation-validation
  (:require [gravity.p15-public-native-admission.observation-replay-validation :refer [validate-observation-replay]]
            [gravity.p15-public-native-admission.observation-handoff-validation :refer [validate-observation-handoff]]))

(defn validate-observation
  [workstream pin observation]
  (into (validate-observation-replay workstream pin observation)
        (validate-observation-handoff workstream pin observation)))
