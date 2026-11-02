(ns gravity.c7-type-checker.diagnostics
  "Compatibility entry points for native C7 diagnostic projection."
  (:require [gravity.types :as types]))

(defn type-fail! [fail! source-span type-message governing-document id source-path subject extra]
  (types/native-type-fail! fail! source-span type-message governing-document id source-path subject extra))

(defn validate-overrides! [source-span type-fail! override-diagnostics source-path module overrides]
  (types/validate-native-overrides! source-span type-fail! override-diagnostics source-path module overrides))

(defn type-diagnostics [source-span diagnostic-ids rejected-designs source-path nodes]
  (types/native-type-diagnostics source-span diagnostic-ids rejected-designs source-path nodes))
