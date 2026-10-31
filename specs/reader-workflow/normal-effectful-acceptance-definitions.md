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

Complete-population refusals include unknown tags, a valid resource followed by
an unknown tail or a key outside the explicit grant, and text followed by a
resource under the fixed resource-only policy. Existing `disallowed` and
`undeclared-effect` fixtures exercise scope and effect-policy denial. The old
text-only leaf-policy interpretation of `disallowed` is not exposed by this
fixed normal API. Concatenated/repeated cases retain the fixture path but supply
derived source text; they do not claim those bytes were read from the file.

The only interposed Var is the existing private C2 consuming seam. Its wrapper
always calls the unchanged original, forwards original authority/path/invocation
and makes assertions while the real normal command owns the live context.
Fresh verification is repeated. Missing/duplicate invocations, handler/policy,
payload, request identity and returned-result tampering are submitted to the
same original consumer, including public identity recomputation. A prior result
is checked against a new invocation with a different original return. Equal
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

Local validation: source/data checks passed 2 tests / 27 assertions; language
boundary passed 6 / 11; static lint reported zero errors and warnings. Both
document structure validators and `git diff --check` passed. None of these
checks loaded the new namespace or executed an actual target.

Future execution must first produce the real normal build artifact and live
fresh-consumer facts from these definitions; neither model results nor a green
unrelated CI run substitute for them. This increment stops at an exact source
candidate for OWN to route for independent review. No review, publication,
runtime run, release, migration, self-hosting or full task closure is claimed.
