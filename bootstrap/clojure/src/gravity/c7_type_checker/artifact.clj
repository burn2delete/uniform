(ns gravity.c7-type-checker.artifact
  "Compatibility assembly and existing native C7 file intake."
  (:require [gravity.types :as types]))

(defn source-artifact [ops source-path source-text]
  (types/assemble-source-artifact ops source-path source-text))

(defn file-artifact
  [source-artifact path]
  (source-artifact path (slurp path)))
