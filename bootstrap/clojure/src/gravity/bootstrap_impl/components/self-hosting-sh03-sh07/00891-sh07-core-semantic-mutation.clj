

(defn sh07-core-semantic-mutation
  [semantic-projection-id mutation]
  (let [semantic-projection-id
        (or semantic-projection-id
            (sh07-core-semantic-projection-id mutation))]
    (cond-> mutation
      (:target-upstream-binding-id mutation)
      (assoc :target-upstream-binding-id
             (:target-binding-id mutation))

      (and semantic-projection-id
           (:target-definition-artifact-id mutation))
      (assoc :target-definition-artifact-id
             semantic-projection-id)

      (and semantic-projection-id
           (:authenticated-sh06-artifact-id mutation))
      (assoc :authenticated-sh06-artifact-id
             semantic-projection-id))))

(defn sh07-core-semantic-error-transfer
  [semantic-projection-id transfer]
  (let [semantic-projection-id
        (or semantic-projection-id
            (sh07-core-semantic-projection-id transfer))]
    (cond-> transfer
      (and semantic-projection-id
           (:authenticated-sh06-artifact-id transfer))
      (assoc :authenticated-sh06-artifact-id
             semantic-projection-id))))

(defn sh07-core-semantic-error-handler
  [semantic-projection-id handler]
  (let [semantic-projection-id
        (or semantic-projection-id
            (sh07-core-semantic-projection-id handler))]
    (cond-> handler
      (and semantic-projection-id
           (:authenticated-sh06-artifact-id handler))
      (assoc :authenticated-sh06-artifact-id
             semantic-projection-id))))

(defn sh07-core-semantic-node
  [semantic-projection-id node]
  (case (:core-form node)
    :var
    (update node :attributes
            #(sh07-core-semantic-var-reference
              semantic-projection-id %))
    :set!
    (update node :attributes
            #(sh07-core-semantic-mutation
              semantic-projection-id %))
    :throw
    (update node :attributes
            #(sh07-core-semantic-error-transfer
              semantic-projection-id %))
    :try
    (update node :attributes
            #(sh07-core-semantic-error-handler
              semantic-projection-id %))
    node))

(defn sh07-core-semantic-identity-preimage
  [preimage]
  (let [semantic-projection-id
        (sh07-core-semantic-projection-id preimage)]
    (cond-> preimage
      (and semantic-projection-id
           (contains? preimage :projection-binding))
      (assoc :projection-binding semantic-projection-id)

      (and semantic-projection-id
           (contains? preimage :lineage))
      (assoc-in
       [:lineage :authenticated-sh06-artifact-id]
       semantic-projection-id)

      (:binding-table preimage)
      (update :binding-table
              #(mapv
                (partial sh07-core-semantic-binding
                         semantic-projection-id)
                %))

      (:resolution-table preimage)
      (update :resolution-table
              #(mapv sh07-core-semantic-resolution %))

      (:var-references preimage)
      (update :var-references
              #(mapv
                (partial sh07-core-semantic-var-reference
                         semantic-projection-id)
                %))

      (:mutations preimage)
      (update :mutations
              #(mapv
                (partial sh07-core-semantic-mutation
                         semantic-projection-id)
                %))

      (:error-transfers preimage)
      (update :error-transfers
              #(mapv
                (partial sh07-core-semantic-error-transfer
                         semantic-projection-id)
                %))

      (:error-handlers preimage)
      (update :error-handlers
              #(mapv
                (partial sh07-core-semantic-error-handler
                         semantic-projection-id)
                %))

      (:nodes preimage)
      (update :nodes
              #(mapv
                (partial sh07-core-semantic-node
                         semantic-projection-id)
                %))

      (= :var (:core-form preimage))
      (update :attributes
              #(sh07-core-semantic-var-reference
                semantic-projection-id %))

      (= :set! (:core-form preimage))
      (update :attributes
              #(sh07-core-semantic-mutation
                semantic-projection-id %))

      (= :throw (:core-form preimage))
      (update :attributes
              #(sh07-core-semantic-error-transfer
                semantic-projection-id %))

      (= :try (:core-form preimage))
      (update :attributes
              #(sh07-core-semantic-error-handler
                semantic-projection-id %)))))

(defn sh07-core-resolve-digest-preimage!
  [source-path purpose preimage resolved-digests]
  (case purpose
    :sh07-core-node-id
    (-> preimage
        (assoc :children
               (sh07-core-resolve-reference-vector!
                source-path (:children preimage) resolved-digests))
        (assoc :evaluated-children
               (sh07-core-resolve-reference-vector!
                source-path (:evaluated-children preimage)
                resolved-digests))
        sh07-core-semantic-identity-preimage)

    :sh07-core-artifact-id
    (let [result
          (sh07-core-execute!
           source-path 'sh07-resolve-identity-preimage
           [preimage resolved-digests])]
      (when-not (= :accepted (:status result))
        (throw
         (ex-info "SH-07 identity digest preimage did not resolve"
                  {:id "C6-VERIFY" :stage :core-lowering
                   :source-path source-path
                   :reason (:reason result)})))
      (sh07-core-semantic-identity-preimage (:value result)))

    :sh07-core-provenance-binding-id
    (let [result
          (sh07-core-execute!
           source-path 'sh07-resolve-provenance-preimage
           [preimage resolved-digests])]
      (when-not (= :accepted (:status result))
        (throw
         (ex-info "SH-07 provenance digest preimage did not resolve"
                  {:id "C6-VERIFY" :stage :core-lowering
                   :source-path source-path
                   :reason (:reason result)})))
      (:value result))

    preimage))

(defn sh07-core-digest-requests
  [source-path digest-requests]
  (loop [remaining digest-requests
         ordinal 0
         resolved []]
    (if (empty? remaining)
      resolved
      (let [request (first remaining)]
        (when-not
         (and (= ordinal (:ordinal request))
              (= #{:ordinal :purpose :preimage} (set (keys request)))
              (<= 0 ordinal 65539))
          (throw
           (ex-info "Malformed SH-07 digest request sequence"
                    {:id "C6-VERIFY" :stage :core-lowering
                     :digest-request request :ordinal ordinal})))
        (let [purpose (:purpose request)
              preimage
              (sh07-core-resolve-digest-preimage!
               source-path purpose (:preimage request) resolved)
              digest
              (reader-canonical-hash
               {:domain :gravity/sh07-declared-digest-v1
                :purpose purpose
                :preimage preimage})]
          (recur (rest remaining)
                 (inc ordinal)
                 (conj resolved digest)))))))

(defn sh07-core-resolve-result
  [source-path template digest-requests resolved-digests]
  (let [resolution
        (sh07-core-execute!
         source-path 'sh07-resolve-core-template
         [template resolved-digests])]
    (when-not (= :accepted (:status resolution))
      (throw
       (ex-info "SH-07 controlled digest-slot resolution failed"
                {:id "C6-VERIFY" :stage :core-lowering
                 :source-path source-path
                 :reason (:reason resolution)
                 :resolution resolution})))
    (:value resolution)))