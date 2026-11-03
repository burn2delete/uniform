(ns gravity.c15-diagnostics.records
  (:require [gravity.diagnostics :as diagnostics]
            [gravity.c15-diagnostics.operations :as operations]
            [gravity.digest :as digest]))
(defn source-overrides [module]
  (diagnostics/source-overrides module))
(defn source-span [path index]
  (operations/invoke :source-span (fn [p i] {:source p :form-index i}) path index))
(defn stable-id [diagnostic]
  (diagnostics/stable-id #(operations/invoke :sha256-hex digest/sha256-hex %) diagnostic))
(defn diagnostic-record
  [rule severity stage message-key source-path form-index primary-artifact facts remediation
   & {:keys [related origin-chain redactions lifecycle generated? profile target syntax-id]}]
  (diagnostics/diagnostic-record
    source-span #(operations/invoke :c15-stable-diagnostic-id stable-id %)
    rule severity stage message-key source-path form-index primary-artifact facts remediation
    :related related :origin-chain origin-chain :redactions redactions :lifecycle lifecycle :generated? generated?
    :profile profile :target target :syntax-id syntax-id))
(defn catalog [configuration]
  (diagnostics/native-catalog (:c15-diagnostics-diagnostic-ids configuration)))
