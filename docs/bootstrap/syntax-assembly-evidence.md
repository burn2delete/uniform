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
and equal serialization/deserialization payloads. Full assembly and C3 product
comparisons and adversarial regressions are in progress on the draft PR.

This is a Clojure seed extraction with explicit residual boundaries. Other C3
helpers, source descriptor/metadata adaptation, native index implementation and
earlier bootstrap definitions remain open. It adds no language feature,
extension authority, resource accounting, migration, release, self-hosting or
seed-retirement authority.
