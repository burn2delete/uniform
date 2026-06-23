# DOM16 - Blockchain and Smart Contract Domain Specification

Sequence: 139
Phase: 9 - Domain-Specific Computing Coverage
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This domain proves that Gravity can cover blockchain and smart-contract slices
normally written in Solidity, Vyper, Move, Rust, Go, TypeScript, or chain SDK
DSLs.

The replacement scope is deterministic contract logic, chain ABI schemas,
state schemas, transaction/message schemas, gas/resource reports, contract Wasm,
indexers, wallet/client bindings, invariant tests, and upgrade/migration
artifacts.

## Requirements

- Contract execution must use deterministic profiles and reject unrecorded
  clock, randomness, network, filesystem, model, tool, process, or host effects.
- Contract state, messages, events, and ABI boundaries must have schemas and
  versioning policy.
- Arithmetic, serialization, canonicalization, resource/gas accounting, and
  storage behavior must be explicit.
- State transitions require capability and authorization checks appropriate to
  the chain model.
- Upgrades require compatibility, migration, and replay/invariant evidence.
- Chain clients and indexers may use distributed/hosted profiles but must keep
  provider effects and replay records explicit.

## Dependencies

- `P2`, `P9`, `P12`, and `P13` define core, distributed, formal, and
  compatibility rules.
- `B4`, `B10`, `B11`, and `B13` define Wasm, workflow, query, and artifact
  output.
- `SAFE9`, `SAFE10`, `SAFE11`, and formal/test phases define numeric,
  capability, taint, invariant, and conformance requirements.

## Outputs and Artifacts

- Blockchain domain manifest.
- Contract artifact or Wasm module.
- Chain ABI schema.
- State and event schema bundle.
- Gas/resource report.
- Determinism proof record.
- Invariant/property test report.
- Upgrade/migration record.
- Chain client/indexer artifact.
- Blockchain diagnostics.

## Domain Manifest

```clojure
{:domain :blockchain-smart-contracts
 :profiles #{:core :formal :distributed}
 :backends #{:wasm :workflow-graph :query-relational}
 :artifacts #{:contract-wasm :chain-abi :state-schema
              :gas-report :determinism-proof}
 :examples #{:token-transfer :escrow :governance-vote :indexer}
 :rejects #{:nondeterministic-contract-effect :unchecked-overflow
            :abi-upgrade-without-migration :unauthorized-state-mutation}}
```

## Replacement Scope

Gravity should replace:

- token and escrow contracts,
- deterministic state transitions,
- ABI/schema generation,
- indexer schemas and queries,
- wallet/client bindings,
- invariant and property tests,
- migration and upgrade manifests.

Chain validators, consensus clients, and remote nodes remain provider
boundaries unless implemented as separate Gravity domains.

## Minimum End-to-End Slice

The first complete slice is a token transfer contract:

- Gravity source declares balances schema, transfer message, authorization,
  checked arithmetic, and gas/resource policy.
- Compiler rejects clock/random/network effects.
- Wasm or chain backend emits contract artifact and ABI.
- Property tests prove conservation and insufficient-balance rejection.
- Upgrade fixture validates schema compatibility.

## Diagnostics

Blockchain diagnostics use `DOM16` identifiers:

- `DOM16-DETERMINISM` for forbidden nondeterministic effects.
- `DOM16-SCHEMA` for missing ABI, state, event, or transaction schemas.
- `DOM16-NUMERIC` for unchecked overflow or invalid resource arithmetic.
- `DOM16-GAS` for missing gas/resource accounting.
- `DOM16-AUTH` for unauthorized state mutation.
- `DOM16-UPGRADE` for incompatible ABI/state upgrades without migration.
- `DOM16-INVARIANT` for missing or failed property evidence.
- `DOM16-CONFORMANCE` for missing chain/runtime compatibility evidence.

Diagnostics must include contract/function id, source span, schema id,
capability, numeric mode, gas record, missing proof/artifact, and remediation.

## Rejected Designs

Gravity rejects nondeterministic smart-contract execution.

Gravity rejects unchecked contract arithmetic.

Gravity rejects ABI/state upgrades without migration evidence.

Gravity rejects state mutation without declared authorization.

Gravity rejects contracts without schemas and invariant tests.

## Conformance Criteria

A conforming blockchain slice must demonstrate:

- token, escrow, or governance contract examples,
- deterministic contract artifacts and ABI schemas,
- checked arithmetic and gas/resource reports,
- property/invariant tests,
- upgrade/migration fixtures,
- rejection of nondeterminism, unchecked overflow, unauthorized mutation, and
  incompatible upgrades.
