# Incremental projection rename tasks

Spec: [spec.md](spec.md). Plan: [plan.md](plan.md). Evidence: [evidence.md](evidence.md).

- [x] Rename the internal projection namespace and its complete live references.
- [x] Preserve metadata precedence and serialized stage identity in focused tests.
- [ ] Record affected test, actual planner, language and documentation checks.
- [ ] Independently review the exact implementation and retained limitations.
- [ ] Coordinator verifies the final base and merges the accepted finite change.

The public facade and other legacy helpers remain explicit future debt; no
global naming gate or duplicate component roadmap is introduced.
