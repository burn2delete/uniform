# Retired validator branch correspondence

Owner: ACC-01. This is a source disposition proposal for four original changed
C7 definitions, not an implementation or accepted semantic equivalence claim.
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

## Proposed disposition and acceptance

Retain current definitions and account for these eight exact historical source
differences explicitly. Independent review must decide whether their removal is
fully supported by the current validator contract and Gravity arithmetic
semantics. No host-language execution is offered as that proof. Until then,
the four incorporated dispositions remain proposals, with no completion credit.

The Clojure reader checks use evaluation disabled and verify all four complete
comparisons and both validator pins. Ten inert negative controls refuse a
dropped disposition, an unrelated argument-contract change for each definition,
and a reintroduced work-limit result in either validator. Bootstrap and Gravity
are not loaded; genuine owner controls remain UNRUN. This supplies no whole
ACC-01, PR502 closure, release or self-hosting authority.
