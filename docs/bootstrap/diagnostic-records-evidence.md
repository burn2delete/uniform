# Diagnostics: native seed records and identity data

## Boundary and contracts

D1, D2, D3, D8, D9, C1, C15, BOOT8 and TEST2 govern this increment.
The original independent source base is main
58196ba55a8bf4a2a8d82d6622ba85a04d7d1cb7, tree
030f46f68c520dae0b809d32a66f0f3d86630288. It does not contain or depend on
the unmerged effects graph candidate in PR699. That candidate's failed
Resolution check remains a separate nonpassing observation.

Move the existing native C15 record and stable-ID bodies into gravity.diagnostics.
The stage consumes explicit span/hash/identity operations, diagnostic fields,
source paths, primary artifact, facts, remediation and optional related/origin/
redaction/lifecycle data. It emits the existing diagnostic record, ordering key
and legacy ID. C15 wrappers retain dynamic operation interposition and their
public arities. The stage imports no producer, facade, Bootstrap, CLI or release.

The preimage remains rule, stage, primary artifact and facts, serialized through
the existing pr-str protocol. Wording does not participate. This is not a new
canonical map encoding or input authentication. Supplied callbacks are seed
implementation dependencies, not proof. C15 retains validation and rule decisions.

## Development observations

The before producer executes at clean main58196ba5 in the records checkout.
The after producer executes with working source changes over that base. Neither
observation is execution at a later committed head. Both consume retained real
C8-UNDECLARED rejection data for the paired source spellings. The original C8
source-production/control context is unchanged; this is diagnostic-data intake,
not a fresh source compilation or canonical effect-to-C15 adapter.

Each spelling emits a structured diagnostic with the actual upstream span,
complete upstream diagnostic as facts and the retained Core primary ID.
A native validation fixture exercises the existing C15 validator; changing the
diagnostic ID to diag-forged rejects with C15-ID / missing stable-id. Its
catalog/golden/redaction labels are explicit legacy test data, not performed
golden conformance, redaction or publication evidence.

Twelve complete record pairs and twelve captured serialized-text pairs match.
Both imported input text hashes verify; both diagnostic IDs recompute under
the unchanged legacy preimage protocol. The IDs differ between spellings
because the retained facts contain their distinct actual source paths:

- gravity: diag-5bd08a38f040f4069d2c1390839b2a0ea3c113691c3a69106cbec67b3b0961e7.
- qst: diag-28d25cce3d2a72a9680c314539659eb2613e64055b09c57b75b23d7d82cf2a66.

The C15 bootstrap-free suite passes 6 tests / 87 assertions; the existing base
carrier regression passes 1 / 46. Tests cover exact callbacks and order, custom
identity/record interposition, unchanged ID across wording changes, changed ID
for rule/stage/artifact/facts changes, retained spans/origins/facts/options and
the absence of a Bootstrap dependency. Ownership passes 21 / 821 and language
passes 6 / 11. No inventory count, catalog entry or registration is changed.
Docs validate 240 documents / 19 indexes; roadmap and governance validate.

Changed-file contextual lint covers all five changed Clojure paths against the
exact base: one unchanged baseline warning, zero blocking warnings/errors.
An initial local lint invocation could not find clj-kondo before producing a
lint observation; its unavailable receipt is retained. The existing local
clj-kondo v2025.10.23 then produced the complete comparison. No download,
configuration change or warning waiver was used. An initial test invocation
selected C15 through the self-hosting runner, which refused the unowned namespace
before execution. The correct existing bootstrap-free runner produced the
reported result; no source failure was retried or reclassified.

The first reviewer conditionally accepted source
6b23279418c1fb6787455c77411901437ccb9c04, tree
41ad2d4bdf944b2ddde1a1aa9d8f82bfd90dd2e8, and its evidence-only child
61f227896379ddc69426ad9b442063b3f02c9777, tree
f9d8f3019ce8ec33c1ee99cb843ac44abd614e08. The second review found a blocking
metadata omission at 61f22789: the C15 component manifest did not yet list the
new diagnostics source dependency or diagnostics/digest test dependencies.
That exact candidate was not independently accepted for integration.

The correction adds those existing component IDs, attaches the namespace
contract to all four public Vars and checks each API's arity metadata. An
ownership regression compares the manifest with the stage's declared Gravity
dependencies and the test file's actual imports. No component count, catalog
entry or registration changes. C15 now passes 6 / 95; ownership passes
22 / 824; language passes 6 / 11. All six changed Clojure paths pass contextual
lint against the original main base with zero warnings/errors. The preceding
one-warning comparison remains historical evidence; no warning rule changed.

A further producer run at clean 61f22789 with the working metadata correction
again matches all twelve complete and serialized records, imported hashes and
diagnostic IDs against the original before control. This is a working-source
observation, not execution at the correction's later commit.

Initial source CI 41212179123 is terminal cancelled: five jobs succeeded, six
were cancelled and readiness was skipped. Actor, cause and descendant cleanup
are not established. No manual cancellation or job retry is performed. The
existing 61f22789 run 41212309115 remains a separate observation. The corrected
candidate requires exact independent review, its own successful CI and clean
current-main integration preflight before integration.

## Current-main composition and retained CI failure

Main subsequently advanced to f485e117186084a5ab4a04358831f4f93e0dab0a,
tree 3eca3173a9605e7c121fa217ee5fecaf070c506b, through integrated PR701.
Its three incoming paths change Core verification-session invocation forwarding,
the associated Reader test and its specification. Normal main composition has
no conflict and changes none of this slice's implementation, tests or metadata.
Before this report update, the composed tree is
8db59d4dbcf59867d61170036b9dadf01fe1ad7d, exactly the tree of GitHub's tested
merge 70bd7798465e36102037124372f3c46cdac672c3, whose parents are that main
and candidate 2d8f5448992ff939a3d705be7a3ea0f617121b28.

Run 41212844570 has a nonpassing source-snapshot job 124571327761. Its single
launch exits 1 during classpath construction: core.specs.alpha 0.4.74 cannot be
resolved from central. The retained read-only dependency diagnostic reports
the JAR/POM missing. No snapshot implementation execution or completed package
comparison is established. The tracked source-mutation diff is empty. All six
receipt-manifest entries verify; SHA256SUMS hashes to
e7c1e405d8e4320372d219e82a6d56d076e69318941d3e82c2e716246b4d60be.
The raw job log hashes to
1acf4eadc7b2dee4b7d82501d1dbf3ece9bae0ca267761d4a6c80115bee636e8.
The observed text establishes an unresolved dependency, not an HTTP status or
source defect. No dependency download, failed job or upload is retried, and no
cache, runner, watchdog or CI configuration is changed.

Both independent reviews conditionally accept exact 2d8f5448 against original
base 58196ba5. Composition with the actual new main and this evidence report
require fresh exact review and validation. They provide no remedy or waiver
for the retained failed run. The original before/after production context stays
unchanged; no fresh source-production or authority evidence is claimed.
With the working main composition, the base carrier regression again passes
1 / 46, all six changed Clojure paths pass contextual lint against f485e117
with zero warnings/errors, and docs validate 240 / 19. No inherited Core
session source test or restricted source/native path is rerun by this slice.

## Unsupported boundaries

The earlier compatibility records synthesized syntax IDs from the form index
and carried fixed hosted/JVM context. The phase-availability correction below
removes those fabricated facts. Redaction records are retained, not applied.
The API does not sanitize secrets or define
safe public output. Stream verification, schema/catalog semantics, rendering,
localization, generated-origin authentication, source admission, full compiler
composition, migration, release, self-hosting and seed retirement remain open.

## Phase availability correction

The current source composes actual main
b3f3390e32acc89842ddd3d432ed4f7c7c8648a6 with the earlier candidate
2508a2efc2daa1213ac671480cb4e39a9d031f2a. The two metadata conflicts were
resolved from complete parents: all main ownership forms remain unchanged,
the one C15 dependency regression is appended, and only the Diagnostics map
row is selected from the earlier candidate. Incoming implementations and CI
policy are inherited unchanged. This is working-source evidence, not a claim
that the earlier failed candidate passed. A later actual-main composition and
fresh exact integration checks remain required.

C15's availability rule and the Gravity C15 owner's context/primary predicates
govern the correction. `diagnostic-record` accepts explicit optional `:profile`,
`:target` and `:syntax-id`; absent values remain present as nil. It does not
derive syntax identity from a form index. The native records facade and
Bootstrap compatibility wrapper forward all three values. The source-artifact
adapter supplies the parsed module's actual profile/target; its four synthetic
diagnostic-system examples use `:compiler-diagnostics` and have no invented
syntax identity. Its stream carries the same stage; the internal pass name
remains unchanged.

Native validation permits unavailable context/syntax only for `:read-source`
and `:compiler-diagnostics`. Other stages require keyword profile/target and
a nonempty string or keyword syntax ID. Missing keys and false/invalid values
remain refusals. Validator and capability-report predicates agree; failed
checks produce an incomplete report. A report still trusts legacy supplied
redaction/golden labels and upstream status fields: it is not authentication,
canonical C15 proof or publication authority. Diagnostic-system exception data
keeps unavailable profile/target/syntax explicit and preserves the override's
subject kind separately from the diagnostic stage.

The original paired C8-UNDECLARED input has no profile, target or syntax ID.
The corrected constructor retains the actual effect-check stage, complete
upstream facts, span and primary artifact, with explicit nil context. Both
legacy diagnostic IDs listed above remain unchanged and recompute. Native
admission now correctly refuses each record with C15-SCHEMA / phase-context;
forging its ID separately refuses with C15-ID / stable-id. Neither rejection
is hidden by retagging the semantic stage or inventing context.

Both input text/value pairs and their hashes are unchanged. Six complete and
serialized record/bundle/forged-record pairs differ only by the three explicit
context corrections. The new context refusals are intentional behavior changes;
the historical twelve-record equality claim above does not describe this source.
Earlier intermediate local captures that accepted nil at any stage are also
superseded by the phase-aware validation. One new negative fixture initially
expected location rejection after deleting the primary artifact; the existing
ID check correctly ran first. The fixture now reidentifies the malformed input
to exercise location validation separately; no production check was reordered.

Focused tests cover early/system nil availability, semantic-stage refusal,
known context forwarding through both wrappers, actual span variants, missing
keys, malformed values, identity preimages and incomplete native reports.
Bootstrap-free tests and Bootstrap carrier tests run in separate fresh JVMs.
No cold-load guard, catalog count, source identity protocol or CI configuration
is weakened. Full diagnostic binding, redaction, rendering, canonical schema,
source admission and all broader unsupported boundaries remain open.

Main then advanced through JS PR719 to
43c69654b3f2daefa05e3902cbe78b469af47d96, tree
088aa0c632516704aa552a9fa2a046828661fabc. A normal composition adds that
actual parent to the correction's intermediate source
590bf415af9a9980b40ee20121ec5f1dca248598. Its only conflict is the ownership
test file: the complete new main file is preserved and the one reviewed C15
dependency test appended. All incoming JS source, consumers, tests, owner/helper
records and evidence are conserved. The C15 correction's implementation and
focused tests are unchanged. No prior CI green is transferred to this composition.

Fresh composed-source checks pass: C15 8 tests / 216 assertions and affected
C15/C16 cache 4 / 49 (each in a fresh leaf JVM), Bootstrap carrier/wrapper
2 / 56, ownership 30 / 959 and language boundary 6 / 11. The paired emission
and verification run again confirms both unchanged input hashes, six complete
context-only data/text changes, both unchanged/recomputed legacy IDs, two real
semantic-context refusals and two forged-ID refusals. This remains a bounded
native diagnostic-data slice, not fresh canonical source compilation or proof.
