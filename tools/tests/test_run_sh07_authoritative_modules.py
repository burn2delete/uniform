from __future__ import annotations

import fcntl
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import time
import unittest
from unittest import mock


TOOLS = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(TOOLS))

import run_sh07_authoritative_modules as runner  # noqa: E402


def fixture_output_validator(
    module: str,
    source_path: str,
    source_byte_count: int,
    source_bytes_sha256: str,
    _proof_contract_sha256: str,
    path: Path,
) -> bool:
    output = path.read_text(encoding="utf-8")
    return (
        output.startswith(
            "{:artifact :gravity/sh07-authoritative-proof-run "
            ":schema-version 2 :status :passed "
        )
        and f':module "{module}"' in output
        and f':source-path "{source_path}"' in output
        and f":source-byte-count {source_byte_count}" in output
        and f':source-bytes-sha256 "{source_bytes_sha256}"' in output
        and ":verification-status :passed" in output
        and ":capability-proof-status :complete" in output
        and ":failed-checks []" in output
    )


def authoritative_output(
    source_size: int,
    source_sha: str,
    *,
    form_count: int = 0,
    census_hash: str = "sha256:c77fe8082508cb13e566555686f893c03e9e854de0a19b5d53876d5d500bf947",
) -> str:
    return (
        "{:artifact :gravity/sh07-authoritative-proof-run "
        ":schema-version 2 :status :passed "
        ":fresh-process-required? true :persistent-iteration-cache-used? false "
        ":modules [{:module \"alpha\" :status :accepted "
        ":source-path \"bootstrap/gravity/src/gravity/alpha.gravity\" "
        f":source-byte-count {source_size} :source-bytes-sha256 \"{source_sha}\" "
        f":source-revision-id \"{source_sha}\" :artifact-id \"sha256:artifact\" "
        ":verification-status :passed :capability-proof-status :complete "
        ":failed-checks [] :coverage-census "
        "{:artifact :gravity/sh07-authoritative-coverage-census :schema-version 1 "
        ":authority-scope :individual-existing-runner-output-only "
        ":aggregate-authoritative? false :module \"alpha\" "
        f":module-namespace gravity.alpha :source-revision-id \"{source_sha}\" "
        ":sh07-artifact-id \"sha256:artifact\" :sh06-status :accepted "
        ":task \"SH-07-B45\" :request-schema-version 15 "
        ":scope :sh07-b15-keyword-map-lookup "
        f":source-binding {{:source-byte-count {source_size} "
        f":source-bytes-sha256 \"{source_sha}\"}} "
        ":request-counts {:fragment-count 0 :root-form-count 0 "
        f":form-count {form_count} :binding-count 0 :local-binding-count 0 "
        ":resolution-count 0} :core-counts {:core-node-count 0 "
        ":definition-count 0 :call-count 0 :reference-count 0 "
        ":keyword-lookup-count 0 :core-form-frequencies {}} "
        ":integrity {:root-form-id-order-exact? true :form-id-order-exact? true "
        ":source-snapshot-stable? true :source-revision-bound-to-bytes? true "
        ":target-source-reread-disabled? true} "
        f":census-hash \"{census_hash}\"}} "
        ":contract-checks {:exact? true "
        ":authoritative-coverage-census-current? true}}]}\n"
    )


class FakeLauncher:
    def __init__(self, outcomes: dict[str, runner.ProcessOutcome] | None = None) -> None:
        self.outcomes = outcomes or {}
        self.calls: list[str] = []

    def __call__(
        self,
        command: list[str],
        cwd: Path,
        stdout_path: Path,
        stderr_path: Path,
        _timeout_seconds: float,
    ) -> runner.ProcessOutcome:
        module = command[-1]
        self.calls.append(module)
        outcome = self.outcomes.get(module, runner.ProcessOutcome(0, False, 0.01))
        if outcome.exit_code == 0 and not outcome.timed_out:
            source_path = f"bootstrap/gravity/src/gravity/{module}.gravity"
            source = cwd / source_path
            stdout_path.write_text(
                "{:artifact :gravity/sh07-authoritative-proof-run "
                ":schema-version 2 :status :passed "
                ":fresh-process-required? true "
                ":persistent-iteration-cache-used? false "
                f':modules [{{:module "{module}" :status :accepted '
                f':source-path "{source_path}" '
                f":source-byte-count {source.stat().st_size} "
                f':source-bytes-sha256 "{runner.sha256_file(source)}" '
                ":verification-status :passed "
                ":capability-proof-status :complete :failed-checks [] "
                ":contract-checks {:ok true}}]}\n",
                encoding="utf-8",
            )
        else:
            stdout_path.write_text(
                f'{{:status :failed :modules [{{:module "{module}"}}]}}\n',
                encoding="utf-8",
            )
        stderr_path.write_text(
            "timed out\n" if outcome.timed_out else "failure\n" if outcome.exit_code else "",
            encoding="utf-8",
        )
        return outcome


class Sh07CheckpointTests(unittest.TestCase):
    @staticmethod
    def module_catalog() -> dict[str, str]:
        return {
            module: f"bootstrap/gravity/src/gravity/{module}.gravity"
            for module in ["alpha", "beta", "gamma"]
        }

    def make_repository(self, root: Path) -> None:
        files = {
            "deps.edn": "{:paths []}\n",
            "bootstrap/clojure/src/gravity/bootstrap.clj": "(ns gravity.bootstrap)\n",
            "bootstrap/gravity/p15_s23/compiler.gravity": "(ns gravity.p15-s23.compiler)\n",
            "bootstrap/gravity/p15_s23/emitter.gravity": "(ns gravity.p15-s23.emitter)\n",
            "bootstrap/gravity/src/gravity/macro.gravity": "(ns gravity.macro)\n",
            "bootstrap/gravity/src/gravity/resolution.gravity": "(ns gravity.resolution)\n",
            "bootstrap/gravity/src/gravity/checked_core.gravity": "(ns gravity.checked-core)\n",
            "bootstrap/gravity/src/gravity/alpha.gravity": "(ns gravity.alpha)\n",
            "bootstrap/gravity/src/gravity/beta.gravity": "(ns gravity.beta)\n",
            "bootstrap/gravity/src/gravity/gamma.gravity": "(ns gravity.gamma)\n",
            "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn": (
                "{:schema :test "
                ":boundary {:task \"SH-07-B45\" :request-schema-version 15 "
                ":scope :sh07-b15-keyword-map-lookup} "
                ":authoritative-coverage-census "
                "{:schema-version 1 :module-expectations "
                "{:alpha {:module-namespace gravity.alpha "
                ":source-binding {:source-byte-count 19 "
                ":source-bytes-sha256 "
                '"sha256:164685cc0f9ae0bba036899d1be9fc9e56d8220af87990fd35f3078286ea00c0"} '
                ":request-counts {:fragment-count 0 :root-form-count 0 "
                ":form-count 0 :binding-count 0 :local-binding-count 0 "
                ":resolution-count 0} "
                ":core-counts {:core-node-count 0 :definition-count 0 "
                ":call-count 0 :reference-count 0 :keyword-lookup-count 0 "
                ":core-form-frequencies {}}}}}}\n"
            ),
            "bootstrap/clojure/test/gravity/self_hosting/sh07_authoritative_runner.clj": "(ns test.runner)\n",
        }
        for relative, content in files.items():
            path = root / relative
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content, encoding="utf-8")

    def make_source_bound_repository(self, root: Path) -> tuple[int, str]:
        self.make_repository(root)
        source = root / self.module_catalog()["alpha"]
        source_size = source.stat().st_size
        source_sha = runner.sha256_file(source)
        (root / runner.PROOF_CONTRACT_RELATIVE).write_text(
            "{:artifact :gravity/sh07-proof-process-contract "
            ":schema-version 2 :coverage-census-policy :source-bound-derived "
            ":authority-claims {:unsupported-claims "
            "[:exact-authentic-coverage :aggregate :release]} "
            ":boundary {:task \"SH-07-B45\" :request-schema-version 15 "
            ":scope :sh07-b15-keyword-map-lookup} "
            ":authoritative-coverage-census {:schema-version 2 "
            ":policy :source-bound-derived :counts-precommitted? false "
            ":independent-count-oracle? false "
            ":unsupported-claims [:exact-authentic-coverage :aggregate :release] "
            ":module-expectations {:alpha {:module-namespace gravity.alpha "
            f":source-binding {{:source-byte-count {source_size} "
            f':source-bytes-sha256 "{source_sha}"' + "}}}}}\n",
            encoding="utf-8",
        )
        return source_size, source_sha

    def source_bound_output(self, source_size: int, source_sha: str) -> str:
        return (
            "{:artifact :gravity/sh07-authoritative-proof-run :schema-version 3 "
            ":status :passed :fresh-process-required? true "
            ":persistent-iteration-cache-used? false :modules [{:module \"alpha\" "
            ":status :accepted :source-path "
            "\"bootstrap/gravity/src/gravity/alpha.gravity\" "
            f":source-byte-count {source_size} :source-bytes-sha256 \"{source_sha}\" "
            f":source-revision-id \"{source_sha}\" "
            ':artifact-id "sha256:' + "a" * 64 + '" '
            ":verification-status :passed :capability-proof-status :complete "
            ":failed-checks [] :nested {:artifact-id \"sha256:"
            + "b" * 64 + "\"} :coverage-census "
            "{:artifact :gravity/sh07-authoritative-coverage-census :schema-version 2 "
            ":authority-scope :individual-source-bound-derived "
            ":aggregate-authoritative? false :coverage-census-policy "
            ":source-bound-derived :counts-precommitted? false "
            ":independent-count-oracle? false :unsupported-claims "
            "[:exact-authentic-coverage :aggregate :release] "
            ":census-hash \"sha256:" + "c" * 64 + "\"}}]}\n"
        )

    def run_in_repository(
        self,
        root: Path,
        launcher: FakeLauncher,
        modules: list[str],
        *,
        resume: bool = True,
        source_contracts: runner.SourceContracts | None = None,
    ) -> tuple[int, dict[str, object]]:
        proof_contract = (
            root
            / "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn"
        )
        return runner.run_modules(
            root=root,
            state_dir=root / "checkpoints",
            modules=modules,
            module_catalog=self.module_catalog(),
            base_command=["fake-runner"],
            timeout_seconds=1,
            resume=resume,
            launcher=launcher,
            output_validator=fixture_output_validator,
            source_contracts=source_contracts,
            source_contract_proof_sha256=(
                runner.sha256_file(proof_contract) if source_contracts else None
            ),
            lock_path=root / "heavy.lock",
        )

    def source_contract(self, root: Path, module: str) -> dict[str, dict[str, object]]:
        relative = self.module_catalog()[module]
        source = root / relative
        return {
            module: {
                "source_path": relative,
                "source_byte_count": source.stat().st_size,
                "source_bytes_sha256": runner.sha256_file(source),
            }
        }

    def test_source_contract_mismatch_records_exit_75_without_launch(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            contract = self.source_contract(root, "alpha")
            contract["alpha"]["source_byte_count"] = 999
            launcher = FakeLauncher()
            code, manifest = self.run_in_repository(
                root, launcher, ["alpha"], source_contracts=contract
            )
            self.assertEqual(75, code)
            self.assertEqual([], launcher.calls)
            self.assertEqual("source-contract-mismatch", manifest["state"])
            self.assertIn("source contract mismatch", manifest["preflight_error"])
            persisted = json.loads(
                (root / "checkpoints/manifest.json").read_text(encoding="utf-8")
            )
            self.assertEqual("source-contract-mismatch", persisted["state"])
            self.assertEqual({}, persisted["modules"])

    @unittest.skipUnless(shutil.which("clojure"), "clojure CLI is required")
    def test_source_bound_attestation_links_nested_edn_and_rejects_tamper(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source_size, source_sha = self.make_source_bound_repository(root)
            stdout = root / "proof.edn"
            stdout.write_text(self.source_bound_output(source_size, source_sha), encoding="utf-8")
            contract_sha = runner.sha256_file(root / runner.PROOF_CONTRACT_RELATIVE)
            self.assertEqual("source-bound-derived", runner.source_bound_policy(root))
            policy_contract = root / runner.PROOF_CONTRACT_RELATIVE
            policy_contract.write_text(
                policy_contract.read_text(encoding="utf-8").replace(
                    ":policy :source-bound-derived",
                    ":policy :exact-precommitted",
                    1,
                ),
                encoding="utf-8",
            )
            with self.assertRaises(runner.CheckpointError):
                runner.source_bound_policy(root)
            policy_contract.write_text(
                policy_contract.read_text(encoding="utf-8").replace(
                    ":policy :exact-precommitted",
                    ":policy :source-bound-derived",
                    1,
                ),
                encoding="utf-8",
            )
            contract_sha = runner.sha256_file(policy_contract)
            attestation = runner.create_source_bound_attestation(
                root, "alpha", stdout,
                proof_contract_sha256=contract_sha,
                reviewer="sol-reviewer",
                reviewed_at="2026-08-07T12:00:00Z",
                method="independent source/census/artifact linkage review",
                limitations=["counts are derived, not independently predeclared"],
            )
            self.assertTrue(runner.validate_source_bound_attestation(
                root, "alpha", stdout, attestation,
                expected_proof_contract_sha256=contract_sha,
            ))
            contract_path = root / runner.PROOF_CONTRACT_RELATIVE
            contract_path.write_text(
                contract_path.read_text(encoding="utf-8").replace(
                    ":schema-version 2", ":schema-version 3", 1
                ),
                encoding="utf-8",
            )
            self.assertFalse(runner.validate_source_bound_attestation(
                root, "alpha", stdout, attestation,
                expected_proof_contract_sha256=contract_sha,
            ))
            forged = dict(attestation, artifact_id="sha256:" + "d" * 64)
            self.assertFalse(runner.validate_source_bound_attestation(
                root, "alpha", stdout, forged,
                expected_proof_contract_sha256=contract_sha,
            ))
            stdout.write_text(stdout.read_text(encoding="utf-8") + " ", encoding="utf-8")
            self.assertFalse(runner.validate_source_bound_attestation(
                root, "alpha", stdout, attestation,
                expected_proof_contract_sha256=contract_sha,
            ))

    def test_forged_manifest_proof_contract_binding_never_resumes(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            launcher = FakeLauncher()
            contracts = self.source_contract(root, "alpha")
            code, _ = self.run_in_repository(
                root, launcher, ["alpha"], source_contracts=contracts
            )
            self.assertEqual(0, code)
            manifest_path = root / "checkpoints/manifest.json"
            manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
            manifest["modules"]["alpha"]["proof_contract_sha256"] = "sha256:" + "f" * 64
            manifest_path.write_text(json.dumps(manifest), encoding="utf-8")
            code, _ = self.run_in_repository(
                root, launcher, ["alpha"], source_contracts=contracts
            )
            self.assertEqual(0, code)
            self.assertEqual(["alpha", "alpha"], launcher.calls)

    def test_source_bound_unbound_selection_is_rejected_before_launch(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_source_bound_repository(root)
            launcher = FakeLauncher()
            with self.assertRaisesRegex(
                    runner.CheckpointError, "requires source contracts"):
                runner.run_modules(
                    root=root,
                    state_dir=root / "checkpoints",
                    modules=["beta"],
                    module_catalog=self.module_catalog(),
                    base_command=["fake-runner"],
                    launcher=launcher,
                    output_validator=fixture_output_validator,
                    source_contracts={
                        "alpha": {
                            "source_path": self.module_catalog()["alpha"],
                            "source_byte_count": 19,
                            "source_bytes_sha256": runner.sha256_file(
                                root / self.module_catalog()["alpha"]
                            ),
                        }
                    },
                    source_contract_proof_sha256=runner.sha256_file(
                        root / runner.PROOF_CONTRACT_RELATIVE
                    ),
                    lock_path=root / "heavy.lock",
                )
            self.assertEqual([], launcher.calls)

    def test_matching_source_contract_allows_launch(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            launcher = FakeLauncher()
            code, _ = self.run_in_repository(
                root,
                launcher,
                ["alpha"],
                source_contracts=self.source_contract(root, "alpha"),
            )
            self.assertEqual(0, code)
            self.assertEqual(["alpha"], launcher.calls)

    def test_source_contract_requires_trusted_contract_hash(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            with self.assertRaisesRegex(runner.CheckpointError, "trusted proof contract"):
                runner.run_modules(
                    root=root,
                    state_dir=root / "checkpoints",
                    modules=["alpha"],
                    module_catalog=self.module_catalog(),
                    base_command=["fake-runner"],
                    launcher=FakeLauncher(),
                    output_validator=fixture_output_validator,
                    source_contracts=self.source_contract(root, "alpha"),
                    lock_path=root / "heavy.lock",
                )

    def test_source_contract_discovery_binds_exact_contract_hash(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            source = root / self.module_catalog()["alpha"]
            contract_sha = "sha256:" + "a" * 64
            output = (
                f"#proof-contract-sha256\t{contract_sha}\n"
                "#coverage-census-policy\tsource-bound-derived\n"
                "alpha\tbootstrap/gravity/src/gravity/alpha.gravity\t"
                f"{source.stat().st_size}\t{runner.sha256_file(source)}\n"
            )
            completed = subprocess.CompletedProcess(
                ["fake-runner", "--source-contracts"], 0, output, ""
            )
            with mock.patch.object(runner.subprocess, "run", return_value=completed):
                observed_sha, contracts = runner.discover_source_contracts(
                    root,
                    ["fake-runner"],
                    1,
                    module_catalog=self.module_catalog(),
                )
            self.assertEqual(contract_sha, observed_sha)
            self.assertEqual(self.source_contract(root, "alpha"), contracts)

    def test_identical_context_resumes_only_verified_passed_modules(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            launcher = FakeLauncher()
            code, first = self.run_in_repository(root, launcher, ["alpha", "beta"])
            self.assertEqual(0, code)
            self.assertEqual(["alpha", "beta"], launcher.calls)
            self.assertEqual("completed", first["state"])

            code, second = self.run_in_repository(root, launcher, ["alpha", "beta"])
            self.assertEqual(0, code)
            self.assertEqual(["alpha", "beta"], launcher.calls)
            self.assertEqual("completed", second["state"])
            self.assertFalse(second["aggregate_authoritative"])
            manifest = json.loads((root / "checkpoints/manifest.json").read_text())
            self.assertEqual(runner.SCHEMA, manifest["schema"])
            self.assertEqual("passed", manifest["modules"]["alpha"]["state"])
            self.assertTrue((root / "checkpoints/modules/alpha.stdout.log").is_file())
            self.assertFalse(list((root / "checkpoints").glob(".*.tmp")))

    def test_source_change_invalidates_every_checkpoint(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            launcher = FakeLauncher()
            _, first = self.run_in_repository(root, launcher, ["alpha", "beta"])
            previous = first["shared_context_fingerprint"]
            source = root / "bootstrap/gravity/src/gravity/checked_core.gravity"
            source.write_text("(ns gravity.checked-core)\n;; changed\n", encoding="utf-8")

            code, second = self.run_in_repository(root, launcher, ["alpha", "beta"])
            self.assertEqual(0, code)
            self.assertEqual(["alpha", "beta", "alpha", "beta"], launcher.calls)
            self.assertNotEqual(previous, second["shared_context_fingerprint"])
            self.assertEqual(
                previous, second["invalidated_shared_context_fingerprint"]
            )

    def test_stage2_plan_source_changes_invalidate_every_checkpoint(self) -> None:
        for name in ["compiler.gravity", "emitter.gravity"]:
            with self.subTest(name=name), tempfile.TemporaryDirectory() as directory:
                root = Path(directory)
                self.make_repository(root)
                launcher = FakeLauncher()
                _, first = self.run_in_repository(root, launcher, ["alpha", "beta"])
                previous = first["shared_context_fingerprint"]
                source = root / "bootstrap/gravity/p15_s23" / name
                source.write_text(
                    source.read_text(encoding="utf-8") + "; changed\n",
                    encoding="utf-8",
                )

                code, second = self.run_in_repository(
                    root, launcher, ["alpha", "beta"]
                )
                self.assertEqual(0, code)
                self.assertEqual(
                    ["alpha", "beta", "alpha", "beta"], launcher.calls
                )
                self.assertNotEqual(
                    previous, second["shared_context_fingerprint"]
                )
                self.assertEqual(
                    previous, second["invalidated_shared_context_fingerprint"]
                )

    def test_module_source_change_invalidates_only_its_checkpoint(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            launcher = FakeLauncher()
            self.run_in_repository(root, launcher, ["alpha", "beta"])
            source = root / "bootstrap/gravity/src/gravity/alpha.gravity"
            source.write_text("(ns gravity.alpha)\n; changed\n", encoding="utf-8")

            code, manifest = self.run_in_repository(
                root, launcher, ["alpha", "beta"]
            )
            self.assertEqual(0, code)
            self.assertEqual(["alpha", "beta", "alpha"], launcher.calls)
            self.assertEqual(["beta"], manifest["resumed_modules"])
            self.assertNotEqual(
                manifest["modules"]["alpha"]["module_context_fingerprint"],
                manifest["modules"]["beta"]["module_context_fingerprint"],
            )

    def test_unselected_module_source_change_does_not_invalidate_selection(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            launcher = FakeLauncher()
            self.run_in_repository(root, launcher, ["alpha", "beta"])
            source = root / "bootstrap/gravity/src/gravity/gamma.gravity"
            source.write_text("(ns gravity.gamma)\n; changed\n", encoding="utf-8")

            code, manifest = self.run_in_repository(
                root, launcher, ["alpha", "beta"]
            )
            self.assertEqual(0, code)
            self.assertEqual(["alpha", "beta"], launcher.calls)
            self.assertEqual(["alpha", "beta"], manifest["resumed_modules"])

    def test_pinned_gravity_source_changes_invalidate_every_checkpoint(self) -> None:
        for relative in [
            "bootstrap/gravity/src/gravity/macro.gravity",
            "bootstrap/gravity/src/gravity/resolution.gravity",
        ]:
            with self.subTest(relative=relative), tempfile.TemporaryDirectory() as directory:
                root = Path(directory)
                self.make_repository(root)
                launcher = FakeLauncher()
                self.run_in_repository(root, launcher, ["alpha", "beta"])
                source = root / relative
                source.write_text(
                    source.read_text(encoding="utf-8") + "; changed\n",
                    encoding="utf-8",
                )

                code, _ = self.run_in_repository(root, launcher, ["alpha", "beta"])
                self.assertEqual(0, code)
                self.assertEqual(
                    ["alpha", "beta", "alpha", "beta"], launcher.calls
                )

    def test_source_change_during_child_run_stops_the_sequence(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            delegate = FakeLauncher()

            def mutating_launcher(
                command: list[str],
                cwd: Path,
                stdout_path: Path,
                stderr_path: Path,
                timeout_seconds: float,
            ) -> runner.ProcessOutcome:
                outcome = delegate(
                    command, cwd, stdout_path, stderr_path, timeout_seconds
                )
                source = root / "bootstrap/gravity/src/gravity/checked_core.gravity"
                source.write_text(
                    source.read_text(encoding="utf-8") + "; changed during run\n",
                    encoding="utf-8",
                )
                return outcome

            code, manifest = runner.run_modules(
                root=root,
                state_dir=root / "checkpoints",
                modules=["alpha", "beta"],
                module_catalog={
                    "alpha": "bootstrap/gravity/src/gravity/alpha.gravity",
                    "beta": "bootstrap/gravity/src/gravity/beta.gravity",
                },
                base_command=["fake-runner"],
                timeout_seconds=1,
                launcher=mutating_launcher,
                output_validator=fixture_output_validator,
                lock_path=root / "heavy.lock",
            )
            self.assertEqual(75, code)
            self.assertEqual(["alpha"], delegate.calls)
            self.assertEqual("context-changed", manifest["state"])
            self.assertFalse(manifest["modules"]["alpha"]["context_stable"])

    def test_current_module_change_during_child_stops_the_sequence(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            delegate = FakeLauncher()

            def mutating_launcher(
                command: list[str],
                cwd: Path,
                stdout_path: Path,
                stderr_path: Path,
                timeout_seconds: float,
            ) -> runner.ProcessOutcome:
                outcome = delegate(
                    command, cwd, stdout_path, stderr_path, timeout_seconds
                )
                if command[-1] == "alpha":
                    source = root / "bootstrap/gravity/src/gravity/alpha.gravity"
                    source.write_text("(ns gravity.alpha)\n; changed\n", encoding="utf-8")
                return outcome

            code, manifest = runner.run_modules(
                root=root,
                state_dir=root / "checkpoints",
                modules=["alpha", "beta"],
                module_catalog=self.module_catalog(),
                base_command=["fake-runner"],
                timeout_seconds=1,
                launcher=mutating_launcher,
                output_validator=fixture_output_validator,
                lock_path=root / "heavy.lock",
            )
            self.assertEqual(75, code)
            self.assertEqual(["alpha"], delegate.calls)
            self.assertEqual(["alpha"], manifest["modules"]["alpha"]["stale_modules"])

    def test_transient_module_bytes_change_and_restore_fails_output_binding(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            delegate = FakeLauncher()
            source = root / "bootstrap/gravity/src/gravity/alpha.gravity"
            original = source.read_bytes()

            def transient_launcher(
                command: list[str],
                cwd: Path,
                stdout_path: Path,
                stderr_path: Path,
                timeout_seconds: float,
            ) -> runner.ProcessOutcome:
                source.write_bytes(b"(ns gravity.alpha)\n; transient\n")
                try:
                    return delegate(
                        command, cwd, stdout_path, stderr_path, timeout_seconds
                    )
                finally:
                    source.write_bytes(original)

            code, manifest = runner.run_modules(
                root=root,
                state_dir=root / "checkpoints",
                modules=["alpha"],
                module_catalog=self.module_catalog(),
                base_command=["fake-runner"],
                timeout_seconds=1,
                launcher=transient_launcher,
                output_validator=fixture_output_validator,
                lock_path=root / "heavy.lock",
            )
            self.assertEqual(1, code)
            self.assertEqual("failed", manifest["state"])
            self.assertTrue(manifest["modules"]["alpha"]["context_stable"])
            self.assertFalse(manifest["modules"]["alpha"]["output_contract_checked"])

    def test_catalog_discovery_is_bounded_for_fresh_and_resumed_sequences(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            launcher = FakeLauncher()
            calls: list[int] = []

            def provider() -> dict[str, str]:
                calls.append(len(calls) + 1)
                return self.module_catalog()

            arguments = {
                "root": root,
                "state_dir": root / "checkpoints",
                "modules": ["alpha", "beta", "gamma"],
                "module_catalog": self.module_catalog(),
                "catalog_provider": provider,
                "base_command": ["fake-runner"],
                "timeout_seconds": 1,
                "launcher": launcher,
                "output_validator": fixture_output_validator,
                "lock_path": root / "heavy.lock",
            }
            code, _ = runner.run_modules(**arguments)
            self.assertEqual(0, code)
            self.assertEqual(2, len(calls))
            self.assertEqual(["alpha", "beta", "gamma"], launcher.calls)

            code, manifest = runner.run_modules(**arguments)
            self.assertEqual(0, code)
            self.assertEqual(4, len(calls))
            self.assertEqual(["alpha", "beta", "gamma"], launcher.calls)
            self.assertEqual(["alpha", "beta", "gamma"], manifest["resumed_modules"])

    def test_later_module_change_during_current_child_uses_new_context(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            delegate = FakeLauncher()

            def mutating_launcher(
                command: list[str],
                cwd: Path,
                stdout_path: Path,
                stderr_path: Path,
                timeout_seconds: float,
            ) -> runner.ProcessOutcome:
                outcome = delegate(
                    command, cwd, stdout_path, stderr_path, timeout_seconds
                )
                if command[-1] == "alpha":
                    source = root / "bootstrap/gravity/src/gravity/beta.gravity"
                    source.write_text("(ns gravity.beta)\n; changed\n", encoding="utf-8")
                return outcome

            code, manifest = runner.run_modules(
                root=root,
                state_dir=root / "checkpoints",
                modules=["alpha", "beta"],
                module_catalog=self.module_catalog(),
                base_command=["fake-runner"],
                timeout_seconds=1,
                launcher=mutating_launcher,
                output_validator=fixture_output_validator,
                lock_path=root / "heavy.lock",
            )
            self.assertEqual(0, code)
            self.assertEqual(["alpha", "beta"], delegate.calls)
            self.assertEqual("completed", manifest["state"])

    def test_completed_module_change_during_later_child_fails_final_sweep(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            delegate = FakeLauncher()

            def mutating_launcher(
                command: list[str],
                cwd: Path,
                stdout_path: Path,
                stderr_path: Path,
                timeout_seconds: float,
            ) -> runner.ProcessOutcome:
                outcome = delegate(
                    command, cwd, stdout_path, stderr_path, timeout_seconds
                )
                if command[-1] == "beta":
                    source = root / "bootstrap/gravity/src/gravity/alpha.gravity"
                    source.write_text("(ns gravity.alpha)\n; changed\n", encoding="utf-8")
                return outcome

            code, manifest = runner.run_modules(
                root=root,
                state_dir=root / "checkpoints",
                modules=["alpha", "beta"],
                module_catalog=self.module_catalog(),
                base_command=["fake-runner"],
                timeout_seconds=1,
                launcher=mutating_launcher,
                output_validator=fixture_output_validator,
                lock_path=root / "heavy.lock",
            )
            self.assertEqual(75, code)
            self.assertEqual(["alpha", "beta"], delegate.calls)
            self.assertEqual("context-changed", manifest["state"])
            self.assertEqual(["alpha"], manifest["modules"]["beta"]["stale_modules"])

    def test_catalog_mapping_change_during_child_stops_with_exit_75(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            delegate = FakeLauncher()
            observed_catalog = self.module_catalog()
            alternate = root / "bootstrap/gravity/src/gravity/alpha_v2.gravity"
            alternate.write_text("(ns gravity.alpha-v2)\n", encoding="utf-8")

            def mutating_launcher(
                command: list[str],
                cwd: Path,
                stdout_path: Path,
                stderr_path: Path,
                timeout_seconds: float,
            ) -> runner.ProcessOutcome:
                outcome = delegate(
                    command, cwd, stdout_path, stderr_path, timeout_seconds
                )
                observed_catalog["alpha"] = (
                    "bootstrap/gravity/src/gravity/alpha_v2.gravity"
                )
                return outcome

            code, manifest = runner.run_modules(
                root=root,
                state_dir=root / "checkpoints",
                modules=["alpha"],
                module_catalog=self.module_catalog(),
                catalog_provider=lambda: dict(observed_catalog),
                base_command=["fake-runner"],
                timeout_seconds=1,
                launcher=mutating_launcher,
                output_validator=fixture_output_validator,
                lock_path=root / "heavy.lock",
            )
            self.assertEqual(75, code)
            self.assertEqual("context-changed", manifest["state"])
            self.assertTrue(manifest["modules"]["alpha"]["context_stable"])
            self.assertTrue(manifest["modules"]["alpha"]["output_contract_checked"])
            self.assertIsNotNone(manifest["shared_context_fingerprint_after"])

    def test_tampered_output_invalidates_only_that_module_receipt(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            launcher = FakeLauncher()
            self.run_in_repository(root, launcher, ["alpha", "beta"])
            (root / "checkpoints/modules/alpha.stdout.log").write_text(
                "tampered\n", encoding="utf-8"
            )

            code, manifest = self.run_in_repository(root, launcher, ["alpha", "beta"])
            self.assertEqual(0, code)
            self.assertEqual(["alpha", "beta", "alpha"], launcher.calls)
            self.assertEqual("completed", manifest["state"])

    def test_coherently_tampered_manifest_and_output_cannot_resume(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            launcher = FakeLauncher()
            self.run_in_repository(root, launcher, ["alpha"])
            stdout = root / "checkpoints/modules/alpha.stdout.log"
            stdout.write_text("not an EDN proof\n", encoding="utf-8")
            manifest_path = root / "checkpoints/manifest.json"
            manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
            manifest["modules"]["alpha"]["stdout_sha256"] = runner.sha256_file(stdout)
            manifest_path.write_text(json.dumps(manifest), encoding="utf-8")

            code, _ = self.run_in_repository(root, launcher, ["alpha"])
            self.assertEqual(0, code)
            self.assertEqual(["alpha", "alpha"], launcher.calls)

    @unittest.skipUnless(shutil.which("clojure"), "clojure CLI is required")
    def test_coherently_rehashed_census_cannot_override_contract_expectation(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            calls: list[str] = []

            def launcher(
                command: list[str],
                cwd: Path,
                stdout_path: Path,
                stderr_path: Path,
                _timeout_seconds: float,
            ) -> runner.ProcessOutcome:
                module = command[-1]
                calls.append(module)
                source = cwd / "bootstrap/gravity/src/gravity/alpha.gravity"
                stdout_path.write_text(
                    authoritative_output(source.stat().st_size, runner.sha256_file(source)),
                    encoding="utf-8",
                )
                stderr_path.write_text("", encoding="utf-8")
                return runner.ProcessOutcome(0, False, 0.01)

            contract_path = root / runner.PROOF_CONTRACT_RELATIVE
            forge_during_validation = False
            forged_once = False

            def validator(
                module: str,
                source_path: str,
                source_size: int,
                source_sha: str,
                trusted_contract_sha: str,
                output_path: Path,
            ) -> bool:
                nonlocal forged_once
                original = contract_path.read_bytes()
                if forge_during_validation and not forged_once:
                    forged_once = True
                    contract_path.write_bytes(
                        original.replace(b":form-count 0", b":form-count 1", 1)
                    )
                try:
                    return runner.output_contract_passed(
                        module,
                        source_path,
                        source_size,
                        source_sha,
                        trusted_contract_sha,
                        output_path,
                        clojure_command="clojure",
                        cwd=root,
                    )
                finally:
                    contract_path.write_bytes(original)

            arguments = {
                "root": root,
                "state_dir": root / "checkpoints",
                "modules": ["alpha"],
                "module_catalog": self.module_catalog(),
                "base_command": ["clojure"],
                "timeout_seconds": 10,
                "launcher": launcher,
                "output_validator": validator,
                "lock_path": root / "heavy.lock",
            }
            code, _ = runner.run_modules(**arguments)
            self.assertEqual(0, code)
            stdout = root / "checkpoints/modules/alpha.stdout.log"
            source = root / "bootstrap/gravity/src/gravity/alpha.gravity"
            stdout.write_text(
                authoritative_output(
                    source.stat().st_size,
                    runner.sha256_file(source),
                    form_count=1,
                    census_hash=(
                        "sha256:ed906ed90e5f6f64609621ef65e6135d"
                        "b839663c3cfd7f7994af549e45c5376d"
                    ),
                ),
                encoding="utf-8",
            )
            manifest_path = root / "checkpoints/manifest.json"
            manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
            manifest["modules"]["alpha"]["stdout_sha256"] = runner.sha256_file(stdout)
            manifest_path.write_text(json.dumps(manifest), encoding="utf-8")

            forge_during_validation = True
            code, result = runner.run_modules(**arguments)
            self.assertEqual(0, code)
            self.assertEqual(["alpha", "alpha"], calls)
            self.assertEqual([], result["resumed_modules"])
            self.assertTrue(forged_once)
            self.assertEqual(
                runner.sha256_file(contract_path),
                result["shared_context"]["files"][
                    next(
                        index
                        for index, entry in enumerate(
                            result["shared_context"]["files"]
                        )
                        if entry["path"] == runner.PROOF_CONTRACT_RELATIVE
                    )
                ]["sha256"],
            )

    def test_manifest_log_path_cannot_escape_checkpoint_directory(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            launcher = FakeLauncher()
            self.run_in_repository(root, launcher, ["alpha"])
            outside = root / "outside.log"
            outside.write_text("external\n", encoding="utf-8")
            manifest_path = root / "checkpoints/manifest.json"
            manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
            entry = manifest["modules"]["alpha"]
            entry["stdout_path"] = "../../outside.log"
            entry["stdout_sha256"] = runner.sha256_file(outside)
            manifest_path.write_text(json.dumps(manifest), encoding="utf-8")

            code, _ = self.run_in_repository(root, launcher, ["alpha"])
            self.assertEqual(0, code)
            self.assertEqual(["alpha", "alpha"], launcher.calls)

    def test_failure_is_durable_and_stops_before_later_modules(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            launcher = FakeLauncher(
                {"beta": runner.ProcessOutcome(7, False, 0.02)}
            )
            code, manifest = self.run_in_repository(
                root, launcher, ["alpha", "beta", "gamma"]
            )
            self.assertEqual(7, code)
            self.assertEqual(["alpha", "beta"], launcher.calls)
            self.assertEqual("failed", manifest["state"])
            self.assertEqual("beta", manifest["stopped_at_module"])
            persisted = json.loads((root / "checkpoints/manifest.json").read_text())
            self.assertEqual(7, persisted["modules"]["beta"]["exit_code"])
            self.assertNotIn("gamma", persisted["modules"])
            self.assertEqual(
                "failure\n",
                (
                    root
                    / "checkpoints"
                    / persisted["modules"]["beta"]["stderr_path"]
                ).read_text(),
            )

    def test_timeout_returns_124_and_stops(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            launcher = FakeLauncher(
                {"alpha": runner.ProcessOutcome(124, True, 1.0)}
            )
            code, manifest = self.run_in_repository(root, launcher, ["alpha", "beta"])
            self.assertEqual(124, code)
            self.assertEqual(["alpha"], launcher.calls)
            self.assertEqual("timed-out", manifest["state"])
            self.assertEqual("timed-out", manifest["modules"]["alpha"]["state"])
            self.assertTrue(manifest["modules"]["alpha"]["timed_out"])

    def test_signal_exit_is_normalized_and_raw_code_is_preserved(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            launcher = FakeLauncher({"alpha": runner.ProcessOutcome(-9, False, 0.1)})
            code, manifest = self.run_in_repository(root, launcher, ["alpha"])
            self.assertEqual(137, code)
            self.assertEqual(137, manifest["modules"]["alpha"]["exit_code"])
            self.assertEqual(-9, manifest["modules"]["alpha"]["raw_child_exit_code"])

    def test_exit_zero_without_runner_pass_contract_fails_closed(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)

            def incomplete_output(
                command: list[str],
                _cwd: Path,
                stdout_path: Path,
                stderr_path: Path,
                _timeout_seconds: float,
            ) -> runner.ProcessOutcome:
                stdout_path.write_text("{:status :passed}\n", encoding="utf-8")
                stderr_path.write_text("", encoding="utf-8")
                return runner.ProcessOutcome(0, False, 0.01)

            code, manifest = runner.run_modules(
                root=root,
                state_dir=root / "checkpoints",
                modules=["alpha"],
                module_catalog={
                    "alpha": "bootstrap/gravity/src/gravity/alpha.gravity"
                },
                base_command=["fake-runner"],
                timeout_seconds=1,
                launcher=incomplete_output,
                output_validator=fixture_output_validator,
                lock_path=root / "heavy.lock",
            )
            self.assertEqual(1, code)
            self.assertEqual("failed", manifest["state"])
            self.assertFalse(manifest["modules"]["alpha"]["output_contract_checked"])

    def test_nested_pass_marker_cannot_override_failed_top_level_status(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)

            def misleading_output(
                command: list[str],
                _cwd: Path,
                stdout_path: Path,
                stderr_path: Path,
                _timeout_seconds: float,
            ) -> runner.ProcessOutcome:
                module = command[-1]
                stdout_path.write_text(
                    "{:artifact :gravity/sh07-authoritative-proof-run "
                    ":schema-version 2 :status :failed "
                    ":fresh-process-required? true "
                    ":persistent-iteration-cache-used? false "
                    f':modules [{{:module "{module}" :status :passed}}]}}\n',
                    encoding="utf-8",
                )
                stderr_path.write_text("", encoding="utf-8")
                return runner.ProcessOutcome(0, False, 0.01)

            code, manifest = runner.run_modules(
                root=root,
                state_dir=root / "checkpoints",
                modules=["alpha"],
                module_catalog={
                    "alpha": "bootstrap/gravity/src/gravity/alpha.gravity"
                },
                base_command=["fake-runner"],
                timeout_seconds=1,
                launcher=misleading_output,
                output_validator=fixture_output_validator,
                lock_path=root / "heavy.lock",
            )
            self.assertEqual(1, code)
            self.assertEqual("failed", manifest["state"])
            self.assertFalse(manifest["modules"]["alpha"]["output_contract_checked"])

    @unittest.skipUnless(shutil.which("clojure"), "clojure CLI is required")
    def test_production_validator_parses_one_exact_edn_result(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            source = root / "bootstrap/gravity/src/gravity/alpha.gravity"
            source_size = source.stat().st_size
            source_sha = runner.sha256_file(source)
            contract_sha = runner.sha256_file(
                root / runner.PROOF_CONTRACT_RELATIVE
            )
            output = root / "result.edn"
            output.write_text(
                "{:artifact :gravity/sh07-authoritative-proof-run "
                ":schema-version 2 :status :passed "
                ":fresh-process-required? true "
                ":persistent-iteration-cache-used? false "
                ":modules [{:module \"alpha\" :status :accepted "
                ":source-path \"bootstrap/gravity/src/gravity/alpha.gravity\" "
                f":source-byte-count {source_size} "
                f':source-bytes-sha256 "{source_sha}" '
                f':source-revision-id "{source_sha}" '
                ':artifact-id "sha256:artifact" '
                ":verification-status :passed "
                ":capability-proof-status :complete :failed-checks [] "
                ":coverage-census "
                "{:artifact :gravity/sh07-authoritative-coverage-census "
                ":schema-version 1 "
                ":authority-scope :individual-existing-runner-output-only "
                ":aggregate-authoritative? false :module \"alpha\" "
                ":module-namespace gravity.alpha "
                f':source-revision-id "{source_sha}" '
                ':sh07-artifact-id "sha256:artifact" :sh06-status :accepted '
                ':task "SH-07-B45" :request-schema-version 15 '
                ":scope :sh07-b15-keyword-map-lookup "
                f':source-binding {{:source-byte-count {source_size} '
                f':source-bytes-sha256 "{source_sha}"}} '
                ":request-counts {:fragment-count 0 :root-form-count 0 "
                ":form-count 0 :binding-count 0 :local-binding-count 0 "
                ":resolution-count 0} "
                ":core-counts {:core-node-count 0 :definition-count 0 "
                ":call-count 0 :reference-count 0 :keyword-lookup-count 0 "
                ":core-form-frequencies {}} "
                ":integrity {:root-form-id-order-exact? true "
                ":form-id-order-exact? true :source-snapshot-stable? true "
                ":source-revision-bound-to-bytes? true "
                ":target-source-reread-disabled? true} "
                ':census-hash "sha256:c77fe8082508cb13e566555686f893c03e9e854de0a19b5d53876d5d500bf947"} '
                ":contract-checks "
                "{:exact? true :authoritative-coverage-census-current? true}}]}\n",
                encoding="utf-8",
            )
            self.assertTrue(
                runner.output_contract_passed(
                    "alpha",
                    "bootstrap/gravity/src/gravity/alpha.gravity",
                    source_size,
                    source_sha,
                    contract_sha,
                    output,
                    clojure_command="clojure",
                    cwd=root,
                )
            )
            valid_output = output.read_text(encoding="utf-8")
            # The embedded hash was produced by gravity.bootstrap/
            # reader-canonical-hash for this census. Accepting the unmodified
            # receipt proves the standalone validator's canonicalization is
            # byte-for-byte compatible with the production reader hash.
            for label, old, new in [
                ("obsolete outer schema", ":schema-version 2", ":schema-version 1"),
                ("missing census", ":coverage-census ", ":ignored-coverage-census "),
                (
                    "bad census hash",
                    "sha256:c77fe8082508cb13e566555686f893c03e9e854de0a19b5d53876d5d500bf947",
                    "sha256:" + "f" * 64,
                ),
                (
                    "false census integrity",
                    ":root-form-id-order-exact? true",
                    ":root-form-id-order-exact? false",
                ),
                ("negative census count", ":form-count 0", ":form-count -1"),
            ]:
                with self.subTest(label=label):
                    output.write_text(valid_output.replace(old, new, 1), encoding="utf-8")
                    self.assertFalse(
                        runner.output_contract_passed(
                            "alpha",
                            "bootstrap/gravity/src/gravity/alpha.gravity",
                            source_size,
                            source_sha,
                            contract_sha,
                            output,
                            clojure_command="clojure",
                            cwd=root,
                        )
                    )
            output.write_text(valid_output, encoding="utf-8")
            output.write_text(
                output.read_text(encoding="utf-8").replace(
                    "gravity/alpha.gravity", "gravity/beta.gravity"
                ),
                encoding="utf-8",
            )
            self.assertFalse(
                runner.output_contract_passed(
                    "alpha",
                    "bootstrap/gravity/src/gravity/alpha.gravity",
                    source_size,
                    source_sha,
                    contract_sha,
                    output,
                    clojure_command="clojure",
                    cwd=root,
                )
            )
            output.write_text(
                output.read_text(encoding="utf-8")
                .replace("gravity/beta.gravity", "gravity/alpha.gravity")
                .replace(str(source_size), str(source_size + 1), 1)
                .replace(source_sha, "sha256:" + "0" * 64),
                encoding="utf-8",
            )
            self.assertFalse(
                runner.output_contract_passed(
                    "alpha",
                    "bootstrap/gravity/src/gravity/alpha.gravity",
                    source_size,
                    source_sha,
                    contract_sha,
                    output,
                    clojure_command="clojure",
                    cwd=root,
                )
            )
            output.write_text(
                output.read_text(encoding="utf-8")
                .replace(str(source_size + 1), str(source_size), 1)
                .replace("sha256:" + "0" * 64, source_sha)
                .replace(":status :passed", ":status :failed", 1),
                encoding="utf-8",
            )
            self.assertFalse(
                runner.output_contract_passed(
                    "alpha",
                    "bootstrap/gravity/src/gravity/alpha.gravity",
                    source_size,
                    source_sha,
                    contract_sha,
                    output,
                    clojure_command="clojure",
                    cwd=root,
                )
            )

    def test_module_names_cannot_escape_the_log_directory(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            with self.assertRaisesRegex(runner.CheckpointError, "safe slugs"):
                self.run_in_repository(root, FakeLauncher(), ["../escape"])

    def test_catalog_paths_fail_closed(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            cases = [
                {"alpha": "../escape.gravity"},
                {"alpha": "bootstrap/gravity/src/gravity/absent.gravity"},
                {
                    "alpha": "bootstrap/gravity/src/gravity/alpha.gravity",
                    "beta": "bootstrap/gravity/src/gravity/alpha.gravity",
                },
            ]
            for catalog in cases:
                with self.subTest(catalog=catalog), self.assertRaises(
                    runner.CheckpointError
                ):
                    runner.run_modules(
                        root=root,
                        state_dir=root / "checkpoints",
                        modules=["alpha"],
                        module_catalog=catalog,
                        base_command=["fake-runner"],
                        timeout_seconds=1,
                        launcher=FakeLauncher(),
                        output_validator=fixture_output_validator,
                        lock_path=root / "heavy.lock",
                    )

    def test_catalog_handshake_parses_exact_tab_delimited_rows(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            output = (
                "alpha\tbootstrap/gravity/src/gravity/alpha.gravity\n"
                "beta\tbootstrap/gravity/src/gravity/beta.gravity\n"
            )
            completed = subprocess.CompletedProcess([], 0, output, "")
            with mock.patch.object(runner.subprocess, "run", return_value=completed):
                self.assertEqual(
                    {
                        "alpha": "bootstrap/gravity/src/gravity/alpha.gravity",
                        "beta": "bootstrap/gravity/src/gravity/beta.gravity",
                    },
                    runner.discover_module_catalog(root, ["fake-runner"], 1),
                )
            malformed = subprocess.CompletedProcess([], 0, "alpha only\n", "")
            with mock.patch.object(runner.subprocess, "run", return_value=malformed):
                with self.assertRaisesRegex(runner.CheckpointError, "malformed"):
                    runner.discover_module_catalog(root, ["fake-runner"], 1)

    def test_symlinked_catalog_source_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            source = root / "bootstrap/gravity/src/gravity/alpha.gravity"
            source.unlink()
            source.symlink_to(root / "bootstrap/gravity/src/gravity/beta.gravity")
            with self.assertRaisesRegex(runner.CheckpointError, "non-symlink"):
                self.run_in_repository(root, FakeLauncher(), ["alpha"])

    def test_v1_manifest_never_resumes(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            state = root / "checkpoints"
            state.mkdir()
            (state / "manifest.json").write_text(
                json.dumps(
                    {
                        "schema": "gravity/sh07-authoritative-module-checkpoints-v1",
                        "modules": {"alpha": {"state": "passed"}},
                    }
                ),
                encoding="utf-8",
            )
            launcher = FakeLauncher()
            with self.assertRaisesRegex(runner.CheckpointError, "unsupported schema"):
                self.run_in_repository(root, launcher, ["alpha"])
            self.assertEqual([], launcher.calls)

    def test_same_path_classpath_jar_replacement_invalidates_shared_context(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            root = parent / "repository"
            root.mkdir()
            self.make_repository(root)
            dependency = parent / "dependency.jar"
            dependency.write_bytes(b"first jar")

            def capture(command: list[str], **_kwargs: object) -> dict[str, object]:
                if command[-1] == "-Sdescribe":
                    stdout = f'{{:config-files ["{root / "deps.edn"}"]}}'
                elif command[-1] == "-Spath":
                    stdout = os.pathsep.join(
                        [str(root / "bootstrap/clojure/src"), str(dependency)]
                    )
                else:
                    stdout = "runtime version"
                return {
                    "command": command,
                    "exit_code": 0,
                    "stdout": stdout,
                    "stderr": "",
                    "complete": True,
                }

            with mock.patch.object(runner, "command_capture", side_effect=capture), mock.patch.object(
                runner.shutil, "which", return_value=sys.executable
            ):
                first = runner.shared_context_fingerprint(
                    root,
                    ["fake-runner"],
                    module_catalog=self.module_catalog(),
                    require_runtime_identity=True,
                )
                dependency.write_bytes(b"replacement jar")
                second = runner.shared_context_fingerprint(
                    root,
                    ["fake-runner"],
                    module_catalog=self.module_catalog(),
                    require_runtime_identity=True,
                )
            self.assertNotEqual(first["sha256"], second["sha256"])
            entries = second["runtime"]["clojure_classpath_entries"]
            jar_entry = next(entry for entry in entries if entry["kind"] == "file")
            self.assertEqual(str(dependency.resolve()), jar_entry["path"])
            self.assertEqual(runner.sha256_file(dependency), jar_entry["sha256"])

    def test_root_data_readers_content_invalidates_shared_context(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            resource = root / "bootstrap/clojure/test/data_readers.clj"
            resource.write_text("{}\n", encoding="utf-8")

            def capture(command: list[str], **_kwargs: object) -> dict[str, object]:
                if command[-1] == "-Sdescribe":
                    stdout = f'{{:config-files ["{root / "deps.edn"}"]}}'
                elif command[-1] == "-Spath":
                    stdout = str(root / "bootstrap/clojure/test")
                else:
                    stdout = "runtime version"
                return {
                    "command": command,
                    "exit_code": 0,
                    "stdout": stdout,
                    "stderr": "",
                    "complete": True,
                }

            with mock.patch.object(runner, "command_capture", side_effect=capture), mock.patch.object(
                runner.shutil, "which", return_value=sys.executable
            ):
                first = runner.shared_context_fingerprint(
                    root,
                    ["fake-runner"],
                    module_catalog=self.module_catalog(),
                    require_runtime_identity=True,
                )
                resource.write_text("{foo/bar foo/read}\n", encoding="utf-8")
                second = runner.shared_context_fingerprint(
                    root,
                    ["fake-runner"],
                    module_catalog=self.module_catalog(),
                    require_runtime_identity=True,
                )
            self.assertNotEqual(first["sha256"], second["sha256"])
            directory_entry = second["runtime"]["clojure_classpath_entries"][0]
            resource_entry = next(
                entry for entry in directory_entry["files"]
                if entry["path"] == "data_readers.clj"
            )
            self.assertEqual(runner.sha256_file(resource), resource_entry["sha256"])

    def test_unrelated_test_source_does_not_invalidate_shared_context(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            unrelated = root / "bootstrap/clojure/test/unrelated_test.clj"
            unrelated.write_text("(ns unrelated-test)\n", encoding="utf-8")

            def capture(command: list[str], **_kwargs: object) -> dict[str, object]:
                if command[-1] == "-Sdescribe":
                    stdout = f'{{:config-files ["{root / "deps.edn"}"]}}'
                elif command[-1] == "-Spath":
                    stdout = str(root / "bootstrap/clojure/test")
                else:
                    stdout = "runtime version"
                return {
                    "command": command,
                    "exit_code": 0,
                    "stdout": stdout,
                    "stderr": "",
                    "complete": True,
                }

            with mock.patch.object(runner, "command_capture", side_effect=capture), mock.patch.object(
                runner.shutil, "which", return_value=sys.executable
            ):
                first = runner.shared_context_fingerprint(
                    root,
                    ["fake-runner"],
                    module_catalog=self.module_catalog(),
                    require_runtime_identity=True,
                )
                unrelated.write_text("(ns unrelated-test)\n;; edit\n", encoding="utf-8")
                second = runner.shared_context_fingerprint(
                    root,
                    ["fake-runner"],
                    module_catalog=self.module_catalog(),
                    require_runtime_identity=True,
                )
            self.assertEqual(first["sha256"], second["sha256"])

    def test_root_classpath_directory_rejects_symlinks_and_special_files(self) -> None:
        for kind in ["class", "symlink", "fifo"]:
            with self.subTest(kind=kind), tempfile.TemporaryDirectory() as directory:
                root = Path(directory)
                self.make_repository(root)
                entry = root / "bootstrap/clojure/test/shadow.class"
                if kind == "class":
                    entry.write_bytes(b"AOT shadow")
                elif kind == "symlink":
                    target = root / "target.class"
                    target.write_bytes(b"target")
                    entry.symlink_to(target)
                else:
                    os.mkfifo(entry)

                def capture(
                    command: list[str], **_kwargs: object
                ) -> dict[str, object]:
                    if command[-1] == "-Sdescribe":
                        stdout = f'{{:config-files ["{root / "deps.edn"}"]}}'
                    elif command[-1] == "-Spath":
                        stdout = str(root / "bootstrap/clojure/test")
                    else:
                        stdout = "runtime version"
                    return {
                        "command": command,
                        "exit_code": 0,
                        "stdout": stdout,
                        "stderr": "",
                        "complete": True,
                    }

                with mock.patch.object(
                    runner, "command_capture", side_effect=capture
                ), mock.patch.object(
                    runner.shutil, "which", return_value=sys.executable
                ):
                    with self.assertRaisesRegex(
                        runner.CheckpointError, "runtime identity is incomplete"
                    ):
                        runner.runtime_identity(root, ["fake-runner"], True)
                    identity = runner.runtime_identity(
                        root, ["fake-runner"], False
                    )
                self.assertFalse(identity["complete"])
                self.assertTrue(identity["clojure_classpath_errors"])

    def test_external_or_missing_classpath_directory_fails_closed(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            root = parent / "repository"
            root.mkdir()
            self.make_repository(root)
            external = parent / "external-classes"
            external.mkdir()
            missing = parent / "missing-classes"

            for bad_entry, message in [
                (external, "external classpath directory"),
                (missing, "classpath entry is absent"),
            ]:
                with self.subTest(bad_entry=bad_entry):
                    def capture(
                        command: list[str], **_kwargs: object
                    ) -> dict[str, object]:
                        if command[-1] == "-Sdescribe":
                            stdout = f'{{:config-files ["{root / "deps.edn"}"]}}'
                        elif command[-1] == "-Spath":
                            stdout = str(bad_entry)
                        else:
                            stdout = "runtime version"
                        return {
                            "command": command,
                            "exit_code": 0,
                            "stdout": stdout,
                            "stderr": "",
                            "complete": True,
                        }

                    with mock.patch.object(
                        runner, "command_capture", side_effect=capture
                    ), mock.patch.object(
                        runner.shutil, "which", return_value=sys.executable
                    ):
                        with self.assertRaisesRegex(
                            runner.CheckpointError, "runtime identity is incomplete"
                        ):
                            runner.runtime_identity(root, ["fake-runner"], True)
                        identity = runner.runtime_identity(
                            root, ["fake-runner"], False
                        )
                    self.assertFalse(identity["complete"])
                    self.assertRegex(identity["clojure_classpath_errors"][0], message)

    def test_held_shared_lock_fails_before_launching_a_module(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            lock_path = root / "heavy.lock"
            launcher = FakeLauncher()
            with lock_path.open("a+", encoding="utf-8") as held:
                fcntl.flock(held.fileno(), fcntl.LOCK_EX | fcntl.LOCK_NB)
                with self.assertRaisesRegex(runner.CheckpointError, "unavailable"):
                    self.run_in_repository(root, launcher, ["alpha"])
            self.assertEqual([], launcher.calls)

    def test_symlinked_lock_is_rejected_without_touching_target(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            victim = root / "victim.txt"
            victim.write_text("KEEP-ME\n", encoding="utf-8")
            (root / "heavy.lock").symlink_to(victim)
            with self.assertRaisesRegex(runner.CheckpointError, "safely"):
                self.run_in_repository(root, FakeLauncher(), ["alpha"])
            self.assertEqual("KEEP-ME\n", victim.read_text(encoding="utf-8"))

    def test_symlinked_module_output_is_rejected_without_touching_target(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            victim = root / "victim.txt"
            victim.write_text("KEEP-ME\n", encoding="utf-8")
            stdout = root / "alpha.stdout.log"
            stdout.symlink_to(victim)
            with self.assertRaisesRegex(runner.CheckpointError, "safely"):
                runner.default_launcher(
                    [sys.executable, "-c", "print('proof')"],
                    root,
                    stdout,
                    root / "alpha.stderr.log",
                    1,
                )
            self.assertEqual("KEEP-ME\n", victim.read_text(encoding="utf-8"))

    def test_hardlinked_module_output_is_rejected_before_truncation(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            victim = root / "victim.txt"
            victim.write_text("KEEP-ME\n", encoding="utf-8")
            stdout = root / "alpha.stdout.log"
            os.link(victim, stdout)
            with self.assertRaisesRegex(runner.CheckpointError, "one owned regular file"):
                runner.default_launcher(
                    [sys.executable, "-c", "print('proof')"],
                    root,
                    stdout,
                    root / "alpha.stderr.log",
                    1,
                )
            self.assertEqual("KEEP-ME\n", victim.read_text(encoding="utf-8"))

    def test_symlinked_modules_directory_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.make_repository(root)
            state = root / "checkpoints"
            state.mkdir()
            outside = root / "outside"
            outside.mkdir()
            (state / "modules").symlink_to(outside, target_is_directory=True)
            with self.assertRaisesRegex(runner.CheckpointError, "cannot be a symlink"):
                self.run_in_repository(root, FakeLauncher(), ["alpha"])

    def test_real_timeout_kills_descendants_before_returning(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            child_pid_path = root / "child.pid"
            child_code = (
                "import os,signal,time,pathlib;"
                "signal.signal(signal.SIGTERM,signal.SIG_IGN);"
                f"pathlib.Path({str(child_pid_path)!r}).write_text(str(os.getpid()));"
                "time.sleep(30)"
            )
            leader_code = (
                "import subprocess,sys,time;"
                f"subprocess.Popen([sys.executable,'-c',{child_code!r}]);"
                "time.sleep(30)"
            )
            outcome = runner.default_launcher(
                [sys.executable, "-c", leader_code],
                root,
                root / "stdout.log",
                root / "stderr.log",
                0.5,
            )
            self.assertTrue(outcome.timed_out)
            self.assertEqual(124, outcome.exit_code)
            child_pid = int(child_pid_path.read_text(encoding="utf-8"))
            alive = True
            for _ in range(40):
                try:
                    os.kill(child_pid, 0)
                except ProcessLookupError:
                    alive = False
                    break
                time.sleep(0.05)
            if alive:
                os.kill(child_pid, 9)
            self.assertFalse(alive, "timed-out descendant survived launcher return")


if __name__ == "__main__":
    unittest.main()
