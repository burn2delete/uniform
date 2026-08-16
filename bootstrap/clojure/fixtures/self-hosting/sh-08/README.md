# SH-08 Bounded Type Slice Fixtures

These paired `.gravity` and `.qst` fixtures exercise an executable
Gravity-owned SH-08 local type template over structurally validated
SH-07 B47 canonical core records. The primitive leaf remains structurally
validated. The function/local/call leaf additionally requires the complete
nodes, definitions, binding table, function records, calls, call edges,
recursion components, and lexical bindings to match the B47 canonical identity
preimage before inference. The C7 entry request also carries the complete B47
wrapper, canonical core, authenticated core request, real B47 verification
report, canonical identity preimage, authenticated-envelope records, and
provenance-binding preimage under one exact coordinator-resolved digest
preimage. Gravity validates that binder before typing. SHA-256 resolution and
B47 report execution remain host-owned: this slice does not claim native
Gravity cryptographic verification or remove host digest authority.

The bounded slices cover primitive literals, vector/map/set literal
descriptors, definitions, Gravity truthiness, equal-type conditional joins,
fixed-arity first-order functions, immutable `let` locals, direct local calls,
and one capture-free named callable value hop with one authoritative primitive
signature (integer, bool, or string). Function inference uses a declared
finite round bound. The `function-self-recursive-type` pair additionally
admits exactly one named, capture-free, positive-fixed-arity self edge. Its
recursive call must forward each positional parameter directly, in exact
arity/order and binding lineage; its sibling branch must be a direct
primitive-literal base from the same authoritative family; and a concrete
external call must supply every parameter slot. The bounded monotone fixed
point must converge. It emits an additive recursive proof, call fact, and
constraint ledger while preserving the function skeleton's pending ownership
and thrown-error obligations. The `function-value-typed-bool` pair and
`function-self-recursive-string-type` pair exercise the bool and string
diagonals; their `.gravity` and `.qst` files are byte-identical parity inputs.
Mutual SCCs, zero-arity/no-base cycles, captures, higher-order or polymorphic
recursion, transformed/literal recursive arguments, multi-arity/variadic
recursion, edge/order tampering, unsupported primitive kinds, and
nonconvergence remain explicit C7 diagnostics. Other nonlocal or lexically
supplied callable shapes remain explicit `C7-ANNOTATION` rejections.

All accepted and rejected function fixtures first pass through the SH-07 B47
canonical lowering path and are then consumed by the Gravity C7 leaf. This is
distinct from asking the generic stage2 compiler planner to compile and execute
the fixture as a standalone compiler module: in particular, the
`function-call-nonlocal` pair models the one accepted capture-free callable
parameter shape. It does not claim that generic stage2-plan execution supports
such calls.

The slices preserve declared profile, target, effect, capability, source,
origin, generated-origin, binding, ordered-argument, and B47 identity-preimage
data. Digest preimages are deterministic and checked for exact equality, but
their digest resolution remains coordinator-owned. They do not claim list
lowering, general higher-order calls, captures, multi-hop callable flow,
variadic or multi-arity functions, primitive kinds outside integer/bool/string,
general recursive inference beyond the single bounded self-edge rule, records,
unions, protocols, generics, casts,
dynamic boundaries, ownership, layout, schemas,
effect legality, capability legality, native authenticated-envelope
cryptographic verification, or complete SH-08 support.
