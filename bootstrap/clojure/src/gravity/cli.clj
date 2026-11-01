(ns gravity.cli
  "Deterministic bootstrap CLI presentation and selected Core arguments.

  Core argument normalization returns a path/options value after command
  selection. This namespace does not observe host state, select or run
  commands, authenticate or render diagnostics, choose exit codes, or
  terminate the process. Those responsibilities remain at their existing
  bootstrap boundaries until their owning stages are extracted.")

(def ^:private namespace-contract
  {:namespace 'gravity.cli
   :contract-boundary :bootstrap-cli-presentation-and-core-arguments
   :governing-contracts '[D1 D2 D3 D8 D9 L1 L2 L3 C1 C6 C15 T1 BOOT1 BOOT8 TEST2]
   :public-api
   {'p18-cli-version-record
    {:arglists '([packaged-jvm-cli?])
     :returns :structured-version-record}
    'p18-cli-help-text
    {:arglists '([packaged-jvm-cli?])
     :returns :human-help-text}
    'sh07-core-input
    {:arglists '([arguments])
     :command-selection :already-selected
     :returns :path-and-reader-options
     :diagnostics ["SH07-CLI-ARGS"]
     :admission :none}}
   :artifact-inputs [:packaged-jvm-cli-state :selected-sh07-command-arguments]
   :artifact-outputs [:bootstrap-version-record :bootstrap-help-text :sh07-core-input]
   :diagnostic-ownership {:stage ["SH07-CLI-ARGS"]
                          :source-decoding :reader :core-legality :checked-core
                          :rendering :existing-diagnostic-presentation}
   :ownership
   {:owns [:deterministic-bootstrap-version-presentation
           :deterministic-bootstrap-help-presentation
           :sh07-core-argument-normalization]
    :does-not-own
    [:host-state-observation :other-command-argument-normalization :command-dispatch
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
   "  gravity build --reader-inputs EDN --principal PACKAGE --profile meta --target jvm\n"
   "    --allow-read KEY --package-policy EDN --workspace-policy EDN --format json\n"
   "    (local hermetic input entry; effectful Reader bridge unavailable)\n"
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

(defn sh07-core-input
  "Normalize the already selected Core command's arguments without source IO."
  [arguments]
  (let [[_ path & flags] arguments]
    (when-not (and (string? path) (not (.startsWith ^String path "-"))
                   (or (empty? flags) (= ["--development-current-reader"] (vec flags))))
      (throw (ex-info "usage: gravity sh07-core FILE [--development-current-reader]"
                      {:id "SH07-CLI-ARGS" :stage :command-line :severity :error})))
    [path (if (empty? flags) {} {:reader :development-current})]))

(alter-meta! #'sh07-core-input assoc :gravity/namespace-contract namespace-contract)
