# Current native DATA and physical verification records

## Purpose and contracts

This increment preserves the useful PR613 and PR615 behavior against current
main. C7 requires complete current verification records; D8 and D9 preserve
native class, metadata, callback, cycle, identity and tamper checks when project
accounting is removed. Native DATA validity is distinct from B47 authority.

The clean source checkpoint is `e857f603c6167181b7a12574a4cbb73c990496df`,
tree `2ed513b4948dc606cf17dd48d109e88e527e2d9b`, composed over main
`4d8a085a9b68bb8c349869c574b94ee4cea81406`. The accompanying EDN records
the source hashes and exact commands. No frozen compiler pin is renewed.

## Original path accounting

PR613, `5ebff5cae276ddd93b002a28806a1c363687f7c9`, authored five paths:

- C7 accounting fields/helpers and the carrier-preflight alias are already
  absent on current main. They remain absent. Restore only the independently
  meaningful native DATA checks on authenticated imported Core and accepted
  imported results, using the existing `sh08-vf-original-data` directly. The
  original invalid-carrier diagnostics remain distinct from authentication
  failure, and rejected Core stops before producer traversal and inference.
- The source-coverage file already omits the four retired accounting helpers;
  it is unchanged.
- The SH08 tests are ported into a focused namespace instead of rewriting the
  shared fixture file. Native safety/extents inputs are preserved, including
  depth 192, width 65,537, shared acyclic occurrences and a 16,777,217-character
  string. The removed alias would duplicate the native predicate: thirteen
  duplicate assertions are retired while all distinct inputs remain.
- The original Markdown and census remain historical Git inputs. This report
  and the current EDN record the new implementation and evidence.

PR615, `bb3a9acfbb5dd683d6df0403867a179a5fdc51bc`, authored four paths:

- Restore the physical-entry check in `sh08-ft-all-checks-true?` and move it
  before catalog lookup in the receipt/coordinator validators. Native DATA is
  validated first; every physical entry must have a unique Keyword key and an
  exactly true value. A duplicate ArrayMap entry cannot disappear behind
  key-set construction or ordinary lookup.
- Preserve all three test kernels, substituting only the retired carrier
  alias's existing native predicate. The original width-70,000 input,
  callback/cycle cases, complete catalog positives, duplicate/malformed
  physical maps, non-true values and unknown/missing catalog entries remain.
- The two original evidence files remain historical inputs; the current
  report/census explicitly distinguish that source from this checkpoint.

No historical parent stack, budget entrypoint, generic bound, cost proof or
replacement work allowance is restored.

## Current evidence

The focused current namespace passed naturally with a 2 GiB heap:

```text
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.native-carrier-record-current-test --fail-fast
7 tests, 111 assertions, 0 failures, 0 errors
```

Those checks comprise the two current carrier kernels (32 assertions), three
preserved physical-record kernels (60 assertions), and two imported Core/result
native postcondition controls (19 assertions). The latter declare authentication
and inference as oracles; native traversal, receipt validation, diagnostics,
ordering, memo transitions and callback refusal execute the Gravity code.

The existing imported-module validation/routing controls separately passed
2 tests / 18 assertions. They retain the 1,025-entry memo/input cases, producer
identity refusal, authentication refusal and current four-argument inference.

The language-boundary gate passed 6 tests / 11 assertions. Canonical document
validation passed 240 documents and 19 indexes; roadmap structural validation
and the diff check passed. Changed-file lint reported zero blocking findings.

## Limits

These are normalized compiler-kernel results. They do not establish public C7,
B47/coordinator, source-admission, numeric-proof, native-C, backend, runtime,
migration, release or self-hosting authority. Earlier coordinator comparisons
and outer-record boundaries are not claimed to be completely callback-safe by
this check-map repair. Independent review and exact integration preflight remain
required.
