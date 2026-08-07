from __future__ import annotations

import copy
import json
from pathlib import Path
import sys
import tempfile
import unittest


TOOLS = Path(__file__).resolve().parents[1]
ROOT = TOOLS.parent
sys.path.insert(0, str(TOOLS))

import validate_project_structure as validator  # noqa: E402


MANIFEST_PATH = ROOT / "contracts" / "project-structure.json"


class ProjectStructureValidationTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.manifest = json.loads(MANIFEST_PATH.read_text(encoding="utf-8"))

    def errors_for(self, mutate) -> list[str]:
        candidate = copy.deepcopy(self.manifest)
        mutate(candidate)
        return validator.validate_manifest(candidate)

    def test_canonical_manifest_is_valid(self) -> None:
        self.assertEqual([], validator.validate_manifest(self.manifest))

    def test_canonical_pass_order_is_enforced(self) -> None:
        errors = self.errors_for(
            lambda manifest: manifest["canonical_passes"].__setitem__(
                1, {**manifest["canonical_passes"][1], "order": 3}
            )
        )
        self.assertTrue(any("canonical position 2" in error for error in errors), errors)

    def test_missing_slice_dependency_is_rejected(self) -> None:
        def mutate(manifest: dict) -> None:
            manifest["slices"][1]["depends_on"] = ["SH-NOT-DECLARED"]

        errors = self.errors_for(mutate)
        self.assertTrue(any("unknown dependency 'SH-NOT-DECLARED'" in error for error in errors), errors)

    def test_slice_dependency_cycle_is_rejected(self) -> None:
        def mutate(manifest: dict) -> None:
            manifest["slices"][0]["depends_on"] = ["SH-01"]

        errors = self.errors_for(mutate)
        self.assertTrue(any("dependency cycle" in error for error in errors), errors)

    def test_cross_owner_path_overlap_is_rejected(self) -> None:
        def mutate(manifest: dict) -> None:
            manifest["path_policy"]["policies"].append(
                {
                    "id": "overlap-fixture",
                    "kind": "reviewed",
                    "owner": "sh-reader",
                    "patterns": ["deps.edn"],
                    "editable": True,
                    "review_required": True,
                    "reviewer": "master-coordinator",
                    "allow_overlap": False,
                }
            )

        errors = self.errors_for(mutate)
        self.assertTrue(any("ownership overlap" in error for error in errors), errors)

    def test_wildcard_exact_overlap_is_not_missed(self) -> None:
        wildcard = "bootstrap/clojure/test/gravity/self_hosting/sh*_*.clj"
        exact = "bootstrap/clojure/test/gravity/self_hosting/sh07_authoritative_runner.clj"
        self.assertTrue(validator._patterns_overlap(wildcard, exact))
        self.assertTrue(validator._patterns_overlap(exact, wildcard))

    def test_wildcard_wildcard_overlap_is_not_missed(self) -> None:
        left = "bootstrap/clojure/test/gravity/self_hosting/sh*_*.clj"
        right = "bootstrap/clojure/test/gravity/self_hosting/sh07_*.clj"
        self.assertTrue(validator._patterns_overlap(left, right))
        self.assertFalse(
            validator._patterns_overlap(
                left, "bootstrap/clojure/test/gravity/other_namespace/sh07_*.clj"
            )
        )

    def test_normative_coordinator_owner_parity_is_enforced(self) -> None:
        def mutate(manifest: dict) -> None:
            for policy in manifest["path_policy"]["policies"]:
                if policy["id"] == "reviewed-artifact-module":
                    policy["owner"] = "sh-artifact-emission"
                    break

        errors = self.errors_for(mutate)
        self.assertTrue(any("normative ownership parity" in error for error in errors), errors)

    def test_normative_leaf_module_owner_parity_is_enforced(self) -> None:
        def mutate(manifest: dict) -> None:
            path = "bootstrap/gravity/src/gravity/backend/b2_c_backend_design.gravity"
            manifest["ownership"]["module_paths"][path] = "sh-target-llvm"

        errors = self.errors_for(mutate)
        self.assertTrue(any("module owner mismatch" in error for error in errors), errors)

    def test_normative_projection_fails_closed_on_unrecognized_edn(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "ownership.edn"
            path.write_text(
                "{:schema :gravity/self-hosting-slice-ownership-v1 "
                ":integration-owner :master-coordinator}\n",
                encoding="utf-8",
            )
            surfaces, owners, errors = validator.parse_normative_ownership(path)
        self.assertEqual([], surfaces)
        self.assertEqual({}, owners)
        self.assertTrue(errors)

    def test_malformed_pass_requires_all_boundary_fields(self) -> None:
        def mutate(manifest: dict) -> None:
            del manifest["canonical_passes"][0]["output_artifacts"]

        errors = self.errors_for(mutate)
        self.assertTrue(any("missing required fields: output_artifacts" in error for error in errors), errors)

    def test_adjacent_artifact_continuity_is_required(self) -> None:
        def mutate(manifest: dict) -> None:
            for artifact in manifest["artifacts"]:
                if artifact["id"] == "reader-products":
                    artifact["consumed_by"] = []
                    break

        errors = self.errors_for(mutate)
        self.assertTrue(any("reader-products" in error and "consumer" in error for error in errors), errors)

    def test_authority_is_a_required_policy_with_a_claim_ceiling(self) -> None:
        for entry in self.manifest["canonical_passes"] + self.manifest["slices"]:
            authority = entry["authority"]
            self.assertIn("required_level", authority)
            self.assertIn("maximum_claim", authority)
            self.assertNotIn("level", authority)
            self.assertIn(authority["maximum_claim"], {"contract-boundary-only", "coordination-only"})

        def mutate(manifest: dict) -> None:
            del manifest["slices"][0]["authority"]["maximum_claim"]

        errors = self.errors_for(mutate)
        self.assertTrue(any("unknown maximum claim" in error for error in errors), errors)

    def test_generated_policy_must_name_a_generator(self) -> None:
        def mutate(manifest: dict) -> None:
            for policy in manifest["path_policy"]["policies"]:
                if policy["id"] == "generated-evidence":
                    del policy["generator"]
                    break

        errors = self.errors_for(mutate)
        self.assertTrue(any("generated policy needs a generator" in error for error in errors), errors)


if __name__ == "__main__":
    unittest.main()
