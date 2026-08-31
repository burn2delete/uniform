; Semantic decomposition of HEAD reader line 6106.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-call-specific-diagnostic-l16!
 [operator node]
 (case
  operator
  altmacro/provider
  (typed-diagnostic!
   "L16-PROVIDER"
   "alternative macro provider is missing, ambiguous, or unsupported"
   node
   "Select a macro provider declared for the active profile, target, facets, and build grants."
   {:provider-id nil,
    :provider-version nil,
    :macro-symbol 'example/macro,
    :expansion-phase :provider-selection,
    :active-profile (:profile node),
    :l4-rule :macro-binding-resolution})
  altmacro/equivalence
  (typed-diagnostic!
   "L16-EQUIVALENCE"
   "alternative expansion differs from the L4 reference contract"
   node
   "Emit an expansion structurally equivalent to the reference engine up to allowed alpha-renaming."
   {:provider-id 'custom.macros/typed-expander,
    :provider-version "fixture-1",
    :macro-symbol 'example/macro,
    :expansion-phase :reference-comparison,
    :l4-rule :expansion-result})
  altmacro/syntax-object-loss
  (typed-diagnostic!
   "L16-SYNTAX-OBJECT"
   "alternative syntax object representation loses required observable data"
   node
   "Preserve source spans, metadata, lexical context, hygiene marks, and generated-origin chains."
   {:provider-id 'custom.macros/typed-expander,
    :provider-version "fixture-1",
    :macro-symbol 'example/macro,
    :expansion-phase :syntax-serialization,
    :l4-rule :syntax-object-contract})
  altmacro/hygiene
  (typed-diagnostic!
   "L16-HYGIENE"
   "alternative macro expansion hides capture or compares identifiers illegally"
   node
   "Use hygienic identifiers by default and record explicit capture operations in generated origin."
   {:provider-id 'custom.macros/typed-expander,
    :provider-version "fixture-1",
    :macro-symbol 'example/capture,
    :expansion-phase :hygiene,
    :l4-rule :hygiene})
  altmacro/phase
  (typed-diagnostic!
   "L16-PHASE"
   "macro expansion captures a runtime-only value"
   node
   "Pass serializable compile-time inputs or defer runtime-only access to generated code."
   {:provider-id 'custom.macros/typed-expander,
    :provider-version "fixture-1",
    :macro-symbol 'example/runtime-capture,
    :expansion-phase :macro-invocation,
    :l4-rule :phase-separation})
  altmacro/build-effect
  (typed-diagnostic!
   "L16-BUILD-EFFECT"
   "alternative macro expansion performs an undeclared or ungranted build effect"
   node
   "Declare the build effect, provider, grant id, replay policy, and redaction policy in the macro trace."
   {:provider-id 'custom.macros/typed-expander,
    :provider-version "fixture-1",
    :macro-symbol 'example/build,
    :expansion-phase :macro-invocation,
    :build-effects #{:build/read-file},
    :l4-rule :build-effects})
  altmacro/hermetic
  (typed-diagnostic!
   "L16-HERMETIC"
   "alternative macro expansion cannot be replayed in hermetic mode"
   node
   "Record deterministic inputs, output digests, replay records, and secret redaction policy."
   {:provider-id 'custom.macros/typed-expander,
    :provider-version "fixture-1",
    :macro-symbol 'example/hermetic,
    :expansion-phase :replay,
    :build-effects #{:build/read-file},
    :l4-rule :hermetic-expansion})
  altmacro/cache
  (typed-diagnostic!
   "L16-CACHE"
   "incremental macro cache entry is reused under incompatible inputs"
   node
   "Invalidate cache entries when source, provider, profile, target, facet, grant, or compiler inputs change."
   {:provider-id 'custom.macros/typed-expander,
    :provider-version "fixture-1",
    :macro-symbol 'example/cached,
    :expansion-phase :cache-decision,
    :l4-rule :deterministic-expansion})
  altmacro/facet
  (typed-diagnostic!
   "L16-FACET"
   "facet-aware macro dispatch bypasses the L14 facet system"
   node
   "Route facet forms through namespace-scoped activation, ambiguity checks, domain IR, and ordinary generated-code validation."
   {:provider-id 'custom.macros/typed-expander,
    :provider-version "fixture-1",
    :macro-symbol 'example/facet,
    :expansion-phase :facet-dispatch,
    :l4-rule :facet-boundary})
  altmacro/generated
  (typed-diagnostic!
   "L16-GENERATED"
   "alternative macro generated syntax fails normal Gravity validation"
   node
   "Validate generated forms through syntax, type, effect, capability, memory, profile, and safety checks."
   {:provider-id 'custom.macros/typed-expander,
    :provider-version "fixture-1",
    :macro-symbol 'example/generated,
    :expansion-phase :generated-syntax-validation,
    :l4-rule :generated-form-validation})
  semantic-early-call-specific-diagnostic-unhandled))
