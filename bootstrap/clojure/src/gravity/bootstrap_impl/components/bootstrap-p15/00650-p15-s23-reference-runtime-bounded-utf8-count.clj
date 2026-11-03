(declare p15-s23-trusted-carrier-validation
         p15-s23-closed-core-utf8-count)

(defn p15-s23-reference-runtime-hash
  [value]
  (str "sha256:"
       (sha256-hex
        (pr-str (c-backend-canonical-value value)))))

(defn p15-s23-reference-runtime-fail!
  [source-path target missing-fact value extra]
  (p15-s23-stage2-runtime-executor-fail!
   "P15S23X002" source-path value
   (merge {:target target
           :missing-fact missing-fact
           :boundary :pinned-reference-runtime-contract
           :result-committed? false
           :output-committed? false}
          extra)))

(defn p15-s23-reference-runtime-value-children
  [value]
  (cond
    (map? value)
    (reduce (fn [children [key item]]
              (conj children key item))
            [] value)
    (or (vector? value) (set? value) (seq? value))
    (reduce conj [] value)
    :else []))

(defn- p15-s23-reference-runtime-carrier-missing-fact
  [reason class-name]
  (case reason
    :untrusted-scalar-class
    (if (and class-name
             (or (.endsWith ^String class-name "Seq")
                 (.endsWith ^String class-name "ChunkedCons")))
      :runtime-contract-unsupported-collection
      :runtime-contract-unsupported-scalar)
    :untrusted-vector-class :runtime-contract-unsupported-collection
    :untrusted-map-class-or-comparator :runtime-contract-unsupported-collection
    :untrusted-set-class-or-comparator :runtime-contract-unsupported-collection
    :untrusted-list-class :runtime-contract-unsupported-collection
    :untrusted-list-tail-class :runtime-contract-unsupported-collection
    :carrier-metadata :runtime-contract-carrier-metadata
    :cyclic-carrier :runtime-contract-cyclic-carrier
    :cyclic-vector-backing :runtime-contract-cyclic-carrier
    :invalid-vector-view-extent :runtime-contract-invalid-carrier
    :untrusted-vector-backing :runtime-contract-invalid-carrier
    :runtime-contract-invalid-carrier))

(defn- p15-s23-reference-runtime-validate-unicode!
  [source-path target definition-name value]
  (loop [pending [value]
         seen (java.util.IdentityHashMap.)]
    (if (empty? pending)
      :passed
      (let [item (peek pending)
            pending (pop pending)
            texts (cond
                    (string? item) [item]
                    (or (keyword? item) (symbol? item))
                    (cond-> [(name item)]
                      (namespace item) (conj (namespace item)))
                    :else [])
            invalid-text
            (some (fn [text]
                    (let [measurement
                          (p15-s23-closed-core-utf8-count text)]
                      (when-not (= :valid (:status measurement))
                        {:text-kind (cond
                                      (string? item) :string
                                      (keyword? item) :keyword
                                      :else :symbol)
                         :measurement measurement})))
                  texts)]
        (when (or invalid-text
                  (and (char? item)
                       (<= 0xD800 (int item) 0xDFFF)))
          (p15-s23-reference-runtime-fail!
           source-path target :runtime-contract-invalid-unicode nil
           (merge {:runtime-contract-definition definition-name}
                  invalid-text)))
        (if (and (or (map? item) (vector? item) (set? item) (seq? item))
                 (.containsKey seen item))
          (recur pending seen)
          (do
            (when (or (map? item) (vector? item) (set? item) (seq? item))
              (.put seen item true))
            (recur (into pending
                         (p15-s23-reference-runtime-value-children item))
                   seen)))))))

(defn p15-s23-reference-runtime-bounded-value!
  ([source-path target definition-name value]
   (let [validation (p15-s23-trusted-carrier-validation value :default-only)]
     (when-not (= :passed (:status validation))
       (p15-s23-reference-runtime-fail!
        source-path target
        (p15-s23-reference-runtime-carrier-missing-fact
         (:reason validation) (:class validation))
        nil
        (merge {:runtime-contract-definition definition-name
                :carrier-reason (:reason validation)}
               (select-keys validation [:class]))))
     (p15-s23-reference-runtime-validate-unicode!
      source-path target definition-name value)
     {:status :passed}))
  ;; Transitional callers may still supply the removed admission limits. They
  ;; are intentionally ignored; semantic carrier validation is unbounded.
  ([source-path target definition-name value _maximum-nodes _maximum-depth]
   (p15-s23-reference-runtime-bounded-value!
    source-path target definition-name value)))
