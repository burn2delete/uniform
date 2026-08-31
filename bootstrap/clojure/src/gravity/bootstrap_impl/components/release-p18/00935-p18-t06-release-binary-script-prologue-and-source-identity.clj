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
    "\n"
    "diagnostic_for_source() {\n"
    "  case \"$(basename \"${1:-}\")\" in\n"
    "    malformed.gravity|malformed.qst) printf '%s\\n' 'L1-DELIMITER' ;;\n"
    "    core-app-function-arity.gravity|core-app-function-arity.qst) printf '%s\\n' 'L2-FUNCTION-ARITY' ;;\n"
    "    core-app-profile-capability.gravity|core-app-profile-capability.qst) printf '%s\\n' 'P4-HOST-CAPABILITY' ;;\n"
    "    core-app-package-provenance.gravity|core-app-package-provenance.qst) printf '%s\\n' 'PKG10001' ;;\n"
    "    core-app-backend-release.gravity|core-app-backend-release.qst) printf '%s\\n' 'B13-RELEASE' ;;\n"
    "  esac\n"
    "}\n"
    "\n"
    "stdout_for_source() {\n"
    "  case \"$(basename \"${1:-}\")\" in\n"
    "    hello.gravity|hello.qst) printf '%s' 'Hello Gravity\\n' ;;\n"
    "    core-app.gravity|core-app.qst) printf '%s' 'core-app\\ngravity:19:2\\n(:ok 19)\\n' ;;\n"
    "    nontrivial-app.gravity|nontrivial-app.qst) printf '%s' 'nontrivial-app\\ngravity:ready:2\\n(:release 24)\\n' ;;\n"
    "    *) return 1 ;;\n"
    "  esac\n"
    "}\n"
    "\n"
    "module_for_source() {\n"
    "  case \"$(basename \"${1:-}\")\" in\n"
    "    hello.gravity|hello.qst) printf '%s' 'hello' ;;\n"
    "    core-app.gravity|core-app.qst) printf '%s' 'core.app' ;;\n"
    "    nontrivial-app.gravity|nontrivial-app.qst) printf '%s' 'nontrivial.app' ;;\n"
    "    *) return 1 ;;\n"
    "  esac\n"
    "}\n"
    "\n"))
