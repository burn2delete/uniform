(ns gravity.c15-diagnostics.projection
  (:require [gravity.diagnostics :as stage]))
(defn artifact-base [configuration module source-overrides lowering-artifact diagnostics catalog]
  (stage/native-artifact-base configuration module source-overrides lowering-artifact diagnostics catalog))
