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
indexers, account-abstraction schemas, programmable-account bindings,
wallet/client bindings, invariant tests, and upgrade/migration artifacts.

## Requirements

- Contract execution must use deterministic profiles and reject unrecorded
  clock, randomness, network, filesystem, model, tool, process, or host effects.
- Contract state, messages, events, and ABI boundaries must have schemas and
  versioning policy.
- Arithmetic, serialization, canonicalization, resource/gas accounting, and
  storage behavior must be explicit.
- Transaction ordering assumptions must be explicit for order-sensitive
  contracts, including mempool visibility, block/order construction, sequencer
  or builder trust, private order flow, and fair-ordering or commit-reveal
  requirements.
- Account abstraction must make account validation logic,
  user-operation/authorization schemas, session keys, paymasters, delegated
  execution, replay domains, nonces, bundler assumptions, and simulation
  assumptions explicit.
- Ethereum account-abstraction profiles must declare the selected standard and
  version, including ERC-4337 user-operation flows, EIP-7702 EOA delegation
  authorizations, and ERC-7579 modular smart-account interfaces when used.
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
- Transaction-ordering and MEV exposure report.
- Account-abstraction and programmable-account report.
- Ethereum account-abstraction profile manifest.
- User-operation, authorization, session-key, sponsorship, and replay schemas.
- Invariant/property test report.
- Upgrade/migration record.
- Chain client/indexer artifact.
- Wallet/client binding artifact for programmable accounts.
- Blockchain diagnostics.

## Domain Manifest

```clojure
{:domain :blockchain-smart-contracts
 :profiles #{:core :formal :distributed}
 :backends #{:wasm :workflow-graph :query-relational}
 :artifacts #{:contract-wasm :chain-abi :state-schema
              :gas-report :determinism-proof :ordering-report
              :account-validation :user-operation-schema
              :ethereum-account-abstraction-profile
              :erc-4337-entrypoint-binding :eip-7702-authorization-schema
              :erc-7579-module-manifest
              :wallet-client-binding}
 :examples #{:token-transfer :escrow :governance-vote :indexer
             :programmable-account :erc-4337-wallet :eip-7702-delegation
             :erc-7579-modular-account}
 :rejects #{:nondeterministic-contract-effect :unchecked-overflow
            :abi-upgrade-without-migration :unauthorized-state-mutation
            :order-sensitive-contract-without-assumption
            :account-validation-without-replay-domain
            :account-abstraction-profile-mismatch}}
```

## Replacement Scope

Gravity should replace:

- token and escrow contracts,
- deterministic state transitions,
- ABI/schema generation,
- indexer schemas and queries,
- account validation and user-operation schemas,
- ERC-4337 user-operation and EntryPoint bindings,
- EIP-7702 EOA authorization and delegation schemas,
- ERC-7579 modular smart-account and module manifests,
- session-key, sponsorship, and delegated-execution policies,
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

## Account Abstraction and Programmable Accounts

Gravity models account abstraction as part of the contract and client boundary,
not as an implicit wallet convention.

Programmable accounts must declare:

- account validation logic, including signature schemes, multisig thresholds,
  policy predicates, guardian recovery, spending limits, and time windows;
- user-operation or authorization schemas, including signer identity, target,
  call data, value, fee fields, chain id, replay domain, nonce, expiry, and
  permitted capabilities;
- session keys, including scope, allowance, duration, revocation, rotation,
  storage location, and whether the key can delegate or sponsor execution;
- paymaster and sponsorship policies, including who pays, what is sponsored,
  quota, anti-drain limits, post-operation accounting, and failure behavior;
- delegated execution rules, including delegate identity, allowed entrypoints,
  capability transfer, storage access, reentrancy exposure, and revocation;
- replay domains and nonce semantics, including per-account, per-session,
  per-chain, per-entrypoint, cross-chain, batch, and upgrade interactions;
- bundler, relayer, aggregator, simulation, and mempool assumptions, including
  validation/execution split, state freshness, gas estimation, censorship,
  replacement, and inclusion failure behavior;
- wallet/client binding artifacts, including typed data, signing prompts,
  account manifests, chain/client feature negotiation, and user-operation
  encoding fixtures.

The account-abstraction report records which account code validated an
operation, which authorization schema was signed, which session key or sponsor
was used, which replay domain and nonce were consumed, which delegated
capability was exercised, which bundler or simulation assumptions were trusted,
and which wallet/client binding produced the user-visible signing artifact.

Paymasters, bundlers, relayers, aggregators, wallet clients, and account
plugins are provider boundaries unless implemented as Gravity components. A
contract or account that trusts those parties must name the trust assumption and
provide conformance evidence for the selected deployment profile.

### Named Ethereum Account-Abstraction Profiles

Gravity may target Ethereum account-abstraction standards through named profile
records. A profile record is not just a label; it pins the chain, standard,
version, contract addresses or discovery mechanism, schema hashes, and provider
assumptions used by generated account, wallet, and client artifacts.

An ERC-4337 profile must record:

- EntryPoint version, address or deployment policy, chain id, and user-operation
  hash domain;
- canonical `UserOperation` schema, fee fields, nonce key/sequence semantics,
  `initCode` or factory handling, and EIP-7702 flag handling when supported;
- account `validateUserOp` semantics, signature scheme, validation data,
  valid-after and valid-until time ranges, and rejection behavior;
- paymaster `validatePaymasterUserOp` and `postOp` policy, deposit/stake,
  quota, gas limit, and anti-drain rules;
- aggregator, signature aggregation, and alternative mempool assumptions when
  selected;
- bundler simulation phases, state freshness, reputation, replacement,
  censorship, inclusion failure, and gas estimation assumptions;
- wallet/client binding fixtures for typed data, signing prompts, RPC methods,
  and user-operation encoding.

An EIP-7702 profile must record:

- authorization tuple schema, including chain id, delegate address, account
  nonce, signature fields, and canonical signing domain;
- delegation indicator, delegate-code target, and code-loading semantics used
  by the selected chain revision;
- validity, revocation, replay, duplicate tuple, and failed-authorization
  behavior;
- interaction with account validation, ERC-4337 factory/init paths, wallet
  prompts, session keys, and delegated capabilities;
- gas, access-list, and authorization-list accounting expected by the client
  and bundler or transaction sender.

An ERC-7579 profile must record:

- account interface support, including ERC-165 and ERC-1271 compatibility where
  required by the deployment;
- module types and ids for validation, execution, fallback, and hook modules;
- module install, uninstall, initialization, configuration, and revocation
  schemas;
- which module may authorize a call, transform execution, intercept fallback,
  or observe post-call state;
- compatibility with ERC-4337 validation, session keys, paymasters, wallet
  client manifests, and upgrade policy.

The account-abstraction report records the selected profile ids and explains
how each generated artifact satisfies or intentionally does not target a named
standard. A claim of ERC-4337, EIP-7702, or ERC-7579 compatibility without the
required profile manifest is rejected.

## Transaction Ordering and MEV Effects

Gravity models transaction-order dependence as a contract effect, not as an
implicit property of the host chain.

Order-sensitive entrypoints must declare:

- mempool visibility, including public, encrypted, private relay, or sequencer
  visibility;
- ordering authority, including validator, builder, sequencer, batcher, bridge,
  or application-level queue assumptions;
- exposure to frontrun, backrun, sandwich, liquidation, oracle update, auction,
  governance-vote, bridge-message, or slippage-sensitive execution;
- slippage, price-impact, deadline, nonce, max-loss, and replay constraints
  that bound adverse ordering;
- private order flow assumptions and fallback behavior when private order flow
  is unavailable, censored, delayed, or leaked;
- fair-ordering, batch auction, commit-reveal, sealed bid, threshold encryption,
  or delayed-reveal mechanisms when the invariant depends on hidden intent or
  unbiased ordering.

The ordering report records which functions read ordering-sensitive state,
which external observations can change between signing and execution, which
parties can observe or reorder transactions, and which mitigation proves the
contract remains safe under the declared chain model.

Private relays, builders, sequencers, and off-chain batchers are provider
boundaries unless implemented as Gravity components. Contracts that trust those
parties must name the trust assumption and provide conformance evidence for the
selected deployment profile.

## Diagnostics

Blockchain diagnostics use `DOM16` identifiers:

- `DOM16-DETERMINISM` for forbidden nondeterministic effects.
- `DOM16-SCHEMA` for missing ABI, state, event, or transaction schemas.
- `DOM16-NUMERIC` for unchecked overflow or invalid resource arithmetic.
- `DOM16-GAS` for missing gas/resource accounting.
- `DOM16-AUTH` for unauthorized state mutation.
- `DOM16-ACCOUNT-VALIDATION` for programmable accounts without declared
  validation logic or authorization policy.
- `DOM16-USEROP` for missing user-operation or authorization schemas.
- `DOM16-SESSION-KEY` for unscoped, unbounded, or unrecoverable session keys.
- `DOM16-PAYMASTER` for sponsorship without payer, quota, accounting, or
  failure semantics.
- `DOM16-DELEGATION` for delegated execution without capability and revocation
  rules.
- `DOM16-REPLAY` for missing replay domains, nonce semantics, expiry, or
  cross-chain replay protection.
- `DOM16-BUNDLER` for account flows without declared bundler, relayer,
  aggregator, simulation, or inclusion assumptions.
- `DOM16-WALLET-BINDING` for missing typed-data, signing, account-manifest, or
  user-operation encoding artifacts.
- `DOM16-AA-PROFILE` for missing, stale, or contradictory named account
  abstraction profile records.
- `DOM16-ERC4337` for ERC-4337 flows without EntryPoint, UserOperation,
  validation, paymaster, aggregator, mempool, simulation, or encoding evidence.
- `DOM16-EIP7702` for EIP-7702 delegation flows without authorization tuple,
  delegation indicator, nonce, replay-domain, code-loading, or gas-accounting
  evidence.
- `DOM16-ERC7579` for modular smart accounts without declared module types,
  lifecycle, compatibility, revocation, or interface evidence.
- `DOM16-UPGRADE` for incompatible ABI/state upgrades without migration.
- `DOM16-INVARIANT` for missing or failed property evidence.
- `DOM16-ORDERING` for order-sensitive functions without declared ordering,
  mempool, sequencer, builder, or private-flow assumptions.
- `DOM16-MEV` for frontrun, sandwich, liquidation, auction, governance, bridge,
  or slippage exposure without a bound or mitigation.
- `DOM16-CONFORMANCE` for missing chain/runtime compatibility evidence.

Diagnostics must include contract/function/account id, source span, schema id,
capability, numeric mode, gas record, account-validation rule,
user-operation schema, session-key id, paymaster or sponsor id, delegation
rule, replay domain, nonce, named account-abstraction profile id, EntryPoint id,
authorization tuple schema id, module manifest id, bundler/simulation
assumption id, ordering assumption id, MEV exposure, wallet-binding artifact,
missing proof/artifact, and remediation.

## Rejected Designs

Gravity rejects nondeterministic smart-contract execution.

Gravity rejects unchecked contract arithmetic.

Gravity rejects ABI/state upgrades without migration evidence.

Gravity rejects state mutation without declared authorization.

Gravity rejects contracts without schemas and invariant tests.

Gravity rejects programmable accounts without declared validation logic,
authorization schemas, replay domains, nonce semantics, and wallet/client
binding artifacts.

Gravity rejects user-operation flows that omit authorization schema, expiry,
replay domain, fee policy, bundler/simulation assumptions, or inclusion-failure
behavior.

Gravity rejects claims of ERC-4337, EIP-7702, or ERC-7579 compatibility without
the corresponding standard profile manifest, schema fixtures, and rejection
fixtures for incompatible chains or wallet/client bindings.

Gravity rejects unscoped session keys, unbounded sponsorship, delegated
execution without revocation, and paymasters without quota, anti-drain, and
post-operation accounting rules.

Gravity rejects order-sensitive contracts that omit transaction-ordering,
mempool-visibility, sequencer/builder, or private-order-flow assumptions.

Gravity rejects slippage-sensitive contracts without max-loss, deadline,
commit-reveal, fair-ordering, batch-auction, or equivalent mitigation evidence.

## Conformance Criteria

A conforming blockchain slice must demonstrate:

- token, escrow, or governance contract examples,
- deterministic contract artifacts and ABI schemas,
- checked arithmetic and gas/resource reports,
- account-abstraction reports for programmable-account entrypoints,
- named Ethereum account-abstraction profiles for ERC-4337, EIP-7702, and
  ERC-7579 where selected,
- user-operation, authorization, session-key, sponsorship, delegation, replay,
  and wallet/client binding artifacts,
- bundler, relayer, aggregator, simulation, and inclusion assumptions for
  account-abstraction flows,
- ordering and MEV exposure reports for order-sensitive entrypoints,
- property/invariant tests,
- upgrade/migration fixtures,
- rejection of nondeterminism, unchecked overflow, unauthorized mutation, and
  incompatible upgrades,
- rejection of programmable accounts that omit validation logic,
  user-operation schemas, replay domains, nonces, sponsorship limits,
  delegation revocation, bundler/simulation assumptions, or wallet/client
  binding artifacts,
- rejection of claimed ERC-4337, EIP-7702, or ERC-7579 compatibility when the
  standard-specific profile, encoding, validation, authorization, module, or
  wallet-binding evidence is missing or stale,
- rejection of order-sensitive contracts that omit assumptions or mitigation
  evidence for public mempools, private order flow, sequencers, builders, or
  fair-ordering/commit-reveal requirements.
