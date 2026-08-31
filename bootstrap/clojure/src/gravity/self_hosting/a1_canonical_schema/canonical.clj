(ns gravity.self-hosting.a1-canonical-schema.canonical
  "Closed-value metering, construction, copying, and output sizing for A1."
  (:require [gravity.self-hosting.a1-canonical-schema.budget :as budget]
            [gravity.self-hosting.a1-canonical-schema.config :as config]
            [gravity.self-hosting.a1-canonical-schema.ordering :as ordering]))

(def ^:dynamic *construct-vector* (fn [items] (vec items)))
(def ^:dynamic *construct-map*
  (fn [entries]
    (reduce (fn [result [key value]] (assoc result key value))
            clojure.lang.PersistentHashMap/EMPTY entries)))

(declare meter-value!)

(defn meter-string! [state value path counter]
  (when-not (config/scalar-string? value) (config/fail! "E-TYPE" path))
  (let [size (config/utf8-length value)]
    (when (> size (:string-bytes config/limits)) (config/fail! "E-BOUND" path))
    (budget/charge! state counter (+ 5 size) path)
    (+ 5 size)))

(defn meter-value! [state value path depth counter]
  (when (> depth (:depth config/limits)) (config/fail! "E-BOUND" path))
  (budget/acquire! state :frames 1 path)
  (try
    (budget/work! state 1 path)
    (cond
      (nil? value) (do (budget/charge! state counter 1 path) 1)
      (config/exact-class? value Boolean)
      (do (budget/charge! state counter 2 path) 2)
      (config/uint64? value) (do (budget/charge! state counter 9 path) 9)
      (config/exact-class? value String) (meter-string! state value path counter)

      (config/canonical-vector? value)
      (do
        (config/no-metadata! value path)
        (let [n (count value)]
          (when (> n (:items config/limits)) (config/fail! "E-BOUND" path))
          (budget/charge! state counter 5 path)
          (+ 5 (reduce + 0
                       (map-indexed
                         (fn [index item]
                           (meter-value! state item (config/path-child path index)
                                         (inc depth) counter))
                         value)))))

      (config/canonical-map? value)
      (do
        (config/no-metadata! value path)
        (let [n (count value)]
          (when (> n (:items config/limits)) (config/fail! "E-BOUND" path))
          (budget/charge! state counter 5 path)
          (+ 5 (ordering/ordered-entries
                 state value path
                 #(reduce + 0
                          (map (fn [[key item]]
                                 (+ (meter-value! state key
                                                  (config/path-child path key)
                                                  (inc depth) counter)
                                    (meter-value! state item
                                                  (config/path-child path key)
                                                  (inc depth) counter))) %))))))

      :else (config/fail! "E-TYPE" path))
    (finally (budget/release! state :frames 1))))

(declare copy-value!)

(defn copy-value! [state value path depth]
  (when (> depth (:depth config/limits)) (config/fail! "E-BOUND" path))
  (budget/acquire! state :frames 1 path)
  (try
    (budget/work! state 1 path)
    (cond
      (nil? value) nil
      (config/exact-class? value Boolean) value
      (config/uint64? value)
      (do
        (when (and (config/exact-class? value clojure.lang.BigInt)
                   (<= value Long/MAX_VALUE))
          (budget/work! state 1 path))
        (config/canonical-uint value))
      (config/exact-class? value String) value
      (config/canonical-vector? value)
      (do
        (budget/work! state 1 path)
        (*construct-vector*
          (map-indexed (fn [index item]
                         (copy-value! state item (config/path-child path index)
                                      (inc depth)))
                       value)))
      (config/canonical-map? value)
      (do
        (budget/work! state 1 path)
        (ordering/ordered-entries
          state value path
          #(*construct-map*
             (map (fn [[key item]]
                    [(copy-value! state key (config/path-child path key)
                                  (inc depth))
                     (copy-value! state item (config/path-child path key)
                                  (inc depth))]) %))))
      :else (config/fail! "E-TYPE" path))
    (finally (budget/release! state :frames 1))))

(declare measure-value!)

(defn checked-size [left right path]
  (let [sum (+ left right)]
    (when (> sum (:output-bytes config/limits))
      (config/fail! "E-BOUND" path))
    sum))

(defn measure-value! [state value path depth]
  (when (> depth (:depth config/limits)) (config/fail! "E-BOUND" path))
  (budget/acquire! state :frames 1 path)
  (try
    (budget/work! state 1 path)
    (cond
      (nil? value) 1
      (config/exact-class? value Boolean) 2
      (config/uint64? value) 9
      (config/exact-class? value String) (+ 5 (config/utf8-length value))
      (config/canonical-vector? value)
      (reduce (fn [size [index item]]
                (checked-size size
                              (measure-value! state item
                                              (config/path-child path index)
                                              (inc depth))
                              path))
              5 (map-indexed vector value))
      (config/canonical-map? value)
      (ordering/ordered-entries
        state value path
        #(reduce (fn [size [key item]]
                   (checked-size
                     (checked-size size
                                   (measure-value! state key
                                                   (config/path-child path key)
                                                   (inc depth))
                                   path)
                     (measure-value! state item (config/path-child path key)
                                     (inc depth))
                     path))
                 5 %))
      :else (config/fail! "E-HOST" (config/path-of "internal")))
    (finally (budget/release! state :frames 1))))

(defn finish-copy! [state value]
  (let [size (measure-value! state value nil 0)]
    (budget/reserve! state :output size nil)
    (budget/commit! state :output size)
    (copy-value! state value nil 0)))
