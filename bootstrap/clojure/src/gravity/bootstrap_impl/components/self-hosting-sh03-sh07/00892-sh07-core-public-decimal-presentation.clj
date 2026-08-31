

(defn sh07-core-public-decimal-presentation
  [core]
  (update
   core :nodes
   (fn [nodes]
     (mapv
      (fn [node]
        (let [descriptor (get-in node [:attributes :value])]
          (if (and (= :decimal
                      (get-in node [:attributes :literal-kind]))
                   (map? descriptor)
                   (= :gravity/arbitrary-decimal-literal
                      (:kind descriptor)))
            (assoc-in
             node [:attributes :value]
             (java.math.BigDecimal.
              (java.math.BigInteger. (:unscaled-value descriptor))
              (int (:scale descriptor))))
            node)))
      nodes))))

(defn sh07-core-canonical-artifact
  [resolved-template]
  (-> resolved-template
      (assoc :artifact :gravity/sh07-canonical-core-artifact
             :artifact-id (:artifact-id-request resolved-template)
             :provenance-binding-id
             (:provenance-binding-id-request resolved-template)
             :identity-preimage
             (sh07-core-semantic-identity-preimage
              (:identity-preimage resolved-template)))
      (dissoc :artifact-id-request :provenance-binding-id-request)
      sh07-core-public-decimal-presentation))

(defn sh07-core-raise-diagnostic!
  [source-path template]
  (let [diagnostic (first (:diagnostics template))
        request (first (:digest-requests template))
        diagnostic-id
        (reader-canonical-hash
         {:domain :gravity/sh07-declared-digest-v1
          :purpose (:purpose request)
          :preimage (:preimage request)})
        resolved
        (assoc diagnostic :diagnostic-id-request diagnostic-id)]
    (throw (ex-info "Gravity checked-core lowering rejected the source"
                    resolved))))

(defn sh07-core-nested-depth
  [value]
  (loop [frontier [[value 0]]
         maximum 0
         visited 0]
    (if (empty? frontier)
      maximum
      (let [[item depth] (peek frontier)
            frontier (pop frontier)]
        (cond
          (or (> depth 256) (> visited 8388608))
          257

          (or (map? item) (vector? item) (set? item) (list? item))
          (let [next-depth (inc depth)
                children
                (if (map? item)
                  (mapcat (fn [[key value]] [key value]) item)
                  item)
                bounded-children (vec (take 65537 children))]
            (if (> (count bounded-children) 65536)
              257
              (recur
               (into frontier
                     (map #(vector % next-depth) bounded-children))
               (max maximum next-depth)
               (inc visited))))

          (coll? item)
          257

          :else
          (recur frontier maximum (inc visited)))))))

(defn sh07-core-request-diagnostic!
  [request rule-specific]
  (let [source-path (get-in request [:provenance :actual-source-path])
        lineage (:lineage request)
        module (:module request)
        reason (or (:reason rule-specific)
                   :bounded-authenticated-core-request)
        remediation
        (case reason
          :fragment-root-form-bound
          "Split the top-level definition into supported bounded helper definitions."

          "Replay the Gravity template and bind every digest ordinal exactly once.")
        diagnostic
        {:artifact :gravity/sh07-core-diagnostic
         :rule "C6-VERIFY"
         :severity :error
         :stage :core-lowering
         :syntax-id nil
         :form-id nil
         :core-node-id nil
         :source-span {:source source-path}
         :generated-origin-chain []
         :namespace (:namespace module)
         :profile (:profile module)
         :target (:target module)
         :lowering-rule :sh07-b47-function-call-recursion-products
         :facts {:reason reason
                 :rule-specific rule-specific
                 :source-revision-id (:source-revision-id lineage)
                 :sh06-artifact-id (:sh06-artifact-id lineage)
                 :authenticated-sh06-artifact-id
                 (:authenticated-sh06-artifact-id lineage)
                 :sh06-semantic-projection-id
                 (:sh06-semantic-projection-id lineage)
                 :fail-closed true}
         :remediation remediation
         :diagnostic-id-request
         (reader-canonical-hash
          {:domain :gravity/sh07-request-bound-diagnostic-v16
           :source-revision-id (:source-revision-id lineage)
           :rule-specific rule-specific})}]
    (throw (ex-info "SH-07 authenticated request exceeded a bound"
                    diagnostic))))