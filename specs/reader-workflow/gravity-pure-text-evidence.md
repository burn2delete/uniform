# Gravity-owned pure-text source implementation

Canonical task: `reader/finite-remaining-work/extension-policy-closure`,
FL-P01-T01. This implements the coordinator-selected narrow pure-text source
capability. It does not complete the full extension task or the original native
goal, which remains blocked. Genuine normal acceptance remains unrun.

Base: `bf97427ecc6da86e80c2e51e23a0d0bb681a68a0`, tree
`10db1221fff2ca965391db25aa93194c17c9134d`, descended from preserved E351.
Branch: `codex/reader-gravity-pure-text`. This successor implements the Gravity
alternative selected from the historical
[producer decision](sealed-pure-producer-decision.md); that earlier proposal
and its unrun packet retain their original meaning at bf974. No SH02 host
producer contract revision is introduced.

## Producer, identity and verifier

The fixed new options identity selects `:gravity/sealed-pure-text-reader` in
the existing third Reader argument. Its enabled tag population is exactly
`text`. The standard policy and historical host-pure policy retain their old
selection and dispatch paths. Existing builder/verifier arities remain three
and four. The new private Gravity functions have the required `sh03-` prefix
so the actual plan selector retains their complete call closure.

After parsing the complete source, construction validates the whole tagged
population before calling `sh03-reader-read-text`. That implementation returns
its actual input seed and accepts no provider or callback. The version-two
extension record contains the fixed policy, implementation-owned handler name,
source/content references, form/payload ids, span, input seed, actual returned
seed and actual invocation. It is constructed before the existing digest
requests. Even with trivia disabled, the entire new record survives semantic
projection. Invocation digest dependencies include the source-unit identity;
the existing Reader result identity depends on the invocation digest.

The complete current Reader source/plan/functions binding and existing Gravity
producer envelope identify the private implementation. There is no embedded
self-hash of the containing Reader source. The unchanged raw verifier calls
the real producer again on the original bytes and selected policy, then
compares the complete fresh result, including the new record. It does not copy
the submitted return or accept caller hashes as execution evidence.

The text payload grammar is enabled only on frames carrying the new fixed
policy selector. Old tag frames and old policy behavior are conserved.
Effectful, unknown and other mixed populations refuse before the new handler.
Standard `inst` and `uuid` are intentionally outside this new text-only variant;
their existing standard behavior is unchanged.

## Adapter and normal consumer

`gravity.reader.pure-text` owns fixed policy identity and checks/projection of
already verified raw results. It is not an authentication API for caller data.
The normal adapter derives the tagged datum from the verified returned seed
and checks its equality with the original decoded payload. It calls neither
historical host dispatch path. Occurrence ids, spans and raw text are projected
to the existing C2 representation; actual Gravity input/returned seeds and
invocation are retained intact. The adapter summary includes the complete raw
record identity, while the existing adapted invocation digest includes the
projected record. No `:pure-extension-results` companion is produced.

`reader-pure-source-artifact [source-path source-text project-context
reader-source-path]` is a narrow new C2 entrypoint because the old constructor
fixes standard options. Existing arities/defaults delegate with those same
standard options. The added options arity accepts only standard options or
the exact new fixed options with explicit current source, native inputs and
direct construction. It does not admit caller precomputed pure products.

The C2 artifact retains original source text and project context as
reconstruction inputs, not authority tokens. The existing opaque C3 Reader
reverification port requires its internal authority, native artifact and valid
explicit current-source invocation. It reconstructs a complete new C2 artifact
through the actual producer/raw verifier/adapter and compares the whole result
before existing binding/envelope checks admit it. The syntax artifact view
requires this port for the new variant even when a caller omits the invocation;
the missing invocation refuses. Presence of a versioned record, text tag,
reconstruction input or new summary identity also selects verification, so
removing one selector cannot silently drop the remaining new product.

All three unsealed companion refusal boundaries remain: standard precomputed
products, syntax artifact view and the independently reviewed direct C3 guard.
The old Clojure handler is not relabeled as a Gravity producer. No envelope
schema, standard identity domain, resource helper, ledger or PR549 record is
changed.

## Validation and limits

The SOURCE/MODELED tests evaluate selected Gravity forms as Clojure with
synthetic parsed graphs and explicit ports. This is not Gravity execution.
They cover actual source control flow from population preflight to handler
call and digest construction; complete result replay; source bytes, parent,
policy, private implementation, input/result and occurrence mutations; full
record retention in both trivia modes; adapter return derivation; occurrence
projection; and consumer reconstruction before admission. A modeled handler
body mutation fails replay. Caller C2 field changes fail the modeled normal
port before its envelope continuation. Named changed host definitions compile
with unbound source ports, without loading Bootstrap. The actual source plan
selector retains both complete producer and verifier function closures.

Intermediate focused run: 24 tests / 152 assertions, zero failures/errors,
exit 0, across the new models, existing composition/admission/selection models
and language boundary. After the final projection and paired-definition
changes, the affected final run was:

```sh
clojure -J-Xmx256m -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -e '(require (quote gravity.reader.gravity-pure-text-test) (quote gravity.self-hosting.sh01-language-boundary-test)) (let [r (clojure.test/run-tests (quote gravity.reader.gravity-pure-text-test) (quote gravity.self-hosting.sh01-language-boundary-test))] (println (pr-str {:bootstrap-loaded? (boolean (find-ns (quote gravity.bootstrap))) :syntax-loaded? (boolean (find-ns (quote gravity.syntax))) :genuine-definition-loaded? (boolean (find-ns (quote gravity.reader.sealed-pure-integration-test))) :evidence-class :source-modeled})) (System/exit (if (zero? (+ (:fail r) (:error r))) 0 1)))'
git diff --check
```

Result: 14 tests / 89 assertions (new models 8 / 78; language boundary 6 / 11),
zero failures/errors, exit 0. All three printed load indicators were false.
Both compiler-free document validators also passed with exit 0:
`clojure -J-Xmx256m -M tools/validate_gravity_docs.clj` (240 documents,
19 phase indexes) and `clojure -J-Xmx256m -M tools/validate_full_language_roadmap.clj`.
Initial model preparation exposed missing model declarations, a source-test
namespace alias and delimiters in test definitions; these were corrected
before the passing runs. No passing genuine result is inferred from them.

`gravity.reader.sealed-pure-integration-test` defines two genuine normal
positive constructors for the existing byte-paired `.qst`/`.gravity` text
fixtures, normal consumer reconstruction, thirteen artifact tamper controls per
source extension, and four mixed refusal constructors. Controls include
recomputing public C2 incremental/integrity/artifact hashes after tampering.
The definition was read as data only. Requiring it loads Bootstrap and remains
held; it has not been run or listed through a runtime test runner.

The independent bf974 receipt
`/tmp/reader-direct-guard-independent-bf.edn`, SHA256
`d3e07d8960d315e135fdf16dac490e30e1e3c3ca952ccdf99e715640d34371d1`,
was read and hash-verified once. Its qualified SOURCE/guard-model PASS concerns
bf974 only (140 checks, 36 presence models, 10 negatives); its audit was not
rerun. It does not accept the new producer or later changed consumer. All
affected implementation requires its own exact-candidate independent review.

Remaining genuine gate: execute the new normal paired constructors, raw fresh
verifier and normal consumer with exact source/runtime/configuration receipts
only after the existing common-resource runtime hold is cleared by its owners.
E351's old run package is not a run request or acceptance receipt for this
changed Reader. No genuine Reader/Gravity/native runtime, process probe, lease, alternate root, publication,
review dispatch or integration was performed. Full granted/effectful handling,
full extension closure, release and seed retirement remain open.

New semantic names are `gravity.reader.pure-text`, `reader-pure-source-artifact`
and their behavior-named tests. New Gravity private names retain `sh03-` because
the existing plan selector requires that compatibility prefix. Existing numbered
component files and wire identifiers are conserved; no rename migration occurs.
