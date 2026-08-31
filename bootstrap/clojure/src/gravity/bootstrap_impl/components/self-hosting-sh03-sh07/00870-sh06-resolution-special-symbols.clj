

;; SH-06 normalizes the verified SH-05 syntax product into the exact bounded
;; request accepted by gravity.resolution.  This remains coordinator-owned
;; transport code: resolution policy and result construction live in Gravity.
(def sh06-resolution-special-symbols
  '#{quote if do let loop recur fn def defn defconst var set! try catch
     finally throw match new syntax-quote unquote splice-unquote unsafe})

(def sh06-resolution-core-symbols
  '#{= not not= identical? < > <= >= + - * / mod quot rem inc dec min max
     zero? pos? neg? compare
     get get-in assoc assoc-in update update-in dissoc select-keys merge
     merge-with conj disj into empty count first second last rest next nth
     peek pop take drop take-while drop-while subvec vec vector list hash-map
     set sorted-map sorted-set keys vals contains? find
     nil? some? true? false? boolean? symbol? keyword? string? char? number?
     integer? ratio? map? vector? set? seq? coll? sequential? empty? seq
     map mapv map-indexed filter filterv remove keep keep-indexed reduce
     reduce-kv some every? not-any? concat mapcat partition partition-all
     range repeat repeatedly iterate zipmap frequencies group-by sort sort-by
     sort-by-pr-str distinct reverse identity constantly comp complement
     partial juxt apply
     name namespace symbol keyword str subs pr-str println format hash
     bit-and bit-or bit-xor bit-not bit-shift-left bit-shift-right
     numerator denominator meta with-meta vary-meta atom deref reset! swap!
     volatile! vreset! vswap! ex-info ex-data
     read-string slurp spit load-file resolve ns-resolve
     agent send send-off await promise deliver future future-call
     realized? delay force time rand rand-int
     transient persistent! conj! assoc! dissoc! pop!
     bigint bigdec double float long int short byte boolean
     even? odd? abs gcd lcm
     starts-with? ends-with? includes? split join replace trim lower-case
     upper-case blank?
     uuid random-uuid inst? uuid? tagged-literal
     type class instance? satisfies? extends?})

(def sh06-resolution-type-symbols
  '#{I8 I16 I32 I64 U8 U16 U32 U64 F32 F64 Bool String Symbol Keyword
     Dynamic Unit Never Any Object Class Throwable Exception RuntimeException
     Map Vector Set List Seq Fn Ratio BigInt BigDecimal})

(def sh06-resolution-all-core-symbols
  (set/union sh06-resolution-special-symbols
             sh06-resolution-core-symbols
             sh06-resolution-type-symbols))

(defn sh06-resolution-semantic-span
  [span ordinal]
  (merge
   {:ordinal ordinal}
   (select-keys (or span {})
                [:byte-start :byte-end :line-start :column-start
                 :line-end :column-end :scalar-start :scalar-end])))

(defn sh06-resolution-source-revision-id
  [sh05-artifact]
  (or (get-in sh05-artifact
              [:gravity-macro-boundary :authenticated-sh04-artifact
               :gravity-syntax-boundary :reader-source-revision :revision-id])
      (get-in sh05-artifact
              [:gravity-macro-boundary :authenticated-sh04-artifact
               :c2-reader-artifact :source-unit-record :source-id])
      (reader-canonical-hash
       {:domain :gravity/sh06-upstream-source-revision-v1
        :syntax-stream-id (:expanded-syntax-stream-id sh05-artifact)
        :macro-trace-id (:macro-expansion-trace-id sh05-artifact)})))

(defn sh06-resolution-envelope-id
  [sh05-artifact]
  (or (get-in sh05-artifact
              [:gravity-macro-boundary :authenticated-envelope
               :semantic-envelope-id])
      (reader-canonical-hash
       {:domain :gravity/sh06-upstream-envelope-reference-v1
        :artifact-id (:artifact-id sh05-artifact)
        :syntax-stream-id (:expanded-syntax-stream-id sh05-artifact)
        :macro-trace-id (:macro-expansion-trace-id sh05-artifact)})))

(defn sh06-resolution-package
  [namespace]
  {:name (symbol (str namespace)) :version "workspace"})

(defn sh06-resolution-definition-kind
  [form]
  (let [operator (when (seq? form) (first form))
        value (when (and (seq? form) (> (count form) 2)) (nth form 2))]
    (case operator
      defconst :compile-time-constant
      defn :function
      defmacro :macro
      defschema :schema
      defprotocol :protocol
      def (if (and (seq? value) (= 'fn (first value))) :function :var)
      nil)))

(defn sh06-resolution-definition-records
  [module sh05-artifact]
  (let [exports (set (:exports module))
        module-namespace (:module module)
        artifact-id (:artifact-id sh05-artifact)]
    (->> (:expanded-syntax-stream sh05-artifact)
         (map-indexed
          (fn [ordinal syntax]
            (let [form (:form syntax)
                  kind (sh06-resolution-definition-kind form)
                  declared-name (when kind (second form))]
              (when (and kind (symbol? declared-name))
                (let [simple-name (clojure.core/symbol (name declared-name))
                      declared-namespace
                      (some-> (namespace declared-name) clojure.core/symbol)
                      binding-namespace
                      (or declared-namespace module-namespace)]
                 {:name simple-name
                 :kind kind
                 :namespace binding-namespace
                 :package (sh06-resolution-package binding-namespace)
                 :binding-class :namespace
                 :visibility (if (or (contains? exports declared-name)
                                     (contains? exports simple-name))
                               :public
                               :private)
                 :profile-set [(:profile module)]
                 :target-set [(:target module)]
                 :type-ref (case kind
                             :function :gravity.type/function
                             :macro :gravity.syntax/macro
                             :schema :gravity.type/schema
                             :protocol :gravity.type/protocol
                             :gravity.type/value)
                 :effects (vec (sort (:effects module)))
                 :capabilities (vec (sort (:capabilities module)))
                 :safety (:safety module)
                 :semantic-span
                 (sh06-resolution-semantic-span (:span syntax) ordinal)
                 :source-span (:span syntax)
                 :definition-syntax-id (:syntax/id syntax)
                 :definition-artifact-id artifact-id})))))
         (remove nil?)
         vec)))

(defn sh06-resolution-core-record
  [symbol kind ordinal]
  {:name symbol
   :kind kind
   :namespace 'gravity.core
   :package {:name 'gravity/core :version "bootstrap-contract-v1"}
   :binding-class :core
   :visibility :public
   :profile-set (vec (sort known-source-profiles))
   :target-set [:all]
   :type-ref (case kind
               :special-form :gravity.syntax/special-form
               :type :gravity.type/type
               :gravity.type/core-var)
   :effects (if (= symbol 'println) [:io/write] [])
   :capabilities (if (= symbol 'println) [:io/stdout] [])
   :safety :safe
   :semantic-span {:catalog :gravity/core :ordinal ordinal}
   :source-span {:source "gravity.core" :form-index ordinal}
   :definition-syntax-id
   (reader-canonical-hash
    {:domain :gravity/sh06-core-binding-syntax-v1
     :name symbol :kind kind})
   :definition-artifact-id
   (reader-canonical-hash
    {:domain :gravity/sh06-core-catalog-v1
     :catalog-version 1})})

(defn sh06-resolution-core-records
  []
  (mapv
   (fn [ordinal symbol]
     (sh06-resolution-core-record
      symbol
      (cond
        (contains? sh06-resolution-special-symbols symbol) :special-form
        (contains? sh06-resolution-type-symbols symbol) :type
        :else :var)
      ordinal))
   (range)
   (sort sh06-resolution-all-core-symbols)))

(defn sh06-resolution-request-overrides
  [module]
  (let [candidate (get-in module [:metadata :compiler :sh06-request])]
    (if (map? candidate) candidate {})))

(defn sh06-resolution-candidate-for
  [overrides namespace name]
  (first
   (filter
    #(and (= namespace (:namespace %)) (= name (:name %)))
    (:candidate-bindings overrides))))