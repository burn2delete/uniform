# Reader seed table validation and indexed source positions

## Governing contracts and scope

D1/D2/D3/D8/D9, L1, C1/C2/C3/C15, BOOT8 and TEST2 govern this bounded
ownership move. Parentb3f55a5a700a972dc7e14e29943e78822251c8b3, tree
b24d91d303c610e2bb3c66b5b60f85ad1e9dcbc6, preserves unmerged draft PR1181.
No inherited source/test/CI result grants this slice migration or integration
authority. Every named prior acceptance remains bound to its original tuple.

Move only stage1-reader-validate-table! into gravity.reader and the duplicate
stage1-reader-location/stage1-reader-span bodies into gravity.source-span.
Keep all three original Bootstrap public names/arities as facades. Keep table
source acquisition, source admission, table extraction and table orchestration
in Bootstrap. Do not change either manifest or source pins.

## Public API and dependency direction

gravity.reader/validate-compatibility-table! accepts a late failure-owner Var,
source path and native seed table, returning the original complete marker or
owner refusal. It owns only the existing historical table-shape checks and
STAGE1READER006 branch/argument selection. The supplied failure owner retains
diagnostic construction/publication and returning-failure behavior.

gravity.source-span/indexed-source-location accepts a late byte-count-owner
Var, source text, line starts and UTF-16 index. indexed-source-span accepts a
late location-owner Var, source path/text, line starts and start/end indices.
They own the original indexed coordinate/span arithmetic and construct no
diagnostics. Bootstrap supplies its existing utf8-byte-count and
stage1-reader-location Vars. Each owner is dereferenced at its original call
site in operator position before argument evaluation; later calls remain late.

Source-span's six existing APIs retain their line/column conventions, byte
counting, behavior and signatures. Indexed reader coordinates are a separate
explicit API: do not reinterpret stage0 columns as Unicode-scalar columns or
replace old source-location/source-span behavior. Reader and source-span keep
their existing dependency directions; neither discovers/imports Bootstrap.

## Inputs, outputs, provenance and diagnostics

Inputs are supplied source text, paths, table data and coordinates, not file
authority or authentic source evidence. Outputs are native complete/refusal
records and the original source location/span maps, preserving line, scalar
column label, UTF-16 char index, UTF-8 byte offsets, source path and start/end
references. Existing consumer token/form/document identities do not change.
No identity preimage, cache, new envelope or invalidated semantic fact is added.

Keep field/diagnostic validation order, all original refusal arguments and
exception identity, index clamping and original host boundary failures. The
span primitive must resolve its supplied location owner separately for start
and end; byte counting must remain through the supplied original owner.
Source decoding, scalar validity, project policy, diagnostic identity and
publication remain with their existing owners.

## Acceptance and tests

Capture original/candidate full location/span and table outcomes on retained
actual paired .gravity/.qst paths. Include ASCII, Unicode, CR/LF/CRLF, indices
inside/beyond text, original invalid-index host failures, real accepted table,
bad engine, each missing table field and each missing diagnostic field.
Compare complete outputs/refusals and raw source provenance deterministically.

Prove the three moved bodies modulo explicit ports, original facades, unchanged
table acquisition/orchestration, all fifteen Reader and six source-span APIs,
both manifests and source pins. Test late/per-call owners, operator-before-
argument ordering, returning failure owners and exact exception identity.
Run focused source-span/Reader tests, downstream Reader regression, language/
source ownership gates, contextual lint, formatting/validators, independent
semantic/adversarial review, own exact wired CI and immediate integration
preflight before sole-coordinator integration. Maintain an early draft PR.

## Actual ownership and CI boundary

An actual seven-path lease owns Reader, source-span, component00363, existing
source-span/Reader-artifact tests, the namespace map and this evidence record.
tools/ci/run.clj is deferred to the agreed next writer, the PR915 composition
owner, after this thread's preceding normal release. Do not reclaim its window,
adopt a foreign token or edit the CI file before a fresh actual claim/handoff.
The full slice requires adding gravity.source-span-test to the existing Reader
CI command: that Bootstrap-dependent namespace is intentionally excluded from
the bootstrap-free leaf runner. Existing Reader artifact tests own table checks;
do not substitute another test route to evade the shared-file handoff.

## Unsupported boundaries

This initial scope record grants no implementation or executed-test credit.
The helpers preserve historical native seed rules, not complete C2 table/schema
validation or Unicode/coordinate admission. They do not authenticate paths,
source/table/coordinates/callbacks, open files, define canonical identities,
normalize host errors, add source/work quotas, replace canonical SH03, or grant
complete Reader, migration, self-hosting, release or seed retirement. Changing
implementation source invalidates affected authority evidence conservatively;
ordinary semantic testing remains separate from renewing that evidence.
