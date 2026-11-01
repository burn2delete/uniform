# Native Reader source-path diagnostic slice

## Contract boundary

This slice moves only the existing two-argument source-path refusal body from
Bootstrap component00778 into gravity.reader. L1 owns the co-canonical extension
rule; C2 owns byte/hash/path provenance, and C15 owns diagnostic transport,
identity, publication and rendering. D1/D2/D3/D8/D9, C1/C3, BOOT1/BOOT8 and TEST2
also govern the slice. No language rule changes.

The public API is reject-source-path! with explicit late Vars for the
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

## Implemented native boundary

The existing two-argument body now lives in gravity.reader/reject-source-path!.
Only its four owner lookups become explicit late Var ports. Bootstrap retains
both compatibility arities, with its entire one-argument capture and late
self-call unchanged. All three earlier Reader bodies and the Bootstrap loader
are byte-identical to parent a05b1b5e0aff705123880bbb629fbff146356d12.
The whole compatibility component differs only by its two-argument wrapper.
Reader now owns both native encoding and extension rule data; callers retain
admission, identity resolution and publication.

Actual before/after native controls compile both source spellings and preserve
nine complete refusal/arity records, including an existing invalid-extension
file, a missing file, a directory and supplied nil/empty/actual snapshots.
Complete data and serialized products are equal. Focused tests pass21/263,
including original-file versus supplied-snapshot provenance, missing-file facts,
late stage and one-to-two-argument interposition, dependency exception identity,
hash-before-failure lookup and failure-before-extension lookup. Trusted host
interpositions remain separate from genuine Core controls and confer no source
authority. The language boundary gate passes6/11.

The first focused test invocation used the self-hosting runner, which rejected
the Reader namespace before tests executed. That selection nonpass remains in
uniform-reader-path-diagnostic-focused.log. The normal development runner passes
without changing source, guards or selection policy. Native controls and exact
conservation use /tmp/uniform-reader-path-diagnostic-*.clj; complete current Core
controls use reader-path-before/after in the CLI evidence directory. Own CI,
independent exact review and final clean current-main preflight remain required.

All four complete data and original serialized pairs pass: native products,
argument products and both complete public Core artifacts. Both Core subjects
retain25 nodes and one API target read, source SHA
8e63ea9e37ab40a8e66975b3e58dc8eb5b9b74aa339d2625d5bd67070802758d and
recomputed Core id
sha256:16ec6d50a8514188f3010880f3022e88f7f1054fe4058a5caa6b74367ee98f0e.
The actual complete current C6 source compiles through the fixed seed; its
development-unadmitted, incomplete and non-self-hosted boundaries remain.
Coupled Reader/CLI/diagnostics/literal/Release tests pass46/673. Docs240/19,
roadmap/governance185 and contextual lint3 paths/0 warnings/0 blockers pass.
Lint receipts are archived outside the source checkout. No admission pin or
guard is replaced, and no artifact field is projected away for equality.

## Current accounting, custody and release-proof composition

Actual main 7ad0edc7abcc7ea7b8f536e8363bd66ad2329453/tree
5271f1984a28f9da39b24beb999ad2038fc4c210 includes PR749 accounting
removal, PR746 host custody and PR748 read-only release verification. The
[ordinary-development record](current-ordinary-composition-evidence.md)
distinguishes current semantics from immutable historical A1 evidence.
All incoming implementation and tests remain preserved; Safety's shared
registrations are rebuilt from exact main plus its frozen association,
including the new process helper and unchanged primary catalog formulas.
Stage algorithms and fixtures remain conserved. Fresh current-base controls,
catalog identities, ownership/language, contextual lint and independent review
remain required. Old-base CI supplies no current integration eligibility.

Fresh current-base coupled checks pass46/673, ownership/language46/1136.
All4 complete data and original serialized pairs, actual source hashes,
deterministic identities and whole-component conservation pass. Current Core
controls still accept both spellings with25 nodes/one target read/source8e63
and recomputed Core16ec. Contextual lint passes3 paths/0 warnings/0 blockers.
Terminal lint receipts are archived outside the source checkout. Source
admission, historical denied routes and migration pins remain unchanged.
