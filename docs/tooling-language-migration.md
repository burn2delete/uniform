# Tooling Language Migration

## Purpose

Gravity/Uniform is a Lisp system and its implementation path must remain
Lisp-based. This document makes the host-language direction explicit and turns
the existing mixed-language tree into bounded migration debt instead of an
architecture that future work can extend.

The machine-readable policy is `contracts/language-boundary.edn`. Its Clojure
gate is part of the standard self-hosting test discovery and can be run directly
with:

```bash
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

## Decision

- Gravity/Uniform is the destination language for the compiler, runtime,
  standard library, and eventually its own tooling.
- Clojure is the only temporary bootstrap, seed, and tooling implementation
  language.
- Python is prohibited for new work. The checked-in Python inventory is frozen,
  removal-only migration debt and has no implementation, evidence, aggregate,
  self-hosting, seed-retirement, or release authority.
- The existing Java launcher, eleven C launcher/runtime/fixture files, and two
  Bash public launchers are frozen host-boundary debt under the same
  removal-only rule. No new Java, C, or shell implementation surface is
  permitted.
- No replacement host language may be introduced. Clojure replacements must be
  designed so Gravity/Uniform can subsequently replace them behind explicit
  equivalence and provenance evidence.

## Current Gap

At adoption, the repository contains 162 Python files: 54 below `src/gravity`,
89 non-test files below `tools`, and 19 Python tool tests. It also contains one
Java launcher, eleven C launcher/runtime/fixture files, and two Bash public
launchers. This means the repository is not yet physically Lisp-only. The
policy does not hide that fact: it pins the legacy paths and contents, rejects
growth or modification, and permits absence so reviewed migration can only
move the counts downward.

Historical reports and evidence may continue to mention commands that were
actually run. Those records are provenance, not permission to execute or extend
the Python tooling layer for new work.

## Migration Order

1. Freeze the legacy Python and Java inventories. Reject new or modified files;
   allow deletion only.
2. Move coordination, admission, source-policy, and worktree checks to Clojure
   first so the migration is governed without depending on Python.
3. Replace documentation, coverage, project-structure, evidence-production,
   and development-orchestration tools in bounded Clojure slices. Each slice
   must preserve positive fixtures, negative fixtures, stable diagnostics,
   output schemas, and nonclaims before deleting its Python predecessor.
4. Remove Python semantic scaffolds rather than treating them as compiler
   authority. Port only behavior required by a governing Gravity contract.
5. Retire the Python tooling and evidence contracts after their final path is
   removed and their Clojure replacements are independently accepted.
6. Replace Clojure incrementally with Gravity/Uniform-authored equivalents under
   the self-hosting and seed-retirement contracts. Preserve the explicit
   Clojure audit/recovery boundary until the successor evidence closes it.
7. Remove the frozen Java, C, and Bash host-boundary files when Gravity/Uniform
   no longer requires them; replace checked-in foreign fixtures with bounded
   generated test inputs if their conformance coverage is still required.

## Acceptance Criteria

The direction is enforced when:

1. new or content-modified Python, Java, C, and shell files fail the Clojure
   boundary gate, including extensionless non-Lisp shebang scripts;
2. deletion of a pinned legacy file passes;
3. new tooling and bootstrap code is Clojure and no other host language is
   admitted;
4. Gravity/Uniform source remains the only successor direction;
5. each migration deletion has behavioral and evidence parity appropriate to
   the tool it replaces; and
6. documentation reports the remaining legacy counts without claiming that the
   physical tree is already Lisp-only.

## Nonclaims

Freezing a file does not validate it. A Clojure port is not automatically an
authoritative compiler component, and removing Python does not by itself prove
self-hosting or seed retirement. Those claims remain governed by D1, D2, D9,
BOOT7, BOOT8, and the relevant milestone evidence.
