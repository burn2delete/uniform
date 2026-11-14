# Current source to verified scalar MIR

The production entrypoint is
`gravity.compiler.application/current-source-to-verified-mir`. It constructs
and freshly verifies checked Core and MIR from an actual `.gravity` or `.qst`
source file and an explicit source policy. It supports the existing hosted,
safe, JVM-targeted checked-I64 addition and applicable Boolean scalar families.
This is one intermediate compiler handoff, with one exported `main` function.

## API

The caller writes ordinary source, for example:

```clojure
(ns compiler.integration.add
  (:profile :hosted)
  (:target :jvm)
  (:effects #{:error/raise})
  (:capabilities #{})
  (:safety :safe)
  (:exports [main]))

(defn main [left :- I64 right :- I64]
  (+ left right))
```

Then the caller invokes the application with the file path and its policy:

```clojure
(require '[gravity.compiler.application :as compiler])

(compiler/current-source-to-verified-mir
  "/path/to/add.gravity"
  {:profile :hosted :target :jvm :effects #{:error/raise}
   :capabilities #{} :safety :safe})
```

The policy has exactly the five keys shown above and must match the parsed
source declaration. This entrypoint accepts `:hosted`, `:jvm`, `:safe`, empty
capabilities, and either empty effects or `#{:error/raise}`. Checked I64 addition
requires the declared raise effect. The Boolean examples use empty effects.
The caller supplies no accepted Core, MIR, facts, verifier reports, plans,
bindings, or authority tokens. Extra policy keys are rejected.

The public wrapper lives in
[`application.clj`](../bootstrap/clojure/src/gravity/compiler/application.clj).
A successful result has `:artifact :gravity/compiler-application`,
`:schema-version 1`, and `:status :accepted`, together with these fields:

| Field | Meaning |
| --- | --- |
| `:source` | Actual path, byte count, and SHA-256 of the selected source bytes. |
| `:source-target` | `:jvm`, matching the source declaration and policy. |
| `:checked-core` | Source-bound checked Core from the existing C6-C10 owner. |
| `:mir-module` | MIR from the existing C11 owner. |
| `:core-verification` | Validated projection of the fresh Uniform Core verifier report. |
| `:mir-verification` | The actual Uniform MIR verifier report, bound to the Core artifact and MIR module. |
| `:source-policy-receipt` | Current parent, source-policy, resolution, and selected owner evidence. |
| `:compiler-bindings` | Source and executable identities for the actual selected C6-C10 and C11 owners. |
| `:compiler-host` | `{:implementation :clojure-stage0-seed :target :jvm :self-hosted? false}`. |
| `:artifact-id` | Digest of the application result, including its provenance. |

Rejections throw `ExceptionInfo` carrying owned diagnostic data. Diagnostic
rule IDs and source attribution remain available to callers; the adapter does
not replace semantic failures with a generic success or report.

## Construction and scope

The semantic owners remain
[`c6_c10_checked_core_pipeline.gravity`](../language/compiler/src/gravity/compiler/c6_c10_checked_core_pipeline.gravity)
and
[`c11_mir_specification.gravity`](../language/compiler/src/gravity/compiler/c11_mir_specification.gravity).
Their source contracts and interfaces remain the binding owners. The small
production adapter composes the existing ingress, binding, envelope, digest,
and fresh-construction components 00716, 00720, 00723, and 00724a with the C11
family and opaque-construction components 00740 and 00744.

1. Read the selected regular source file as strict UTF-8 and snapshot its
   actual bytes. Select current Reader, Syntax, expansion, and C5 owners from
   their actual source bytes.
2. Reconstruct the source parents and binding envelope, preserve the current
   Reader/Syntax/resolution evidence, and check the parsed source policy
   against the explicit caller policy.
3. Invoke the actual Uniform C6-C10 builder. Mechanically resolve its digest
   requests through the existing host transport.
4. Reconstruct the parents again, compare the source and binding evidence,
   and invoke the actual Uniform C6-C10 verifier with the fresh envelope and
   original builder template and digest requests. Validate the returned report
   before producing the public checked-Core artifact.
5. Within the original lexical opaque C11 construction owner, select the
   existing scalar family and invoke its actual Uniform builder and verifier.
   Only after the actual verifier passes, compare its report structure with
   the report constructed by that same Uniform owner. The flat Boolean
   sequence uses its existing family builder, verifier, and report wrapper.
6. Recheck the selected source and compiler-owner bytes before returning the
   application artifact.

The host performs transport, carrier checks, identity computation, and
composition. It does not add a second semantic checker. Parameter sources
retain the existing Clojure seed plan assembly. Their expression-owner receipt
records `:invoked? false` with `:reason :parameter-seed-plan`; it does not claim
that the Uniform expression emitter ran. The no-parameter flat Boolean source
uses the current expression owner. The compiler host and source target are
separate fields even though both are JVM in this increment.

The source-policy receipt retains `:admission-status :development-unadmitted`,
`:verification-scope :selected-current-source-host-seed`, and
`:self-hosted? false`. Fresh development verification does not establish a
historical source-interface admission or seed-retirement authority.

The existing public historical interfaces and pins remain unchanged. Reusing
the internal current-source binding transport does not broaden a historical
interface's family claims. The separate whole-runtime packet is not repaired
or used as an alternate route.

Accepted I64 addition retains I64 parameter and result types,
`#{:error/raise}` latent effects, numeric-error information, immutable ownership
facts, and a runtime-checked signed 64-bit overflow guard. MIR consumes the
guard token before the addition. The tested Boolean conditional has a Bool
parameter and result and an explicit branch/join; the tested flat Boolean
sequence has no parameters, a Bool result, and no runtime guard.

No JavaScript target admission or emission, compositional multi-function
support, self-hosting, seed retirement, automation changes, or end-to-end
compiler completion is claimed. Other arithmetic is outside this application
increment. These source artifacts remain genuinely JVM-targeted.

## Development evidence

Work is isolated on `codex/current-source-verified-mir`. Recorded checkpoints:

| Checkpoint | Commit |
| --- | --- |
| Latest-main base and planning baseline | `4023d7657c9bdca8329a7860c17f6d2f0b56148e` |
| Initial production checkpoint | `e8a398d8c59a3179b6289c2a8d6b5ae37a3d1ad7` |
| Production candidate for the evidence below | `ce7a37bbb54d9f66d5854e3829fb15ea02f7a9f1` |
| Candidate plus isolated replay-test source selection | `5971348f638971376040912f0124dc0032522f4c` |

The last checkpoint changes only the existing replay test's explicit current
Reader selection (two inserted lines replacing one line). It does not change
the production candidate or any historical owner pin. This document is an
evidence checkpoint; pending results below are not reported as passes.

### Source-backed baselines

The required source-backed baselines were reproduced before editing:

```sh
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh12-checked-i64-add-producer-test \
  --exact sh12-checked-i64-add-produces-original-bound-facts \
  --exact sh12-checked-i64-add-rejects-undeclared-error-effect \
  --progress
# 2 tests, 27 assertions, 0 failures, 0 errors

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh13-mir-integer-add-bridge-test \
  --exact sh13-checked-i64-add-original-producer-bridge \
  --progress
# 1 test, 112 assertions, 0 failures, 0 errors
```

Evidence: `baseline-sh12.log` and `baseline-sh13.log`. These tests establish
actual current-source construction, fresh verification, checked I64 MIR,
numeric endpoints, overflow rejection, and undeclared-effect rejection. They
do not establish target emission.

### Confirmed application and boundary commands

```sh
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.compiler.application-test \
  --exact current-source-to-verified-mir \
  --progress
# 1 test, 234 assertions, 0 failures, 0 errors

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.compiler.application-test \
  --exact current-source-flat-boolean-to-verified-mir \
  --progress
# 1 test, 71 assertions, 0 failures, 0 errors

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.compiler.application-test \
  --exact current-source-rejects-supplied-authority \
  --progress
# 1 test, 20 assertions, 0 failures, 0 errors

clojure -M:test \
  --namespace gravity.self-hosting.sh01-language-boundary-test
# 6 tests, 11 assertions, 0 failures, 0 errors

clojure -M tools/validate_gravity_docs.clj
# validation passed: 240 docs, 19 phase indexes, ASCII, no placeholders
```

The corresponding logs are `application-accepted-v2.log`,
`application-flat-v2.log`, `application-authority.log`,
`language-boundary-final.log`, and `docs-validation.log`. Documentation
validation was recorded before this evidence expansion and needs its final
rerun with the completed note. Logs are retained in the development evidence
directory and summarized in the draft PR; temporary source paths below belong
to these exact recorded runs.

The main acceptance test creates fresh source files and calls the public API
without importing SH12/SH13 test helpers. It checks the original I64 source,
a renamed `.qst` file with a renamed namespace and parameters and reversed
operand order, and a separate Boolean conditional. The renamed case also
binds hostile Clojure printer settings and verifies that compiler identities
remain stable. The separate flat-family test compiles `(do true false)`.

The supplied-authority test records ten rejections: missing policy, empty
policy, missing effects, a JS target, supplied Core, supplied MIR, a supplied
verifier report, a supplied binding, a supplied token, and a source-artifact
map in place of a path. The first nine retain `C11-MODULE` and
`:application-explicit-hosted-safe-jvm-policy`; the last retains
`C11-MODULE` and `:application-actual-source-path`. The 20 rejection assertions
passed. Logged source attribution names the actual file for policy rejections
and `<compiler-application>` for the non-path input.

### Accepted artifact identities

These are observed identities, not inputs to the API. The MIR module identity
is its module symbol; it is not a separate SHA-256 digest. The actual MIR
report binds that module identity to the checked-Core artifact ID. Application
identity also includes provenance, so it describes the recorded source path
and receipt as well as the resulting artifacts.
The logged `source-policy` identity is the receipt's
`:owner-metadata-content-id`.

Original I64 addition, report scope `:closed-i64-parameter-linear`:

```text
path: /tmp/uniform-application-11332302631940928726/add.gravity
source: sha256:c63ca7179cd0f0384e7ec77b1a42da1e8275530f97738b2a491a9e96d5a5a5a5
checked-core: sha256:40fbe6722cba1f7038612de2f3be2a4f376234c8df02799813e0ec05c5608716
application: sha256:d07b7567da432361f75aa742ba6a11f9ce97a60194ae5a0e8c11d83489e9d0d7
mir-module: compiler.integration.add
source-policy: sha256:b360e7acb16009fe46a3d03e17b3c49b3d89c01fa961c35ab5358311857ca8b5
source-target: :jvm
```

Renamed and reordered I64 addition, report scope
`:closed-i64-parameter-linear`:

```text
path: /tmp/uniform-application-7216401500697333403/renamed-sum.qst
source: sha256:fc0be28e1501da2d81f6bd4d1bf8772c94fc789241d888da34516076a433dac9
checked-core: sha256:cf310742e0ea8fb33df93dbb6618045cb1a0ddf5176e48220c44af9e32d4d4cb
application: sha256:2b335cb825df64fb82e955774b92e2273424c5a8cdd7522ffc853629272b4d7e
mir-module: example.renamed.sum
source-policy: sha256:da8428a0b5ca10128a4f32940be9332ccea322286e067638d17afbe28ac1548f
source-target: :jvm
```

Boolean `(if enabled true false)`, report scope
`:single-conditional-branch-join`:

```text
path: /tmp/uniform-application-8931874081418372500/choice.gravity
source: sha256:405f83784a94f2fe0237ca100621edf6eff0e77aad972b0dc606621f76d3d67a
checked-core: sha256:f27e383b79f6660ab3341b4d61f506e86ab493909d0d37fd86ef663746ae5fd5
application: sha256:dff68b1756eb1e3bea55f7086abd97677780af10b9fbd5b6e34105761d779dd0
mir-module: example.boolean.choice
source-policy: sha256:383c585dfc120cf7e9bb37474ed900c12dd64724ae47de1ea9056c4cfb574fed
source-target: :jvm
```

Boolean `(do true false)`, report scope `:closed-flat-bool-literal-sequence`:

```text
path: /tmp/uniform-application-13498888900902245515/boolean-sequence.gravity
source: sha256:7fadb331489f35148e0d4918066470154a784079cb5a1b8e1d9b882300d695ca
checked-core: sha256:41aec8fc59aa6e4ff4015e74ea9e3e9c8a332c3e968c1ffc4644fbc67f239858
application: sha256:efa73f280bad21fc8cf78ce9f124aec6011af3ecb77b6ffec65b58cfb3a94b83
mir-module: compiler.integration.boolean.sequence
source-policy: sha256:f0e2dec5f634ebbcd2037df1ac62f879c4091896a46b87396a648b58322944bd
source-target: :jvm
```

Selected Uniform owner source identities for the production candidate:

```text
language/compiler/src/gravity/compiler/c6_c10_checked_core_pipeline.gravity
sha256:c5433537cf9423b14a0456696d9f7c3eb5953f88a9535787ef9c53af883bc6b8

language/compiler/src/gravity/compiler/c11_mir_specification.gravity
sha256:a2a570f314f8d787a70c093574c64041acd019844dbea743096ac74c5f2415f7
```

### Existing replay prerequisite and remaining verification

An additional existing regression,
`gravity.self-hosting.sh12-branch-join-mir-test/sh12-current-c6-replay-distinguishes-projection-substitution-from-source`,
failed on the unchanged base in an isolated baseline checkout. Its ingress
used the historical default Reader binding while selecting current source.
`baseline-replay-current-selection.log` records `C2-HASH`, missing field
`:exact-regular-pinned-sh03-reader-source`, expected Reader byte count
`231576`, and actual byte count `246369`: 1 test, 1 assertion, 0 failures,
1 error. This establishes that the failure predates the production adapter.

Commit `5971348f638971376040912f0124dc0032522f4c` selects the current Reader
explicitly at that existing test's ingress call. This repairs the exact
current-source replay prerequisite without changing a historical pin,
historical interface claim, the separate whole-runtime packet, or Uniform
semantics. The repaired replay result is pending at this evidence checkpoint.

Partial negative-test logs already record these owned rejections, each with
the actual temporary input path. The full selectors are still running; these
individual observations do not establish their final pass counts.

| Observed case | Diagnostic | Missing fact | Log |
| --- | --- | --- | --- |
| Altered Core type fact | `C6-VERIFY` | `:root-bound-gravity-artifact-template` | `application-tampered.log` |
| Altered Core verifier report | `C6-VERIFY` | `:fresh-gravity-source-verifier-replay` | `application-tampered.log` |
| Substituted source identity | `C6-ORIGIN` | `:source-content-front-end-plan-binding` | `application-substitution.log` |

The following checks are implemented and running or queued; final results
must be recorded before calling verification complete:

| Test or gate | Required evidence still pending |
| --- | --- |
| `current-source-rejects-invalid-programs` | Malformed source (`C2-DELIMITER`), wrong operand type and arity (`C6-LOWERING-GAP`), and undeclared raise (`C8-UNDECLARED`), with original source attribution. |
| `current-source-rejects-tampered-handoffs` | Altered Core type facts, altered Core verifier report, missing MIR guard evidence, and altered MIR report identity are rejected. |
| `current-source-rejects-source-and-binding-substitution` | Substituted source binding, compiler binding, and source bytes are rejected. |
| `current-source-keeps-the-increment-scope` | Subtraction, multiplication, and an extra function are rejected by this application scope. |
| Existing source/MIR regressions | Original checked-I64 producer, Boolean branch/join, integer bridge, numeric endpoint/overflow, and repaired current-source replay results on the candidate. |
| Final documentation validation | Completed evidence note remains ASCII and passes the documentation validator. |

## Next handoff

Finish the pending verification record for this increment and keep its work on
the draft PR. The next separately authorized compiler increment is actual JS
target admission and emission from verified MIR, with its own source policy
and target evidence. This increment does not relabel JVM source artifacts as
JS, claim target output, or claim an end-to-end compiler. No merge is included.
