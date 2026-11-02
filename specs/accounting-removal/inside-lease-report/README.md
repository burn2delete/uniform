# Synchronous inside-lease report port

This source increment starts from accepted `d666cea2` (normal completion and
observed broker disposition lineage `eefd` / `ce9753b7` / `47b44ed9`). It adds
one optional port to the existing `run-namespace-process` caller:

```clojure
{:resource-broker existing-broker-options
 :development-result existing-source-and-completion-request
 :inside-lease-report! synchronous-owner-callback}
```

The callback must be callable and requires both existing resource and completion
options. The namespace guard still runs first. No blocked namespace is enabled,
no planner/catalog claim changes, and no guard flag or private lookup is added.

## Observation and ordering

The callback runs once, on the acquiring/calling thread, inside the existing
operation. On success it runs after actual process capture, strict child result
read and normalization, before returning to broker terminal assessment and
release/quarantine. On an operation error it runs before that original error is
rethrown through disposition, with only facts actually obtained so far.

The observation contains `:job`, `:tag` (`:returned` or `:threw`), `:result`
(the actual normalized result on return, otherwise nil), and `:error` (the
original Throwable on throw, otherwise nil). `:delivery-request` appears only
after delivery preparation returned; `:process-result` appears only after the
process-result call returned. Neither partial capture nor an error is promoted
to a normalized passing completion. Original objects are retained locally.

The callback return is ignored. The original result identity is conserved when
no parent-report augmentation is requested. Callback errors cannot prevent
normal broker disposition. Callback-enabled calls use d666's observed arity
even without a final parent report, preserving original errors if release also
fails. Fatal causes take precedence; original and callback errors remain as raw
suppressed objects or in the suppressed local observation context. Interrupt
status observed at this boundary is restored after disposition, including when
release consumed it. No release or callback is retried.

The d666 final report remains after actual release/quarantine. Semantic failure,
inside-report failure, release failure, and final-report publication failure
remain distinct. No closed child or parent record is changed. Calls without the
new option retain their original operation, guard, result and broker path.

## Owner bindings not supplied

The port supplies no guarded RV route, canonical heavy lock, authentic private
SecureDirectoryStream, byte admission, child-local observations collector,
request-correlated semantic receipt reader or effective configuration. RV owns
those source bindings. A child completion record cannot stand in for the live
child observations atom or semantic authenticity. ALIAS and Reader remain
conditional consumers, not automatically admitted callers.

Startup36 and configurationde37 reviews remain separate and unaccepted inputs;
neither candidate is imported. Native start before return and actual process
coverage remain outside this source increment. In-JVM quarantine cannot survive
parent exit. No worker, controller, finalizer, keepalive, durable custody,
automatic clearance or global process registry is introduced.

## Controls and scope

`check.clj` reads the actual caller/helper/publisher forms without evaluating the
runner namespace. It loads only broker tooling, replacing acquire/release,
process and completion ports with inspected inert models. Fake Process methods
refuse waiting, probing and destruction. Owned temporary DATA file writes test
callback IO before release and final report IO after release; they are not real
semantic publications or operation receipts.

The controls cover same-thread order, exactly once, result/error identity,
partial facts, fatal/interruption/suppressed precedence, explicit interrupt
consumption by release, disposition on callback failure, quarantine, unchanged
default behavior and guard/acquisition refusals. Read-only source comparison
conserves all other forms/unowned bytes and the original operation after removing
inert fact capture. Coherent source negatives omit callback, replace result,
swallow callback error, forge an error result and omit interrupt restoration.

This frozen source/model candidate awaits independent review eligibility through
Accounting. It performs no real child, lease, lock, process probe, target runtime,
Bootstrap load, genuine operation, publication, CI or integration.
