# Reader dispatch development evidence

The next source increment is documented separately in
[normal Reader dispatch seam](normal-dispatch-evidence.md); its injected fixture
results do not establish authenticated Reader execution.

Task: `reader/finite-remaining-work/extension-policy-closure` (open).
Base: `abc4ed1c8bdb944c7aed949a0215f14579864311`.

The first bootstrap increment is `gravity.reader.dispatch/dispatch`. It runs
reviewed, implementation-owned handlers on an already-validated text seed.
The policy registers a tag to a handler symbol and selects the allowed profile,
effects and capabilities. Policy cannot supply executable code or assert purity.
The implementation table supplies the executable functions and their required
effects independently of registry declarations.

`gravity.reader/read-text` returns the supplied seed without a provider call.
`gravity.reader/read-input` invokes a separately supplied trusted host provider
and returns a seed containing its actual text result. The build grant must bind
the handler principal, provider, exact input, capability, build phase and
invocation lifetime before either the handler or provider executes. Unknown and
disallowed tags, unavailable handlers, disallowed profiles, undeclared effects,
missing capabilities and mismatched grants reject with `C2-EXTENSION`.

The invocation record is constructed after the actual handler returns. It names
the selected policy id, selected entry and effective permission sets, handler,
tag, profile, source/form/span and, for an effectful call, provider and grant.
It records implementation-owned effects, not a caller's purity claim. Provider
exceptions propagate as execution failures; they are not successful invocations.

## Focused checks

- `clojure -J-Xmx512m -M:dev-test --namespace gravity.reader.dispatch-test`:
  6 tests, 70 assertions, zero failures/errors.
- `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`:
  6 tests, 11 assertions, zero failures/errors.
- `git diff --check`: passed.

Tests wrap the real handler implementations to observe calls and retain their
real return values. The granted test reads the declared local fixture through
the provider. Negative controls observe zero handler and provider calls. Foreign
container/callable inputs and unpaired-surrogate text reject; ambient data readers
are not consulted. These checks construct no compiler or broker jobs.

## Remaining acceptance

This is a Clojure bootstrap leaf and does not close the task. It supports only
the two reviewed handlers and the private text-seed interface. It does not load
arbitrary extension code, validate a complete syntax seed, authenticate source,
or sandbox a malicious host provider. The provider callback is a trusted host
binding; it is never accepted as a pure handler. Original input validation and
all normal native/callback/provenance rules remain prerequisites at the owning
Reader boundary.

Still required: Uniform-authored dispatch, integration with the normal Reader's
registered-tag parsing and syntax seeds, authentic policy/implementation binding,
normal source-bound invocation hashes, independent fresh reconstruction and
positive/negative Reader integration fixtures. The current fixed inst/uuid
projection records have not been relabeled as execution evidence. Existing
C2/PR374/selected-Syntax work and historical failed operations are unchanged.
Independent exact-source acceptance is pending. No release, migration,
self-hosting, runtime admission or complete extension-policy support is claimed.

## Independent leaf review and projection repair

The exact `f27f076231c056909e83a6dd59f662542cd4864f` bootstrap leaf received
independent review from existing reviewer `01a183df-0570-76e0-adfa-796c573e66f7`.
Receipt SHA256:
`9213fa159fabae82610edd7ec496f52e16735491897cd776342412cc5c49de80`.
The reviewer accepted only the declared leaf, reproduced 6 tests / 70 assertions
and 6 / 11, and passed 2 / 35 additional adversarial controls. Committing provider
result/exception regression coverage was suggested as follow-through, not a
blocking finding. Full Reader and current-composition acceptance remain open.

The old head's CI projection failure is retained as a nonpass. The task row had
a relative evidence link and lacked its generated roadmap/source-span update.
The repair uses an absolute immutable HTTPS evidence link, regenerates the
Reader table, records the standalone test owner, and pins those three files to
real source commit `2e0292c61603aad200df397e397e94e3c7d24818`. The SH crosswalk
is regenerated with a pending source-version review disposition and preserves
the complete predecessor mapping review. No mapping checker is weakened and
no implementation or task acceptance is inferred.

Focused repair controls passed: Reader projection/check tests 5 / 34;
dependency controls 14 / 402; crosswalk source/serialization/negative controls
10 / 107; ownership inventory 1 / 554; `git diff --check`. The initial crosswalk
test expected the old Reader pin as current and failed once. It now checks the
historical predecessor plus the successor's exact Git bytes/hashes, unchanged
task predicates and retained historical suffix. An initial unknown ownership
test selector ran no tests; the corrected exact selector produced 1 / 554.

Dispatch implementation, dispatch test and fixture bytes remain identical to
`f27f0762`. The old independent review is not presented as acceptance of the
changed projection inputs. Changed-input review and current-head CI are pending.

## Current-main composition

Independent metadata-delta review accepted exact `da033740` / tree `3715774c`
with receipt SHA256
`d5c4be019502c982c8de9b54f0df9d5527c1cfd9e1c10d74485592fd11cfde97`.
It reproduced 29 tests / 543 assertions and byte-identical regeneration. This
receipt does not confer mapping promotion, integration or full Reader acceptance.

Main `43112981c2e6ee47e1cc1451dbe3b750f88ee874` was merged normally into the
Reader branch. The source merge `e446f3268ecc8ce25408236631a41c5939e7a952`
retains both the incoming private-collection test owner and Reader dispatch test
owner. The composed ownership bytes are pinned to that real source merge; task
and roadmap bytes retain their unchanged `2e0292c6` pins. The generated crosswalk
retains incoming accepted mapping review as predecessor history and keeps the
composed source-version disposition pending. Original Reader bridge fields and
the earlier metadata review remain preserved under the composition record.

Composition controls passed: Reader projection 5 / 34, dependency controls
14 / 402, crosswalk controls 10 / 108. The extra assertion checks exact union of
both standalone ownership maps. Dispatch implementation, tests and fixture are
unchanged. During bridge preparation the intended data write completed before
an extra trailing delimiter caused the ad hoc Clojure command to exit with an
error. That nonpass is retained; no write was repeated. Subsequent documented
generation and check commands passed and controls read the actual serialized
bridge. The source merge alone was not treated as validation evidence.

Fresh composed-head CI and independent integration review remain pending. No
Reader semantic/compiler/broker execution or whole-task closure is inferred.
