# STD18 - Cryptography Library Specification

Sequence: 228
Phase: 16 - Standard Library
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

`gravity.crypto` defines cryptographic hashes, MACs, signatures, key exchange, encryption, authenticated encryption, KDFs, password hashing, randomness, key storage handles, constant-time helpers, and algorithm policy.
The library must make cryptographic use safer than ad hoc byte manipulation while preserving explicit algorithm, provider, key, randomness, side-channel, and policy metadata.

Cryptography is security-sensitive standard library code.
It must default to approved high-level constructions, isolate low-level primitives, reject ambiguous algorithms, protect secrets in diagnostics and artifacts, and provide audit evidence for unsafe or constant-time internals.

## Requirements

- Cryptographic operations MUST declare algorithm, mode, provider, key type, nonce/IV policy, randomness provider, and output format.
- Secrets MUST be typed values with redaction, lifetime, memory, and serialization restrictions.
- Randomness APIs MUST distinguish cryptographic randomness from deterministic PRNGs and test seeds.
- Constant-time APIs MUST avoid secret-dependent branches, memory access, timing, and diagnostics where required by the algorithm policy.
- Low-level primitives MUST be marked hazardous or restricted unless wrapped by approved constructions.
- Custom algorithms MUST be rejected unless a policy explicitly allows audited experimental use.
- Host crypto providers MAY be delegated to only with provider identity, version, FIPS or policy mode where relevant, and error mapping artifacts.
- Serialization of keys, signatures, ciphertexts, and hashes MUST use STD10 schemas and redaction policy.
- Formal or high-assurance profiles MUST require proof or external verification artifacts for selected primitives.
- Optimizations MUST preserve constant-time and secret-handling properties.

## Module Surface

- Hashes and MACs: `hash`, `digest`, `hmac`, `mac`, `verify-mac`, and `hash-policy`.
- Signatures: `keypair`, `public-key`, `sign`, `verify`, `signature`, and `signature-policy`.
- Encryption: `encrypt`, `decrypt`, `aead-encrypt`, `aead-decrypt`, `nonce`, `associated-data`, and `ciphertext`.
- Key agreement and KDFs: `derive-key`, `key-exchange`, `kdf`, `hkdf`, `password-hash`, and `salt`.
- Randomness: `crypto-random`, `random-bytes`, `random-provider`, and `entropy-source`.
- Secrets: `secret`, `with-secret`, `zeroize`, `key-handle`, `redact-secret`, and `secret-scope`.
- Constant-time helpers: `constant-time=`, `ct-select`, `ct-copy`, and `constant-time-proof`.
- Policy: `algorithm-policy`, `provider-policy`, `allowed-algorithm?`, and `crypto-artifact`.

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

- Crypto module manifest with algorithms, providers, capabilities, secret policies, and profile matrix.
- Algorithm policy artifacts with approved, deprecated, experimental, and forbidden algorithms.
- Provider delegation records with identity, version, mode, target, and error mapping.
- Constant-time proof or audit artifacts for sensitive implementations.
- Test vectors for hashes, MACs, signatures, KDFs, AEAD, randomness adapters, and redaction.
- Negative fixtures for secret leakage, custom algorithm use, missing random provider, nonce reuse, and unsafe low-level primitives.
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

## Conformance Criteria

- Approved algorithms have test vectors and policy artifacts.
- Secret values redact consistently in diagnostics, logs, test reports, and package artifacts.
- Randomness fixtures distinguish crypto providers, deterministic tests, and unavailable entropy.
- Constant-time fixtures or external audit artifacts cover sensitive operations.
- Provider delegation records reproduce Gravity diagnostics and policy decisions.
- Restricted profiles reject unsupported providers and hidden allocation.
- Serialization fixtures preserve schemas and secret redaction.
- Documentation examples compile only with required crypto capabilities.
