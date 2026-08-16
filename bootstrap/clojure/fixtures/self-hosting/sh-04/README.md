# SH-04 Syntax Fixtures

Every checked-in source case has a byte-identical co-canonical `.gravity` /
`.qst` peer. The extension and physical path are provenance only; semantic
syntax identity excludes them.

## Accepted pairs

- `source-syntax`: public C2-to-C3 input with Unicode text, nested forms,
  abbreviations, metadata, and multiple exact source spans.
- `template-descriptor`: a complete bounded descriptor for direct execution of
  the Gravity-owned `gravity.bootstrap.syntax` object build, resolved verify,
  stream build, serialize, deserialize, and graph templates. Its synthetic
  reader binding is structurally complete and digest-bound to the source
  revision; public tests separately exercise fresh SH-03 products.

## Rejected scenario pairs

Each rejected fixture is executable scenario data. The dedicated SH-04 test
loads `template-descriptor`, applies the declared mutation, and requires the
Gravity-owned template to reject with the listed C3 rule.

| Base name | Phase | Expected rule | Rejection |
| --- | --- | --- | --- |
| `shape-extra-key` | build | `C3-SHAPE` | descriptor has an undeclared field |
| `id-request-substitution` | verify | `C3-ID` | digest request no longer replays exactly |
| `span-reversed` | build | `C3-SPAN` | byte range ends before it starts |
| `span-backwards-column` | build | `C3-SPAN` | same-line end column precedes its start column |
| `origin-missing-producer` | build | `C3-ORIGIN` | source origin has no producer |
| `hygiene-duplicate-mark` | build | `C3-HYGIENE` | a mark appears twice |
| `hygiene-marks-not-vector` | build | `C3-HYGIENE` | marks use the wrong container type |
| `hygiene-rename-target-not-introduced` | build | `C3-HYGIENE` | rename target is absent from the introduced identifier set |
| `capture-unintentional` | build | `C3-CAPTURE` | capture is not explicitly intentional |
| `metadata-non-map` | build | `C3-METADATA` | metadata is not a map |
| `fact-stale-version` | build | `C3-FACT-STALE` | attached fact has a nonpositive version |
| `serialization-substitution` | serialize | `C3-SERIALIZE` | syntax changed after its identity request |

Graph-cycle, dangling-reference, duplicate-id, exact-bound, over-bound,
reader-binding substitution, resolved-id mutation, stream-root mutation, and
carrier mutation cases are generated in the test namespace so large
repetitive carriers do not inflate the repository.

Generated spans are structured records with a stable producer id and ordinal.
Serialization uses the Gravity-owned fixed-order
`:gravity/sh04-syntax-stream-carrier-v1` vector and its Gravity deserializer.
The carrier preserves the complete resolved syntax stream, graph, hygiene,
metadata, fact, origin, and ownership products. Host printer/reader round trips
receive no SH-04 credit.

The fixture suite proves a Gravity-owned executable SH-04 semantic slice. It
does not claim seed retirement: plan execution, digest resolution, public
routing, and artifact adaptation still cross the Clojure coordinator boundary.
