

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
    (cond-> {:c2-semantic-hash c2-semantic-hash
     :c3-semantic-hash c3-semantic-hash
     :plan-semantic-hash plan-semantic-hash
     :reader-integrity-hash reader-integrity-hash
     :builder-source-hash (:source-content-hash source-binding)
     :builder-plan-hash (:plan-semantic-hash source-binding)
     :builder-function-hash (:builder-semantic-hash source-binding)}
      (contains? ingress :resolved-parameter)
      (assoc :parameter-resolution-hash
             (p15-s23-c6c10-canonical-digest source-path (:resolved-parameter ingress))))))

(defn p15-s23-c6c10-private-builder-envelope
  [source-path ingress source-binding]
  (let [envelope
        (assoc
         (select-keys
          ingress
          [:source-content-hash :front-end-products :stage2-plan
           :authoritative-module :carrier-validation :resolved-parameter])
         :kind :gravity/private-fresh-c2-c3-stage2-plan-envelope
         :scope
         (let [plan (:stage2-plan ingress) functions (:functions plan)
               instructions (get-in functions ['main :instructions])]
           (if (and (= 1 (count functions)) (= 1 (count instructions))
                    (= :builtin-call (:op (first instructions)))
                    (contains? '#{+ - *} (:function (first instructions))))
             (if (= '* (:function (first instructions))) :bounded-checked-i64-mul-hosted-safe-jvm-source
               (if (= '- (:function (first instructions)))
                 :bounded-checked-i64-sub-hosted-safe-jvm-source :bounded-checked-i64-add-hosted-safe-jvm-source))
             :bounded-pure-hosted-safe-jvm-source))
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
         value :reject)]
    (when-not (= :passed (:status validation))
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" source-path :trusted-gravity-checked-core-carrier
       (assoc validation :carrier carrier)))
    validation))


;; Selected-source Reader policy helpers; DATA projection is not admission.

(declare sh03-reader-resolve-source-path sh03-reader-current-binding!)

(defn- p15-s23-c6c10-validate-current-reader-program!
  [request-source expected-path binding emitter-rule]
  ;; Only the existing Reader owner constructs this binding. This helper is
  ;; not a public acceptor of supplied plans, Reader products or reports.
  (let [contract c6-source-contract/current-reader-program
        expected (:binding contract)
        plan (:plan binding)
        exports (into {} (map (fn [[name _]]
                               [name (select-keys (get-in plan [:functions name]) [:arity :params])]))
                      (:exports contract))
        rule-hash (str "sha256:"
                       (sha256-hex (pr-str (c-backend-canonical-value (:emitter emitter-rule)))))
        rule-record (p15-s23-stage2-plan-emitter-rule-record (:emitter emitter-rule))]
    (when-not (= expected (select-keys binding (keys expected)))
      (p15-s23-c6c10-host-fail! "C6-VERIFY" request-source
                               :selected-current-reader-program-identities {}))
    (when-not (= expected-path (p15-s23-c6c10-canonical-file-path (:source-path binding)))
      (p15-s23-c6c10-host-fail! "C6-VERIFY" request-source
                               :selected-current-reader-source-path {}))
    (when-not (= (:exports contract) exports)
      (p15-s23-c6c10-host-fail! "C6-VERIFY" request-source
                               :selected-current-reader-export-shapes {}))
    (when-not (and (= (get-in contract [:plan-emitter :source-content-hash])
                      p15-s23-stage2-compiler-expected-source-content-hash)
                   (= (get-in contract [:plan-emitter :source-byte-count])
                      p15-s23-stage2-compiler-expected-source-byte-count)
                   (= :complete (:status rule-record))
                   (= rule-record (:rule-record emitter-rule))
                   (= (get-in contract [:plan-emitter :source-rule-hash])
                      rule-hash (:source-rule-hash emitter-rule)
                      (get-in plan [:compiler :emitter-source-rule-hash]))
                   (= (:declared-expression-labels contract)
                      (select-keys (:compiler plan)
                                   (keys (:declared-expression-labels contract)))))
      (p15-s23-c6c10-host-fail! "C6-VERIFY" request-source
                               :selected-current-reader-plan-emitter-join {}))
    binding))

(defn p15-s23-c6c10-current-reader-program!
  ([request-source] (p15-s23-c6c10-current-reader-program! request-source 2))
  ([request-source version]
  ;; Authenticate actual C6/compiler source inputs before and after the actual
  ;; selected-source Reader owner. No caller-selected path, pins or binding.
  (p15-s23-c6c10-source-program-contract version)
  (when-not (contains? #{2 3} version)
    (p15-s23-c6c10-host-fail! "C6-VERIFY" request-source
                             :selected-current-reader-source-contract {}))
  (let [before (p15-s23-c6c10-authenticated-source-binding-inputs! request-source version)
        path (p15-s23-c6c10-canonical-file-path (sh03-reader-resolve-source-path))
        binding (sh03-reader-current-binding! request-source path)
        after (p15-s23-c6c10-authenticated-source-binding-inputs! request-source version)
        emitter-rule (:emitter-rule after)
        source-before (dissoc (:source-snapshot before) :source-text)
        source-after (dissoc (:source-snapshot after) :source-text)]
    (when-not (and (= source-before source-after)
                   (= (select-keys (:emitter-rule before) [:source-path :source-rule-hash])
                      (select-keys emitter-rule [:source-path :source-rule-hash])))
      (p15-s23-c6c10-host-fail! "C6-VERIFY" request-source
                               :stable-current-reader-dependency-inputs {}))
    (p15-s23-c6c10-validate-current-reader-program! request-source path binding emitter-rule)
    {:binding binding
     :invocation {:reader-source-path path
                  :authenticated-envelope-options {:source :development-current}}
     :verification-scope :selected-source-reader-plan
     ;; Actual envelope state must be obtained from fresh C2 products. A plan
     ;; match supplies neither a fabricated state nor frozen admission.
     :reader-result-executed? false :envelope-state-observed? false})))

(declare p15-s23-c11-mir-digest)

(defn- p15-s23-c6c10-reader-owner-field!
  [source-path value key]
  ;; Parent product records have comparator-free native keyword headers. Do
  ;; not traverse unconsumed source forms just to obtain their metadata view.
  (let [pairs
        (try (source-interface-header/pairs! value nil)
             (catch clojure.lang.ExceptionInfo error
               (if (= :versioned-c11-interface (:missing-fact (ex-data error)))
                 (p15-s23-c6c10-host-fail! "C6-VERIFY" source-path :reader-owner-product-header {})
                 (throw error))))
        keys (mapv first pairs)]
    (when-not (and (every? #(identical? clojure.lang.Keyword (class %)) keys)
                   (= (count keys) (count (set keys))))
      (p15-s23-c6c10-host-fail! "C6-VERIFY" source-path :reader-owner-product-header {}))
    (second (first (filter #(identical? key (first %)) pairs)))))

(def p15-s23-c6c10-reader-binding-projection-keys
  [:artifact :status :semantic-authority :compiled-by :executed-by
   :generic-bridge-residual? :self-hosted? :source-path :source-byte-count
   :source-content-hash :plan-semantic-hash :functions-semantic-hash
   :function-count :function-names-hash :function-shapes-hash
   :entrypoint-semantic-hash :verifier-semantic-hash :builtin-functions-hash
   :observed-instruction-nodes :observed-instruction-depth])
(def p15-s23-c6c10-reader-revision-projection-keys
  [:owner :source-language :logical-source-path :source-content-hash
   :source-byte-count :plan-semantic-hash :functions-semantic-hash
   :builder-function :builder-semantic-hash])
(def p15-s23-c6c10-reader-rule-projection-keys
  [:artifact :owner :source-content-hash :source-byte-count :plan-semantic-hash
   :functions-semantic-hash :builder-function :builder-semantic-hash
   :verifier-function :verifier-semantic-hash :semantic-authority
   :compiled-by :executed-by :self-hosted? :admission-status])
(def p15-s23-c6c10-reader-product-binding-projection-keys
  [:adapter-contract :adapted-source-unit-id :adapted-token-stream-id
   :adapted-form-tree-id :adapted-extension-invocation-set-id])

(defn- p15-s23-c6c10-require-reader-owner-metadata!
  [source-path witness]
  ;; SH02 owner metadata permits only the native default comparator. This is
  ;; not the strict Core carrier domain, which continues to reject TreeMaps.
  ;; Preflight precedes every lookup, equality, traversal and digest below.
  (let [validation (p15-s23-trusted-carrier-validation witness :default-only)]
    (when-not (= :passed (:status validation))
      (p15-s23-c6c10-host-fail! "C6-VERIFY" source-path
       :trusted-current-reader-owner-metadata
       (select-keys validation [:reason :class]))))
  witness)

(defn- p15-s23-c6c10-project-reader-owner-metadata
  [source-path witness]
  (p15-s23-c6c10-require-reader-owner-metadata! source-path witness)
  (let [binding (:binding witness) descriptor (:descriptor witness)
        revision (:source-revision descriptor) envelope (:envelope witness)
        rule (:source-rule envelope) replay (:gravity-template-replay envelope)
        provenance (:provenance witness)
        owner-id (fn [branch value]
                   (clojure.core/binding [*print-length* nil *print-level* nil *print-meta* false
                             *print-dup* false *print-readably* true *print-namespace-maps* false]
                     (p15-s23-c11-mir-digest
                      {:domain :gravity/c6-current-reader-owner-metadata-v1
                       :branch branch :metadata value})))]
    {:owner-metadata-content-id (owner-id :complete-witness witness)
     :projection-contract
     {:artifact :gravity/c6-reader-owner-metadata-projection :schema-version 1
      :encoding :owner-canonical-semantic-v1
      :sorted-metadata-policy :default-only :core-carrier-policy :reject}
     :reader-plan-binding
     (assoc (select-keys binding p15-s23-c6c10-reader-binding-projection-keys)
            :instruction-summary-content-id (owner-id :instruction-summary (:instruction-summary binding)))
     :reader-source-revision
     (assoc (select-keys revision p15-s23-c6c10-reader-revision-projection-keys)
            :function-shapes-content-id (owner-id :reader-function-shapes (:function-shapes revision))
            :verifier-semantic-hash
            (get-in revision [:function-shapes 'sh03-verify-reader-result :semantic-hash]))
     :reader-result-identity
     (assoc (select-keys provenance
              [:actual-sh03-reader-result-id :actual-c2-artifact-id
               :actual-reader-product-integrity-hash :actual-reader-source-id])
            :actual-sh03-semantic-product-binding
            (select-keys (:actual-sh03-semantic-product-binding provenance)
                         p15-s23-c6c10-reader-product-binding-projection-keys))
     :authenticated-envelope
     (assoc (select-keys envelope
              [:artifact :schema-version :status :stage :semantic-envelope-id :provenance-binding-id])
            :source-rule
            (assoc (select-keys rule p15-s23-c6c10-reader-rule-projection-keys)
                   :function-shapes-content-id (owner-id :envelope-function-shapes (:function-shapes rule))
                   :owner-source-rule-content-id (owner-id :complete-source-rule rule))
            :gravity-template-replay
            (assoc (select-keys replay
                     [:status :identity-enforcement :eligible-for-contextual-acceptance? :request-count :semantic-authority])
                   :checks-content-id (owner-id :template-checks (:checks replay))
                   :diagnostics-content-id (owner-id :template-diagnostics (:diagnostics replay))))}))

(defn- p15-s23-c6c10-project-expression-owner-metadata
  [source-path binding]
  (p15-s23-c6c10-require-reader-owner-metadata! source-path binding)
  (let [owner-id (fn [branch value]
                   (clojure.core/binding [*print-length* nil *print-level* nil *print-meta* false
                             *print-dup* false *print-readably* true *print-namespace-maps* false]
                     (p15-s23-c11-mir-digest
                      {:domain :gravity/c6-current-expression-owner-metadata-v1
                       :branch branch :metadata value})))]
    (assoc (select-keys binding
             [:artifact :status :source-path :source-content-hash :semantic-hash
              :artifact-hash :generic-bridge-residual? :clojure-seed-boundary?
              :self-hosted? :source-contract-version :functions-semantic-hash :function-count])
           :functions-content-id (owner-id :export-shapes (:functions binding))
           :declared-plan-compiler-labels-content-id
           (owner-id :declared-plan-compiler-labels (:declared-plan-compiler-labels binding))
           :owner-binding-content-id (owner-id :complete-binding-metadata binding))))

(declare p15-s23-stage2-sh02-descriptor-envelope-verify!
         p15-s23-c6c10-require-trusted-carrier!)

(defn- p15-s23-c6c10-current-reader-envelope-state!
  ([source-path raw-front-end reader-path selected-binding]
   (p15-s23-c6c10-current-reader-envelope-state! source-path raw-front-end reader-path selected-binding 2))
  ([source-path raw-front-end reader-path selected-binding version]
  (p15-s23-c6c10-source-program-contract version)
  (when-not (contains? #{2 3} version)
    (p15-s23-c6c10-host-fail! "C6-VERIFY" source-path :selected-current-reader-invocation-policy {}))
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
                     {:source-interface {:interface :gravity/c6-c10 :version version}
                      :admission-status state :verification-scope :selected-current-source-host-seed
                      :envelope-verification :passed :self-hosted? false})]
        ;; Only the fixed projection crosses into the unchanged strict Core
        ;; carrier. The full original envelope was verified above, unmodified.
        (p15-s23-c6c10-require-trusted-carrier! source-path :current-reader-policy-receipt receipt)
        (cond-> receipt
          (= 3 version) (assoc :semantic-family :closed-flat-bool-literal-sequence))))))))
