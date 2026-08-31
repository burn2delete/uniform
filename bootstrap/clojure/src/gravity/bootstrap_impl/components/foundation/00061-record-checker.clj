

(defn record-checker!
  [checker key value]
  (swap! checker update key conj value)
  value)