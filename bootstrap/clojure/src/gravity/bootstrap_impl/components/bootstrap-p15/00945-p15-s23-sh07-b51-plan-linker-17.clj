
(defn p15-s23-sh07-b51-build-receipted-bundle-core-products
  [commit tree]
  (binding [*p15-s23-sh07-b51-v9-receipted-bundle-construction* true]
    (let [{:keys [token state] :as session}
          (p15-s23-sh07-b51-open-source-session commit tree)]
      (binding [*p15-s23-sh07-b51-governed-source-session*
                {:token token :state state}]
      ;; These three products are semantic event values.  Their bytes remain
      ;; solely in the private session entries and never cross the wrapper.
      (p15-s23-sh07-b51-governed-source-event!
       :l1-contract 0 nil 'gravity.bootstrap/p15-s23-sh07-b51-l1-contract
       (fn [{:keys [text]}] text))
      (p15-s23-sh07-b51-governed-source-event!
       :l2-contract 1 nil 'gravity.bootstrap/p15-s23-sh07-b51-l2-contract
       (fn [{:keys [text]}] text))
      (p15-s23-sh07-b51-governed-source-event!
       :checked-core-revision 2 nil
       'gravity.bootstrap/p15-s23-sh07-b51-checked-core-contract
       (fn [{:keys [descriptor text] :as entry}]
         (p15-s23-sh07-b51-cache-session-product!
          :checked-core-entry entry)
         (vec (read-forms (:path descriptor) text))))
      (doseq [module-ordinal (range 7)]
        (p15-s23-sh07-b51-validate-module-input! module-ordinal))
      (let [census (p15-s23-sh07-b51-derive-callsite-census)
            authority
            (p15-s23-sh07-b51-build-link-authority-from-census census)
            authority
            (p15-s23-sh07-b51-cache-session-product!
             :link-authority authority)
            emitter (p15-s23-sh07-b51-current-emitter)
            compilation-emitter
            (p15-s23-stage2-compiler-artifact-augmented-emitter emitter)
            sources (:sources authority)
            descriptors (:module-source-descriptors authority)
            manifest (:link-manifest authority)
            source-authorities
            (mapv
             (fn [descriptor source]
               (let [module
                     (assoc (:module source)
                            :p15-s23-sh07-module-source-descriptor descriptor
                            :p15-s23-sh07-link-manifest manifest)]
                 (p15-s23-sh07-compiler-source-authority
                  (:path source) (:text source) module (:table source))))
             descriptors sources)
            compiler-bindings
            (mapv
             (fn [source]
               (p15-s23-stage2-compiler-artifact-binding!
                compilation-emitter (:path source)
                (:target (:module source))))
             sources)
            resolution-plan-authority
            (p15-s23-sh07-b51-governed-source-event!
             :sh06-resolution-binding 12 nil
             'gravity.bootstrap/p15-s23-sh07-b51-current-sh06-resolution-binding
             (fn [entry]
               (p15-s23-sh07-b51-current-sh06-resolution-binding
                entry emitter)))
            c5-preimage
            (p15-s23-sh07-b51-c5-authority-preimage-from-session
             commit tree descriptors manifest)
            plan-sources
            (mapv #(assoc %1
                          :source-authority %2
                          :compiler-artifact-binding %3)
                  sources source-authorities compiler-bindings)
            _ (p15-s23-sh07-b51-cache-session-product!
               :plan-sources plan-sources)
            products
            (p15-s23-sh07-b51-build-execution-bundle emitter)
            checked-core-binding
            (p15-s23-sh07-b51-session-checked-core-binding emitter)
            checked-core-binding-var
            (ns-resolve 'gravity.bootstrap 'sh07-core-cached-binding)
            module-transactions
            (with-redefs-fn
              {checked-core-binding-var (delay checked-core-binding)}
              (fn []
                (mapv
                 (fn [module-ordinal]
                   (p15-s23-sh07-b51-build-module-transaction
                    commit tree module-ordinal
                    (p15-s23-sh07-b51-session-checked-product
                     [:module-input module-ordinal])
                    c5-preimage resolution-plan-authority))
                 (range 7))))
            source-forms (mapv (comp vec :forms :module) sources)
            source-semantic-carrier
            (p15-s23-sh07-b51-source-semantic-carrier-from-session!
             commit tree module-transactions)
            source-products
            {:m0s (:module-source-descriptors products)
             :m1 (:link-manifest products)
             :m2s (:module-plans products)
             :bundle-core (:execution-bundle-core products)
             :execution-bundle-core-id (:execution-bundle-core-id products)
             :source-forms source-forms
             :module-transactions module-transactions
             :sh06-resolution-plan-authority resolution-plan-authority
             :source-semantic-carrier source-semantic-carrier}
            sh06-authority-carrier
            (p15-s23-sh07-b51-sh06-authority-carrier-from-session!
             commit tree source-products c5-preimage)
            receipt (p15-s23-sh07-b51-finalize-source-session!)
            returned-products
            (assoc source-products :source-read-receipt receipt
                   :sh06-authority-carrier sh06-authority-carrier)]
        (when-not
         (p15-s23-sh07-b51-exact-keys?
          returned-products
          p15-s23-sh07-b51-v9-builder-receipted-product-keys)
          (p15-s23-sh07-b51-governed-read-fail!
           :receipted-product-shape {}))
        (p15-s23-sh07-b51-replay-returned-source-authority-events!
         (:m0s returned-products) (:m1 returned-products)
         resolution-plan-authority receipt)
        (when-not
         (p15-s23-sh07-b51-source-semantic-carrier-valid?
          {:commit commit :tree tree} receipt module-transactions
          source-semantic-carrier)
          (p15-s23-sh07-b51-governed-read-fail!
           :source-semantic-carrier {}))
        returned-products)))))

(def ^:private p15-s23-sh07-b51-sh06-authority-carrier-base-keys
  #{:content :source-descriptors :bundle-core-id
    :resolution-artifact-paths :resolution-artifacts
    :resolution-verifications :semantic-projection-paths
    :semantic-projection-ids :module-source-descriptors :link-manifest
    :link-authority-event-product-ids :link-authority-id
    :expected-edge-census :resolution-plan-authority
    :external-binding-authorities})

(defn- p15-s23-sh07-b51-sh06-authority-carrier-from-session!
  [commit tree products preimage]
  (let [state (p15-s23-sh07-b51-active-source-session)
        handoffs (mapv #(p15-s23-sh07-b51-session-checked-product
                        [:sh06-handoff %]) (range 7))
        authorities (mapv :authority handoffs)]
    (when-not
     (and (= [commit tree] [(:commit state) (:tree state)])
          (= 40 @(:cursor state))
          (= (vec (repeat 7 preimage)) (mapv :preimage authorities))
          (= (:descriptors state)
             (get-in products [:source-semantic-carrier :source-descriptors]))
          (= @(:events state)
             (get-in products [:source-semantic-carrier :source-events])))
      (p15-s23-sh07-b51-c5-authority-fail! :session-carrier-join {}))
    (p15-s23-sh07-b51-assemble-sh06-authority-carrier
     commit tree products (:descriptors state) @(:events state)
     authorities (mapv :verification handoffs) (mapv :request handoffs))))

(defn- p15-s23-sh07-b51-assemble-sh06-authority-carrier
  [commit tree products source-descriptors events c5-authorities
   resolution-verifications replayed-requests]
  (let [
        module-descriptors (:m0s products)
        manifest (:m1 products)
        transactions (:module-transactions products)
        resolution-plan-authority (:sh06-resolution-plan-authority products)
        content {:commit commit :tree tree}
        resolution-artifacts (mapv :resolution-artifact transactions)
        resolution-artifact-ids (mapv :artifact-id resolution-artifacts)
        semantic-projection-ids
        (mapv #(get-in % [:authenticated-core-request :lineage
                          :sh06-semantic-projection-id])
              transactions)
        resolution-artifact-paths
        (mapv #(vector :resolution-artifacts %) (range 7))
        semantic-projection-paths
        (mapv #(vector :semantic-projection-ids %) (range 7))
        link-authority-event-product-ids
        (mapv :product-id (subvec events 17 24))
        link-authority-id
        (p15-s23-sh07-b51-canonical-id
         [:gravity/sh07-b51-sh06-link-authority-id-e2-v1
          {:resolution-artifact-ids resolution-artifact-ids
           :semantic-projection-ids semantic-projection-ids
           :event-product-ids link-authority-event-product-ids
           :manifest-id (:manifest-id manifest)
           :resolution-plan-authority resolution-plan-authority
           :external-binding-authorities c5-authorities}])
        base
        {:content content
         :source-descriptors (subvec source-descriptors 3 10)
         :bundle-core-id (:execution-bundle-core-id products)
         :resolution-artifact-paths resolution-artifact-paths
         :resolution-artifacts resolution-artifacts
         :resolution-verifications resolution-verifications
         :semantic-projection-paths semantic-projection-paths
         :semantic-projection-ids semantic-projection-ids
         :module-source-descriptors module-descriptors
         :link-manifest manifest
         :link-authority-event-product-ids link-authority-event-product-ids
         :link-authority-id link-authority-id
         :expected-edge-census (:edges manifest)
         :resolution-plan-authority resolution-plan-authority
         :external-binding-authorities c5-authorities}
        carrier
        (assoc base :carrier-id
               (p15-s23-sh07-b51-canonical-id
                [:gravity/sh07-b51-sh06-authority-carrier-id-e2-v1 base]))]
    (when-not
     (and (= 13 (count source-descriptors))
          (= 40 (count events))
          (= (vec (range 7)) (mapv :module-ordinal c5-authorities))
          (= 7 (count (set (map :authority-id c5-authorities))))
          (= (mapv :logical-source-path module-descriptors)
             (mapv :source-path c5-authorities))
          (= (vec (repeat 7 content)) (mapv :content c5-authorities))
          (= content (:content resolution-plan-authority))
          (p15-s23-sh07-b51-resolution-plan-authority-valid?
           resolution-plan-authority)
          (= 7 (count module-descriptors) (count transactions)
             (count c5-authorities) (count resolution-artifacts)
             (count resolution-verifications)
             (count semantic-projection-ids))
          (= (:module-source-ids manifest)
             (mapv :module-source-id module-descriptors))
          (= (:manifest-id manifest) (:manifest-id (:bundle-core products)))
          (= (:execution-bundle-core-id products)
             (p15-s23-sh07-b51-canonical-id (:bundle-core products)))
          (= replayed-requests
             (mapv :authenticated-core-request transactions))
          (every? #(= :passed (:status %)) resolution-verifications)
          (every? p15-s23-sh07-b51-digest-id? semantic-projection-ids)
          (p15-s23-sh07-b51-exact-keys?
           base p15-s23-sh07-b51-sh06-authority-carrier-base-keys)
          (= (conj p15-s23-sh07-b51-sh06-authority-carrier-base-keys
                   :carrier-id)
             (set (keys carrier))))
      (p15-s23-sh07-b51-c5-authority-fail!
       :sh06-authority-carrier-join
       {:transaction-count (count transactions)
        :source-descriptor-count (count source-descriptors)}))
    carrier))
