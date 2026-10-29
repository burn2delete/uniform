# First PMK operation input binding

The first existing C7 owner selector is
`sh08-private-map-keys-preparation-semantic-cells`. It executes
`sh08-pmk-type-view` and `sh08-pmk-index-forms` through the normal `invoke-c7`
compiled-plan bridge. Its single test includes accepted type overlays and form
indexes, malformed tables, invalid keys, late opaque values, callback guards,
duplicate form IDs and malformed source forms. The 11,000-entry prior table
exercises removal of the old reservation refusal. These are real owner calls
when run; the input-binding tests do not execute them.

`gravity.pmk.first-request/prepare` revalidates the complete fifteen-selector
adapter contract before projecting exactly this one selector. It retains the
full parent plan and digest, owner APIs, source/checkout/runtime/input identities,
CWD, seven-element normal child argv and four distinct output paths. It does
not mutate the historical nine-selector packet or weaken the complete closure
contract to admit a subset. The original public alias-to-PMK edges in the
three-owner selector remain in that parent contract; they are outside this
first execution selection.

`bind-inputs` checks an independently reviewed expected request against the
observed tuple before calling a normal owner's separate binding verifier.
Required inputs include ordered classpath hashes, Java and Clojure executable
hashes, config/environment digests, the exact controller and all eight port
identities, explicit guard roots/policies/classes/order and participant coverage,
whole-operation deadline, capture size, descendant-containment evidence,
terminal/release ordering, new-operation history and absent output observations.
Missing inputs, a denied/pending/uncertain/executed operation or an original
handle refuse before that verifier. There is no default root or controller.

This is a finite input-binding repair, not a new production execution controller.
The namespace provides no production verifier, lease/process port or CLI. Even a
successful consistency check returns `:execution :unrun`, unauthenticated, and
ready only for the owner's platform assessment. Supplied hashes and a verifier
return value cannot themselves authenticate live roots or contain descendants.
A real supported implementation and independent operational evidence must bind
the tuple; a synthetic verifier used in controls supplies neither. The existing
SH-01 broker does not establish strict descendant containment, and a bare
`:dev-test` child supplies none of these outer safeguards. No H/root adoption,
live acquisition, process/holder probe, source observation through a compiler,
unknown-operation replay or request submission is part of this repair.

The current missing facts are the existing authorized PMK outer controller,
its guard root/participant coverage and acquisition order, its supported
containment/whole-watchdog/capture/release implementation, and independently
observed fresh operation/output state. Until these are available, the genuine
selector remains unsubmitted. A 2 GiB heap request is only one field and cannot
satisfy these requirements. R4/22232 and the specific Reader/DSE actions remain
separate preserved history; shared component names prove no interference or
release either way.

Compiler-free tests use synthetic adapter results and binding-verifier ports to
exercise projection, missing fields, tuple drift, wrong owner, output aliasing,
history refusal and verifier failures. They assert zero verifier calls on early
failure. No owner namespace is required, no lease/start/termination port is
available, and no actual platform submission occurs. Run them directly:

```bash
clojure -J-Xmx256m -Sdeps '{:paths ["bootstrap/clojure/test"]}' -M -e '(require (quote gravity.pmk.first-request-test)) (let [r (clojure.test/run-tests (quote gravity.pmk.first-request-test))] (System/exit (if (zero? (+ (:fail r) (:error r))) 0 1)))'
```

The governing evidence distinctions follow D1, D8, D9, TEST1 and the development
verification guide. This repair grants no PMK semantic/native, integration,
migration, self-hosting or release acceptance. Existing dependent PR534 source
composition remains required before integration.
