(defn
 sh05-authenticated-source-snapshot!
 "Check current bounded no-follow source identity against nested C2."
 [source-path c2-artifact phase legacy?]
 (gravity.macro/source-snapshot!
  source-path
  c2-artifact
  phase
  legacy?
  ^{:line 148, :column 101}
  (macro-stage-operations)))

(defn-
 sh05-source-snapshot-stable!
 [source-path before c2-artifact legacy?]
 (gravity.macro/source-stable!
  source-path
  before
  c2-artifact
  legacy?
  ^{:line 148, :column 101}
  (macro-stage-operations)))

(defn-
 sh05-authenticated-c3-parent!
 "Validate the complete C3 owner artifact before SH-05 consumes it.\n\n  The C3 artifact id is only one witness.  SH-05 also requires the C3 verifier,\n  capability proof, accepted status, and source/path provenance to agree.  This\n  keeps the direct-parent edge honest while preserving the established C4\n  boundary diagnostic for an invalid parent.\n  "
 [source-path c3-artifact]
 (gravity.macro/accept-syntax-parent!
  source-path
  c3-artifact
  ^{:line 148, :column 101}
  (macro-stage-operations)))
