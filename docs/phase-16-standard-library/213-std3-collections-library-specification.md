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

## List Family Predicate

Unary `(list? value)` returns Bool true exactly when the normally evaluated
value belongs to the persistent List family, including the empty List, and
false for every other normally evaluated value. A traversal view or another
collection family is not a List merely because it can be traversed.
Its operand follows ordinary L2 evaluation exactly once; operand effects are
preserved, and a control transfer aborts invocation. Classification neither
evaluates stored payloads, mutates the value nor itself constructs a collection.
It establishes no member types, length, nonemptiness or flow-typing facts.
Collection semantics are owned by `gravity.collections`; a `gravity.core`
compatibility binding does not change that ownership. Existing profile,
target, effect, capability and safety legality requirements still apply.

## Vector Family Predicate

Unary `(vector? value)` returns Bool true exactly when the normally evaluated
value belongs to the Vector family, and false for every other normally
evaluated value. Its operand follows ordinary L2 evaluation exactly once;
operand effects are preserved, and a control transfer aborts invocation.
Classification does not evaluate stored payloads or mutate the value, and
does not establish member types, length, index bounds or range facts.
Collection semantics are owned by `gravity.collections`; a `gravity.core`
compatibility binding does not change that ownership. Existing profile,
target, effect, capability and safety legality requirements still apply.

## Map Family Predicate

Unary `(map? value)` returns Bool true exactly when the normally evaluated
value belongs to the Map family, and false for every other normally evaluated
value. Its operand follows ordinary L2 evaluation exactly once; operand
effects are preserved, and a control transfer aborts invocation. The query
does not evaluate stored keys or values, mutate the Map, or itself allocate a
collection. It establishes no key/member types, cardinality, presence or
ordering facts. `gravity.collections` owns the semantics; a `gravity.core`
compatibility binding does not change that ownership. Existing profile,
target, effect, capability and safety legality requirements still apply.

## Set Family Predicate

Unary `(set? value)` returns Bool true exactly when the normally evaluated
value belongs to the Set family, and false for every other normally evaluated
value. Its operand follows ordinary L2 evaluation exactly once; operand
effects are preserved, and a control transfer aborts invocation. Classification
does not evaluate stored payloads, mutate the value or itself construct a
collection. This rule does not select member-type, cardinality, enumeration,
flow-typing, representation or profile policy; their governing requirements
still apply. `gravity.collections` owns the semantics; a `gravity.core`
compatibility binding does not change that ownership.

## Persistent Set Conversion And Cardinality

For a finite Vector or finite persistent List whose stored values support Gravity equality and hashing,
`(set items)` returns a persistent Set containing one representative of each
value-equality class in the input. Duplicate occurrences do not add members;
an empty input produces an empty Set. This rule does not promise an iteration
order, object identity or physical sharing. The original input is unchanged,
and stored payloads are not evaluated.

The unary operand follows ordinary L2 evaluation exactly once; a control
transfer aborts invocation. The selected representation must declare possible
allocation, including empty conversion, and storage/lifetime policy under
effects, ownership and profile rules. Value equality and hashing follow STD2;
a host representation must not silently substitute different value rules.

The initial List compiler subset accepts exactly a homogeneous Keyword List
and returns a bare Set. The input has no established static length; conversion
establishes no Set member-domain, cardinality, order or nonempty refinement.
Arbitrary sequence views and nil remain outside this conversion contract.
Existing Vector conversion behavior is preserved.

The development List provider must perform complete first/rest traversal and
termination validation before member comparison, formatting, hashing or Set
insertion. It must validate every physical Keyword member and complete the
census before deriving the result; no member may be skipped. A temporary
snapshot may then use the existing Keyword deduplication implementation.
Temporary construction and returned conversion follow the declared allocation,
ownership, lifetime, effect, capability and profile rules.
Temporary and output storage may allocate, including for empty conversion;
stored values are shared shallowly with persistent lifetimes and no borrowed
input List spine.

A Never operand retains the shared Set-constructor non-invocation route. Its
result is Never, latent allocation remains possible in the signature, and
direct allocation is empty. It establishes no invoked List or Vector domain,
count, census, output or cardinality premise. Original source, profile, target,
binding and operand effect/capability legality still require validation.

For a persistent Set, `(count items)` returns the non-negative integer number
of distinct stored members, including zero for an empty Set. It evaluates its
operand normally, preserves transfers and the original Set, and does not
evaluate stored payloads. Collection semantics belong to `gravity.collections`;
a `gravity.core` compatibility binding does not change that ownership. These
rules do not grant arbitrary sequence conversion, every member family or
profile support, or Set equality and iteration implementation support.

## Persistent Set Membership

For a persistent Set, `(contains? items value)` returns Bool true exactly when
a stored member equals `value` under the applicable Gravity value-equality
rules, and false otherwise. Both operands follow ordinary L2 left-to-right
evaluation once each; a control transfer aborts invocation. The query preserves
the Set, and does not evaluate stored payloads or itself construct a collection.
This rule does not select an enumeration order, cardinality analysis or
flow-typing policy.

Collection semantics remain owned by `gravity.collections`; a `gravity.core`
compatibility binding does not grant profile, target, member-equality, hashing
or representation support. Their governing requirements still apply.

## Persistent Set Insertion

For a persistent Set, binary `(conj items value)` returns a Set preserving
the existing stored value-equality classes and adding the class of `value`
only when no equal member is present. Inserting an equal value is not an
error and leaves the Set value-equivalent to its input. The original Set is
unchanged, and stored payloads are not evaluated. Equality and hashing follow
the applicable Gravity member rules, not an unselected host policy.

Both operands follow ordinary L2 evaluation left to right, exactly once;
a control transfer aborts invocation. The selected representation must declare
possible allocation and storage/lifetime policy for both present and absent
values under effects, ownership and profile requirements. This rule promises
neither iteration order, object identity, physical sharing nor allocation-free
execution. It does not grant variadic, arbitrary-member or profile support,
or infer a post-insertion cardinality or member-type fact without evidence.
Collection semantics remain owned by `gravity.collections`; a `gravity.core`
compatibility binding does not change that ownership.

## Persistent Set Value Equality

For two persistent Sets whose stored members support Gravity equality,
`(= left right)` returns Bool true exactly when neither Set has an unmatched
member under the applicable member equality rules. Empty Sets are equal.
Enumeration order, object identity and physical sharing do not determine the
result. Stored payloads are not evaluated, and both original Sets are unchanged.

Both operands follow ordinary L2 evaluation left to right, once each; a control
transfer aborts invocation. Equality itself does not construct a collection.
This rule does not select numeric conversions, cross-collection equality,
iteration order, a hashing algorithm, storage representation or profile support.
Their governing contracts still apply.

## Binary Persistent Vector Append

For a persistent Vector, binary `(conj items value)` returns a Vector containing
the original stored values in their original order followed by the one appended
value. Heterogeneous stored values are permitted by this semantic rule. The
original Vector is unchanged, and stored payloads are not evaluated.

Both operands follow ordinary L2 evaluation left to right, once each; a control
transfer aborts invocation. The selected representation must declare possible
allocation and storage/lifetime policy, including an empty input Vector, under
effects, ownership and profile rules. This rule does not grant variadic or
coercing behavior, object identity, physical sharing, unbounded profile support,
or new static type/cardinality facts without their required evidence.
Collection semantics remain owned by `gravity.collections`; a core compatibility
binding does not grant representation or profile support.

## Persistent Vector Value Equality

For two persistent Vectors whose stored values support Gravity equality,
`(= left right)` returns true exactly when their counts agree and corresponding
stored values are equal under their applicable Gravity value rules. Position
and order matter. Stored payloads are not evaluated, and the original Vectors
are unchanged; equality is not an object-identity or physical-sharing test.

Ordinary L2 operand evaluation and control-transfer rules apply. This rule
does not select numeric conversions, cross-collection equality, a storage
representation or profile support; their governing contracts still apply.

## Persistent List Value Equality

For two persistent Lists whose stored values support Gravity equality,
binary `(= left right)` returns Bool true exactly when their counts agree and
corresponding stored values are equal under their applicable Gravity value
rules. Empty Lists are equal. Position and order matter; object identity,
physical sharing and type identity do not determine the result. Stored
payloads are not evaluated, and both original Lists remain unchanged.

Both operands follow ordinary L2 evaluation left to right, exactly once;
a control transfer aborts invocation. Equality itself does not construct a
collection. This rule does not select numeric conversions, cross-collection
equality, variadic behavior, representation or profile support. Their governing
requirements still apply; a compiler must state its supported equality domain.

## Persistent Vector Lookup With Established Bounds

For a persistent Vector and an independently established integer index in
`0 <= index < (count items)`, `(get items index)` returns the stored value at
that zero-based position. The stored payload is not evaluated, and the original
Vector is unchanged.

Both operands are ordinary L2 arguments evaluated left to right, once each;
a control transfer aborts invocation. This in-range rule does not define an
out-of-range or absent-result policy, invent a host-null fallback, or select a
representation or profile. Bounds must still be proven or runtime-checked under
the requirements above; unestablished bounds are not implicitly safe.

## Persistent Vector Slice With Established Bounds

For a persistent Vector and independently established integer indexes in
`0 <= start <= end <= (count items)`, ternary `(subvec items start end)`
returns a persistent Vector containing the stored values at indexes from
`start` inclusive to `end` exclusive, in their original order. Equal indexes
produce an empty Vector. The original Vector is unchanged, and stored payloads
are not evaluated.

All three operands follow ordinary L2 evaluation left to right, exactly once;
a control transfer aborts invocation. The selected representation must declare
possible allocation, including empty and full slices, and whether result
storage retains, borrows or copies input storage under ownership, lifetime,
effect and profile requirements. This rule promises no object identity or
physical sharing.

Bounds must be proven or runtime-checked under the requirements above. This
in-range rule does not define an out-of-range fallback or a host exception
policy, grant two-operand or variadic support, or restrict the language rule
to literal Vectors. `gravity.collections` owns the semantics; compatibility
bindings do not grant representation or profile support. An implementation's
supported proof and operand subset must be stated separately.

## Persistent Vector Emptiness

For a persistent Vector, unary `(empty? items)` returns Bool true exactly when
it contains no stored elements, and false otherwise, including when its stored
values are false or nil. Its operand follows ordinary L2 evaluation exactly
once; operand effects are preserved, and a control transfer aborts invocation.
The query neither evaluates stored payloads, mutates the Vector nor itself
constructs a collection. It establishes no member, extent, nonempty-branch or
flow-typing fact. This rule does not promise allocation-free internal execution,
provider complexity, representation or profile support; their governing
requirements still apply. Collection semantics remain owned by `gravity.collections`.

## Persistent Vector First With Established Nonempty Input

For a persistent Vector containing at least one stored value, unary
`(first items)` returns the stored value at zero-based index 0, including a
stored false or nil. Its operand follows ordinary L2 evaluation exactly once;
a control transfer aborts invocation. Stored payloads are not evaluated, and
the original Vector is unchanged. The query does not itself construct a
collection.

The nonempty premise must be established by facts or checks permitted by the
governing type, safety and profile contracts. This rule does not define the
result for an empty Vector, select a host sequence or coercion rule, or promise
object identity, a storage representation or allocation-free internal
execution. It does not restrict the language to literal Vectors; a compiler's
supported subset must state which nonempty and stored-value facts it accepts.
Collection semantics remain owned by `gravity.collections`; compatibility
bindings do not grant profile or target support.

## Persistent Vector Rest As A List Suffix

For a persistent Vector, unary `(rest items)` returns a persistent List of
the stored values after index 0, in their original order. Empty and singleton
Vectors produce an empty List. Stored false and nil remain values, stored
payloads are not evaluated, and the original Vector is unchanged.

The operand follows ordinary L2 evaluation exactly once; operand effects are
preserved, and a control transfer aborts invocation. Constructing the List may
allocate, including when the result is empty. The selected representation must
declare its allocation, List-spine copying and shallow stored-value sharing
policy under effect, ownership, lifetime and profile rules. The returned List
has persistent value lifetimes and does not borrow the Vector's backing storage.
Shared stored values must retain their required lifetimes independently of the
input Vector's storage. This rule promises no object identity, physical sharing,
allocation-free execution, provider complexity or profile availability.

Collection semantics remain owned by `gravity.collections`; compatibility
bindings do not grant representation or profile support. A compiler must state
its supported operand and member-type subset and validate the input before
construction without inventing facts for an unvalidated input.

## Persistent Map Keys As A List

Unary `(keys items)` returns a persistent List containing each stored Map key
exactly once. An empty Map returns an empty List. Default Map enumeration order
is unspecified; ordered or sorted provider contracts retain their stronger
guarantees. The result establishes no positional order, key identity, presence,
cardinality, nonempty or flow-refinement facts.

Evaluate the operand once in non-tail position before enumeration. Preserve
operand effects, capabilities and transfers. A non-completing operand prevents
enumeration and construction; its result is Never, with no direct allocation
from `keys`. Stored values are inert: enumeration neither evaluates nor hashes
them as keys. The original Map is unchanged.

The checked subset accepts complete finite literal Map types whose physical
`member-types` Vector has an even number of paired key/value slots, with every
key-slot type exactly Keyword and every value type fully validated.
The normal result type is exactly the homogeneous Keyword List, including for
an empty Map. Other key families and general Maps reject in this subset.
A bare Map parameter is accepted only through L5's complete private call-entry
key-domain rule: each normal entry independently establishes the whole finite
Keyword-key Map and validates every stored value type. Different entry shapes
and value types are permitted; the parameter remains bare Map and the result
remains the homogeneous Keyword List.

This exception requires the complete closed private function/use/call census,
actual argument sources and types, bare-Map compatibility and call facts.
Selected-key presence evidence cannot authorize it. At least one normal entry
is required, and an empty Map is a valid witness. A Never argument in any
coordinate makes that call non-invoking. Unknown, circular, unaccounted or
all-non-invoking entries cannot ground the proof. Public entry, escape,
mutation and parameter re-entry cannot be silently excluded. Each consuming
compiler stage must reconstruct the proof and retain its function, parameter,
`keys` use and complete caller dependencies and invalidations.

Validate profile, target, type, key-domain, census and construction conditions
as part of the corresponding walk or construction; ignored values and
malformed tails remain subject to complete type validation. This exception adds
no common shape, key presence, extent, cardinality, nonempty, flow or
specialization facts.

Normal completion permits `:memory/allocate`, including for an empty result.
The development provider checks the actual Map and enumerates its complete
physical entries, validating every Keyword key before fresh output List
construction, then performs the indexed snapshot and output copying. The
returned List owns its copied spine, shares stored keys shallowly, has
persistent value lifetimes and borrows no Map backing storage. This establishes
no allocation bound for other providers.
Conversion of this List follows the separate Set-conversion contract above.

## Map Lookup With An Explicit Default

For maps, `(get map key default)` returns the associated value when the key is
present, including a stored false or nil, and otherwise returns the default,
if all operands complete normally. Presence is not a truthiness test.

All three operands are ordinary call arguments under L2: evaluate the map,
key, then default eagerly, even when the key is present. A control transfer
aborts invocation; the default is not an implicit thunk or lazy branch.
Key matching follows STD2 value semantics, and lookup preserves the original
map. This rule does not select a representation or grant profile support.

## Map Lookup With Established Presence

For maps, when independent facts establish that the evaluated key is present,
`(get map key)` returns its stored value, including false or nil. The stored
payload is not evaluated, and the original Map is unchanged. Member-type
families alone do not establish the identity or presence of a particular key.

Both operands are ordinary L2 arguments: evaluate the Map, then the key, once
each. A control transfer aborts invocation without requiring a value or presence
fact for an operand that does not return. This present-key rule does not define
the result for an absent key, invent a host-null fallback, or select a storage
representation or profile. Implementations must not assume presence when it is
unestablished; unsupported cases retain their owning typing diagnostics.

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
- List family classification includes empty Lists, distinguishes other collection families and traversal views, preserves ordinary unary evaluation and transfers, and neither evaluates payloads nor invents member, cardinality or flow-typing facts.
- Vector family classification distinguishes Vector and non-Vector values, preserves ordinary unary evaluation and transfers, and neither evaluates stored payloads nor invents member or bounds facts.
- Map family classification distinguishes Map and non-Map values, preserves ordinary unary evaluation and transfers, and neither evaluates stored keys/values nor invents key, member, presence or cardinality facts.
- Set family classification distinguishes Set and non-Set values, preserves ordinary unary evaluation and transfers, and does not evaluate stored payloads, mutate the value or construct a collection.
- Persistent Set conversion covers empty, unique and duplicate Vector and persistent List values under supported Gravity equality/hash rules, preserves ordinary evaluation, transfers and the old value, and declares allocation/storage policy. The homogeneous Keyword List subset validates the complete physical Keyword census before member equality, hashing and insertion, and returns a bare Set without static member-domain or cardinality facts. Set cardinality counts distinct stored members without evaluating payloads or promising iteration order.
- Persistent Set equality covers empty, reordered and differing membership under applicable Gravity member equality, preserves ordinary operand evaluation, transfers and original values, and does not evaluate stored payloads or depend on enumeration order or object identity.
- Persistent Set membership covers empty, present and absent values under applicable Gravity member equality, preserves ordinary operand evaluation, transfers and original values, and neither evaluates stored payloads nor depends on enumeration order or object identity.
- Binary persistent Set insertion covers empty input, absent values and equal existing members under applicable Gravity equality, preserves ordinary operand evaluation, transfers and original values, and declares allocation/storage policy without inventing cardinality, member-type, order or identity facts.
- Binary persistent Vector append covers empty and nonempty heterogeneous values, preserves the ordered prefix and original input, retains ordinary operand evaluation and transfers without executing stored payloads, and declares possible allocation/storage policy without granting variadic, coercing, unbounded-profile or static-fact authority.
- Persistent Vector equality covers equal, reordered, changed and different-length values, corresponding nested values, ordinary operand evaluation and transfers, inert payloads and original-value preservation under the applicable member equality rules.
- Binary persistent List equality covers empty, equal, reordered, changed and different-length values, corresponding nested values, ordinary operand evaluation and transfers, inert payloads and original-value preservation under the applicable member equality rules, without substituting type or object identity.
- Proven-in-range Vector lookup returns the indexed stored value, preserves the original Vector and inert payloads, and retains ordinary operand evaluation and transfers without assuming an out-of-range result policy.
- Proven-in-range Vector slicing covers empty, full, prefix, middle and suffix ranges, preserves stored order, inert payloads and the original input, retains ordinary eager operand evaluation and transfers, and declares allocation/storage policy.
- Persistent Vector emptiness covers empty and nonempty values, including stored false/nil and inert payloads, preserves ordinary evaluation, effects, transfers and the original value, and does not construct a collection or invent extent, member or flow-typing facts.
- Proven-nonempty Vector first covers singleton and ordered multi-element values, stored false/nil, ordinary operand evaluation and transfers, inert payloads and original-value preservation without selecting an empty-Vector result policy.
- Persistent Vector rest returns an ordered persistent List suffix, including an empty List for empty and singleton inputs, preserves false/nil, inert payloads, operand evaluation, effects, transfers and the original Vector, and declares possible allocation, List-spine copying, shallow stored-value sharing and persistent lifetimes without borrowing Vector storage.
- Map keys returns a persistent homogeneous Keyword List for the initial finite Keyword-key Map subset, includes every stored key once, permits allocation even when empty, preserves stronger provider order contracts without default order or cardinality facts, validates all input types and physical keys before output construction, and preserves inert values, operand effects, capabilities and transfers.
- Private bare-Map `keys` fixtures reconstruct complete entry-specific whole-key-domain and compatibility evidence at each compiler consumer, cover different normal entry shapes and empty Maps, reject String keys, ignored malformed values, unknown or non-invoking-only witnesses and uncovered callers/escape/mutation/re-entry, and preserve the same result and allocation/storage policy without refining the parameter.
- Explicit-default Map lookup distinguishes missing keys from stored false/nil and preserves eager operand evaluation and transfers.
- Proven-present two-operand Map lookup preserves stored false/nil, the original value and operand evaluation/transfer behavior, and does not infer key presence from member-type families alone.
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
