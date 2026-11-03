# Native backend fixture text ownership

## Contract boundary

D1, D2, D3, D8, D9, C1, C15, B1, B9, B10, B11, B12, B13, B14,
BOOT8 and TEST2 govern this extraction. Ten existing fixed text values move
from Bootstrap to the backend that owns their native fixture recognition:
HDL module, testbench and timing constraints; Workflow graph and replay
fixture; Query SQL statement, migration SQL and result adapter; Mobile bundle
and permission fixture. Definition bodies and UTF-8 bytes remain unchanged.

The public API exposes these immutable string values alongside the existing
recognizers and catalog operations. Namespace metadata records each value as
fixed native reference fixture data with no admission authority. The input is
the existing static fixture definition; output is its existing string value.
Stages require only Clojure core and string operations. Bootstrap consumes
them and retains its existing public names as values bound during namespace
loading. These aliases are snapshots, not forwarding functions; rebinding a
Bootstrap fixture Var remains independent of rebinding its stage Var.

Stages own reference text and token recognition. Existing consumers own
refusal decisions, failure construction, source admission and publication.
Source spans, input artifact identity and deterministic catalog ordering
remain under their current contracts. No diagnostic ID or serialized record
changes, and no new authority, quota or cost admission is introduced.

## Evidence and acceptance

A clean baseline at c29ecab29 records all ten original strings, their UTF-8
digests, actual emitted text files, commit/tree and host identity. A fresh
clean committed candidate capture must match every raw record and digest.
The source extractor separately reconstructs all eight complete production
files from that immutable parent, preserving predicate, catalog and refusal
bodies and replacing only fixture definitions with compatibility bindings.

Four focused tests preserve ten frozen baseline digests, check compatibility
value identity, write actual UTF-8 files, and hash their emitted bytes. Existing
positive fixture recognition, malformed-text rejection, actual owner refusal,
provenance and determinism assertions remain intact. Language-boundary,
ownership, documentation, roadmap and governance checks, independent review,
own CI/artifacts and exact clean current-main preflight remain required.

The initial test-generator invocation failed to resolve its shell namespace
before writing tests. The corrected helper imports that Clojure namespace;
the original helper and failure remain retained as orchestration nonpasses.

This successor starts from the pending relocated PR875 composition. Its
earlier reviews and CI do not transfer. The earlier Mobile/catalog timeouts
remain nonpasses; this move makes no Resolution performance claim.

## Unsupported boundaries

Fixed text is reference fixture data. Emitting it does not compile arbitrary
Gravity or target code, validate canonical target grammar, accept verified
MIR/domain IR, execute a backend or confer profile, effect, capability, proof,
publication or certificate authority. Complete, checked, permission and
policy labels inside fixture text remain data. Query migration SQL is an
unchanged inert reference value; it is not executed against a database.

No held native broker/factory, private JVM authority route, default frozen
Reader, package/archive/release builder or Attempt30 route executes. No
release, self-hosting, authoritative migration or seed retirement is claimed.
