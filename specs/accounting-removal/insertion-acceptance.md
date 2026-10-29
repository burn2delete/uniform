# ACC-05 inferred insertion acceptance preparation

This package covers draft PR535's two-file residual: three C7 inferred Set
insertion checkers and their scoped throw consumer. It retains the existing
ACC-05 task identity. It grants no semantic, initializer, Native, integration,
self-hosting or release acceptance.

## Demonstrated preparation gaps and correction

The actual SH08 `invoke-c7` calls `compile-plan`, which selects the C7 source
through `c-backend-stage2-plan-emitter-source-rule!` and compiles it with
`p15-s23-stage2-compiler-artifact-plan`. It executes through
`p15-s23-stage2-runtime-execute-function`. Loading its test namespace requires
`gravity.bootstrap`. The development runner's `load-selected-namespaces!`
precedes `select-test-vars`; even `--list` loads that namespace. The runner's
exact selector checks are useful but do not bind an expected checkout/source
tuple before that load. A namespace-load failure is not an insertion refusal.

`gravity.types.insertion-acceptance` fixes the preparation gap without modifying
that shared runner. It reads actual test definitions without evaluating them,
checks exact owner/selector routes and returns the existing runner command as
data. It never requires the owning test namespace, compiles Gravity, launches
the command, acquires a broker lease, replaces Vars or inspects processes.
`attribute-result` distinguishes a supplied setup/load failure from a supplied
test result; all remain explicitly unverified reports. It authenticates no
execution and cannot turn an injected model or CI success into acceptance.

`snapshot!` binds HEAD/tree, observed origin/main/tree, deps.edn and a
conservative complete tracked bootstrap/contracts file-hash map, including the
bootstrap loader's load-order manifest, component/data shards, fixtures,
Gravity sources and tests. It also includes this adapter and its controls even
before they are tracked. `prepare!` rereads that closure before and after
reading the actual test definitions. Inputs are root-relative; no original
checkout or private receipt path is hardcoded. A trusted caller supplies the
expected exact tuple. These checks do not establish hostile same-user isolation,
external classpath/runtime identity, clean-source admission or future freshness
after the function returns. Renew preparation at the actual acceptance revision.

## Actual owner selectors and consumers

All lane definitions are executable data in `insertion_acceptance.clj`.
No prefix/regex or whole-namespace selection is used. The first four lanes use
`gravity.self-hosting.sh08-function-call-type-test`; composition uses
`gravity.self-hosting.sh01-stage2-runtime-iteration-test`.

| Lane | Actual selected tests | Owning consumer and refusal coverage |
| --- | --- | --- |
| inferred | `sh08-string-set-insertion-development-kernel`, `sh08-keyword-set-insertion-development-kernel`, `sh08-symbol-set-insertion-development-kernel`, `sh08-insertion-inferred-call-ignores-unrelated-function-count` | Actual three inferred checkers via `invoke-c7`; descriptor/type/source/result parity, arity/ID guards and forged operation rejection; unrelated function count no longer supplies an inner charge. |
| outer | Three `sh08-{string,keyword,symbol}-set-insertion-throw-consumer-development-kernel` tests | `sh08-tc-products` -> `sh08-tc-products-with-source-forms` -> three inferred checkers. Existing outer prefix/owner cutoff, stale equality identity, malformed argument list, unknown type and invalid member controls remain. |
| identity | Keyword and Symbol insertion ID-guard kernels, `sh08-symbol-set-carrier-development-kernel`, `pl-semantic-native-ingress-and-rejection-diagnostics` | Full native/member validation before callbacks, carrier schema/version and descriptor/source parity; no budget-only guard replaces these requirements. String ID negatives also occur in its inferred kernel. |
| consumers | `pl-insertion-contract-parity-and-coherent-stale-fields`, `pl-insertion-effects-rederive-and-reject-coherent-stale-fields` | `pr359-prepend-case-data`, C7 `sh08-pl-inferred-call-valid?`, Effects `body-va-context`/`body-pl-call-valid?`, emitter `p15-s23-typed-compile-node`; coherent stale descriptor/fact/row/cache negatives. |
| composition | Three `current-gravity-{string,keyword,symbol}-set-insertion-authenticated-composition` tests | Real C5 construction, SH07 development session/replay, B47 context, C7 construction/verification, C8 sealing/effect verification and emitter/runtime consumers on existing fixtures. These are genuine future compiler execution, not cheap preparation controls. |

The C7 production caller is
`sh08-ft-function-type-core-artifact-with-imports-and-allowance`, which obtains
`direct-throws` with `sh08-th-products` then calls the changed scoped consumer
when inference has no diagnostics/unknown nodes. The wrapper
`sh08-tc-products` supplies nil source forms in normalized consumer tests.
The complete source-form production route therefore still needs the genuine
composition panel; direct checker tests alone cannot establish it.

Module initialization is a separate downstream boundary. The actual runtime
APIs are `p15-s23-runtime-initializer-work`,
`p15-s23-runtime-initializer-type?`, `p15-s23-runtime-initialize-module` and
`p15-s23-runtime-execute-closed-plan`. The existing
`current-gravity-module-literal-runtime-development-kernel` exercises constructor
identity, exactly-once initialization and malformed module plans. It does not
call the three changed inferred checkers. It is not substituted for insertion
composition, and this adapter intentionally refuses a runtime-initializer
consumer under the outer lane. No new initializer/PMK/domain work is imported.

## Bound source and reusable evidence

Inspection bound candidate `51931bc8c974505fb054d4be349f1d4fdb34779e`, tree
`c019b136627dc40438b0fdc1eb39c1fedffaeae0`, and remote-readback main
`6276c94b0b753c888ef8fecc794bcc81245ea3b3`, tree
`9f464ed0f03866bd4978e4213a488f3b1ae48683`.
Main's Reader dispatch change is absent from this candidate. A later composed
candidate must renew affected dependency preparation and acceptance; this
inspection is not a merge or an assertion that current main was tested.

C7 SHA256 remains
`1c0242ed45983fc974f1aa66b95049b2d47b4ddb7303198bbd631615022ce106`;
SH08 test SHA256 remains
`20d071d161ef3b1295e509de3fdba558e692707a1a8894a23d28d1d1375c2a86`.
The independently reviewed source/control receipt
`b44bd7977d4c6c698c15560170e9bc6601ffc7b9200a299e76b56dc1283ccc94`
and draft publication receipt
`0d98ccfc9ba448437f4363f31f91f9ecd9ee636de42143320923afe45b584276`
retain their exact static/CI scope. Original cheap control results are reused;
no original unknown operation or stopped metadata write is replayed.

## Controls and present execution prerequisites

The focused adapter control namespace is compiler-free:

```text
clojure -J-Xmx256m -M:dev-test --namespace gravity.types.insertion-acceptance-test
clojure -J-Xmx256m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

Positive synthetic controls exercise exact route/command preparation and honest
failure attribution. Negatives alter/miss source identities, selectors,
consumers, definitions and failure phases. Actual-source preparation separately
reads every selected real test definition and binds current source/resource
bytes, while never executing those tests. These are adapter/source controls,
not language semantics tests or compiler authentication evidence.

| Prerequisite | Current classification and exact consequence |
| --- | --- |
| Exact source/consumer selection before owner load | Missing adapter corrected here; compiler-free controls and independent review required for this new adapter only. Ordinary development needs no fresh migration fingerprint. |
| Current execution hold | Present explicit instruction: no semantic/Gravity/Native launch is authorized. Preparation may proceed; the first genuine action after explicit release is the exact outer lane command produced by `prepare!` on its then-current source tuple. |
| Runtime/emitter source selection and effective classpath | Real presently applicable safeguard. The owner chooses its genuine emitter source rule; effective JVM/dependencies/resources and actual host restrictions must be reviewed for that invocation. This adapter does not invent a termination or capacity result. |
| Module initialization and broader consumer composition | Existing APIs/tests identified above; their actual results remain unrun here. Require only the affected route for the claim being made; no blanket initializer closure is inferred. |
| Historical UNKNOWN deliveries/session22232 and denied operations | Preserved non-executable history. No present interference is established by this source inspection; they are not a blanket gate and are not probed, replayed or cleared. |
| Current-main composition | Real affected integration requirement before merge; main drift is explicit. CI on the earlier exact branch does not validate a newly composed source. |

No semantic, Native or initializer acceptance result is supplied by this package.
