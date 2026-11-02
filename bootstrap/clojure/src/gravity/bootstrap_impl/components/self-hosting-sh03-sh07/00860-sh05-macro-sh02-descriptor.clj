(defn
 sh05-macro-sh02-descriptor
 [source-path binding summary]
 (gravity.macro/envelope-descriptor
  source-path
  binding
  summary
  ^{:line 148, :column 101}
  (macro-stage-operations)))

(def
 sh05-macro-artifact-keys
 #{:expanded-defn-count
   :capability-based-proof
   :macro-expansion-trace-id
   :diagnostics
   :expanded-syntax-stream-id
   :expanded-forms
   :task
   :artifact-id
   :expanded-syntax-stream
   :expanded-syntax-graph
   :document-set
   :governing-document
   :execution-boundary
   :macro-environment
   :status
   :slice
   :kind
   :generated-origin-source-map
   :gravity-macro-boundary
   :provenance
   :sh04-syntax-artifact
   :pass
   :macro-expansion-trace})

(defn
 sh05-path-neutral-semantic-value
 [value]
 (gravity.macro/path-neutral-value
  value
  ^{:line 148, :column 101}
  (macro-stage-operations)))

(def sh05-identity-chunk-width 16)

(defn
 sh05-ordered-identity-chunks
 [values projector]
 (gravity.macro/identity-chunks
  values
  projector
  ^{:line 148, :column 101}
  (macro-stage-operations)))

(defn
 sh05-macro-run-semantic-summary
 [run]
 (gravity.macro/run-summary run))

(defn
 sh05-macro-artifact-semantic-payload
 [artifact]
 (gravity.macro/artifact-payload
  artifact
  ^{:line 148, :column 101}
  (macro-stage-operations)))

(defn
 sh05-macro-artifact-identity-input
 [artifact]
 (gravity.macro/artifact-identity-input
  artifact
  ^{:line 148, :column 101}
  (macro-stage-operations)))

(defn
 sh05-macro-artifact-id
 [artifact]
 (gravity.macro/artifact-id
  artifact
  ^{:line 148, :column 101}
  (macro-stage-operations)))

(defn
 sh05-expanded-syntax-stream-identity-input
 [expanded-stream]
 (gravity.macro/stream-identity-input
  expanded-stream
  ^{:line 148, :column 101}
  (macro-stage-operations)))

(defn
 sh05-expanded-syntax-stream-id
 [source-path expanded-stream]
 (gravity.macro/stream-id
  source-path
  expanded-stream
  ^{:line 148, :column 101}
  (macro-stage-operations)))
