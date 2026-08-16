#!/usr/bin/env python3
"""Focused tests for the Python tooling-layer contract."""

from __future__ import annotations

import copy
import ast
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

    def test_process_tree_telemetry_is_one_nonauthoritative_support_component(self) -> None:
        support = self.component(self.contract, "process-tree-telemetry-support")
        self.assertEqual(["tools/process_tree_telemetry.py"], support["includes"])
        self.assertEqual("tooling-support", support["category"])
        self.assertEqual("none", support["authority_ceiling"])
        self.assertEqual([], support["allowed_dependency_categories"])
        self.assertEqual(["process", "host-metrics"], support["effects"])

    def test_coordinator_process_cycle_is_exact_and_explicit(self) -> None:
        self.assertEqual(
            [[
                "tools/run_stage3_verification.py",
                "tools/verify_development.py",
                "tools/run_stage3_verification.py",
            ]],
            self.contract["constraints"]["allowed_dependency_cycles"],
        )

    def test_duplicate_json_key_is_rejected(self) -> None:
        with self.assertRaises(validator.DuplicateKeyError):
            json.loads('{"schema_version": 1, "schema_version": 2}', object_pairs_hook=validator._object_no_duplicates)

    def test_os_open_effect_keeps_read_only_census_least_privilege(self) -> None:
        def effects(flags: str) -> set[str]:
            tree = ast.parse(f"import os\nos.open('artifact', {flags})\n")
            return validator.observed_effects(tree)

        self.assertNotIn("filesystem-write", effects("os.O_RDONLY | os.O_NOFOLLOW"))
        for flags in (
            "os.O_WRONLY",
            "os.O_RDWR",
            "os.O_CREAT",
            "getattr(os, 'O_RDWR', 0)",
            "getattr(os, selected_flag, 0)",
            "selected_flags",
        ):
            self.assertIn("filesystem-write", effects(flags), flags)

        admitted_alias = ast.parse(
            "import os as operating\n"
            "operating.open('artifact', operating.O_RDONLY | operating.O_NOFOLLOW)\n"
        )
        self.assertNotIn("filesystem-write", validator.observed_effects(admitted_alias))
        hostile_sources = (
            "import os\ndef read(os):\n os.open('artifact', os.O_RDONLY)\n",
            "import os\ndef read():\n os = object()\n os.open('artifact', os.O_RDONLY)\n",
            "import os\ndef outer():\n os = object()\n def read():\n  os.open('artifact', os.O_RDONLY)\n",
            "import os\ndef read(values):\n return [os.open('artifact', os.O_RDONLY) for os in values]\n",
            "import os\ndef read(getattr):\n os.open('artifact', getattr(os, 'O_RDONLY', 0))\n",
            "import os\ngetattr = lambda *args: 0\ndef read():\n os.open('artifact', getattr(os, 'O_RDONLY', 0))\n",
            "import os as operating\ndef read(operating):\n operating.open('artifact', operating.O_RDONLY)\n",
            "import os\nflags = object()\nos.open('artifact', flags.O_RDONLY)\n",
            "os = object()\ndef read(value=os.open('artifact', os.O_RDONLY)):\n import os\n",
            "os = object()\n@os.open('artifact', os.O_RDONLY)\ndef read():\n import os\n",
            "os = object()\nclass Reader(os.open('artifact', os.O_RDONLY)):\n import os\n",
            "os = object()\nclass Reader:\n import os\n def read(self):\n  os.open('artifact', os.O_RDONLY)\n",
            "os = object()\nclass Reader:\n os.open('artifact', os.O_RDONLY)\n import os\n",
            "class Reader:\n import os\n os.open('artifact', os.O_RDONLY)\n",
            "def outer():\n import os\n def middle():\n  def inner():\n   nonlocal os\n   os = object()\n  inner()\n middle()\n os.open('artifact', os.O_RDONLY)\n",
            "def outer():\n import os\n def inner():\n  nonlocal os\n  (os := object())\n inner()\n os.open('artifact', os.O_RDONLY)\n",
            "def outer():\n import os\n def inner(values):\n  nonlocal os\n  [(os := value) for value in values]\n inner([object()])\n os.open('artifact', os.O_RDONLY)\n",
            "import os\n*os, = [object()]\nos.open('artifact', os.O_RDONLY)\n",
            "import os\nfor *os, in [[object()]]:\n os.open('artifact', os.O_RDONLY)\n",
            "def unrelated():\n import os as operating\ndef read():\n operating.open('artifact', operating.O_RDONLY)\n",
        )
        for source in hostile_sources:
            self.assertIn(
                "filesystem-write",
                validator.observed_effects(ast.parse(source)),
                source,
            )

    def test_os_write_only_whitelists_proven_subprocess_stdin(self) -> None:
        pipe_source = "import os\nimport subprocess\nprocess = subprocess.Popen([])\nos.write(process.stdin.fileno(), b'x')\n"
        self.assertNotIn("filesystem-write", validator.observed_effects(ast.parse(pipe_source)))
        arbitrary_source = "import os\nhandle = open('artifact', 'rb')\nos.write(handle.fileno(), b'x')\n"
        self.assertIn("filesystem-write", validator.observed_effects(ast.parse(arbitrary_source)))
        reassigned_source = "import os\nimport subprocess\nprocess = subprocess.Popen([])\nprocess = object()\nos.write(process.stdin.fileno(), b'x')\n"
        self.assertIn("filesystem-write", validator.observed_effects(ast.parse(reassigned_source)))
        reassigned_stdin_source = "import os\nimport subprocess\nprocess = subprocess.Popen([])\nprocess.stdin = open('artifact', 'rb')\nos.write(process.stdin.fileno(), b'x')\n"
        self.assertIn("filesystem-write", validator.observed_effects(ast.parse(reassigned_stdin_source)))
        setattr_stdin_source = "import os\nimport subprocess\nprocess = subprocess.Popen([])\nsetattr(process, 'stdin', open('artifact', 'rb'))\nos.write(process.stdin.fileno(), b'x')\n"
        self.assertIn("filesystem-write", validator.observed_effects(ast.parse(setattr_stdin_source)))
        closure_stdin_source = "import os\nimport subprocess\ndef outer():\n process = subprocess.Popen([])\n def inner():\n  process.stdin = object()\n inner()\n os.write(process.stdin.fileno(), b'x')\n"
        self.assertIn(
            "filesystem-write",
            validator.observed_effects(ast.parse(closure_stdin_source)),
        )
        closure_setattr_source = "import os\nimport subprocess\ndef outer():\n process = subprocess.Popen([])\n def inner():\n  setattr(process, 'stdin', object())\n inner()\n os.write(process.stdin.fileno(), b'x')\n"
        self.assertIn(
            "filesystem-write",
            validator.observed_effects(ast.parse(closure_setattr_source)),
        )
        shadowed_parameter_source = "import os\nimport subprocess\nprocess = subprocess.Popen([])\ndef write(process):\n os.write(process.stdin.fileno(), b'x')\n"
        self.assertIn(
            "filesystem-write",
            validator.observed_effects(ast.parse(shadowed_parameter_source)),
        )
        sibling_scope_source = "import os\nimport subprocess\ndef create():\n process = subprocess.Popen([])\ndef write(process):\n os.write(process.stdin.fileno(), b'x')\n"
        self.assertIn(
            "filesystem-write",
            validator.observed_effects(ast.parse(sibling_scope_source)),
        )
        sibling_import_source = "import os\ndef unrelated():\n import subprocess as child_process\ndef write():\n process = child_process.Popen([])\n os.write(process.stdin.fileno(), b'x')\n"
        self.assertIn(
            "filesystem-write",
            validator.observed_effects(ast.parse(sibling_import_source)),
        )
        shadowed_module_source = "import os\nimport subprocess\ndef write(subprocess):\n process = subprocess.Popen([])\n os.write(process.stdin.fileno(), b'x')\n"
        self.assertIn(
            "filesystem-write",
            validator.observed_effects(ast.parse(shadowed_module_source)),
        )
        shadowed_os_parameter = "import os\nimport subprocess\ndef write(os):\n process = subprocess.Popen([])\n os.write(process.stdin.fileno(), b'x')\n"
        self.assertIn(
            "filesystem-write",
            validator.observed_effects(ast.parse(shadowed_os_parameter)),
        )
        shadowed_os_local = "import os\nimport subprocess\ndef write():\n os = object()\n process = subprocess.Popen([])\n os.write(process.stdin.fileno(), b'x')\n"
        self.assertIn(
            "filesystem-write",
            validator.observed_effects(ast.parse(shadowed_os_local)),
        )
        shadowed_os_alias = "import os as operating\nimport subprocess\ndef write(operating):\n process = subprocess.Popen([])\n operating.write(process.stdin.fileno(), b'x')\n"
        self.assertIn(
            "filesystem-write",
            validator.observed_effects(ast.parse(shadowed_os_alias)),
        )
        imported_alias_pipe = "import os as operating\nimport subprocess as child_process\ndef write():\n process = child_process.Popen([])\n operating.write(process.stdin.fileno(), b'x')\n"
        self.assertNotIn(
            "filesystem-write",
            validator.observed_effects(ast.parse(imported_alias_pipe)),
        )
        local_import_arbitrary = "def write(handle):\n import os as operating\n operating.write(handle, b'x')\n"
        self.assertIn(
            "filesystem-write",
            validator.observed_effects(ast.parse(local_import_arbitrary)),
        )
        local_import_pipe = "def write():\n import os as operating\n import subprocess as child_process\n process = child_process.Popen([])\n operating.write(process.stdin.fileno(), b'x')\n"
        local_import_effects = validator.observed_effects(ast.parse(local_import_pipe))
        self.assertNotIn("filesystem-write", local_import_effects)
        self.assertIn("process", local_import_effects)
        comprehension_shadow = "import os\nimport subprocess\ndef write(handles):\n process = subprocess.Popen([])\n return [os.write(process.stdin.fileno(), b'x') for os in handles]\n"
        self.assertIn(
            "filesystem-write",
            validator.observed_effects(ast.parse(comprehension_shadow)),
        )
        for pattern_shadow in (
            "import os, subprocess\ndef write(value):\n process = subprocess.Popen([])\n match value:\n  case process:\n   os.write(process.stdin.fileno(), b'x')\n",
            "import os, subprocess\ndef write(value):\n process = subprocess.Popen([])\n match value:\n  case [*process]:\n   os.write(process.stdin.fileno(), b'x')\n",
            "import os, subprocess\ndef write(value):\n process = subprocess.Popen([])\n match value:\n  case {**os}:\n   os.write(process.stdin.fileno(), b'x')\n",
            "import os, subprocess\ndef write(value):\n process = subprocess.Popen([])\n match value:\n  case Box(value=process):\n   os.write(process.stdin.fileno(), b'x')\n",
            "import os, subprocess\ndef write(value):\n process = subprocess.Popen([])\n match value:\n  case [process] | {'p': process}:\n   os.write(process.stdin.fileno(), b'x')\n",
        ):
            self.assertIn(
                "filesystem-write",
                validator.observed_effects(ast.parse(pattern_shadow)),
            )
        for walrus_shadow in (
            "import os, subprocess\ndef write(values):\n process = subprocess.Popen([])\n return [os.write(process.stdin.fileno(), b'x') for value in values if (process := value)]\n",
            "import os, subprocess\ndef write(values):\n process = subprocess.Popen([])\n return [os.write(process.stdin.fileno(), b'x') for value in values if (os := value)]\n",
        ):
            self.assertIn(
                "filesystem-write",
                validator.observed_effects(ast.parse(walrus_shadow)),
            )
        local_pipe_source = "import os\nimport subprocess\ndef write():\n process = subprocess.Popen([])\n os.write(process.stdin.fileno(), b'x')\n"
        self.assertNotIn(
            "filesystem-write",
            validator.observed_effects(ast.parse(local_pipe_source)),
        )

    def test_inventory_addition_fails_closed_even_when_pattern_matches(self) -> None:
        inventory = list(self.inventory) + ["src/gravity/unreviewed_new_module.py"]
        errors = self.validate(
            inventory=inventory,
            source_overrides={"src/gravity/unreviewed_new_module.py": "VALUE = 1\n"},
        )
        self.assertTrue(any("inventory_count" in error for error in errors), errors)
        self.assertTrue(any("inventory_sha256" in error for error in errors), errors)

    def test_pipe_proof_obeys_class_declaration_and_definition_scopes(self) -> None:
        negatives = (
            "import os, subprocess\nclass C:\n import os as class_os\n process = subprocess.Popen([])\n def f(self):\n  class_os.write(process.stdin.fileno(), b'x')\n",
            "import os\nclass C:\n import subprocess as child_process\n def f(self):\n  process = child_process.Popen([])\n  os.write(process.stdin.fileno(), b'x')\n",
            "class C:\n import os as operating\n import subprocess as child_process\n def f(self):\n  process = child_process.Popen([])\n  operating.write(process.stdin.fileno(), b'x')\n",
            "import os, subprocess\nos = object()\nprocess = subprocess.Popen([])\ndef f(value=os.write(process.stdin.fileno(), b'x')):\n import os\n return value\n",
            "import os, subprocess\nos = object()\nprocess = subprocess.Popen([])\n@os.write(process.stdin.fileno(), b'x')\ndef f():\n import os\n pass\n",
            "import os, subprocess\nos = object()\nprocess = subprocess.Popen([])\ndef f(value: os.write(process.stdin.fileno(), b'x')):\n import os\n pass\n",
            "import os, subprocess\nos = object()\nprocess = subprocess.Popen([])\nf = lambda value=os.write(process.stdin.fileno(), b'x'): value\n",
            "import os, subprocess\ndef f():\n global os\n os = object()\n process = subprocess.Popen([])\n os.write(process.stdin.fileno(), b'x')\n",
        )
        for source in negatives:
            self.assertIn("filesystem-write", validator.observed_effects(ast.parse(source)))

        class_definition_scope_negative = "import os, subprocess\nos = object()\nprocess = subprocess.Popen([])\n@operating.write(process.stdin.fileno(), b'x')\nclass C(os.write(process.stdin.fileno(), b'x')):\n import os as operating\n"
        self.assertIn(
            "filesystem-write",
            validator.observed_effects(ast.parse(class_definition_scope_negative)),
        )
        class_comprehension_inner = "class C:\n import os\n import subprocess\n process = subprocess.Popen([])\n values = [os.write(process.stdin.fileno(), b'x') for item in ()]\n"
        self.assertIn(
            "filesystem-write",
            validator.observed_effects(ast.parse(class_comprehension_inner)),
        )

        class_body = "class C:\n import os as operating\n import subprocess as child_process\n process = child_process.Popen([])\n operating.write(process.stdin.fileno(), b'x')\n"
        class_comprehension_outer = "class C:\n import os\n import subprocess\n process = subprocess.Popen([])\n values = [item for item in os.write(process.stdin.fileno(), b'x')]\n"
        late_class_bindings = (
            "os = object()\nimport subprocess\nclass C:\n process = subprocess.Popen([])\n os.write(process.stdin.fileno(), b'x')\n import os\n",
            "import os, subprocess\nprocess = object()\nclass C:\n os.write(process.stdin.fileno(), b'x')\n process = subprocess.Popen([])\n",
            "os = object()\nimport subprocess\nprocess = object()\nclass C:\n os.write(process.stdin.fileno(), b'x')\n import os\n process = subprocess.Popen([])\n",
        )
        for source in (class_body, class_comprehension_outer, *late_class_bindings):
            self.assertIn(
                "filesystem-write",
                validator.observed_effects(ast.parse(source)),
                source,
            )
        method_local = "class C:\n def f(self):\n  import os as operating\n  import subprocess as child_process\n  process = child_process.Popen([])\n  operating.write(process.stdin.fileno(), b'x')\n"
        declared_global = "import os, subprocess\ndef f():\n global os, subprocess\n process = subprocess.Popen([])\n os.write(process.stdin.fileno(), b'x')\n"
        declared_nonlocal = "def outer():\n import os as operating\n import subprocess as child_process\n def inner():\n  nonlocal operating, child_process\n  process = child_process.Popen([])\n  operating.write(process.stdin.fileno(), b'x')\n"
        for source in (method_local, declared_global, declared_nonlocal):
            effects = validator.observed_effects(ast.parse(source))
            self.assertNotIn("filesystem-write", effects)
            self.assertIn("process", effects)

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

    def test_semantic_support_policy_cannot_authorize_edits(self) -> None:
        contract = copy.deepcopy(self.contract)
        policy = next(item for item in contract["policies"] if item["id"] == "reviewed-python-semantic-support")
        policy["authorizes_edits"] = True
        errors = self.validate(contract)
        self.assertTrue(any("non-writable" in error for error in errors), errors)

    def test_semantic_component_cannot_replace_reviewed_support_ownership(self) -> None:
        contract = copy.deepcopy(self.contract)
        component = self.component(contract, "semantic-library")
        component["source_path_policy_refs"] = ["reviewed-central-routing"]
        errors = self.validate(contract)
        self.assertTrue(any("must retain only the reviewed external support" in error for error in errors), errors)

    def test_semantic_support_policy_cannot_be_generated_or_unresolved(self) -> None:
        for kind in ("generated", "unresolved"):
            contract = copy.deepcopy(self.contract)
            policy = next(item for item in contract["policies"] if item["id"] == "reviewed-python-semantic-support")
            policy["kind"] = kind
            errors = self.validate(contract)
            self.assertTrue(any("must name a reviewed semantic-support policy" in error for error in errors), errors)

    def test_semantic_support_cannot_receive_authority(self) -> None:
        contract = copy.deepcopy(self.contract)
        self.component(contract, "semantic-library")["authority_ceiling"] = "non-authoritative-observation"
        errors = self.validate(contract)
        self.assertTrue(any("semantic support requires authority ceiling none" in error for error in errors), errors)

    def test_external_semantic_support_policy_shape_and_claim_are_exact(self) -> None:
        project = validator.load_json(ROOT / "contracts" / "project-structure.json")
        mutations = {
            "owner": "sh-reader",
            "reviewer": "sh-reader",
            "editable": False,
            "review_required": False,
            "allow_overlap": True,
        }

        def policy_errors(project_contract: dict) -> list[str]:
            with tempfile.TemporaryDirectory() as directory:
                root = Path(directory)
                contracts = root / "contracts"
                contracts.mkdir()
                (contracts / "project-structure.json").write_text(
                    json.dumps(project_contract), encoding="utf-8"
                )
                errors: list[str] = []
                enums = validator._validate_enums(self.contract, errors)
                validator._validate_policies(self.contract, enums, root, errors)
                return errors

        for field, value in mutations.items():
            changed = copy.deepcopy(project)
            policy = next(
                item
                for item in changed["path_policy"]["policies"]
                if item["id"] == "reviewed-python-semantic-support"
            )
            policy[field] = value
            errors = policy_errors(changed)
            self.assertTrue(
                any(f"must set {field}=" in error for error in errors), errors
            )

        for duplicate in (False, True):
            changed = copy.deepcopy(project)
            coordinator = next(
                item
                for item in changed["ownership"]["owners"]
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
            errors = policy_errors(changed)
            self.assertTrue(
                any("claimed exactly once by master-coordinator" in error for error in errors),
                errors,
            )

        changed = copy.deepcopy(project)
        changed["ownership"]["module_paths"]["src/gravity/reader.py"] = (
            "master-coordinator"
        )
        errors = policy_errors(changed)
        self.assertTrue(
            any("outside ownership.module_paths" in error for error in errors), errors
        )

        changed = copy.deepcopy(project)
        changed["slices"][0]["path_policy_ids"].append(
            "reviewed-python-semantic-support"
        )
        errors = policy_errors(changed)
        self.assertTrue(
            any("outside Stage0 slices" in error for error in errors), errors
        )

    def test_semantic_selector_lists_cannot_weaken_concrete_components(self) -> None:
        contract = copy.deepcopy(self.contract)
        contract["constraints"]["semantic_categories"].remove("semantic")
        self.component(contract, "semantic-library")["authority_ceiling"] = "reviewed"
        errors = self.validate(contract)
        self.assertTrue(any("exact semantic-support boundary" in error for error in errors), errors)
        self.assertTrue(any("semantic support requires authority ceiling none" in error for error in errors), errors)

        contract = copy.deepcopy(self.contract)
        contract["constraints"]["semantic_forbidden_effects"].remove(
            "filesystem-write"
        )
        path = "src/gravity/reader.py"
        source = (ROOT / path).read_text(encoding="utf-8") + (
            "\nfrom pathlib import Path\n"
            "def hidden_write():\n"
            "    Path('x').write_text('x')\n"
        )
        errors = self.validate(contract, source_overrides={path: source})
        self.assertTrue(any("exact semantic-support boundary" in error for error in errors), errors)
        self.assertTrue(any("observed forbidden effects: filesystem-write" in error for error in errors), errors)

        contract = copy.deepcopy(self.contract)
        contract["constraints"]["semantic_forbidden_import_roots"].remove(
            "subprocess"
        )
        source = (ROOT / path).read_text(encoding="utf-8") + "\nimport subprocess\n"
        errors = self.validate(contract, source_overrides={path: source})
        self.assertTrue(any("exact semantic-support boundary" in error for error in errors), errors)
        self.assertTrue(any("imports forbidden roots: subprocess" in error for error in errors), errors)

    def test_semantic_components_cannot_become_inert_guarded_clis(self) -> None:
        contract = copy.deepcopy(self.contract)
        for identifier in ("semantic-library", "semantic-document-coverage"):
            self.component(contract, identifier)["import_safety"] = "guarded-cli"
        overrides = {}
        for path in ("src/gravity/reader.py", "src/gravity/ai_document_coverage.py"):
            overrides[path] = (ROOT / path).read_text(encoding="utf-8") + (
                "\nif __name__ == '__main__':\n"
                "    pass\n"
            )
        errors = self.validate(contract, source_overrides=overrides)
        self.assertTrue(any("must retain import_safety='library'" in error for error in errors), errors)
        self.assertTrue(any("semantic support must not expose a CLI main guard" in error for error in errors), errors)

    def test_semantic_component_partition_and_dependency_shape_are_exact(self) -> None:
        contract = copy.deepcopy(self.contract)
        library = self.component(contract, "semantic-library")
        coverage = self.component(contract, "semantic-document-coverage")
        library["includes"], coverage["includes"] = (
            coverage["includes"],
            library["includes"],
        )
        errors = self.validate(contract)
        self.assertTrue(
            any("semantic support must retain includes=" in error for error in errors),
            errors,
        )

        contract = copy.deepcopy(self.contract)
        self.component(contract, "semantic-library")["includes"] = [
            "src/gravity/reader.py"
        ]
        errors = self.validate(contract)
        self.assertTrue(
            any("semantic support must retain includes=" in error for error in errors),
            errors,
        )

        contract = copy.deepcopy(self.contract)
        self.component(contract, "semantic-library")["role"] = "package-marker"
        errors = self.validate(contract)
        self.assertTrue(
            any("semantic support must retain role='semantic-library'" in error for error in errors),
            errors,
        )

        contract = copy.deepcopy(self.contract)
        self.component(contract, "semantic-document-coverage")[
            "allowed_dependency_categories"
        ] = ["semantic", "orchestration"]
        errors = self.validate(contract)
        self.assertTrue(
            any(
                "semantic support must retain allowed_dependency_categories=['semantic']"
                in error
                for error in errors
            ),
            errors,
        )

    def test_semantic_component_output_and_test_surfaces_are_exact(self) -> None:
        contract = copy.deepcopy(self.contract)
        self.component(contract, "semantic-package")["output_path_policy_refs"] = [
            "generated-evidence"
        ]
        errors = self.validate(contract)
        self.assertTrue(
            any("semantic support must retain output_path_policy_refs=[]" in error for error in errors),
            errors,
        )

        contract = copy.deepcopy(self.contract)
        self.component(contract, "semantic-library")["test_surfaces"] = [
            "import-smoke"
        ]
        errors = self.validate(contract)
        self.assertTrue(
            any(
                "semantic support must retain test_surfaces=['import-smoke', 'validator-cli']"
                in error
                for error in errors
            ),
            errors,
        )

    def test_semantic_root_and_policy_identity_are_exact(self) -> None:
        contract = copy.deepcopy(self.contract)
        contract["constraints"]["semantic_root"] = "src"
        errors = self.validate(contract)
        self.assertTrue(
            any("must retain the exact semantic-support root 'src/gravity'" in error for error in errors),
            errors,
        )

        contract = copy.deepcopy(self.contract)
        alias = "reviewed-python-semantic-support-alias"
        policy = next(
            item
            for item in contract["policies"]
            if item["id"] == "reviewed-python-semantic-support"
        )
        policy["id"] = alias
        contract["constraints"]["semantic_source_policy"] = alias
        for identifier in (
            "semantic-package",
            "semantic-library",
            "semantic-document-coverage",
        ):
            self.component(contract, identifier)["source_path_policy_refs"] = [alias]
        errors = self.validate(contract)
        self.assertTrue(
            any(
                "must retain the exact semantic-support policy 'reviewed-python-semantic-support'"
                in error
                for error in errors
            ),
            errors,
        )
        self.assertTrue(
            any("must name a reviewed semantic-support policy" in error for error in errors),
            errors,
        )

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
