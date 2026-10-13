
(defn p15-s23-sh07-b51-build-execution-bundle
  ([]
   (p15-s23-sh07-b51-build-execution-bundle
    (p15-s23-sh07-b51-current-emitter)))
  ([emitter]
  (when-not (= emitter (p15-s23-sh07-b51-current-emitter))
    (throw (ex-info "unauthenticated SH-07 B51 compiler identity"
                    {:id "L2-UNKNOWN-CORE-FORM"
                     :reason :unauthenticated-b51-external-link})))
  (let [{:keys [module-source-descriptors link-manifest sources]}
        (p15-s23-sh07-b51-build-link-authority)
        compilation-emitter
        (p15-s23-stage2-compiler-artifact-augmented-emitter emitter)
        raw-plans
        (mapv
         (fn [descriptor source]
           (let [module
                 (assoc (:module source)
                        :p15-s23-sh07-module-source-descriptor descriptor
                        :p15-s23-sh07-link-manifest link-manifest)]
             (p15-s23-stage2-construct-emitted-core-plan
              compilation-emitter (:path source) (:text source)
              module (:table source))))
         module-source-descriptors sources)
        ;; M2 identity is constructed lexically from the seven plans emitted
        ;; immediately above.  There is deliberately no Var that accepts an
        ;; arbitrary plan and returns an M2 product or id.
        plans
        (mapv
         (fn [descriptor raw-plan]
           (let [emitter-source-id
                 (get-in raw-plan
                         [:compiler :expression-lowering-source-content-hash])
                 emitter-semantic-id
                 (get-in raw-plan
                         [:compiler :expression-lowering-semantic-hash])
                 base
                 (-> (dissoc raw-plan :plan-id)
                     (assoc :sh07-b51-module-source-id
                            (:module-source-id descriptor)
                            :sh07-b51-link-manifest-id
                            (:manifest-id link-manifest))
                     ;; Attempt30 makes the already authenticated compiler
                     ;; binding coordinates explicit in every M2.  The two
                     ;; census implementations join these aliases without
                     ;; depending on a lower-level compiler-field spelling.
                     (assoc-in [:compiler :source-content-hash]
                               emitter-source-id)
                     (assoc-in [:compiler :semantic-hash]
                               emitter-semantic-id))
                 identity
                 (-> base
                     (update :source dissoc :path)
                     (update :module dissoc :source-path))]
             (assoc base :plan-id
                    (c4-artifact-id
                     (c-backend-canonical-value identity)))))
         module-source-descriptors raw-plans)
        plan-by-source
        (into {} (map (juxt :sh07-b51-module-source-id identity)) plans)
        bundle-core
        {:domain :gravity/sh07-b51-execution-bundle-core-id-v1
         :schema-version 1
         :manifest-id (:manifest-id link-manifest)
         :module-plans
         (mapv (fn [descriptor plan]
                 {:module-ordinal (:module-ordinal descriptor)
                  :module-source-id (:module-source-id descriptor)
                  :plan-id (:plan-id plan)})
               module-source-descriptors plans)
         :routes
         (mapv (fn [edge]
                 {:edge-ordinal (:ordinal edge)
                  :caller-plan-id
                  (:plan-id
                   (get plan-by-source (:caller-module-source-id edge)))
                  :callee-plan-id
                  (:plan-id
                   (get plan-by-source (:callee-module-source-id edge)))})
               (:edges link-manifest))
         :public-roots
         (mapv (fn [root]
                 {:root-ordinal (:root-ordinal root)
                  :function (:function root)
                  :arity (:arity root)
                  :plan-id (:plan-id
                            (nth plans (:module-ordinal root)))})
               p15-s23-sh07-b51-public-root-specs)}
        bundle-core-id (p15-s23-sh07-b51-canonical-id bundle-core)
        emitter-source-id (get-in (first plans)
                                  [:compiler :source-content-hash])
        emitter-semantic-id (get-in (first plans)
                                    [:compiler :semantic-hash])
        products
        {:artifact :gravity/sh07-b51-linked-plan-products-v2
         :module-source-descriptors module-source-descriptors
         :link-manifest link-manifest
         :module-plans plans
         :emitter-source-id emitter-source-id
         :emitter-semantic-id emitter-semantic-id
         :execution-bundle-core bundle-core
         :execution-bundle-core-id bundle-core-id}]
    (when-not (and
               (p15-s23-sh07-b51-digest-id? emitter-source-id)
               (p15-s23-sh07-b51-digest-id? emitter-semantic-id)
               (every? #(and (= emitter-source-id
                                 (get-in % [:compiler :source-content-hash]))
                              (= emitter-semantic-id
                                 (get-in % [:compiler :semantic-hash])))
                       plans)
               (p15-s23-sh07-b51-execution-bundle-core-valid-structure?
                module-source-descriptors link-manifest plans bundle-core
                bundle-core-id))
      (throw (ex-info "invalid fresh-source SH-07 B51 bundle-core products"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-external-link
                       :emitter-source-id emitter-source-id
                       :emitter-semantic-id emitter-semantic-id
                       :manifest-valid?
                       (p15-s23-sh07-b51-link-manifest-valid?
                        module-source-descriptors link-manifest)
                       :plan-valid?
                       (mapv #(p15-s23-sh07-b51-plan-valid-structure?
                               %1 link-manifest %2)
                             module-source-descriptors plans)
                       :plan-links-valid?
                       (mapv #(p15-s23-sh07-b51-plan-links-valid-structure?
                               %1 link-manifest %2)
                             module-source-descriptors plans)
                       :bundle-core-id bundle-core-id
                       :computed-bundle-core-id
                       (p15-s23-sh07-b51-canonical-id bundle-core)})))
    products)))

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
