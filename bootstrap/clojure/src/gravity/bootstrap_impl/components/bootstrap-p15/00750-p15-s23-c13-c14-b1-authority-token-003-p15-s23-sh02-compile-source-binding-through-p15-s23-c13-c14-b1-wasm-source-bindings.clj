(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn- p15-s23-sh02-compile-source-binding!
  [source-path {:keys [source-text emitter-rule inputs]}]
  (when-not
   (and (= p15-s23-sh02-source-byte-count (:source-byte-count inputs))
        (= p15-s23-sh02-expected-source-content-hash
           (:source-content-hash inputs)))
    (p15-s23-b3-llvm-fail!
     "B1-INPUT" source-path {}
     {:missing-fact :pinned-sh02-source-identity
      :expected-source-bytes p15-s23-sh02-source-byte-count
      :observed-source-bytes (:source-byte-count inputs)
      :expected-source-content-hash
      p15-s23-sh02-expected-source-content-hash
      :observed-source-content-hash (:source-content-hash inputs)}))
  (let [compilation-source-path (:source-path inputs)
        plan
        (p15-s23-stage2-compiler-artifact-plan
         (:emitter emitter-rule) compilation-source-path source-text)
        functions (:functions plan)
        complete-shapes
        (into
         (sorted-map)
         (map (fn [[name function]]
                [name (select-keys function [:arity :params])]))
         functions)
        plan-hash
        (p15-s23-c11-mir-digest
         (p15-s23-stage2-compiler-artifact-semantic-input plan))
        functions-hash (p15-s23-c11-mir-digest functions)
        builder-hash
        (p15-s23-c11-mir-digest
         (get functions p15-s23-sh02-builder-function))
        verifier-hash
        (p15-s23-c11-mir-digest
         (get functions p15-s23-sh02-verifier-function))]
    (when-not
     (and (= p15-s23-sh02-expected-function-count (count functions))
          (= p15-s23-sh02-expected-plan-semantic-hash plan-hash)
          (= p15-s23-sh02-expected-functions-semantic-hash functions-hash)
          (= p15-s23-sh02-expected-builder-semantic-hash builder-hash)
          (= p15-s23-sh02-expected-verifier-semantic-hash verifier-hash)
          (= p15-s23-sh02-required-functions
             (select-keys complete-shapes
                          (keys p15-s23-sh02-required-functions))))
      (p15-s23-b3-llvm-fail!
       "B1-METADATA" source-path {}
       {:missing-fact :pinned-sh02-gravity-function-identity
        :sh02-boundary :authenticated-envelope
        :observed-function-count (count functions)
        :observed-plan-semantic-hash plan-hash
        :observed-functions-semantic-hash functions-hash
        :observed-builder-semantic-hash builder-hash
        :observed-verifier-semantic-hash verifier-hash}))
    {:owner :gravity.compiler/authenticated-envelope
     :source-path compilation-source-path
     :source-byte-count (:source-byte-count inputs)
     :source-content-hash (:source-content-hash inputs)
     :plan-semantic-hash plan-hash
     :functions-semantic-hash functions-hash
     :builder-semantic-hash builder-hash
     :verifier-function p15-s23-sh02-verifier-function
     :verifier-semantic-hash verifier-hash
     :function-shapes complete-shapes
     :plan plan}))

(defn- p15-s23-sh02-source-binding-cache-key
  [request]
  {:schema-version 2
   :owner :gravity/p15-s23-sh02-source-binding
   :source-relative-path p15-s23-sh02-source-relative-path
   :authenticated-inputs (:inputs request)
   :additional-bootstrap-targets *additional-bootstrap-targets*
   :expected-source-byte-count p15-s23-sh02-source-byte-count
   :expected-source-content-hash
   p15-s23-sh02-expected-source-content-hash
   :expected-plan-semantic-hash
   p15-s23-sh02-expected-plan-semantic-hash
   :expected-functions-semantic-hash
   p15-s23-sh02-expected-functions-semantic-hash
   :expected-builder-semantic-hash
   p15-s23-sh02-expected-builder-semantic-hash
   :expected-verifier-semantic-hash
   p15-s23-sh02-expected-verifier-semantic-hash
   :expected-function-count p15-s23-sh02-expected-function-count
   :builder-function p15-s23-sh02-builder-function
   :verifier-function p15-s23-sh02-verifier-function
   :required-functions p15-s23-sh02-required-functions})

(defn- p15-s23-sh02-cached-source-binding!
  [source-path request]
  (if-not *p15-s23-stage2-plan-emission-context*
    (p15-s23-sh02-compile-source-binding! source-path request)
    (let [key (p15-s23-sh02-source-binding-cache-key request)
          context *p15-s23-stage2-plan-emission-context*]
      (locking context
        (if-let [binding (get @context key)]
          binding
          (let [binding
                (p15-s23-sh02-compile-source-binding!
                 source-path request)]
            (swap! context assoc key binding)
            binding))))))

(defn- p15-s23-sh02-source-binding!
  [candidate source-path]
  (p15-s23-c13-c14-b1-require-authority!
   candidate source-path :load-pinned-sh02-source)
  (let [request (p15-s23-sh02-source-binding-inputs! candidate source-path)]
    ;; Every authority-bearing call reopens and authenticates both source
    ;; inputs above.  Successful compilation may be shared only between exact
    ;; authenticated inputs and validation policy in the current plan-emission
    ;; request.
    (p15-s23-sh02-cached-source-binding! source-path request)))

(defn- p15-s23-c13-c14-b1-c-source-bindings!
  [candidate source-path]
  {:c13
   (p15-s23-c13-c14-b1-source-binding!
    candidate source-path
    {:owner :gravity.compiler/c13-mir-optimization
     :relative-path p15-s23-c13-source-relative-path
     :source-byte-count p15-s23-c13-source-byte-count
     :source-content-hash p15-s23-c13-expected-source-content-hash
     :plan-semantic-hash p15-s23-c13-expected-plan-semantic-hash
     :functions-semantic-hash
     p15-s23-c13-expected-functions-semantic-hash
     :builder-semantic-hash p15-s23-c13-expected-builder-semantic-hash
     :builder-function p15-s23-c13-builder-function
     :required-functions p15-s23-c13-required-functions
     :emitter-target :c})
   :c14
   (p15-s23-c13-c14-b1-source-binding!
    candidate source-path
    {:owner :gravity.compiler/c14-target-lowering
     :relative-path p15-s23-c14-source-relative-path
     :source-byte-count p15-s23-c14-source-byte-count
     :source-content-hash p15-s23-c14-expected-source-content-hash
     :plan-semantic-hash p15-s23-c14-expected-plan-semantic-hash
     :functions-semantic-hash
     p15-s23-c14-expected-functions-semantic-hash
     :builder-semantic-hash
     p15-s23-c14-c-expected-builder-semantic-hash
     :builder-function p15-s23-c14-c-builder-function
     :required-functions p15-s23-c14-c-required-functions
     :emitter-target :c})
   :b1
   (p15-s23-c13-c14-b1-source-binding!
    candidate source-path
    {:owner :gravity.backend/b1-backend-interface
     :relative-path p15-s23-b1-source-relative-path
     :source-byte-count p15-s23-b1-source-byte-count
     :source-content-hash p15-s23-b1-expected-source-content-hash
     :plan-semantic-hash p15-s23-b1-expected-plan-semantic-hash
     :functions-semantic-hash p15-s23-b1-expected-functions-semantic-hash
     :builder-semantic-hash p15-s23-b1-c-expected-builder-semantic-hash
     :builder-function p15-s23-b1-c-builder-function
     :required-functions p15-s23-b1-c-required-functions
     :emitter-target :c})})

(defn- p15-s23-c13-c14-b1-wasm-source-bindings!
  [candidate source-path]
  {:c13
   (p15-s23-c13-c14-b1-source-binding!
    candidate source-path
    {:owner :gravity.compiler/c13-mir-optimization
     :relative-path p15-s23-c13-source-relative-path
     :source-byte-count p15-s23-c13-source-byte-count
     :source-content-hash p15-s23-c13-expected-source-content-hash
     :plan-semantic-hash p15-s23-c13-expected-plan-semantic-hash
     :functions-semantic-hash
     p15-s23-c13-expected-functions-semantic-hash
     :builder-semantic-hash p15-s23-c13-expected-builder-semantic-hash
     :builder-function p15-s23-c13-builder-function
     :required-functions p15-s23-c13-required-functions
     :emitter-target :wasm})
   :c14
   (p15-s23-c13-c14-b1-source-binding!
    candidate source-path
    {:owner :gravity.compiler/c14-target-lowering
     :relative-path p15-s23-c14-source-relative-path
     :source-byte-count p15-s23-c14-source-byte-count
     :source-content-hash p15-s23-c14-expected-source-content-hash
     :plan-semantic-hash p15-s23-c14-expected-plan-semantic-hash
     :functions-semantic-hash
     p15-s23-c14-expected-functions-semantic-hash
     :builder-semantic-hash
     p15-s23-c14-wasm-expected-builder-semantic-hash
     :builder-function p15-s23-c14-wasm-builder-function
     :required-functions p15-s23-c14-wasm-required-functions
     :emitter-target :wasm})
   :b1
   (p15-s23-c13-c14-b1-source-binding!
    candidate source-path
    {:owner :gravity.backend/b1-backend-interface
     :relative-path p15-s23-b1-source-relative-path
     :source-byte-count p15-s23-b1-source-byte-count
     :source-content-hash p15-s23-b1-expected-source-content-hash
     :plan-semantic-hash p15-s23-b1-expected-plan-semantic-hash
     :functions-semantic-hash p15-s23-b1-expected-functions-semantic-hash
     :builder-semantic-hash p15-s23-b1-wasm-expected-builder-semantic-hash
     :builder-function p15-s23-b1-wasm-builder-function
     :required-functions p15-s23-b1-wasm-required-functions
     :emitter-target :wasm})}))
