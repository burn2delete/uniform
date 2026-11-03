(require '[gravity.checked-core.bool-branch-join :as bool-branch-join]
         '[gravity.c6-c10.source-interface :as c6-source-interface])

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
                        (when (= 2 version) :development-current))
        reader (when (= 2 version) (p15-s23-c6c10-current-reader-program! source-path))
        _ (when (= 2 version)
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
        expression (when (= 2 version)
                     (p15-s23-stage2-current-expression-development-binding!
                      source-path))
        ingress
        (if (= 2 version)
          (binding [*p15-s23-stage2-compiler-artifact-binding* expression]
            (p15-s23-c6c10-private-ingress-products
             source-path (:source-text context) (get-in reader [:invocation :reader-source-path]) 2
             (dissoc (:binding reader) :plan) (:invocation reader)))
          (p15-s23-c6c10-private-ingress-products source-path (:source-text context)))
        current-source-before-builder
        (when (= 2 version)
          (p15-s23-c6c10-read-current-source-snapshot! source-path))
        _ (when (= 2 version)
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
          (when (= 2 version)
            (p15-s23-c6c10-read-current-source-snapshot! source-path))
          _
          (when (= 2 version)
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
        (= 2 version)
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
