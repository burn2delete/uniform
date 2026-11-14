(require '[gravity.checked-core.bool-branch-join :as bool-branch-join]
         '[gravity.c6-c10.source-interface :as c6-source-interface])

(declare sh05-macro-resolve-source-path sh06-resolution-resolve-source-path)

(defn- p15-s23-c6c10-current-source-snapshot-matches-binding?
  [source-binding source-snapshot]
  (and (= (:source-byte-count source-binding)
          (:source-byte-count source-snapshot))
       (= (:source-content-hash source-binding)
          (:source-content-hash source-snapshot))
       (= (p15-s23-c6c10-canonical-file-path
           (:source-path source-binding))
          (:canonical-source-path source-snapshot))))

(defn- p15-s23-c6c10-fresh-construction
  ([context] (p15-s23-c6c10-fresh-construction context 1))
  ([context version]
  (let [context (p15-s23-c6c10-validate-public-context! context)
        source-path (:source-path context)
        source-binding (p15-s23-c6c10-source-binding!
                        source-path version
                        (when (contains? #{2 3} version) :development-current))
        reader (when (contains? #{2 3} version)
                 (p15-s23-c6c10-current-reader-program! source-path))
        _ (when (contains? #{2 3} version)
            (when-not
             (and (p15-s23-c6c10-current-source-snapshot-matches-binding?
                   source-binding (get-in reader [:c6-source-snapshot :before]))
                  (p15-s23-c6c10-current-source-snapshot-matches-binding?
                   source-binding (get-in reader [:c6-source-snapshot :after])))
              (p15-s23-c6c10-host-fail!
               "C6-VERIFY" source-path
               :stable-current-c6-source-binding
               {:source-binding
                (select-keys source-binding [:source-byte-count :source-content-hash])
                :reader-snapshot
                (select-keys (get-in reader [:c6-source-snapshot :after])
                             [:source-byte-count :source-content-hash])})))
        expression (when (contains? #{2 3} version)
                     (p15-s23-stage2-current-expression-development-binding!
                      source-path))
        ingress
        (if (contains? #{2 3} version)
          (binding [*p15-s23-stage2-compiler-artifact-binding* expression]
            (p15-s23-c6c10-private-ingress-products
             source-path (:source-text context) (get-in reader [:invocation :reader-source-path]) version
             (dissoc (:binding reader) :plan) (:invocation reader)))
          (p15-s23-c6c10-private-ingress-products source-path (:source-text context)))
        current-source-before-builder
        (when (contains? #{2 3} version)
          (p15-s23-c6c10-read-current-source-snapshot! source-path))
        _ (when (contains? #{2 3} version)
            (when-not
             (p15-s23-c6c10-current-source-snapshot-matches-binding?
              source-binding current-source-before-builder)
              (p15-s23-c6c10-host-fail!
               "C6-VERIFY" source-path
               :stable-current-c6-source-before-builder
               {:source-binding
                (select-keys source-binding [:source-byte-count :source-content-hash])
                :current-source-snapshot
                (select-keys current-source-before-builder
                             [:source-byte-count :source-content-hash])})))
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
          current-source-after-verification
          (when (contains? #{2 3} version)
            (p15-s23-c6c10-read-current-source-snapshot! source-path))
          _
          (when (contains? #{2 3} version)
            (when-not
             (p15-s23-c6c10-current-source-snapshot-matches-binding?
              source-binding current-source-after-verification)
              (p15-s23-c6c10-host-fail!
               "C6-VERIFY" source-path
               :stable-current-c6-source-after-verification
               {:source-binding
                (select-keys source-binding [:source-byte-count :source-content-hash])
                :current-source-snapshot
                (select-keys current-source-after-verification
                             [:source-byte-count :source-content-hash])})))
          artifact
          (p15-s23-c6c10-public-artifact
           context source-binding ingress sealed-result)]
      (when (and (= 2 version)
                 (not (bool-branch-join/source-family?
                       artifact (= [:gravity/bool] (get-in (last (:core-nodes artifact)) [:type :params])))))
        (p15-s23-c6c10-host-fail! "C6-VERIFY" source-path :v2-bool-branch-join-source-family {}))
      (when (= 3 version)
        (p15-s23-c6c10-require-trusted-carrier! source-path :flat-bool-constructed-core artifact)
        (when-not (bool-branch-join/flat-sequence-source-family? artifact)
          (p15-s23-c6c10-host-fail! "C6-VERIFY" source-path :v3-flat-bool-sequence-source-family {})))
      (cond-> {:artifact artifact
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
                    [:status :request-count :semantic-authority :checks])}
        (contains? #{2 3} version)
        (assoc :source-policy-receipt
               (let [receipt (assoc (:source-policy-receipt ingress)
                              :expression-binding
                              (p15-s23-c6c10-project-expression-owner-metadata
                               source-path (dissoc expression :plan)))]
                 (p15-s23-c6c10-require-trusted-carrier! source-path :complete-current-source-policy-receipt receipt)
                 receipt)))))))

(defn p15-s23-stage2-gravity-checked-core-source-artifact
  ([context]
   (:artifact (p15-s23-c6c10-fresh-construction context)))
  ([context header]
   (let [version (c6-source-interface/interface-version! header)]
     (:artifact (p15-s23-c6c10-fresh-construction context version))))
  ([source-path source-text requested-target]
   (:artifact (p15-s23-c6c10-fresh-construction
               (p15-s23-stage2-gravity-checked-core-context source-path source-text requested-target))))
  ([source-path source-text requested-target header]
   (let [version (c6-source-interface/interface-version! header)]
     (:artifact (p15-s23-c6c10-fresh-construction
                 (p15-s23-stage2-gravity-checked-core-context source-path source-text requested-target)
                 version)))))

(defn- p15-s23-c6c10-application-owner-sources
  []
  (into {}
        (map (fn [[owner path]]
               (let [snapshot (c2-pass-cache/source-snapshot! path)]
                 [owner {:source-path (:canonical-path snapshot)
                         :source-byte-count (:byte-count snapshot)
                         :source-content-hash (:bytes-hash snapshot)}])))
        {:reader (sh03-reader-resolve-source-path)
         :syntax (sh04-syntax-resolve-source-path)
         :expansion (sh05-macro-resolve-source-path)
         :resolution (sh06-resolution-resolve-source-path)
         :expression (p15-s23-stage2-compiler-artifact-source-path)
         :checked-core (p15-s23-c6c10-resolve-source-path)}))

(defn- p15-s23-c6c10-recheck-application-owners!
  [source-path expected-owner-sources]
  (p15-s23-c6c10-require-trusted-carrier!
   source-path :compiler-application-owner-sources expected-owner-sources)
  (let [observed (p15-s23-c6c10-application-owner-sources)]
    (when-not (= expected-owner-sources observed)
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :stable-current-compiler-application-owners
       {:expected-owner-sources expected-owner-sources :observed-owner-sources observed})))
  nil)

(defn- p15-s23-c6c10-current-scalar-construction
  "Internal source transport for the production compiler application.

   The caller supplies source and policy, never accepted Core, a plan, facts
   or verifier evidence. Existing Uniform owners construct and replay all
   semantic products; the host resolves their digest requests and transports
   exact source identities. Public historical source interfaces are unchanged."
  [source-path source-text policy]
  (p15-s23-c6c10-require-trusted-carrier! source-path :compiler-application-policy policy)
  (let [policy-keys #{:profile :target :effects :capabilities :safety}
        _ (when-not (and (map? policy) (= policy-keys (set (keys policy))))
            (p15-s23-c6c10-host-fail!
             "C6-VERIFY" source-path :explicit-compiler-application-policy {}))
        context (p15-s23-stage2-gravity-checked-core-context source-path source-text (:target policy))
        owner-sources (p15-s23-c6c10-application-owner-sources)
        ;; Version 2 selects the existing current compiler-program ABI only.
        ;; No v2 Core-family admission or interface label crosses this route.
        source-binding (p15-s23-c6c10-source-binding! source-path 2 :development-current)
        reader-path (get-in owner-sources [:reader :source-path])
        reader-binding (sh03-reader-current-binding! source-path reader-path)
        invocation {:reader-source-path reader-path
                    :authenticated-envelope-options {:source :development-current}
                    :syntax-source-selection
                    {:schema :gravity/sh04-development-syntax-selection-v1
                     :source-path (get-in owner-sources [:syntax :source-path])}
                    :macro-source-mode :development-current}
        identity-keys [:source-byte-count :source-content-hash]
        _ (when-not
           (and (= (select-keys source-binding identity-keys)
                   (select-keys (:checked-core owner-sources) identity-keys))
                (= (select-keys reader-binding identity-keys)
                   (select-keys (:reader owner-sources) identity-keys)))
            (p15-s23-c6c10-host-fail!
             "C6-VERIFY" source-path :current-application-selected-owner-bindings {}))
        products (fn []
                   (let [ingress (p15-s23-c6c10-private-ingress-products
                                  source-path source-text reader-path :compiler-application
                                  (dissoc reader-binding :plan) invocation)
                         expression (get-in ingress [:source-policy-receipt :expression-binding])]
                     (when (and expression
                                (not= (:source-content-hash expression)
                                      (get-in owner-sources [:expression :source-content-hash])))
                       (p15-s23-c6c10-host-fail!
                        "C6-VERIFY" source-path :current-application-selected-expression-binding {}))
                     ingress))
        ingress (products)
        observed-policy (select-keys (:authoritative-module ingress) policy-keys)
        _ (when-not (= (p15-s23-c6c10-canonical-identity source-path policy)
                       (p15-s23-c6c10-canonical-identity source-path observed-policy))
            (p15-s23-c6c10-host-fail!
             "C6-ORIGIN" source-path :declared-source-policy-matches-request
             {:requested-policy policy :source-policy observed-policy}))
        envelope (p15-s23-c6c10-private-builder-envelope source-path ingress source-binding)
        raw (p15-s23-c6c10-invoke-pinned-source-function!
             source-path source-binding p15-s23-c6c10-builder-function
             [envelope] :compiler-application-builder)
        sealed (p15-s23-c6c10-seal-digest-request-result! source-path raw)]
    (when (= :rejected (:status sealed))
      (p15-s23-c6c10-throw-sealed-rejection! source-path sealed))
    (let [fresh (products)
          fresh-envelope (p15-s23-c6c10-private-builder-envelope source-path fresh source-binding)
          _ (when-not
             (and (= (p15-s23-c6c10-canonical-identity source-path envelope)
                     (p15-s23-c6c10-canonical-identity source-path fresh-envelope))
                  (= (p15-s23-c6c10-canonical-identity source-path (:source-policy-receipt ingress))
                     (p15-s23-c6c10-canonical-identity source-path (:source-policy-receipt fresh))))
              (p15-s23-c6c10-host-fail!
               "C6-ORIGIN" source-path :fresh-current-source-parent-and-binding-replay {}))
          report (p15-s23-c6c10-invoke-pinned-source-function!
                  source-path source-binding p15-s23-c6c10-verifier-function
                  [fresh-envelope (:artifact-template raw) (:digest-requests raw)]
                  :compiler-application-verifier)
          _ (p15-s23-c6c10-require-trusted-carrier!
             source-path :compiler-application-core-verifier-report report)
          _ (when (= :rejected (:status report))
              (p15-s23-c6c10-throw-sealed-rejection!
               source-path (p15-s23-c6c10-seal-digest-request-result! source-path report)))
          verification (p15-s23-c6c10-validate-gravity-replay-report! source-path raw report)
          artifact (p15-s23-c6c10-public-artifact context source-binding ingress sealed)
          receipt (assoc (:source-policy-receipt ingress)
                         :policy policy :owner-sources owner-sources
                         :parameter-resolution
                         (select-keys (:resolved-parameter ingress)
                           [:sh06-artifact-id :sh06-semantic-projection-id :projection-binding
                            :root-syntax-id :function-syntax-id :function-scope-id])
                         :expression-binding
                         (or (get-in ingress [:source-policy-receipt :expression-binding])
                             (assoc (:expression owner-sources)
                                    :invoked? false :reason :parameter-seed-plan)))]
      (p15-s23-c6c10-require-trusted-carrier! source-path :compiler-application-source-policy receipt)
      (p15-s23-c6c10-recheck-application-owners! source-path owner-sources)
      {:artifact artifact :context context :source-policy-receipt receipt
       :binding-pins (:binding-pins envelope)
       :source-binding
       (assoc (select-keys source-binding
                [:source-path :source-byte-count :source-content-hash :plan-semantic-hash
                 :functions-semantic-hash :builder-semantic-hash :verifier-semantic-hash :function-shapes])
              :source-mode :development-current :semantic-authority :gravity-source
              :builder-function p15-s23-c6c10-builder-function
              :verifier-function p15-s23-c6c10-verifier-function)
       :gravity-verification
       (assoc (select-keys verification [:status :request-count :semantic-authority :checks])
              :checked-core-artifact-id (:artifact-id artifact)
              :digest-graph-proof-id (get-in sealed [:graph-proof :graph-proof-id]))})))
