from __future__ import annotations

import copy
import contextlib
import io
import json
import re
from pathlib import Path
import sys
import tempfile
import unittest


TOOLS = Path(__file__).resolve().parents[1]
ROOT = TOOLS.parent
sys.path.insert(0, str(TOOLS))

import render_project_structure as renderer  # noqa: E402


MANIFEST_PATH = ROOT / "contracts" / "project-structure.json"
INVALID_CHANGED_PATHS = (
    "/absolute/file.gravity",
    "C:/drive/file.gravity",
    "C:\\drive\\file.gravity",
    "../outside.gravity",
    "inside/../outside.gravity",
    "./inside.gravity",
    "inside/./file.gravity",
    "inside//file.gravity",
    "inside/file.gravity/",
    "inside/fi\x00le.gravity",
    "inside/*.gravity",
    "inside/file?.gravity",
    "inside/[ab].gravity",
    "inside/{a,b}.gravity",
    "inside\\file.gravity",
)


class ProjectStructureRendererTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.manifest = json.loads(MANIFEST_PATH.read_text(encoding="utf-8"))

    def test_report_is_deterministic_and_has_all_views(self) -> None:
        first = renderer.render_structure(self.manifest)
        second = renderer.render_structure(copy.deepcopy(self.manifest))
        self.assertEqual(first, second)
        self.assertEqual(renderer.stable_json(first), renderer.stable_json(second))
        self.assertEqual(18, first["summary"]["canonical_pass_count"])
        self.assertEqual(30, first["summary"]["slice_count"])
        self.assertEqual(18, len(first["canonical_pass_table"]))
        self.assertEqual(53, len(first["owner_path_view"]["policies"]))
        self.assertEqual([], first["changed_path_impact"]["changed_paths"])
        self.assertIn("contract_identity", first)

    def test_contract_identities_are_deterministic_canonical_and_ordered(self) -> None:
        def reorder_keys(value):
            if isinstance(value, dict):
                return {
                    key: reorder_keys(value[key])
                    for key in reversed(list(value))
                }
            if isinstance(value, list):
                return [reorder_keys(item) for item in value]
            return value

        first = renderer.contract_identity_view(self.manifest)
        second = renderer.contract_identity_view(reorder_keys(copy.deepcopy(self.manifest)))
        self.assertEqual(first, second)
        self.assertEqual("sha256", first["algorithm"])
        self.assertEqual("1", first["schema_version"])
        self.assertEqual(
            "gravity.project-structure.static-contract/v1", first["domain_version"]
        )
        self.assertEqual(
            [item["id"] for item in self.manifest["canonical_passes"]],
            [item["id"] for item in first["canonical_passes"]],
        )
        self.assertEqual(
            sorted(item["id"] for item in self.manifest["artifacts"]),
            [item["id"] for item in first["artifacts"]],
        )
        digests = (
            [first["manifest"]["sha256"]]
            + [item["sha256"] for item in first["canonical_passes"]]
            + [first["canonical_passes_sha256"]]
            + [item["sha256"] for item in first["artifacts"]]
            + [first["artifacts_sha256"]]
        )
        self.assertTrue(all(re.fullmatch(r"[0-9a-f]{64}", digest) for digest in digests))

    def test_artifact_list_reordering_does_not_change_contract_identities(self) -> None:
        candidate = copy.deepcopy(self.manifest)
        candidate["artifacts"].reverse()
        self.assertEqual(
            renderer.contract_identity_view(self.manifest),
            renderer.contract_identity_view(candidate),
        )

    def test_field_local_mutations_change_only_the_relevant_entry_identity(self) -> None:
        baseline = renderer.contract_identity_view(self.manifest)

        pass_candidate = copy.deepcopy(self.manifest)
        pass_candidate["canonical_passes"][0]["name"] += " revised"
        pass_changed = renderer.contract_identity_view(pass_candidate)
        self.assertNotEqual(baseline["manifest"], pass_changed["manifest"])
        self.assertNotEqual(
            baseline["canonical_passes"][0], pass_changed["canonical_passes"][0]
        )
        self.assertEqual(
            baseline["canonical_passes"][1:], pass_changed["canonical_passes"][1:]
        )
        self.assertEqual(baseline["artifacts"], pass_changed["artifacts"])

        artifact_candidate = copy.deepcopy(self.manifest)
        artifact_id = artifact_candidate["artifacts"][0]["id"]
        artifact_candidate["artifacts"][0]["name"] += " revised"
        artifact_changed = renderer.contract_identity_view(artifact_candidate)
        baseline_by_id = {item["id"]: item for item in baseline["artifacts"]}
        changed_by_id = {item["id"]: item for item in artifact_changed["artifacts"]}
        self.assertNotEqual(baseline["manifest"], artifact_changed["manifest"])
        self.assertNotEqual(baseline_by_id[artifact_id], changed_by_id[artifact_id])
        self.assertEqual(
            {key: value for key, value in baseline_by_id.items() if key != artifact_id},
            {key: value for key, value in changed_by_id.items() if key != artifact_id},
        )
        self.assertEqual(baseline["canonical_passes"], artifact_changed["canonical_passes"])

    def test_identity_domains_are_separated(self) -> None:
        value = {"id": "same", "value": [1, 2, 3]}
        self.assertNotEqual(
            renderer._canonical_json_sha256(value, "canonical-pass"),
            renderer._canonical_json_sha256(value, "artifact"),
        )

    def test_identity_hashing_rejects_non_json_nonfinite_and_cyclic_data(self) -> None:
        bad_values = [{"bad": {1, 2}}, {"bad": float("inf")}, {1: "bad-key"}]
        cyclic = []
        cyclic.append(cyclic)
        bad_values.append(cyclic)
        for value in bad_values:
            with self.subTest(value=type(value).__name__):
                with self.assertRaises(renderer.RenderError):
                    renderer._canonical_json_sha256(value, "manifest")

        # The structural validator intentionally ignores extension fields.
        # Identity calculation still fails closed after that validation succeeds.
        manifest_values = [{"bad": {1, 2}}, {"bad": float("nan")}]
        cyclic_extension = []
        cyclic_extension.append(cyclic_extension)
        manifest_values.append(cyclic_extension)
        for value in manifest_values:
            candidate = copy.deepcopy(self.manifest)
            candidate["identity_test_extension"] = value
            with self.assertRaises(renderer.RenderError):
                renderer.contract_identity_view(candidate)

    def test_deep_valid_json_fails_with_stable_api_and_cli_diagnostics(self) -> None:
        nested = []
        cursor = nested
        for _ in range(600):
            child = []
            cursor.append(child)
            cursor = child
        candidate = copy.deepcopy(self.manifest)
        candidate["identity_test_extension"] = nested
        encoded = json.dumps(candidate)
        decoded = json.loads(encoded)
        with self.assertRaisesRegex(renderer.RenderError, "nesting depth"):
            renderer.contract_identity_view(decoded)

        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "deep-valid.json"
            path.write_text(encoded, encoding="utf-8")
            stdout = io.StringIO()
            stderr = io.StringIO()
            with contextlib.redirect_stdout(stdout), contextlib.redirect_stderr(stderr):
                result = renderer.main([str(path), "--section", "identity"])
        self.assertEqual(1, result)
        self.assertEqual("", stdout.getvalue())
        self.assertIn("project structure rendering failed", stderr.getvalue())
        self.assertIn("nesting depth", stderr.getvalue())

    def test_identities_do_not_change_existing_views_or_mutate_manifest(self) -> None:
        candidate = copy.deepcopy(self.manifest)
        before = copy.deepcopy(candidate)
        expected = {
            "summary": renderer.render_summary(candidate),
            "passes": renderer.canonical_pass_table(candidate),
            "waves": renderer.slice_topological_waves(candidate),
            "owners": renderer.owner_path_view(candidate),
        }
        renderer.contract_identity_view(candidate)
        actual = {
            "summary": renderer.render_summary(candidate),
            "passes": renderer.canonical_pass_table(candidate),
            "waves": renderer.slice_topological_waves(candidate),
            "owners": renderer.owner_path_view(candidate),
        }
        self.assertEqual(before, candidate)
        self.assertEqual(expected, actual)

    def test_canonical_pass_table_preserves_d1_order(self) -> None:
        table = renderer.canonical_pass_table(self.manifest)
        self.assertEqual(
            [item["id"] for item in self.manifest["canonical_passes"]],
            [item["id"] for item in table],
        )
        self.assertEqual(list(range(1, 19)), [item["order"] for item in table])

    def test_slice_waves_are_topological_and_stable(self) -> None:
        waves = renderer.slice_topological_waves(self.manifest)
        positions = {
            slice_id: wave["wave"]
            for wave in waves
            for slice_id in wave["slices"]
        }
        self.assertEqual(30, sum(len(wave["slices"]) for wave in waves))
        self.assertEqual(
            [sorted(wave["slices"]) for wave in waves],
            [wave["slices"] for wave in waves],
        )
        for item in self.manifest["slices"]:
            for dependency in item["depends_on"]:
                self.assertLess(positions[dependency], positions[item["id"]])

    def test_selected_slice_waves_include_dependencies(self) -> None:
        waves = renderer.slice_topological_waves(self.manifest, ["SH-06"])
        self.assertEqual(
            [["SH-00"], ["SH-01", "SH-02"], ["SH-03"], ["SH-04"], ["SH-05"], ["SH-06"]],
            [wave["slices"] for wave in waves],
        )

    def test_changed_path_impact_closes_wildcard_owner_and_dependents(self) -> None:
        impact = renderer.changed_path_impact_closure(
            self.manifest,
            ["bootstrap/clojure/test/gravity/self_hosting/sh07_authoritative_runner.clj"],
        )
        self.assertIn("reserved-tests", impact["impacted_policy_ids"])
        self.assertIn("sh-reader", impact["impacted_owners"])
        self.assertIn("SH-03", impact["direct_slices"])
        self.assertIn("SH-07", impact["impacted_slices"])
        self.assertIn("reader-products", impact["impacted_artifacts"])
        self.assertIn("reader", impact["impacted_passes"])

    def test_unowned_changed_paths_are_explicit_and_blocking(self) -> None:
        impact = renderer.changed_path_impact_closure(self.manifest, ["unclaimed/path.txt"])
        self.assertEqual(["unclaimed/path.txt"], impact["unowned_paths"])
        self.assertEqual(["unclaimed/path.txt"], impact["unresolved_paths"])
        self.assertEqual("incomplete", impact["impact_status"])
        self.assertFalse(impact["impact_complete"])
        self.assertTrue(impact["blocking"])
        self.assertEqual([], impact["impacted_owners"])
        self.assertEqual([], impact["impacted_slices"])
        view = renderer.owner_path_view(self.manifest, ["unclaimed/path.txt"])
        self.assertEqual(["unclaimed/path.txt"], view["unowned_paths"])
        self.assertTrue(view["paths"][0]["unowned"])

        identity = impact["impact_identity"]
        self.assertTrue(identity["non_authoritative"])
        self.assertFalse(identity["authorizes_cache_reuse"])
        self.assertFalse(identity["authorizes_verification"])
        self.assertFalse(identity["authorizes_release"])
        self.assertRegex(identity["base_manifest_sha256"], r"^[0-9a-f]{64}$")
        self.assertRegex(identity["sha256"], r"^[0-9a-f]{64}$")
        self.assertTrue(impact["blocking"])

    def test_impact_identity_covers_complete_closure_and_impacted_contracts(self) -> None:
        impact = renderer.changed_path_impact_closure(
            self.manifest,
            ["bootstrap/clojure/test/gravity/self_hosting/sh07_authoritative_runner.clj"],
        )
        identity = impact["impact_identity"]
        closure = copy.deepcopy(impact)
        del closure["impact_identity"]
        expected = renderer._canonical_json_sha256(
            {
                "base_manifest_sha256": identity["base_manifest_sha256"],
                "closure": closure,
            },
            "impact-closure",
        )
        self.assertEqual(expected, identity["sha256"])
        self.assertEqual(
            impact["impacted_passes"],
            [item["id"] for item in identity["impacted_passes"]],
        )
        self.assertEqual(
            impact["impacted_artifacts"],
            [item["id"] for item in identity["impacted_artifacts"]],
        )

    def test_cli_identity_section_and_alias_are_equivalent(self) -> None:
        outputs = []
        for section in ("identity", "identities"):
            stdout = io.StringIO()
            stderr = io.StringIO()
            with contextlib.redirect_stdout(stdout), contextlib.redirect_stderr(stderr):
                result = renderer.main([str(MANIFEST_PATH), "--section", section])
            self.assertEqual(0, result)
            self.assertEqual("", stderr.getvalue())
            payload = json.loads(stdout.getvalue())
            self.assertEqual({"manifest", "contract_identity"}, set(payload))
            outputs.append(payload)
        self.assertEqual(outputs[0], outputs[1])

    def test_coordinator_tooling_and_top_level_outputs_have_conservative_impact(self) -> None:
        for path in (
            "contracts/project-structure.json",
            "tools/validate_math_system.py",
            "target/core-app",
            "target/core-app.gravity-artifact.edn",
        ):
            with self.subTest(path=path):
                impact = renderer.changed_path_impact_closure(self.manifest, [path])
                self.assertEqual([], impact["unowned_paths"])
                self.assertEqual([], impact["unresolved_paths"])
                self.assertTrue(impact["impact_complete"])
                self.assertFalse(impact["blocking"])
                self.assertIn("SH-00", impact["direct_slices"])
                self.assertIn("master-coordinator", impact["impacted_owners"])

    def test_local_path_matcher_has_independent_exact_directory_and_glob_behavior(self) -> None:
        self.assertTrue(renderer._path_pattern_matches("one/file.gravity", "one/file.gravity"))
        self.assertFalse(renderer._path_pattern_matches("one/file.gravity", "one/other.gravity"))
        self.assertTrue(renderer._path_pattern_matches("one/dir/", "one/dir/file.gravity"))
        self.assertFalse(renderer._path_pattern_matches("one/dir/", "one/directory/file.gravity"))
        self.assertTrue(renderer._path_pattern_matches("one/sh-*/", "one/sh-03/file.gravity"))
        self.assertTrue(renderer._path_pattern_matches("one/sh*_*.clj", "one/sh07_test.clj"))
        self.assertFalse(
            renderer._path_pattern_matches("one/sh*_*.clj", "one/sh07/foo_test.clj")
        )
        self.assertTrue(renderer._path_pattern_matches("one/file?.clj", "one/file1.clj"))
        self.assertTrue(renderer._path_pattern_matches("one/file[0-9].clj", "one/file7.clj"))
        self.assertTrue(renderer._path_pattern_matches("one/{a,b}.clj", "one/b.clj"))
        self.assertFalse(renderer._path_pattern_matches("one/{a,b}.clj", "one/c.clj"))
        self.assertFalse(renderer._path_pattern_matches("one/SH*_*.clj", "one/sh07_test.clj"))

    def test_owner_path_view_uses_local_wildcard_matching(self) -> None:
        path = "bootstrap/clojure/test/gravity/self_hosting/sh07_authoritative_runner.clj"
        view = renderer.owner_path_view(self.manifest, [path])
        row = view["paths"][0]
        self.assertEqual(["reserved-tests"], row["policy_ids"])
        self.assertFalse(row["unowned"])

    def test_flat_test_glob_does_not_claim_nested_unowned_path(self) -> None:
        path = "bootstrap/clojure/test/gravity/self_hosting/sh07/foo_test.clj"
        view = renderer.owner_path_view(self.manifest, [path])
        self.assertEqual([], view["paths"][0]["policy_ids"])
        self.assertTrue(view["paths"][0]["unowned"])
        impact = renderer.changed_path_impact_closure(self.manifest, [path])
        self.assertEqual([path], impact["unowned_paths"])
        self.assertEqual([path], impact["unresolved_paths"])
        self.assertTrue(impact["blocking"])

    def test_every_module_owner_without_a_slice_mapping_is_unresolved(self) -> None:
        expected = {
            "bootstrap/gravity/src/gravity/backend/b10_workflow_graph_backend_design.gravity": "sh-target-workflow",
            "bootstrap/gravity/src/gravity/backend/b11_query_relational_backend_design.gravity": "sh-target-query",
            "bootstrap/gravity/src/gravity/backend/b12_mobile_backend_design.gravity": "sh-target-mobile",
            "bootstrap/gravity/src/gravity/backend/b2_c_backend_design.gravity": "sh-target-c",
            "bootstrap/gravity/src/gravity/backend/b3_llvm_backend_design.gravity": "sh-target-llvm",
            "bootstrap/gravity/src/gravity/backend/b5_jvm_backend_design.gravity": "sh-target-jvm",
            "bootstrap/gravity/src/gravity/backend/b6_javascript_typescript_backend_design.gravity": "sh-target-js",
            "bootstrap/gravity/src/gravity/backend/b7_mlir_backend_design.gravity": "sh-target-mlir",
            "bootstrap/gravity/src/gravity/backend/b8_gpu_backend_design.gravity": "sh-target-gpu",
            "bootstrap/gravity/src/gravity/backend/b9_hdl_backend_design.gravity": "sh-target-hdl",
            "bootstrap/gravity/src/gravity/compiler/c16_incremental_compilation_design.gravity": "sh-incremental",
            "bootstrap/gravity/src/gravity/compiler/c17_compiler_plugin_pass_api.gravity": "sh-pass-api",
            "bootstrap/gravity/src/gravity/compiler/c18_compiler_verification_pass_correctness.gravity": "sh-verification",
            "bootstrap/clojure/src/gravity/c16_incremental.clj": "sh-incremental",
            "bootstrap/clojure/src/gravity/c17_plugin.clj": "sh-pass-api",
            "bootstrap/clojure/src/gravity/c18_verification.clj": "sh-verification",
            "bootstrap/clojure/src/gravity/capability_validation.clj": "sh-capability",
            "bootstrap/clojure/src/gravity/compiler_verification_shared.clj": "sh-verification",
            "bootstrap/clojure/src/gravity/pass_execution.clj": "sh-verification",
            "bootstrap/clojure/src/gravity/profile_validation.clj": "sh-profile",
            "bootstrap/clojure/test/gravity/c16_incremental_test.clj": "sh-incremental",
            "bootstrap/clojure/test/gravity/c17_plugin_test.clj": "sh-pass-api",
            "bootstrap/clojure/test/gravity/c18_verification_test.clj": "sh-verification",
            "bootstrap/clojure/test/gravity/capability_validation_test.clj": "sh-capability",
            "bootstrap/clojure/test/gravity/compiler_verification_shared_test.clj": "sh-verification",
            "bootstrap/clojure/test/gravity/pass_execution_test.clj": "sh-verification",
            "bootstrap/clojure/test/gravity/profile_validation_test.clj": "sh-profile",
        }
        slice_owners = {item["owner"] for item in self.manifest["slices"]}
        slice_policy_ids = {
            policy_id
            for item in self.manifest["slices"]
            for policy_id in item["path_policy_ids"]
        }
        bridged_policy_owners = {
            policy["owner"]
            for policy in self.manifest["path_policy"]["policies"]
            if policy["id"] in slice_policy_ids
        }
        actual = {
            path: owner
            for path, owner in self.manifest["ownership"]["module_paths"].items()
            if owner not in slice_owners and owner not in bridged_policy_owners
        }
        self.assertEqual(expected, actual)
        for path, owner in expected.items():
            with self.subTest(path=path):
                impact = renderer.changed_path_impact_closure(self.manifest, [path])
                self.assertEqual([owner], impact["unresolved_owners"])
                self.assertEqual([path], impact["unresolved_paths"])
                self.assertEqual("incomplete", impact["impact_status"])
                self.assertFalse(impact["impact_complete"])
                self.assertTrue(impact["blocking"])
                self.assertEqual([], impact["direct_slices"])
                self.assertEqual([], impact["impacted_slices"])

    def test_changed_paths_must_be_normalized_relative_and_concrete(self) -> None:
        for path in INVALID_CHANGED_PATHS:
            with self.subTest(path=repr(path)):
                with self.assertRaises(renderer.RenderError):
                    renderer.changed_path_impact_closure(self.manifest, [path])
                with self.assertRaises(renderer.RenderError):
                    renderer.owner_path_view(self.manifest, [path])

        valid = "bootstrap/gravity/src/gravity/backend/b2_c_backend_design.gravity"
        impact = renderer.changed_path_impact_closure(self.manifest, [valid, valid])
        self.assertEqual([valid], impact["changed_paths"])

    def test_cli_rejects_invalid_paths_and_fails_closed_on_unresolved_impact(self) -> None:
        unresolved = "bootstrap/gravity/src/gravity/backend/b2_c_backend_design.gravity"
        stdout = io.StringIO()
        stderr = io.StringIO()
        with contextlib.redirect_stdout(stdout), contextlib.redirect_stderr(stderr):
            result = renderer.main(
                [str(MANIFEST_PATH), "--section", "summary", "--changed-path", unresolved]
            )
        self.assertEqual(2, result)
        payload = json.loads(stdout.getvalue())
        self.assertIn("summary", payload)
        self.assertTrue(payload["changed_path_impact"]["blocking"])
        self.assertIn("incomplete", stderr.getvalue())

        for invalid in INVALID_CHANGED_PATHS:
            with self.subTest(path=invalid):
                stdout = io.StringIO()
                stderr = io.StringIO()
                with contextlib.redirect_stdout(stdout), contextlib.redirect_stderr(stderr):
                    result = renderer.main(
                        [str(MANIFEST_PATH), "--changed-path", invalid]
                    )
                self.assertEqual(1, result)
                self.assertEqual("", stdout.getvalue())
                self.assertIn("rendering failed", stderr.getvalue())

    def test_invalid_manifest_fails_closed_before_rendering(self) -> None:
        candidate = copy.deepcopy(self.manifest)
        candidate["canonical_passes"][0]["order"] = 99
        with self.assertRaises(renderer.RenderError):
            renderer.render_structure(candidate)
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "invalid.json"
            path.write_text(json.dumps(candidate), encoding="utf-8")
            self.assertEqual(1, renderer.main([str(path)]))

if __name__ == "__main__":
    unittest.main()
