
(defn- p15-s23-sh07-probe-child
  [source-path child depth active node-budget decimal-occurrences]
  (try
    (p15-s23-sh07-canonical-decoration
     source-path child depth (p15-s23-sh07-active-copy active) node-budget
     decimal-occurrences)
    (catch clojure.lang.ExceptionInfo error
      (p15-s23-sh07-decoration-failed
       (or (:reason (ex-data error)) :contained-decoration-failure)))
    (catch Exception _
      (p15-s23-sh07-decoration-failed :contained-decoration-failure))))

(defn p15-s23-sh07-canonical-decoration
  ([source-path value]
   (p15-s23-sh07-canonical-decoration
    source-path value 0 (java.util.IdentityHashMap.)
    (:maximum-nodes p15-s23-sh07-canonical-decoration-limits) []))
  ([source-path value decimal-occurrences]
   (if (p15-s23-sh07-o-c2-carrier? decimal-occurrences)
     (p15-s23-sh07-o-c2-decoration decimal-occurrences)
     (p15-s23-sh07-canonical-decoration
      source-path value 0 (java.util.IdentityHashMap.)
      (:maximum-nodes p15-s23-sh07-canonical-decoration-limits)
      decimal-occurrences)))
  ([source-path value depth ^java.util.IdentityHashMap active]
   (p15-s23-sh07-canonical-decoration
    source-path value depth active
    (:maximum-nodes p15-s23-sh07-canonical-decoration-limits) []))
  ([source-path value depth ^java.util.IdentityHashMap active node-budget]
   (p15-s23-sh07-canonical-decoration
    source-path value depth active node-budget []))
  ([source-path value depth ^java.util.IdentityHashMap active node-budget
    decimal-occurrences]
   (let [{:keys [maximum-depth maximum-collection-width
                 maximum-scalar-bytes]}
         p15-s23-sh07-canonical-decoration-limits]
     (if (<= node-budget 0)
       (p15-s23-sh07-decoration-failed :node-count-limit)
       (if (> depth maximum-depth)
       (p15-s23-sh07-decoration-failed :depth-limit)
       (let [host-decimal? (instance? java.math.BigDecimal value)
             kind
             (try
               ;; A host decimal does not identify a source occurrence.  G19
               ;; admission is available only through the producer/oracle
               ;; entry points that require authenticated adapted form/token
               ;; identities.
               (when-not host-decimal?
                 (p15-s23-sh07-o-scalar-kind value))
               (catch Exception _ nil))
             collection? (contains? #{:vector :list :map :set} kind)]
         (if (= kind nil)
           (p15-s23-sh07-decoration-failed
            (if host-decimal?
              :unsupported-decimal-readable-spelling
              :unsupported-value-carrier))
           (if-not collection?
             (try
               (let [rendered (p15-s23-sh07-o-scalar-text value)]
                 {:status :decorated :text (:text rendered)
                  :node-occurrences 1
                  :maximum-depth depth
                  :output-utf8-bytes (:output-utf8-bytes rendered)})
               (catch clojure.lang.ExceptionInfo error
                 (p15-s23-sh07-decoration-failed
                  (or (:reason (ex-data error))
                      :contained-decoration-failure)))
               (catch Exception _
                 (p15-s23-sh07-decoration-failed
                  :contained-decoration-failure)))
             (if (.containsKey active value)
               (p15-s23-sh07-decoration-failed :cyclic-value)
               (let [width (count value)]
                 (if (> width maximum-collection-width)
                   (p15-s23-sh07-decoration-failed
                    :collection-width-limit)
                   (let [next-active (p15-s23-sh07-active-copy active)]
                     (.put next-active value Boolean/TRUE)
                     (case kind
                       :vector
                       (loop [remaining (vec value) decorations [] used 1]
                         (if (empty? remaining)
                           (p15-s23-sh07-combine-decorations
                            depth node-budget "[" " " "]" decorations)
                           (let [decorated
                                 (p15-s23-sh07-probe-child
                                  source-path (first remaining)
                                  (inc depth) next-active
                                  (- node-budget used) decimal-occurrences)]
                             (if (= :decorated (:status decorated))
                               (recur (rest remaining)
                                      (conj decorations decorated)
                                      (+ used (:node-occurrences decorated)))
                               decorated))))

                       :list
                       (loop [remaining (seq value) decorations [] used 1]
                         (if (nil? remaining)
                           (p15-s23-sh07-combine-decorations
                            depth node-budget "(" " " ")" decorations)
                           (let [decorated
                                 (p15-s23-sh07-probe-child
                                 source-path (first remaining)
                                  (inc depth) next-active
                                  (- node-budget used) decimal-occurrences)]
                             (if (= :decorated (:status decorated))
                               (recur (next remaining)
                                      (conj decorations decorated)
                                      (+ used (:node-occurrences decorated)))
                               decorated))))

                       :set
                       (let [results
                             (mapv
                              #(p15-s23-sh07-probe-child
                                source-path % (inc depth) next-active
                                (dec node-budget) decimal-occurrences)
                              (vec value))
                             any-failed?
                             (reduce
                              (fn [failed? result]
                                (or failed?
                                    (not= :decorated (:status result))))
                              false results)]
                         (if any-failed?
                           (p15-s23-sh07-decoration-failed
                            :unordered-decoration-failure)
                           (let [ordered
                                 (p15-s23-sh07-stable-order-by
                                  (fn [left right]
                                    (p15-s23-sh07-canonical-text-compare
                                     (:text left) (:text right)))
                                  results)]
                             (if (some true?
                                       (map #(= (:text %1) (:text %2))
                                            ordered (rest ordered)))
                               (p15-s23-sh07-decoration-failed
                                :source-integrity-mismatch)
                               (p15-s23-sh07-combine-decorations
                                depth node-budget "#{" " " "}" ordered)))))

                       :map
                       (let [results
                             (mapv
                              (fn [entry]
                                {:key-result
                                 (p15-s23-sh07-probe-child
                                  source-path (key entry)
                                  (inc depth) next-active (dec node-budget)
                                  decimal-occurrences)
                                 :value-result
                                 (p15-s23-sh07-probe-child
                                  source-path (val entry)
                                  (inc depth) next-active (dec node-budget)
                                  decimal-occurrences)
                                 :key (key entry)})
                              (vec value))
                             any-failed?
                             (reduce
                              (fn [failed? entry]
                                (or failed?
                                    (not= :decorated
                                          (get-in entry [:key-result :status]))
                                    (not= :decorated
                                          (get-in entry
                                                  [:value-result :status]))))
                              false results)]
                         (if any-failed?
                           (p15-s23-sh07-decoration-failed
                            :unordered-decoration-failure)
                           (let [ordered
                                 (p15-s23-sh07-stable-order-by
                                  (fn [left right]
                                    (let [key-order
                                          (p15-s23-sh07-canonical-text-compare
                                           (get-in left [:key-result :text])
                                           (get-in right [:key-result :text]))]
                                      (if (zero? key-order)
                                        (p15-s23-sh07-canonical-text-compare
                                         (get-in left [:value-result :text])
                                         (get-in right [:value-result :text]))
                                        key-order)))
                                  results)
                                 key-collision?
                                 (some
                                  true?
                                  (map
                                   #(and
                                     (= (get-in %1 [:key-result :text])
                                        (get-in %2 [:key-result :text]))
                                     (not= (:key %1) (:key %2)))
                                   ordered (rest ordered)))]
                             (if key-collision?
                               (p15-s23-sh07-decoration-failed
                                :source-integrity-mismatch)
                               (let [entries
                                     (mapv
                                      (fn [entry]
                                        (let [key-result (:key-result entry)
                                              value-result
                                              (:value-result entry)
                                              pair
                                              (p15-s23-sh07-combine-decorations
                                               (min (:maximum-depth key-result)
                                                    (:maximum-depth value-result))
                                               (+ 1
                                                  (:node-occurrences key-result)
                                                  (:node-occurrences value-result))
                                               "" " " ""
                                               [key-result value-result])]
                                          (if (= :decorated (:status pair))
                                            (update pair :node-occurrences dec)
                                            pair)))
                                      ordered)]
                                 (if-let [failed
                                          (first
                                           (filter #(not= :decorated
                                                          (:status %))
                                                   entries))]
                                   failed
                                   (p15-s23-sh07-combine-decorations
                                    depth node-budget "{" ", " "}"
                                    entries))))))))))))))))))))
