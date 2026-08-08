(ns gravity.c3-reader-integrity
  "Hosted Stage0 C3 validation of its C2 reader-artifact input.

  The leaf recomputes compatibility integrity facts through injected C2
  operations. It does not execute or authenticate the canonical reader, own
  SH03/SH04 authority, or grant proof, self-hosting, or release authority.")

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private function-operation-keys
  #{:c2-lexical-product-validation
    :c2-incremental-hashes
    :c2-literal-records
    :c2-deferred-semantic-literals
    :c3-deferred-ratio-descriptor-from-raw
    :c2-reader-product-integrity-record
    :reader-canonical-hash
    :sha256-hex
    :c2-reader-artifact-id
    :c3-c2-reader-integrity-report
    :c3-validate-c2-reader-artifact!
    :c3-syntax-fail!
    :source-span})
(def ^:private scalar-operation-keys #{:max-reader-form-graph-depth})
(def ^:private operation-keys
  (into function-operation-keys scalar-operation-keys))

(def ^:private namespace-contract
  {:namespace 'gravity.c3-reader-integrity
   :contract-boundary :hosted-c3-c2-reader-input-integrity
   :public-api
   {'with-operations {:arglists '([operations thunk])}
    'c3-c2-reader-integrity-report {:arglists '([c2-artifact])}
    'c3-validate-c2-reader-artifact!
    {:arglists '([source-path c2-artifact])}}
   :operation-interposition
   {:accepted-keys operation-keys
    :partial-overrides? true
    :unknown-keys-rejected? true}
   :artifact-inputs [:hosted-c2-reader-document-artifact]
   :artifact-outputs [:hosted-c3-reader-input-integrity-report]
   :ownership
   {:owns [:hosted-c3-reader-input-integrity-recomputation
           :hosted-c3-stale-reader-input-rejection]
    :does-not-own [:canonical-c2-reader-authority
                   :sh03-reader-product-authentication
                   :sh04-syntax-boundary-authentication
                   :source-reading
                   :diagnostic-construction
                   :canonical-c3-syntax-object-authority
                   :proof-authority
                   :attestation-authority
                   :self-hosted-authority
                   :release-authority]}
   :dependency-direction
   {:requires ['clojure.core]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-c2-authority? false
   :canonical-c3-authority? false
   :proof-authority? false
   :self-hosted? false
   :release-authority? false})

(defn- validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C3 reader-integrity operations must be a map"
                    {:operations operations})))
  (let [unknown (vec (remove operation-keys (keys operations)))]
    (when (seq unknown)
      (throw (ex-info "C3 reader-integrity operations contain unknown keys"
                      {:unknown-keys unknown}))))
  (doseq [key function-operation-keys
          :when (contains? operations key)]
    (when-not (fn? (get operations key))
      (throw (ex-info "C3 reader-integrity operation must be a function"
                      {:operation key :value (get operations key)}))))
  (when (contains? operations :max-reader-form-graph-depth)
    (let [depth (:max-reader-form-graph-depth operations)]
      (when-not (and (integer? depth) (pos? depth))
        (throw (ex-info "C3 reader-integrity depth limit must be positive"
                        {:operation :max-reader-form-graph-depth
                         :value depth})))))
  operations)

(defn with-operations [operations thunk]
  (validate-operations! operations)
  (when-not (fn? thunk)
    (throw (ex-info "C3 reader-integrity thunk must be a function"
                    {:thunk thunk})))
  (binding [*operations* operations] (thunk)))

(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key) (get *operations* key)))

(defn- invoke [key & args]
  (if-let [operation (current-operation key)]
    (apply operation args)
    (throw (ex-info (str "C3 reader-integrity requires operation " key)
                    {:operation key}))))

(defn c3-c2-reader-integrity-report [c2-artifact]
  (if-let [operation (current-operation :c3-c2-reader-integrity-report)]
    (binding [*active-operation-keys*
              (conj *active-operation-keys*
                    :c3-c2-reader-integrity-report)]
      (operation c2-artifact))
    (try
      (let [source-unit (:source-unit-record c2-artifact)
            token-stream (:token-stream c2-artifact)
            form-tree (:form-tree c2-artifact)
            top-level-form-ids (:top-level-form-ids c2-artifact)
            syntax-seeds (:syntax-seed-stream c2-artifact)
            extension-invocations
            (or (:reader-extension-invocation-records c2-artifact) [])
            diagnostics (or (:reader-diagnostics c2-artifact) [])
            raw-source-available? (every? #(string? (:raw %)) token-stream)
            source-text (when raw-source-available?
                          (apply str (map :raw token-stream)))
            lexical
            (when source-text
              (invoke :c2-lexical-product-validation
                      source-text token-stream form-tree top-level-form-ids))
            graph-valid? (true? (:graph-valid? lexical))
            depth-valid?
            (and (integer? (:max-form-depth lexical))
                 (<= (:max-form-depth lexical)
                     (:max-reader-form-graph-depth *operations*)))
            recomputed-hashes
            (when (and graph-valid? depth-valid?)
              (invoke :c2-incremental-hashes
                      source-unit token-stream form-tree syntax-seeds
                      extension-invocations diagnostics))
            literal-records (invoke :c2-literal-records form-tree)
            deferred-records (invoke :c2-deferred-semantic-literals form-tree)
            forms-by-id (into {} (map (juxt :form-id identity) form-tree))
            canonical-form-ids
            (loop [pending (vec (reverse top-level-form-ids))
                   ordered []
                   seen #{}]
              (if-let [form-id (peek pending)]
                (let [remaining (pop pending)
                      form (forms-by-id form-id)]
                  (if (or (contains? seen form-id) (nil? form))
                    (recur remaining ordered seen)
                    (recur (into remaining (reverse (:children form)))
                           (conj ordered form-id)
                           (conj seen form-id))))
                ordered))
            deferred-descriptors-valid?
            (every?
             (fn [form]
               (let [ratio-form
                     (cond
                       (= :ratio (:kind form)) form
                       (= :metadata-wrapper (:kind form))
                       (let [ratios
                             (filterv #(= :ratio (:kind %))
                                      (keep forms-by-id (:children form)))]
                         (when (= 1 (count ratios)) (first ratios)))
                       :else nil)]
                 (if (and ratio-form
                          (= :gravity/deferred-ratio-literal
                             (get-in ratio-form [:value :artifact])))
                   (let [expected
                         (invoke :c3-deferred-ratio-descriptor-from-raw
                                 (:raw ratio-form))]
                     (and expected
                          (= expected (:value ratio-form))
                          (if (= :metadata-wrapper (:kind form))
                            (= expected (:value form) (:expanded-form form))
                            true)))
                   true)))
             form-tree)
            expected-integrity
            (when recomputed-hashes
              (invoke :c2-reader-product-integrity-record
                      source-unit top-level-form-ids recomputed-hashes
                      literal-records deferred-records))
            source-id (:source-id source-unit)
            source-path (:path source-unit)
            expected-source-map
            (mapv #(select-keys % [:syntax-id :form-id :span]) syntax-seeds)
            expected-parsed-values
            (mapv #(get-in forms-by-id [% :value]) top-level-form-ids)
            parsed-semantic-values-valid?
            (and (= expected-parsed-values
                    (:parsed-semantic-values c2-artifact))
                 (= expected-parsed-values (mapv :form syntax-seeds))
                 (= (count top-level-form-ids)
                    (count (:parsed-semantic-values c2-artifact))
                    (count syntax-seeds)))
            stable-token-ids?
            (= (mapv :token-id token-stream)
               (mapv #(keyword (str "tok-" %)) (range (count token-stream))))
            stable-form-ids?
            (and (= (mapv :form-id form-tree) canonical-form-ids)
                 (= canonical-form-ids
                    (mapv #(keyword (str "form-" %))
                          (range (count form-tree)))))
            stable-seed-ids?
            (and (= (mapv :syntax-id syntax-seeds)
                    (mapv #(str "stage0-syntax-" %)
                          (range (count syntax-seeds))))
                 (= (mapv :form-id syntax-seeds) (vec top-level-form-ids))
                 (= (mapv #(get-in % [:span :form-index]) syntax-seeds)
                    (vec (range (count syntax-seeds)))))
            stable-literal-ids?
            (= (mapv :literal-id (:literal-decoding-records c2-artifact))
               (mapv #(keyword (str "lit-" %))
                     (range (count (:literal-decoding-records c2-artifact)))))
            source-id-valid?
            (= source-id
               (invoke :reader-canonical-hash (:identity-inputs source-unit)))
            source-bytes-valid?
            (and source-text
                 (= (:bytes-hash source-unit)
                    (str "sha256:" (invoke :sha256-hex source-text))))
            product-provenance-valid?
            (and
             (every? #(and (= source-id (:source-id %))
                           (= source-id (get-in % [:span :file]))
                           (= source-path (:source-path %))
                           (= source-path (get-in % [:span :source])))
                     token-stream)
             (every? #(and (= source-id (:source-id %))
                           (= source-id (get-in % [:span :file]))
                           (= source-path (:source-path %))
                           (= source-path (get-in % [:span :source]))
                           (= source-id (get-in % [:origin :source-id]))
                           (= source-path (get-in % [:origin :source-path])))
                     form-tree)
             (every? #(and (:form-id %)
                           (= source-id (get-in % [:span :file]))
                           (= source-path (get-in % [:span :source])))
                     syntax-seeds))
            checks
            {:artifact-kind-valid?
             (= :gravity/stage0-c2-reader-document-artifact
                (:kind c2-artifact))
             :artifact-id-valid?
             (= (:artifact-id c2-artifact)
                (invoke :c2-reader-artifact-id
                        (dissoc c2-artifact :artifact-id)))
             :stable-token-ids? stable-token-ids?
             :stable-form-ids? stable-form-ids?
             :stable-seed-ids? stable-seed-ids?
             :stable-literal-ids? stable-literal-ids?
             :source-id-valid? source-id-valid?
             :source-bytes-valid? (boolean source-bytes-valid?)
             :lexical-graph-valid? graph-valid?
             :reader-depth-valid? depth-valid?
             :product-provenance-valid? product-provenance-valid?
             :reader-source-map-valid?
             (= expected-source-map (:reader-source-map c2-artifact))
             :parsed-semantic-values-valid? parsed-semantic-values-valid?
             :incremental-hashes-valid?
             (= recomputed-hashes (:incremental-reader-hashes c2-artifact))
             :literal-records-valid?
             (= literal-records (:literal-decoding-records c2-artifact))
             :deferment-records-valid?
             (= deferred-records
                (get-in c2-artifact
                        [:semantic-error-deferment-record
                         :deferred-literal-records]))
             :deferment-policy-valid?
             (and (true? (get-in c2-artifact
                                 [:semantic-error-deferment-record :deferred?]))
                  (false? (get-in c2-artifact
                                  [:semantic-error-deferment-record
                                   :semantic-analysis-in-reader?])))
             :deferred-descriptors-valid? deferred-descriptors-valid?
             :integrity-record-valid?
             (= expected-integrity (:reader-product-integrity c2-artifact))}]
        (assoc checks
               :authentic? (every? true? (vals checks))
               :failures
               (vec (keep (fn [[field passed?]] (when-not passed? field))
                          checks))))
      (catch StackOverflowError _
        {:authentic? false
         :failures [:reader-depth-stack-overflow-contained?]})
      (catch Exception ex
        {:authentic? false
         :failures [:reader-product-validation-exception]
         :cause-class (.getName (class ex))}))))

(defn c3-validate-c2-reader-artifact! [source-path c2-artifact]
  (if-let [operation (current-operation :c3-validate-c2-reader-artifact!)]
    (binding [*active-operation-keys*
              (conj *active-operation-keys*
                    :c3-validate-c2-reader-artifact!)]
      (operation source-path c2-artifact))
    (let [base-report (c3-c2-reader-integrity-report c2-artifact)
          source-path-binding-valid?
          (= source-path (get-in c2-artifact [:source-unit-record :path]))
          report
          (-> base-report
              (assoc :source-path-binding-valid? source-path-binding-valid?)
              (update :failures
                      #(cond-> (vec %)
                         (not source-path-binding-valid?)
                         (conj :source-path-binding-valid?)))
              (assoc :authentic?
                     (and (:authentic? base-report)
                          source-path-binding-valid?)))]
      (when-not (:authentic? report)
        (invoke
         :c3-syntax-fail!
         "C3-FACT-STALE" source-path
         {:source-span (or (get-in c2-artifact [:form-tree 0 :span])
                           (invoke :source-span source-path 0))
          :producer :c2-reader-artifact
          :form-kind (get-in c2-artifact [:form-tree 0 :kind])}
         {:missing-fields (:failures report)
          :facts {:reader-product-integrity report}}))
      report)))
