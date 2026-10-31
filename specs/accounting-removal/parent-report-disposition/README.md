# Normal parent report and resource disposition

This source increment extends accepted development-result boundary `47b44ed9`
and its accepted `eefd1ed5` resource custody basis. It does not import the held
pending-start or effective-configuration candidates.

## Callable boundary

`sh01-host-resource-broker/with-lease` retains its three-argument behavior.
The optional fourth argument is a synchronous observer receiving the actual
operation result/error, raw lease/process registry, and disposition after the
release attempt or quarantine decision. States are `:acquire-failed`,
`:released`, `:release-failed`, and `:quarantined`. Only a successful actual
`release!` return supplies a release receipt. An attempted release that throws
can be partial; neither continued possession nor successful release is assumed.

Operation, assessment, release, and observer errors retain their raw objects.
Fatal causes take precedence; other errors and the raw disposition observation
are suppressed on the thrown error. Interrupt status is restored. The observer
cannot skip the necessary resource decision by failing. There is no retry or
automatic quarantine clearance.

The existing synchronous `run-namespace-process` caller accepts opt-in
`:parent-report {:file absolute-path :limit-bytes positive-integer}` alongside
its existing `:resource-broker` and `:development-result` options. It validates
the actual accepted child-completion result and correlated resource disposition,
then writes the closed, non-authoritative
`:gravity/development-parent-report-v1` report with CREATE_NEW, WRITE and
NOFOLLOW_LINKS. The caller owns the output directory. No directory creation,
overwrite, delete, retry, alternate transport or ALIAS-specific schema is added.
The report records source/configuration equality supplied by the caller; this
is not effective-configuration attestation or semantic authenticity.

Failed child semantics remain failed after successful resource release. A
rejected child report remains an operation error independently of release.
Final validation, encoding, output-limit or publication failure propagates
after the disposition decision; it does not fabricate a published receipt.
Successful publication adds `:parent-report` to the ordinary result. On failure,
the suppressed local observation retains the original result/error and custody.

## Lifetime boundary and remaining handoffs

Quarantine is strong retention inside this JVM only. A returned value, thrown
error, or report does not prevent a one-shot parent from exiting. JVM-owned
locks and this registry cannot provide custody after parent exit. Unresolved
custody is explicitly `:unsupported-in-JVM-only`; no keepalive or durable
containment facility is implemented. A future owner must provide an accepted
externally surviving custodian and supported resource/handle transfer or
containment contract if continued custody after exit is required. No such
transfer facility is established by this source increment.

Pending native start, effective parent/child configuration, actual lifecycle,
report readback/publication and final-release observations remain separate
consumer obligations. Tracked handles are not global descendant proof. No
change here clears held reviews, admits a genuine operation or proves runtime
or release readiness. OWN consumer and RV semantic collector remain separately
owned; neither is edited or imported.

## Source and model validation

`check.clj` runs the actual new broker arity, publisher and normal caller with
injected acquire/release and process ports. It uses owned temporary DATA files;
it never acquires a real lease, starts a child, probes a process, or loads
Bootstrap or the production runner namespace. The model has 9 tests and 125
assertions, including synchronous order, raw identities, fatal/interruption
handling, failed semantics, refused/contradictory reports, publication failures,
quarantine, assessment errors, and actual normal-caller wiring.

Separate read-only form comparison preserves every other source form, the
legacy broker body and the default parent path. Four source mutations must fail:
omitted release, demoted fatal error, falsely attempted quarantine release, and
quarantine promoted to passing. Model DATA publication is not a real final
operation receipt. Independent acceptance and any later publication are outside
this frozen source task.
