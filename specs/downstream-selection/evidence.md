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

The downstream verification-report, verify! and authentic? overloads also
preserve explicit selection. Verification constructs C11 through its versioned
public constructor before downstream preflight, then reconstructs the expected
packet through the versioned packet entrypoint. Legacy replay retains
preflight-before-unversioned-construction order. No report is cached between
invocations or accepted from the caller.

Independent review of 0d0a3d29 found that the selected constructor was inside
backend containment, which would replace the owner's compact header diagnostic.
The correction places that public owner call outside backend containment.
The composition control now fails if an owner rejection reaches containment;
its former rethrowing containment stub was insufficient evidence. The reviewed
revision and this correction retain separate Git identities.

This increment does not extend C13/C14/B1 source contracts, wire the accepted
relative C13 helper, change source pins or introduce a transport format. Existing downstream
contracts continue to reject unsupported inputs. The Core artifact schema
remains 1, independently of source-interface version 3. Core and C11 policy
digest domains remain the responsibility of the actual C11/Core owner.

## Explicit downstream source-contract binding

After the public C11 verifier returns, the explicit constructor selects
`p15-s23-c11-mir-contract-binding` using the already validated header version
and derives its exact rule with `p15-s23-c11-mir-source-rule-record`. These are
existing Native owner definitions at f66; no source tuple is caller-supplied or
selected from report labels. The helper compares this rule to both the original
C11 artifact and the fresh report using the owner's strict structural comparator.

The original artifact/report ingress values must match. The actual Core/context
and policy go to the owner's existing versioned policy DATA validator, followed
by its policy-binding projection. The resulting distinct Core-policy binding ID
and C11-policy hash must match the artifact's independent verification report,
MIR pass record and B1 preflight binding. This is additional relative wiring
after fresh public authority; it is not a standalone report admission API.

The packet retains this selection under `[:c11 :source-contract-selection]`.
The existing packet semantic projection includes the complete C11 record;
semantic, artifact and physical-provenance binding identities are recomputed.
Versioned verification reconstructs that same selection through the fresh
public path and strictly compares the complete packet. Coherently recomputing
an input packet's digest cannot replace the fresh expected packet. Legacy
construction does not add the selection field.

No Gravity source pin was changed. C14/B1's historical rule gates still reject
unsupported source tuples; binding the selected C11 contract does not confer
new lowering acceptance. That requires independently reviewed downstream source
contracts and their real source/plan/function identities.

Source-only main comparison used main 6276c94b0b753c888ef8fecc794bcc81245ea3b3
and merge base 0c3c3706126d3262a1fd9cf6a31372de1124995d. Main had no changes
in the two edited packet components or their packet semantic projection since
that base. Main still lacks the versioned C11 source-interface module and public
arities. Their exact producer-owned composition gate remains Native f66's
versioned public constructor/verifier and fixed contract/rule helpers; the
consumer does not import or reimplement them. This comparison is not a merge
or composed runtime acceptance.

The three versioned policy/contract dependencies are declared without local
implementations so the legacy baseline can still resolve the source forms.
Declarations preserve existing Native Var roots and leave missing definitions
unbound; they do not install a fallback or confer versioned acceptance.

Genuine Core/context, separately fresh Core report, versioned C11 artifact/report,
actual downstream acceptance and substitution negatives remain UNACCEPTED.
No compiler, producer, authentication, broker or process inspection ran.

## Compiler-free controls

The test reads and evaluates actual Clojure dispatch forms in isolated namespaces
with injected semantic owners. It loads neither the bootstrap nor Gravity source.
Composition controls read immutable Native header, C11 source-interface and public
C11 dispatch sources using read-only `git show`; SHA256 pins are checked before
isolated evaluation. Missing dependency objects fail rather than selecting another
revision. Actual native header parsing and mixed-version discrimination execute;
Core production, policy validation and authentication do not.
The controls check exact argument/report object forwarding,
owner-before-downstream order, unchanged injected owner rejections for unsupported
or mixed versions, altered family, crossed tuples and Reader/expression
commitments, and the original legacy route. These fixtures are NONACCEPTANCE;
injected altered-family/crossed-tuple/commitment failures demonstrate propagation
only, not execution of genuine policy/authentication validators.

Commands observed before review:

```text
clojure -J-Xmx256m -Sdeps '{:paths ["bootstrap/clojure/test"]}' -M -e "(require 'gravity.self-hosting.downstream-selection-test)(let [r (clojure.test/run-tests 'gravity.self-hosting.downstream-selection-test)] (System/exit (if (zero? (+ (:fail r) (:error r))) 0 1)))"
9 tests, 113 assertions, zero failures/errors.
The earlier dispatch-only increment passed 3 tests, 27 assertions.

clojure -J-Xmx256m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
6 tests, 11 assertions, zero failures/errors.
```

Next source criterion: independent exact-source review and validated composition
with the Native versioned API, then separately scoped downstream source-contract
selection. In particular, Gravity's existing b1-c11-source-rule-valid? still
checks historical exact source pins. The header selects the upstream built-in
C11 source contract; it cannot legalize a new downstream source tuple. No
replacement source/plan/function identities are invented here.
Actual execution remains held pending the
producer's supported runtime disposition and genuine tuple handoff.
