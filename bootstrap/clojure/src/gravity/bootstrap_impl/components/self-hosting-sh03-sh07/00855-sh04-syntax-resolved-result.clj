(defn- syntax-with-source-index-transaction
  [artifact descriptor-operation thunk]
  (binding [*sh04-syntax-form-index-transaction*
            (when (identical? descriptor-operation #'sh04-syntax-source-descriptor)
              {:artifact artifact :thread (Thread/currentThread) :slot (volatile! nil)})]
    (thunk)))

(defn-
 sh04-syntax-build-from-fresh-c2!
 ([source-path c2-artifact integrity-report]
  (sh04-syntax-build-from-fresh-c2!
   source-path
   c2-artifact
   integrity-report
   nil))
 ([source-path c2-artifact integrity-report invocation]
  (gravity.syntax/assemble-fresh-reader!
   source-path
   c2-artifact
   integrity-report
   invocation
   (syntax-stage-assembly-operations))))
(defn
 sh04-syntax-resolved-result!
 [source-path c2-artifact]
 (gravity.syntax/assemble-reader!
  source-path
  c2-artifact
  (syntax-stage-assembly-operations)))
