(declare ^:private resolution-stage-operations)


;; Seed catalog configuration and compatibility entry points. Declaration and
;; core-record normalization belongs to gravity.resolution; resolution policy
;; and result construction remain in Gravity.
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
     integer? ratio? map? vector? set? list? seq? coll? sequential? empty? seq
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

(defn
 sh06-resolution-semantic-span
 [span ordinal]
 (gravity.resolution/semantic-span span ordinal))

(defn
 sh06-resolution-source-revision-id
 [sh05-artifact]
 (gravity.resolution/source-revision-id
  sh05-artifact
  (resolution-stage-operations)))

(defn
 sh06-resolution-envelope-id
 [sh05-artifact]
 (gravity.resolution/envelope-id
  sh05-artifact
  (resolution-stage-operations)))

(defn
 sh06-resolution-package
 [namespace]
 (gravity.resolution/package-record namespace))

(defn
 sh06-resolution-definition-kind
 [form]
 (gravity.resolution/definition-kind form))

(defn
 sh06-resolution-definition-records
 [module sh05-artifact]
 (gravity.resolution/definition-records
  module
  sh05-artifact
  (resolution-stage-operations)))

(defn
 sh06-resolution-core-record
 [symbol kind ordinal]
 (gravity.resolution/core-record
  symbol
  kind
  ordinal
  (resolution-stage-operations)))

(defn
 sh06-resolution-core-records
 []
 (gravity.resolution/core-records (resolution-stage-operations)))

(defn
 sh06-resolution-request-overrides
 [module]
 (gravity.resolution/request-overrides module))

(defn
 sh06-resolution-candidate-for
 [overrides namespace name]
 (gravity.resolution/candidate-for overrides namespace name))
