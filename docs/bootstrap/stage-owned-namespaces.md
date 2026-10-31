# Stage-owned bootstrap namespaces

## Purpose and authority

Replace bootstrap-wide implementation bodies incrementally with namespaces
that own a compiler stage or ecosystem boundary. Gravity/Uniform remains the
destination language; Clojure extractions are temporary seed organization.
This map assigns responsibilities and proposed interfaces, not new language
semantics or evidence of full implementation.

D0, D1, D2, D3, D6, D8 and D9 govern every row. Contract identifiers below
resolve through [the document sequence](../document-sequence.md). C1 defines
stage contracts; C15 defines structured diagnostics; TEST2 defines compiler
tests. The existing Gravity owners in `bootstrap/gravity/src/gravity/` retain
semantic authority. Namespace extraction alone does not renew BOOT3/BOOT7
migration evidence or establish release, self-hosting or seed retirement.

## Dependency direction

Compiler dependencies follow artifact flow:

```text
reader -> syntax -> macro -> resolution -> checked-core
  -> types -> effects -> ownership -> safety -> mir
  -> optimization -> lowering -> backend.*
```

Arrows mean artifact consumption, not a requirement to import the preceding
namespace. Passes consume explicit records; producers do not import consumers.
Profile and capability validation remain compile-time gates before MIR and
target emission. Checked-core owns core construction; its name alone does not
certify completion of the later checks. Domain lowering preserves typed-core
or MIR anchors and exposes C1 verification points even when passes are fused.

`diagnostics` is a shared carrier/schema service. Stage rule decisions stay
with the producing stage. `runtime` consumes executable artifacts and declared
services; it cannot repair compiler legality. `standard-library` supplies
profile/effect/capability-declared source and APIs. `package` supplies resolved
dependency and authority records; `build` orchestrates explicit pass inputs.
`tooling` and `cli` consume these APIs and structured diagnostic views.
`bootstrap` wires temporary seed adapters and equivalence runs; `release`
consumes build, provenance and bootstrap evidence. No extracted stage imports
`bootstrap`, `cli`, or `release`. No backend imports a source reader to bypass
checked IR. Runtime and backend implementations share data contracts, not
mutual initialization dependencies.

During extraction, compatibility wrappers may pass explicit operations into
a stage. The stage must not discover bootstrap vars with `resolve`, invoke the
orchestrator, or treat a supplied callback as proof authority. Keep historical
interposition at the wrapper when existing tests rely on it. Remove a wrapper
only after its callers and evidence move to the stage API.

## Boundary and interface assignments

All APIs below are intended stage interfaces unless explicitly listed as
implemented in the slice record. They do not add callable stubs. `verify`
means verification under the owning contract, not acceptance of caller claims.
Every row owns its rule decisions and preserves upstream diagnostic identity.
Test namespace names are assigned destinations; existing tests remain valid
owners until moved with their implementation.

| Namespace | Governing contracts; intended boundary | Intended public API and artifact input -> output | Planned diagnostic ownership | Assigned tests: positive / negative |
| --- | --- | --- | --- | --- |
| `gravity.reader` | L1, C2; decoding, tokens, forms and source records | `read-source-artifact`, `verify`; bytes + encoding + explicit project/options -> source unit, tokens, forms, reader hashes | Planned: L1/C2 lexical, encoding, delimiter, extension and hash rules. Current assembly propagates owner decisions and owns no diagnostic rules. | `gravity.reader.artifact-test`: actual syntax artifact / malformed delimiter, namespace shape, identity drift |
| `gravity.syntax` | C3; immutable syntax, spans, origins, hygiene | `build`, `verify`, `reader-products-authentic?` plus [pinned plan runtime APIs](syntax-runtime-evidence.md); source/reader products + explicit seed operations -> C3 syntax artifact, plan binding and verification. Remaining adapters are open. | C3 missing spans, identity, hygiene, origin joins, pinned plan and digest refusal, runtime containment; catalog remains with the existing C3 diagnostics helper | `gravity.syntax.pipeline-test`: genuine paired products and plan/template execution / malformed source, forged syntax, source revision, serialization, plan drift, digest tampering and host failure |
| `gravity.macro` | L4, L12, L16, SAFE12, C4; compile-time expansion | [Pinned execution](macro-runtime-evidence.md) and [artifact/verification APIs](macro-artifacts-evidence.md), including `expand`, `assemble` and `verify`; authenticated syntax + macro/build context -> expanded syntax, trace, origins, identity and verification records | C4 plan/return/trace, artifact/parent/provenance rules and host containment; Gravity and existing C4 helpers retain semantic expansion/capture/build-authority rules | `gravity.macro.pipeline-test`: paired expansion / illegal return, missing grant, parent/output/provenance forgery and stale receipt refusal |
| `gravity.resolution` | L2, L3, L7, C5; bindings, imports, namespace graph | [Source and plan runtime APIs](resolution-runtime-evidence.md), [declaration/core-record APIs](resolution-records-evidence.md), [dependency/import APIs](resolution-imports-evidence.md) and [lexical collection APIs](resolution-analysis-evidence.md); source/toolchain records + explicit operations -> actual Gravity plan/result; expanded syntax + module/catalog/dependency inputs -> declaration/import/scope/reference records and dependency edges. `resolve-names` and `verify` artifact interfaces remain proposed. | Current stage: record normalization, C5 source/plan and host containment. Binding and import rules remain with the Gravity resolver and existing catalog. | `gravity.resolution.pipeline-test`: actual nested source bindings / unresolved or out-of-scope reference, declaration/import provenance, lexical visibility, pattern paths, catalog order, source/plan drift and stale receipt refusal |
| `gravity.checked-core` | L2, C6; core AST construction and source mapping | `lower`, `verify`; resolved syntax -> core AST, source map, lowering report | C6 malformed core forms, arities, missing resolution and mapping | `gravity.checked-core-test`: executable core / unsupported or malformed form |
| `gravity.types` | L5, L17, C7; inference and type legality | `check`, `verify`; resolved core + type environment -> typed core, constraints, substitutions | C7 type mismatch, occurs check, invalid instantiation | `gravity.types-test`: inferred call / incompatible argument or forged type fact |
| `gravity.effects` | L6, L15, C8; body/latent effects and capability requirements | `check`, `verify`; typed core + declarations + grants -> effected core, effect/capability facts | C8 undeclared effects, missing capabilities, forbidden profile effects | `gravity.effects-test`: declared effect / missing capability or understated body effect |
| `gravity.ownership` | L10, SAFE2-5, C9; ownership, lifetime and regions | `check`, `verify`; effected core + lifecycle facts -> ownership facts, escape/cleanup report | C9 moves, borrow conflicts, initialization, escapes and linear resources | `gravity.ownership-test`: valid borrow lifecycle / use after move or outliving escape |
| `gravity.safety` | D8, SAFE1-16, C10; aggregate safety and compile-time legality gates | `analyze`, `verify`; typed/effected/profile/capability/ownership facts -> checked core, four-outcome safety report, audits | C10 missing facts, unclassified danger, invalid unsafe boundary | `gravity.safety-test`: proven/checked operation / missing evidence or illegal profile |
| `gravity.mir` | C11; target-independent control/data flow | `build`, `verify`; safety-checked core -> typed/effected MIR, source/proof mappings | C11 malformed CFG, SSA, types, effects or lost facts | `gravity.mir-test`: executable MIR / malformed edge or unchecked core |
| `gravity.optimization` | D6, PERF1-6, C13; evidence-preserving MIR transformations | `optimize`, `verify`; verified MIR + pass config + proofs -> optimized MIR, invalidation/check manifest | C13 unsound rewrite, dropped effects or unjustified check removal | `gravity.optimization-test`: semantics-preserving rewrite / proofless check elision |
| `gravity.lowering` | C12, C14, B1; domain/target preparation | `lower`, `verify`; verified MIR/domain IR + target/runtime contract -> verified target-ready IR, ABI/layout manifest | C12/C14 missing domain anchor, target feature or unsupported operation | `gravity.lowering-test`: anchored lowering / unanchored domain IR or unchecked input |
| `gravity.diagnostics` | C15; shared diagnostic carrier, schema and projections | `diagnostic`, `fail!`; stage rule + location/facts -> structured diagnostics and rendering inputs | Carrier/schema validity; stage rules remain stage-owned | `gravity.diagnostics-test`: carrier and wrapper parity / thrown carrier and override precedence |
| `gravity.backend.c` | B1, B2, B13, B14; C emission | `emit`, `verify`; verified C-ready IR + ABI/runtime selection -> C source, source map, artifact manifest | B2 unsupported operation, ABI/layout, emission facts | `gravity.backend.c-test`: compile emitted C / unchecked IR or incompatible layout |
| `gravity.backend.llvm` | B1, B3, B13, B14; LLVM emission | `emit`, `verify`; verified LLVM-ready IR + triple/features -> LLVM IR/object, debug/proof map | B3 unsupported op, target assumptions and LLVM verification | `gravity.backend.llvm-test`: verified emitted module / invalid target feature or IR |
| `gravity.backend.wasm` | B1, B4, B13, B14; Wasm emission | `emit`, `verify`; verified Wasm-ready IR + memory/import contract -> Wasm module, import/provenance manifest | B4 memory/import/profile incompatibility | `gravity.backend.wasm-test`: validate/run module / undeclared import |
| `gravity.backend.jvm` | B1, B5, B13, B14; JVM emission | `emit`, `verify`; verified JVM-ready IR + host interop contract -> classes/JAR, metadata/source maps | B5 class, signature, host boundary and unsupported op | `gravity.backend.jvm-test`: execute class / invalid interop or unchecked input |
| `gravity.backend.js` | B1, B6, B13, B14; JS/TS emission | `emit`, `verify`; verified JS-ready IR + numeric/host contract -> JS, declarations, source map | B6 numeric, async, host and semantic representability gaps | `gravity.backend.js-test`: execute emitted module / unrepresentable semantics |
| `gravity.backend.mlir` | B1, B7, B13, B14; MLIR dialect lowering | `emit`, `verify`; anchored verified IR + dialect/pass contracts -> MLIR module, lowering manifest | B7 dialect legality, missing anchor and pass evidence | `gravity.backend.mlir-test`: verify emitted module / illegal dialect operation |
| `gravity.backend.gpu` | B1, B8, P11, B13, B14; GPU kernels | `emit`, `verify`; verified GPU IR + memory/barrier/features -> kernel artifact, launch manifest | B8 memory space, barrier, launch and target legality | `gravity.backend.gpu-test`: validated kernel / illegal memory space or barrier |
| `gravity.backend.hdl` | B1, B9, P8, B13, B14; clocked hardware | `emit`, `verify`; verified hardware IR + clock/reset/timing facts -> HDL, constraints, trace map | B9 clock/reset, synthesis and hardware profile violations | `gravity.backend.hdl-test`: simulate emitted design / invalid clock crossing or allocation |
| `gravity.backend.workflow` | B1, B10, B13, B14; durable graph emission | `emit`, `verify`; verified workflow IR + schemas/replay policy -> graph, event schemas, replay manifest | B10 nondeterminism, missing event or authority | `gravity.backend.workflow-test`: graph replay / unrecorded nondeterminism |
| `gravity.backend.query` | B1, B11, B13, B14; relational lowering | `emit`, `verify`; verified query IR + row/transaction/capability facts -> parameterized query plan, schema/map | B11 row/schema, transaction and database authority | `gravity.backend.query-test`: parameterized plan / schema mismatch or missing database grant |
| `gravity.backend.mobile` | B1, B12, B13, B14; platform packaging/adapters | `emit`, `verify`; verified mobile IR + platform lifecycle/permissions -> mobile artifacts and permission manifest | B12 platform/lifecycle/permission mismatch | `gravity.backend.mobile-test`: platform package / undeclared permission or unsupported lifecycle |
| `gravity.runtime` | R1-12; declared execution services | `execute`, `runtime-manifest`; executable artifact + services/grants -> result, events, runtime manifest | Runtime panic, resource, host normalization and authority rules | `gravity.runtime-test`: execute declared program / absent service or forbidden authority |
| `gravity.standard-library` | L13, STD1-20; library API contracts and implementation | `library-manifest`, `validate-api`; library source + profile/API policy -> compiled library, effect/capability/stability manifest | Library contract, profile availability, unsafe wrapper and stability rules | `gravity.standard-library-test`: documented example / unavailable API or unsafe boundary leak |
| `gravity.package` | PKG1, PKG4-6, PKG8-10; resolution and package authority | `resolve-dependencies`, `verify`; project + registry metadata + grants -> lockfile, graph, capability/provenance records | PKG dependency, signature, authority widening and audit violations | `gravity.package-test`: reproducible lock / transitive authority escalation |
| `gravity.build` | PKG2-3, PKG7, PKG11, C16; build DAG and invalidation | `build`, `plan`; project + lock + source/compiler/pass identities -> artifacts, pipeline and reproducibility manifest | Build effects, cache invalidation and target matrix rules | `gravity.build-test`: deterministic rebuild / stale dependency or undeclared build effect |
| `gravity.tooling` | T2-12, C15; editor, inspector and diagnostic views | `inspect`, `format`, `lint`; syntax/IR/diagnostics + tool config -> views, edits, reports | Tool protocol/edit validity; compiler rule ids preserved | `gravity.tooling-test`: source-linked inspection / invalid edit or identity-changing format |
| `gravity.cli` | T1; command dispatch and presentation | `-main`; args + explicit IO/build context -> API invocation, output, exit status | Argument/command errors; stage diagnostics rendered unchanged | `gravity.cli-test`: actual command artifact / invalid args and compiler rejection exit |
| `gravity.bootstrap` | BOOT1-8; seed composition and compatibility | Existing stage adapters and `-main`; source + seed/stage records -> stage artifacts, comparison/provenance records | Bootstrap identity/equivalence and adapter association failures | `gravity.bootstrap-test` and `gravity.self-hosting.*`: stage comparison / substituted source or stale binding |
| `gravity.release` | PKG12, BOOT6-8, phase 18; release evidence and distribution | `assemble`, `verify-release`; built artifacts + SBOM/signatures/stage evidence -> distribution, release manifest, verification report | Release provenance, signature, reproducibility and seed-retirement refusals | `gravity.release-test`: verified bootstrap-hosted bundle / missing signature or false seedless claim |

## Incremental extraction and acceptance

Extract executable behavior, not empty namespaces or a second compiler.
Preserve existing API arities, diagnostics, actual supplied paths, extension
provenance and deterministic artifact identity. Keep `.gravity` and `.qst`
first-class. Record exactly which execution paths and source spellings ran.

Each slice must identify owning contract documents and exact paths; emit or
compile a real accepted artifact; reject a real invalid input; exercise affected
composition, interposition, identity and adversarial cases; state residual host
boundaries and unrun claims. Use focused tests and the language-boundary gate.
Do not refresh frozen migration fingerprints merely to make development pass.

Open the slice as an early draft PR, maintain its evidence as implementation
progresses, freeze a clean named-branch commit, and obtain independent
acceptance of the exact source and any later lifecycle metadata. Apply the
existing [workstream lifecycle](../workstream-governance.md), current-base
integration preflight and required GitHub checks before the coordinator merges
to main. A self-audit, green tests or this map cannot confer acceptance.

## Reader artifact assembly slice

The first extraction covered here moves the body of the existing
`gravity.bootstrap/read-source-artifact` into `gravity.reader`. Its public API
is `read-source-artifact [source-path source-text project-context operations]`.
The compatibility API retains its existing two- and three-argument forms.
Explicit operations retain the original reader, namespace-shape validation,
syntax construction, source identity, extension and byte-count behavior.

This slice owns assembly of the existing `:gravity/stage0-reader-artifact`.
It preserves records, source maps, namespace clause syntax, metadata, source
identity and pass declarations. Parsing and diagnostic decisions remain with
the supplied existing reader operations. It does not implement a new decoder,
canonical C2 authentication, C3 validation, macro expansion or a self-hosted
reader. The direct API is seed composition, not a public authority boundary:
caller-supplied operations are trusted implementation dependencies, not proof
certificates. Existing authenticated consumers retain their own verification.

The [syntax pipeline extraction](syntax-pipeline-evidence.md) additionally moves
C3 construction and reader association verification into `gravity.syntax`.
The [syntax runtime extraction](syntax-runtime-evidence.md) moves pinned plan
authentication, execution, result admission and digest resolution into the
same namespace. SH04 reader/envelope and rich-object host adaptation and the
other syntax helper boundaries remain open. The
[reader view extraction](syntax-reader-views-evidence.md) additionally moves
registered-literal projection and authenticated reader views into the stage.
The [assembly extraction](syntax-assembly-evidence.md) moves semantic bindings,
stream replay/serialization, generated syntax and envelope assembly while
retaining explicit seed descriptor and native-index operations.
The [descriptor/index extraction](syntax-descriptors-evidence.md) moves those
algorithms into the stage while retaining explicit seed lifetime and root
configuration. Earlier C3 helpers and bootstrap definitions remain open.
`gravity.diagnostics` and the existing narrow helper namespaces remain partial
extractions. Other destination interfaces in the table remain proposed;
existing Gravity modules, hosted facades and tests retain their current status.
Follow-on slices should move the owning behavior and its real tests before
adopting the proposed destination API. See the adjacent slice evidence record
for executed checks and exact unsupported claims.
