# Native mobile document data ownership

## Scope and governing contracts

The intended slice moves the remaining B12 document manifest constructor,
ordered validation checks and coverage projection into `gravity.backend.mobile`.
D1, D2, D3, D8, D9, B1, B12, B13 and B14 govern its architecture and evidence
boundary. This scope declaration precedes implementation and its early draft PR.

The starting source is `52f6857e64ee1bc8922bc327108a3e14b30d0056`, tree
`c33c9e6756c397e06babe3cc1a85f117cb332d5b`. Published main
`7204d9b0b9c80011e637f8456deec558301127c1` is already its ancestor.
Prior slice CI results confer no acceptance on this successor.

## Proposed ownership boundary

The stage already owns ten native diagnostic context, catalog, failure,
override, fixed bundle/permission text and structural predicate APIs. Three
additional APIs will own manifest data construction, the existing 24 ordered
shape refusals and coverage projection. Bootstrap names and arities will remain
adapters. Explicit late operations must preserve hash/content and callback
order, borrowed input identity, source locations and generated origins.
The stage owns diagnostic payload shape; the caller owns throw transport and
publication. Source admission and final artifact assembly/hash remain upstream.
The stage must load without Bootstrap.

## Required evidence

The slice must emit both fixed text files, reject malformed document data
through existing B12 diagnostics, compare complete old/new records, exercise
all 24 guards, test late callback/value mutation, conserve unmoved source and
prior CI commands, and pass focused/cold/boundary/ownership checks. Independent
semantic, adversarial and provider reviews must bind the actual clean commit.
Results receive credit only after execution. No implementation or acceptance
is claimed here.

## Unsupported boundaries

Legacy iOS/Android, native symbol, binding, permission, lifecycle, thread,
storage, store-audit, proof and conformance labels are document data. Substring
predicates do not compile, package, sign or execute a mobile application, check
native classes/callbacks, grant OS permissions or verify lifecycle legality.

The retained C2-HASH source-artifact refusal remains held. This slice will not
invoke source/file-artifact builders, retry or rebind the reader, repin source,
substitute an upstream result, or reconstruct an admitted artifact. No verified
MIR/UI/platform IR, source-to-backend compilation, native bindings, platform
permission/thread/null/error/callback validation, package/signing/store
authority, device/simulator execution, B1/B12/B13/B14 conformance, release,
self-hosting or seed-retirement support is claimed.
