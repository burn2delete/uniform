

(defn- sh07-proof-transaction-thread-id
  []
  (.getId (Thread/currentThread)))

(defn- sh07-proof-transaction-artifact-root
  [stage artifact]
  (case stage
    :sh05
    {:artifact-id (:artifact-id artifact)
     :stream-id (:expanded-syntax-stream-id artifact)
     :trace-id (:macro-expansion-trace-id artifact)}

    :sh06
    {:artifact-id (:artifact-id artifact)
     :semantic-projection-id (:semantic-projection-id artifact)
     :alias-table-id (:alias-table-id artifact)}

    :sh07
    {:artifact-id (:artifact-id artifact)
     :semantic-projection-id
     (get-in artifact
             [:gravity-core-boundary :canonical-core-artifact
              :semantic-projection-id])}

    {:artifact-id (:artifact-id artifact)}))

(def ^:private sh07-proof-transaction-carrier-node-limit 67108864)
(def ^:private sh07-proof-transaction-carrier-visiting
  (Object.))

(defn- sh07-proof-transaction-carrier-scalar?
  [value]
  (or (boolean? value)
      (instance? java.lang.Byte value)
      (instance? java.lang.Short value)
      (instance? java.lang.Integer value)
      (instance? java.lang.Long value)
      (instance? java.lang.Float value)
      (instance? java.lang.Double value)
      (instance? java.math.BigInteger value)
      (instance? java.math.BigDecimal value)
      (instance? clojure.lang.BigInt value)
      (instance? clojure.lang.Ratio value)
      (string? value)
      (keyword? value)
      (symbol? value)
      (char? value)))

(defn- sh07-proof-transaction-carrier-kind
  [value]
  (cond
    (sh07-proof-transaction-carrier-scalar? value) :scalar
    (instance? clojure.lang.LazySeq value) :lazy
    (map? value) :map
    (or (vector? value) (set? value) (list? value)
        (instance? clojure.lang.IMapEntry value))
    :collection
    :else :invalid))

(defn- sh07-proof-transaction-carrier-track-identity?
  [value]
  (or (instance? clojure.lang.IObj value)
      (map? value) (vector? value) (set? value) (list? value)
      (instance? clojure.lang.IMapEntry value)))

(defn- sh07-proof-transaction-immutable-carrier-analysis
  [^java.util.IdentityHashMap completed value maximum-nodes]
  (let [pending (java.util.ArrayDeque.)
        operations (java.util.ArrayDeque.)
        exit-starts (java.util.ArrayDeque.)
        push-enter!
        (fn [entry]
          ;; The old walker did not count nil roots, values, or metadata.
          (when-not (nil? entry)
            (.push pending entry)
            (.push operations false)))]
    (push-enter! value)
    (loop [logical-nodes 0
           examined-nodes 0
           reused-identities 0]
      (cond
        (> logical-nodes maximum-nodes)
        {:immutable? false
         :reason :carrier-node-bound
         :logical-nodes logical-nodes
         :examined-nodes examined-nodes
         :reused-identities reused-identities}

        (.isEmpty pending)
        {:immutable? true
         :reason nil
         :logical-nodes logical-nodes
         :examined-nodes examined-nodes
         :reused-identities reused-identities}

        :else
        (let [current (.pop pending)
              exit? (true? (.pop operations))]
          (if exit?
            (let [start (long (.pop exit-starts))]
              (.put completed current (long (- logical-nodes start)))
              (recur logical-nodes examined-nodes reused-identities))
            (let [kind (sh07-proof-transaction-carrier-kind current)
                  supported? (not (#{:lazy :invalid} kind))
                  track-identity?
                  (and supported?
                       (sh07-proof-transaction-carrier-track-identity?
                        current))
                  prior (when track-identity? (.get completed current))]
              (cond
                (= :lazy kind)
                {:immutable? false :reason :lazy-carrier
                 :logical-nodes logical-nodes
                 :examined-nodes examined-nodes
                 :reused-identities reused-identities}

                (not supported?)
                {:immutable? false :reason :mutable-or-unknown-carrier
                 :logical-nodes logical-nodes
                 :examined-nodes examined-nodes
                 :reused-identities reused-identities}

                (identical? sh07-proof-transaction-carrier-visiting prior)
                {:immutable? false :reason :carrier-cycle
                 :logical-nodes logical-nodes
                 :examined-nodes examined-nodes
                 :reused-identities reused-identities}

                (number? prior)
                (recur (+ logical-nodes (long prior))
                       examined-nodes (inc reused-identities))

                :else
                (let [next-logical (inc logical-nodes)
                      metadata (when (instance? clojure.lang.IObj current)
                                 (meta current))]
                  (when track-identity?
                    (.put completed current
                          sh07-proof-transaction-carrier-visiting)
                    (.push pending current)
                    (.push operations true)
                    (.push exit-starts (long logical-nodes)))
                  (push-enter! metadata)
                  (case kind
                    :map
                    (doseq [[key entry] current]
                      (push-enter! key)
                      (push-enter! entry))
                    :collection
                    (doseq [entry current] (push-enter! entry))
                    nil)
                  (recur next-logical (inc examined-nodes)
                         reused-identities))))))))))

(defn- sh07-proof-transaction-immutable-carrier?
  ([value]
   (sh07-proof-transaction-immutable-carrier?
    (java.util.IdentityHashMap.) value))
  ([completed value]
   (true?
    (:immutable?
     (sh07-proof-transaction-immutable-carrier-analysis
      completed value sh07-proof-transaction-carrier-node-limit)))))

(defn- sh07-proof-transaction-context!
  []
  (when-let [context *sh07-proof-transaction-context*]
    (let [state @context]
      (when-not (and (:open? state)
                     (= (:owner-thread-id state)
                        (sh07-proof-transaction-thread-id)))
        (throw
         (ex-info "SH-07 proof transaction is closed or thread-confined"
                  {:id "C6-VERIFY"
                   :stage :sh07-proof-transaction
                   :reason (if (:open? state)
                             :cross-thread-access
                             :closed-transaction)})))
      context)))