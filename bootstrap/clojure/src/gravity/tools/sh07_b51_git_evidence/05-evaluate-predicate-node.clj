
(defn- evaluate-predicate-node!
  [node env item-bound? depth budget id]
  (when-not (and (vector? node) (seq node))
    (fail! id :predicate-bool-node {:node node}))
  (swap! budget inc)
  (when (or (> @budget 100000) (> depth 256))
    (fail! id :predicate-fuel {:depth depth :nodes @budget}))
  (case (first node)
    :and
    (do
      (when (< (count node) 3)
        (fail! id :predicate-and-node {:node node}))
      (loop [children (next node)]
        (if-let [child (first children)]
          (if (true? (evaluate-predicate-node!
                      child env item-bound? (inc depth) budget id))
            (recur (next children))
            false)
          true)))

    :equal
    (do
      (when-not (= 3 (count node))
        (fail! id :predicate-equal-node {:node node}))
      (= (predicate-term! (second node) env item-bound?
                          (inc depth) budget id)
         (predicate-term! (nth node 2) env item-bound?
                          (inc depth) budget id)))

    :exact-keys
    (do
      (when-not (= 3 (count node))
        (fail! id :predicate-exact-keys-node {:node node}))
      (let [value (predicate-term! (second node) env item-bound?
                                   (inc depth) budget id)
            keys-value (predicate-term! (nth node 2) env item-bound?
                                        (inc depth) budget id)]
        (when-not (and (map? value) (vector? keys-value)
                       (every? keyword? keys-value)
                       (= (count keys-value) (count (distinct keys-value))))
          (fail! id :predicate-exact-keys-type))
        (= (set keys-value) (set (keys value)))))

    :h-equal
    (do
      (when-not (= 5 (count node))
        (fail! id :predicate-h-equal-node {:node node}))
      (let [domain (predicate-term! (second node) env item-bound?
                                    (inc depth) budget id)
            value (predicate-term! (nth node 2) env item-bound?
                                   (inc depth) budget id)
            expected (digest-term! (nth node 3) env item-bound?
                                   (inc depth) budget id)
            omit-paths (predicate-term! (nth node 4) env item-bound?
                                        (inc depth) budget id)]
        (when-not (keyword? domain)
          (fail! id :predicate-h-domain {:domain domain}))
        (= expected
           (codec/H [domain (omit-required-paths! value omit-paths id)]))))

    :r-equal
    (do
      (when-not (= 3 (count node))
        (fail! id :predicate-r-equal-node {:node node}))
      (let [value (predicate-term! (second node) env item-bound?
                                   (inc depth) budget id)
            expected (digest-term! (nth node 2) env item-bound?
                                   (inc depth) budget id)
            bytes (cond
                    (instance? (Class/forName "[B") value) value
                    (and (vector? value)
                         (every? #(and (integer? %) (<= 0 % 255)) value))
                    (byte-array (map unchecked-byte value))
                    :else (fail! id :predicate-byte-vector))]
        (= expected (codec/R bytes))))

    :every
    (do
      (when-not (= 3 (count node))
        (fail! id :predicate-every-node {:node node}))
      (when item-bound?
        (fail! id :predicate-item-shadowing))
      (let [items (predicate-term! (second node) env item-bound?
                                   (inc depth) budget id)]
        (when-not (and (vector? items) (seq items))
          (fail! id :predicate-every-type))
        (every? true?
                (map #(evaluate-predicate-node!
                       (nth node 2) (assoc env :item %) true
                       (inc depth) budget id)
                     items))))

    :not
    (do
      (when-not (= 2 (count node))
        (fail! id :predicate-not-node {:node node}))
      (not (evaluate-predicate-node!
            (second node) env item-bound? (inc depth) budget id)))

    :lte
    (do
      (when-not (= 3 (count node))
        (fail! id :predicate-lte-node {:node node}))
      (let [left (predicate-term! (second node) env item-bound?
                                  (inc depth) budget id)
            right (predicate-term! (nth node 2) env item-bound?
                                   (inc depth) budget id)]
        (when-not (and (integer? left) (not (neg? left))
                       (integer? right) (not (neg? right)))
          (fail! id :predicate-lte-type))
        (<= left right)))

    (fail! id :predicate-bool-operator {:operator (first node)})))

(defn evaluate-contract-predicate!
  "Evaluate one architecture-owned PREDICATE-SPECS program. Caller-selected
  programs are rejected; this is a closed AST interpreter, never host eval."
  [spec env]
  (exact-keys! spec
               #{:ordinal :row-ordinal :row-id :phase :obligation :program
                 :predicate-id :required-subject-paths}
               "SH07-B51-CONTRACT-DISCHARGE" :predicate-spec-keys)
  (exact-keys! env #{:context :subjects :carriers}
               "SH07-B51-CONTRACT-DISCHARGE" :predicate-env-keys)
  (exact-keys! (:context env)
               #{:row-ordinal :row-id :phase :obligation}
               "SH07-B51-CONTRACT-DISCHARGE" :predicate-context-keys)
  (when-not (and (integer? (:ordinal spec))
                 (< -1 (:ordinal spec) (count predicate-specs))
                 (= spec (nth predicate-specs (:ordinal spec)))
                 (vector? (:subjects env))
                 (map? (:carriers env)))
    (fail! "SH07-B51-CONTRACT-DISCHARGE" :predicate-spec-or-env))
  (boolean
   (evaluate-predicate-node!
   (:program spec) env false 0 (atom 0)
    "SH07-B51-CONTRACT-DISCHARGE")))

(defn resolve-contract-phase!
  "Resolve one phase from a closed carrier pool using the literal ROW-PLAN.
  This is the data-only L evaluator substrate; semantic carrier admission is a
  separate prerequisite and this function grants no evidence authority."
  [phase carriers]
  (when-not (some #{phase} phases)
    (fail! "SH07-B51-CONTRACT-DISCHARGE" :phase {:phase phase}))
  (when-not (map? carriers)
    (fail! "SH07-B51-CONTRACT-DISCHARGE" :carrier-pool))
  (let [rows
        (->> row-plan
             (keep
              (fn [row]
                (let [bindings (filterv #(= phase (:phase %))
                                        (:binding-specs row))]
                  (when (seq bindings)
                    (let [resolved
                          (mapv
                           (fn [binding]
                             (let [value
                                   (strict-path-value!
                                    carriers
                                    (into [(:carrier binding)]
                                          (:path binding))
                                    "SH07-B51-CONTRACT-DISCHARGE")
                                   without-id
                                   (assoc binding
                                          :row-ordinal (:ordinal row)
                                          :value value)]
                               (assoc without-id :value-id
                                      (codec/H
                                       [:gravity/sh07-b51-contract-binding-v2
                                        without-id]))))
                           bindings)
                          resolved-obligations
                          (filterv
                           (fn [obligation]
                             (some #(some #{obligation} (:obligations %))
                                   bindings))
                           (:obligations row))]
                      {:row-ordinal (:ordinal row)
                       :row-plan-id row-plan-id
                       :phase phase
                       :resolved-obligations resolved-obligations
                       :bindings resolved
                       :status :bindings-resolved
                       :diagnostic (:diagnostic row)})))))
             vec)
        specs-by-triple
        (into {}
              (map (fn [spec]
                     [[(:row-ordinal spec) (:phase spec) (:obligation spec)]
                      spec])
                   predicate-specs))]
    (doseq [resolved-row rows
            obligation (:resolved-obligations resolved-row)]
      (let [subjects
            (filterv #(some #{obligation} (:obligations %))
                     (:bindings resolved-row))
            spec (get specs-by-triple
                      [(:row-ordinal resolved-row) phase obligation])
            context
            {:row-ordinal (:row-ordinal resolved-row)
             :row-id (:id (nth row-plan (:row-ordinal resolved-row)))
             :phase phase
             :obligation obligation}]
        (when-not (and spec
                       (= (:required-subject-paths spec)
                          (mapv :path subjects))
                       (evaluate-contract-predicate!
                        spec {:context context
                              :subjects subjects
                              :carriers carriers}))
          (fail! "SH07-B51-CONTRACT-DISCHARGE"
                 :predicate-false
                 {:row-ordinal (:row-ordinal resolved-row)
                  :phase phase :obligation obligation}))))
    {:phase phase
     :row-plan-id row-plan-id
     :resolved-rows rows
     :resolved-root
     (codec/H
      [(get {:pre-evidence :gravity/sh07-b51-pre-resolved-row-root-v1
             :runtime :gravity/sh07-b51-runtime-resolved-row-root-v1
             :post-i9 :gravity/sh07-b51-post-i9-resolved-row-root-v1
             :post-g9 :gravity/sh07-b51-post-g9-resolved-row-root-v1
             :post-p9 :gravity/sh07-b51-post-p9-resolved-row-root-v1}
            phase)
       rows])
     :authority :none}))

(def ^:private empty-raw-sha256
  "sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")

(def ^:private no-link-options
  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))

(def ^:private no-open-options (make-array OpenOption 0))

(defn- fail!
  ([id reason] (fail! id reason {}))
  ([id reason details]
   (throw (ex-info "SH-07 B5.1 Git evidence rejected"
                   (merge {:id id :reason reason} details)))))
