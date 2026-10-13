
(defn- unsigned-wire-sort
  "Return ROWS as a vector sorted by unsigned lexicographic W bytes of
  (KEY-FN row).  Duplicate encoded keys reject before sorting."
  [key-fn rows]
  (when-not (ifn? key-fn)
    (fail! :key-function-required
           {:actual-class (some-> key-fn class .getName)}))
  (when-not (vector? rows)
    (fail! :row-vector-required
           {:actual-class (some-> rows class .getName)}))
  (let [decorated (mapv (fn [row] [(W (key-fn row)) row]) rows)
        encoded-keys (mapv (comp vec first) decorated)]
    (when-not (= (count encoded-keys) (count (set encoded-keys)))
      (fail! :duplicate-wire-sort-key {:row-count (count rows)}))
    (->> decorated
         (sort (fn [[left] [right]]
                 (unsigned-byte-compare left right)))
         (mapv second))))

(def transport-api
  "Immutable non-function API value for the codec's bounded transport and
  unsigned-ordering substrate.  H, W, and R are intentionally the only public
  function Vars in this namespace."
  {:strict-read-wire strict-read-wire
   :bounded-wire-bytes bounded-wire-bytes
   :read-bounded-bytes read-bounded-bytes
   :read-bounded-frame read-bounded-frame
   :bounded-byte-count! bounded-byte-count!
   :unsigned-byte-compare unsigned-byte-compare
   :unsigned-wire-sort unsigned-wire-sort})
