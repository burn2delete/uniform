(ns gravity.self-hosting.a1-canonical-schema.ordering
  "Bounded canonical map ordering for A1."
  (:require [gravity.self-hosting.a1-canonical-schema.budget :as budget]
            [gravity.self-hosting.a1-canonical-schema.config :as config]))

(defn merge-index-pass! [^objects entries ^ints source ^ints target n width]
  (doseq [start (range 0 n (* 2 width))]
    (let [middle (min n (+ start width))
          end (min n (+ start (* 2 width)))]
      (loop [left start right middle output start]
        (when (< output end)
          (cond
            (= left middle)
            (do (aset-int target output (aget source right))
                (recur left (inc right) (inc output)))

            (= right end)
            (do (aset-int target output (aget source left))
                (recur (inc left) right (inc output)))

            (not (pos? (config/byte-compare
                         (key (aget entries (aget source left)))
                         (key (aget entries (aget source right))))))
            (do (aset-int target output (aget source left))
                (recur (inc left) right (inc output)))

            :else
            (do (aset-int target output (aget source right))
                (recur left (inc right) (inc output)))))))))

(defn apply-index-order! [^objects entries ^ints order n]
  (dotimes [start n]
    (when-not (neg? (aget order start))
      (let [saved (aget entries start)]
        (loop [destination start]
          (let [source (aget order destination)]
            (aset-int order destination -1)
            (if (= source start)
              (aset entries destination saved)
              (do
                (aset entries destination (aget entries source))
                (recur source)))))))))

(defn bottom-up-mergesort [entries n]
  (let [ordered (object-array n)
        left (int-array n)
        right (int-array n)]
    (doseq [[index entry] (map-indexed vector entries)]
      (aset ordered index entry)
      (aset-int left index index))
    (loop [width 1 source left target right]
      (if (>= width n)
        (do (apply-index-order! ordered source n) ordered)
        (do
          (merge-index-pass! ordered source target n width)
          (recur (* 2 width) target source))))))

(defn ordered-entries [state value path consume]
  (let [n (count value)]
    (budget/work! state n path)
    (budget/acquire! state :key-slots n path)
    (try
      (let [entries (seq value)]
        (doseq [[key _] entries]
          (when-not (config/exact-class? key String)
            (config/fail! "E-TYPE" path))
          (when-not (config/scalar-string? key) (config/fail! "E-TYPE" path))
          (when (> (config/utf8-length key) (:string-bytes config/limits))
            (config/fail! "E-BOUND" path)))
        (let [key-bytes (reduce + 0 (map #(config/utf8-length (key %)) entries))
              rounds (loop [size (max 1 n) result 0]
                       (if (<= size 1) result
                           (recur (quot (inc size) 2) (inc result))))]
          (budget/work! state (* (+ n key-bytes) rounds) path)
          (consume (bottom-up-mergesort entries n))))
      (finally (budget/release! state :key-slots n)))))
