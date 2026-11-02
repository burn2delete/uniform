# Fixed compiler source binding after the Reader declaration change

This source/DATA correction follows the two reviewed `build-read-declared-input`
declaration additions at `5949ef468537fe319a62a4e5436dd7b6254e207f`.
D1, D3, D9, BOOT2, BOOT5, BOOT7, BOOT8 and
`docs/development-verification.md` govern its input identity and evidence scope.
It does not renew migration, historical admission or release evidence.

## Original failures and source cause

PR692 run `41190876966` failed four jobs at `P15S23Q002`: the seed builtin set
already included the guarded build-read operation, but the Gravity-authored
emitter and runtime declaration lists omitted it. The reviewed correction adds
that existing symbol once to each list; implementations and legality guards
are unchanged.

Changed-head run `41194873709` tested merge
`8e96baaad58fe3d46bd89e17c145c5dde1771625`, tree
`61d03f0655620fcb79c8979823a3a463801eb1cc`. Bootstrap, Macro, Resolution and
checked-core-runtime each reached `P15S23Q001`, with failed child exits 1.
The original logs name `stage2-compiler-source-byte-count`: expected 128447,
observed 128499. Both failed runs remain separate evidence, without retries.

`c-backend-stage2-plan-emitter-source-rule!` authenticates the physical compiler
source through `p15-s23-stage2-compiler-pinned-source!` before parsing it. The
fixed compiler byte/hash literals in component 00645 identify the source before
the reviewed declaration additions. Their originating revision `d3bb1b753`
also authored that original compiler source. This is a concrete source identity
check, not an input allowance or a compiler work quota.

## Authored revision equation

The original compiler blob at `39b89ce8` has 128447 bytes and SHA256
`be4ba46088d413322ba5cf86156bb22bb79aeb7bec1fcf3899d8b081019ef854`.
Replace exactly the two declaration suffixes
`hash-map list pr-str rest second str vector]` with the same suffix followed
by `build-read-declared-input` before the closing bracket. Each insertion adds
26 ASCII bytes. The resulting source is byte-identical to the reviewed compiler
at `5949ef46`, its publication composition and this correction's parent.
Its 128499 bytes independently hash to
`57c13e44b46726a136ecd237dde5e85d86cd55b82bd8ac6033afce2fb87bf527`.
The regression reverses those exact insertions, authenticates the entire old
preimage against its fixed hash/count, and checks the forward equation.
Candidate-supplied hashes or CI output do not choose the expected binding.

Component 00645 now names that fixed authored seed revision. The complete
authentication function is unchanged: regular-file and no-symlink checks,
exact size, exact full-byte hash, stable file identity/time/size and strict UTF8
decoding still precede parsing. There is no runtime-selected pin, fallback,
guard suppression or change to Reader effects, capabilities, scope or arity.
Existing production-intake tests name this new seed input; their source edits
are not observations of an actual H6 run.

## Controls and remaining evidence

The isolated Reader source suite passes 4 tests / 43 assertions. It evaluates
only the selected host byte-authentication definition with temporary DATA files
and the real SHA256 primitive. It does not load Bootstrap, parse Gravity target
source, compile a plan, invoke a provider or execute a historical admission.
Current exact bytes pass. Old source, truncated/extended source, same-length
tampering, unrelated same-length bytes and a missing file retain `P15S23Q001`
with the appropriate byte-count or hash fact. Advancing the byte count while
retaining the old hash still refuses. The complete builtin population
and fixed handler model controls remain in the same suite.

Historical plan bindings in `gravity.c6-c10.source-contract`, immutable evidence
reports, runtime artifact pins and migration records are unchanged. Their old
source identities do not become valid evidence for the new compiler revision;
later consumers must reconstruct current development artifacts or reject stale
identities. BOOT7/BOOT8 compatibility, equivalence, provenance and release
obligations remain separate. This candidate requires independent affected
source review before any push; configured changed-head CI is not yet run.
