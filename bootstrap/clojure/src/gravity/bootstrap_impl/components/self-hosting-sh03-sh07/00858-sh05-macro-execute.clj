(defn
 sh05-macro-execute!
 [source-path binding function arguments]
 (gravity.macro/execute-plan!
  source-path
  binding
  function
  arguments
  ^{:line 112, :column 65}
  (macro-stage-operations)))

(defn
 sh05-resolve-request-preimage!
 [source-path request resolved-digests]
 (gravity.macro/resolve-digest-preimage!
  source-path
  request
  resolved-digests
  ^{:line 112, :column 65}
  (macro-stage-operations)))

(defn
 sh05-macro-resolve-digests!
 [source-path digest-requests]
 (gravity.macro/resolve-digests!
  source-path
  digest-requests
  ^{:line 112, :column 65}
  (macro-stage-operations)))

(defn
 sh05-deep-merge
 [left right]
 (gravity.macro/merge-request
  left
  right
  ^{:line 148, :column 101}
  (macro-stage-operations)))

(defn sh05-defn-form? [form] (gravity.macro/defn-form? form))

(defn sh05-defmacro-form? [form] (gravity.macro/defmacro-form? form))

(defn
 sh05-semantic-component-id
 [call-site-syntax-id role value]
 (gravity.macro/semantic-component-id
  call-site-syntax-id
  role
  value
  ^{:line 148, :column 101}
  (macro-stage-operations)))

(defn
 sh05-default-macro-request
 [source-path c3-artifact module syntax form]
 (gravity.macro/request
  source-path
  c3-artifact
  module
  syntax
  form
  ^{:line 148, :column 101}
  (macro-stage-operations)))

(defn
 sh05-macro-raise-rejection!
 [source-path raw resolved-digests]
 (gravity.macro/raise-rejection!
  source-path
  raw
  resolved-digests
  ^{:line 112, :column 65}
  (macro-stage-operations)))
