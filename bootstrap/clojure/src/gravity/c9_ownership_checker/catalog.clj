(ns gravity.c9-ownership-checker.catalog
  "Stable C9 diagnostic and override catalog for the hosted facade."
  (:require [gravity.ownership :as ownership]))

(def diagnostic-ids ownership/native-diagnostic-ids)

(def governing-document ownership/native-governing-document)

(def rejected-designs ownership/native-rejected-designs)

(def override-diagnostics ownership/native-override-diagnostics)

(defn ownership-message [id]
  (ownership/native-ownership-message id))
