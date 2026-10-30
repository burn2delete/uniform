
(defn- envelope-valid?
  [expected-stage expected-lane expected-key-id expected-key envelope
   source-id provenance parents]
  ;; Validate the operational binding as well as the semantic id.  In
  ;; particular, a valid artifact from another stage/lane/pipeline must be a
  ;; cache miss, never an accepted hit.  Any verifier/identity exception is a
  ;; fail-closed miss rather than a cache-serving error.
  (try
    (let [payload (:payload envelope)
          diagnostics (vec (or (:diagnostics payload)
                               (:reader-diagnostics payload)
                               []))
          semantic (semantic-preimage
                    expected-stage source-id parents
                    (:payload-artifact-id envelope)
                    (canonical-id! expected-stage diagnostics))
          integrity (payload-integrity expected-stage payload)
          owner-id (when-not (= :source expected-stage)
                     (delegated-artifact-identity expected-stage payload))]
      (and (map? envelope)
           (= :gravity/authenticated-compiler-artifact (:artifact envelope))
           (= pipeline-schema-version (:schema-version envelope))
           (= pipeline-id (:pipeline envelope))
           (= expected-stage (:stage envelope))
           (= :accepted (:status envelope))
           (= expected-lane (:mode envelope))
           (= expected-key-id (:cache-key-id envelope))
           (= expected-key-id
              (canonical-id! expected-stage expected-key))
           (= source-id (:source-identity envelope))
           (= provenance (:provenance envelope))
           (= parents (:input-artifact-ids envelope))
           (= semantic (:semantic-preimage envelope))
           (= (:artifact-id envelope)
              (canonical-id! expected-stage
                             (envelope-id-preimage envelope)))
           (= (:payload-integrity-id envelope) integrity)
           (= (:payload-size-witness envelope)
              (payload-size-witness expected-stage payload))
           ;; All currently exposed owner identities are independent from the
           ;; facade wrapper.  SH-07 core's owner identity is verifier-bound
           ;; and may be unavailable here; its replay verifier still runs on a
           ;; cache hit.
           (or (= :source expected-stage)
               (nil? owner-id)
               (= (:artifact-id payload) owner-id))
           (= (get-in envelope [:pass :id]) (get stage-pass-ids expected-stage))
           (= (get-in envelope [:pass :preserves])
              (get-in stage-fact-contracts [expected-stage :preserves]))
           (= (get-in envelope [:pass :invalidates])
              (get-in stage-fact-contracts [expected-stage :invalidates]))
           (= (get-in envelope [:receipt :receipt-id])
              (get-in (receipt expected-stage expected-lane
                               (:artifact-id envelope) parents
                               (get-in envelope [:receipt :cache :hit?])
                               expected-key-id source-id provenance)
                      [:receipt-id]))))
    (catch clojure.lang.ExceptionInfo _ false)
    (catch IllegalArgumentException _ false)))

(defn- embedded-parent-id
  "Read the existing bootstrap artifact's authenticated parent projection."
  [stage payload]
  (case stage
    :reader nil
    :syntax (get-in payload [:c2-reader-artifact :artifact-id])
    :macroexpansion (get-in payload [:sh04-syntax-artifact :artifact-id])
    :resolution (get-in payload [:sh05-macro-artifact :artifact-id])
    :checked-core (get-in payload [:sh06-resolution-artifact :artifact-id])
    nil))

(defn- authenticate-embedded-parent!
  [stage payload parents]
  (when (and (pos? (get stage-index stage))
             (not= :reader stage))
    (let [expected (last parents)
          observed (embedded-parent-id stage payload)]
      (when-not (= expected observed)
        (fail! "ARTIFACT-PARENT"
               "bootstrap artifact does not authenticate the requested parent"
               {:stage stage
                :expected-parent-id expected
                :observed-parent-id observed})))))

(defn- reader-operation!
  [source-path request]
  (when (= :development-current (:reader request))
    (let [reader-path (bootstrap-call 'sh03-reader-resolve-source-path)
          invocation {:reader-source-path reader-path
                      :authenticated-envelope-options {:source :development-current}}
          binding (:binding (bootstrap-call 'sh03-reader-verification-invocation! source-path invocation))
          identity (select-keys binding [:source-content-hash :source-byte-count :plan-semantic-hash :functions-semantic-hash])
          inputs (get request :module-inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []})
          views (if (and (vector? (:module-artifacts inputs)) (seq (:module-artifacts inputs)))
                  (bootstrap-call 'sh06-resolution-with-reader-invocation source-path invocation
                                  #(bootstrap-call 'sh06-resolution-module-input-views! source-path inputs))
                  (bootstrap-call 'sh06-resolution-module-input-views! source-path inputs))]
      (when-not (and (= 4 (count identity)) (every? some? (vals identity)))
        (fail! "ARTIFACT-READER" "selected Reader binding is incomplete" {:source-path source-path}))
      {:invocation invocation :binding identity :module-inputs inputs
       :module-artifact-ids (mapv :artifact-id views)})))

(defn- stable-reader-after! [source-path request]
  (when (:reader-operation request)
    (when-not (= (:reader-operation request) (reader-operation! source-path request))
      (fail! "ARTIFACT-MUTATED" "selected Reader or module inputs changed during the pipeline"
             {:source-path source-path}))))

(defn- build-payload
  ([stage source-path source-text context previous]
   (build-payload stage source-path source-text context previous nil))
  ([stage source-path source-text context previous request]
   (if-let [{:keys [invocation module-inputs]} (:reader-operation request)]
     (let [reader-path (:reader-source-path invocation)
           parent (:payload (last previous))]
       (case stage
         :reader (bootstrap-call 'compiler-c2-reader-source-artifact source-path source-text context nil nil reader-path invocation)
         :syntax (bootstrap-call 'compiler-c3-syntax-source-artifact source-path source-text context parent
                                 (var-get (bootstrap-var 'sh03-reader-internal-product-authority)) nil invocation)
         :macroexpansion (bootstrap-call 'sh05-macro-artifact-from-c3-artifact source-path parent invocation)
         :resolution (bootstrap-call 'sh06-resolution-artifact-from-sh05-artifact source-path parent module-inputs reader-path invocation)
         :checked-core (bootstrap-call 'sh07-core-from-resolution-artifact parent invocation)))
     (case stage
       :source (source-payload source-path nil context)
       :reader (bootstrap-call 'compiler-c2-reader-source-artifact
                               source-path source-text context)
       :syntax (bootstrap-call
                'compiler-c3-syntax-source-artifact
                source-path source-text context (:payload (last previous))
                (var-get (bootstrap-var 'sh03-reader-internal-product-authority)))
    ;; SH-05 and SH-06 consume only their immediate authenticated parent.
    ;; Their historical source entrypoints remain compatibility wrappers, but
    ;; the unified pipeline must not rebuild an already-authenticated prefix.
       :macroexpansion (bootstrap-call 'sh05-macro-artifact-from-c3-artifact
                                       source-path (:payload (last previous)))
       :resolution (bootstrap-call 'sh06-resolution-artifact-from-sh05-artifact
                                   source-path (:payload (last previous)))
       :checked-core (bootstrap-call 'sh07-core-from-resolution-artifact
                                     (:payload (last previous)))))))

(defn- build-stage!
  [stage lane source-id provenance source-path source-text context previous
   request]
  (let [parents (parent-ids stage previous)
        preimage (cache-key-preimage stage lane source-id parents request)
        key-id (canonical-id! stage preimage)
        key [key-id (:actual-path provenance)]]
    (if (and (= lane :fast) (contains? @stage-cache key))
      (let [cached (get @stage-cache key)]
        (if (and (envelope-valid? stage lane key-id preimage cached source-id
                                  provenance parents)
                 (try
                   (authenticate-embedded-parent! stage (:payload cached) parents)
                   true
                   (catch clojure.lang.ExceptionInfo _ false))
                 (or (not= :reader stage)
                     (try
                       (authenticate-reader-source! source-path source-id
                                                    (:payload cached))
                       true
                       (catch clojure.lang.ExceptionInfo _ false)))
                 (apply revalidate-payload? (cond-> [stage source-path (:payload cached)]
                                              (:reader-operation request) (conj (:reader-operation request)))))
          (-> cached
              (assoc :cache-hit? true
                     :cache-policy (assoc (:cache-policy cached)
                                          :observed-hit? true))
              (assoc :receipt
                     (receipt stage lane (:artifact-id cached) parents true
                              key-id source-id provenance)))
          (do
            (swap! stage-cache dissoc key)
            (let [payload (apply build-payload (cond-> [stage source-path source-text context previous]
                                                 (:reader-operation request) (conj request)))
                  _ (when (= :reader stage)
                      (authenticate-reader-source! source-path source-id payload))
                  _ (authenticate-embedded-parent! stage payload parents)
                  built (envelope stage lane source-id provenance parents payload
                                  false key-id)]
              (cache-put! key built)
              built))))
      (let [payload (apply build-payload (cond-> [stage source-path source-text context previous]
                                           (:reader-operation request) (conj request)))
            _ (when (= :reader stage)
                (authenticate-reader-source! source-path source-id payload))
            _ (authenticate-embedded-parent! stage payload parents)
            built (envelope stage lane source-id provenance parents payload false
                            key-id)]
        (when (= lane :fast)
          (cache-put! key built))
        built))))

(defn- stable-source-after!
  [source-path before]
  (let [after (source-snapshot! source-path)]
    (when-not (and (= (:canonical-path before) (:canonical-path after))
                   (= (:byte-count before) (:byte-count after))
                   (= (:bytes-hash before) (:bytes-hash after)))
      (fail! "ARTIFACT-MUTATED"
             "source changed or was rebound while the artifact pipeline ran"
             {:source-path source-path
              :before (select-keys before [:canonical-path :byte-count :bytes-hash])
              :after (select-keys after [:canonical-path :byte-count :bytes-hash])}))
    after))

(defn- validate-request
  [request]
  (when-not (map? request)
    (fail! "ARTIFACT-REQUEST"
           "authenticated artifact pipeline request must be a map"
           {:request request}))
  (let [known #{:source-path :target-stage :mode :reader :module-inputs}]
    (when-let [unknown (seq (remove known (keys request)))]
      (fail! "ARTIFACT-REQUEST"
             "authenticated artifact pipeline request contains unknown keys"
             {:unknown-keys (vec (sort unknown))
              :known-keys (vec (sort known))})))
  (let [source-path (:source-path request)
        target-stage (get request :target-stage :checked-core)
        mode (get request :mode :fast)]
    (require-source-path! source-path)
    (require-stage! target-stage)
    (require-lane! mode)
    (when (and (contains? request :reader) (not= :development-current (:reader request)))
      (fail! "ARTIFACT-REQUEST" "Reader option must select development-current" {}))
    (when (and (contains? request :module-inputs) (not= :development-current (:reader request)))
      (fail! "ARTIFACT-REQUEST" "module inputs require explicit current Reader selection" {}))
    (merge {:source-path source-path :target-stage target-stage :mode mode}
           (select-keys request [:reader :module-inputs]))))
