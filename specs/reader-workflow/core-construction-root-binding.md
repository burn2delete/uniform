# Construction root identity and intentional execution binding

## Source result and scope

This correction starts at `651fb42bb45f107ba4c8060fa0729b622ac75bd8`, tree
`74dee85c9888859e2d76d0b837abc40af24d1884`. Accounting's source attribution
receipt SHA256 is
`9abdb20d5861781bf4df06874cc5cd427526b0166678c30160e6ce2fe42c9c5d`.
It records session 82687's C6-VERIFY/construction-dependency-drift category,
but not its failed checkpoint or first false comparison. The 95 external pins
do not record effective Var values or intermediate construction facts.

The pinned source has a conditional conflict: 00897 snapshots each protected
Var with var-get, then later compares the effective value by identity. Its
protected vector includes the dynamic execution Var. 00898 intentionally
thread-binds that Var to its captured current Core binding before the inner
checks. When that selected object differs from the outer effective value, the
old roots predicate becomes false even with no root mutation. Exact extracted
source controls reproduce this conflict. They do not establish that it caused
the actual run: checkpoint A or another comparison could have failed first.

C6 requires verified input and provenance joins before type checking; D1/D3
preserve the distinction between implementation facts and artifacts; D8/D9
require rejection and qualified evidence; L9 governs failure propagation.
This change repairs the temporary Clojure construction invariant. It changes
no Gravity lowering semantics, C7/C8 contract or authoritative migration pin.

## Minimal separation of the two invariants

All protected Var identities and the complete protected Var/name lists remain.
For every Var except the execution Var, capture and comparison still use the
same effective var-get identity. Other dynamic guard semantics are unchanged.

Only the execution Var's captured value becomes a construction-local record:

```clojure
{:root stable-raw-root :outer original-effective-value}
```

The roots check always requires its raw root to remain identical. Outside the
builder it additionally requires the original outer effective value. Thus a
restored outer binding is legal, while an arbitrary different outer binding is
not. A raw-root replacement is detected even while a thread binding masks it.
No protected Var is removed and no current observation replaces a saved pin.

Inside the private builder, after reading its existing captured current Core
binding, a local copy of construction facts records
`:execution-binding captured`. That copy is used only by the existing inner
run/bookend calls under the unchanged `binding` vector. The roots predicate
requires the effective execution value to be that exact selected object.
Equal-looking copies and same-hash substituted plans are refused. The outer
facts used by the caller's final check retain their original outer context.
This internal fact is not a caller option, public authority flag, session field,
manifest, digest input or returned artifact. Public arities do not change.

The existing checked_core structural-runner condition still compares the
selected binding's source-content-hash with the current source key. It is
byte-conserved and independently exercised as a selected expression with inert
values. That hash predicate alone accepts same-hash plan substitutions, which
is why this correction also preserves a selected-object join rather than
switching all checks to raw roots and accepting any thread binding.

Thread, phase, catalog, carrier, selected Reader, dependencies, plan-binding
views, context-catalog and envelope checks retain their order and source.
Request/artifact membership, input-key/captured-source checks, both source-key
bookends, default nil paths and explicit invocation forwarding remain. A
failure still emits the same aggregate construction-dependency-drift reason;
no checkpoint logging, diagnostic field or whitelist is added.

## Inert controls and conservation evidence

`gravity.reader.core-construction-binding-test` reads the exact pinned and
current source as data. It evaluates only the capture function, roots predicate,
builder binding vector, local facts expression and existing legality condition.
Project operation lists are read but never resolved or executed. The fixture
uses its own disposable Vars and inert objects, with a strict executable-head
allowlist for the capture/predicate/facts slices. It supplies no successful
project verifier, carrier, hash, Reader or execution callback. No whole guard,
constructor, verifier, plan, Bootstrap or owner namespace runs.

Controls show old conditional failure and new legitimate success for distinct
bindings, unchanged success for an identical selected binding, restored outer
identity, refusal of a real guarded-root replacement, masked execution-root
tampering and another dynamic Var's changed effective value. Wrong selected
objects, nil, equal-looking copies and same-source-hash plan substitutions
refuse. The unchanged source-hash expression still rejects wrong hashes and
missing bindings. A controlled throw uses the exact selected binding vector
and accepted unchanged causal helper to check unwind, original outer/cause
identity and interrupt flags, including a fatal node before interruption.

Source equations preserve every other 00897/00898 form, both protected lists,
all other top-level guard comparisons, the builder body after removing only
the new local fact pair, and the source-hash legality expression. 00899 is
byte-identical to the accepted known-diagnostic source: privacy, exact class
and native validation, closed diagnostic sub-version, complete causal scan,
ready output, full identity/integrity, replay, real report catalog and legacy
selection are unchanged rather than revalidated through a fabricated owner.

Initial test attempts had a delimiter error, then two harness extraction
errors: a symbol binding was looked up as a keyword, and a nested source
definition was searched only at top level. Those nonpasses remain separate
from corrected passing source-slice controls. No positive result from those
attempts is extended to runtime or owner acceptance.

## Acceptance and explicit limits

The source task is isolated from the read-only Accounting checkout and all
previous frozen candidates. OWN supplied the fresh reservation; attachment
inspection found no matching prior root/binding worktree. This does not claim
global writer vacancy. The current candidate changes only 00897, 00898, its
focused source fixture and this explanation.

OWN routes one independent affected review of the exact frozen source tuple.
No actual constructor or guard result is supplied by these controls, and no
actual failed checkpoint is recovered. Session 82687's root cause remains
unproved; sessions 53455 and 56177 remain unknown history. ACC09 alone owns
its fixture. No retry, fourth selector, Reader/Gravity/provider/CLI/hash/runtime,
probe, lease, CI, publication, pin renewal or fallback occurs here. The genuine
acceptance goal remains open, and no release/self-hosting/seed-retirement claim
is made.

Final local controls pass 7 tests / 91 assertions, boundary 6 / 11, focused test
lint with zero errors/warnings, both documentation validators and diff hygiene.
Source conservation equations pass for the protected lists, other dynamic
semantics, seven unchanged top-level comparisons, builder binding/body and six
pinned dependency/session/fixture inputs. The proof helper's first delimiter
error is retained separately from its corrected pass. These are inert source
results, not a new execution or a recovered runtime observation.
