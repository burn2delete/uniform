
(defn- p15-s23-sh07-b51-current-emitter []
  (if *p15-s23-sh07-b51-governed-source-session*
    (or (p15-s23-sh07-b51-session-checked-product :current-emitter)
        (let [emitter
              (p15-s23-sh07-b51-governed-source-event!
               :compiler-emitter 10 nil
               'gravity.bootstrap/p15-s23-sh07-b51-current-emitter
               (fn [{:keys [descriptor text]}]
                 (let [source-path (:path descriptor)
                       forms
                       (:forms
                        (p15-s23-compiler-source-form-record-from-text
                         source-path text))]
                   (p15-s23-compiler-def-value
                    source-path forms 'p15-s23-stage2-plan-emitter))))]
          (p15-s23-sh07-b51-cache-session-product!
           :current-emitter emitter)))
    (let [{:keys [path text]}
          (p15-s23-sh07-b51-compatibility-source-record
           p15-s23-sh07-b51-compiler-source-path)
          forms (:forms
                 (p15-s23-compiler-source-form-record-from-text path text))]
      (p15-s23-compiler-def-value
       path forms 'p15-s23-stage2-plan-emitter))))

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
        sources
        (or (when *p15-s23-sh07-b51-governed-source-session*
              (p15-s23-sh07-b51-session-checked-product :plan-sources))
            sources)
        compilation-emitter
        (p15-s23-stage2-compiler-artifact-augmented-emitter emitter)
        raw-plans
        (mapv
         (fn [descriptor source]
           (let [module
                 (cond->
                  (assoc (:module source)
                         :p15-s23-sh07-module-source-descriptor descriptor
                         :p15-s23-sh07-link-manifest link-manifest)
                   (:source-authority source)
                   (assoc :p15-s23-sh07-source-authority
                          (:source-authority source))
                   (:compiler-artifact-binding source)
                   (assoc :p15-s23-sh07-compiler-artifact-binding
                          (:compiler-artifact-binding source)))]
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

(defn- p15-s23-sh07-b51-project-context
  [commit tree source-path]
  (let [project-root-id
        (reader-canonical-hash
         [:gravity/sh07-b51-c2-project-root-id-v1
          {:content {:commit commit :tree tree}
           :scope :sh07-b51-c2-explicit-project}])]
    {:project-root-id project-root-id
     :project-root-path nil
     :project-relative-path source-path}))

(def ^:private p15-s23-sh07-b51-resolved-pass
  {:artifact :gravity/sh07-core-resolved-verification
   :schema-version 16 :status :passed :rule nil})

(def ^:private p15-s23-sh07-b51-resolved-reject
  {:artifact :gravity/sh07-core-resolved-verification
   :schema-version 16 :status :rejected :rule "C6-VERIFY"})

;; SH-06 is intentionally unaware of B51's seven-fragment manifest.  The
;; pure constructors below close the exact M0/M1-to-SH06 import projection.
;; Installing that projection requires an explicit lexical SH06 constructor
;; and verifier seam; ambient Var state is deliberately not used as authority.

(defn- p15-s23-sh07-b51-c5-authority-fail!
  [cause details]
  (p15-s23-sh07-b51-governed-read-fail!
   :unauthenticated-c5-external-binding
   (assoc details :cause cause)))

(def ^:private p15-s23-sh07-b51-c5-preimage-keys
  #{:schema :content :content-object-proof :source-descriptors
    :module-source-inputs :module-source-descriptors :link-manifest})

(def ^:private p15-s23-sh07-b51-c5-authority-keys
  #{:schema :schema-version :content :module-ordinal :source-path
    :sh05-artifact-id :base-request-id :imports :import-bindings
    :dependency-edges :preimage :self-id-omits :authority-id})

(def ^:private p15-s23-sh07-b51-c5-import-keys
  #{:namespace :alias :kind :profile :targets :visibility :refer :effects
    :capabilities :safety :boundary :semantic-span :source-span
    :dependency-artifact-id :foreign-record-complete?})

(def ^:private p15-s23-sh07-b51-c5-import-binding-keys
  #{:name :kind :namespace :package :binding-class :visibility :profile-set
    :target-set :type-ref :effects :capabilities :safety :semantic-span
    :source-span :definition-syntax-id :definition-artifact-id})

(def ^:private p15-s23-sh07-b51-c5-dependency-edge-keys
  #{:from :to :kind :boundary :semantic-span :source-span})

(def ^:private p15-s23-sh07-b51-resolution-plan-authority-keys
  #{:schema :content :content-object-proof :source-descriptor
    :source-object-proof :plan-binding :self-id-omits :authority-id})

(def ^:private p15-s23-sh07-b51-resolution-plan-binding-keys
  #{:source-path :source-byte-count :source-content-hash
    :plan-semantic-hash :functions-semantic-hash :function-count
    :function-names-hash :function-shapes-hash
    :public-function-hashes :public-function-shapes
    :artifact :status :semantic-authority :compiled-by :executed-by
    :generic-bridge-residual? :self-hosted? :plan})

(def ^:private p15-s23-sh07-b51-sh06-context-keys
  #{:external-binding-authority :resolution-plan-authority})
