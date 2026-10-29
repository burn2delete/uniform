# C11 source interface versions

The authenticated C11 constructor and contextual verifier retain their legacy
arities and historical v1 source contract. The new explicit arities take a final
selector argument, `{:interface :gravity/c11-mir :version 2}`. The selector must
be a metadata-free native map with exactly those keys, native Keyword interface
identity and native Long version 2. It selects a built-in source contract; it
does not supply source identities, verifier results or construction authority.

The governing semantic contract remains
`phase-06-compiler-architecture/090-c11-gravity-mir-specification.md`.
Existing Core authentication, profile, source, origin, type, effect, ownership,
safety, index, dominance and full artifact comparison requirements remain.

## Version and source binding

V2 requires the reviewed current C11 source and all compiled plan, function,
builder and verifier identities in `gravity.c11-mir.source-contract`. Source
binding reads the actual UTF8 bytes, validates their identity, compiles that same
text and compares the actual resulting identities and required function shapes.
Historical v1 constants and its ten-argument definitions builder remain intact;
v2 requires the current eleven-argument builder with its leading checked Core.

V2 initially admits only the Bool branch-join source family: Boolean literals,
truthiness normalization, one conditional and a Bool-returning function, with an
optional single authenticated Bool entry parameter and its local reference.
The gate follows public Core authentication and precedes MIR construction. The
existing parameter descriptor validates its declaration and original facts.
Unsupported families are rejected, rather than inferred from a supplied MIR
parameter vector or report. The family restriction is semantic, not a work or
input quota. Future families require an explicit contract extension.

The selected version is bound into the artifact schema and source rule,
independent verifier report, pass execution, B1 candidate and scope identities.
The v2 scope also identifies the supported source family. A selector and artifact
version mismatch fails before payload traversal. On legacy arities, only a
safely recognized v2 header receives that early mismatch diagnostic; malformed
legacy inputs retain the original owning validation and containment path.

## Verification and acceptance

The public constructor still authenticates checked Core through the actual
public Core verifier before executing the Gravity C11 builder. The public report
reauthenticates Core, recomputes the independent Native report, executes a fresh
Gravity replay and compares the complete typed artifact. An artifact-contained
report remains untrusted payload. The header grants no report authority.

Dispatch and malformed-input regression controls live in the existing Native
helper test owner. These controls establish interface behavior and Bootstrap
loading, not genuine final construction or replay. Acceptance of the new source
contract additionally requires genuine final construction and contextual public
verification of the same artifacts, exact diagnostics for substitutions and
mixed versions, and immutable source and harness receipts. Until those results
are independently accepted, v2 final-artifact acceptance remains unproven.

This interface does not select C12, backend emission, native-code execution,
release admission or the separate workflow replay-policy contracts.
