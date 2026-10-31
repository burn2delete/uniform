(require '[gravity.build.reader-bridge :as declared-reader-bridge])
(declare ^:private p15-s23-stage2-native-carrier-header)
(declare ^:private p15-s23-stage2-hosted-native-data)

(defn- p15-s23-stage2-bootstrap-native-identical?
  [plan left right]
  (when-not (and (true? (:compiler-artifact-plan? plan))
                 (= :gravity/stage2-compiler-artifact-plan (:kind plan))
                 (= :meta (get-in plan [:module :profile]))
                 (= :jvm (get-in plan [:module :target])))
    (throw (IllegalArgumentException.
            "bootstrap-native-identical? requires a compiler-artifact meta/JVM plan")))
  (identical? left right))

(defn- p15-s23-stage2-bootstrap-symbol-utf16-unit-count
  [plan value]
  (when-not (and (true? (:compiler-artifact-plan? plan))
                 (= :gravity/stage2-compiler-artifact-plan (:kind plan))
                 (= :meta (get-in plan [:module :profile]))
                 (= :jvm (get-in plan [:module :target])))
    (throw (IllegalArgumentException.
            "bootstrap-symbol-utf16-unit-count requires a compiler-artifact meta/JVM plan")))
  (when-not (identical? clojure.lang.Symbol (class value))
    (throw (IllegalArgumentException.
            "bootstrap-symbol-utf16-unit-count requires a native Symbol")))
  (let [^clojure.lang.Symbol value value
        ^String name (.getName value)
        ^String namespace (.getNamespace value)]
    ;; Native null names render as nil unqualified, or four literal units when
    ;; qualified. Long arithmetic keeps two maximal String lengths representable.
    (+ (if (nil? name) (if (nil? namespace) 0 4) (long (.length name)))
       (if (nil? namespace) 0 (inc (long (.length namespace)))))))

(defn p15-s23-stage2-runtime-invoke-builtin
  ([plan callee args]
   (p15-s23-stage2-runtime-invoke-builtin nil plan callee args))
  ([runtime plan callee args]
   (p15-s23-stage2-runtime-invoke-builtin runtime plan callee args nil nil))
  ([runtime plan callee args instruction public-invocation-scope]
  (try
    (case callee
      build-read-declared-input
      (do (p15-s23-stage2-runtime-assert-exact-arity! plan callee args 1)
          (declared-reader-bridge/read! runtime plan instruction args public-invocation-scope))
      hosted-native-data-preflight
      (do (p15-s23-stage2-runtime-assert-exact-arity! plan callee args 1)
          (p15-s23-stage2-hosted-native-data plan (nth args 0)))
      bootstrap-native-identical?
      (do (p15-s23-stage2-runtime-assert-exact-arity! plan callee args 2)
          (p15-s23-stage2-bootstrap-native-identical? plan (nth args 0) (nth args 1)))
      bootstrap-native-carrier-header
      (do (p15-s23-stage2-runtime-assert-exact-arity! plan callee args 1)
          (p15-s23-stage2-native-carrier-header plan (nth args 0)))
      bootstrap-symbol-utf16-unit-count
      (do (p15-s23-stage2-runtime-assert-exact-arity! plan callee args 1)
          (p15-s23-stage2-bootstrap-symbol-utf16-unit-count plan (nth args 0)))
      + (p15-s23-stage2-runtime-add args)
      - (do (p15-s23-stage2-runtime-assert-min-arity!
             plan callee args 1)
            (p15-s23-stage2-runtime-subtract args))
      * (p15-s23-stage2-runtime-multiply args)
      / (do (p15-s23-stage2-runtime-assert-min-arity!
             plan callee args 1)
            (p15-s23-stage2-runtime-divide args))
      = (do (p15-s23-stage2-runtime-assert-min-arity!
             plan callee args 1)
            (p15-s23-stage2-runtime-equal args))
      < (do (p15-s23-stage2-runtime-assert-min-arity!
             plan callee args 2)
            (p15-s23-stage2-runtime-less-than args))
      > (do (p15-s23-stage2-runtime-assert-min-arity!
             plan callee args 2)
            (p15-s23-stage2-runtime-greater-than args))
      <= (do (p15-s23-stage2-runtime-assert-min-arity!
              plan callee args 2)
             (p15-s23-stage2-runtime-less-than-or-equal args))
      >= (do (p15-s23-stage2-runtime-assert-min-arity!
              plan callee args 2)
             (p15-s23-stage2-runtime-greater-than-or-equal args))
      str (p15-s23-stage2-runtime-str args)
      pr-str (p15-s23-seed-readable-pr-str
              (get-in plan [:source :path]) args)
      hash-map (do (p15-s23-stage2-runtime-assert-even-arity!
                    plan callee args)
                   (apply hash-map args))
      vector (vec args)
      list (apply list args)
      conj (do (p15-s23-stage2-runtime-assert-min-arity!
                plan callee args 1)
               (apply conj args))
      assoc (do
              (p15-s23-stage2-runtime-assert-min-arity!
               plan callee args 3)
              (when (even? (count args))
                (p15-s23-stage2-runtime-fail-call-arity!
                 "L2-BUILTIN-ARITY" plan callee args
                 "a collection followed by key/value pairs"))
              (apply assoc args))
      get (do (p15-s23-stage2-runtime-assert-between-arity!
               plan callee args 2 3)
              ;; `get` dominates checked-core execution. Avoid `apply` here:
              ;; it turns the already-materialized argument vector into a seq
              ;; and performs generic bounded-arity dispatch on every lookup.
              (case (count args)
                2 (get (nth args 0) (nth args 1))
                3 (get (nth args 0) (nth args 1) (nth args 2))))
      first (do (p15-s23-stage2-runtime-assert-exact-arity!
                 plan callee args 1)
                (first (nth args 0)))
      second (do (p15-s23-stage2-runtime-assert-exact-arity!
                  plan callee args 1)
                 (second (nth args 0)))
      rest (do (p15-s23-stage2-runtime-assert-exact-arity!
                plan callee args 1)
               (p15-s23-seed-readable-normalized-rest (nth args 0)))
      count (do (p15-s23-stage2-runtime-assert-exact-arity!
                 plan callee args 1)
                (count (nth args 0)))
      symbol? (do
                (p15-s23-stage2-runtime-assert-exact-arity!
                 plan callee args 1)
                (symbol? (nth args 0)))
      keyword? (do
                 (p15-s23-stage2-runtime-assert-exact-arity!
                  plan callee args 1)
                 (keyword? (nth args 0)))
      char? (do
              (p15-s23-stage2-runtime-assert-exact-arity!
               plan callee args 1)
              (char? (nth args 0)))
      number? (do
                (p15-s23-stage2-runtime-assert-exact-arity!
                 plan callee args 1)
                (number? (nth args 0)))
      seq? (do
             (p15-s23-stage2-runtime-assert-exact-arity!
              plan callee args 1)
             (seq? (nth args 0)))
      list? (do
              (p15-s23-stage2-runtime-assert-exact-arity!
               plan callee args 1)
              (list? (nth args 0)))
      vector? (do
                (p15-s23-stage2-runtime-assert-exact-arity!
                 plan callee args 1)
                (vector? (nth args 0)))
      map? (do
             (p15-s23-stage2-runtime-assert-exact-arity!
              plan callee args 1)
             (map? (nth args 0)))
      set? (do
             (p15-s23-stage2-runtime-assert-exact-arity!
              plan callee args 1)
             (set? (nth args 0)))
      string? (do
                (p15-s23-stage2-runtime-assert-exact-arity!
                 plan callee args 1)
                (string? (nth args 0)))
      contains? (do
                  (p15-s23-stage2-runtime-assert-exact-arity!
                   plan callee args 2)
                  (contains? (nth args 0) (nth args 1)))
      even? (do
              (p15-s23-stage2-runtime-assert-exact-arity!
               plan callee args 1)
              (even? (nth args 0)))
      integer? (do
                 (p15-s23-stage2-runtime-assert-exact-arity!
                  plan callee args 1)
                 (integer? (nth args 0)))
      boolean? (do
                 (p15-s23-stage2-runtime-assert-exact-arity!
                  plan callee args 1)
                 (boolean? (nth args 0)))
      keys (do
             (p15-s23-stage2-runtime-assert-exact-arity!
              plan callee args 1)
             (apply list (keys (nth args 0))))
      set (do
            (p15-s23-stage2-runtime-assert-exact-arity!
             plan callee args 1)
            (set (nth args 0)))
      sort-by-pr-str (do
                       (p15-s23-stage2-runtime-assert-exact-arity!
                        plan callee args 1)
                       (apply list (sort-by pr-str (nth args 0))))
      vec (do
            (p15-s23-stage2-runtime-assert-exact-arity!
             plan callee args 1)
            (into [] (nth args 0)))
      quot (do
             (p15-s23-stage2-runtime-assert-exact-arity!
              plan callee args 2)
             (quot (nth args 0) (nth args 1)))
      subvec (do
               (p15-s23-stage2-runtime-assert-exact-arity!
                plan callee args 3)
               (into []
                     (subvec (nth args 0) (nth args 1) (nth args 2)))))
    (catch clojure.lang.ExceptionInfo ex
      (throw ex))
    (catch Exception ex
      (p15-s23-stage2-runtime-fail-builtin-error! plan callee ex)))))

(declare p15-s23-stage2-runtime-execute-instruction)
(declare p15-s23-stage2-runtime-execute-function)
(declare p15-s23-stage2-runtime-artifact-invoke)
(declare p15-s23-stage2-runtime-artifact-function)
(declare p15-s23-stage2-runtime-artifact-concat-function)
(declare p15-s23-stage2-runtime-artifact-println-function)
(declare p15-s23-stage2-runtime-artifact-println-two-function)
