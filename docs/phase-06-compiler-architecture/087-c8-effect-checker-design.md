# C8 - Effect Checker Design

Sequence: 87
Phase: 6 - Compiler Architecture
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The effect checker implements `L6` over typed core. It computes expression,
function, namespace, module, macro-output, build, replay, and residual effects;
validates them against declarations, profiles, capabilities, package/build
grants, runtime providers, and safety mode; and emits effect artifacts that MIR,
optimizers, runtimes, and tools must preserve.

Types say what values are. Effects say what actions occur. Capabilities say who
has authority to perform them.

## Requirements

- Every expression with behavior beyond pure value construction must carry
  effect facts or a diagnostic.
- Function artifacts must record latent effects and thrown-error effects.
- Inferred function and namespace effects must fit inside declared allowances.
- Effect legality is the intersection of source declaration, active profile,
  package manifest, build/deployment grants, runtime/provider support, and
  safety mode.
- Build effects must be separated from runtime effects.
- Nondeterministic, external, AI, workflow, and distributed effects must record
  replay or audit obligations.
- Effect ordering constraints must be emitted for operations that cannot be
  reordered.
- Missing capabilities must be reported before MIR construction.

## Dependencies

- `L6` defines the effect system.
- `L15` defines capability providers and grants.
- `L9` defines error and throw effects.
- `L12` defines build effects and hermeticity.
- `C7` provides typed core and function types.
- Profile documents define effect availability per profile.
- Runtime and package documents define providers, deployment grants, and
  manifest policy.

## Outputs and Artifacts

- Effect graph.
- Function latent-effect table.
- Namespace and module effect summaries.
- Capability proof record.
- Build-effect log.
- Replay-effect requirements.
- Effect ordering constraints.
- Residual effect report.
- Effect diagnostics.

## Effect Graph

```clojure
{:artifact :gravity/effect-graph
 :module module-id
 :nodes {core-node-id {:direct #{:network/http}
                       :latent #{}
                       :transitive #{:network/http}
                       :ordering :sequence
                       :source source-span}}
 :functions {fn-id {:declared #{:network/http}
                    :inferred #{:network/http}
                    :latent #{:network/http}
                    :throws #{HttpError}}}
 :namespace {:declared #{:network/http}
             :inferred #{:network/http}}
 :build-effects []
 :replay-required #{}
 :diagnostics []}
```

The graph preserves alternatives for branches where useful, but legality checks
use the conservative effect set required by `L6`.

## Legality Intersection

An effect is legal only when all applicable authorities accept it:

```clojure
{:effect :network/http
 :source core-node-id
 :allowed-by {:function true
              :namespace true
              :profile true
              :package true
              :deployment true
              :runtime true
              :safety true}
 :required-capabilities #{:http/client}
 :granted-capabilities #{:http/client}
 :result :accepted}
```

Backends and runtimes may narrow support. They cannot grant new authority that
was absent from source, package, or deployment policy.

## Capability Proof

Capability proof records connect effect use to grants:

```clojure
{:artifact :gravity/capability-proof
 :effect :filesystem/read
 :source core-node-id
 :capability :fs/read
 :grant {:scope ["config/*.edn"]
         :principal 'app.server
         :phase :runtime}
 :provider :gravity.runtime/filesystem
 :status :accepted}
```

The proof is not a safety proof by itself; it is authority evidence consumed by
profile validation, safety analysis, runtime selection, and audit tooling.

## Build and Runtime Effects

The checker maintains separate domains:

- build effects from reader extensions, macros, compile-time evaluation,
  compiler plugins, target probes, and code generators,
- runtime effects from the emitted program,
- package/build effects from dependency resolution and artifact generation.

A macro may have build effects while emitting pure runtime code, or pure build
behavior while emitting effectful runtime code. The two are recorded separately.

## Replay and Nondeterminism

Replay records are required for effects such as:

- time,
- randomness,
- external IO,
- database reads when external,
- workflow events,
- model calls,
- tool calls,
- AI human-review decisions (`:ai/human-review`),
- scheduler-sensitive operations.

The checker emits replay obligations that downstream workflow, AI, runtime, and
test documents must satisfy. A profile that forbids replay gaps rejects missing
records before target lowering.

## Ordering Constraints

Effect ordering records include:

- sequence,
- may commute,
- must not duplicate,
- must not eliminate,
- must bracket resource acquire/release,
- must preserve volatile/MMIO/atomic ordering,
- must preserve replay order.

Optimizers consume these records before reordering, fusing, or eliminating
operations.

## Residual Effects

Residual effects are effects that remain after static proof or specialization.
Examples include preserved runtime checks, dynamic dispatch, fallback provider
calls, residual capability checks, and replay logging. The checker records them
so MIR and runtime artifacts do not hide them.

## Diagnostics

Effect checker diagnostics use `C8` identifiers:

- `C8-UNDECLARED` for inferred effects outside declaration.
- `C8-PROFILE` for effects rejected by profile.
- `C8-CAPABILITY` for missing or insufficient capability grants.
- `C8-BUILD` for ungranted build effects.
- `C8-REPLAY` for replay-sensitive effects without replay obligations.
- `C8-ORDER` for missing effect ordering constraints.
- `C8-RUNTIME` for no legal runtime/provider support.
- `C8-UNKNOWN` for unregistered effect names.
- `C8-VERIFY` for malformed effect artifacts.

Diagnostics must include core node id, source span, generated-origin chain,
function, namespace, effect, capability, profile, target, provider, grant, and
remediation.

## Rejected Designs

Gravity rejects treating type checking as sufficient legality.

Gravity rejects effects inferred only by backend or runtime behavior.

Gravity rejects macros that hide runtime effects.

Gravity rejects capability grants inferred from effect names.

Gravity rejects replay-sensitive effects without replay or audit artifacts.

Gravity rejects optimizations that reorder effects without effect evidence.

## Conformance Criteria

A conforming effect checker must demonstrate:

- direct, latent, transitive, build, and residual effect computation,
- namespace and function declaration checks,
- profile-specific effect rejection,
- capability proof acceptance and rejection,
- build-effect hermetic rejection,
- replay obligation emission,
- ordering constraints for resource, volatile, MMIO, atomic, workflow, and AI
  cases,
- MIR effect annotation preservation,
- diagnostics for every legality failure class.
