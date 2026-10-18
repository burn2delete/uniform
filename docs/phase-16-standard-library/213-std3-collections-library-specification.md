# STD3 - Collections Library Specification

Sequence: 213
Phase: 16 - Standard Library
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

`gravity.collections` defines Gravity's standard persistent and bounded collection APIs.
It provides the Lisp-facing collection vocabulary expected by code-as-data, macros, ordinary application code, and compiler tooling.
The library must preserve value semantics while allowing profile-specific representations such as host-backed vectors, native persistent tries, firmware-fixed buffers, hardware tuples, and GPU arrays.

The collection contract is semantic first.
A vector is ordered indexed data, a map is associative key/value data, a set is membership data, and a sequence is a traversal view.
Implementations may vary by backend, but equality, iteration order where specified, hashing, bounds behavior, and mutation visibility must be stable.

## Requirements

- Persistent collections MUST expose value semantics and stable equality across supported profiles.
- Collection APIs MUST declare allocation behavior for every constructor, update, builder, and conversion.
- Indexing, lookup, slicing, and mutation-like operations MUST be protected by `:runtime-checked` bounds checks or classified as `:proven-safe`.
- Transient and builder APIs MUST be linear or scope-bound so they cannot be used after persistent conversion.
- Iterator and sequence views MUST define whether they retain, borrow, copy, or stream collection state.
- Hash maps and sets MUST use Gravity equality and hash contracts from STD2.
- Ordered maps and sorted sets MUST declare comparator totality requirements.
- `:hardware`, `:firmware`, and `:kernel` APIs MUST reject unbounded allocation unless an explicit bounded representation is selected.
- Host-backed implementations MUST preserve Gravity diagnostics and artifact metadata.
- Unsafe optimized internals MUST be hidden behind safe wrappers and audit records.

## Module Surface

- Constructors: `list`, `vector`, `map`, `set`, `queue`, `sorted-map`, `sorted-set`, `array`, and bounded variants.
- Queries: `count`, `empty?`, `contains?`, `get`, `find`, `nth`, `first`, `rest`, `peek`, `keys`, and `vals`.
- Updates: `conj`, `assoc`, `dissoc`, `update`, `pop`, `subvec`, `replace`, `merge`, and `merge-with`.
- Traversal: `seq`, `iterator`, `map`, `filter`, `reduce`, `fold`, `partition`, `take`, `drop`, `zip`, and `transduce`.
- Builders: `builder`, `transient`, `persistent`, `append!`, `assoc!`, `freeze!`, and `with-capacity`.
- Protocols: `Seqable`, `Associative`, `Indexed`, `Counted`, `Reducible`, `Buildable`, `Hashable`, and `ComparableCollection`.
- Bounded systems APIs: `fixed-vector`, `ring-buffer`, `static-map`, and `memory-view`.

## Map Lookup With An Explicit Default

For maps, `(get map key default)` returns the associated value when the key is
present, including a stored false or nil, and otherwise returns the default,
if all operands complete normally. Presence is not a truthiness test.

All three operands are ordinary call arguments under L2: evaluate the map,
key, then default eagerly, even when the key is present. A control transfer
aborts invocation; the default is not an implicit thunk or lazy branch.
Key matching follows STD2 value semantics, and lookup preserves the original
map. This rule does not select a representation or grant profile support.

## Persistent Map Removal

For maps, `(dissoc map key)` returns a Map without the association matching
`key` under STD2 value equality. A missing key returns a value-equivalent
unchanged Map. Stored false and nil are values, not evidence of absence.
The original Map and all other stored associations are preserved; stored
payloads are not evaluated.

Both operands are ordinary L2 arguments evaluated left to right. A control
transfer aborts invocation. The selected representation must declare possible
allocation and storage/lifetime policy for both present and absent keys under
effects, ownership and profile rules. This rule promises neither physical
sharing nor object identity nor allocation-free execution, and does not grant
variadic or arbitrary-key implementation support.

## Persistent List Queries

For persistent Lists, `(first items)` returns the first stored value, or nil
for an empty List. `(rest items)` returns the ordered List suffix after the
first element, or an empty List when items is empty. `(empty? items)` returns
Bool true exactly when the List contains no elements, and false otherwise.
`(count items)` returns the non-negative integer number of stored elements,
including zero for an empty List.
The operand is evaluated under L2 before querying its value. These queries
do not mutate the original List or evaluate stored payloads.

The implementation must declare whether its suffix retains input storage,
borrows, or copies under the memory and profile rules. A no-allocation claim
requires the selected representation.

## Persistent List Prepend

For persistent Lists, `(conj items value)` returns a List whose first stored
value is `value` and whose remaining ordered values are those of `items`.
Both operands are ordinary L2 arguments evaluated left to right; a control
transfer aborts invocation. The old List is unchanged, and stored payloads are
not evaluated.

The selected representation must declare allocation and storage/lifetime
policy for constructing the returned List under effects, ownership and profile
rules. The ordered suffix need not use the identical storage object. This
binary rule does not grant variadic or arbitrary sequence support.

## Dependencies

- `D1` and `D3` for literal data, metadata, namespaces, and syntax objects.
- `L2`, `L5`, `L6`, `L7`, and `L10` for types, effects, capabilities, dispatch, and collection values.
- `SAFE1`, `SAFE2`, `SAFE4`, `SAFE5`, `SAFE6`, and `SAFE15` for memory safety, bounds checks, linear resources, unsafe internals, and proof evidence used by optimization.
- `P1` through `P13` for profile-specific allocation and backend behavior.
- `PERF1`, `PERF2`, and `PERF8` for representation choices, specialization, and benchmark evidence.
- `STD1` and `STD2` for library architecture and core equality/hash semantics.

## Example

```clojure
(ns sample.index
  (:require [gravity.collections :as c])
  (:profile :native))

(defn add-user [users user]
  (c/assoc users (:id user) user))
```

The call to `assoc` allocates a new persistent map node or uses a profile-specific representation.
The allocation effect is visible to the compiler.
In a no-allocation profile, this function must choose a bounded representation or be rejected.

## Profile Availability

- `:core` receives persistent lists, vectors, maps, sets, sequences, reducers, and pure builders with explicit allocation metadata.
- `:hardware` receives static tuples, fixed arrays, and compile-time maps only.
- `:firmware` receives fixed buffers, ring buffers, and arena-backed collections when bounds are declared.
- `:kernel` receives collections only when allocation strategy and lifetime are explicit.
- `:native` receives persistent collections, transients, arrays, builders, and memory views under ownership rules.
- `:hosted` may delegate implementations to host runtimes while preserving Gravity value semantics.
- `:distributed` may persist collections only through STD10 canonical serialization.
- `:ai` may pass collections through prompts, tool schemas, and memory only with taint and schema metadata.
- `:gpu` receives arrays and data-parallel traversals that lower to legal memory spaces.
- `:formal` receives total APIs or proof-carrying partial operations.

## Outputs and Artifacts

- Collection module manifest with representation families and profile matrix.
- Type and effect signatures for all constructors, updates, traversals, and builders.
- Bounds, iterator, transient, equality, hash, and ordering fixtures.
- Negative fixtures for use-after-free builder state, invalid iterator use, missing allocation strategy, and unsupported profile access.
- Representation notes for persistent tries, arrays, bounded buffers, and host-backed collections.
- Unsafe internal audit records for optimized hashing, array access, and transient mutation.
- Benchmark evidence for standard operations per profile and representation.

## Diagnostics

- `STD3001` when an operation would allocate in a profile that forbids hidden allocation.
- `STD3002` when an index or slice lacks proof or a runtime check.
- `STD3003` when a transient builder escapes its scope.
- `STD3004` when an iterator is used after invalidation.
- `STD3005` when a comparator is not total for a sorted collection.
- `STD3006` when a host-backed collection changes Gravity equality, hash, or order semantics.
- `STD3007` when a collection crosses a workflow, AI, or package boundary without schema metadata.
- `STD3008` when optimized internals lack an unsafe audit artifact.

## Conformance Criteria

- Persistent operations preserve old collection values in all supported profiles.
- Explicit-default Map lookup distinguishes missing keys from stored false/nil and preserves eager operand evaluation and transfers.
- Binary persistent Map removal covers present, absent and empty Maps, stored false/nil, original-value preservation, eager operand evaluation and transfers, inert payloads, and explicit allocation/storage policy.
- Persistent List queries cover empty and nonempty ordered values and cardinality without executing stored payloads, and declare their suffix storage policy.
- Binary persistent List prepend preserves the old ordered values after the new head, ordinary operand evaluation and the original List, with explicit allocation/storage policy.
- Bounds fixtures pass with static proofs and runtime checks where appropriate.
- Equality and hash fixtures agree with STD2 across targets.
- Bounded systems profiles reject unbounded collection construction.
- Builder and transient APIs are linear or scope-bound.
- Documentation examples compile under declared profiles and fail under rejected profiles.
- Benchmarks report profile, representation, allocator, target, and compiler settings.
- Serialization and workflow fixtures prove that collections retain canonical shape across artifact boundaries.
