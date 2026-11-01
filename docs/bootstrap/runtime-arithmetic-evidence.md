# Hosted seed arithmetic slice

## Contract boundary

This slice moves only the existing add, subtract, multiply and divide helper
bodies from Bootstrap component00587-02 into gravity.runtime. D1/D2/D3/D8/D9,
L2, C1/C15, SAFE9, R1/R4/R11/R12, BOOT8 and TEST2 govern the boundary. It
preserves temporary Clojure seed behavior and does not define Gravity numeric
modes through host-language assumptions.

The public APIs are hosted-add, hosted-subtract, hosted-multiply and
hosted-divide, each taking the existing already-evaluated argument sequence.
They return the existing host numeric result or propagate its existing host
failure. They require only clojure.core. Bootstrap retains private compatibility
Vars, caller arity checks, error-to-diagnostic translation, execution context,
source/provenance, effects, capabilities and all authority checks. No admission,
provider authority, source loading or native target execution is added.

## Evidence requirements

Real paired Gravity source programs must compile to actual seed plans and
execute all four operators through the unchanged dispatcher. Positive controls
cover existing zero/unary/binary/ternary/variadic paths where legal. Division by
zero, overflow and illegal arity controls must retain complete actual diagnostics.
Full before/after source, plan, result and diagnostic records must preserve
provenance and recomputed deterministic identities.

Direct helper tests remain separate from genuine source controls. They cover
host result classes, exceptions, compatibility arities and Var interposition.
Whole-component conservation must leave context/authentication checks, remaining
helpers and the entire dispatcher unchanged. Registry and stage-map changes must
preserve other owners and stable helper identities. Independent exact-source
review, proportionate regression checks, own CI and clean current-main preflight
remain required before integration.

## Unsupported boundaries

This is temporary hosted seed arithmetic. It establishes no full L2/SAFE9 or
runtime-family conformance, numeric-mode selection, provider mapping, source/Core
authentication, new authority, native/backend execution, performance guarantee,
migration, release, self-hosting or seed retirement. Historical restricted
Reader/H6/native and default pinned routes remain unused. Scope publication
contains no implementation or acceptance evidence.

## Implemented extraction and evidence

The four bodies now live in gravity.runtime with only name/privacy changes.
Bootstrap retains private one-argument wrappers. Whole-component conservation
against8a980ac27d9392c3e85eaead76646fc7f3f38810 proves every context/authentication
definition and other helper unchanged. The entire builtin dispatcher and
Bootstrap loader are unchanged. Earlier Runtime API bodies remain exact. No
existing root inventory contains these four helpers; no new authority guard is
invented. Existing source/test ownership and primary-consumer registration remain
unchanged; component00587-02 is an additional Bootstrap call site within that
same Runtime namespace boundary.

Paired actual Gravity fixtures compile through the existing current source-plan
constructor. Eighteen positive functions per spelling execute all four
operators at their existing legal arities. Eight real refusals per spelling
retain L2-BUILTIN-ERROR or L2-BUILTIN-ARITY for division by zero, overflow,
illegal arity and wrong operand types. The accepted source SHA is
f2f67bc1a3a46d8ab2196ea9ab861c7d42619683ce05ca989f0f75089565ebe9 and plan id
sha256:4c7e7898beae10faa90b635236bfc9ed7c7f8ee296f62fd1afc24afab38f3647.
The rejected-program source SHA is
56d8c2b98965ebf3e3e55d49189cf564363609816100b660fae46a651486ab0b and plan id
sha256:c58f8158b810fd00f13303a1b189081736f0f525c633557163ce6461870651f5.
All four complete source/plan/result/refusal records match in original
serialization and data before/after.

Complete current C6/Core controls also accept both source spellings with25 nodes
and one target read, retaining source8e63 and recomputed Core16ec identities.
All seven complete data and original serialized pairs pass, with no projection.
The fixed seed and native Clojure runtime execute; this is ordinary development,
not source admission or self-hosting. Source/control paths and full plan/source
records retain actual provenance.

Focused Runtime tests pass9/296, coupled Runtime/CLI/literal tests20/443 and
ownership/language45/1118. Tests cover result classes, host exception kinds,
private-wrapper arity/privacy and late stage interposition, one stage call per
wrapper, real source execution and caller arity refusal before helper execution.
Trusted callback tests remain separate from genuine source controls. Contextual
lint3 paths/0 warnings/0 blockers passes; receipts are archived outside checkout.

The first focused run records20 failures from test assumptions: source hashes
include a sha256 prefix, and legacy builtin diagnostics have a nil source span
at this seam. Corrected assertions preserve those actual representations. The
actual source path remains in the surrounding source/plan/control record; no
coordinate is invented. This legacy diagnostic provenance gap is outside this
extraction, and no full C15 source-map conformance is claimed. The first temporary
conservation script stopped at a malformed def; its nonpass is retained, and the
corrected read-only whole-component comparison passes without source changes.

Controls, conservation and complete comparisons use
/tmp/uniform-runtime-arithmetic-*.clj and corresponding logs. Own CI, exact
independent acceptance and clean actual-main integration remain required.

## Current accounting, custody and release-proof composition

Actual main 7ad0edc7abcc7ea7b8f536e8363bd66ad2329453/tree
5271f1984a28f9da39b24beb999ad2038fc4c210 includes PR749 accounting
removal, PR746 host custody and PR748 read-only release verification. The
[ordinary-development record](current-ordinary-composition-evidence.md)
distinguishes current semantics from immutable historical A1 evidence.
All incoming implementation and tests remain preserved; Safety's shared
registrations are rebuilt from exact main plus its frozen association,
including the new process helper and unchanged primary catalog formulas.
Stage algorithms and fixtures remain conserved. Fresh current-base controls,
catalog identities, ownership/language, contextual lint and independent review
remain required. Old-base CI supplies no current integration eligibility.

Fresh current-base coupled checks pass20/443, ownership/language46/1136.
All7 complete data and original serialized pairs, actual source hashes,
deterministic identities and whole-component conservation pass. Current Core
controls still accept both spellings with25 nodes/one target read/source8e63
and recomputed Core16ec. Contextual lint passes3 paths/0 warnings/0 blockers.
Terminal lint receipts are archived outside the source checkout. Source
admission, historical denied routes and migration pins remain unchanged.
