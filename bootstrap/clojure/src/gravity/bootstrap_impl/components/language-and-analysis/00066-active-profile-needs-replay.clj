

(defn active-profile-needs-replay?
  [ctx]
  (contains? #{:distributed :ai} (:profile @ctx)))