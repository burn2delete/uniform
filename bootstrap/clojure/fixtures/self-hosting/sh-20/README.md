# SH-20 Artifact Bundle Leaf Fixtures

This directory contains an early executable Gravity-owned artifact bundle
builder plus co-canonical accepted and rejected descriptor fixtures.

The engine validates exact normalized schemas for lowercase SHA-256 digest
records, compiler and build provenance, source maps, safety evidence,
target/runtime metadata, reproducibility policy, and release evidence
references. Its bounded dependency graph has explicit nodes and edges,
deterministic unique ordinals, reference closure, and edges that must point
toward strictly lower node ordinals, rejecting cycles and artifact
self-dependencies.

Artifact and build identities are explicit identity inputs. Checkout and output
paths remain available in source maps and provenance, while every structure
admitted to the semantic identity has a bounded path-neutral schema. The
Clojure test only proves that the current seed parser can round-trip the
plain-EDN carrier; it is not external consumer evidence.

Digest values are format-validated records supplied by the caller; this leaf
does not compute or authenticate them. Target byte emission, digest
calculation, signing, SBOM construction, authenticated SH-17 input, generated
release evidence, and external consumer verification remain pending. This is a
partial SH-20 leaf and does not change completion accounting.
