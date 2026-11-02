# Syntax plan runtime extraction

This increment moves the pinned SH-04 syntax plan source checks, plan identity
checks, lazy cache construction and freshness checks, runtime host-error
containment, native result admission and declared-slot digest resolution into
`gravity.syntax`. The existing bootstrap functions retain their arities and
supply explicit, trusted seed dependencies as callable Vars. They preserve
late interposition. The source and function pins remain unchanged.

D1, D2, D3, D9, L1, C1, C2, C3, C15 and TEST2 govern this boundary. The public
runtime API and each function's operation inputs are recorded in the namespace
contract attached to the public Vars:

| API | Inputs and output |
| --- | --- |
| `boundary-fail!` | Rule, source, missing field, observed value and facts -> existing structured C3 diagnostic |
| `resolve-plan-source-path`, `read-pinned-plan-source!` | Seed anchor/configuration and request source -> selected path or exact pinned source record |
| `plan-identities`, `build-plan-binding!` | Compiled plan or pinned source/compiler ports -> exact identity map or authenticated syntax plan binding |
| `plan-binding-cache`, `current-plan-binding!` | Builder operations or request/cache -> lazy process binding or freshly checked binding |
| `execute-plan!` | Source, binding, function and arguments -> Gravity runtime value with host metadata removed |
| `require-result-carrier!`, `accept-result!` | Source and result -> native carrier or accepted Gravity result |
| `resolve-object-template!`, `resolve-stream-template!` | Source, template and digests -> object or stream with only its declared identity slot resolved |
| `resolve-request-preimage!`, `resolve-stream-request-preimage!` | Source, request and preceding digests -> checked ordered request preimage |
| `resolve-template!` | Source, plan binding, template result, reader binding/revision -> freshly replayed resolved syntax and verification reports |

Every API also takes explicit operations as its last argument. The bootstrap
retains the cache holder
for its existing process lifetime; the stage constructs the delay and checks
fresh source and plan identities on admission. Operations are trusted host
composition, not caller proof authority. The dependency points from bootstrap
to syntax and from syntax to explicit operations and the existing verifier.
The stage does not import or resolve bootstrap.

Inputs are the pinned Gravity source, plan and function identities, generic
compiler and runtime operations, declared digest requests and reader binding.
Outputs are the unchanged authenticated plan binding, native Gravity results,
resolved syntax templates, digest identities and verification reports.
The stage owns C3-ID source/plan/digest refusals and C3-SHAPE runtime and result
containment through the existing diagnostic catalog and source-span producer.
Digest placeholders are interpreted only in declared identity slots; ordinary
source maps remain semantic data. Interruptions propagate with the thread
interrupt flag restored. Fatal host failures outside the existing contained
classes retain their previous behavior.

The shared trusted-carrier validator intentionally performs native shape,
metadata, comparator and cycle checks without quota accounting. Its transitional
five-argument API ignores the supplied legacy limits. This extraction preserves
that behavior: a native value deeper than 256 is accepted at this boundary.
The existing `:bounded-syntax-result-carrier` diagnostic field is retained for
compatibility and does not certify a depth, width or node budget. This boundary
also adds no compiler time, memory or instruction allowance.

The complete genuine C3 artifact and pinned binding compare equal across the
extraction. The C3 product also equals the artifact recorded before the preceding
integrated pipeline slice, with identity
`sha256:c1e2b235f9efb1087cb1c8ddf2df2f53873853f0cd419b3034d466d11b643859`.
The actual syntax source compiles to its pinned 102-function plan. A genuine
generated template replays and resolves with the same reader binding, revision,
syntax identity and serialization identity. The paired source extensions,
full Unicode/metadata fixture, existing shape refusals and downstream tamper
checks continue to pass.

Direct runtime tests reject actual wrong-length and same-length changed source
bytes, altered cached source/plan identities, removed plan functions, malformed
digest dependencies/order/identity slots, substituted reader binding/revision,
foreign carriers and host metadata. Execution tests verify the existing host
failure classes and interruption propagation; operations captured before
redefinition still invoke the owning Var. Ordinary source maps containing
`:digest-ref` remain data. Cold stage loading does not load bootstrap.

Re-evaluating the three original runtime adapters from base
`6c7df70327e8ccfe138bd21ffa0db89a11cee99e` in a fresh JVM reproduced an older
SH-04 test's stale three-argument worker callback error. Its callbacks now match
the existing four-argument worker. Positive tests use explicit current-source
context and retain `:development-unadmitted` provenance. The two-argument
standalone entry still performs fresh validation and refuses the currently
changed SH-02 snapshot under frozen default admission; the fixture now checks
that refusal rather than expecting acceptance. No production arity, source
pin or admission rule was relaxed.

Executed development checks:

| Check | Result |
| --- | --- |
| Full genuine artifact and plan binding comparison | Equal across extraction |
| `gravity.syntax.pipeline-test` and `gravity.bootstrap-compatibility.c3-test` | 22 tests, 257 assertions; passed |
| `gravity.self-hosting.sh04-fresh-c2-construction-test` | 5 tests, 30 assertions; passed |
| C3 construction, verification and artifact identity helper suites | 12 tests, 163 assertions; passed |
| Ownership, catalog and language-boundary suites | 50 tests, 1025 assertions; passed |
| CI command harness | 11 tests, 101 assertions; passed |
| Documentation, roadmap and workstream governance validators | Passed; 240 documents, 19 indexes, 185 existing dispositions |
| Changed-file lint and whitespace checks | Passed with no new warnings or errors |

The existing CI syntax regression command now includes the fresh-C2 suite.
Its 2 GiB host heap and process timeout are host safeguards, not compiler
admission allowances. Source ownership and component inventories are unchanged;
the existing syntax owner and tests continue to cover these bodies. No frozen
migration or source-version reporting fingerprint was renewed.

Independent exact-source acceptance and remote integration checks are tracked
on the pull request before merge.

This increment does not complete the entire syntax migration. SH-04 reader
binding, registered-literal projection, envelope binding, rich-object/stream
adaptation, graph adaptation and earlier helper boundaries remain open. Source
path and fixed pin configuration remain bootstrap composition. It grants no
language feature, migration, self-hosting, seed-retirement or release authority.
