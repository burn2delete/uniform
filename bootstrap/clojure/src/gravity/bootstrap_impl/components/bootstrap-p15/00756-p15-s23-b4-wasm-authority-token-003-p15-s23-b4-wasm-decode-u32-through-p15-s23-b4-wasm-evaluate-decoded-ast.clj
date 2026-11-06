(let [p15-s23-b4-wasm-authority-token (nth __gravity_bootstrap_lexical_values_136338 0)
      p15-s23-b4-wasm-node-state (nth __gravity_bootstrap_lexical_values_136338 1)]
(defn p15-s23-b4-wasm-decode-u32
  [bytes offset limit]
  (gravity.backend.wasm/decode-u32 #'p15-s23-b4-wasm-canonical-bounded-octet! #'p15-s23-b4-wasm-parser-u32-bytes #'p15-s23-b4-wasm-fail! bytes offset limit))

(defn p15-s23-b4-wasm-decode-s32
  [bytes offset limit]
  (gravity.backend.wasm/decode-s32 #'p15-s23-b4-wasm-canonical-bounded-octet! #'p15-s23-b4-wasm-parser-s32-bytes #'p15-s23-b4-wasm-fail! bytes offset limit))

(declare p15-s23-b4-wasm-parse-instructions!)

(defn p15-s23-b4-wasm-parse-instructions!
  [bytes offset limit stack initialized stop-opcodes if-count]
  (gravity.backend.wasm/parse-instructions!
   {:decode-u32 #'p15-s23-b4-wasm-decode-u32
    :decode-s32 #'p15-s23-b4-wasm-decode-s32
    :fail! #'p15-s23-b4-wasm-fail!
    :parse-instructions! #'p15-s23-b4-wasm-parse-instructions!
    :intersection #'set/intersection}
   bytes offset limit stack initialized stop-opcodes if-count))

(declare p15-s23-b4-wasm-evaluate-decoded-ast))
