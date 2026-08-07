from __future__ import annotations

import hashlib
import json
from pathlib import Path
import tempfile
import unittest
from unittest import mock

from tools import run_sh07_authoritative_modules as sh07
from tools import run_stage3_verification as stage3


class Stage3WrapperTests(unittest.TestCase):
    def setUp(self) -> None:
        self.lock = Path(f"/private/tmp/gravity-stage3-test-{self.id().replace('/', '-')}.lock")
        self.lock.unlink(missing_ok=True)
        self.lock_patch = mock.patch.object(stage3, "CANONICAL_LOCK", self.lock)
        self.lock_text_patch = mock.patch.object(stage3, "CANONICAL_LOCK_TEXT", str(self.lock))
        self.lock_patch.start()
        self.lock_text_patch.start()

    def tearDown(self) -> None:
        self.lock_text_patch.stop()
        self.lock_patch.stop()
        self.lock.unlink(missing_ok=True)

    @staticmethod
    def _hash(path: Path) -> str:
        return "sha256:" + hashlib.sha256(path.read_bytes()).hexdigest()

    def _runner_report(
        self,
        root: Path,
        report_path: Path,
        nonce: str,
        check_id: str,
        command_hash: str,
        batch: str = "public-c7-check",
        *,
        status: str = "passed",
        exit_code: int = 0,
    ) -> dict[str, object]:
        selectors = list(stage3._FIXED_BATCH_SELECTORS[batch])
        cache_keys = (
            "sh06-hits", "sh06-misses", "core-hits", "core-misses",
            "verification-hits", "verification-misses",
        )
        per = []
        for index, selector in enumerate(selectors):
            skipped = index >= len(selectors) if status == "partial" else False
            failed = status == "failed" and index == len(selectors) - 1
            cache = {key: 0 for key in cache_keys}
            per.append({
                "test-var": selector,
                "selection-index": index,
                "status": "skipped" if skipped else ("failed" if failed else "passed"),
                "counts": {"type": "summary", "test": 0 if skipped else 1,
                           "pass": 0 if skipped or failed else 1,
                           "fail": 1 if failed else 0, "error": 0},
                "cache": cache,
                "elapsed-ms": 0 if skipped else 1,
                "completed?": not skipped,
                "skipped-tail?": skipped,
            })
        counts = {
            "type": "summary",
            "test": len(selectors),
            "pass": len(selectors) - 1 if status == "failed" else (0 if status == "partial" else len(selectors)),
            "fail": 1 if status == "failed" else 0,
            "error": 0,
        }
        return {
            "schema": "gravity/stage3-verification-receipt-v1",
            "stage": "stage3",
            "status": status,
            "exit-code": exit_code,
            "batch-id": batch,
            "batch-name": batch,
            "selection-order": selectors,
            "executed-vars": [] if status == "partial" else selectors,
            "executed": [] if status == "partial" else selectors,
            "skipped-tail": selectors if status == "partial" else [],
            "skipped-vars": selectors if status == "partial" else [],
            "counts": counts,
            "cache": {key: 0 for key in cache_keys},
            "elapsed-ms": 1,
            "per-var-results": per,
            "authority": "non-authoritative",
            "authoritative?": False,
            "cache-authoritative?": False,
            "fresh-authoritative-run-required?": True,
            "report-file": str(report_path),
            "nonce": nonce,
            "check-id": check_id,
            "command-identity-sha256": command_hash,
        }

    def _pure_launcher(self, root: Path, *, report_status: str = "passed", report_exit: int = 0):
        def launcher(command, cwd, env, timeout):
            report_path = Path(command[command.index("--report-file") + 1])
            nonce = command[command.index("--report-nonce") + 1]
            check_id = command[command.index("--report-check-id") + 1]
            command_hash = command[command.index("--report-command-identity-sha256") + 1]
            report = self._runner_report(
                root, report_path, nonce, check_id, command_hash,
                status=report_status, exit_code=report_exit,
            )
            stage3.atomic_receipt_write(report_path, report, root=root)
            return stage3.ChildResult(
                report_exit,
                "diagnostic output",
                "",
                False,
                (),
                None,
                False,
                123,
                1.0,
                "run_with_heartbeat.process_tree_metrics-v1",
                "between-sample spikes may be missed",
            )
        return launcher

    def test_pure_report_and_lock_evidence(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            command_hash = "sha256:" + "a" * 64
            code, receipt = stage3.run_stage3(
                root=root,
                receipt_path=root / ".cpcache" / "receipt.json",
                nonce="nonce1",
                check_id="public-c7",
                batch="public-c7-check",
                command_identity_sha256=command_hash,
                launcher=self._pure_launcher(root),
                timeout_seconds=2,
            )
            self.assertEqual(0, code)
            self.assertEqual("passed", receipt["status"])
            self.assertTrue(receipt["lock"]["acquired"])
            self.assertTrue(receipt["lock"]["validated"])
            self.assertTrue(receipt["lock"]["released"])
            self.assertEqual(123, receipt["observed_peak_process_tree_rss_bytes"])
            self.assertIn("runner_report", receipt)

    def test_missing_report_cannot_claim_exit_zero(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)

            def launcher(command, cwd, env, timeout):
                return stage3.ChildResult(
                    0, "", "", False, (), None, False, 2, 1.0,
                    "run_with_heartbeat.process_tree_metrics-v1",
                    "between-sample spikes may be missed",
                )

            code, receipt = stage3.run_stage3(
                root=root,
                receipt_path=root / ".cpcache" / "receipt.json",
                nonce="nonce2",
                check_id="missing",
                command_identity_sha256="sha256:" + "b" * 64,
                launcher=launcher,
                timeout_seconds=2,
            )
            self.assertNotEqual(0, code)
            self.assertEqual("failed", receipt["status"])
            self.assertFalse(receipt["no_surviving_descendants"])

    def test_report_exit_mismatch_fails_even_when_child_zero(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            launcher = self._pure_launcher(root, report_status="failed", report_exit=1)
            # Make the child itself claim zero while the report claims one.
            def mismatch(command, cwd, env, timeout):
                launcher(command, cwd, env, timeout)
                return stage3.ChildResult(
                    0, "", "", False, (), None, False, 4, 1.0,
                    "run_with_heartbeat.process_tree_metrics-v1",
                    "between-sample spikes may be missed",
                )

            code, receipt = stage3.run_stage3(
                root=root,
                receipt_path=root / ".cpcache" / "receipt.json",
                nonce="nonce3",
                check_id="mismatch",
                command_identity_sha256="sha256:" + "c" * 64,
                launcher=mismatch,
                timeout_seconds=2,
            )
            self.assertNotEqual(0, code)
            self.assertEqual("failed", receipt["status"])

    def test_report_pass_cannot_hide_nonzero_child(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            base = self._pure_launcher(root)

            def mismatch(command, cwd, env, timeout):
                base(command, cwd, env, timeout)
                return stage3.ChildResult(
                    1, "", "", False, (), None, False, 4, 1.0,
                    "run_with_heartbeat.process_tree_metrics-v1",
                    "between-sample spikes may be missed",
                )

            code, receipt = stage3.run_stage3(
                root=root,
                receipt_path=root / ".cpcache" / "receipt.json",
                nonce="nonce-pass-child-fail",
                check_id="pass-child-fail",
                command_identity_sha256="sha256:" + "8" * 64,
                launcher=mismatch,
                timeout_seconds=2,
            )
            self.assertNotEqual(0, code)
            self.assertEqual("failed", receipt["status"])

    def test_one_final_runner_failure_with_skipped_suffix_is_valid_evidence(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            report_path = root / "report.json"
            nonce = "failure-nonce"
            check_id = "failure-check"
            command_hash = "sha256:" + "9" * 64
            report = self._runner_report(
                root,
                report_path,
                nonce,
                check_id,
                command_hash,
                batch="source-plan-contract",
                status="failed",
                exit_code=1,
            )
            selectors = list(report["selection-order"])
            # A fail-fast runner may publish the exact unexecuted suffix.
            last = selectors[-1]
            report["executed-vars"] = selectors[:-1]
            report["executed"] = selectors[:-1]
            report["skipped-tail"] = [last]
            report["skipped-vars"] = [last]
            report["per-var-results"][-1]["status"] = "skipped"
            report["per-var-results"][-1]["counts"] = {
                "type": "summary", "test": 0, "pass": 0, "fail": 0, "error": 0,
            }
            report["per-var-results"][-1]["elapsed-ms"] = 0
            report["per-var-results"][-1]["completed?"] = False
            report["per-var-results"][-1]["skipped-tail?"] = True
            report["per-var-results"][-2]["status"] = "failed"
            report["per-var-results"][-2]["counts"] = {
                "type": "summary", "test": 1, "pass": 0, "fail": 1, "error": 0,
            }
            report["counts"] = {
                "type": "summary", "test": len(selectors) - 1,
                "pass": len(selectors) - 2, "fail": 1, "error": 0,
            }
            stage3._validate_runner_report(  # type: ignore[attr-defined]
                report,
                root=root,
                report_path=report_path,
                batch="source-plan-contract",
                nonce=nonce,
                check_id=check_id,
                command_identity_sha256=command_hash,
            )

    def test_runner_report_trailing_data_fails_closed(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            base = self._pure_launcher(root)

            def trailing(command, cwd, env, timeout):
                result = base(command, cwd, env, timeout)
                report_path = Path(command[command.index("--report-file") + 1])
                with report_path.open("ab") as stream:
                    stream.write(b"{}")
                return result

            code, receipt = stage3.run_stage3(
                root=root,
                receipt_path=root / ".cpcache" / "receipt.json",
                nonce="nonce4",
                check_id="trailing",
                command_identity_sha256="sha256:" + "d" * 64,
                launcher=trailing,
                timeout_seconds=2,
            )
            self.assertNotEqual(0, code)
            self.assertEqual("failed", receipt["status"])

    def test_runner_report_hardlink_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            base = self._pure_launcher(root)

            def hardlink(command, cwd, env, timeout):
                result = base(command, cwd, env, timeout)
                report_path = Path(command[command.index("--report-file") + 1])
                os_link = report_path.with_suffix(".link")
                os_link.unlink(missing_ok=True)
                os_link.hardlink_to(report_path)
                return result

            code, receipt = stage3.run_stage3(
                root=root,
                receipt_path=root / ".cpcache" / "receipt.json",
                nonce="nonce5",
                check_id="hardlink",
                command_identity_sha256="sha256:" + "e" * 64,
                launcher=hardlink,
                timeout_seconds=2,
            )
            self.assertNotEqual(0, code)
            self.assertEqual("failed", receipt["status"])

    def test_pure_lock_replacement_overrides_child_success(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            base = self._pure_launcher(root)

            def replace_lock(command, cwd, env, timeout):
                result = base(command, cwd, env, timeout)
                replacement = self.lock.with_name(self.lock.name + ".replacement")
                replacement.unlink(missing_ok=True)
                descriptor = replacement.open("w", encoding="utf-8")
                descriptor.close()
                replacement.chmod(0o600)
                replacement.replace(self.lock)
                return result

            code, receipt = stage3.run_stage3(
                root=root,
                receipt_path=root / ".cpcache" / "receipt.json",
                nonce="nonce6",
                check_id="lock-replacement",
                command_identity_sha256="sha256:" + "f" * 64,
                launcher=replace_lock,
                timeout_seconds=2,
            )
            self.assertNotEqual(0, code)
            self.assertEqual("failed", receipt["status"])
            self.assertFalse(receipt["lock"]["validated"])

    def test_authority_requires_actual_completed_c7_manifest(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity"
            source.parent.mkdir(parents=True)
            source.write_text("source", encoding="utf-8")
            proof = root / sh07.PROOF_CONTRACT_RELATIVE
            proof.parent.mkdir(parents=True)
            proof.write_text("proof", encoding="utf-8")

            def launcher(command, cwd, env, timeout):
                state = Path(command[command.index("--state-dir") + 1])
                modules = state / "modules"
                modules.mkdir(parents=True)
                stdout = modules / "c7-types.stdout.log"
                stderr = modules / "c7-types.stderr.log"
                stdout.write_text("structured output", encoding="utf-8")
                stderr.write_text("", encoding="utf-8")
                context = {
                    "module": "c7-types",
                    "sha256": "sha256:" + "c" * 64,
                    "files": [{"path": source.relative_to(root).as_posix(),
                               "size": source.stat().st_size,
                               "sha256": self._hash(source)}],
                }
                record = {
                    "state": "passed",
                    "command": ["clojure", "--fresh", "c7-types"],
                    "module_context_fingerprint": context["sha256"],
                    "proof_contract_sha256": self._hash(proof),
                    "module_context": context,
                    "context_stable": True,
                    "output_contract_checked": True,
                    "stdout_path": "modules/c7-types.stdout.log",
                    "stderr_path": "modules/c7-types.stderr.log",
                    "stdout_sha256": self._hash(stdout),
                    "stderr_sha256": self._hash(stderr),
                    "exit_code": 0,
                    "raw_child_exit_code": 0,
                    "timed_out": False,
                }
                manifest = {
                    "schema": sh07.SCHEMA,
                    "state": "completed",
                    "selected_modules": ["c7-types"],
                    "aggregate_authoritative": False,
                    "authority_scope": "individual-existing-runner-outputs-only",
                    "resumed_modules": [],
                    "shared_context_fingerprint": "sha256:" + "d" * 64,
                    "lock_path": str(self.lock),
                    "lock_mode": "0600",
                    "lock_acquired": True,
                    "lock_validated": True,
                    "lock_released": True,
                    "modules": {"c7-types": record},
                }
                (state / "manifest.json").write_text(json.dumps(manifest), encoding="utf-8")
                return stage3.ChildResult(
                    0, "", "", False, (), None, False, 123, 1.0,
                    "run_with_heartbeat.process_tree_metrics-v1",
                    "between-sample spikes may be missed",
                )

            with mock.patch.object(sh07, "output_contract_passed", return_value=True):
                code, receipt = stage3.run_stage3(
                    root=root,
                    receipt_path=root / ".cpcache" / "authority.json",
                    nonce="auth1",
                    check_id="authority",
                    mode=stage3.MODE_AUTHORITY,
                    batch="authority",
                    command_identity_sha256="sha256:" + "e" * 64,
                    launcher=launcher,
                    timeout_seconds=2,
                )
            self.assertEqual(0, code)
            self.assertEqual("scoped-proof-authority", receipt["authority"])
            self.assertEqual("authoritative-child", receipt["lock"]["owner"])


if __name__ == "__main__":
    unittest.main()
