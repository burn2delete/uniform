"""Phase 02 document-specific safety coverage validation."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any

from gravity.capability_supply_chain import capability_supply_chain_diagnostic, validate_capability_supply_chain_file
from gravity.domain_safety import domain_safety_diagnostic, validate_domain_safety_file
from gravity.memory_safety import analyze_memory_safety_file, memory_safety_diagnostic
from gravity.safety import analyze_safety_file, safety_manifest_diagnostic
from gravity.safety_conformance import safety_conformance_diagnostic, validate_safety_conformance_file
from gravity.unsafe_audit import unsafe_audit_diagnostic, validate_unsafe_audit_file


ROOT = Path(__file__).resolve().parents[2]
REQUIRED_DOCUMENTS = {f"SAFE{index}" for index in range(1, 17)}
VALIDATORS = {
    "safety-classifier",
    "memory-safety",
    "unsafe-audit",
    "domain-safety",
    "capability-supply-chain",
    "safe12-macro-safety",
    "safe13-ai-tool-safety",
    "safe15-certificate-safety",
    "safety-conformance",
}
SAFE12_NORMAL_CHECKS = {"macro", "type", "effect", "capability", "memory", "profile", "safety"}
SAFE13_CODE_CHECKS = {"macro", "type", "effect", "capability", "memory", "package", "safety"}
SAFE15_CERTIFICATE_FIELDS = {
    "id",
    "package_id",
    "compiler_id",
    "provider_id",
    "claim",
    "source_span",
    "artifact_node",
    "profile",
    "target",
    "assumptions",
    "method",
    "inputs",
    "invalidated_by",
    "trust_root",
    "signature",
    "verification_status",
}
SAFE15_PROOF_FIELDS = {
    "id",
    "claim",
    "source_span",
    "artifact_node",
    "profile",
    "target",
    "assumptions",
    "method",
    "provider",
    "inputs",
    "invalidated_by",
    "result",
}


class SafetyDocumentCoverageError(Exception):
    def __init__(
        self,
        code: str,
        message: str,
        *,
        document: str | None = None,
        task_id: str | None = None,
        source: str,
        missing_fact: str,
        remediation: str,
        span: dict[str, Any] | None = None,
    ) -> None:
        super().__init__(message)
        self.code = code
        self.message = message
        self.document = document
        self.task_id = task_id
        self.source = source
        self.missing_fact = missing_fact
        self.remediation = remediation
        self.span = span or {"source": source}

    def to_diagnostic(self) -> dict[str, Any]:
        return {
            "id": self.code,
            "message": self.message,
            "document": self.document,
            "task_id": self.task_id,
            "span": self.span,
            "missing_fact": self.missing_fact,
            "remediation": self.remediation,
            "analyzer_stage": "phase02-safety-document-coverage",
        }


def validate_phase02_document_coverage_file(path: Path) -> dict[str, Any]:
    return validate_phase02_document_coverage_manifest(load_json(path), str(path))


def phase02_document_coverage_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_phase02_document_coverage_file(path)
    except SafetyDocumentCoverageError as exc:
        return exc.to_diagnostic()
    return None


def validate_phase02_document_coverage_manifest(manifest: dict[str, Any], source: str) -> dict[str, Any]:
    if manifest.get("kind") != "phase02-safety-document-coverage-input":
        raise_error(
            "SAFE16-FIXTURE",
            "document coverage input has the wrong artifact kind",
            source=source,
            missing_fact="phase02-safety-document-coverage-input",
            remediation="Use the phase02-safety-document-coverage-input manifest shape.",
        )
    records = manifest.get("documents", [])
    documents = {record.get("document") for record in records}
    missing = sorted(REQUIRED_DOCUMENTS - documents)
    extra = sorted(doc for doc in documents if doc not in REQUIRED_DOCUMENTS)
    if missing or extra or len(records) != 16:
        raise_error(
            "SAFE16-FIXTURE",
            "Phase 02 document coverage must include exactly SAFE1 through SAFE16",
            source=source,
            missing_fact=f"missing={missing};extra={extra};count={len(records)}",
            remediation="Add one document coverage record for each SAFE document.",
        )

    accepted: list[dict[str, Any]] = []
    rejected: list[dict[str, Any]] = []
    for record in records:
        accepted_record, rejected_record = validate_document_record(record, source)
        accepted.append(accepted_record)
        rejected.append(rejected_record)

    return {
        "kind": "phase02-safety-document-coverage-artifact",
        "phase": "02",
        "package": manifest.get("package"),
        "module": manifest.get("module"),
        "input_hash": artifact_hash(manifest),
        "documents": sorted(documents),
        "accepted": accepted,
        "rejected": rejected,
        "coverage_summary": {
            "documents": len(accepted),
            "accepted_artifacts": len(accepted),
            "rejected_diagnostics": len(rejected),
            "status": ":passed",
        },
        "diagnostics": [],
    }


def validate_document_record(record: dict[str, Any], source: str) -> tuple[dict[str, Any], dict[str, Any]]:
    document = record.get("document")
    task_id = record.get("task_id")
    governing_doc = record.get("governing_doc")
    if not document or not task_id or not governing_doc:
        raise_record_error(record, "SAFE16-FIXTURE", "document record lacks document, task id, or governing doc", source, "document-task-governing-doc")
    doc_path = path_from_root(governing_doc)
    if not doc_path.exists():
        raise_record_error(record, "SAFE16-FIXTURE", f"governing document does not exist: {governing_doc}", source, "governing-doc")
    if f"# {document} -" not in doc_path.read_text(encoding="utf-8"):
        raise_record_error(record, "SAFE16-FIXTURE", "governing document id does not match coverage record", source, "governing-doc-id")

    for surface in record.get("implementation_surfaces", []):
        if not path_from_root(surface).exists():
            raise_record_error(record, "SAFE16-ARTIFACT", f"implementation surface does not exist: {surface}", source, "implementation-surface")

    accepted = record.get("accepted", {})
    rejected = record.get("rejected", {})
    accepted_result = validate_accepted_fixture(record, accepted, source)
    rejected_result = validate_rejected_fixture(record, rejected, source)
    return accepted_result, rejected_result


def validate_accepted_fixture(record: dict[str, Any], accepted: dict[str, Any], source: str) -> dict[str, Any]:
    validator = accepted.get("validator")
    if validator not in VALIDATORS:
        raise_record_error(record, "SAFE16-FIXTURE", f"unsupported accepted validator: {validator}", source, "accepted-validator")
    fixture = require_fixture(record, accepted, source)
    artifact = run_accepted_validator(validator, fixture)
    for field in accepted.get("required_artifact_fields", []):
        if not artifact.get(field):
            raise_record_error(record, "SAFE16-ARTIFACT", f"accepted artifact missing field: {field}", source, field)
    document = record["document"]
    artifact_document = artifact.get("document")
    artifact_documents = set(artifact.get("documents", []))
    if artifact_document and artifact_document not in {document, "SAFE16"}:
        raise_record_error(record, "SAFE16-ARTIFACT", "accepted artifact has the wrong document id", source, "artifact-document")
    if artifact_documents and document not in artifact_documents:
        raise_record_error(record, "SAFE16-ARTIFACT", "accepted artifact does not cover the requested document", source, "artifact-documents")
    return {
        "task_id": record["task_id"],
        "document": document,
        "governing_doc": record["governing_doc"],
        "validator": validator,
        "fixture": rel(fixture),
        "artifact_kind": artifact.get("kind"),
        "coverage": record.get("coverage_claims", []),
        "artifact_hash": artifact_hash(artifact),
    }


def validate_rejected_fixture(record: dict[str, Any], rejected: dict[str, Any], source: str) -> dict[str, Any]:
    validator = rejected.get("validator")
    if validator not in VALIDATORS:
        raise_record_error(record, "SAFE16-FIXTURE", f"unsupported rejected validator: {validator}", source, "rejected-validator")
    fixture = require_fixture(record, rejected, source)
    expected = rejected.get("diagnostic")
    diagnostic = run_rejected_validator(validator, fixture)
    if diagnostic is None:
        raise_record_error(record, "SAFE16-DIAGNOSTIC", f"rejected fixture was accepted: {rel(fixture)}", source, "rejected-diagnostic")
    if diagnostic.get("id") != expected:
        raise_record_error(
            record,
            "SAFE16-DIAGNOSTIC",
            f"rejected fixture produced {diagnostic.get('id')} instead of {expected}",
            source,
            "diagnostic-id",
        )
    return {
        "task_id": record["task_id"],
        "document": record["document"],
        "validator": validator,
        "fixture": rel(fixture),
        "diagnostic": diagnostic["id"],
        "missing_fact": diagnostic.get("missing_fact") or diagnostic.get("missing_evidence"),
    }


def run_accepted_validator(validator: str, fixture: Path) -> dict[str, Any]:
    if validator == "safety-classifier":
        return analyze_safety_file(fixture)
    if validator == "memory-safety":
        return analyze_memory_safety_file(fixture)
    if validator == "unsafe-audit":
        return validate_unsafe_audit_file(fixture)
    if validator == "domain-safety":
        return validate_domain_safety_file(fixture)
    if validator == "capability-supply-chain":
        return validate_capability_supply_chain_file(fixture)
    if validator == "safe12-macro-safety":
        return validate_safe12_macro_safety_file(fixture)
    if validator == "safe13-ai-tool-safety":
        return validate_safe13_ai_tool_safety_file(fixture)
    if validator == "safe15-certificate-safety":
        return validate_safe15_certificate_safety_file(fixture)
    if validator == "safety-conformance":
        return validate_safety_conformance_file(fixture)
    raise ValueError(f"unknown validator: {validator}")


def run_rejected_validator(validator: str, fixture: Path) -> dict[str, Any] | None:
    if validator == "safety-classifier":
        return safety_manifest_diagnostic(fixture)
    if validator == "memory-safety":
        return memory_safety_diagnostic(fixture)
    if validator == "unsafe-audit":
        return unsafe_audit_diagnostic(fixture)
    if validator == "domain-safety":
        return domain_safety_diagnostic(fixture)
    if validator == "capability-supply-chain":
        return capability_supply_chain_diagnostic(fixture)
    if validator == "safe12-macro-safety":
        return safe12_macro_safety_diagnostic(fixture)
    if validator == "safe13-ai-tool-safety":
        return safe13_ai_tool_safety_diagnostic(fixture)
    if validator == "safe15-certificate-safety":
        return safe15_certificate_safety_diagnostic(fixture)
    if validator == "safety-conformance":
        return safety_conformance_diagnostic(fixture)
    raise ValueError(f"unknown validator: {validator}")


def validate_safe12_macro_safety_file(path: Path) -> dict[str, Any]:
    manifest = load_json(path)
    source = str(path)
    if manifest.get("kind") != "safe12-macro-safety-input":
        raise_safe12("SAFE12-ORIGIN", "macro safety input has the wrong kind", manifest, source, "safe12-macro-safety-input")
    requested = set(manifest.get("requested_build_effects", []))
    declared = set(manifest.get("declared_build_effects", []))
    granted = set(manifest.get("granted_build_effects", []))
    if requested - declared or requested - granted:
        raise_safe12("SAFE12-BUILD-EFFECT", "macro requested undeclared or ungranted build effects", manifest, source, "declared-granted-build-effects")
    if manifest.get("runtime_capture"):
        raise_safe12("SAFE12-PHASE", "macro captured runtime authority at compile time", manifest, source, "phase-separation")
    if not manifest.get("generated_origin_chain"):
        raise_safe12("SAFE12-ORIGIN", "macro output lacks generated-origin chain", manifest, source, "generated-origin-chain")
    hygiene = manifest.get("hygiene_context", {})
    if hygiene.get("captures_privileged"):
        raise_safe12("SAFE12-HYGIENE", "macro expansion captures privileged bindings", manifest, source, "privileged-capture-policy")
    declaration = manifest.get("safety_declaration", {})
    if declaration.get("generates_unsafe") and not manifest.get("generated_unsafe_island"):
        raise_safe12("SAFE12-GENERATED-UNSAFE", "macro declares unsafe generation without SAFE6 island metadata", manifest, source, "safe6-generated-unsafe-metadata")
    checks = set(declaration.get("generated_code_checked_by", []))
    if not SAFE12_NORMAL_CHECKS.issubset(checks):
        raise_safe12("SAFE12-PROFILE", "macro generated code did not pass ordinary safety passes", manifest, source, "normal-safety-pass-coverage")
    propagation = manifest.get("taint_capability_propagation", {})
    if not propagation.get("taint_preserved") or not propagation.get("capabilities_preserved"):
        raise_safe12("SAFE12-TAINT", "macro output lost taint or capability metadata", manifest, source, "taint-capability-preservation")
    if manifest.get("facet_output", {}).get("bypasses_safety_checks"):
        raise_safe12("SAFE12-FACET", "facet macro bypassed facet safety checks", manifest, source, "facet-safety-route")
    if manifest.get("alternative_engine", {}).get("safety_equivalent") is False:
        raise_safe12("SAFE12-ENGINE", "alternative macro engine failed safety preservation", manifest, source, "engine-safety-equivalence")
    return {
        "kind": "safe12-macro-safety-artifact",
        "document": "SAFE12",
        "macro_expansion_trace": {
            "macro": manifest.get("macro_symbol"),
            "definition_span": manifest.get("definition_span"),
            "call_site_span": manifest.get("call_site_span"),
            "generated_form_span": manifest.get("generated_form_span"),
            "generated_origin_chain": manifest.get("generated_origin_chain"),
        },
        "generated_origin_chain": manifest.get("generated_origin_chain", []),
        "macro_build_effect_records": [
            {
                "requested": sorted(requested),
                "declared": sorted(declared),
                "granted": sorted(granted),
            }
        ],
        "macro_safety_declaration_records": [declaration],
        "hygiene_capture_records": [hygiene],
        "taint_capability_propagation_records": [propagation],
        "diagnostics": [],
    }


def validate_safe13_ai_tool_safety_file(path: Path) -> dict[str, Any]:
    manifest = load_json(path)
    source = str(path)
    if manifest.get("kind") != "safe13-ai-tool-safety-input":
        raise_safe13("SAFE13-MODEL-EFFECT", "AI safety input has the wrong kind", manifest, source, "safe13-ai-tool-safety-input")
    model = manifest.get("model_call", {})
    if not model.get("effect") or not model.get("provider_id") or not model.get("model_id"):
        raise_safe13("SAFE13-MODEL-EFFECT", "model call lacks effect, provider, or model identity", manifest, source, "model-effect-trace")
    tool = manifest.get("tool_call", {})
    missing_caps = sorted(set(tool.get("capabilities", [])) - set(tool.get("granted_capabilities", [])))
    if missing_caps:
        raise_safe13("SAFE13-TOOL-CAPABILITY", "tool call lacks required authority", manifest, source, ",".join(missing_caps))
    if not tool.get("input_schema_validated"):
        raise_safe13("SAFE13-TOOL-SCHEMA", "tool call arguments were not schema validated", manifest, source, "tool-schema-validation")
    for prompt in manifest.get("prompt_provenance", []):
        if prompt.get("trusted") is False and prompt.get("controls_privileged_policy"):
            raise_safe13("SAFE13-PROMPT-INJECTION", "untrusted prompt content controls privileged policy or tools", manifest, source, "prompt-role-policy-separation")
    side_effect = tool.get("side_effect_class")
    if side_effect in {":write", ":destructive", ":external", ":privileged"}:
        review = tool.get("human_review", {})
        if tool.get("human_review_required") and review.get("decision") != ":approved":
            raise_safe13("SAFE13-HUMAN-REVIEW", "gated tool call lacks approved human-review record", manifest, source, "human-review-record")
    taint = manifest.get("taint", {})
    if not taint.get("model_output_validated"):
        raise_safe13("SAFE13-TOOL-SCHEMA", "model output reached a tool without validation", manifest, source, "model-output-validation")
    if not taint.get("secret_redaction"):
        raise_safe13("SAFE13-SECRET", "secret-bearing AI data lacks redaction policy", manifest, source, "secret-redaction")
    generated = manifest.get("generated_code", {})
    if generated.get("executed_before_checks") or not SAFE13_CODE_CHECKS.issubset(set(generated.get("compiler_safety_checks", []))):
        raise_safe13("SAFE13-GENERATED-CODE", "AI generated code was accepted before normal compiler safety checks", manifest, source, "generated-code-safety-checks")
    if not manifest.get("replay_records"):
        raise_safe13("SAFE13-REPLAY", "AI action lacks replay or audit records", manifest, source, "replay-records")
    retention = manifest.get("agent_memory", {})
    if retention.get("retention_policy") in {None, ":unspecified"}:
        raise_safe13("SAFE13-RETENTION", "agent memory retention policy is missing", manifest, source, "memory-retention-policy")
    return {
        "kind": "safe13-ai-tool-safety-artifact",
        "document": "SAFE13",
        "model_call_trace": [model],
        "tool_call_trace": [tool],
        "prompt_message_provenance_records": manifest.get("prompt_provenance", []),
        "tool_schema_validation_records": [{"tool_id": tool.get("tool_id"), "status": ":validated"}],
        "human_review_records": [tool.get("human_review", {})],
        "replay_records": manifest.get("replay_records", []),
        "model_output_taint_records": [taint],
        "generated_code_safety_records": [generated],
        "diagnostics": [],
    }


def validate_safe15_certificate_safety_file(path: Path) -> dict[str, Any]:
    manifest = load_json(path)
    source = str(path)
    if manifest.get("kind") != "safe15-certificate-safety-input":
        raise_safe15("SAFE15-CERT-SCHEMA", "certificate safety input has the wrong kind", manifest, source, "safe15-certificate-safety-input")
    proofs = {proof.get("id"): proof for proof in manifest.get("proof_records", [])}
    certificates = {cert.get("id"): cert for cert in manifest.get("certificates", [])}
    if not proofs:
        raise_safe15("SAFE15-PROOF-MISSING", "no proof records are present", manifest, source, "proof-record")
    for proof in proofs.values():
        missing = sorted(SAFE15_PROOF_FIELDS - set(proof))
        if missing or proof.get("result") != ":proven-safe":
            raise_safe15("SAFE15-PROOF-MISSING", "proof record lacks required accepted evidence", manifest, source, ",".join(missing or ["accepted-proof-result"]))
    for cert in certificates.values():
        missing = sorted(SAFE15_CERTIFICATE_FIELDS - set(cert))
        if missing:
            raise_safe15("SAFE15-CERT-SCHEMA", "certificate shape is incomplete", manifest, source, ",".join(missing))
        if cert.get("verification_status") != ":accepted" or not cert.get("trust_root") or not cert.get("signature"):
            raise_safe15("SAFE15-CERT-TRUST", "certificate is untrusted or unsigned", manifest, source, "trusted-signature")
        if cert.get("profile") not in manifest.get("accepted_profiles", []) or cert.get("target") not in manifest.get("accepted_targets", []):
            raise_safe15("SAFE15-CERT-MISMATCH", "certificate profile or target is incompatible", manifest, source, "profile-target-match")
    for record in manifest.get("check_erasure_records", []):
        proof = proofs.get(record.get("proof_id"))
        certificate = certificates.get(record.get("certificate_id"))
        if not proof or not certificate:
            raise_safe15("SAFE15-CHECK-ERASE", "erased check lacks matching proof or certificate", manifest, source, "check-erasure-proof-certificate")
        if record.get("backend_preservation") != ":preserved":
            raise_safe15("SAFE15-BACKEND", "backend cannot preserve certificate assumptions", manifest, source, "backend-preservation")
    for invalidation in manifest.get("invalidation_records", []):
        if invalidation.get("status") == ":invalidated" and invalidation.get("certificate_used"):
            raise_safe15("SAFE15-INVALIDATED", "invalidated certificate is still being used", manifest, source, "certificate-invalidation")
    return {
        "kind": "safe15-certificate-safety-artifact",
        "document": "SAFE15",
        "proof_records": list(proofs.values()),
        "safety_certificates": list(certificates.values()),
        "check_erasure_records": manifest.get("check_erasure_records", []),
        "certificate_trust_records": manifest.get("certificate_trust_records", []),
        "invalidation_records": manifest.get("invalidation_records", []),
        "imported_certificate_verification_records": manifest.get("imported_certificate_verification_records", []),
        "proof_provider_records": manifest.get("proof_provider_records", []),
        "audit_views_for_unsafe_wrappers": manifest.get("audit_views_for_unsafe_wrappers", []),
        "diagnostics": [],
    }


def safe12_macro_safety_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_safe12_macro_safety_file(path)
    except SafetyDocumentCoverageError as exc:
        return exc.to_diagnostic()
    return None


def safe13_ai_tool_safety_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_safe13_ai_tool_safety_file(path)
    except SafetyDocumentCoverageError as exc:
        return exc.to_diagnostic()
    return None


def safe15_certificate_safety_diagnostic(path: Path) -> dict[str, Any] | None:
    try:
        validate_safe15_certificate_safety_file(path)
    except SafetyDocumentCoverageError as exc:
        return exc.to_diagnostic()
    return None


def require_fixture(record: dict[str, Any], entry: dict[str, Any], source: str) -> Path:
    fixture_value = entry.get("fixture")
    if not fixture_value:
        raise_record_error(record, "SAFE16-FIXTURE", "coverage entry lacks fixture", source, "fixture")
    fixture = path_from_root(fixture_value)
    if not fixture.exists():
        raise_record_error(record, "SAFE16-FIXTURE", f"fixture does not exist: {fixture_value}", source, "fixture")
    return fixture


def path_from_root(value: str) -> Path:
    path = Path(value)
    return path if path.is_absolute() else ROOT / path


def rel(path: Path) -> str:
    try:
        return str(path.resolve().relative_to(ROOT))
    except ValueError:
        return str(path)


def load_json(path: Path) -> dict[str, Any]:
    return json.loads(path.read_text(encoding="utf-8"))


def artifact_hash(value: Any) -> str:
    data = json.dumps(value, sort_keys=True, separators=(",", ":"))
    return "sha256:" + hashlib.sha256(data.encode("utf-8")).hexdigest()


def raise_record_error(record: dict[str, Any], code: str, message: str, source: str, missing_fact: str) -> None:
    raise SafetyDocumentCoverageError(
        code,
        message,
        document=record.get("document"),
        task_id=record.get("task_id"),
        source=source,
        missing_fact=missing_fact,
        remediation="Update the document coverage manifest, fixture, or owning analyzer evidence.",
    )


def raise_error(code: str, message: str, *, source: str, missing_fact: str, remediation: str) -> None:
    raise SafetyDocumentCoverageError(
        code,
        message,
        source=source,
        missing_fact=missing_fact,
        remediation=remediation,
    )


def raise_safe12(code: str, message: str, manifest: dict[str, Any], source: str, missing_fact: str) -> None:
    raise SafetyDocumentCoverageError(
        code,
        message,
        document="SAFE12",
        source=source,
        missing_fact=missing_fact,
        remediation="Preserve macro safety metadata, authority, hygiene, phase, taint, and source provenance before accepting expansion.",
        span=manifest.get("generated_form_span") or manifest.get("call_site_span") or {"source": source},
    )


def raise_safe13(code: str, message: str, manifest: dict[str, Any], source: str, missing_fact: str) -> None:
    raise SafetyDocumentCoverageError(
        code,
        message,
        document="SAFE13",
        source=source,
        missing_fact=missing_fact,
        remediation="Declare AI effects, validate schemas, preserve prompt roles, enforce capabilities, and record replay or review evidence.",
        span=manifest.get("source_span") or {"source": source},
    )


def raise_safe15(code: str, message: str, manifest: dict[str, Any], source: str, missing_fact: str) -> None:
    raise SafetyDocumentCoverageError(
        code,
        message,
        document="SAFE15",
        source=source,
        missing_fact=missing_fact,
        remediation="Attach accepted proof and certificate evidence, trust metadata, invalidation rules, and backend preservation before using the claim.",
        span=manifest.get("source_span") or {"source": source},
    )
