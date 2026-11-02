

(defn
 sh05-macro-trace-identity-input
 [trace]
 (gravity.macro/trace-identity-input
  trace
  ^{:line 148, :column 101}
  (macro-stage-operations)))

(defn
 sh05-macro-trace-id
 [source-path trace]
 (gravity.macro/trace-id
  source-path
  trace
  ^{:line 148, :column 101}
  (macro-stage-operations)))

(defn
 sh05-macro-envelope-summary
 [artifact]
 (gravity.macro/envelope-summary
  artifact
  ^{:line 148, :column 101}
  (macro-stage-operations)))

(defn
 sh05-macro-gravity-verifier-report
 [source-path boundary]
 (gravity.macro/replay-report
  source-path
  boundary
  ^{:line 148, :column 101}
  (macro-stage-operations)))
