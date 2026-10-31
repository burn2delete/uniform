# Diagnostics: native seed records and identity data

## Boundary and contracts

D1, D2, D3, D8, D9, C1, C15, BOOT8 and TEST2 govern this increment.
The independent source base is main
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

## Unsupported boundaries

Legacy records still synthesize syntax IDs from the form index and carry the
fixed hosted/JVM context. These are preserved compatibility data, not canonical
C15 support for unavailable metadata or other profiles/targets. Redaction
records are retained, not applied. The API does not sanitize secrets or define
safe public output. Stream verification, schema/catalog semantics, rendering,
localization, generated-origin authentication, source admission, full compiler
composition, migration, release, self-hosting and seed retirement remain open.
