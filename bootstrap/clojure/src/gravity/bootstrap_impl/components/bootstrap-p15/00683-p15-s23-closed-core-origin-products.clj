

(defn p15-s23-closed-core-origin-products
  [source-path source-content-hash plan-path form-record syntax
   expanded-syntax indexes token-ordinal-by-id generated-role]
  (let [top-level-form-id
        (when (map? form-record)
          (p15-s23-closed-core-top-level-form-id
           (:form-id form-record) (:parent-by-id indexes)))]
  (when-not (and (map? form-record)
                 (map? syntax)
                 (string? (get syntax :syntax/id))
                 (= top-level-form-id (get-in syntax [:source :form-id])))
    (p15-s23-closed-core-fail!
     "C6-ORIGIN" source-path form-record
     {:missing-fact :genuine-c2-c3-origin-closure
      :plan-path plan-path}))
  (when (seq (:metadata form-record))
    (p15-s23-closed-core-fail!
     "C6-LOWERING-GAP" source-path
     (assoc form-record
            :syntax-id (get syntax :syntax/id)
            :c2-form-id (:form-id form-record)
            :source-span (:span form-record)
            :generated-origin
            (vec (concat (or (:origin syntax) [])
                         (or (:generated-origin form-record) [])
                         (or (:generated-origin expanded-syntax) [])))
            :lowering-rule :pure-closed-core-metadata-exclusion
            :profile :hosted
            :target :jvm)
     {:missing-fact :metadata-preserving-pure-core-lowering
      :plan-path plan-path
      :active-profile :hosted
      :target :jvm
      :target-neutral-request? true}))
  (let [form-id (:form-id form-record)
        form-ordinal (get-in indexes [:ordinal-by-id form-id])
        structural-path (get-in indexes [:structural-path-by-id form-id])
        syntax-input (p15-s23-closed-core-syntax-semantic-input syntax)
        semantic-syntax-id (p15-s23-closed-core-digest syntax-input)
        reader-generated-origin
        (mapv p15-s23-closed-core-path-neutral-generated-origin
              (:generated-origin form-record))
        enclosing-c3-origin
        (mapv p15-s23-closed-core-path-neutral-generated-origin
              (:origin syntax))
        enclosing-generated-origin
        (mapv p15-s23-closed-core-path-neutral-generated-origin
              (:generated-origin expanded-syntax))
        semantic-base
        {:source-content-hash source-content-hash
         :plan-path plan-path
         :form-ordinal form-ordinal
         :form-structural-path structural-path
         :form-kind (:kind form-record)
         :form-raw-hash (str "sha256:" (sha256-hex (:raw form-record)))
         :span (p15-s23-closed-core-path-neutral-span (:span form-record))
         :token-window
         [(get token-ordinal-by-id (:open-token form-record))
          (get token-ordinal-by-id (:close-token form-record))]
         :enclosing-syntax-origin-id semantic-syntax-id
         :generated-role generated-role
         ;; Reader desugaring and enclosing C3/macro provenance are distinct
         ;; producers.  Keeping their chains separate prevents abbreviated
         ;; quote/metadata forms from collapsing into an explicit surface form
         ;; that happens to have the same runtime value.
         :reader-generated-origin reader-generated-origin
         :enclosing-c3-origin enclosing-c3-origin
         :enclosing-generated-origin enclosing-generated-origin}
        origin-id (p15-s23-closed-core-digest semantic-base)
        semantic (assoc semantic-base :origin-id origin-id)
        raw
        (p15-s23-closed-core-bind-raw-provenance
         {:origin-id origin-id
          :actual-source-path source-path
          :c2-source-id (:source-id form-record)
          :c2-form-id form-id
          :c2-open-token-id (:open-token form-record)
          :c2-close-token-id (:close-token form-record)
          :c2-span (:span form-record)
          :c2-surface-span (:surface-span form-record)
          :c2-form-kind (:kind form-record)
          :c2-abbrev (:abbrev form-record)
          :c2-reader-generated-origin
          (vec (or (:generated-origin form-record) []))
          :c3-syntax-id (:syntax/id syntax)
          :c3-source (:source syntax)
          :c3-origin (:origin syntax)
          :expanded-generated-origin (:generated-origin expanded-syntax)
          :generated-role generated-role
          :input-origin-id nil})]
    {:semantic semantic
     :raw raw
     :source {:origin-id origin-id
              :span (:span semantic)
              :enclosing-syntax-origin-id semantic-syntax-id
              :generated? (boolean (or generated-role
                                       (seq reader-generated-origin)
                                       (seq enclosing-c3-origin)
                                       (seq enclosing-generated-origin)))}})))

(defn p15-s23-closed-core-generated-origin-products
  [source-path source-content-hash plan-path base-products generated-role]
  (let [base-semantic (:semantic base-products)
        semantic-base
        {:source-content-hash source-content-hash
         :plan-path plan-path
         :form-ordinal (:form-ordinal base-semantic)
         :form-structural-path (:form-structural-path base-semantic)
         :form-kind :compiler-generated
         :form-raw-hash (:form-raw-hash base-semantic)
         :span (:span base-semantic)
         :token-window (:token-window base-semantic)
         :enclosing-syntax-origin-id
         (:enclosing-syntax-origin-id base-semantic)
         :generated-role generated-role
         :generated-origin
         [{:kind :generated
           :producer {:kind :compiler-pass
                      :name 'gravity.compiler/c6-closed-core-lowering
                      :version "p15-s23-closed-v1"}
           :inputs [(:origin-id base-semantic)]
           :reason generated-role
           :build-effects []}]}
        origin-id (p15-s23-closed-core-digest semantic-base)
        semantic (assoc semantic-base :origin-id origin-id)
        raw
        (p15-s23-closed-core-bind-raw-provenance
         (assoc (dissoc (:raw base-products)
                        :provenance-binding-hash
                        :actual-path-binding-hash)
                :origin-id origin-id
                :generated-role generated-role
                :input-origin-id (:origin-id base-semantic)))]
    {:semantic semantic
     :raw raw
     :source {:origin-id origin-id
              :span (:span semantic)
              :enclosing-syntax-origin-id
              (:enclosing-syntax-origin-id semantic)
              :generated? true}}))

(defn p15-s23-closed-core-scalar-literal-type
  [value]
  (cond
    (nil? value) :gravity/nil
    (string? value) :gravity/string
    (boolean? value) :gravity/bool
    (integer? value) :gravity/integer
    (char? value) :gravity/char
    (keyword? value) :gravity/keyword
    (symbol? value) :gravity/symbol
    :else nil))

(def p15-s23-closed-core-quoted-scalar-type
  {:kind :gravity/union
   :members [:gravity/bool :gravity/char :gravity/integer :gravity/keyword
             :gravity/nil :gravity/string :gravity/symbol]})

(defn p15-s23-closed-core-quoted-value-type
  [value]
  (or
   (p15-s23-closed-core-scalar-literal-type value)
   (cond
     (and (map? value)
          (= :gravity/deferred-ratio-literal (:artifact value)))
     :gravity/deferred-ratio
     (ratio? value) :gravity/ratio
     (number? value) :gravity/noninteger-number
     (seq? value) :gravity/list
     (vector? value) :gravity/vector
     (map? value) :gravity/map
     (set? value) :gravity/set
     :else :gravity/unsupported-quoted-value)))

(defn p15-s23-closed-core-first-c7-source-violation
  [executable-form-records]
  (first
   (keep
    (fn [record]
      (let [value (:value record)
            operator (when (seq? value) (first value))]
        (cond
          (and (= 'quote operator)
               (= 2 (count value))
               (nil? (p15-s23-closed-core-scalar-literal-type
                      (second value))))
          {:kind :quoted-value :record record}

          (or (and (number? value) (not (integer? value)))
              (and (map? value)
                   (= :gravity/deferred-ratio-literal (:artifact value))))
          {:kind :numeric-literal :record record}

          (and (= 'str operator)
               (not (contains? #{2 3} (count value))))
          {:kind :str-arity :record record}

          :else nil)))
    executable-form-records)))

(defn p15-s23-closed-core-literal-type
  ([source-path value]
   (p15-s23-closed-core-literal-type
    source-path value {:missing-fact :closed-scalar-literal-type}))
  ([source-path value subject]
   (or
    (p15-s23-closed-core-scalar-literal-type value)
    (p15-s23-closed-core-fail!
     "C7-TYPE-MISMATCH" source-path subject
     {:missing-fact :closed-scalar-literal-type
      :observed-value value
      :observed-class (some-> value class .getName)
      :expected-type :closed-scalar-literal
      :actual-type (or (some-> value class .getName) :nil)
      :relevant-binding-id :not-applicable
      :excluded-values [:non-integer-number :collection
                        :opaque-host-value]}))))

(defn p15-s23-closed-core-type-join
  [left right]
  (let [members (vec (sort-by
                      pr-str
                      (set (concat
                            (if (and (map? left)
                                     (= :gravity/union (:kind left)))
                              (:members left)
                              [left])
                            (if (and (map? right)
                                     (= :gravity/union (:kind right)))
                              (:members right)
                              [right])))))]
    (if (= 1 (count members))
      (first members)
      {:kind :gravity/union :members members})))