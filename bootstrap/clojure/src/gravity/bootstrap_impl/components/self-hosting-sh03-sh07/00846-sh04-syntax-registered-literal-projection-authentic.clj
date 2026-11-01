(defn
 sh04-syntax-registered-literal-projection-authentic?
 [c2-view gravity-boundary]
 (gravity.syntax/registered-literal-projection-authentic?
  c2-view
  gravity-boundary
  (syntax-stage-reader-view-operations)))
