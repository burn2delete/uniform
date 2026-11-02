# Retired validator branch correspondence

Owner: ACC-01. This records the narrowly accepted source disposition for four
original changed C7 definitions, not an implementation or semantic equivalence claim.
The original 29-path inventory and its counts remain unchanged.

## Exact inputs and differences

Historical PR502 head is `6a4f87f013d0076b81aba9e0667650e70d28e635`.
Assessed main is `2654d155322f7863dd087065f19000b43c877cbe`, tree
`8e2f688ffcad861682c54571a07eee37517b9363`. The companion EDN pins both
complete C7 files, each complete definition and every differing subtree.

| Preserved definition | Complete source differences on assessed main |
| --- | --- |
| `sh08-pc-first-work` | `(+ base 0)` is `base`; the validator `:work-limit` alternative is absent while the separate carrier work-limit mapping survives. |
| `sh08-pc-key-part` | `(+ base 0)` is `base`; the validator work-limit diagnostic selection is replaced by its original semantic failure reason. |
| `sh08-lsi-vector-first` | Two `used (+ used 0)` rebindings are absent; the two validator work-limit alternatives are replaced by the original grounding failure reason. Both validator calls survive. |
| `sh08-vsl-parameter` | The `work (+ work 0)` rebinding is absent; the validator work-limit selection is replaced by the original receiver-proof failure reason. The validation call survives. |

There are exactly two differing subtrees per definition. Argument lists and all
other complete-form source remain identical. This explains why these four units
are not exact historical form matches without discarding the unique differences
or restoring obsolete diagnostic branches merely to obtain a hash match.

## Validator result source and preserved boundaries

The record also pins the current `sh08-lc-validate-types` and
`sh08-lc-native-arguments` definitions. Their literal rejection maps contain
only the listed semantic reasons: nested Never, cyclic or malformed types,
unsupported types, unsupported arguments and cyclic arguments. Neither complete
definition contains `:work-limit` or `:work-count`. The validator's ingress
rejection returns that separately pinned ingress result.

These are source observations. They do not execute either helper, prove native
broker behavior or close transitive call graphs. All surrounding carrier,
source, descriptor, type, result and receiver checks remain in the complete
form comparison. Outer budgets, width guards and carrier accounting are still
present and remain separate unresolved work; this proposal does not approve
their retention or remove them.

## Independently reviewed source disposition

Independent classification accepts retaining the current four definitions and rejecting
restoration of the eight listed accounting remnants under the repository's
project-accounting removal instruction. All other complete-form source and
arguments match history. The old zero-charge expressions and work-limit
diagnostic alternatives remain recorded as history rather than recreated to
obtain matching hashes. This request does not endorse the still-present outer
budgets or width predicates. It does not claim arithmetic or host-number-object
identity equivalence. Exact packet `1da3e25b`, tree
`53642c707fecf553b687d89fe47c72f36876ee1a`, has independent source
classification receipt
`5b7b28a3881386b00333f989711d173fdeca9a4a766046e3f4ab6ae9d65c467c`:
155 source checks and twelve effective return-map mutation refusals. The
consumer reproduced that receipt byte-identically and freshly verified all
six selected definitions and governing policy pins against main `b1d590cc`.
See `retired-validator-classification-consumption.edn`.

This closes only restoration of the eight listed accounting remnants. All
other caller source and arguments remain conserved. It grants no arithmetic,
number-object identity, native/runtime, outer-quota or whole-PR569 acceptance.
The original EDN records' pending states remain captured proposal history;
this later consumption supplies the narrowly accepted decision.

The evidence commit carries the older C7 whole-file hash `25e4dc4b...`, while
the assessed main and current main carry `9c026369...`. The six selected
definitions are identical across both; the complete files differ elsewhere.
The evidence commit's C7 file is unchanged from its parent. No whole-file
equivalence or current-source execution follows from the selected-form match.

### Current normal-return source envelope

`retired-validator-return-envelope.edn` adds a distinct bounded source analysis
on main `e4d98947201a9473d8bca5777c1f59f5e3426bfd`. The complete C7 file and all
six selected definitions are unchanged from the original correspondence. The
record pins the governing accounting policy, L2 core-control semantics and D9.

The analysis reads only `sh08-lc-native-arguments` and
`sh08-lc-validate-types`. It joins tail branches of `if`, follows `let` result
aliases, treats `recur` as a control transfer, and uses a pinned summary for the
native-arguments helper when the validator returns `ingress`. An unclassified
tail expression, unknown returned variable or shadowed helper call refuses the
proof. Loop-bound variables are unknown rather than assumed to retain their
initial values, so a value escaping through a loop cannot acquire a false
literal-map summary.

Unlike the earlier literal-map scan, the validator's conservative envelope
includes the ingress helper's unsupported/cyclic argument reasons as well as
its own nested-Never, cyclic-type, malformed-type and unsupported-type reasons.
No normal-return reason in that envelope is `:work-limit`, and no returned map
schema contains `:work-count`. This supports independent assessment of the
historical work-limit alternatives in the four callers.

Branch conditions are deliberately not refined. For example, the envelope
includes the ingress helper's accepted argument result even though the actual
status guard excludes it from the returned-ingress branch. This conservative
overapproximation suffices for the work-limit exclusion; it is not an exact
runtime return-schema or reachability claim. The argument assumes L2 control
semantics and the pinned, unrebound helper definitions, not a host-language
implementation of Gravity arithmetic or native behavior.

Twelve negative source controls each mutate one actual return map in a pinned
helper while preserving the surrounding source. They introduce a retired
work-limit reason, a nonliteral reason, an unknown provider return, an escaping
loop variable, a shadowed helper call or returned work telemetry. The work-limit
and telemetry mutations preserve both status alternatives and expose the
forbidden result in the computed envelope before rejection. No source helper,
Bootstrap or target program is executed. Termination, exceptions, effects,
mutable bindings, native providers and transitive equivalence remain unproved.

The Clojure reader checks use evaluation disabled and verify all four complete
comparisons and both validator pins. Ten inert negative controls refuse a
dropped disposition, an unrelated argument-contract change for each definition,
and a reintroduced work-limit result in either validator. Bootstrap and Gravity
are not loaded; genuine owner controls remain UNRUN. This supplies no whole
ACC-01, PR502 closure, release or self-hosting authority.
