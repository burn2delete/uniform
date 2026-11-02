# Preserved quoted-data fixture constructors

Owner: ACC-01. Base main `2654d155322f7863dd087065f19000b43c877cbe`.
Historical PR502 head `6a4f87f013d0076b81aba9e0667650e70d28e635`.

## Scope and purpose

Restore the complete original bodies of
`sh08-quoted-data-recursive-development-kernel` and
`sh08-quoted-data-guard-development-kernel` in their existing canonical file.
The only code changes are six fixture-expression lines: explicit `list`
construction replaces current quoted literals. No fixture constructor
equivalence at the host carrier/metadata boundary is assumed.

All original assertions already survive on main and remain unchanged. The
recursive test retains scalar/container reconstruction, nested unevaluated
`throw` data, nested quote, type/value equality, constraints, no-evaluation
solution, type-table equality and exact data origin paths. The guard test
retains Boolean acceptance, non-Boolean `L7-GUARD-TYPE` rejection, diagnostic
node identity and false-guard shape behavior.

L2 defines quote as non-evaluating data; L7 requires normally completing guards
to produce Bool. These restored fixture definitions retain their original
inputs without substituting host-language assumptions for those contracts.

## Conservation and evidence

`quote-fixture-controls-census.edn` pins both raw original/current intervals and
the complete before/after test files. Both restored intervals match historical
bytes exactly. Every other test byte, every shared helper and all production
remain unchanged. The two shared fixture helpers are historically identical.
All six direct C7 definitions and argument lists are pinned: four are
historically identical; `sh08-qd-node` and `sh08-qd-products` differ and remain
explicit dependencies. No part of unaccepted PR591 is copied into production.

Reader checks use evaluation disabled and keep Bootstrap unloaded. Language
boundary passes 6 tests/11 assertions; docs, roadmap and staged/full-parent
whitespace pass. Pinned clj-kondo 2025.10.23 with cache disabled reports zero
errors and identical complete 71-warning multisets before/after, with no new
findings or suppression changes. New EDN has zero findings. Existing genuine
selectors are not run or retried.

## Acceptance boundary

Independent source review, current-head CI, exact composition and clean
integration preflight are separate gates. Both genuine tests remain UNRUN.
Restoring their definitions supplies no actual quoted-data, authentication,
guard, compiler or runtime acceptance. Surrounding metering remains separate
open work. No whole-file incorporation, ACC-01/PR502 closure, release or
self-hosting claim follows.
