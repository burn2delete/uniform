

(defn sh03-reader-plan-instruction-summary
  [source-path functions]
  (loop [pending
         (reduce
          (fn [tasks [_ function]]
            (into tasks (map #(vector % 1) (:instructions function))))
          [] functions)
         summary {}
         builtins #{}
         observed-nodes 0
         observed-depth 0]
    (when (> observed-nodes sh03-reader-plan-maximum-nodes)
      (sh03-reader-boundary-fail!
       source-path :bounded-sh03-reader-plan-instructions
       observed-nodes {:maximum-nodes sh03-reader-plan-maximum-nodes}))
    (if (empty? pending)
      {:instruction-summary summary
       :builtin-functions builtins
       :observed-nodes observed-nodes
       :observed-depth observed-depth}
      (let [[instruction depth] (peek pending)
            pending (pop pending)
            op (:op instruction)
            children
            (case op
              (:literal :quote :local) []
              (:vector-literal :set-literal) (:items instruction)
              :map-literal
              (mapcat (juxt :key :value) (:entries instruction))
              :do (:body instruction)
              :if [(:test instruction) (:then instruction) (:else instruction)]
              (:let :loop)
              (concat (map :expr (:bindings instruction))
                      (:body instruction))
              :recur (:args instruction)
              (:builtin-call :function-call) (:args instruction)
              [])]
        (recur
         (into pending (map #(vector % (inc depth)) children))
         (update summary op (fnil inc 0))
         (cond-> builtins (= :builtin-call op)
           (conj (:function instruction)))
         (inc observed-nodes)
         (max observed-depth depth))))))

(defn sh03-reader-build-plan!
  [request-source {:keys [source-path bytes source-byte-count
                          source-content-hash]}]
  (let [source-text
        (sh03-reader-strict-source-text!
         request-source source-path bytes)
        macro-artifact (macro-source-artifact source-path source-text)
        module-base (assoc (:module macro-artifact)
                           :forms (:expanded-forms macro-artifact))
        all-functions (stage0-function-table module-base)
        selected-functions
        (into
         (sorted-map)
         (filter (fn [[function-name _]]
                   (str/starts-with? (name function-name)
                                     sh03-reader-function-prefix)))
         all-functions)
        module (assoc module-base :function-table selected-functions)
        emitter-binding
        (c-backend-stage2-plan-emitter-source-rule! source-path :jvm)
        emitter
        (p15-s23-stage2-compiler-artifact-augmented-emitter
         (:emitter emitter-binding))
        functions
        (into
         (sorted-map)
         (map
          (fn [[name definition]]
            [name
             (select-keys
              (p15-s23-stage2-seed-compile-function
               emitter module definition)
              sh03-reader-function-keys)]))
         selected-functions)
        audit (sh03-reader-plan-instruction-summary source-path functions)
        plan-base
        {:kind :gravity/stage2-compiler-artifact-plan
         :compiler-artifact-plan? true
         :entrypoint sh03-reader-entrypoint
         :source {:sha256 source-content-hash
                  :byte-count source-byte-count}
         :compiler
         {:owner :gravity-source
          :stage :p15-s23-stage2-expression-lowering
          :compiled-by :clojure-stage0-seed
          :executed-by :clojure-stage2-generic-rule-runner
          :emitter-source-content-hash
          p15-s23-stage2-compiler-artifact-expected-source-content-hash
          :emitter-semantic-hash
          p15-s23-stage2-compiler-artifact-expected-semantic-hash
          :emitter-source-rule-hash (:source-rule-hash emitter-binding)
          :generic-bridge-residual? true
          :self-hosted? false}
         :module (select-keys module
                              [:module :profile :target :effects
                               :capabilities :exports :safety])
         :functions functions
         :instruction-summary (:instruction-summary audit)
         :effect-summary {:declared (:effects module)
                          :inferred #{}
                          :capabilities (:capabilities module)}
         :sh03-reader
         {:slice :SH-03
          :source-language :gravity
          :entrypoint sh03-reader-entrypoint
          :verifier sh03-reader-verifier
          :target-source-reread? false
          :clojure-seed-boundary? true
          :self-hosted? false}}
        plan (assoc plan-base :plan-id (reader-canonical-hash plan-base))]
    {:plan plan
     :source-path source-path
     :source-byte-count source-byte-count
     :source-content-hash source-content-hash
     :builtin-functions (:builtin-functions audit)}))

(defn sh03-reader-plan-identities
  [plan]
  (let [functions (:functions plan)
        function-names (vec (sort-by str (keys functions)))
        function-shapes
        (into (sorted-map)
              (map (fn [[name function]]
                     [name (select-keys function [:arity :params])]))
              functions)
        audit
        (sh03-reader-plan-instruction-summary
         "<sh03-reader-plan>" functions)]
    {:plan-semantic-hash
     (reader-canonical-hash (dissoc plan :plan-id))
     :functions-semantic-hash (reader-canonical-hash functions)
     :function-count (count functions)
     :function-names-hash (reader-canonical-hash function-names)
     :function-shapes-hash (reader-canonical-hash function-shapes)
     :entrypoint-semantic-hash
     (reader-canonical-hash (get functions sh03-reader-entrypoint))
     :verifier-semantic-hash
     (reader-canonical-hash (get functions sh03-reader-verifier))
     :builtin-functions-hash
     (reader-canonical-hash
      (vec (sort-by str (:builtin-functions audit))))
     :instruction-summary (:instruction-summary audit)
     :observed-instruction-nodes (:observed-nodes audit)
     :observed-instruction-depth (:observed-depth audit)}))

(defn sh03-reader-plan-task
  [instruction locals target tail? depth]
  {:instruction instruction :locals locals :target target
   :tail? tail? :depth depth})

(defn sh03-reader-plan-sequence-tasks
  [instructions locals target tail? depth]
  (let [instructions (vec instructions)
        last-index (dec (count instructions))]
    (mapv (fn [index instruction]
            (sh03-reader-plan-task
             instruction locals target
             (and tail? (= index last-index)) (inc depth)))
          (range) instructions)))