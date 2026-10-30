# Syntax source adaptation and native indexes

This increment moves source descriptor construction, empty hygiene, recursive
host metadata cleanup, descendant traversal, native carrier qualification,
root-current checks and form/literal index algorithms into `gravity.syntax`.
Bootstrap retains compatibility wrappers and process-local seed configuration:
the transaction Var, supported-runtime flag, literal-operation defaults and
immutable root snapshots. The stage receives these through explicit ports;
it does not import or resolve bootstrap.

D1, D2, D3, D9, L1, C1, C2, C3, C15 and TEST2 govern the boundary. C3 owns
source descriptor projection and its preserved source/reader facts. Existing
reader, literal, origin and syntax diagnostics retain their owning producers.
The native indexes are private immutable lookup projections, not cached
validation, authentication, proof or capability facts.

| Public API | Inputs and output |
| --- | --- |
| `empty-hygiene` | Namespace -> existing empty hygiene context |
| `strip-host-metadata` | Value and recursive callback port -> metadata-free structural value |
| `descendant-form-ids` | Form index and root -> existing descendant closure |
| `source-descriptor` | Reader seed/form, validated products/report, source/reader identities, registry, operations, canonical seed operations and transaction Var -> exact syntax descriptor |
| `native-keyword-map?` | Value -> exact native map/key qualification |
| `index-roots-current?` | Seed root snapshot -> current roots and absence of thread interposition |
| `form-index` | Artifact, explicit transaction, operations and canonical seed operations -> original form lookup projection |
| `literal-index` | Artifact, index fields/factory, explicit transaction, operations and canonical seed operations -> original literal lookup projection |

Constant seed operation maps contain callable Vars so late interposition stays
live, including recursive cleanup, without reconstructing the maps per node.
Index operation maps are accessed through the native lookup interface, avoiding
new `get` callbacks. Runtime and literal-default values are read through ports
at their original positions. Original JVM type hints are retained.

Reuse still requires the same transaction, owner artifact, source vector,
thread, supported runtime, native shape and current implementation roots.
Altered descriptor/index operation maps disable reuse by object identity,
without comparing or hashing supplied values. This guard is inline and cannot be
replaced through an operation-map callback. Canonical operations and transaction
state are explicit trusted seed configuration, not caller authentication proof.
Canonical descriptor composition
introduces no extra transaction binding. The root snapshot additionally covers
the new stage descriptor and descendant entry points and canonical operation-map
Vars. Opaque records/keys,
collisions, malformed carriers, duplicated ids and interposed operations keep
their original eager construction, callback order and failure behavior.

Before extraction, genuine paired registered-literal fixtures produced one
source descriptor each with identical semantic source identity and retained
source metadata. Complete descriptor, assembly and C3 comparisons and the
native-index regressions passed during development. Final expanded checks,
independent acceptance and remote CI are tracked on the draft PR.

This extraction adds no input quota, language feature, reader extension,
validation cache or authority. Process-local seed lifetime/configuration and
earlier C3 helper implementations remain explicit residual boundaries. No
migration, seed-retirement, release, self-hosting or performance claim is made.
