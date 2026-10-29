# Standard dispatch source and returned-product binding

Task: `reader/finite-remaining-work/extension-policy-closure` (open).
Base: PR549 merge `60f59d225b03338eb30cdc22850838ecebd07303`, tree
`83d1e0a4ee9f4aaccd8e8164594f495f672a5ef5`. That merge is consumed, not retried.

## Concrete correction

[C2 Reader design](../../docs/phase-06-compiler-architecture/081-c2-reader-implementation-design.md)
requires exact original-byte joins, source spans/origins, registered handlers,
declared authority, and independent fresh reconstruction. PR549 connected the
normal host-value walk to real registered standard handlers. Its observations
were deliberately outside the authenticated SH03 products. The precomputed
product checker compared projected records and their summary, but did not
reconstruct the host values or compare those observations.

The standard-tag walk now checks its original byte snapshot against both
source-content hashes, recomputes the existing hosted source identity, and
checks duplicated options, features, root, relative path and actual supplied
path joins before dispatch. Native identity carriers are checked before any
hash printing, collection traversal or metadata protocol on foreign objects.
No project input/work quota is introduced. Source origin and nonempty,
ordered, in-snapshot spans are required. The real handler seed and invocation retain
source path and origin alongside the existing policy/handler/input/result
identities. Complete tag-policy preflight remains before the first call;
only the existing implementation-owned pure inst/uuid handlers enter this path.

After the existing internal authority, raw-result, digest and report checks,
the precomputed-product payload checker uses original bytes, semantic closure
and the same pure-handler host reconstruction. It compares every reconstructed
value through the existing raw-to-adapted form-id map and the complete actual
observation vector. Missing, duplicated or altered observations and substituted
returned values cannot pass this comparison. Sources without tags keep the
existing payload checks and require no extension observations. Envelope
verification remains mandatory after payload validation.

Observations remain `:unsealed-bootstrap-observation`. Matching a deterministic
hash or this portable reconstruction is not producer authentication, a grant,
fresh Gravity parsing, public admission, mapping acceptance or task closure.
No source/envelope schema or identity domain is promoted or weakened.

## Executed portable controls

The existing `gravity.reader.normal-dispatch-test` executes exact production
Clojure definitions with injected parsed graphs and semantic tables, real
source-unit construction, byte hashes and registered Date/UUID handlers.
It does not fabricate a successful authenticator or launch a Gravity parser.

- Normal/direct dispatch: 18 tests, 211 assertions, zero failures/errors.
- Language boundary through the prescribed `-M:test` command: 6 tests,
  11 assertions, zero failures/errors.
- All changed production files read successfully with `*read-eval* false`.
- `git diff --check`: passed.

Controls cover changed source/content/root identities, stale/missing bytes,
content-id and supplied-path joins, origin/span refusals before invocation,
foreign maps/metadata implementations and lazy sequences with zero callbacks,
actual fresh pure-handler invocation, altered policy/handler/grant/source/span/
input/result observations, missing/duplicate observations and forged/missing
returned values or form projections. Existing declared effect/provider grant,
unknown-tag, native-class and source-slice controls remain passing.

Early test attempts included an unchanged root-id mutation, a path mutation
that also changed its expected supplied path, and an invalid IMeta reify.
Those are failed fixture attempts, not successful controls; corrected controls
produced the results above.

## Next bounded execution gate

The new hook in the complete internal precomputed-product verifier has not
been executed through genuine Reader products. Loading that route requires
the held Gravity producer/bootstrap path. No producer, compiler, broker,
process-state probe or alternate authentication path was launched here.

The next request is one approved selected-current Reader-to-C2 transaction
containing both standard tags, with explicit project/source identities. Observe
the actual registered invocations and returned values, then exercise the
existing internal consumer with missing/altered/duplicate observations and
changed returned values while retaining the authentic source/result/envelope
checks. Both source extensions need eventual composition evidence. This is a
finite adapter check, not a request for a full compiler suite or a new producer.

Independent exact-source review and genuine adapter execution remain separate
gates. Full extension-policy/authentication completion, effectful normal tags,
arbitrary handlers, mapping, release, self-hosting and seed retirement remain
open. The canonical Reader task and accepted historical receipts are unchanged.

## Independent review correction

ACC12 requested changes on `1c10242e4c1bc68557f70c1ed932e2a3615ac464`,
receipt SHA256
`21440cddd43920a232f19b2e75eb013e70e4cab2a59e7bca042075674ab36446`.
A direct standard-dispatch caller could supply a foreign source-path object
that reached canonical hashing before the String check. The corrected direct
entry validates the path before seed hashing; an Object `toString` tripwire
requires zero callbacks and zero handler invocations. The normal host walk
also validates the native form/span before inspecting its byte extent, with a
foreign map `get` tripwire requiring zero callbacks. The new comparison helper
uses the semantic name `reader-standard-dispatch-products-match?`; historical
declarations and wire identifiers are unchanged. The earlier source remains
changes-requested evidence. The successor requires its own review.

The earlier CI static job also rejected the missing declaration of the
test-loaded comparison helper. Its semantic name is now declared alongside
the existing exact-definition fixture entrypoints. No lint exemption or
workflow change is introduced.
