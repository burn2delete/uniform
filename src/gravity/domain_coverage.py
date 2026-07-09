"""Phase 09 domain-specific coverage artifact validation."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any


REQUIRED_DOCUMENTS = [f"DOM{index}" for index in range(1, 22)]
GENERIC_DOMAIN_FIELDS = [
    "task_id",
    "governing_doc",
    "domain",
    "profiles",
    "backends",
    "runtime_services",
    "schemas",
    "capabilities",
    "effects",
    "accepted_examples",
    "rejected_examples",
    "artifacts",
    "diagnostics",
    "dependencies",
    "replacement_scope",
    "conformance",
    "evidence",
]


DOMAIN_CONTRACTS: dict[str, dict[str, tuple[str, str]]] = {
    "DOM1": {
        "fixed_widths": ("DOM1-WIDTH", "fixed-width integer, port, and interface evidence"),
        "clock_reset": ("DOM1-CLOCK", "clock and reset domain evidence"),
        "cdc": ("DOM1-CDC", "clock-domain-crossing evidence"),
        "no_runtime": ("DOM1-RUNTIME", "no-runtime hardware profile evidence"),
        "finite_bounds": ("DOM1-UNBOUNDED", "finite loop and resource bounds"),
        "interface_manifest": ("DOM1-INTERFACE", "hardware interface manifest"),
        "timing_manifest": ("DOM1-TIMING", "timing and synthesis evidence"),
        "conformance": ("DOM1-CONFORMANCE", "hardware conformance fixtures"),
    },
    "DOM2": {
        "startup": ("DOM2-STARTUP", "startup and reset vector evidence"),
        "memory_map": ("DOM2-MEMORY", "firmware memory map"),
        "mmio": ("DOM2-MMIO", "MMIO register schema"),
        "interrupt_vector": ("DOM2-INTERRUPT", "interrupt vector evidence"),
        "runtime_boundary": ("DOM2-RUNTIME", "runtime boundary evidence"),
        "bsp": ("DOM2-BSP", "board support package evidence"),
        "conformance": ("DOM2-CONFORMANCE", "firmware conformance fixtures"),
    },
    "DOM3": {
        "runtime_model": ("DOM3-RUNTIME", "kernel runtime model"),
        "raw_memory": ("DOM3-RAW", "raw memory unsafe audit"),
        "allocator": ("DOM3-ALLOC", "kernel allocation policy"),
        "interrupt": ("DOM3-INTERRUPT", "interrupt handling evidence"),
        "syscall": ("DOM3-SYSCALL", "syscall schema and taint evidence"),
        "capability": ("DOM3-CAPABILITY", "kernel capability evidence"),
        "abi": ("DOM3-ABI", "kernel ABI evidence"),
        "conformance": ("DOM3-CONFORMANCE", "kernel conformance fixtures"),
    },
    "DOM4": {
        "register_schema": ("DOM4-REGISTER", "device register schema"),
        "mmio_audit": ("DOM4-MMIO", "MMIO unsafe audit"),
        "dma_lifetime": ("DOM4-DMA", "DMA ownership and lifetime evidence"),
        "interrupt": ("DOM4-INTERRUPT", "driver interrupt evidence"),
        "cache": ("DOM4-CACHE", "cache coherency evidence"),
        "capability": ("DOM4-CAPABILITY", "device capability evidence"),
        "adapter": ("DOM4-ADAPTER", "hosted or mobile adapter evidence"),
        "conformance": ("DOM4-CONFORMANCE", "driver conformance fixtures"),
    },
    "DOM5": {
        "memory": ("DOM5-MEMORY", "native memory and ownership evidence"),
        "target": ("DOM5-TARGET", "native target feature evidence"),
        "ub": ("DOM5-UB", "undefined-behavior rejection evidence"),
        "optimization": ("DOM5-OPTIMIZATION", "proof-gated optimization evidence"),
        "numeric": ("DOM5-NUMERIC", "numeric mode evidence"),
        "ffi": ("DOM5-FFI", "safe FFI boundary evidence"),
        "benchmark": ("DOM5-BENCHMARK", "benchmark context"),
        "conformance": ("DOM5-CONFORMANCE", "native conformance fixtures"),
    },
    "DOM6": {
        "dom_access": ("DOM6-DOM", "browser DOM capability evidence"),
        "taint": ("DOM6-TAINT", "web taint validation evidence"),
        "schema": ("DOM6-SCHEMA", "route/component/API schemas"),
        "package": ("DOM6-PACKAGE", "web package artifact evidence"),
        "numeric": ("DOM6-NUMERIC", "web numeric behavior evidence"),
        "sourcemap": ("DOM6-SOURCEMAP", "source map evidence"),
        "conformance": ("DOM6-CONFORMANCE", "web conformance fixtures"),
    },
    "DOM7": {
        "permission": ("DOM7-PERMISSION", "platform permission evidence"),
        "lifecycle": ("DOM7-LIFECYCLE", "mobile lifecycle evidence"),
        "thread": ("DOM7-THREAD", "thread and UI boundary evidence"),
        "nullability": ("DOM7-NULL", "host nullability translation evidence"),
        "storage": ("DOM7-STORAGE", "mobile storage and migration evidence"),
        "secret": ("DOM7-SECRET", "mobile secret handling evidence"),
        "conformance": ("DOM7-CONFORMANCE", "mobile conformance fixtures"),
    },
    "DOM8": {
        "route": ("DOM8-ROUTE", "typed route and handler evidence"),
        "schema": ("DOM8-SCHEMA", "request, response, and config schemas"),
        "taint": ("DOM8-TAINT", "backend taint validation evidence"),
        "capability": ("DOM8-CAPABILITY", "backend capability evidence"),
        "job": ("DOM8-JOB", "worker retry and idempotency evidence"),
        "secret": ("DOM8-SECRET", "backend secret evidence"),
        "observability": ("DOM8-OBSERVABILITY", "observability evidence"),
        "conformance": ("DOM8-CONFORMANCE", "backend conformance fixtures"),
    },
    "DOM9": {
        "schema": ("DOM9-SCHEMA", "distributed schema evidence"),
        "replay": ("DOM9-REPLAY", "event replay evidence"),
        "idempotency": ("DOM9-IDEMPOTENCY", "idempotency evidence"),
        "retry": ("DOM9-RETRY", "retry policy evidence"),
        "compensation": ("DOM9-COMPENSATION", "compensation evidence"),
        "capability": ("DOM9-CAPABILITY", "distributed capability evidence"),
        "migration": ("DOM9-MIGRATION", "event-log migration evidence"),
        "crdt": ("DOM9-CRDT", "CRDT evidence"),
        "monotonicity": ("DOM9-MONOTONICITY", "CALM monotonicity evidence"),
        "coordination": ("DOM9-COORDINATION", "coordination policy evidence"),
        "conflict": ("DOM9-CONFLICT", "conflict semantics evidence"),
        "sync": ("DOM9-SYNC", "local-first sync evidence"),
        "convergence": ("DOM9-CONVERGENCE", "convergence evidence"),
        "conformance": ("DOM9-CONFORMANCE", "distributed conformance fixtures"),
    },
    "DOM10": {
        "schema": ("DOM10-SCHEMA", "database schema mapping evidence"),
        "prepared_bindings": ("DOM10-QUERY", "prepared query binding evidence"),
        "taint": ("DOM10-TAINT", "SQL taint evidence"),
        "migration": ("DOM10-MIGRATION", "migration policy evidence"),
        "transaction": ("DOM10-TRANSACTION", "transaction and retry evidence"),
        "layout": ("DOM10-LAYOUT", "storage binary layout evidence"),
        "durability": ("DOM10-DURABILITY", "durability and recovery evidence"),
        "conformance": ("DOM10-CONFORMANCE", "query or recovery conformance fixtures"),
    },
    "DOM11": {
        "schema": ("DOM11-SCHEMA", "dataset schema evidence"),
        "capability": ("DOM11-CAPABILITY", "data source capability evidence"),
        "taint": ("DOM11-TAINT", "analytics taint evidence"),
        "lineage": ("DOM11-LINEAGE", "lineage evidence"),
        "memory": ("DOM11-MEMORY", "bounded materialization evidence"),
        "numeric": ("DOM11-NUMERIC", "numeric aggregate evidence"),
        "determinism": ("DOM11-DETERMINISM", "determinism policy evidence"),
        "conformance": ("DOM11-CONFORMANCE", "analytics conformance fixtures"),
    },
    "DOM12": {
        "domain": ("DOM12-DOMAIN", "numeric domain evidence"),
        "mode": ("DOM12-MODE", "numeric mode evidence"),
        "certificate": ("DOM12-CERTIFICATE", "approximation certificate"),
        "rewrite": ("DOM12-REWRITE", "symbolic equivalence proof"),
        "fastmath": ("DOM12-FASTMATH", "fast-math policy evidence"),
        "interop": ("DOM12-INTEROP", "numeric provider boundary evidence"),
        "benchmark": ("DOM12-BENCHMARK", "accuracy benchmark context"),
        "conformance": ("DOM12-CONFORMANCE", "numeric conformance fixtures"),
    },
    "DOM13": {
        "kernel": ("DOM13-KERNEL", "GPU kernel legality evidence"),
        "memory": ("DOM13-MEMORY", "device memory and transfer evidence"),
        "sync": ("DOM13-SYNC", "synchronization evidence"),
        "launch": ("DOM13-LAUNCH", "launch descriptor evidence"),
        "math": ("DOM13-MATH", "GPU numeric evidence"),
        "host_effect": ("DOM13-HOST-EFFECT", "host effect rejection evidence"),
        "target": ("DOM13-TARGET", "device target feature evidence"),
        "conformance": ("DOM13-CONFORMANCE", "GPU conformance fixtures"),
    },
    "DOM14": {
        "timestep": ("DOM14-TIMESTEP", "simulation timestep policy"),
        "allocation": ("DOM14-ALLOC", "frame allocation evidence"),
        "determinism": ("DOM14-DETERMINISM", "deterministic replay evidence"),
        "numeric": ("DOM14-NUMERIC", "game numeric evidence"),
        "asset": ("DOM14-ASSET", "asset schema evidence"),
        "plugin": ("DOM14-PLUGIN", "plugin capability evidence"),
        "performance": ("DOM14-PERFORMANCE", "frame budget evidence"),
        "conformance": ("DOM14-CONFORMANCE", "game conformance fixtures"),
    },
    "DOM15": {
        "secret": ("DOM15-SECRET", "secret redaction evidence"),
        "random": ("DOM15-RANDOM", "approved randomness capability"),
        "provider": ("DOM15-PROVIDER", "crypto provider manifest"),
        "webauthn": ("DOM15-WEBAUTHN", "WebAuthn ceremony evidence"),
        "passkey": ("DOM15-PASSKEY", "passkey credential policy"),
        "private_compute": ("DOM15-PRIVATE-COMPUTE", "private computation provider evidence"),
        "boundary": ("DOM15-BOUNDARY", "plaintext/ciphertext boundary audit"),
        "noise": ("DOM15-NOISE", "noise and depth budget evidence"),
        "leakage": ("DOM15-LEAKAGE", "privacy leakage evidence"),
        "custody": ("DOM15-CUSTODY", "key custody evidence"),
        "constant_time": ("DOM15-CONSTANT-TIME", "constant-time analysis evidence"),
        "custom_crypto": ("DOM15-CUSTOM", "custom crypto review evidence"),
        "taint": ("DOM15-TAINT", "protocol taint evidence"),
        "ffi": ("DOM15-FFI", "safe crypto FFI wrapper audit"),
        "conformance": ("DOM15-CONFORMANCE", "crypto conformance fixtures"),
    },
    "DOM16": {
        "determinism": ("DOM16-DETERMINISM", "contract determinism evidence"),
        "schema": ("DOM16-SCHEMA", "ABI, state, event, and transaction schemas"),
        "numeric": ("DOM16-NUMERIC", "checked contract arithmetic evidence"),
        "gas": ("DOM16-GAS", "gas and resource accounting"),
        "auth": ("DOM16-AUTH", "state mutation authorization evidence"),
        "account_validation": ("DOM16-ACCOUNT-VALIDATION", "account validation evidence"),
        "userop": ("DOM16-USEROP", "user operation schema"),
        "session_key": ("DOM16-SESSION-KEY", "session key policy"),
        "paymaster": ("DOM16-PAYMASTER", "paymaster policy"),
        "delegation": ("DOM16-DELEGATION", "delegation and revocation evidence"),
        "replay": ("DOM16-REPLAY", "replay domain and nonce evidence"),
        "bundler": ("DOM16-BUNDLER", "bundler and simulation assumptions"),
        "wallet_binding": ("DOM16-WALLET-BINDING", "wallet binding artifacts"),
        "aa_profile": ("DOM16-AA-PROFILE", "account-abstraction profile record"),
        "erc4337": ("DOM16-ERC4337", "ERC-4337 profile evidence"),
        "eip7702": ("DOM16-EIP7702", "EIP-7702 authorization evidence"),
        "erc7579": ("DOM16-ERC7579", "ERC-7579 module evidence"),
        "upgrade": ("DOM16-UPGRADE", "upgrade and migration evidence"),
        "invariant": ("DOM16-INVARIANT", "invariant evidence"),
        "ordering": ("DOM16-ORDERING", "transaction-ordering assumptions"),
        "mev": ("DOM16-MEV", "MEV exposure and mitigation evidence"),
        "conformance": ("DOM16-CONFORMANCE", "chain conformance evidence"),
    },
    "DOM17": {
        "pass_contract": ("DOM17-PASS", "compiler pass contract"),
        "metadata": ("DOM17-METADATA", "metadata preservation evidence"),
        "generated": ("DOM17-GENERATED", "generated-code validation evidence"),
        "plugin": ("DOM17-PLUGIN", "plugin capability evidence"),
        "macro": ("DOM17-MACRO", "macro hygiene evidence"),
        "diagnostic": ("DOM17-DIAGNOSTIC", "diagnostic origin chain evidence"),
        "bootstrap": ("DOM17-BOOTSTRAP", "bootstrap provenance evidence"),
        "conformance": ("DOM17-CONFORMANCE", "tooling conformance fixtures"),
    },
    "DOM18": {
        "model": ("DOM18-MODEL", "model provider and budget evidence"),
        "prompt": ("DOM18-PROMPT", "prompt provenance evidence"),
        "tool": ("DOM18-TOOL", "tool schema and review policy"),
        "schema": ("DOM18-SCHEMA", "structured output schema"),
        "taint": ("DOM18-TAINT", "AI taint validation evidence"),
        "secret": ("DOM18-SECRET", "AI secret policy evidence"),
        "replay": ("DOM18-REPLAY", "AI replay evidence"),
        "generated": ("DOM18-GENERATED", "generated-code compiler validation"),
        "eval": ("DOM18-EVAL", "eval report evidence"),
    },
    "DOM19": {
        "claim": ("DOM19-CLAIM", "verification claim manifest"),
        "proof": ("DOM19-PROOF", "proof object"),
        "stale": ("DOM19-STALE", "stale-proof invalidation evidence"),
        "assumption": ("DOM19-ASSUMPTION", "solver assumption manifest"),
        "counterexample": ("DOM19-COUNTEREXAMPLE", "counterexample mapping"),
        "eml": ("DOM19-EML", "EML semantic proof evidence"),
        "elision": ("DOM19-ELISION", "proof-gated elision evidence"),
        "zk_relation": ("DOM19-ZK-RELATION", "zk relation metadata"),
        "zk_input": ("DOM19-ZK-INPUT", "public/private input split"),
        "zk_setup": ("DOM19-ZK-SETUP", "zk setup and trust evidence"),
        "zk_privacy": ("DOM19-ZK-PRIVACY", "zk privacy facet evidence"),
        "zk_cost": ("DOM19-ZK-COST", "prover and verifier cost evidence"),
        "zk_chain": ("DOM19-ZK-CHAIN", "recursive or folding chain evidence"),
        "zk_provider": ("DOM19-ZK-PROVIDER", "zk provider record"),
        "conformance": ("DOM19-CONFORMANCE", "formal conformance fixtures"),
    },
    "DOM20": {
        "args": ("DOM20-ARGS", "argument schema evidence"),
        "filesystem": ("DOM20-FILESYSTEM", "filesystem root capability evidence"),
        "shell": ("DOM20-SHELL", "command schema evidence"),
        "taint": ("DOM20-TAINT", "shell taint evidence"),
        "destructive": ("DOM20-DESTRUCTIVE", "destructive action review evidence"),
        "hermeticity": ("DOM20-HERMETICITY", "hermeticity evidence"),
        "secret": ("DOM20-SECRET", "script secret evidence"),
        "audit": ("DOM20-AUDIT", "dry-run and audit evidence"),
    },
    "DOM21": {
        "node": ("DOM21-NODE", "typed visual node schema"),
        "edge": ("DOM21-EDGE", "typed visual edge schema"),
        "effect": ("DOM21-EFFECT", "visual effect evidence"),
        "capability": ("DOM21-CAPABILITY", "tool/model capability evidence"),
        "human_review": ("DOM21-HUMAN-REVIEW", "human-review policy graph"),
        "replay": ("DOM21-REPLAY", "workflow replay evidence"),
        "generated": ("DOM21-GENERATED", "generated-code compiler validation"),
        "mapping": ("DOM21-MAPPING", "visual diagnostic node mapping"),
        "migration": ("DOM21-MIGRATION", "graph migration evidence"),
    },
}


class DomainCoverageError(Exception):
    def __init__(
        self,
        code: str,
        message: str,
        *,
        record: dict[str, Any] | None = None,
        source: str,
        missing_fact: str,
        remediation: str,
    ) -> None:
        super().__init__(message)
        self.code = code
        self.message = message
        self.record = record or {}
        self.source = source
        self.missing_fact = missing_fact
        self.remediation = remediation

    def to_diagnostic(self) -> dict[str, Any]:
        return {
            "id": self.code,
            "message": self.message,
            "document": self.record.get("document"),
            "task_id": self.record.get("task_id"),
            "domain": self.record.get("domain"),
            "profile": first_value(self.record.get("profiles")),
            "backend": first_value(self.record.get("backends")),
            "artifact_id": self.record.get("artifact_id") or self.record.get("id"),
            "source_span": self.record.get("source_span", {"source": self.source}),
            "missing_fact": self.missing_fact,
            "remediation": self.remediation,
            "analyzer_stage": "phase09-domain-coverage-validation",
        }


def validate_domain_file(path: Path) -> dict[str, Any]:
    return validate_domain_manifest(load_manifest_file(path), str(path))


def domain_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_domain_file(path)
    except DomainCoverageError as exc:
        return exc.to_diagnostic()
    return None


def validate_domain_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "domain-coverage-input":
        raise_error("P09-MANIFEST", "domain coverage input has wrong kind", {}, source, "domain-coverage-input")

    slice_manifest = require_dict(manifest, "domain_slice_manifest", "P09-MANIFEST", source)
    validate_slice_manifest(slice_manifest, source)
    domains = require_dict(manifest, "domains", "P09-MANIFEST", source)
    validate_domain_set(domains, source)
    accepted = require_list(manifest, "accepted_domain_fixtures", "P09-ACCEPTED", source)
    rejected = require_list(manifest, "rejected_domain_fixtures", "P09-REJECTED", source)
    replacement_claims = require_list(manifest, "replacement_claim_records", "P09-CLAIM", source)
    conformance = require_list(manifest, "domain_conformance_evidence", "P09-CONFORMANCE", source)

    validate_fixture_index(accepted, domains, "accepted", source)
    validate_fixture_index(rejected, domains, "rejected", source)
    validate_replacement_claims(replacement_claims, source)
    validate_conformance_evidence(conformance, source)

    return {
        "kind": "domain-coverage-artifact",
        "phase": "09",
        "package": manifest.get("package"),
        "module": manifest.get("module"),
        "domain_slice_manifest": slice_manifest,
        "domain_contracts": {key: domains[key] for key in REQUIRED_DOCUMENTS},
        "accepted_domain_fixtures": accepted,
        "rejected_domain_fixtures": rejected,
        "replacement_claim_records": replacement_claims,
        "domain_conformance_evidence": conformance,
        "coverage_summary": {
            "documents": len(REQUIRED_DOCUMENTS),
            "domain_records": len(domains),
            "accepted_fixtures": len(accepted),
            "rejected_fixtures": len(rejected),
            "replacement_claims": len(replacement_claims),
            "conformance_records": len(conformance),
            "status": ":passed",
        },
        "input_hash": artifact_hash(manifest),
        "diagnostics": [],
    }


def validate_slice_manifest(record: dict[str, Any], source: str) -> None:
    for key in ["packet", "incumbent_comparison", "profile_matrix", "artifact_packet", "proof_gaps"]:
        if not record.get(key):
            raise_error("P09-MANIFEST", "domain slice manifest lacks required packet field", record, source, key)
    documents = set(record.get("documents", []))
    missing = sorted(set(REQUIRED_DOCUMENTS) - documents, key=document_sort_key)
    if missing:
        raise_error("P09-MANIFEST", "domain slice manifest omits documents", record, source, ",".join(missing))


def validate_domain_set(domains: dict[str, Any], source: str) -> None:
    missing = sorted(set(REQUIRED_DOCUMENTS) - set(domains), key=document_sort_key)
    if missing or len(domains) != len(REQUIRED_DOCUMENTS):
        raise_error("P09-MANIFEST", "Phase 09 coverage must include DOM1 through DOM21", {}, source, ",".join(missing))
    for document in REQUIRED_DOCUMENTS:
        validate_domain_record(document, domains[document], source)


def validate_domain_record(document: str, record: dict[str, Any], source: str) -> None:
    if record.get("document") != document:
        raise_error("P09-MANIFEST", "domain record document id mismatch", record, source, document)
    for field in GENERIC_DOMAIN_FIELDS:
        if not record.get(field):
            raise_error("P09-MANIFEST", "domain record lacks required field", record, source, field)
    diagnostics = set(record.get("diagnostics", []))
    evidence = require_dict(record, "evidence", "P09-MANIFEST", source)
    for fact, (code, missing_fact) in DOMAIN_CONTRACTS[document].items():
        if code not in diagnostics:
            raise_error(code, "domain record omits owning diagnostic id", record, source, code)
        if not evidence.get(fact):
            raise_error(code, f"{document} missing required evidence: {fact}", record, source, missing_fact)
    replacement_scope = require_dict(record, "replacement_scope", "P09-CLAIM", source)
    if replacement_scope.get("claim_status") == ":full-replacement":
        raise_error("P09-CLAIM", "broad replacement claim is not supported by slice evidence", record, source, "slice-scoped-claim")
    if not replacement_scope.get("provider_boundaries") or not replacement_scope.get("claim_limits"):
        raise_error("P09-CLAIM", "replacement scope lacks provider boundaries or limits", record, source, "provider-boundaries-and-limits")
    conformance = require_dict(record, "conformance", "P09-CONFORMANCE", source)
    for key in ["accepted_fixture", "rejected_fixture", "artifact_evidence", "validation_command"]:
        if not conformance.get(key):
            raise_error("P09-CONFORMANCE", "domain conformance record lacks evidence", record, source, key)


def validate_fixture_index(records: list[Any], domains: dict[str, Any], kind: str, source: str) -> None:
    documents = {record.get("document") for record in records if isinstance(record, dict)}
    missing = sorted(set(REQUIRED_DOCUMENTS) - documents, key=document_sort_key)
    if missing or len(records) != len(REQUIRED_DOCUMENTS):
        raise_error(
            f"P09-{kind.upper()}",
            f"{kind} domain fixture index must include DOM1 through DOM21",
            {},
            source,
            ",".join(missing),
        )
    for record in records:
        document = record.get("document")
        if document not in domains:
            raise_error(f"P09-{kind.upper()}", f"{kind} fixture references unknown document", record, source, "document")
        if not record.get("fixture") or not record.get("artifact") or not record.get("evidence"):
            raise_error(f"P09-{kind.upper()}", f"{kind} fixture lacks path, artifact, or evidence", record, source, "fixture-artifact-evidence")


def validate_replacement_claims(records: list[Any], source: str) -> None:
    documents = {record.get("document") for record in records if isinstance(record, dict)}
    missing = sorted(set(REQUIRED_DOCUMENTS) - documents, key=document_sort_key)
    if missing or len(records) != len(REQUIRED_DOCUMENTS):
        raise_error("P09-CLAIM", "replacement claim records must include DOM1 through DOM21", {}, source, ",".join(missing))
    for record in records:
        if record.get("claim_status") != ":slice-supported":
            raise_error("P09-CLAIM", "replacement claim must remain evidence-scoped", record, source, "slice-supported")
        if not record.get("evidence_refs") or not record.get("excluded_provider_boundaries"):
            raise_error("P09-CLAIM", "replacement claim lacks evidence refs or excluded providers", record, source, "evidence-and-exclusions")


def validate_conformance_evidence(records: list[Any], source: str) -> None:
    documents = {record.get("document") for record in records if isinstance(record, dict)}
    missing = sorted(set(REQUIRED_DOCUMENTS) - documents, key=document_sort_key)
    if missing or len(records) != len(REQUIRED_DOCUMENTS):
        raise_error("P09-CONFORMANCE", "conformance evidence must include DOM1 through DOM21", {}, source, ",".join(missing))
    for record in records:
        for key in ["accepted_behavior", "rejected_behavior", "artifacts", "validation"]:
            if not record.get(key):
                raise_error("P09-CONFORMANCE", "conformance evidence lacks required field", record, source, key)


def require_dict(manifest: dict[str, Any], key: str, code: str, source: str) -> dict[str, Any]:
    value = manifest.get(key)
    if not isinstance(value, dict) or not value:
        raise_error(code, "manifest lacks required object", {}, source, key)
    return value


def require_list(manifest: dict[str, Any], key: str, code: str, source: str) -> list[Any]:
    value = manifest.get(key)
    if not isinstance(value, list) or not value:
        raise_error(code, "manifest lacks required list", {}, source, key)
    return value


def raise_error(code: str, message: str, record: dict[str, Any], source: str, missing_fact: str) -> None:
    raise DomainCoverageError(
        code,
        message,
        record=record,
        source=source,
        missing_fact=missing_fact,
        remediation="Update the Phase 09 domain manifest, fixture, claim record, conformance evidence, or governing diagnostic.",
    )


def load_manifest_file(path: Path) -> dict[str, Any]:
    manifest = json.loads(path.read_text(encoding="utf-8"))
    if "extends" not in manifest:
        return manifest
    base = load_manifest_file(path.parent / manifest["extends"])
    result = json.loads(json.dumps(base))
    for pointer in manifest.get("delete", []):
        delete_pointer(result, pointer)
    for pointer, value in manifest.get("set", {}).items():
        set_pointer(result, pointer, value)
    return result


def set_pointer(value: Any, pointer: str, replacement: Any) -> None:
    parent, key = pointer_parent(value, pointer)
    if isinstance(parent, list):
        parent[int(key)] = replacement
    else:
        parent[key] = replacement


def delete_pointer(value: Any, pointer: str) -> None:
    parent, key = pointer_parent(value, pointer)
    if isinstance(parent, list):
        del parent[int(key)]
    else:
        del parent[key]


def pointer_parent(value: Any, pointer: str) -> tuple[Any, str]:
    parts = pointer_parts(pointer)
    if not parts:
        raise ValueError("cannot address manifest root")
    current = value
    for part in parts[:-1]:
        current = current[int(part)] if isinstance(current, list) else current[part]
    return current, parts[-1]


def pointer_parts(pointer: str) -> list[str]:
    if not pointer.startswith("/"):
        raise ValueError(f"invalid JSON pointer: {pointer}")
    return [part.replace("~1", "/").replace("~0", "~") for part in pointer.split("/")[1:]]


def document_sort_key(document: str | None) -> int:
    if not document or not document.startswith("DOM"):
        return 0
    return int(document[3:])


def first_value(value: Any) -> Any:
    if isinstance(value, list) and value:
        return value[0]
    return value


def artifact_hash(value: Any) -> str:
    data = json.dumps(value, sort_keys=True, separators=(",", ":"))
    return "sha256:" + hashlib.sha256(data.encode("utf-8")).hexdigest()
