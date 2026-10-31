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

Before/after comparison, regression checks, contextual lint, independent review,
exact-head CI and clean current-main integration preflight are pending.

## Unsupported boundaries

Legacy records still synthesize syntax IDs from the form index and carry the
fixed hosted/JVM context. These are preserved compatibility data, not canonical
C15 support for unavailable metadata or other profiles/targets. Redaction
records are retained, not applied. The API does not sanitize secrets or define
safe public output. Stream verification, schema/catalog semantics, rendering,
localization, generated-origin authentication, source admission, full compiler
composition, migration, release, self-hosting and seed retirement remain open.
