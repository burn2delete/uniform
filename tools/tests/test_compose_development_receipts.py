#!/usr/bin/env python3
"""Focused tests for read-only development receipt composition."""

from __future__ import annotations

from contextlib import redirect_stderr, redirect_stdout
import copy
import hashlib
import io
import json
from pathlib import Path
import sys
import tempfile
import unittest
from unittest import mock

TOOLS = Path(__file__).resolve().parents[1]
if str(TOOLS) not in sys.path:
    sys.path.insert(0, str(TOOLS))

import compose_development_receipts as composer
import verify_development as verifier


def canonical(value: object) -> str:
    return json.dumps(value, ensure_ascii=True, sort_keys=True, separators=(",", ":"))


def manifest() -> dict:
    def check(check_id: str, dependencies: list[str]) -> dict:
        return {
            "id": check_id, "lane": "focused", "command": ["tool", check_id],
            "inputs": [f"{check_id}.txt"], "depends_on": dependencies,
            "cost": "cheap", "lock": None, "exclusive": False, "fresh": False,
            "authority": "none", "daemonization": "forbidden",
        }
    return {
        "schema_version": 1, "name": "composition-test",
        "lanes": {"preflight": {}, "focused": {}, "heavy-candidate": {}},
        "checks": [check("leaf", ["base"]), check("base", []), check("side", ["base"])],
    }


class CompositionTests(unittest.TestCase):
    def setUp(self) -> None:
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name).resolve()
        self.manifest = manifest()
        digest = hashlib.sha256(canonical(self.manifest).encode()).hexdigest()
        self.identity = {"path": "manifest.json", "sha256": digest}

    def tearDown(self) -> None:
        self.temp.cleanup()

    def record(self, check_id: str, *, status: str = "passed") -> dict:
        declaration = next(check for check in self.manifest["checks"] if check["id"] == check_id)
        identity = verifier.check_identity(declaration, self.root)
        record = {
            "id": check_id, "lane": declaration["lane"], "command": declaration["command"],
            "depends_on": declaration["depends_on"], "lock": declaration["lock"],
            "exclusive": declaration["exclusive"], "cost": declaration["cost"],
            "fresh": declaration["fresh"], "timeout_seconds": identity["timeout_seconds"],
            "command_identity": identity["command"],
            "inputs": identity["inputs"],
            "status": status,
            "authority": "fresh-command-pass-non-authoritative" if status == "passed" else "non-authoritative",
            "returncode": 0, "stdout": "volatile output", "stderr": "",
            "started_at": "volatile-start", "finished_at": "volatile-finish", "duration_ms": 1.0,
        }
        record["cache_key"] = composer._cache_key(self.manifest, composer._check_identity(declaration, record))
        return record

    def receipt(self, *records: dict) -> dict:
        return {
            "schema_version": 1, "kind": "development-verification-receipt",
            "manifest": copy.deepcopy(self.identity), "root": str(self.root),
            "status": "passed", "authoritative": False, "checks": list(records),
        }

    def compose(self, *receipts: dict, expected: list[str] | None = None) -> dict:
        return composer.compose_receipts(self.manifest, receipts, expected_ids=expected, root=self.root)

    def assert_invalid(self, receipt: dict, fragment: str) -> None:
        with self.assertRaisesRegex(composer.CompositionError, fragment):
            self.compose(receipt)

    def rekey(self, record: dict, *, bind_supervision: bool = False) -> None:
        declaration = next(check for check in self.manifest["checks"] if check["id"] == record["id"])
        if bind_supervision:
            identity = composer._check_identity(declaration, record)
            unbound = copy.deepcopy(identity)
            unbound["command"]["runtime"].pop("supervision_environment", None)
            marker = hashlib.sha256(("gravity-supervision:" + canonical(unbound)).encode()).hexdigest()[:32]
            record["command_identity"]["runtime"]["supervision_environment"] = {
                "_GRAVITY_VERIFIER_RUN": {"present": True, "sha256": hashlib.sha256(marker.encode()).hexdigest()}
            }
        record["cache_key"] = composer._cache_key(self.manifest, composer._check_identity(declaration, record))

    def test_reordering_and_passed_reused_dedupe_are_deterministic(self) -> None:
        base = self.record("base")
        reused = self.record("base", status="reused")
        reused["finished_at"] = "different"
        leaf = self.record("leaf")
        first = self.compose(self.receipt(leaf, base), self.receipt(reused), expected=["base", "leaf"])
        second = self.compose(self.receipt(reused), self.receipt(base, leaf), expected=["leaf", "base"])
        self.assertEqual(first, second)
        self.assertEqual([item["id"] for item in first["checks"]], ["base", "leaf"])
        self.assertEqual(first["checks"][0]["status"], "satisfied")
        digest_payload = {key: value for key, value in first.items() if key != "composition_sha256"}
        self.assertEqual(first["composition_sha256"], hashlib.sha256(canonical(digest_payload).encode()).hexdigest())

    def test_complete_incomplete_and_dag_order(self) -> None:
        complete = self.compose(self.receipt(self.record("leaf"), self.record("side"), self.record("base")))
        self.assertEqual(complete["status"], "complete")
        self.assertEqual(complete["coverage"]["present"], ["base", "leaf", "side"])
        incomplete = self.compose(self.receipt(self.record("base")), expected=["base", "leaf"])
        self.assertEqual(incomplete["status"], "incomplete")
        self.assertEqual(incomplete["coverage"]["missing"], ["leaf"])
        self.assertFalse(incomplete["authoritative"])

    def test_empty_and_unknown_expected_ids(self) -> None:
        result = self.compose(self.receipt(self.record("base")), expected=[])
        self.assertEqual(result["status"], "complete")
        self.assertEqual(result["coverage"]["expected"], [])
        with self.assertRaisesRegex(composer.CompositionError, "unknown expected checks"):
            self.compose(self.receipt(self.record("base")), expected=["unknown"])

    def test_rejects_duplicate_semantic_and_key_conflicts(self) -> None:
        base = self.record("base")
        changed = self.record("base")
        changed["command_identity"]["runtime"]["environment"]["COMPOSITION_TEST"] = {
            "present": True, "sha256": "1" * 64,
        }
        self.rekey(changed, bind_supervision=True)
        with self.assertRaisesRegex(composer.CompositionError, "conflicting semantic identity or cache_key"):
            self.compose(self.receipt(base), self.receipt(changed))
        leaf = self.record("leaf"); leaf["cache_key"] = base["cache_key"]
        with mock.patch.object(composer, "_cache_key", return_value=base["cache_key"]):
            with self.assertRaisesRegex(composer.CompositionError, "conflicting semantic identity"):
                self.compose(self.receipt(base, leaf))

    def test_rejects_identity_tampering_with_retained_key(self) -> None:
        for field, replacement in (("command_identity", {"argv": ["other"]}), ("inputs", [])):
            record = self.record("base")
            record[field] = replacement
            with self.subTest(field=field), self.assertRaisesRegex(composer.CompositionError, "invalid keys"):
                self.compose(self.receipt(record))

    def test_rejects_recomputed_key_for_malformed_internal_identity(self) -> None:
        cases: list[tuple[str, dict, str]] = []
        record = self.record("base"); record["command_identity"] = "bad"; cases.append(("command-string", record, "command_identity"))
        record = self.record("base"); record["inputs"] = "bad"; cases.append(("inputs-string", record, "inputs"))
        record = self.record("base"); record["command_identity"]["runtime"].pop("supervision_environment"); cases.append(("supervision-missing", record, "runtime has invalid keys"))
        record = self.record("base"); record["command_identity"]["runtime"]["supervision_environment"] = {}; cases.append(("supervision-empty", record, "invalid keys"))
        record = self.record("base"); record["command_identity"]["runtime"]["supervision_environment"]["_GRAVITY_VERIFIER_RUN"]["sha256"] = "0" * 64; cases.append(("supervision-wrong", record, "supervision binding"))
        record = self.record("base"); record["command_identity"]["env"] = {"X": {"present": False, "sha256": "0" * 64}}; cases.append(("env-binding", record, "present lowercase"))
        record = self.record("base"); record["inputs"]["files"][0] = {"path": "base.txt", "exists": True, "sha256": None}; cases.append(("file-record", record, "existing file shape"))
        record = self.record("base"); record["inputs"]["sha256"] = "0" * 64; cases.append(("input-digest", record, "does not match"))
        for name, record, fragment in cases:
            self.rekey(record)
            with self.subTest(name=name), self.assertRaisesRegex(composer.CompositionError, fragment):
                self.compose(self.receipt(record))

    def test_accepts_verifier_generated_identity(self) -> None:
        result = self.compose(self.receipt(self.record("base")), expected=["base"])
        self.assertEqual(result["status"], "complete")

    def test_verifier_and_composer_share_exact_semantic_identity(self) -> None:
        declaration = next(check for check in self.manifest["checks"] if check["id"] == "base")
        declaration["fresh"] = True
        declaration["timeout_seconds"] = 17
        self.identity["sha256"] = hashlib.sha256(canonical(self.manifest).encode()).hexdigest()
        record = self.record("base")
        self.assertEqual(
            composer._check_identity(declaration, record),
            verifier.check_identity(declaration, self.root),
        )
        self.assertEqual(
            record["cache_key"],
            verifier.cache_key(self.manifest, declaration, self.root),
        )
        self.assertEqual(record["timeout_seconds"], 17.0)

    def test_old_receipt_cannot_compose_after_fresh_or_timeout_change(self) -> None:
        declaration = next(check for check in self.manifest["checks"] if check["id"] == "base")

        old_fresh_record = self.record("base")
        declaration["fresh"] = True
        self.identity["sha256"] = hashlib.sha256(canonical(self.manifest).encode()).hexdigest()
        self.assert_invalid(self.receipt(old_fresh_record), "declaration does not match")

        declaration["fresh"] = False
        old_timeout_record = self.record("base")
        declaration["timeout_seconds"] = 23
        self.identity["sha256"] = hashlib.sha256(canonical(self.manifest).encode()).hexdigest()
        self.assert_invalid(self.receipt(old_timeout_record), "declaration does not match")

        current_record = self.record("base")
        current_record.pop("timeout_seconds")
        self.assert_invalid(self.receipt(current_record), "missing timeout_seconds")

    def test_rejects_noncanonical_receipt_declaration_scalars(self) -> None:
        for field, value, fragment in (
            ("fresh", 0, "fresh declaration metadata must be boolean"),
            ("timeout_seconds", True, "finite positive float"),
            ("timeout_seconds", 1, "finite positive float"),
        ):
            record = self.record("base")
            record[field] = value
            with self.subTest(field=field, value=value):
                self.assert_invalid(self.receipt(record), fragment)

    def test_rejects_rekeyed_noncanonical_or_wrong_input_selection(self) -> None:
        mutations = [
            ("empty", []),
            ("unrelated", [{"path": "other.txt", "exists": False, "sha256": None}]),
            ("dot-segment", [{"path": "dir/./x", "exists": False, "sha256": None}]),
            ("repeated-separator", [{"path": "dir//x", "exists": False, "sha256": None}]),
        ]
        for name, files in mutations:
            record = self.record("base")
            record["inputs"]["files"] = files
            record["inputs"]["sha256"] = hashlib.sha256(canonical(files).encode()).hexdigest()
            self.rekey(record, bind_supervision=True)
            with self.subTest(name=name), self.assertRaises(composer.CompositionError):
                self.compose(self.receipt(record))

    def test_rejects_receipt_after_current_declared_input_changes(self) -> None:
        record = self.record("base")
        (self.root / "base.txt").write_text("changed after receipt", encoding="utf-8")
        self.assert_invalid(self.receipt(record), "does not match current declared inputs")

    def test_rejects_missing_union_dependencies(self) -> None:
        self.assert_invalid(self.receipt(self.record("leaf")), "missing union dependencies")

    def test_rejects_status_authority_unknown_and_bad_top_level(self) -> None:
        cases = []
        bad = self.record("base"); bad["status"] = "failed"; cases.append((self.receipt(bad), "status"))
        bad = self.record("base"); bad["authority"] = "non-authoritative"; cases.append((self.receipt(bad), "authority"))
        bad = self.record("base"); bad["id"] = "unknown"; cases.append((self.receipt(bad), "unknown check"))
        bad_receipt = self.receipt(self.record("base")); bad_receipt["status"] = "planned"; cases.append((bad_receipt, "status must be passed"))
        bad_receipt = self.receipt(self.record("base")); bad_receipt["authoritative"] = True; cases.append((bad_receipt, "authoritative=false"))
        for receipt, fragment in cases:
            with self.subTest(fragment=fragment):
                self.assert_invalid(receipt, fragment)

    def test_rejects_bad_returncode_output_and_timing(self) -> None:
        mutations = [
            ("returncode", 99, "returncode"), ("returncode", None, "returncode"),
            ("stdout", [], "stdout"), ("stderr", None, "stderr"),
            ("started_at", "", "started_at"), ("finished_at", None, "finished_at"),
            ("duration_ms", float("inf"), "duration_ms"), ("duration_ms", -1, "duration_ms"),
            ("duration_ms", True, "duration_ms"),
        ]
        for field, value, fragment in mutations:
            record = self.record("base"); record[field] = value
            with self.subTest(field=field, value=value):
                self.assert_invalid(self.receipt(record), fragment)
        record = self.record("base"); record.pop("returncode")
        self.assert_invalid(self.receipt(record), "returncode")

    def test_rejects_impossible_reuse_and_duplicate_id(self) -> None:
        base = next(check for check in self.manifest["checks"] if check["id"] == "base")
        base["fresh"] = True
        self.identity["sha256"] = hashlib.sha256(canonical(self.manifest).encode()).hexdigest()
        self.assert_invalid(self.receipt(self.record("base", status="reused")), "cannot be reused")
        base["fresh"] = False; base["lane"] = "heavy-candidate"; base["authority"] = "declared"
        self.identity["sha256"] = hashlib.sha256(canonical(self.manifest).encode()).hexdigest()
        self.assert_invalid(self.receipt(self.record("base", status="reused")), "cannot be reused")
        base["lane"] = "focused"; base["authority"] = "none"
        self.identity["sha256"] = hashlib.sha256(canonical(self.manifest).encode()).hexdigest()
        duplicate = self.record("base")
        self.assert_invalid(self.receipt(duplicate, copy.deepcopy(duplicate)), "duplicate check id")

    def test_rejects_manifest_root_declaration_and_key_mismatch(self) -> None:
        cases = []
        bad = self.receipt(self.record("base")); bad["manifest"]["sha256"] = "0" * 64; cases.append((bad, "manifest digest"))
        bad = self.receipt(self.record("base")); bad["root"] = str(self.root / "other"); cases.append((bad, "root does not match"))
        record = self.record("base"); record["lane"] = "preflight"; cases.append((self.receipt(record), "declaration"))
        record = self.record("base"); record["cache_key"] = "A" * 64; cases.append((self.receipt(record), "invalid cache_key"))
        for receipt, fragment in cases:
            with self.subTest(fragment=fragment):
                self.assert_invalid(receipt, fragment)

    def test_rejects_malformed_manifest_identity(self) -> None:
        identities = [
            ({"path": "manifest.json"}, "exactly"),
            ({"path": "manifest.json", "sha256": self.identity["sha256"], "extra": 1}, "exactly"),
            ({"path": 7, "sha256": self.identity["sha256"]}, "string or null"),
            ({"path": "manifest.json", "sha256": "A" * 64}, "lowercase"),
        ]
        for identity, fragment in identities:
            receipt = self.receipt(self.record("base")); receipt["manifest"] = identity
            with self.subTest(identity=identity):
                self.assert_invalid(receipt, fragment)

    def test_cli_success_failure_and_bounds(self) -> None:
        manifest_path = self.root / "manifest.json"
        receipt_path = self.root / "receipt.json"
        manifest_path.write_text(json.dumps(self.manifest), encoding="utf-8")
        receipt_path.write_text(json.dumps(self.receipt(self.record("base"))), encoding="utf-8")
        out = io.StringIO()
        with redirect_stdout(out):
            code = composer.main(["--manifest", str(manifest_path), "--root", str(self.root),
                                  "--expected-check", "base", str(receipt_path)])
        self.assertEqual(code, 0)
        self.assertEqual(json.loads(out.getvalue())["status"], "complete")
        with redirect_stdout(io.StringIO()):
            code = composer.main(["--manifest", str(manifest_path), "--root", str(self.root),
                                  "--expected-check", "leaf", str(receipt_path)])
        self.assertEqual(code, 1)
        receipt_path.write_text('{"x":1,"x":2}', encoding="utf-8")
        with redirect_stderr(io.StringIO()):
            self.assertEqual(composer.main(["--manifest", str(manifest_path), str(receipt_path)]), 2)
        with mock.patch.object(composer, "MAX_RECEIPTS", 1), redirect_stderr(io.StringIO()):
            self.assertEqual(composer.main([str(receipt_path), str(receipt_path)]), 2)
        with mock.patch.object(composer, "MAX_TOTAL_RECEIPT_BYTES", 1), redirect_stderr(io.StringIO()):
            self.assertEqual(composer.main([str(receipt_path)]), 2)
        receipt_path.write_text('{"x":NaN}', encoding="utf-8")
        with self.assertRaisesRegex(composer.CompositionError, "invalid JSON constant"):
            composer.load_receipt(receipt_path)
        oversized = self.root / "large.json"
        oversized.write_bytes(b" " * (composer.MAX_RECEIPT_BYTES + 1))
        with self.assertRaisesRegex(composer.CompositionError, "exceeds"):
            composer.load_receipt(oversized)


if __name__ == "__main__":
    unittest.main()
