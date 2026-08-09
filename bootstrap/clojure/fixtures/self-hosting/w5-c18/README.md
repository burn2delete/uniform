# W5 C18 executable pass verification

This bounded `:meta` leaf exercises the C18 pass-verification contract over
four risk classes: low, medium, high, and critical. The accepted request
contains the required evidence kinds, accepted translation validations, fresh
proof and certificate records, a regression counterexample, a sandboxed plugin,
and native backend conformance data. The verifier computes a path-neutral
identity input and keeps actual checkout paths in provenance only.

The pass artifact chain is exact. Translation validations and proofs bind to
their owning pass version, target, input, output, proof, certificate IDs, and
explicit content labels. Those labels remain marked `:unverified` and
`:cryptographic-hash-verified? false`; this leaf does not claim that it hashed
artifact bytes. Exact ID-keyed semantic-owner tables bind every evidence,
validation, proof, and certificate ID to its pass, kind, checker, artifact
relation, and reference role; coherent cross-pass relabeling is rejected.
Every governed validation requires its exact ordered, nonempty proof and
certificate reference vectors. Evidence has an exact eight-entry inventory and
binds each input/output pair, artifact owner, and checker to the owning pass
plus its validation or proof record. Backend conformance binds the lowering pass,
profile, candidate target, and differential result, and admits exactly one
emitted artifact whose ID is the lowering output. Duplicate or orphan evidence
and added backend artifacts are rejected before they can enter the trust
report, gate, or semantic identity.

The accepted request constructor requires the actual source path; the
co-canonical `.gravity` and `.qst` peers therefore retain independent suffix
provenance while sharing semantic identity. The accepted record is intentionally not a release acceptance. Its gate is
`:blocked` because the Clojure seed boundary, JVM execution boundary, and
independent review remain explicit residual gaps. Producer booleans are ignored
by recomputation; `:clojure-seed-boundary?` is true, `:self-hosted?` is false,
and `:release?` is false. Result rechecking freezes the complete accepted and
rejected result keysets and compares the entire supplied accepted result with a
fresh recomputation. Missing, extra, artifact, status, diagnostic, completion,
authority, and residual-gap substitutions fail closed.

The candidate target is exactly `:llvm-x86_64-linux` on Linux/x86_64 with
LLVM/ELF/`:sysv-amd64` bindings. Darwin and Windows are ordered unsupported
targets with no Clojure invocation, JVM linking, fallback, or cross-target
inference; stage2 `:jvm` is only the seed-plan boundary.

The rejected co-canonical pair mutates one independent C18 family at a time:
`C18-RISK`, `C18-EVIDENCE`, `C18-VALIDATION`, `C18-PROOF`,
`C18-TRUST-REPORT`, `C18-RELEASE-GATE`, `C18-COUNTEREXAMPLE`, `C18-PLUGIN`,
and `C18-BACKEND`. `.gravity` and `.qst` files are byte-identical.

This leaf is an interface and executable verifier record, not a global
completion, public-release, proof-authority, or seed-retirement claim. Stage2
compilation and later JVM execution must be reviewed by the owning integration
work before any authority can be considered.
