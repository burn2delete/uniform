

(defn p15-s23-seed-readable-preflight!
  [source-path args]
  (let [{:keys [maximum-arguments maximum-nodes maximum-depth
                maximum-collection-width maximum-scalar-bytes
                maximum-output-bytes]}
        p15-s23-seed-readable-printer-limits
        active (java.util.IdentityHashMap.)
        stats (volatile! {:node-count 0
                          :scalar-bytes 0
                          :maximum-depth 0})]
    (when-not (identical? clojure.lang.PersistentVector (class args))
      (p15-s23-seed-readable-printer-fail!
       source-path :unsupported-argument-carrier
       {:expected-argument-carrier :persistent-vector}))
    (when (> (count args) maximum-arguments)
      (p15-s23-seed-readable-printer-fail!
       source-path :argument-count-limit
       {:observed-arguments (count args)
        :maximum-arguments maximum-arguments}))
    (letfn [(fail-width! [observed]
              (p15-s23-seed-readable-printer-fail!
               source-path :collection-width-limit
               {:observed-width observed
                :maximum-collection-width maximum-collection-width}))
            (record-node! [depth scalar-bytes]
              (let [next-stats
                    (vswap! stats
                            (fn [{:keys [node-count] :as current}]
                              (-> current
                                  (assoc :node-count (inc node-count))
                                  (update :scalar-bytes + scalar-bytes)
                                  (update :maximum-depth max depth))))
                    node-count (:node-count next-stats)
                    total-scalar-bytes (:scalar-bytes next-stats)]
                (when (> node-count maximum-nodes)
                  (p15-s23-seed-readable-printer-fail!
                   source-path :node-count-limit
                   {:observed-nodes node-count
                    :maximum-nodes maximum-nodes}))
                (when (> depth maximum-depth)
                  (p15-s23-seed-readable-printer-fail!
                   source-path :depth-limit
                   {:observed-depth depth
                    :maximum-depth maximum-depth}))
                (when (> total-scalar-bytes maximum-scalar-bytes)
                  (p15-s23-seed-readable-printer-fail!
                   source-path :scalar-byte-limit
                   {:observed-scalar-bytes total-scalar-bytes
                    :maximum-scalar-bytes maximum-scalar-bytes}))
                (when (> (+ (* total-scalar-bytes 6) (* node-count 4))
                         maximum-output-bytes)
                  (p15-s23-seed-readable-printer-fail!
                   source-path :prospective-output-byte-limit
                   {:maximum-output-bytes maximum-output-bytes}))))
            (snapshot-value! [value depth]
              (let [kind (p15-s23-seed-readable-value-kind source-path value)
                    collection? (contains? #{:vector :list :map :set} kind)]
                (record-node! depth 0)
                (if-not collection?
                  (let [snapshot
                        (p15-s23-seed-readable-snapshot-scalar!
                         source-path kind value)
                        scalar-bytes
                        (p15-s23-seed-readable-scalar-bytes!
                         source-path kind snapshot)]
                    (vswap! stats update :scalar-bytes + scalar-bytes)
                    (let [{:keys [node-count scalar-bytes]} @stats]
                      (when (> scalar-bytes maximum-scalar-bytes)
                        (p15-s23-seed-readable-printer-fail!
                         source-path :scalar-byte-limit
                         {:observed-scalar-bytes scalar-bytes
                          :maximum-scalar-bytes maximum-scalar-bytes}))
                      (when (> (+ (* scalar-bytes 6) (* node-count 4))
                               maximum-output-bytes)
                        (p15-s23-seed-readable-printer-fail!
                         source-path :prospective-output-byte-limit
                         {:maximum-output-bytes maximum-output-bytes})))
                    snapshot)
                  (do
                    (when (.containsKey active value)
                      (p15-s23-seed-readable-printer-fail!
                       source-path :cyclic-value
                       {:observed-depth depth}))
                    (.put active value Boolean/TRUE)
                    (try
                      (case kind
                        :vector
                        (let [width (count value)]
                          (when (> width maximum-collection-width)
                            (fail-width! width))
                          (loop [index 0
                                 snapshot []]
                            (if (= index width)
                              snapshot
                              (recur
                               (inc index)
                               (conj snapshot
                                     (snapshot-value!
                                      (nth value index) (inc depth)))))))

                        :list
                        (loop [cursor (seq value)
                               width 0
                               snapshot []]
                          (if (nil? cursor)
                            (apply list snapshot)
                            (do
                              (when (>= width maximum-collection-width)
                                (fail-width! (inc width)))
                              (when-not
                               (or (identical? clojure.lang.PersistentList
                                               (class cursor))
                                   (identical?
                                    clojure.lang.PersistentList$EmptyList
                                    (class cursor)))
                                (p15-s23-seed-readable-printer-fail!
                                 source-path :unsupported-list-tail-carrier {}))
                              (recur
                               (next cursor)
                               (inc width)
                               (conj snapshot
                                     (snapshot-value!
                                      (first cursor) (inc depth)))))))

                        :set
                        (loop [cursor (seq value)
                               width 0
                               snapshot []]
                          (if (nil? cursor)
                            (let [result (into #{} snapshot)]
                              (when-not (= (count result) width)
                                (p15-s23-seed-readable-printer-fail!
                                 source-path :snapshot-collision {}))
                              result)
                            (do
                              (when (>= width maximum-collection-width)
                                (fail-width! (inc width)))
                              (recur
                               (next cursor)
                               (inc width)
                               (conj snapshot
                                     (snapshot-value!
                                      (first cursor) (inc depth)))))))

                        :map
                        (loop [cursor (seq value)
                               width 0
                               snapshot []]
                          (if (nil? cursor)
                            (let [result (into {} snapshot)]
                              (when-not (= (count result) width)
                                (p15-s23-seed-readable-printer-fail!
                                 source-path :snapshot-collision {}))
                              result)
                            (do
                              (when (>= width maximum-collection-width)
                                (fail-width! (inc width)))
                              (let [entry (first cursor)]
                                (when-not
                                 (identical? clojure.lang.MapEntry
                                             (class entry))
                                  (p15-s23-seed-readable-printer-fail!
                                   source-path :unsupported-map-entry-carrier
                                   {}))
                                (recur
                                 (next cursor)
                                 (inc width)
                                 (conj snapshot
                                       [(snapshot-value!
                                         (key entry) (inc depth))
                                        (snapshot-value!
                                         (val entry) (inc depth))])))))))
                      (finally
                        (.remove active value)))))))]
      (let [snapshot-args
            (loop [index 0 snapshot []]
              (if (= index (count args))
                snapshot
                (recur (inc index)
                       (conj snapshot
                             (snapshot-value! (nth args index) 0)))))]
        (assoc @stats
               :snapshot-args snapshot-args
               :printer-boundary :clojure-seed-compatibility)))))

(defn p15-s23-seed-readable-integer-text
  [value]
  (let [integer (biginteger value)
        negative? (neg? (.signum integer))
        magnitude (.abs integer)
        ten (java.math.BigInteger/valueOf 10)]
    (if (zero? (.signum magnitude))
      "0"
      (loop [remaining magnitude digits []]
        (if (zero? (.signum remaining))
          (str (when negative? "-") (apply str (reverse digits)))
          (let [digit (.intValue (.remainder remaining ten))]
            (recur (.divide remaining ten)
                   (conj digits (.charAt "0123456789" digit)))))))))

(defn p15-s23-seed-readable-append-hex4!
  [^StringBuilder builder code]
  (let [digits "0123456789abcdef"]
    (doseq [shift [12 8 4 0]]
      (.append builder
               (.charAt digits (bit-and 15 (bit-shift-right code shift))))))
  builder)

(defn p15-s23-seed-readable-append-fixed-decimal!
  [^StringBuilder builder value width]
  (let [text (Long/toString (long value))]
    (dotimes [_ (- width (.length text))]
      (.append builder \0))
    (.append builder text))
  builder)