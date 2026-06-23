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
encrypted value types, WebAuthn/passkey credential ceremonies,
private-computation adapters, protocol parsers, taint-safe sinks, constant-time
annotations, vetted provider bindings, and audit/test-vector artifacts.

## Requirements

- Secret-bearing values must have secret/taint labels, redaction policy, and
  sink restrictions.
- Randomness, key access, signing, encryption, decryption, hashing, network,
  filesystem, HSM/KMS, model, tool, and logging effects require capabilities.
- Crypto providers must be declared with algorithm, version, parameters, side
  channel policy, FFI boundary, and test vector evidence.
- WebAuthn, FIDO, and passkey providers must declare relying-party id, origin
  policy, challenge generation, user-presence and user-verification policy,
  authenticator attachment, transport, attestation conveyance, credential
  backup/sync state, device-bound versus synced-passkey policy, resident or
  discoverable credential policy, and privacy leakage model.
- FHE, MPC, threshold, secure-enclave, and private-computation providers must be
  declared with scheme/protocol, participants, key custody, parameter set,
  leakage model, capability surface, and audit evidence.
- Encrypted values must distinguish plaintext, ciphertext, secret shares,
  commitments, blinded values, and revealed/public outputs in the type system.
- FHE scheme parameters must include security level, polynomial/ring settings,
  modulus chain, scale/precision policy, batching policy, bootstrapping policy,
  supported operations, maximum multiplicative depth, and noise budget rules.
- MPC scheme parameters must include field/ring, party set, threshold, adversary
  model, preprocessing/triple source, transcript policy, and reveal rules.
- Plaintext/ciphertext boundary crossings require explicit encrypt, decrypt,
  reveal, re-randomize, share, reconstruct, or attest operations with
  capabilities and audit records.
- Compiler/runtime checks must reject private-computation programs that exceed
  declared noise budget, depth, precision, participant, transcript, or provider
  constraints.
- Diagnostics must report privacy leakage through outputs, metadata, shape,
  access patterns, timing, transcripts, prompts, logs, or provider boundaries.
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
- Private-computation provider manifest.
- WebAuthn/passkey credential policy manifest.
- Registration and authentication ceremony transcript fixtures.
- Encrypted value and plaintext/ciphertext boundary report.
- Scheme parameter, noise/depth, and leakage diagnostics.
- Constant-time analysis report.
- Test vector and fuzz fixtures.
- Private-computation test vectors, transcript fixtures, and audit evidence.
- FFI safe-wrapper audit.
- Redaction conformance report.
- Security/crypto diagnostics.

## Domain Manifest

```clojure
{:domain :security-crypto
 :profiles #{:core :native :hosted :formal}
 :backends #{:llvm :c :wasm}
 :artifacts #{:constant-time-report :test-vectors :fuzz-fixtures
              :taint-policy :provider-audit :private-compute-report
              :webauthn-passkey-policy :credential-ceremony-transcript
              :leakage-diagnostics}
 :examples #{:password-hash :signature :encrypted-storage :protocol-parser
             :passkey-login :webauthn-registration :fhe-evaluation
             :mpc-aggregation}
 :rejects #{:secret-log :insecure-random :unreviewed-custom-crypto
            :timing-branch-on-secret :implicit-decrypt
            :webauthn-origin-mismatch :passkey-policy-mismatch
            :noise-budget-exceeded :unaudited-reveal}}
```

## Replacement Scope

Gravity should replace:

- secret-safe application crypto wrappers,
- password hashing and token signing APIs,
- encrypted storage adapters,
- WebAuthn and passkey registration, authentication, decommissioning, and
  relying-party credential policy adapters,
- encrypted value APIs for FHE, MPC, threshold, and private-computation
  workflows,
- secure random provider calls,
- protocol parsers,
- constant-time low-level routines when evidence is available,
- redaction and taint policies.

Vetted external crypto libraries, HSM/KMS systems, FHE runtimes, MPC networks,
threshold signing services, and secure-enclave systems remain providers.

## WebAuthn and Passkey Credential Model

Gravity models WebAuthn and passkeys as capability-gated public-key credential
ceremonies, not as ambient browser or platform authentication.

A WebAuthn/passkey provider manifest must record:

- relying-party id, expected origins, cross-origin and iframe policy, and
  related-origin policy where supported;
- challenge source, freshness window, replay cache, and ceremony id;
- credential id, public-key algorithm, COSE key metadata, signature counter or
  counter policy, and credential lifecycle state;
- user-presence and user-verification requirements, including whether the
  authenticator, platform, or relying party enforces each requirement;
- authenticator attachment, transport, resident/discoverable credential mode,
  and client capability assumptions;
- attestation conveyance, attestation statement format, trust path, revocation
  and metadata policy, and when `none` or self attestation is acceptable;
- credential backup eligibility and backup state, including whether the
  credential is a synced passkey, device-bound passkey, roaming authenticator,
  or enterprise-managed credential;
- account binding, account recovery, credential rotation, decommissioning, and
  unknown-credential signaling policy;
- privacy labels for credential ids, user handles, attestation data,
  authenticator metadata, client capabilities, and cross-origin use.

Registration and authentication ceremony transcripts are audit artifacts. They
must include public challenge data, relying-party inputs, client data hash,
authenticator data, assertion or attestation verification result, provider
version, and redaction policy. They must not include private keys, biometric
data, unrevealed local authenticator state, or raw user secrets.

Gravity distinguishes synced passkeys from device-bound credentials. A synced
passkey may improve recovery and multi-device usability, but it must not be used
as proof that one physical device or hardware key was present unless the
attestation and provider policy explicitly establish that property. A
device-bound or enterprise credential may carry stronger custody assumptions,
but the relying party must still declare the accepted trust roots and
attestation privacy tradeoff.

Boundary rules are explicit:

- creating a credential requires a fresh challenge, relying-party policy,
  user-consent boundary, and credential-storage decision;
- asserting a credential requires origin and relying-party id validation,
  challenge replay protection, signature verification, user-presence and
  user-verification checks, and account-binding validation;
- decommissioning, unknown-credential signaling, and accepted-credential
  signaling are credential lifecycle operations with audit records;
- attestation may influence policy only through a declared trust model and
  accepted root or metadata source;
- credential ids, user handles, and attestation data are privacy-sensitive
  values subject to taint and redaction policy.

Negative fixtures reject reused challenges, origin or relying-party mismatch,
authentication without signature verification, relying on user verification when
the provider did not prove it, treating synced credentials as device-bound, and
using attestation as a tracking identifier outside declared policy.

## Private Computation Model

Gravity must model private computation as typed computation over protected
values, not as opaque calls hidden behind a stringly provider API.

Encrypted value types must carry:

- protection kind: plaintext, ciphertext, secret share, commitment, blinded
  value, attested enclave value, or public/revealed value,
- scheme or protocol id,
- parameter set and security level,
- key id, owner, custody location, rotation policy, and threshold policy,
- allowed operation set and provider capability,
- noise budget, multiplicative depth, precision, scale, or transcript budget
  where the scheme needs them,
- leakage label for value, metadata, shape, timing, access pattern, transcript,
  prompt, and output leakage.

Boundary rules are explicit:

- encryption moves plaintext into ciphertext or shares only through a declared
  provider capability and key custody policy,
- decryption, reveal, reconstruct, compare, branch, serialize, prompt, log, or
  export operations require a boundary operation with audit evidence,
- ciphertext/plaintext mixing is rejected unless the scheme explicitly supports
  it and the result type records the new leakage and noise/depth state,
- provider calls must return updated budget/depth/precision metadata or prove
  that the operation is budget-neutral,
- public outputs must carry a reveal reason, participant set, and leakage
  summary.

Provider manifests for FHE and MPC must name the implementation, version,
scheme/protocol, parameter set, supported operation subset, unsupported
operations, key custody mode, participant roles, network/transcript effects,
side-channel policy, test vectors, and independent review/audit status.

## Private Computation Slice

The first private-computation slice is encrypted aggregation:

- Gravity source declares encrypted input type, FHE or MPC provider capability,
  scheme parameters, key custody, participant set, and leakage policy.
- Type checking proves only approved arithmetic is applied to ciphertexts or
  shares.
- Compiler/runtime evidence proves noise budget, depth, precision, transcript,
  and reveal constraints remain inside declared limits.
- Provider wrapper emits parameter manifest, boundary manifest, test vectors,
  transcript fixtures where applicable, and audit evidence.
- Negative fixtures reject implicit decrypt, secret-dependent branching on
  protected values, logging ciphertext metadata beyond policy, provider use
  without custody evidence, and operations that exceed noise/depth limits.

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
- `DOM15-WEBAUTHN` for WebAuthn registration or authentication ceremonies that
  omit relying-party id, origin, challenge, signature, user-verification,
  user-presence, or attestation policy checks.
- `DOM15-PASSKEY` for passkey providers that omit synced versus device-bound
  credential policy, backup state, discoverable credential policy, lifecycle
  handling, or privacy labels.
- `DOM15-PRIVATE-COMPUTE` for encrypted value, FHE, MPC, threshold, or enclave
  computation that lacks a declared scheme/protocol, participant set, parameter
  set, capability, or provider evidence.
- `DOM15-BOUNDARY` for implicit plaintext/ciphertext crossings, unauthorized
  decrypt/reveal/reconstruct/export operations, or missing boundary audit
  records.
- `DOM15-NOISE` for FHE or approximate-encryption operations that exceed
  declared noise, depth, scale, precision, or bootstrapping constraints.
- `DOM15-LEAKAGE` for unapproved leakage through outputs, metadata, shape,
  access patterns, timing, transcripts, prompts, logs, or provider boundaries.
- `DOM15-CUSTODY` for missing or inconsistent key owner, key custody, threshold,
  rotation, participant, or attestation evidence.
- `DOM15-CONSTANT-TIME` for secret-dependent timing branches or memory access
  where policy forbids them.
- `DOM15-CUSTOM` for custom primitives without review/test vectors.
- `DOM15-TAINT` for unvalidated protocol or external inputs reaching trusted
  sinks.
- `DOM15-FFI` for unsafe provider boundaries.
- `DOM15-CONFORMANCE` for missing test vectors, fuzzing, or audit evidence.

Diagnostics must include source span, secret id or provider id, algorithm,
scheme/protocol, parameter set, key custody, relying-party id, origin,
credential id, attestation policy, effect, capability, taint category, leakage
category, budget/depth state, participant set, missing evidence, and remediation.

## Rejected Designs

Gravity rejects secret leakage into logs, prompts, traces, and diagnostics.

Gravity rejects insecure randomness for cryptographic use.

Gravity rejects custom crypto by default.

Gravity rejects constant-time claims without analysis or audit evidence.

Gravity rejects provider bindings without test vectors and boundary metadata.

Gravity rejects WebAuthn/passkey ceremonies that omit origin and relying-party
checks, challenge replay protection, signature verification, user-presence and
user-verification policy, credential lifecycle state, or privacy labels.

Gravity rejects treating synced passkeys as device-bound credentials unless the
declared provider and attestation policy prove equivalent custody.

Gravity rejects private-computation providers without scheme parameters, key
custody, participant/leakage policy, test vectors, and audit evidence.

Gravity rejects implicit decryption, reveal, reconstruction, export, prompt, or
logging at plaintext/ciphertext boundaries.

Gravity rejects FHE/MPC programs whose declared parameter set cannot support the
requested operation depth, precision, participant model, transcript policy, or
leakage budget.

## Conformance Criteria

A conforming security/crypto slice must demonstrate:

- password hash, signature, encrypted storage, and protocol-parser examples,
- secret redaction and taint-flow tests,
- randomness capability enforcement,
- provider manifests and test vectors,
- WebAuthn/passkey registration, authentication, and decommissioning fixtures,
- relying-party origin, challenge, attestation, user-verification, backup-state,
  synced-passkey, and device-bound credential policy tests,
- encrypted value type checking,
- FHE/MPC/provider capability enforcement,
- scheme parameter manifests covering noise budget, depth, precision, key
  custody, participant set, and leakage policy,
- plaintext/ciphertext boundary tests for encrypt, decrypt, reveal,
  reconstruct, export, prompt, log, and serialization paths,
- privacy leakage diagnostics for output, metadata, shape, access pattern,
  timing, transcript, prompt, log, and provider-boundary leakage,
- provider test vectors, MPC transcript fixtures where applicable, and
  independent audit/review evidence,
- constant-time/audit reports where required,
- fuzz fixtures for parsers,
- rejection of secret logs, insecure randomness, unreviewed custom crypto, and
  unsafe provider wrappers,
- rejection of reused WebAuthn challenges, origin mismatch, missing
  user-verification evidence, unsafe attestation reliance, synced/device-bound
  policy mismatch, and credential privacy leakage,
- rejection of implicit decrypt/reveal, missing key custody evidence,
  unaudited private-computation providers, and noise/depth budget violations.
