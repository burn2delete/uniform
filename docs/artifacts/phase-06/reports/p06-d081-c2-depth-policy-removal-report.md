# C2 depth-policy removal

## Scope

Document 081 retires project delimiter/form-depth quotas. Hosted C2 incremental
hashing, its host-oracle token precheck and C3 Reader-integrity reconstruction
still enforced those quotas. This change removes that admission policy and its
operation-schema entries, Bootstrap operation-map fields and numeric constants.
The existing component manifest keeps its resource and changes only the first
definition name after the two constants are removed.

Cycle rejection, lexical graph legality, source and product provenance, native
carrier/callback rules, canonical projections and all five fresh incremental
product hashes remain. C3 skips incremental hash reconstruction for an invalid
lexical graph while retaining its other independent identity checks.

Host StackOverflowError containment remains. The host-oracle diagnostic keeps
C2-HASH, its source span/options and actual host-error class; it now reports
reader-host-stack-overflow without asserting a fictional maximum depth. The
separate translation of genuine Reader rejection diagnostics is unchanged.

## Retained validation

| Executed revision | Checks | Actual result |
| --- | --- | --- |
| 068de7405b6096d62e1b1636b973e0f250bdc1a6 | Original C2/C3 hosted leaf namespaces | Natural exit 0; 23 tests / 998 assertions |
| 92f9e4e81f451bc1465a7d8948bf24d4f259bb53 | First changed leaf run | Natural exit 1; 24 tests / 1066 passes / 4 failures / 0 errors |
| 4ac40585ebdae9b5fb98b5a6a7e6ef84c85e5dc2 | Corrected leaf run | Natural exit 0; 24 tests / 1074 assertions; 781.6545 ms |
| a9b6264191deabded04508a63b08b7167c246303 | Composed Bootstrap caller and structural panel | Natural exit 0; 20 tests / 249 assertions; 12883.954916 ms |

The first candidate's four failures were a test oracle indexing captured
variadic arguments as a vector. Reading the actual argument sequence corrected
the oracle and added explicit diagnostic-ID/path assertions. Production code
was unchanged by that correction; the failed run remains nonpassing evidence.

Leaf checks used 256 MiB with a 60-second admitted watchdog and normal broker
leases. They did not load Bootstrap. The composed panel used 2 GiB with a
120-second admitted watchdog, exclusive broker leases and the canonical lock.
Both successful changed runs retained equal clean source/control bookends,
complete bounded output and actual normal scope returns.

The leaf selectors were gravity.c2-artifact-identity-test and
gravity.c3-reader-integrity-test. The composed panel selected all seven tests
in gravity.bootstrap-compatibility.c2-test, only the Reader-integrity wrapper
test in gravity.bootstrap-compatibility.c3-test, only
c2-graph-depth-validation-is-iterative-and-stack-safe in gravity.bootstrap-test,
and the language-boundary/LOC tests. Other legacy tests were not selected.

Actual hosted graph metrics and hashing accept normalized acyclic graphs across
the former limits, retain fresh callback order and reject cycles before hashing.
The existing 3000-node normalized graph control retains iterative graph checks
and now accepts hashing while rejecting its cyclic counterpart. C3's new leaf
controls inject reported depths around a shallow artifact: they establish fresh
recomputation and tamper rejection without a depth allowance, not authenticated
deep-source construction. Controlled host-stack failure checks preserve the
owning diagnostic, source span/options and error class.

## CI prerequisite repair

The first hosted static gate exposed three inherited source-resolver expressions
which called java.io.File/pathSeparator instead of reading that static field.
The C6, C11 and compiler-source resolver corrections change only those three
expressions; their owners' other code, source paths, pins and guards are retained.
Compiler-free checks passed one test / 13 assertions covering exact replacement
bytes, source-form counts, the real field value and synthetic classpath splitting.
They did not load Bootstrap or execute backend emission or source constructors.
The original hosted failure remains nonpassing evidence; it is not a C2 semantic
failure or a passing CI receipt for a successor.

## Remaining boundaries

This closes the hosted C2 depth-policy slice only. It does not establish new
authenticated Reader/Syntax admission, a full C5 or C14 contextual result,
performance improvement, complete accounting-removal acceptance or release
authority. Existing genuine-source failures and other owners' interfaces remain
separate. Main's intervening C7/Effects/emitter/SH08 changes were preserved by a
normal merge; the C2 implementation and test blobs were conserved.
