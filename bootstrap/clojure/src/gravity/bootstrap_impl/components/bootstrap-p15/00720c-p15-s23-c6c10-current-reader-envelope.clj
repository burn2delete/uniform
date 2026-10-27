(declare p15-s23-stage2-sh02-descriptor-envelope-verify!
         p15-s23-c6c10-require-trusted-carrier!)

(defn- p15-s23-c6c10-current-reader-envelope-state! [source-path raw-front-end reader-path]
  ;; This is called only on freshly constructed C2/C3 owner products, never
  ;; supplied Reader data, a cached result, or a caller verification report.
  (let [c2 (:c2-reader-artifact raw-front-end)
        boundary (:gravity-reader-boundary c2)
        binding (:plan-binding boundary)
        descriptor (:authenticated-envelope-descriptor boundary)
        envelope (:authenticated-envelope boundary)
        result (:resolved-reader-result boundary)
        expected (:binding c6-source-contract/current-reader-program)
        policy {:source :development-current}]
    (p15-s23-c6c10-require-trusted-carrier! source-path :current-reader-envelope-witness
      {:binding binding :descriptor descriptor :envelope envelope :result result})
    (when-not (and (= expected (select-keys binding (keys expected)))
                   (= reader-path (p15-s23-c6c10-canonical-file-path (:source-path binding)))
                   (= :gravity/sh03-reader-result (:artifact result))
                   (= 1 (:schema-version result)) (= :accepted (:status result))
                   (= :c2-reader (:stage envelope)) (= :accepted (:status envelope))
                   (= :gravity-source (:semantic-authority envelope)))
      (p15-s23-c6c10-host-fail! "C6-VERIFY" source-path :actual-selected-current-reader-products {}))
    (when-not (= :passed (p15-s23-stage2-sh02-descriptor-envelope-verify!
                          envelope :c2-reader :gravity/sh03-reader-products descriptor source-path policy))
      (p15-s23-c6c10-host-fail! "C6-VERIFY" source-path :actual-current-reader-envelope-verification {}))
    (let [rule (:source-rule envelope) state (:admission-status rule)]
      (when-not (= :development-unadmitted state)
        (p15-s23-c6c10-host-fail! "C6-VERIFY" source-path :actual-current-reader-envelope-admission-state {}))
      {:source-interface {:interface :gravity/c6-c10 :version 2}
       :admission-status state :verification-scope :selected-current-source-host-seed
       :reader-plan-binding binding
       :reader-source-revision (:source-revision descriptor)
       :reader-result (select-keys result [:artifact :schema-version :status :actual-path-provenance
                                          :incremental-reader-hashes :execution-boundary])
       :authenticated-envelope
       (select-keys envelope [:artifact :schema-version :status :stage :semantic-envelope-id
                              :provenance-binding-id :source-rule :gravity-template-replay])
       :envelope-verification :passed :self-hosted? false})))
