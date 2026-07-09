# Fixture

## Tasks

- [x] `FL-P00-T02` Enforce full-language completion rules in roadmap tooling.

## Evidence Ledger

| Date | Agent | Task | Evidence | Result |
| --- | --- | --- | --- | --- |
| 2026-07-02 | Codex | `FL-P00-T02` | `tools/validate_full_language_roadmap.py`; commands: `python3 tools/validate_full_language_roadmap.py --self-test`, `python3 tools/validate_full_language_roadmap.py`, `python3 tools/validate_gravity_docs.py` | Validator rejects deliberately overclaimed scaffold-only full-language task fixtures and accepts audited control tasks with command evidence. |
