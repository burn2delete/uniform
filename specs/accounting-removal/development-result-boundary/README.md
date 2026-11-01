# Normal development completion delivery

This finite SOURCE/DATA-IO increment starts from qualified accepted
`ce9753b7f7d5f985e6440d5d10a1d26c841364f5`, tree
`4929eae2c3acd33aca2c4728d22713044244efb6`. It changes the existing
development-test-runner sink and normal process report boundary. It does not
import or modify held startup36, held de37 configuration, or OWN partial
`3d1f1fd6a3aebb22c685f9f367884e4fe6f06423`. Original PR600 remains complete.

## Gap and existing APIs

The normal `-m gravity.development-test-runner` path leaves
`*machine-result-sink*` nil. Its integer exit and captured text are not its
closed structured completion record. The warm worker's binding and the
different warm-batch/timing report paths are not reused here.

The normal parent's existing `run-namespace-process` now optionally accepts:

```clojure
:development-result {:source-commit "<40 lowercase hex characters>"
                     :source-tree "<40 lowercase hex characters>"
                     :configuration-sha256 "<64 lowercase hex characters>"
                     :limit-bytes <existing caller-declared capture limit>}
```

This mode requires the existing explicit exact-test-vars selection. Names
are qualified under the job's actual namespace, sorted as the child runner
does, and must be unique. It retains the existing selector API, job/owner
guard, explicit resource class, caller-supplied command prefix and CWD.
No ALIAS names, catalog entries, worker, wrapper or controller are added.

The parent creates a fresh temporary directory, a fixed result.edn path and a
new request ID. The request binds those expected namespace/qualified vars/
fail-fast values and supplied source/configuration identities. It adds one
`--machine-result EDN` transport argument to the normal child command. The
caller cannot supply the internal delivery object through run-namespace-process.
Existing commands and schemas are unchanged when the option is absent.

`run-cli!` binds its existing sink to a once-only file delivery, composing any
already bound sink. The original closed `:gravity/development-test-result-v1`
map is constructed at the original point after run-selected-tests returns
its final counters, including fixture/finally effects. No guessed assertion
count or captured text substitutes for that record. The record's keys remain
exactly schema, authority, authoritative?, selection, summary, outcome and
exit-code. The existing -main and integer-return behavior remain.

The transport envelope is generic, not an ALIAS semantic report:

```clojure
{:schema :gravity/development-test-result-delivery-v1
 :request {:id "<fresh request ID>"
           :source {:source-commit "..." :source-tree "..."
                    :configuration-sha256 "..."}
           :selection {:namespaces ["gravity.example-test"]
                       :test-vars ["gravity.example-test/a"] :fail-fast? true}}
 :result <unchanged closed development-test-result-v1 record>}
```

The sink compares actual selected names to the requested selection, serializes
UTF8 EDN, checks the caller's capture limit before file creation, and writes
once with CREATE_NEW/WRITE/NOFOLLOW_LINKS. Duplicate callbacks, existing output,
symlinks, oversized delivery and original callback/IO errors refuse. It does
not overwrite, retry, switch output paths, or turn a callback error into success.
The inherited sink receives the same raw result object; its errors propagate.

After the normal actual Process/token/result path completes, the parent reads
at most the declared bytes plus one excess-byte observation, rejects malformed
UTF8 and requires exactly one EDN form. It validates envelope/request equality,
the closed result shape, exact namespace/all qualified vars/fail-fast, all four
nonnegative integer counters, full selected test count, outcome and exit code.
Early fail-fast records are retained as incomplete refusal rather than full
completion. A complete failing record can remain a failed completion.

It also requires agreement with actual process exit/status, exited root,
not-live cleanup root, terminal tracked handles, complete untruncated captures
without errors or forced closes. It preserves raw process fields and unknown
descendant coverage. The normal returned report adds development-result,
development-result-request, development-result-file and
`:development-result-binding-status :supplied-data-matched`. Failed reads or
validation throw stable SH01-DEVELOPMENT-RESULT errors with the delivery,
available record and original process map; IO/parser errors remain as causes.
Upstream launch/wait errors and fatal/interruption behavior are unchanged.

## Authority and custody limits

The source/configuration fields are compared to the parent's supplied binding.
They are not effective-runtime attestation, semantic authenticity, source
authentication, cache admission or acceptance of held de37. UUID/file freshness
is request correlation, not a signature. The caller still owes its actual
parent/child argv, source/configuration verification and independent composition.
Report-enabled argv has new delivery arguments and requires fresh exact pins;
the old 16-argument DATA proof is not relabelled as this operation.

This is an internal completion transport, not final report publication or a
one-shot release guarantee. The parent owns the retained temporary directory
and result path, including partial failure output; this patch does not delete
or recover them. The caller owes safe directory lifetime, failure artifact
custody, final output publication and release/quarantine observations. Native
filesystem read/write/close latency and startup are not hard-bounded by this
code. The caller declares the existing host capture limit; no new allowance
or compiler input/work quota is introduced.

ACC06's post-token custody remains unchanged. Pre-token start/observer/retention,
supervisor death, parent exit while quarantine exists, async escape, manual
development-loop leases, unknown descendants and quarantine recovery remain
open. The normal report is consumed before the existing with-lease disposition;
it does not attach the actual release receipt or keep a parent alive. No new
finalizer, broker rule, lock controller, keepalive or runtime admission is added.

RV's distinct semantic collector remains RV-owned and is not imported. All
startup/configuration reviews, PR645 integration and prior publication/merge/
unknown-operation holds remain unchanged. This candidate stops before review
dispatch; Accounting decides distinct eligibility. No publication, CI, genuine
operation, target namespace or held retry is authorized by these controls.

## Compiler-free evidence

The directly run check.clj reads selected source forms with read-eval disabled
and evaluates only the relevant helper/caller/CLI forms in private model
namespaces. Namespace loading, selected test execution, process creation,
wait/capture, broker acquisition/disposition and policy discovery are inert
ports. The actual file sink, bounded reader/validator, command construction
and final result constructor operate on supplied DATA and owned temporary IO
fixtures. No real dev/parallel runner namespace, selected test, Bootstrap or
Gravity runtime is loaded; no child, lease, lock or process probe occurs.

Final owner model: 7 tests / 122 assertions, zero failures/errors, core-JAR-only
Clojure 1.12.5/OpenJDK 26.0.1 tooling JVM, heap256MiB. Five coherent source
mutations separately refuse: unbound sink (one model error), missing request
equality (six failures), missing EOF check (one), missing full count (four),
and missing root-exit check (four), all natural exit1. Source controls conserve
the closed machine-result constructor, default execution/parser behavior,
accepted selector constructor and original normal operation after only the
new opt-in branches are removed. Other source forms, token/finalizer/broker
and all unowned paths are unchanged. Language-boundary evidence is separate;
counts are not summed. Lint has no new findings; the five shared-file warnings
are inherited. New checker formatting passes.

Preparation logs retain an initial reader failure on the source's ::test alias,
then a model-only dynamic-var setup failure. Using the actual dynamic def forms
corrected the checker; neither failure ran a target or changed production to
weaken a control. Earlier successful model logs remain at their original scope.
