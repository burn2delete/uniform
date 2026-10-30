# Guarded List-construction source incorporation proposal

Owner: ACC-01. This proposal covers exactly the original changed
`body-lc-source` and `p15-s23-lc-source` definitions. It changes no compiler
code and grants no independent acceptance.

## Complete source equation

Historical source is PR502 `6a4f87f013d0076b81aba9e0667650e70d28e635`.
Assessed main is `c486a4d78f67937deb42b8bfa978e456d583bc32`. Each complete
historical body is reader-identical at path `[3 2 2]` in its current function:

```clojure
(defn NAME ARGS
  (if (NATIVE-MAP? validation)
    (if (= {:status :accepted} validation) HISTORICAL-BODY nil)
    nil))
```

Both argument lists are unchanged. No source identifier, numeric literal,
schema value, guard expression or failure branch is normalized. Whole-function
identity is false. Raw comments and whitespace are covered only by whole-file
pins; raw body byte identity is not claimed.

The companion EDN records both complete function/body hashes, original-unit
membership, whole historical/current files, exact guards and both nil branches.
The native guard definitions match those pinned in the earlier independent
predicate review. That receipt supplies only those two source pins here; its
33 predicate decisions are not extended. Its sealed 13,050 versus handoff
13,052 count discrepancy remains historical, with no count credited here.

## Proposed decision and exclusions

The requested decision is conditional complete-body source incorporation under
pinned, unrebound source/native-guard bindings and L2 control semantics. It does
not equate old and new validation-input contracts or behavior outside the
guarded path. Extra-field accepted cells, invalid native cells and failed
validation take the explicit current failure routes; they are not normalized
into the accepted cell.

Native prelude effects, callbacks, exceptions, termination, mutable globals and
transitive/runtime behavior remain unproved. Pinning the guard does not approve
its numeric conditions or other uses. The C7 List-construction builder has
additional differences and is explicitly outside this two-unit proposal. No
whole List-construction support or accounting-removal claim follows.

## Validation and remaining acceptance

Both full source equations and argument lists, original-unit memberships,
historical body hashes and unchanged native-guard pins pass. Fourteen effective
source corruptions refuse native/cell bypass, status-only weakening, lost
historical body, accepted failure routes, changed validation bindings and
changed argument lists. These are inert source checks; no Gravity definition,
Bootstrap, native guard/provider, compiler or runtime executes.

L2, C7, C8, C14, D9 and current accounting-removal policy govern review.
Independent receipt
`fd2d64dbfc35138e6da7048abae4585b39f2e34e1d2b6eb9f32d9763e1ea1547`
accepts conditional complete-body source incorporation for these two units:
5,052 sealed checks and 44 independent corruption refusals, with no blocking
findings. `construction-guard-review-consumption.edn` rehashes the checker and
five artifacts, exact frozen tree/inputs, both body equations, arguments,
native guards, accepted-cell tests, nil failures and governing pins. No reviewer
checker rerun is claimed. Prior B33 contributes only the two source guard pins,
with its count discrepancy preserved and no decision/count inherited.

The existing 1023 argument condition inside the historical bodies remains
visible and unendorsed. The source classification does not approve that bound
or close accounting removal. C7's builder and unconditional input-contract,
native-prelude, callback, exception, termination, transitive and runtime claims
remain outside this decision. The original machine proposal stays historical;
canonical adoption and whole-scope completion remain separate.

The original inventory and stable
ACC IDs are unchanged. No whole PR569/ACC01/PR502, release or self-hosting
acceptance is claimed.
