

(defn p15-s23-reference-runtime-rule-authentic?
  [runtime-rule]
  (try
    (let [source-path (:runtime-artifact-source-path runtime-rule)
          target :jvm
          _ (p15-s23-reference-runtime-bounded-value!
             source-path target :runtime-rule runtime-rule
             p15-s23-reference-runtime-max-rule-nodes
             p15-s23-reference-runtime-max-contract-depth)
          plan (:runtime-artifact-plan runtime-rule)
          authoritative-module
          (:runtime-artifact-authoritative-module runtime-rule)
          definitions (:runtime-contract-definitions runtime-rule)
          _ (p15-s23-reference-runtime-bounded-value!
             source-path target :runtime-artifact-plan plan)
          _ (doseq [[name value] definitions]
              (p15-s23-reference-runtime-bounded-value!
               source-path target name value))
          function-hashes
          (into (sorted-map)
                (map (fn [[name definition]]
                       [name
                        (p15-s23-stage2-runtime-artifact-function-semantic-hash
                         definition)]))
                (:functions plan))
          contract-definition-hash
          (p15-s23-reference-runtime-hash definitions)
          validation
          (p15-s23-reference-runtime-contract-validation!
           source-path target definitions authoritative-module plan)
          derived-facts (:derived-contract-facts validation)
          derived-facts-hash
          (p15-s23-reference-runtime-hash derived-facts)
          hash-input
          (c-backend-stage2-runtime-artifact-hash-input
           plan authoritative-module definitions function-hashes derived-facts)
          _ (p15-s23-reference-runtime-bounded-value!
             source-path target :runtime-artifact-hash-input hash-input)
          artifact-hash (p15-s23-reference-runtime-hash hash-input)
          runtime (:runtime runtime-rule)
          kernel (:kernel runtime-rule)
          compiler-source-binding
          (p15-s23-reference-runtime-pinned-file-binding
           (:stage2-compiler-source-path runtime-rule)
           p15-s23-stage2-compiler-expected-source-byte-count
           p15-s23-stage2-compiler-expected-source-content-hash)
          runtime-source-binding
          (p15-s23-reference-runtime-pinned-file-binding
           (:runtime-source-path runtime-rule)
           p15-s23-stage2-runtime-artifact-expected-source-byte-count
           p15-s23-stage2-runtime-artifact-expected-source-content-hash)
          runtime-artifact-source-binding
          (p15-s23-reference-runtime-pinned-file-binding
           source-path
           p15-s23-stage2-runtime-artifact-expected-source-byte-count
           p15-s23-stage2-runtime-artifact-expected-source-content-hash)
          runtime-rule-hash (p15-s23-reference-runtime-hash runtime)
          kernel-rule-hash (p15-s23-reference-runtime-hash kernel)
          expected-runtime-rule-source
          {:kind :gravity-source
           :sha256 (:stage2-compiler-source-content-hash runtime-rule)
           :stage2-compiler-source
           {:sha256 (:stage2-compiler-source-content-hash runtime-rule)}
           :runtime-source
           {:sha256 (:runtime-source-content-hash runtime-rule)}
           :runtime-rule-hash runtime-rule-hash
           :runtime-kernel-rule-hash kernel-rule-hash
           :runtime-artifact-source
           {:sha256 (:runtime-artifact-source-content-hash runtime-rule)
            :artifact-hash artifact-hash
            :function p15-s23-stage2-runtime-artifact-function
            :concat-function p15-s23-stage2-runtime-artifact-concat-function
            :println-function p15-s23-stage2-runtime-artifact-println-function
            :println-two-function
            p15-s23-stage2-runtime-artifact-println-two-function
            :closed-plan-function
            p15-s23-stage2-runtime-artifact-closed-plan-function
            :closed-plan-function-hash
            (get function-hashes
                 p15-s23-stage2-runtime-artifact-closed-plan-function)
            :closed-plan-helper-functions
            p15-s23-stage2-runtime-artifact-closed-plan-helper-functions
            :closed-function-hashes
            (select-keys
             function-hashes
             (conj p15-s23-stage2-runtime-artifact-closed-plan-helper-functions
                   p15-s23-stage2-runtime-artifact-closed-plan-function))
            :function-hashes function-hashes
            :contract-definition-hash contract-definition-hash
            :contract-validation (dissoc validation :derived-contract-facts)
            :derived-contract-facts-hash derived-facts-hash
            :println-over-two-boundary
            p15-s23-stage2-runtime-artifact-println-over-two-boundary
            :generic-bridge-residual? true
            :generic-emitter-effect-summary-credited? false}}]
      (and (= p15-s23-reference-runtime-rule-keys
              (set (keys runtime-rule)))
           (= p15-s23-reference-runtime-executor-keys
              (set (keys runtime)))
           (= p15-s23-reference-runtime-kernel-keys
              (set (keys kernel)))
           (= p15-s23-reference-runtime-plan-keys
              (set (keys plan)))
           (= p15-s23-reference-runtime-plan-module-keys
              (set (keys (:module plan))))
           (= p15-s23-reference-runtime-plan-source-keys
              (set (keys (:source plan))))
           (every? #(= p15-s23-reference-runtime-plan-function-keys
                       (set (keys %)))
                   (vals (:functions plan)))
           (= p15-s23-reference-runtime-authoritative-module-keys
              (set (keys authoritative-module)))
           (= p15-s23-reference-runtime-expected-plan-id (:plan-id plan))
           (= p15-s23-reference-runtime-expected-plan-id
              (c4-artifact-id
               (c-backend-canonical-value
                (-> plan
                    (dissoc :plan-id)
                    (update :source dissoc :path)
                    (update :module dissoc :source-path)))))
           (= p15-s23-reference-runtime-expected-authoritative-module-hash
              (p15-s23-reference-runtime-hash
               (dissoc authoritative-module :source-path)))
           (string? (:runtime-source-path runtime-rule))
           (string? (:stage2-compiler-source-path runtime-rule))
           (string? source-path)
           (map? compiler-source-binding)
           (map? runtime-source-binding)
           (map? runtime-artifact-source-binding)
           (= (:canonical-path runtime-source-binding)
              (:canonical-path runtime-artifact-source-binding)
              (p15-s23-reference-runtime-existing-canonical-path
               (get-in plan [:source :path]))
              (p15-s23-reference-runtime-existing-canonical-path
               (get-in plan [:module :source-path]))
              (p15-s23-reference-runtime-existing-canonical-path
               (:source-path authoritative-module)))
           (= p15-s23-stage2-compiler-expected-source-content-hash
              (:stage2-compiler-source-content-hash runtime-rule))
           (= p15-s23-stage2-runtime-artifact-expected-source-content-hash
              (:runtime-source-content-hash runtime-rule))
           (= (:runtime-source-path runtime-rule)
              (:runtime-artifact-source-path runtime-rule))
           (= runtime-rule-hash (:runtime-rule-hash runtime-rule))
           (= kernel-rule-hash (:runtime-kernel-rule-hash runtime-rule))
           (= p15-s23-reference-runtime-expected-executor-hash
              runtime-rule-hash)
           (= p15-s23-reference-runtime-expected-kernel-hash
              kernel-rule-hash)
           (= (p15-s23-stage2-runtime-executor-rule-record runtime)
              (:runtime-rule-record runtime-rule))
           (= (p15-s23-stage2-runtime-kernel-rule-record kernel)
              (:kernel-rule-record runtime-rule))
           (= (:engine runtime) (:runtime-engine runtime-rule))
           (= (:engine kernel) (:runtime-kernel-engine runtime-rule))
           (= expected-runtime-rule-source (:runtime-rule-source runtime-rule))
           (= hash-input (:runtime-artifact-hash-input runtime-rule))
           (= (:functions plan) (:runtime-artifact-functions runtime-rule))
           (= p15-s23-stage2-runtime-artifact-function
              (:runtime-artifact-function runtime-rule))
           (= p15-s23-stage2-runtime-artifact-concat-function
              (:runtime-artifact-concat-function runtime-rule))
           (= p15-s23-stage2-runtime-artifact-println-function
              (:runtime-artifact-println-function runtime-rule))
           (= p15-s23-stage2-runtime-artifact-println-two-function
              (:runtime-artifact-println-two-function runtime-rule))
           (= p15-s23-stage2-runtime-artifact-closed-plan-function
              (:runtime-artifact-closed-plan-function runtime-rule))
           (= p15-s23-stage2-runtime-artifact-println-over-two-boundary
              (:runtime-artifact-println-over-two-boundary runtime-rule))
           (true? (:runtime-artifact-generic-bridge-residual? runtime-rule))
           (false?
            (:runtime-artifact-generic-emitter-effect-summary-credited?
             runtime-rule))
           (= (:effects authoritative-module)
              (:runtime-artifact-effects runtime-rule))
           (= (:capabilities authoritative-module)
              (:runtime-artifact-capabilities runtime-rule))
           (= p15-s23-stage2-runtime-artifact-expected-source-content-hash
              (:runtime-artifact-source-content-hash runtime-rule))
           (= p15-s23-reference-runtime-expected-contract-definition-hash
              contract-definition-hash
              (:runtime-contract-definition-hash runtime-rule))
           (= p15-s23-reference-runtime-expected-function-hashes
              function-hashes
              (:runtime-artifact-function-hashes runtime-rule))
           (= p15-s23-stage2-runtime-artifact-expected-closed-function-hashes
              (:runtime-artifact-closed-function-hashes runtime-rule))
           (= p15-s23-stage2-runtime-artifact-closed-plan-helper-functions
              (:runtime-artifact-closed-plan-helper-functions runtime-rule))
           (= (get function-hashes
                   p15-s23-stage2-runtime-artifact-closed-plan-function)
              (:runtime-artifact-closed-plan-function-hash runtime-rule))
           (= p15-s23-reference-runtime-source-provider-selections
              (:providers authoritative-module)
              (:runtime-artifact-providers runtime-rule))
           (= (c-backend-canonical-value validation)
              (c-backend-canonical-value
               (:runtime-contract-validation-record runtime-rule)))
           (= derived-facts-hash
              (:runtime-contract-derived-facts-hash runtime-rule))
           (= p15-s23-reference-runtime-expected-derived-facts-hash
              derived-facts-hash)
           (= p15-s23-stage2-runtime-artifact-expected-artifact-hash
              artifact-hash
              (:runtime-artifact-hash runtime-rule))))
    (catch Exception _ false)))

(defn p15-s23-reference-runtime-authority
  ([]
   (p15-s23-reference-runtime-authority
    nil {:observed-operation-set #{:println}}))
  ([plan]
   (p15-s23-reference-runtime-authority
    plan
    (p15-s23-closed-runtime-plan-validation!
     (or (get-in plan [:source :path]) "runtime-authority-plan")
     :jvm plan)))
  ([plan closed-plan-validation]
   (let [writes-stdout?
         (contains? (:observed-operation-set closed-plan-validation)
                    :println)]
     {:mode :closed-plan-reference
      :source-principal 'gravity.bootstrap.p15-s23.runtime
      :handler-principal :gravity.bootstrap/reference-harness
      :providers (cond-> #{:gravity.reference/jvm-managed-allocator}
                   writes-stdout?
                   (conj :gravity.reference/transcript-capture))
      :grants (cond-> #{:gravity.reference/grant-managed-allocation}
                writes-stdout?
                (conj :gravity.reference/grant-reference-stdout
                      :gravity.reference/grant-test-fixture))
      :failure-injection nil
      :deployment-stdout? false})))