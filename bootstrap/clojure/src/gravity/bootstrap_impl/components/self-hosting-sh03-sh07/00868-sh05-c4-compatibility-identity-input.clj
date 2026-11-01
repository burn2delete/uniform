(defn
 sh05-c4-compatibility-identity-input
 [artifact]
 (gravity.macro/c4-identity-input artifact))

(defn
 sh05-c4-compatibility-artifact-id
 [artifact]
 (gravity.macro/c4-artifact-id
  artifact
  ^{:line 148, :column 101}
  (macro-stage-operations)))

(defn
 sh05-c4-compatibility-artifact
 [source-path sh05]
 (gravity.macro/c4-artifact
  source-path
  sh05
  ^{:line 148, :column 101}
  (macro-stage-operations)))

(defn
 sh05-authoritative-compiler-source-path?
 [source-path]
 (gravity.macro/compiler-source-path?
  source-path
  ^{:line 148, :column 101}
  (macro-stage-operations)))

(defn
 sh05-form-contains-legacy-macro-position?
 [form]
 (gravity.macro/legacy-position?
  form
  ^{:line 148, :column 101}
  (macro-stage-operations)))

(defn
 sh05-bounded-authoritative-source?
 [source-path module forms]
 (gravity.macro/source-eligible?
  source-path
  module
  forms
  ^{:line 148, :column 101}
  (macro-stage-operations)))
