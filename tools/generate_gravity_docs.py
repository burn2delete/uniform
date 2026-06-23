#!/usr/bin/env python3
"""Generate the Gravity design document set from the PDF-derived sequence."""

from __future__ import annotations

import json
import re
from dataclasses import dataclass
from pathlib import Path
from textwrap import dedent


ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / "docs"


PHASES: dict[int, tuple[str, str]] = {
    0: (
        "Foundation and Thesis",
        "Defines what Gravity is, why it exists, and which constraints keep the project coherent.",
    ),
    1: (
        "Core Language",
        "Defines the semantic foundation shared by every profile, backend, runtime, and tool.",
    ),
    2: (
        "Safety",
        "Defines the rules that make safe Gravity code memory-safe, race-safe, auditable, and capability-governed.",
    ),
    3: (
        "Profile System",
        "Defines the feature contracts that let one language compile across incompatible runtime environments.",
    ),
    4: (
        "Performance Model",
        "Defines how Gravity preserves maximum performance without making unsafe behavior implicit.",
    ),
    5: (
        "Mathematical and Elementary Function System",
        "Defines numeric semantics, EFIR, EML normalization, certified approximations, and proof artifacts.",
    ),
    6: (
        "Compiler Architecture",
        "Defines the implementation spine from reader through diagnostics, optimization, lowering, and pass APIs.",
    ),
    7: (
        "Backend Architecture",
        "Defines concrete target emitters and the backend contract for typed artifacts.",
    ),
    8: (
        "Runtime Architecture",
        "Defines multiple runtimes rather than one universal runtime.",
    ),
    9: (
        "Domain-Specific Computing Coverage",
        "Validates that Gravity can replace specialized languages across fields of computing.",
    ),
    10: (
        "Schema, Data and Interop",
        "Defines schemas, encodings, migrations, typed outputs, and cross-language bindings.",
    ),
    11: (
        "AI and Agentic Programming",
        "Defines model, tool, agent, memory, policy, evaluation, and approval contracts.",
    ),
    12: (
        "Build, Package and Artifact System",
        "Defines project files, builds, packages, artifacts, provenance, and target matrices.",
    ),
    13: (
        "Tooling and Developer Experience",
        "Defines the command line, REPL, editor, debugger, registry, inspector, profiler, and AI-assisted tools.",
    ),
    14: (
        "Testing, Verification and Conformance",
        "Defines how the language, compiler, runtime, profiles, backends, and libraries are mechanically tested.",
    ),
    15: (
        "Bootstrap and Self-Hosting",
        "Defines how Gravity moves from a seed compiler to a mostly self-hosted compiler.",
    ),
    16: (
        "Standard Library",
        "Defines the stable libraries exposed across profiles and runtimes.",
    ),
    17: (
        "Governance and Evolution",
        "Defines how the language, standard library, targets, unsafe code, and ecosystem change over time.",
    ),
}


RAW_INVENTORY = """
001|0|D0|Gravity Vision & Design Thesis
002|0|D1|System Architecture Overview
003|0|D2|Implementation Roadmap & Milestones
004|0|D3|Terminology & Concept Model
005|0|D4|Universal Computing Coverage Charter
006|0|D5|Language Replacement Strategy
007|0|D6|Performance Philosophy & Charter
008|0|D7|Extensibility Philosophy
009|0|D8|Safety Philosophy & Charter
010|0|D9|Verifiability & Mathematical Correctness Charter
011|1|L1|Surface Syntax Specification
012|1|L2|Core Language Semantics
013|1|L3|Namespace & Module System Specification
014|1|L4|Macro System Specification
015|1|L5|Type System Specification
016|1|L6|Effect System Specification
017|1|L7|Pattern Matching Specification
018|1|L8|Protocols, Interfaces & Dispatch Specification
019|1|L9|Error Handling Specification
020|1|L10|Memory Model Specification
021|1|L11|Concurrency Model Specification
022|1|L12|Compile-Time Evaluation Specification
023|1|L13|Standard Library Design Principles
024|1|L14|Language Facet System Specification
025|1|L15|Capability Provider Specification
026|1|L16|Alternative Macro System Contract
027|1|L17|Alternative Type System Contract
028|1|L18|Alternative Memory Model Contract
029|1|L19|Language Interoperability & Migration Specification
030|2|SAFE1|Safe Gravity Semantics
031|2|SAFE2|Memory Safety Model
032|2|SAFE3|Ownership, Borrowing & Lifetimes
033|2|SAFE4|Region and Arena Safety
034|2|SAFE5|Linear Resource Safety
035|2|SAFE6|Unsafe Code and Audit Model
036|2|SAFE7|FFI Safety
037|2|SAFE8|Concurrency and Data-Race Safety
038|2|SAFE9|Numeric Safety
039|2|SAFE10|Capability Security Model
040|2|SAFE11|Taint Tracking and Input Safety
041|2|SAFE12|Macro Safety
042|2|SAFE13|AI Tool Safety
043|2|SAFE14|Supply-Chain Safety
044|2|SAFE15|Safety Proof and Certificate Model
045|2|SAFE16|Safety Conformance Test Plan
046|3|P1|Profile System Specification
047|3|P2|:core Profile Specification
048|3|P3|:meta Profile Specification
049|3|P4|:hosted Profile Specification
050|3|P5|:native Profile Specification
051|3|P6|:firmware Profile Specification
052|3|P7|:kernel Profile Specification
053|3|P8|:hardware Profile Specification
054|3|P9|:distributed Profile Specification
055|3|P10|:ai Profile Specification
056|3|P11|:gpu / Accelerator Profile Specification
057|3|P12|:formal Verification Profile Specification
058|3|P13|Profile Compatibility Matrix
059|4|PERF1|Performance Model Specification
060|4|PERF2|Zero-Cost Abstractions Specification
061|4|PERF3|Specialization & Partial Evaluation Design
062|4|PERF4|Memory Layout Optimization Design
063|4|PERF5|Benchmark Suite & Performance Governance
064|4|PERF6|Profile-Guided Optimization Design
065|4|PERF7|Autotuning & Multiversioning Design
066|4|PERF8|SIMD, Vectorization & Cache Optimization Strategy
067|4|PERF9|Realtime and Deterministic-Latency Performance Model
068|4|PERF10|Performance/Safety Check Elision Rules
069|5|MATH1|Numeric Tower Specification
070|5|MATH2|Elementary Function System Specification
071|5|MATH3|Elementary Function IR - EFIR Specification
072|5|MATH4|EML Normalization & Search Design
073|5|MATH5|Certified Approximation Specification
074|5|MATH6|Interval Arithmetic & Real Proof Engine
075|5|MATH7|Numeric Modes & Precision Contracts
076|5|MATH8|Floating-Point Semantics Specification
077|5|MATH9|Symbolic Math and Rewrite System Specification
078|5|MATH10|Elementary Function Optimization Strategy
079|5|MATH11|Math Verification and Conformance Test Plan
080|6|C1|Compiler Architecture Overview
081|6|C2|Reader Implementation Design
082|6|C3|Syntax Object Model
083|6|C4|Macro Expansion Engine Design
084|6|C5|Name Resolution & Namespace Analyzer Design
085|6|C6|AST and Core Lowering Design
086|6|C7|Type Checker Design
087|6|C8|Effect Checker Design
088|6|C9|Ownership, Lifetime and Region Checker Design
089|6|C10|Safety Analysis Pipeline Design
090|6|C11|Gravity MIR Specification
091|6|C12|Domain IR Architecture
092|6|C13|MIR Optimization Passes Design
093|6|C14|Target Lowering Architecture
094|6|C15|Compiler Diagnostics Specification
095|6|C16|Incremental Compilation Design
096|6|C17|Compiler Plugin and Pass API Specification
097|6|C18|Compiler Verification and Pass-Correctness Strategy
098|7|B1|Backend Interface Specification
099|7|B2|C Backend Design
100|7|B3|LLVM Backend Design
101|7|B4|Wasm Backend Design
102|7|B5|JVM Backend Design
103|7|B6|JavaScript / TypeScript Backend Design
104|7|B7|MLIR Backend Design
105|7|B8|GPU Backend Design
106|7|B9|HDL Backend Design
107|7|B10|Workflow Graph Backend Design
108|7|B11|Query / Relational Backend Design
109|7|B12|Mobile Backend Design
110|7|B13|Artifact Emission Specification
111|7|B14|Backend Conformance Test Plan
112|8|R1|Runtime Architecture Overview
113|8|R2|No-Runtime Execution Model
114|8|R3|Minimal Native Runtime Design
115|8|R4|Managed Runtime Design
116|8|R5|Memory Runtime Design
117|8|R6|Concurrency Runtime Design
118|8|R7|Distributed Runtime Design
119|8|R8|AI Runtime Design
120|8|R9|REPL and Interactive Runtime Design
121|8|R10|FFI Runtime Design
122|8|R11|Runtime Capability Enforcement Design
123|8|R12|Runtime Observability and Diagnostics Design
124|9|DOM1|Hardware Computing Domain Specification
125|9|DOM2|Firmware and Embedded Domain Specification
126|9|DOM3|Operating System and Kernel Domain Specification
127|9|DOM4|Drivers and Device Interaction Domain Specification
128|9|DOM5|High-Performance Native Computing Domain Specification
129|9|DOM6|Web Frontend and UI Domain Specification
130|9|DOM7|Mobile Application Domain Specification
131|9|DOM8|Backend Services Domain Specification
132|9|DOM9|Distributed Systems Domain Specification
133|9|DOM10|Database and Storage Engine Domain Specification
134|9|DOM11|Data, Query and Analytics Domain Specification
135|9|DOM12|Scientific and Numeric Computing Domain Specification
136|9|DOM13|GPU and Accelerator Computing Domain Specification
137|9|DOM14|Game Engine and Simulation Domain Specification
138|9|DOM15|Security and Cryptography Domain Specification
139|9|DOM16|Blockchain and Smart Contract Domain Specification
140|9|DOM17|Compiler and Language Tooling Domain Specification
141|9|DOM18|AI and Agentic Computing Domain Specification
142|9|DOM19|Formal Verification Domain Specification
143|9|DOM20|Scripting, Shell and Automation Domain Specification
144|9|DOM21|Low-Code, Visual Programming and Workflow Domain Specification
145|10|S1|Schema System Specification
146|10|S2|Serialization Specification
147|10|S3|Canonical Data Format Specification
148|10|S4|GraphQL Generation Design
149|10|S5|OpenAPI Generation Design
150|10|S6|Database Mapping and Migration Design
151|10|S7|Binary Encoding and ABI Schema Specification
152|10|S8|Typed Configuration and Environment Specification
153|10|S9|Artifact Schema Specification
154|11|A1|AI Programming Model Specification
155|11|A2|Model Provider Specification
156|11|A3|Prompt and Structured Output Specification
157|11|A4|Tool Definition Specification
158|11|A5|Agent Definition Specification
159|11|A6|Agent Workflow Specification
160|11|A7|Memory and Retrieval Specification
161|11|A8|AI Policy and Safety Model
162|11|A9|AI Evaluation Framework Design
163|11|A10|Human-in-the-Loop and Approval Workflow Specification
164|11|A11|Prompt Injection and Tool Misuse Defense Specification
165|12|PKG1|Project File Specification
166|12|PKG2|Build System Architecture
167|12|PKG3|Artifact Model Specification
168|12|PKG4|Package Manager Specification
169|12|PKG5|Dependency Resolution Specification
170|12|PKG6|Capability and Permission Manifest Specification
171|12|PKG7|Reproducible Build Specification
172|12|PKG8|Package Safety and Audit Metadata Specification
173|12|PKG9|Private Registry and Latent Package Space Design
174|12|PKG10|Supply-Chain Security and Provenance Specification
175|12|PKG11|Cross-Compilation and Target Matrix Specification
176|12|PKG12|Artifact Signing, Verification and SBOM Specification
177|13|T1|CLI Specification
178|13|T2|REPL UX Specification
179|13|T3|Formatter Specification
180|13|T4|Linter Specification
181|13|T5|Language Server Protocol Design
182|13|T6|Debugger Design
183|13|T7|Documentation Generator Design
184|13|T8|Dev Server Design
185|13|T9|Package Registry UX Specification
186|13|T10|Compiler Explorer and IR Inspector Design
187|13|T11|Profiler and Performance Inspector Design
188|13|T12|Safety Audit Explorer Design
189|13|T13|AI-Assisted Development Tooling Specification
190|14|TEST1|Language Conformance Test Plan
191|14|TEST2|Compiler Test Strategy
192|14|TEST3|Runtime Test Strategy
193|14|TEST4|Profile Compliance Test Plan
194|14|TEST5|Safety Conformance Test Plan
195|14|TEST6|Backend Conformance Test Plan
196|14|TEST7|Standard Library Test Strategy
197|14|TEST8|AI and Workflow Evaluation Strategy
198|14|TEST9|Fuzzing and Property Testing Plan
199|14|TEST10|Differential Testing Strategy
200|14|TEST11|Formal Semantics and Verification Plan
201|14|TEST12|Performance Regression Test Plan
202|14|TEST13|Self-Hosting Validation Plan
203|15|BOOT1|Bootstrap Strategy
204|15|BOOT2|Seed Compiler Design
205|15|BOOT3|Self-Hosted Compiler Plan
206|15|BOOT4|Compiler-in-Gravity Coding Standard
207|15|BOOT5|Stage Compatibility Matrix
208|15|BOOT6|Trusting Trust and Reproducible Bootstrap Plan
209|15|BOOT7|Self-Hosting Validation and Equivalence Plan
210|15|BOOT8|Bootstrap Artifact Provenance Specification
211|16|STD1|Standard Library Architecture
212|16|STD2|Core Library Specification
213|16|STD3|Collections Library Specification
214|16|STD4|String and Text Library Specification
215|16|STD5|Numeric and Math Library Specification
216|16|STD6|Memory and Resource Library Specification
217|16|STD7|Concurrency Library Specification
218|16|STD8|IO and Filesystem Library Specification
219|16|STD9|Network and HTTP Library Specification
220|16|STD10|Serialization and Schema Library Specification
221|16|STD11|Database and Query Library Specification
222|16|STD12|Workflow Library Specification
223|16|STD13|AI and Agent Library Specification
224|16|STD14|Testing Library Specification
225|16|STD15|Compiler Meta-Programming Library Specification
226|16|STD16|Platform and OS Library Specification
227|16|STD17|Hardware and Firmware Library Specification
228|16|STD18|Cryptography Library Specification
229|16|STD19|UI and Application Library Specification
230|16|STD20|Standard Library Stability Policy
231|17|GOV1|Language Evolution Process
232|17|GOV2|Compatibility Policy
233|17|GOV3|Standard Library Governance
234|17|GOV4|Security Review Process
235|17|GOV5|Target Support Policy
236|17|GOV6|RFC Process
237|17|GOV7|Experimental Feature Policy
238|17|GOV8|Deprecation and Stabilization Policy
239|17|GOV9|Unsafe Code Governance Policy
240|17|GOV10|Ecosystem Package Governance Policy
""".strip()


@dataclass(frozen=True)
class Doc:
    seq: int
    phase: int
    doc_id: str
    title: str

    @property
    def phase_name(self) -> str:
        return PHASES[self.phase][0]

    @property
    def slug(self) -> str:
        clean = self.title.lower()
        clean = clean.replace(":core", "core")
        clean = clean.replace(":meta", "meta")
        clean = clean.replace(":hosted", "hosted")
        clean = clean.replace(":native", "native")
        clean = clean.replace(":firmware", "firmware")
        clean = clean.replace(":kernel", "kernel")
        clean = clean.replace(":hardware", "hardware")
        clean = clean.replace(":distributed", "distributed")
        clean = clean.replace(":ai", "ai")
        clean = clean.replace(":gpu", "gpu")
        clean = clean.replace(":formal", "formal")
        clean = clean.replace("&", "and")
        clean = re.sub(r"[^a-z0-9]+", "-", clean).strip("-")
        return f"{self.seq:03d}-{self.doc_id.lower()}-{clean}"

    @property
    def phase_dir(self) -> Path:
        phase_slug = re.sub(r"[^a-z0-9]+", "-", self.phase_name.lower()).strip("-")
        return DOCS / f"phase-{self.phase:02d}-{phase_slug}"

    @property
    def path(self) -> Path:
        return self.phase_dir / f"{self.slug}.md"


def parse_inventory() -> list[Doc]:
    docs: list[Doc] = []
    for line in RAW_INVENTORY.splitlines():
        seq_s, phase_s, doc_id, title = line.split("|", 3)
        docs.append(Doc(int(seq_s), int(phase_s), doc_id, title))
    if len(docs) != 240:
        raise SystemExit(f"expected 240 docs, found {len(docs)}")
    for expected, doc in enumerate(docs, start=1):
        if doc.seq != expected:
            raise SystemExit(f"sequence gap at {expected}: {doc}")
    return docs


def prefix(doc_id: str) -> str:
    return re.match(r"[A-Z]+", doc_id).group(0)  # type: ignore[union-attr]


def article(text: str) -> str:
    return "an" if text[:1].lower() in {"a", "e", "i", "o", "u"} else "a"


def domain_from_title(title: str) -> str:
    base = title
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
    ]:
        base = base.replace(suffix, "")
    return base


def category_name(doc: Doc) -> str:
    return {
        "D": "foundation",
        "L": "language",
        "SAFE": "safety",
        "P": "profile",
        "PERF": "performance",
        "MATH": "math",
        "C": "compiler",
        "B": "backend",
        "R": "runtime",
        "DOM": "domain",
        "S": "schema",
        "A": "ai",
        "PKG": "package",
        "T": "tooling",
        "TEST": "testing",
        "BOOT": "bootstrap",
        "STD": "standard-library",
        "GOV": "governance",
    }[prefix(doc.doc_id)]


def purpose(doc: Doc) -> str:
    subject = domain_from_title(doc.title)
    p = category_name(doc)
    if p == "foundation":
        return (
            f"This document establishes the {subject.lower()} for Gravity. It anchors the "
            "language as a self-hosting, homoiconic computing substrate with one semantic "
            "model and many compilation profiles."
        )
    if p == "language":
        return (
            f"This document specifies the {subject.lower()} that every compiler, profile, "
            "runtime, backend, macro, and library must preserve."
        )
    if p == "safety":
        return (
            f"This document defines the {subject.lower()} rules that make safe Gravity code "
            "free of undefined behavior and explicit about every unsafe escape hatch."
        )
    if p == "profile":
        return (
            f"This document defines the {subject.lower()} contract: which language features "
            "are legal, which effects are available, and which runtime assumptions are forbidden."
        )
    if p == "performance":
        return (
            f"This document defines the {subject.lower()} needed for predictable high performance "
            "without hiding allocation, synchronization, bounds checks, or unsafe operations."
        )
    if p == "math":
        return (
            f"This document defines the {subject.lower()} portion of Gravity's numeric stack, "
            "including EFIR/EML where relevant, certified approximations, and proof artifacts."
        )
    if p == "compiler":
        return (
            f"This document defines the compiler-side {subject.lower()} that turns source forms "
            "into typed, effect-annotated, profile-valid artifacts."
        )
    if p == "backend":
        return (
            f"This document defines the {subject.lower()} required to emit target artifacts while "
            "preserving MIR meaning, safety evidence, and profile constraints."
        )
    if p == "runtime":
        return (
            f"This document defines the {subject.lower()} for the runtime layer. Gravity has "
            "multiple runtimes, and this contract prevents runtime assumptions from leaking "
            "into profiles that cannot support them."
        )
    if p == "domain":
        return (
            f"This document defines how Gravity covers the {subject.lower()} domain without "
            "creating a separate language or diluting the core semantic model."
        )
    if p == "schema":
        return (
            f"This document defines the {subject.lower()} for typed data, interop, generated APIs, "
            "configuration, artifacts, and structured AI outputs."
        )
    if p == "ai":
        return (
            f"This document defines the {subject.lower()} for model calls, tools, agents, memory, "
            "policy, evaluation, approval, and replay-safe workflows."
        )
    if p == "package":
        return (
            f"This document defines the {subject.lower()} for projects, dependency resolution, "
            "artifact provenance, permissions, reproducibility, and registry behavior."
        )
    if p == "tooling":
        return (
            f"This document defines the {subject.lower()} for developer-facing tools that expose "
            "compiler truth rather than hiding profile, effect, safety, or artifact state."
        )
    if p == "testing":
        return (
            f"This document defines the {subject.lower()} that makes the design mechanically "
            "testable across language, compiler, runtime, backend, safety, performance, and AI behavior."
        )
    if p == "bootstrap":
        return (
            f"This document defines the {subject.lower()} needed to move from a seed compiler to a "
            "self-hosted Gravity compiler with reproducible provenance."
        )
    if p == "standard-library":
        return (
            f"This document defines the {subject.lower()} library surface in a profile-aware way so "
            "libraries remain safe, portable, and optimizable."
        )
    return (
        f"This document defines the {subject.lower()} governance mechanism for language evolution, "
        "compatibility, security, stabilization, unsafe code, and ecosystem health."
    )


COMMON_PRINCIPLES = [
    "Gravity is one homoiconic language with one semantic model and many compilation profiles.",
    "A profile is a compile-time contract, not a runtime preference.",
    "Safe Gravity has no undefined behavior: an operation is proven safe, checked, rejected, or explicitly unsafe.",
    "Effects, capabilities, allocation, nondeterminism, and host access are represented in syntax, types, manifests, or artifacts.",
    "The compiler emits typed artifacts with enough metadata for audit, replay, verification, and target-specific lowering.",
]


def focus_points(doc: Doc) -> list[str]:
    p = category_name(doc)
    subject = domain_from_title(doc.title)
    base = [
        f"Define the normative vocabulary for {subject}.",
        f"State the compile-time and runtime obligations for {subject}.",
        f"Identify which profile constraints can reject or narrow {subject}.",
        f"Describe the artifacts, metadata, diagnostics, and conformance evidence produced by {subject}.",
    ]
    by_category = {
        "foundation": [
            "Keep every later specification aligned with the whole-stack thesis.",
            "Separate language semantics from compiler architecture and execution ecosystem.",
            "Define success in terms of coverage, safety, performance, verifiability, and self-hosting.",
        ],
        "language": [
            "Specify the source-level contract before any backend-specific lowering.",
            "Preserve macro expansion, syntax metadata, profile validation, and typed core semantics.",
            "Define how dynamic hosted conveniences degrade or disappear in lower-level profiles.",
        ],
        "safety": [
            "Define the exact safety property, the static proof obligation, and the runtime check fallback.",
            "Make every unsafe island auditable and mechanically connected to its proofs or tests.",
            "Preserve safety through macros, compiler passes, backends, packages, and generated code.",
        ],
        "profile": [
            "List allowed language features, forbidden effects, required analyses, and runtime assumptions.",
            "Specify compatibility with adjacent profiles and cross-profile module boundaries.",
            "Define the diagnostics for feature use that violates the selected profile.",
        ],
        "performance": [
            "Treat performance promises as observable compiler contracts with benchmark evidence.",
            "Make optimization depend on proof, profiling, specialization, and target knowledge.",
            "Forbid performance shortcuts that erase safety, capability, or provenance evidence.",
        ],
        "math": [
            "Represent elementary expressions in EFIR before lowering to execution forms.",
            "Use EML as a symbolic search and verification representation, not as the only evaluator.",
            "Attach certificates for approximation error, branch domains, rewrites, and floating-point behavior.",
        ],
        "compiler": [
            "Map source forms through reader, macro expansion, analysis, checking, MIR, optimization, and lowering.",
            "Preserve source spans, syntax identity, type/effect evidence, ownership facts, and diagnostics.",
            "Define pass inputs and outputs so the compiler can become self-hosted in Gravity.",
        ],
        "backend": [
            "Define the backend ABI from typed MIR and domain IR into target-specific artifacts.",
            "Preserve capability, safety, debug, provenance, and conformance metadata through emission.",
            "Explain how target restrictions are reported rather than hidden behind implicit behavior.",
        ],
        "runtime": [
            "Separate no-runtime, minimal native, managed, distributed, AI, and interactive assumptions.",
            "Describe what support code is linked, generated, delegated to host runtimes, or forbidden.",
            "Expose runtime services through capabilities and profile-aware contracts.",
        ],
        "domain": [
            "Map the domain's existing specialized languages to Gravity profiles, libraries, and backends.",
            "Define what Gravity must express natively and what interop remains necessary.",
            "Identify the conformance examples that prove the domain is actually covered.",
        ],
        "schema": [
            "Define canonical typed data forms that can generate schemas, bindings, migrations, and validators.",
            "Preserve round-trip guarantees between source schemas and emitted artifacts.",
            "Support structured model outputs without weakening validation or capability rules.",
        ],
        "ai": [
            "Model nondeterminism as an effect that can be recorded, replay-protected, evaluated, and audited.",
            "Bind tools, memory, model providers, and approvals to explicit capabilities.",
            "Ensure generated code and plans pass the same safety and conformance gates as human code.",
        ],
        "package": [
            "Make package metadata authoritative for profile targets, capabilities, permissions, provenance, and safety.",
            "Resolve dependencies reproducibly across cross-compilation and latent package spaces.",
            "Attach artifact signing, SBOM, audit, and policy evidence to the build graph.",
        ],
        "tooling": [
            "Surface real compiler state: profiles, effects, IR, diagnostics, safety audits, and artifacts.",
            "Keep tools scriptable so they support local development, CI, and self-hosting.",
            "Avoid UI features that conceal rejected unsafe behavior or unresolved capabilities.",
        ],
        "testing": [
            "Convert each design promise into conformance suites, fixtures, fuzzing, differential tests, or proof checks.",
            "Test by profile and target so portable behavior and target-specific behavior stay distinct.",
            "Require deterministic evidence for self-hosting, backends, performance, and AI replay.",
        ],
        "bootstrap": [
            "Track which compiler stages are trusted, generated, self-hosted, and reproducible.",
            "Define equivalence checks between seed and self-hosted compilers.",
            "Record artifact provenance so bootstrap trust can shrink over time.",
        ],
        "standard-library": [
            "Split library surfaces by profile availability and capability requirements.",
            "Expose safe APIs even when implementations use audited unsafe internals.",
            "Keep library contracts precise enough for compiler optimization and conformance tests.",
        ],
        "governance": [
            "Define who may change stable contracts and which evidence is required.",
            "Separate experimental, preview, stable, deprecated, and removed states.",
            "Require security and compatibility review for unsafe, target, package, and ecosystem changes.",
        ],
    }
    return base + by_category[p]


def dependencies(doc: Doc, docs: list[Doc]) -> list[str]:
    deps = ["D0", "D1", "D3"]
    p = category_name(doc)
    if doc.seq > 10:
        deps.extend(["D6", "D8", "D9"])
    if p in {"language", "safety", "profile", "compiler", "backend", "runtime", "domain", "schema", "ai", "package", "tooling", "testing", "bootstrap", "standard-library"}:
        deps.extend(["L2", "L5", "L6", "L15"])
    if p in {"safety", "compiler", "backend", "runtime", "ai", "package", "testing", "standard-library"}:
        deps.extend(["SAFE1", "SAFE2", "SAFE6", "SAFE10"])
    if p in {"profile", "backend", "runtime", "domain", "testing", "standard-library"}:
        deps.extend(["P1", "P13"])
    if p in {"compiler", "backend", "runtime", "testing", "bootstrap"}:
        deps.extend(["C1", "C11", "C14"])
    if p in {"backend", "runtime"}:
        deps.append("B1")
    if p in {"schema", "ai", "package"}:
        deps.extend(["S1", "S3", "S9"])
    if p == "math":
        deps.extend(["L5", "L6", "PERF1", "PERF10"])
    if p == "governance":
        deps.extend(["GOV1", "GOV2"])

    result: list[str] = []
    for dep in deps:
        if dep != doc.doc_id and dep not in result:
            result.append(dep)
    return result


def outputs(doc: Doc) -> list[str]:
    p = category_name(doc)
    subject = domain_from_title(doc.title)
    common = [
        f"A normative definition of {subject}.",
        "A list of compiler, runtime, tool, package, or governance obligations.",
        "Conformance criteria that can become tests, audits, or review gates.",
    ]
    specialized = {
        "foundation": ["Project-level decision records and non-negotiable design constraints."],
        "language": ["Syntax, semantics, diagnostics, and examples that backends must preserve."],
        "safety": ["Safety proofs, runtime checks, unsafe audit records, and rejection diagnostics."],
        "profile": ["Feature matrices, effect permissions, capability requirements, and compatibility rules."],
        "performance": ["Optimization contracts, benchmark requirements, and proof-backed check elision rules."],
        "math": ["EFIR graphs, EML forms, approximation certificates, and numeric conformance fixtures."],
        "compiler": ["Typed AST/MIR contracts, pass interfaces, diagnostics, and lowering invariants."],
        "backend": ["Target artifacts plus debug, safety, provenance, and conformance metadata."],
        "runtime": ["Runtime service contracts, linked support libraries, and capability enforcement points."],
        "domain": ["Domain coverage examples and replacement criteria for incumbent specialized languages."],
        "schema": ["Generated schemas, encodings, validators, migrations, bindings, and artifact schemas."],
        "ai": ["Agent definitions, tool contracts, memory policies, eval suites, approval records, and replay logs."],
        "package": ["Project manifests, lockfiles, SBOMs, signatures, audit metadata, and reproducible build records."],
        "tooling": ["CLI commands, editor protocols, inspectors, reports, and machine-readable diagnostics."],
        "testing": ["Executable conformance suites, fixtures, property generators, fuzzers, and evidence bundles."],
        "bootstrap": ["Stage artifacts, compiler equivalence records, and reproducible bootstrap provenance."],
        "standard-library": ["Profile-aware APIs, safe wrappers, examples, and conformance tests."],
        "governance": ["Review workflows, policy gates, compatibility records, and stabilization decisions."],
    }[p]
    return common + specialized


def requirements(doc: Doc) -> list[str]:
    p = category_name(doc)
    subject = domain_from_title(doc.title)
    reqs = [
        f"{subject} MUST be defined in terms of Gravity source forms, typed core semantics, effects, capabilities, profiles, or artifacts.",
        f"{subject} MUST state which behavior is portable across profiles and which behavior is target-specific.",
        f"{subject} MUST preserve explicit safety evidence; it MUST NOT depend on implicit undefined behavior.",
        f"{subject} MUST produce diagnostics that name the violated rule and the responsible source span or manifest entry.",
    ]
    by_category = {
        "foundation": [
            "Foundation documents MUST be stable enough for later specifications to cite without redefining the thesis.",
            "Foundation documents MUST distinguish goals, constraints, non-goals, milestones, and replacement criteria.",
        ],
        "language": [
            "Language specifications MUST reduce to the small typed core unless they intentionally define a primitive.",
            "Language specifications MUST describe macro expansion, metadata, hygiene, and profile validation effects where applicable.",
        ],
        "safety": [
            "Safety documents MUST define the rejection path, the runtime check path, and the unsafe escape path.",
            "Safety documents MUST describe how compiler passes preserve or regenerate proofs after optimization.",
        ],
        "profile": [
            "Profile documents MUST enumerate allowed effects, forbidden operations, memory assumptions, runtime assumptions, and backend expectations.",
            "Profile documents MUST define cross-profile imports and what evidence is required at the boundary.",
        ],
        "performance": [
            "Performance documents MUST separate semantic guarantees from optimization strategies.",
            "Performance documents MUST require measurable evidence before an optimization is considered part of a profile promise.",
        ],
        "math": [
            "Math documents MUST state exactness, approximation, domain, branch, rounding, and proof obligations.",
            "Math documents MUST distinguish EFIR/EML verification forms from emitted execution implementations.",
        ],
        "compiler": [
            "Compiler documents MUST define input and output data structures for each pass.",
            "Compiler documents MUST preserve type, effect, ownership, capability, and source-location evidence across passes.",
        ],
        "backend": [
            "Backend documents MUST describe target limits and the diagnostics used when MIR cannot be represented.",
            "Backend documents MUST state how emitted artifacts carry provenance, safety, debug, and conformance metadata.",
        ],
        "runtime": [
            "Runtime documents MUST declare which services are linked, generated, delegated, or unavailable.",
            "Runtime documents MUST ensure runtime APIs are mediated through profile and capability checks.",
        ],
        "domain": [
            "Domain documents MUST identify incumbent languages, replacement scope, required libraries, required backends, and proof examples.",
            "Domain documents MUST state the minimum end-to-end examples that demonstrate credible coverage.",
        ],
        "schema": [
            "Schema documents MUST guarantee validation boundaries and deterministic canonical encoding where claimed.",
            "Schema documents MUST define compatibility behavior for generated APIs, migrations, bindings, and artifacts.",
        ],
        "ai": [
            "AI documents MUST treat model calls as nondeterministic effects with replay, audit, and evaluation records.",
            "AI documents MUST require explicit capabilities for tools, memory access, writes, network calls, and human approval bypasses.",
        ],
        "package": [
            "Package documents MUST make builds reproducible and policy-checkable from manifests, lockfiles, and artifact metadata.",
            "Package documents MUST attach safety, capability, provenance, signing, and audit information to dependency and artifact graphs.",
        ],
        "tooling": [
            "Tooling documents MUST expose machine-readable output for CI and human-readable output for local development.",
            "Tooling documents MUST surface profile, effect, safety, artifact, and diagnostic state without guessing.",
        ],
        "testing": [
            "Testing documents MUST map design requirements to deterministic tests or proof artifacts.",
            "Testing documents MUST cover positive, negative, profile-specific, backend-specific, and regression cases.",
        ],
        "bootstrap": [
            "Bootstrap documents MUST record the trusted computing base for every stage.",
            "Bootstrap documents MUST define equivalence checks between generated, seed-compiled, and self-hosted artifacts.",
        ],
        "standard-library": [
            "Standard-library documents MUST define profile availability and capability requirements for each API family.",
            "Standard-library documents MUST expose safe APIs over audited unsafe internals where performance requires it.",
        ],
        "governance": [
            "Governance documents MUST define the evidence required before a contract becomes stable.",
            "Governance documents MUST protect compatibility, security, unsafe-code review, and ecosystem quality.",
        ],
    }[p]
    return reqs + by_category


def example_block(doc: Doc) -> str:
    p = category_name(doc)
    subject = domain_from_title(doc.title)
    examples = {
        "foundation": f"""```clojure
(decision {doc.doc_id.lower()}
  {{:subject "{subject}"
    :binding true
    :applies-to [:language :compiler :runtime :ecosystem]}})
```""",
        "language": f"""```clojure
(ns example.{doc.doc_id.lower()}
  (:profile :hosted)
  (:effects #{{:io/write}}))

(defn sample [value]
  (match value
    {{:ok x}} x
    {{:err e}} (throw e)))
```""",
        "safety": f"""```clojure
(ns safety.{doc.doc_id.lower()}
  (:profile :native)
  (:safety :safe)
  (:effects #{{:memory/region}}))

(with-region [r]
  (let [buf (region/alloc r Byte 4096)]
    (buffer/fill! buf 0)))
```""",
        "profile": f"""```clojure
(ns profile.{doc.doc_id.lower()}
  (:profile :native)
  (:target :llvm)
  (:effects #{{:memory/region :thread/spawn}})
  (:capabilities [clock monotonic]))
```""",
        "performance": f"""```clojure
(defn kernel-step
  {{:optimize [:inline :specialize]
    :bounds :elide-when-proven}}
  [xs :- (Vector F32)]
  (vector/map fast-step xs))
```""",
        "math": f"""```clojure
(defn f
  {{:elementary true
    :domain {{x [-1.0 1.0]}}
    :mode :certified-approx
    :max-error 1.0e-9}}
  [x :- F64]
  (+ (sin x) (log (+ 1.0 x))))
```""",
        "compiler": f"""```clojure
(defpass {doc.doc_id.lower()}-pass
  {{:input :typed-core
    :output :gravity-mir
    :preserves [:types :effects :source-spans]}}
  [module]
  (lower module))
```""",
        "backend": f"""```clojure
(defbackend {doc.doc_id.lower()}
  {{:input :gravity-mir
    :target :native
    :emits [:object :debug :provenance]
    :rejects [:unresolved-effects :unsupported-layouts]}})
```""",
        "runtime": f"""```clojure
(runtime {doc.doc_id.lower()}
  {{:profile :native
    :services [:allocator :panic :atomics]
    :capability-checks true}})
```""",
        "domain": f"""```clojure
(domain {doc.doc_id.lower()}
  {{:profiles [:core :native]
    :required-backends [:llvm :c]
    :proof-examples [:compile :run :conformance]}})
```""",
        "schema": f"""```clojure
(defschema Example
  {{:id U64
    :name String
    :created-at Instant}}
  {{:derives [:json-schema :graphql :binary-abi]}})
```""",
        "ai": f"""```clojure
(defagent reviewer
  {{:model :provider/default
    :tools [repo/read repo/propose-patch]
    :effects #{{:ai/model-call :filesystem/read}}
    :approval {{:writes :human-required}}}})
```""",
        "package": f"""```clojure
(project
  {{:name example/gravity-module
    :profiles [:core :native]
    :capabilities [:filesystem/read]
    :artifacts [:library :docs]
    :reproducible true}})
```""",
        "tooling": f"""```bash
gravity inspect --document {doc.doc_id} --format json
gravity check --profile native --explain-effects
```""",
        "testing": f"""```clojure
(deftest {doc.doc_id.lower()}-conformance
  {{:profile :core
    :requires [:deterministic :negative-cases]}}
  (is (conforms? '{doc.doc_id} sample-fixture)))
```""",
        "bootstrap": f"""```clojure
(bootstrap-stage {doc.doc_id.lower()}
  {{:compiler :seed
    :input :gravity-source
    :output :self-hosted-artifact
    :checks [:hash :semantic-equivalence :reproducible]}})
```""",
        "standard-library": f"""```clojure
(ns gravity.std.{doc.doc_id.lower()}
  (:profile [:core :hosted :native])
  (:exports [open close map reduce]))
```""",
        "governance": f"""```clojure
(rfc
  {{:id "{doc.doc_id}-0001"
    :area "{subject}"
    :state :review
    :required-evidence [:spec :tests :compatibility :security]}})
```""",
    }
    return examples[p]


def criticality(doc: Doc) -> str:
    if doc.seq <= 30:
        return (
            "This document is part of the critical first 30. Implementation should not harden "
            "until this contract has at least one reviewed draft and traceable acceptance criteria."
        )
    if doc.phase <= 6:
        return "This document is upstream of compiler hardening and should be stabilized before backend proliferation."
    if doc.phase <= 12:
        return "This document can evolve in parallel once the core language, safety, profile, and compiler contracts are stable."
    return "This document is downstream of the core contracts and should cite stable upstream specifications rather than redefining them."


def render_doc(doc: Doc, docs: list[Doc]) -> str:
    phase_desc = PHASES[doc.phase][1]
    deps = dependencies(doc, docs)
    source_pages = "PDF pages 114-128 for the final sequence; pages 1-33, 73-113 for source concepts."
    subject = domain_from_title(doc.title)
    principles = "\n".join(f"- {item}" for item in COMMON_PRINCIPLES)
    focus = "\n".join(f"- {item}" for item in focus_points(doc))
    reqs = "\n".join(f"- {item}" for item in requirements(doc))
    outs = "\n".join(f"- {item}" for item in outputs(doc))
    deps_text = "\n".join(f"- `{dep}`" for dep in deps) if deps else "- None"
    criteria = "\n".join(
        [
            f"- A reader can implement or review {article(subject)} {subject.lower()} component without reading unrelated phase documents.",
            "- Every MUST/SHOULD statement has a test, proof, fixture, audit rule, or review gate named in this document.",
            "- Profile-specific behavior is explicitly marked as allowed, forbidden, checked, or delegated.",
            "- Unsafe behavior is absent from the safe surface or isolated behind an auditable unsafe contract.",
            "- Generated artifacts include enough metadata for diagnostics, provenance, conformance, and future self-hosting.",
        ]
    )
    return dedent(
        f"""\
        # {doc.doc_id} - {doc.title}

        Sequence: {doc.seq}
        Phase: {doc.phase} - {doc.phase_name}
        Status: Draft 1
        Source basis: {source_pages}

        ## Purpose

        {purpose(doc)}

        {criticality(doc)}

        ## Phase Context

        {phase_desc}

        This document must stay consistent with the Gravity thesis: a Clojure-inspired Lisp whose code is data, whose compiler is programmable from inside the language, and whose semantics are stratified by compilation profile rather than by separate languages.

        ## Shared Principles

        {principles}

        ## Scope

        This document covers {subject.lower()} as a first-class part of Gravity. It defines the vocabulary, required behavior, rejected behavior, and emitted evidence for this area. It does not replace lower-level documents for syntax, core semantics, type checking, effect checking, capability checking, memory safety, or backend lowering; it composes with them.

        ## Normative Focus

        {focus}

        ## Requirements

        {reqs}

        ## Dependencies

        {deps_text}

        Dependencies are semantic dependencies, not necessarily authoring blockers. A downstream document may be drafted earlier, but it must be reconciled when an upstream contract changes.

        ## Outputs and Artifacts

        {outs}

        ## Example

        {example_block(doc)}

        ## Cross-Profile Behavior

        - `:core` code may only rely on pure, portable semantics and must not require host services.
        - `:hardware`, `:firmware`, and `:kernel` code must reject GC assumptions, dynamic eval, reflection, unbounded allocation, and ambient authority.
        - `:native` code may use explicit memory strategies, FFI, threads, and target-specific optimization when effects and capabilities make them visible.
        - `:hosted` code may delegate services to JVM, JavaScript, Wasm, or similar runtimes while preserving Gravity diagnostics and artifacts.
        - `:distributed` and `:ai` code must record nondeterminism, external calls, retries, tool access, and replay-relevant data.
        - `:meta` code may manipulate syntax and IR, but generated code must pass the same profile, type, effect, and safety checks as handwritten code.

        ## Safety, Performance, and Verification

        Safety checks are specifications first and runtime operations only when proof is insufficient. Optimizations may remove checks only when the compiler can preserve or regenerate the proof that made the check redundant. Mathematical, memory, concurrency, capability, and AI behavior must be represented in artifacts that can be audited after compilation.

        Performance work in this area must expose its assumptions: target, profile, layout, specialization inputs, benchmarks, and proof obligations. The fastest implementation accepted by Gravity is the fastest implementation that still satisfies the declared semantic and safety contract.

        ## Diagnostics

        Diagnostics for this document should include:

        - The violated rule identifier.
        - The source span, syntax object, manifest entry, package dependency, or artifact edge that caused the violation.
        - The active profile and target.
        - The type, effect, ownership, capability, or proof fact that was missing.
        - A deterministic remediation path.

        ## Conformance Criteria

        {criteria}

        ## Change Control

        Changes to this document require updates to the document inventory, impacted downstream references, and any generated conformance fixtures. If a change affects safe-code guarantees, profile legality, artifact shape, or self-hosting trust, it also requires a safety and compatibility review.
        """
    )


def render_phase_index(phase: int, phase_docs: list[Doc]) -> str:
    name, desc = PHASES[phase]
    rows = "\n".join(
        f"- [{doc.seq:03d} {doc.doc_id} - {doc.title}]({doc.path.name})" for doc in phase_docs
    )
    return dedent(
        f"""\
        # Phase {phase} - {name}

        {desc}

        ## Documents

        {rows}
        """
    )


def relative(path: Path) -> str:
    return path.relative_to(ROOT).as_posix()


def relative_to_docs(path: Path) -> str:
    return path.relative_to(DOCS).as_posix()


def clean_markdown(content: str) -> str:
    lines = content.lstrip().splitlines()
    normalized = [line[8:] if line.startswith("        ") else line for line in lines]
    return "\n".join(normalized).rstrip() + "\n"


def render_root_index(docs: list[Doc]) -> str:
    phase_lines = []
    for phase in sorted(PHASES):
        phase_docs = [doc for doc in docs if doc.phase == phase]
        phase_dir = phase_docs[0].phase_dir
        phase_lines.append(
            f"- [Phase {phase} - {PHASES[phase][0]}]({relative_to_docs(phase_dir / 'README.md')}) ({len(phase_docs)} docs)"
        )
    critical = "\n".join(
        f"{doc.seq}. [{doc.doc_id} - {doc.title}]({relative_to_docs(doc.path)})" for doc in docs[:30]
    )
    return dedent(
        f"""\
        # Gravity Documentation Set

        This repository contains the 240-document Gravity design set identified in `Gravity Lisp Design.pdf`.

        Gravity is a self-hosting, homoiconic, Clojure-inspired language platform for the whole software stack. The central design move is one semantic model with many compilation profiles, not one runtime everywhere.

        ## Phases

        {chr(10).join(phase_lines)}

        ## Critical First 30

        The PDF identifies the first 30 documents as the documents to write before serious implementation work begins:

        {critical}

        ## Source Concepts

        - Code is data; compiler extension uses syntax objects and IR values rather than opaque text.
        - Profiles define legal features and runtime assumptions for `:core`, `:hardware`, `:firmware`, `:kernel`, `:native`, `:hosted`, `:distributed`, `:ai`, `:meta`, `:gpu`, and `:formal`.
        - Safe Gravity has no undefined behavior. Unsafe work is explicit, isolated, audited, and attached to artifacts.
        - Effects and capabilities make host access, IO, allocation, nondeterminism, model calls, and tool access visible.
        - EFIR and EML make elementary functions analyzable, optimizable, and certifiable without forcing EML to be the only execution representation.
        - The compiler is intended to become mostly self-hosted: reader, macroexpander, analyzer, MIR, passes, package system, build system, and standard library move into Gravity over time.
        """
    )


def render_manifest(docs: list[Doc]) -> str:
    payload = [
        {
            "sequence": doc.seq,
            "phase": doc.phase,
            "phaseName": doc.phase_name,
            "id": doc.doc_id,
            "title": doc.title,
            "path": relative(doc.path),
            "category": category_name(doc),
        }
        for doc in docs
    ]
    return json.dumps(payload, indent=2) + "\n"


def render_sequence(docs: list[Doc]) -> str:
    lines = [
        "# Final Gravity Document Sequence",
        "",
        "This inventory is transcribed from the final sequence in `Gravity Lisp Design.pdf`.",
        "",
        "| Seq | ID | Phase | Document |",
        "| ---: | --- | --- | --- |",
    ]
    for doc in docs:
        lines.append(
            f"| {doc.seq} | `{doc.doc_id}` | {doc.phase} - {doc.phase_name} | [{doc.title}]({relative_to_docs(doc.path)}) |"
        )
    return "\n".join(lines) + "\n"


def write(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(clean_markdown(content), encoding="utf-8")


def main() -> None:
    docs = parse_inventory()
    for doc in docs:
        write(doc.path, render_doc(doc, docs))

    for phase in sorted(PHASES):
        phase_docs = [doc for doc in docs if doc.phase == phase]
        write(phase_docs[0].phase_dir / "README.md", render_phase_index(phase, phase_docs))

    write(DOCS / "README.md", render_root_index(docs))
    write(DOCS / "document-sequence.md", render_sequence(docs))
    write(DOCS / "document-inventory.json", render_manifest(docs))

    print(f"generated {len(docs)} documents in {DOCS}")


if __name__ == "__main__":
    main()
