# Native source-throw envelope construction

## Scope and contracts

This scope precedes implementation and its early draft PR. D1, D2, D3, D8,
D9, L2, L9, C1, C15, R1, R4, R11, R12, BOOT8 and TEST2 govern the slice.
Starting source is `f12318b6e9f966582e962a99b3fe8191f60dba5c`, tree
`d2c6fbd909e00506e09f910cbfdc750a25d2e35a`. Its ordered parents are the
previous frozen source `61073df88809f5c024fc40b1a9adde4c6200e96e` and published
main `643d75125f1b32a7756f80560ff6f3fbd5c5fbbb`. That normal merge imports only
the two published A1 diagnostic-path files; all other source modes and blobs
remain unchanged. Earlier acceptance does not transfer to this successor.

## Runtime boundary

The existing exception-envelope constructor moves from private
`p15-s23-stage2-runtime-source-throw!` in component 00590 into:

```clojure
gravity.runtime/source-throw!
[source-throw-key source-throw-token-var plan payload]
```

The stage constructs and throws the existing internal `ExceptionInfo`, retaining
its message, complete data shape, source/module facts, payload identity and
opaque token identity. Bootstrap passes its existing namespaced keyword and
private token Var. The token is read at the original map-value position, after
exception-constructor selection and before plan lookups. It is not eagerly
captured at wrapper entry. The private compatibility name and arity stay intact.

Inputs are trusted caller-owned key/token data, an existing native seed plan,
and an already evaluated payload. Output is non-returning source-throw control.
No plan, source hash, compiler provenance or deterministic plan identity is
rewritten. No facts are invalidated; only the existing ephemeral envelope is
constructed. The token has process-local object identity and is not a portable
or serialized error identity.

Runtime owns envelope construction. Existing instruction dispatch owns malformed
throw rejection and operand evaluation. Bootstrap retains the opaque token,
operation-map wiring, scope/entry/call authority and failure publication.
Upstream type, effect and profile legality remain with their existing owners.
There is no new diagnostic id. The dependency direction stays Clojure core to
Runtime to Bootstrap; Runtime must not require Bootstrap.

## Evidence obligations

Use the existing paired direct-typed-throw source fixture through the existing
fixed compiler-rule and ordinary three-argument seed plan compiler. Read-only
inspection confirmed the compiler source matches its existing 128499-byte and
SHA256 `57c13e44b46726a136ecd237dde5e85d86cd55b82bd8ac6033afce2fb87bf527`
binding. Preserve that pin. Emit complete plans with actual source-path/hash and
compiler context recorded alongside them. Recompute their original canonical
plan identities. Execute the false branch and throwing branch, retaining payload
and token identity within each process.

Add paired ordinary defn-based zero/many-operand source fixtures. They must
reject with the existing seed `L2-THROW-ILLEGAL` before transport, without claiming
fresh C7/C8 acceptance. Retain malformed-instruction rejection, source-throw
operation selection before operand evaluation, exactly-once operand evaluation,
late token reads, plan lookup order and exact propagated host exceptions.
Compare original and stage constructor fields and identities in the same process;
serialized observations must explicitly exclude the opaque token.

Extend the existing Runtime pipeline namespace and reuse its current whole
namespace CI command. Preserve all previous Runtime bodies, token allocation,
00590 nonmoved forms, recurrence representation, source admission and all scope
guards. Require focused regressions, boundary/ownership, conservation, current
main context, independent semantic/adversarial/provider/CI review, a clean source
commit and final integration gates. Record results only after execution.

## Unsupported boundaries

This is temporary Clojure seed control transport, not a canonical Gravity error
ABI, host-exception normalization system, portable serialized token, target
runtime, new capability grant, runtime-family selection or full Runtime
conformance. It does not establish fresh C7/C8 typing/effect/source-allowance
evidence, plan authentication, source/package admission, migration, release,
self-hosting or seed retirement.

Held Reader/C2 routes, authenticated composition, private source factories,
backend source builders and release admission remain uninvoked and unchanged.
The permitted seed rule loader does not provide those authorities. No Reader
rebinding, source repinning or replacement admission route is included.

## Executed development evidence

The original Runtime namespace passes 63 tests and 2484 assertions. After the
extraction and five new tests, it passes 68 tests and 2552 assertions, with zero
failures or errors. The new tests compare the original constructor, stage API
and compatibility wrapper in one process, retaining physical token, payload,
source and module identity. A plan lookup changes the token and exception
constructor roots to check their original selection order. Exact host exception
propagation and once-only operand evaluation are checked separately.

The paired direct-typed-throw programs compile through the ordinary seed route,
emit complete EDN plans with actual source path/hash and compiler provenance,
and execute both branches. The four new zero/many-operand fixtures reject with
`L2-THROW-ILLEGAL` before transport. All six complete before/after observations
are retained externally; the process-local token is checked before exclusion
from serialized records. The baseline receipt SHA256 is
`117e809ca8243dd8d99a291eb05c7de55c6dd6aff667f76844debc390dd41eb2`;
the candidate receipt SHA256 is
`80821ee9f17f8310bda48b9bf813688c434ed4b21107e84f1bec5df7f158fc27`.
Implementation hashes differ intentionally and are recorded separately from
semantic observations.

The candidate language/ownership gate passes 48 tests and 1202 assertions.
The document validator passes all 240 canonical documents and 19 phase indexes;
the full-language roadmap structural validator passes. These checks do not
establish full-language or milestone completion. A supporting batch initially
ran from an older worktree; its revision guard stopped source capture before
emission. Those logs are retained and confer no candidate evidence. The checks
reported here ran from this Runtime worktree.

The unchanged `runtime-native-validation` command passes with its existing 2g
heap and 180-second host watchdog. All 21 contract command vectors and test
registrations are unchanged. The CI harness passes 54 tests and 650 assertions.
Cold Runtime loading exposes 38 APIs without loading Bootstrap. Conservation
receipt SHA256
`d7cbdf830f76041e1c141b8bc6c90fe4b726b7c280e536fbaf78f26919e7a284`
records all 37 previous Runtime bodies, the token allocation and all nonmoved
00590 forms unchanged, 14 protected paths, six equal source observations and
19 byte-preserved generated files archived outside the worktree. The two source
files lint with zero warnings. Whole-file test formatting retains pre-existing
drift; the five added test forms are formatted separately with their reader
forms preserved, allowing for fresh anonymous-function argument symbols.

The early draft is PR #1284. Exact final-source review, current-main context,
provider/CI acceptance and integration remain separate obligations. Earlier
Package or Runtime acceptance does not transfer to this source tuple.
