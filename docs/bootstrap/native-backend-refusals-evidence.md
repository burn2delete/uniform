# Native backend refusal ownership

## Contract boundary

D1, D2, D3, D8, D9, C1, C15, B1, B9, B10, B11, B12, B13, B14,
BOOT8 and TEST2 govern this extraction. Four native failure-payload builders
and four explicit source-override selectors move from Bootstrap into HDL,
Workflow, Query and Mobile. The existing predicate, catalog and fixture
implementations remain unchanged. No diagnostic identifier is renamed.

Each stage exposes `fail! [id source-path subject extra operations]` and
`validate-source-overrides! [source-path overrides operations]`. These are
trusted seed composition APIs, not public input admission or proof APIs.
Namespace metadata declares their exact operation inputs, native callback
output, governing contracts, diagnostic ownership and core/string-only
dependency direction. Bootstrap keeps its existing function names and arities.

Stages own the existing native failure payload defaults, final extra-field
merge, and explicit override refusal selection. Existing consumers retain
artifact-validation decisions, source admission, generic diagnostic throw
transport and publication. Subject source spans remain borrowed when truthy;
missing or false spans use the existing source-span operation. Extra fields
retain their original final precedence. The override table is read only when
the fail selector is truthy; unknown, false and absent selectors retain nil.

The failure wrapper captures the generic throw operation before payload
evaluation, preserving the original function-root lookup order. Source-span
and native context callbacks retain late Var lookup. Override wrappers retain
late table and failure-function lookup through zero-argument suppliers. The
failure-function supplier runs before selector-name evaluation and diagnostic
argument construction. Callback return values and exception
identity propagate unchanged. None of these operations confers authority.

## Evidence and acceptance

A clean baseline at bccc70f5 records 360 native payload refusals across known
and unknown diagnostic IDs, nil/empty/provided/false subject fields and final
extra-field overrides. It also records 41 actual explicit override refusals,
20 valid override cases and four actual empty-artifact refusals. A clean
committed candidate capture must match the complete original raw records,
parsed records, source identity and all recorded hashes. Historical captures
remain unchanged.

A frozen-parent reconstruction checks all nine complete production files.
It changes only the operation references needed by the extracted bodies,
compatibility wrappers and namespace contracts. Focused tests retain earlier
real fixture emission, malformed-text refusal, catalog identity and artifact
refusal checks. New tests cover operation order, final extra precedence,
borrowed spans and IDs, callback result/exception identity, captured throw-root
order, lazy override-table reads and late wrapper interposition.

The initial focused run had three callback-order expectation failures. The
replacement expectations come from a separate clean c29ecab29 observation,
after checking all four original constructor files byte-for-byte against
bccc70f5. The original run is retained as a nonpass. Adversarial inspection
also required a failure-function supplier to preserve lookup before a Named
selector's name operation; a regression case preserves that original behavior.
One orchestration parse failure and two patch-context failures occurred before
source writes and confer no test or compiler evidence.
The first refined conservation check also found patch-added final newlines in
compatibility files. Their original EOF whitespace was restored; the complete
nine-file reconstruction then passed. The earlier failure remains retained.

Language-boundary, ownership, document, roadmap and governance checks, cold
namespace loading, independent acceptance, own CI/artifact audit and exact
clean current-main integration preflight remain required. The source starts
from pending PR878; predecessor reviews and CI do not transfer here. Earlier
Mobile, catalog and lint timeouts remain nonpasses. No timeout fix is claimed.

## Relocated main composition

The successor normally merges actual main
2a7d5eefd8483c0144dee62df7ce02cecf984035, tree
58a17e4d017e173e01e97e46aa07ce568216b102, after PR863 integration.
The merge has no conflicts. Native backend production files, assertions and
all four live language/compiler source pointers remain byte-identical to the
reviewed 67c8a743 candidate. Incoming main files retain their actual contents.
Fresh clean captures, composition review and own CI are required; no green
provider tree or predecessor result is adopted.

A separate before-CLI control on unchanged 67c8a743 stopped at the existing
C3 exact-pinned-syntax-source check (81,241 expected bytes, 78,643 observed).
It produced no Core-emission evidence. That source-authenticity refusal is
retained; no CLI edit, source pin, hash constant or authority setting changes
to obtain a passing result. This native backend slice makes no public Core,
canonical C3 or broader compiler-conformance claim.

## Unsupported boundaries

The relocation composition normally merges actual main
92123759e99efc89bf7346a0ea8892f4f903d7af, tree
533ffab46c9bbe8460e5fc8301217a12d84369c7, after PR860, PR861,
PR862 and PR874 integration. The merge is conflict-free. Native backend code
and tests remain unchanged; the ownership map retains main's relocated
frontend/C6 paths together with the existing Mobile helper registrations.
This source needs its own fresh clean captures, review and CI; the earlier
850ac0ec green run with a newer provider base remains historical.

The subsequent normal composition incorporates actual main
7f3319be3a2bb9b6c5415d0d04b7a597015f01ac, tree
6c0ccf9433d9ac8b793b5d256b486a66101f5593. Its only incoming changes are
tools/ci/run.clj and tools/ci/run_test.clj, preserved byte-for-byte from main.
All 25 owned paths match the reviewed ccc48880 predecessor before this
evidence paragraph update; no native backend implementation changes are
introduced. Fresh source-specific captures, independent review, CI artifact
verification and immediate current-main preflight remain integration gates.

The next normal composition incorporates actual main
c28077686b5bd641c8cc0717bbd23641a701987a, tree
67eb8f7ab968c29fc3b58ce7a4ac980ac41e4d21. Its eleven incoming paths
authenticate selected Syntax owners and preserve the consumer controls. They
remain byte-identical to main, outside this slice's 25 owned paths. This
evidence update is the only authored change beyond the reviewed predecessor;
native backend code, tests, registrations and compatibility behavior remain
unchanged. Earlier source-specific CI and captures do not transfer.

These are native document-fixture refusals. They do not establish canonical
C15 findings, verified target IR, profile/effect/capability legality, backend
execution, input authentication, redaction/binding/publication or certificates.
Existing complete, policy and proof labels remain data. Generic throw and
context callbacks are trusted host operations, not sandboxed providers.

No held native broker/factory, private JVM authority route, default frozen
Reader, package/archive/release builder or Attempt30 route executes. No
release, self-hosting, authoritative migration or seed retirement is claimed.
