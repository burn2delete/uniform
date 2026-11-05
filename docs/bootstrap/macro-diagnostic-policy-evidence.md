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
