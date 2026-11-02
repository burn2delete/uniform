(let [p15-s23-b4-wasm-authority-token (nth __gravity_bootstrap_lexical_values_136338 0)
      p15-s23-b4-wasm-node-state (nth __gravity_bootstrap_lexical_values_136338 1)]
(defn p15-s23-b4-wasm-operation-bytes
  [operation operations-by-id operation-index]
  (gravity.backend.wasm/operation-bytes #'p15-s23-b4-wasm-s32-leb #'p15-s23-b4-wasm-constant-i32 #'p15-s23-b4-wasm-forwarded? #'p15-s23-b4-wasm-local #'p15-s23-b4-wasm-comparison-opcodes #'p15-s23-b4-wasm-fail! operation operations-by-id operation-index))

(defn p15-s23-b4-wasm-operation-stream
  [operations all-by-id operation-index]
  (gravity.backend.wasm/operation-stream #'p15-s23-b4-wasm-operation-bytes operations all-by-id operation-index))

(defn p15-s23-b4-wasm-function-bytes
  [input0]
  (gravity.backend.wasm/function-bytes #'p15-s23-b4-wasm-operation-stream #'p15-s23-b4-wasm-local #'p15-s23-b4-wasm-fail! input0))
(alter-meta! #'p15-s23-b4-wasm-function-bytes assoc :arglists (quote ([{:keys [function block-order operations operation-index]}])))

(defn p15-s23-b4-wasm-evaluate
  [input0]
  (gravity.backend.wasm/evaluate #'p15-s23-b4-wasm-constant-i32 #'p15-s23-b4-wasm-forwarded? input0))
(alter-meta! #'p15-s23-b4-wasm-evaluate assoc :arglists (quote ([{:keys [function block-order operations]}])))

(defn p15-s23-b4-wasm-reconstruct
  [preflight]
  (gravity.backend.wasm/reconstruct #'p15-s23-b4-wasm-u32-leb #'p15-s23-b4-wasm-function-bytes #'p15-s23-b4-wasm-section #'p15-s23-b4-wasm-evaluate preflight))

(defn p15-s23-b4-wasm-parser-u32-bytes
  [value]
  (gravity.backend.wasm/parser-u32-bytes value))

(defn p15-s23-b4-wasm-parser-s32-bytes
  [value]
  (gravity.backend.wasm/parser-s32-bytes value))

(defn- p15-s23-b4-wasm-canonical-bounded-octet!
  [value missing-fact]
  (gravity.backend.wasm/canonical-bounded-octet! #'p15-s23-b4-wasm-fail! value missing-fact))

)
