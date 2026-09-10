# SH-07 B51 Git-Object Publication V9 Pre-Evidence Erratum V1

Status: draft architecture correction; report only; no implementation authority

## Purpose

This report corrects two pre-evidence contradictions in the independently
accepted and integrated SH-07 B51 Git-object evidence-publication architecture
V9. It preserves V9's process isolation, canonical codec, immutable Git-object
reads, publication topology, evidence lifecycle, fixed-fuel semantics, and all
authority nonclaims. It changes only the inputs that may exist before the
evidence-eligibility commit Q.

The first contradiction names an A9 lifecycle receipt path that is absent from
the accepted candidate, authoritative main, and all Git history. The second
requires Q and PRE to contain postimage values that can exist only after the
fresh A/B/M execution and later publication phases. Both conditions make an
authentic I9 candidate impossible and must fail closed rather than be
fabricated.

## Machine-readable architecture authority

```json gravity-architecture-authority-v1
{
  "schema": "gravity/architecture-authority-v1",
  "workstream_id": "sh07-b51-git-object-evidence-publication-v9-pre-evidence-erratum-v1",
  "invariant_family": "architecture/self-hosting-sh07-b51-pin-free-publication-v1",
  "report_path": "docs/artifacts/phase-15/reports/sh07-b51-git-object-evidence-publication-v9-pre-evidence-erratum-v1.md",
  "status": "draft",
  "base_commit": "8c39712c80e7da66c23a23a6c1f01ba1f970b682",
  "dependencies": [
    {
      "id": "sh07-b51-git-object-evidence-publication-architecture-v9",
      "required_state": "integrated"
    }
  ],
  "historical_references": [],
  "authority": {
    "integration_only": true,
    "release": false,
    "self_hosting": false,
    "seed_retirement": false
  }
}
```

## Reproduced contradictions

At authoritative main `8c39712c80e7da66c23a23a6c1f01ba1f970b682`,
V9 defines its three A9-only PRE paths as the architecture report, provisional
companion report, and
`docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/a9-lifecycle-receipt.edn`.
The first two blobs exist. The lifecycle receipt does not exist in the tree,
and `git log --all --` for that exact path is empty. The content-addressed
terminal A9 workstream record does exist at:

```text
contracts/workstream-records/terminal/sh07-b51-git-object-evidence-publication-architecture-v9-461e2300fb893cbc2b60fb23e51b7242608a1aefd0bb15c8af4d72219a2b3ef1.json
```

I9 owns neither the missing review path nor the terminal record. It cannot add
or infer the missing A9 input without violating ownership and immutable-object
reconstruction.

V9 also requires the eligibility receipt and PRE discharge to contain the
complete `MASKED-ROOT`, including `:postimage-spans`, `:postimage-files`, and
`:postimage-root`. Those postimage spans are defined from the four final digest
RHS values and the quoted final carrier. Those values arise only from the
fresh A/B/M execution and later evidence/publication lifecycle. V9 separately
forbids Q and PRE from containing a runtime or future value. The two rules are
simultaneously unsatisfiable.

## Corrected A9 immutable input

The V9 phrase "three A9-only paths" and its literal vector are replaced by
`A9-ONLY-PATHS-E1`, exactly:

```clojure
["docs/artifacts/phase-15/reports/sh07-b51-git-object-evidence-publication-architecture-v9.md"
 "docs/artifacts/phase-15/reports/sh07-b51-git-object-evidence-publication-provisional-evidence-v9.md"
 "contracts/workstream-records/terminal/sh07-b51-git-object-evidence-publication-architecture-v9-461e2300fb893cbc2b60fb23e51b7242608a1aefd0bb15c8af4d72219a2b3ef1.json"]
```

The distinct union used by `PRE-C-PATHS` therefore remains exactly 72 paths,
and `PRE-DISCHARGE-OBSERVATIONS` remains exactly 77 operations: show C; one
72-path `ls-tree -z`; 72 ordered `cat-file blob` reads; show the BOOT5 baseline;
one baseline `ls-tree`; and one baseline `cat-file blob` read. The A9 artifact
count remains three and the static decode count remains 63.

The `A9-LIFECYCLE` PRE carrier is reconstructed directly from the third blob.
It has exactly `:coordinate :record :architecture-integrated`. `:coordinate`
is the immutable descriptor for the content-addressed terminal JSON at C/T.
`:record` is the complete strictly decoded canonical JSON object. Its filename
digest, raw SHA-256, workstream ID, invariant family, dependency, report path,
candidate identity, complete lifecycle history, independent accepted review,
integrated state, authority ceiling, and terminal manifest entry must all
recompute and join. `:architecture-integrated` is true only if every check
passes. No separately authored A9 lifecycle receipt is read or inferred.

Every V9 reference to the absent A9 lifecycle receipt as a C input is replaced
by this terminal-record carrier. The V9 `A9-PATHS` ownership-plan vector still
describes historical lifecycle allocation, but it grants no permission to add
the absent path during I9.

## Acyclic masked-source split

The V9 pre-evidence `MASKED-ROOT` is replaced by `PRE-MASKED-ROOT`. It has
exactly:

```clojure
{:coordinates PRE-COORDINATES
 :preimage-spans PREIMAGE-SPANS
 :preimage-files PREIMAGE-FILES
 :preimage-root PREIMAGE-ROOT}
```

The five coordinates, delimiter checks, adjacency bytes, source paths, load
ordinals, form ordinals, owners, RHS kinds, mask bytes, and preimage span/file
algorithms are unchanged from V9. Every RHS at C must be exactly
`:gravity/provisional-no-authority`. `PREIMAGE-ROOT` retains V9's
`:gravity/sh07-b51-masked-source-root-v1` domain over the closed coordinates
and two masked preimage file descriptors. PRE contains no postimage span,
postimage file, final digest, final carrier, E9 blob, or descendant identity.

The Q eligibility receipt key formerly named `:masked-root` is renamed
`:pre-masked-root`. The PRE discharge evidence carrier key is likewise
`:pre-masked-root`. The corresponding eligibility boolean is
`:pre-masked-root-match`; its meaning is byte-identical equality between the
strictly decoded receipt value and an independent C/T reconstruction. The
masked-file count remains two.

After the fresh A/B/M execution has produced all four digest RHS values and the
quoted carrier, runtime constructs `RUNTIME-MASKED-ROOT` with exactly V9's
original complete key set:

```clojure
{:coordinates COORDINATES
 :preimage-spans PREIMAGE-SPANS
 :postimage-spans POSTIMAGE-SPANS
 :preimage-files PREIMAGE-FILES
 :postimage-files POSTIMAGE-FILES
 :preimage-root PREIMAGE-ROOT
 :postimage-root POSTIMAGE-ROOT
 :equal true}
```

Its preimage projection must equal Q's authenticated `PRE-MASKED-ROOT`
byte-for-byte. Its postimage values are authenticated only in the runtime
discharge and later lifecycle joins. `:equal` is true only when the independently
recomputed preimage and postimage roots are equal. No runtime or postimage value
is moved backward into C, PRE, or Q.

## Preserved V9 requirements

All requirements not explicitly replaced above remain byte-for-byte normative,
including:

- exactly one L-created M process and the ordered fresh A, B, then M sessions;
- exact 39-string M argv and the two exact checker argv vectors;
- exact public codec functions `H`, `W`, and `R`;
- the five-operation Q check by L, M, A, and B;
- exact 12-source descriptors, 39 source events, and 12/12/39/1 counters;
- exact 77-operation PRE observation count and 33-document session;
- exact contract row plan, predicate specifications, phase discharges, and
  identity-cycle rejection, subject only to the two renamed pre-mask fields;
- exactly six M-staged payloads plus identity receipt written last;
- Git-object-only existing evidence replay and zero final writes;
- fixed-fuel owner selection, authenticated external binding acceptance, and
  unauthorized cross-fragment rejection; and
- all implementation, aggregate SH-07, self-hosting, release, performance,
  hostile-host, and seed-retirement nonclaims.

This report adds no implementation path, evidence blob, fixture, receipt,
review, pin, or completion authority.

## Implementation and lifecycle consequence

The existing draft I9 implementation must not freeze against uncorrected A9.
After this erratum is independently accepted and integrated, I9 must reconcile
onto that authoritative main, add this erratum as an integrated dependency,
and implement `A9-ONLY-PATHS-E1`, `A9-LIFECYCLE`, `PRE-MASKED-ROOT`, and
`RUNTIME-MASKED-ROOT` exactly. Any implementation that creates the absent A9
receipt, accepts a missing C path, inserts a placeholder postimage, copies a
future value into Q/PRE, changes the 72/77 counts, or grants authority from a
partial carrier rejects.

## Conformance and acceptance criteria

This architecture correction is acceptable only if a fresh independent review
confirms that:

1. the missing A9 path is absent at authoritative main and throughout Git
   history;
2. the named terminal A9 record exists, authenticates its filename digest, and
   contains the complete integrated lifecycle;
3. the corrected three-path substitution preserves exactly 72 PRE paths, 77
   PRE operations, three A9 artifacts, and 63 strict decodes;
4. `PRE-MASKED-ROOT` is constructible solely from C/T and contains no runtime,
   E9, final digest, final carrier, or descendant value;
5. `RUNTIME-MASKED-ROOT` preserves V9's full pre/post equality proof without
   moving a future value backward into PRE;
6. every unchanged V9 topology, schema, publication, fixed-fuel, evidence, and
   lifecycle requirement remains normative; and
7. the candidate is report-only, passes architecture/governance/document
   validation, receives independent acceptance, and grants no implementation
   or aggregate SH-07 authority.
