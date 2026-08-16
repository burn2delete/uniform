# SH-06 Namespace and Binding Resolution Fixtures

Every source case has a byte-identical co-canonical `.gravity` / `.qst` peer.
The path and extension are retained as provenance but do not participate in
semantic binding, namespace-analysis, dependency-graph, or artifact identity.

## Accepted pairs

- `resolution-order` exercises lexical, current-namespace, alias-qualified,
  fully-qualified, referred, core, and type-position resolution.
- `module-boundaries` exercises public exports, a hosted dependency, an
  accepted pure-core cross-profile edge, effects, capabilities, aliases, and
  dependency-graph emission.
- `foreign-explicit` exercises an explicit foreign import record with an
  interop boundary instead of ambient host lookup.
- `compiler-subset` is a small compiler-shaped `:meta` namespace with multiple
  functions, parameters, local bindings, conditionals, loops, and recursion.

## Rejected scenario pairs

Each rejected source carries executable scenario input below
`[:compiler :sh06-request]` and a separate expected-result oracle below
`[:sh06]`. The oracle is test data and is never an input to resolution.

| Base name | Expected rule | Rejection family |
| --- | --- | --- |
| `unresolved` | `C5-UNRESOLVED` | symbol has no binding |
| `ambiguous` | `C5-AMBIGUOUS` | multiple legal candidates |
| `private` | `C5-PRIVATE` | private binding crosses a namespace boundary |
| `alias` | `C5-ALIAS` | alias is unknown or duplicated |
| `shadow` | `C5-SHADOW` | lexical binding shadows illegally |
| `cycle` | `C5-CYCLE` | namespace dependency cycle |
| `cross-profile` | `C5-CROSS-PROFILE` | profile edge lacks an accepted boundary |
| `capability` | `C5-CAPABILITY` | required authority is unavailable |
| `target` | `C5-TARGET` | dependency is target-incompatible |
| `foreign` | `C5-FOREIGN` | foreign import record is malformed |

The fixtures cover the bounded bootstrap resolver surface only. They do not
claim type/effect checking, package resolution, seed retirement, or release
readiness.
