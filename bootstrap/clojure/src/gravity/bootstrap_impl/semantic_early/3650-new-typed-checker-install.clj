; Semantic decomposition of HEAD reader line 3650.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-early-checker-initial-state
 []
 (clojure.core/reduce
  (clojure.core/fn
   [state__1386__auto__ key__1387__auto__]
   (clojure.core/assoc state__1386__auto__ key__1387__auto__ []))
  {}
  (clojure.core/concat
   semantic-early-checker-core-keys
   semantic-early-checker-alternatives-keys
   semantic-early-checker-interop-keys
   semantic-early-checker-safe-memory-keys
   semantic-early-checker-safety-early-keys
   semantic-early-checker-safety-late-keys)))

(defn
 new-typed-checker
 []
 (atom (semantic-early-checker-initial-state)))

(clojure.core/doseq
 [symbol__1382__auto__
  '[semantic-early-checker-core-keys
    semantic-early-checker-alternatives-keys
    semantic-early-checker-interop-keys
    semantic-early-checker-safe-memory-keys
    semantic-early-checker-safety-early-keys
    semantic-early-checker-safety-late-keys
    semantic-early-checker-initial-state]]
 (clojure.core/ns-unmap clojure.core/*ns* symbol__1382__auto__))
