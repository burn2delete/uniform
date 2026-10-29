(declare p15-s23-stage2-sh02-descriptor-envelope-verify!
         p15-s23-c6c10-require-trusted-carrier!)

(defn- p15-s23-c6c10-current-reader-envelope-state!
  [source-path raw-front-end reader-path selected-binding]
  ;; C3 deliberately projects its embedded C2 view without the raw Reader
  ;; boundary. Its authenticated provenance retains the actual C2 envelope and
  ;; descriptor. The selected binding comes from this fresh owner's plan call.
  (let [field #(p15-s23-c6c10-reader-owner-field! source-path %1 %2)
        c2 (field raw-front-end :c2-reader-artifact)
        c3 (field raw-front-end :authenticated-c3-source-artifact)
        boundary (field c3 :gravity-syntax-boundary)
        provenance (field boundary :reader-authentication-provenance)
        authentication (field c2 :sh03-reader-authentication)
        integrity (field c2 :reader-product-integrity)
        source-unit (field c2 :source-unit-record)
        c2-artifact-id (field c2 :artifact-id)
        witness {:binding selected-binding :provenance provenance :authentication authentication
                 :c2-integrity integrity :source-unit-record source-unit :c2-artifact-id c2-artifact-id}]
    (p15-s23-c6c10-require-reader-owner-metadata! source-path witness)
    (let [descriptor (:actual-sh03-envelope-descriptor provenance)
          envelope (:actual-sh03-authenticated-envelope provenance)
          revision (:source-revision descriptor)
          witness (assoc witness :descriptor descriptor :envelope envelope)
          expected (:binding c6-source-contract/current-reader-program)
          identity-keys [:source-content-hash :source-byte-count :plan-semantic-hash :functions-semantic-hash]
          policy {:source :development-current}]
    (let [checks
          {:selected-program (= expected (select-keys selected-binding (keys expected)))
           :selected-path (and (string? (:source-path selected-binding))
                               (= reader-path (p15-s23-c6c10-canonical-file-path (:source-path selected-binding))))
           :source-revision (= (select-keys selected-binding identity-keys) (select-keys revision identity-keys))
           :builder (= (:entrypoint-semantic-hash selected-binding) (:builder-semantic-hash revision))
           :verifier (= (:verifier-semantic-hash selected-binding)
                        (get-in revision [:function-shapes 'sh03-verify-reader-result :semantic-hash]))
           :retained-revision (= revision (:reader-source-revision authentication))
           :c2-artifact (= c2-artifact-id (:actual-c2-artifact-id provenance))
           :c2-integrity (= (:integrity-hash integrity)
                            (:actual-reader-product-integrity-hash provenance))
           :source-id (= (:source-id source-unit) (:actual-reader-source-id provenance))
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
      (let [receipt (merge (p15-s23-c6c10-project-reader-owner-metadata source-path witness)
                     {:source-interface {:interface :gravity/c6-c10 :version 2}
                      :admission-status state :verification-scope :selected-current-source-host-seed
                      :envelope-verification :passed :self-hosted? false})]
        ;; Only the fixed projection crosses into the unchanged strict Core
        ;; carrier. The full original envelope was verified above, unmodified.
        (p15-s23-c6c10-require-trusted-carrier! source-path :current-reader-policy-receipt receipt)
        receipt)))))
