# Reader dispatch development evidence

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
