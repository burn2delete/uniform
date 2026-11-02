# Types: hosted verification data and missing-field decisions

D1, D2, D3, D8, D9, L5, L17, C1, C7, C15, BOOT8 and TEST2 govern this
increment. The original control is clean main
22253adc8fd6096bd6fad97762156fd47df70d22, tree
2f51295b36528c70bc1a493cbc0cb1825bc09015, in the Analysis checkout.

Three existing function bodies move unchanged from
gravity.c7-type-checker.verification to gravity.types:
typed-core-verifier-report, type-capability-proof and validate!. The old
namespace forwards the same arguments. Inputs are explicit native nodes, type
facts, constraints, function/dynamic/cast/generic/dispatch/schema/layout records,
diagnostic identifiers, report data and proof/failure callbacks. Outputs are
the existing hosted verifier report, legacy flag projection, ordered missing
field refusals or :complete sentinel. Existing catalog, source enrichment and
diagnostic construction remain in C7; the stage owns report and missing-field
decisions. No Bootstrap, C7 consumer or downstream dependency enters the stage.

These APIs do not authenticate records or verify canonical typing proofs.
Set-based node coverage does not validate duplicate native identities. Fixed
statuses and caller-supplied projection callbacks grant no capability authority.
Canonical type semantics, profiles, ownership, effects, layout legality,
source admission, migration, release, self-hosting and seed retirement remain
outside this increment. No frozen Reader route or previously refused hosted
source owner is retried.

The before producer compiles the current Gravity C7 source through the ordinary
fixed-seed development compiler. It executes the resulting plan against retained
paired primitive Core products, accepts their type results, passes replay and
rejects a tampered type table. The retained Core is historical input, not fresh
source acquisition or renewed upstream admission. Separately, the actual native
verification functions consume the retained 43-node source view and emitted
type products from the earlier Types evidence. Original source records and
semantic/provenance digest preimages remain complete; both hashes recompute.
The native report remains failed because schema/layout facts are absent.
Making an emitted constraint pending produces the actual C7-VERIFY refusal for
constraints-solved?. No full typed-core acceptance is claimed for that view.

The first capture used an incorrect digest-purpose name in its assertion; its
non-pass is retained. The corrected assertion uses the existing documented
sh07-core-provenance-binding-id purpose without changing input data or identity.
An initial test command selected the SH runner, which does not own the C7
namespace. That command's usage non-pass is retained; the documented development
runner selects the existing bootstrap-free C7 test namespace.

After products, whole-file conservation, focused regression and boundary checks,
independent acceptance, own CI/artifacts and immediate clean current-main
integration preflight remain pending at this draft snapshot.
