# Semantic incremental projection names

Owner: semantic-name migration. Planning reference: C16.
Input: `7d008ac94ddc7d0bad78354317035e87ff4889cb`.

Migrate only the internal incremental projection namespace, following the
[existing audit](../../.specify/naming-pilot/plan.md),
[naming rule](../../.specify/semantic-names.md), and
[incremental contract](../../docs/phase-06-compiler-architecture/095-c16-incremental-compilation-design.md).

Acceptance: the semantic namespace loads through the unchanged public facade;
metadata precedence, cache-key fields, fixed identity strings and diagnostic
products retain their behavior. Preserve all public APIs, schemas, diagnostic
IDs and historical pins. This is not implementation of a real incremental cache
or closure of [Compiler API outcomes](../../docs/component-roadmaps/compiler-api.md).
Review and integration remain separate from the [observed checks](evidence.md).
