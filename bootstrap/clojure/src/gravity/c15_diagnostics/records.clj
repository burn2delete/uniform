(ns gravity.c15-diagnostics.records
  (:require [clojure.string]
            [gravity.c15-diagnostics.operations :as operations]
            [gravity.digest :as digest]))
(defn source-overrides [module]
  (or (get-in module [:metadata :compiler :c15-diagnostics]) (get-in module [:metadata :compiler :verification]) {}))
(defn source-span [path index]
  (operations/invoke :source-span (fn [p i] {:source p :form-index i}) path index))
(defn stable-id [diagnostic]
  (str "diag-" (operations/invoke :sha256-hex digest/sha256-hex
                                  (pr-str {:rule (:rule diagnostic) :stage (:stage diagnostic)
                                           :primary-artifact (get-in diagnostic [:primary :artifact])
                                           :facts (:facts diagnostic)}))))
(defn diagnostic-record
  [rule severity stage message-key source-path form-index primary-artifact facts remediation
   & {:keys [related origin-chain redactions lifecycle generated?]}]
  (let [diagnostic {:artifact :gravity/diagnostic :rule rule :severity severity :stage stage
                    :message-key message-key
                    :primary {:span (source-span source-path form-index)
                              :syntax-id (str "c15-syntax-" form-index) :artifact primary-artifact}
                    :related (vec related) :origin-chain (vec origin-chain) :profile :hosted :target :jvm
                    :involved-artifacts [primary-artifact] :facts facts :remediation (vec remediation)
                    :redactions (vec redactions) :lifecycle (or lifecycle :active) :generated? (true? generated?)}]
    (assoc diagnostic :diagnostic-id (operations/invoke :c15-stable-diagnostic-id stable-id diagnostic)
           :ordering-key [rule stage primary-artifact form-index])))
(defn catalog [configuration]
  {:artifact :gravity/diagnostic-catalog :status :complete
   :rules (mapv (fn [id] {:rule id :severity (if (= "C15-GOLDEN" id) :hint :error)
                          :message-key (keyword "diagnostic" (clojure.string/lower-case
                                                               (clojure.string/replace id #"_" "-")))
                          :explain-page (str "gravity://diagnostics/" id) :lifecycle :active
                          :stable-id-policy :rule-primary-artifact-stage-facts})
                (:c15-diagnostics-diagnostic-ids configuration))})
