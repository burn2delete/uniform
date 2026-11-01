# Build metadata validation boundary

## Governing contracts and scope

D1/D2/D3/D8/D9, PKG1/PKG2/PKG3/PKG6/PKG7/PKG10/PKG11, C1/C15/C16,
BOOT8 and TEST2 govern this increment. PKG2 requires declared build effects;
PKG7 requires controlled network inputs. Existing native Stage0 metadata checks
enforce those two conditions before instruction-plan execution.

Move only `validate-stage0-compiled-build-graph!` and
`validate-stage0-compiled-reproducible-build!` from component00537 into
`gravity.build/check-graph-effects!` and `check-offline-recipe!`. Preserve the
existing wrappers, arities, set/subset algorithm, offline-network equality,
diagnostic identifiers, complete diagnostic payload and evaluation order.
An explicit late diagnostic Var port keeps diagnostic construction with its
existing owner. The stage may depend on clojure.set; it must not import
Bootstrap, CLI, Release, backend or host invocation namespaces.

## Evidence obligations

The initial scope is based on actual main
b363c32b5569e4ea76df852abd142be7c067292e. Before implementation, capture native
positive and negative records and actual complete-source compilation or
instruction-plan execution. After extraction, compare complete values and
serialization, source provenance, deterministic identities, both full component
and surrounding behavior. Exercise real undeclared-effect PKG2001 and
uncontrolled-network PKG7003 failures, including late diagnostic-root behavior.

Run proportionate focused and affected tests, language boundary, ownership
registration, docs/roadmap/governance validation, contextual lint and independent
adversarial review. Each final source tuple requires its own CI and clean
current-main integration preflight. This scope document is not implementation
or acceptance evidence.

## Unsupported boundaries

These are two checks over trusted native metadata, not complete PKG2/PKG7
validation. Existing fixture identities, complete/status/proof labels and
package metadata remain supplied compatibility data. They do not authenticate
source, grants, builders, lockfiles, signatures, SBOMs or reproducibility.
No package fetch, build action, shell, network access, cache reuse, release,
self-hosting, seed retirement or namespace-completion claim follows. Other
Build namespaces retain their current owners and authority boundaries.
