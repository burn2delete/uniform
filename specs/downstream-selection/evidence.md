# Explicit C11 selection at downstream construction

Owner: ACC11. Contract basis: C13, C14, B1 and OWN-06 PR545
(`a2011d9e1a8d91116dba933bbd12c4d91d336da0`). Candidate basis:
`d2e35467d26fde9bc241e235b4c5916ce9bbb601`.

The four-argument `p15-s23-stage2-c13-c14-b1-packet-from-c11!` forwards
`[c11-artifact checked-core context interface-header]` to the existing public
C11 verifier. Its fresh report is passed unchanged to the existing downstream
construction stages. Owner header, mixed-version, policy and authentication
rejections occur before downstream source bindings or stages and retain their
owning diagnostics. Downstream host failures retain the existing containment
categories. The three-argument overload retains unversioned verification.

## Dependency and unaccepted work

The inspected versioned public C11 API is at Native candidate
`f66d9ff4554815b89ee7d51ee73b7961f0c33a44`, tree
`1ee9986eb613abe6c0218603d78d5f4279a4ac9d`. The explicit header is
`{:interface :gravity/c11-mir :version 3}`. This ACC11 base still has legacy
C11 arities. The new overload is dependent draft preparation; it cannot run
successfully here until the versioned owner API is composed and validated.
No producer implementation was copied or replaced.

This increment does not extend C13/C14/B1 source contracts, add a versioned
downstream packet-verifier overload, wire the accepted relative C13 helper,
change source pins or introduce a transport format. Existing downstream
contracts continue to reject unsupported inputs. The Core artifact schema
remains 1, independently of source-interface version 3. Core and C11 policy
digest domains remain the responsibility of the actual C11/Core owner.

Genuine Core/context, separately fresh Core report, versioned C11 artifact/report,
actual downstream acceptance and substitution negatives remain UNACCEPTED.
No compiler, producer, authentication, broker or process inspection ran.

## Compiler-free controls

The test reads and evaluates only the actual public Clojure dispatch form in
an isolated namespace with injected owners. It loads neither the bootstrap
nor Gravity source. It checks exact argument/report object forwarding,
owner-before-downstream order, unchanged injected owner rejections for unsupported
or mixed versions, altered family, crossed tuples and Reader/expression
commitments, and the original legacy route. These fixtures are NONACCEPTANCE;
they do not execute the genuine header/policy/authentication validators.

Commands observed before review:

```text
clojure -J-Xmx256m -Sdeps '{:paths ["bootstrap/clojure/test"]}' -M -e "(require 'gravity.self-hosting.downstream-selection-test)(let [r (clojure.test/run-tests 'gravity.self-hosting.downstream-selection-test)] (System/exit (if (zero? (+ (:fail r) (:error r))) 0 1)))"
3 tests, 27 assertions, zero failures/errors.

clojure -J-Xmx256m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
6 tests, 11 assertions, zero failures/errors.
```

Next source criterion: independent exact-source review and validated composition
with the Native versioned API, then separately scoped downstream source-contract
selection and packet verification. Actual execution remains held pending the
producer's supported runtime disposition and genuine tuple handoff.
