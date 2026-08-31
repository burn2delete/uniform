

(defn- sh07-core-template-carrier-census
  [value]
  (let [maximum-nodes (* 32 1024 1024)
        maximum-scalar-bytes (* 8 268435456)
        frontier (java.util.ArrayDeque.)]
    (.addLast frontier [value 0])
    (loop [nodes 0
           aggregate-nodes 0
           component-nodes 0
           scalar-bytes 0
           maximum-depth 0
           maximum-width 0]
      (if (.isEmpty frontier)
        {:status :complete
         :nodes nodes
         :aggregate-nodes aggregate-nodes
         :component-nodes component-nodes
         :scalar-bytes scalar-bytes
         :maximum-depth maximum-depth
         :maximum-width maximum-width}
        (let [[current depth] (.removeLast frontier)
              next-nodes (inc nodes)
              aggregate?
              (or (map? current) (vector? current)
                  (set? current) (seq? current))]
          (cond
            (> next-nodes maximum-nodes)
            {:status :truncated
             :reason :measurement-node-bound
             :observed-at-least next-nodes
             :maximum maximum-nodes}

            aggregate?
            (let [components
                  (if (map? current)
                    (mapcat (fn [[key item]] [key item]) current)
                    current)
                  width (if (map? current)
                          (* 2 (count current))
                          (count current))]
              (doseq [component components]
                (.addLast frontier [component (inc depth)]))
              (recur
               next-nodes
               (inc aggregate-nodes)
               (+ component-nodes width)
               scalar-bytes
               (max maximum-depth depth)
               (max maximum-width width)))

            :else
            (let [next-scalar-bytes
                  (+ scalar-bytes (* 4 (count (str current))))]
              (if (> next-scalar-bytes maximum-scalar-bytes)
                {:status :truncated
                 :reason :measurement-scalar-byte-bound
                 :observed-at-least next-scalar-bytes
                 :maximum maximum-scalar-bytes}
                (recur
                 next-nodes
                 aggregate-nodes
                 component-nodes
                 next-scalar-bytes
                 (max maximum-depth depth)
                 maximum-width)))))))))

(defn sh07-core-run-structural-request-for-test
  "Test-only structural runner. It does not authenticate SH-06 membership;
  callers testing the executable boundary must use
  sh07-core-run-request-for-test with the verified SH-06 artifact."
  [authenticated-request]
  (let [_ (sh07-core-request-preflight! authenticated-request)
        source-path
        (get-in authenticated-request [:provenance :actual-source-path])
        result
        (sh07-core-execute!
         source-path 'sh07-build-core-template [authenticated-request])]
    (case (:status result)
      :rejected (sh07-core-raise-diagnostic! source-path result)
      :accepted
      (let [template (:core-template result)
            digest-requests (:digest-requests result)
            template-verification
            (sh07-core-execute!
             source-path 'sh07-verify-core-template
             [authenticated-request template digest-requests])
            _ (when-not (= :passed (:status template-verification))
                (throw
                 (ex-info "Gravity SH-07 template replay failed"
                          {:id "C6-VERIFY" :stage :core-lowering
                           :source-path source-path
                           :template-verification
                           template-verification
                           :template-carrier-census
                           (sh07-core-template-carrier-census
                            template)
                           :digest-request-carrier-census
                           (sh07-core-template-carrier-census
                            digest-requests)})))
            resolved-digests
            (sh07-core-digest-requests source-path digest-requests)
            resolved-template
            (sh07-core-resolve-result
             source-path template digest-requests resolved-digests)
            resolved-verification
            (sh07-core-execute!
             source-path 'sh07-verify-core-resolved
             [authenticated-request resolved-template
              digest-requests resolved-digests])
            _ (when-not (= :passed (:status resolved-verification))
                (throw
                 (ex-info "Gravity SH-07 resolved replay failed"
                          {:id "C6-VERIFY" :stage :core-lowering
                           :source-path source-path
                           :resolved-verification
                           resolved-verification})))]
        {:raw-template-result result
         :canonical-core-artifact
         (sh07-core-canonical-artifact resolved-template)
         :digest-requests digest-requests
         :resolved-digests resolved-digests
         :template-verification template-verification
         :resolved-verification resolved-verification})
      (throw
       (ex-info "Gravity SH-07 returned an invalid result status"
                {:id "C6-VERIFY" :stage :core-lowering
                 :source-path source-path
                 :status (:status result)})))))

(defn sh07-core-run-request-for-test
  [resolution-artifact authenticated-request]
  (let [upstream-verification
        (sh06-resolution-artifact-verification resolution-artifact)
        expected-request
        (when (= :passed (:status upstream-verification))
          (sh07-core-authenticated-request resolution-artifact))]
    (when-not (= :passed (:status upstream-verification))
      (sh07-core-request-diagnostic!
       authenticated-request
       {:reason :authenticated-sh06-artifact-required
        :failed-checks (:failed-checks upstream-verification)}))
    (when-not (= expected-request authenticated-request)
      (sh07-core-request-diagnostic!
       authenticated-request
       {:reason :authenticated-sh06-request-membership-mismatch}))
    (sh07-core-run-structural-request-for-test authenticated-request)))

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