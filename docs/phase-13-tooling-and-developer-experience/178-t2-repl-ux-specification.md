# T2 - REPL UX Specification

Sequence: 178
Phase: 13 - Tooling and Developer Experience
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines the Gravity REPL user experience. The REPL is an
interactive compiler and runtime session with explicit profile, target,
namespace, capability, and artifact state. It supports source evaluation,
syntax inspection, macro expansion, type/effect queries, hot loading, workflow
replay inspection, and controlled runtime execution.

The REPL must preserve safe Gravity semantics. Convenience does not grant
ambient filesystem, network, shell, secrets, model, or database authority.

## Session Model

A REPL session records:

- project manifest and lockfile hash;
- active profile and target;
- active namespace;
- loaded source roots and artifact roots;
- capability grant set;
- evaluation history;
- generated artifacts;
- diagnostics;
- runtime ledger links;
- transcript output.

Sessions may be ephemeral or recorded. Recorded sessions are JSONL artifacts
that can be replayed for debugging when effects permit replay.

## Requirements

- A REPL session MUST declare profile and target before effectful evaluation.
- Capability grants MUST be explicit and visible.
- Evaluation MUST run reader, macro, type, effect, profile, and safety checks before execution.
- Macro expansion inspection MUST preserve syntax-object metadata and source spans.
- REPL commands MUST expose type, effect, ownership, capability, and profile information.
- Effectful REPL evaluation MUST record runtime ledgers when a runtime participates.
- Unsafe forms MUST require unsafe policy and audit record.
- AI, workflow, and distributed REPL operations MUST declare replay behavior.
- Transcript records MUST redact secrets.
- Every interactive operation MUST have a command form that tools can call.

## Commands

Required commands include:

- `:profile`;
- `:target`;
- `:ns`;
- `:load`;
- `:eval`;
- `:expand`;
- `:type`;
- `:effects`;
- `:capabilities`;
- `:inspect`;
- `:artifacts`;
- `:diagnostics`;
- `:replay`;
- `:transcript`.

## Semantic Dependencies

- `L1` through `L6` define source, macro, type, and effect behavior.
- `L15` defines capabilities.
- `R9` defines interactive runtime behavior.
- `C2` through `C8` define reader, macro, and analysis phases.
- `A1` and `A6` define AI and workflow replay concerns.

## Outputs and Artifacts

The REPL emits:

- transcript artifacts;
- evaluation result records;
- diagnostic records;
- syntax and expansion views;
- type and effect reports;
- runtime ledger links;
- generated artifact manifests;
- replay traces when requested.

## Example

```bash
gravity repl --project gravity.edn --profile hosted --target jvm-21 --grant fs/read
```

```clojure
user=> (:effects '(fs/read "notes.txt"))
#{:filesystem/read}
user=> (:expand '(when ok (println "yes")))
user=> (:artifacts)
```

## Rejection Rules

- Reject effectful evaluation before profile and target are set.
- Reject runtime effects without capability grants.
- Reject unsafe forms without audit policy.
- Reject transcript storage that would reveal secrets.
- Reject REPL-only semantics not accepted by normal compilation.
- Reject replay of live nondeterministic operations without recorded effects.
- Reject hot replacement that invalidates loaded type or effect assumptions.

## Diagnostics

- `T2001` reports missing REPL profile or target.
- `T2002` reports missing capability grant.
- `T2003` reports unsafe REPL operation.
- `T2004` reports transcript redaction violation.
- `T2005` reports REPL/compiler semantic mismatch.
- `T2006` reports replay violation.
- `T2007` reports invalid hot replacement.

## Conformance Criteria

- A pure `:core` REPL session evaluates portable expressions without host grants.
- An effectful hosted session requires explicit capability grants.
- Macro expansion inspection preserves source spans and syntax metadata.
- REPL results match normal compiled semantics for the same form.
- Transcript JSONL redacts secrets.
- Replay fixtures reuse recorded effects.
- Hot loading rejects changes that invalidate active checked assumptions.
