# Runtime: hosted seed invocation and execution records

## Boundary and contracts

D1, D2, D3, D8, D9, C1, C15, R1, R4, R11, R12, BOOT8 and TEST2 govern
this increment. The integration base is main
3a4d5ea11c946bd374dfdfa09feaa1d78aa6c582, tree
844c7dc075bc59fa95259f7bff0bfacdf6201b06. This source excludes the unmerged
effects and diagnostics candidates; their CI failures remain separate history.

Four existing bodies move from
bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00593-p15-s23-stage2-runtime-execute-function.clj
into gravity.runtime: execute-function-scoped, execute-function, artifact-invoke
and execute-plan. They own function lookup, argument/recur arity control, module
dispatch, runtime-artifact invocation and the existing stdout/result record.
The stage imports no namespace. Its contract declares each API's exact operation
keys/arguments and the namespace's input/output artifacts, diagnostic ownership
and unsupported boundary.

Eighteen explicit seed Var ports retain native scope checks, module entry,
registered Reader entry, B51 entry, binders, instruction execution, recur control
and diagnostics. They are trusted implementation dependencies, not authority
proofs. The Bootstrap wrappers preserve original arities, private scoped entry
and dynamic Var interposition. Remaining kernel source is byte-conserved.

Scoped entry still checks the native scope before module selection and function
entry authorization, before lookup/binding/execution. Module-scope routing still
passes nil to the B51 gate while retaining the selected scope for instruction
execution. execute-plan retains its separate legacy main/arity/instruction-runner
protocol; it does not acquire a new function-entry scope guard. No source/Core
admission, public plan authentication, provider grant or runtime-family selection
is supplied by this API.

## Actual development evidence

The before producer executes at clean original main
f485e117186084a5ab4a04358831f4f93e0dab0a in the original imports checkout.
The after producer executes with working extraction and Bootstrap wiring over
that base. These are working-source observations, not execution at a later
committed head or migration authority bookends.

Both actual hello source spellings compile to Stage2 plans and execute through
the existing runner. The plan record and direct function call both print
Hello Gravity followed by newline. Both preserve plan ID
sha256:34670558f69c8d83bfa8c73dc0d2135f47c29211caaac6dd9ad7c22889837bf9.
The actual current Gravity runtime source also compiles; its formatting function
is invoked through the existing runtime-artifact route and returns prefix A B
followed by newline. This is hosted seed execution, not generated native code,
backend support or a canonical runtime manifest.

Unknown function, wrong function arity, missing runtime artifact/function,
wrong artifact arity and missing main reject with the existing
L2-UNKNOWN-CORE-FORM, L2-FUNCTION-ARITY, P15S23X002 and L3-UNKNOWN-ALIAS rules.
Twenty complete and twenty captured serialized-text pairs match, including
full source/plan data, execution records, output and full negative diagnostics.
All source hashes and declared plan IDs recompute through the original canonical
preimage protocol. No identity encoding changes. Source paths remain separately
recorded; raw seed plans may supply no diagnostic path, which remains unavailable
rather than being invented by this extraction.

The conservation check preserves all four bodies and compatibility arities after
explicit callback bindings and verifies the remaining kernel suffix byte for
byte. Initial temporary extraction assertions stopped before project writes:
the first text splitter also matched a defn example inside a diagnostic string.
A startup attempt with a missing newly required namespace failed before any
Gravity compiler or runtime program executed. The logs/reports are retained.
The corrected top-level extraction produced the reported successful after run;
no failed semantic execution, denied native action or source-authority pin was
retried or renewed.

The branch then fast-forwards to current main 3a4d5ea1, preserving working runtime
source and adding only the already integrated MIR changes. The runtime component
itself is unchanged in that incoming main delta. Fresh focused tests exercise
the composed source; the original twenty paired records retain their original
production context.

## Tests and registration

The focused suite covers real paired source execution/plan identity/diagnostics,
actual Gravity runtime formatting, native denial before hostile plan lookup,
registered/module/B51 gate ordering, denial before binding, module dispatch,
artifact-plan removal with context retention and private/public Var interposition.
Mock scope markers exercise control flow only; they are not native handles or
admission proofs. Existing exact binder/recur and artifact-str regressions run
without invoking historical authenticated compositions.

The exact EDN/JSON association records gravity.runtime / sh-runtime / SH-19,
the real Bootstrap consumer and its standalone pipeline test. Ownership tests
reject missing/forged namespace, owner, consumer, test and registration entries.
Primary component counts and the existing leaf catalog are unchanged.

The focused suite passes 5 tests / 88 assertions at current main with working
extraction and registration. Ownership passes 23 / 854 and language passes
6 / 11. Existing exact binder/recur and artifact-str regressions pass 1 / 3 and
1 / 6 respectively. These repeat only affected ordinary kernels, not historical
authenticated source/native compositions.

Full changed-file contextual lint, docs/roadmap/governance checks, independent
acceptance, final exact-head CI and clean current-main integration preflight
remain required before integration.

## Unsupported boundaries

This is temporary Clojure invocation machinery for existing compiled plans.
Gravity retains language/runtime semantics; existing native/module/Reader/B51
holders retain authority. The API does not authenticate arbitrary native plans
or callback maps, classify runtime families/services, supply full R1/R4/R11/R12
conformance, grant effects/capabilities, sanitize public output, or perform
release, migration, self-hosting, seed retirement or performance certification.
Compiler legality remains a compile-time obligation. Existing statuses and
execution records confer no additional authority.
