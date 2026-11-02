(defn
 sh05-contain-coordinator-operation!
 [source-path responsibility operation]
 (gravity.macro/contain-operation!
  source-path
  responsibility
  operation
  ^{:line 112, :column 65}
  (macro-stage-operations)))

(defn
 sh05-resolve-expansion-template!
 [source-path expansion-template resolved-digests]
 (gravity.macro/resolve-expansion-template!
  source-path
  expansion-template
  resolved-digests
  ^{:line 112, :column 65}
  (macro-stage-operations)))

(defn
 sh05-run-macro-request!
 [source-path binding request]
 (gravity.macro/expand
  source-path
  binding
  request
  ^{:line 112, :column 65}
  (macro-stage-operations)))

(defn
 sh05-source-syntax-stream
 [c3-artifact]
 (gravity.macro/source-syntax-stream c3-artifact))

(defn
 sh05-expanded-form
 [form]
 (gravity.macro/expanded-form
  form
  ^{:line 148, :column 101}
  (macro-stage-operations)))

(defn
 sh05-package-trace
 [run profile target]
 (gravity.macro/trace-record run profile target))

(defn
 sh05-expanded-syntax-object
 [syntax form run]
 (gravity.macro/expanded-syntax-object
  syntax
  form
  run
  ^{:line 148, :column 101}
  (macro-stage-operations)))

(defn
 sh05-expanded-graph
 [expanded-stream trace]
 (gravity.macro/expanded-graph expanded-stream trace))

(defn
 sh05-expanded-graph-valid?
 [graph]
 (gravity.macro/expanded-graph-valid? graph))
