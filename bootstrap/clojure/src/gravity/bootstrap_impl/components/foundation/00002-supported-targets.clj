
(def supported-targets #{:jvm})
(def ^:dynamic *additional-bootstrap-targets* #{})

(defn bootstrap-target-supported?
  [target]
  (contains? (set/union supported-targets *additional-bootstrap-targets*)
             target))