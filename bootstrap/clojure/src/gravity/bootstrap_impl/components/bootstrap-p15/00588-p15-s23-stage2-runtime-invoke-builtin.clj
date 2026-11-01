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

(def ^:private p15-s23-runtime-builtin-operations
  {:add #'p15-s23-stage2-runtime-add
   :subtract #'p15-s23-stage2-runtime-subtract
   :multiply #'p15-s23-stage2-runtime-multiply
   :divide #'p15-s23-stage2-runtime-divide
   :equal #'p15-s23-stage2-runtime-equal
   :less-than #'p15-s23-stage2-runtime-less-than
   :greater-than #'p15-s23-stage2-runtime-greater-than
   :less-than-or-equal #'p15-s23-stage2-runtime-less-than-or-equal
   :greater-than-or-equal #'p15-s23-stage2-runtime-greater-than-or-equal
   :str #'p15-s23-stage2-runtime-str
   :assert-min-arity! #'p15-s23-stage2-runtime-assert-min-arity!
   :assert-exact-arity! #'p15-s23-stage2-runtime-assert-exact-arity!
   :assert-even-arity! #'p15-s23-stage2-runtime-assert-even-arity!
   :assert-between-arity! #'p15-s23-stage2-runtime-assert-between-arity!
   :fail-call-arity! #'p15-s23-stage2-runtime-fail-call-arity!
   :readable-pr-str #'p15-s23-seed-readable-pr-str
   :normalized-rest #'p15-s23-seed-readable-normalized-rest})

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
      (gravity.runtime/hosted-builtin plan callee args p15-s23-runtime-builtin-operations))
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
