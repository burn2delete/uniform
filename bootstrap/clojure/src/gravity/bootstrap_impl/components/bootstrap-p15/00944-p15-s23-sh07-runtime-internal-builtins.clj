(declare sh07-core-cached-binding
         p15-s23-sh07-b51-runtime-admitted-products-match?
         ^:private p15-s23-sh07-b51-consume-admitted-carrier-validation!)

(defn- p15-s23-sh07-runtime-internal-fail!
  [plan callee reason]
  (fail! "L2-BUILTIN-ERROR"
         "compiler-internal SH-07 builtin rejected its invocation"
         {:source-span {:source (or (get-in plan [:source :path])
                                    p15-s23-sh07-checked-core-source-relative-path)}
          :function callee
          :reason reason
          :result-committed? false
          :output-committed? false
          :remediation
          "Execute the builtin only from its authenticated checked-core plan occurrence with inputs satisfying the closed builtin contract."}))

(defn- p15-s23-sh07-runtime-v1-plan-id
  [plan]
  (str "sha256:"
       (sha256-hex
        (pr-str (c-backend-canonical-value (dissoc plan :plan-id))))))

(defn- p15-s23-sh07-runtime-v1-instruction-in-function?
  [definition instruction]
  (boolean
   (some #(= instruction %)
         (tree-seq coll? seq (:instructions definition)))))

(defn- p15-s23-sh07-runtime-instruction-children
  [instruction]
  (case (:op instruction)
    :println (:args instruction)
    :do (:body instruction)
    :if [(:test instruction) (:then instruction) (:else instruction)]
    :let (concat (map :expr (:bindings instruction)) (:body instruction))
    :loop (concat (map :expr (:bindings instruction)) (:body instruction))
    :recur (:args instruction)
    :builtin-call (:args instruction)
    :function-call (:args instruction)
    :vector-literal (:items instruction)
    :set-literal (:items instruction)
    :map-literal (mapcat (fn [entry] [(:key entry) (:value entry)])
                         (:entries instruction))
    []))

(defn- p15-s23-sh07-runtime-v2-instruction-in-function?
  [definition instruction]
  (loop [pending (vec (:instructions definition))]
    (if (empty? pending)
      false
      (let [candidate (peek pending)
            pending (pop pending)]
        (if (= instruction candidate)
          true
          (recur (into pending
                       (reverse
                        (filter map?
                                (p15-s23-sh07-runtime-instruction-children
                                 candidate))))))))))

(defn- p15-s23-sh07-runtime-v2-provenance-authentic?
  [descriptor manifest plan instruction]
  (let [callee (:function instruction)
        caller (:compiler-internal-caller instruction)
        provenance (:compiler-internal-provenance instruction)
        definition (get-in plan [:functions caller])]
    (and (map? definition)
         (= #{:op :function :compiler-internal-caller
              :compiler-internal-provenance :args}
            (set (keys instruction)))
         (= :builtin-call (:op instruction))
         (= p15-s23-sh07-instruction-provenance-v2-keys
            (set (keys provenance)))
         (= :gravity/sh07-compiler-internal-call-provenance-v2
            (:artifact provenance))
         (= (:manifest-id manifest) (:manifest-id provenance))
         (= (:module-source-id descriptor) (:module-source-id provenance))
         (= (get-in plan [:source :sha256]) (:source-sha256 provenance))
         (= caller (:function provenance) (:name definition))
         (= callee (:callee provenance))
         (= (p15-s23-sh07-source-definition-hash definition)
            (:function-source-hash provenance))
         (p15-s23-sh07-form-contains-symbol? (:body definition) callee)
         (p15-s23-sh07-runtime-v2-instruction-in-function?
          definition instruction))))

(defn- p15-s23-sh07-runtime-v1-provenance-authentic? [plan instruction]
  (let [callee (:function instruction)
        caller (:compiler-internal-caller instruction)
        provenance (:compiler-internal-provenance instruction)
        definition (get-in plan [:functions caller])]
    (and (map? definition)
         (= #{:op :function :compiler-internal-caller
              :compiler-internal-provenance :args}
            (set (keys instruction)))
         (= :builtin-call (:op instruction))
         (= p15-s23-sh07-instruction-provenance-v1-keys
            (set (keys provenance)))
         (= :gravity/sh07-compiler-internal-call-provenance-v1
            (:artifact provenance))
         (= p15-s23-sh07-checked-core-source-relative-path
            (:source-relative-path provenance))
         (= (get-in plan [:source :sha256]) (:source-sha256 provenance))
         (= caller (:function provenance) (:name definition))
         (= callee (:callee provenance))
         (= (p15-s23-sh07-source-definition-hash definition)
            (:function-source-hash provenance))
         (p15-s23-sh07-form-contains-symbol? (:body definition) callee)
         (p15-s23-sh07-runtime-v1-instruction-in-function?
          definition instruction))))

;; Preserve the frozen B47 private seams used by the accepted V1 evidence.
(defn- p15-s23-sh07-runtime-plan-id [plan]
  (p15-s23-sh07-runtime-v1-plan-id plan))

(defn- p15-s23-sh07-runtime-provenance-authentic? [plan instruction]
  (p15-s23-sh07-runtime-v1-provenance-authentic? plan instruction))

(defn- p15-s23-sh07-runtime-v1-plan-authentic? [runtime plan instruction]
  (try
    (let [binding @sh07-core-cached-binding
          callee (:function instruction)
          caller (:compiler-internal-caller instruction)]
      (and (= binding (:sh07-core-binding runtime))
           (= plan (:plan binding))
           (= (:plan-id plan) (p15-s23-sh07-runtime-v1-plan-id plan))
           (= (:source-content-hash binding) (get-in plan [:source :sha256]))
           (= :gravity/stage2-compiler-artifact-plan (:kind plan))
           (true? (:compiler-artifact-plan? plan))
           (= 'gravity.checked-core (get-in plan [:module :module]))
           (= :meta (get-in plan [:module :profile]))
           (= :jvm (get-in plan [:module :target]))
           (= #{} (get-in plan [:module :effects]))
           (= #{} (get-in plan [:module :capabilities]))
           (= :safe (get-in plan [:module :safety]))
           (= {:owner :gravity-source
               :stage :p15-s23-stage2-expression-lowering
               :compiled-by :clojure-stage0-seed
               :generic-bridge-residual? true
               :artifact-hash
               (p15-s23-stage2-compiler-artifact-expected-artifact-hash)
               :source-content-hash
               p15-s23-stage2-compiler-artifact-expected-source-content-hash
               :semantic-hash
               p15-s23-stage2-compiler-artifact-expected-semantic-hash}
              (:compiler plan))
           (contains? (get-in p15-s23-sh07-declared-builtin-contracts
                              [callee :callers] #{}) caller)
           (p15-s23-sh07-runtime-v1-provenance-authentic? plan instruction)))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread)) (throw interrupted))
    (catch Throwable _ false)))

(defn- p15-s23-sh07-runtime-v2-plan-authentic?
  [runtime plan instruction]
  (try
    (let [descriptors (:sh07-b51-module-source-descriptors runtime)
          manifest (:sh07-b51-link-manifest runtime)
          plans (:sh07-b51-module-plans runtime)
          bundle (:sh07-b51-execution-bundle runtime)
          carrier (:sh07-b51-linked-carrier-v3 runtime)
          descriptor
          (when (vector? descriptors)
            (first (filter #(and (map? %)
                                 (= (:sh07-b51-module-source-id plan)
                                    (:module-source-id %)))
                           descriptors)))
          callee (:function instruction)
          caller (:compiler-internal-caller instruction)
          definition (when (map? plan) (get-in plan [:functions caller]))
          definition-hash
          (when (map? definition)
            (p15-s23-sh07-source-definition-hash definition))
          definition-row
          (when (map? descriptor)
            (first (filter #(= caller (:name %))
                           (:definitions descriptor))))
          builtin-rows
          (when (map? descriptor)
            (filter #(and (= caller (:caller %))
                          (= callee (:builtin %)))
                    (:builtin-calls descriptor)))]
      (and (p15-s23-sh07-b51-runtime-admitted-products-match?
            runtime carrier descriptors manifest plans bundle)
           (map? descriptor)
           (vector? plans)
           (integer? (:module-ordinal descriptor))
           (<= 0 (:module-ordinal descriptor))
           (< (:module-ordinal descriptor) (count plans))
           (= plan (nth plans (:module-ordinal descriptor)))
           (= (:plan-id plan)
              (c4-artifact-id
               (c-backend-canonical-value
                (-> plan
                    (dissoc :plan-id)
                    (update :source dissoc :path)
                    (update :module dissoc :source-path)))))
           (= (:source-sha256 descriptor) (get-in plan [:source :sha256]))
           (= (:module descriptor) (get-in plan [:module :module]))
           (= :meta (get-in plan [:module :profile]))
           (= :jvm (get-in plan [:module :target]))
           (= #{} (get-in plan [:module :effects]))
           (= #{} (get-in plan [:module :capabilities]))
           (= :safe (get-in plan [:module :safety]))
           (= (:module-source-id descriptor)
              (:sh07-b51-module-source-id plan))
           (= (:manifest-id manifest)
              (:sh07-b51-link-manifest-id plan))
           (= definition-hash (:source-hash definition-row))
           (= caller (:name definition-row))
           (= (:arity definition) (:arity definition-row))
           (seq builtin-rows)
           (every?
            #(and (= definition-hash (:caller-function-source-hash %))
                  (= (get p15-s23-sh07-declared-builtin-exact-arities callee)
                     (:arity %))
                  (true? (:compiler-internal? %)))
            builtin-rows)
           (contains? (get-in p15-s23-sh07-declared-builtin-contracts
                              [callee :callers]
                              #{})
                      caller)
           (p15-s23-sh07-runtime-v2-provenance-authentic?
            descriptor manifest plan instruction)))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn- p15-s23-sh07-runtime-plan-authentic? [runtime plan instruction]
  (case (get-in instruction [:compiler-internal-provenance :artifact])
    :gravity/sh07-compiler-internal-call-provenance-v1
    (p15-s23-sh07-runtime-v1-plan-authentic? runtime plan instruction)
    :gravity/sh07-compiler-internal-call-provenance-v2
    (p15-s23-sh07-runtime-v2-plan-authentic? runtime plan instruction)
    false))

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
