(defn
 sh07-core-run-structural-request-for-test
 "Test-only structural runner. It does not authenticate SH-06 membership;\n  callers testing the executable boundary must use\n  sh07-core-run-request-for-test with the verified SH-06 artifact."
 [authenticated-request]
 (gravity.checked-core/run-structural-request-for-test
  authenticated-request
  (checked-core-stage-operations)))

(defn
 sh07-core-run-request-for-test
 ([resolution-artifact authenticated-request]
  (gravity.checked-core/run-request-for-test
   resolution-artifact
   authenticated-request
   (checked-core-stage-operations)))
 ([resolution-artifact authenticated-request invocation]
  (gravity.checked-core/run-request-for-test
   resolution-artifact
   authenticated-request
   invocation
   (checked-core-stage-operations))))

(defn sh07-core-projection-diagnostic!
  [resolution-artifact reason]
  (let [source-path
        (sh07-core-source-path-from-resolution resolution-artifact)
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
         :namespace
         (get-in resolution-artifact [:namespace-analysis :namespace])
         :profile
         (get-in resolution-artifact [:namespace-analysis :profile])
         :target
         (get-in resolution-artifact [:namespace-analysis :target])
         :lowering-rule :sh07-b47-function-call-recursion-products
         :facts {:reason reason
                 :rule-specific {:reason reason}
                 :source-revision-id
                 (get-in resolution-artifact
                         [:gravity-resolution-boundary
                          :authenticated-resolution-request
                          :module :source-revision-id])
                 :sh06-artifact-id (:artifact-id resolution-artifact)
                 :authenticated-sh06-artifact-id
                 (:artifact-id resolution-artifact)
                 :fail-closed true}
         :remediation
         "Replay the Gravity template and bind every digest ordinal exactly once."
         :diagnostic-id-request
         (reader-canonical-hash
          {:domain :gravity/sh07-projection-diagnostic-v16
           :reason reason
           :sh06-artifact-id (:artifact-id resolution-artifact)})}]
    (throw (ex-info "SH-07 projection authentication failed" diagnostic))))

(declare sh07-core-exact-comparison-value)

(defn sh07-core-error-transfers-coherent?
  [core]
  (let [nodes (:nodes core)
        transfers (:error-transfers core)
        throw-nodes
        (when (vector? nodes)
          (filterv #(and (map? %) (= :throw (:core-form %))) nodes))
        throw-node-by-id
        (when (vector? throw-nodes)
          (into {} (map (juxt :node-id identity)) throw-nodes))
        shared-attribute-keys
        [:construction-order :runtime-reachability
         :transfer-policy :result-policy :required-effect
         :authenticated-sh06-artifact-id
         :sh06-semantic-projection-id
         :type-legality :effect-registry-legality
         :effect-profile-capability-legality
         :profile-error-lowering-legality
         :ownership-legality :safety-classification]]
    (and
     (vector? nodes)
     (vector? transfers)
     (= (count throw-nodes) (count transfers))
     (= (count throw-nodes) (count throw-node-by-id))
     (every?
      true?
      (map-indexed
       (fn [ordinal transfer]
         (let [node (get throw-node-by-id (:core-node-id transfer))
               value-node-id (:value-core-node-id transfer)]
           (and
            (map? transfer)
            (map? node)
            (= ordinal (:ordinal transfer))
            (= [value-node-id] (:evaluated-children transfer))
            (= [value-node-id] (:children node))
            (= [] (:resolved-binding-ids node))
            (= :value-then-transfer
               (get-in node [:evaluation :kind]))
            (= [{:index 0 :core-node-id value-node-id}]
               (get-in node [:evaluation :order]))
            (= 0 (get-in node [:attributes :value-child-index]))
            (= :value-then-transfer
               (get-in node [:attributes :evaluation-order]))
            (= [:evaluate-value :transfer-error]
               (:ordered-steps transfer))
            (= (select-keys transfer shared-attribute-keys)
               (select-keys (:attributes node)
                            shared-attribute-keys)))))
       transfers)))))
