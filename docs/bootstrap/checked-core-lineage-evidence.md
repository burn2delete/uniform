# Checked-core lineage and fragment records

This increment moves the existing C6 lineage, projection binding, fragment
selection, binding order, coverage and module assembly record helpers into
`gravity.checked-core`. D1, D2, D3, D9, L2, C1, C3-C6, C15, B13, BOOT8 and TEST2
govern the boundary. The B13-derived records are Core request data, not complete
backend artifact manifests or release evidence.

Inputs are existing verified C5 semantic records, projected form trees,
bindings, resolutions, aliases and explicit seed operations. Outputs are
source/semantic lineage identities, projection-binding inputs, ordered fragment
records, coverage summaries and module assembly records. Exact source identity,
semantic projection identity, generated traces, reference order and declared
alias requirements remain intact. An undeclared fragment alias must retain its
existing C6-VERIFY diagnostic.

Bootstrap retains owner verification, admission, authenticated request assembly,
publication, proof transactions and migration checks. The lineage implementation
helper stays private. All public APIs declare admission:none, and late
construction/receipt qualification includes their stage and compatibility roots.
No input quotas, work accounting or replacement migration pins are introduced.

Acceptance requires actual paired source requests and lineage/fragment outputs,
real undeclared-alias rejection, complete before/after equality, deterministic
identity/provenance checks, proportionate regressions, independent adversarial
acceptance, current CI and final exact clean integration preflight. A named
namespace or wrapper alone grants no completion credit. This early draft grants
no acceptance and no full Core, backend, release, performance, self-hosting or
seed-retirement support.
