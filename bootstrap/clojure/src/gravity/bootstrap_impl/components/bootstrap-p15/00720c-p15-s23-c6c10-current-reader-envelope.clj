(declare p15-s23-stage2-sh02-descriptor-envelope-verify!
         p15-s23-c6c10-require-trusted-carrier!)

(defn- p15-s23-c6c10-current-reader-envelope-state!
  [source-path raw-front-end reader-path selected-binding]
  ;; C3 deliberately projects its embedded C2 view without the raw Reader
  ;; boundary. Its authenticated provenance retains the actual C2 envelope and
  ;; descriptor. The selected binding comes from this fresh owner's plan call.
  (let [c2 (:c2-reader-artifact raw-front-end)
        provenance (get-in raw-front-end [:authenticated-c3-source-artifact
                                         :gravity-syntax-boundary :reader-authentication-provenance])
        descriptor (:actual-sh03-envelope-descriptor provenance)
        envelope (:actual-sh03-authenticated-envelope provenance)
        revision (:source-revision descriptor)
        authentication (:sh03-reader-authentication c2)
        expected (:binding c6-source-contract/current-reader-program)
        identity-keys [:source-content-hash :source-byte-count :plan-semantic-hash :functions-semantic-hash]
        policy {:source :development-current}]
    (p15-s23-c6c10-require-trusted-carrier! source-path :current-reader-envelope-witness
      {:binding selected-binding :descriptor descriptor :envelope envelope :provenance provenance})
    (let [checks
          {:selected-program (= expected (select-keys selected-binding (keys expected)))
           :selected-path (and (string? (:source-path selected-binding))
                               (= reader-path (p15-s23-c6c10-canonical-file-path (:source-path selected-binding))))
           :source-revision (= (select-keys selected-binding identity-keys) (select-keys revision identity-keys))
           :builder (= (:entrypoint-semantic-hash selected-binding) (:builder-semantic-hash revision))
           :verifier (= (:verifier-semantic-hash selected-binding)
                        (get-in revision [:function-shapes 'sh03-verify-reader-result :semantic-hash]))
           :retained-revision (= revision (:reader-source-revision authentication))
           :c2-artifact (= (:artifact-id c2) (:actual-c2-artifact-id provenance))
           :c2-integrity (= (get-in c2 [:reader-product-integrity :integrity-hash])
                            (:actual-reader-product-integrity-hash provenance))
           :source-id (= (get-in c2 [:source-unit-record :source-id]) (:actual-reader-source-id provenance))
           :result-id (and (string? (:reader-result-id authentication))
                           (= (:reader-result-id authentication) (:actual-sh03-reader-result-id provenance)))
           :envelope-id (and (string? (:semantic-envelope-id envelope))
                             (= (:semantic-envelope-id envelope) (:semantic-envelope-id authentication)
                                (:actual-sh03-semantic-envelope-id provenance)))
           :provenance-id (and (string? (:provenance-binding-id envelope))
                               (= (:provenance-binding-id envelope) (:provenance-binding-id authentication)
                                  (:actual-sh03-provenance-binding-id provenance)))
           :accepted-envelope (and (= :c2-reader (:stage envelope)) (= :accepted (:status envelope))
                                   (= :gravity-source (:semantic-authority envelope)))}]
      (when-not (every? true? (vals checks))
        (p15-s23-c6c10-host-fail! "C6-VERIFY" source-path :actual-selected-current-reader-products {:checks checks})))
    (when-not (= :passed (p15-s23-stage2-sh02-descriptor-envelope-verify!
                          envelope :c2-reader :gravity/sh03-reader-products descriptor source-path policy))
      (p15-s23-c6c10-host-fail! "C6-VERIFY" source-path :actual-current-reader-envelope-verification {}))
    (let [state (get-in envelope [:source-rule :admission-status])]
      (when-not (= :development-unadmitted state)
        (p15-s23-c6c10-host-fail! "C6-VERIFY" source-path :actual-current-reader-envelope-admission-state {}))
      {:source-interface {:interface :gravity/c6-c10 :version 2}
       :admission-status state :verification-scope :selected-current-source-host-seed
       :reader-plan-binding selected-binding :reader-source-revision revision
       ;; These are actual retained identities, not a fabricated full Reader
       ;; result or accepted status for a result that C3 intentionally omits.
       :reader-result-identity
       (select-keys provenance [:actual-sh03-reader-result-id :actual-sh03-semantic-product-binding
                                :actual-c2-artifact-id :actual-reader-product-integrity-hash :actual-reader-source-id])
       :authenticated-envelope
       (select-keys envelope [:artifact :schema-version :status :stage :semantic-envelope-id
                              :provenance-binding-id :source-rule :gravity-template-replay])
       :envelope-verification :passed :self-hosted? false})))
