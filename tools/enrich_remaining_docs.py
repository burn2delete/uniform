#!/usr/bin/env python3
"""Add focused Gravity contracts to phase ranges not completed by workers."""

from __future__ import annotations

import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / "docs"
TARGET_PHASES = {0, 1, 2, 12, 13, 14, 15, 16, 17}


def subject(title: str) -> str:
    result = title
    for suffix in [
        " Specification",
        " Design",
        " Strategy",
        " Plan",
        " Model",
        " Policy",
        " Charter",
        " Overview",
        " Semantics",
        " Architecture",
        " Process",
    ]:
        result = result.replace(suffix, "")
    return result


def prefix(doc_id: str) -> str:
    return "".join(ch for ch in doc_id if ch.isalpha())


def base_header(doc_id: str, title: str) -> str:
    return f"## Gravity Implementation Contract\n\nThe `{doc_id}` contract makes `{title}` operational rather than aspirational. It is written so a compiler, package tool, runtime, governance process, or conformance suite can consume it without inferring hidden policy.\n"


def foundation(doc_id: str, title: str) -> str:
    focus = subject(title)
    return base_header(doc_id, title) + f"""
Binding decisions for {focus}:

- Gravity is one language with many legal subsets. A document may narrow the language for a profile, but it MUST NOT define a second language.
- A design claim is accepted only when it names the source surface, core semantic rule, profile rule, compiler artifact, and conformance evidence it depends on.
- The default answer to hidden runtime services is rejection. Hosted convenience is legal only when the active profile and artifact metadata say which host service owns it.
- Extensibility is allowed at typed boundaries: syntax objects, macro contracts, compiler pass APIs, capability providers, memory providers, backends, and artifact schemas.
- Replacement of another language is credible only when Gravity can express the domain example, compile it under the right profile, emit an inspectable artifact, and run or verify its conformance case.

Acceptance evidence:

- A reader can point from this document to the specific language, safety, profile, compiler, package, and test documents that enforce it.
- Any proposed exception names the profile where it is legal and the artifact where it is recorded.
- The design does not require ambient authority, implicit allocation, implicit dynamic eval, or implicit undefined behavior.
"""


def language(doc_id: str, title: str) -> str:
    focus = subject(title)
    return base_header(doc_id, title) + f"""
Normative language rules for {focus}:

- Surface forms are read as syntax objects with source spans, namespace context, metadata, and expansion provenance.
- Macro expansion produces ordinary Gravity forms that must pass the caller profile's type, effect, capability, memory, and safety checks.
- Core lowering preserves enough information to explain diagnostics after optimization and target lowering.
- Dynamic behavior is a hosted-profile feature, not a universal language assumption. Lower-level profiles must statically reject dynamic eval, reflection, unbounded allocation, and host-dependent dispatch unless a narrower document explicitly allows a checked boundary.
- The type system and effect system cooperate: a well-typed expression is not legal until its effects are legal for the active profile, package manifest, and deployment grant.

Required examples:

```clojure
(ns example.{doc_id.lower()}
  (:profile :native)
  (:effects #{{:memory/region}}))

(defn checked-example [x :- I64]
  :- I64
  (if (> x 0) x 0))
```

Conformance evidence:

- Positive and negative fixtures for the feature in `:core`, `:hosted`, `:native`, and one constrained profile.
- Macro-generated and handwritten forms produce equivalent legality results.
- Diagnostics include source span, generated-origin chain, active profile, inferred type, inferred effect set, and missing capability when applicable.
"""


def safety(doc_id: str, title: str) -> str:
    focus = subject(title)
    return base_header(doc_id, title) + f"""
Safe Gravity rule for {focus}:

- Safe code has no undefined behavior. An operation is statically proven safe, runtime checked, rejected, or moved into an explicit unsafe island.
- Unsafe islands are not exemptions from documentation. They emit audit artifacts that name the unsafe operation, source span, owning package, review policy, proof or test evidence, and safe API boundary.
- Safety is profile-aware: hosted code may rely on managed runtime checks, native code may rely on ownership and regions, kernel/firmware code must avoid hidden allocation and GC, and AI/distributed code must record nondeterminism and tool authority.
- Generated code, macro output, optimized MIR, backend artifacts, and package dependencies must preserve the same safety contract as source code.

Safety artifact shape:

```clojure
{{:document "{doc_id}"
 :safe-surface "{focus}"
 :checks [:type :effect :ownership :capability :bounds :taint]
 :unsafe-islands []
 :proofs [:static-analysis :conformance-fixtures]}}
```

Conformance evidence:

- Negative fixtures for use-after-free, double-free, uninitialized read, data race, unchecked narrowing, capability bypass, tainted sink, and unsafe macro expansion where applicable.
- Runtime checks are present only when static proof is unavailable.
- Optimizations that remove checks carry proof references in the emitted artifact.
"""


def package(doc_id: str, title: str) -> str:
    focus = subject(title)
    return base_header(doc_id, title) + f"""
Package-system rules for {focus}:

- Project metadata is part of compilation, not an external build-script convention.
- Profiles, targets, capabilities, permissions, dependencies, artifact kinds, safety policies, and provenance requirements are declared before source analysis completes.
- Lockfiles and artifact manifests must be reproducible across machines for the same inputs, seed compiler, dependency graph, target matrix, and policy set.
- Package resolution cannot grant capabilities. It can only select dependencies whose declared needs fit the package policy and deployment grant.
- Artifact signing, SBOM, safety audit metadata, and build provenance are emitted as first-class Gravity artifacts.

Required manifest facts:

```clojure
{{:document "{doc_id}"
 :package-contract "{focus}"
 :profiles [:core :hosted :native]
 :targets [:jvm :wasm :llvm]
 :capabilities [:filesystem/read]
 :artifacts [:library :schema :docs]
 :reproducible true
 :safety-policy :deny-unreviewed-unsafe}}
```

Conformance evidence:

- Resolution is deterministic with and without network access when the lockfile is complete.
- A dependency that requests an undeclared capability is rejected before build execution.
- Emitted artifacts include source hash, compiler identity, profile, target, dependency graph, capability set, safety summary, and signature status.
"""


def tooling(doc_id: str, title: str) -> str:
    focus = subject(title)
    return base_header(doc_id, title) + f"""
Developer-experience rules for {focus}:

- Tools report compiler truth. They MUST NOT guess around profile errors, missing capabilities, unsafe islands, or unresolved artifacts.
- Every interactive command has a scriptable equivalent and a machine-readable output mode for CI.
- Tool output includes active profile, target, package, source span, diagnostic code, and artifact path when those facts exist.
- REPL and dev-server workflows run inside an explicit profile and capability grant; they cannot silently widen authority for convenience.
- AI-assisted tooling proposes changes through normal artifacts and conformance gates rather than bypassing compiler policy.

Required command behavior:

```bash
gravity {doc_id.lower()} --profile native --format json
gravity check --explain-effects --explain-capabilities
```

Conformance evidence:

- Golden JSON fixtures for diagnostics and successful reports.
- Terminal output is stable enough for docs while JSON remains stable enough for automation.
- Tooling fixtures include generated-code diagnostics, profile violations, capability denials, and unsafe audit summaries.
"""


def testing(doc_id: str, title: str) -> str:
    focus = subject(title)
    return base_header(doc_id, title) + f"""
Testing rules for {focus}:

- Every design promise must become a deterministic check: unit test, conformance fixture, property test, fuzz target, differential test, benchmark gate, proof artifact, or replay record.
- Positive tests prove accepted behavior; negative tests prove illegal behavior is rejected with useful diagnostics.
- Profile-specific tests must run with explicit profile, target, capability, memory provider, runtime provider, and safety policy inputs.
- AI and distributed tests must record nondeterminism, model/tool inputs, replay data, and approval decisions.
- Performance tests define both measurement method and semantic contract. A faster result that violates safety or profile legality fails.

Evidence bundle shape:

```clojure
{{:document "{doc_id}"
 :test-area "{focus}"
 :profiles [:core :native :hosted]
 :cases [:positive :negative :generated :artifact]
 :evidence [:logs :artifacts :diagnostics :proofs]}}
```

Conformance evidence:

- Fixtures can run in CI without ambient credentials unless the test explicitly declares a live capability.
- Test artifacts are content-addressed or otherwise traceable to source, compiler, dependency graph, and target.
- Failures identify the violated rule and the smallest reproducible fixture.
"""


def bootstrap(doc_id: str, title: str) -> str:
    focus = subject(title)
    return base_header(doc_id, title) + f"""
Bootstrap rules for {focus}:

- The seed compiler is a temporary trusted component, not the long-term definition of Gravity.
- Each stage records source hash, compiler hash, generated artifact hash, profile set, backend set, and conformance evidence.
- A self-hosted compiler artifact is accepted only when it reproduces the expected outputs or explains intentional deltas through reviewed compatibility records.
- Bootstrap stages shrink the trusted computing base by moving reader, macroexpander, analyzer, MIR, passes, build rules, package logic, and standard library code into Gravity.
- Trusting-trust defenses require independent rebuilds, provenance records, and equivalence tests between seed-built and self-built artifacts.

Stage record shape:

```clojure
{{:document "{doc_id}"
 :bootstrap-area "{focus}"
 :stage :self-hosting
 :inputs [:gravity-source :seed-compiler :lockfile]
 :outputs [:compiler-artifact :provenance :equivalence-report]
 :checks [:reproducible :semantic-equivalence :conformance]}}
```

Conformance evidence:

- The same source and lockfile produce identical bootstrap artifacts under the declared environment.
- Stage compatibility matrices identify which documents, profiles, and backends are supported at each stage.
- Provenance is sufficient for an auditor to reconstruct which compiler compiled the compiler.
"""


def stdlib(doc_id: str, title: str) -> str:
    focus = subject(title)
    return base_header(doc_id, title) + f"""
Standard-library rules for {focus}:

- Library APIs declare profile availability, effects, capabilities, allocation behavior, error model, safety policy, and artifact impact.
- Safe APIs may wrap unsafe internals only when the unsafe implementation is isolated, audited, tested, and prevented from leaking invalid states.
- `:core` libraries avoid host services; `:kernel` and `:firmware` libraries avoid hidden allocation and GC; `:hosted` libraries may delegate to host runtimes through explicit providers.
- Library functions expose enough semantic information for specialization, check elision, inlining, and backend lowering.
- Stability applies to behavior, diagnostics, artifacts, and profile availability, not just function names.

Library declaration shape:

```clojure
(ns gravity.std.{doc_id.lower()}
  (:profiles #{{:core :native :hosted}})
  (:effects #{{}})
  (:capabilities [])
  (:safety :safe)
  (:stability :draft))
```

Conformance evidence:

- API fixtures for every supported profile.
- Negative fixtures for unsupported profiles and missing capabilities.
- Documentation examples compile under the profiles they claim.
- Unsafe internals have audit artifacts and safe-wrapper tests.
"""


def governance(doc_id: str, title: str) -> str:
    focus = subject(title)
    return base_header(doc_id, title) + f"""
Governance rules for {focus}:

- Stable contracts change through a recorded proposal, compatibility analysis, security review, conformance update, and migration plan.
- Experimental features must name their owning document, profile availability, stabilization criteria, and removal path.
- Unsafe-code, target-support, package-policy, and security changes require stricter review than ordinary library additions.
- A proposal that affects safe-code guarantees, profile legality, emitted artifacts, self-hosting trust, or package provenance cannot be accepted without updated tests or proof artifacts.
- Deprecation preserves diagnostics and migration tools for a defined compatibility window.

Review record shape:

```clojure
{{:document "{doc_id}"
 :governance-area "{focus}"
 :state :review
 :required-evidence [:spec-diff :tests :compatibility :security :migration]
 :affected-contracts [:language :profile :safety :artifact]}}
```

Conformance evidence:

- RFC records link to changed documents, fixtures, artifacts, and compatibility notes.
- Stabilization requires successful conformance runs for every affected profile and target tier.
- Security and unsafe-code decisions remain auditable after release.
"""


RENDERERS = {
    "D": foundation,
    "L": language,
    "SAFE": safety,
    "PKG": package,
    "T": tooling,
    "TEST": testing,
    "BOOT": bootstrap,
    "STD": stdlib,
    "GOV": governance,
}


def insert_section(path: Path, section: str) -> bool:
    text = path.read_text(encoding="utf-8")
    if "## Gravity Implementation Contract" in text or "## Gravity-Specific Contract" in text:
        return False
    marker = "\n## Cross-Profile Behavior\n"
    if marker not in text:
        raise RuntimeError(f"missing insertion marker in {path}")
    text = text.replace(marker, "\n" + section.rstrip() + "\n" + marker, 1)
    path.write_text(text, encoding="utf-8")
    return True


def enrich_readme(path: Path, phase: int) -> bool:
    text = path.read_text(encoding="utf-8")
    if "## Phase Authoring Contract" in text:
        return False
    section = f"""
## Phase Authoring Contract

- Phase {phase} documents must cite upstream language, safety, profile, compiler, package, and conformance contracts instead of redefining them.
- Every document in this phase must name concrete artifacts, rejection rules, and evidence that make its contract testable.
- Profile-specific behavior must be explicit: allowed, checked, delegated, or rejected.
- Unsafe behavior must be isolated behind audit records and safe API boundaries.
- Tooling, package, test, bootstrap, standard-library, and governance documents must preserve the same effects/capabilities/artifact discipline as source code.
"""
    path.write_text(text.rstrip() + "\n" + section, encoding="utf-8")
    return True


def main() -> None:
    inventory = json.loads((DOCS / "document-inventory.json").read_text(encoding="utf-8"))
    changed = 0
    for entry in inventory:
        if entry["phase"] not in TARGET_PHASES:
            continue
        doc_id = entry["id"]
        renderer = RENDERERS[prefix(doc_id)]
        path = ROOT / entry["path"]
        if insert_section(path, renderer(doc_id, entry["title"])):
            changed += 1

    for phase in TARGET_PHASES:
        readmes = sorted(DOCS.glob(f"phase-{phase:02d}-*/README.md"))
        if len(readmes) != 1:
            raise RuntimeError(f"expected one README for phase {phase}, found {readmes}")
        if enrich_readme(readmes[0], phase):
            changed += 1

    print(f"enriched {changed} files in remaining phase ranges")


if __name__ == "__main__":
    main()
