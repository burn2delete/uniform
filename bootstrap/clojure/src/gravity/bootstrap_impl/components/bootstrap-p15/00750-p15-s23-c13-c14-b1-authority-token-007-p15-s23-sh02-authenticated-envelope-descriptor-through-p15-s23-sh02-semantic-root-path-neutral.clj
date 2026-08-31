(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn p15-s23-sh02-authenticated-envelope-descriptor
  [stage packet workspace-root invocation-root]
  (let [record (get packet stage)
        projections (p15-s23-sh02-stage-semantic-projections stage packet)
        facts (p15-s23-sh02-fact-transitions stage packet)
        identities (p15-s23-sh02-stage-identity-subjects stage packet)
        logical-source-path
        (case stage :c13 p15-s23-c13-source-relative-path
                    :b1 p15-s23-b1-source-relative-path)
        actual-source-path
        (case stage
          :c13 (get-in record [:actual-path-provenance :c13-source])
          :b1 (get-in record [:actual-path-provenance :b1-source]))]
    {:artifact :gravity/private-authenticated-envelope-descriptor
     :schema-version 1
     :stage (case stage :c13 :c13-mir-optimization
                        :b1 :b1-backend-interface)
     :artifact-kind (:artifact record)
     :source-revision
     (p15-s23-sh02-stage-source-revision record logical-source-path)
     :projection-contract
     {:contract-kind (case stage :c13 :identity-optimization-pass
                                 :b1 :backend-input-admission)
      :contract-version 1 :profile :hosted
      :target :llvm-x86_64-linux
      :required-semantic-projections (mapv :name projections)
      :required-fact-families (mapv :name facts)
      :required-identity-subjects (mapv :name identities)}
     :semantic-projections projections
     :fact-transitions facts
     :effect-capability-relation
     (p15-s23-sh02-effect-capability-relation stage packet)
     :proof-composite (p15-s23-sh02-proof-composite stage packet)
     :preservation
     {:requires (mapv :name facts)
      :preserves (mapv :name facts)
      :invalidates [] :regenerates [] :residual-checks []}
     :identity-subjects identities
     :lineage (p15-s23-sh02-stage-lineage stage packet)
     :reference-closure (p15-s23-sh02-reference-closure stage packet)
     :actual-path-provenance
     {:source-path actual-source-path
      :workspace-root workspace-root
      :invocation-root invocation-root}
     :bounds p15-s23-sh02-authenticated-envelope-bounds}))

(def p15-s23-sh02-builder-result-keys
  #{:status :artifact-template :digest-requests :digest-graph-root
    :digest-graph-roots :semantic-envelope-root
    :provenance-binding-root :identity-checks :diagnostics :authority})

(def p15-s23-sh02-builder-authority
  {:semantic-owner :gravity-source
   :host-role :bounded-validation-hashing-and-instantiation
   :scope :reusable-authenticated-envelope
   :semantic-root-path-neutral? true
   :physical-provenance-separate? true
   :self-hosted? false})

(def p15-s23-sh02-verifier-result-keys
  #{:status :artifact-template :digest-graph-root :digest-graph-roots
    :semantic-envelope-root :provenance-binding-root :identity-checks
    :identity-enforcement :eligible-for-contextual-acceptance?
    :request-count :semantic-authority :checks :diagnostics})

(def p15-s23-sh02-verifier-checks
  [:fresh-descriptor-replay
   :exact-artifact-template-replay
   :exact-digest-request-replay
   :path-neutral-semantic-root
   :separate-physical-provenance-root])

(def p15-s23-sh02-stage-envelope-keys
  #{:artifact :schema-version :status :stage :sealed-artifact
    :semantic-envelope-id
    :provenance-binding-id :identity-checks :request-count
    :request-graph-id :gravity-template-replay :source-rule
    :diagnostics :semantic-authority :host-tcb :self-hosted?})

(def p15-s23-sh02-template-replay-summary-keys
  #{:status :identity-enforcement :eligible-for-contextual-acceptance?
    :request-count :semantic-authority :checks :diagnostics})

(defn- p15-s23-sh02-fail!
  [source-path subject missing-fact facts]
  (p15-s23-b3-llvm-fail!
   "B1-METADATA" source-path subject
   (merge {:missing-fact missing-fact
           :sh02-boundary :authenticated-envelope}
          facts)))

(defn- p15-s23-sh02-require-bounded-carrier!
  [source-path carrier value]
  (let [bounds p15-s23-sh02-authenticated-envelope-bounds
        validation
        (p15-s23-trusted-carrier-validation
         value :default-only
         (:maximum-carrier-nodes bounds)
         (:maximum-carrier-depth bounds)
         (:maximum-digest-requests bounds))]
    (when-not (= :passed (:status validation))
      (p15-s23-sh02-fail!
       source-path {} :trusted-bounded-sh02-carrier
       (merge {:carrier carrier}
              (select-keys
               validation
               [:reason :observed-nodes :observed-depth
                :maximum-nodes :maximum-depth :maximum-width]))))
    validation))

(defn- p15-s23-sh02-validate-builder-carrier!
  [source-path raw-result]
  (p15-s23-sh02-require-bounded-carrier!
   source-path :gravity-builder-result raw-result)
  ;; The SH-02 carrier preflight above enforces its 2,048-request contract
  ;; before the shared canonical C6/C10 walker applies the remaining depth,
  ;; scalar, integer, and per-container limits.
  (try
    (p15-s23-c6c10-validate-builder-result-canonical-carrier!
     source-path raw-result)
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch StackOverflowError _
      (p15-s23-sh02-fail!
       source-path {} :bounded-sh02-builder-host-stack {}))
    (catch clojure.lang.ExceptionInfo exception
      (p15-s23-sh02-fail!
       source-path {} :canonical-sh02-builder-carrier
       {:bounded-reason (:missing-fact (ex-data exception))})))
  raw-result)

(defn- p15-s23-sh02-validate-builder-envelope!
  [source-path raw-result]
  (p15-s23-sh02-validate-builder-carrier! source-path raw-result)
  (when-not
   (and (map? raw-result)
        (= p15-s23-sh02-builder-result-keys
           (set (keys raw-result)))
        (= :accepted (:status raw-result))
        (map? (:artifact-template raw-result))
        (vector? (:digest-requests raw-result))
        (pos? (count (:digest-requests raw-result)))
        (<= (count (:digest-requests raw-result))
            (:maximum-digest-requests
             p15-s23-sh02-authenticated-envelope-bounds))
        (vector? (:digest-graph-roots raw-result))
        (= 2 (count (:digest-graph-roots raw-result)))
        (= (:digest-graph-root raw-result)
           (:semantic-envelope-root raw-result)
           (first (:digest-graph-roots raw-result)))
        (= (:provenance-binding-root raw-result)
           (second (:digest-graph-roots raw-result)))
        (vector? (:identity-checks raw-result))
        (<= (count (:identity-checks raw-result))
            (:maximum-identity-subjects
             p15-s23-sh02-authenticated-envelope-bounds))
        (= [] (:diagnostics raw-result))
        (= p15-s23-sh02-builder-authority (:authority raw-result)))
    (p15-s23-sh02-fail!
     source-path raw-result :exact-sh02-gravity-builder-envelope
     {:observed-status (:status raw-result)
      :observed-keys (when (map? raw-result)
                       (set (keys raw-result)))}))
  raw-result)

(defn- p15-s23-sh02-path-values
  [actual-path-provenance]
  (->> (vals actual-path-provenance)
       (filter string?)
       set))

(defn- p15-s23-sh02-semantic-root-path-neutral?
  [semantic-preimage actual-path-provenance]
  (let [path-values (p15-s23-sh02-path-values actual-path-provenance)]
    (and
     (not-any?
      (fn [item]
        (and (map? item) (contains? item :actual-path-provenance)))
      (p15-s23-sh02-contained-values semantic-preimage))
     (empty?
      (set/intersection
       path-values
       (->> (p15-s23-sh02-contained-values semantic-preimage)
            (filter string?)
            set)))))))
