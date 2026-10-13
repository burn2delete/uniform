
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
            receipt (p15-s23-sh07-b51-finalize-source-session!)
            returned-products
            {:m0s (:module-source-descriptors products)
             :m1 (:link-manifest products)
             :m2s (:module-plans products)
             :bundle-core (:execution-bundle-core products)
             :execution-bundle-core-id (:execution-bundle-core-id products)
             :source-forms source-forms
             :module-transactions module-transactions
             :sh06-resolution-plan-authority resolution-plan-authority
             :source-semantic-carrier source-semantic-carrier
             :source-read-receipt receipt}]
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
