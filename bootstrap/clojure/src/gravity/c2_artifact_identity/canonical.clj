(ns gravity.c2-artifact-identity.canonical)

(defn reader-canonical-value [canonical-value value]
  (cond
    (map? value)
    (let [decorated (mapv (fn [[key item]]
                            (let [entry [(canonical-value key) (canonical-value item)]]
                              (clojure.lang.MapEntry/create (pr-str entry) entry)))
                          value)]
      [:map (->> decorated (sort-by key) (mapv val))])

    (set? value)
    (let [decorated (mapv (fn [item]
                            (let [entry (canonical-value item)]
                              (clojure.lang.MapEntry/create (pr-str entry) entry)))
                          value)]
      [:set (mapv val (sort-by key decorated))])

    (vector? value) [:vector (mapv canonical-value value)]
    (seq? value) [:list (mapv canonical-value value)]
    :else value))

(defn reader-canonical-hash [sha256-hex canonical-value value]
  (str "sha256:"
       (sha256-hex
        (binding [*print-length* nil *print-level* nil *print-meta* true]
          (pr-str (canonical-value value))))))
