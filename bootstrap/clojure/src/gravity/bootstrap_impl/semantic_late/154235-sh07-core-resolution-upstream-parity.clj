; Semantic decomposition of committed HEAD reader line 154235.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-sh07-core-resolution-projection!-upstream-parity
 [state]
 (clojure.core/let
  [{:keys
    [source-path
     scope-index
     upstream-scopes
     projected-scope-by-upstream
     reference-index
     upstream-references
     resolved-references]}
   state]
  (when-not
   (and
    (= @scope-index (count upstream-scopes))
    (= (count @projected-scope-by-upstream) (count upstream-scopes))
    (= @reference-index (count upstream-references))
    (= (count upstream-references) (count resolved-references)))
   (throw
    (ex-info
     "SH-07 resolution projection did not consume SH-06 inputs"
     {:id "C6-VERIFY",
      :stage :core-lowering,
      :source-path source-path,
      :reason :sh06-resolution-projection-incomplete,
      :scopes [@scope-index (count upstream-scopes)],
      :references [@reference-index (count upstream-references) (count resolved-references)]})))))
