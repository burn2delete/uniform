# Ordinary-function and verifier fixture successor

Planning: ACC-01/04/07/12/13. Accountable owner: ACC-01,
thread `01a183dd-7208-7d33-a336-7483fd90e358`. This finite fixture/test successor
is stacked on PR598 at `d58d61c4a83f09ebd3c742a3cc13b9d000244759`, tree
`5b895b67131de6d209b50585551840fd3954011f`. It preserves four PR502 source
paths and their actual construction/verification obligations. Production code is
unchanged; neither this draft nor its parents confer semantic acceptance.

## Contracts and source correspondence

[L5](../../docs/phase-01-core-language/015-l5-type-system-specification.md),
[C7](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md) and
[D9](../../docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md)
require actual function return/type facts, source identities, complete constraint
products and fresh result verification. Fixture names and matching bytes are not
proof that either source spelling executed successfully.

The preserved work-ledger source defines `tone`, returning Keyword through an
ordinary present Map lookup. The qualified source defines `shade`, returning
Keyword through `gravity.core/get`. These are real source/typing/verification
cases; the first basename does not make its behavior accounting-only.

[The census](verifier-fixtures-census.edn) compares thirty complete relevant C7
production definitions at PR502 head
`6a4f87f013d0076b81aba9e0667650e70d28e635` with the unchanged candidate source
and assessed main `e3d990e9c6594f140a85397fd8e16a80078a4f10`. Twenty-eight match
exactly, including all selected Map-get producer/source helpers and both public
fresh-verification wrappers. Two retain explicit differences:

- The ordinary construction entrypoint reads current `:work-count` telemetry
  rather than the three historical `:ordinary-work-count` occurrences in handler
  and throw-product allowance arithmetic. No type, source, constraint or result
  expression differs in that complete form.
- The imported-result wrapper reports `:imported-type-result-carrier-limit`
  rather than the preserved `:imported-type-result-carrier-invalid`. Current
  shared carrier preflight remains metered. This is a real diagnostic difference;
  native/semantic replacement and accounting removal remain separate open work.

Those differences are enumerated, not normalized into an implementation or
whole-constructor equivalence claim. No current telemetry read, reason or carrier
policy is rewritten. Source incorporation is proposed only within the recorded
forms and remains subject to independent disposition. General public native
callback/cycle safety, imported carriers and policy APIs are not accepted here.

## Fixtures and genuine controls

All four original `.gravity`/`.qst` files are copied byte for byte under
`bootstrap/clojure/fixtures/self-hosting/sh-08/preserved/`. Both pairs have equal
bytes. The original accepted paths remain historical source identities; the new
copies are outside accepted fixtures. No contents, namespace or operator spelling
is changed.

The exact preserved `ordinary-function-construction-preserves-semantic-results`
test body is restored. Its source helper defaults to the original work-ledger
fixture and adds an explicit basename arity for the qualified case; the existing
current source/Core transport and returned rows are unchanged. The qualified test
is the same complete control with only its test name, fixture basename, definition
name and selected key changed. Both retain successful return/type/constraint
products and malformed Core/source diagnostics; no payment-refusal test is added.
Existing allowance arguments are required by the unchanged temporary API, not a
new budget requirement.

The new fresh-verification control uses the actual existing source-request
constructor and public C7 producer/verifier, not the private comparison arenas.
For each `.gravity` fixture it checks source/companion bytes, actual Keyword
returns, the expected Map-get fact, nonempty type/constraint products and a fresh
passed result with complete expected/candidate equality. Eight candidate
mutations per fixture alter schema version, status, typed types, constraint
ledger, return type, module namespace, identity ledger or an unknown field.
These controls are present but UNRUN. The companion spelling has no fresh paired
typed/runtime result; byte equality is not claimed as that result.

## Observed checks and finite acceptance

Pinned local Clojure source validation passed with reader evaluation disabled:
all parent test definitions unchanged, four added helper/test definitions, exact
original ordinary body, explicit qualified/helper adaptations, four exact fixture
byte copies, equal pairs, unique definitions, thirty production correspondences
and eight candidate mutations. The generator and invocation are retained locally;
the generator digest is in the census. Production source equals the parent bytes.

`clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`
passed 6 tests / 11 assertions. Contextual SH08 clj-kondo reports zero errors /
71 warnings, exactly the parent's finding multiset. Canonical docs, full-language
roadmap and whitespace checks passed. No Bootstrap, Gravity/compiler, C12,
authenticated request, Effects/emitter, runtime, broker or held selector ran
locally; the checks above are source/structure evidence only.

Keep draft pending coordinator-routed independent source/result disposition and
applicable execution assessment. No stopped review or denied operation is
replayed, transferred or routed through this draft. Required acceptance is UNRUN:

1. Run both construction controls with actual current source/Core and C7 products,
   preserving real path/extension provenance, function return facts, full type and
   constraint joins and malformed Core diagnostics.
2. Run the new fresh source/verification control for both fixtures, including every
   candidate mutation. Confirm actual reconstruction rather than reusing producer
   output as verification authority. Do not launch any restricted C12 path.
3. Assess the companion source requirements under the existing paired-source
   policy. Record which spelling actually ran; renew paired composition where
   reader/identity/transport changes require it. No runtime value is claimed here.
4. Independently disposition the selected source correspondences and preserve the
   two genuine current/historical differences. General native callback/cycle,
   imported-result and accounting removal obligations remain open.
5. Consume applicable CI, compose accepted dependencies with current main, review
   changed composition and integrate only exact accepted identities. No release,
   source-authentication, self-hosting, ACC or PR502 closure follows from this draft.
