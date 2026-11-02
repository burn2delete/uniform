(let [p15-s23-b4-wasm-authority-token (nth __gravity_bootstrap_lexical_values_136338 0)
      p15-s23-b4-wasm-node-state (nth __gravity_bootstrap_lexical_values_136338 1)]
(defn p15-s23-b4-wasm-verify-integrity!
  [artifact fresh context binding preflight]
  (let [source-path (p15-s23-c11-ingress-source-path context)]
    (p15-s23-b4-wasm-require-trusted-final-carrier!
     source-path artifact)
    (when-not (map? artifact)
      (p15-s23-b4-wasm-fail!
       "B4-MANIFEST" source-path {}
       {:missing-fact :bounded-b4-final-artifact-map}))
    (p15-s23-c11-mir-bounded-value!
     source-path :b4-final-artifact-integrity artifact
     p15-s23-b4-wasm-max-final-artifact-carrier-nodes
     p15-s23-b4-wasm-max-final-artifact-carrier-depth)
    (let [raw-bytes (get-in artifact [:raw-wasm :bytes])]
      (when (and (vector? raw-bytes)
                 (<= (count raw-bytes) p15-s23-b4-wasm-max-module-bytes)
                 (every? #(and (integer? %) (<= 0 % 255)) raw-bytes))
        (let [observed (str "sha256:"
                            (sha256-bytes-hex
                             (byte-array (map unchecked-byte raw-bytes))))]
          (when-not (= observed (get-in artifact [:raw-wasm :content-hash]))
            (p15-s23-b4-wasm-fail!
             "B13-HASH" source-path artifact
             {:missing-fact :raw-wasm-content-hash
              :content-hash (get-in artifact [:raw-wasm :content-hash])
              :expected-hash observed})))))
    (when-not (and (= p15-s23-b4-wasm-final-artifact-keys
                       (set (keys artifact)))
                   (= :gravity/p15-s23-b4-authenticated-wasm-artifact
                      (:kind artifact))
                   (= 1 (:schema-version artifact))
                   (= [] (:diagnostics artifact))
                   (true? (:clojure-seed-boundary? artifact))
                   (false? (:self-hosted? artifact))
                   (= (:semantic-id artifact)
                      (p15-s23-b4-wasm-artifact-id artifact))
                   (= (:artifact-id artifact)
                      (p15-s23-c11-mir-digest
                       {:kind (:kind artifact) :schema-version 1
                        :semantic-id (:semantic-id artifact)}))
                   (= (:actual-path-binding-id artifact)
                      (p15-s23-b4-wasm-actual-path-binding-id
                       (:semantic-id artifact)
                       (:actual-path-provenance artifact))))
      (p15-s23-b4-wasm-fail!
       "B4-MANIFEST" source-path artifact
       {:missing-fact :exact-frozen-b4-envelope-and-identities}))
    (let [wasm-packet (:wasm-packet preflight)
          b1-record (:b1 wasm-packet)
          expected-reconstruction (p15-s23-b4-wasm-reconstruct preflight)
          expected-lowering
          (p15-s23-b4-wasm-invoke-builder!
           p15-s23-b4-wasm-authority-token binding
           b1-record source-path expected-reconstruction)
          parser (p15-s23-b4-wasm-parse-module!
                  (get-in artifact [:raw-wasm :bytes])
                  expected-reconstruction)
          raw-bytes (byte-array
                     (map unchecked-byte
                          (get-in artifact [:raw-wasm :bytes])))
          content-hash (str "sha256:" (sha256-bytes-hex raw-bytes))
          expected-provenance
          {:source source-path
           :c11-source (get-in fresh [:provenance :actual-paths :c11-source])
           :c13-source (get-in wasm-packet
                               [:actual-path-provenance :c13-source])
           :c14-source (get-in wasm-packet
                               [:actual-path-provenance :c14-source])
           :b1-source (get-in wasm-packet
                              [:actual-path-provenance :b1-source])
           :b4-source (:source-path binding)
           :node p15-s23-b4-wasm-node-path}
          expected-contract-bindings
          (p15-s23-b4-wasm-contract-bindings fresh wasm-packet binding)
          contract-binding-id
          (:contract-binding-id expected-contract-bindings)
          expected-result (:expected-result expected-reconstruction)
          stdout-bytes (.getBytes
                        (str "B4NODE1:" expected-result "\n")
                        java.nio.charset.StandardCharsets/UTF_8)
          empty-bytes (byte-array 0)
          expected-node
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
           :expected-result expected-result :observed-result expected-result
           :repeat-result expected-result
           :stdout-hash (str "sha256:" (sha256-bytes-hex stdout-bytes))
           :stdout-byte-count (alength stdout-bytes)
           :stderr-hash (str "sha256:" (sha256-bytes-hex empty-bytes))
           :stderr-byte-count 0 :invocation-local-start-count 1
           :process-tree nil
           :atomic-tool-identity-binding? false
           :whole-process-tree-reaping-proved? false}
          expected-c11-verification
          {:status :passed :mir-id (:mir-id fresh)
           :checked-core-artifact-id (:source-core-artifact-id fresh)
           :semantic-replay-parity :passed
           :b1-preflight (:b1-preflight fresh)}]
      (when-not
       (and
        (= {:artifact-id (:artifact-id fresh) :mir-id (:mir-id fresh)
            :source-core-artifact-id (:source-core-artifact-id fresh)
            :verification-report-id
            (get-in fresh [:verification-report :report-id])
            :verification-report-hash
            (get-in fresh [:verification-report :report-hash])}
           (:c11 artifact))
        (= (p15-s23-b4-wasm-source-rule binding) (:source-rule artifact))
        (= expected-contract-bindings (:contract-bindings artifact))
        (= expected-provenance (:actual-path-provenance artifact))
        (= wasm-packet (:c13-c14-b1-packet artifact))
        (= (dissoc expected-lowering :wasm-bytes) (:lowering artifact))
        (= expected-reconstruction (:independent-reconstruction artifact))
        (= parser (:independent-parser artifact))
        (= {:bytes (:wasm-bytes expected-reconstruction)
            :content-hash content-hash
            :byte-count (count (:wasm-bytes expected-reconstruction))}
           (:raw-wasm artifact))
        (= {:artifact :gravity/wasm-backend-manifest
            :status :partial-bounded-executable-slice
            :target-kind :core-module :target :wasm32-unknown-unknown
            :feature-record p15-s23-b4-wasm-bounded-feature-policy
            :imports [] :exports [{:name "main" :kind :function :index 0}]
            :runtime-helpers [] :effects #{} :capabilities #{}
            :contract-binding-id contract-binding-id}
           (:b4-record artifact))
        (= {:artifact :gravity/content-addressed-artifact-record
            :logical-path "program.wasm"
            :retention :ephemeral-conformance-intent
            :content-hash content-hash
            :byte-count (count (:wasm-bytes expected-reconstruction))
            :contract-binding-id contract-binding-id}
           (:b13-record artifact))
        (= {:artifact :gravity/backend-conformance-report
            :status :bounded-experimental-slice
            :validator :pinned-node-20.9.0
            :expected-result expected-result :observed-result expected-result
            :tool-identity-race-residual :unclosed
            :process-tree-proof :bounded-captured-set-only
            :negative-diagnostics :covered-by-focused-tests
            :contract-binding-id contract-binding-id}
           (:b14-record artifact))
        (= {:artifact :gravity/backend-translation-validation
            :status :bounded-experimental-slice
            :gravity-source-lowering :reconstructed
            :raw-module-parser :passed :differential-execution :passed
            :contract-binding-id contract-binding-id}
           (:c18-record artifact))
        (= {:task :FL-P07-B4-PHASE1
            :subset
            :bounded-pure-scalar-forwarding-do-let-if-integer-comparisons
            :whole-b4? false :public? false :release? false
            :component-model? false :wit? false :wasi? false
            :memory? false :self-hosted? false
            :compile-to-any-target? false
            :atomic-tool-identity-binding? false
            :whole-process-tree-reaping-proved? false}
           (:scope artifact))
        (= expected-c11-verification (:c11-verification artifact))
        (= expected-node (:node-conformance artifact)))
       (p15-s23-b4-wasm-fail!
        "B4-MANIFEST" source-path artifact
        {:missing-fact :fresh-c11-source-proof-byte-and-tool-bindings}))
      :passed))))
