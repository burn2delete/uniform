"""Hostile mutation tests for the strict W1 Linux interface validator."""

from __future__ import annotations

import base64
import copy
import hashlib
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest


ROOT = Path(__file__).resolve().parents[2]
TOOLS = ROOT / "tools"
sys.path.insert(0, str(TOOLS))
import validate_w1_executable_carrier_interface as validator  # noqa: E402


def digest(value: object) -> str:
    return validator.canonical_sha256(value)


def elf_blob(elf_type: int, payload: bytes) -> dict[str, object]:
    header = bytearray(64)
    header[:7] = b"\x7fELF\x02\x01\x01"
    header[16:18] = elf_type.to_bytes(2, "little")
    header[18:20] = (62).to_bytes(2, "little")
    raw = bytes(header) + payload
    return {
        "bytes_base64": base64.b64encode(raw).decode("ascii"),
        "byte_count": len(raw),
        "content_sha256": "sha256:" + hashlib.sha256(raw).hexdigest(),
        "format": "ELF", "architecture": "x86_64", "abi": "SysV",
    }


def transcript(name: str, exit_code: int, diagnostics: list[str]) -> dict[str, object]:
    transcript_path = "tools/tests/test_validate_w1_executable_carrier_interface.py"
    transcript_bytes = (ROOT / transcript_path).read_bytes()
    payload = {
        "path": transcript_path,
        "content_sha256": "sha256:" + hashlib.sha256(transcript_bytes).hexdigest(),
        "exit_code": exit_code,
        "diagnostic_ids": sorted(diagnostics),
    }
    return validator.section(payload)


class Fixture:
    def __init__(
        self, *, final: bool = False,
        source_preimage_override: str | None = None,
        semantic_preimage_override: str | None = None,
    ) -> None:
        source_path = "tools/validate_w1_executable_carrier_interface.py"
        source_bytes = (ROOT / source_path).read_bytes()
        source_hash = "sha256:" + hashlib.sha256(source_bytes).hexdigest()
        source_preimage = (
            '{:content-sha256 "' + source_hash
            + '", :kind :gravity/w1-source-unit, :path "' + source_path + '"}'
        )
        if source_preimage_override is not None:
            source_preimage = source_preimage_override
        source_id = validator.gravity_mir_digest_preimage(source_preimage)
        semantic_preimage = (
            "{:kind :gravity/c13-bounded-identity-optimized-mir, "
            ':record {:mir [:const 42], :source-id "' + source_id + '"}}'
        )
        if semantic_preimage_override is not None:
            semantic_preimage = semantic_preimage_override
        semantic_id = validator.gravity_mir_digest_preimage(semantic_preimage)
        safety = {"mode": ":safe", "outcomes": {"proven_safe": 3, "runtime_checked": 1, "rejected": 0, "unsafe_island": 0}}
        content = {
            "mir": ["const", 42], "source_id": source_id,
            "semantic_id": semantic_id,
            "semantic_id_algorithm": validator.GRAVITY_MIR_DIGEST_ALGORITHM,
            "semantic_id_preimage_edn": semantic_preimage,
        }
        content_hash = digest(content)
        carrier_payload = {
            "artifact_id": validator.artifact_id(validator.C13_KIND, [source_id], content_hash),
            "kind": validator.C13_KIND, "schema_version": 1, "status": ":accepted" if final else ":development-observed",
            "profile": validator.PROFILE, "target": validator.TARGET,
            "source_id": source_id, "semantic_id": semantic_id,
            "effects": [":pure"], "capabilities": [], "safety": safety,
            "content": content, "content_hash": content_hash,
        }
        carrier = validator.section(carrier_payload)
        target = dict(validator.TARGET_RECORD)
        target["fingerprint"] = validator.target_fingerprint(target)
        record = {
            "source": carrier["artifact_id"], "target": target,
            "target_fingerprint_algorithm": validator.TARGET_FINGERPRINT_ALGORITHM,
        }
        record_hash = digest(record)
        lowering = validator.section({
            "artifact_id": validator.artifact_id(validator.C14_KIND, [carrier["artifact_id"]], record_hash),
            "kind": validator.C14_KIND, "schema_version": 1, "status": ":accepted" if final else ":development-observed",
            "profile": validator.PROFILE, "target": validator.TARGET, "backend": ":llvm",
            "input_artifact_id": carrier["artifact_id"], "record": record, "content_hash": record_hash,
        })
        packet = {"lowering": lowering["artifact_id"], "abi": "SysV", "target": validator.TARGET}
        packet_hash = digest(packet)
        backend_input = validator.section({
            "artifact_id": validator.artifact_id(validator.B1_KIND, [carrier["artifact_id"], lowering["artifact_id"]], packet_hash),
            "kind": validator.B1_KIND, "schema_version": 1, "status": ":accepted" if final else ":development-observed",
            "profile": validator.PROFILE, "target": validator.TARGET,
            "input_artifact_id": carrier["artifact_id"], "lowering_artifact_id": lowering["artifact_id"],
            "packet": packet, "content_hash": packet_hash,
        })
        module = {"llvm_ir": 'target triple = "x86_64-unknown-linux-gnu"\ndefine i32 @main() { ret i32 42 }', "input": backend_input["artifact_id"]}
        module_hash = digest(module)
        backend = validator.section({
            "artifact_id": validator.artifact_id(validator.B3_KIND, [backend_input["artifact_id"]], module_hash),
            "kind": validator.B3_KIND, "schema_version": 1, "status": ":accepted" if final else ":development-observed",
            "profile": validator.PROFILE, "target": validator.TARGET,
            "input_artifact_id": backend_input["artifact_id"], "llvm_version": validator.LLVM_VERSION,
            "module": module, "content_hash": module_hash,
        })
        obj, executable = elf_blob(1, b"object-w1"), elf_blob(3, b"executable-w1")
        emission = validator.section({
            "artifact_id": validator.artifact_id(validator.EMISSION_KIND, [backend["artifact_id"]], executable["content_sha256"]),
            "kind": validator.EMISSION_KIND, "schema_version": 1, "status": ":accepted" if final else ":development-observed",
            "profile": validator.PROFILE, "target": validator.TARGET,
            "backend_artifact_id": backend["artifact_id"], "object": obj, "executable": executable,
        })
        positive = transcript("w1-positive", 0, ["W1-ACCEPTED"])
        rejected_ids = ["W1-AUTHORITY", "W1-BYTES", "W1-PROVENANCE", "W1-REVIEW", "W1-STAGE9", "W1-TARGET", "W1-VERIFIER"]
        negative = transcript("w1-negative", 1, rejected_ids)
        diagnostics = validator.section({"accepted": ["W1-ACCEPTED"], "rejected": rejected_ids})
        lineage = {
            "source_id": source_id, "semantic_id": semantic_id,
            "c13_artifact_id": carrier["artifact_id"], "c14_artifact_id": lowering["artifact_id"],
            "b1_artifact_id": backend_input["artifact_id"], "b3_artifact_id": backend["artifact_id"],
            "emission_artifact_id": emission["artifact_id"],
        }
        edge_values = [
            (source_id, carrier["artifact_id"], "source-to-c13"),
            (carrier["artifact_id"], lowering["artifact_id"], "c13-to-c14"),
            (lowering["artifact_id"], backend_input["artifact_id"], "c14-to-b1"),
            (backend_input["artifact_id"], backend["artifact_id"], "b1-to-b3"),
            (backend["artifact_id"], emission["artifact_id"], "b3-to-elf"),
        ]
        edges = [{"from_id": a, "to_id": b, "kind": kind, "target": validator.TARGET} for a, b, kind in edge_values]
        provenance = validator.section({"lineage": lineage, "edges": edges})
        replay = {"artifact_id": digest({"replay": final}), "content_hash": digest({"replay-content": final}), "transcript_id": positive["id"], "native": final}
        verification = validator.section({
            "predicate": validator.VERIFIER_PREDICATE, "predicate_version": 1,
            "command": validator.INTEGRATION_VERIFIER_COMMAND if not final else None,
            "status": ":passed" if final else ":development-observed", "replay_contract_frozen": False,
            "replay": replay, "diagnostic_ids": ["W1-ACCEPTED"],
        })
        hashes = {name: digest({"tool": name}) for name in validator.LLVM_TOOLS}
        if final:
            environment = {"execution": ":native-linux", "os": "linux", "arch": "x86_64", "abi": "SysV", "llvm_version": validator.LLVM_VERSION, "tools": hashes, "kernel_id": digest("kernel"), "machine_id": digest("machine")}
        else:
            environment = {
                "execution": ":development-emulated", "image": validator.PINNED_IMAGE,
                "image_platform": validator.IMAGE_PLATFORM, "pull_policy": validator.PULL_POLICY,
                "llvm_version": validator.LLVM_VERSION, "tools": hashes,
                "docker": {"engine_version": "28.3.2", "engine_id": digest("docker-engine"), "host_id": digest("docker-host"), "host_os": "recorded-host-os", "host_arch": "recorded-host-arch"},
                "emulation": {"kind": "qemu-binfmt", "qemu_id": digest("qemu"), "binfmt_id": digest("binfmt")},
            }
        authority = validator.section({
            "development_only": not final, "emulated": not final, "authoritative": final,
            "native_replay": final, "public_route": False, "clojure_seed_boundary": True,
            "self_hosted": False, "release": False, "inventory": validator.AUTHORITY_INVENTORY,
        })
        review_path = "tools/tests/test_validate_w1_executable_carrier_interface.py"
        review_bytes = (ROOT / review_path).read_bytes()
        review_hash = "sha256:" + hashlib.sha256(review_bytes).hexdigest()
        review_id = validator.artifact_id(validator.REVIEW_KIND, [review_path], review_hash)
        review = validator.section({
            "status": ":accepted" if final else ":pending",
            "reviewer_class": ":independent-sol" if final else ":none",
            "reviewed_commit": "a" * 40 if final else None,
            "reviewed_tree": "b" * 40 if final else None,
            "review_artifact_id": review_id if final else None,
            "review_path": review_path if final else None,
            "review_kind": validator.REVIEW_KIND if final else None,
            "review_schema": validator.REVIEW_SCHEMA if final else None,
            "review_content_sha256": review_hash if final else None,
            "independent": final,
        })
        fixture_path = "tools/tests/test_validate_w1_executable_carrier_interface.py"
        fixture_hash = "sha256:" + hashlib.sha256((ROOT / fixture_path).read_bytes()).hexdigest()
        inputs = validator.section({
            "source": {
                "path": source_path, "content_sha256": source_hash,
                "source_id": source_id,
                "source_id_algorithm": validator.GRAVITY_MIR_DIGEST_ALGORITHM,
                "source_id_preimage_edn": source_preimage,
            },
            "fixtures": [{"path": fixture_path, "content_sha256": fixture_hash}],
        })
        document = {
            "artifact_kind": validator.FINAL_ARTIFACT_KIND if final else validator.DEVELOPMENT_ARTIFACT_KIND,
            "document_schema": validator.FINAL_DOCUMENT_SCHEMA if final else validator.DEVELOPMENT_DOCUMENT_SCHEMA,
            "schema_version": 1, "mode": ":authoritative-native" if final else ":development-emulated",
            "interface_kind": validator.INTERFACE_KIND, "interface_schema": validator.INTERFACE_SCHEMA,
            "evidence_identity": {"self_id": "sha256:" + "1" * 64, "canonicalization": validator.CANONICALIZATION},
            "producer": {"repository": "gravity", "commit": "a" * 40, "tree": "b" * 40},
            "inputs": inputs,
            "carrier": carrier, "lowering": lowering, "backend_input": backend_input,
            "backend": backend, "emission": emission,
            "transcripts": validator.section({"positive": [positive], "negative": [negative]}),
            "diagnostics": diagnostics, "provenance": provenance, "verification": verification,
            "environment": environment, "authority": authority, "review": review,
        }
        bindings = {
            "carrier-artifact-id": backend["artifact_id"], "carrier-content-hash": backend["content_hash"],
            "carrier-schema": 1, "source-id": source_id, "semantic-id": semantic_id,
            "profile": validator.PROFILE, "target": validator.TARGET,
            "effects": [":pure"], "capabilities": [], "safety": safety,
            "accepted-diagnostic-ids": ["W1-ACCEPTED"], "rejected-diagnostic-ids": rejected_ids,
            "provenance-edges": {"artifact-kind": validator.B3_KIND, "schema-version": validator.B3_SCHEMA_VERSION},
        }
        claims = {"public-route?": False, "clojure-seed-boundary?": True, "self-hosted?": False, "release?": False}
        if final:
            document["consumer_handoff"] = {
                "contract": validator.CONSUMER_CONTRACT, "contract-version": 1, "workstream": ":w1",
                "interface-kind": validator.INTERFACE_KIND, "interface-schema": validator.INTERFACE_SCHEMA,
                "artifact-id": "sha256:" + "1" * 64, "producer-commit": "a" * 40, "producer-tree": "b" * 40,
                "verifier": {"predicate": validator.VERIFIER_PREDICATE, "predicate-version": 1, "replay-artifact-id": replay["artifact_id"], "replay-content-hash": replay["content_hash"], "status": ":passed"},
                "review": {"status": ":accepted", "reviewer-class": ":independent-sol", "reviewed-commit": "a" * 40, "review-artifact-id": review_id},
                "bindings": bindings, "claims": claims,
            }
        else:
            document["consumer_handoff_candidate"] = {
                "contract": validator.CONSUMER_CONTRACT, "contract-version": 1, "workstream": ":w1",
                "interface-kind": validator.INTERFACE_KIND, "interface-schema": validator.INTERFACE_SCHEMA,
                "status": ":development-only", "bindings": bindings, "claims": claims,
            }
        self.document = document
        self.rehash()

    def rehash(self) -> None:
        identity = validator.canonical_evidence_sha256(self.document)
        self.document["evidence_identity"]["self_id"] = identity
        if "consumer_handoff" in self.document:
            self.document["consumer_handoff"]["artifact-id"] = identity


class Tests(unittest.TestCase):
    def assert_rejected(self, mutate, needle: str, *, final: bool = False) -> None:
        fixture = Fixture(final=final)
        mutate(fixture.document)
        errors = validator.validate_artifact(fixture.document)
        self.assertTrue(any(needle in error for error in errors), errors)

    def test_development_candidate_and_final_shape_are_fail_closed(self) -> None:
        dev, final = Fixture(), Fixture(final=True)
        dev_errors = validator.validate_artifact(dev.document)
        self.assertTrue(any(validator.SOURCE_ID_PREIMAGE_UNRESOLVED in error for error in dev_errors), dev_errors)
        self.assertTrue(any(validator.C13_SEMANTIC_PREIMAGE_UNRESOLVED in error for error in dev_errors), dev_errors)
        final_errors = validator.validate_artifact(final.document, require_authoritative=True)
        self.assertTrue(any(validator.REPLAY_CONTRACT_UNFROZEN in error for error in final_errors), final_errors)
        self.assertNotIn("consumer_handoff", dev.document)
        self.assertNotIn("consumer_handoff_candidate", final.document)
        bindings = dev.document["consumer_handoff_candidate"]["bindings"]
        self.assertEqual(bindings["carrier-artifact-id"], dev.document["backend"]["artifact_id"])
        self.assertEqual(bindings["carrier-content-hash"], dev.document["backend"]["content_hash"])
        self.assertNotEqual(bindings["carrier-artifact-id"], dev.document["emission"]["artifact_id"])
        self.assertNotEqual(bindings["carrier-content-hash"], dev.document["emission"]["executable"]["content_sha256"])
        self.assertEqual(dev.document["artifact_kind"], "gravity/w1-executable-carrier-linux-amd64-emulated-development-evidence")
        self.assertEqual(dev.document["document_schema"], "gravity.w1.executable-carrier-development-evidence/v1")
        self.assertNotIn(":passed", validator.canonical_json(dev.document).decode("utf-8"))
        self.assertNotIn('"status":":accepted"', validator.canonical_json(dev.document).decode("utf-8"))

    def test_require_authoritative_rejects_development(self) -> None:
        errors = validator.validate_artifact(Fixture().document, require_authoritative=True)
        self.assertTrue(any("--require-authoritative" in error for error in errors), errors)

    def test_exact_linux_identifiers_and_no_c_gate_b_residue(self) -> None:
        for field, bad in [
            ("interface_kind", ":w1/executable-c13-c14-b1-c-backend"),
        ]:
            self.assert_rejected(lambda d, f=field, b=bad: d.__setitem__(f, b), field)
        self.assert_rejected(lambda d: d["lowering"].__setitem__("kind", ":gravity/c14-bounded-c-lowering-record"), "lowering.kind")
        self.assert_rejected(lambda d: d["backend"].__setitem__("kind", ":gravity/b2-hosted-c17-gate-b"), "backend.kind")
        self.assert_rejected(lambda d: d["verification"].__setitem__("predicate", "gravity.bootstrap/p15-s23-stage2-b2-c17-gate-b-verify!"), "verification.predicate")

    def test_target_alias_bare_llvm_and_cross_target_provenance_rejected(self) -> None:
        self.assert_rejected(lambda d: d["carrier"].__setitem__("target", ":llvm-x86-64-linux"), "carrier.target")
        self.assert_rejected(lambda d: d["carrier"].__setitem__("target", ":llvm"), "carrier.target")
        self.assert_rejected(lambda d: d["provenance"]["edges"][2].__setitem__("target", ":llvm-aarch64-darwin"), "provenance.edges[2].target")

    def test_stage9_narrative_and_unknown_keys_fail_closed(self) -> None:
        self.assert_rejected(lambda d: d["verification"].__setitem__("command", ["stage9", "status-only"]), "forbidden cross-target/evidence")
        self.assert_rejected(lambda d: d["review"].__setitem__("narrative", "looks good"), "unknown keys")
        self.assert_rejected(lambda d: d.__setitem__("consumer_handoff", {}), "unknown keys")

    def test_emitted_bytes_hash_length_and_format_are_recomputed(self) -> None:
        self.assert_rejected(lambda d: d["emission"]["executable"].__setitem__("content_sha256", digest("lie")), "does not match emitted bytes")
        self.assert_rejected(lambda d: d["emission"]["object"].__setitem__("byte_count", 999), "does not match emitted bytes")
        self.assert_rejected(lambda d: d["emission"]["executable"].__setitem__("format", "Mach-O"), "emission.executable.format")
        self.assert_rejected(lambda d: d["emission"]["object"].__setitem__("bytes_base64", base64.b64encode(b"not-elf").decode("ascii")), "ELF bytes")
        self.assert_rejected(lambda d: d["emission"]["executable"].__setitem__("bytes_base64", elf_blob(1, b"wrong-type")["bytes_base64"]), "ELF type")

    def test_transcript_paths_and_raw_bytes_are_recomputed(self) -> None:
        self.assert_rejected(lambda d: d["transcripts"]["positive"][0].__setitem__("path", "../outside.log"), "repository-relative")
        self.assert_rejected(lambda d: d["transcripts"]["positive"][0].__setitem__("path", "target/validation/missing.log"), "cannot read exact repository file")
        self.assert_rejected(lambda d: d["transcripts"]["positive"][0].__setitem__("content_sha256", digest("not-the-file")), "does not match exact file bytes")

    def test_hash_and_lineage_tampering_rejected(self) -> None:
        self.assert_rejected(lambda d: d["carrier"]["content"].__setitem__("mir", ["const", 99]), "carrier.content_hash")
        self.assert_rejected(lambda d: d["backend_input"].__setitem__("lowering_artifact_id", digest("wrong")), "backend_input.lowering_artifact_id")
        self.assert_rejected(lambda d: d["provenance"]["lineage"].__setitem__("b3_artifact_id", digest("wrong")), "provenance.lineage")
        self.assert_rejected(lambda d: d["consumer_handoff_candidate"]["bindings"]["provenance-edges"].__setitem__("artifact-kind", ":gravity/other-artifact"), "consumer_handoff_candidate.bindings")
        self.assert_rejected(lambda d: d["consumer_handoff_candidate"]["bindings"]["provenance-edges"].__setitem__("schema-version", 2), "consumer_handoff_candidate.bindings")

    def test_source_and_c13_semantic_preimages_are_recomputed(self) -> None:
        self.assert_rejected(
            lambda d: d["inputs"]["source"].__setitem__("source_id", digest("arbitrary-source")),
            "production preimage mismatch",
        )
        self.assert_rejected(
            lambda d: d["inputs"]["source"].__setitem__(
                "source_id_preimage_edn", "{:kind :gravity/changed-source}"
            ),
            "production preimage mismatch",
        )
        self.assert_rejected(
            lambda d: d["inputs"]["source"].__setitem__("source_id_algorithm", "sha256-json"),
            "inputs.source.source_id_algorithm",
        )
        self.assert_rejected(
            lambda d: d["carrier"].__setitem__("semantic_id", digest("arbitrary-semantic")),
            "carrier.content.semantic_id",
        )
        self.assert_rejected(
            lambda d: d["carrier"]["content"].__setitem__(
                "semantic_id_preimage_edn",
                "{:kind :gravity/c13-bounded-identity-optimized-mir, :record {}}",
            ),
            "production C13 preimage mismatch",
        )
        self.assert_rejected(
            lambda d: d["carrier"]["content"].__setitem__(
                "semantic_id_algorithm", "gravity-interface-local-id/v1"
            ),
            "carrier.content.semantic_id_algorithm",
        )

    def test_consistent_unrelated_preimages_fail_closed_after_full_rehash(self) -> None:
        fixture = Fixture(
            source_preimage_override="{}",
            semantic_preimage_override="{}",
        )
        errors = validator.validate_artifact(fixture.document)
        self.assertTrue(
            any(validator.SOURCE_ID_PREIMAGE_UNRESOLVED in error for error in errors),
            errors,
        )
        self.assertTrue(
            any(validator.C13_SEMANTIC_PREIMAGE_UNRESOLVED in error for error in errors),
            errors,
        )
        self.assertFalse(any("canonical mismatch" in error for error in errors), errors)

    def test_development_environment_is_exact_and_hashes_full(self) -> None:
        self.assert_rejected(lambda d: d["environment"].__setitem__("image", "silkeh/clang:20"), "environment.image")
        self.assert_rejected(lambda d: d["environment"].__setitem__("pull_policy", "--pull=always"), "environment.pull_policy")
        self.assert_rejected(lambda d: d["environment"]["tools"].__setitem__("clang", "sha256:abc"), "environment.tools.clang")
        self.assert_rejected(lambda d: d["environment"]["tools"].pop("llvm-readobj"), "missing keys")
        self.assert_rejected(lambda d: d["environment"]["tools"].__setitem__("gcc", digest("gcc")), "unknown keys")

    def test_status_only_missing_transcripts_diagnostics_and_review_rejected(self) -> None:
        self.assert_rejected(lambda d: d["verification"].__setitem__("command", []), "verification.command")
        self.assert_rejected(lambda d: d["transcripts"].__setitem__("negative", []), "transcripts.negative")
        self.assert_rejected(lambda d: d["diagnostics"].__setitem__("rejected", []), "diagnostics.rejected")
        self.assert_rejected(lambda d: d["review"].__setitem__("status", ":accepted"), "review.status")

    def test_malformed_structures_fail_closed_without_crashing(self) -> None:
        for key in ("carrier", "transcripts", "producer", "evidence_identity", "verification", "review"):
            document = Fixture().document
            document[key] = []
            errors = validator.validate_artifact(document)
            self.assertTrue(errors, key)
        document = Fixture().document
        document["carrier"]["content"]["mir"] = float("nan")
        self.assertTrue(validator.validate_artifact(document))

    def test_final_requires_native_replay_independent_review_and_exact_handoff(self) -> None:
        self.assert_rejected(lambda d: d["verification"]["replay"].__setitem__("native", False), "verification.replay.native", final=True)
        self.assert_rejected(lambda d: d["review"].__setitem__("reviewer_class", ":author"), "review.reviewer_class", final=True)
        self.assert_rejected(lambda d: d["review"].__setitem__("review_artifact_id", d["emission"]["artifact_id"]), "distinct later review artifact", final=True)
        self.assert_rejected(lambda d: d["consumer_handoff"]["verifier"].__setitem__("status", ":observed"), "consumer_handoff.verifier", final=True)
        self.assert_rejected(lambda d: d["consumer_handoff"]["bindings"].__setitem__("target", ":llvm"), "consumer_handoff.bindings", final=True)
        self.assert_rejected(lambda d: d["consumer_handoff"]["bindings"].__setitem__("carrier-artifact-id", d["emission"]["artifact_id"]), "consumer_handoff.bindings", final=True)
        self.assert_rejected(lambda d: d["consumer_handoff"]["bindings"]["provenance-edges"].__setitem__("schema-version", 2), "provenance-edges", final=True)
        self.assert_rejected(lambda d: d["consumer_handoff"]["claims"].__setitem__("release?", True), "consumer_handoff.claims", final=True)

    def test_final_handoff_serialization_is_exact_kebab_case(self) -> None:
        final = Fixture(final=True).document["consumer_handoff"]
        self.assertEqual(set(final), validator.HANDOFF_KEYS)
        self.assertEqual(set(final["bindings"]), validator.HANDOFF_BINDING_KEYS)
        self.assertEqual(set(final["claims"]), validator.HANDOFF_CLAIM_KEYS)
        self.assert_rejected(lambda d: d["consumer_handoff"].__setitem__("contract_version", d["consumer_handoff"].pop("contract-version")), "unknown keys", final=True)

    def test_nested_kebab_allowlist_duplicate_json_and_development_outer_key(self) -> None:
        self.assert_rejected(
            lambda d: d["consumer_handoff_candidate"].__setitem__("carrier_artifact_id", digest("wrong")),
            "unknown keys",
        )
        self.assert_rejected(lambda d: d.__setitem__("consumer-handoff-candidate", {}), "unknown keys")
        fixture = Fixture(final=True)
        encoded = json.dumps(fixture.document, separators=(",", ":"))
        needle = '"contract":":gravity/p15-public-native-admission","contract-version"'
        duplicate = encoded.replace(needle, '"contract":":gravity/p15-public-native-admission","contract":":gravity/p15-public-native-admission","contract-version"', 1)
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "duplicate.json"
            path.write_text(duplicate, encoding="utf-8")
            result = subprocess.run(
                [sys.executable, str(TOOLS / "validate_w1_executable_carrier_interface.py"), str(path)],
                cwd=ROOT, text=True, capture_output=True, check=False,
            )
            self.assertNotEqual(result.returncode, 0)
            self.assertIn("duplicate JSON object key", result.stdout)

    def test_source_fixture_and_non_circular_hashes_are_recomputed(self) -> None:
        self.assert_rejected(lambda d: d["inputs"]["source"].__setitem__("content_sha256", digest("source-lie")), "does not match exact file bytes")
        self.assert_rejected(lambda d: d["inputs"]["fixtures"][0].__setitem__("path", "../outside.log"), "repository-relative")
        self.assert_rejected(lambda d: d["verification"]["replay"].__setitem__("kind", "gravity/provisional-replay"), "unknown keys")
        self.assert_rejected(lambda d: d["backend"]["module"].__setitem__("payload-containing-commit", "a" * 40), "payload may contain reviewed A only")

    def test_replay_contract_is_explicitly_unfrozen_and_blocks_authority(self) -> None:
        development = Fixture().document
        self.assertIs(development["verification"]["replay_contract_frozen"], False)
        self.assertEqual(development["verification"]["command"], validator.INTEGRATION_VERIFIER_COMMAND)
        self.assertIsNone(Fixture(final=True).document["verification"]["command"])
        development_errors = validator.validate_artifact(development)
        self.assertTrue(any(validator.SOURCE_ID_PREIMAGE_UNRESOLVED in error for error in development_errors), development_errors)
        self.assertTrue(any(validator.C13_SEMANTIC_PREIMAGE_UNRESOLVED in error for error in development_errors), development_errors)
        self.assert_rejected(lambda d: d["verification"].__setitem__("command", ["gravity", "verify-w1-linux"]), "integration-only SH17 argv")
        self.assert_rejected(lambda d: d["verification"].__setitem__("replay_contract_frozen", True), validator.REPLAY_CONTRACT_UNFROZEN)
        authoritative_errors = validator.validate_artifact(Fixture(final=True).document)
        self.assertTrue(any(validator.REPLAY_CONTRACT_UNFROZEN in error for error in authoritative_errors), authoritative_errors)

    def test_exact_target_policy_record_packet_and_module_lineage(self) -> None:
        expected = "sha256:25e57788c750cb3d184fe54c68fc9e2e69c807bb5c1510e12160ed53a89df593"
        target = dict(validator.TARGET_RECORD)
        self.assertEqual(validator.target_fingerprint(target), expected)
        self.assert_rejected(lambda d: d["lowering"]["record"]["target"].__setitem__("triple", "x86_64-unknown-linux-gnu-invalid"), "lowering.record.target.triple")
        self.assert_rejected(lambda d: d["lowering"]["record"]["target"].__setitem__("data_layout", "e-m:e-p270:32:32"), "lowering.record.target.data_layout")
        self.assert_rejected(lambda d: d["lowering"]["record"]["target"].__setitem__("fingerprint", digest("forged-target")), "canonical Gravity target preimage mismatch")
        self.assert_rejected(lambda d: d["lowering"]["record"].__setitem__("target_fingerprint_algorithm", "constant-comparison/v1"), "lowering.record.target_fingerprint_algorithm")
        self.assert_rejected(lambda d: d["backend_input"]["packet"].__setitem__("target", ":llvm"), "backend_input.packet.target")
        self.assert_rejected(lambda d: d["backend_input"]["packet"].__setitem__("abi", "aarch64"), "backend_input.packet.abi")
        self.assert_rejected(lambda d: d["backend"]["module"].__setitem__("input", digest("wrong")), "backend.module.input")
        self.assert_rejected(lambda d: d["carrier"]["safety"]["outcomes"].__setitem__("unsafe_island", 1), "unsafe_island")

    def test_authority_inventory_and_review_a_tree_gate(self) -> None:
        self.assert_rejected(lambda d: d["authority"]["inventory"].__setitem__("jvm", ":public"), "authority.inventory")
        self.assert_rejected(lambda d: d["review"].__setitem__("reviewed_tree", "c" * 40), "review.reviewed_tree", final=True)
        self.assert_rejected(lambda d: d["review"].__setitem__("review_content_sha256", digest("review-lie")), "does not match exact file bytes", final=True)

    def test_self_identity_and_cli(self) -> None:
        fixture = Fixture()
        fixture.document["authority"]["release"] = True
        self.assertTrue(any("evidence_identity.self_id" in e for e in validator.validate_artifact(fixture.document)))
        fixture = Fixture()
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "w1.json"
            path.write_text(json.dumps(fixture.document), encoding="utf-8")
            result = subprocess.run([sys.executable, str(TOOLS / "validate_w1_executable_carrier_interface.py"), str(path)], cwd=ROOT, text=True, capture_output=True, check=False)
            self.assertNotEqual(result.returncode, 0)
            self.assertIn(validator.SOURCE_ID_PREIMAGE_UNRESOLVED, result.stdout)
            self.assertIn(validator.C13_SEMANTIC_PREIMAGE_UNRESOLVED, result.stdout)
            result = subprocess.run([sys.executable, str(TOOLS / "validate_w1_executable_carrier_interface.py"), "--require-authoritative", "--artifact", str(path)], cwd=ROOT, text=True, capture_output=True, check=False)
            self.assertNotEqual(result.returncode, 0)
            self.assertIn("--require-authoritative", result.stdout)


if __name__ == "__main__":
    unittest.main()
