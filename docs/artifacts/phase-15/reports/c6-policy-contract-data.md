# C6 contract DATA prerequisite for relative policy validation

## Purpose

PR552's Native-owned relative policy validator depends on fixed C6 compiler,
Reader and expression contract DATA that is absent from its main baseline.
This finite source increment restores that dependency without construction,
source compilation, report authentication or public API version dispatch.

## Source and requirements

`gravity.c6-c10.source-contract` is byte-identical to preserved Native commit
f66d9ff4554815b89ee7d51ee73b7961f0c33a44. Its compiler, Reader and expression
records remain original observations, not current-main source admission.
No source hash, byte count, function shape or semantic identity is renewed.

`p15-s23-c6c10-source-program-contract` is the preserved pure selector from
the same revision, placed after the existing host diagnostic definitions.
It accepts only native Long versions 1, 2 and 3. Version 1 still derives from
unchanged legacy constants; versions 2 and 3 select the fixed records. Invalid
classes and unsupported values use the existing C6 diagnostic path without
passing caller DATA into the diagnostic subject. Existing constructors and
verifiers do not call a new route or change their default behavior.

## Dependencies and outputs

C6 and D9 govern semantic preservation and evidence attribution. The existing
C6 diagnostic owner remains responsible for full diagnostic records. The
selector returns contract DATA only, without an executable plan or an
authentication status. C11 policy validation and its two distinct digest
domains remain the next producer dependency; genuine versioned public C11
verification remains separate.

## Conformance evidence

`tools/c6_contract_data_test.clj` evaluates only the selected pure definition
and actual legacy constant definitions in an isolated namespace. A synthetic
diagnostic transport checks the selector's owning rule and missing-fact label;
it does not establish full diagnostic construction or source authority.
Positive v1/v2/v3, immutable selection, changed identity, invalid version/class
and foreign-callback controls passed 3 tests / 49 assertions. Bootstrap stayed
unloaded. No producer, compiler, broker, process or authentication ran.

Acceptance requires the focused checks, language boundary, document checks,
exact source review and independently validated current-main composition.
This prerequisite grants no PR552 completion, policy acceptance, OWN-06 closure,
v3 product authentication, release or self-hosting credit. Existing denied and
unknown operations, including PR446 registration, remain unchanged.
