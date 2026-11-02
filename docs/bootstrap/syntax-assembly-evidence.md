# Syntax assembly extraction

This increment moves the remaining SH-04 stream assembler, semantic reader
bindings, semantic source/span projection, expansion history, generated
products, rich-object projection and envelope descriptor construction into
`gravity.syntax`. Bootstrap retains compatibility arities and explicit seed
ports. Source descriptors, host metadata cleanup and native index machinery
remain seed operations; their remaining implementation is not claimed complete.

D1, D2, D3, D9, L1, C1, C2, C3, C15 and TEST2 govern this boundary. Public APIs,
argument/return shapes, operation keys, dependencies and artifact declarations
are recorded in the namespace contract. C3 owns stream replay, serialization
and syntax envelope assembly diagnostics. Upstream C2 authentication retains
reader-source and revision refusals. The stage requires only Clojure and the
existing pure reader-expansion/verifier helpers; it never loads bootstrap.

Inputs are freshly validated C2 products and their actual validation report,
the explicit reader invocation, pinned syntax execution/digest/envelope ports
and seed descriptor/index operations. Outputs are the unchanged resolved stream,
raw templates, generated syntax, graph/hygiene/metadata/fact records,
serialization/deserialization records, semantic reader binding and revision,
authenticated envelope and actual-reader provenance. A report or supplied
operation map is not caller proof authority. The public source constructor
still validates its reader product before entering this internal assembly API.

| API | Boundary |
| --- | --- |
| `logical-source-stem`, `semantic-source-id`, `semantic-span` | Source context and spans -> co-canonical semantic source identity and source span |
| `current-reader-product-binding`, `descriptor-reader-product-binding` | Actual reader records or descriptor -> existing product identity projection |
| `reader-semantic-binding` | Source, validated reader products and semantic source id -> binding and retained source revision |
| `rich-object` | Pure syntax and serialization id -> compatibility-rich syntax object |
| `reader-expansion-history`, `reader-expansions-current?` | Reader occurrences and authenticated syntax boundary -> exact expansion projection or association result |
| `generated-products!` | Existing base syntax, pinned binding and reader identity -> actual generated template and verified resolved syntax |
| `envelope-descriptor` | Source, pinned syntax binding and verified summary -> existing private envelope descriptor |
| `assemble-fresh-reader!` | Source, real C2 validation report, reader products and invocation -> complete syntax assembly |
| `assemble-reader!` | Source and reader products -> freshly validated legacy assembly path |

Every API takes explicit operations last. The legacy assembly entry retains
its frozen default envelope-admission refusal on current SH-02 source drift;
it does not silently select current-source admission.

The seed transaction adapter binds exactly the original source-product loop.
Canonical descriptor Vars retain the original thread/artifact-local native
index reuse and root-interposition guards. A supplied descriptor callback that
is not that canonical Var runs with the transaction disabled, so custom
composition cannot hide lookups behind the original cache qualification.

The supplied project accounting-removal instruction also removes the old
1 MiB upper cutoff on a retained reader revision's byte count. Nonnegative
integer shape, source/hash joins, reader verification and envelope authenticity
remain required. This does not renew a frozen reader pin or authenticate a
forged revision; it removes one consumer-side input quota.

Before extraction, both actual registered-literal fixtures emitted accepted
two-object syntax assemblies with resolved-stream identity
`sha256:620e7e36536af1bfd2eb5618ae08b15e2b8c0fa72acb204077acedb46d556aef`
and equal serialization/deserialization payloads. Complete assembly maps and
complete C3 artifacts compare equal across extraction for both extensions;
the C3 artifact identity remains
`sha256:0e62cacb7046ae54171f5a9188beb07794ae58053526a16c763b5cdaed1e92fc`.

Direct stage tests compare resolved streams, rich syntax, source identity,
envelopes and serialization records with the actual source-constructor output.
They execute real pinned rules and reject modified stream-template, resolved
stream verification, serialization and deserialization results with unchanged
C3-ID/C3-SERIALIZE diagnostics. A custom descriptor callback observes fresh
native lookups while canonical composition retains the original index reuse;
both emit equal complete assemblies and leave no transaction active afterward.
The revision-count test projects large nonnegative integer counts without a
quota, rejects negative/nil/noninteger shape, and independently refuses every
deliberately altered source revision. It does not claim authenticated large
reader-source admission from a structural projection test.

| Executed development check | Result |
| --- | --- |
| Complete paired syntax assembly and C3 product comparison | Equal across extraction |
| Syntax, C3 compatibility, fresh-C2 and both native index suites | 57 tests, 583 assertions; passed |
| C3 construction/verification/identity and reader-expansion helpers | 16 tests, 191 assertions; passed |
| Ownership, catalog and language-boundary suites | 50 tests, 1025 assertions; passed |
| CI harness | 11 tests, 101 assertions; passed |
| Documentation, roadmap and workstream validators | Passed; 240 documents, 19 indexes, 185 existing dispositions |
| Changed-file lint and whitespace checks | Passed; no new findings |
| Cold stage load | 38 public APIs; bootstrap not loaded |

Both native index namespaces now run in the existing CI syntax command. Its
heap and process watchdog remain host safeguards, not compiler accounting.
Existing source ownership/component inventories, fixed Gravity source/function
pins and migration/reporting fingerprints are unchanged. Independent acceptance
and final exact-head remote checks are recorded on the PR before integration.

This is a Clojure seed extraction with explicit residual boundaries. Other C3
helpers, source descriptor/metadata adaptation, native index implementation and
earlier bootstrap definitions remain open. It adds no language feature,
extension authority, resource accounting, migration, release, self-hosting or
seed-retirement authority.
