# PMK preparation acceptance adapter

This implements the source/selector attribution part of existing ACC-04 for
[draft PR534](https://github.com/burn2delete/uniform/pull/534). It introduces no
new accounting task or execution authority. The semantic source stays at
`c98de34ae6b7f2f38b4276889bc14dcdc47a3de2`, tree
`e3c442e055c9a6db468190fb18f9636517668400`; the adapter adds no Gravity behavior.
The machine-readable input and selector contract is
[pmk-acceptance.edn](pmk-acceptance.edn).

## Concrete gaps and implementation

The existing development runner's `--list` path calls
`load-selected-namespaces!` before selection. SH08 requires Bootstrap; its
`invoke-c7` forces a delayed compiled C7 plan. Listing that namespace is not
a nonloading inspection interface. Its normal machine-result sink records
selectors, counters and outcome, but no source/resource tuple. These are the two
gaps addressed by `gravity.pmk.acceptance`.

`capture` reads files, Java/Clojure/JAR identities and classpath resource URLs.
It neither loads Bootstrap nor launches Git, a process, a broker or a compiler.
It hashes a sorted path/SHA256 inventory of `deps.edn`, the Clojure bootstrap
source tree, Gravity source tree, SH08 fixtures, the owning test, the SH09
Effects leaf bridge used by the native controls, and development runner.
The adapter's own implementation bytes/resource are included as well.
Preparation recomputes that inventory digest and checks its exact count,
the actual `:dev-test` alias/main options, ordered classpath roots and ordered JAR
identities. Java version/vendor records are not binary runtime attestation.
Classpath resources for every captured bootstrap source and all four adapter/test/runner
resources must resolve exactly once to the path derived from the separately
observed checkout root. Changing both the expected and actual URL cannot select
another root. Local absolute paths stay
in memory/private prepared inputs; the public contract contains hashes only.

`prepare` compares that snapshot with the pinned contract and separately
supplied Git before/after records, reads top-level source forms without eval,
resolves actual `deftest` selectors, follows their local helper calls and
checks the declared owner APIs and production caller edges. A pinned closure
digest prevents dropping or substituting valid selectors, roles, APIs or edges;
all selected roles/APIs survive in the plan. Its argv is a
prepared development command, not an invocation. Git records are caller
observations; this adapter does not independently attest a Git commit or grant
admission. Missing/stale records reject rather than infer identities.

`attribute` accepts a future normal runner's `*machine-result-sink*` value and
the before/after plans. It checks source stability, exact namespace/selectors,
normal non-authoritative schema, the prepared non-fail-fast invocation, integer
counters and outcome/exit consistency.
It preserves a failed result as failed. It rejects a host/process receipt as a
test result, and never authenticates supplied counters or claims semantic
acceptance. The future admitted caller must obtain the sink from the normal
runner, supply fresh Git bookends and call `capture`/`prepare` again afterward.
No owner lookup or injected fake compiler implementation is provided.

## Actual selector and consumer closure

All selected tests are existing definitions in
`gravity.self-hosting.sh08-function-call-type-test`. The EDN names them exactly.

| Selection | Actual owner/consumer and assertions |
| --- | --- |
| Preparation semantic cells | `sh08-pmk-index-forms` and `sh08-pmk-type-view`: removed reservation widths, full table/form validation, duplicate/invalid IDs, overlay rules and opaque callback guards |
| Prior-view controls | `sh08-lq-step` and `sh08-lq-derive`: actual prior inference, semantic malformed-table `C7-VERIFY`, unknown/deferred/Never cases, preserved downstream report/refusal |
| Prior other argument | `sh08-lq-step`: actual normality of the other private argument, unused Never arguments and deferred inference |
| Throw source index | `sh08-tc-products-with-source-forms`: active scoped throw caller, valid wide vectors and equally late duplicate/invalid forms; genuine downstream budget refusal survives |
| Call-constraint consumer | `sh08-ft-call-constraints-with-context`: real accepted and refused reports, early source mismatch without a fabricated report and unchanged binding/constraint outputs |
| Current source composition | Actual C3 -> SH05 -> SH06 -> SH07 request, C7 type artifact and `sh08-verify-function-type-result`: authentic input, descriptor/row/fact cross-family forgery and fresh rejection; paired Q bytes are parity only |
| Three recursive-validation native tests | Real C7/Effects/emitter validators: native backing/view legality, foreign/custom collections, inert/cyclic metadata, spine/view cycles and callback preflight. This is independent supporting coverage, not PMK-specific native ingress or whole native acceptance |

The eight production edges bind indexing to both throw/call-constraint callers,
type-view to LQ derivation and the actual prepared inference pass, and the
unchanged type overlay/inferred-call owners. Quoted data is excluded; owner
symbols must occur in actual named invocation sites. This call-site closure is a
static wiring check, not proof a branch executes or validates its inputs.
ANE classifier/domain reports and escaped Effects cells remain unchanged and
outside this PMK preparation package.

## Dependency applicability and execution prerequisites

The contract records observed main
`6276c94b0b753c888ef8fecc794bcc81245ea3b3`, tree
`9f464ed0f03866bd4978e4213a488f3b1ae48683`. Unlike the earlier `abc4ed1c`
bookkeeping-only delta, this main changes `gravity/reader/dispatch.clj` and
source/consumer harnesses. Its Clojure source tree differs from the candidate.
No old semantic/native evidence is promoted across that changed dependency.
The exact c98 reader/conservation receipt (3 tests/1,807 assertions), independent
static review and remote changed-lint pass retain their named original scopes.

| Remaining prerequisite | Current classification |
| --- | --- |
| Exact selector/resource/result adapter | Implemented here; compiler-free controls check wiring/attribution only |
| Explicit hold on Gravity/Native/semantic execution | Presently applicable parent restriction; prepared argv does not override it |
| Heap, watchdog, capture and authentic runtime/source context for a real call | Actual host/admission safeguards; no capacity or broker state has been observed or acquired |
| PMK-specific native ingress plus actual downstream diagnostics and source composition | Genuine unrun acceptance; shared native support selectors do not close it |
| Original22232 and historical process/driver failures | Preserved, unrepeatable evidence. No demonstrated current interference was established; UNKNOWN is not a blanket gate or proof of free capacity |
| Stopped PR-description update | Remains stopped; this package does not retry or reroute it |

The next genuine action after the explicit execution hold is lifted is the
normal development runner on the exact
`sh08-private-map-keys-preparation-semantic-cells` selector, with this snapshot,
normal source/emitter context, supported host safeguards and actual sink/result
bookends. Then exercise the named LQ, throw, call-constraint and source-composition
consumers and applicable native controls. No broad main-freshness rerun is a
prerequisite; compose changed dependencies and validate that composition before
integration. The whole ACC-04, ANE migration and release remain open.

## Compiler-free controls

Run only the support namespace, which does not require Bootstrap:

```bash
clojure -J-Xmx256m -Sdeps '{:paths ["bootstrap/clojure/test"]}' -M -e '(require (quote gravity.pmk.acceptance-test)) (let [r (clojure.test/run-tests (quote gravity.pmk.acceptance-test))] (System/exit (if (zero? (+ (:fail r) (:error r))) 0 1)))'
```

Controls use synthetic source/test definitions and receipts to reject wrong or
missing Git identity, changed dependencies/runtime, missing/shadowed resources,
paired wrong resource URLs, dropped valid closure items, quoted decoy calls,
wrong selector/consumer, source drift, wrong namespace, malformed counters,
authority mislabelling and a failed/incomplete run relabelled as passed. They
exercise the adapter's observable contract, not a model of Gravity semantics.
