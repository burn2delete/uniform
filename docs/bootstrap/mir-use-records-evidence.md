# MIR: native placement and use/definition records

D1, D2, D3, D8, D9, C1, C6-C11, C15, BOOT8 and TEST2 govern this
bounded extraction. Its original clean source parent is the independently
reviewed, not yet integrated MIR fact-table candidate
6b91c6cd5240a70488e789571cb691e86ee93135, tree
772c8d1f7aef847a75536042d91e940ed1b5b466, in the Analysis checkout.
That parent remains pending integration in PR815/PR816; no acceptance or CI
result is transferred to this successor.

Eight existing native C11 helpers move from Bootstrap into gravity.mir:
downstream-ids, expected-block-operation-ids, expected-data-flow, expected-uses,
expected-terminator-uses, expected-terminator-value-uses, expected-value and
expected-runtime-check-value. Explicit owner parameter, opcode, id, source and
use operations plus native nodes, operands, paths, operations and optional
parameter data produce the original placement references, edges, uses and
value records. The eight bodies are conserved apart from names, explicit
operation ports and forwarding those ports in the optional-parameter arity.
Bootstrap wrappers retain late Vars and their original four-to-five-argument
expected-value dispatch. Remaining fragment schemas and existing helpers are
unchanged. The stage still has no imports or reverse Bootstrap dependency.

The stage owns native reference-record construction. Existing consumers own
equality, dominance, type/effect/safety/origin decisions and diagnostic
construction. An expected record is not a verified value, source admission or
certificate. Borrowed source, ownership, effects, safety and parameter-definition
objects remain data; callbacks are not authenticated. Single-entry/return and
single-conditional placement remain the supported families. Full C11,
general CFGs, loops, matches, exceptions and runtime-check legality are open.

Both existing normalized branch/join fixture spellings and current Gravity C11
builder/verifier compile through the ordinary fixed seed and emit real MIR.
The Gravity owner verifies the result and rejects an actual forged data-flow
edge with C11-DOMINANCE. Complete plans, sources, MIR, positive/negative reports,
native reference records and an explicitly unverified runtime-check value are
captured before/after. Historical fixture labels and serialization hashes
remain test data, not authentication or renewed migration evidence.

Tests cover downstream closure with reordered input, guard-before-operand
indices, ordered use filtering, false constant payloads, parameter definitions,
borrowed fact identity, owner callback order and exception identity, wrapper
ports and optional-parameter interposition. The first temporary extraction
script had an extra parenthesis before any source write; its nonpass is retained
and the corrected whole-source conservation check passes. Full data/raw text
conservation, focused/boundary checks, lint, contract validators, independent
acceptance, own draft PR CI/artifacts, parent integration and immediate exact
clean current-main preflight remain integration gates. Final receipts are
maintained in the PR. No complete MIR, backend/native execution, performance,
migration, release, self-hosting or seed retirement is claimed. Restricted
native/private factories, frozen Reader, package/archive/release builders and
Attempt30 routes remain unexecuted.
