
(defn p15-s23-sh07-b51-build-sh06-authority-carrier
  [commit tree products]
  (let [expected-product-keys p15-s23-sh07-b51-v9-builder-receipted-product-keys
        receipt (:source-read-receipt products)
        source-descriptors (:source-descriptors receipt)
        module-descriptors (:m0s products)
        manifest (:m1 products)
        transactions (:module-transactions products)
        resolution-plan-authority (:sh06-resolution-plan-authority products)
        source-semantic-carrier (:source-semantic-carrier products)
        content {:commit commit :tree tree}
        entries
        (mapv
         (fn [module-ordinal]
           (p15-s23-sh07-b51-acquire-source-entry
            commit tree (nth p15-s23-sh07-b51-source-specs
                             (+ 3 module-ordinal))))
         (range 7))
        sources
        (mapv
         (fn [module-ordinal entry]
           (p15-s23-sh07-b51-module-input-record
            (p15-s23-sh07-b51-module-spec module-ordinal) entry))
         (range 7) entries)
        preimage
        {:schema :gravity/sh07-b51-c5-authority-preimage-v1
         :content content
         :content-object-proof (:content-object-proof (first entries))
         :source-descriptors (mapv :descriptor entries)
         :module-source-inputs
         (mapv #(select-keys % [:descriptor :source-object-proof]) entries)
         :module-source-descriptors module-descriptors
         :link-manifest manifest}
        _ (p15-s23-sh07-b51-validate-c5-authority-preimage! preimage)
        c5-authorities
        (mapv
         (fn [module-ordinal source transaction]
           (let [resolution (:resolution-artifact transaction)
                 sh05 (:sh05-macro-artifact resolution)
                 base (sh06-resolution-request (:path source) sh05)]
             (p15-s23-sh07-b51-c5-external-binding-authority
              module-ordinal source sh05 base preimage)))
         (range 7) sources transactions)
        contexts
        (mapv #(p15-s23-sh07-b51-sh06-context
                % resolution-plan-authority)
              c5-authorities)
        resolution-artifacts (mapv :resolution-artifact transactions)
        resolution-artifact-ids (mapv :artifact-id resolution-artifacts)
        resolution-verifications
        (mapv sh06-resolution-artifact-verification
              resolution-artifacts contexts)
        replayed-requests
        (mapv sh07-core-authenticated-request
              resolution-artifacts contexts)
        semantic-projection-ids
        (mapv #(get-in % [:authenticated-core-request :lineage
                          :sh06-semantic-projection-id])
              transactions)
        resolution-artifact-paths
        (mapv #(vector :resolution-artifacts %) (range 7))
        semantic-projection-paths
        (mapv #(vector :semantic-projection-ids %) (range 7))
        link-authority-event-product-ids
        (mapv :product-id (subvec (:events receipt) 17 24))
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
     (and (p15-s23-sh07-b51-exact-keys? products expected-product-keys)
          (= content (select-keys receipt [:commit :tree]))
          (= 13 (count source-descriptors))
          (= {:acquire-count 13 :decode-count 13
              :event-count 40 :finalize-count 1}
             (:counters receipt))
          (= (:source-descriptors preimage) (subvec source-descriptors 3 10))
          (= content (:content resolution-plan-authority))
          (p15-s23-sh07-b51-resolution-plan-authority-valid?
           resolution-plan-authority)
          (p15-s23-sh07-b51-source-semantic-carrier-valid?
           content receipt transactions source-semantic-carrier)
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

(defn p15-s23-sh07-b51-sh06-authority-carrier-valid?
  [commit tree products carrier]
  (try
    (= carrier
       (p15-s23-sh07-b51-build-sh06-authority-carrier
        commit tree products))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-runtime-context [linked-products runtime]
  ;; Historical host compatibility surface.  It deliberately cannot attach a
  ;; runtime token: Attempt32 admits invocation only through bounded carrier-v4
  ;; preflight, local authority, equality/joins, and root/route/arity checks.
  (let [_ linked-products]
    (p15-s23-sh07-b51-unadmitted-runtime-context runtime)))

(defn p15-s23-sh07-b51-runtime-augmented-plan-authorized?
  [runtime plan]
  (try
    (let [context (p15-s23-sh07-b51-admitted-runtime-context runtime)
          plans (:module-plans context)]
      (boolean
       (and (map? runtime) (map? plan)
            context
            (some #(identical? plan %) plans))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-execute-public-root
  [root-ordinal args runtime]
  (let [_ [root-ordinal args runtime]]
    (throw
     (ex-info "Attempt32 public roots require linked carrier-v4 admission"
              {:id "L2-UNKNOWN-CORE-FORM"
               :reason :unauthenticated-b51-linked-carrier
               :result-committed? false
               :output-committed? false}))))

(defn- p15-s23-sh07-b51-plan-valid-structure?
  [descriptor manifest plan]
  (let [plan-functions (:functions plan)
        descriptor-functions (:definitions descriptor)
        exports (set (:exports descriptor))]
  (and (p15-s23-sh07-b51-module-source-valid? descriptor)
       (map? plan-functions)
       (= (set (map :name descriptor-functions))
          (set (keys plan-functions)))
       (every? (fn [row]
                 (let [definition (get plan-functions (:name row))]
                   (and (= (:arity row) (:arity definition))
                        (= (:source-hash row)
                           (p15-s23-sh07-source-definition-hash definition))
                        (= (case (:visibility row)
                             :public :public
                             :private (if (empty? exports)
                                        :stage2-local
                                        :private)
                             ::invalid)
                           (get-in definition [:binding :visibility])))))
               descriptor-functions)
       (= (:module-source-id descriptor)
          (:sh07-b51-module-source-id plan))
       (= (:manifest-id manifest) (:sh07-b51-link-manifest-id plan))
       (= (:module descriptor) (get-in plan [:module :module]))
       (= (:source-sha256 descriptor) (get-in plan [:source :sha256]))
       (= (:plan-id plan)
          (c4-artifact-id
           (c-backend-canonical-value
            (-> plan
                (dissoc :plan-id)
                (update :source dissoc :path)
                (update :module dissoc :source-path))))))))

(defn- p15-s23-sh07-b51-plan-external-links [plan]
  (vec
   (for [[caller definition] (:functions plan)
         instruction (tree-seq coll? seq (:instructions definition))
         :when (and (map? instruction) (= :function-call (:op instruction))
                    (contains? instruction :external-link))]
     (let [ordinal (get-in instruction [:external-link :edge-ordinal])]
       {:caller caller
        :callee (:function instruction)
        :arity (count (:args instruction))
        :edge-ordinal ordinal
        :external-link (:external-link instruction)}))))

(defn- p15-s23-sh07-b51-plan-links-valid-structure?
  [descriptor manifest plan]
  (let [expected
        (filter #(= (:module-source-id descriptor)
                    (:caller-module-source-id %)) (:edges manifest))
        edge-by-ordinal (into {} (map (juxt :ordinal identity)) expected)
        actual
        (mapv (fn [row]
                (assoc row :callsite-path
                       (:callsite-path
                        (get edge-by-ordinal (:edge-ordinal row)))))
              (p15-s23-sh07-b51-plan-external-links plan))]
    (and (= (count expected) (count actual))
         (= (set (map (fn [row]
                        [(:caller-function row) (:callee-function row)
                         (:arity row) (:ordinal row)
                         (:callsite-path row)]) expected))
            (set (map (fn [row]
                        [(:caller row) (:callee row) (:arity row)
                         (:edge-ordinal row) (:callsite-path row)]) actual)))
         (every? #(= {:artifact :gravity/sh07-b51-external-call-v1
                      :manifest-id (:manifest-id manifest)
                      :edge-ordinal
                   (get-in % [:external-link :edge-ordinal])}
                     (:external-link %)) actual))))
