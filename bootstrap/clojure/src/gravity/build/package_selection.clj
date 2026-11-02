(ns gravity.build.package-selection
  "Compatibility entry point; package selection is owned by gravity.package."
  (:require [gravity.build.package-policy :as policy]
            [gravity.package :as package]))

(defn select-exact [request]
  (package/select-exact #'policy/decide request))

(alter-meta! #'select-exact assoc :arglists '([{:keys [constraints candidates locked-records mode context allowed-effects allowed-capabilities unsafe-policy], :as request}]))
