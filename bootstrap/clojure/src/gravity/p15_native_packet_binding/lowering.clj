(ns gravity.p15-native-packet-binding.lowering
  (:require [clojure.string :as str]))

(defn checked-rendered-text
  [source-path value scalar-kind plan-fail! bounds-fail!]
  (let [kind (scalar-kind value)]
    (when-not kind
      (plan-fail! source-path "native provider supports only scalar values"
                  {:observed-value-type (some-> value class .getName)
                   :missing-fact :bounded-native-scalar}))
    (when (and (= :integer kind)
               (or (< (bigint value) (bigint Long/MIN_VALUE))
                   (> (bigint value) (bigint Long/MAX_VALUE))))
      (bounds-fail! source-path "integer exceeds native signed 64-bit bound"
                    {:observed-value (str value)
                     :maximum Long/MAX_VALUE
                     :minimum Long/MIN_VALUE
                     :missing-fact :signed-64-bit-integer}))
    (if (nil? value) "nil" (str value))))

(defn push-scalar
  [source-path value
   {:keys [scalar-kind checked-rendered-text utf8-bytes hex-encode value-limit
           bounds-fail!]}]
  (let [kind (scalar-kind value)
        rendered (checked-rendered-text source-path value)
        rendered-bytes (utf8-bytes rendered)]
    (when (> (alength rendered-bytes) value-limit)
      (bounds-fail! source-path "scalar exceeds native value byte bound"
                    {:observed-value-bytes (alength rendered-bytes)
                     :maximum-value-bytes value-limit
                     :missing-fact :bounded-native-value}))
    {:instructions
     [(case kind
        :nil "push-nil"
        :bool (str "push-bool " rendered)
        :integer (str "push-int " rendered)
        :string (str "push-string " (hex-encode rendered-bytes)))]
     :value value
     :rendered rendered
     :stack-delta 1
     :maximum-relative-depth 1
     :stdout ""}))

(defn combine-evaluations [evaluations]
  (loop [remaining (seq evaluations)
         instructions []
         stdout ""
         depth 0
         maximum-depth 0]
    (if-let [evaluation (first remaining)]
      (let [next-depth (+ depth (:stack-delta evaluation))]
        (recur (next remaining)
               (into instructions (:instructions evaluation))
               (str stdout (:stdout evaluation))
               next-depth
               (max maximum-depth
                    (+ depth (:maximum-relative-depth evaluation)))))
      {:instructions instructions
       :stdout stdout
       :stack-delta depth
       :maximum-relative-depth maximum-depth})))

(defn lower-call
  [source-path instruction call-kind
   {:keys [lower-expression combine-evaluations utf8-bytes value-limit
           plan-fail! bounds-fail!]}]
  (let [function (if (= call-kind :println)
                   'println
                   (:function instruction))
        args (:args instruction)]
    (when-not (and (vector? args) (<= 1 (count args) 2))
      (plan-fail! source-path "native builtin arity must be one or two"
                  {:unsupported-op call-kind
                   :observed-function function
                   :observed-arity (when (sequential? args) (count args))
                   :supported-arities [1 2]
                   :missing-fact :bounded-native-builtin-arity}))
    (when-not (or (= 'str function) (= 'println function))
      (plan-fail! source-path "native packet plan contains an unsupported call"
                  {:unsupported-op call-kind
                   :observed-function function
                   :supported-functions ['str 'println]
                   :missing-fact :bounded-native-builtin}))
    (let [lowered-args (mapv #(lower-expression source-path %) args)
          combined (combine-evaluations lowered-args)
          rendered-values (mapv :rendered lowered-args)
          result-text (if (= 'str function)
                        (apply str rendered-values)
                        "nil")
          result-bytes (utf8-bytes result-text)
          emitted (if (= 'println function)
                    (str (str/join " " rendered-values) "\n")
                    "")]
      (when (> (alength result-bytes) value-limit)
        (bounds-fail! source-path "builtin result exceeds native value bound"
                      {:observed-value-bytes (alength result-bytes)
                       :maximum-value-bytes value-limit
                       :unsupported-op call-kind
                       :missing-fact :bounded-native-value}))
      (assoc combined
             :instructions (conj (:instructions combined)
                                 (str (name function) " " (count args)))
             :value (if (= 'str function) result-text nil)
             :rendered result-text
             :stdout (str (:stdout combined) emitted)
             :stack-delta (+ (:stack-delta combined) 1 (- (count args)))
             :maximum-relative-depth
             (max (:maximum-relative-depth combined)
                  (:stack-delta combined))))))

(defn lower-expression
  [source-path instruction {:keys [push-scalar lower-call plan-fail!]}]
  (when-not (map? instruction)
    (plan-fail! source-path "native packet instruction is malformed"
                {:observed-instruction instruction
                 :missing-fact :native-instruction-map}))
  (case (:op instruction)
    :literal (push-scalar source-path (:value instruction))
    :quote (push-scalar source-path (:value instruction))
    :builtin-call (lower-call source-path instruction :builtin-call)
    :println (lower-call source-path instruction :println)
    (plan-fail! source-path
                "native packet plan contains an unsupported operation"
                {:unsupported-op (:op instruction)
                 :supported-operations [:literal :quote :builtin-call
                                        :println]
                 :missing-fact :bounded-native-plan-operation})))

(defn validate-entrypoint!
  [source-path plan lower-expression combine-evaluations plan-fail!]
  (let [entrypoint (:entrypoint plan)
        functions (:functions plan)
        function (when (map? functions) (get functions entrypoint))]
    (when-not (and (= :gravity/stage2-hosted-core-compiled-plan (:kind plan))
                   (symbol? entrypoint)
                   (map? function)
                   (zero? (:arity function))
                   (empty? (:params function))
                   (vector? (:instructions function))
                   (= 1 (count functions)))
      (plan-fail! source-path "native packet requires one zero-arity entrypoint"
                  {:observed-plan-kind (:kind plan)
                   :observed-entrypoint entrypoint
                   :observed-function-count
                   (when (map? functions) (count functions))
                   :observed-entrypoint-arity (:arity function)
                   :missing-fact :single-zero-arity-native-entrypoint}))
    (let [lowered (combine-evaluations
                   (mapv #(lower-expression source-path %)
                         (:instructions function)))]
      (assoc lowered :entrypoint entrypoint))))

(defn validate-authority! [source-path plan lowered plan-fail!]
  (let [println-count (count (filter #(str/starts-with? % "println ")
                                     (:instructions lowered)))
        str-count (count (filter #(str/starts-with? % "str ")
                                 (:instructions lowered)))
        declared-effects (set (get-in plan [:effect-summary :declared] #{}))
        inferred-effects (set (get-in plan [:effect-summary :inferred] #{}))
        capabilities (set (get-in plan [:module :capabilities] #{}))
        required-effects (cond-> #{}
                           (pos? str-count) (conj :memory/allocate)
                           (pos? println-count) (conj :io/write))
        required-capabilities (cond-> #{}
                                (pos? str-count) (conj :memory/allocator)
                                (pos? println-count) (conj :io/stdout))
        required-inferred-effects (cond-> #{}
                                    (pos? println-count) (conj :io/write))]
    (when-not (and (every? declared-effects required-effects)
                   (every? inferred-effects required-inferred-effects)
                   (every? capabilities required-capabilities))
      (plan-fail! source-path
                  "native lowering lacks effect or capability authority"
                  {:declared-effects declared-effects
                   :inferred-effects inferred-effects
                   :declared-capabilities capabilities
                   :required-effects required-effects
                   :required-inferred-effects required-inferred-effects
                   :required-capabilities required-capabilities
                   :missing-fact :native-effect-capability-authority}))
    {:declared-effects declared-effects
     :inferred-effects inferred-effects
     :declared-capabilities capabilities
     :required-effects required-effects
     :required-inferred-effects required-inferred-effects
     :required-capabilities required-capabilities}))
