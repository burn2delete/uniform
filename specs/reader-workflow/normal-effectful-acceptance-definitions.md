# Normal effectful Reader acceptance definitions

Task: `reader/finite-remaining-work/extension-policy-closure` / FL-P01-T01 (open).
Base: `482417c3b241544685045e9ec3578a1be9e0e39e`, tree
`ccce555bb8a68a0029ad47e8c7df3d2e72a31485`.

## Source cut and actual API

`bootstrap/clojure/acceptance/gravity/reader/declared_input_acceptance_test.clj`
contains opt-in, UNRUN actual acceptance definitions. No acceptance namespace,
Bootstrap, Reader, compiler, CLI or provider was required or executed to check
these definitions. This is a source increment, not runtime acceptance or a new
permission gate. No production implementation or Accounting fixture is changed.

The definitions call `gravity.build.invocation/run-declared-input-build!` with
the real native argument vector: `build`, explicit principal, meta/JVM selection,
JSON format, repeated distinct `--allow-read` keys, package/workspace policies
and the EDN reader request. The request contains actual source path/text,
source requirements, declared key/content pairs and narrowing implementation
and policy hashes. The real current Reader and handler binding helpers derive
those hashes; the constructor independently recomputes them. These identities
grant no authority. The unchanged normal host invocation owns grant issuance
and expiry and resolves the fixed `reader-declared-input-build!` constructor.

The fixed policy supports `gravity.reader/read-input` for `resource`, with
`:build/read-file`, `:fs/read`, meta/JVM, build phase and invocation lifetime.
The actual Gravity handler, original private receipt, raw builder/verifier and
fresh C2 consumer are the intended execution path. No parser, compiler, issuer,
handler, provider or result substitute is supplied. A separate definition calls
the actual CLI entrypoint and checks its closed JSON projection and redaction.

## Population and verification definitions

Both conserved `.qst` and `.gravity` fixture spellings are used. Definitions
cover empty, ASCII and Unicode returns and one/two occurrences of the same key.
Each occurrence must retain distinct form identity and the original source,
span, input seed, returned seed, selected handler, policy and complete request.
The same key has one declared store entry; occurrence identities remain distinct.
Repeated occurrences now use the paired `repeated-granted` files themselves.

Complete-population refusals include unknown tags, a valid resource followed by
an unknown tail or a key outside the explicit grant, and text followed by a
resource under the fixed resource-only policy. Existing `disallowed` and
`undeclared-effect` fixtures exercise scope and effect-policy denial. The old
text-only leaf-policy interpretation of `disallowed` is not exposed by this
fixed normal API. Paired `resource-unknown` and `resource-outside-grant` files
retain their own actual path and bytes. A separate refusal keeps the explicit
permission and source requirements while omitting the declared store entry;
it expects `L15-CONTRACT :undeclared-input`. Another removes only the explicit
`--allow-read` argument and expects `T1003`; request data cannot restore authority.

The only interposed Var is the existing private C2 consuming seam. Its wrapper
always calls the unchanged original, forwards original authority/path/invocation
and makes assertions while the real normal command owns the live context.
Fresh verification is repeated. Missing/duplicate invocations, handler/policy,
payload, request identity and returned-result tampering are submitted to the
same original consumer, including public identity recomputation. Source-byte
hash, input-seed datum, request input and artifact-digest substitutions are
explicit cases. A prior result is checked against a new invocation with a
different original return and against the paired `changed-source` files with
the same original return. Equal
public data is not treated as proof of a stale receipt. After normal return,
the public view must refuse to extend the expired effectful invocation.

## Provider observation limit

There is no supported causal provider-read counter in the current API. The
private `gravity.build.declared-inputs/input-entry` is the store-read seam;
construction `read-input!` calls it and registers an original receipt.
`verify-original-result!` instead checks that receipt and returned candidate.
The bridge's verification branch calls the latter; it does not read the store.
These are source facts, not measured provider counts. The definitions count
verified consumer products only, never label that number provider reads, and
do not replace private store access to invent an observer. Actual causal
zero-repeat-read measurement remains unavailable through a supported observer.

## Loader selection and local checks

The acceptance root is absent from default `deps.edn` paths and every alias.
No runner, leaf catalog or CI selection is changed. Ordinary development runner
selection loads only its selected namespace; the explicit namespace option can
load this root only when separately added to the classpath. Its `--list` path
also requires selected namespaces and therefore must not be used as a source
check. The leaf runner uses its literal catalog. The self-hosting runner scans
the existing self-hosting test root, not this acceptance root.

`clojure -M tools/check_reader_acceptance_definitions.clj` reads Clojure forms
with reader evaluation disabled and checks route/observer structure, classpath
exclusion, exact fixture pairs and mixed/repeated population data. Its negatives
replace the normal entry, insert direct grant issuance, remove original consumer
forwarding, include the root in default paths, alter paired text and omit the
unknown tail. It checks actual constructor preparation/request preflight order.
These source checks do not establish runtime behavior. Existing inst/uuid,
standard and pure implementations/tests/fixtures remain byte-conserved.

Original frozen validation: source/data checks passed 2 tests / 27 assertions; language
boundary passed 6 / 11; static lint reported zero errors and warnings. Both
document structure validators and `git diff --check` passed. None of these
checks loaded the new namespace or executed an actual target.

## Current-main source reconciliation

The original four-file definition commit is
`dc334ab75a73f96cc1b7ad006ff5c963e9817bf8`, tree
`f8da4747724a1a7b358c23c2ca7bf520720666ae`. Its missing physical checkout was
recovered through managed worktree creation from that preserved Git commit.
The recovery started clean at exactly that tree. A normal merge composes main
`c1c994f3b251daf89305d3e2ed24a4af16aeccef`; the original four files are
byte-conserved by that merge before the definition and paired-fixture additions.
No production API or implementation is changed by this increment.

The existing `sealed_pure_integration_test.clj` remains byte-conserved. Its
`normal-gravity-pure-text-pair` definition calls the actual
`reader-pure-source-artifact` and `c3-reader-artifact-view` routes for both
extensions, checking policy, handler, seed/result and source tampering.
`normal-gravity-pure-text-mixed-refusals` defines unknown and effectful tag
refusals under the pure policy. These definitions remain UNRUN; their existence
does not establish the canonical task's accepted authorization behavior.

The inert checker reads those pure definitions as source data alongside this
increment. It checks physical paired fixture populations and explicit
source/input/result/digest, missing-input and missing-permission definitions,
including negative mutations that remove required definitions. It neither
loads either acceptance namespace nor executes their assertions. Unknown and
disallowed population refusals have consumer-product assertions only; no causal
provider counter or ambient-execution measurement is available through the
supported API. Original receipt verification's lack of store reads remains a
source fact, not a runtime measurement or a new observer API.

Frozen `dc838f78` validation: inert source/data checks pass 3 tests / 66 assertions;
language boundary passes 6 / 11; static lint has zero errors and warnings;
both document validators and `git diff --check` pass. An additional default
`cljfmt check` reports formatting differences for both Clojure files; checking
their exact original frozen contents reports the same two-file baseline failure.
Existing formatting is preserved outside this increment. No acceptance
namespace, target or provider was loaded or executed by these checks.

## F1 live-consumer observation correction

Independent review of `dc838f785433fd1cf36e9dedd1902693d1d1116d` requested one
correction: the selected tamper test's rejection assertions and the helper's
fresh verification assertions could all be skipped when the matching observer
guard was false. The earlier positive test's separate counter did not protect
selection of the tamper test alone. That review remains historical evidence,
not acceptance of this successor.

The shared `normal-build!` helper now counts only matching live-consumer visits
and unconditionally asserts exactly one after every successful command return.
The check is outside the conditional observer and covers both tamper callbacks,
the saved baseline and other successful helper calls. Original consumer
forwarding and candidate verification remain unchanged. This count describes
live-consumer observations, never provider reads.

The inert regression extracts only the observer closure and direct
post-command assertion, and evaluates them over core data with a core observer.
It never evaluates the command, project Var/redefinition map, acceptance test
body, constructor or actual consumer. Removing the post-command assertion
reproduces the old false-guard escape; the corrected assertion rejects both
zero and duplicate visits and accepts exactly one. Original forwarding within
the extracted closure is checked as core argument forwarding, with no claim of
successful project-port execution or genuine acceptance.

Semantic correction commit `1d1488cc7d12872f0905bb5f57a194f3ebc83ac1` is separate
from strict formatting commit `99722d40bc34037b0994764f087e8465e0d2cc9d`.
Pinned `cljfmt 0.13.4` now passes both owned Clojure files and configured
`tools/ci`; the previous two-file default-format nonpass is resolved rather
than waived. The two files are outside the configured formatting root, as the
review confirmed. Full source-data reads before and after formatting preserve
ordered forms, explicit metadata and exact string/regex literals, allowing
only bijective renaming of reader-generated anonymous-function symbols.

Successor checks pass 4 inert tests / 71 assertions, language boundary 6 / 11,
static lint, document validators and diff checks. Fixture pairs, production
APIs and the five production/pure source equations remain conserved. Actual
acceptance execution, independent acceptance of this correction and broad task
closure remain open; historical runtime holds and ACC09 evidence are unchanged.

Future execution must first produce the real normal build artifact and live
fresh-consumer facts from these definitions; neither model results nor a green
unrelated CI run substitute for them. This increment stops at an exact source
candidate for OWN to route for independent review. No review, publication,
runtime run, release, migration, self-hosting or full task closure is claimed.
