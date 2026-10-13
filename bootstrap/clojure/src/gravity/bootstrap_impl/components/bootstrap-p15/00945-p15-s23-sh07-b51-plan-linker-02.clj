
(defn- p15-s23-sh07-b51-remove-runtime-context!
  [token context]
  (locking p15-s23-sh07-b51-admitted-runtime-contexts
    (let [query (p15-s23-sh07-b51-token-query token)
          context-ref
          (.get ^java.util.HashMap
                p15-s23-sh07-b51-admitted-runtime-contexts query)
          registered-context
          (when context-ref (.get ^java.lang.ref.WeakReference context-ref))]
      (when (identical? context registered-context)
        (.remove ^java.util.HashMap
                 p15-s23-sh07-b51-admitted-runtime-contexts query)
        true))))

(defn- p15-s23-sh07-b51-unadmitted-runtime-context [runtime]
  (assoc (apply dissoc (if (map? runtime) runtime {})
                p15-s23-sh07-b51-runtime-authority-keys)
         :sh07-b51-external-call-trace (atom [])))

(defn- p15-s23-sh07-b51-register-admitted-runtime-context
  [linked-products carrier raw route args runtime]
  (let [token (Object.)
        trace (atom [])
        top-level-invocation (Object.)
        top-level-owner
        (java.util.concurrent.atomic.AtomicReference. {:state :entering})
        receipt-slot
        (java.util.concurrent.atomic.AtomicReference. {:state :available})
        admitted-runtime
        (merge
         (apply dissoc (if (map? runtime) runtime {})
                p15-s23-sh07-b51-runtime-authority-keys)
         {:sh07-b51-runtime-context-token token
          :sh07-b51-runtime-token-boundary-id
          p15-s23-sh07-b51-runtime-token-boundary-id
          :sh07-b51-linked-carrier-v4 carrier
          ;; The Stage2 V2 builtin bridge still reads this historical key.
          ;; It holds the same carrier-v4 object; no carrier-v3 is admitted.
          :sh07-b51-linked-carrier-v3 carrier
          :sh07-b51-module-source-descriptors
          (:module-source-descriptors linked-products)
          :sh07-b51-link-manifest (:link-manifest linked-products)
          :sh07-b51-module-plans (:module-plans linked-products)
         :sh07-b51-execution-bundle (:execution-bundle linked-products)
         :sh07-b51-external-call-trace trace})
        context
        {:token-ref (java.lang.ref.WeakReference. token)
         :runtime-ref (java.lang.ref.WeakReference. admitted-runtime)
         :carrier carrier
         :module-source-descriptors
         (:module-source-descriptors linked-products)
         :link-manifest (:link-manifest linked-products)
         :module-plans (:module-plans linked-products)
         :execution-bundle (:execution-bundle linked-products)
         :external-call-trace trace
         :authority-ref {:carrier carrier :products linked-products}
         :raw-ref raw
         :selected-root-ref (:root route)
         :selected-plan-ref (:plan route)
         :arguments-ref args
         :top-level-invocation-ref top-level-invocation
         :top-level-owner-ref top-level-owner
         :receipt-slot-ref receipt-slot
         :thread-ref (Thread/currentThread)}]
    (when (contains? #{1 6 8} (get-in route [:root :root-ordinal]))
      (when-not (and (vector? args)
                     (pos? (count args))
                     (identical? raw (nth args 0))
                     (identical? carrier
                                 (:v18-executable-descriptor raw)))
        (throw (ex-info "selected SH-07 root arguments are not admission-bound"
                        {:id "L2-UNKNOWN-CORE-FORM"
                         :reason :unauthenticated-b51-invocation-identity}))))
    (try
      (p15-s23-sh07-b51-issue-top-level-validation-receipt! context)
      (p15-s23-sh07-b51-register-runtime-context! context)
      {:runtime admitted-runtime :context-lease context}
      (catch Throwable failure
        (p15-s23-sh07-b51-retire-admitted-runtime-context! context)
        (p15-s23-sh07-b51-remove-runtime-context! token context)
        (throw failure)))))

(defn- p15-s23-sh07-b51-admitted-runtime-context [runtime]
  (when (map? runtime)
    (let [token (:sh07-b51-runtime-context-token runtime)
          context
          (when token
            (p15-s23-sh07-b51-runtime-context-by-token token))]
      (when
       (and context
            (identical? token
                        (.get ^java.lang.ref.WeakReference
                              (:token-ref context)))
            (identical? runtime
                        (.get ^java.lang.ref.WeakReference
                              (:runtime-ref context)))
            (identical? (Thread/currentThread) (:thread-ref context))
            (= p15-s23-sh07-b51-runtime-token-boundary-id
               (:sh07-b51-runtime-token-boundary-id runtime))
            (identical? (:carrier context)
                        (:sh07-b51-linked-carrier-v4 runtime))
            (identical? (:carrier context)
                        (:sh07-b51-linked-carrier-v3 runtime))
            (identical? (:module-source-descriptors context)
                        (:sh07-b51-module-source-descriptors runtime))
            (identical? (:link-manifest context)
                        (:sh07-b51-link-manifest runtime))
            (identical? (:module-plans context)
                        (:sh07-b51-module-plans runtime))
            (identical? (:execution-bundle context)
                        (:sh07-b51-execution-bundle runtime))
            (identical? (:external-call-trace context)
                        (:sh07-b51-external-call-trace runtime)))
        context))))

(defn p15-s23-sh07-b51-runtime-admitted-products-match?
  [runtime carrier descriptors manifest plans bundle]
  (boolean
   (when-let [context
              (p15-s23-sh07-b51-admitted-runtime-context runtime)]
     (and (identical? carrier (:carrier context))
          (identical? descriptors (:module-source-descriptors context))
          (identical? manifest (:link-manifest context))
          (identical? plans (:module-plans context))
          (identical? bundle (:execution-bundle context))))))

(def ^:private p15-s23-sh07-b51-effective-builtins
  '#{* + - / < <= = > >= assoc conj count first get hash-map list pr-str
     rest second str vector symbol? keyword? char? number? list? seq? vector?
     map? set? string? contains? even? set sort-by-pr-str vec quot subvec
     integer? boolean? keys sh07-canonical-text-compare
     sh07-declared-digest-hash sh07-observe-utf8-byte-count
     sh07-b51-admitted-executable-carrier-validation?})

(defn p15-s23-sh07-b51-exact-keys? [value expected-keys]
  (and (map? value) (= expected-keys (set (keys value)))))

(defn p15-s23-sh07-b51-digest-id? [value]
  (and (string? value)
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn p15-s23-sh07-b51-canonical-id [value]
  (reader-canonical-hash value))

(defn p15-s23-sh07-b51-owner-edge-kind
  [caller-owner callee-owner]
  (cond
    (= caller-owner callee-owner)
    (case caller-owner
      :r1-producer :producer-internal
      :r6-independent-verifier :root6-internal
      nil)

    (and (= :r8-finalizer caller-owner)
         (= :r1-producer callee-owner))
    :root8-to-producer-interface

    :else nil))

(defn p15-s23-sh07-b51-binding-census [source-path module module-ordinal]
  (let [syntax-stream
        (mapv (fn [ordinal form]
                {:form form
                 :syntax/id
                 (p15-s23-sh07-b51-canonical-id
                  {:domain :gravity/sh07-b51-attempt24-syntax-v1
                   :module-ordinal module-ordinal :form-ordinal ordinal})
                 :span {:source source-path :form-index ordinal}})
              (range) (:forms module))
        sh05
        {:artifact-id
         (p15-s23-sh07-b51-canonical-id
          {:domain :gravity/sh07-b51-attempt24-sh05-census-v1
           :module-ordinal module-ordinal})
         :expanded-syntax-stream syntax-stream}
        definitions (sh06-resolution-definition-records module sh05)
        core (sh06-resolution-core-records)
        analysis (sh06-resolution-analysis-inputs module sh05 {})
        lexical (vec (mapcat :bindings (:lexical-scopes analysis)))]
    {:definition-count (count definitions)
     :core-binding-count (count core)
     :lexical-binding-count (count lexical)}))

(defn p15-s23-sh07-b51-builtin-call-census [function-table]
  (vec
   (mapcat
    (fn [caller]
      (let [definition (get function-table caller)
            caller-hash (p15-s23-sh07-source-definition-hash definition)]
        (mapcat
         (fn [body-form]
           (letfn [(walk [value]
                     (cond
                       (seq? value)
                       (if (= 'quote (first value))
                         []
                         (let [callee (first value)
                               row
                               (when (contains? p15-s23-sh07-b51-effective-builtins
                                                callee)
                                 [{:caller caller
                                   :caller-function-source-hash caller-hash
                                   :builtin callee
                                   :arity (count (rest value))
                                   :compiler-internal?
                                   (contains?
                                    p15-s23-sh07-declared-builtin-exact-arities
                                    callee)}])]
                           (into (or row []) (mapcat walk value))))
                       (map? value)
                       (mapcat walk
                               (mapcat identity
                                       (sort-by #(pr-str
                                                  (reader-canonical-value %))
                                                value)))
                       (coll? value) (mapcat walk value)
                       :else []))]
             (walk body-form)))
         (:body definition))))
    (sort-by str (keys function-table)))))

(defn p15-s23-sh07-b51-module-spec [ordinal]
  (when (and (integer? ordinal) (<= 0 ordinal 6))
    (nth p15-s23-sh07-b51-module-specs ordinal)))

(defn p15-s23-sh07-b51-definition-row-valid? [row]
  (and (p15-s23-sh07-b51-exact-keys?
        row p15-s23-sh07-b51-definition-keys)
       (symbol? (:name row))
       (integer? (:arity row)) (<= 0 (:arity row))
       (contains? #{:public :private} (:visibility row))
       (p15-s23-sh07-b51-digest-id? (:source-hash row))))

(defn p15-s23-sh07-b51-external-binding-row-valid? [row]
  (and (p15-s23-sh07-b51-exact-keys?
        row p15-s23-sh07-b51-external-binding-keys)
       (symbol? (:symbol row))
       (integer? (:callee-module-ordinal row))
       (<= 0 (:callee-module-ordinal row) 6)
       (symbol? (:callee-module row))
       (p15-s23-sh07-b51-digest-id? (:callee-source-sha256 row))
       (p15-s23-sh07-b51-digest-id?
        (:callee-definition-source-hash row))
       (integer? (:arity row)) (<= 0 (:arity row))
       (contains? #{:producer-internal :root6-internal
                    :root8-to-producer-interface}
                  (:owner-edge row))))
