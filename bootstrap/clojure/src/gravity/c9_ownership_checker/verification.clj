(ns gravity.c9-ownership-checker.verification
  "Diagnostic, verifier, proof, and validation projections for hosted C9."
  (:require [gravity.ownership :as ownership]))

(defn ownership-diagnostics [source-span diagnostic-ids rejected-designs source-path ownership]
  (ownership/native-ownership-diagnostics source-span diagnostic-ids rejected-designs source-path ownership))

(defn linear-paths-exact? [linear]
  (ownership/native-linear-paths-exact? linear))

(defn verifier-report [linear-paths-exact? diagnostic-ids c8-artifact ownership borrow lifetimes moves escape region arena linear transfer runtime unsafe diagnostics]
  (ownership/native-verifier-report linear-paths-exact? diagnostic-ids c8-artifact ownership borrow lifetimes moves escape region arena linear transfer runtime unsafe diagnostics))

(defn ownership-capability-proof [artifact]
  (ownership/native-ownership-capability-proof artifact))

(defn validate! [capability-proof fail! source-path artifact]
  (ownership/native-validate! capability-proof fail! source-path artifact))
