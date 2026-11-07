# SAFE6 unsafe-audit records and executable presence checks

## Scope and governing contracts

This scope precedes implementation and an early draft PR. D1, D2, D3, D8,
D9, SAFE1, SAFE6, SAFE15, C1, C6, C7, C8, C10, C15, BOOT8 and TEST2 govern
the slice. Starting source is
`087b7d2cccec0c209e9c1d14467f2efe2bdf5a37`, tree
`3b6da8cc73dca4e0d592cef273e22f426072f9cd`. Published main
`aa6af9bddd96776a3d59d8c920ab0e4a4d8839b0` is already an ancestor.
Earlier SAFE1, Runtime or Diagnostics acceptance does not transfer.

The preceding SAFE1 source remains clean and frozen. MIR/Optimization static
discovery SHA256
`9c5c881ec4d880b104406d98c1d64b17915cf45a14946d4351a87fd93ffcc58b`
found no qualifying unheld next source slice; its held C11 source binding,
builder and verifier boundaries are unchanged and are not used here.

## Complete selected Safety group

Move all ten branches of `record-safe6-call!` from component 00090 and both
`stage0-unsafe-forms` and `validate-stage0-unsafe-island!` from component 00521
into `gravity.safety`. These are two existing SAFE6 surfaces: typed source audit
records and temporary Stage0 executable presence checks. Keep their distinct
inputs, refusal ownership and unsupported behavior explicit.

```clojure
native-unsafe-forms
[unsafe-forms-var form]

validate-native-unsafe-island!
[failure-var difference-var required-metadata-var module form]

record-native-unsafe-audit-call!
[checker ctx node operator args effects capabilities return-type spec operations]
```

The scanner preserves quote exclusion, the outer unsafe-form stop, recursive
collection traversal, original lazy `mapcat` behavior, actual form identity and
the original recursive Var selection point. It does not implement general
macro expansion or infer unsafe operations.

The validator preserves safe/safe-optimized refusal, metadata-map presence,
required field-key presence, sorted first missing key, ordered independent
guards, original owner results/exceptions and complete diagnostic payloads.
It receives live failure, set-difference and required-metadata Vars. Presence
of a key does not authenticate its value or approve unsafe behavior.

The recorder preserves the complete unsafe-island, safe-wrapper,
operation-inventory, review-status, invariant-proof, generated-provenance,
policy-decision, dependency-summary, release-audit and conformance branches.
Its trusted operation map has exactly two late Vars:
`:arg-value [args index]` and `:record! [checker key record]`. Each branch
selects its record sink before payload evaluation. Preserve all five original
context dereferences (three base fields and two policy fields), the nil-kind
fast path, false/nil/default distinctions, source/generated identity and the
base-record return rather than the sink result. Do not assume textual
evaluation order within a Clojure map literal.

Bootstrap retains the historical names/arities as thin adapters, both SAFE6
catalogs and conformance summary, other SAFE7/8/9/11 forms in component 00090,
the required executable-metadata catalog, source traversal/call position,
checker sink, actual source readers and audit/compiled-plan projectors. Safety
adds no Bootstrap, Diagnostics or Clojure set import; callers supply operations.

Inputs are native source forms, actual module/path/safety data, original
checker/context/node/spec/argument data, effects/capabilities/return type,
source/generated provenance, and explicit live operations/catalogs. Outputs
are original unsafe-form sequences, the original complete-or-owner-refusal
result, the base call record or nil, and the existing ten checker families.
No identity encoding or semantic invalidation is added.

## Diagnostic ownership and evidence

The moved executable guards own `SAFE6-UNSAFE-FORBIDDEN` and
`SAFE6-MISSING-METADATA` at their existing Stage0 check positions. Diagnostic
construction/transport remain with supplied `fail!`. The ten `typed-safe6-*`
files are direct catalog-refusal calls; they do not demonstrate malformed
unsafe metadata reaching the executable validator and receive no such credit.
The typed diagnostic catalog and its caller stay unchanged.

Before extraction, capture complete ordinary `unsafe-audit-source-artifact`
outputs for the existing accepted `unsafe-audit.gravity` and both hello
spellings. The accepted audit source exercises all ten record branches. Use
the original host form reader, macro/Core/typed path, actual source path/text
hash and original typed-artifact hash. No reader rebinding, authenticated
module-input overload or private factory is allowed.

For executable evidence, use actual macro-source products and the existing
public Stage0 compiled-core-plan path on ordinary hosted sources; emit real
instruction plans with existing identities. Exercise the genuine unsafe
syntax in `core-app-unsafe-forbidden.gravity` and
`core-app-unsafe-metadata.gravity` through the unchanged public source safety
or compiled-plan caller. Preserve complete before/after plan and diagnostic
observations. The separate three-argument seed compiler used by Runtime does
not invoke these guards and cannot supply this validator evidence.

Actual unsafe execution is unsupported by this Stage0 expression compiler.
Do not invent an executable accepted unsafe body or turn audit-record labels
into proof. Compile supported ordinary sources, emit actual typed unsafe audit
records, and reject the real unsafe-form cases at their original guards.

Native controls must cover scanner laziness/quote/recursion/interposition,
all ten recording branches, original context and sink-selection order,
returning failure callbacks, required-key catalog changes, nil/false and
unverified labels, malformed native inputs, exact exceptions, and physical
span/origin/form/fact identities. Preserve all 15 earlier Safety APIs and all
unrelated forms in both components. Safety will expose 18 APIs.

Append one complete `gravity.safety.unsafe-audit-test` command at 2g/180 seconds;
preserve all 23 earlier CI vectors. Update both existing Safety API inventory
checks, run affected SAFE1/native Safety, Types/Effects, language/ownership,
cold loading, CI harness, docs/roadmap and complete current-main lint checks.
Archive actual generated outputs with byte hashes. Final exact independent
semantic/adversarial review, provider/CI audit, clean source freeze and current
integration preflight remain required. The designated integrator retains Main.

## Unsupported boundaries

These are trusted temporary Stage0 source-record and field-presence rules.
They do not authenticate source/Core, callbacks, catalog roots, proof or review
references, certificates, capability/provider grants, safe wrappers or package
policy. Recorded approved/passed/accepted/complete labels remain unverified
data. The validator does not prove invariants or review validity.

No unsafe operation or native runtime check executes here. No unsafe island is
approved, no certificate is issued, and no optimization check is erased.
Canonical C10/checked-core authority, complete SAFE1/SAFE6/SAFE15 conformance,
MIR/backend preservation, performance, release, migration, self-hosting and seed
retirement remain unsupported. Held C2/Reader/authenticated source/private
builders/factories/pins remain unchanged and uninvoked.
