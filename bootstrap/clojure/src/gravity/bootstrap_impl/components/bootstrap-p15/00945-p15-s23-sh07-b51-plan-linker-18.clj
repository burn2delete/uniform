
(defn p15-s23-sh07-b51-build-sh06-authority-carrier
  "Reconstruct retained SH06 authority without acquiring a source. This is
  source-handoff validation only; it grants no whole-object admission."
  [commit tree products]
  (let [receipt (:source-read-receipt products)
        retained (get-in products [:sh06-authority-carrier
                                  :external-binding-authorities])
        preimage (:preimage (first retained))
        content {:commit commit :tree tree}]
    (when-not
     (and (p15-s23-sh07-b51-exact-keys?
           products p15-s23-sh07-b51-v9-builder-receipted-product-keys)
          (p15-s23-sh07-b51-exact-keys?
           (:sh06-authority-carrier products)
           (conj p15-s23-sh07-b51-sh06-authority-carrier-base-keys :carrier-id))
          (= content (select-keys receipt [:commit :tree]))
          (p15-s23-sh07-b51-exact-keys?
           receipt #{:schema :role :commit :tree :source-descriptors
                     :source-descriptors-root :events :event-root
                     :counters :finalized})
          (= :gravity/sh07-b51-source-read-receipt-v3 (:schema receipt))
          (contains? #{:producer-census :independent-verifier-census :m}
                     (:role receipt))
          (true? (:finalized receipt))
          (= {:acquire-count 13 :decode-count 13
              :event-count 40 :finalize-count 1} (:counters receipt))
          (vector? retained) (= 7 (count retained))
          (= (vec (range 7)) (mapv :module-ordinal retained))
          (= (vec (repeat 7 preimage)) (mapv :preimage retained))
          (= content (:content preimage))
          (= (nth (:source-descriptors receipt) 12 nil)
             (get-in products [:sh06-resolution-plan-authority
                               :source-descriptor]))
          (= (:source-descriptors preimage)
             (subvec (:source-descriptors receipt) 3 10))
          (= (:m0s products) (:module-source-descriptors preimage))
          (= (:m1 products) (:link-manifest preimage))
          (vector? (:module-transactions products))
          (= 7 (count (:module-transactions products)))
          (= (vec (range 7))
             (mapv :module-ordinal (:module-transactions products)))
          (= (:source-descriptors preimage)
             (mapv :source-descriptor (:module-transactions products)))
          (every? #(p15-s23-sh07-b51-exact-keys?
                    % #{:module-ordinal :source-descriptor :resolution-artifact
                        :c2-reader-artifact :authenticated-core-request
                        :resolved-digests :module-core :template-carrier
                        :generated-digest-carrier :resolved-core-carrier
                        :module-digest-requests})
                  (:module-transactions products))
          (p15-s23-sh07-b51-source-semantic-carrier-valid?
           content receipt (:module-transactions products)
           (:source-semantic-carrier products)))
      (p15-s23-sh07-b51-c5-authority-fail! :retained-carrier-join {}))
    (p15-s23-sh07-b51-replay-returned-source-authority-events!
     (:m0s products) (:m1 products)
     (:sh06-resolution-plan-authority products) receipt)
    (doseq [ordinal (range 17 24)]
      (let [event (nth (:events receipt) ordinal)
            plan (nth p15-s23-sh07-b51-source-event-plan ordinal)]
        (when-not (and (= plan (select-keys event (keys plan)))
                       (= (:source-raw-sha256 event)
                          (:raw-sha256 (nth (:source-descriptors receipt)
                                           (:source-ordinal plan))))
                       (= (:product-id event)
                          (p15-s23-sh07-b51-source-event-product-id event true)))
          (p15-s23-sh07-b51-c5-authority-fail!
           :retained-link-event {:ordinal ordinal}))))
    (let [sources (:sources
                   (p15-s23-sh07-b51-validate-c5-authority-preimage! preimage))
          transactions (:module-transactions products)
          authorities
          (mapv (fn [ordinal source transaction]
                  (let [sh05 (get-in transaction
                                     [:resolution-artifact :sh05-macro-artifact])]
                    (p15-s23-sh07-b51-c5-external-binding-authority
                     ordinal source sh05
                     (sh06-resolution-base-request (:path source) sh05) preimage)))
                (range 7) sources transactions)
          contexts (mapv #(p15-s23-sh07-b51-sh06-context
                           % (:sh06-resolution-plan-authority products))
                         authorities)
          artifacts (mapv :resolution-artifact transactions)]
      (when-not (= (:source-forms products)
                   (mapv (comp vec :forms :module) sources))
        (p15-s23-sh07-b51-c5-authority-fail! :retained-source-forms {}))
      (when-not (= retained authorities)
        (p15-s23-sh07-b51-c5-authority-fail! :retained-c5-authorities {}))
      (p15-s23-sh07-b51-assemble-sh06-authority-carrier
       commit tree products (:source-descriptors receipt) (:events receipt)
       authorities
       (mapv sh06-resolution-artifact-verification artifacts contexts)
       (mapv sh07-core-authenticated-request artifacts contexts)))))
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
