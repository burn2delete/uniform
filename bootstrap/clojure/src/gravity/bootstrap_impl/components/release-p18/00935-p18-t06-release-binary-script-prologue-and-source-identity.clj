(defn- __gravity_bootstrap_p18_t06_prologue_and_source_identity []
  (str
    "#!/usr/bin/env bash\n"
    "set -euo pipefail\n"
    "script_dir=\"$(cd \"$(dirname \"${BASH_SOURCE[0]}\")\" && pwd)\"\n"
    "root_dir=\"$(cd \"$script_dir/../../..\" && pwd)\"\n"
    "release_binary_path="
    (p18-shell-single-quote p18-t06-release-binary-path)
    "\n"
    "final_proof_record="
    (p18-shell-single-quote p18-t06-final-proof-path)
    "\n"
    "release_boundary_record="
    (p18-shell-single-quote p18-t06-release-boundary-path)
    "\n"
    "provenance_record="
    (p18-shell-single-quote (str p18-t06-artifact-dir "/p18-t06-provenance.edn"))
    "\n"
    "sbom_record="
    (p18-shell-single-quote (str p18-t06-artifact-dir "/p18-t06-sbom.edn"))
    "\n"
    "signing_record="
    (p18-shell-single-quote (str p18-t06-artifact-dir "/p18-t06-signing-record.edn"))
    "\n"
    "governance_record="
    (p18-shell-single-quote
      (str p18-t06-artifact-dir "/p18-t06-governance-approval.edn"))
    "\n"
    "\n"))
