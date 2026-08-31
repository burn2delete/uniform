

(defn effect-registry-entry
  [effect]
  (get effect-registry effect))

(defn build-effect?
  [effect]
  (= :build (:kind (effect-registry-entry effect))))

(defn replay-sensitive-effect?
  [effect]
  (true? (:replay-record (effect-registry-entry effect))))