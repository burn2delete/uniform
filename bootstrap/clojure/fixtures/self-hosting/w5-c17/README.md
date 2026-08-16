# W5 C17 executable plugin/pass leaf

This directory contains the bounded executable C17 compiler-plugin/pass slice.
It is a stage-owned `:meta` Gravity component, compiled and invoked through the
existing stage2 compiler-artifact plan.  It is deliberately non-authoritative:
the Clojure seed, JVM stage2 runtime, independent review, and later W4/W6
integration remain residual boundaries.  It does not claim a global plugin
loader, a global pass registry, self-host completion, or release readiness.

The engine is
`bootstrap/gravity/src/gravity/compiler/w5_c17_plugin_executor.gravity`.
Accepted and rejected requests are co-canonical byte-identical `.gravity` and
`.qst` sources.  Exported request constructors require the actual source path,
so extension provenance cannot silently default to `.gravity`.  The accepted fixture exercises both a sandboxed package and a
policy-approved trusted package.  Both execute a deterministic identity pass,
produce a C17 output-verifier report and execution trace, and expose all C16
cache-key inputs (package/version, manifest, grants, dependencies, compiler API,
build effects, replay records, and input artifact identities).

Pass registration is exact rather than shape-only: artifact, pass id, owning
plugin, version, requirements, proof obligations, emissions, diagnostics,
contract version, and the manifest's single pass membership are bound
together. Domain and facet registrations are also exact records whose plugin,
registered identity, profile, target scope, effects, capabilities, lowering
path, diagnostics, conformance selector, and accepted result are cross-bound
to the request manifest and pass contract. A coherent plugin substitution in
both registration records is rejected. Every diagnostic carries the deterministic
`:w5/plugin-execution` pass identity, including manifest and trust failures.

Operational records use only the exact `:llvm-x86_64-linux` candidate on
Linux/x86_64 with LLVM/ELF/`:sysv-amd64` bindings.  Ordered Darwin and Windows
entries are unsupported and set Clojure invocation, JVM linking, fallback, and
cross-target inference to false; stage2 `:jvm` remains a seed-plan boundary.

The engine validates the manifest before plugin code and checks API range,
package identity, signature/revocation facts, scoped compiler capabilities,
hermetic build effects, sandbox restrictions, pass preservation, C12 domain and
L14 facet registrations, and normal output verification.  It rejects hidden
compiler state or authority, unversioned or incompatible APIs, missing or
excess scopes, denied build effects, invalid preservation, opaque domain/facet
payloads, unverifiable output, and trust/signature failures.

Rejected fixture functions provide one total request for each stable C17 family:

- `C17-MANIFEST` - malformed manifest or provenance;
- `C17-API` - unversioned or incompatible API;
- `C17-CAPABILITY` - missing, excess, or mismatched compiler scope;
- `C17-BUILD-EFFECT` - non-hermetic or ungranted build effect;
- `C17-SANDBOX` - hidden state or ambient authority;
- `C17-PASS-CONTRACT` - incomplete contract or dropped preserved fact;
- `C17-OUTPUT` - missing, substituted, or unverifiable output;
- `C17-DOMAIN` / `C17-FACET` - opaque or unverified registration;
- `C17-TRUST` - rejected policy, invalid signature, or revocation.

Diagnostics retain plugin id, package id, version, pass id, logical source span,
requested capability/effect, trust level, compiler API version, source or
artifact identity, profile, target, and structured remediation.  Actual
checkout paths occur only in provenance; semantic identity and C16 cache-key
inputs are path-neutral.  Result and verifier records remain explicitly
non-authoritative with `:clojure-seed-boundary? true`, `:self-hosted? false`, and
`:release? false`.
