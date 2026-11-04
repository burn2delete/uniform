(ns gravity.c14-lowering.projection
  (:require [gravity.lowering :as lowering]
            [gravity.c14-lowering.operations :as operations]
            [gravity.digest]))
(defn- sha256-hex [value] (operations/invoke :sha256-hex gravity.digest/sha256-hex value))
(defn- source-span [path index]
  (operations/invoke :source-span (fn [p i] {:source p :form-index i}) path index))
(defn artifact-base [configuration source-path source-text module source-overrides optimization-artifact diagnostics]
  (lowering/native-artifact-base configuration source-path source-text module source-overrides
                                 optimization-artifact diagnostics
                                 {:sha256-hex (fn [] sha256-hex) :source-span (fn [] source-span)}))
