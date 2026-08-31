

(defn p15-s23-stage2-gravity-checked-core-context
  [source-path source-text requested-target]
  (let [path-count
        (when (string? source-path)
          (p15-s23-closed-core-bounded-utf8-count source-path 4096))
        source-count
        (when (string? source-text)
          (p15-s23-closed-core-bounded-utf8-count
           source-text p15-s23-c6c10-max-source-bytes))
        safe-source-path
        (if (and (= :valid (:status path-count))
                 (qst-or-gravity-source? source-path))
          source-path
          "<gravity-checked-core>")]
   (when (= :over-limit (:status path-count))
     (p15-s23-c6c10-host-fail!
      "C6-CORE-SHAPE"
      safe-source-path
      :maximum-scalar-characters
      {:observed-scalar-characters (.length ^String source-path)
       :maximum-scalar-characters 4096}))
   (when (= :invalid-surrogate (:status path-count))
     (p15-s23-c6c10-host-fail!
      "C6-CORE-SHAPE"
      safe-source-path
      :well-formed-unicode-scalar-string
      {:value-kind :string}))
   (when-not (and (string? source-path)
                 (= :valid (:status path-count))
                 (qst-or-gravity-source? source-path)
                 (string? source-text)
                 (= :valid (:status source-count))
                 (keyword? requested-target)
                 (<= (count (str requested-target)) 256))
    (p15-s23-c6c10-host-fail!
     "C6-CORE-SHAPE"
     safe-source-path
     :bounded-co-canonical-source-context
     {:source-path-valid? (= :valid (:status path-count))
      :source-path-extension-valid?
      (and (string? source-path) (qst-or-gravity-source? source-path))
      :source-text? (string? source-text)
      :source-text-status (or (:status source-count) :not-a-string)
      :requested-target requested-target}))
  (p15-s23-c6c10-canonical-record source-path source-path)
  (p15-s23-c6c10-canonical-record source-path requested-target)
  {:kind :gravity/p15-s23-stage2-gravity-checked-core-context
   :source-path source-path
   :source-text source-text
   :source-content-hash (str "sha256:" (sha256-hex source-text))
   :requested-target requested-target}))

(defn p15-s23-c6c10-validate-public-context!
  [context]
  (let [trusted-top-level?
        (and (map? context)
             (contains? p15-s23-trusted-carrier-map-classes
                        (.getName (class context)))
             (nil? (meta context))
             (<= (count context) 5))]
    (when-not trusted-top-level?
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" "<gravity-checked-core>"
       :exact-public-source-verification-context
       {:context-class (some-> context class .getName)}))
    (p15-s23-c6c10-require-trusted-carrier!
     "<gravity-checked-core>" :gravity-checked-core-context context)
    (let [source-path
          (if (string? (:source-path context))
            (:source-path context)
            "<gravity-checked-core>")]
    (when-not
     (and (= p15-s23-c6c10-public-context-keys
             (set (keys context)))
          (= :gravity/p15-s23-stage2-gravity-checked-core-context
             (:kind context))
          (= context
             (p15-s23-stage2-gravity-checked-core-context
              (:source-path context) (:source-text context)
              (:requested-target context))))
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :exact-public-source-verification-context
       {:context-keys (set (keys context))}))
      context)))

(defn p15-s23-c6c10-throw-sealed-rejection!
  [source-path sealed-result]
  (let [diagnostic (or (first (:sealed-diagnostics sealed-result)) {})
        rule (or (:rule diagnostic) (:id diagnostic) "C6-VERIFY")
        contract (p15-s23-c6c10-upstream-diagnostic-contract rule)
        primary-span (or (get-in diagnostic [:primary :span]) {})
        missing-fact (get-in diagnostic [:facts :missing-fact])
        public-span (assoc primary-span
                           :source source-path
                           :file source-path)
        _ (when-not (keyword? missing-fact)
            (p15-s23-c6c10-host-fail!
             "C6-VERIFY" source-path
             :sealed-gravity-diagnostic-missing-fact
             {:source-span public-span}))
        origin-chain
        (if (and (vector? (:origin-chain diagnostic))
                 (<= (count (:origin-chain diagnostic)) 64))
          (:origin-chain diagnostic) [])
        syntax-id
        (p15-s23-c6c10-diagnostic-semantic-id
         (get-in diagnostic [:primary :syntax-id]))
        core-node-id
        (p15-s23-c6c10-diagnostic-semantic-id
         (or (get-in diagnostic [:primary :core-node-id])
             (get-in diagnostic [:primary :artifact])))
        operation-id
        (p15-s23-c6c10-diagnostic-semantic-id
         (or (get-in diagnostic [:primary :mir-operation-id])
             (get-in diagnostic [:primary :artifact])))
        origin-id
        (p15-s23-c6c10-diagnostic-semantic-id
         (or (get-in diagnostic [:primary :origin-id])
             (first origin-chain)))
        data
        (p15-s23-c6c10-owned-upstream-data
         (merge
          diagnostic
          {:id rule
           :rule rule
           :bootstrap-stage :stage0
           :stage (:stage contract)
           :diagnostic-family (:family contract)
           :document-id (:document-id contract)
           :expected-document (:expected-document contract)
           :source-span public-span
           :missing-fact missing-fact
           :syntax-id syntax-id
           :core-node-id core-node-id
           :operation-id operation-id
           :origin-id origin-id
           :generated-origin-chain origin-chain
           :sealed-diagnostic diagnostic
           :sealed-diagnostics (:sealed-diagnostics sealed-result)
           :digest-graph-proof (:graph-proof sealed-result)
           :clojure-seed-boundary? true
           :self-hosted? false}))]
    (throw
     (ex-info
      (str rule " from Gravity C6-C10 checked-core source")
      data))))

(defn p15-s23-c6c10-public-artifact
  [context source-binding ingress sealed-result]
  (let [template (:sealed-artifact-template sealed-result)
        target-request-metadata
        {:requested-target (:requested-target context)
         :source-target (:source-target template)
         :identity-bearing? false
         :downstream-lowering-required? true}
        physical-provenance-base
        {:actual-paths
         {:source (:source-path context)
          :gravity-c6-c10-source (:source-path source-binding)}
         :source-content-hash (:source-content-hash context)
         :c2-source-unit-id
         (get-in ingress [:front-end-products :source-unit-id])
         :c3-artifact-id
         (get-in ingress [:front-end-products :c3-artifact-id])
         :binding-pins
         (p15-s23-c6c10-binding-pins
          (:source-path context) ingress source-binding)
         :digest-graph-proof-id
         (get-in sealed-result [:graph-proof :graph-proof-id])}
        request-binding-id
        (p15-s23-c6c10-canonical-digest
         (:source-path context)
         {:domain :gravity/c6-c10-physical-request-binding-v1
          :artifact-id (:artifact-id template)
          :source-content-hash (:source-content-hash context)
          :target-request-metadata target-request-metadata
          :actual-paths (:actual-paths physical-provenance-base)
          :digest-graph-proof-id
          (:digest-graph-proof-id physical-provenance-base)})
        physical-provenance
        (assoc physical-provenance-base
               :request-binding-id request-binding-id)]
    (assoc template
           :target-request-metadata target-request-metadata
           :physical-provenance physical-provenance)))

(defn- p15-s23-c6c10-fresh-construction
  [context]
  (let [context (p15-s23-c6c10-validate-public-context! context)
        source-path (:source-path context)
        source-binding (p15-s23-c6c10-source-binding! source-path)
        ingress
        (p15-s23-c6c10-private-ingress-products
         source-path (:source-text context))
        envelope
        (p15-s23-c6c10-private-builder-envelope
         source-path ingress source-binding)
        raw-result
        (p15-s23-c6c10-invoke-pinned-source-function!
         source-path source-binding p15-s23-c6c10-builder-function
         [envelope] :gravity-source-builder)
        sealed-result
        (p15-s23-c6c10-seal-digest-request-result!
         source-path raw-result)]
    (when (= :rejected (:status sealed-result))
      (p15-s23-c6c10-throw-sealed-rejection!
       source-path sealed-result))
    (let [gravity-verification
          (p15-s23-c6c10-gravity-replay-verification!
           source-path source-binding envelope raw-result)
          artifact
          (p15-s23-c6c10-public-artifact
           context source-binding ingress sealed-result)]
      {:artifact artifact
       :context context
       :private-envelope envelope
       :private-source-binding source-binding
       :raw-result raw-result
       :sealed-result sealed-result
       :source-binding (dissoc source-binding :plan :function-manifest)
       :binding-pins (:binding-pins envelope)
       :graph-proof (:graph-proof sealed-result)
       :gravity-verification
       (select-keys gravity-verification
                    [:status :request-count :semantic-authority :checks])})))

(defn p15-s23-stage2-gravity-checked-core-source-artifact
  ([source-path source-text requested-target]
   (:artifact
    (p15-s23-c6c10-fresh-construction
     (p15-s23-stage2-gravity-checked-core-context
      source-path source-text requested-target))))
  ([context]
   (:artifact (p15-s23-c6c10-fresh-construction context))))