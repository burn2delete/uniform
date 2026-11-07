(ns gravity.module-analysis.dependencies
  (:require [gravity.resolution.stage0-namespace :as stage]))

(defn parse-options
  [{:keys [fail! source-span]} source-path entry option-items]
  (stage/parse-options {:fail! fail!, :source-span source-span} source-path entry option-items))

(defn parse-dependency-entry
  [{:keys [fail! source-span parse-options]} source-path kind entry]
  (stage/parse-dependency-entry {:fail! fail!, :source-span source-span, :parse-options parse-options} source-path kind entry))

(defn parse-dependencies
  [{:keys [parse-dependency-entry]} source-path kind entries]
  (stage/parse-dependencies {:parse-dependency-entry parse-dependency-entry} source-path kind entries))
