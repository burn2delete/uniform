# Checked-core request slice: Reader dependency composition

This composition combines the independently reviewed request/reference source
with the actual incoming Reader dependency. D1, D2, D3, D9, L1, L2, C1-C6,
C15, BOOT8 and TEST2 govern the existing boundary. It changes no language rule,
Core API or admission authority.

The retained source is 2f5145710bcb040025e505b34ae069c732f0c8c5. Its previously
reviewed main composition is 4faa8de843522ffd66b4e48aef4d280e4f7cd5a0, tree
71cd74bc15e786897089281d53e687e9b86488ba. Incoming main is
8f39736e06d20ad854fd4a58cde2a974add67d36, tree
d3caf23cbbcf48049ff644c02aa41a0c5aad96b8, whose parent is 3524ebe6. A normal
merge combines the 82-file Reader delta without conflicts. The Core stage,
stage test, Gravity Core source and SH-07 components 885-909 are byte-conserved
from the retained candidate. No shared CI path changes in this composition.

## Dependency boundary

The incoming Reader adds fixed pure-text policy construction/admission and
changes Reader source/verification inputs, C2 construction, C3 artifact views,
late Reader bindings and affected C5 identity expectations. Existing standard
constructor arities delegate with standard options. This composition exercises
only explicit current-development standard Reader/C5 inputs and existing Core
request APIs. It does not exercise pure-text/custom/effectful handlers, providers,
native launch or historical Reader/migration paths and grants them no authority.

The Core stage still exposes 38 public APIs and four private helpers. Bootstrap
retains C5 owner verification, Core admission, proof lifetime and publication.
The composed source must freshly verify its current upstream input and preserve
semantic identities separately from authenticated owner joins. It must not keep
old authenticated hashes after the Reader implementation changes.

## Actual products and identity delta

Actual paired current Reader/C5 products for source-occurrences.gravity and
source-occurrences.qst each emit a 66-form Core request. The source forms,
module, fragments and semantic lineage remain unchanged. New authenticated C5
owner identity is
`sha256:81f394e461a95b8c6cea45d27d6953526e293804a18cc46971161bec933dc342`,
replacing
`sha256:32b10c76d2d00207eb6eecaa908aa2b7623869c34098056a0c56d2053e9ac328`.
The projection binding changes from
`sha256:779017dd0c05bb4de564cc2a64de4b0fd73d062f222a1b61d022b54df55e2bb1`
to
`sha256:d589d6111f7c6489dac51308390b51078c7812e82a17030ab4430612d0902646`.
The new binding is recomputed from the complete existing projection preimage;
both actual digest references resolve to it.

Each complete raw request differs in exactly 288 leaves: 270 binding-table
upstream binding ids, nine resolution-table upstream binding ids, seven
definition-artifact ids, the authenticated C5 owner and the projection binding.
There is no blanket whole-request equality claim across different Reader inputs.
The existing semantic-binding and semantic-resolution helpers normalize only
their documented owner joins; semantic lineage excludes only the authenticated
C5 owner for this comparison. The complete remaining normalized request is
equal, with all source forms retained. Every new Core upstream binding/reference
joins the actual fresh C5 binding table, including its definition-owner facts.

Passing the retained old C5 parent to the actual current request gateway raises
C6-VERIFY with missing-fields [fresh-authenticated-sh06-resolution]. Missing
lexical context still raises C6-VERIFY / sh06-context, and an unresolved digest
index still raises C6-VERIFY. These are actual producer/gateway observations,
not caller-supplied proof or Core owner admission.

Fresh C5 parents round-tripped through the same EDN transport independently
reverify with status passed for both spellings. The retained old parents fail
twelve currentness/replay checks, including fresh upstream C4 verification and
the authenticated resolution request. Thus the old-parent observation is not
explained merely by using serialized input. No failed check is bypassed.

## Validation and remaining gates

The fresh Core runtime CI mode passes stage 19 tests / 1526 assertions,
current-binding/kernel 4 / 57 and projection kernels 3 / 286: 26 tests / 1869
assertions, zero failures/errors. The language boundary passes 6 / 11. Actual
paired producer and stale-parent observations exit 0, and complete normalized
request/actual owner-join verification passes for both spellings. The two
changed C5 source/identity tests pass 2 tests / 32 assertions, including actual
unresolved and escaped-reference rejections. Fresh/old serialized C5 report
controls also exit 0. Docs validation passes 240 docs / 19 phase indexes;
roadmap structural and workstream-governance validation pass without granting
completion authority. Contextual lint, independent composition acceptance,
applicable CI and clean final integration preflight remain required.

The original final CI run 41179745050 remains an actual NONPASS: Resolution
receipt status timeout, exit 124, 600-second process watchdog. Its tested
checkout was 375662580fd9120a5410853abdc961e3b2f70bf9 with tree 71cd74bc, equal
to the retained candidate tree. Receipt SHA256 is
9954cdc925105d8919966edfe26c1386d1768283509d276442c144419c352611.
The active selector, timeout cause and descendant cleanup remain unknown.
That failed head is not rerun, and no guard extension or bypass is introduced.
This changed-input composition does not clear or causally explain the failure.
Separate CI-owner work remains separate evidence, subject to its own review and
integration. No release, performance, migration, self-hosting, seed-retirement,
full Core or pure/custom Reader completion is claimed.
