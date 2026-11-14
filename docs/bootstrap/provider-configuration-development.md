# Explicit provider configuration development

Finding: `build.provider-configuration.explicit-layers-v1`.
Parent: `build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution`.
The parent remains Open with its existing criteria. This note describes the
local bootstrap API. The source-bound implementation task file, pinned plan,
source-version records and historical migration suffix remain unchanged.
Historical author observations remain in the evidence record.

### Source-backed behavior and scope

[L15 Selection](../../docs/phase-01-core-language/025-l15-capability-provider-specification.md#selection)
supplies five precedence layers. [PKG1](../../docs/phase-12-build-package-and-artifact-system/165-pkg1-project-file-specification.md#requirements)
requires nonexecuting configuration; [PKG6](../../docs/phase-12-build-package-and-artifact-system/170-pkg6-capability-and-permission-manifest-specification.md#requirements)
keeps requests, denials and grants separate. The Build resolution row in
[acceptance dependencies](../../docs/acceptance-dependencies.md)
permits an early declared configuration product before actual compatible
selection. Implement only that input normalization, with accepted/rejected
decoded fixtures. This is no claim of source conformance or completion of L15,
PKG1, PKG5, PKG6 or the parent task.

Owned paths are the new
[normalizer](../../bootstrap/clojure/src/gravity/build/provider_configuration.clj),
its [tests](../../bootstrap/clojure/test/gravity/build/provider_configuration_test.clj),
this development note and
[evidence.md](../../specs/build-workflow/evidence.md). The namespace
`gravity.build.provider-configuration` and function `normalize` describe their
subsystem and behavior. No legacy renames or caller changes are needed.

### Local decoded bootstrap carrier

This defines a local Clojure input/output seam, not a Gravity project-file or
provider-declaration schema. Normalize one explicit selection context at a time.
Input is exactly `{:profile keyword :target keyword :layers map}`. Context IDs
are unqualified keywords; their semantic legality remains with the actual
profile/target owners. All five layer keys must be present, even when empty:
`:source`, `:package`, `:workspace`, `:profile-default`, `:compiler-default`.
The latter two additionally bind both `:profile` and `:target` to the active
context, preventing reuse of a default table for another context.

Every layer contains exactly `:selections`, `:effects`, `:capabilities`, plus
the two context bindings for defaults. `:selections` is a vector of
`{:capability qualified-keyword :phase :build-or-runtime
:provider {:id qualified-symbol :version string} :location location}`.
The actual phase values are `:build` and `:runtime`. Provider versions are
literal numeric release identifiers, optionally with dotted numeric components,
a hyphen suffix and a plus suffix; suffix characters are ASCII alphanumerics,
dots and hyphens. They are preserved exactly, with no range solving, aliases,
case folding or version ordering. This local grammar is not a universal L15
version grammar. Two selections for the same phase/capability in one layer are
ambiguous and reject, even when identical; different layers and phases remain
separate. Multiple source scopes must be submitted as separate contexts.

Each effect/capability summary is exactly
`{:build {:request [] :deny []} :runtime {:request [] :deny []}}`.
Each vector entry is `{:id qualified-keyword :location location}`. Locations
are public decoded `{:file string :start long :end long}` records with
nonnegative half-open offsets and `end >= start`; no file is opened or offset
extent authenticated. Strings must contain valid Unicode scalars and no control
characters; identifiers also exclude whitespace and extra slashes. Offsets use
the local signed-64-bit carrier. Maps/vectors must be native persistent
array/hash maps and vectors, without metadata; provider symbols must also be
without metadata. Lazy sequences, callbacks, records and custom carriers reject.
Unknown/missing fields reject instead of silently discarding authority inputs.
The caller must supply already-public data; this seam cannot authorize private
registry disclosure or redact secrets from otherwise well-formed input.

Output retains `:profile` and `:target`, replacing the layer map with a vector
in the five-layer L15 order. It retains exact references, locations and default
bindings; selections sort by phase/capability and request/deny entries sort by
id/file/start/end. Overlapping requests and denials survive, including within
one summary. No layer is collapsed, no winner is chosen and no denial is
converted to permission. No source effect analysis, transitive dependency
summary, policy/trust verdict, grant or execution result is synthesized.

Errors throw `ExceptionInfo` with a fixed message and only
`{:id "BUILD-PROVIDER-CONFIGURATION" :reason keyword :path vector}`.
Paths use schema-owned keys and vector indices, never submitted keys or values.
Reasons distinguish carrier/metadata/field/identifier/reference/location/phase
failures, duplicate selection, and profile/target default binding mismatch.
These local shape diagnostics do not assert L15 compatibility or authority.

### Checks and remaining dependencies

Run the focused namespace, `tools/acceptance_dependencies_test.clj` (including
`unchanged-captured-spans-follow-layout-only-ledger-edits` and
`normalized-source-snapshot-tampering-rejects`), the language-boundary gate and
both document validators. Record actual argument vectors, identities, failures
and unrun checks in evidence.md. Preserve byte-identical canonical region,
historical suffix and pinned plan. Use the documented `:dev-test` runner for this namespace:

```bash
clojure -M:dev-test --namespace gravity.build.provider-configuration-test
```

Start needs only explicit public decoded fixtures. Later composition needs real
Auth declaration/selection and checked profile/target/policy products; actual
resolution, compatibility, private-registry authorization, trust, grants and
execution retain their current owners. Independent acceptance remains pending;
this worker cannot accept or integrate its own increment. Release and self-host
dependencies remain separate. Historical operator restrictions and observations remain in the evidence
record. This API and its documentation confer no provider or integration
authority.
