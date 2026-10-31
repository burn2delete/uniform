(ns gravity.c15-diagnostics.records
  (:require [clojure.string]
            [gravity.diagnostics :as diagnostics]
            [gravity.c15-diagnostics.operations :as operations]
            [gravity.digest :as digest]))
(defn source-overrides [module]
  (or (get-in module [:metadata :compiler :c15-diagnostics]) (get-in module [:metadata :compiler :verification]) {}))
(defn source-span [path index]
  (operations/invoke :source-span (fn [p i] {:source p :form-index i}) path index))
(defn stable-id [diagnostic]
  (diagnostics/stable-id #(operations/invoke :sha256-hex digest/sha256-hex %) diagnostic))
(defn diagnostic-record
  [rule severity stage message-key source-path form-index primary-artifact facts remediation
   & {:keys [related origin-chain redactions lifecycle generated?]}]
  (diagnostics/diagnostic-record
    source-span #(operations/invoke :c15-stable-diagnostic-id stable-id %)
    rule severity stage message-key source-path form-index primary-artifact facts remediation
    :related related :origin-chain origin-chain :redactions redactions :lifecycle lifecycle :generated? generated?))
(defn catalog [configuration]
  {:artifact :gravity/diagnostic-catalog :status :complete
   :rules (mapv (fn [id] {:rule id :severity (if (= "C15-GOLDEN" id) :hint :error)
                          :message-key (keyword "diagnostic" (clojure.string/lower-case
                                                               (clojure.string/replace id #"_" "-")))
                          :explain-page (str "gravity://diagnostics/" id) :lifecycle :active
                          :stable-id-policy :rule-primary-artifact-stage-facts})
                (:c15-diagnostics-diagnostic-ids configuration))})
