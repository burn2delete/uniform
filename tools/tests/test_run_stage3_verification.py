from __future__ import annotations

import copy
import hashlib
import json
import os
from pathlib import Path
import re
import tempfile
import unittest
from unittest import mock

from tools import run_sh07_authoritative_modules as sh07
from tools import run_stage3_verification as stage3
from tools import verify_development as verifier


_CLOJURE_STAGE3_RUNNER = (
    Path(__file__).parents[2]
    / "bootstrap/clojure/test/gravity/self_hosting/stage3_verification_runner.clj"
)


def _clojure_vector_definition(name: str) -> list[str]:
    """Read one literal fixed vector without starting a Clojure runtime."""

    source = _CLOJURE_STAGE3_RUNNER.read_text(encoding="utf-8")
    definition = re.search(
        rf"\(def(?:\s+\^:[^\s]+)?\s+{re.escape(name)}\b(.*?)(?=\n\(def(?:\s|\s+\^)|\Z)",
        source,
        re.DOTALL,
    )
    vector = re.search(r"\[(.*?)\]", definition.group(1), re.DOTALL) if definition else None
    if vector is None:
        raise AssertionError(f"missing literal Clojure vector definition: {name}")
    body = vector.group(1)
    if name == "batch-order":
        return re.findall(r":([A-Za-z0-9_-]+)", body)
    return re.findall(r"'([^\s\]]+)", body)


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

    def test_successful_sampled_child_requires_positive_rss(self) -> None:
        receipt: dict[str, object] = {}
        stage3._finish_receipt(  # type: ignore[attr-defined]
            receipt,
            stage3.ChildResult(
                0,
                observed_peak_process_tree_rss_bytes=0,
                rss_sampling_cadence_seconds=1.0,
                rss_sampling_contract="run_with_heartbeat.process_tree_metrics-v1",
                rss_sampling_limitation="RSS unavailable; between-sample spikes may be missed",
            ),
        )
        self.assertEqual(receipt["status"], "failed")
        self.assertEqual(receipt["exit_code"], 75)
        self.assertTrue(receipt["rss_sampling_failed"])

    def test_fixed_batches_match_clojure_order_selectors_and_heap_contract(self) -> None:
        """Keep the Python command boundary identical to the Clojure allowlist."""

        self.assertEqual(
            list(stage3.FIXED_BATCHES),
            _clojure_vector_definition("batch-order")
            + ["authority", "c8-authority", "c9-authority", "c10-authority", "c11-authority"],
        )
        selector_definitions = {
            "primitive-pure": "primitive-pure-selectors",
            "primitive-bool-authenticated": "primitive-bool-authenticated-selectors",
            "recursive-pure": "recursive-pure-selectors",
            "recursive-authenticated": "recursive-authenticated-selectors",
            "authoritative-ho-pure": "authoritative-ho-pure-selectors",
            "authoritative-ho-authenticated": "authoritative-ho-authenticated-selectors",
            "source-control-form-arity": "source-control-form-arity-selectors",
            "source-plan-contract": "source-plan-contract-selectors",
            "coverage-census-contract": "coverage-census-contract-selectors",
            "fragment-size-preflight": "fragment-size-preflight-selectors",
            "public-c7-check": "public-c7-check-selectors",
            "stage4-c8-source-structural": "stage4-c8-source-structural-selectors",
            "stage4-sh09-adapter": "stage4-sh09-adapter-selectors",
            "stage4-public-c8": "stage4-public-c8-selectors",
            "stage5-c9-source-structural": "stage5-c9-source-structural-selectors",
            "stage5-c9-kernel": "stage5-c9-kernel-selectors",
            "stage5-sh10-c8-adapter": "stage5-sh10-c8-adapter-selectors",
            "stage5-public-c9": "stage5-public-c9-selectors",
            "stage6-c10-source-structural": "stage6-c10-source-structural-selectors",
            "stage6-c10-kernel": "stage6-c10-kernel-selectors",
            "stage6-public-c10": "stage6-public-c10-selectors",
            "stage6-sh11-c9-safety-adapter": "stage6-sh11-c9-safety-adapter-selectors",
            "stage7-c11-source-preflight": "stage7-c11-source-preflight-selectors",
            "stage7-c11-shape-preflight": "stage7-c11-shape-preflight-selectors",
            "stage7-sh12-c10-mir-adapter": "stage7-sh12-c10-mir-adapter-selectors",
            "stage7-public-c11": "stage7-public-c11-selectors",
            "stage8-c12-source-shape": "stage8-c12-source-shape-selectors",
            "stage8-public-c12": "stage8-public-c12-selectors",
            "stage8-sh13-c11-domain-evidence": "stage8-sh13-c11-domain-evidence-selectors",
            "stage9-c13-source-shape": "stage9-c13-source-shape-selectors",
            "stage9-sh16-c13-evidence-boundary": "stage9-sh16-c13-evidence-boundary-selectors",
        }
        for batch, definition in selector_definitions.items():
            self.assertEqual(
                list(stage3._FIXED_BATCH_SELECTORS[batch]),
                _clojure_vector_definition(definition),
                batch,
            )
        expected_heap = {
            "primitive-pure": "-J-Xmx8g",
            "primitive-bool-authenticated": "-J-Xmx8g",
            "recursive-pure": "-J-Xmx8g",
            "recursive-authenticated": "-J-Xmx8g",
            "authoritative-ho-pure": "-J-Xmx8g",
            "authoritative-ho-authenticated": "-J-Xmx8g",
            "source-control-form-arity": "-J-Xmx2g",
            "source-plan-contract": "-J-Xmx2g",
            "coverage-census-contract": "-J-Xmx2g",
            "fragment-size-preflight": "-J-Xmx2g",
            "public-c7-check": "-J-Xmx2g",
            "stage4-c8-source-structural": "-J-Xmx2g",
            "stage4-sh09-adapter": "-J-Xmx8g",
            "stage4-public-c8": "-J-Xmx2g",
            "stage5-c9-source-structural": "-J-Xmx2g",
            "stage5-c9-kernel": "-J-Xmx2g",
            "stage5-sh10-c8-adapter": "-J-Xmx8g",
            "stage5-public-c9": "-J-Xmx2g",
            "authority": "-J-Xmx8g",
            "c8-authority": "-J-Xmx8g",
            "c9-authority": "-J-Xmx8g",
            "stage6-c10-source-structural": "-J-Xmx2g",
            "stage6-c10-kernel": "-J-Xmx2g",
            "stage6-public-c10": "-J-Xmx2g",
            "stage6-sh11-c9-safety-adapter": "-J-Xmx8g",
            "stage7-c11-source-preflight": "-J-Xmx512m",
            "stage7-c11-shape-preflight": "-J-Xmx512m",
            "stage7-sh12-c10-mir-adapter": "-J-Xmx8g",
            "stage7-public-c11": "-J-Xmx2g",
            "stage8-c12-source-shape": "-J-Xmx512m",
            "stage8-public-c12": "-J-Xmx2g",
            "stage8-sh13-c11-domain-evidence": "-J-Xmx8g",
            "stage9-c13-source-shape": "-J-Xmx512m",
            "stage9-sh16-c13-evidence-boundary": "-J-Xmx8g",
            "c10-authority": "-J-Xmx8g",
            "c11-authority": "-J-Xmx8g",
        }
        self.assertEqual(stage3._BATCH_HEAP, expected_heap)
        for batch in stage3._BATCH_COMMANDS:
            self.assertEqual(stage3._BATCH_COMMANDS[batch][1], expected_heap[batch])
        c8 = stage3._FIXED_BATCH_SELECTORS["stage4-c8-source-structural"]
        self.assertFalse(any(
            marker in selector
            for selector in c8
            for marker in (
                "sh07-b29-c8-source-has-exact-authentic-coverage",
                "sh07-b29-c8-calls-lookups-and-error-effect",
                "sh07-b29-c8-is-deterministic-path-neutral",
                "sh07-b29-c8-replay-and-alteration",
                "sh07-b29-existing-rejected-families",
            )
        ))
        merged = list(stage3._FIXED_BATCH_SELECTORS["stage4-sh09-adapter"])
        self.assertEqual(6, len(merged))
        self.assertEqual(6, len(set(merged)))
        self.assertTrue(merged[-1].endswith("/sh09-c7-adapter-authenticated-gravity-boundary"))

        c9_adapter = list(stage3._FIXED_BATCH_SELECTORS["stage5-sh10-c8-adapter"])
        self.assertEqual(5, len(c9_adapter))
        self.assertEqual(5, len(set(c9_adapter)))
        self.assertTrue(c9_adapter[-1].endswith("/sh10-c8-adapter-authenticated-gravity-boundary"))

        c10_adapter = list(
            stage3._FIXED_BATCH_SELECTORS["stage6-sh11-c9-safety-adapter"]
        )
        self.assertEqual(5, len(c10_adapter))
        self.assertEqual(5, len(set(c10_adapter)))
        self.assertTrue(
            c10_adapter[-1].endswith("/sh11-c9-safety-authenticated-gravity-boundary")
        )
        c11 = list(stage3._FIXED_BATCH_SELECTORS["stage7-c11-source-preflight"])
        self.assertEqual(
            [
                "gravity.self-hosting.sh07-c11-mir-source-preflight-test/sh07-c11-source-binding-is-exact",
                "gravity.self-hosting.sh07-c11-mir-source-preflight-test/sh07-c11-source-control-form-arities-are-exact",
                "gravity.self-hosting.sh07-c11-mir-source-preflight-test/sh07-c11-source-exports-have-definitions",
            ],
            c11,
        )
        self.assertEqual(3, len(c11))
        self.assertEqual(3, len(set(c11)))
        c11_shape = list(stage3._FIXED_BATCH_SELECTORS["stage7-c11-shape-preflight"])
        self.assertEqual(c11[1:], c11_shape)
        self.assertEqual(2, len(c11_shape))
        self.assertEqual(2, len(set(c11_shape)))
        self.assertEqual(
            {"stage7-c11-shape-preflight": "stage7-c11-source-preflight"},
            dict(stage3.EXECUTION_PROFILE_BATCH_OWNERS),
        )
        c11_adapter = list(
            stage3._FIXED_BATCH_SELECTORS["stage7-sh12-c10-mir-adapter"]
        )
        self.assertEqual(6, len(c11_adapter))
        self.assertEqual(6, len(set(c11_adapter)))
        self.assertTrue(
            c11_adapter[0].endswith("/sh12-c10-verification-envelope-preflight")
        )
        self.assertTrue(
            c11_adapter[-1].endswith("/sh12-c10-authenticated-gravity-boundary")
        )
        self.assertEqual(
            (
                "gravity.bootstrap-test/gravity-c11-source-and-builder-identities-are-pinned",
                "gravity.bootstrap-test/"
                "public-check-accepts-gravity-authored-c11-mir-specification",
            ),
            stage3._FIXED_BATCH_SELECTORS["stage7-public-c11"],
        )
        c12_shape = stage3._FIXED_BATCH_SELECTORS["stage8-c12-source-shape"]
        self.assertEqual(2, len(c12_shape))
        self.assertEqual(2, len(set(c12_shape)))
        self.assertTrue(c12_shape[0].endswith("/sh07-c12-domain-ir-source-shape-and-control"))
        self.assertTrue(c12_shape[1].endswith("/sh07-c12-domain-ir-export-completeness"))
        c12_adapter = stage3._FIXED_BATCH_SELECTORS["stage8-sh13-c11-domain-evidence"]
        self.assertEqual(6, len(c12_adapter))
        self.assertEqual(6, len(set(c12_adapter)))
        self.assertTrue(c12_adapter[-1].endswith("/sh13-c11-domain-evidence-path-neutral-provenance"))
        self.assertEqual(
            (
                "gravity.bootstrap-test/public-check-accepts-gravity-authored-c12-domain-ir-architecture",
            ),
            stage3._FIXED_BATCH_SELECTORS["stage8-public-c12"],
        )
        c13_boundary = stage3._FIXED_BATCH_SELECTORS[
            "stage9-sh16-c13-evidence-boundary"
        ]
        c13_shape = stage3._FIXED_BATCH_SELECTORS["stage9-c13-source-shape"]
        self.assertEqual(2, len(c13_shape))
        self.assertEqual(2, len(set(c13_shape)))
        self.assertTrue(
            c13_shape[0].endswith(
                "/sh07-c13-mir-optimization-source-shape-and-control"
            )
        )
        self.assertTrue(
            c13_shape[1].endswith(
                "/sh07-c13-mir-optimization-export-completeness"
            )
        )
        self.assertEqual(4, len(c13_boundary))
        self.assertEqual(4, len(set(c13_boundary)))
        self.assertTrue(c13_boundary[0].endswith("/sh16-c13-evidence-boundary-surface"))
        self.assertTrue(
            c13_boundary[-1].endswith(
                "/sh16-c13-evidence-boundary-separates-top-level-provenance"
            )
        )

    def test_retired_singleton_batch_ids_are_rejected(self) -> None:
        for retired in (
            "recursive-integer-authenticated",
            "recursive-string-authenticated",
            "authoritative-ho-fixture-parity",
            "authoritative-ho2-authenticated",
            "stage4-sh09-adapter-synthetic",
            "stage4-sh09-authenticated",
        ):
            with self.assertRaises(stage3.Stage3Error):
                stage3.batch_command(retired)

    def test_stage7_c11_source_preflight_has_exact_three_test_census(self) -> None:
        """The complete C11 source namespace cannot silently drift."""

        source_path = (
            Path(__file__).parents[2]
            / "bootstrap/clojure/test/gravity/self_hosting/sh07_c11_mir_source_preflight_test.clj"
        )
        names = re.findall(r"\(deftest\s+([^\s\)]+)", source_path.read_text(encoding="utf-8"))
        self.assertEqual(
            [
                "sh07-c11-source-binding-is-exact",
                "sh07-c11-source-control-form-arities-are-exact",
                "sh07-c11-source-exports-have-definitions",
            ],
            names,
        )
        self.assertEqual(3, len(names))
        self.assertEqual(3, len(set(names)))

    def test_stage7_c11_shape_profile_is_ordered_subset_of_source_owner(self) -> None:
        source = list(stage3._FIXED_BATCH_SELECTORS["stage7-c11-source-preflight"])
        shape = list(stage3._FIXED_BATCH_SELECTORS["stage7-c11-shape-preflight"])
        self.assertEqual(source[1:], shape)
        self.assertEqual(len(shape), len(set(shape)))
        self.assertTrue(all(selector in source for selector in shape))

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

    def test_timeout_with_raw_zero_is_exit_124_not_false_pass(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            base = self._pure_launcher(root)

            def timed_out(command, cwd, env, timeout):
                result = base(command, cwd, env, timeout)
                return stage3.dataclasses.replace(result, timed_out=True)

            code, receipt = stage3.run_stage3(
                root=root,
                receipt_path=root / ".cpcache" / "timeout.json",
                nonce="timeout-zero",
                check_id="timeout-zero",
                batch="public-c7-check",
                command_identity_sha256="sha256:" + "7" * 64,
                launcher=timed_out,
                timeout_seconds=2,
            )
            self.assertEqual(124, code)
            self.assertEqual("failed", receipt["status"])
            self.assertTrue(receipt["child"]["timed_out"])

    def test_supervision_failure_with_raw_zero_is_exit_75(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            base = self._pure_launcher(root)

            def failed_supervision(command, cwd, env, timeout):
                result = base(command, cwd, env, timeout)
                return stage3.dataclasses.replace(result, supervision_failed=True)

            code, receipt = stage3.run_stage3(
                root=root,
                receipt_path=root / ".cpcache" / "supervision.json",
                nonce="supervision-zero",
                check_id="supervision-zero",
                batch="public-c7-check",
                command_identity_sha256="sha256:" + "6" * 64,
                launcher=failed_supervision,
                timeout_seconds=2,
            )
            self.assertEqual(75, code)
            self.assertEqual("failed", receipt["status"])
            self.assertTrue(receipt["child"]["supervision_failed"])

    def test_receipt_publication_never_overwrites_racing_target(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = stage3._safe_root(Path(directory))
            target = root / "receipt.json"
            original_link = os.link

            def racing_link(source, destination, **kwargs):
                parent_fd = kwargs["dst_dir_fd"]
                victim_fd = os.open(
                    destination,
                    os.O_WRONLY | os.O_CREAT | os.O_EXCL,
                    0o600,
                    dir_fd=parent_fd,
                )
                os.write(victim_fd, b"user-owned\n")
                os.close(victim_fd)
                return original_link(source, destination, **kwargs)

            with mock.patch.object(stage3.os, "link", side_effect=racing_link):
                with self.assertRaises(stage3.Stage3Error):
                    stage3.atomic_receipt_write(target, {"value": 1}, root=root)
            self.assertEqual(b"user-owned\n", target.read_bytes())
            self.assertEqual(1, target.stat().st_nlink)

    def test_receipt_publication_rejects_preexisting_symlink_and_hardlink(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = stage3._safe_root(Path(directory))
            victim = root / "victim"
            victim.write_bytes(b"preserve")
            symlink = root / "receipt-symlink.json"
            symlink.symlink_to(victim.name)
            with self.assertRaises(stage3.Stage3Error):
                stage3.atomic_receipt_write(symlink, {"value": 1}, root=root)
            self.assertEqual(b"preserve", victim.read_bytes())
            hardlink = root / "receipt-hardlink.json"
            os.link(victim, hardlink)
            with self.assertRaises(stage3.Stage3Error):
                stage3.atomic_receipt_write(hardlink, {"value": 2}, root=root)
            self.assertEqual(b"preserve", victim.read_bytes())

    def test_receipt_cleanup_error_cannot_mask_fatal_signal(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = stage3._safe_root(Path(directory))
            original_unlink = os.unlink

            def failed_cleanup(path, **kwargs):
                if str(path).endswith(".tmp"):
                    raise OSError("synthetic cleanup failure")
                return original_unlink(path, **kwargs)

            with mock.patch.object(stage3.os, "link", side_effect=KeyboardInterrupt), \
                    mock.patch.object(stage3.os, "unlink", side_effect=failed_cleanup):
                with self.assertRaises(KeyboardInterrupt):
                    stage3.atomic_receipt_write(
                        root / "fatal.json", {"value": 1}, root=root
                    )

    def test_output_validation_ignores_directory_nlink_churn(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            state = Path(directory) / "state"
            modules = state / "modules"
            modules.mkdir(parents=True)
            modules_fd = os.open(modules, stage3._DIR_FLAGS)
            output = b"structural output"
            digest = "sha256:" + hashlib.sha256(output).hexdigest()
            original_fstat = os.fstat
            module_calls = 0

            def changing_directory_nlink(descriptor):
                nonlocal module_calls
                info = original_fstat(descriptor)
                if descriptor == modules_fd:
                    module_calls += 1
                    if module_calls > 1:
                        values = list(info)
                        values[3] = info.st_nlink + 1
                        return os.stat_result(values)
                return info

            try:
                with mock.patch.object(stage3.os, "fstat", side_effect=changing_directory_nlink):
                    self.assertTrue(stage3._validate_captured_output_contract(
                        root=Path(directory),
                        state_dir=state,
                        modules_fd=modules_fd,
                        output_bytes=output,
                        expected_stdout_hash=digest,
                        source_path="source.gravity",
                        source_size=4,
                        source_hash="sha256:" + "a" * 64,
                        proof_hash="sha256:" + "b" * 64,
                        validator=lambda *args, **kwargs: True,
                    ))
            finally:
                os.close(modules_fd)

    def test_source_snapshot_bytes_remain_bound_after_path_replacement(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = stage3._safe_root(Path(directory))
            source = root / "source.gravity"
            source.write_bytes(b"original source")
            snapshot = stage3._open_regular_bounded_snapshot(
                root, source, maximum=1024, label="source"
            )
            self.assertIsNotNone(snapshot)
            payload, info, descriptor, parent_fd, leaf = snapshot
            moved = root / "source.original"
            source.rename(moved)
            source.write_bytes(b"replacement source")
            try:
                self.assertEqual(b"original source", payload)
                os.lseek(descriptor, 0, os.SEEK_SET)
                self.assertEqual(b"original source", os.read(descriptor, 1024))
                self.assertNotEqual(
                    (info.st_dev, info.st_ino),
                    (os.stat(leaf, dir_fd=parent_fd, follow_symlinks=False).st_dev,
                     os.stat(leaf, dir_fd=parent_fd, follow_symlinks=False).st_ino),
                )
            finally:
                os.close(descriptor)
                os.close(parent_fd)

    def test_parent_receipt_boundary_rejects_loose_lifecycle_shapes(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = stage3._safe_root(Path(directory))
            identities = {"command": {"fixed": "stage3"}}
            command_hash = "sha256:" + verifier._sha256_text(
                verifier._canonical(identities["command"])
            )
            receipt_path = root / ".cpcache" / "parent-boundary.json"
            code, receipt = stage3.run_stage3(
                root=root,
                receipt_path=receipt_path,
                nonce="parent-boundary",
                check_id="parent-boundary",
                batch="public-c7-check",
                command_identity_sha256=command_hash,
                launcher=self._pure_launcher(root),
                timeout_seconds=2,
            )
            self.assertEqual(0, code)
            check = {
                "id": "parent-boundary",
                "stage3_mode": stage3.MODE_PURE,
                "stage3_batch": "public-c7-check",
                "authority": "none",
            }
            report_path = Path(str(receipt["runner_report_path"]))

            def validate(value):
                return verifier._validate_stage3_receipt(
                    value,
                    check=check,
                    identities=identities,
                    root=root,
                    receipt_path=receipt_path,
                    runner_report_path=report_path,
                    nonce="parent-boundary",
                    expected_returncode=0,
                )

            validate(receipt)
            mutations = []
            value = copy.deepcopy(receipt)
            value["status"] = "failed"
            mutations.append(value)
            value = copy.deepcopy(receipt)
            value["exit_code"] = False
            mutations.append(value)
            value = copy.deepcopy(receipt)
            del value["child"]["timed_out"]
            mutations.append(value)
            value = copy.deepcopy(receipt)
            value["child"]["supervision_failed"] = "false"
            mutations.append(value)
            value = copy.deepcopy(receipt)
            value["child"]["returncode"] = False
            mutations.append(value)
            value = copy.deepcopy(receipt)
            value["no_surviving_descendants"] = False
            mutations.append(value)
            value = copy.deepcopy(receipt)
            value["lock"]["validated"] = False
            mutations.append(value)
            value = copy.deepcopy(receipt)
            value["child"]["timed_out"] = True
            mutations.append(value)
            for tampered in mutations:
                with self.assertRaises(verifier.VerificationError):
                    validate(tampered)

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

    def test_merged_sh09_batch_preserves_fail_fast_skipped_tail(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            report_path = root / "sh09-report.json"
            nonce = "sh09-failure-nonce"
            check_id = "sh09-failure-check"
            command_hash = "sha256:" + "a" * 64
            report = self._runner_report(
                root,
                report_path,
                nonce,
                check_id,
                command_hash,
                batch="stage4-sh09-adapter",
                status="failed",
                exit_code=1,
            )
            selectors = list(report["selection-order"])
            failed_index = len(selectors) - 2
            report["executed-vars"] = selectors[: failed_index + 1]
            report["executed"] = selectors[: failed_index + 1]
            report["skipped-tail"] = selectors[failed_index + 1 :]
            report["skipped-vars"] = selectors[failed_index + 1 :]
            for index in range(failed_index + 1, len(selectors)):
                report["per-var-results"][index].update({
                    "status": "skipped",
                    "counts": {"type": "summary", "test": 0, "pass": 0, "fail": 0, "error": 0},
                    "cache": {key: 0 for key in (
                        "sh06-hits", "sh06-misses", "core-hits", "core-misses",
                        "verification-hits", "verification-misses",
                    )},
                    "elapsed-ms": 0,
                    "completed?": False,
                    "skipped-tail?": True,
                })
            report["per-var-results"][failed_index].update({
                "status": "failed",
                "counts": {"type": "summary", "test": 1, "pass": 0, "fail": 1, "error": 0},
            })
            report["counts"] = {
                "type": "summary", "test": failed_index + 1,
                "pass": failed_index, "fail": 1, "error": 0,
            }
            stage3._validate_runner_report(  # type: ignore[attr-defined]
                report,
                root=root,
                report_path=report_path,
                batch="stage4-sh09-adapter",
                nonce=nonce,
                check_id=check_id,
                command_identity_sha256=command_hash,
            )

    def test_stage7_c11_shape_profile_preserves_exact_skipped_tail(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            report_path = root / "c11-shape-report.json"
            nonce = "c11-shape-failure-nonce"
            check_id = "c11-shape-failure-check"
            command_hash = "sha256:" + "b" * 64
            report = self._runner_report(
                root,
                report_path,
                nonce,
                check_id,
                command_hash,
                batch="stage7-c11-shape-preflight",
                status="failed",
                exit_code=1,
            )
            selectors = list(report["selection-order"])
            report["executed-vars"] = selectors[:1]
            report["executed"] = selectors[:1]
            report["skipped-tail"] = selectors[1:]
            report["skipped-vars"] = selectors[1:]
            report["per-var-results"][0].update({
                "status": "failed",
                "counts": {"type": "summary", "test": 1, "pass": 0, "fail": 1, "error": 0},
            })
            report["per-var-results"][1].update({
                "status": "skipped",
                "counts": {"type": "summary", "test": 0, "pass": 0, "fail": 0, "error": 0},
                "cache": {key: 0 for key in (
                    "sh06-hits", "sh06-misses", "core-hits", "core-misses",
                    "verification-hits", "verification-misses",
                )},
                "elapsed-ms": 0,
                "completed?": False,
                "skipped-tail?": True,
            })
            report["counts"] = {
                "type": "summary", "test": 1, "pass": 0, "fail": 1, "error": 0,
            }
            stage3._validate_runner_report(  # type: ignore[attr-defined]
                report,
                root=root,
                report_path=report_path,
                batch="stage7-c11-shape-preflight",
                nonce=nonce,
                check_id=check_id,
                command_identity_sha256=command_hash,
            )

    def test_stage7_public_c11_identity_failure_skips_public_check(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            report_path = root / "c11-public-report.json"
            nonce = "c11-public-failure-nonce"
            check_id = "c11-public-failure-check"
            command_hash = "sha256:" + "c" * 64
            report = self._runner_report(
                root,
                report_path,
                nonce,
                check_id,
                command_hash,
                batch="stage7-public-c11",
                status="failed",
                exit_code=1,
            )
            selectors = list(report["selection-order"])
            self.assertEqual(
                selectors,
                [
                    "gravity.bootstrap-test/gravity-c11-source-and-builder-identities-are-pinned",
                    "gravity.bootstrap-test/public-check-accepts-gravity-authored-c11-mir-specification",
                ],
            )
            report["executed-vars"] = selectors[:1]
            report["executed"] = selectors[:1]
            report["skipped-tail"] = selectors[1:]
            report["skipped-vars"] = selectors[1:]
            report["per-var-results"][0].update({
                "status": "failed",
                "counts": {"type": "summary", "test": 1, "pass": 0, "fail": 1, "error": 0},
            })
            report["per-var-results"][1].update({
                "status": "skipped",
                "counts": {"type": "summary", "test": 0, "pass": 0, "fail": 0, "error": 0},
                "cache": {key: 0 for key in (
                    "sh06-hits", "sh06-misses", "core-hits", "core-misses",
                    "verification-hits", "verification-misses",
                )},
                "elapsed-ms": 0,
                "completed?": False,
                "skipped-tail?": True,
            })
            report["counts"] = {
                "type": "summary", "test": 1, "pass": 0, "fail": 1, "error": 0,
            }
            stage3._validate_runner_report(  # type: ignore[attr-defined]
                report,
                root=root,
                report_path=report_path,
                batch="stage7-public-c11",
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

            def validate_from_held_source(**arguments):
                original = source.with_suffix(".held")
                source.rename(original)
                source.write_text("transient replacement", encoding="utf-8")
                try:
                    self.assertEqual(len(b"source"), arguments["source_size"])
                    self.assertEqual(
                        "sha256:" + hashlib.sha256(b"source").hexdigest(),
                        arguments["source_hash"],
                    )
                finally:
                    source.unlink()
                    original.rename(source)
                return True

            with mock.patch.object(sh07, "output_contract_passed", return_value=True), \
                    mock.patch.object(
                        stage3,
                        "_validate_captured_output_contract",
                        side_effect=validate_from_held_source,
                    ), \
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

    def test_c8_fixed_policy_binds_module_source_and_fresh_child_command(self) -> None:
        """C8 proof validation is exact and cannot become a module passthrough."""

        self.assertEqual("c8-effects", stage3.C8_AUTHORITY_POLICY["module"])
        self.assertEqual(
            "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity",
            stage3.C8_AUTHORITY_POLICY["source_path"],
        )
        self.assertEqual(
            ("--fresh", "c8-effects"), stage3.C8_AUTHORITY_POLICY["child_args"]
        )
        self.assertEqual(82797, stage3.C8_AUTHORITY_POLICY["source_size"])
        self.assertEqual(
            "sha256:de3fb80e14336cadacf710a0b2fef33b19efab0728d5ca08e7a25c72df7afe16",
            stage3.C8_AUTHORITY_POLICY["source_sha256"],
        )
        with self.assertRaises(stage3.Stage3Error):
            stage3._authority_policy_for_batch("arbitrary-module")
        with self.assertRaises(stage3.Stage3Error):
            stage3.batch_command("c8-authority")

        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source_relative = Path(str(stage3.C8_AUTHORITY_POLICY["source_path"]))
            source = root / source_relative
            source.parent.mkdir(parents=True)
            source.write_bytes(
                (Path(__file__).parents[2] / source_relative).read_bytes()
            )
            proof = root / sh07.PROOF_CONTRACT_RELATIVE
            proof.parent.mkdir(parents=True)
            proof.write_bytes(b"fixed proof contract")
            state = root / "state"
            modules = state / "modules"
            modules.mkdir(parents=True)
            stdout = modules / "c8-effects.stdout.log"
            stderr = modules / "c8-effects.stderr.log"
            stdout.write_bytes(b"structured c8 output")
            stderr.write_bytes(b"")
            context_sha = "sha256:" + "c" * 64
            shared_sha = "sha256:" + "d" * 64
            source_sha = "sha256:" + hashlib.sha256(source.read_bytes()).hexdigest()
            context = {
                "module": "c8-effects",
                "sha256": context_sha,
                "files": [{
                    "path": str(source_relative),
                    "size": source.stat().st_size,
                    "sha256": source_sha,
                }],
            }
            record = {
                "state": "passed",
                "command": [*sh07.default_base_command(), "--fresh", "c8-effects"],
                "module_context_fingerprint": context_sha,
                "proof_contract_sha256": "sha256:" + hashlib.sha256(proof.read_bytes()).hexdigest(),
                "module_context": context,
                "context_stable": True,
                "output_contract_checked": True,
                "stdout_path": "modules/c8-effects.stdout.log",
                "stderr_path": "modules/c8-effects.stderr.log",
                "stdout_sha256": "sha256:" + hashlib.sha256(stdout.read_bytes()).hexdigest(),
                "stderr_sha256": "sha256:" + hashlib.sha256(stderr.read_bytes()).hexdigest(),
                "exit_code": 0,
                "raw_child_exit_code": 0,
                "timed_out": False,
            }
            manifest = {
                "schema": sh07.SCHEMA,
                "tool_version": sh07.TOOL_VERSION,
                "fingerprint_policy_version": sh07.FINGERPRINT_POLICY_VERSION,
                "state": "completed",
                "selected_modules": ["c8-effects"],
                "aggregate_authoritative": False,
                "authority_scope": "individual-source-bound-derived",
                "resumed_modules": [],
                "lock_path": str(self.lock),
                "lock_mode": "0600",
                "lock_acquired": True,
                "lock_validated": True,
                "lock_released": True,
                "shared_context_fingerprint": shared_sha,
                "shared_context_fingerprint_after": shared_sha,
                "shared_context": {
                    "command": sh07.default_base_command(),
                    "authoritative_module_catalog": {
                        "c8-effects": str(source_relative),
                    },
                },
                "modules": {"c8-effects": record},
            }
            with mock.patch.object(
                stage3,
                "_recompute_shared_context",
                return_value={"sha256": shared_sha},
            ):
                valid, evidence = stage3._authority_manifest_valid(
                    root,
                    state,
                    manifest,
                    check_output_contract=False,
                    policy=stage3.C8_AUTHORITY_POLICY,
                )
            self.assertTrue(valid, evidence)
            self.assertEqual("c8-effects", evidence["module"])
            record["command"] = [*sh07.default_base_command(), "--fresh", "c7-types"]
            with mock.patch.object(
                stage3,
                "_recompute_shared_context",
                return_value={"sha256": shared_sha},
            ):
                invalid, details = stage3._authority_manifest_valid(
                    root,
                    state,
                    manifest,
                    check_output_contract=False,
                    policy=stage3.C8_AUTHORITY_POLICY,
                )
            self.assertFalse(invalid)
            self.assertTrue(any("fresh module" in error for error in details["errors"]))

    def test_c9_fixed_policy_binds_module_source_and_fresh_child_command(self) -> None:
        """C9 proof admission is an exact fixed policy, never a passthrough."""

        policy = stage3.C9_AUTHORITY_POLICY
        self.assertEqual("c9-ownership", policy["module"])
        self.assertEqual(
            "bootstrap/gravity/src/gravity/compiler/c9_ownership_checker_engine.gravity",
            policy["source_path"],
        )
        self.assertEqual(71132, policy["source_size"])
        self.assertEqual(
            "sha256:4f26a5ca5fdd7755016f332fc5c795f84a98b83b76cef79806b8021807897fcd",
            policy["source_sha256"],
        )
        self.assertEqual(("--fresh", "c9-ownership"), policy["child_args"])
        with self.assertRaises(stage3.Stage3Error):
            stage3._authority_policy_for_batch("c9-ownership")
        with self.assertRaises(stage3.Stage3Error):
            stage3.batch_command("c9-authority")

        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source_relative = Path(str(policy["source_path"]))
            source = root / source_relative
            source.parent.mkdir(parents=True)
            source.write_bytes((Path(__file__).parents[2] / source_relative).read_bytes())
            self.assertEqual(source.stat().st_size, policy["source_size"])
            self.assertEqual(self._hash(source), policy["source_sha256"])

    def test_c10_fixed_policy_binds_module_source_and_fresh_child_command(self) -> None:
        """C10 proof admission is fixed to the frozen safety source."""

        policy = stage3.C10_AUTHORITY_POLICY
        self.assertEqual("c10-safety", policy["module"])
        self.assertEqual(
            "bootstrap/gravity/src/gravity/compiler/c10_safety_analysis_pipeline.gravity",
            policy["source_path"],
        )
        self.assertEqual(112712, policy["source_size"])
        self.assertEqual(
            "sha256:2d334872a84394acc636280796e205a74b227327aa3d646d6c19d55210bd4968",
            policy["source_sha256"],
        )
        self.assertEqual(("--fresh", "c10-safety"), policy["child_args"])
        with self.assertRaises(stage3.Stage3Error):
            stage3._authority_policy_for_batch("c10-safety")
        with self.assertRaises(stage3.Stage3Error):
            stage3.batch_command("c10-authority")

        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source_relative = Path(str(policy["source_path"]))
            source = root / source_relative
            source.parent.mkdir(parents=True)
            source.write_bytes((Path(__file__).parents[2] / source_relative).read_bytes())
            self.assertEqual(source.stat().st_size, policy["source_size"])
            self.assertEqual(self._hash(source), policy["source_sha256"])

    def test_c11_fixed_policy_binds_module_source_and_fresh_child_command(self) -> None:
        """C11 proof admission is fixed to the frozen MIR source."""

        policy = stage3.C11_AUTHORITY_POLICY
        self.assertEqual("c11-mir", policy["module"])
        self.assertEqual(
            "bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity",
            policy["source_path"],
        )
        self.assertEqual(253588, policy["source_size"])
        self.assertEqual(
            "sha256:34f0e797420b35417dbecb32c28465f7ffbb867c18ac59159bf8ace465054136",
            policy["source_sha256"],
        )
        self.assertEqual(("--fresh", "c11-mir"), policy["child_args"])
        with self.assertRaises(stage3.Stage3Error):
            stage3._authority_policy_for_batch("c11-mir")
        with self.assertRaises(stage3.Stage3Error):
            stage3.batch_command("c11-authority")

        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source_relative = Path(str(policy["source_path"]))
            source = root / source_relative
            source.parent.mkdir(parents=True)
            source.write_bytes((Path(__file__).parents[2] / source_relative).read_bytes())
            self.assertEqual(source.stat().st_size, policy["source_size"])
            self.assertEqual(self._hash(source), policy["source_sha256"])

    def test_c8_proof_candidate_uses_only_fixed_child_module(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            observed: list[list[str]] = []

            def launcher(command, cwd, env, timeout):
                observed.append(list(command))
                return stage3.ChildResult(0, "", "", False, (), None, False)

            with mock.patch.object(
                stage3,
                "_authority_manifest_valid",
                return_value=(True, {"module": "c8-effects"}),
            ):
                code, receipt = stage3.run_stage3(
                    root=root,
                    receipt_path=root / ".cpcache" / "c8-proof.json",
                    nonce="c8-proof",
                    check_id="c8-proof",
                    mode=stage3.MODE_PROOF_CANDIDATE,
                    batch="c8-authority",
                    command_identity_sha256="sha256:" + "1" * 64,
                    launcher=launcher,
                    timeout_seconds=2,
                )
            self.assertEqual(0, code)
            self.assertTrue(receipt["proof_candidate"])
            self.assertEqual("none", receipt["authority"])
            self.assertEqual("c8-authority", receipt["proof_batch"])
            self.assertEqual("c8-effects", receipt["proof_module"])
            self.assertEqual("c8-effects", observed[0][observed[0].index("--module") + 1])

    def test_c9_proof_candidate_uses_only_fixed_child_module(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            observed: list[list[str]] = []

            def launcher(command, cwd, env, timeout):
                observed.append(list(command))
                return stage3.ChildResult(0, "", "", False, (), None, False)

            with mock.patch.object(
                stage3,
                "_authority_manifest_valid",
                return_value=(True, {"module": "c9-ownership"}),
            ):
                code, receipt = stage3.run_stage3(
                    root=root,
                    receipt_path=root / ".cpcache" / "c9-proof.json",
                    nonce="c9-proof",
                    check_id="c9-proof",
                    mode=stage3.MODE_PROOF_CANDIDATE,
                    batch="c9-authority",
                    command_identity_sha256="sha256:" + "2" * 64,
                    launcher=launcher,
                    timeout_seconds=2,
                )
            self.assertEqual(0, code)
            self.assertTrue(receipt["proof_candidate"])
            self.assertEqual("none", receipt["authority"])
            self.assertEqual("c9-authority", receipt["proof_batch"])
            self.assertEqual("c9-ownership", receipt["proof_module"])
            self.assertEqual("c9-ownership", observed[0][observed[0].index("--module") + 1])

    def test_c10_proof_candidate_uses_only_fixed_child_module(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            observed: list[list[str]] = []

            def launcher(command, cwd, env, timeout):
                observed.append(list(command))
                return stage3.ChildResult(0, "", "", False, (), None, False)

            with mock.patch.object(
                stage3,
                "_authority_manifest_valid",
                return_value=(True, {"module": "c10-safety"}),
            ):
                code, receipt = stage3.run_stage3(
                    root=root,
                    receipt_path=root / ".cpcache" / "c10-proof.json",
                    nonce="c10-proof",
                    check_id="c10-proof",
                    mode=stage3.MODE_PROOF_CANDIDATE,
                    batch="c10-authority",
                    command_identity_sha256="sha256:" + "3" * 64,
                    launcher=launcher,
                    timeout_seconds=2,
                )
            self.assertEqual(0, code)
            self.assertTrue(receipt["proof_candidate"])
            self.assertEqual("none", receipt["authority"])
            self.assertEqual("c10-authority", receipt["proof_batch"])
            self.assertEqual("c10-safety", receipt["proof_module"])
            self.assertEqual(
                "c10-safety", observed[0][observed[0].index("--module") + 1]
            )

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
