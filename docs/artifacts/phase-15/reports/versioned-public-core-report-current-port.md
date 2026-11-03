# Current public Core report controls

## Scope and source accounting

This is the current-v2 test port of PR604, original commit
`edd55dcf6449a7677ce16d65f65091cee314a19c`, tree
`7fe94c10d3e71fe86c557c68b17c8919b32e8991`, authored over PR603
`d561fa9926ac15d25e3b85caafaada63db7056ae`.

The three authored paths are accounted for separately:

- Component 00726's v2 report, verifier and predicate overloads already exist
  on main. They retain fresh construction, digest-reference rehydration,
  candidate replay, typed semantic comparison, target and source provenance,
  and the distinct C6/C11 policy identities. No production hunk is replayed.
- The original report remains historical evidence in Git. This report records
  the supported current test port and does not renew its source authority.
- The unique public-report test is adapted to the current split owners and
  supported public v2 API. The prerequisite Reader-policy controls are the
  reviewed PR597 port; the old constructor and fixed-source stack are omitted.

Current public C6 accepts v2. The duplicate synthetic v3-positive report case
is removed, and all three public report/verifier/predicate entrypoints now
explicitly reject a native Long v3 header before context or fresh-owner work.
Other former v3 refusal controls now exercise supported v2, so a rejected
header cannot mask the semantic behavior being tested.

## Preserved mechanisms

The controls retain repeated fresh calls, policy binding to the actual supplied
artifact/context, digest rehydration and candidate replay shape, type-sensitive
semantic substitutions, target/provenance substitutions, raw-reference and
native-carrier refusal before fresh construction, forged verifier-report
refusal, legacy report routing, the single-argument predicate's false result,
and interruption propagation with its thread flag restored.

Source construction, public context and Gravity invocation are declared
oracles. Header parsing, native carrier checks, canonical encoding, digest
rehydration, typed comparison, policy projection and report validation use the
current implementation. The tiny candidate payload is synthetic Core data;
these checks do not construct or authenticate a real Core artifact.

## Focused evidence

Command:

```text
clojure -J-Xmx512m -M tools/c6_public_report_test.clj
```

The current public-report controls pass 6 tests / 69 assertions, with zero
failures and errors. The loaded prerequisite relative-policy controls pass
4/54 and Reader-policy controls pass 6/42. The original 6/78 result is not
relabelled: removing 15 duplicate v3-positive assertions and adding 6 early
public-v3 refusal assertions produces the current 69 assertions.

Initial port attempts exposed a stale nested harness lookup and two omitted
dependencies of the current split canonical-stream/resolver owners. Those
attempts produced no public-report test result. The corrected loader evaluates
the actual current helper definitions; it does not replace those helpers with
oracles. Logs are retained under `/tmp/uniform-pr604-current-report-*`.

The language-boundary gate passed 6 tests / 11 assertions. Canonical document
validation passed 240 documents and 19 phase indexes, roadmap structural
validation passed, changed-file lint found zero findings, and the diff check
passed.

## Evidence limits

No Gravity execution, genuine Core authentication, public v3 construction,
C11 admission, compiler migration, backend, release, self-hosting or
seed-retirement credit follows from this synthetic mechanism test. Separate
real C6/C11 evidence remains separately recorded. Independent acceptance and
the exact current-main integration preflight remain required.
