# DOM15 - Security and Cryptography Domain Specification

Sequence: 138
Phase: 9 - Domain-Specific Computing Coverage
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This domain proves that Gravity can cover security and cryptography slices
normally written in C, Rust, Go, Java, Python, protocol DSLs, or vendor crypto
APIs.

The replacement scope is secret types, capability-gated randomness, key
management adapters, password hashing, signatures, encryption wrappers,
protocol parsers, taint-safe sinks, constant-time annotations, vetted provider
bindings, and audit/test-vector artifacts.

## Requirements

- Secret-bearing values must have secret/taint labels, redaction policy, and
  sink restrictions.
- Randomness, key access, signing, encryption, decryption, hashing, network,
  filesystem, HSM/KMS, model, tool, and logging effects require capabilities.
- Crypto providers must be declared with algorithm, version, parameters, side
  channel policy, FFI boundary, and test vector evidence.
- Constant-time or side-channel-sensitive code must declare timing policy and
  emit analysis/audit evidence.
- Custom cryptographic primitives require explicit experimental/review status,
  test vectors, and proof/audit records.
- Protocol parsers must be memory safe, taint-aware, and fuzz/test-vector
  covered.
- Logs, traces, prompts, and diagnostics must redact secrets.

## Dependencies

- `SAFE10`, `SAFE11`, `SAFE13`, `R11`, and `R12` define capabilities, taint,
  AI/tool safety, runtime enforcement, and observability redaction.
- `B2`, `B3`, `B4`, `R10`, and `B13` define native/Wasm/FFI/artifact support.
- Testing, formal, package, and governance phases define audit, fuzz, and review
  requirements.

## Outputs and Artifacts

- Security/crypto domain manifest.
- Secret and taint policy report.
- Randomness capability manifest.
- Crypto provider manifest.
- Constant-time analysis report.
- Test vector and fuzz fixtures.
- FFI safe-wrapper audit.
- Redaction conformance report.
- Security/crypto diagnostics.

## Domain Manifest

```clojure
{:domain :security-crypto
 :profiles #{:core :native :hosted :formal}
 :backends #{:llvm :c :wasm}
 :artifacts #{:constant-time-report :test-vectors :fuzz-fixtures
              :taint-policy :provider-audit}
 :examples #{:password-hash :signature :encrypted-storage :protocol-parser}
 :rejects #{:secret-log :insecure-random :unreviewed-custom-crypto
            :timing-branch-on-secret}}
```

## Replacement Scope

Gravity should replace:

- secret-safe application crypto wrappers,
- password hashing and token signing APIs,
- encrypted storage adapters,
- secure random provider calls,
- protocol parsers,
- constant-time low-level routines when evidence is available,
- redaction and taint policies.

Vetted external crypto libraries and HSM/KMS systems remain providers.

## Minimum End-to-End Slice

The first complete slice is token signing:

- Gravity source declares signing key secret, randomness capability when needed,
  provider algorithm, and redaction policy.
- Runtime capability checks grant key access only to the signing function.
- Provider wrapper emits FFI/host boundary manifest and test vectors.
- Observability fixture proves key material is redacted.
- Negative fixture rejects logging or prompting with the secret.

## Diagnostics

Security/crypto diagnostics use `DOM15` identifiers:

- `DOM15-SECRET` for secret flow into logs, prompts, traces, errors, or public
  artifacts.
- `DOM15-RANDOM` for crypto/random use without approved randomness capability.
- `DOM15-PROVIDER` for undeclared or unsupported crypto provider behavior.
- `DOM15-CONSTANT-TIME` for secret-dependent timing branches or memory access
  where policy forbids them.
- `DOM15-CUSTOM` for custom primitives without review/test vectors.
- `DOM15-TAINT` for unvalidated protocol or external inputs reaching trusted
  sinks.
- `DOM15-FFI` for unsafe provider boundaries.
- `DOM15-CONFORMANCE` for missing test vectors, fuzzing, or audit evidence.

Diagnostics must include source span, secret id or provider id, algorithm,
effect, capability, taint category, missing evidence, and remediation.

## Rejected Designs

Gravity rejects secret leakage into logs, prompts, traces, and diagnostics.

Gravity rejects insecure randomness for cryptographic use.

Gravity rejects custom crypto by default.

Gravity rejects constant-time claims without analysis or audit evidence.

Gravity rejects provider bindings without test vectors and boundary metadata.

## Conformance Criteria

A conforming security/crypto slice must demonstrate:

- password hash, signature, encrypted storage, and protocol-parser examples,
- secret redaction and taint-flow tests,
- randomness capability enforcement,
- provider manifests and test vectors,
- constant-time/audit reports where required,
- fuzz fixtures for parsers,
- rejection of secret logs, insecure randomness, unreviewed custom crypto, and
  unsafe provider wrappers.
