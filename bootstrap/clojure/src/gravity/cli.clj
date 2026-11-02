(ns gravity.cli
  "Deterministic bootstrap CLI presentation values.

  This namespace does not observe host state, parse arguments, select or run
  commands, authenticate or render diagnostics, choose exit codes, or
  terminate the process. Those responsibilities remain at their existing
  bootstrap boundaries until their owning stages are extracted.")

(def ^:private namespace-contract
  {:namespace 'gravity.cli
   :contract-boundary :bootstrap-cli-presentation-values
   :public-api
   {'p18-cli-version-record
    {:arglists '([packaged-jvm-cli?])
     :returns :structured-version-record}
    'p18-cli-help-text
    {:arglists '([packaged-jvm-cli?])
     :returns :human-help-text}}
   :artifact-inputs [:packaged-jvm-cli-state]
   :artifact-outputs [:bootstrap-version-record :bootstrap-help-text]
   :ownership
   {:owns [:deterministic-bootstrap-version-presentation
           :deterministic-bootstrap-help-presentation]
    :does-not-own
    [:host-state-observation :argument-normalization :command-dispatch
     :compiler-semantics :diagnostic-authenticity :diagnostic-rendering
     :exit-code-selection :system-exit :java-entrypoint :clojure-entrypoint
     :release-claims]}
   :dependency-direction
   {:requires ['clojure.core]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :bootstrap-hosted? true
   :production-t1-cli-conformance? false
   :full-t1-command-surface? false
   :seedless-release? false
   :self-hosted? false})

(def ^:private experimental-c-route-version-record
  {:status :implementation-present-public-exposure-disabled
   :experiment-state :proposed
   :exposure :internal-only
   :public-command-route? false
   :excluded-from-executable-command-contract-credit? true
   :governance-conforming? false
   :t1-cli-conformance? false
   :p18-t04-proof-credited? false
   :public-target-support-claim? false})

(def ^:private experimental-c-route-help
  (str
   "  :experimental-verified-mir-c-route "
   "\"implementation present; public exposure disabled pending "
   "feature-specific GOV4, GOV9, and GOV7 records; exact requests reject "
   "before source-file I/O, output-filesystem I/O, native calls, tool "
   "execution, or publication\"\n"))

(defn p18-cli-version-record
  [packaged-jvm-cli?]
  {:command "gravity"
   :phase (if packaged-jvm-cli? "P18-T02" "P18-T01")
   :bootstrap-hosted? true
   :packaged-jvm-cli? packaged-jvm-cli?
   :seedless-release? false
   :executable-command-contract? true
   :executable-command-contract-scope :established-bootstrap-subset
   :experimental-verified-mir-c-route experimental-c-route-version-record
   :delegates-to
   (if packaged-jvm-cli?
     "java -cp target/phase-18/jvm-cli/gravity-jvm-cli.jar gravity.cli.Main"
     "clojure -M:gravity")
   :replaced-by "Phase 18 self-hosted release artifact"})

(defn p18-cli-help-text
  [packaged-jvm-cli?]
  (str
   "gravity bootstrap-hosted command\n\n"
   "Usage:\n"
   "  gravity --version\n"
   "  gravity help\n"
   "  gravity check <file.qst|file.gravity>\n"
   "  gravity sh07-core <file.qst|file.gravity>\n"
   "  gravity run <file.qst|file.gravity>\n"
   "  gravity compile <file.qst|file.gravity>\n"
   "  gravity compile <file.qst|file.gravity> -o <executable>\n"
   "  gravity test\n"
   "  gravity p18-t05-seedless-release-boundary\n"
   "  gravity p18-t05-write-seedless-release-artifacts\n"
   "  gravity p18-t06-final-release\n"
   "  gravity p18-t06-write-final-release-artifacts\n"
   "  gravity p18-t00-co-canonical-source-extensions\n"
   "  gravity p18-t00-write-co-canonical-source-extension-artifacts\n"
   "  gravity <artifact-command> <file.qst|file.gravity>\n\n"
   "Metadata:\n"
   "  :bootstrap-hosted? true\n"
   "  :packaged-jvm-cli? "
   (if packaged-jvm-cli? "true" "false")
   "\n"
   "  :seedless-release? false\n"
   experimental-c-route-help))
