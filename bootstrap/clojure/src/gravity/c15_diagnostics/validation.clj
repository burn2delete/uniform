(ns gravity.c15-diagnostics.validation
  (:require [gravity.diagnostics :as diagnostics]
            [gravity.c15-diagnostics.operations :as operations]
            [gravity.c15-diagnostics.records :as records]))
(defn context-valid? [diagnostic]
  (diagnostics/native-context-valid? diagnostic))
(defn primary-valid? [diagnostic]
  (diagnostics/native-primary-valid? diagnostic))
(defn fail! [configuration id source-path subject extra]
  (diagnostics/native-failure! configuration id source-path subject extra
    {:invoke (fn [] operations/invoke) :source-span (fn [] records/source-span)}))

(defn validate-source-overrides! [configuration source-path overrides]
  (diagnostics/native-validate-source-overrides! configuration source-path overrides
    {:fail! (fn [] fail!)}))

(defn validate! [configuration source-path artifact]
  (diagnostics/validate-native!
   {:fail! #'fail! :stable-id #'records/stable-id
    :context-valid? #'context-valid? :primary-valid? #'primary-valid?}
   configuration source-path artifact))
