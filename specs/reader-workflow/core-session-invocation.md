# Explicit Reader invocation in development Core sessions

This finite source increment closes the session interface gap identified in
`reader/finite-remaining-work/extension-policy-closure`. Its base is
`0be1c7d4ff8f4a0bc1acba68c9799e6f461be2e7`. C6 requires verified resolver
inputs and preserved provenance before type checking; BOOT7 requires exact
comparison inputs and provenance for equivalence claims. This change supplies
an existing source-selection context to both session branches; it grants no
new admission or self-hosting authority.

## Interface and source joins

`gravity.bootstrap/sh07-core-verification-session` retains its one-argument
entry and adds `[resolution-artifact invocation]`. An explicit invocation map is
forwarded as the same original object to both existing ports:

- `sh07-core-from-resolution-artifact resolution-artifact invocation`;
- `sh06-resolution-artifact-verification resolution-artifact
  :gravity.bootstrap/legacy invocation`.

The existing ports validate the invocation carrier and exact options, selected
Reader path, current source/plan binding and authenticated artifact joins.
The construction guard retains its dependency, catalog, source and operation
bookends. A map containing `:source :development-current` is a selection request,
not authority to skip those checks. No observed hash or byte count is adopted.

The one-argument entry delegates with nil; the nil branch uses both original
unary ports. Only omitted/explicit nil selects those ports; every non-nil
value, including false, is forwarded to both explicit ports and their existing
validation. Schema version 2 and all session output fields are unchanged.
No invocation field, authority flag, new digest or alternate verifier is added.
Full upstream snapshots, full expected integrity, session binding, replay
validation and the real report/check catalog remain unchanged.

After independent acceptance, a caller using the existing explicit C5 source
API can pass its actual source text, module inputs, selected Reader path and
invocation through C5's five-argument entry, then pass that same invocation to
this two-argument session entry. The invocation shape remains:

```clojure
{:reader-source-path selected-reader-path
 :authenticated-envelope-options {:source :development-current}}
```

The caller still checks session readiness, takes its fresh expected artifact,
and uses `sh07-core-artifact-verification-against-expected` with the original
session. Existing coordinator/B47, C7 and C8 carriers are not rewritten here.
Accounting alone owns its composition fixture; this candidate changes none of
its tests and does not authorize fixture adoption or another selector run.

## Focused evidence

`gravity.reader.core-session-invocation-test` reads the selected session and
integrity helper definitions as source data, then evaluates only those bodies
in disposable fixture namespaces with strict injected ports. It never loads
Bootstrap, Reader, Core operations or a Gravity plan. Fixture digests and
reports have no semantic or authentication authority.

The five tests passed 52 assertions. They check identical invocation delivery
to both ports, exact argument counts, legacy/nil equality, dropped or copied
contexts, wrong Reader path, injected Reader drift, wrong verifier context,
stale sessions, full upstream substitution, expected-product/catalog mutation,
binding/integrity mutation and unchanged Error/interrupt handling. Deliberately
mutated forwarding bodies fail against the same strict ports. Catalog mutation
here concerns retained expected-product fixture data; it is not an execution
of the real Core semantic catalog.

The first test run had two interrupt-flag assertion failures after test
reporting. The corrected test captures the flag immediately in its catch and
clears it before reporting; production exception handling was unchanged.
That earlier nonpass is preserved, not replaced by a claimed runtime success.

Source preservation independently checks the original AST against the new
nil-selected body: they are equal. Every other `00899` form is equal. Seven
source ports are byte-conserved: C5's explicit source API, the historical Reader
pin, current Reader binding, invocation validation, Core construction guards,
session integrity helpers and the authoritative fresh verifier. Boundary
checks passed 6 / 11, focused test lint has zero errors/warnings, and
`git diff --check` passed.

## Limits and next acceptance criterion

Development replay retains its existing semantics: it checks the supplied
artifact/session against the retained snapshot and expected product. It does
not reread the Reader file after session creation. Source drift refusal during
fresh construction/verification belongs to the unchanged existing ports; the
injected checks establish forwarding behavior, not actual file or plan proof.
The unchanged session binding still binds the upstream report's status rather
than hashing all report metadata; this interface change does not strengthen
that historical contract or claim rejection of arbitrary report-metadata edits.

Next criterion: OWN routes independent exact-source review of this interface,
its preservation equations and fixture scope before any Accounting adoption.
Actual C5/session/replay/B47/C7/C8 execution remains unrun. No historical
231576-byte Reader pin, authentication helper, release record, migration
fingerprint, public admission rule or full task status is renewed. The frozen
alias-reader and normal-entry definition candidates remain separate.

## Independent F1 correction

Independent review of `724d8332cb6a0afcdc77514b43a202281356a83f` requested one
P2 correction. Receipt SHA256
`e9dba43b5a8cd7d61221ae263ee19a2746eff911fb95ce6d7269c8fff0a36af3`
records 86 qualified source/injected checks and the false-value counterexample.
The original receipt and counterexample remain unchanged. Their scope does not
confer actual Reader/Core acceptance.

The original two truthiness decisions silently selected legacy ports for
false. This successor changes only those decisions to `some?`, so false is
forwarded instead of being converted to nil/default selection. The existing
three-argument resolution verifier enters `sh06-resolution-with-reader-invocation`,
whose `some?` check invokes the original Reader invocation validator. No new
validator, authentication flag or authority is introduced.

The new pinned-parent control evaluates that actual validator body with inert
carrier/source ports. Restoring the old branch conditions reproduces a ready
false session. The corrected body refuses false, zero, string, vector, empty
map and invalid-option map through its explicit ports, retaining the same
original object and legacy verifier context. Omitted and explicit nil remain
equal ready sessions. A pinned fixture prevents the current Reader's historical
source-pin refusal from masking the bug.

The complete focused suite passes 6 tests / 84 assertions; boundary checks pass
6 / 11 and focused test lint has zero errors/warnings. Source equations show
exactly two decision changes and identical nil-selected bodies, with every
other session form and the seven governing ports unchanged. The first local
test edit had an unmatched delimiter; its nonpass is retained separately from
the passing corrected run. Independent affected review remains the next
criterion. No Accounting adoption, selector retry, CI rerun or actual target
execution was performed, and the separate frozen candidates were untouched.
