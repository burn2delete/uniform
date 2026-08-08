#!/usr/bin/env python3
"""Focused tests for the Python tooling-layer contract."""

from __future__ import annotations

import copy
import json
import os
from pathlib import Path
import subprocess
import sys
import unittest
from unittest import mock


ROOT = Path(__file__).resolve().parents[2]
TOOLS = ROOT / "tools"
if str(TOOLS) not in sys.path:
    sys.path.insert(0, str(TOOLS))

import validate_python_tooling_contract as validator


class PythonToolingContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.contract = validator.load_json(ROOT / "contracts" / "python-tooling.json")
        cls.inventory = validator.discover_python_inventory(ROOT)

    def validate(
        self,
        contract: dict | None = None,
        *,
        inventory: list[str] | None = None,
        source_overrides: dict[str, str] | None = None,
        readme_text: str | None = None,
    ) -> list[str]:
        return validator.validate_contract(
            contract if contract is not None else copy.deepcopy(self.contract),
            root=ROOT,
            inventory=inventory if inventory is not None else list(self.inventory),
            source_overrides=source_overrides,
            readme_text=readme_text,
        )

    @staticmethod
    def component(contract: dict, identifier: str) -> dict:
        return next(item for item in contract["components"] if item["id"] == identifier)

    def test_repository_contract_passes(self) -> None:
        self.assertEqual([], self.validate())

    def test_shared_heavy_lock_is_one_least_privilege_support_component(self) -> None:
        support = self.component(self.contract, "resource-lock-support")
        process = self.component(self.contract, "process-development-tools")
        self.assertEqual(["tools/shared_heavy_lock.py"], support["includes"])
        self.assertEqual("tooling-support", support["category"])
        self.assertEqual("none", support["authority_ceiling"])
        self.assertEqual([], support["allowed_dependency_categories"])
        self.assertEqual(["filesystem-read", "filesystem-write"], support["effects"])
        self.assertEqual(["tooling-support"], process["allowed_dependency_categories"])

    def test_duplicate_json_key_is_rejected(self) -> None:
        with self.assertRaises(validator.DuplicateKeyError):
            json.loads('{"schema_version": 1, "schema_version": 2}', object_pairs_hook=validator._object_no_duplicates)

    def test_inventory_addition_fails_closed_even_when_pattern_matches(self) -> None:
        inventory = list(self.inventory) + ["src/gravity/unreviewed_new_module.py"]
        errors = self.validate(
            inventory=inventory,
            source_overrides={"src/gravity/unreviewed_new_module.py": "VALUE = 1\n"},
        )
        self.assertTrue(any("inventory_count" in error for error in errors), errors)
        self.assertTrue(any("inventory_sha256" in error for error in errors), errors)

    def test_overlapping_component_classification_is_rejected(self) -> None:
        contract = copy.deepcopy(self.contract)
        self.component(contract, "semantic-package")["includes"] = ["src/gravity/*.py"]
        errors = self.validate(contract)
        self.assertTrue(any("must match exactly one component" in error for error in errors), errors)

    def test_unsafe_component_pattern_is_rejected(self) -> None:
        contract = copy.deepcopy(self.contract)
        self.component(contract, "semantic-library")["includes"] = ["../src/gravity/*.py"]
        errors = self.validate(contract)
        self.assertTrue(any("repository-relative" in error for error in errors), errors)

    def test_dependency_edge_drift_is_rejected(self) -> None:
        contract = copy.deepcopy(self.contract)
        contract["scope"]["dependency_edge_sha256"] = "sha256:" + "0" * 64
        errors = self.validate(contract)
        self.assertTrue(any("dependency_edge_sha256" in error for error in errors), errors)

    def test_semantic_tool_import_and_direction_are_rejected(self) -> None:
        path = "src/gravity/reader.py"
        source = (ROOT / path).read_text(encoding="utf-8") + "\nimport tools.output_publication\n"
        errors = self.validate(source_overrides={path: source})
        self.assertTrue(any("forbidden category edge" in error for error in errors), errors)
        self.assertTrue(any("imports forbidden roots: tools" in error for error in errors), errors)

    def test_semantic_write_effect_is_rejected(self) -> None:
        path = "src/gravity/reader.py"
        source = (ROOT / path).read_text(encoding="utf-8") + "\nfrom pathlib import Path\nPath('x').write_text('x')\n"
        errors = self.validate(source_overrides={path: source})
        self.assertTrue(any("filesystem-write" in error and "semantic" in error for error in errors), errors)
        self.assertTrue(any("top-level effects" in error for error in errors), errors)

    def test_common_mutation_and_static_process_aliases_are_rejected(self) -> None:
        path = "src/gravity/reader.py"
        base = (ROOT / path).read_text(encoding="utf-8")
        source = base + """
from pathlib import Path as P
import os
from tempfile import NamedTemporaryFile as temporary_file
launch = os.fork
def hidden_effects():
    P('x').rmdir()
    temporary_file()
    launch()
"""
        errors = self.validate(source_overrides={path: source})
        self.assertTrue(any("filesystem-write" in error for error in errors), errors)
        self.assertTrue(any("process" in error for error in errors), errors)

    def test_network_import_is_rejected(self) -> None:
        path = "src/gravity/reader.py"
        source = (ROOT / path).read_text(encoding="utf-8") + "\nimport socket\n"
        errors = self.validate(source_overrides={path: source})
        self.assertTrue(any("network use is forbidden" in error for error in errors), errors)

    def test_dependency_cycle_is_rejected(self) -> None:
        path = "src/gravity/reader.py"
        source = (ROOT / path).read_text(encoding="utf-8") + "\nimport gravity.core\n"
        errors = self.validate(source_overrides={path: source})
        self.assertTrue(any(error.startswith("dependencies: cycle:") for error in errors), errors)

    def test_validator_output_policy_parity_is_enforced(self) -> None:
        contract = copy.deepcopy(self.contract)
        component = self.component(contract, "isolated-artifact-validators")
        component["output_classes"] = ["stdout-diagnostic"]
        errors = self.validate(contract)
        self.assertTrue(any("lacks isolated output class" in error for error in errors), errors)

    def test_isolated_generator_cannot_bypass_shared_atomic_writer(self) -> None:
        path = "tools/generate_full_language_coverage_matrix.py"
        source = (ROOT / path).read_text(encoding="utf-8").replace(
            "atomic_write_json(", "unshared_json_writer("
        ).replace("atomic_write_text(", "unshared_text_writer(")
        errors = self.validate(source_overrides={path: source})
        self.assertTrue(any("never calls a shared atomic writer" in error for error in errors), errors)

    def test_isolated_generator_direct_write_is_rejected(self) -> None:
        path = "tools/generate_full_language_coverage_matrix.py"
        source = (ROOT / path).read_text(encoding="utf-8") + "\ndef direct_write(path):\n    Path(path).write_text('x')\n"
        errors = self.validate(source_overrides={path: source})
        self.assertTrue(any("isolated output performs direct filesystem writes" in error for error in errors), errors)

    def test_output_validator_must_depend_on_publication_primitive(self) -> None:
        path = "tools/validate_reader.py"
        source = (ROOT / path).read_text(encoding="utf-8").replace(
            "if __package__:\n"
            "    from .output_publication import atomic_write_json\n"
            "else:\n"
            "    from output_publication import atomic_write_json\n",
            "atomic_write_json = None\n",
        )
        errors = self.validate(source_overrides={path: source})
        self.assertTrue(any("lacks output_publication dependency" in error for error in errors), errors)

    def test_reviewed_source_generator_must_be_serialized(self) -> None:
        contract = copy.deepcopy(self.contract)
        self.component(contract, "reviewed-source-generators")["execution_mode"] = "parallel-safe"
        errors = self.validate(contract)
        self.assertTrue(any("must be coordinator-serialized" in error for error in errors), errors)

    def test_python_authority_grant_is_rejected(self) -> None:
        contract = copy.deepcopy(self.contract)
        contract["constraints"]["python_authority_granted"] = True
        self.component(contract, "process-development-tools")["authority_ceiling"] = "reviewed"
        errors = self.validate(contract)
        self.assertTrue(any("must remain false" in error for error in errors), errors)
        self.assertTrue(any("cannot grant authority" in error or "must be declared" in error for error in errors), errors)

    def test_unresolved_semantic_policy_cannot_authorize_edits(self) -> None:
        contract = copy.deepcopy(self.contract)
        policy = next(item for item in contract["policies"] if item["id"] == "unresolved-semantic-source")
        policy["authorizes_edits"] = True
        errors = self.validate(contract)
        self.assertTrue(any("unresolved policy must not authorize edits" in error for error in errors), errors)

    def test_semantic_component_cannot_replace_unresolved_ownership(self) -> None:
        contract = copy.deepcopy(self.contract)
        component = self.component(contract, "semantic-library")
        component["source_path_policy_refs"] = ["reviewed-central-routing"]
        errors = self.validate(contract)
        self.assertTrue(any("must retain only the unresolved" in error for error in errors), errors)

    def test_semantic_root_cannot_be_reclassified_as_tooling(self) -> None:
        contract = copy.deepcopy(self.contract)
        component = self.component(contract, "semantic-library")
        component["category"] = "orchestration"
        errors = self.validate(contract)
        self.assertTrue(any("semantic-root path has a non-semantic category" in error for error in errors), errors)

    def test_none_output_class_is_exclusive(self) -> None:
        contract = copy.deepcopy(self.contract)
        component = self.component(contract, "semantic-package")
        component["output_classes"] = ["none", "stdout-diagnostic"]
        errors = self.validate(contract)
        self.assertTrue(any("none must be the only output class" in error for error in errors), errors)

    def test_readme_parity_is_fail_closed(self) -> None:
        errors = self.validate(readme_text="# Python Tooling Layer\n")
        self.assertTrue(any("required_statements" in error for error in errors), errors)

    def test_source_size_and_parser_recursion_fail_structured(self) -> None:
        path = "src/gravity/reader.py"
        oversized = "VALUE = '" + "x" * validator.MAX_SOURCE_BYTES + "'\n"
        errors = self.validate(source_overrides={path: oversized})
        self.assertTrue(any("source exceeds" in error for error in errors), errors)

        parse_errors: list[str] = []
        with mock.patch.object(validator.ast, "parse", side_effect=RecursionError("too deep")):
            trees = validator._parse_sources(ROOT, [path], parse_errors, {path: "VALUE = 1\n"})
        self.assertEqual({}, trees)
        self.assertTrue(any("too deep" in error for error in parse_errors), parse_errors)

    def test_categories_are_disjoint_and_representative(self) -> None:
        expected = {
            "src/gravity/reader.py": "semantic",
            "src/gravity/ai_document_coverage.py": "semantic-coverage",
            "tools/output_publication.py": "tooling-support",
            "tools/validate_reader.py": "validator",
            "tools/verify_development.py": "orchestration",
            "tools/generate_gravity_docs.py": "reviewed-generator",
            "tools/tests/test_validate_project_structure.py": "test",
        }
        for path, category in expected.items():
            matches = [
                component
                for component in self.contract["components"]
                if validator._component_matches(component, path)
            ]
            self.assertEqual(1, len(matches), path)
            self.assertEqual(category, matches[0]["category"], path)

    def test_bounded_import_smoke_has_no_output_or_jvm(self) -> None:
        code = """
import contextlib
import io
import sys
from pathlib import Path
root = Path(sys.argv[1])
sys.path[:0] = [str(root / 'src'), str(root / 'tools'), str(root / 'tools/tests')]
modules = [
    'gravity.reader',
    'gravity.ai_document_coverage',
    'output_publication',
    'validate_core_lowering',
    'verify_development',
    'generate_gravity_docs',
    'test_validate_project_structure',
]
stdout = io.StringIO()
stderr = io.StringIO()
with contextlib.redirect_stdout(stdout), contextlib.redirect_stderr(stderr):
    for module in modules:
        __import__(module)
if stdout.getvalue() or stderr.getvalue():
    raise SystemExit('import emitted output')
print('import smoke passed')
"""
        environment = os.environ.copy()
        environment["PYTHONDONTWRITEBYTECODE"] = "1"
        completed = subprocess.run(
            [sys.executable, "-c", code, str(ROOT)],
            cwd=ROOT,
            env=environment,
            check=False,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            timeout=15,
        )
        self.assertEqual(0, completed.returncode, completed.stderr)
        self.assertEqual("import smoke passed\n", completed.stdout)


if __name__ == "__main__":
    unittest.main()
