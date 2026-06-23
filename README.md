# Gravity

This workspace contains the Gravity design document set derived from `/Users/mattr/Downloads/Gravity Lisp Design.pdf`.

Gravity is a self-hosting, homoiconic, Clojure-inspired language platform for the whole stack. The core design is one semantic model with many compilation profiles, not one runtime everywhere.

## Document Set

- [docs/README.md](docs/README.md) is the entry point.
- [docs/source-concepts.md](docs/source-concepts.md) summarizes the PDF concepts used to write the documents.
- [docs/document-sequence.md](docs/document-sequence.md) lists the final 240-document sequence.
- [docs/document-inventory.json](docs/document-inventory.json) is the machine-readable inventory.

## Validation

Run:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_gravity_docs.py
```

Expected result:

```text
validation passed: 240 docs, 18 phase indexes, ASCII, no placeholders
```

## Generation and Enrichment

- [tools/generate_gravity_docs.py](tools/generate_gravity_docs.py) contains the canonical 240-document inventory and baseline document renderer.
- [tools/enrich_remaining_docs.py](tools/enrich_remaining_docs.py) records the deterministic enrichment pass used for phases that were not completed by workers.
- Do not rerun the baseline generator over edited documents unless you intend to regenerate the full tree and then reapply enrichment.

