# Root6 architecture authority fixture V2

This fixture uses the corrected history wording.  Rejected work is retained as
history and cannot supply authority.

```json gravity-architecture-authority-v1
{
  "schema": "gravity/architecture-authority-v1",
  "workstream_id": "sh07-root6-fixture-v2",
  "invariant_family": "architecture/self-hosting-sh07-root6-fixture",
  "report_path": "tools/fixtures/architecture_authority/accepted/root6-v2-history.md",
  "status": "draft",
  "base_commit": "e143921004ff76b5f5ad7e55e8cd24fe23455ded",
  "dependencies": [
    {
      "id": "sh07-b51-vector-destructuring-architecture-v18-attempt-17",
      "required_state": "integrated"
    },
    {
      "id": "sh07-b51-vector-destructuring-architecture-v18-attempt-19",
      "required_state": "integrated"
    }
  ],
  "historical_references": [
    {
      "id": "sh07-b51-vector-destructuring-architecture-v18-attempt-15",
      "terminal_state": "rejected",
      "role": "history"
    }
  ],
  "authority": {
    "integration_only": true,
    "release": false,
    "self_hosting": false,
    "seed_retirement": false
  }
}
```
