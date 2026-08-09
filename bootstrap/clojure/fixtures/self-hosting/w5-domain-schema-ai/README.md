# W5 Wave6 Slice B: domain, schema, and AI execution

This bounded `:meta` consumer composes the DOM17/DOM18 domain manifest,
S1 schema, S2 serializer, S6 migration, and A1-A11 model/prompt/tool/agent/
workflow/memory/policy/evaluation/human-review/defense records. It validates
effect and capability sets separately, keeps external values tainted until
schema validation, preserves source provenance, and denies ambient provider,
database, filesystem, network, process, secret, and tool authority.

The accepted fixture is structurally valid but remains `:incomplete`,
`:blocked`, and `:non-authority`: it does not contact a provider, execute a
tool, write a database, replay a workflow, promote an agent, or claim
full-language/240-document completion. The JVM is only the stage2 seed
harness; seed is true and public, self-hosted, release, and full-language
credit flags are false. Provider execution, migration execution, workflow
replay, and independent policy review remain residual boundaries.

The candidate target is frozen to Linux/x86_64/LLVM/ELF/`:sysv-amd64` with no
fallback. Unsupported targets are the ordered vector
`[:darwin :darwin-arm64 :darwin-x86_64 :windows]`, each explicitly carrying
`:invokes-clojure? false`, `:links-jvm? false`, and `:fallback? false`.

The rejected co-canonical pair mutates schema compatibility, serializer trust,
migration data-loss policy, provider credentials, prompt authority, tool review,
agent memory mode, workflow replay, memory policy, policy fallback, evaluation
probes, human review, injection defense, effect/capability separation, target,
authority, and result substitution. It also directly rejects artifact,
request-schema/profile, model, diagnostics, keyword/string model references,
missing/mismatched/duplicate links, and ambient or extra capabilities. Stable
diagnostics retain source spans, related artifact links, origin chains,
structured facts, remediation, and redaction markers. Request constructors
require actual source path, extension, and kind, with `.gravity`/`:gravity` and
`.qst`/`:qst` provenance checked separately. `.gravity` and `.qst` pairs are
byte-identical.

The provenance binding is suffix-exact: a `.gravity`/`:gravity` declaration
must end in `.gravity`, and a `.qst`/`:qst` declaration must end in `.qst`.
Forged path suffixes reject with the stable `DOM17-DIAGNOSTIC` provenance
diagnostic. Source spans use the exact `:source-id`, `:start-byte`,
`:end-byte`, `:line`, and `:column` keyset; all fields have fixed types,
nonnegative byte bounds, ordered bytes, and one-based line/column values.
Missing, extra, mistyped, negative, and reversed span fields are rejected.

Migration, tool, agent, and memory effect/capability sets are exact governed
sets; policy allow and deny sets are exact authority partitions. Extra process
or shell authority rejects with the owning stable diagnostic. Provider identity
fields bind to the provider, model, pinned version/adapter contract, and
structured schema mode; substituted identity fields reject with `A2007`.

Model budgets are exact A2 records with `:max-output-tokens`, `:max-retries`,
`:max-wall-time-ms`, and `:max-cost-usd`. Agent budgets are exact A5/R8 records
with `:max-model-calls`, `:max-tool-calls`, `:max-retries`,
`:max-wall-time-ms`, `:max-output-tokens`, `:max-cost-usd`, and
`:max-human-reviews`; all values are bounded and cross-budget relations are
checked before acceptance. Missing, extra, mistyped, zero, negative,
out-of-bound, and substituted values reject with stable A2005/A5006 diagnostics.

This leaf is static-only. Do not run a model provider, database, native/LLVM
backend, Docker, or JVM/Clojure process as evidence for the contract.
