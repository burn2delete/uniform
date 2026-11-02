

(defn p15-s23-seed-readable-preflight!
  [source-path args]
  (let [active (java.util.IdentityHashMap.)]
    (when-not (identical? clojure.lang.PersistentVector (class args))
      (p15-s23-seed-readable-printer-fail!
       source-path :unsupported-argument-carrier
       {:expected-argument-carrier :persistent-vector}))
    (letfn [(snapshot-value! [value depth]
              (let [kind (p15-s23-seed-readable-value-kind source-path value)
                    collection? (contains? #{:vector :list :map :set} kind)]
                (if-not collection?
                  (let [snapshot
                        (p15-s23-seed-readable-snapshot-scalar!
                         source-path kind value)]
                    (p15-s23-seed-readable-validate-scalar!
                     source-path kind snapshot)
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
                        (loop [index 0
                               snapshot []]
                          (if (= index (count value))
                            snapshot
                            (recur
                             (inc index)
                             (conj snapshot
                                   (snapshot-value!
                                    (nth value index) (inc depth))))))

                        :list
                        (loop [cursor (seq value)
                               snapshot []]
                          (if (nil? cursor)
                            (apply list snapshot)
                            (do
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
                               (conj snapshot
                                     (snapshot-value!
                                      (first cursor) (inc depth)))))))

                        :set
                        (loop [cursor (seq value)
                               snapshot []]
                          (if (nil? cursor)
                            (let [result (into #{} snapshot)]
                              (when-not (= (count result) (count snapshot))
                                (p15-s23-seed-readable-printer-fail!
                                 source-path :snapshot-collision {}))
                              result)
                            (recur
                             (next cursor)
                             (conj snapshot
                                   (snapshot-value!
                                    (first cursor) (inc depth))))))

                        :map
                        (loop [cursor (seq value)
                               snapshot []]
                          (if (nil? cursor)
                            (let [result (into {} snapshot)]
                              (when-not (= (count result) (count snapshot))
                                (p15-s23-seed-readable-printer-fail!
                                 source-path :snapshot-collision {}))
                              result)
                            (let [entry (first cursor)]
                              (when-not
                               (identical? clojure.lang.MapEntry
                                           (class entry))
                                (p15-s23-seed-readable-printer-fail!
                                 source-path :unsupported-map-entry-carrier
                                 {}))
                              (recur
                               (next cursor)
                               (conj snapshot
                                     [(snapshot-value!
                                       (key entry) (inc depth))
                                      (snapshot-value!
                                       (val entry) (inc depth))]))))))
                      (finally
                        (.remove active value)))))))]
      (let [snapshot-args
            (loop [index 0 snapshot []]
              (if (= index (count args))
                snapshot
                (recur (inc index)
                       (conj snapshot
                             (snapshot-value! (nth args index) 0)))))]
        {:snapshot-args snapshot-args
         :printer-boundary :clojure-seed-compatibility}))))

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
