

(defn p15-s23-c6c10-seal-digest-request-result!
  [source-path raw-result]
  (let [{:keys [validation resolved-digests resolved-requests root-digests]}
        (p15-s23-c6c10-resolve-digest-request-graph!
         source-path raw-result)
        request-count (:request-count validation)
        resolve-complete
        (fn [value]
          (p15-s23-c6c10-resolve-digest-references!
           source-path value request-count nil resolved-digests))
        sealed-template
        (when (some? (:artifact-template raw-result))
          (resolve-complete (:artifact-template raw-result)))
        sealed-diagnostics (resolve-complete (:diagnostics raw-result))
        resolved-envelope
        (cond-> (assoc raw-result
                       :artifact-template sealed-template
                       :diagnostics sealed-diagnostics
                       :digest-requests resolved-requests
                       :digest-graph-roots root-digests)
          (contains? raw-result :digest-graph-root)
          (assoc :digest-graph-root (first root-digests)))
        _
        (p15-s23-c6c10-validate-builder-result-canonical-carrier!
         source-path resolved-envelope)
        residual-refs
        (p15-s23-c6c10-collect-digest-ref-ordinals!
         source-path resolved-envelope request-count nil)]
    (when-not (empty? residual-refs)
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :fully-resolved-digest-reference-closure
       {:residual-reference-count (count residual-refs)}))
    (if (= :accepted (:status raw-result))
      (when-not (and (= 1 (count root-digests))
                     (= (first root-digests)
                        (:artifact-id sealed-template)))
        (p15-s23-c6c10-host-fail!
         "C6-VERIFY" source-path :sealed-artifact-id-equals-root-digest
         {:root-digests root-digests
          :artifact-id (:artifact-id sealed-template)}))
      (when (seq root-digests)
        (when-not (= root-digests
                     (mapv :diagnostic-id sealed-diagnostics))
          (p15-s23-c6c10-host-fail!
           "C6-VERIFY" source-path
           :sealed-diagnostic-ids-equal-root-digests
           {:root-digests root-digests
            :diagnostic-ids (mapv :diagnostic-id sealed-diagnostics)}))))
    (let [request-chunk-digests
          (mapv
           (fn [chunk-index chunk]
             (p15-s23-c6c10-canonical-digest
              source-path
              {:domain :gravity/c6-c10-resolved-request-chunk-v1
               :chunk-index chunk-index
               :requests (vec chunk)}))
           (range)
           (partition-all 128 resolved-requests))
          graph-proof-base
          {:domain :gravity/c6-c10-sealed-digest-request-graph-v1
           :algorithm :sha256
           :encoding :gravity/canonical-edn-v1
           :request-count request-count
           :root-digests root-digests
           :request-chunk-size 128
           :request-chunk-digests request-chunk-digests}
          graph-proof-id
          (p15-s23-c6c10-canonical-digest
           source-path graph-proof-base)]
      {:kind :gravity/p15-s23-c6-c10-sealed-builder-result
       :status (:status raw-result)
       :sealed-artifact-template sealed-template
       :sealed-diagnostics sealed-diagnostics
       :root-digests root-digests
       :resolved-digests resolved-digests
       :resolved-requests resolved-requests
       :graph-proof (assoc graph-proof-base :graph-proof-id graph-proof-id)
       :authority (:authority raw-result)
       :containment (:containment raw-result)
       :propagated-upstream? (true? (:propagated-upstream? raw-result))})))

(defn p15-s23-c6c10-binding-pins
  [source-path ingress source-binding]
  (let [front-end (:front-end-products ingress)
        integrity (:reader-product-integrity front-end)
        c2-semantic-hash
        (p15-s23-c6c10-canonical-digest
         source-path
         {:domain :gravity/c6-c10-authenticated-c2-pin-v1
          :source-content-hash (:source-content-hash ingress)
          :source-unit-id (:source-unit-id front-end)
          :incremental-reader-hashes
          (:incremental-reader-hashes front-end)
          :reader-product-integrity integrity})
        reader-integrity-hash
        (p15-s23-c6c10-canonical-digest
         source-path
         {:domain :gravity/c6-c10-reader-integrity-pin-v1
          :c2-semantic-hash c2-semantic-hash
          :reader-product-integrity integrity})
        c3-semantic-hash
        (p15-s23-c6c10-canonical-digest
         source-path
         {:domain :gravity/c6-c10-authenticated-c3-pin-v2
          :c2-semantic-hash c2-semantic-hash
          :c3-syntax-ids
          (mapv :syntax/id (:c3-syntax-object-stream front-end))
          :c3-capability-proof (:c3-capability-proof front-end)})
        plan-semantic-hash
        (p15-s23-c6c10-canonical-digest
         source-path
         {:domain :gravity/c6-c10-authenticated-stage2-plan-pin-v1
          :c3-semantic-hash c3-semantic-hash
          :plan-id (get-in ingress [:stage2-plan :plan-id])})]
    {:c2-semantic-hash c2-semantic-hash
     :c3-semantic-hash c3-semantic-hash
     :plan-semantic-hash plan-semantic-hash
     :reader-integrity-hash reader-integrity-hash
     :builder-source-hash (:source-content-hash source-binding)
     :builder-plan-hash (:plan-semantic-hash source-binding)
     :builder-function-hash (:builder-semantic-hash source-binding)}))

(defn p15-s23-c6c10-private-builder-envelope
  [source-path ingress source-binding]
  (let [envelope
        (assoc
         (select-keys
          ingress
          [:source-content-hash :front-end-products :stage2-plan
           :authoritative-module :carrier-validation])
         :kind :gravity/private-fresh-c2-c3-stage2-plan-envelope
         :scope :bounded-pure-hosted-safe-jvm-source
         :binding-pins
         (p15-s23-c6c10-binding-pins
          source-path ingress source-binding))]
    envelope))

(defn p15-s23-c6c10-invoke-pinned-source-function!
  [source-path source-binding function-name arguments boundary]
  (try
    (p15-s23-stage2-runtime-execute-function
     {:engine :gravity-c6-c10-pinned-source-host-runner
      :compiler-artifact-plan? true}
     (:plan source-binding)
     function-name
     arguments)
    (catch StackOverflowError error
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :contained-gravity-source-host-stack
       {:boundary boundary
        :contained-host-error (.getName (class error))}))
    (catch clojure.lang.ExceptionInfo exception
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :contained-gravity-source-diagnostic
       {:boundary boundary
        :cause-diagnostic (:id (ex-data exception))}))
    (catch InterruptedException error
      (.interrupt (Thread/currentThread))
      (throw error))
    (catch Exception error
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :contained-gravity-source-host-failure
       {:boundary boundary
        :contained-host-error (.getName (class error))}))))

(defn p15-s23-c6c10-gravity-replay-verification!
  [source-path source-binding envelope raw-result]
  (let [report
        (p15-s23-c6c10-invoke-pinned-source-function!
         source-path source-binding p15-s23-c6c10-verifier-function
         [envelope (:artifact-template raw-result)
          (:digest-requests raw-result)]
         :gravity-source-verifier)
        expected-keys
        #{:artifact-template :checks :diagnostics :digest-graph-root
          :digest-graph-roots :request-count :semantic-authority :status}]
    (p15-s23-c6c10-canonical-record source-path report)
    (when-not
     (and (map? report)
          (= expected-keys (set (keys report)))
          (= :passed (:status report))
          (= :gravity-source (:semantic-authority report))
          (= (count (:digest-requests raw-result))
             (:request-count report))
          (vector? (:diagnostics report))
          (empty? (:diagnostics report))
          (vector? (:checks report))
          (= [:fresh-template-replay
              :exact-digest-request-replay
              :bounded-pure-c6-c10-scope]
             (:checks report))
          (= (p15-s23-c6c10-canonical-identity
              source-path (:artifact-template raw-result))
             (p15-s23-c6c10-canonical-identity
              source-path (:artifact-template report)))
          (= (p15-s23-c6c10-canonical-identity
              source-path (:digest-graph-roots raw-result))
             (p15-s23-c6c10-canonical-identity
              source-path (:digest-graph-roots report)))
          (= (p15-s23-c6c10-canonical-identity
              source-path (:digest-graph-root raw-result))
             (p15-s23-c6c10-canonical-identity
              source-path (:digest-graph-root report))))
     (p15-s23-c6c10-host-fail!
      "C6-VERIFY" source-path :fresh-gravity-source-verifier-replay
      {:status (:status report)
       :request-count (:request-count report)}))
    report))

(def p15-s23-c6c10-public-context-keys
  #{:kind :source-path :source-text :source-content-hash :requested-target})

(def p15-s23-c6c10-physical-artifact-keys
  #{:target-request-metadata :physical-provenance})

(defn p15-s23-c6c10-require-trusted-carrier!
  [source-path carrier value]
  (let [validation
        (p15-s23-trusted-carrier-validation
         value :reject p15-s23-c6c10-max-carrier-nodes
         p15-s23-c6c10-max-carrier-depth
         p15-s23-c6c10-max-container-width)]
    (when-not (= :passed (:status validation))
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :trusted-gravity-checked-core-carrier
       (assoc validation :carrier carrier)))
    validation))