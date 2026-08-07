from __future__ import annotations

import copy
import contextlib
import io
import json
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
        self.assertEqual(33, len(first["owner_path_view"]["policies"]))
        self.assertEqual([], first["changed_path_impact"]["changed_paths"])

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

    def test_coordinator_tooling_and_top_level_outputs_have_conservative_impact(self) -> None:
        for path in (
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
