# Native workflow document data ownership

## Scope and governing contracts

This slice moves the remaining B10 document manifest constructor,
ordered validation checks and coverage projection into `gravity.backend.workflow`.
D1, D2, D3, D8, D9, B1, B10, B13 and B14 govern the architecture and evidence
boundary. Draft PR #1270 was opened at scope commit
`ea18898b5dfc1cbbc16056ba87f8fffe65b9a467` before implementation.

The starting source is `788990157d89ebf73c8f02f1fc01220e94829e09`, tree
`4949d6ca378ab377bde5e6eabbd77ab85bc2179a`. Published main
`fa14c00518690feb113f9e506d3c8c0b8a96ebea` is already its ancestor.
Prior slice CI results confer no acceptance on a successor.

## Public API and boundary

The stage already owns ten diagnostic context, catalog, failure, override,
fixed graph/replay text and structural predicate APIs. Three additional APIs
own manifest data construction, the existing 25 ordered shape refusals and
coverage projection: `native-manifest [operations source-path input-id]`,
`validate-native-manifest! [operations source-path artifact]` and
`native-coverage-report [operations artifact]`. Bootstrap names and arities
remain adapters. Explicit late operations preserve hash, content and callback evaluation
order, source/debug locations, generated origins and borrowed input identity.
Bootstrap retains source admission, diagnostic publication and final
artifact assembly/hash. The stage requires Clojure core and `clojure.string`
and must load without Bootstrap. Its `:must-not-require` contract names forbidden
require edges; Bootstrap is the consumer, never a stage dependency. The stage
owns diagnostic payload shape; the caller owns throw transport and publication.
These current facts supersede the earlier context-only evidence's core-only
and caller-owned failure-construction descriptions.

The constructor's three ports are `:artifact-id`, `:workflow-graph` and
`:replay-fixture`. Validation additionally uses `:fail!`, the graph/replay
structural predicates and `:diagnostic-ids`; coverage uses the same five
non-failure ports. Vars are resolved at each original use. Hash callbacks run
graph then replay, with later content reading the current roots again.
Validation evaluates graph then replay; the coverage map's compiled evaluation
order is replay then graph. Existing callback-driven hash/content mismatches
remain observable and do not become authentic evidence.

## Verification and artifacts

The cold tests emit fixed workflow/replay files under
`target/validation/workflow-native-manifest/`, reject malformed document data
with existing B10 identifiers, preserve source identity and generated origins,
and exercise all 25 refusal steps, callback return/throw identity, late Var
interposition, distinct predicate orders and callee-before-subject selection.
The SHA256 callback hashes only explicit unverified fixture data.

Development results on the implemented source:

- The cold namespace passed 11 tests and 159 assertions. Existing compatibility
  tests passed 15 tests and 128 assertions before and after extraction.
- Language-boundary and ownership checks passed 48 tests and 1202 assertions.
  The CI harness passed 54 tests and 615 assertions. CI formatting passed.
- A separate comparison using the original C4 operation and all three original
  Bootstrap roots rebound together matched complete raw and parsed records
  for 52 outcomes: 13 returns and 39 refusals. This includes all record guards,
  ten missing text tokens, returning/throwing callbacks, late diagnostic IDs,
  hash/value mutation, predicate interposition and callee-before-subject lookup.
  Receipt `/tmp/uniform-workflow-manifest-parity.edn`, SHA256
  `4fbbc29500414ad56dd3f4d1b8210d51e41de96d6f1d28a2bd41f6010a8ef99f`.
- The emitted graph is 1030 bytes, SHA256
  `5072e27d66d95833a640ec6acea56c3e992976d4ee9a35e98800fc5413ecb127`.
  The replay fixture is 554 bytes, SHA256
  `de4413358afb6b74806bc28cecfb777e1539d60e8371f983e08f2238e42ba8fa`.
- A fresh JVM loaded all thirteen stage APIs and the new native test namespace
  without Bootstrap. The exact appended 2g/180-second native command passed.
  The ten prior API bodies, fourteen protected paths, workflow and fifteen
  prior contract command vectors are unchanged. The three production body
  transforms match the inspected proposal, with the descriptive require-edge
  metadata clarification recorded separately. Conservation receipt
  `/tmp/uniform-workflow-manifest-conservation.edn`, SHA256
  `e69243c3bc95c1d3914be6479fa964a1dd52b3b1741dc0ee3a1957a90a69cdf0`.
  Document, roadmap, lint and CI formatting checks passed.

The CI catalog appends `workflow-manifest-native-validation` at the existing
native 2g/180-second settings. Results receive credit only after execution.
Final committed-source acceptance, complete current-base context, hosted CI
and integration remain separate evidence obligations.

## Unsupported boundaries

Legacy status, proof, provider, policy and replay labels are document data.
Substring checks do not parse, execute or verify workflows. Fixed policy
records do not enforce compiler work allowances or authenticate external
authority. Existing missing schema, replay, idempotency, taint and capability
semantics must remain explicit limitations of this ownership increment.
The existing model-token, tool-call and external-write policy values remain
external workflow/provider fixture data. This move adds no compiler accounting
or runtime policy enforcement.

The C2-HASH source-artifact refusal remains held. This slice will not invoke
source/file-artifact builders, retry or rebind the reader, repin source,
substitute an upstream result, or reconstruct an admitted artifact. No verified
workflow IR, source-to-backend compilation, durable execution, replay or
migration validation, capability or human-review authority, B1/B10/B13/B14
conformance, release, self-hosting or seed-retirement support is claimed.
