

(defn sh06-resolution-require-carrier!
  [source-path carrier value]
  (let [validation
        (p15-s23-trusted-carrier-validation
         value :default-only
         (:maximum-carrier-nodes sh06-resolution-transport-bounds)
         (:maximum-carrier-depth sh06-resolution-transport-bounds)
         (:maximum-container-width sh06-resolution-transport-bounds))]
    (when-not (= :passed (:status validation))
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED" source-path :bounded-sh06-resolution-carrier
       (select-keys
        validation
        [:reason :class :observed-nodes :observed-depth
         :maximum-nodes :maximum-depth :maximum-width])
       {:carrier carrier
        :transport-bounds sh06-resolution-transport-bounds}))
    validation))

(defn sh06-resolution-digest-reference
  [value]
  (when (and (map? value)
             (= #{:digest-ref} (set (keys value)))
             (integer? (:digest-ref value))
             (not (neg? (:digest-ref value))))
    (:digest-ref value)))

(defn sh06-resolution-resolve-declared-id!
  [source-path value resolved-digests slot]
  (let [ordinal (sh06-resolution-digest-reference value)
        resolved (when (some? ordinal) (get resolved-digests ordinal))]
    (when-not (and (some? ordinal)
                   (p15-s23-sh02-sha256-id? resolved))
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED" source-path slot value
       {:resolved-digest resolved :digest-ordinal ordinal}))
    resolved))

(defn sh06-resolution-resolve-record-binding-id!
  [source-path record resolved-digests slot]
  (assoc record :binding-id
         (sh06-resolution-resolve-declared-id!
          source-path (:binding-id record) resolved-digests slot)))

(defn sh06-resolution-resolve-analysis!
  ([source-path raw-analysis resolved-digests]
   (sh06-resolution-resolve-analysis!
    source-path raw-analysis resolved-digests false))
  ([source-path raw-analysis resolved-digests allow-pending-artifact-id?]
   (let [bindings
        (mapv
         #(sh06-resolution-resolve-record-binding-id!
           source-path % resolved-digests :binding-table-binding-id)
         (:binding-table raw-analysis))
        resolutions
        (mapv
         #(sh06-resolution-resolve-record-binding-id!
           source-path % resolved-digests :resolution-table-binding-id)
         (:resolution-table raw-analysis))
        binding-identities
        (mapv
         #(sh06-resolution-resolve-declared-id!
           source-path % resolved-digests
           :identity-preimage-binding-identity)
         (get-in raw-analysis [:identity-preimage :binding-identities]))
        invalidation-binding-ids
        (mapv
         #(sh06-resolution-resolve-declared-id!
           source-path % resolved-digests
           :incremental-invalidation-binding-id)
         (get-in raw-analysis
                 [:incremental-invalidation-inputs :binding-ids]))
        identity-preimage
        (-> (:identity-preimage raw-analysis)
            (assoc :binding-identities binding-identities)
            (assoc :resolutions
                   (mapv #(dissoc % :source-span) resolutions)))
        artifact-ordinal
        (sh06-resolution-digest-reference (:artifact-id raw-analysis))
        artifact-id
        (if (and allow-pending-artifact-id?
                 (= artifact-ordinal (count resolved-digests)))
          (:artifact-id raw-analysis)
          (sh06-resolution-resolve-declared-id!
           source-path (:artifact-id raw-analysis) resolved-digests
           :namespace-analysis-artifact-id))]
    (-> raw-analysis
        (assoc :artifact-id artifact-id)
        (assoc :binding-table bindings)
        (assoc :resolution-table resolutions)
        (assoc :identity-preimage identity-preimage)
        (assoc-in [:incremental-invalidation-inputs :binding-ids]
                  invalidation-binding-ids)))))

(defn sh06-resolution-resolve-digest-requests!
  [source-path raw-analysis digest-requests]
  (when-not (and (vector? digest-requests)
                 (<= (count digest-requests) 8192)
                 (seq digest-requests))
    (sh06-resolution-boundary-fail!
     "C5-UNRESOLVED" source-path :bounded-resolution-digest-requests
     digest-requests {:maximum-digest-requests 8192}))
  (let [binding-count (count (:binding-table raw-analysis))]
    (when-not (= (inc binding-count) (count digest-requests))
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED" source-path :complete-resolution-digest-requests
       digest-requests {:binding-count binding-count}))
    (loop [remaining digest-requests resolved []]
      (if (empty? remaining)
        resolved
        (let [request (first remaining)
              ordinal (:ordinal request)
              purpose (:purpose request)
              preimage
              (if (= purpose :sh06-namespace-analysis-id)
                (:identity-preimage
                 (sh06-resolution-resolve-analysis!
                  source-path raw-analysis resolved true))
                (:preimage request))]
          (when-not (= ordinal (count resolved))
            (sh06-resolution-boundary-fail!
             "C5-UNRESOLVED" source-path
             :ordered-resolution-digest-requests request
             {:resolved-count (count resolved)}))
          (recur
           (rest remaining)
           (conj resolved
                 (reader-canonical-hash
                  {:domain :gravity/sh06-declared-digest-v1
                   :purpose purpose :preimage preimage}))))))))

(defn sh06-resolution-raise-rejection!
  [source-path template]
  (let [diagnostic (first (:diagnostics template))
        requests (:digest-requests template)
        request (first requests)
        diagnostic-id
        (reader-canonical-hash
         {:domain :gravity/sh06-declared-digest-v1
          :purpose (:purpose request)
          :preimage (:preimage request)})
        rule (:rule diagnostic)]
    (when-not (and (= :rejected (:status template))
                   (= 1 (count (:diagnostics template)))
                   (= 1 (count requests))
                   (contains? (set c5-resolution-diagnostic-ids) rule)
                   (= 0 (:ordinal request))
                   (= :sh06-resolution-diagnostic-id (:purpose request))
                   (= {:digest-ref 0}
                      (:diagnostic-id-request diagnostic))
                   (p15-s23-sh02-sha256-id? diagnostic-id))
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED" source-path
       :exact-gravity-resolution-rejection template {}))
    (c5-resolution-fail!
     rule source-path
     (assoc diagnostic
            :source-span
            (assoc (or (:source-span diagnostic)
                       (source-span source-path 0))
                   :source source-path))
     {:severity :error
      :profile (:profile diagnostic)
      :diagnostic-id diagnostic-id
      :facts (:facts diagnostic)
      :remediation (:remediation diagnostic)})))

(defn sh06-resolution-run-request!
  [source-path binding request]
  (sh06-resolution-require-carrier!
   source-path :authenticated-sh06-resolution-request request)
  (let [template
        (sh06-resolution-execute!
         source-path binding 'sh06-build-resolution-template [request])]
    (sh06-resolution-require-carrier!
     source-path :gravity-resolution-template template)
    (case (:status template)
      :rejected (sh06-resolution-raise-rejection! source-path template)
      :accepted
      (let [raw-analysis (:namespace-analysis-template template)
            digest-requests (:digest-requests template)
            template-verification
            (sh06-resolution-execute!
             source-path binding 'sh06-verify-resolution-template
             [raw-analysis digest-requests])
            _ (when-not (= :passed (:status template-verification))
                (sh06-resolution-boundary-fail!
                 "C5-UNRESOLVED" source-path
                 :fresh-gravity-resolution-template-verification
                 template-verification {}))
            resolved-digests
            (sh06-resolution-resolve-digest-requests!
             source-path raw-analysis digest-requests)
            resolved-analysis
            (sh06-resolution-resolve-analysis!
             source-path raw-analysis resolved-digests)
            resolved-verification
            (sh06-resolution-execute!
             source-path binding 'sh06-verify-resolution-resolved
             [resolved-analysis digest-requests resolved-digests])]
        (sh06-resolution-require-carrier!
         source-path :resolved-gravity-resolution-analysis
         resolved-analysis)
        (when-not (= :passed (:status resolved-verification))
          (sh06-resolution-boundary-fail!
           "C5-UNRESOLVED" source-path
           :fresh-gravity-resolution-resolved-verification
           resolved-verification {}))
        {:raw-template-result template
         :raw-analysis raw-analysis
         :resolved-analysis resolved-analysis
         :digest-requests digest-requests
         :resolved-digests resolved-digests
         :template-verification template-verification
         :resolved-verification resolved-verification})
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED" source-path :gravity-resolution-result-status
       template {}))))

(defn sh06-resolution-order-compatibility
  [order]
  (case order
    :local-lexical-binding :local
    :current-namespace-binding :namespace
    :alias-qualified-required-binding :alias-qualified
    :fully-qualified-namespace-binding :fully-qualified
    :profile-allowed-core-binding :core-auto-import
    :explicit-foreign-import-binding :foreign
    order))