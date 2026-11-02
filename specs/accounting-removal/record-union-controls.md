# Original Record and Union controls

Owner: ACC-01. This test-only draft restores two complete controls from PR502
head `6a4f87f013d0076b81aba9e0667650e70d28e635` onto main base
`e047157d711652a5624012f0da7d6911ce086718`, tree
`2b2168fe83fc08a7b4a882d75650c5b6e6eac31d`.

## Scope and requirements

The original `record-union-descriptor-rows-have-no-count-quota` and
`union-branch-definitions-have-no-count-quota` bodies and comments are copied
byte for byte. Every preexisting test byte and all production bytes remain
unchanged. The controls exercise actual C7 helpers through `invoke-c7`; no
Clojure implementation substitutes for those helpers.

The descriptor control uses 65 fields, variants and match branches. It preserves
late duplicate, invalid identifier, extra field and nonvector negatives, plus
family diagnostics for types, field values, access, payloads and match coverage.
The Core control preserves all 65 branch names and the last branch result,
invalid name and match shapes, missing and duplicate definitions, and malformed
last-branch nodes. These are normalized descriptor/Core DATA controls, not
claims of authenticated source admission or executable Record/Union support.

## Dependencies and artifacts

`record-union-controls-census.edn` pins the base, historical source, raw copied
spans, complete forms, test files and unchanged C7 file. Its syntactic dependency
closure contains 31 C7 definitions. This is source correspondence, not runtime
reachability or proof of behavior. The local Clojure checker reads with reader
evaluation disabled and never loads Bootstrap.

Six dependency forms differ from history: the four row validators perform
uniqueness checks after their field validation loops on current main;
`sh08-ru-supported-type?` has an explicit keyword guard; and
`sh08-ru-value-type` differs in nil comparison order and Boolean classification.
The census retains both form hashes. No historical helper equivalence is claimed.
The existing shape checks contain no row-count quota; this draft changes no
production behavior and does not resolve other accounting fields.

## Validation and acceptance

- Source integrity: PASS, two exact original controls, prior bytes preserved,
  production unchanged, 31 dependencies pinned.
- Language boundary: `clojure -M:test --namespace
  gravity.self-hosting.sh01-language-boundary-test`, PASS: 6 tests, 11 assertions.
- clj-kondo 2025.10.23 with repository config and cache disabled: zero errors,
  71 warnings exactly matching the unchanged base finding multiset.
- Structural documentation and full-language roadmap validators: PASS.
- Whitespace check: PASS.

The first lint baseline used a temporary filename inconsistent with its
namespace and produced a filename-only error. Repeating against a matching
namespace path removed that baseline artifact; the full remaining finding
multiset matches. No compiler or runtime command was run.

Genuine execution of both kernels is UNRUN. Independent acceptance and fresh
composed integration remain pending; the coordinator alone routes those steps.
This draft confers no whole Record/Union milestone, accounting-removal, release,
self-hosting or PR502 closure credit.
