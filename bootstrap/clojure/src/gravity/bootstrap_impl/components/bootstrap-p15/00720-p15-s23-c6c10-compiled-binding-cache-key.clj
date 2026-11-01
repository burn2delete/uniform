

(defn- p15-s23-c6c10-compiled-binding-cache-key
  [inputs]
  (let [{:keys [source-snapshot emitter-rule]} inputs
        version (p15-s23-c6c10-source-program-version inputs)
        contract (p15-s23-c6c10-source-program-contract version)]
  (cond-> {:schema-version 1
   :owner :gravity/p15-s23-c6-c10-source-binding
   :source-relative-path p15-s23-c6c10-source-relative-path
   :source-path (:canonical-source-path source-snapshot)
   :source-byte-count (:source-byte-count source-snapshot)
   :source-content-hash (:source-content-hash source-snapshot)
   :emitter-target :jvm
   :emitter-source-path
   (p15-s23-c6c10-canonical-file-path (:source-path emitter-rule))
   :emitter-source-byte-count
   p15-s23-stage2-compiler-expected-source-byte-count
   :emitter-source-content-hash
   p15-s23-stage2-compiler-expected-source-content-hash
   :emitter-source-rule-hash (:source-rule-hash emitter-rule)
   :expected-plan-semantic-hash
   (:plan-semantic-hash contract)
   :expected-functions-semantic-hash
   (:functions-semantic-hash contract)
   :builder-function p15-s23-c6c10-builder-function
   :expected-builder-semantic-hash
   (:builder-semantic-hash contract)
   :verifier-function p15-s23-c6c10-verifier-function
   :expected-verifier-semantic-hash
   (:verifier-semantic-hash contract)
   :required-functions (:required-functions contract)}
    (= 2 version) (assoc :source-contract-version version))))

(defn- p15-s23-c6c10-compile-authenticated-source-binding
  [request-source {:keys [source-snapshot emitter-rule] :as inputs}]
  (p15-s23-c6c10-record-compiled-binding-metric! :compile-start)
  (try
    (binding [*print-length* nil
              *print-level* nil
              *print-meta* false
              *print-dup* false
              *print-readably* true
              *print-namespace-maps* false]
      (let [version (p15-s23-c6c10-source-program-version inputs)
            contract (p15-s23-c6c10-source-program-contract version)
            {:keys [source-path canonical-source-path
                    source-byte-count source-content-hash source-text]}
            source-snapshot
            compilation-source-path canonical-source-path
            emitter (:emitter emitter-rule)
            plan
            (p15-s23-stage2-compiler-artifact-plan
             emitter compilation-source-path source-text)
            function-manifest
            (p15-s23-c6c10-function-merkle-manifest
             compilation-source-path source-content-hash (:functions plan))
            plan-pin-input
            (p15-s23-c6c10-plan-pin-input
             source-content-hash plan function-manifest)
            plan-semantic-hash
            (p15-s23-c6c10-canonical-digest
             compilation-source-path plan-pin-input)
            builder-semantic-hash
            (p15-s23-c6c10-canonical-digest
             compilation-source-path
             {:domain :gravity/c6-c10-exported-builder-v1
              :source-content-hash source-content-hash
              :definition
              (p15-s23-c6c10-path-neutral-value
               source-content-hash
               (get-in plan
                       [:functions p15-s23-c6c10-builder-function]))})
            verifier-semantic-hash
            (p15-s23-c6c10-canonical-digest
             compilation-source-path
             {:domain :gravity/c6-c10-exported-verifier-v1
              :source-content-hash source-content-hash
              :definition
              (p15-s23-c6c10-path-neutral-value
               source-content-hash
               (get-in plan
                       [:functions p15-s23-c6c10-verifier-function]))})
            observed-shapes
            (into {}
                  (map (fn [[function-name _]]
                         [function-name
                          (select-keys
                           (get-in plan [:functions function-name])
                           [:arity :params])]))
                  (:required-functions contract))
            compiled
            (cond-> {:kind :gravity/p15-s23-c6-c10-source-binding
             :status :complete
             :request-source request-source
             :source-path source-path
             :source-byte-count source-byte-count
             :source-content-hash source-content-hash
             :legacy-plan-id (:plan-id plan)
             :plan-semantic-hash plan-semantic-hash
             :functions-semantic-hash (:root-digest function-manifest)
             :builder-semantic-hash builder-semantic-hash
             :verifier-semantic-hash verifier-semantic-hash
             :function-shapes observed-shapes
             :function-manifest function-manifest
             :plan plan}
              (= 2 version) (assoc :source-contract-version version))]
        (p15-s23-c6c10-record-compiled-binding-metric!
         :compile-complete)
        compiled))
    (catch Throwable throwable
      (p15-s23-c6c10-record-compiled-binding-metric!
       :compile-failed)
      (when (instance? InterruptedException throwable)
        (.interrupt (Thread/currentThread)))
      (throw throwable))))

(defn p15-s23-c6c10-compile-source-binding
  ([request-source] (p15-s23-c6c10-compile-source-binding request-source 1))
  ([request-source version]
  (p15-s23-c6c10-compile-authenticated-source-binding
   request-source
   (p15-s23-c6c10-authenticated-source-binding-inputs!
    request-source version))))

(defn- p15-s23-c6c10-source-binding-valid?
  ([binding] (p15-s23-c6c10-source-binding-valid? binding 1))
  ([binding version]
  (let [contract (p15-s23-c6c10-source-program-contract version)]
  (and (if (contains? binding :source-contract-version)
         (let [tag (:source-contract-version binding)]
           (and (identical? Long (class tag))
                (== (.longValue ^Long version) (.longValue ^Long tag))))
         (= 1 version))
       (= :complete (:status binding))
       (= (:source-byte-count contract)
          (:source-byte-count binding))
       (= (:source-content-hash contract)
          (:source-content-hash binding))
       (= (:plan-semantic-hash contract)
          (:plan-semantic-hash binding))
       (= (:functions-semantic-hash contract)
          (:functions-semantic-hash binding))
       (= (:builder-semantic-hash contract)
          (:builder-semantic-hash binding))
       (= (:verifier-semantic-hash contract)
          (:verifier-semantic-hash binding))
       (= (:required-functions contract)
          (:function-shapes binding))
       (= (:function-count contract) (get-in binding [:function-manifest :function-count]))
       (or (= 1 version)
           (and (= (:chunk-size contract) (get-in binding [:function-manifest :chunk-size]))
                (= (:chunk-counts contract)
                   (mapv :count (get-in binding [:function-manifest :chunks])))))
       (<= (apply max 0
                  (map :count
                       (get-in binding [:function-manifest :chunks])))
           (:chunk-size contract))))))

(defn p15-s23-c6c10-validate-source-binding!
  ([binding] (p15-s23-c6c10-validate-source-binding! binding 1))
  ([binding version]
  (when-not (p15-s23-c6c10-source-binding-valid? binding version)
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" (:request-source binding)
     :exact-pinned-gravity-c6-c10-source-binding
     (dissoc binding :plan :function-manifest)))
  binding))
