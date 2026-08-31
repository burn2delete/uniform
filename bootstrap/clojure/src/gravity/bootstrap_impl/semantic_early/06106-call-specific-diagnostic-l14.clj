; Semantic decomposition of HEAD reader line 6106.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-call-specific-diagnostic-l14!
 [operator node]
 (case
  operator
  facet/not-active
  (typed-diagnostic!
   "L14-FACET-NOT-ACTIVE"
   "facet form is used without namespace-scoped activation"
   node
   "Activate the facet in namespace metadata or emit activation with macro provenance.")
  facet/ambiguous
  (typed-diagnostic!
   "L14-FACET-AMBIGUOUS"
   "multiple active facets claim the same surface form"
   node
   "Disambiguate the facet form through aliases or explicit activation.")
  facet/profile
  (typed-diagnostic!
   "L14-PROFILE"
   "facet is used outside its supported profile set"
   node
   "Use a facet supported by the active profile or reject before backend lowering.")
  facet/build-effect
  (typed-diagnostic!
   "L14-BUILD-EFFECT"
   "facet expansion or artifact emission lacks a required build effect grant"
   node
   "Declare and grant the facet build effect in namespace or package policy.")
  facet/capability
  (typed-diagnostic!
   "L14-CAPABILITY"
   "facet requires a missing runtime or compile-time capability"
   node
   "Grant the facet capability or use a facet implementation that does not require it.")
  facet/lowering
  (typed-diagnostic!
   "L14-LOWERING"
   "facet output cannot lower to declared Gravity core or domain IR"
   node
   "Emit checked Gravity forms, declared domain IR, or both.")
  facet/domain-check
  (typed-diagnostic!
   "L14-DOMAIN-CHECK"
   "facet-local domain validation failed"
   node
   "Fix the domain rule violation or emit a proof artifact accepted by the facet checker.")
  facet/generated-code-invalid
  (typed-diagnostic!
   "L14-GENERATED-CODE"
   "facet-generated Gravity code fails ordinary checking"
   node
   "Validate generated forms through the normal type, effect, capability, memory, and safety pipeline.")
  facet/ir-schema
  (typed-diagnostic!
   "L14-IR-SCHEMA"
   "facet IR is invalid, stale, or incompatible with its artifact schema"
   node
   "Emit versioned serializable domain IR with a compatible schema version.")
  facet/composition-invalid
  (typed-diagnostic!
   "L14-COMPOSITION"
   "facet composition crosses an undeclared boundary or hides effects"
   node
   "Declare the composition boundary and preserve both facets' effects, capabilities, and artifacts.")
  facet/privacy-drop
  (typed-diagnostic!
   "L14-PRIVACY-BOUNDARY"
   "facet lowering or composition drops a private input, witness, credential, or disclosure policy"
   node
   "Carry the privacy label, witness provenance, reveal reason, and public-output schema across the facet edge.")
  semantic-early-call-specific-diagnostic-unhandled))
