# Baseline, provenance and adoption decisions

## Source baseline

This proposal was authored on the cloud computer in a separate worktree from
main `c21068a6d39259b7110845bbbcefaf9bf6901232` (2026-10-25), after fetching
origin/main. Source facts and links refer to that snapshot unless explicitly
marked otherwise. No private laptop work is incorporated or relabelled.

The pre-existing complete 100-page roadmap deliverable is preserved as
`Uniform completion roadmap.docx` and its `uniform-roadmap-review-package.zip`.
The available editorial source-and-decision record identifies technical baseline
`dc0a3c15dec6b42832381e1c4ff1daea49157312` (PR354), separately dated operational
facts, all 30 self-hosting milestones and 180 acceptance requirements, 36
full-language tasks, 21 domain slices and a 240-contract index. It distinguishes
89 selected normative source texts from a fresh audit of all 240. These are
historical source-package coverage facts, not this proposal's execution results.
The original delivered package is retained in
[Library version 1](https://chatgpt.com/api/library/files/libfile_664cc760c3cc819199c1b43970cdac9b/download).

The component decomposition is a new successor layer requested on 2026-10-25.
It does not replace source-history authorship, overwrite earlier reports or
claim the prior roadmap was absent. Current canonical contracts and directed
accounting removal govern over superseded historical quota wording.

## Explicit decisions

| Decision | Recommendation | Decision owner and closure |
| --- | --- | --- |
| Component names | Use concise semantic names shown in the registry; reserve IDs for traceability. | Deus with maintainers confirms naming in review; meanwhile names remain working defaults and independent work proceeds. |
| Reader/Syntax cut | Reader owns lexical products/seeds; Syntax owns full contexts/hygiene objects. | Reader and Syntax maintainers review the concrete handoff, with Deus integration. Close by accepted interface or explicit canonical-contract amendment. |
| Owner assignment | Preserve existing reserved code ownership; assign unowned component responsibility explicitly without creating a new execution hierarchy. | Deus records the current accountable owner when known. Until then no reserved path changes are authorized by these docs. |
| Full compiler population | Derive actual required source and dependency closure for the declared compiler outcome, not a fixed six-frontend-file substitution. | Release and component maintainers produce auditable closure; Deus reviews the declared scope. |
| Platform product architecture | Keep hardware/OS product choices separate from the selected compiler release. | Platform architect/product owner proposes target ISA, firmware/kernel/device/app boundary and external tool/fabrication dependencies; user/Deus decides the product scope. See Platform's finite decision gate. |

## Validation scope

This documentation lane runs only document/roadmap/language-boundary checks and
local link/content checks. These verify text structure and policy, not compiler
semantics or release acceptance. No restricted compiler execution, reader-oracle
action or migration retry is part of this work. Exact check results are recorded
in the PR and the directory's validation note after execution.

## Proposed owner-safe navigation links

For the shared docs overview / overall roadmap owner:

> [Component project roadmaps](README.md) propose named,
> finite architectural projects; Reader is the first fully worked example.
> Their outcomes compose into existing release gates without replacing contracts
> or auto-closing historical milestones.

For the Spec Kit guidance owner (relative to `.specify/README.md`):

> Link each bounded spec to its [component outcome](README.md)
> and existing normative contracts. Use semantic implementation names, retain
> planning IDs as traceability and do not duplicate component task ledgers.

These are handoff proposals, not edits to shared or reserved files.

The links above resolve from this handoff page. When inserting the first into
a docs-root overview, use target `component-roadmaps/README.md`; when inserting
the second into `.specify/README.md`, use target
`../docs/component-roadmaps/README.md`. This note is not a shared-file edit.
