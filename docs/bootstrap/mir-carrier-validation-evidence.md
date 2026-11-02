# MIR: native carrier and relative structure validation

D1, D2, D3, D8, D9, C1, C6-C11, C15, BOOT8 and TEST2 govern this
extraction. Its source parent is e7bc2ca71208ad0ec030089245c239230621f2d8,
tree c2f702fcc3a2f42303014432a53135e77c50127b. The parent node-record
slice remains pending in draft PR830. Its review and tests do not transfer
acceptance, and its inherited CI formatting failure and resolution watchdog
timeout remain nonpasses.

Four existing bodies move from Bootstrap to gravity.mir: validate-carrier!,
metadata-free?, require-strict-structure! and require!. The legacy
bounded-value! name remains a compatibility wrapper; its five-argument form
still calls its live three-argument Var and ignores the two former accounting
arguments. No quota or accounting result is introduced. The source-binding
prefix and node-record tail stay unchanged.

Inputs are native MIR carrier data, source and definition context, expected
and actual structural data, conditions, and explicitly supplied operations.
Outputs remain the native passed map, metadata predicate, original rule return
or original owner diagnostic. Carrier validation receives four live operations
for native class/comparator/metadata/acyclicity validation, Unicode checking,
diagnostic construction and host-error containment. Relative structure and
condition rules receive the original mismatch/failure operations. The stage
owns these traversal and rule bodies; existing callers own input and callback
authentication, native class policy, diagnostic construction and containment.
The stage imports nothing and is consumed by Bootstrap.

Before extraction, the ordinary fixed seed executes current Gravity C11 source
and emits MIR for the paired normalized .gravity/.qst fixtures. Native carrier
validation accepts both. Replacing the module identity with a lone UTF-16
surrogate rejects C11-VERIFY, missing well-formed-unicode-c11-carrier. Changing
the module identity also rejects relative structural parity. Captures retain
complete source/compiler texts and hashes, checked input, emitted MIR, native
validation products and full diagnostics. The invalid replacement is recorded
as its UTF-16 code units so the artifact can reproduce the malformed input
without an invalid UTF-8 serialization.

Tests cover real emission and refusal parity, metadata at nested keys/values
and collections, original diagnostic arguments and returns, containment of
host errors, preserved interruption, explicit live Vars and legacy dispatch.
Whole-file extraction controls compare all four bodies with their parent,
allowing only names, explicit ports and indentation, and preserve surrounding
source exactly. Final test counts, before/after equality, source identities,
independent review and CI receipts are maintained in the draft PR.

Native passed/proof/authenticated/runtime-checked labels remain unverified
data. These rules do not authenticate supplied callbacks or inputs, issue a
certificate, admit source, establish full MIR or runtime-check legality, or
implement general CFGs, backend/native execution, migration, release,
self-hosting or seed retirement. No restricted native/private factory,
frozen Reader, package/archive/release builder or Attempt30 route is executed.
Current-main composition, applicable successful CI, independent acceptance and
an immediate exact clean integration preflight remain required before merge.
