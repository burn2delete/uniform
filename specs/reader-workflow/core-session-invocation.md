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

## Failed expected-construction origin (source successor)

The retained ACC09 source `bf25769ea6470f37978e757c182fbbb7960258b2`
returned `:failed` / `:expected-build-failed` in session 56177. Its complete
original output SHA256 is
`d865cafe74acc6b0b09a37b323049e5f51f5d3f7fd42f96df39d54388cfbba13`;
its stop receipt SHA256 is
`53327b55b4de46cb12b3a91d8fc33779892898e6537041774deaa79b73e5134f`.
Neither exposes the underlying expected-construction outcome. The cause remains
unknown: the old handler collapsed a normal nil return and a caught Throwable.
This successor starts at merged PR701, commit
`f485e117186084a5ab4a04358831f4f93e0dab0a`, and changes only that local
construction boundary, its injected controls and this contract.

The earlier statement that all output fields are unchanged applies to the
invocation increment. This successor explicitly extends schema 2 failed
sessions with optional `:expected-build-failure`, present exactly when expected
construction yields nil. Its closed forms are:

```clojure
{:origin :expected-core-construction :kind :nil-return}
{:origin :expected-core-construction :kind :exception
 :exception-class "java.io.IOException"}
```

The exception class is the caught host exception's binary class name, not a
claimed underlying cause. Origin names the owning call boundary, not a guessed
Reader/C5/lowering stage. No message, stack, path, ex-data, cause object,
Throwable, arbitrary diagnostic id or reason is copied. Existing source-path
and provenance fields remain as before; the added diagnostic carries none.
No safe upstream diagnostic identifier is established by the retained failure,
so none is invented or extracted from untrusted ex-data.

The root session kind and schema version remain unchanged because ready
products, their keys, all admission/replay inputs and existing status/reason
fields are unchanged. Existing source consumers inspect readiness and identity;
there is no closed root-key validator for failed sessions. Consumers selecting
a closed failed-session projection must explicitly opt into this optional
field; consumers expecting the exact old failed map must update their
expectation. The two diagnostic variants have exactly the keys shown above.
They are local development diagnostics, not an authenticated product or new
authority field. Failed sessions remain invalid and replay fails closed.

Normal nil still has `:expected-build-failed`; ordinary Exception containment
has the same reason but records `:kind :exception`. Direct InterruptedException
still restores the thread flag and rethrows the identical object. The
construction handler now catches Exception rather than Throwable: direct
Error and other non-Exception Throwables propagate unchanged. For a caught
Exception, a causal InterruptedException restores the flag and rethrows the
original outer Exception; any causal non-Exception Throwable also propagates
that original outer object. Identity tracking terminates cyclic cause chains
without a depth quota. Causes are inspected only to preserve fatal/interruption
control flow, never serialized. This is an explicit construction error-policy
change: fatal failures are no longer converted to ordinary absence. Synthetic
injected throwables check it without causing actual machine failure.

Upstream verification's existing catch-Throwable containment and direct
interrupt policy are byte/form unchanged and outside this correction. This
successor makes no stronger claim about causal failures at that upstream
boundary. Construction still uses the same two/unary ports, then the same
upstream three/unary ports after ordinary containment. No callbacks, pins,
Reader options, construction guards, integrity helpers, catalogs, validators,
authoritative verifier or replay bodies change. Omitted/nil ready output and
explicit ready output retain their exact previous values.

The added source controls distinguish nil, ExceptionInfo and IOException,
assert exact redacted maps and failed-session refusal, propagate synthetic
fatal and causal interruption objects with flag preservation, and terminate a
cyclic cause chain. Existing forwarding, malformed invocation, legacy, stale,
substituted, catalog and integrity controls remain. These are injected source
checks, with no Bootstrap or Gravity target execution.

The exact missing observable for any future separately authorized diagnostic
run is the new failed-only field from the actual session returned to ACC09,
bound to that future source and original invocation inputs. The current ACC09
consumer projects only status/reason into its exception, so merely installing
this source would not expose the new field in its old output. Accounting alone
owns any consumer change. A future class-only result may still leave the
specific internal cause unknown; it cannot retroactively enrich session 56177.
No selector retry, fourth selector, provider/CLI/runtime run, diagnostic probe,
lease, pin renewal, Accounting fixture adoption or publication is authorized
by these source definitions. OWN independent affected review is next; original
failed receipts and the open genuine acceptance goal remain unchanged.

Local source verification for this successor passed 8 tests / 124 assertions,
with no Bootstrap namespace loaded; language boundary passed 6 / 11. Test lint
reported zero errors/warnings, both documentation validators passed, and diff
hygiene passed. Source preservation compared the exact failed bf257 session
and seven ports to the merged base, confirmed all eight other session-file
forms unchanged, and compared complete injected ready results for explicit,
omitted and nil invocation. The unchanged upstream branch, status/reason
expressions and original session root-map expression are form-equal. These
checks do not establish actual Reader/C5/Core semantic acceptance.

## Independent causal-interruption correction

Independent review of source `e0decbd02aed148b06c22f1bc6f33dc23fd50f1a`
returned changes-requested with one P2 finding F1 after 113 qualified checks.
Receipt SHA256 is
`d3bfb006d7ced8983424f6403569f5a9a6571d5ea7b2502bce8a20bcb1e86745`.
That source and its freeze remain immutable history. Its class-only privacy,
closed failed-only metadata and ready-output checks passed; it did not receive
acceptance. Its early fatal exit missed interruption deeper in a cause chain,
and a direct fatal root bypassed its Exception-only catch entirely.

The two reproduced chains are ExceptionInfo -> AssertionError ->
InterruptedException and direct AssertionError -> InterruptedException. Both
previously propagated the identical outer object with the interrupt flag false.
This correction replaces the initial candidate's construction catch policy
above with a local catch-Throwable scan that visits every identity-distinct
cause before deciding the outcome. It remembers whether any node is a
non-Exception Throwable and whether any node is InterruptedException. After
the scan, it restores the flag if interruption was found, then rethrows the
identical outer object if either fact requires propagation. No fatal node exits
the scan early. Cycles terminate by identity, without a depth quota. Preexisting
interrupt state is never cleared by production code. Fatal failures remain
propagated rather than normalized to nil or ordinary diagnostics.

This is confined to construction control flow. Schema 2, both exact diagnostic
variants, ordinary Exception containment, nil returns, same-object invocation
forwarding, omitted/nil behavior, ready output, integrity/catalog checks and
failed-session refusal are unchanged from e0dec. The upstream catch policy and
all other session-file forms and seven source ports stay unchanged. No new
component or general error framework is introduced.

Injected regressions cover both exact reported chains, deeper interruption
after fatal nodes, interruption before fatal nodes, cycles containing both,
preexisting interrupt flags, original outer/cause identity and absence of
upstream calls after propagation. Existing direct/wrapped fatal and interrupt,
ordinary ExceptionInfo/IOException, nil, privacy, stale/substitution, malformed
invocation, ready, catalog and integrity controls remain. The old receipt's
historical 8/124 source success did not include these two failing chains and
must not be treated as proof of their correctness.

The original bf257/session 56177 root cause remains unknown. ACC09's existing
status/reason projection still omits the diagnostic and remains separate future
fixture work owned by Accounting. This correction authorizes no selector retry,
fourth launch, runtime/CLI/provider/probe/lease, pin renewal, publication or CI.
The candidate stops for OWN's same-reviewer independent affected review before
adoption. The original genuine acceptance goal remains open.
