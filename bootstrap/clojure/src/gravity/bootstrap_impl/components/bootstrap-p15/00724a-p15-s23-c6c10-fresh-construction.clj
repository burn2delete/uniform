(require '[gravity.checked-core.bool-branch-join :as bool-branch-join])

(defn- p15-s23-c6c10-fresh-construction
  ([context] (p15-s23-c6c10-fresh-construction context 1))
  ([context version]
  (let [context (p15-s23-c6c10-validate-public-context! context)
        source-path (:source-path context)
        source-binding (p15-s23-c6c10-source-binding! source-path version)
        reader (when (= 2 version) (p15-s23-c6c10-current-reader-program! source-path))
        expression (when (= 2 version)
                     (let [rule (c-backend-stage2-plan-emitter-source-rule! source-path :jvm)]
                       (p15-s23-stage2-compiler-artifact-binding! (:emitter rule) source-path :jvm 2)))
        ingress
        (if (= 2 version)
          (binding [*p15-s23-stage2-compiler-artifact-binding* expression]
            (p15-s23-c6c10-private-ingress-products
             source-path (:source-text context) (get-in reader [:invocation :reader-source-path]) 2
             (dissoc (:binding reader) :plan)))
          (p15-s23-c6c10-private-ingress-products source-path (:source-text context)))
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
