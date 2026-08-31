(let [p15-s23-b4-wasm-authority-token (nth __gravity_bootstrap_lexical_values_136338 0)
      p15-s23-b4-wasm-node-state (nth __gravity_bootstrap_lexical_values_136338 1)]
(def p15-s23-b4-wasm-frozen-nested-keysets
  {[:actual-path-provenance]
   #{:source :c11-source :c13-source :c14-source :b1-source
     :b4-source :node}
   [:c11] #{:artifact-id :mir-id :source-core-artifact-id
            :verification-report-id :verification-report-hash}
   [:source-rule] #{:artifact :owner :source-content-hash :source-byte-count
                    :plan-semantic-hash :functions-semantic-hash
                    :builder-function :builder-semantic-hash :function-shapes
                    :compiled-by :executed-by :self-hosted?}
   [:contract-bindings]
   #{:artifact :profile-target :effects-capabilities
     :safety-proofs-ownership :types-source-map :abi-runtime-providers
     :dependencies :c11-verifier :contract-binding-id}
   [:contract-bindings :profile-target] #{:kind :content-id :entry-count}
   [:contract-bindings :effects-capabilities] #{:kind :content-id :entry-count}
   [:contract-bindings :safety-proofs-ownership]
   #{:kind :content-id :entry-count}
   [:contract-bindings :types-source-map] #{:kind :content-id :entry-count}
   [:contract-bindings :abi-runtime-providers]
   #{:kind :content-id :entry-count}
   [:contract-bindings :dependencies] #{:kind :content-id :entry-count}
   [:contract-bindings :c11-verifier] #{:kind :content-id :entry-count}
   [:c13-c14-b1-packet] p15-s23-c13-c14-b1-wasm-final-packet-keys
   [:b4-record] #{:artifact :status :target-kind :target :feature-record
                  :imports :exports :runtime-helpers :effects :capabilities
                  :contract-binding-id}
   [:b13-record] #{:artifact :logical-path :retention :content-hash
                   :byte-count :contract-binding-id}
   [:b14-record] #{:artifact :status :validator :expected-result
                   :observed-result :tool-identity-race-residual
                   :process-tree-proof :negative-diagnostics
                   :contract-binding-id}
   [:c18-record] #{:artifact :status :gravity-source-lowering
                   :raw-module-parser :differential-execution
                   :contract-binding-id}
   [:lowering] (disj p15-s23-b4-wasm-gravity-lowering-keys :wasm-bytes)
   [:raw-wasm] #{:bytes :content-hash :byte-count}
   [:independent-reconstruction]
   #{:artifact :target :target-kind :features :abi :operation-count
     :operation-opcodes :block-order :operation-index :wasm-bytes
     :expected-result :imports :exports :memory :table :globals :start :data
     :custom-sections :runtime-helpers :component-model? :wit? :wasi?}
   [:independent-parser]
   #{:artifact :status :format :section-ids :section-offsets :function-count
     :type-count :export-count :imports :exports :memory-count :table-count
     :global-count :operation-count :decoded-ast :decoded-result
     :operation-byte-map :operation-byte-map-coordinate :byte-end-exclusive?
     :definitely-initialized-locals :expected-result}
   [:node-conformance]
   #{:artifact :status :tool :version :architecture :tool-content-hash
     :tool-byte-count :probe-script-hash :timeout-ms :stdin :environment
     :imports :exports :validate :compile :instantiate :expected-result
     :observed-result :repeat-result :stdout-hash :stdout-byte-count
     :stderr-hash :stderr-byte-count :invocation-local-start-count
     :process-tree :atomic-tool-identity-binding?
     :whole-process-tree-reaping-proved?}
   [:c11-verification] #{:status :mir-id :checked-core-artifact-id
                         :semantic-replay-parity :b1-preflight}
   [:scope] #{:task :subset :whole-b4? :public? :release? :component-model?
              :wit? :wasi? :memory? :self-hosted? :compile-to-any-target?
              :atomic-tool-identity-binding?
              :whole-process-tree-reaping-proved?}})

(def p15-s23-b4-wasm-frozen-scope
  {:task :FL-P07-B4-PHASE1
   :subset :bounded-pure-scalar-forwarding-do-let-if-integer-comparisons
   :whole-b4? false :public? false :release? false
   :component-model? false :wit? false :wasi? false
   :memory? false :self-hosted? false :compile-to-any-target? false
   :atomic-tool-identity-binding? false
   :whole-process-tree-reaping-proved? false})

(def p15-s23-b4-wasm-max-final-artifact-carrier-nodes 65536)
(def p15-s23-b4-wasm-max-final-artifact-carrier-depth 128)

(defn- p15-s23-b4-wasm-require-trusted-final-carrier!
  [source-path artifact]
  (let [validation
        (p15-s23-trusted-carrier-validation
         artifact :default-only
         p15-s23-b4-wasm-max-final-artifact-carrier-nodes
         p15-s23-b4-wasm-max-final-artifact-carrier-depth
         p15-s23-b4-wasm-max-final-artifact-carrier-nodes)]
    (when-not (= :passed (:status validation))
      (p15-s23-b4-wasm-fail!
       "B4-MANIFEST" source-path {}
       (assoc
        (select-keys validation
                     [:reason :observed-nodes :observed-depth
                      :maximum-nodes :maximum-depth :maximum-width])
        :missing-fact
        :trusted-comparator-free-b4-final-artifact-carrier)))
    validation))

(defn p15-s23-b4-wasm-verify-frozen-envelope!
  [artifact source-path]
  (p15-s23-b4-wasm-require-trusted-final-carrier!
   source-path artifact)
  (when-not (map? artifact)
    (p15-s23-b4-wasm-fail!
     "B4-MANIFEST" source-path {}
     {:missing-fact :bounded-b4-final-artifact-map}))
  (p15-s23-c11-mir-bounded-value!
   source-path :b4-frozen-envelope artifact
   p15-s23-b4-wasm-max-final-artifact-carrier-nodes
   p15-s23-b4-wasm-max-final-artifact-carrier-depth)
  (when-not (= p15-s23-b4-wasm-final-artifact-keys
               (set (keys artifact)))
    (p15-s23-b4-wasm-fail!
     "B4-MANIFEST" source-path artifact
     {:missing-fact :frozen-b4-envelope-self-consistency}))
  (doseq [[path expected-keys] p15-s23-b4-wasm-frozen-nested-keysets]
    (let [value (get-in artifact path)]
      (when-not (and (map? value) (= expected-keys (set (keys value))))
        (p15-s23-b4-wasm-fail!
         "B4-MANIFEST" source-path artifact
         {:missing-fact :frozen-b4-envelope-self-consistency}))))
  (let [raw (:raw-wasm artifact)
        bytes (:bytes raw)]
    (when-not (and (vector? bytes)
                   (<= 8 (count bytes) p15-s23-b4-wasm-max-module-bytes)
                   (every? #(and (integer? %) (<= 0 % 255)) bytes))
      (p15-s23-b4-wasm-fail!
       "B4-MANIFEST" source-path artifact
       {:missing-fact :raw-wasm-byte-bounds
        :byte-count (if (vector? bytes) (count bytes) 0)}))
    (let [observed-hash
          (str "sha256:"
               (sha256-bytes-hex (byte-array (map unchecked-byte bytes))))]
      (when-not (and (= (count bytes) (:byte-count raw))
                     (= observed-hash (:content-hash raw))
                     (= (:byte-count raw)
                        (get-in artifact [:b13-record :byte-count]))
                     (= (:content-hash raw)
                        (get-in artifact [:b13-record :content-hash])))
        (p15-s23-b4-wasm-fail!
         "B13-HASH" source-path artifact
         {:missing-fact :raw-wasm-content-hash
          :content-hash (:content-hash raw)
          :expected-hash observed-hash}))))
  (let [closure (:contract-bindings artifact)
        _ (p15-s23-c13-c14-b1-wasm-verification-preflight!
           source-path (:c13-c14-b1-packet artifact))
        contract-id (:contract-binding-id closure)
        repeated-contract-ids
        (mapv #(get-in artifact [% :contract-binding-id])
              [:b4-record :b13-record :b14-record :c18-record])
        node (:node-conformance artifact)
        result (:expected-result node)
        stdout-bytes (.getBytes
                      (str "B4NODE1:" result "\n")
                      java.nio.charset.StandardCharsets/UTF_8)
        empty-bytes (byte-array 0)
        fixed-node
        {:artifact :gravity/b4-node-conformance-execution
         :status :passed :tool :node
         :version p15-s23-b4-wasm-node-version
         :architecture p15-s23-b4-wasm-node-architecture
         :tool-content-hash p15-s23-b4-wasm-node-content-hash
         :tool-byte-count p15-s23-b4-wasm-node-byte-count
         :probe-script-hash p15-s23-b4-wasm-node-script-hash
         :timeout-ms p15-s23-b4-wasm-node-timeout-ms
         :stdin :closed :environment :fixed-private
         :imports [] :exports [{:name "main" :kind :function}]
         :validate :passed :compile :passed :instantiate :passed
         :invocation-local-start-count 1 :process-tree nil
         :atomic-tool-identity-binding? false
         :whole-process-tree-reaping-proved? false}
        fixed-node-keys (set (keys fixed-node))]
    (when-not
     (and
      (= :gravity/p15-s23-b4-authenticated-wasm-artifact (:kind artifact))
      (= 1 (:schema-version artifact)) (= [] (:diagnostics artifact))
      (true? (:clojure-seed-boundary? artifact))
      (false? (:self-hosted? artifact))
      (= (:semantic-id artifact) (p15-s23-b4-wasm-artifact-id artifact))
      (= (:artifact-id artifact)
         (p15-s23-c11-mir-digest
          {:kind (:kind artifact) :schema-version 1
           :semantic-id (:semantic-id artifact)}))
      (= (:actual-path-binding-id artifact)
         (p15-s23-b4-wasm-actual-path-binding-id
          (:semantic-id artifact) (:actual-path-provenance artifact)))
      (= :gravity/b4-frozen-contract-binding-closure (:artifact closure))
      (= contract-id
         (p15-s23-c11-mir-digest (dissoc closure :contract-binding-id)))
      (every? #(= contract-id %) repeated-contract-ids)
      (= p15-s23-b4-wasm-bounded-feature-policy
         (get-in artifact [:b4-record :feature-record]))
      (= p15-s23-b4-wasm-frozen-scope (:scope artifact))
      (= fixed-node (select-keys node fixed-node-keys))
      (= result (:observed-result node) (:repeat-result node)
         (get-in artifact [:b14-record :expected-result])
         (get-in artifact [:b14-record :observed-result]))
      (= (str "sha256:" (sha256-bytes-hex stdout-bytes))
         (:stdout-hash node))
      (= (alength stdout-bytes) (:stdout-byte-count node))
      (= (str "sha256:" (sha256-bytes-hex empty-bytes))
         (:stderr-hash node))
      (= 0 (:stderr-byte-count node))
      (= :bounded-experimental-slice (get-in artifact [:b14-record :status]))
      (= :bounded-experimental-slice (get-in artifact [:c18-record :status])))
     (p15-s23-b4-wasm-fail!
      "B4-MANIFEST" source-path artifact
      {:missing-fact :frozen-b4-envelope-self-consistency})))
  :passed))
