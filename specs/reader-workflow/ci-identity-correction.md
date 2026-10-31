# Reader composition CI identity correction

Task: `reader/finite-remaining-work/extension-policy-closure` / FL-P01-T01.
This correction starts at the actual failed CI checkout, not a reconstruction
of current main. Run `41163135997` has public head
`a1aa3651f226359190db282e82375b89de661736` and tested merge
`8a768a52970b4cfb6338725210afceb6b2d46909`, tree
`10e7f1fa368da0a78dfa56bbe02f1148b7fa2285`. Its ordered parents are main
`14a76998496db3a31f065341db3ad84ccd3624fb` and that public head.
The run and all three failed job artifacts were read before correction.
Original logs, artifact receipts and their checkout/source identities remain
retained externally. The failed run is not relabeled as passing.

## Per-job cause

**Bootstrap, syntax-regression:** 61 tests / 615 assertions, two failures.
The Syntax namespace and its declared dependency contract both already contain
`gravity.reader.pure-text`. The independent native-ingress review accepted
that fixed helper for selecting native admission before callback-capable
inspection. The pipeline test still expected the older four-element contract
and three-alias set. The helper's only dependencies are the existing C2
canonical implementation and digest leaf; it does not import Bootstrap, CLI
or release. Both explicit test inventories now include the one actual added
dependency. No production dependency, admission guard or authority is changed.
The separate printed fail-fast sentinel belongs to an intentional catalog
negative control; its command receipt passes and is not an extra defect.

**Macro, macro-regression:** 16 tests / 256 assertions, three failures.
Two failures pin the multiple-definition artifact to
`sha256:aa7c78fa3b2fbd8568f6b1baf92f224876d2452ae20e107b48614aba13039901`;
observed was
`sha256:1617a4b0490daf70aeb539a11f350b1ef73d23033b89f26d5349b7c65622101d`.
The other pins both spellings to
`sha256:f47b8ed8b4f008970ba41a8e06e37f6cfc6f2899179ba8676f3c53dd7836788a`;
both observed
`sha256:8334722f0514a84a726066c269d64167755cad42b3bde1e9f613fe09c778c0a4`.
These are whole-artifact identities for tests explicitly selecting a
development-current Reader source, not an immutable Reader fixture.
Every existing non-golden assertion passed, including exact expanded forms,
normal constructor parity, complete verification, paired identities,
provenance, trace origins and tamper controls. This observation is narrower
than a complete historical before/after artifact-value comparison.

`syntax/reader-semantic-binding` constructs the authenticated envelope identity
from actual Reader source-content and plan-semantic hashes as well as Reader
products. Syntax retains that binding in the syntax objects and parent
artifact. `macro/artifact-payload` includes `:sh04-syntax-artifact`; its id and
syntax-stream-id are identity inputs. Thus an implementation provenance change
is an identity input even when the source fixture and expanded forms agree.
The Reader source SHA256 changed from
`a74e3d1d3bc3d7627493586c78367844b6ba41980cb65b2bee1638fe3273928a` on the
tested main parent to
`4d51fe5d7ee907e88445aa7425ed074918357a2595775fde7fe657c92ca2d825`
in the merge. These are observations, not updated admission pins.

**Resolution, resolution-regression:** 28 tests / 879 assertions, one failure.
The expected complete artifact was
`sha256:49d37a2f0d79fb8902e3be9928cab495e734997f5e3e15bfee186ae5b9ed8bc1`;
observed was
`sha256:da842c6c8fabb1f8b6f041dc01554ed3cd1044a93ce15b91f1e434207a9c4b80`.
`sh06-resolution-artifact-identity-input-with-chunks` includes upstream Macro,
syntax-stream and trace identities, the authenticated request's source revision,
resolved analysis, binding/resolution chunks, dependencies and invalidation.
The existing accepted/rejected real-result, binding, provenance, source/plan,
unresolved-reference and invocation assertions passed. The Macro and Resolution
production namespaces, Resolution identity constructor and both stages' tested
fixture text are byte-conserved from the main parent of the failed merge.

The source equations and CI results explain why these literals cannot remain
unconditional constants for current-source construction. They do not prove
every historical returned field equal or grant runtime acceptance.

## Bounded correction and negative controls

No new observed digest is substituted for an expected digest. The Macro tests
now assert both actual C3 parent joins and require each upstream id independently
to affect artifact identity. Existing complete artifact recomputation,
constructor/verifier parity, exact forms, paired identities, trace and forgery
checks remain. The Resolution test recomputes the complete artifact identity,
asserts the authenticated request source revision and each actual upstream
join, rejects missing upstream ids, and requires each to affect the identity.
Existing semantic and verification checks remain. The historical fixed values
above remain failed evidence rather than new expected values.

The new compiler-free `gravity.reader.ci-identity-test` reads the actual source
forms. It checks the Syntax dependency and the acyclic helper requirements,
evaluates only the pure Macro/Resolution preimage projections with supplied
DATA ports, and proves actual upstream and implementation fields survive.
Negative controls remove the C3 parent, remove implementation source provenance,
and separately remove each of five Resolution joins. They detect those lost
fields. The projections are isolated models, not authenticated artifacts or
Gravity runs; hashing is not replaced in production or in the real CI tests.

## Local validation and review boundary

The compiler-free selection passed three tests / 17 assertions. Bootstrap,
Syntax, Macro and Resolution namespaces remained unloaded. Language boundary
passed six tests / 11 assertions. Direct clj-kondo of the four affected test
files reports zero errors and zero warnings. `git diff --check` passes.
No local Bootstrap, Reader, Gravity producer, shared-resource lease, process
probe, provider or held 6343 operation ran. Real changed pipeline tests remain
unrun locally. Hosted CI after independent affected review must execute them;
the previous failure is neither a network outage nor permission to rerun an
unchanged job. No new source/proof/admission pin is refreshed.

Requested review: the two dependency expectations, replacement of three fixed
whole-artifact golden sites with the explicit lineage invariants, preservation
of all semantic/verification controls, and compiler-free negative models at
the exact successor tuple. If a stage owner requires an immutable artifact
golden, that owner must supply a fixture with a fixed Reader implementation and
its complete input/provenance preimage; the existing development-current
fixture is not such a fixture. No whole-stage redesign is requested.

The effectful-provider report at `f07efdf4` remains a separate frozen source
handoff. PR673 remains draft at its existing public head. Original genuine
Reader acceptance, the shared resource hold, effectful admission, full Reader
closure, release, self-hosting and seed retirement remain open. This local
candidate makes no publication, CI-pass, review-acceptance or merge claim.

## Follow-up: complete independent identity equations

Candidate `35d172be12d602e50961525299417e260470f3df`, tree
`a612e525f3667730fee198e2eb58e3320a2f222f`, received an independent
CHANGES-REQUIRED review, SHA256
`9395811f2a16ac1fa5a1f1db40597ed1ee8617887bd0b802fc2847db1055ca0d`.
Its two P2 counterexamples were valid: losing `:document-set` from Macro's
preimage or `:invalidation-id` from Resolution's preimage escaped the new
partial lineage predicates. The original CI failure and this failed review
remain history. The earlier 3 / 17 model result was not a complete oracle.

The successor adds test-only `gravity.reader.identity-expectations`. It states
the complete existing artifact equations independently of the production
artifact-id and preimage helpers. Macro's 19 fields include the full boundary
and selected plan binding, document and execution contracts, environment,
diagnostics, run summaries, ordered chunks and authenticated Syntax parent.
Resolution's 14 fields include analysis, source revision, all upstream ids,
binding and resolution chunks, dependency graph and invalidation. Its chunks
and dependency ids are independently derived from the artifact's actual rows
and edges with the existing fixed domains and ordering. Actual Reader-dependent
parents remain dynamic inputs, not new observed digest constants.

The real Macro/Resolution tests compare the entire production preimage to the
independent expected map and the actual id to the independently hashed expected
map. Macro additionally checks fixed document/kind/status/task metadata, the
declared governing document and macro environment, and the full selected plan
fields against the separately obtained current Macro binding. Existing exact
semantic output, verifier, paired-source, provenance and tamper checks remain.

Macro uses the existing native `gravity/canonical-edn-v1` tagged value encoding;
the test equation independently states its scalar/collection tags, UTF8 hex
ordering and printer settings. Resolution uses the existing hosted Reader
canonical encoding primitive on the independent expected preimage. Sharing a
SHA256 or canonical encoding primitive does not share either production
artifact projection. Neither production artifact-id helper supplies an expected
value. No general hashing framework or new artifact format is introduced.
The encoder is test-only for already admitted artifact DATA, not a replacement
admission validator or a claim of complete native-carrier conformance.

Compiler-free controls drop and alter every top-level preimage field, including
both independently reported losses. They also exercise each selected plan
field, raw/run-summary fields, selected resolved-expansion fields, ordered
chunk descriptors, dynamic parent/source/analysis/trace/stream values and
invalidation. Eighteen modeled entries cross the existing 16-entry identity
chunk boundary; reversing entries changes the expected identity. This width
is existing serialization structure, not an input admission allowance.
Canonical controls check scalar tags against inspected actual scalar functions,
the fixed domain wrapper, map order, list/vector distinction, printer binding,
metadata refusal and default-comparator assumptions. Inert source models retain
their port namespaces until the test finishes, then remove them.

The expanded compiler-free suite passed 6 tests / 259 assertions, with Bootstrap,
Syntax, Macro and Resolution namespaces unloaded. Intermediate private model
preparation failures (an incorrect field count, early model namespace removal,
metadata-bearing quoted test lists and a misplaced closing parenthesis) are
retained externally, not relabeled as passes. Real changed pipeline tests
remain unrun locally. Source/model success does not establish a corrected CI
result, runtime acceptance, resource clearance or review acceptance.

## Current-main source-model adapter

A normal conflict-free merge of main `3f22e6f5` preserves the reviewed complete
oracle equations and all newer production and Resolution regression behavior.
Main moved the Resolution preimage and chunk implementations to
`gravity.resolution`; the old Bootstrap definitions now forward to that stage.
The two inert source models initially errored by evaluating those forwarding
wrappers without loading their destination namespace. They did not reach the
identity assertions. This failed composition observation is retained separately
from the independently accepted `f07a7805` oracle source.

The test adapter now reads and evaluates the actual moved stage definitions
with explicit operation DATA ports. It does not load the stage or Bootstrap.
The independent expected equations, codec rules, production code and existing
semantic assertions are unchanged. The complete models again pass 6 tests /
259 assertions. All eight ordinary static checks pass on the merged head;
focused adapter lint has zero errors and warnings. This adapter delta requires
its own affected review; the earlier oracle acceptance does not cover it.
Publication and automatic hosted CI for this composed correction remain pending.
