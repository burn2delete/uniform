# Opt-in pure normal result binding

## Declared increment

Production-source successor to `794ba6c4ba26febd5848a5b7d38b789ebac626de`
for `reader/finite-remaining-work/extension-policy-closure` (`FL-P01-T01`).
This implements a fixed, opt-in `text` registration and fresh comparison of
its actual handler result in the normal host adapter. It does not close the
full extension task or admit effectful handlers. The preceding frozen reports
remain history; this increment implements part of their proposed boundary.

## Selection, execution and verification

The existing reader-options `:extension-policy` hash selects the fixed
implementation-owned `:gravity/pure-text-reader` policy via
`gravity.reader.dispatch/pure-policy-id`. Its registrations are inst, uuid and
text; text selects the existing private `gravity.reader/read-text` function.
This is a finite registration, not a caller-extensible handler registry.
No new caller callbacks, purity assertions or authentication flags exist.

`sh03-reader-input-policy` adds `:extension-policy :gravity/pure-text-reader`
to the existing policy data argument only for that hash. The Gravity policy
validator admits this exact opt-in schema and its explicit tag set. The
producer and verifier keep their existing three/four positional arguments.
The namespace validator and original-byte Reader/replay functions are unchanged.
Legacy standard options still produce the original four-field policy.

The normal resolver still verifies the raw Reader graph before adaptation.
The adapter then checks original source slices and semantic reference closure.
Its complete tag-population preflight checks policy hash joins and reads purity
from the private implementation table before dispatch. `resource` is explicitly
unadmitted; unknown tags refuse. Thus a preceding pure tag cannot cause partial
handler execution before a later effectful or unknown tag is discovered.

The normal adapter supplies the resolved plan's actual source-content,
plan-semantic and functions-semantic identities. The result binds those
identities to the selected policy and private handler, original source/content,
form/payload/span, input seed, actual returned seed and invocation. Input/result
hashes use the existing canonical encoder and domains. They are identity data,
not authentication assertions. The private handler table remains a trusted
Clojure bootstrap implementation boundary, not a dynamically loaded handler.

`dispatch-pure-tag` produces a result, then `verify-pure-result!` freshly calls
the selected pure implementation with the verified graph's original form and
payload. It compares the complete reconstructed result, not only a claimed
hash. Source/policy/implementation/handler/input substitutions fail before
reconstruction; changed returned values or invocation/result hashes fail after
fresh pure computation. Result carriers are checked recursively before equality
can consult their collection protocols. No effectful provider is supplied.

Successful opt-in adaptation returns `:pure-extension-results`. Standard
adaptation retains its existing observations and does not add this field.
The new results are checked bootstrap data; they are not a new sealed SH03
product or replay authority. Existing standard precomputed-product/C2 cache
admission remains standard-only and is not expanded by this increment.
The pinned historical Reader hashes are unchanged; selecting current changed
source remains explicit under the existing development-source interface.

## Paired integration definitions

Under `bootstrap/clojure/fixtures/reader-extension-binding/`:

| Pair stem | Defined normal outcome with opt-in policy |
| --- | --- |
| `normal-pure` | Preserve namespace checks; return text seed and compare fresh pure result |
| `normal-unknown` | Unknown tag after text refuses before any handler |
| `normal-effect` | Resource tag after text refuses before any handler/provider |

Each stem has byte-identical `.qst` and `.gravity` files with a complete
namespace declaration. These are unrun integration definitions. Earlier
single-form planning fixtures remain unchanged. No namespace guard is relaxed
to admit them as complete source units.

## Compiler-free checks and limits

`gravity.reader.pure-binding-test` replaces `dispatch` in every execution
test. It tests real result production/comparison and normal whole-population
preflight around a modeled dispatch result. It does not execute any real
handler, Gravity Reader, Bootstrap compiler, provider or genuine transaction.
Its synthetic source identities and injected graph cannot establish source
authenticity or normal integration acceptance.

The tests cover original/fresh comparison, policy/source/implementation/handler/
payload/form/span/input substitutions, changed returned data/invocation/hash,
untrusted carriers, all four policy joins, implementation-owned effect changes,
mixed population refusals and paired source bytes. See the source-only test
namespace for exact assertions. Whole granted-handler acceptance remains open:
no trusted recorded-effect-result producer and non-repeating verifier is added.

Source reading with `*read-eval* false` checked every changed implementation
and test file without evaluating Gravity code. An initial missing close in
the host-walk arity failed that check and was corrected. A first definition
equality check used reader-created regex objects and falsely differed; raw
definition-text comparison then confirmed the existing standard dispatcher,
standard policy guards, namespace validator and raw producer/verifier unchanged.
These source checks are not runtime evidence.

The required language-boundary gate passed 6 tests / 11 assertions. The focused
modeled test command and final counts are recorded below. No genuine Reader,
handler/provider execution, held PR571 transaction, lease or probe runs. The
original blocked goal, task checkboxes, lifecycle records, resource work and
migration fingerprints are unchanged. No review dispatch, publication or merge
is performed. Independent source review and actual normal integration evidence
remain required before broader claims.

## Governing basis

Existing [Reader spec](spec.md), [plan](plan.md), [tasks](tasks.md),
[C2](../../docs/phase-06-compiler-architecture/081-c2-reader-implementation-design.md)
and [L12](../../docs/phase-01-core-language/022-l12-compile-time-evaluation-specification.md)
govern pure extension execution and source/result provenance. Effectful
admission retains the [L15](../../docs/phase-01-core-language/025-l15-capability-provider-specification.md)
grant, audit and replay obligations. New names describe behavior; existing
compatibility-bearing SH03 names and source paths are preserved.

## PR652 interface overlap

Inspected exact local commit
`e6b12e5bb8ff8a7f522ffe8068430dcb2ee5caf5`, with merge base
`ae0cd80165b2085e7ce362d0c7bfc5eeec79b2de`. Its changed path inventory has
no direct overlap with this increment's production paths. Its
`00847-c3-reader-artifact-view.clj` delegates to `gravity.syntax`, and that
namespace retains the internal Reader reverification port. Its
`00850-sh03-c3-precomputed-c2-verify.clj` extraction concerns later syntax
runtime bodies; this increment leaves the existing precomputed-product/C2
admission path unchanged. The opt-in handler-result binding is in the normal
Reader adapter before those downstream views. Later composition must preserve
the additional pure result field without treating it as a sealed artifact or
silently adding it to standard admission. No PR652 source or unreviewed
namespace migration is imported, and its integration is not a prerequisite.

## Final focused commands

```sh
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -e '(require (quote gravity.reader.pure-binding-test)) (let [r (clojure.test/run-tests (quote gravity.reader.pure-binding-test))] (System/exit (if (zero? (+ (:fail r) (:error r))) 0 1)))'
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
git diff --check
```

Focused modeled tests: 7 tests / 49 assertions, zero failures/errors.
Language boundary: 6 tests / 11 assertions, zero failures/errors.
The earlier modeled checkpoints (5/39 and 6/47) are superseded only by the
final focused result; none executed an actual handler.
