

(defn p15-s23-c11-mir-runtime-check-fact-record
  [checked-core node fact-kind]
  (gravity.mir/runtime-check-fact-record #'p15-s23-c11-mir-runtime-check-fact-id #'p15-s23-c11-mir-runtime-check-operation-id #'p15-s23-c11-mir-runtime-check-token-id checked-core node fact-kind))

(defn p15-s23-c11-mir-add-runtime-check-facts
  [checked-core fact-kind table]
  (gravity.mir/add-runtime-check-facts #'p15-s23-c11-mir-runtime-check-fact-id #'p15-s23-c11-mir-runtime-check-fact-record checked-core fact-kind table))

(defn p15-s23-c11-mir-reindex-fact-table
  [nodes table id-key suffix]
  (gravity.mir/reindex-fact-table nodes table id-key suffix))

(defn p15-s23-c11-mir-profile-target-table
  [checked-core]
  (gravity.mir/profile-target-table checked-core))

(defn p15-s23-c11-mir-capability-proof-table
  [checked-core]
  (gravity.mir/capability-proof-table checked-core))

(defn p15-s23-c11-mir-safety-proof-table
  [checked-core]
  (gravity.mir/safety-proof-table checked-core))

(defn p15-s23-c11-mir-pass-contract-record
  []
  (gravity.mir/pass-contract-record))

(defn p15-s23-c11-mir-canonical-block-order
  [mir]
  (gravity.mir/canonical-block-order mir))

(defn p15-s23-c11-mir-operation-sequence
  [mir]
  (gravity.mir/operation-sequence #'p15-s23-c11-mir-canonical-block-order mir))

(defn p15-s23-c11-mir-path-prefix?
  [prefix path]
  (gravity.mir/path-prefix? prefix path))
