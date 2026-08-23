# P07-D102 B5 JVM Backend Proof Report

Date: 2026-06-29
Task: `P07-D102`
Status: complete (stage0 B5 JVM backend document coverage)

## Governing Document Read

- `docs/phase-07-backend-architecture/102-b5-jvm-backend-design.md`

## Implemented Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity`
- `bootstrap/clojure/fixtures/rejected/backend-b5-*.gravity`
- `docs/artifacts/phase-07/backend/stage0-p07-d102-b5-jvm-backend-proof.edn`

The `backend-b5-jvm-document` command emits
`:gravity/stage0-b5-jvm-backend-document-artifact` from the current P07-T03
hosted lowering artifact. It records B5 classfile/JVM target pinning, class and
module model, Java source and module descriptors, JAR/module artifact records,
Java interop descriptors, nullability and exception translation, reflection and
dynamic-use policy, classloading policy, deterministic resource cleanup,
thread/monitor/executor/atomic effect records, native-image configuration,
profile-boundary rejection, B5 diagnostics, document-specific results, and
capability-based proof.

## Validation

```text
clojure -M:gravity backend-b5-jvm-document bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-b5-jvm-backend-document-artifact,
 :task "P07-D102",
 :artifact-id "sha256:cd31fb185a1936f408d0ef6f666265c8ee7554ba9c3a9fb27710407f4292ca76",
 :document-set ["B5"],
 :diagnostics 11,
 :rejected-designs 5,
 :conformance-criteria 13,
 :java-structural true,
 :javac-proof :requires-proof-command,
 :jar-proof :requires-proof-command,
 :proof :complete}
```

Java source hash:

```text
sha256:cdde9b2e2e9379bff5d84f141f4bb1bf227ca31117219a00ca49155120e0cdbc
```

Module descriptor hash:

```text
sha256:e9637572a955ba204d3722d80ac1deb17863fa7910c0ffc1725fdbc0fedec0c1
```

```text
javac --release 21 -d /tmp/gravity-p07-b5-classes /tmp/gravity-p07-b5-src/module-info.java /tmp/gravity-p07-b5-src/gravity/stage0/Hosted.java
passed
```

```text
jar --create --file /tmp/gravity-p07-b5.jar -C /tmp/gravity-p07-b5-classes .
passed
```

```text
jar --list --file /tmp/gravity-p07-b5.jar
META-INF/
META-INF/MANIFEST.MF
module-info.class
gravity/
gravity/stage0/
gravity/stage0/Hosted$GravityPanic.class
gravity/stage0/Hosted$Resource.class
gravity/stage0/Hosted.class
```

```text
clojure -M:test
Ran 81 tests containing 4616 assertions.
0 failures, 0 errors.
```

```text
clojure -M -e <phase-07 backend proof EDN parse>
{:parsed 11,
 :tasks [:P07-D098 :P07-D099 :P07-D100 :P07-D101 :P07-D102 :P07-T01 :P07-T02 :P07-T03 :P07-T04 :P07-T05 :P07-T06],
 :statuses [:complete :complete :complete :complete :complete :complete :complete :complete :complete :complete :complete]}
```

```text
git diff --check
passed
```

## Rejected Diagnostics

The rejected fixture suite covers all B5 JVM backend diagnostic IDs:

- `B5-TARGET`
- `B5-NULL`
- `B5-EXCEPTION`
- `B5-REFLECTION`
- `B5-CLASSLOADING`
- `B5-INTEROP`
- `B5-RESOURCE`
- `B5-THREAD`
- `B5-NATIVE-IMAGE`
- `B5-PROFILE`
- `B5-MANIFEST`

## Proof Records

- `docs/artifacts/phase-07/backend/stage0-p07-d102-b5-jvm-backend-proof.edn`

## Remaining Limits

This completes `P07-D102` for deterministic Clojure stage0 coverage of the B5
JVM backend design contract. The emitted Java and module descriptor compile
with `javac --release 21` and package into a JAR, but this does not claim
production JVM backend optimization, native-image execution, server/runtime
deployment, or full Phase 07 completion.
