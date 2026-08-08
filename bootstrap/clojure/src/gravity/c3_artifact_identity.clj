(ns gravity.c3-artifact-identity
  "Hosted Stage0 path-neutral identity projection for C3 artifacts.

  This leaf consumes already authenticated reader and SH04 boundary records. It
  normalizes checkout provenance out of the hosted C3 identity preimage but
  does not authenticate either upstream, define canonical serialization, or
  grant proof, self-hosting, or release authority.")

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private operation-keys
  #{:c2-token-hash-input
    :c2-form-hash-input
    :c2-syntax-seed-hash-input
    :c2-extension-hash-input
    :c2-path-neutral-span
    :c3-path-neutral-origin
    :reader-canonical-hash
    :c3-path-neutral-reader-artifact-view
    :c3-path-neutral-syntax-object
    :c3-gravity-syntax-boundary-identity-view
    :c3-artifact-identity-input
    :c3-artifact-id})

(def ^:private namespace-contract
  {:namespace 'gravity.c3-artifact-identity
   :contract-boundary :hosted-c3-path-neutral-artifact-identity
   :public-api
   {'with-operations {:arglists '([operations thunk])}
    'c3-path-neutral-reader-artifact-view {:arglists '([c2-view])}
    'c3-path-neutral-syntax-object {:arglists '([syntax])}
    'c3-gravity-syntax-boundary-identity-view {:arglists '([boundary])}
    'c3-artifact-identity-input {:arglists '([artifact])}
    'c3-artifact-id {:arglists '([artifact])}}
   :operation-interposition
   {:accepted-keys operation-keys
    :partial-overrides? true
    :unknown-keys-rejected? true
    :function-values-must-satisfy :fn?}
   :artifact-inputs [:authenticated-hosted-c2-reader-view
                     :authenticated-hosted-sh04-boundary
                     :hosted-c3-syntax-artifact]
   :artifact-outputs [:hosted-c3-path-neutral-identity-preimage
                      :hosted-c3-artifact-id]
   :ownership
   {:owns [:hosted-c3-path-neutral-artifact-identity-projection]
    :does-not-own [:canonical-c3-syntax-object-authority
                   :c2-reader-product-authentication
                   :sh04-boundary-authentication
                   :canonical-encoding
                   :signature-or-trust-root
                   :diagnostic-policy
                   :proof-authority
                   :self-hosted-authority
                   :release-authority]}
   :dependency-direction
   {:requires ['clojure.core]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-c3-authority? false
   :self-hosted? false
   :release-authority? false})

(defn- unsupported [key]
  (fn [& _]
    (throw (ex-info (str "C3 artifact identity requires operation " key)
                    {:operation key}))))

(defn- validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C3 artifact identity operations must be a map"
                    {:operations operations})))
  (let [unknown (vec (remove operation-keys (keys operations)))]
    (when (seq unknown)
      (throw (ex-info "C3 artifact identity operations contain unknown keys"
                      {:unknown-keys unknown}))))
  (doseq [[key value] operations]
    (when-not (fn? value)
      (throw (ex-info "C3 artifact identity operation must be a function"
                      {:operation key :value value}))))
  operations)

(defn with-operations [operations thunk]
  (validate-operations! operations)
  (when-not (fn? thunk)
    (throw (ex-info "C3 artifact identity thunk must be a function"
                    {:thunk thunk})))
  (binding [*operations* operations] (thunk)))

(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key) (get *operations* key)))

(defmacro ^:private definterposable [name args & body]
  (let [key (keyword name)]
    `(defn ~name ~args
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys* (conj *active-operation-keys* ~key)]
           (operation# ~@args))
         (do ~@body)))))

(defn- invoke [key & args]
  (apply (or (current-operation key) (unsupported key)) args))

(definterposable c3-path-neutral-reader-artifact-view
  [c2-view]
  (-> c2-view
      (update :sh03-reader-authentication
              #(dissoc % :provenance-binding-id))
      (update :source-unit-record
              #(-> %
                   (dissoc :path)
                   (update :project-root-record dissoc :path)))
      (update :token-stream #(invoke :c2-token-hash-input %))
      (update :form-tree #(invoke :c2-form-hash-input %))
      (update :syntax-seed-stream #(invoke :c2-syntax-seed-hash-input %))
      (update :reader-extension-invocation-records
              #(invoke :c2-extension-hash-input %))
      (update :reader-source-map
              (fn [records]
                (mapv #(update % :span
                               (fn [span]
                                 (invoke :c2-path-neutral-span span)))
                      records)))
      (update :literal-decoding-records
              (fn [records]
                (mapv #(update % :span
                               (fn [span]
                                 (invoke :c2-path-neutral-span span)))
                      records)))
      (update-in [:semantic-error-deferment-record
                  :deferred-literal-records]
                 (fn [records]
                   (mapv #(update % :span
                                  (fn [span]
                                    (invoke :c2-path-neutral-span span)))
                         records)))))

(definterposable c3-path-neutral-syntax-object
  [syntax]
  (-> syntax
      (update :span
              (fn [span]
                (-> span
                    (update :primary
                            #(invoke :c2-path-neutral-span %))
                    (update :all
                            #(mapv (fn [item]
                                     (invoke :c2-path-neutral-span item))
                                   (or % []))))))
      (update :origin
              #(mapv (fn [origin]
                       (invoke :c3-path-neutral-origin origin))
                     (or % [])))))

(definterposable c3-gravity-syntax-boundary-identity-view
  [boundary]
  (let [binding (:plan-binding boundary)
        result (:resolved-syntax-result boundary)
        envelope (:authenticated-envelope boundary)]
    {:slice (:slice boundary)
     :owner (:owner boundary)
     :adapter-contract (:adapter-contract boundary)
     :plan-binding
     (select-keys
      binding
      [:artifact :status :semantic-authority :source-byte-count
       :source-content-hash :plan-semantic-hash
       :functions-semantic-hash :function-count
       :function-names-hash :function-shapes-hash
       :public-function-hashes :public-function-shapes])
     :reader-semantic-binding (:reader-semantic-binding boundary)
     :reader-source-revision (:reader-source-revision boundary)
     :resolved-syntax-result
     (select-keys
      result
      [:artifact :kind :schema-version :status :artifact-id
       :semantic-source-id
       :reader-binding :root-syntax-ids :graph-verification-report
       :syntax-serialization :authority :trusted-boundary])
     :semantic-envelope-id (:semantic-envelope-id envelope)
     :resolved-stream-verification
     (select-keys (:resolved-stream-verification boundary)
                  [:artifact :schema-version :status :checks])
     :stream-digest-requests (:stream-digest-requests boundary)
     :stream-resolved-digests (:stream-resolved-digests boundary)
     :gravity-syntax-serialization
     (select-keys (:gravity-syntax-serialization boundary)
                  [:artifact :schema-version :status :encoding
                   :payload-id-request])
     :gravity-syntax-deserialization
     (select-keys (:gravity-syntax-deserialization boundary)
                  [:artifact :schema-version :status :encoding])
     :uncredited-compatibility-facade
     (:uncredited-compatibility-facade boundary)
     :target-source-reread? (:target-source-reread? boundary)
     :clojure-adapter-residual? (:clojure-adapter-residual? boundary)
     :self-hosted? (:self-hosted? boundary)}))

(definterposable c3-artifact-identity-input
  [artifact]
  (let [boundary (:gravity-syntax-boundary artifact)]
    (cond->
     (-> artifact
         (dissoc :artifact-id)
         (update :c2-reader-artifact c3-path-neutral-reader-artifact-view)
         (update :syntax-object-stream
                 #(mapv c3-path-neutral-syntax-object (or % [])))
         (update-in [:origin-chain-graph :nodes]
                    (fn [nodes]
                      (mapv #(update % :origin
                                     (fn [origins]
                                       (mapv (fn [origin]
                                               (invoke :c3-path-neutral-origin
                                                       origin))
                                             (or origins []))))
                            (or nodes []))))
         (update-in [:gravity-origin-chain-graph :nodes]
                    (fn [nodes]
                      (mapv #(update % :origin
                                     (fn [origins]
                                       (mapv (fn [origin]
                                               (invoke :c3-path-neutral-origin
                                                       origin))
                                             (or origins []))))
                            (or nodes [])))))
      boundary
      (assoc :c2-reader-artifact (:reader-semantic-binding boundary)
             :gravity-syntax-boundary
             (c3-gravity-syntax-boundary-identity-view boundary)))))

(definterposable c3-artifact-id
  [artifact]
  (invoke :reader-canonical-hash (c3-artifact-identity-input artifact)))
