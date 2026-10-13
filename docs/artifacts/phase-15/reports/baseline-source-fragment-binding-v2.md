# Baseline source fragment binding amendment V2

## Purpose

This maintenance amendment permits the scoped Clojure source extraction needed
to enforce the 250-line boundary. It extends the namespace-only source evidence
in the SH-07 V9 implementation without changing Gravity program behavior. It
governs source layout and receipt construction only.

## Dependencies and preserved contracts

D1, D9, C1, BOOT7, BOOT8, and PKG3 continue to govern semantics and provenance.
The accepted V9 architecture and its E3/E4 errata remain historical inputs. The
fixed ordered seven logical algorithm source paths, their Git-blob descriptors,
and their algorithm-source root are unchanged. Implementation fragments must
not masquerade as namespaces or become extra algorithm descriptor rows.

The implementation on main before this amendment remains unfinished. This
amendment neither completes that implementation nor transfers acceptance to it.

## Requirements

Source products follow only literal absolute Clojure `load` resources beneath
`bootstrap/clojure/src`. Read bytes from the already authenticated C/T source
authority. Each selected object must have mode `100644`, the exact expected
path, a valid blob OID, and matching byte count and raw SHA-256. Reject missing
objects, unsafe paths, cycles, and a namespace declaration inside a fragment.
Preserve source form order. Do not introduce live-checkout, classpath-resource,
or filesystem fallbacks into identity construction.

Checker semantic identities use the expanded forms. Facade artifact identities
continue to describe the facade blob. Dependency closures bind every fragment.
Bootstrap component sources remain selected through the authenticated load
order manifest and retain their actual enclosing `gravity.bootstrap` namespace.

Namespace source records keep their original fields. Add a distinct
`:source-fragments` vector to both SOURCE-CLOSURE and LOADED-NAMESPACES. Each
fragment record has exactly `:owner-namespace :logical-coordinate :relative-path
:blob-oid :byte-count :sha256`. The owner must match the enclosing root's actual
namespace. Coordinate, blob, length, and digest must come from the same C/T
authority as the root. Deduplicate by owner and path; sort by owner name and
path. Reconstruct records from authenticated objects rather than trusting a
supplied receipt. Include them in both receipt preimages.

The amended root domains are `:gravity/sh07-b51-source-closure-root-v2` and
`:gravity/sh07-b51-loaded-namespace-root-v2`. Register both domains and retain
the historical V1 domains. Do not relabel a V1 receipt as V2. The declared host
surface includes `require` and `load`; literal source loading adds no ambient
source authority.

The literal owned-C set includes the original owned files, their extracted
implementation files, the changed bootstrap load-order manifest, and this
amendment. PRE paths remain literal, ordered, unique, and source controlled.
With N owned-C files, the existing PRE construction has N+44 paths, N+49 Git
operations, and N+36 static UTF-8 decodes. Four-participant aggregate counters
are four times their respective PRE counters, plus the existing 20 Q-coordinate
Git operations. No acquisition, decode, or preservation row may be omitted.

The five existing pin-mask owners and their RHS/delimiter rules are unchanged.
Bind each coordinate to the actual component resource's authenticated load
ordinal and local form ordinal. The carrier slot moves into its extracted
component; the mask still covers exactly five slots in two physical files.
The corresponding P9 source allocation names those same two physical files.

## Outputs and conformance

The source products, checker identities, dependency manifests, namespace and
fragment receipts, updated PRE census, and scoped validation records are the
outputs. Parsed source forms must match main after reassembly, except for the
explicit evidence adaptations, corrected contract paths, and an equivalent
partition of the long row-plan constant.

Positive fixtures must cover ordered expansion, nested fragments, authentic
identity changes, and separate owner records. Negative fixtures must reject a
missing or changed object, unsafe resource, cycle, namespace substitution, and
owner substitution. Scope checks to the affected modules; no full suite is
required for this maintenance candidate. Preserve public successful execution
and rejection diagnostics.

This amendment grants no implementation correctness, I9 eligibility, aggregate
SH-07 completion, self-hosting, release, performance, hostile-host, or
seed-retirement authority. Such claims still require their governing evidence
bundles and independent acceptance.
