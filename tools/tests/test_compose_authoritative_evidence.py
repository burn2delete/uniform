#!/usr/bin/env python3
"""Focused, Python-only tests for exact authoritative-evidence composition."""

from __future__ import annotations

import copy
import hashlib
import json
import os
from pathlib import Path
import platform
import shutil
import subprocess
import sys
import tempfile
import unittest
from unittest import mock


ROOT = Path(__file__).resolve().parents[2]
TOOLS = ROOT / "tools"
if str(TOOLS) not in sys.path:
    sys.path.insert(0, str(TOOLS))

import compose_authoritative_evidence as composer


def canonical(value: object) -> bytes:
    return json.dumps(
        value, ensure_ascii=True, allow_nan=False, sort_keys=True, separators=(",", ":")
    ).encode("utf-8")


class AuthoritativeEvidenceTests(unittest.TestCase):
    maxDiff = None

    def setUp(self) -> None:
        self.temporary = tempfile.TemporaryDirectory()
        self.root = Path(self.temporary.name).resolve()
        for directory in (
            "contracts",
            "tools",
            "docs/reviews",
            "target/validation/sh07/modules",
            "bootstrap/clojure/test/gravity/self_hosting",
            "bootstrap/gravity/p15_s23",
            "bootstrap/gravity/src/gravity",
            "bootstrap/gravity/src/gravity/backend",
        ):
            (self.root / directory).mkdir(parents=True, exist_ok=True)
        shutil.copy2(
            ROOT / "contracts/authoritative-evidence.json",
            self.root / "contracts/authoritative-evidence.json",
        )
        shutil.copy2(
            ROOT / "contracts/project-structure.json",
            self.root / "contracts/project-structure.json",
        )
        shutil.copy2(
            ROOT / "contracts/python-tooling.json",
            self.root / "contracts/python-tooling.json",
        )
        self.verification = {
            "schema_version": 1,
            "name": "authority-test",
            "lanes": {
                "preflight": {}, "focused": {}, "heavy-candidate": {},
            },
            "checks": [
                {
                    "id": "base", "lane": "focused", "cost": "cheap",
                    "lock": None, "exclusive": False, "authority": "none",
                    "daemonization": "forbidden", "fresh": True,
                    "command": ["tool", "base"], "inputs": ["input.txt"],
                    "depends_on": [],
                },
                {
                    "id": "leaf", "lane": "focused", "cost": "cheap",
                    "lock": None, "exclusive": False, "authority": "none",
                    "daemonization": "forbidden", "fresh": True,
                    "command": ["tool", "leaf"], "inputs": ["input.txt"],
                    "depends_on": ["base"],
                },
            ],
        }
        self.write_json("tools/development_verification_manifest.json", self.verification)
        self.module = "fixture"
        self.source_path = "bootstrap/gravity/src/gravity/backend/b4_wasm_backend_design.gravity"
        self.write_bytes(self.source_path, b"(ns fixture)\n")
        required = [
            "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn",
            "bootstrap/clojure/test/gravity/self_hosting/sh07_authoritative_runner.clj",
            "tools/run_sh07_authoritative_modules.py",
            "bootstrap/gravity/p15_s23/compiler.gravity",
            "bootstrap/gravity/p15_s23/emitter.gravity",
            "bootstrap/gravity/src/gravity/macro.gravity",
            "bootstrap/gravity/src/gravity/resolution.gravity",
            "bootstrap/gravity/src/gravity/checked_core.gravity",
        ]
        for path in required:
            destination = self.root / path
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(ROOT / path, destination)
        self.checkpoint = self.checkpoint_value(required)
        self.write_json("target/validation/sh07/manifest.json", self.checkpoint)
        self.development_path = "target/validation/development-composition.json"
        development = {
            "schema_version": 1,
            "kind": "development-verification-composition",
            "authoritative": False,
            "status": "complete",
            "checks": [],
        }
        development["composition_sha256"] = hashlib.sha256(canonical(development)).hexdigest()
        self.write_json(self.development_path, development)
        self.c2_path = "target/validation/c2-witness.json"
        identifier = "sha256:" + "a" * 64
        c2 = {
            "schema_version": 1,
            "kind": "gravity/c2-cache-witness",
            "authoritative": False,
            "aggregate_authoritative": False,
            "release_authority": False,
            "proof_authority": False,
            "semantic_key_id": identifier,
            "storage_key_id": "sha256:" + "b" * 64,
            "artifact_id": "sha256:" + "c" * 64,
            "boundary_projection_id": "sha256:" + "d" * 64,
            "observations": [
                {"status": "stored", "reader_executed": True, "artifact_reused": False},
                {"status": "hit", "reader_executed": False, "artifact_reused": True},
            ],
        }
        c2["witness_sha256"] = composer._semantic_sha256(
            c2, "gravity.authoritative-evidence/c2-cache-witness/v1"
        )
        self.write_json(self.c2_path, c2)

    def tearDown(self) -> None:
        self.temporary.cleanup()

    def write_bytes(self, relative: str, data: bytes) -> None:
        path = self.root / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(data)

    def write_json(self, relative: str, value: object) -> None:
        self.write_bytes(relative, json.dumps(value, indent=2, sort_keys=True).encode() + b"\n")

    def entry(self, relative: str) -> dict[str, object]:
        data = (self.root / relative).read_bytes()
        return {
            "path": relative,
            "size": len(data),
            "sha256": "sha256:" + hashlib.sha256(data).hexdigest(),
        }

    def child_edn(
        self,
        source: dict[str, object],
        *,
        artifact_id: str | None = None,
        module: str | None = None,
        source_path: str | None = None,
    ) -> bytes:
        module = module or self.module
        source_path = source_path or self.source_path
        artifact = artifact_id or "sha256:" + "e" * 64
        census = "sha256:" + "f" * 64
        verification = "sha256:" + "1" * 64
        catalog = "sha256:" + "2" * 64
        checks = " ".join(
            f":{key} true" for key in [
                "adapter-current?", "authoritative-coverage-census-current?",
                "capability-proof-complete?", "counts-precommitted-policy-current?",
                "coverage-census-policy-current?", "coverage-milestone-current?",
                "fresh-process-required?", "independent-count-oracle-policy-current?",
                "independent-verification-passed?", "iteration-cache-non-authoritative?",
                "proof-transaction-current?", "request-schema-current?",
                "required-core-product-counts-exact?", "required-core-products-present?",
                "required-request-products-present?",
                "required-verification-checks-present-and-passed?", "scope-current?",
                "source-revision-bound-to-bytes?", "source-snapshot-stable?",
                "target-source-reread-disabled?", "task-current?",
                "unsupported-claims-explicit?",
            ]
        )
        text = f'''{{:artifact :gravity/sh07-authoritative-proof-run
 :elapsed-ms 1
 :fresh-process-required? true
 :modules [{{:artifact-id "{artifact}"
            :call-edge-count 1
            :capability-proof-status :complete
            :contract-checks {{{checks}}}
            :coverage-census {{:aggregate-authoritative? false
                              :artifact :gravity/sh07-authoritative-coverage-census
                              :authority-scope :individual-existing-runner-output-only
                              :census-hash "{census}"
                              :core-counts {{:call-count 1 :core-form-frequencies {{:call 1}}
                                            :core-node-count 1 :definition-count 1
                                            :keyword-lookup-count 0 :reference-count 1}}
                              :integrity {{:form-id-order-exact? true
                                          :root-form-id-order-exact? true
                                          :source-revision-bound-to-bytes? true
                                          :source-snapshot-stable? true
                                          :target-source-reread-disabled? true}}
                              :module "{module}"
                              :module-namespace fixture.core
                              :request-counts {{:binding-count 1 :form-count 1
                                               :fragment-count 1 :local-binding-count 1
                                               :resolution-count 1 :root-form-count 1}}
                              :request-schema-version 15
                              :schema-version 1
                              :scope :fixture
                              :sh06-status :accepted
                              :sh07-artifact-id "{artifact}"
                              :source-binding {{:source-byte-count {source["size"]}
                                                :source-bytes-sha256 "{source["sha256"]}"}}
                              :source-revision-id "{source["sha256"]}"
                              :task "fixture-task"}}
            :elapsed-ms 1
            :failed-checks []
            :form-count 1
            :fragment-count 1
            :function-record-count 1
            :keyword-lookup-count 0
            :module "{module}"
            :proof-transaction {{:artifact :gravity/sh07-proof-transaction-receipt
                                :artifact-id "{artifact}"
                                :check-catalog-bindings {{[:sh05 :construction] "{catalog}"
                                                         [:sh05 :final] "{catalog}"
                                                         [:sh06 :final] "{catalog}"
                                                         [:sh07 :final] "{catalog}"}}
                                :checked-core-revision {{:public-function-hashes {{fixture-fn "{catalog}"}}}}
                                :cleanup-complete? true
                                :construction-receipts-cleared? true
                                :cross-epoch-reuse-count 0
                                :cross-epoch-reuse? false
                                :failed-report-executions 0
                                :failed-report-reuse-count 0
                                :failed-report-reuse? false
                                :final-snapshot-rechecked? true
                                :maximum-receipts 64
                                :owner-thread-id 3
                                :phase-order [:construction :independent-audit]
                                :phases []
                                :retained-receipt-count 0
                                :schema-version 1
                                :source-snapshot {{:source-byte-count {source["size"]}
                                                  :source-content-hash "{source["sha256"]}"}}
                                :status :passed
                                :thread-confined? true
                                :verification-report-id "{verification}"}}
            :recursion-component-count 0
            :resolution-count 1
            :schema-version 15
            :source-byte-count {source["size"]}
            :source-bytes-sha256 "{source["sha256"]}"
            :source-path "{source_path}"
            :source-revision-id "{source["sha256"]}"
            :status :accepted
            :verification-status :passed}}]
 :persistent-iteration-cache-used? false
 :proof-receipt-reuse-count 0
 :proof-receipt-reuse-used? false
 :schema-version 2
 :status :passed}}\n'''
        return text.encode("utf-8")

    def checkpoint_value(self, required: list[str]) -> dict[str, object]:
        source = self.entry(self.source_path)
        self.write_bytes("runtime/clojure", b"clojure-launcher\n")
        self.write_bytes("runtime/java", b"java-runtime\n")
        self.write_bytes("runtime/config.edn", b"{:fixture true}\n")
        self.write_bytes("runtime/library.jar", b"fixture-jar\n")
        launcher = self.root / "runtime/clojure"
        java = self.root / "runtime/java"
        config = self.root / "runtime/config.edn"
        library = self.root / "runtime/library.jar"
        launcher_entry = self.entry("runtime/clojure")
        java_entry = self.entry("runtime/java")
        config_entry = self.entry("runtime/config.edn")
        library_entry = self.entry("runtime/library.jar")
        stdout_path = "target/validation/sh07/modules/fixture.stdout.log"
        stderr_path = "target/validation/sh07/modules/fixture.stderr.log"
        self.write_bytes(stdout_path, self.child_edn(source))
        self.write_bytes(stderr_path, b"")
        shared = {
            "tool_version": 3,
            "fingerprint_policy_version": 1,
            "command": ["clojure", "-J-Xmx512m", "-M", "-m", "runner"],
            "resolved_executable": str(launcher),
            "resolved_executable_sha256": launcher_entry["sha256"],
            "environment": {
                name: os.environ.get(name)
                for name in [
                    "JAVA_HOME", "JAVA_OPTS", "JAVA_TOOL_OPTIONS", "_JAVA_OPTIONS",
                    "JDK_JAVA_OPTIONS", "CLJ_JVM_OPTS", "CLJ_CONFIG",
                ]
            },
            "runtime": {
                "required": True,
                "operating_system": platform.system(),
                "operating_system_release": platform.release(),
                "architecture": platform.machine(),
                "java_path": str(java),
                "java_sha256": java_entry["sha256"],
                "java_version": {"complete": True},
                "clojure_sdescribe": {"complete": True},
                "clojure_classpath": {"complete": True},
                "clojure_classpath_entries": [{
                    "path": str(library),
                    "kind": "file",
                    "size": library_entry["size"],
                    "sha256": library_entry["sha256"],
                }],
                "clojure_classpath_errors": [],
                "clojure_config_files": [{
                    "path": str(config), "sha256": config_entry["sha256"],
                }],
                "complete": True,
            },
            "authoritative_module_catalog": {self.module: self.source_path},
            "files": [self.entry(path) for path in required],
        }
        shared["sha256"] = composer._plain_json_sha256(shared)
        context = {
            "fingerprint_policy_version": 1,
            "module": self.module,
            "shared_context_sha256": shared["sha256"],
            "files": [source],
        }
        context["sha256"] = composer._plain_json_sha256(context)
        record = {
            "state": "passed",
            "command": [*shared["command"], "--fresh", self.module],
            "module_context_fingerprint": context["sha256"],
            "module_context": context,
            "context_stable": True,
            "shared_context_fingerprint_after": shared["sha256"],
            "stale_modules": [],
            "stdout_path": "modules/fixture.stdout.log",
            "stderr_path": "modules/fixture.stderr.log",
            "stdout_sha256": self.entry(stdout_path)["sha256"],
            "stderr_sha256": self.entry(stderr_path)["sha256"],
            "output_contract_checked": True,
            "exit_code": 0,
            "raw_child_exit_code": 0,
            "timed_out": False,
            "elapsed_seconds": 1.0,
            "finished_at": "now",
        }
        return {
            "schema": "gravity/sh07-authoritative-module-checkpoints-v2",
            "tool_version": 3,
            "fingerprint_policy_version": 1,
            "state": "completed",
            "shared_context_fingerprint": shared["sha256"],
            "shared_context": shared,
            "selected_modules": [self.module],
            "source_contracts": {
                self.module: {
                    "source_path": self.source_path,
                    "source_byte_count": source["size"],
                    "source_bytes_sha256": source["sha256"],
                }
            },
            "module_contexts": {self.module: context},
            "modules": {self.module: record},
            "resumed_modules": [],
            "aggregate_authoritative": False,
            "authority_scope": "individual-existing-runner-outputs-only",
            "started_at": "before",
            "finished_at": "after",
            "updated_at": "after",
        }

    @property
    def artifacts(self) -> list[str]:
        project = json.loads((self.root / "contracts/project-structure.json").read_text())
        passes = {item["id"]: item for item in project["canonical_passes"]}
        slices = {item["id"]: item for item in project["slices"]}
        return sorted({
            artifact
            for item in list(passes.values()) + list(slices.values())
            for field in ("input_artifacts", "output_artifacts", "artifact_inputs", "artifact_outputs")
            for artifact in item.get(field, [])
        })

    @property
    def policies(self) -> list[str]:
        project = json.loads((self.root / "contracts/project-structure.json").read_text())
        return sorted(item["id"] for item in project["path_policy"]["policies"])

    @property
    def passes(self) -> list[str]:
        project = json.loads((self.root / "contracts/project-structure.json").read_text())
        return sorted(item["id"] for item in project["canonical_passes"])

    @property
    def slices(self) -> list[str]:
        project = json.loads((self.root / "contracts/project-structure.json").read_text())
        return sorted(item["id"] for item in project["slices"])

    def compose(self, output: str = "target/validation/candidate.json", **overrides: object) -> dict:
        arguments: dict[str, object] = {
            "checkpoint_path": "target/validation/sh07/manifest.json",
            "changed_paths": [self.source_path],
            "pass_ids": self.passes,
            "slice_ids": self.slices,
            "module_ids": [self.module],
            "verification_check_ids": ["base"],
            "artifact_ids": self.artifacts,
            "policy_ids": self.policies,
            "development_composition_paths": [self.development_path],
            "c2_cache_witness_paths": [self.c2_path],
            "output_path": output,
            "root": self.root,
        }
        arguments.update(overrides)
        return composer.compose_candidate(**arguments)

    def admission_files(self, candidate: dict, **changes: object) -> tuple[str, str]:
        attestation: dict[str, object] = {
            "schema_version": 1,
            "kind": "gravity/authoritative-evidence-reviewed-attestation",
            "reviewer": "reviewer-1",
            "reviewed_at": "2026-08-07T00:00:00Z",
            "decision": "admit",
            "candidate_sha256": candidate["candidate_sha256"],
            "child_edn_projection_sha256": candidate["sh07"]["child_edn_projection_sha256"],
            "claimed_subject": candidate["evidence_subject"],
            "findings": [],
            "signature": None,
            "release_authority": False,
        }
        attestation.update(changes)
        attestation_path = "docs/reviews/attestation.json"
        self.write_json(attestation_path, attestation)
        attestation_sha = composer._semantic_sha256(
            attestation, "gravity.authoritative-evidence/reviewed-attestation/v1"
        )
        policy = {
            "schema_version": 1,
            "kind": "gravity/authoritative-evidence-promotion-policy",
            "policy_id": "test-reviewed-policy",
            "reviewed": True,
            "promotion_enabled": True,
            "allowed_reviewers": ["reviewer-1"],
            "aggregate_authority": False,
            "release_authority": False,
            "self_hosting_authority": False,
            "seed_retirement_authority": False,
            "signature": None,
            "admissions": [{
                "candidate_sha256": candidate["candidate_sha256"],
                "attestation_sha256": attestation_sha,
                "claimed_subject": candidate["evidence_subject"],
            }],
        }
        policy_path = "contracts/promotion-policy.json"
        self.write_json(policy_path, policy)
        return policy_path, attestation_path

    def assert_code(self, code: str, callback) -> None:
        with self.assertRaises(composer.EvidenceError) as raised:
            callback()
        self.assertEqual(code, raised.exception.code, str(raised.exception))

    def test_candidate_is_deterministic_exact_and_nonauthoritative(self) -> None:
        first = self.compose()
        second = self.compose("target/validation/candidate-two.json")
        self.assertEqual(first, second)
        self.assertFalse(first["claims"]["authoritative"])
        self.assertFalse(first["claims"]["aggregate_authoritative"])
        self.assertIsNone(first["claims"]["signature"])
        self.assertFalse(first["references"]["authority_contribution"])
        self.assertEqual(self.artifacts, first["context_nonclaims"]["artifact_ids"])
        self.assertEqual(self.policies, first["context_nonclaims"]["policy_ids"])
        self.assertEqual([self.module], first["evidence_subject"]["module_ids"])
        self.assertFalse(first["context_nonclaims"]["authority_contribution"])
        self.assertEqual(
            "non-authoritative-observation",
            first["python_tooling"]["component"]["authority_ceiling"],
        )
        self.assertFalse(first["python_tooling"]["authority_contribution"])
        self.assertTrue(all(
            item["maximum_claim"] == "coordination-only"
            and item["authority_contribution"] is False
            for item in first["context_nonclaims"]["authority_ceilings"]
        ))
        self.assertNotIn("repository_root", first)
        records = first["sh07"]["modules"][0]["child_edn"]["identity_records"]
        self.assertFalse(any(item["key"] == "owner-thread-id" for item in records))
        self.assertTrue(any(item["key"] == "public-function-hash" for item in records))
        self.assertIn("cryptographic_child_identity_incomplete", first["sh07"]["deficiencies"])
        result = composer.validate_candidate(
            "target/validation/candidate.json", root=self.root
        )
        self.assertEqual(first["candidate_sha256"], result["candidate_sha256"])
        self.assertRegex(first["sh07"]["child_edn_projection_sha256"], r"^sha256:[0-9a-f]{64}$")

    def test_output_is_restricted_to_target_validation(self) -> None:
        self.assert_code("AE012", lambda: self.compose("docs/candidate.json"))
        isolated = self.root / "isolated-output"
        with mock.patch.dict(os.environ, {"GRAVITY_OUTPUT_ROOT": str(isolated)}):
            self.assert_code("AE012", self.compose)
        self.assertFalse((isolated / "target/validation/candidate.json").exists())

    def test_contract_and_output_routing_are_code_pinned(self) -> None:
        with self.assertRaises(TypeError):
            self.compose(contract_path="docs/forged-contract.json")
        with self.assertRaises(TypeError):
            self.compose(project_structure_path="docs/forged-project.json")
        with self.assertRaises(TypeError):
            self.compose(verification_manifest_path="docs/forged-verification.json")
        contract_path = self.root / "contracts/authoritative-evidence.json"
        original = json.loads(contract_path.read_text())
        forged = copy.deepcopy(original)
        forged["candidate"]["output_prefix"] = "docs/"
        self.write_json("contracts/authoritative-evidence.json", forged)
        self.assert_code("AE014", self.compose)
        self.write_json("contracts/authoritative-evidence.json", original)
        tooling_path = self.root / "contracts/python-tooling.json"
        tooling = json.loads(tooling_path.read_text())
        forged_tooling = copy.deepcopy(tooling)
        component = next(
            item for item in forged_tooling["components"]
            if item["id"] == "authoritative-evidence-composition"
        )
        component["authority_ceiling"] = "none"
        self.write_json("contracts/python-tooling.json", forged_tooling)
        self.assert_code("AE014", self.compose)
        self.write_json("contracts/python-tooling.json", tooling)

    def test_scope_and_dependency_closures_fail_closed(self) -> None:
        self.assert_code(
            "AE007",
            lambda: self.compose(verification_check_ids=["leaf"]),
        )
        self.assert_code(
            "AE007",
            lambda: self.compose(artifact_ids=["source-forms"]),
        )
        self.assert_code(
            "AE007",
            lambda: self.compose(policy_ids=[]),
        )
        self.assert_code(
            "AE007",
            lambda: self.compose(slice_ids=[item for item in self.slices if item != "SH-29"]),
        )
        self.write_bytes("tools/unrelated.py", b"# unrelated\n")
        self.assert_code(
            "AE007",
            lambda: self.compose(changed_paths=["tools/unrelated.py"]),
        )

    def test_unrelated_selected_module_cannot_become_an_evidence_subject(self) -> None:
        second_module = "fixture-two"
        second_source_path = (
            "bootstrap/gravity/src/gravity/backend/b10_workflow_graph_backend_design.gravity"
        )
        self.write_bytes(second_source_path, b"(ns fixture-two)\n")
        second_source = self.entry(second_source_path)
        stdout = "target/validation/sh07/modules/fixture-two.stdout.log"
        stderr = "target/validation/sh07/modules/fixture-two.stderr.log"
        self.write_bytes(
            stdout,
            self.child_edn(
                second_source, module=second_module, source_path=second_source_path
            ),
        )
        self.write_bytes(stderr, b"")
        value = copy.deepcopy(self.checkpoint)
        shared = value["shared_context"]
        shared.pop("sha256")
        shared["authoritative_module_catalog"][second_module] = second_source_path
        shared["sha256"] = composer._plain_json_sha256(shared)
        for module in [self.module]:
            context = value["module_contexts"][module]
            context.pop("sha256")
            context["shared_context_sha256"] = shared["sha256"]
            context["sha256"] = composer._plain_json_sha256(context)
            value["modules"][module]["module_context"] = context
            value["modules"][module]["module_context_fingerprint"] = context["sha256"]
            value["modules"][module]["shared_context_fingerprint_after"] = shared["sha256"]
        context = {
            "fingerprint_policy_version": 1,
            "module": second_module,
            "shared_context_sha256": shared["sha256"],
            "files": [second_source],
        }
        context["sha256"] = composer._plain_json_sha256(context)
        record = copy.deepcopy(value["modules"][self.module])
        record.update({
            "command": [*shared["command"], "--fresh", second_module],
            "module_context": context,
            "module_context_fingerprint": context["sha256"],
            "shared_context_fingerprint_after": shared["sha256"],
            "stdout_path": "modules/fixture-two.stdout.log",
            "stderr_path": "modules/fixture-two.stderr.log",
            "stdout_sha256": self.entry(stdout)["sha256"],
            "stderr_sha256": self.entry(stderr)["sha256"],
        })
        value["selected_modules"] = sorted([self.module, second_module])
        value["source_contracts"][second_module] = {
            "source_path": second_source_path,
            "source_byte_count": second_source["size"],
            "source_bytes_sha256": second_source["sha256"],
        }
        value["module_contexts"][second_module] = context
        value["modules"][second_module] = record
        value["shared_context_fingerprint"] = shared["sha256"]
        self.write_json("target/validation/sh07/manifest.json", value)
        self.assert_code(
            "AE007",
            lambda: self.compose(module_ids=sorted([self.module, second_module])),
        )

    def test_repository_binding_rejects_nongit_and_inherited_git_dir(self) -> None:
        self.assertFalse(composer._repository_context(self.root)["commit_bound"])
        git_dir = subprocess.run(
            ["git", "-C", str(ROOT), "rev-parse", "--absolute-git-dir"],
            check=True, capture_output=True, text=True,
        ).stdout.strip()
        with mock.patch.dict(os.environ, {"GIT_DIR": git_dir}):
            context = composer._repository_context(self.root)
        self.assertFalse(context["commit_bound"])
        self.assertFalse(context["canonical_top_level_validated"])

    def test_weak_child_identity_blocks_promotion_despite_output_checked(self) -> None:
        source = self.entry(self.source_path)
        stdout = "target/validation/sh07/modules/fixture.stdout.log"
        self.write_bytes(stdout, self.child_edn(source, artifact_id="weak-artifact-id"))
        self.checkpoint["modules"][self.module]["stdout_sha256"] = self.entry(stdout)["sha256"]
        self.write_json("target/validation/sh07/manifest.json", self.checkpoint)
        candidate = self.compose()
        self.assertIn("artifact_id_not_cryptographic", candidate["sh07"]["deficiencies"])
        policy, attestation = self.admission_files(candidate)
        result = composer.promote_candidate(
            "target/validation/candidate.json", policy_path=policy,
            attestation_path=attestation, output_path="target/validation/promoted.json",
            root=self.root,
        )
        self.assertFalse(result["claims"]["authoritative"])
        self.assertTrue(result["promotion_blocked"])

    def test_resume_cache_and_output_contract_are_independent_rejections(self) -> None:
        cases = [
            ("resumed_modules", lambda value: value.__setitem__("resumed_modules", [self.module])),
            ("output_contract", lambda value: value["modules"][self.module].__setitem__("output_contract_checked", False)),
        ]
        for name, mutation in cases:
            with self.subTest(name=name):
                value = copy.deepcopy(self.checkpoint)
                mutation(value)
                self.write_json("target/validation/sh07/manifest.json", value)
                self.assert_code("AE009", self.compose)
        self.write_json("target/validation/sh07/manifest.json", self.checkpoint)
        stdout_path = self.root / "target/validation/sh07/modules/fixture.stdout.log"
        text = stdout_path.read_text().replace(
            ":persistent-iteration-cache-used? false",
            ":persistent-iteration-cache-used? true",
        )
        stdout_path.write_text(text)
        value = copy.deepcopy(self.checkpoint)
        value["modules"][self.module]["stdout_sha256"] = self.entry(
            "target/validation/sh07/modules/fixture.stdout.log"
        )["sha256"]
        self.write_json("target/validation/sh07/manifest.json", value)
        self.assert_code("AE009", self.compose)

    def test_real_current_v2_cache_and_identity_shapes_become_explicit_blockers(self) -> None:
        stdout = "target/validation/sh07/modules/fixture.stdout.log"
        text = (self.root / stdout).read_text().replace(
            ":proof-receipt-reuse-used? false", ":proof-receipt-reuse-used? true"
        ).replace(":proof-receipt-reuse-count 0", ":proof-receipt-reuse-count 14")
        self.write_bytes(stdout, text.encode())
        value = copy.deepcopy(self.checkpoint)
        value["modules"][self.module]["stdout_sha256"] = self.entry(stdout)["sha256"]
        self.write_json("target/validation/sh07/manifest.json", value)
        candidate = self.compose()
        runner_bytes = (
            ROOT / "bootstrap/clojure/test/gravity/self_hosting/sh07_authoritative_runner.clj"
        ).read_bytes()
        self.assertEqual(
            "sha256:" + hashlib.sha256(runner_bytes).hexdigest(),
            candidate["sh07"]["child_runner"]["sha256"],
        )
        self.assertIn("proof_receipt_cache_used", candidate["sh07"]["deficiencies"])
        self.assertIn("proof_receipt_cache_count_nonzero", candidate["sh07"]["deficiencies"])
        records = candidate["sh07"]["modules"][0]["child_edn"]["identity_records"]
        self.assertTrue(any(item["path"].endswith("/symbol:fixture-fn") for item in records))

    def test_legacy_launcher_symlink_is_bound_as_promotion_deficiency(self) -> None:
        launcher = self.root / "runtime/clojure"
        target = self.root / "runtime/clojure-real"
        launcher.rename(target)
        launcher.symlink_to(target.name)
        candidate = self.compose()
        self.assertIn(
            "runtime_launcher_canonical_target_unbound", candidate["sh07"]["deficiencies"]
        )

    def test_strict_duplicate_nan_and_depth_bounds(self) -> None:
        duplicate = self.root / self.development_path
        duplicate.write_text('{"schema_version":1,"schema_version":1}')
        self.assert_code("AE001", self.compose)
        duplicate.write_text('{"x":NaN}')
        self.assert_code("AE001", self.compose)
        duplicate.write_text("[" * 120 + "]" * 120)
        self.assert_code("AE002", self.compose)

    def test_symlink_hardlink_and_missing_outputs_are_rejected(self) -> None:
        target = self.root / self.c2_path
        backup = target.with_suffix(".saved")
        target.rename(backup)
        target.symlink_to(backup.name)
        self.assert_code("AE004", self.compose)
        target.unlink()
        os.link(backup, target)
        self.assert_code("AE004", self.compose)
        target.unlink()
        backup.rename(target)
        (self.root / "target/validation/sh07/modules/fixture.stderr.log").unlink()
        self.assert_code("AE004", self.compose)

    def test_forged_and_stale_candidates_are_rejected(self) -> None:
        candidate = self.compose()
        path = self.root / "target/validation/candidate.json"
        forged = json.loads(path.read_text())
        forged["evidence_subject"]["module_ids"] = ["forged"]
        self.write_json("target/validation/candidate.json", forged)
        self.assert_code(
            "AE007",
            lambda: composer.validate_candidate("target/validation/candidate.json", root=self.root),
        )
        self.write_json("target/validation/candidate.json", candidate)
        (self.root / self.source_path).write_text("(ns changed)\n")
        self.assert_code(
            "AE008",
            lambda: composer.validate_candidate("target/validation/candidate.json", root=self.root),
        )

    def test_current_runtime_bytes_are_revalidated(self) -> None:
        self.compose()
        self.write_bytes("runtime/java", b"changed-runtime\n")
        self.assert_code(
            "AE008",
            lambda: composer.validate_candidate("target/validation/candidate.json", root=self.root),
        )

    def test_reviewed_material_cannot_mint_authority(self) -> None:
        candidate = self.compose()
        policy, attestation = self.admission_files(candidate)
        result = composer.promote_candidate(
            "target/validation/candidate.json",
            policy_path=policy,
            attestation_path=attestation,
            output_path="target/validation/promoted.json",
            root=self.root,
        )
        self.assertFalse(result["claims"]["authoritative"])
        self.assertEqual("none", result["claims"]["authority_scope"])
        for field in (
            "aggregate_authoritative", "release_authoritative",
            "self_hosting_authoritative", "seed_retirement_authoritative",
        ):
            self.assertFalse(result["claims"][field])
        self.assertTrue(result["claims"]["release_blocked"])
        self.assertTrue(result["promotion_blocked"])
        self.assertIn("trusted_admission_root_missing", result["blockers"])
        self.assertIn("review_material_self_asserted_not_trusted", result["blockers"])
        self.assertIn("repository_commit_unbound", result["blockers"])
        self.assertIsNone(result["claims"]["signature"])
        self.assertFalse(result["claims"]["digest_is_signature"])
        self.assertEqual(candidate["evidence_subject"], result["evidence_subject"])
        self.assertEqual(candidate["context_nonclaims"], result["context_nonclaims"])

    def test_forged_review_material_still_cannot_mint_authority(self) -> None:
        candidate = self.compose()
        mutations = [
            {"claimed_subject": {"module_ids": ["forged"]}},
            {"child_edn_projection_sha256": "sha256:" + "0" * 64},
            {"reviewer": "unknown"},
            {"signature": "digest-is-not-a-signature"},
        ]
        for index, mutation in enumerate(mutations):
            with self.subTest(index=index):
                policy, attestation = self.admission_files(candidate, **mutation)
                result = composer.promote_candidate(
                    "target/validation/candidate.json",
                    policy_path=policy,
                    attestation_path=attestation,
                    output_path="target/validation/promoted.json",
                    root=self.root,
                )
                self.assertFalse(result["claims"]["authoritative"])
                self.assertTrue(result["promotion_blocked"])

    def test_promotion_revalidates_current_bytes(self) -> None:
        candidate = self.compose()
        policy, attestation = self.admission_files(candidate)
        (self.root / self.source_path).write_text("(ns stale)\n")
        self.assert_code(
            "AE008",
            lambda: composer.promote_candidate(
                "target/validation/candidate.json",
                policy_path=policy,
                attestation_path=attestation,
                output_path="target/validation/promoted.json",
                root=self.root,
            ),
        )

    def test_promotion_uses_one_candidate_snapshot(self) -> None:
        candidate = self.compose()
        policy, attestation = self.admission_files(candidate)
        candidate_path = self.root / "target/validation/candidate.json"
        original_file_sha = "sha256:" + hashlib.sha256(candidate_path.read_bytes()).hexdigest()
        original_validator = composer._validate_candidate_snapshot

        def replace_after_snapshot(value, binding, root):
            result = original_validator(value, binding, root)
            forged = copy.deepcopy(value)
            forged["evidence_subject"] = {"module_ids": ["forged"]}
            self.write_json("target/validation/candidate.json", forged)
            return result

        with mock.patch.object(
            composer, "_validate_candidate_snapshot", side_effect=replace_after_snapshot
        ):
            result = composer.promote_candidate(
                "target/validation/candidate.json", policy_path=policy,
                attestation_path=attestation, output_path="target/validation/promoted.json",
                root=self.root,
            )
        self.assertEqual(candidate["candidate_sha256"], result["candidate"]["candidate_sha256"])
        self.assertEqual(original_file_sha, result["candidate"]["file"]["sha256"])
        self.assertEqual(candidate["evidence_subject"], result["evidence_subject"])

    def test_review_inputs_must_be_reviewed_source(self) -> None:
        candidate = self.compose()
        policy, attestation = self.admission_files(candidate)
        unsafe_policy = "target/validation/policy.json"
        shutil.copy2(self.root / policy, self.root / unsafe_policy)
        self.assert_code(
            "AE011",
            lambda: composer.promote_candidate(
                "target/validation/candidate.json",
                policy_path=unsafe_policy,
                attestation_path=attestation,
                output_path="target/validation/promoted.json",
                root=self.root,
            ),
        )


if __name__ == "__main__":
    unittest.main()
