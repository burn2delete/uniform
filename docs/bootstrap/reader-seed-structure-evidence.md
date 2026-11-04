# Reader indexed source-position ownership

## Governing contracts and scope

D1/D2/D3/D8/D9, L1, C1/C2/C3/C15, BOOT8 and TEST2 govern this bounded
ownership move. Parentb3f55a5a700a972dc7e14e29943e78822251c8b3, tree
b24d91d303c610e2bb3c66b5b60f85ad1e9dcbc6, preserves unmerged draft PR1181.
No inherited source/test/CI result grants this slice migration or integration
authority. Every named prior acceptance remains bound to its original tuple.

Move only the duplicate stage1-reader-location/stage1-reader-span bodies into
gravity.source-span. Keep both Bootstrap names/arities as facades. Bootstrap
retains table source loading, admission, extraction, validation, failure
construction and orchestration. Both manifests and source pins are unchanged.

Independent ownership review places the source-table boundary with Bootstrap
and identifies indexed coordinates as the next Reader extraction. The initial
unaccepted prototype also moved the seed-table validator to Reader; that patch
is retained and its Reader/validator changes were removed. This corrects the
owning stage for a seed-source schema. All fifteen Reader APIs remain unchanged.

## Public API and dependency direction

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

Inputs are supplied source text, paths, coordinates and owners, not file
authority or authentic source evidence. Outputs are original source location/
span maps, preserving line, scalar
column label, UTF-16 char index, UTF-8 byte offsets, source path and start/end
references. Existing consumer token/form/document identities do not change.
No identity preimage, cache, new envelope or invalidated semantic fact is added.

Keep index clamping, exact owner exception identity and original host boundary failures. The
span primitive must resolve its supplied location owner separately for start
and end; byte counting must remain through the supplied original owner.
Source decoding, scalar validity, project policy, diagnostic identity and
publication remain with their existing owners.

## Acceptance and tests

Capture original/candidate full location/span outcomes on retained
actual paired .gravity/.qst paths. Include ASCII, Unicode, CR/LF/CRLF, indices
inside/beyond text and original invalid-index host failures. Native unit
controls also cover surrogate/unpaired-surrogate strings without asserting
valid encoded source admission. Retain accepted table, bad engine and each
missing table/diagnostic field as unchanged regression controls.
Compare complete outputs/refusals and raw source provenance deterministically.

Prove the two moved bodies modulo explicit ports, original facades, unchanged
table acquisition/validation/orchestration, all fifteen Reader and six prior
source-span APIs, manifests and pins. Test late/per-call owners, operator-before-
argument ordering and exact reference/exception identity. Compare existing
parser/token/form/literal records and malformed diagnostic IDs/order.
Run focused source-span/Reader tests, downstream Reader regression, language/
source ownership gates, contextual lint, formatting/validators, independent
semantic/adversarial review, own exact wired CI and immediate integration
preflight before sole-coordinator integration. Maintain an early draft PR.

## Actual ownership and CI boundary

An actual seven-path lease owns Reader, source-span, component00363, existing
source-span/Reader-artifact tests, the namespace map and this evidence record.
Reader and its artifact test retain their parent bytes after the ownership
correction; five source paths survive under that initial seven-path lease.
tools/ci/run.clj was initially deferred to the agreed next writer, the PR915
composition owner, following this thread's preceding normal release. That
handoff has now resolved through its reported normal release, actual absence
and a fresh own claim as recorded below.
The Bootstrap-dependent gravity.source-span-test namespace is intentionally
excluded from the bootstrap-free leaf runner. After the original PR915 owner
reported its normal release, actual row absence was observed and a fresh own
one-path CI lease was obtained. The full slice now adds this namespace once to
the existing Reader CI command. No foreign token/lease or substitute route was
used; the earlier deferred handoff remains history.

## Unsupported boundaries

Original scope3176071f7b3b010be0aaaf056518012b33e16168 captures 84 position
records and 34 unchanged table controls across 42 actual paths: seventy outputs,
32 original table refusals and sixteen native index failures. Host failures are
not semantic refusals. Full raw/parsed baseline SHA-256 is
ce3ddd4d03d2e6aee60e91ba88337f64ab665f176929a1afcedf1fdc6dbf1e7b.
This scope/observation record grants no accepted implementation or CI credit.

The helpers preserve historical native coordinate rules, not complete C2
validation or Unicode/index admission. They do not authenticate paths,
source/coordinates/callbacks, open files, define canonical identities,
normalize host errors, add source/work quotas, replace canonical SH03, or grant
complete Reader, migration, self-hosting, release or seed retirement. Changing
implementation source invalidates affected authority evidence conservatively;
ordinary semantic testing remains separate from renewing that evidence.

## Focused implementation evidence

The two coordinate bodies move behind explicit per-call operator-dereferenced
owner Vars. Original/candidate native outputs and complete downstream Reader
artifacts remain to be compared at a clean candidate. Focused source-span and
Reader artifact tests pass fifty-two tests and 823 assertions. They cover
indexed scalar-column/UTF-16/UTF-8 differences, clamping and invalid indices,
unpaired-surrogate native strings, start/end/path reference identity, per-call
location/byte-owner rebinding and exact owner exception identity. Reader's
fifteen public functions and its artifact test retain their parent bytes.

New source-span tests lint with zero warnings/errors. The source-span module
retains its existing unused private namespace-contract warning; no new error
or warning policy is suppressed. Table validation and its thirty-two original
refusals are regression controls, not credit for a new Reader-owned validator.

## Committed conservation and regression

Source96919a41cba9a971a569c0a1efdcf813ccf2d281, tree
004dc47eed72283f987a61a83711b56b48fdcd74, is a clean six-path commit.
The exact wired Reader command passes 110 tests and 4,591 assertions at its
original one GiB heap and 120-second process watchdog. Language/source
ownership gates pass forty-eight tests and 1,198 assertions with zero skipped
namespaces. Cold loading source-span and Reader exposes eight and fifteen
public functions respectively without loading Bootstrap. Docs, roadmap,
governance and CI formatting pass.

All 118 candidate coordinate/table-control records are byte-identical and
parsed-equal to the retained original baseline at ce3ddd4d03d2e6aee60e91ba88337f64ab665f176929a1afcedf1fdc6dbf1e7b.
Complete downstream captures preserve literal outcomes and four token streams
at a65f15a6ac535fa0aaf0963480a4d189d9f555b3225e4dac8626e6d2727ba673,
52 metadata-aware form records and twenty join refusals at
ad4c5c42a3b93f2eac63a9a81cc786237885974966822a3298325841b7a9ab14,
and forty historical-parser records at
05facbbf3623085382609ddc1f6ad3d44850027e7536d461e23bee34be35c7a4.
Malformed-source refusals retain their exact diagnostic IDs, order and spans;
the previously declared form-builder composite-set repairs remain unchanged.

Static comparison proves both moved coordinate bodies modulo explicit owner
ports, all prior source-span implementation code, all fifteen Reader API
bodies, the full Reader/artifact-test bytes, exact Bootstrap table validation/
loading/orchestration and facades, both manifests and the single CI insertion.
Contextual lint covers forty-three present Clojure/EDN paths against mainf662:
3,459 unchanged baseline warnings and zero blocking warnings/errors. The extra
unchanged warning versus earlier slices is source-span's existing private
contract warning, not a new error or a suppressed policy.

Both independent reviewers accept the exact source with no actionable defect.
Their scope remains code-only. Documentation-successor bookend review, own
exact source/base CI and immediate integration preflight remain separate gates.
