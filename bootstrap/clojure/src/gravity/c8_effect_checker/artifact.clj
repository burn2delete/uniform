(ns gravity.c8-effect-checker.artifact
  "Compatibility assembly and existing native C8 file intake."
  (:require [gravity.effects :as effects]))

(defn source-artifact [ops source-path source-text]
  (effects/assemble-source-artifact ops source-path source-text))

(defn file-artifact [source-artifact path]
  (source-artifact path (slurp path)))
