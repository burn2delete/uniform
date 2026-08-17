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
STAGE0_COMPONENT_PATH = ROOT / "contracts" / "stage0-clojure-components.json"


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

    def stage0_errors_for(self, mutate) -> list[str]:
        contract = json.loads(STAGE0_COMPONENT_PATH.read_text(encoding="utf-8"))
        mutate(contract)
        errors: list[str] = []
        validator._validate_stage0_component_contract(self.manifest, errors, contract)
        return errors

    def test_canonical_manifest_is_valid(self) -> None:
        self.assertEqual([], validator.validate_manifest(self.manifest))

    def test_python_semantic_support_is_reviewed_coordinator_path_outside_stage_modules(self) -> None:
        policy = next(
            item
            for item in self.manifest["path_policy"]["policies"]
            if item["id"] == "reviewed-python-semantic-support"
        )
        self.assertEqual("reviewed", policy["kind"])
        self.assertEqual("master-coordinator", policy["owner"])
        self.assertEqual(["src/gravity/"], policy["patterns"])
        self.assertTrue(policy["editable"])
        self.assertTrue(policy["review_required"])
        self.assertNotIn("src/gravity/reader.py", self.manifest["ownership"]["module_paths"])

    def test_python_semantic_support_cannot_be_assigned_to_stage_module_paths(self) -> None:
        def mutate(manifest: dict) -> None:
            manifest["ownership"]["module_paths"]["src/gravity/reader.py"] = "master-coordinator"

        errors = self.errors_for(mutate)
        self.assertTrue(
            any("src/gravity support paths must remain outside ownership.module_paths" in error for error in errors),
            errors,
        )

    def test_python_semantic_support_policy_is_required_even_without_owner_reference(self) -> None:
        def mutate(manifest: dict) -> None:
            manifest["path_policy"]["policies"] = [
                item
                for item in manifest["path_policy"]["policies"]
                if item["id"] != "reviewed-python-semantic-support"
            ]
            coordinator = next(item for item in manifest["ownership"]["owners"] if item["id"] == "master-coordinator")
            coordinator["path_policy_ids"].remove("reviewed-python-semantic-support")

        errors = self.errors_for(mutate)
        self.assertTrue(any("missing required policy 'reviewed-python-semantic-support'" in error for error in errors), errors)

    def test_python_semantic_support_policy_shape_is_exact(self) -> None:
        fields_and_values = {
            "owner": "sh-reader",
            "reviewer": "sh-reader",
            "editable": False,
            "review_required": False,
            "allow_overlap": True,
        }
        for field, value in fields_and_values.items():
            def mutate(manifest: dict, field=field, value=value) -> None:
                policy = next(
                    item
                    for item in manifest["path_policy"]["policies"]
                    if item["id"] == "reviewed-python-semantic-support"
                )
                policy[field] = value

            errors = self.errors_for(mutate)
            self.assertTrue(any(f"must set {field}=" in error or "coordinator-owned" in error for error in errors), errors)

    def test_python_semantic_support_requires_exact_coordinator_claim(self) -> None:
        for duplicate in (False, True):
            def mutate(manifest: dict, duplicate=duplicate) -> None:
                coordinator = next(
                    item
                    for item in manifest["ownership"]["owners"]
                    if item["id"] == "master-coordinator"
                )
                if duplicate:
                    coordinator["path_policy_ids"].append(
                        "reviewed-python-semantic-support"
                    )
                else:
                    coordinator["path_policy_ids"].remove(
                        "reviewed-python-semantic-support"
                    )

            errors = self.errors_for(mutate)
            self.assertTrue(
                any("must claim 'reviewed-python-semantic-support' exactly once" in error for error in errors),
                errors,
            )

    def test_python_semantic_support_cannot_be_assigned_to_a_stage0_slice(self) -> None:
        def mutate(manifest: dict) -> None:
            manifest["slices"][0]["path_policy_ids"].append(
                "reviewed-python-semantic-support"
            )

        errors = self.errors_for(mutate)
        self.assertTrue(
            any("src/gravity semantic support must remain outside Stage0 slices" in error for error in errors),
            errors,
        )

    def test_python_semantic_support_matches_normative_ownership_prefix(self) -> None:
        routing, generated, surfaces, python_support, owners, errors = (
            validator.parse_normative_ownership()
        )
        self.assertEqual([], errors)
        self.assertEqual(["src/gravity/"], python_support)

    def test_python_semantic_support_normative_prefix_is_required_once(self) -> None:
        source = validator.NORMATIVE_OWNERSHIP.read_text(encoding="utf-8")
        marker = ':python-semantic-support-prefixes\n  ["src/gravity/"]\n'
        for replacement in ("", marker + marker):
            with self.subTest(replacement=replacement), tempfile.TemporaryDirectory() as directory:
                path = Path(directory) / "ownership.edn"
                path.write_text(source.replace(marker, replacement, 1), encoding="utf-8")
                *_, errors = validator.parse_normative_ownership(path)
                self.assertTrue(
                    any("python-semantic-support-prefixes" in error for error in errors),
                    errors,
                )

    def test_python_semantic_support_normative_path_drift_is_rejected(self) -> None:
        source = validator.NORMATIVE_OWNERSHIP.read_text(encoding="utf-8")
        source = source.replace(
            ':python-semantic-support-prefixes\n  ["src/gravity/"]',
            ':python-semantic-support-prefixes\n  ["src/gravity-legacy/"]',
            1,
        )
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "ownership.edn"
            path.write_text(source, encoding="utf-8")
            errors: list[str] = []
            validator._validate_normative_ownership_parity(
                self.manifest, errors, path
            )
        self.assertTrue(
            any(
                "reviewed-python-semantic-support" in error
                and "patterns differ" in error
                for error in errors
            ),
            errors,
        )

    def test_stage0_component_contract_projection_is_valid(self) -> None:
        contract = validator.load_stage0_component_contract()
        errors: list[str] = []
        validator._validate_stage0_component_contract(self.manifest, errors, contract)
        self.assertEqual([], errors)

    def test_p15_coordinator_reservations_are_exact_and_not_components(self) -> None:
        contract = validator.load_stage0_component_contract()
        self.assertEqual(
            validator.STAGE0_COORDINATOR_INTEGRATION_RESERVATIONS,
            contract["coordinator_integration_reservations"],
        )
        component_paths = {
            item[kind]["path"]
            for item in contract["components"]
            for kind in ("source", "test")
        }
        reserved_paths = {
            path
            for item in contract["coordinator_integration_reservations"]
            for path in item["paths"]
        }
        self.assertTrue(reserved_paths.isdisjoint(component_paths))

    def test_p15_coordinator_reservation_mutation_fails_closed(self) -> None:
        errors = self.stage0_errors_for(
            lambda contract: contract["coordinator_integration_reservations"][0]["paths"].pop()
        )
        self.assertTrue(
            any("coordinator_integration_reservations" in error for error in errors),
            errors,
        )

    def test_stage0_leaf_execution_groups_are_exact_and_exhaustive(self) -> None:
        contract = validator.load_stage0_component_contract()
        by_id = {component["id"]: component for component in contract["components"]}
        expected_by_id = validator.STAGE0_LEAF_EXECUTION_GROUP_BY_COMPONENT
        self.assertEqual(47, len(expected_by_id))
        self.assertEqual(
            validator.STAGE0_LEAF_EXECUTION_GROUP_COUNTS,
            {
                group: sum(
                    component["leaf_execution_group"] == group
                    for component in contract["components"]
                )
                for group in validator.STAGE0_LEAF_EXECUTION_GROUPS
            },
        )
        for component_id, component in by_id.items():
            expected = expected_by_id.get(component_id)
            self.assertEqual(expected, component["leaf_execution_group"], component_id)
            self.assertEqual(
                component["test"]["lane"] == "bootstrap-free",
                component["leaf_execution_group"] is not None,
                component_id,
            )

    def test_leaf_execution_group_is_distinct_from_semantic_stage0_group(self) -> None:
        contract = validator.load_stage0_component_contract()
        by_id = {component["id"]: component for component in contract["components"]}
        expected_distinctions = {
            "digest": ("compatibility-support", "foundation-reader"),
            "syntax-object-stream": ("compiler", "foundation-reader"),
            "syntax-origin": ("compiler", "foundation-reader"),
            "module-analysis": ("compiler", "foundation-reader"),
            "compiler-verification-shared": ("compatibility-support", "compiler"),
            "darwin-publication": ("compatibility-support", "compiler"),
            "pass-cache": ("compatibility-support", "compiler"),
        }
        for component_id, expected in expected_distinctions.items():
            component = by_id[component_id]
            self.assertEqual(expected, (component["stage0_group"], component["leaf_execution_group"]))

    def test_stage0_leaf_execution_group_drift_is_rejected(self) -> None:
        def mutate(contract: dict) -> None:
            by_id = {component["id"]: component for component in contract["components"]}
            by_id["digest"]["leaf_execution_group"] = "compiler"
            by_id["c10-safety-analysis"]["leaf_execution_group"] = "foundation-reader"

        errors = self.stage0_errors_for(mutate)
        self.assertTrue(any("leaf_execution_group" in error and "digest" in error for error in errors), errors)
        self.assertTrue(any("leaf_execution_group" in error and "c10-safety-analysis" in error for error in errors), errors)

    def test_stage0_leaf_execution_group_must_match_test_lane(self) -> None:
        def mutate(contract: dict) -> None:
            by_id = {component["id"]: component for component in contract["components"]}
            by_id["digest"]["leaf_execution_group"] = None
            by_id["diagnostics"]["leaf_execution_group"] = "foundation-reader"

        errors = self.stage0_errors_for(mutate)
        self.assertTrue(
            any("leaf_execution_group" in error and "bootstrap-free" in error for error in errors),
            errors,
        )

    def test_stage0_leaf_execution_group_rejects_unknown_value(self) -> None:
        errors = self.stage0_errors_for(
            lambda contract: contract["components"][1].__setitem__(
                "leaf_execution_group", "unknown-group"
            )
        )
        self.assertTrue(any("unknown leaf execution group" in error for error in errors), errors)

    def test_strict_json_rejects_duplicate_object_keys(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "duplicate.json"
            path.write_text('{"schema_version": 1, "schema_version": 1}', encoding="utf-8")
            with self.assertRaises(validator.ManifestError):
                validator.load_manifest(path)

    def test_strict_json_rejects_nonstandard_constants(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "nan.json"
            path.write_text('{"schema_version": NaN}', encoding="utf-8")
            with self.assertRaises(validator.ManifestError):
                validator.load_manifest(path)

    def test_stage0_component_duplicate_id_is_rejected(self) -> None:
        errors = self.stage0_errors_for(
            lambda contract: contract["components"][1].__setitem__(
                "id", contract["components"][2]["id"]
            )
        )
        self.assertTrue(any("duplicate component id" in error for error in errors), errors)

    def test_stage0_component_static_source_dependency_drift_is_rejected(self) -> None:
        errors = self.stage0_errors_for(
            lambda contract: contract["components"][1].__setitem__(
                "direct_source_dependencies", []
            )
        )
        self.assertTrue(any("static ns :require dependencies" in error for error in errors), errors)

    def test_stage0_leaf_bootstrap_source_dependency_is_rejected(self) -> None:
        def mutate(contract: dict) -> None:
            dependencies = contract["components"][1]["direct_source_dependencies"]
            dependencies.append("bootstrap")
            dependencies.sort()

        errors = self.stage0_errors_for(mutate)
        self.assertTrue(any("may not depend on bootstrap" in error for error in errors), errors)

    def test_stage0_namespace_compatibility_authority_drift_is_rejected(self) -> None:
        def mutate(contract: dict) -> None:
            for component in contract["components"]:
                if component["id"] == "c5-name-resolution":
                    component["authority"]["compatibility_only"] = False
                    component["authority"]["ceiling"] = "none"
                    return
            self.fail("c5 component not found")

        errors = self.stage0_errors_for(mutate)
        self.assertTrue(any("reviewed namespace contract" in error for error in errors), errors)

    def test_stage0_module_owner_must_match_component_and_policy(self) -> None:
        def mutate(manifest: dict) -> None:
            path = "bootstrap/clojure/src/gravity/c10_safety_analysis.clj"
            manifest["ownership"]["module_paths"][path] = "sh-reader"

        errors = self.errors_for(mutate)
        self.assertTrue(any("project module_paths owner mismatch" in error for error in errors), errors)

    def test_stage0_edn_projection_has_exact_reserved_and_compatibility_shapes(self) -> None:
        reserved, compatibility, errors = validator.parse_stage0_component_ownership()
        self.assertEqual([], errors)
        self.assertEqual(53, len(reserved))
        self.assertEqual(5, len(compatibility))
        self.assertEqual(sorted(compatibility), compatibility)

    def test_stage0_edn_projection_rejects_duplicate_reserved_leaf(self) -> None:
        source = validator.NORMATIVE_OWNERSHIP.read_text(encoding="utf-8")
        source = source.replace(
            '  "bootstrap" :master-coordinator\n',
            '  "bootstrap" :master-coordinator\n  "bootstrap" :master-coordinator\n',
            1,
        )
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "ownership.edn"
            path.write_text(source, encoding="utf-8")
            reserved, compatibility, errors = validator.parse_stage0_component_ownership(path)
        self.assertEqual(
            {"bootstrap": "master-coordinator"},
            {key: value for key, value in reserved.items() if key == "bootstrap"},
        )
        self.assertEqual(5, len(compatibility))
        self.assertTrue(any("repeats key" in error for error in errors), errors)

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

    def test_normative_central_routing_policy_parity_is_enforced(self) -> None:
        def mutate(manifest: dict) -> None:
            for policy in manifest["path_policy"]["policies"]:
                if policy["id"] == "reviewed-central-routing":
                    policy["patterns"].append("README.md")
                    break

        errors = self.errors_for(mutate)
        self.assertTrue(any("reviewed-central-routing" in error for error in errors), errors)

    def test_stage0_coordinator_support_paths_are_exact_and_non_leaf(self) -> None:
        manifest = validator.load_manifest()
        module_paths = manifest["ownership"]["module_paths"]
        self.assertEqual(
            {
                "bootstrap/clojure/test/gravity/bootstrap_compatibility/c2_test.clj",
                "bootstrap/clojure/test/gravity/bootstrap_compatibility/c3_test.clj",
    "bootstrap/clojure/test/gravity/bootstrap_compatibility/c4_test.clj",
    "bootstrap/clojure/test/gravity/bootstrap_compatibility/c5_test.clj",
    "bootstrap/clojure/test/gravity/bootstrap_compatibility/c6_test.clj",
    "bootstrap/clojure/test/gravity/bootstrap_compatibility/c7_test.clj",
    "bootstrap/clojure/test/gravity/bootstrap_compatibility/c8_test.clj",
    "bootstrap/clojure/test/gravity/bootstrap_compatibility/c9_test.clj",
    "bootstrap/clojure/test/gravity/bootstrap_compatibility/c10_test.clj",
    "bootstrap/clojure/test/gravity/bootstrap_compatibility/c11_test.clj",
    "bootstrap/clojure/test/gravity/bootstrap_compatibility/c12_test.clj",
    "bootstrap/clojure/test/gravity/bootstrap_compatibility/c13_test.clj",
    "bootstrap/clojure/test/gravity/bootstrap_compatibility/c14_test.clj",
    "bootstrap/clojure/test/gravity/bootstrap_compatibility/c15_test.clj",
    "bootstrap/clojure/test/gravity/bootstrap_compatibility/c16_test.clj",
    "bootstrap/clojure/test/gravity/bootstrap_compatibility/c17_test.clj",
    "bootstrap/clojure/test/gravity/bootstrap_compatibility/c18_test.clj",
                "bootstrap/clojure/test/gravity/bootstrap_compatibility/core_ast_lowering_test.clj",
                "bootstrap/clojure/test/gravity/bootstrap_compatibility/module_analysis_test.clj",
                "bootstrap/clojure/test/gravity/bootstrap_compatibility/profile_validation_test.clj",
                "bootstrap/clojure/test/gravity/bootstrap_compatibility/capability_validation_test.clj",
                "bootstrap/clojure/test/gravity/bootstrap_free_leaf_test_runner.clj",
                "bootstrap/clojure/test/gravity/development_test_runner.clj",
                "bootstrap/clojure/test/gravity/self_hosting_test_runner.clj",
            },
            validator.STAGE0_COORDINATOR_SUPPORT_PATHS,
        )
        component_paths = {
            component[section]["path"]
            for component in validator.load_stage0_component_contract()["components"]
            for section in ("source", "test")
        }
        for path in validator.STAGE0_COORDINATOR_SUPPORT_PATHS:
            self.assertEqual("master-coordinator", module_paths[path])
            self.assertNotIn(path, component_paths)

    def test_stage0_coordinator_support_path_owner_drift_is_rejected(self) -> None:
        path = "bootstrap/clojure/test/gravity/bootstrap_compatibility/c2_test.clj"

        def mutate(manifest: dict) -> None:
            manifest["ownership"]["module_paths"][path] = "sh-reader"

        errors = self.errors_for(mutate)
        self.assertTrue(any("coordinator support path" in error for error in errors), errors)

    def test_normative_central_routing_owner_cannot_be_transferred(self) -> None:
        def mutate(manifest: dict) -> None:
            for policy in manifest["path_policy"]["policies"]:
                if policy["id"] == "reviewed-central-routing":
                    policy["owner"] = "sh-reader"
                    break
            for owner in manifest["ownership"]["owners"]:
                if owner["id"] == "master-coordinator":
                    owner["path_policy_ids"].remove("reviewed-central-routing")
                elif owner["id"] == "sh-reader":
                    owner["path_policy_ids"].append("reviewed-central-routing")

        errors = self.errors_for(mutate)
        self.assertTrue(any("must set 'owner' to 'master-coordinator'" in error for error in errors), errors)

    def test_normative_generated_evidence_policy_parity_is_enforced(self) -> None:
        def mutate(manifest: dict) -> None:
            for policy in manifest["path_policy"]["policies"]:
                if policy["id"] == "generated-evidence":
                    policy["patterns"] = ["docs/artifacts/", "target/validation/"]
                    break

        errors = self.errors_for(mutate)
        self.assertTrue(any("generated-evidence" in error for error in errors), errors)

    def test_normative_generated_evidence_cannot_become_editable_source(self) -> None:
        def mutate(manifest: dict) -> None:
            for policy in manifest["path_policy"]["policies"]:
                if policy["id"] in {"generated-evidence", "generated-coverage"}:
                    policy["kind"] = "reviewed"
                    policy["editable"] = True
                    policy["reviewer"] = "master-coordinator"
                    policy.pop("generator", None)

        errors = self.errors_for(mutate)
        self.assertTrue(any("must set 'kind' to 'generated'" in error for error in errors), errors)

    def test_normative_projection_rejects_reader_discard_and_comments(self) -> None:
        source = validator.NORMATIVE_OWNERSHIP.read_text(encoding="utf-8")
        for replacement in (
            "#_:central-routing\n  [",
            "; hidden policy\n  :central-routing\n  [",
        ):
            with self.subTest(replacement=replacement), tempfile.TemporaryDirectory() as directory:
                path = Path(directory) / "ownership.edn"
                path.write_text(
                    source.replace(":central-routing\n  [", replacement, 1),
                    encoding="utf-8",
                )
                *_, errors = validator.parse_normative_ownership(path)
                self.assertTrue(any("reader" in error or "comments" in error for error in errors), errors)

    def test_normative_projection_fails_closed_on_unrecognized_edn(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "ownership.edn"
            path.write_text(
                "{:schema :gravity/self-hosting-slice-ownership-v1 "
                ":integration-owner :master-coordinator}\n",
                encoding="utf-8",
            )
            routing, generated, surfaces, python_support, owners, errors = validator.parse_normative_ownership(path)
        self.assertEqual([], routing)
        self.assertEqual([], generated)
        self.assertEqual([], surfaces)
        self.assertEqual([], python_support)
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
