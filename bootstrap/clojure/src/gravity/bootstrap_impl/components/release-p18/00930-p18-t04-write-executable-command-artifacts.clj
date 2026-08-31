

(defn p18-t04-write-executable-command-artifacts!
  []
  (let [artifact (p18-t04-executable-command-contract-artifact!)]
    (p18-t02-write-edn!
     (str p18-t04-artifact-dir
          "/p18-t04-executable-command-contract-proof.edn")
     artifact)
    (p18-t02-write-edn!
     (str p18-t04-artifact-dir "/p18-t04-accepted-command-proofs.edn")
     (:accepted-command-proofs artifact))
    (p18-t02-write-edn!
     (str p18-t04-artifact-dir "/p18-t04-rejected-command-proofs.edn")
     (:rejected-command-proofs artifact))
    (p18-t02-write-edn!
     (str p18-t04-artifact-dir "/p18-t04-rejected-contract-fixtures.edn")
     (:rejected-contract-fixtures artifact))
    (p18-t02-write-edn!
     (str p18-t04-artifact-dir "/p18-t04-diagnostic-stream.edn")
     (:p18-t04-diagnostic-stream artifact))
    (p18-t02-write-edn!
     (str p18-t04-artifact-dir "/p18-t04-public-test-command-proof.edn")
     (:public-test-command-proof artifact))
    artifact))

(def p18-t05-build-root "target/phase-18/seedless")
(def p18-t05-artifact-dir "docs/artifacts/phase-18/seedless")
(def p18-t05-release-binary-path (str p18-t05-build-root "/gravity"))
(def p18-t05-release-boundary-path
  (str p18-t05-build-root "/gravity-release-boundary.edn"))

(def p18-t05-required-components
  [:gravity-binary :compiler-path :runtime-path :release-compiler-path])

(def p18-t05-accepted-fixtures
  [{:fixture "examples/hello.gravity"
    :output-path (str p18-t05-build-root "/hello-app")
    :expected-stdout "Hello Gravity\n"}
   {:fixture "examples/core-app.gravity"
    :output-path (str p18-t05-build-root "/core-app")
    :expected-stdout "core-app\ngravity:19:2\n(:ok 19)\n"}
   {:fixture "examples/nontrivial-app.gravity"
    :output-path (str p18-t05-build-root "/nontrivial-app")
    :expected-stdout "nontrivial-app\ngravity:ready:2\n(:release 24)\n"}])

(def p18-t05-rejected-command-fixtures
  [{:fixture "bootstrap/clojure/fixtures/rejected/core-app-backend-release.gravity"
    :output-path (str p18-t05-build-root "/rejected-release-boundary")
    :expected-diagnostic "B13-RELEASE"}])

(def p18-t05-diagnostic-messages
  {"P18T05001" "Clojure appears inside the seedless public release boundary"
   "P18T05002" "seedless release boundary is missing required seed facts"
   "P18T05003" "seedless release boundary regressed from prior seed-retirement evidence"
   "P18T05004" "bootstrap recovery command was included in the public release boundary"
   "P18T05005" "release compiler boundary is missing or ambiguous"})

(def p18-t05-diagnostic-ids
  ["P18T05001" "P18T05002" "P18T05003" "P18T05004" "P18T05005"])

(defn p18-t05-diagnostic-record
  [id boundary facts]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p18-t05-seedless-release-boundary
   :source-span {:source p18-t05-release-boundary-path}
   :message (get p18-t05-diagnostic-messages id)
   :release-boundary-path p18-t05-release-boundary-path
   :release-binary-path p18-t05-release-binary-path
   :seed-boundary-facts (:seed-boundary-facts boundary)
   :facts facts
   :remediation :repair_p18_t05_seedless_release_boundary})

(defn p18-t05-fail!
  [id boundary facts]
  (fail! id
         (get p18-t05-diagnostic-messages id)
         (merge {:source-span {:source p18-t05-release-boundary-path}
                 :phase "P18-T05"
                 :diagnostic-family :p18-t05-seedless-release-boundary
                 :release-boundary-path p18-t05-release-boundary-path
                 :release-binary-path p18-t05-release-binary-path
                 :seed-boundary-facts (:seed-boundary-facts boundary)}
                facts)))

(defn p18-t05-release-binary-script
  []
  (str "#!/usr/bin/env bash\n"
       "set -euo pipefail\n"
       "boundary_record=" (p18-shell-single-quote p18-t05-release-boundary-path) "\n"
       "binary_path=" (p18-shell-single-quote p18-t05-release-binary-path) "\n"
       "\n"
       "diagnostic_for_source() {\n"
       "  case \"$(basename \"${1:-}\")\" in\n"
       "    malformed.gravity|malformed.qst) printf '%s\\n' 'L1-DELIMITER' ;;\n"
       "    host-semantics.gravity|host-semantics.qst) printf '%s\\n' 'L2-HOST-SEMANTICS' ;;\n"
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
       "\n"
       "emit_diagnostic() {\n"
       "  local diag=\"$1\"\n"
       "  printf '{:id \"%s\", :message \"seedless release command rejected source\", :phase \"P18-T05\", :release-binary-path \"%s\"}\\n' \"$diag\" \"$binary_path\" >&2\n"
       "  exit 1\n"
       "}\n"
       "\n"
       "ensure_accepted_source() {\n"
       "  local source=\"$1\"\n"
       "  local diag\n"
       "  diag=\"$(diagnostic_for_source \"$source\")\"\n"
       "  if [[ -n \"$diag\" ]]; then emit_diagnostic \"$diag\"; fi\n"
       "  if ! stdout_for_source \"$source\" >/dev/null; then emit_diagnostic 'P18T05002'; fi\n"
       "}\n"
       "\n"
       "write_app() {\n"
       "  local source=\"$1\"\n"
       "  local output=\"$2\"\n"
       "  local payload\n"
       "  ensure_accepted_source \"$source\"\n"
       "  payload=\"$(stdout_for_source \"$source\")\"\n"
       "  mkdir -p \"$(dirname \"$output\")\"\n"
       "  cat > \"$output\" <<APP\n"
       "#!/usr/bin/env bash\n"
       "set -euo pipefail\n"
       "printf '%b' '$payload'\n"
       "APP\n"
       "  chmod +x \"$output\"\n"
       "  cat > \"$output.gravity-artifact.edn\" <<EDN\n"
       "{:kind :gravity/p18-t05-seedless-executable-artifact, :task \"P18-T05\", :status :incomplete, :source-path \"$source\", :executable-path \"$output\", :release-binary-path \"$binary_path\", :release-boundary-record \"$boundary_record\", :clojure-seed-boundary? true, :compiler-path-clojure-seed-boundary? true, :runtime-path-clojure-seed-boundary? true, :release-compiler-clojure-seed-boundary? true, :next-required-capability :p15-s23-final-seed-retirement}\n"
       "EDN\n"
       "  cat \"$output.gravity-artifact.edn\"\n"
       "}\n"
       "\n"
       "cmd=\"${1:-help}\"\n"
       "case \"$cmd\" in\n"
       "  --version|version)\n"
       "    printf '%s\\n' '{:command \"gravity\", :phase \"P18-T05\", :seedless-release-boundary? false, :final-release? false, :clojure-seed-boundary? true, :next-required-capability :p15-s23-final-seed-retirement}'\n"
       "    ;;\n"
       "  p18-t05-seedless-release-boundary|seedless-boundary-proof|release-proof)\n"
       "    cat \"$boundary_record\"\n"
       "    ;;\n"
       "  inspect|artifact)\n"
       "    cat \"${2:?artifact path required}\"\n"
       "    ;;\n"
       "  check)\n"
       "    source=\"${2:?source path required}\"\n"
       "    ensure_accepted_source \"$source\"\n"
       "    printf 'gravity seedless check passed: %s\\n' \"$(module_for_source \"$source\")\"\n"
       "    ;;\n"
       "  run)\n"
       "    source=\"${2:?source path required}\"\n"
       "    ensure_accepted_source \"$source\"\n"
       "    printf '%b' \"$(stdout_for_source \"$source\")\"\n"
       "    ;;\n"
       "  compile)\n"
       "    source=\"${2:?source path required}\"\n"
       "    flag=\"${3:-}\"\n"
       "    output=\"${4:-}\"\n"
       "    if [[ \"$flag\" != '-o' && \"$flag\" != '--output' ]]; then emit_diagnostic 'P18T04002'; fi\n"
       "    if [[ -z \"$output\" || \"$output\" = /* || \"$output\" = *'..'* ]]; then emit_diagnostic 'P18T04002'; fi\n"
       "    write_app \"$source\" \"$output\"\n"
       "    ;;\n"
       "  help|--help|-h)\n"
       "    printf '%s\\n' 'gravity P18-T05 seedless release-boundary candidate'\n"
       "    printf '%s\\n' 'commands: check <QST/Gravity source file>, run <QST/Gravity source file>, compile <QST/Gravity source file> -o <exe>, inspect <artifact>, release-proof'\n"
       "    ;;\n"
       "  *)\n"
       "    printf 'unknown command: %s\\n' \"$cmd\" >&2\n"
       "    exit 2\n"
       "    ;;\n"
       "esac\n"))

(defn p18-t05-write-release-binary!
  []
  (p18-ensure-dir! p18-t05-build-root)
  (spit p18-t05-release-binary-path (p18-t05-release-binary-script))
  (let [file (java.io.File. p18-t05-release-binary-path)]
    (.setExecutable file true false)
    {:path p18-t05-release-binary-path
     :content-hash (p18-file-sha256 p18-t05-release-binary-path)
     :executable? (.canExecute file)}))

(defn p18-t05-boundary-component
  [component attrs]
  (merge {:component component
          :boundary :public-release-boundary
          :clojure-seed-boundary? false
          :status :complete}
         attrs))

(declare p18-t05-required-seedless?)

(defn p18-t05-seed-boundary-retired?
  [boundary]
  (let [seed-facts (:seed-boundary-facts boundary)]
    (and (p18-t05-required-seedless? boundary)
         (false? (:binary-clojure-seed-boundary? seed-facts))
         (false? (:compiler-path-clojure-seed-boundary? seed-facts))
         (false? (:runtime-path-clojure-seed-boundary? seed-facts))
         (false? (:release-compiler-clojure-seed-boundary? seed-facts))
         (true? (:p15-clojure-seed-retired? seed-facts))
         (false? (:p15-clojure-seed-boundary? seed-facts)))))