# SH-07 B51 Once-Per-Invocation Nested Validation Architecture Independent Review V2

## Decision

REJECT the exact frozen architecture candidate. Version 2 correctly repairs
the version-1 outer-fact projection defect, but its caller-blind nested Root-1
mint depends on a call-entry transition that does not exist on the exact
selected-Root-8 execution path. The architecture must resolve that metering
cutpoint before implementation may be registered.

The reviewed tuple is:

- base commit: `e42ffdf67e43d5dada919ed239370b17a11ba950`;
- candidate commit: `dbc0fb60890626ecf826078d6ed679da99504b84`;
- candidate tree: `cae3f5044341141c1d1495d0e8f6249cb9018d51`;
- architecture report:
  `docs/artifacts/phase-15/reports/sh07-b51-once-per-invocation-nested-validation-architecture-v2.md`;
- report SHA-256:
  `ab8d8eb624bb53f7514ac4fcc38401b0a0b1a518bf28bb739f3c651816278ba7`;
- provisional evidence:
  `docs/artifacts/phase-15/reports/sh07-b51-once-per-invocation-nested-validation-provisional-evidence-v2.md`;
- provisional-evidence SHA-256:
  `2713d122e8f1ebe82f03a5ef727e80ec2348c7d82fca4689b9c284bdad550209`;
- review-pending lifecycle HEAD:
  `300ff4f0fac33f188191ff4c533fce9be2ea70ca`.

The candidate worktree was clean before this review. The candidate adds only
the immutable report and provisional-evidence artifact; its later commits add
and advance only the active workstream record and compact ledger. This review
does not alter either candidate artifact.

Reviewer identity:
`sh07-runtime-validation-architecture-reviewer-v2`, canonical task
`/root/runtime_validation_arch_v2_review`.

## Blocking Finding: Root-8 Callee Entry Is Outside the Counted Machine Loop

The report requires the existing direct-call evaluator to mint the child
Root-1 receipt "within that already counted call-entry transition," adding no
evaluator iteration and hiding or refunding no fuel. It separately requires
exact transition attribution without equating wrapper calls with transitions.
Those are sound requirements, but they do not match the exact current
execution topology named by the candidate.

At implementation snapshot
`45556a3f447a1262cd5f01afe5630571ee759d18`, selected Root 8 is entered through
`p15-s23-stage2-runtime-execute-function`. Its external public-Root-1 call is
handled by `p15-s23-stage2-runtime-execute-instruction`, which authenticates
the external link, evaluates the argument, and calls
`p15-s23-sh07-b51-runtime-execute-external-machine`.

In
`bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00946-p15-s23-sh07-b51-runtime-machine.clj`, that external-machine function:

1. records the external call;
2. calls `p15-s23-sh07-b51-runtime-machine-call-action` to resolve and bind the
   callee and create the initial action and frames; and only then
3. enters `(loop [action initial-action frames initial-frames steps 0] ...)`.

The fuel guard and increment occur only inside that loop. Consequently the
specific ordinary Root-8-to-Root-1 callee-entry action on which the report
places the mint is a host wrapper operation before `steps 0`, not an existing
counted machine transition. Calls reached later from inside the bounded
machine do pass through a counted `:call-arguments` continuation, but that is
not the selected-Root-8 initial external entry under review.

This leaves mutually incompatible implementation choices under the frozen
report:

- mint in the current call-action cutpoint, making the report's
  "already counted" assertion and exact transition attribution false;
- add a distinct counted call-entry action, contradicting the promise of no
  new evaluator iteration and changing transition accounting; or
- move the mint into the first counted evaluation transition, which is a new
  cutpoint and timing/lifetime design not specified by this candidate.

The clean successor evidence cannot repair this ambiguity after the fact.
Under D6 and D9, the architecture must first name the real metered operation,
its exact before-body ordering, its callee-local and top-level cleanup edges,
and how reference/successor transition attribution remains honest at the fixed
8,388,608 ceiling. The current text explicitly requires rejection of hidden
transitions, so accepting an architecture whose pivotal nested mint rests on a
nonexistent counted transition would violate its own gate.

## Required Repair

A successor architecture must choose and specify one mechanically real
Root-8-to-public-Root-1 entry cutpoint against the current evaluator:

1. identify the exact host function/action and the exact machine iteration, if
   any, that owns receipt mint before the first Root-1 instruction executes;
2. state whether mint is separately metered trusted-runtime bookkeeping or is
   part of a semantic machine transition, without calling an uncounted wrapper
   a transition;
3. if the machine gains or reclassifies an action, define reference and
   successor transition accounting, exact-limit behavior, and remaining
   headroom while retaining the 8,388,608 ceiling;
4. retain the caller-blind predicate over only the resolved callee and live
   admitted context, with no caller, stack, callsite, or mode branch; and
5. bind callee-local cleanup and the top-level fatal/interruption backstop to
   that exact entry so no issued receipt or replay handle survives.

The repaired decision must receive a new immutable tuple and fresh independent
review. This finding does not authorize implementation of any one repair.

## Verified Nonblocking Properties

Independent review found the remainder of the V2 design coherent:

- integrated Attempt 32 is the sole authority dependency, and rejected V1 is
  cited only as terminal history;
- the four host layers correctly distinguish bounded preflight, shallow outer
  coordinates, fully validated nested authority, and selected invocation;
- the Root-1-private gate reproduces the complete outer sequence and replaces
  only `sh07-b51-executable-carrier-shape?` at the nested position;
- fresh SH-06, predecessor descriptor, predecessor contract, raw observation,
  physical invocation, exact outer keys, and fixed coordinates remain
  executable Root-1 predicates;
- Root 6 replaces only
  `sh07-b51-r6-c30-carrier-authoritative?` and retains its independently
  authored outer, same-request, reconstruction, verifier, and result checks;
- the host admission snapshot really constructs local authority, compares and
  fully validates the supplied nested carrier, validates both certificate
  roles and joined M0/M1/M2/M3/S30/root products, and resolves the authentic
  root owner, plan, function, and arity before receipt authority;
- Root 1 consumes certificate A plus common joins and Root 6 consumes
  certificate B plus common joins; neither projection supplies an outer fact,
  semantic result, or the other role's witness;
- the 21-key private receipt, object-identity edges, one optional slot,
  `available -> issued -> retired` child gate, `fresh -> consumed -> invalid`
  receipt state, stable fatal lookup, callee cleanup, and top-level backstop
  close ordinary, rejection, replay, second-mint, and propagation paths;
- selected Root 8 receives no Root-8 receipt, retains its complete general raw
  validator, and reaches the exact public Root-1 definition and its sole
  Root-1-owned receipt occurrence without a caller-visible distinction;
- the exact two source cutpoints, closed provenance chain, separate internal
  builtin, 1,480 external edges, and rejection of unauthorized copied, shared,
  dynamic, callback, reflective, compatibility, or application paths are
  explicit;
- malformed map-shaped `:physical-invocation {}` remains a mandatory
  `:source-integrity-mismatch` differential with no result, output, or external
  trace despite host shallow-join success and possible receipt consumption;
- the frame ceiling remains 4,096, the authenticated external-call ceiling
  remains 65,536, and external link authorization and trace accounting remain
  intact; and
- the mutation, provenance, identity, clean rebuild, differential, fixed-limit,
  predicate-count, and D6 cost/correctness evidence matrix is otherwise
  comprehensive. The dirty full-fuel and wrapper samples remain honestly
  provisional and confer no benchmark or completion authority.

These passing properties cure V1's blocker but do not supply the missing
counted nested-entry cutpoint.

## Validation Receipts

The following gates were rerun from the clean review-pending worktree before
the review artifact was added:

```text
clojure -M tools/validate_architecture_authority.clj \
  --report docs/artifacts/phase-15/reports/sh07-b51-once-per-invocation-nested-validation-architecture-v2.md
architecture authority validation passed

clojure -M tools/validate_workstream_governance.clj
validation passed: 150 workstreams

clojure -M tools/validate_gravity_docs.clj
validation passed: 240 docs, 19 phase indexes, ASCII, no placeholders

clojure -M tools/validate_full_language_roadmap.clj
full-language roadmap validation passed

clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
6 tests, 11 assertions, 0 failures, 0 errors

git diff --check \
  e42ffdf67e43d5dada919ed239370b17a11ba950..dbc0fb60890626ecf826078d6ed679da99504b84
passed
```

Exact commit, tree, report digest, evidence digest, base ancestry, integrated
dependency state, rejected-history state, candidate path set,
governance-only post-candidate path set, and clean status were independently
checked.

At review time authoritative main was
`f88c8094d4150bc5cd3488f598a3d8617e1b7018`; the candidate was not its ancestor.
No latest-main reconciliation was attempted because rejection is terminal and
confers no integration eligibility.

## Authority and SH-07 Status

This review rejects only the exact V2 tuple named above. It grants no
implementation registration, integration, carrier, certificate, performance,
safety, proof, equivalence, roadmap, self-hosting, release, or seed-retirement
authority and no aggregate SH-07 completion credit.

Overall SH-07 remains stopped at the nested-validation architecture gate. The
outer-fact laundering blocker is resolved; the current blocker is the absent
counted Root-8-to-public-Root-1 entry transition. Implementation, rebuilt
identities and certificates, clean Root-1/Root-6/Root-8 correctness evidence,
independent implementation acceptance, and reconciliation to main remain
downstream.
