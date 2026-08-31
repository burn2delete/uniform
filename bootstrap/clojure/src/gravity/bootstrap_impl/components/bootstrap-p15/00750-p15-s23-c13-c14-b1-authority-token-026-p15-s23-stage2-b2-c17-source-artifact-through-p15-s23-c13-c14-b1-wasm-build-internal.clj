(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn p15-s23-stage2-b2-c17-source-artifact!
  [source-path source-text]
  (try
    (let [upstream-diagnostic-owner (Object.)
          [checked-core context]
          (binding [*p15-s23-c11-upstream-diagnostic-owner*
                    upstream-diagnostic-owner
                    *p15-s23-c11-mir-diagnostic-context*
                    {:requested-target :c}
                    *additional-bootstrap-targets*
                    stage2-runtime-derived-source-targets]
            (try
              (let [context
                    (p15-s23-stage2-gravity-checked-core-context
                     source-path source-text :c)]
                [(p15-s23-stage2-gravity-checked-core-source-artifact
                  context)
                 context])
              (catch InterruptedException interrupted
                (.interrupt (Thread/currentThread))
                (throw interrupted))
              (catch clojure.lang.ExceptionInfo exception
                (let [data
                      (p15-s23-backend-trusted-exception-data
                       exception 65536 128)]
                  (if (and
                       data
                       (p15-s23-c11-mir-owned-upstream-diagnostic? data))
                    (p15-s23-c11-mir-contain-checked-core-exception!
                     source-path :b2-source-checked-core-diagnostic
                     exception)
                    (throw exception))))))
          c11 (p15-s23-stage2-c11-mir-artifact checked-core context)]
      (p15-s23-stage2-b2-c17-artifact-from-c11!
       c11 checked-core context))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch StackOverflowError _
      (p15-s23-c-backend-fail!
       "B2-MANIFEST" source-path {}
       {:missing-fact :bounded-hostile-b2-c17-source-host-stack}))
    (catch clojure.lang.ExceptionInfo exception
      (let [data
            (p15-s23-backend-trusted-exception-data
             exception 65536 128)
            replayed
            (when (map? data)
              (p15-s23-stage2-reader-replayed-diagnostic
               source-path source-text))]
        (if (and data
                 (p15-s23-stage2-canonical-c2-diagnostic-authentic?
                  source-path source-text data replayed))
          (throw exception)
          (p15-s23-c-backend-contain-exception!
           source-path :contained-b2-c17-source-diagnostic exception))))
    (catch Exception exception
      (p15-s23-c-backend-contain-exception!
       source-path :contained-b2-c17-source-host-failure exception))))

(defn- p15-s23-b2-c17-semantic-pure-closure-evidence
  [source-path fresh-c11 gate-a]
  (let [mir (:mir-module fresh-c11)
        function (get-in mir [:functions 'main])
        block-order (p15-s23-b3-llvm-block-order mir function)
        operations
        (p15-s23-b3-llvm-operation-sequence function block-order)
        union-fields
        (fn [table fields]
          (reduce
           set/union #{}
           (for [row (vals table) field fields]
             (let [value (get row field)]
               (cond
                 (set? value) value
                 (sequential? value) (set value)
                 (nil? value) #{}
                 :else #{value})))))
        semantic-effects
        (union-fields (:effect-table mir)
                      [:direct :latent :transitive :residual])
        semantic-capabilities
        (union-fields (:capability-table mir) [:required :granted])
        base
        {:artifact :gravity/b2-c17-semantic-pure-closure-evidence
         :schema-version 1 :status :passed
         :c11-artifact-id (:artifact-id fresh-c11)
         :c11-mir-id (:mir-id fresh-c11)
         :mir-module-id (:module-id mir)
         :operation-count (count operations)
         :effect-fact-row-count (count (:effect-table mir))
         :capability-fact-row-count (count (:capability-table mir))
         :runtime-check-count (count (:runtime-check-table mir))
         :capability-proof-count (count (:capability-proof-table mir))
         :semantic-effects semantic-effects
         :semantic-capabilities semantic-capabilities
         :semantic-effect-count (count semantic-effects)
         :semantic-capability-count (count semantic-capabilities)
         :main-latent-effects (set (:latent-effects function))
         :main-capabilities (set (:capabilities function))
         :all-operation-effects-empty?
         (every? empty? (map :effects operations))
         :all-operation-capabilities-empty?
         (every? empty? (map :capabilities operations))}]
    (when-not
     (and (= :passed (:verification-status mir))
          (= (:artifact-id fresh-c11)
             (get-in gate-a [:input-bindings :c11-artifact-id]))
          (= (:module-id mir)
             (get-in gate-a [:input-bindings :mir-module-id])
             (get-in gate-a [:verified-input-closure :mir-module-id]))
          (= (count operations)
             (get-in gate-a [:verified-input-closure :operation-count]))
          (= (count (:effect-table mir))
             (get-in gate-a [:verified-input-closure :effect-count]))
          (= (count (:capability-table mir))
             (get-in gate-a [:verified-input-closure :capability-count]))
          (= (count (:runtime-check-table mir))
             (get-in gate-a [:verified-input-closure :runtime-check-count]))
          (zero? (:runtime-check-count base))
          (zero? (:capability-proof-count base))
          (empty? (:main-latent-effects base))
          (empty? (:main-capabilities base))
          (true? (:all-operation-effects-empty? base))
          (true? (:all-operation-capabilities-empty? base))
          (empty? semantic-effects) (empty? semantic-capabilities))
      (p15-s23-c-backend-fail!
       "B14-METADATA" source-path gate-a
       {:missing-fact :authenticated-semantic-pure-c17-closure}))
    (assoc base :evidence-id
           (p15-s23-c11-mir-digest
            {:kind :gravity/b2-c17-semantic-pure-closure-evidence
             :schema-version 1 :record base}))))

(defn p15-s23-stage2-b2-c17-verification-report
  [artifact checked-core context]
  (let [source-path (p15-s23-c11-ingress-source-path context)]
    (try
      (p15-s23-b2-c17-verification-preflight! source-path artifact)
      (let [fresh-c11
            (p15-s23-stage2-c11-mir-artifact checked-core context)
            expected
            (p15-s23-b2-c17-build-internal!
             p15-s23-c13-c14-b1-authority-token
             fresh-c11 checked-core context)]
        (p15-s23-b2-c17-assert-exact!
         source-path expected artifact
         :contextual-fresh-c11-through-b2-c17-reconstruction)
        (when-not (= expected artifact)
          (p15-s23-c-backend-fail!
           "B2-MANIFEST" source-path artifact
           {:missing-fact :fresh-context-bound-b2-c17-artifact}))
        (let [semantic-pure-closure
              (p15-s23-b2-c17-semantic-pure-closure-evidence
               source-path fresh-c11 expected)
              base
              {:artifact :gravity/b2-c17-contextual-verification-report
               :schema-version 1
               :status :passed
               :artifact-id (:artifact-id artifact)
               :semantic-id (:semantic-id artifact)
               :fresh-c11 :passed
               :fresh-c13 :passed
               :fresh-c14 :passed
               :fresh-b1 :passed
               :gravity-b2-source-replay :passed
               :independent-c-reconstruction :passed
               :semantic-pure-closure semantic-pure-closure
               :external-tool-execution :not-performed-in-gate-a
               :public? false :release? false :self-hosted? false}]
          (assoc base :report-id
                 (p15-s23-c11-mir-digest
                  {:kind
                   :gravity/b2-c17-contextual-verification-report
                   :schema-version 1 :report base}))))
      (catch InterruptedException interrupted
        (.interrupt (Thread/currentThread))
        (throw interrupted))
      (catch StackOverflowError _
        (p15-s23-c-backend-fail!
         "B2-MANIFEST" source-path {}
         {:missing-fact :bounded-hostile-b2-c17-verifier-stack}))
      (catch clojure.lang.ExceptionInfo exception
        (p15-s23-c-backend-contain-exception!
         source-path :contained-b2-c17-verifier-diagnostic exception))
      (catch Exception exception
        (p15-s23-c-backend-contain-exception!
         source-path :contained-b2-c17-verifier-failure exception)))))

(defn p15-s23-stage2-b2-c17-verify!
  [artifact checked-core context]
  (let [report
        (p15-s23-stage2-b2-c17-verification-report
         artifact checked-core context)]
    (when-not (= :passed (:status report))
      (p15-s23-c-backend-fail!
       "B2-MANIFEST" (p15-s23-c11-ingress-source-path context)
       artifact {:missing-fact :b2-c17-verification-status}))
    :passed))

(defn p15-s23-stage2-b2-c17-authentic?
  ([artifact] false)
  ([artifact checked-core context]
   (try
     (= :passed
        (p15-s23-stage2-b2-c17-verify!
         artifact checked-core context))
     (catch InterruptedException interrupted
       (.interrupt (Thread/currentThread))
       (throw interrupted))
     (catch StackOverflowError _ false)
     (catch Exception _ false))))

(declare p15-s23-c13-c14-b1-wasm-build-internal!
         p15-s23-b4-wasm-fail!
         p15-s23-b4-wasm-contain-exception!))
