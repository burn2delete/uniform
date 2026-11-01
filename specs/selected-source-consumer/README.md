# Portable selected-source consumer controls

Owner: ACC13 successor. Scope: prepared Reader -> Syntax -> SH05 -> C5 harness,
not execution or authentication acceptance. The governing contracts are
[C3](../../docs/phase-06-compiler-architecture/082-c3-syntax-object-model.md),
[C5](../../docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md)
and the [selected Syntax source/diagnostic consumer contract](../../docs/end-to-end-contracts.md#selected-syntax-source-and-diagnostic-consumer-contract).

This integration contains only the portable library, its cheap controls and
evidence documents. The selected Syntax implementation remains in draft PR544;
none of its compiler, source-binding, load-order or Gravity source changes are
included. Existing source pins and authentication policies remain those of main.
`exercise!` is prepared for a future separately accepted implementation/runtime
tuple; its presence does not make the selected route available or authorize it.

## Implementation

`gravity.resolve.selected-source-harness` is a test-support library. Requiring
it does not load Bootstrap, acquire a lease, launch a process or call a source
constructor. It has no main entry point, parent controller or automatic run.

`prepare-inputs!` takes an absolute checkout root and an explicit `{:head ...
:tree ...}` tuple. It checks the clean Git identities before and after reading
the existing 192-byte fixture, Reader and Syntax. It derives paths from that
root and retains exact bytes, strict UTF8 text and hashes; no original checkout,
control directory, branch name or preserved candidate is hardcoded. It reads
files and Git state only. Private prepared inputs should remain local.

`exercise!` is a future, explicit consumer function. It accepts an owner lookup
and prepared inputs. The normal lookup, `loaded-owner`, resolves already-loaded
`gravity.bootstrap` Vars; it does not load Bootstrap. The caller must supply
the reviewed runtime/emitter context and actual admission before invoking it.
Input files and exact clean source identities are rechecked before and after.
Local untracked evidence also makes the clean-source check reject.

The function retains the genuine five-argument C5 constructor, once, with
source path/text, module inputs, Reader path and explicit invocation. The
constructor owns actual C2/C3/SH05/SH06 construction. The harness checks normal
public verification, selected implementation authentication, six semantic
projection mutations with normal envelope/identity functions, original Reader/
parent/provenance negatives, physical binding/context substitution, artifact-only
selection and selected registered-projection rejection. It does not replace
owner implementations or supply authoritative reports.

Source assertions join returned syntax to actual C2 seeds, form records and
token ranges; compare UTF8 byte/scalar/line/column coordinates and source origins;
and check the normal derived Reader expansion history against descriptor summary
and node facts. Prepared negative calls alter actual returned coordinates,
history summary and facts, then require normal C3 rejection.

`check-diagnostic-controls!` separately checks the supplied exact causal-origin
kernel against a parent/request/diagnostic case, including altered causal facts,
parent/source identities, unrelated errors and ambiguous owners.
`check-transported-diagnostic!` requires the carried origin and preservation of
all original diagnostic data, message and cause. Neither function constructs or
authenticates the supplied parent. A real generated rejection must be obtained
through the normal owner route before claiming diagnostic composition; the
valid 192-byte construction stimulus does not itself produce that failure.
Source nodes require one source origin; C2 seed/form and returned syntax spans
must agree on all byte, scalar, line and column fields. Ambiguity controls
duplicate the matching stream even when its owner is not the final node.

## Cheap validation

From the checkout root:

```clojure
;; Run with the source and test directories on the Clojure classpath.
(require 'clojure.test 'gravity.resolve.selected-source-harness-test)
(clojure.test/run-tests 'gravity.resolve.selected-source-harness-test)
```

The focused command uses `clojure -J-Xmx256m -Sdeps
'{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -e ...`,
with a nonzero exit for any failed assertion. Run the repository language gate
after source changes:

```bash
clojure -J-Xmx256m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

Cheap controls use injected source/history fixtures. The causal controls read
and evaluate only the existing pure `sh06-resolution-diagnostic-origin` defn in
an isolated namespace, with hash and source-revision collaborators injected as
premises. They exercise the real leaf's rejection logic without loading the
Bootstrap aggregate. They do not prove parent authentication or compiler
construction. No original Reader control or source checkout is modified.
The injected preparation checks cover two explicit checkout roots and changed
source identities. Test-source lookup itself is checkout-relative, as required
by the command above; this does not limit the library's explicit input root.
An execution receipt must independently bind `loaded-owner`, the actual
`prepare-inputs!` output and the runtime context. The injectable lookup seam is
not evidence that an arbitrary caller supplied those normal owners.

## Preserved development history and remaining execution acceptance

The subsequent [reconciliation receipt](reconciliation.edn) records the successful
same-actor fetch recovery, main `68a127520c2465a90a4a9b06742cb58ebaffca77`
composition, affected cheap checks and independent unchanged-input review.
It supersedes pending reconciliation/CI statements in the initial
[preparation evidence](evidence.edn) without rewriting their historical failures.

The PR544 development branch preserved `b83e8b1c24201e052bce4bc6617d5f3b4c9ffe96` and merged main
`43112981c2e6ee47e1cc1451dbe3b750f88ee874` at
`65b7e166fb5e231aebd1b0a03c20d11008de1f4f`, tree
`cd642dd1415b7d4fb28707b1d6847cf493195122`. The original four checkout variants
and draft PR512 at `5b5f0a05efb1453b479d6ab967e42fd30b0663a6` remain separate.
Its inherited selected-source implementation is unfinished and remains draft.
Its old hosted controls do not validate the new emitter/source composition.

Required next evidence: independent exact harness/source review, reviewed
runtime and source/control identities, and supported resolution of actual
admission safety before any live run. Selected positive/Guest AE/C5, real
generated diagnostic composition, paired extensions and relocation remain unrun.
No held Reader continuation, original22232, denied process inspection or pending
review submission is retried. Git approval grants no runtime capacity or compiler
admission. No ACC13 closure, frozen/public authority, C12 or release is claimed.

The [control integration record](integration.edn) separately records extraction
onto main, exact source conservation, cheap validation and independent review.
Preparatory control integration does not discharge any genuine execution gate
listed above. Earlier PR544 draft and pending-state records remain history.
