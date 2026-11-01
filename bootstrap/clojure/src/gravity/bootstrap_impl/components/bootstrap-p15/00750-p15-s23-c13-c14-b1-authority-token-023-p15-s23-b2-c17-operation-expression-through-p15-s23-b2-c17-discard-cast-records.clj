(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn p15-s23-b2-c17-operation-expression
  [operation operations operation-index]
  (gravity.backend.c/operation-expression #'p15-s23-b2-c17-value-name #'p15-s23-b2-c17-constant-expression #'p15-s23-b2-c17-forwarded-opcode? operation operations operation-index))

(defn p15-s23-b2-c17-operation-record
  [operation operations operation-index]
  (gravity.backend.c/operation-record #'p15-s23-b2-c17-value-name #'p15-s23-b2-c17-operation-expression operation operations operation-index))

(defn p15-s23-b2-c17-return-id
  [function block-order]
  (gravity.backend.c/return-id function block-order))

(defn p15-s23-b2-c17-source-text
  [operation-records return-value-name]
  (gravity.backend.c/source-text operation-records return-value-name))

(defn p15-s23-b2-c17-abi-layout-record
  [b1-record]
  (gravity.backend.c/abi-layout-record b1-record))

(defn p15-s23-b2-c17-runtime-helper-record
  [b1-record]
  (gravity.backend.c/runtime-helper-record b1-record))

(defn- p15-s23-b2-c17-source-debug-entries
  [operations operation-records]
  (gravity.backend.c/source-debug-entries operations operation-records))

(defn- p15-s23-b2-c17-source-debug-map-record
  [b1-record operations operation-records return-id return-value-name]
  (gravity.backend.c/source-debug-map-record #'p15-s23-b2-c17-source-debug-entries b1-record operations operation-records return-id return-value-name))

(defn- p15-s23-b2-c17-operation-proof-bindings
  [operations operation-records]
  (gravity.backend.c/operation-proof-bindings operations operation-records))

(defn- p15-s23-b2-c17-discard-cast-records
  [operations operation-records]
  (gravity.backend.c/discard-cast-records operations operation-records))

)
