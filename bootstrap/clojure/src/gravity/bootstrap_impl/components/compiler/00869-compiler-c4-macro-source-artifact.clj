;; This final adapter intentionally replaces the earlier legacy compiler binding.
#_{:clj-kondo/ignore [:redefined-var]}


(defn compiler-c4-macro-source-artifact
 [source-path source-text]
 (gravity.macro/compile-source
  source-path
  source-text
  ^{:line 148, :column 101}
  (macro-stage-operations)))
