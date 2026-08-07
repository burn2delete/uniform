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

    def test_receipt_bounds_worst_case_unicode_child_streams(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)

            def launcher(command, cwd, env, timeout):
                report_path = Path(command[command.index("--report-file") + 1])
                nonce = command[command.index("--report-nonce") + 1]
                check_id = command[command.index("--report-check-id") + 1]
                command_hash = command[command.index("--report-command-identity-sha256") + 1]
                stage3.atomic_receipt_write(
                    report_path,
                    self._runner_report(
                        root, report_path, nonce, check_id, command_hash
                    ),
                    root=root,
                )
                # Exercise byte-boundary truncation with multi-byte UTF-8,
                # while keeping the synthetic child entirely in Python.
                return stage3.ChildResult(
                    0,
                    "🙂" * 100_000,
                    "漢" * 100_000,
                    False,
                    (),
                    None,
                    False,
                )

            receipt_path = root / ".cpcache" / "unicode.json"
            code, receipt = stage3.run_stage3(
                root=root,
                receipt_path=receipt_path,
                nonce="unicode",
                check_id="unicode",
                command_identity_sha256="sha256:" + "1" * 64,
                launcher=launcher,
                timeout_seconds=2,
            )
            self.assertEqual(0, code)
            encoded = receipt_path.read_bytes()
            self.assertLessEqual(len(encoded), stage3.MAX_RECEIPT_BYTES)
            self.assertLessEqual(
                len(receipt["child"]["stdout"].encode("utf-8"))
                + len(receipt["child"]["stderr"].encode("utf-8")),
                stage3.MAX_CHILD_OUTPUT_COMBINED_BYTES,
            )

    def test_reviewed_attestation_mode_is_explicitly_deferred(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.assertNotIn(stage3.MODE_REVIEWED_ATTESTATION, stage3.MODES)
            with self.assertRaises(stage3.Stage3Error):
                stage3.run_stage3(
                    root=root,
                    receipt_path=root / ".cpcache" / "attestation.json",
                    nonce="attestation",
                    check_id="attestation",
                    mode=stage3.MODE_REVIEWED_ATTESTATION,
                    batch="authority",
                    command_identity_sha256="sha256:" + "2" * 64,
                    launcher=self._pure_launcher(root),
                    timeout_seconds=2,
                )

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
            record_overrides: dict[str, object] = {}

            def launcher(command, cwd, env, timeout):
                state = Path(command[command.index("--state-dir") + 1])
                modules = state / "modules"
                modules.mkdir(parents=True, exist_ok=True)
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
                    "command": [*sh07.default_base_command(), "--fresh", "c7-types"],
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
                record.update(record_overrides)
                manifest = {
                    "schema": sh07.SCHEMA,
                    "tool_version": sh07.TOOL_VERSION,
                    "fingerprint_policy_version": sh07.FINGERPRINT_POLICY_VERSION,
                    "state": "completed",
                    "selected_modules": ["c7-types"],
                    "aggregate_authoritative": False,
                    "authority_scope": "individual-existing-runner-outputs-only",
                    "resumed_modules": [],
                    "shared_context_fingerprint": "sha256:" + "d" * 64,
                    "shared_context_fingerprint_after": "sha256:" + "d" * 64,
                    "shared_context": {
                        "command": sh07.default_base_command(),
                        "authoritative_module_catalog": {},
                    },
                    "lock_path": str(self.lock),
                    "lock_mode": "0600",
                    "lock_acquired": True,
                    "lock_validated": True,
                    "lock_released": True,
                    "modules": {"c7-types": record},
                    # The SH07 checkpoint manifest carries complete runtime,
                    # classpath, source-contract, and module contexts.  It is
                    # legitimately larger than the compact Stage3 receipt.
                    "bounded_context_padding": "x" * (70 * 1024),
                }
                self.assertGreater(
                    len(json.dumps(manifest).encode("utf-8")),
                    stage3.MAX_RECEIPT_BYTES,
                )
                self.assertLess(
                    len(json.dumps(manifest).encode("utf-8")),
                    stage3.MAX_AUTHORITY_MANIFEST_BYTES,
                )
                (state / "manifest.json").write_text(json.dumps(manifest), encoding="utf-8")
                return stage3.ChildResult(
                    0, "", "", False, (), None, False, 123, 1.0,
                    "run_with_heartbeat.process_tree_metrics-v1",
                    "between-sample spikes may be missed",
                )

            with mock.patch.object(sh07, "output_contract_passed", return_value=True), \
                    mock.patch.object(stage3, "_validate_captured_output_contract", return_value=True), \
                    mock.patch.object(
                        stage3,
                        "_recompute_shared_context",
                        return_value={"sha256": "sha256:" + "d" * 64},
                    ):
                code, receipt = stage3.run_stage3(
                    root=root,
                    receipt_path=root / ".cpcache" / "authority.json",
                    nonce="auth1",
                    check_id="authority",
                    mode=stage3.MODE_PROOF_CANDIDATE,
                    batch="authority",
                    command_identity_sha256="sha256:" + "e" * 64,
                    launcher=launcher,
                    timeout_seconds=2,
                )
                for suffix, overrides in (
                    ("missing-raw-exit", {"raw_child_exit_code": None}),
                    ("missing-timeout", {"timed_out": None}),
                ):
                    record_overrides.clear()
                    record_overrides.update(overrides)
                    rejected_code, rejected = stage3.run_stage3(
                        root=root,
                        receipt_path=root / ".cpcache" / f"authority-{suffix}.json",
                        nonce=f"auth-{suffix}",
                        check_id=f"authority-{suffix}",
                        mode=stage3.MODE_PROOF_CANDIDATE,
                        batch="authority",
                        command_identity_sha256="sha256:" + "e" * 64,
                        launcher=launcher,
                        timeout_seconds=2,
                    )
                    self.assertNotEqual(0, rejected_code)
                    self.assertFalse(rejected["proof_candidate"])
            self.assertEqual(0, code)
            self.assertEqual("none", receipt["authority"])
            self.assertTrue(receipt["non_authoritative"])
            self.assertEqual("none", receipt["authority_scope"])
            self.assertEqual("source-bound-derived-proof-candidate", receipt["evidence_kind"])
            self.assertTrue(receipt["proof_candidate"])
            self.assertEqual("passed", receipt["proof_candidate_status"])
            self.assertEqual("individual-source-bound-derived", receipt["candidate_scope"])
            self.assertTrue(receipt["attestation_required"])
            self.assertFalse(receipt["attestation_present"])
            self.assertFalse(receipt["aggregate_authoritative"])
            self.assertFalse(receipt["release_authoritative"])
            self.assertFalse((Path(receipt["state_dir"]) / "attestations").exists())
            self.assertEqual("authoritative-child", receipt["lock"]["owner"])

    def test_authority_missing_manifest_exit_zero_fails_closed(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)

            def launcher(command, cwd, env, timeout):
                # The launcher claims success but publishes neither the
                # checkpoint manifest nor the c7-types output files.
                return stage3.ChildResult(0, "", "", False, (), None, False)

            code, receipt = stage3.run_stage3(
                root=root,
                receipt_path=root / ".cpcache" / "authority-missing.json",
                nonce="auth-missing",
                check_id="authority-missing",
                mode=stage3.MODE_PROOF_CANDIDATE,
                batch="authority",
                command_identity_sha256="sha256:" + "a" * 64,
                launcher=launcher,
                timeout_seconds=2,
            )
            self.assertNotEqual(0, code)
            self.assertEqual("failed", receipt["status"])
            self.assertTrue(receipt["child"]["supervision_failed"])
            self.assertEqual("none", receipt["authority"])
            self.assertTrue(receipt["non_authoritative"])
            self.assertFalse(receipt["proof_candidate"])

    def test_authority_state_modules_rename_cannot_redirect_held_output(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)

            def launcher(command, cwd, env, timeout):
                state = Path(command[command.index("--state-dir") + 1])
                modules = state / "modules"
                moved = state / "modules-original"
                modules.rename(moved)
                modules.mkdir(mode=0o700)
                # A manifest in the replacement directory must not make the
                # wrapper consume output from that replacement: it retained
                # the original modules dirfd before launch.
                (modules / "c7-types.stdout.log").write_text("fake", encoding="utf-8")
                (modules / "c7-types.stderr.log").write_text("", encoding="utf-8")
                return stage3.ChildResult(0, "", "", False, (), None, False,
                                          1, 1.0,
                                          "run_with_heartbeat.process_tree_metrics-v1",
                                          "between-sample spikes may be missed")

            code, receipt = stage3.run_stage3(
                root=root,
                receipt_path=root / ".cpcache" / "rename.json",
                nonce="rename-modules",
                check_id="rename-modules",
                mode=stage3.MODE_PROOF_CANDIDATE,
                batch="authority",
                command_identity_sha256="sha256:" + "b" * 64,
                launcher=launcher,
                timeout_seconds=2,
            )
            self.assertEqual(75, code)
            self.assertEqual("failed", receipt["status"])
            self.assertTrue(receipt["child"]["supervision_failed"])
            self.assertTrue(receipt["non_authoritative"])


if __name__ == "__main__":
    unittest.main()
