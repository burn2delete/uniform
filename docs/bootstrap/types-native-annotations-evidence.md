# Native type annotation checks

## Purpose and governing contracts

D1/D2/D3/D8/D9, L5/L17, C1/C7/C15, BOOT8 and TEST2 govern this
increment. The source parent is
6b55e82bf558b740ec681f941b9169e7973e4f09, tree
b7a477a38be10b0ee2ffac53c08030acac8a9fda. It contains only the
existing source cohort and verified published Main
41aaea100091dc80067328e9b066f7378845eca3. Inherited increments retain
their own attribution; this work grants them no new acceptance.

Move the existing typed/assert, typed/value and typed/return native checks
from language-and-analysis components 00070 and 00072 into gravity.types.
These three checks already participate in the ordinary Stage0 typed-core
pipeline. Preserve their original decisions, result values, diagnostic
messages, source subjects, ordering and compatibility limitations.

## Contract boundary and public API

The new stage APIs are check-native-assert!, check-native-value! and
check-native-return!, each taking explicit operations, the original native
source node and the original argument-fact sequence. The operations expose
the existing type-keyword catalog, typed diagnostic publisher, type-token
classifier, literal classifier and primitive compatibility predicate. Resolve
each supplied Var at its original use point, before evaluating that call's
arguments. A callback or catalog is not proof authority.

The stage owns native annotation shape, unknown-type and mismatch decisions.
Bootstrap keeps the original two-argument compatibility adapters and retains
type keyword/compatibility policy, inference, node/fact construction, profile,
effect, ownership and safety decisions, source authentication and diagnostic
publication. Existing hosted C7 data/report/runtime APIs retain their separate
contracts. gravity.types imports neither Bootstrap nor a consumer.

## Artifact inputs, outputs and diagnostics

Inputs are caller-owned native nodes and type/value facts. Preserve borrowed
identity, metadata, type spellings, declared versus independently established
actual facts and source/generated-origin data. Return the original nil or
expected-type value, propagate the owner's return/exception, or emit the
original L5-ANNOTATION-REQUIRED or L5-TYPE-MISMATCH refusal through the supplied
diagnostic operation. The enclosing typed-core artifact keeps its current
producer, source identity, pass records and deterministic ordering.

The dependency direction is existing core/fact production -> native Types
checks -> existing type/effect/ownership/safety consumers. These checks grant
no backend or runtime permission and cannot repair later legality.

## Tests and acceptance

The separate cold gravity.c7-type-checker-test baseline passes 17 tests /
473 assertions under the existing 2g/180-second child. Retain its Bootstrap-free
API/metadata checks and add a separate gravity.types.pipeline-test for native
checks. Register that complete namespace in both standalone dependency maps
and append its own native CI child without changing existing commands,
selectors, watchdogs or the workflow.

Exercise actual typed-core source emission, all three native call sites,
real annotation/type-mismatch refusals, paired source/plan identity and emitted
C text, original subject metadata, returning diagnostics, first-refusal order,
late function/catalog Vars and callable selection before argument lookup.
Compare all original functions rebound together against extracted functions
on identical live inputs and full returned/refused outcomes. Recover the old
bodies and conserve all surrounding classifiers/callers. Run affected cold and
native regressions, CI coverage controls, the language boundary, contextual
lint, formatting, docs/roadmap and independent semantic/adversarial review.
Own hosted evidence and exact coordinator integration remain separate gates.

## Unsupported boundaries

This extraction does not implement full canonical L5/C7 typing, general
constraint solving, nominal/composite validation, integer-width or numeric
conversion proofs, schema or initialization legality, profile/effect/capability
authority, ownership/safety admission, source authentication, layout or FFI
legality, or diagnostic redaction. Preserve the native compatibility predicate's
existing permissive categories without presenting them as proven types.

It neither executes private C6/C11 or default frozen Reader routes nor renews
source pins, migration, release, self-hosting or seed-retirement evidence.
Actual typed-core construction uses the ordinary Stage0 source reader; actual
paired executable-plan checks use the explicit current development route.
No native toolchain, live provider, broker/controller or Main action is part
of this slice.

## Implemented extraction and development evidence

Three native checks now live in gravity.types, raising its declared public API
from 29 to 32. Five distinct function/catalog ports preserve per-use Var
selection through one private selector. Bootstrap retains all three original
two-argument adapters. Its type keyword and primitive compatibility catalogs,
type-token/literal classifiers, call-site ordering, source loader and diagnostic
publisher remain unchanged. No new type rule or admission route is added.

Expanded cold C7 passes 17 tests / 494 assertions and remains Bootstrap-free.
The complete native Types namespace passes 10 / 139 under the unchanged
2g/180-second child. Actual source covers the existing typed-core fixture and
all three native call sites in both source extensions; actual malformed or
mismatched annotations retain L5 refusal ids, original source spans, expected/
actual types and remediation. Actual paired Hello plans retain deterministic
plan identity and emitted runtime C text. No native compiler is executed.

All three immutable original functions are rebound together for differential
comparison. All 45 complete raw and parsed outcomes match: 36 native cases and
nine actual typed-source artifact/refusal cases, nineteen returns and twenty-six
refusals. Returning diagnostics retain their original continuation behavior;
native subject metadata, unsafe/check/human-review fields and source facts are
unchanged. Capture SHA256:
6fc45a3d7c6c3d70b90dd06843426369268715a1b67b64a75ceb6cbe2d7fbba9.

Source reconstruction recovers all three original bodies and the entire
prior stage bytes outside its declared contract additions. Nine named
surrounding loader/caller/classifier/diagnostic/workflow paths remain exact.
The prior ten CI commands, selectors and watchdogs are unchanged; one complete
native Types child is appended. Coverage controls reject omission/substitution.
The complete CI harness passes 54 / 580; boundary/ownership passes 48 / 1202.
Lint, tools/ci formatting, diff and both document validators pass.

An initial local ownership check used an incorrect nonexistent namespace name;
the registered runner rejected selection before tests. That log is retained
separately. The corrected command selects the existing owned sh01-ownership-test
alongside the language-boundary test; no runner scope or selection policy was
changed. Initial test syntax errors were caught and corrected by lint before
execution. No semantic test failure was suppressed or converted to a pass.

Complete contextual lint, independent source/adversarial reviews, own hosted
CI/artifact audit and exact coordinator integration remain separate evidence.
Existing legacy permissive nominal and numeric compatibility is recorded as a
limitation, not promoted to canonical type, layout, conversion or safety proof.
