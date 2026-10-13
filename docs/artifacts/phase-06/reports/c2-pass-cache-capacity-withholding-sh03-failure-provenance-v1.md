# C2 Pass-Cache Capacity Withholding SH03 Failure Provenance Version 1

Status: provisional historical failure evidence; not an acceptance receipt

## Purpose and evidence limit

This artifact records the real SH03 failure that motivated
`c2-pass-cache-capacity-withholding-architecture-v1`. It is deliberately
provisional. The observed run was made in a dirty implementation worktree based
on `2178263120d81ea0a659218e6c2376bfde928b27` with the SH03 source-pin repair
present. No clean commit, tree, complete stdout/stderr capture, or worktree
digest was bound at run time. This record therefore supports problem
provenance only. It is not implementation, C2, C16, SH03, cache, equivalence,
proof, release, or self-hosting evidence.

## Observed run

- Base commit: `2178263120d81ea0a659218e6c2376bfde928b27`.
- Worktree state: dirty; the SH03 pin repair was present; the complete dirty
  path set and worktree diff were not captured and are unknown.
- Command: `clojure -M:test --namespace gravity.self-hosting.sh03-reader-adapter-test --fail-fast`.
- Result: 21 tests, 983 assertions, 1 error.
- Failing test: `sh03-public-reader-accepts-its-own-gravity-source`, lines
  869-883 of the recorded test file.
- The test resolves `bootstrap/sh03-reader-source-relative-path`. The repository
  path named by that binding is
  `bootstrap/gravity/src/gravity/bootstrap/reader.gravity`; its exact size was
  257161 bytes. The shorter description
  `bootstrap/gravity/src/gravity/reader.gravity` is not the repository path and
  is retained only as an informal report label, not as a provenance claim.
- Observed exception data: `:id "C16-KEY"`, `:stage :c2-reader`,
  `:schema-version 1`, and `:maximum-nodes 524288`.
- The encoder increments its node counter before applying the strict greater-
  than bound check. Therefore the first rejected visit was 524289. That number
  is an inference from the recorded exception and the bound-check source, not a
  complete node census. The complete node count is unknown.

The failure occurred while the authentic public reader attempted to accept its
own Gravity source. The observation does not establish that any proposed
withholding behavior is correct.

## File bindings available after the run

The following SHA-256 and byte counts were computed from the named files at the
recorded base commit. They bind the immutable base inputs that can still be
recovered; they do not retroactively prove that every dirty worktree byte was
identical at run time.

| Path | Bytes | SHA-256 |
| --- | ---: | --- |
| `bootstrap/gravity/src/gravity/bootstrap/reader.gravity` | 257161 | `bd262c746ad88d9213e4b3160943fecde948c5ceee12a65dfe67ffb29d4e5b77` |
| `bootstrap/clojure/test/gravity/self_hosting/sh03_reader_adapter_test.clj` | 58043 | `32a0f41bbe214ca4b63f3c8e723b9648b896c425cb5fbe7639deb2d40a76e1e0` |
| `bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00808-sh03-reader-expected-source-byte-count.clj` | 8071 | `c18029d5ae41415b0f6c1c377640d5e6ee52d2c42d3b9cbac02115807a6a0d8c` |
| `bootstrap/clojure/src/gravity/c2_pass_cache/canonical_encode.clj` | 3411 | `19e9ce058a5c7770e0124404c6a5887cb958c07ded11a78ce5c51096fed8bd32` |
| `bootstrap/clojure/src/gravity/c2_pass_cache/foundation.clj` | 7711 | `b72f1a1e59f676733faffc1de8a57ab8b61328451b5a46eaaeeb165676de2f89` |

The relevant bound-check implementation is
`bootstrap/clojure/src/gravity/c2_pass_cache/canonical_encode.clj`: it performs
`swap!`/increment, then rejects when the resulting count is greater than
`maximum-canonical-nodes`. The bound is 524288 in
`bootstrap/clojure/src/gravity/c2_pass_cache/foundation.clj`. The SH03 binding
file above names the exact source path used by the failing test.

## Reproduction and promotion requirement

The historical command is:

```text
clojure -M:test --namespace gravity.self-hosting.sh03-reader-adapter-test --fail-fast
```

Stronger evidence requires a fresh rerun in a clean named-branch worktree that
binds the exact commit and tree, the SH03 pin, all changed-file digests, the
command and environment, complete stdout/stderr, exit code, test/assertion/error
counts, exception data, source byte count, and an independently measured
complete canonical-node census. Until that rerun exists, the complete count and
the exact dirty-worktree identity remain unknown and must not be invented.

## Nonclaims

This record neither accepts the architecture nor authorizes source changes. It
does not prove that the failure was the only SH03 blocker, that 524289 was the
complete traversal count, that a cache capacity fallback is safe, or that any
SH03, SH07, C2, C16, proof, equivalence, release, or self-hosting gate passed.
