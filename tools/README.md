# Frozen Python Tooling Migration Layer

The Python code in `src/gravity/` and `tools/` is legacy migration debt. It is
frozen: do not add or modify Python. It is not the Gravity compiler authority,
an implementation milestone claim, a release gate, or an accepted language
direction. New bootstrap, seed, and tooling work uses Clojure; admitted
Gravity/Uniform replacements then retire the Clojure seed incrementally.

Legacy contract: `contracts/python-tooling.json`

Language boundary: `contracts/language-boundary.edn`

Boundary gate:
`clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`

The historical Python contract validator is itself migration debt. Do not
extend it or use it as precedent for new tooling.

Scope: every tracked or intentional untracked non-`__pycache__` Python file under `src/gravity/` and `tools/`.

Python authority: none; observations and generated outputs remain non-authoritative unless a separate normative contract and coordinator admission establish otherwise.

The contract pins the sorted file inventory and the normalized internal import
edge inventory by SHA-256. Its mutually exclusive components then declare a
role, dependency direction, effect envelope, output class, authority ceiling,
path-policy references, import-safety mode, execution mode, and test surface.
An added file, an unexpected internal import, a dependency cycle, or an effect
outside its component envelope fails validation.

## Layer boundaries

`src/gravity/` is the semantic library layer. It may import other semantic
modules and standard-library modules, read declared repository inputs, and
perform in-memory computation. It may not import `tools`, execute processes,
use the network, or write files. The semantic layer is import-only: it has no
CLI main guards and no import-time effects.

`src/gravity` ownership: coordinator-reviewed semantic support in the external project-structure contract. This contract does not authorize Python edits or compiler authority, and these paths remain outside Stage0 module ownership and slices.

`tools/` is coordinator-owned reviewed tooling under the external
`reviewed-central-routing` policy. CLI tools must keep work behind an explicit
`__main__` guard. Library helpers must remain safe to import.

## Outputs and review

Generated evidence and coverage outputs use the isolated atomic publication layer and remain separate from reviewed source.

Validators exposing `--artifact-out` or `--coverage-out` must depend on
`tools/output_publication.py`, declare an isolated generated-output class, and
reference the matching project-structure output policy. Status printed to
stdout is diagnostic output, not evidence authority.

Reviewed-source generators require coordinator review and nonparallel execution; this contract does not authorize running them or admitting their output.

The reviewed-source generators are `tools/generate_gravity_docs.py` and
`tools/enrich_remaining_docs.py`. They can rewrite canonical documents, so
their component is coordinator-serialized and explicitly distinct from the
isolated evidence generators.

Development receipts, baselines, logs, and checkpoints are isolated working
state. They are not authoritative compiler-pass evidence or release evidence.

Authoritative evidence composition: Python candidates and reviewed-promotion candidates remain non-authoritative; pass/slice/artifact/policy/check data is nonclaim context; no digest is a signature; v1 has no trusted admission root and cannot mint authority.

Evidence producer inventory: `contracts/evidence-producers.json` classifies every production Python filesystem producer separately from the compiler artifact graph; `python3 tools/validate_evidence_producers.py` fails closed on uncovered or overlapping producers, output-policy drift, unreviewed reviewed-path generation, incomplete provenance/schema/nonclaims, and forged or stale promotion records.

The inventory binds generated validation artifacts, coverage reports, private
development receipts/caches/logs/checkpoints, authority candidates, and the two
coordinator-serialized reviewed-document generators to their commands, inputs,
logical outputs, schemas, writers, ceilings, and review obligations. The
`tools/output_publication.py` helper is a publication primitive rather than a
producer. Test writers and `validate_gravity_toolchain.py` temporary scratch
are outside the production producer inventory and cannot be promoted.

Promotion records remain non-authoritative even when their status is
`admitted`. Admission requires a reviewed commit, a distinct reviewer, current
source/output/inventory digests, and all required checks, but the Python layer
has no trusted boundary that could turn that record into compiler, aggregate,
or release authority. A digest is never treated as a signature.

Evidence promotion commit policy: an admitted record's full commit ID must name an existing commit in this repository and be reachable as an ancestor of the current `HEAD`; current source and output bytes are opened by component-wise no-follow traversal. The validator separately inventories the seven validators that expose `--coverage-out`, pins non-producer exclusions exactly, and rejects reviewed-document output patterns that overlap generated artifact policies.

`tools/compose_authoritative_evidence.py` writes only beneath
`target/validation/`. It binds current project-structure and verification
semantics, exact pass/slice/artifact/policy/check scopes, dependency and impact
closures, non-authoritative development and C2 cache references, and fresh
SH07 source/runtime/output identities. The module IDs are the only evidence
subject; pass, slice, artifact, policy, and check sets are dependency context
and never authority claims. Promotion revalidates one coherent candidate
snapshot and records reviewed material below `contracts/` or `docs/`, but v1
always emits another non-authoritative, release-blocked candidate because the
repository has no trusted admission root. Weak or unrecomputed child IDs,
proof-receipt reuse, and a legacy launcher symlink without a checkpointed
canonical target are explicit promotion blockers.

## Validation limits

Static-analysis limit: AST checks cannot prove dynamically constructed imports, calls, or runtime behavior; such behavior requires separate execution evidence and review.

Alias-analysis limit: direct imports and simple module-level aliases are checked, but dynamic `getattr`, monkey-patching, and runtime alias construction require separate review.

Output-policy limit: custom or dynamically constructed argument parsing and writers are outside this static check and require separate review.

The focused Python tests exercise schema failures, path traversal, inventory
and dependency drift, cycles, semantic-layer violations, validator output
policy parity, README parity, and bounded import smoke. They do not launch the
Clojure compiler or a JVM.

## Workstream governance tools

`clojure -M tools/validate_workstream_governance.clj` validates the closed
governance contract and ledger, lifecycle transitions, invariant-family
exclusivity, dependency admission, the two-failure architecture stop, and the
complete integration-evidence envelope. It reads repository records and emits
diagnostics only. A passing result does not establish implementation,
self-hosting, seed-retirement, or release correctness.

`clojure -M tools/check_worktree_preflight.clj` performs a deterministic,
read-only
Git reconciliation check. Inspection mode inventories current and registered
worktrees without mutating them. Integration mode additionally fails closed on
dirty or detached state, missing or divergent bases, and expected-identity
mismatches. Ancestry and tree equivalence are reported separately so a squashed
or already integrated tree is not merged again. Explicit output exclusions are
bounded inspection classifications only; they never make integration dirty
state clean.
