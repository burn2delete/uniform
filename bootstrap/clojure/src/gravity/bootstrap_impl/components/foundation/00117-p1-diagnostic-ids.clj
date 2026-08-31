

(def p1-diagnostic-ids
  ["P1-MISSING-PROFILE" "P1-AMBIGUOUS-PROFILE" "P1-EFFECT"
   "P1-CAPABILITY" "P1-MEMORY" "P1-RUNTIME" "P1-CROSS-IMPORT"
   "P1-MACRO" "P1-FACET" "P1-BACKEND"])

(def p1-diagnostic-messages
  {"P1-MISSING-PROFILE" "namespace is missing an active profile declaration"
   "P1-AMBIGUOUS-PROFILE" "namespace has conflicting profile declarations"
   "P1-EFFECT" "effect is outside the effective profile authority"
   "P1-CAPABILITY" "capability authority is missing or narrowed"
   "P1-MEMORY" "memory regime is illegal for the active profile"
   "P1-RUNTIME" "runtime assumption is unavailable for the active profile"
   "P1-CROSS-IMPORT" "cross-profile import lacks a facade or artifact boundary"
   "P1-MACRO" "macro output violates the caller profile"
   "P1-FACET" "facet is outside active profile support"
   "P1-BACKEND" "backend or target is ineligible for the profile manifest"})

(def p1-underlying-diagnostic-map
  {"L3-NS-MISSING" "P1-MISSING-PROFILE"
   "L3-PROFILE-MULTIPLE" "P1-AMBIGUOUS-PROFILE"
   "L3-CROSS-PROFILE" "P1-CROSS-IMPORT"
   "L3-EFFECT-WIDEN" "P1-EFFECT"
   "L6-EFFECT-PROFILE" "P1-EFFECT"
   "L6-EFFECT-UNDECLARED" "P1-EFFECT"
   "L3-CAPABILITY-MISSING" "P1-CAPABILITY"
   "L6-EFFECT-CAPABILITY" "P1-CAPABILITY"
   "L15-CAPABILITY-MISSING" "P1-CAPABILITY"
   "L15-PROFILE" "P1-CAPABILITY"
   "L10-HIDDEN-ALLOC" "P1-MEMORY"
   "L10-RAW-SAFE" "P1-MEMORY"
   "L10-MMIO-CAP" "P1-MEMORY"
   "SAFE2-PROFILE" "P1-MEMORY"
   "L11-SCHEDULER" "P1-RUNTIME"
   "L11-TASK-SCOPE" "P1-RUNTIME"
   "L4-GENERATED-PROFILE" "P1-MACRO"
   "L4-GENERATED-UNSAFE" "P1-MACRO"
   "L14-PROFILE" "P1-FACET"
   "B1-TARGET-UNSUPPORTED" "P1-BACKEND"})