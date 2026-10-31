# Development Verification Workflow

The live workflow is defined in `docs/development-verification.md` and uses the
repository's Clojure runners directly.

Start with its default SH-07 edit-to-test workflow and three boundaries:
semantic implementation, affected integration validation, and explicit
authoritative migration. Workstream governance and preflight govern admission;
they are not prerequisites for ordinary focused development feedback.

The concise workflow is in [development-verification.md](development-verification.md).
Detailed historical runner notes and measurements remain in the linked
[reference archive](development-verification-reference.md).

Project accounting is removed: no work-unit allowance, debit/reservation ledger,
cost proof, capacity-oracle input quota or budget-exhaustion test is an
implementation or admission requirement. Remove the corresponding mechanisms
and budget-only assertions rather than increasing or renaming limits. Preserve
semantic, authentication and tamper checks independently, with ordinary
accepted/rejected regressions. Host broker, heap, process, capture and platform
restrictions remain separate. Historical cost records describe their original
runs and do not impose a current gate. Managers continue toward their goals
without task-count, failed-attempt or per-run permission quotas.
