# Known diagnostic categories in failed development Core sessions

## Purpose and scope

This source candidate implements the narrow projection proposed by source trace
`18543ba4713bb79f808d59addb7ae7dfaf51ec2e`. The owning contracts are L9 host
error normalization, C15 stable codes and secret redaction, C6 input/provenance
joins and BOOT7 exact comparison inputs. It changes only the ordinary exception
outcome in 00899 after the accepted complete causal scan. It is not a semantic
correction to the failed String/Set construction.

The underlying causes of sessions 53455 and 56177 remain unknown. The source
trace's five literal codes and fifteen literal reasons are the complete
allowlist for this projection; other codes or reasons are intentionally not
inferred. In particular, nested `:facts`, missing-fields, raw values, paths,
messages, causes, stacks and diagnostic-id hashes are not projected.

## Closed failed-only sub-contract

The session version remains 2. Ready sessions, their keys, admission inputs,
legacy omitted/nil behavior and nil-return failures are unchanged. The existing
exception variant retains origin, kind and exception-class and can now add
exactly one optional `:known-diagnostic` field:

```clojure
{:origin :expected-core-construction
 :kind :exception
 :exception-class "clojure.lang.ExceptionInfo"
 :known-diagnostic {:schema-version 1 :code "C6-VERIFY"
                    :reason :construction-selected-reader-drift}}
```

The nested version literal is 1. Its closed key sets are
`#{:schema-version :code}` and `#{:schema-version :code :reason}`. Required code
is one of C2-HASH, C3-ID, C4-TRACE, C6-ORIGIN or C6-VERIFY. Optional reason is
allowed only for C6-VERIFY and must be one of the fifteen pinned top-level
reason literals enumerated in `expected-core-failure-source-trace.edn`.
Returned code/reason values come from the implementation's literal allowlist,
not the observed objects. No emitter authenticity or upstream authority follows
from a diagnostic category.

Either root `:id` or root `:rule` can supply a code. Two non-nil fields must be
exact Strings equal to one another; conflict or unknown code removes the
entire extension. Unknown reason removes only reason; a known code can remain.
A reason is never emitted without its known code. Nested facts are ignored.
Unsupported carriers, metadata, comparators and data cycles remove the entire
extension and retain the old class-only failed result. Unrelated unsupported
payload values can therefore hide an otherwise known code. This conservative
unknown bucket does not assert that the code was absent.

This explicitly amends the earlier class-only privacy contract. Consumers of
closed failed variants must opt into the four-key exception variant and the
nested version/key/value checks. ACC09 currently accepts the exact original
three-key variant; it would drop the extended field until Accounting, its sole
fixture writer, separately reviews that projection. This source candidate does
not edit or import the fixture, and does not authorize its execution.

## Native safety and error policy

Only an object whose exact class is clojure.lang.ExceptionInfo reaches getData.
Subclasses and arbitrary IExceptionInfo objects are not queried for data.
Before any lookup or operation on the returned data, the existing
`p15-s23-trusted-carrier-validation` runs with `:default-only` and its strict
native default policy. Only a passed native map reaches field lookup, exact
String comparisons and exact Keyword allowlist lookup. The checker and its
fixed native header access remain unchanged; no new validation framework or
callback interface is introduced.

The accepted c28 complete identity-cycle-safe causal scan is moved unchanged
into one private helper. It runs before projection, preserving the original
outer throwable, fatal propagation and interruption restoration even when
interruption lies after a fatal node. Projection never scans cause data for a
code. A projection failure is not swallowed as a class-only fallback: its own
complete causal scan restores interruption if present and the identical
projection throwable propagates, including ordinary projection exceptions.
No wrapper replaces that object or its cause. This keeps a failed projector
from masquerading as successful diagnostic containment. Existing upstream
verification containment and execute! normalization remain outside this cut.

## Source verification and acceptance

The source fixture evaluates only the actual selected session definitions,
integrity helpers and existing strict native checker/header definitions in
isolated fixture namespaces. It does not load Bootstrap, source authority
closures, plans or target constructors. Its hosted-only view port throws if
reached; this candidate uses only the strict native policy.

Controls cover both id/rule inputs for every known code, all fifteen reasons,
unknown codes/reasons, contradictions, reason-without-code and nested-fact
refusal. They check literal-owned Strings and exact closed maps, failed-session
refusal and replay refusal. Hostile ExceptionInfo subclasses, IExceptionInfo,
ILookup, native-map keys/values with hostile equality/hash, foreign maps and
metadata, foreign comparators, malformed array-map storage and cyclic native
data leave counters at zero and retain the class-only failed variant.
Projection failures exercise ordinary/fatal/direct/wrapped interruption,
interruption after fatal nodes, preexisting flags, original outer/cause identity
and no upstream continuation. Existing nil/ordinary/ready, same invocation,
legacy defaults, stale/substitution, catalog and integrity controls remain.

Source equations bind the causal helper to the exact accepted c28 scan,
unchanged construction calls and output expression, all eight other original
00899 forms and the seven governing ports. Full injected ready results and
unknown/ordinary/nil failure results match the predecessor. For known codes,
removing only the new nested field restores the predecessor's complete failed
session. These are interface and privacy checks, not real Core semantics.

OWN independent affected review of the exact frozen candidate is next.
Acceptance, Accounting composition, diagnostic observation, selector retry,
fourth launch, publication, CI and runtime remain outside this candidate.
The original genuine acceptance goal and historical failures are preserved.

Local verification passed 12 tests / 328 assertions, language boundary 6 / 11,
focused test lint with zero errors/warnings, both documentation validators and
diff hygiene. Source equations passed the exact scan/body/port/allowlist and
complete-result comparisons above. These results apply only to selected source
and injected ports; no target constructor, plan, Reader, CLI/provider, selector,
probe, lease or runtime operation was executed.
