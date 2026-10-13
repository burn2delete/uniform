
(defn p15-s23-sh07-runtime-invoke-compiler-internal-builtin
  ([runtime plan instruction args]
   (p15-s23-sh07-runtime-invoke-compiler-internal-builtin
    runtime plan instruction args nil))
  ([runtime plan instruction args public-invocation-scope]
   (let [callee (:function instruction)
         expected-arity
         (get p15-s23-sh07-declared-builtin-exact-arities callee)]
     (when-not (p15-s23-sh07-runtime-plan-authentic?
                runtime plan instruction)
       (p15-s23-sh07-runtime-internal-fail!
        plan callee :unauthenticated-compiler-provenance))
     (when-not (= expected-arity (count args))
       (p15-s23-stage2-runtime-fail-call-arity!
        "L2-BUILTIN-ARITY" plan callee args expected-arity))
     (try
       (case callee
         sh07-canonical-text-compare
         (p15-s23-sh07-canonical-text-compare (nth args 0) (nth args 1))

         sh07-declared-digest-hash
         (reader-canonical-hash (nth args 0))

         sh07-observe-utf8-byte-count
         (p15-s23-sh07-observe-utf8-byte-count
          (nth args 0) (nth args 1) (nth args 2))

         sh07-b51-admitted-executable-carrier-validation?
         (p15-s23-sh07-b51-consume-admitted-carrier-validation!
          runtime plan instruction (nth args 0) public-invocation-scope)

         (p15-s23-sh07-runtime-internal-fail!
          plan callee :unknown-internal-builtin))
       (catch clojure.lang.ExceptionInfo error
         (if (= "L2-BUILTIN-ERROR" (:id (ex-data error)))
           (throw error)
           (p15-s23-sh07-runtime-internal-fail!
            plan callee :builtin-contract-rejection)))
       (catch Exception _
         (p15-s23-sh07-runtime-internal-fail!
          plan callee :builtin-contract-rejection))))))
