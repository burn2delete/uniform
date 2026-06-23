# A7 - Memory and Retrieval Specification

Sequence: 160
Phase: 11 - AI and Agentic Programming
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines AI memory as typed, policy-controlled storage for
agentic programs. Memory includes vector retrieval, document stores, summaries,
episodic records, tool outputs, embeddings, and durable user or project facts.
Memory is not trusted instruction text. Retrieved content is data with
provenance, taint labels, retention policy, and replay behavior.

The memory system gives Gravity agents long-lived context without introducing
ambient authority, cross-tenant leakage, secret exfiltration, or unreplayable
hidden state.

## Memory Declaration

A memory declaration contains:

- memory id, owner package, and deployment binding;
- item schema and metadata schema;
- embedding model and vector dimension when vectors are used;
- read and write effects;
- tenant, principal, project, and environment partitioning;
- retention, deletion, redaction, and re-embedding policy;
- indexing and ranking policy;
- prompt-rendering policy;
- replay policy for retrieval and mutation;
- audit and export policy.

The compiler checks memory declarations against agent manifests and deployment
capability grants.

## Requirements

- Memory reads MUST require `:ai/memory-read` and a scoped memory capability.
- Memory writes MUST require `:ai/memory-write` and a separate scoped capability.
- Embedding creation MUST require `:ai/embedding` and a model/provider capability.
- Memory items MUST have schemas and provenance.
- Retrieved content MUST remain tainted until validated or quoted as data.
- Secret and no-store data MUST NOT be embedded or persisted unless policy explicitly permits it.
- Cross-tenant retrieval MUST be rejected unless a declared sharing policy allows it.
- Replay-sensitive workflows MUST record retrieval result ids, ranking scores, and embedding identity.
- Re-embedding MUST record source item hash, old embedding identity, and new embedding identity.
- Deletion and redaction MUST update retrieval indexes and ledgers.

## Retrieval Semantics

Retrieval returns a typed result:

- item ids;
- item schema ids;
- content hashes or redacted content;
- metadata;
- ranking scores;
- embedding identity;
- retrieval query hash;
- taint labels;
- access decision;
- replay token.

The prompt renderer may include retrieval results only through data-authority
sections. Retrieved instructions do not become system or developer
instructions.

## Semantic Dependencies

- `L6` defines memory effects.
- `SAFE10` defines capability enforcement.
- `SAFE11` defines taint tracking.
- `R8` defines AI runtime memory services.
- `S1` defines memory item schemas.
- `S3` defines canonical hashes.
- `A2` defines embedding model providers.
- `A3` defines prompt rendering of retrieved data.
- `A11` defines memory injection defenses.

## Outputs and Artifacts

The compiler emits:

- memory manifest;
- item and metadata schema links;
- embedding model and vector layout;
- partitioning policy;
- retention and redaction policy;
- prompt inclusion policy;
- replay policy;
- conformance fixture references.

The runtime emits:

- write ledger;
- embedding ledger;
- retrieval trace;
- access decision record;
- redaction and deletion record;
- re-index and re-embedding record;
- replay substitution record.

## Example

```clojure
(defmemory support-memory
  {:item SupportMemoryItem
   :metadata SupportMemoryMetadata
   :embedding {:model support-embedding :dimension 1536}
   :partition [:tenant-id :project-id]
   :effects #{:ai/memory-read :ai/memory-write :ai/embedding}
   :retention {:days 90}
   :redaction [:secrets :customer-pii]
   :prompt-policy :quote-as-untrusted-data
   :replay :record-result-ids})
```

The memory declaration permits retrieval for prompt context but requires the
prompt renderer to treat all retrieved content as untrusted data.

## Rejection Rules

- Reject memory access without a scoped capability.
- Reject writes whose item value fails schema validation.
- Reject embedding of secret or no-store fields without policy.
- Reject cross-tenant retrieval by default.
- Reject use of retrieved text as instruction authority.
- Reject replay-sensitive workflows that perform fresh retrieval when recorded retrieval is required.
- Reject vector index reuse after schema or embedding dimension mismatch.
- Reject deletion that leaves retrievable stale entries.

## Diagnostics

- `A7001` reports missing memory capability.
- `A7002` reports item schema failure.
- `A7003` reports prohibited embedding of protected data.
- `A7004` reports cross-tenant retrieval.
- `A7005` reports retrieved instruction injection risk.
- `A7006` reports replay policy violation.
- `A7007` reports embedding/index incompatibility.
- `A7008` reports stale retrieval after deletion or redaction.

Diagnostics include memory id, agent id, query hash, item id, tenant partition,
taint labels, and policy rule.

## Conformance Criteria

- A legal memory declaration emits schema, embedding, partition, and retention artifacts.
- A read without capability is denied.
- A write with invalid schema is rejected.
- A cross-tenant fixture is rejected unless sharing policy allows it.
- A prompt-rendering fixture proves retrieved instructions remain data.
- A replay fixture records retrieval result ids and ranking metadata.
- A redaction fixture proves deleted or redacted content is no longer retrievable.
