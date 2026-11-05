# Native C4 diagnostic policy and context

## Governing contracts and boundary

D1/D2/D3/D8/D9, L4/L12/L16, SAFE12, C1/C4/C15 and TEST2 govern this
increment. Source parent c4399783ec5a5af43caeed2d32ba9a86a9c38221 has tree
95461d14c1eb6902b292cd562d12a03f66e24c6b. Its acceptance and CI are
separate evidence, not acceptance of this successor.

Move the native C4 diagnostic catalog, governing document, rejected-design
metadata, override mapping, message selection, source override lookup, context
construction and compatibility refusal sequencing from component 00028 into
gravity.macro. Retain the existing Bootstrap names and arities as facades.
Bootstrap keeps generic fail!/ExceptionInfo publication, source IO, admission,
authentication and artifact construction. Macro must not import Bootstrap,
gravity.diagnostics or gravity.c4-macro-evidence to discover policy owners.

Inputs are native module metadata, rule, source path, subject, extra facts and
explicit owner/configuration Vars. Outputs are the same native values or
published C4 diagnostic. Preserve catalog order, source/call/fallback span
precedence, extra-fact merge precedence, field-position configuration reads,
callee selection before arguments, returning failures and exception identity.
No artifact preimage, source pin, authentication or constructor changes belong
to this increment. The legacy constructor remains used through its saved alias
in 00867; it is not dead code.

## Evidence and acceptance

Capture complete native outcomes before implementation on actual accepted and
all ten rejected compiler-c4 fixtures in both source extensions, retaining
source text/hash/path, metadata and complete diagnostics. These compatibility
refusals are driven by fixture metadata; they do not prove semantic macro
legality. Keep SH-05 semantic controls separate. Add regression/adversarial
tests for span/configuration/callback timing, unknown overrides, merge order,
identity and facade interposition. Run focused tests, boundary/ownership,
contextual lint, structural validators, independent reviews and exact hosted
CI. Finish clean commits and retain the early draft for coordinator integration.
The source-scope record itself earns no implementation credit.

## Unsupported boundaries

C4-SIZE is historical metadata, not an expansion quota; C4-DEPTH does not
establish a project depth limit. This extraction does not implement canonical
C15 identity, streams, redaction or rendering; authenticate or execute source;
complete C4 semantic expansion; renew migration evidence; or establish
self-hosting, release or seed retirement. Source IO/admission and generic
diagnostic publication remain residual Bootstrap boundaries.

## Implementation and local evidence

Macro now owns four C4 catalog data Vars and four operations:
source-overrides, diagnostic-message, emit-native-diagnostic! and
validate-native-overrides!. Its public inventory grows from 52 to 60 entries;
the shared stage operation map and all prior APIs retain their shape. The
diagnostic operation receives explicit message/span/document/publication ports;
the override operation receives explicit mapping/span/C4-failure ports.
Callable owners are resolved per use before their arguments. Document and
mapping dereferences stay at their original field/lookup positions. Bootstrap
facades retain all old arities and interposition. Its single Macro require
moves earlier within 00028; no duplicate import or manifest change is added.

Before implementation, clean scope commit
bb6baebb8dfb46548e9c120e45b9af09208769fb retains 42 full native records
on 22 actual paired fixture paths: two accepted metadata returns, twenty
fixture-metadata refusals and twenty emitted native diagnostics. Every record
retains source path, complete source text, SHA256, native input and complete
outcome. Metadata-aware EDN SHA256 is
e2afc4c118836ff62791b9dbdb556471f57c1ac87c214f419722f4de996974ce.
Only inert fixture headers were read; source compilation/authentication are
explicitly false. Native before/after comparison is still required.

Focused native-report and owner-call regressions pass 24 tests / 273 assertions.
The API inventory check passes 1 / 65. Boundary/ownership passes 48 / 1202,
with no skipped namespaces. Direct changed-file lint is zero warnings/errors;
document, roadmap and governance validators pass. Initial added-test assertions
incorrectly indexed variadic argument lists as vectors and omitted Bootstrap's
existing stage marker; their four failures are retained in the initial log.
Correcting those test observations required no production repair.

Static conservation recovers the complete prior Macro file byte for byte after
reversing only the new policy block, contract and API inventory. Reversing the
eight facades and require position recovers all of 00028. Catalog/message
bodies and the two parameterized context/refusal bodies recover the exact old
policy. All prior native tests remain; the pipeline test changes only its API
count; only the Macro row changes in the namespace map. Constructors,
authentication, source pins, manifests, other stage rules and CI files remain
unchanged. The still-live saved legacy constructor is preserved.

Source-expansion composition, full native parity, contextual lint, independent
semantic/adversarial review, exact hosted CI and current-base integration remain
separate outstanding checks at this implementation checkpoint. Both existing
native-report and owner-call namespaces remain wired into the Macro CI cohort;
no new CI namespace or command selection is required.
