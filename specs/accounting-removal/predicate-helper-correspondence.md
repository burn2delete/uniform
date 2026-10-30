# Predicate helper source correspondences

Owner: ACC-01. This supplement consumes the unchanged
[predicate source record](predicate-source-disposition.edn), assessed at
`0f7acce5fc93e019062d84a3fd9dcf05643ba7fd`, and checks its six explicitly
nonmatching helper bodies at main `e047157d711652a5624012f0da7d6911ce086718`.
It changes no production, tests or prior evidence. Source correspondence is a
pending independent disposition proposal under C7 and D9; it is not a semantic
execution result.

## Four complete correspondences

The complete Effects and lowering ordered-child helper forms match C7 after
only two explicit parameter renames: `ids` to `children`, and `types` to
`type-table`. The checker verifies those parameter vectors and the entire form
tail, preserving all loop bindings, indexing, defaults, order and return values.
This establishes a complete source correspondence with changed parameter names;
it does not silently treat all argument lists as interchangeable.

The complete Effects and lowering List-predicate call-ID forms match C7 after
renaming only their `sse-ids?` call targets. The three actual ID-validator bodies
are themselves identical, including parameter lists and every guard. The
source caller widths 2, 1 and 6 and validator string length 71 remain unchanged.
This supplement neither removes nor classifies the validator's width guard as
accounting; it preserves the exact source and arity/shape checks.

The machine record pins all source files and complete forms, each explicit
rename, the three complete dependency bodies and prior-source conservation.
The original six difference keys are conserved: four have the complete
correspondences above and two retain explicit structural differences. These
are source facts, not additional accepted helpers or milestone counts.

## Remaining structural difference

C7's `sh08-ft-all-true?` uses a recursive self-call and checks the first value
against true. Effects `body-all?` and lowering `p15-s23-typed-all` use loops and
reverse the equality operands. The latter two complete bodies match after the
explicit `checks` to `values` parameter rename. The checker also pins both
complete expected recursive and iterative forms; it does not infer semantic,
stack, callback or performance equivalence between them.

## Validation and remaining obligations

The local Clojure checker reads with evaluation disabled, confirms every
selected form is unchanged from the prior assessed main and validates the
consumed record's original body hashes. Ten source mutations are detected:
unknown-type fallback, first/last selection, append order, incomplete parameter
mapping, child width, source syntax ID, argument IDs, vector guard, string
length and ID width equality. It evaluates no Gravity code and leaves Bootstrap
unloaded. Generator and file/form hashes are in
[predicate-helper-correspondence.edn](predicate-helper-correspondence.edn).

Source checks, exact six-key conservation, structural docs, full-language
roadmap and whitespace pass. The original reader/source inventory and its
counts remain unchanged. Actual predicate derive, Effects joins, lowering,
runtime, coherent field forgery, late-invalid and opaque zero-callback controls
remain unrun and open. Independent source disposition remains pending; the
stopped PR569 successor review is not retried or replaced. No whole-helper
semantic equivalence, source authentication, integration, ACC or PR502 closure,
release or self-hosting authority is claimed.
