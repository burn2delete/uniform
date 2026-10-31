# Versioned C11 source binding prerequisite

## Purpose and governing requirements

PR552's missing versioned public C11 verifier cannot be implemented by adding
an arity alone. Its genuine source path needs versioned C11 compilation and
identity checks, versioned fresh Core authentication, actual Gravity replay,
and exact contextual artifact parity. C11 and D9 require provenance and
evidence attribution across that path. This finite ACC10 increment restores
the source-binding dependency without claiming the other requirements.

The baseline is ec519df2b66a3b114aef9b48cc7ca0839ed6d44a from PR585, including
PR574 and PR584 prerequisites. Their pending independent/integration
dispositions remain pending. Source restoration uses preserved Native
f66d9ff4554815b89ee7d51ee73b7961f0c33a44's complete
`00732a-p15-s23-c11-mir-versioned-source-binding.clj` and its source-binding
containment wrapper from 00733. The helper body is placed after the existing
fixed selectors in 00732; no new component or load-order entry is needed.

## Behavior and outputs

The internal binding helper and its existing containment wrapper gain explicit
version arguments. Existing two-argument helper calls select version 1 and
derive identities from unchanged legacy constants. Versions 2 and 3 select the
fixed reviewed contracts; no source pin, expected shape or digest is renewed.
Unsupported version classes are rejected before filesystem or compiler access.

Selected source reads preserve exact byte-size/content checks and strict UTF-8
decoding. Version 3 requires a regular non-symlink file, checks the compiled
plan's source identity, and checks file identity, size, modification time and
bytes again after compilation. The actual implementation calls the existing
emitter owner and compiler, then compares required function shapes and plan,
function-set, builder and verifier semantic hashes. No saved plan or hash
fallback is introduced. Private bindings retain the executable plan; public
source-rule projection still exposes only the governing DATA fields.

## Remaining public-verifier dependencies

At this increment, public constructor and verifier arities remain unchanged.
The inspected dependency chain still requires the following source work:

| Owner | Required dependency |
| --- | --- |
| C6 00726 | Explicit-header fresh Core verification report, currently only two arguments |
| C6 00724 / preserved 00724a | Versioned fresh construction and source-policy receipt production |
| C11 00741 | Versioned scope, verifier record and MIR finalization |
| C11 00742 / 00743 | Versioned expected artifact and strict final-artifact validation |
| C11 00744 helpers 02 / 03 | Invoke the actual versioned Core owner and project only its fresh report |
| C11 00744 helpers 04 / 05 | Select actual builder/verifier entrypoints and finalize the selected interface |
| C11 00744 public definitions | Explicit-header constructor/report/verify arities over the complete chain |

These are dependencies, not permission to substitute a supplied report,
synthetic compiler result or historical artifact for fresh authentication.
The source-binding increment alone does not satisfy PR552's public call.

## Conformance and acceptance evidence

`tools/c11_source_binding_test.clj` evaluates the actual selected definitions
in isolation. Six tests / 53 assertions cover unsupported native-version
classes with zero foreign callbacks; real file-size/content and malformed
UTF-8 refusal before compile oracles; selected identity propagation; plan,
function-shape and all four semantic-hash mismatches; unchanged legacy helper
dispatch; and version 3 changed bytes, file replacement and symlink refusal.

Filesystem operations, UTF-8 decoding, source SHA-256 and implementation
control flow are actual. Contract, compiler, semantic-input/digest and
diagnostic transports used by successful unit controls are declared oracles.
Their synthetic results establish mechanism behavior, not current source
compilation, producer acceptance, product authentication or actual replay.
Bootstrap remains unloaded. Independent exact-source acceptance and applicable
composition/CI are still required before integration.

Affected pure selector checks passed 2 tests / 41 assertions, relative policy
checks 4 / 54, and the language boundary 6 / 11. Documents (240 documents,
19 phase indexes), full-language roadmap and diff checks passed.

PR453's actual original-v2 consumer evidence remains with its existing MIR
owner. PR446 remains an unknown original registration. No denied message,
registration, runtime/controller operation or held genuine launch is retried.
No OWN-06 completion, v3 product acceptance, release or self-hosting is claimed.
