(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn- p15-s23-b2-c17-process-exit-code
  [value]
  (gravity.backend.c/process-exit-code value))

(defn p15-s23-b2-c17-proof-to-c-assumption-record
  [b1-record mir operations operation-records return-id return-value-name semantic-result]
  (gravity.backend.c/proof-to-c-assumption-record #'p15-s23-b2-c17-operation-proof-bindings #'p15-s23-b2-c17-discard-cast-records b1-record mir operations operation-records return-id return-value-name semantic-result))

(defn- p15-s23-b2-c17-assert-exact!
  [source-path expected actual missing-fact]
  (gravity.backend.c/assert-exact! #'p15-s23-c6c10-strict-first-mismatch #'p15-s23-c-backend-fail! source-path expected actual missing-fact))

(defn- p15-s23-b2-c17-independent-raw
  [source-path c11-artifact c-packet]
  (let [b1-record (:b1 c-packet)
        payload (:bounded-lowering-payload b1-record)
        mir (:mir payload)
        preflight
        (p15-s23-c13-c14-b1-c-preflight! source-path c11-artifact)]
    (gravity.backend.c/assemble-native
     {:operation-record #'p15-s23-b2-c17-operation-record
      :return-id #'p15-s23-b2-c17-return-id
      :value-name #'p15-s23-b2-c17-value-name
      :source-text #'p15-s23-b2-c17-source-text
      :process-exit-code #'p15-s23-b2-c17-process-exit-code
      :source-debug-map-record #'p15-s23-b2-c17-source-debug-map-record
      :assert-exact! #'p15-s23-b2-c17-assert-exact!
      :build-manifest-record #'p15-s23-b2-c17-build-manifest-record
      :abi-layout-record #'p15-s23-b2-c17-abi-layout-record
      :runtime-helper-record #'p15-s23-b2-c17-runtime-helper-record
      :proof-to-c-assumption-record #'p15-s23-b2-c17-proof-to-c-assumption-record
      :fail! #'p15-s23-c-backend-fail!
      :policy-record #'p15-s23-b2-c17-policy-record
      :dialect-selection-record #'p15-s23-b2-c17-dialect-selection-record
      :header-text #'p15-s23-b2-c17-header-text}
     source-path c11-artifact c-packet b1-record payload mir preflight))))
