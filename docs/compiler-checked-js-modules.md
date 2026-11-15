# Checked scalar JavaScript modules

## Scope and finish line

This increment connects ordinary hosted/safe `:js-ts` Uniform source to
freshly checked Core, verified compositional scalar MIR, B1/C14 target
eligibility, and executable Node 20 / ES2022 ESM modules. Uniform source
owners retain semantic admission, target legality and lowering rules. The
Clojure seed remains the compiler host and transports and publishes artifacts.

The supported language subset is I64 and Bool, checked I64 addition, eager
local bindings, Boolean branches, fixed-arity nonrecursive direct calls,
declared numeric-error effects, actual exports and private helpers. Runtime
I64 arguments and results use JavaScript BigInt with signed 64-bit checks.

Completion requires real Node execution with arguments supplied after
compilation, differential comparison with actual verified MIR, exact runtime
rejection checks, meaningful source maps and declarations, and complete
manifest/provenance sidecars. A copy outside the checkout must execute without
Clojure or repository files. Public CLI dispatch and complete clean-checkout
acceptance remain the following integration increment.

Governing contracts: D1/D2/D3/D8/D9, C6-C11/C14/C15, B1/B6, P4 and SAFE9,
with the applicable runtime, ABI, provider and artifact-provenance contracts.
Historical source-interface admission and release pins are not renewed.

## Starting state

Latest connected main and fetched Git main both resolve to
`4c0ada9438cbcf20587447541ecfd16ab08e99a7`, the merge of PR #1439.
The isolated implementation branch is `codex/js-checked-scalar-modules`.
PR #1429 supplies the current-source construction boundary and PR #1439
supplies scalar function composition. Earlier execution reports remain tied
to their recorded revisions; this increment reproduces its own prerequisites.

The draft opens before implementation. Its scope is limited to genuine
JavaScript source admission, target eligibility, static MIR lowering,
checked JavaScript ABI, production publication, and focused acceptance.
It does not include recursion, closures, imports, collections, additional
arithmetic, source I/O, other backends, self-hosting, seed retirement,
automation changes, public CLI integration, or release packaging.

## Implementation and evidence status

Implementation and verification are in progress. The required source/MIR
baseline, first JavaScript rejection, concrete API invocation, complete
artifact inventory, source/compiler identities and terminal verification
results will be recorded here as each checkpoint completes. An unfinished
or failed execution will not be reported as acceptance.
