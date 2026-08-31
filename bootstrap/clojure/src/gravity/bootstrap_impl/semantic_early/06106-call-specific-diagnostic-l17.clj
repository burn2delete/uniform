; Semantic decomposition of HEAD reader line 6106.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-call-specific-diagnostic-l17!
 [operator node]
 (case
  operator
  alttype/provider
  (typed-diagnostic!
   "L17-PROVIDER"
   "no selected alternative type provider can check this namespace"
   node
   "Select a deterministic type provider declared for the active profile, target, facets, and solver dependencies."
   {:provider-id nil,
    :provider-version nil,
    :active-profile (:profile node),
    :active-target (:target node),
    :l5-rule :type-provider-selection})
  alttype/lowering
  (typed-diagnostic!
   "L17-LOWERING"
   "alternative type facts cannot lower to the common typed core"
   node
   "Emit expression types, binding types, function effects, capability requirements, casts, proof refs, and source-span mappings."
   {:provider-id 'research.types/refinement,
    :typed-core-version :typed-core-v1,
    :missing-fields #{:function-effects :capability-requirements},
    :l5-rule :typed-core-artifact})
  alttype/soundness
  (typed-diagnostic!
   "L17-SOUNDNESS"
   "alternative type provider claims a profile without soundness evidence"
   node
   "Attach proof, theorem, conformance test, or audit evidence for the claimed safe profile."
   {:provider-id 'research.types/refinement,
    :claimed-profile (:profile node),
    :soundness-claim :conservative-extension,
    :evidence nil})
  alttype/effect-erasure
  (typed-diagnostic!
   "L17-EFFECT-ERASURE"
   "alternative function type output erases effect information"
   node
   "Preserve function effect sets in typed core or reject the provider for this profile."
   {:provider-id 'research.types/refinement,
    :function-type 'example/handler,
    :lost-effects #{:network/http},
    :l6-rule :effect-preservation})
  alttype/capability-erasure
  (typed-diagnostic!
   "L17-CAPABILITY-ERASURE"
   "alternative function type output erases capability requirements"
   node
   "Preserve capability requirements and capability value metadata for L15 provider checks."
   {:provider-id 'research.types/refinement,
    :function-type 'example/handler,
    :lost-capabilities #{:network/client},
    :l15-rule :capability-preservation})
  alttype/ownership-fact
  (typed-diagnostic!
   "L17-OWNERSHIP-FACT"
   "alternative checker omits required memory or resource facts"
   node
   "Export ownership, borrow, region, linear, initialization, allocation, and resource facts or insert checks/reject the program."
   {:provider-id 'research.types/refinement,
    :missing-facts #{:region :borrow :linear},
    :l10-rule :memory-fact-export})
  alttype/gradual-boundary-error
  (typed-diagnostic!
   "L17-GRADUAL-BOUNDARY"
   "gradual or dynamic boundary is illegal or unrecorded"
   node
   "Record expected type, runtime check, failure type, source span, and blame information in typed core."
   {:provider-id 'research.types/refinement,
    :source-type "Dynamic",
    :target-type "User",
    :boundary-record nil})
  alttype/unsafe-cast
  (typed-diagnostic!
   "L17-UNSAFE-CAST"
   "unchecked cast is treated as safe optimization evidence"
   node
   "Keep unchecked casts inside explicit unsafe islands and never use them as safe proof artifacts."
   {:provider-id 'research.types/refinement,
    :cast-id :cast/raw-user,
    :safe-evidence? false,
    :unsafe-island nil})
  alttype/domain-fact-error
  (typed-diagnostic!
   "L17-DOMAIN-FACT"
   "domain type facts fail to cross the facet boundary"
   node
   "Serialize schema, EFIR, hardware, workflow, agent, or query facts into typed artifacts at the boundary."
   {:provider-id 'research.types/refinement,
    :facet-id :gravity.facet.schema,
    :missing-domain-facts #{:taint :schema/User}})
  alttype/diagnostic-map-error
  (typed-diagnostic!
   "L17-DIAGNOSTIC-MAP"
   "type diagnostic cannot be mapped through generated code to source"
   node
   "Retain macro expansion provenance, generated-origin chains, source spans, and provider fact ids in diagnostic records."
   {:provider-id 'research.types/refinement,
    :diagnostic-id :diag/type-1,
    :generated-origin nil,
    :source-span nil})
  semantic-early-call-specific-diagnostic-unhandled))
