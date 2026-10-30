# Macro plan execution boundary

This increment assigns the pinned Gravity macro plan adapter to `gravity.macro`.
L4, L12, L16, SAFE12, C1, C3, C4, C15, D1, D2, D3 and D9 govern the boundary.
The implementation work moves executable algorithms from bootstrap into the
stage; bootstrap retains pinned configuration, process-local lifetime and
explicit compatibility operations.

The stage inputs are pinned Gravity source/plan configuration, syntax macro
requests, explicit seed operations, a plan-identity cache and a cached binding.
Outputs are
verified plan bindings, resolved expansions and ordered digest/verification
records. C4 owns plan, return, trace and authority failures; host interruption
and containment behavior must remain observable. Generated source retains
reader identities, call/definition origins and deterministic semantic digests.

Dependencies flow from authenticated syntax through macro execution to expanded
syntax. The stage must not import or discover bootstrap. Seed callbacks remain
explicit host boundaries and confer no proof authority by their presence.

| Public API | Inputs and outputs |
| --- | --- |
| `boundary-fail!` | Rule, path and missing/observed facts -> existing structured C4 rejection |
| `resolve-plan-source-path`, `read-pinned-plan-source!` | Seed anchor and pinned identity -> regular source bytes and identity record |
| `compute-plan-identities`, `plan-identities` | Plan and explicit seed identity cache -> semantic/function/shape identities |
| `build-plan-binding!`, `current-plan-binding!` | Pinned source, compiler operations and cached binding -> checked plan binding |
| `execute-plan!` | Binding, function and arguments -> metadata-cleaned Gravity result or contained host failure |
| `resolve-digest-preimage!`, `resolve-digests!` | Ordered requests and prior digests -> declared preimages and resolved digest vector |
| `raise-rejection!`, `contain-operation!` | Gravity diagnostic template or host operation -> structured rejection or preserved result |
| `resolve-expansion-template!` | Template and digests -> expansion with only schema-declared control slots resolved |
| `expand` | Binding and authenticated syntax request -> expansion plus template/resolved verification records |

Every API takes an explicit operation map. Its callable Vars preserve late
bootstrap interposition. A separate `:input` port reads pinned values, cache
state and diagnostic ids at their original expression positions. Mutable seed
lifetime remains in bootstrap; the stage does not retain global cache state.
Exact byte/function counts here authenticate pinned inputs, not input quotas.
The retained `C4-DEPTH` branch contains actual host stack exhaustion.

Acceptance requires genuine paired source expansion, real invalid-request
rejection, full artifact and plan identity comparison, direct stage adversarial
tests, language-boundary and affected regression checks, independent review and
exact clean candidate integration. The draft PR records executed checks and
the final source tuple as implementation progresses.

The paired `defn-basic` fixtures emit `(def add-one (fn [x] (+ x 1)))` with
artifact identity
`sha256:f47b8ed8b4f008970ba41a8e06e37f6cfc6f2899179ba8676f3c53dd7836788a`.
Complete artifact and plan-binding equality passed across this extraction.
`gravity.macro.pipeline-test` covers these actual products, illegal return/build-effect
requests, pinned-input and binding tampering, declared digest control slots,
ordinary digest-reference-shaped data, host failure and interruption behavior.

This increment does not claim completion of the macro stage. Artifact assembly,
source snapshots, compatibility routing, older macro helpers and their remaining
accounting removal remain open. No release, migration, seed-retirement,
self-hosting or performance authority is established by namespace extraction.

The ownership reporting source-version bridge retains the previous ownership
file and pins the three added macro helper entries to their existing source
commit. It conserves predecessor entries and review history; all 180 current
whole-criterion assessments remain unassessed. This repairs read-only reporting
provenance after helper registration and does not renew compiler migration or
release evidence.
