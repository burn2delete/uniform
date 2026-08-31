

(defn sh03-reader-raise-rejection!
  [source-path source-bytes reader-options project-context result]
  (when (= :rejected (:status result))
    (let [diagnostic (first (:diagnostics result))
          owner-id (str (:id diagnostic))
          id (case owner-id
               "L1-NS-SHAPE" "C2-NS-SHAPE"
               owner-id)
          reader-engine-diagnostic
          (:reader-engine-diagnostic diagnostic)
          reader-stage
          (if (contains? #{"STAGE1READER003"
                           "STAGE1READER004"
                           "STAGE1READER007"}
                         reader-engine-diagnostic)
            :lexical-tokenization
            :recursive-form-building)
          diagnostic-facts
          (let [facts (:facts diagnostic)
                failure-kind (or (:failure-kind facts)
                                 (:reason diagnostic)
                                 (get-in diagnostic [:reader-state :reason]))]
            (if (and (= id "C2-HASH")
                     (contains? #{:delimiter-depth-limit
                                  :reader-frame-depth-limit}
                                failure-kind))
              (assoc facts
                     :failure-kind :reader-resource-depth-limit
                     :gravity-failure-kind failure-kind)
              facts))
          _
          (case id
            "C2-EXTENSION"
            (when-not (qst-or-gravity-source? source-path)
              (try
                (source-path-policy-fail! source-path source-bytes)
                (catch clojure.lang.ExceptionInfo ex
                  (c2-reader-remap-exception! source-path ex))))

            "C2-ENCODING"
            (try
              (decode-gravity-source-bytes source-path source-bytes)
              (catch clojure.lang.ExceptionInfo ex
                (c2-reader-remap-exception! source-path ex)))

            nil)
          source-id
          (try
            (:source-id
             (c2-source-unit-record
              source-path
              (sh03-reader-strict-source-text!
               source-path source-path source-bytes)
              reader-options project-context))
            (catch clojure.lang.ExceptionInfo _
              (get-in result [:source-unit :source-id])))
          source-content-id (get-in result [:source-unit :bytes-hash])
          raw (when-let [raw-spelling (:raw-spelling diagnostic)]
                (try
                  (if (= :gravity/source-slice (:artifact raw-spelling))
                    (let [source-text
                          (sh03-reader-strict-source-text!
                           source-path source-path source-bytes)
                          scalar-boundaries
                          (sh03-reader-source-scalar-boundaries!
                           source-path source-text source-bytes)]
                      (sh03-reader-accepted-raw-text!
                       source-path source-bytes source-content-id
                       scalar-boundaries raw-spelling
                       (get-in diagnostic [:primary :span])))
                    (sh03-reader-raw-text! source-path raw-spelling))
                  (catch clojure.lang.ExceptionInfo _ nil)))]
      (let [span (sh03-reader-path-span
                  source-path source-id
                  (get-in diagnostic [:primary :span]))
            token-id (sh03-reader-legacy-id
                      (get-in diagnostic [:reader-state :token-id]))
            form-id (sh03-reader-legacy-id
                     (get-in diagnostic [:reader-state :form-id]))
            related (sh03-reader-related-records
                     source-path source-id (:related diagnostic))]
        (if (str/starts-with? id "L1-")
          (throw
           (ex-info
            (or (:message diagnostic) id)
            (-> diagnostic
                (assoc :id id
                       :rule id
                       :source-id source-id
                       :source-span span
                       :primary {:span span :artifact source-id}
                       :related related
                       :token-id token-id
                       :form-id form-id
                       :raw-spelling raw
                       :reader-options reader-options)
                (update :reader-state
                        merge {:token-id token-id :form-id form-id}))))
          (c2-reader-fail!
           id source-path
           {:source-id source-id
            :source-span span
            :token-id token-id
            :form-id form-id
            :raw raw
            :reader-options reader-options
            :facts (:facts diagnostic)}
           {:related related
            :reader-engine-diagnostic
            reader-engine-diagnostic
            :remapped-from
            (or (when (not= owner-id id) owner-id)
                (:remapped-from diagnostic))
            :reader-state
            (merge (:reader-state diagnostic)
                   {:artifact :gravity/reader-state
                    :stage reader-stage
                    :token-id token-id
                    :form-id form-id})
            :cause-message
            (or (:message diagnostic) (c2-reader-message id))
            :facts diagnostic-facts}))))))

(defn sh03-reader-sh02-descriptor
  [source-path project-context resolved summary]
  (let [binding (:plan-binding resolved)
        projection-name :reader-product-identities
        fact-name :reader-product-binding
        identity-name :reader-result
        identity-domain :gravity/sh03-reader-result-identity-v2
        evidence-id
        (p15-s23-c6c10-canonical-digest
         source-path
         {:domain :gravity/sh03-reader-envelope-evidence-v2
          :summary summary
          :plan-semantic-hash (:plan-semantic-hash binding)})
        identity-preimage {:summary summary}
        observed-id
        (p15-s23-c6c10-canonical-digest
         source-path
         {:domain identity-domain :semantic-input identity-preimage})
        fact-entries
        [{:syntax-result-id (:syntax-result-id summary)
          :syntax-stream-id (:syntax-stream-id summary)
          :serialization-id (:serialization-set-id summary)
          :graph-id (:graph-id summary)}]
        fact-value {:family fact-name :entries fact-entries}
        artifact-id
        (p15-s23-c6c10-canonical-digest
         source-path {:reader-result (:reader-result-id summary)})]
    {:artifact :gravity/private-authenticated-envelope-descriptor
     :schema-version 1
     :stage :c2-reader
     :artifact-kind :gravity/sh03-reader-products
     :source-revision
     {:owner :sh03-reader
      :source-language :gravity
      :logical-source-path sh03-reader-source-relative-path
      :source-content-hash (:source-content-hash binding)
      :source-byte-count (:source-byte-count binding)
      :plan-semantic-hash (:plan-semantic-hash binding)
      :functions-semantic-hash (:functions-semantic-hash binding)
      :builder-function sh03-reader-entrypoint
      :builder-semantic-hash (:entrypoint-semantic-hash binding)
      :function-shapes
      {sh03-reader-entrypoint {:arity 3}
       sh03-reader-verifier {:arity 4}}}
     :projection-contract
     {:contract-kind :gravity/sh03-reader-product-envelope-contract
      :contract-version 1
      :profile :meta
      :target :jvm
      :required-semantic-projections [projection-name]
      :required-fact-families [fact-name]
      :required-identity-subjects [identity-name]}
     :semantic-projections
     [{:name projection-name
       :role :complete-reader-product-identity-projection
       :entry-count (count summary)
       :value summary}]
     :fact-transitions
     [{:name fact-name
       :disposition :preserved
       :input fact-value
       :output fact-value
       :input-count (count fact-value)
       :output-count (count fact-value)
       :evidence-ids [evidence-id]}]
     :effect-capability-relation
     {:effect-facts {:declared #{} :observed #{}}
      :capability-facts {:required #{} :granted #{}}
      :capability-proof-facts {:proof-ids [evidence-id]}
      :effect-order []
      :provider-selections []
      :grant-scopes []}
     :proof-composite
     {:proof-records [{:proof-id evidence-id :status :checked}]
      :proof-certificate-table {evidence-id {:status :checked}}
      :proof-summary {:required 1 :checked 1}
      :proof-usage [{:proof-id evidence-id :used-by :reader-products}]}
     :preservation
     {:requires [fact-name]
      :preserves [fact-name]
      :invalidates []
      :regenerates []
      :residual-checks [:identity-subject-equality
                        :digest-graph-reachability]}
     :identity-subjects
     [{:name identity-name
       :domain identity-domain
       :preimage identity-preimage
       :observed-id observed-id}]
     :lineage
     [{:stage :sh03-reader
       :artifact-kind :gravity/sh03-reader-result
       :semantic-id (:reader-result-id summary)
       :artifact-id artifact-id
       :verification-id evidence-id
       :relation :produced-from-gravity-reader}]
     :reference-closure
     {:root-id "sh03-reader-result"
      :node-ids ["sh03-reader-result"]
      :edges []
      :fact-reference-ids [evidence-id]
      :origin-reference-ids []
      :proof-reference-ids [evidence-id]
      :runtime-check-reference-ids []
      :observed-node-count 1
      :observed-edge-count 0
      :observed-maximum-depth 0}
     :actual-path-provenance
     {:source-path source-path
      :workspace-root (or (:project-root-path project-context)
                          (System/getProperty "user.dir"))
      :invocation-root (System/getProperty "user.dir")}
     :bounds p15-s23-sh02-authenticated-envelope-bounds}))