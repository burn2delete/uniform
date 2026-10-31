# Verifier census EDN source-form serialization

This ACC-01 evidence-only successor addresses the informational serialization
caveat in PR599's qualified source review. Base:
`b786b464a67e33af2b75bfcf90a87619b71774c3`, tree
`b2fd8edc5444f46c588a5db66e6881b72b3f1e33`. Original receipt SHA256:
`c6504417ad646c89e261e7871d26cd012b47a5783b088cf6eafe2a3662161f23`.

The census stores the eight verifier mutations as source-form data. Its sixth
mutation printed `'forged.module`: the Clojure reader reconstructs a quote form,
while clojure.edn reads an apostrophe-prefixed symbol. That was an informational
source excerpt, not portable equality of EDN and test-source forms. The actual
test mutation was correct and remains unchanged.

Exactly one census fragment changes to `(quote forged.module)`. Both readers
now reconstruct the same explicit list, and all eight mutation forms equal the
unchanged actual test source. The complete Clojure-reader value of the old
census equals the corrected EDN value. All other census bytes remain identical;
there are no production, test, fixture or governing-contract changes.

`verifier-census-edn-serialization.edn` pins the original and corrected raw bytes,
the single field/fragment delta, test source, correction generator and nine
effective reader/source corruption refusals. It itself uses explicit list
serialization with a checked EDN/core-reader roundtrip. Reader evaluation is
disabled; no stored code, target, test, helper or Bootstrap namespace executes.

The existing census generator digest remains historical provenance for its
original output, not a claim that the original writer emitted these corrected
bytes. The new correction record supplies the successor provenance. Original
published bytes, PR599's frozen head, its qualified receipt and caveat remain
available unchanged at their exact identities; no original writer is rerun.

Local source checks pass: the single-fragment byte equation, complete census
reader agreement, all eight actual test-source mutation forms, roundtrips and
nine corruptions. Keep draft pending one affected evidence/source review through
Accounting/Reconciliation. This does not retroactively clear the old caveat,
repair PR598, extend any of the 28 matches/two non-equivalent source forms, or
establish current-main, actual C7/public-verifier, companion, native/runtime,
integration, release, self-hosting or whole ACC-01/PR502 acceptance.
