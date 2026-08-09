#!/usr/bin/env python3
"""Adversarial Python-only tests for the evidence-producer inventory."""

from __future__ import annotations

import copy
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from unittest import mock


ROOT = Path(__file__).resolve().parents[2]
TOOLS = ROOT / "tools"
if str(TOOLS) not in sys.path:
    sys.path.insert(0, str(TOOLS))

import validate_evidence_producers as validator


class EvidenceProducerContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.contract = validator.load_json(ROOT / "contracts/evidence-producers.json")
        cls.python_paths = validator.discover_python_paths(ROOT)
        cls.head = subprocess.run(
            ["git", "-C", str(ROOT), "rev-parse", "HEAD"],
            check=True, capture_output=True, text=True,
        ).stdout.strip()

    def validate(
        self,
        contract: dict | None = None,
        *,
        source_overrides: dict[str, str] | None = None,
        python_paths: list[str] | None = None,
        promotion_records: list[dict] | None = None,
    ) -> list[str]:
        return validator.validate_contract(
            copy.deepcopy(self.contract) if contract is None else contract,
            root=ROOT,
            source_overrides=source_overrides,
            python_paths=list(self.python_paths) if python_paths is None else python_paths,
            promotion_records=promotion_records,
        )

    @staticmethod
    def producer(contract: dict, identifier: str) -> dict:
        return next(item for item in contract["producers"] if item["id"] == identifier)

    def promotion(self, *, status: str = "admitted") -> dict:
        digest = self.contract["inventory"]["producer_sha256"]
        return {
            "schema": "gravity/evidence-promotion-record-v1",
            "status": status,
            "producer_id": "isolated-artifact-validators",
            "producer_source": "tools/validate_reader.py",
            "reviewed_commit": self.head,
            "reviewer": "reviewer-b",
            "producer_author": "author-a",
            "checks": {
                "producer-inventory": "passed",
                "current-input-digests": "passed",
                "current-output-digest": "passed",
                "review-provenance": "passed",
            },
            "source_sha256": "sha256:" + "2" * 64,
            "output_path": "target/validation/evidence/test.json",
            "output_sha256": "sha256:" + "3" * 64,
            "producer_inventory_sha256": digest,
            "authoritative": False,
            "aggregate_authoritative": False,
            "release_authoritative": False,
            "trusted_authority_boundary": False,
            "signature": None,
        }

    def test_repository_contract_passes(self) -> None:
        self.assertEqual([], self.validate())

    def test_duplicate_key_and_nonfinite_number_are_rejected(self) -> None:
        with self.assertRaises(validator.DuplicateKeyError):
            json.loads('{"x": 1, "x": 2}', object_pairs_hook=validator._object_no_duplicates)
        with self.assertRaises(ValueError):
            json.loads('{"x": NaN}', parse_constant=validator._reject_constant)

    def test_uncovered_new_filesystem_producer_fails_closed(self) -> None:
        path = "tools/new_evidence_writer.py"
        source = "from pathlib import Path\nPath('target/x').write_text('x')\n"
        errors = self.validate(
            source_overrides={path: source},
            python_paths=list(self.python_paths) + [path],
        )
        self.assertTrue(any("EP007" in error and path in error and "matched 0" in error for error in errors), errors)
        self.assertTrue(any("EP015" in error for error in errors), errors)

    def test_read_only_artifact_census_path_is_excluded_but_write_mutation_is_not_hidden(self) -> None:
        path = "tools/validate_artifact_census.py"
        source = Path(ROOT / path).read_text(encoding="utf-8") + "\nfrom pathlib import Path\nPath('target/forged').write_text('x')\n"
        errors = self.validate(source_overrides={path: source})
        self.assertTrue(any("EP007" in error and path in error and "matched 0" in error for error in errors), errors)
        self.assertTrue(any("EP015" in error and "producer_count" in error for error in errors), errors)

    def test_os_write_is_a_producer_unless_pipe_proven_in_the_same_scope(self) -> None:
        path = "tools/validate_artifact_census.py"
        shadowed = "import os\nimport subprocess\nprocess = subprocess.Popen([])\ndef write(process):\n os.write(process.stdin.fileno(), b'x')\n"
        self.assertEqual(
            validator.discover_producers(
                ROOT, source_overrides={path: shadowed}, python_paths=[path]
            ),
            [path],
        )
        shadowed_module = "import os\nimport subprocess\ndef write(subprocess):\n process = subprocess.Popen([])\n os.write(process.stdin.fileno(), b'x')\n"
        self.assertEqual(
            validator.discover_producers(
                ROOT, source_overrides={path: shadowed_module}, python_paths=[path]
            ),
            [path],
        )
        for shadowed_os in (
            "import os\nimport subprocess\ndef write(os):\n process = subprocess.Popen([])\n os.write(process.stdin.fileno(), b'x')\n",
            "import os\nimport subprocess\ndef write():\n os = object()\n process = subprocess.Popen([])\n os.write(process.stdin.fileno(), b'x')\n",
            "import os as operating\nimport subprocess\ndef write(operating):\n process = subprocess.Popen([])\n operating.write(process.stdin.fileno(), b'x')\n",
        ):
            self.assertEqual(
                validator.discover_producers(
                    ROOT, source_overrides={path: shadowed_os}, python_paths=[path]
                ),
                [path],
            )
        imported_alias_pipe = "import os as operating\nimport subprocess as child_process\ndef write():\n process = child_process.Popen([])\n operating.write(process.stdin.fileno(), b'x')\n"
        self.assertEqual(
            validator.discover_producers(
                ROOT, source_overrides={path: imported_alias_pipe}, python_paths=[path]
            ),
            [],
        )
        local_import_arbitrary = "def write(handle):\n import os as operating\n operating.write(handle, b'x')\n"
        self.assertEqual(
            validator.discover_producers(
                ROOT, source_overrides={path: local_import_arbitrary}, python_paths=[path]
            ),
            [path],
        )
        local_import_pipe = "def write():\n import os as operating\n import subprocess as child_process\n process = child_process.Popen([])\n operating.write(process.stdin.fileno(), b'x')\n"
        self.assertEqual(
            validator.discover_producers(
                ROOT, source_overrides={path: local_import_pipe}, python_paths=[path]
            ),
            [],
        )
        comprehension_shadow = "import os\nimport subprocess\ndef write(handles):\n process = subprocess.Popen([])\n return [os.write(process.stdin.fileno(), b'x') for os in handles]\n"
        self.assertEqual(
            validator.discover_producers(
                ROOT, source_overrides={path: comprehension_shadow}, python_paths=[path]
            ),
            [path],
        )
        for shadowed_source in (
            "import os, subprocess\ndef write(value):\n process = subprocess.Popen([])\n match value:\n  case {'items': [*process], **rest}:\n   os.write(process.stdin.fileno(), b'x')\n",
            "import os, subprocess\ndef write(value):\n process = subprocess.Popen([])\n match value:\n  case {**os}:\n   os.write(process.stdin.fileno(), b'x')\n",
            "import os, subprocess\ndef write(values):\n process = subprocess.Popen([])\n return [os.write(process.stdin.fileno(), b'x') for value in values if (process := value)]\n",
            "import os, subprocess\ndef write(values):\n process = subprocess.Popen([])\n return [os.write(process.stdin.fileno(), b'x') for value in values if (os := value)]\n",
        ):
            self.assertEqual(
                validator.discover_producers(
                    ROOT, source_overrides={path: shadowed_source}, python_paths=[path]
                ),
                [path],
            )
        proven_pipe = "import os\nimport subprocess\ndef write():\n process = subprocess.Popen([])\n os.write(process.stdin.fileno(), b'x')\n"
        self.assertEqual(
            validator.discover_producers(
                ROOT, source_overrides={path: proven_pipe}, python_paths=[path]
            ),
            [],
        )

    def test_overlapping_source_ownership_is_rejected(self) -> None:
        contract = copy.deepcopy(self.contract)
        self.producer(contract, "development-baseline-receipt")["sources"]["includes"] = ["tools/run_*.py", "tools/measure_development_baseline.py"]
        errors = self.validate(contract)
        self.assertTrue(any("EP007" in error and "matched 2" in error for error in errors), errors)

    def test_pipe_producer_proof_skips_class_namespaces_and_honors_definitions(self) -> None:
        path = "tools/validate_artifact_census.py"
        negatives = (
            "import os, subprocess\nclass C:\n import os as operating\n import subprocess as child_process\n def f(self):\n  process = child_process.Popen([])\n  operating.write(process.stdin.fileno(), b'x')\n",
            "import os, subprocess\nos = object()\nprocess = subprocess.Popen([])\ndef f(value=os.write(process.stdin.fileno(), b'x')):\n import os\n return value\n",
            "import os, subprocess\ndef f():\n global os\n os = object()\n process = subprocess.Popen([])\n os.write(process.stdin.fileno(), b'x')\n",
            "class C:\n import os\n import subprocess\n process = subprocess.Popen([])\n values = [os.write(process.stdin.fileno(), b'x') for item in ()]\n",
            "class C:\n import os as operating\n import subprocess as child_process\n process = child_process.Popen([])\n operating.write(process.stdin.fileno(), b'x')\n",
            "class C:\n import os\n import subprocess\n process = subprocess.Popen([])\n values = [item for item in os.write(process.stdin.fileno(), b'x')]\n",
            "os = object()\nimport subprocess\nclass C:\n process = subprocess.Popen([])\n os.write(process.stdin.fileno(), b'x')\n import os\n",
            "import os, subprocess\nprocess = object()\nclass C:\n os.write(process.stdin.fileno(), b'x')\n process = subprocess.Popen([])\n",
            "os = object()\nimport subprocess\nprocess = object()\nclass C:\n os.write(process.stdin.fileno(), b'x')\n import os\n process = subprocess.Popen([])\n",
        )
        for source in negatives:
            self.assertEqual(
                validator.discover_producers(
                    ROOT, source_overrides={path: source}, python_paths=[path]
                ),
                [path],
            )
        positives = (
            "class C:\n def f(self):\n  import os as operating\n  import subprocess as child_process\n  process = child_process.Popen([])\n  operating.write(process.stdin.fileno(), b'x')\n",
        )
        for source in positives:
            self.assertEqual(
                validator.discover_producers(
                    ROOT, source_overrides={path: source}, python_paths=[path]
                ),
                [],
            )

    def test_output_pattern_must_remain_in_declared_policy(self) -> None:
        contract = copy.deepcopy(self.contract)
        self.producer(contract, "development-baseline-receipt")["output_patterns"] = ["docs/review-ledger.md"]
        errors = self.validate(contract)
        self.assertTrue(any("EP008" in error and "outside declared output policies" in error for error in errors), errors)

    def test_output_policy_authority_and_publication_booleans_are_pinned(self) -> None:
        contract = copy.deepcopy(self.contract)
        generated = next(item for item in contract["output_policies"] if item["id"] == "generated-evidence")
        generated["isolated_atomic_publication_required"] = False
        reviewed = next(item for item in contract["output_policies"] if item["id"] == "reviewed-document-source")
        reviewed["reviewed_source"] = False
        errors = self.validate(contract)
        self.assertGreaterEqual(sum("EP005" in error and "pinned output-policy profile" in error for error in errors), 2, errors)

    def test_unreviewed_direct_reviewed_path_generation_is_rejected(self) -> None:
        contract = copy.deepcopy(self.contract)
        producer = self.producer(contract, "development-baseline-receipt")
        producer["output_policy_refs"] = ["reviewed-document-source"]
        producer["output_patterns"] = ["docs/generated.md"]
        errors = self.validate(contract)
        self.assertTrue(any("EP014" in error and "unreviewed direct reviewed-path" in error for error in errors), errors)

    def test_reviewed_source_requires_both_reviews_and_exact_writer(self) -> None:
        contract = copy.deepcopy(self.contract)
        producer = self.producer(contract, "gravity-document-generator")
        producer["review"]["generation_review_required"] = False
        producer["writer"] = "development-receipt-writer"
        errors = self.validate(contract)
        self.assertTrue(any("EP014" in error and "invalid boundary" in error for error in errors), errors)
        self.assertTrue(any("EP014" in error and "generation and admission review" in error for error in errors), errors)

    def test_missing_writer_schema_provenance_and_nonclaim_are_rejected(self) -> None:
        contract = copy.deepcopy(self.contract)
        producer = self.producer(contract, "development-baseline-receipt")
        producer["writer"] = "missing-writer"
        producer["schemas"] = []
        producer["provenance_required"] = ["command"]
        producer["nonclaims"] = ["benchmark-evidence"]
        errors = self.validate(contract)
        self.assertTrue(any("EP009" in error for error in errors), errors)
        self.assertTrue(any("schemas" in error and "must not be empty" in error for error in errors), errors)
        self.assertTrue(any("EP011" in error for error in errors), errors)
        self.assertTrue(any("EP012" in error for error in errors), errors)

    def test_declared_writer_must_be_observed_in_source(self) -> None:
        contract = copy.deepcopy(self.contract)
        self.producer(contract, "development-baseline-receipt")["writer"] = "heartbeat-state-writer"
        errors = self.validate(contract)
        self.assertTrue(any("EP009" in error and "does not call the declared writer" in error for error in errors), errors)

    def test_writer_profiles_are_exactly_pinned(self) -> None:
        contract = copy.deepcopy(self.contract)
        writer = next(item for item in contract["writers"] if item["id"] == "development-cache-writer")
        writer["implementation"] = "made-up-writer"
        errors = self.validate(contract)
        self.assertTrue(any("EP006" in error and "pinned writer profile" in error for error in errors), errors)

    def test_python_component_authority_ceiling_is_enforced(self) -> None:
        contract = copy.deepcopy(self.contract)
        self.producer(contract, "development-baseline-receipt")["authority_ceiling"] = "authoritative"
        errors = self.validate(contract)
        self.assertTrue(any("EP010" in error for error in errors), errors)

    def test_compiler_artifact_graph_cannot_be_reclassified_as_producer_inventory(self) -> None:
        contract = copy.deepcopy(self.contract)
        contract["boundaries"]["compiler_artifact_graph_is_filesystem_inventory"] = True
        errors = self.validate(contract)
        self.assertTrue(any("EP004" in error and "compiler_artifact_graph" in error for error in errors), errors)

    def test_top_level_nonclaims_are_exactly_pinned(self) -> None:
        contract = copy.deepcopy(self.contract)
        contract["required_nonclaims"] = ["authority"]
        errors = self.validate(contract)
        self.assertTrue(any("EP012" in error and "pinned non-authority" in error for error in errors), errors)

    def test_producer_schema_ids_are_exactly_pinned(self) -> None:
        contract = copy.deepcopy(self.contract)
        self.producer(contract, "development-baseline-receipt")["schemas"] = ["made-up-schema-v1"]
        errors = self.validate(contract)
        self.assertTrue(any("EP017" in error and "pinned schema ids" in error for error in errors), errors)

    def test_inventory_count_and_digest_are_fail_closed(self) -> None:
        contract = copy.deepcopy(self.contract)
        contract["inventory"]["producer_count"] -= 1
        contract["inventory"]["producer_sha256"] = "sha256:" + "0" * 64
        errors = self.validate(contract)
        self.assertTrue(any("EP015" in error and "producer_count" in error for error in errors), errors)
        self.assertTrue(any("EP015" in error and "producer_sha256" in error for error in errors), errors)

    def test_inventory_exclusions_are_exact_and_fail_closed(self) -> None:
        contract = copy.deepcopy(self.contract)
        contract["inventory"]["excluded_patterns"] = []
        errors = self.validate(contract)
        self.assertTrue(any("EP015" in error and "excluded_patterns" in error for error in errors), errors)

    def test_only_exact_coverage_capable_validators_claim_coverage_output(self) -> None:
        coverage = self.producer(self.contract, "isolated-artifact-coverage-validators")
        self.assertEqual(7, len(coverage["sources"]["includes"]))
        self.assertEqual(["--artifact-out", "--coverage-out"], coverage["cli_output_options"])
        general = self.producer(self.contract, "isolated-artifact-validators")
        self.assertEqual(["--artifact-out"], general["cli_output_options"])
        self.assertNotIn("generated-coverage", general["output_policy_refs"])

    def test_reviewed_document_output_cannot_overlap_generated_artifacts(self) -> None:
        contract = copy.deepcopy(self.contract)
        self.producer(contract, "gravity-document-generator")["output_patterns"].append("docs/artifacts/forged.md")
        errors = self.validate(contract)
        self.assertTrue(any("EP008" in error and "overlaps generated project policy" in error for error in errors), errors)

    def test_producer_specific_input_identity_provenance_is_required(self) -> None:
        contract = copy.deepcopy(self.contract)
        producer = self.producer(contract, "development-baseline-receipt")
        producer["provenance_required"].remove("input-identities")
        errors = self.validate(contract)
        self.assertTrue(any("EP011" in error and "input-identities" in error for error in errors), errors)

    def test_admitted_promotion_is_still_nonauthoritative(self) -> None:
        record = self.promotion()
        self.assertEqual(
            [],
            validator.validate_promotion_record(
                record,
                self.contract,
                current_producer_inventory_sha256=self.contract["inventory"]["producer_sha256"],
                current_source_sha256=record["source_sha256"],
                current_output_sha256=record["output_sha256"],
            ),
        )
        self.assertFalse(record["authoritative"])
        self.assertFalse(record["trusted_authority_boundary"])

    def test_forged_promotion_cannot_mint_authority_or_self_review(self) -> None:
        record = self.promotion()
        record["authoritative"] = True
        record["trusted_authority_boundary"] = True
        record["signature"] = record["output_sha256"]
        record["reviewer"] = record["producer_author"]
        errors = validator.validate_promotion_record(
            record,
            self.contract,
            current_producer_inventory_sha256=self.contract["inventory"]["producer_sha256"],
        )
        self.assertTrue(any("EP021" in error and "authoritative" in error for error in errors), errors)
        self.assertTrue(any("EP021" in error and "signature" in error for error in errors), errors)
        self.assertTrue(any("EP023" in error and "differ" in error for error in errors), errors)

    def test_missing_review_commit_is_rejected_against_repository(self) -> None:
        record = self.promotion()
        record["reviewed_commit"] = "1" * 40
        errors = validator.validate_promotion_record(
            record,
            self.contract,
            current_producer_inventory_sha256=self.contract["inventory"]["producer_sha256"],
            root=ROOT,
        )
        self.assertTrue(any("EP025" in error and "does not exist" in error for error in errors), errors)

    def test_review_commit_policy_is_pinned(self) -> None:
        contract = copy.deepcopy(self.contract)
        contract["promotion_policy"]["reviewed_commit_policy"] = "exists-only"
        errors = self.validate(contract)
        self.assertTrue(any("EP025" in error and "ancestor of current HEAD" in error for error in errors), errors)

    def test_promotion_required_checks_and_digests_cannot_be_weakened(self) -> None:
        contract = copy.deepcopy(self.contract)
        contract["promotion_policy"]["required_passed_checks"] = []
        contract["promotion_policy"]["admitted_required_fields"] = ["reviewed_commit"]
        errors = self.validate(contract)
        self.assertTrue(any("EP023" in error and "pinned promotion/admission policy" in error for error in errors), errors)

    def test_git_environment_spoof_does_not_redirect_commit_verification(self) -> None:
        record = self.promotion()
        with mock.patch.dict(os.environ, {"GIT_DIR": "/definitely/missing/git-dir"}, clear=False):
            errors = validator.validate_promotion_record(
                record,
                self.contract,
                current_producer_inventory_sha256=self.contract["inventory"]["producer_sha256"],
                root=ROOT,
            )
        self.assertEqual([], errors)

    def test_existing_but_unreachable_review_commit_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            subprocess.run(["git", "init", "-q", str(root)], check=True)
            environment = dict(os.environ)
            environment.update(
                GIT_AUTHOR_NAME="Test Author", GIT_AUTHOR_EMAIL="author@example.invalid",
                GIT_COMMITTER_NAME="Test Committer", GIT_COMMITTER_EMAIL="committer@example.invalid",
            )
            tree = subprocess.run(
                ["git", "-C", str(root), "mktree"], input="", check=True,
                capture_output=True, text=True, env=environment,
            ).stdout.strip()
            head = subprocess.run(
                ["git", "-C", str(root), "commit-tree", tree], input="head\n", check=True,
                capture_output=True, text=True, env=environment,
            ).stdout.strip()
            subprocess.run(["git", "-C", str(root), "update-ref", "refs/heads/main", head], check=True, env=environment)
            subprocess.run(["git", "-C", str(root), "symbolic-ref", "HEAD", "refs/heads/main"], check=True, env=environment)
            unreachable = subprocess.run(
                ["git", "-C", str(root), "commit-tree", tree], input="unreachable\n", check=True,
                capture_output=True, text=True, env=environment,
            ).stdout.strip()
            record = self.promotion()
            record["reviewed_commit"] = unreachable
            errors = validator.validate_promotion_record(
                record,
                self.contract,
                current_producer_inventory_sha256=self.contract["inventory"]["producer_sha256"],
                root=root,
            )
        self.assertTrue(any("EP025" in error and "not reachable" in error for error in errors), errors)

    def test_current_byte_reader_rejects_symlinked_ancestor(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            real = root / "real"
            real.mkdir()
            (real / "record.json").write_text("{}\n", encoding="utf-8")
            (root / "link").symlink_to(real, target_is_directory=True)
            with self.assertRaisesRegex(ValueError, "no-follow traversal rejected"):
                validator._read_current_regular(root / "link" / "record.json", root)

    def test_stale_promotion_and_failed_checks_are_rejected(self) -> None:
        record = self.promotion()
        record["producer_inventory_sha256"] = "sha256:" + "9" * 64
        record["source_sha256"] = "sha256:" + "8" * 64
        record["checks"]["current-output-digest"] = "failed"
        errors = validator.validate_promotion_record(
            record,
            self.contract,
            current_producer_inventory_sha256=self.contract["inventory"]["producer_sha256"],
            current_source_sha256="sha256:" + "2" * 64,
        )
        self.assertTrue(any("EP022" in error and "stale" in error for error in errors), errors)
        self.assertTrue(any("EP022" in error and "current source bytes" in error for error in errors), errors)
        self.assertTrue(any("EP023" in error and "current-output-digest" in error for error in errors), errors)

    def test_promotion_source_and_output_must_match_selected_producer(self) -> None:
        record = self.promotion()
        record["producer_source"] = "tools/verify_development.py"
        record["output_path"] = "docs/review-ledger.md"
        errors = validator.validate_promotion_record(
            record,
            self.contract,
            current_producer_inventory_sha256=self.contract["inventory"]["producer_sha256"],
        )
        self.assertTrue(any("EP024" in error and "producer_source" in error for error in errors), errors)
        self.assertTrue(any("EP024" in error and "output_path" in error for error in errors), errors)

    def test_admitted_promotion_requires_commit_reviewer_checks_and_digests(self) -> None:
        record = self.promotion()
        record["reviewed_commit"] = "short"
        record["reviewer"] = ""
        record["checks"] = {}
        record["output_sha256"] = "not-a-digest"
        errors = validator.validate_promotion_record(
            record,
            self.contract,
            current_producer_inventory_sha256=self.contract["inventory"]["producer_sha256"],
        )
        self.assertTrue(any("EP023" in error and "reviewed_commit" in error for error in errors), errors)
        self.assertTrue(any("EP023" in error and "reviewer" in error for error in errors), errors)
        self.assertTrue(any("EP023" in error and "checks" in error for error in errors), errors)
        self.assertTrue(any("EP020" in error and "output_sha256" in error for error in errors), errors)


if __name__ == "__main__":
    unittest.main()
