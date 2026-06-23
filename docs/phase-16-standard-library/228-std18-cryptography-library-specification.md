# STD18 - Cryptography Library Specification

Sequence: 228
Phase: 16 - Standard Library
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

`gravity.crypto` defines cryptographic hashes, MACs, signatures, key exchange, encryption, authenticated encryption, KDFs, password hashing, randomness, key storage handles, constant-time helpers, and algorithm policy.
The library must make cryptographic use safer than ad hoc byte manipulation while preserving explicit algorithm, provider, key, randomness, side-channel, and policy metadata.
It also defines post-quantum and crypto-agile policy surfaces so code can migrate algorithms without hiding cryptographic meaning behind opaque provider names.

Cryptography is security-sensitive standard library code.
It must default to approved high-level constructions, isolate low-level primitives, reject ambiguous algorithms, protect secrets in diagnostics and artifacts, and provide audit evidence for unsafe or constant-time internals.

## Requirements

- Cryptographic operations MUST declare algorithm, mode, provider, key type, nonce/IV policy, randomness provider, and output format.
- Secrets MUST be typed values with redaction, lifetime, memory, and serialization restrictions.
- Randomness APIs MUST distinguish cryptographic randomness from deterministic PRNGs and test seeds.
- Constant-time APIs MUST avoid secret-dependent branches, memory access, timing, and diagnostics where required by the algorithm policy.
- Low-level primitives MUST be marked hazardous or restricted unless wrapped by approved constructions.
- Custom algorithms MUST be rejected unless a policy explicitly allows audited experimental use.
- Post-quantum algorithms MUST be declared by family, parameter set, lifecycle state, provider, and serialization format.
- Hybrid classical and post-quantum key establishment MUST declare each component algorithm, transcript binding, KDF combiner, failure behavior, and downgrade policy.
- Algorithm lifecycle states MUST be machine-readable policy facts rather than comments or provider-specific strings.
- Crypto-agile protocols MUST support policy-driven algorithm replacement without changing application data schemas except where the policy records a deliberate migration break.
- Host crypto providers MAY be delegated to only with provider identity, version, FIPS or policy mode where relevant, and error mapping artifacts.
- Serialization of keys, signatures, ciphertexts, and hashes MUST use STD10 schemas and redaction policy.
- Formal or high-assurance profiles MUST require proof or external verification artifacts for selected primitives.
- Optimizations MUST preserve constant-time and secret-handling properties.

## Module Surface

- Hashes and MACs: `hash`, `digest`, `hmac`, `mac`, `verify-mac`, and `hash-policy`.
- Signatures: `keypair`, `public-key`, `sign`, `verify`, `signature`, `ml-dsa`, `slh-dsa`, `signature-policy`, and `signature-lifecycle`.
- Encryption: `encrypt`, `decrypt`, `aead-encrypt`, `aead-decrypt`, `nonce`, `associated-data`, and `ciphertext`.
- Key agreement and KDFs: `derive-key`, `key-exchange`, `ml-kem`, `hybrid-key-exchange`, `kdf`, `hkdf`, `password-hash`, and `salt`.
- Randomness: `crypto-random`, `random-bytes`, `random-provider`, and `entropy-source`.
- Secrets: `secret`, `with-secret`, `zeroize`, `key-handle`, `redact-secret`, and `secret-scope`.
- Constant-time helpers: `constant-time=`, `ct-select`, `ct-copy`, and `constant-time-proof`.
- Policy: `algorithm-policy`, `provider-policy`, `allowed-algorithm?`, `algorithm-lifecycle`, `migration-inventory`, and `crypto-artifact`.

## Dependencies

- `L5`, `L6`, and `L11` for effects, capabilities, and resource ownership.
- `SAFE1`, `SAFE2`, `SAFE5`, `SAFE7`, `SAFE10`, `SAFE11`, and `SAFE15` for memory safety, resources, FFI, capability security, taint, and proof-carrying libraries.
- `P2`, `P5`, `P4`, `P3`, and `P12` for core, native, hosted, meta, and formal profile behavior.
- `STD4` and `STD10` for text, bytes, encodings, schemas, and serialization.
- `STD6` and `STD16` for secret memory, resources, and random providers.
- `PKG8`, `PKG10`, and `PKG12` for safety metadata, provenance, signing, and SBOMs.
- `GOV4` and `GOV9` for security and unsafe-code review.

## Example

```clojure
(ns sample.crypto
  (:require [gravity.crypto :as crypto])
  (:profile :native))

(defn seal [cap key plaintext aad]
  (crypto/aead-encrypt cap
    {:algorithm :xchacha20-poly1305
     :key key
     :plaintext plaintext
     :associated-data aad}))
```

The capability supplies randomness and provider authority.
The algorithm policy defines nonce handling and output schema.
Secrets are redacted from diagnostics and artifacts.

## Post-Quantum and Crypto Agility

NIST-style post-quantum families are first-class algorithm families, not aliases for provider defaults.
`ML-KEM` is the standard key-establishment family for post-quantum KEM use.
`ML-DSA` and `SLH-DSA` are the standard post-quantum signature families.
`FN-DSA`, `FALCON`, and `HQC` MUST be represented as future or experimental families until the governing algorithm policy upgrades their lifecycle state.

Algorithm lifecycle states are:

- `:approved` for algorithms accepted by the active policy and profile.
- `:transitional` for algorithms allowed only with migration evidence or hybrid composition.
- `:deprecated` for algorithms accepted only to read or verify existing artifacts.
- `:experimental` for audited research, interop, or preview use under explicit policy.
- `:future` for named algorithms reserved for later standardization or provider support.
- `:forbidden` for algorithms rejected in new code and rejected for legacy use unless a recovery policy permits read-only handling.

Crypto-agile declarations MUST include stable algorithm identifiers, parameter sets, provider policy, output schemas, lifecycle state, and migration guidance.
Policy must distinguish classical, post-quantum, and hybrid suites instead of accepting a generic `:default`, `:secure`, or provider-chosen algorithm.
Hybrid key establishment MUST combine a classical algorithm and a post-quantum KEM through an approved combiner that binds both public keys, ciphertexts, transcript context, provider identities, and error paths before deriving application keys.

Migration inventory records MUST identify every algorithm-bearing surface:

- source declarations and generated code,
- package manifests, SBOMs, and provider delegation records,
- serialized keys, certificates, signatures, ciphertexts, transcripts, and protocol messages,
- persisted database fields and wire schemas that carry algorithm identifiers,
- tests, fixtures, interoperability vectors, and formal or audit artifacts.

The inventory MUST record replacement target, lifecycle deadline, compatibility mode, verification evidence, and whether old artifacts are read-only, re-signable, re-encryptable, or unrecoverable.

## Profile Availability

- `:core` receives pure hashing, encoding-independent digest values, constant-time comparison, and policy data where no secret provider is needed.
- `:firmware` and `:kernel` receive crypto only with explicit randomness, memory, and provider constraints.
- `:native` receives full crypto APIs under provider and randomness capabilities.
- `:hosted` may delegate to host crypto providers with provider records.
- `:distributed` may use crypto only when randomness, time, and key access are replay-safe or activity-isolated.
- `:ai` may use crypto through tools only when policy permits secret handling.
- `:meta` may inspect crypto policy artifacts but must not expose secrets during compilation.
- `:formal` may require proof artifacts for selected algorithms and constant-time properties.

## Outputs and Artifacts

- Crypto module manifest with algorithms, families, parameter sets, providers, capabilities, secret policies, lifecycle states, and profile matrix.
- Algorithm policy artifacts with approved, transitional, deprecated, experimental, future, and forbidden algorithms.
- Provider delegation records with identity, version, mode, target, error mapping, parameter support, and side-channel claims.
- Post-quantum policy records for ML-KEM, ML-DSA, SLH-DSA, and future or experimental FN-DSA, FALCON, and HQC.
- Hybrid suite artifacts with classical component, post-quantum component, combiner, transcript binding, and downgrade policy.
- Migration inventory artifacts for algorithm-bearing source, packages, schemas, persisted data, and protocol surfaces.
- Constant-time proof or audit artifacts for sensitive implementations.
- Test vectors for hashes, MACs, signatures, KDFs, AEAD, randomness adapters, post-quantum algorithms, hybrid combiners, and redaction.
- Provider policy fixtures that prove accepted and rejected algorithm lifecycles across native, hosted, formal, and restricted profiles.
- Negative fixtures for secret leakage, custom algorithm use, missing random provider, nonce reuse, ambiguous lifecycle state, non-agile declarations, and unsafe low-level primitives.
- SBOM and provenance links for bundled or linked crypto code.

## Diagnostics

- `STD18001` when an algorithm, mode, provider, key type, or nonce policy is ambiguous.
- `STD18002` when a secret would be logged, serialized, copied, or retained without policy.
- `STD18003` when cryptographic randomness is required but unavailable.
- `STD18004` when nonce or IV rules are violated.
- `STD18005` when a low-level primitive is used outside approved wrappers.
- `STD18006` when constant-time policy is violated by secret-dependent behavior.
- `STD18007` when host provider behavior lacks identity, version, or mode artifacts.
- `STD18008` when optimization invalidates side-channel or secret-handling evidence.
- `STD18009` when an algorithm lifecycle state is missing, ambiguous, stale, or inconsistent with provider policy.
- `STD18010` when crypto code relies on non-agile defaults, provider-chosen algorithms, or undeclared parameter sets.
- `STD18011` when hybrid key establishment lacks component identities, transcript binding, combiner policy, or downgrade handling.
- `STD18012` when a migration inventory omits an algorithm-bearing surface or lacks replacement evidence.
- `STD18013` when post-quantum or hybrid test vectors, provider policy, or conformance evidence do not match the declared algorithm family.

## Conformance Criteria

- Approved algorithms have test vectors and policy artifacts.
- Post-quantum ML-KEM, ML-DSA, and SLH-DSA fixtures cover key generation, serialization, success, failure, redaction, provider policy, and lifecycle metadata.
- FN-DSA, FALCON, and HQC fixtures prove future or experimental status is rejected by default and accepted only under explicit audited policy.
- Hybrid key-establishment fixtures prove both components are bound into the transcript and that downgrade, partial failure, and provider mismatch paths are rejected.
- Algorithm lifecycle fixtures exercise approved, transitional, deprecated, experimental, future, and forbidden states across profiles.
- Migration inventory fixtures identify algorithm-bearing source, package, schema, persisted-data, and protocol surfaces with replacement evidence.
- Secret values redact consistently in diagnostics, logs, test reports, and package artifacts.
- Randomness fixtures distinguish crypto providers, deterministic tests, and unavailable entropy.
- Constant-time fixtures or external audit artifacts cover sensitive operations.
- Provider delegation records reproduce Gravity diagnostics and policy decisions.
- Non-agile defaults, ambiguous algorithm names, missing parameter sets, and provider-chosen algorithms produce stable diagnostics.
- Restricted profiles reject unsupported providers and hidden allocation.
- Serialization fixtures preserve schemas and secret redaction.
- Documentation examples compile only with required crypto capabilities.
