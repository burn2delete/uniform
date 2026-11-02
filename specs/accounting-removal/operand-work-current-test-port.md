# Operand-work current test port

## Scope and source identity

PR610 source `2b70fac2fccdc4963e47d1a3cf0713cc970b155a` is
reconciled against main `56f59e0050706dd8035d7d1b6e09b53b2d614d33`,
tree `6aeceb81d4a9dca7b1684dabe809fab6f97a6dcd`.
The three production descriptor field removals are already incorporated in
main. This port changes tests only. It preserves main's accounting-free
APIs, complete input-policy checks and production source. It does not replay
the candidate's inherited implementation stack.

The original `operand-work-census.edn` and `operand-work.md` are preserved
byte-for-byte as historical records, including their original pins, failures,
unrun criteria and nonclaims. Their hashes and reported invocation shapes
describe that historical candidate, not this test port.

## Retained useful controls

Five predicate assertions in the Keyword, String, Integer and Vector
composition selectors now require absence of `:operand-type-work` instead of
retired numeric totals. Surrounding provenance, type, effect, transfer,
allocation, arity and runtime assertions are unchanged.

The existing String/Keyword Vector-to-Set C7 kernels require absence of the
field and reject fully typed descriptors forged with values zero and one.
Both use the current seven-argument `sh08-st-inferred-call` API and retain
the stable `:set-conversion-source-mismatch` diagnostic.

The unchanged affected Effects/lowering/runtime tests exposed one existing
test expectation defect: empty literal Vector has no Keyword member, and
the existing C7 family classifier selects the String descriptor. The Effects
test now requires the exact String validation policy for empty input and the
exact Keyword validation policy for nonempty Keyword input. Never remains
non-invoked. Production classification, construction policies, validation,
full descriptor comparison and all substitution refusals are unchanged.

## Observed checks and limits

The two changed C7 kernels passed 2 tests / 322 assertions. The seven affected
Effects/lowering/runtime development kernels initially had 1 failure in
452 assertions at the empty-input expectation. With that expectation
corrected they passed all 7 tests / 452 assertions.

These execute the existing Gravity C7, Effects and lowering helpers and
interpreter-backed runtime controls. They do not establish fresh authenticated
Reader/Core composition, and the four changed predicate composition selectors
have not been rerun for this port. No native-C compiler, provider, broker or
restricted operation was executed. No new source authenticity, migration,
release, self-hosting or general Set-conversion support is claimed.

C7, C8, C14 and D9 govern the retained semantic and evidence obligations.
Independent acceptance and exact current-main CI/preflight remain separate
integration gates. The historical source census does not confer those gates.
