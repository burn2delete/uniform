(let [p15-s23-b4-wasm-authority-token (nth __gravity_bootstrap_lexical_values_136338 0)
      p15-s23-b4-wasm-node-state (nth __gravity_bootstrap_lexical_values_136338 1)]
(defn p15-s23-b4-wasm-contract-bindings [c11 wasm-packet binding]
  (let [mir (:mir-module c11)
        content (fn [kind value]
                  {:kind kind
                   :content-id
                   (p15-s23-c11-mir-digest
                    (p15-s23-c11-mir-path-neutral-value value))
                   :entry-count (if (coll? value) (count value) 1)})]
    (let [base
          {:artifact :gravity/b4-frozen-contract-binding-closure
           :profile-target
           (content :profile-target
                    (select-keys mir [:profile :source-target :target-request
                                      :target-request-metadata
                                      :profile-target-table]))
           :effects-capabilities
           (content :effects-capabilities
                    (select-keys mir [:effect-table :effect-order-graph
                                      :capability-table
                                      :capability-proof-table]))
           :safety-proofs-ownership
           (content :safety-proofs-ownership
                    (select-keys mir [:safety-table :ownership-table
                                      :runtime-check-table
                                      :proof-certificate-table]))
           :types-source-map
           (content :types-source-map
                    (select-keys mir [:type-table :source-map]))
           :abi-runtime-providers
           (content :abi-runtime-providers
                    {:abi {:parameters [] :result :i32}
                     :target-runtime :none :target-providers []
                     :runtime-helpers []
                     :wasm-feature-policy
                     p15-s23-b4-wasm-bounded-feature-policy
                     :conformance-tool
                     {:tool :node :version p15-s23-b4-wasm-node-version
                      :content-hash p15-s23-b4-wasm-node-content-hash
                      :target-runtime? false}})
           :dependencies
           (content :dependencies
                    {:c11-source-rule (:source-rule c11)
                     :c11-pass-record (:pass-execution-record mir)
                     :c13-semantic-id
                     (get-in wasm-packet [:c13 :semantic-id])
                     :c14-semantic-id
                     (get-in wasm-packet [:c14 :semantic-id])
                     :b1-semantic-id
                     (get-in wasm-packet [:b1 :semantic-id])
                     :wasm-packet-semantic-id (:semantic-id wasm-packet)
                     :c13-source-rule (get-in wasm-packet [:c13 :source-rule])
                     :c14-source-rule (get-in wasm-packet [:c14 :source-rule])
                     :b1-source-rule (get-in wasm-packet [:b1 :source-rule])
                     :b4-source-rule (p15-s23-b4-wasm-source-rule binding)})
           :c11-verifier
           (content :c11-verifier
                    {:report-id (get-in c11 [:verification-report :report-id])
                     :report-hash
                     (get-in c11 [:verification-report :report-hash])
                     :status (get-in c11 [:verification-report
                                          :verification-status])})}]
      (assoc base :contract-binding-id
             (p15-s23-c11-mir-digest base)))))

(defn- p15-s23-b4-wasm-final-record
  [c11 checked-core context c11-report wasm-packet binding lowering
   reconstruction parser-report node-report]
  (let [bytes (byte-array (map unchecked-byte
                               (:wasm-bytes reconstruction)))
        content-hash (str "sha256:" (sha256-bytes-hex bytes))
        source-path (:source-path context)
        contract-bindings
        (p15-s23-b4-wasm-contract-bindings c11 wasm-packet binding)
        contract-binding-id (:contract-binding-id contract-bindings)
        base
        {:kind :gravity/p15-s23-b4-authenticated-wasm-artifact
         :schema-version 1
         :c11 {:artifact-id (:artifact-id c11) :mir-id (:mir-id c11)
               :source-core-artifact-id (:source-core-artifact-id c11)
               :verification-report-id
               (get-in c11 [:verification-report :report-id])
               :verification-report-hash
               (get-in c11 [:verification-report :report-hash])}
         :source-rule (p15-s23-b4-wasm-source-rule binding)
         :contract-bindings contract-bindings
         :c13-c14-b1-packet wasm-packet
         :b4-record {:artifact :gravity/wasm-backend-manifest
                     :status :partial-bounded-executable-slice
                     :target-kind :core-module
                     :target :wasm32-unknown-unknown
                     :feature-record
                     p15-s23-b4-wasm-bounded-feature-policy
                     :imports [] :exports [{:name "main" :kind :function
                                            :index 0}]
                     :runtime-helpers [] :effects #{} :capabilities #{}
                     :contract-binding-id contract-binding-id}
         :b13-record {:artifact :gravity/content-addressed-artifact-record
                      :logical-path "program.wasm"
                      :retention :ephemeral-conformance-intent
                      :content-hash content-hash
                      :byte-count (count bytes)
                      :contract-binding-id contract-binding-id}
         :b14-record {:artifact :gravity/backend-conformance-report
                      :status :bounded-experimental-slice
                      :validator :pinned-node-20.9.0
                      :expected-result (:expected-result reconstruction)
                      :observed-result (:observed-result node-report)
                      :tool-identity-race-residual :unclosed
                      :process-tree-proof :bounded-captured-set-only
                      :negative-diagnostics :covered-by-focused-tests
                      :contract-binding-id contract-binding-id}
         :c18-record {:artifact :gravity/backend-translation-validation
                      :status :bounded-experimental-slice
                      :gravity-source-lowering :reconstructed
                      :raw-module-parser :passed
                      :differential-execution :passed
                      :contract-binding-id contract-binding-id}
         :lowering (dissoc lowering :wasm-bytes)
         :raw-wasm {:bytes (:wasm-bytes reconstruction)
                    :content-hash content-hash
                    :byte-count (count bytes)}
         :independent-reconstruction reconstruction
         :independent-parser parser-report
         :node-conformance node-report
         :c11-verification
         (select-keys c11-report [:status :mir-id :checked-core-artifact-id
                                  :semantic-replay-parity :b1-preflight])
         :scope {:task :FL-P07-B4-PHASE1
                 :subset
                 :bounded-pure-scalar-forwarding-do-let-if-integer-comparisons
                 :whole-b4? false :public? false :release? false
                 :component-model? false :wit? false :wasi? false
                 :memory? false :self-hosted? false
                 :compile-to-any-target? false
                 :atomic-tool-identity-binding? false
                 :whole-process-tree-reaping-proved? false}
         :diagnostics [] :clojure-seed-boundary? true :self-hosted? false}
        provenance {:source source-path
                    :c11-source (get-in c11 [:provenance :actual-paths
                                             :c11-source])
                    :c13-source (get-in wasm-packet
                                        [:actual-path-provenance :c13-source])
                    :c14-source (get-in wasm-packet
                                        [:actual-path-provenance :c14-source])
                    :b1-source (get-in wasm-packet
                                       [:actual-path-provenance :b1-source])
                    :b4-source (:source-path binding)
                    :node p15-s23-b4-wasm-node-path}
        semantic-id (p15-s23-b4-wasm-artifact-id base)
        artifact-id (p15-s23-c11-mir-digest
                     {:kind (:kind base) :schema-version 1
                      :semantic-id semantic-id})]
    (assoc base :semantic-id semantic-id :artifact-id artifact-id
           :actual-path-provenance provenance
           :actual-path-binding-id
           (p15-s23-b4-wasm-actual-path-binding-id
            semantic-id provenance))))

(def p15-s23-b4-wasm-final-artifact-keys
  #{:kind :schema-version :semantic-id :artifact-id :actual-path-binding-id
    :actual-path-provenance :c11 :source-rule :contract-bindings
    :c13-c14-b1-packet
    :b4-record :b13-record :b14-record :c18-record :lowering :raw-wasm
    :independent-reconstruction :independent-parser :node-conformance
    :c11-verification :scope :diagnostics :clojure-seed-boundary?
    :self-hosted?}))
