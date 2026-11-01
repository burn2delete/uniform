# Native Reader source-path diagnostic slice

## Contract boundary

This slice moves only the existing two-argument source-path refusal body from
Bootstrap component00778 into gravity.reader. L1 owns the co-canonical extension
rule; C2 owns byte/hash/path provenance, and C15 owns diagnostic transport,
identity, publication and rendering. D1/D2/D3/D8/D9, C1/C3, BOOT1/BOOT8 and TEST2
also govern the slice. No language rule changes.

The proposed public API is reject-source-path! with explicit late Vars for the
extension operation, co-canonical extension set, byte hash and failure operation,
followed by path and already captured bytes. It constructs the existing
L1-SOURCE-EXTENSION refusal. It does not check admission, open a source file,
authenticate supplied bytes or grant filesystem authority. It requires only
clojure.core and must not depend on Bootstrap, CLI or Release.

Bootstrap retains its one-argument byte capture and late call to its two-argument
compatibility Var. The two-argument wrapper delegates diagnostic construction.
Hashing precedes failure-operator lookup; that lookup precedes diagnostic-field
evaluation. Extension lookup and allowed-extension dereference retain their
original positions. Nil bytes retain the existing missing-file diagnostic.

## Evidence requirements

Before/after controls must retain complete native refusals, actual byte hashes,
spans, origin chains and unavailable profile/target values. Tests cover both
wrapper arities, direct/stage parity, file capture versus supplied snapshots,
late Var replacement, dependency exception identity and fail-operator lookup
order. Existing invalid files must reject before decoding. Complete real Core
products in both source spellings use only the authorized development-current
route and preserve current-manifest provenance and semantic identity.

The source intake candidate PR743 is the immutable parent of this isolated
successor. That parent must integrate before this slice can integrate. Scope
publication is not implementation or acceptance evidence. Exact source review,
proportionate regression checks, own CI and a clean current-main preflight are
required before integration.

## Unsupported boundaries

This remains a temporary native diagnostic construction helper. No canonical
C2 authentication, source admission, new syntax, extension policy, filesystem
grant, sandbox, symlink-race guarantee, quota, full Reader/C15 conformance,
migration, release, self-hosting or seed retirement is claimed. Historical
restricted Reader/H6/native and default pinned routes remain unused. The input
bytes and Var ports remain trusted host dependencies, not evidence of authority.
