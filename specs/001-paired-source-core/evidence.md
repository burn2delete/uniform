# Pilot evidence and status

Recorded: 2026-10-25 | Owner: Spec Kit adoption delegate
Status: artifact workflow demonstrated; implementation/review/integration not
completed by this pilot.

## Direct observations

- `gh pr view 356 --repo burn2delete/uniform --json title,body,url,state,headRefName,headRefOid,files` returned open PR356, branch `codex/sh07-c13-integration`, head
  `700e2929c6d5aeb394f4fe342108a86dfb54f58e`, one test file with 199 additions.
- `git show 700e2929c6d5aeb394f4fe342108a86dfb54f58e:bootstrap/clojure/test/gravity/self_hosting/sh07_current_public_closure_integration_test.clj`
  was read directly. `paired-original-two-slot-source-preserves-core-products`
  covers R1/R3 and deterministic repeat; `paired-original-reader-quote-remains-inert`
  covers R2; `paired-malformed-source-is-rejected-at-the-owning-stage` covers R4.
  The file's reproduction comment includes five existing ingress controls (R5).
- The three generated `.agents/skills/speckit-{specify,plan,tasks}/SKILL.md`
  instructions were read and followed in this session. Actual official
  `create-new-feature.sh` dry-run and creation used number 1, short name
  `paired-source-core`, `SPECIFY_FEATURE_NO_PERSIST=1`; `setup-plan.sh --json`
  selected the Uniform override. `setup-tasks.sh --json` was then run against
  this directory. These initial helper trials predate the current-main language
  policy correction; executable helpers were subsequently removed from the
  versioned integration. Final skills use direct artifact writes/read overrides,
  following the upstream lean pattern. These commands did not dispatch a model
  or run compiler code.
- The artifact outputs are `spec.md`, `plan.md`, `tasks.md` and this evidence
  record. Reproducible scaffold observations are retained in
  [results.edn](../../.specify/validation/results.edn).

## Reported implementation evidence (PR356 owner)

The PR body reports final validation at immutable `700e2929`: 8 tests,
499 assertions, zero failures/errors, natural exit 0. It names raw log
`/tmp/sh07-current-public-closure-700e2929-final.log`, SHA-256
`2f259dcf1cf6c40d6e3bbe9de32528180b403d8c66cb9cbcd1b89a897bc11345`,
and reports structural LOC and document validators passed. These are attributed
claims copied in scope, not a new attestation. This pilot did not retrieve,
hash, retain or independently replay the raw log. The exploratory 3/114 result
and historical C13 4/72 result do not replace the final reported scope.

## Unrun and remaining

No compiler/JVM tests, raw receipt authentication, fresh proof candidate, six-owner
occurrence census, native execution, release/self-hosting checks or merge were
performed here. A later metadata read observed PR356 merged at 2026-10-25 18:40:11 UTC,
merge `228da8a6f179399625c23b3b3770a65742bccc4e`, final head `c3fe9022`.
The initial `700e2929` results above remain historical, not final-head evidence.
This pilot did not perform that review or integration. Adoption PR371 still
awaits Deus review/integration. No reviewer wake was requested. A fresh Codex process must still confirm
local skill discovery and `$speckit-*` chat invocation; no `codex` executable is
available in this execution environment, so only generated-file/helper behavior
is observed. No autonomous `workflow run` was attempted.

## Outcome authority

This record owns only pilot artifact observations. Canonical contracts and the
existing evidence workflow decide compiler and proof authority. Deus decides
integration. Neither generated tasks nor passing helper checks can promote
reported evidence to authentic current coverage or SH07 completion.

## Final adoption checks

On the corrected current-main baseline `228da8a6`, the Clojure scaffold verifier
passed 78 checks using the pinned external CLI: configuration, ASCII, strict
JSON, preset copies, local links, actual official scratch initialization,
workflow removal, local preset installation, inventory checks and byte-identical
skill regeneration before and after `preset update`. The observed command was
`clojure -J-Xmx256m -M .specify/validation/verify.clj /tmp/uniform-spec-kit-venv/bin/specify`.
The existing document validator passed: 240 docs, 19 indexes, ASCII, no
placeholders. The language-boundary test passed 6 tests / 11 assertions with
zero failures/errors. These are lightweight repository checks, not compiler
behavior or historical proof authentication. The final configuration deliberately
omits executable upstream helpers from version control.

The first draft accidentally inherited the supplied divergent checkout's history.
Its base was corrected to current main by rewriting only the adoption branch;
subsequent corrections use forward commits. Final review must inspect the additive
PR diff, not the original draft's inherited scope. Primary-checkout dirty files
were never staged, reset or published by this work.
