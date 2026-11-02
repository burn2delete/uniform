# Effectful Reader provider boundary

Task: `reader/finite-remaining-work/extension-policy-closure` / FL-P01-T01.
Inspected source: `a1aa3651f226359190db282e82375b89de661736`, tree
`eacda6558d685b37afaa7c072bd0b534606f6007`. This supplements the historical
[replay decision](extension-replay-decision.md) with an Auth/Build/runtime
interface trace. It does not reopen the accepted pure implementation.

## Actual available interfaces

| Interface | Actual behavior | Consequence for effectful admission |
| --- | --- | --- |
| `gravity.reader.dispatch/dispatch`, private `read-input` | Resolves one fixed host handler; checks exact policy and seven grant fields; invokes separately supplied `:invoke`; returns value and invocation metadata. | The provider binding is explicitly a trusted host input. This leaf does not issue authority, authenticate a result, or implement normal Gravity effectful dispatch. |
| `gravity.capability-validation/capability-validation-facts` | Intersects explicit package/provider/deployment facts and projects policy decisions. | Its namespace contract explicitly denies grant authority, provider-trust authority and canonical L15 authority. Supplying its output cannot authorize the Reader callback. |
| `record-provider-call!`, `:compile-time-replay` branch | Copies provider, capability and input/output digests from source arguments into checker records; defaults replay/audit metadata. | This records declarations, not a trusted provider invocation with the actual returned bytes. A declared digest is not an authenticated effect transcript. |
| `p15-s23-checked-core-authority-structural-operations` and reference runtime setup | Selects allocation/string construction and stdout capture; the reference setup names managed allocation and transcript capture providers. | These are not a build file-input provider. Their opaque source/runtime authority cannot be reinterpreted as permission to read a resource. |
| `gravity.build.incremental.artifact/source-artifact` | Derives incremental/cache/build graph records from supplied source and stage operations. | No provider call or privately issued input-result receipt is produced by this constructor. A cache manifest does not establish grant provenance. |
| `sh03-reader-resolved-result!` | Runs the selected Gravity producer with three data arguments, then its verifier with the original inputs and raw result, then resolves digests. | No separately authenticated effect result reaches the verifier. Its fresh reconstruction cannot repeat provider IO. |
| `sh03-reader-internal-product-authority` | Private Object identifying the internal precomputed Reader-product route. | It authorizes that product route only; it does not issue filesystem grants or authenticate an arbitrary callback result. |
| `sh03-reader-read-text` / sealed-text record | Gravity handler really returns its seed during raw construction. | There is no corresponding Gravity `read-input` execution in this implementation. Calling the host leaf and copying its output would not establish a Gravity handler invocation. |

The private builtin switch in `p15-s23-stage2-runtime-invoke-builtin` has no
file-input/provider operation. The scoped generic builtin route delegates to
that switch or the separately controlled compiler-internal builtin route; it
is not permission to install an arbitrary caller callback. The missing cut
therefore includes a fixed effectful execution bridge as well as grant/result
authority. It is not just another field in the Reader policy.

Sources for the table are the existing Reader dispatch namespace;
`capability_validation/contract.clj` and `authority.clj`;
`components/foundation/00082-record-provider-call.clj`;
`components/bootstrap-p15/00675-p15-s23-closed-core-fail.clj`;
`00661-p15-s23-reference-runtime-adapter-validated-invoke-01-authority-and-provider-setup.clj`;
`00588-p15-s23-stage2-runtime-invoke-builtin.clj` and
`00590-p15-s23-stage2-runtime-recur-signal.clj`;
`build/incremental/artifact.clj`; and Reader components `00813` and `00828`.
All component paths above are under `bootstrap/clojure/src/gravity/bootstrap_impl/`
unless a namespace path is given. No inspected projection is promoted into a
trust root by this report.

## Smallest two concrete contract alternatives

1. **Hermetic declared-input capability (recommended first cut).** Auth owns an
   invocation-scoped capability for the fixed `:fs/read` provider over an
   implementation-owned immutable declared-input store. Build owns loading
   that store from declared trusted inputs and binding provider/version and
   content provenance. The Gravity handler invokes one fixed runtime bridge
   using that capability and a logical resource key. The issuer retains the
   actual result and exact grant/invocation joins; fresh verification consumes
   the issuer's recorded result without reading either the store or filesystem
   again. Caller-supplied content plus matching hashes cannot create the
   capability or the result authority. L15 explicitly permits an in-memory
   or hermetic build-input provider; this is a file-read capability serviced
   by that provider, not a new pure handler. The declaration must explicitly
   admit the `:meta` build profile rather than widening the fixture filesystem
   provider's current runtime profiles implicitly.
2. **Scoped filesystem-read capability and recorded invocation.** Auth issues
   the same narrow principal/provider/scope/build-phase/lifetime authority;
   Build's fixed provider opens and reads the authorized input once during
   original construction, records actual bytes/result/failure and provenance,
   and seals the invocation result. The Gravity handler calls the fixed bridge
   at that original boundary. Fresh verification uses the producer-owned result
   lookup and never calls the filesystem provider. This needs file identity,
   declared-input capture, redaction and read-failure semantics beyond option 1.
   It does not reuse the unrestricted `:invoke` function as proof of trust.

Both alternatives require an actual issuer and consumer, not a public record
schema plus hashes. The proposed issuer/bridge are not existing public APIs
and are not implemented by this document. No general provider protocol or
extra callback argument is introduced. Whole Build, package-manager completion,
backend completion and seed retirement are not prerequisites for either cut.

## Exact owner handoff and subsequent Reader change

OWN should route one bounded source contract decision to the capability
maintainer responsible for
`auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks`.
That owner must identify the trusted local build-invocation grant source and
the implementation-owned capability/result issuer. The Build maintainer owns
the fixed input provider and original-result capture; the Stage2 runtime owner
owns the fixed execution bridge at
`p15-s23-stage2-runtime-execute-generic-builtin-call-scoped` /
`p15-s23-stage2-runtime-invoke-builtin`. No alternate authority is inferred
from the Auth compatibility projection or reference-runtime capture.

The concrete decision is which of the two provider backends supplies the
invocation-scoped authority and immutable result, and the exact issuer/lookup
interfaces Reader may call. Recommendation: option 1 with one fixed resource
read operation and no caller callbacks. Reader retains end-to-end ownership
of the normal constructor, policy, transcript joins and verifier changes.
The owner contract must also explicitly relate the leaf's `:filesystem/read`
effect to L12's `:build/read-file` domain under `:fs/read`; a build grant must
not become runtime authority through a naming substitution.

After that interface is supplied, the first Reader source change is a fixed
Gravity effectful handler invoked during raw construction, with its complete
result in the existing extension invocation record. Before its first effect,
preflight the entire tag population and every required grant. The existing
three-argument producer/four-argument verifier data boundary must carry only
records authenticated by the chosen issuer; a serialized result or caller hash
must never grant access. The fresh verifier must reconstruct original source,
seeds and policy joins while obtaining returned values from the original
authenticated invocation, without replaying provider IO.

Required owner/Reader joins are principal, provider/version, handler executable
identity, policy, source/form/payload/span, actual input and input content,
result content, invocation, build phase, lifetime, scope and audit/replay policy.
Missing or wrong principal/provider/input/phase/lifetime, stale implementation
or grant, and altered returned value must refuse. Later invalid tags or grants
must prevent all earlier provider calls. Fresh verification must not reuse a
previous acceptance flag, and must preserve native carrier and callback guards.
Standard and sealed pure behavior remain unchanged.

## Evidence scope and outcome

This is a source inspection and precise interface handoff. No authentic
file-input grant issuer, fixed Gravity file-input bridge or recorded-result
consumer was found in the interfaces traced above. This is not a claim about
every subsystem in the repository. The old fixture replay metadata and the
separate reference stdout transcript were inspected and are not that product.
The independently reviewed pure source and PR673 are unchanged.

No additional refusal-only production path, duplicate fixture matrix or
caller-trust flag is added: none would supply the missing authority. Existing
granted/mixed/tamper fixture definitions remain unrun. The original native
goal stays blocked by its genuine standard Reader criterion and resource hold;
this supplement does not change that status. No provider IO, Bootstrap/Reader
load, Gravity execution, runtime/process/lease/probe, publication or review
dispatch was performed. Exact-source inspection is not runtime acceptance.

Local document checks passed on 2026-10-30: `git diff --check`, ASCII scan,
`clojure -J-Xmx256m -M tools/validate_gravity_docs.clj` (240 documents, 19 phase
indexes), and `clojure -J-Xmx256m -M tools/validate_full_language_roadmap.clj`.
The roadmap result is structural and its generated historical coverage is not
new execution or completion evidence. No executable source/tooling changed;
no handler, provider or integration test was run for this document-only cut.

Governing contracts: [C2](../../docs/phase-06-compiler-architecture/081-c2-reader-implementation-design.md),
[L12](../../docs/phase-01-core-language/022-l12-compile-time-evaluation-specification.md),
[L15](../../docs/phase-01-core-language/025-l15-capability-provider-specification.md),
and [Auth ownership](../../docs/component-roadmaps/auth.md).
