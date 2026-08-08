# Python Tooling Layer

The Python code in `src/gravity/` and `tools/` is a bounded stage0 semantic and
development-tooling layer. It is not the Gravity compiler authority, an
implementation milestone claim, or a release gate.

Contract: `contracts/python-tooling.json`

Validator: `python3 tools/validate_python_tooling_contract.py`

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

`src/gravity` ownership: unresolved and blocking in the external project-structure contract; this contract classifies the files but does not authorize edits.

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
