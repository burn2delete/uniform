(ns gravity.build.package-policy
  "Compatibility entry point; package policy is owned by gravity.package."
  (:require [gravity.package :as package]))

(defn decide [request]
  (package/decide-policy request))

(alter-meta! #'decide assoc :arglists '([{:keys [record locked-records mode policy registry visibility registry-grants requested-range constraint-chain profile target]}]))
