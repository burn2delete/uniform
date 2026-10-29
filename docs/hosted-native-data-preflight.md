# Hosted native DATA preflight

## Purpose and boundary

`hosted-native-data-preflight` is a development hosted/JVM bootstrap primitive
for checking original initializer DATA before generic collection access. It
requires a compiler-artifact plan with profile `:hosted`, target `:jvm`, and one
argument. It does not expose the META/JVM native-header or identity operations;
their existing profile guards remain unchanged.

The result is exactly `{:status :passed}` or
`{:status :rejected :reason <keyword>}`. It establishes representation safety
for this traversal only. It is not a type, initializer grammar, Unicode,
source authenticity, execution, migration or public-admission verdict. Callers
must check the same original object before its first generic collection access
and retain all subsequent semantic validation. A supplied result is not a
transferable certificate. No object is copied, normalized or sealed.

## Input policy

The explicit `:hosted-initializer` policy shares the native carrier traversal.
The existing two-argument and transitional five-argument Native interfaces
retain their strict metadata and vector policies.

Exact native scalar and persistent collection classes are checked before
metadata, enumeration or user callbacks. Native lists and Cons cells require
native tails. Sorted maps and sets require the default native comparator.
Unsupported objects and foreign callbacks reject without formatting the input.
The traversal uses ancestor identity to reject cycles while allowing shared
finite aliases. There is no node, width, depth or logical work quota.

Metadata is preserved on the original object and traversed as inert native
DATA. Opcode-shaped metadata is not executed or counted as an initializer
constructor. Rejecting foreign or cyclic metadata is an explicit hosted input
restriction; it is not a claim that the old initializer inspected metadata.

Native SubVectors are allowed only after their complete backing-link chain
and extents are checked using exact native classes. Only visible payload and
visible view metadata are traversed. Unused backing payload and backing
metadata stay inert; inspecting backing structure does not authorize forcing
those values. Foreign backing, invalid extents and ancestor/backing-link cycles
reject before generic view traversal.

## Validation and residual boundaries

Focused controls cover compiled hosted dispatch, finite deep and wide DATA,
shared aliases, metadata preservation, native lists and views, malformed views,
foreign callbacks, inert unused backing, cycles and unchanged strict Native
and META/JVM gates. Accounting removal and actual initializer consumption are
separate integration work: adding this primitive alone does not remove any
initializer or execution accounting or establish whole-runtime safety.
