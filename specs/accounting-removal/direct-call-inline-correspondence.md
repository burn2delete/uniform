# Direct-call helper source incorporation proposal

Owner: ACC-01. This proposal covers exactly two original emitter units:
`p15-s23-typed-direct-call-compatibility` and
`p15-s23-typed-direct-call-arguments`. It changes no production code.

## Complete source correspondence

Historical input is PR502 `6a4f87f013d0076b81aba9e0667650e70d28e635`.
Assessed main is `c486a4d78f67937deb42b8bfa978e456d583bc32`. Each helper's
complete historical body occurs exactly once inside current
`p15-s23-typed-compile-call`; neither helper remains a separate definition.
Each historical call also occurs exactly once and passes precisely its own
declared parameter symbols, in order, without argument substitutions.

Substituting both complete historical bodies into their calls reconstructs
one complete current enclosing `let` subtree exactly, including all other
bindings, branches, checks and result handling inside that subtree. Its source
path is unchanged. The five enclosing outer `let` binding vectors are identical
between historical and current callers, as are the caller's parameters. These
are stronger witnesses than locating two isolated body fragments.

The companion EDN pins both historical functions and their bodies, calls and
source paths, the complete enclosing subtree, outer bindings, complete files
and callers, original-inventory membership and directly named dependencies.
Dependency source differences remain explicit; no transitive equivalence is
inferred from a same-named call. Gravity identifiers, numeric literals and
source terms are not normalized in these equations.

## Proposed decision and limits

The requested decision is conditional source incorporation under the pinned,
unrebound lexical/callee bindings and L2 variable-access semantics. It does not
equate whole callers, prove branch reachability, preserve function call frames
or dynamic binding behavior, establish native/exception/runtime semantics, or
approve retained accounting. Both complete bodies still contain their existing
work accounting; accounting removal remains a separate open obligation.

The earlier `dispatch-source-correspondence.edn` is reused only for its two
inline-helper observations and hashes. Its twelve-unit DSE mapping, moved
dispatch, kind guards, runtime ownership and denied historical review delivery
are outside this proposal. This is not acceptance of that mapping or a retry
of its denied operations. The old held actions remain stopped.

## Validation and acceptance

Both original memberships, complete function/body hashes, exact calls and
body occurrences, the complete enclosing `let` equation and all five outer
binding vectors pass inert source checks. Six effective corruptions refuse
either lost inline body or changed context, budget, edge and argument sources.
No Gravity helper, Bootstrap, compiler, native provider or runtime executes.

L2, C14, D9 and current accounting-removal policy govern independent review.
Independent classification remains pending. The original inventory and stable
ACC IDs remain unchanged. No whole PR569/ACC01/PR502, release, self-hosting,
or actual direct-call support claim follows.
