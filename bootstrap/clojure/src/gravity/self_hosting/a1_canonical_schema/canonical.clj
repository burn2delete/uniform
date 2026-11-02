(ns gravity.self-hosting.a1-canonical-schema.canonical
  "Closed-value validation and construction for A1."
  (:require [gravity.self-hosting.a1-canonical-schema.config :as config]
            [gravity.self-hosting.a1-canonical-schema.ordering :as ordering]))

(def ^:dynamic *construct-vector* (fn [items] (vec items)))
(def ^:dynamic *construct-map*
  (fn [entries]
    (reduce (fn [result [key value]] (assoc result key value))
            clojure.lang.PersistentHashMap/EMPTY entries)))

(declare validate-closed-value!)

(defn- validate-string! [value path]
  (when-not (config/scalar-string? value) (config/fail! "E-TYPE" path))
  value)

(defn validate-closed-value! [value path]
  (cond
    (nil? value) value
    (config/exact-class? value Boolean) value
    (config/uint64? value) value
    (config/exact-class? value String) (validate-string! value path)

    (config/canonical-vector? value)
    (do
      (config/no-metadata! value path)
      (doseq [[index item] (map-indexed vector value)]
        (validate-closed-value! item (config/path-child path index)))
      value)

    (config/canonical-map? value)
    (do
      (config/no-metadata! value path)
      (ordering/ordered-entries
        value path
        (fn [entries]
          (doseq [[key item] entries]
            (validate-closed-value! key (config/path-child path key))
            (validate-closed-value! item (config/path-child path key)))))
      value)

    :else (config/fail! "E-TYPE" path)))

(declare copy-value!)

(defn copy-value! [value path]
  (cond
      (nil? value) nil
      (config/exact-class? value Boolean) value
      (config/uint64? value)
      (config/canonical-uint value)
      (config/exact-class? value String) value
      (config/canonical-vector? value)
      (*construct-vector*
        (map-indexed (fn [index item]
                       (copy-value! item (config/path-child path index)))
                     value))
      (config/canonical-map? value)
      (*construct-map*
        (ordering/ordered-entries
          value path
          #(map (fn [[key item]]
                  [(copy-value! key (config/path-child path key))
                   (copy-value! item (config/path-child path key))]) %)))
      :else (config/fail! "E-TYPE" path)))
