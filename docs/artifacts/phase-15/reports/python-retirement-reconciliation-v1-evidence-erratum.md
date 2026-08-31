# Python Retirement Reconciliation Evidence Erratum

## Purpose

This erratum invalidates one non-replayable validation command recorded for
the terminal workstream `python-retirement-reconciliation-v1`. It does not
change that immutable record or retroactively establish its integration
eligibility.

## Affected Evidence

The record integrated at
`492bb9c9db41f0cd6c8b0705fc0130006d2a38c8` claimed exit 0 for:

```text
clojure -M:test --namespace gravity.self-hosting.sh01-ownership-test --exact gravity.self-hosting.sh01-ownership-test/coordinator-reservations-and-leaf-conventions-are-explicit
```

Exact replay returned exit 1 with diagnostic `SH01-TEST-USAGE` and message
`Unsupported self-hosting test runner arguments`. The runner supports only an
unqualified run, `--dedicated`, `--list`, or one `--namespace` selection. Its
source identity is unchanged between implementation candidate
`904efa50469ee0a94dafc1be1a9b0e6462c1ddea` and this erratum base, so the
recorded command was never replayable for the admitted candidate.

## Truthful Replacement Check

The following Clojure assertion directly checks the affected ownership fact
and returns exit 0 on this erratum candidate:

```text
clojure -M -e '(require (quote [clojure.edn :as edn])) (let [record (edn/read-string (slurp "docs/self-hosting-slice-ownership.edn"))] (assert (not (contains? (:coordinator-owned record) :python-semantic-support-prefixes))))'
```

The implementation candidate remains
`904efa50469ee0a94dafc1be1a9b0e6462c1ddea`, tree
`9060d47ce6f0f431912c5c6383aa6b91d7d50189`. Commit
`174cd02fb3e70d722663277fe0524d682b52713e` corrected the terminal entry in
place and thereby violated the version 1 immutability rule. Commit
`6422d78459a47583a9a07bd1f22676d5dee8bb49` restored the terminal ledger blob
exactly to its integrated value.

## Scope And Acceptance

This erratum invalidates only the unsupported validation-command claim. The
remaining focused retirement evidence must be replayed independently against
this candidate. Acceptance requires the exact erratum commit and tree, a clean
named branch, passing Clojure boundary and tooling gates, governance and
document validation, exact preflight, and independent review.

## Residual Boundaries And Nonclaims

Git history permanently records the terminal-entry mutation and its
restoration. The immutable original entry permanently retains the false
command; this append-only erratum is the correction authority. It does not
retroactively establish the original workstream's eligibility or award
roadmap credit. Clojure and the JVM remain the temporary seed and tooling
boundary, and frozen Java, shell, and C host boundaries remain. This erratum
does not establish compiler correctness, self-hosting, seed retirement, or
release eligibility.
