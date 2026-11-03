

(defn- p15-s23-c6c10-current-source-version?
  [version]
  (and (identical? Long (class version))
       (or (== 2 (.longValue ^Long version))
           (== 3 (.longValue ^Long version)))))

(defn- p15-s23-c6c10-current-source-input?
  [value]
  (and (p15-s23-c6c10-current-source-version?
        (:source-contract-version value))
       (= :development-current (:source-mode value))))

(defn- p15-s23-c6c10-require-current-source-selection!
  [request-source version source-mode]
  (when-not (or (nil? source-mode)
                (and (p15-s23-c6c10-current-source-version? version)
                     (= :development-current source-mode)))
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" request-source :explicit-current-source-selection {})))

(defn- p15-s23-c6c10-source-contract-for
  [value]
  (if (p15-s23-c6c10-current-source-input? value)
    (if (= 3 (:source-contract-version value))
      c6-source-contract/flat-bool-current-compiler-program-abi
      c6-source-contract/current-compiler-program-abi)
    (p15-s23-c6c10-source-program-contract
     (p15-s23-c6c10-source-program-version value))))

(defn- p15-s23-c6c10-compiled-binding-cache-key
  [inputs]
  (let [{:keys [source-snapshot emitter-rule]} inputs
        version (p15-s23-c6c10-source-program-version inputs)
        current-source? (p15-s23-c6c10-current-source-input? inputs)
        contract (p15-s23-c6c10-source-contract-for inputs)]
  (cond-> {:schema-version 1
   :owner :gravity/p15-s23-c6-c10-source-binding
   :source-mode (:source-mode inputs)
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
   :expected-plan-semantic-hash (when-not current-source?
                                  (:plan-semantic-hash contract))
   :expected-functions-semantic-hash (when-not current-source?
                                      (:functions-semantic-hash contract))
   :builder-function p15-s23-c6c10-builder-function
   :expected-builder-semantic-hash (when-not current-source?
                                    (:builder-semantic-hash contract))
   :verifier-function p15-s23-c6c10-verifier-function
   :expected-verifier-semantic-hash (when-not current-source?
                                     (:verifier-semantic-hash contract))
   :required-functions (:required-functions contract)}
    (not= 1 version) (assoc :source-contract-version version)
    (= 3 version) (assoc :semantic-family (:semantic-family contract)))))

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
            current-source? (p15-s23-c6c10-current-source-input? inputs)
            contract (p15-s23-c6c10-source-contract-for inputs)
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
            current-program
            (when current-source?
              (assoc (select-keys contract [:version :semantic-family :source-relative-path
                                            :builder-function :verifier-function
                                            :required-functions])
                     :source-mode :development-current
                     :source-byte-count source-byte-count
                     :source-content-hash source-content-hash
                     :plan-semantic-hash plan-semantic-hash
                     :functions-semantic-hash (:root-digest function-manifest)
                     :builder-semantic-hash builder-semantic-hash
                     :verifier-semantic-hash verifier-semantic-hash
                     :function-count (:function-count function-manifest)
                     :chunk-size (:chunk-size function-manifest)
                     :chunk-counts (mapv :count (:chunks function-manifest))))
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
              current-source? (assoc :source-mode :development-current
                                     :source-program current-program)
              (not= 1 version) (assoc :source-contract-version version)
              (= 3 version) (assoc :semantic-family (:semantic-family contract)))]
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
   (p15-s23-c6c10-compile-source-binding request-source version nil))
  ([request-source version source-mode]
  (p15-s23-c6c10-require-current-source-selection!
   request-source version source-mode)
  (p15-s23-c6c10-compile-authenticated-source-binding
   request-source
   (if (some? source-mode)
     (p15-s23-c6c10-authenticated-source-binding-inputs!
      request-source version source-mode)
     (p15-s23-c6c10-authenticated-source-binding-inputs!
      request-source version)))))

(defn- p15-s23-c6c10-source-binding-valid?
  ([binding] (p15-s23-c6c10-source-binding-valid? binding 1))
  ([binding version]
  (let [current-source? (p15-s23-c6c10-current-source-input? binding)
        current-program (:source-program binding)
        expected-current-program
        (if (= 3 version)
          c6-source-contract/flat-bool-current-compiler-program-abi
          c6-source-contract/current-compiler-program-abi)
        contract (if current-source?
                   (or current-program expected-current-program)
                   (p15-s23-c6c10-source-program-contract version))]
  (and (if (contains? binding :source-contract-version)
         (let [tag (:source-contract-version binding)]
           (and (identical? Long (class tag))
                (== (.longValue ^Long version) (.longValue ^Long tag))))
         (= 1 version))
       (if current-source?
         (and (= (:version expected-current-program) version)
              (= :development-current (:source-mode binding))
              (map? current-program)
              (= :development-current (:source-mode current-program))
              (= p15-s23-c6c10-source-relative-path
                 (:source-relative-path current-program))
              (= (:builder-function expected-current-program)
                 (:builder-function current-program))
              (= (:verifier-function expected-current-program)
                 (:verifier-function current-program))
              (= (:required-functions expected-current-program)
                 (:required-functions current-program))
              (if (= 3 version)
                (= (:semantic-family expected-current-program)
                   (:semantic-family current-program))
                true)
              (= :complete (:status binding))
              (= (:source-byte-count current-program)
                 (:source-byte-count binding))
              (= (:source-content-hash current-program)
                 (:source-content-hash binding))
              (= (:plan-semantic-hash current-program)
                 (:plan-semantic-hash binding))
              (= (:functions-semantic-hash current-program)
                 (:functions-semantic-hash binding))
              (= (:builder-semantic-hash current-program)
                 (:builder-semantic-hash binding))
              (= (:verifier-semantic-hash current-program)
                 (:verifier-semantic-hash binding))
              (= (:required-functions current-program)
                 (:function-shapes binding))
              (= (:function-count current-program)
                 (get-in binding [:function-manifest :function-count]))
              (= (:chunk-size current-program)
                 (get-in binding [:function-manifest :chunk-size]))
              (= (:chunk-counts current-program)
                 (mapv :count (get-in binding [:function-manifest :chunks])))
              (<= (apply max 0
                         (map :count
                              (get-in binding [:function-manifest :chunks])))
                  (:chunk-size current-program)))
         (and (if (= 3 version)
                (identical? (:semantic-family contract) (:semantic-family binding))
                true)
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
                  (:chunk-size contract))))))))

(defn p15-s23-c6c10-validate-source-binding!
  ([binding] (p15-s23-c6c10-validate-source-binding! binding 1))
  ([binding version]
  (when-not (p15-s23-c6c10-source-binding-valid? binding version)
    (p15-s23-c6c10-host-fail!
     "C6-VERIFY" (:request-source binding)
     :exact-pinned-gravity-c6-c10-source-binding
     (dissoc binding :plan :function-manifest)))
  binding))

(let [authority-token (Object.)
      cache-state (atom nil)]

  ;; Bootstrap cache tests resolve these private inspection hooks by symbol.
  #_{:clj-kondo/ignore [:unused-private-var]}
  (defn- p15-s23-c6c10-clear-compiled-binding-cache!
    []
    (reset! cache-state nil)
    :cleared)

  #_{:clj-kondo/ignore [:unused-private-var]}
  (defn- p15-s23-c6c10-compiled-binding-cache-state
    []
    (let [entry @cache-state]
      {:entry-count (if (nil? entry) 0 1)
       :occupied? (some? entry)
       :schema-version (:schema-version entry)
       :cache-key (:cache-key entry)}))

  (defn- p15-s23-c6c10-valid-compiled-binding-cache-entry?
    [entry cache-key]
    (and (map? entry)
         (not (record? entry))
         (nil? (meta entry))
         (= #{:schema-version :authority-token :cache-key :binding}
            (set (keys entry)))
         (= 1 (:schema-version entry))
         (identical? authority-token (:authority-token entry))
         (= cache-key (:cache-key entry))
         (map? (:binding entry))
         (not (record? (:binding entry)))
         (nil? (meta (:binding entry)))
         (not (contains? (:binding entry) :request-source))
         (not (contains? (:binding entry) :source-path))
         (= (:source-mode cache-key) (:source-mode (:binding entry)))
         (p15-s23-c6c10-source-binding-valid?
          (:binding entry) (p15-s23-c6c10-source-program-version cache-key))))

  (defn- p15-s23-c6c10-cached-compiled-binding!
    [request-source inputs]
    (let [cache-key
          (p15-s23-c6c10-compiled-binding-cache-key inputs)
          entry @cache-state]
      (if (p15-s23-c6c10-valid-compiled-binding-cache-entry?
           entry cache-key)
        (do
          (p15-s23-c6c10-record-compiled-binding-metric! :cache-hit)
          (:binding entry))
        (locking cache-state
          (let [entry @cache-state]
            (if (p15-s23-c6c10-valid-compiled-binding-cache-entry?
                 entry cache-key)
              (do
                (p15-s23-c6c10-record-compiled-binding-metric! :cache-hit)
                (:binding entry))
              (do
                (p15-s23-c6c10-record-compiled-binding-metric! :cache-miss)
                (when (and (some? entry)
                           (= cache-key (:cache-key entry)))
                  (reset! cache-state nil))
                (let [binding
                      (p15-s23-c6c10-validate-source-binding!
                       (p15-s23-c6c10-compile-authenticated-source-binding
                        request-source inputs)
                       (p15-s23-c6c10-source-program-version inputs))
                      cached-binding
                      (dissoc binding :request-source :source-path)]
                  (reset!
                   cache-state
                   {:schema-version 1
                    :authority-token authority-token
                    :cache-key cache-key
                    :binding cached-binding})
                  cached-binding))))))))

(defn p15-s23-c6c10-source-binding!
  ([request-source] (p15-s23-c6c10-source-binding! request-source 1))
  ([request-source version]
   (p15-s23-c6c10-source-binding! request-source version nil))
  ([request-source version source-mode]
  ;; Raw module bytes and the Gravity plan-emitter are authenticated before
  ;; every lookup.  Only the immutable compiled compiler program is reused;
  ;; user ingress, builder/verifier execution, digest sealing, diagnostics,
  ;; and target artifacts remain fresh replay boundaries.
  (p15-s23-c6c10-require-current-source-selection!
   request-source version source-mode)
  (let [inputs
        (if (some? source-mode)
          (p15-s23-c6c10-authenticated-source-binding-inputs!
           request-source version source-mode)
          (p15-s23-c6c10-authenticated-source-binding-inputs!
           request-source version))
        cached-binding
        (p15-s23-c6c10-cached-compiled-binding!
         request-source inputs)
        current-source-path
        (get-in inputs [:source-snapshot :source-path])]
    (p15-s23-c6c10-validate-source-binding!
     (assoc cached-binding
            :request-source request-source
            :source-path current-source-path) version)))))

(def p15-s23-c6c10-digest-request-keys
  #{:algorithm :depends-on :encoding :key :ordinal :preimage})
