; Semantic decomposition of HEAD reader line 12567.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-typed-artifact-pass
 []
 {:name :type-effect-capability-check,
  :input :core-ast,
  :output :typed-effected-core,
  :requires
  [:reader :namespace-analyzer :macro-expansion :core-lowering],
  :preserves
  [:source-spans
   :generated-origin
   :profile
   :types
   :effects
   :capabilities],
  :emits
  (clojure.core/vec
   (clojure.core/concat
    semantic-early-typed-pass-emits-01
    semantic-early-typed-pass-emits-02
    semantic-early-typed-pass-emits-03
    semantic-early-typed-pass-emits-04
    semantic-early-typed-pass-emits-05
    semantic-early-typed-pass-emits-06)),
  :rejects
  (clojure.core/vec
   (clojure.core/concat
    semantic-early-typed-pass-rejects-01
    semantic-early-typed-pass-rejects-02
    semantic-early-typed-pass-rejects-03
    semantic-early-typed-pass-rejects-04
    semantic-early-typed-pass-rejects-05
    semantic-early-typed-pass-rejects-06))})
