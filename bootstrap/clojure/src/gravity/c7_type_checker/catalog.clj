(ns gravity.c7-type-checker.catalog
  "Compatibility defaults for the native C7 diagnostic catalog."
  (:require [gravity.types :as types]))

(def diagnostic-ids types/native-diagnostic-ids)

(def governing-document types/native-governing-document)

(def rejected-designs types/native-rejected-designs)

(def override-diagnostics types/native-override-diagnostics)

(defn type-message [id]
  (types/native-type-message id))
