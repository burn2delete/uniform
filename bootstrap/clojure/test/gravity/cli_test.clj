(ns gravity.cli-test
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.cli :as cli]
            [gravity.cli.diagnostic-presentation :as diagnostic-presentation]
            [gravity.cli.dispatch :as dispatch]
            [gravity.cli.entrypoint :as entrypoint]))

(defn- expected-version-record
  [packaged?]
  {:command "gravity"
   :phase (if packaged? "P18-T02" "P18-T01")
   :bootstrap-hosted? true
   :packaged-jvm-cli? packaged?
   :seedless-release? false
   :executable-command-contract? true
   :executable-command-contract-scope :established-bootstrap-subset
   :experimental-verified-mir-c-route
   {:status :implementation-present-public-exposure-disabled
    :experiment-state :proposed
    :exposure :internal-only
    :public-command-route? false
    :excluded-from-executable-command-contract-credit? true
    :governance-conforming? false
    :t1-cli-conformance? false
    :p18-t04-proof-credited? false
    :public-target-support-claim? false}
   :delegates-to
   (if packaged?
     "java -Dgravity.packaged.jvm.cli=true -cp target/development-loop-batches/phase-18/jvm-cli/gravity-jvm-cli.jar:<clojure-runtime-classpath> clojure.main -m gravity.cli.launcher"
     "clojure -M:gravity")
   :replaced-by "Phase 18 self-hosted release artifact"})

(defn- expected-help-text
  [packaged?]
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
   "  gravity compile <file.qst|file.gravity> --target js|js-ts -o <fresh-directory>\n"
   "    (checked hosted/safe scalar module; Node 20 / ES2022 ESM)\n"
   "    (canonical source target :js-ts; compiler host Clojure/JVM)\n"
   "    (Node syntax validation during compilation; import exports to execute)\n"
   "  gravity compile <file.qst|file.gravity> --target js|js-ts\n"
   "    --lowering runtime-derived -o <directory>\n"
   "    (explicit legacy route; retains its existing results and failures)\n"
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
   "  :packaged-jvm-cli? " (if packaged? "true" "false") "\n"
   "  :seedless-release? false\n"
   "  :experimental-verified-mir-c-route \"implementation present; public exposure disabled pending feature-specific GOV4, GOV9, and GOV7 records; exact requests reject before source-file I/O, output-filesystem I/O, native calls, tool execution, or publication\"\n"))

(deftest presentation-values-are-extracted-with-bootstrap-parity
  (doseq [packaged? [false true]]
    (testing (str "deterministic presentation for packaged?=" packaged?)
      (let [version (cli/p18-cli-version-record packaged?)
            help (cli/p18-cli-help-text packaged?)]
        (is (= (expected-version-record packaged?) version))
        (is (= (expected-help-text packaged?) help))
        (is (= version (cli/p18-cli-version-record packaged?)))
        (is (= help (cli/p18-cli-help-text packaged?)))
        (is (string? help))
        (is (.endsWith help "\n"))
        (is (= packaged? (:packaged-jvm-cli? version)))
        (is (= (if packaged? "P18-T02" "P18-T01")
               (:phase version)))
        (is (true? (:bootstrap-hosted? version)))
        (is (false? (:seedless-release? version)))
        (is (false?
             (get-in version
                     [:experimental-verified-mir-c-route
                      :public-target-support-claim?])))
        (is (false?
             (get-in version
                     [:experimental-verified-mir-c-route
                      :public-command-route?])))
        (is (.contains help ":bootstrap-hosted? true"))
        (is (.contains help
                       (str ":packaged-jvm-cli? "
                            (if packaged? "true" "false"))))
        (is (not (.contains
                  help
                  "--target c --lowering verified-mir")))
        (is (.contains help "public exposure disabled"))
        (is (.contains help "exact requests reject before source-file I/O"))
        (with-redefs [bootstrap/p18-packaged-jvm-cli?
                      (constantly packaged?)]
          (is (= version (bootstrap/p18-cli-version-record)))
          (is (= help (bootstrap/p18-cli-help-text)))))))
  (testing "the compatibility wrappers retain their zero-arity surface"
    (is (= '([]) (:arglists (meta #'bootstrap/p18-cli-version-record))))
    (is (= '([]) (:arglists (meta #'bootstrap/p18-cli-help-text))))))

(deftest usage-text-remains-the-entrypoint-compatibility-value
  (let [usage (cli/cli-usage-text)]
    (is (= usage entrypoint/usage-text))
    (is (string? usage))
    (is (.startsWith usage "usage: clojure -M:gravity "))))

(deftest cli-namespace-contract-is-narrow-and-acyclic
  (let [contract-var
        (get (ns-interns 'gravity.cli) 'namespace-contract)
        contract (var-get contract-var)]
    (is (= #{'p18-cli-version-record 'p18-cli-help-text 'cli-usage-text
             'sh07-core-input}
           (set (keys (ns-publics 'gravity.cli)))))
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.cli (:namespace contract)))
    (is (= :bootstrap-cli-presentation-and-core-arguments
           (:contract-boundary contract)))
    (is (= #{'p18-cli-version-record 'p18-cli-help-text 'cli-usage-text
             'sh07-core-input}
           (set (keys (:public-api contract)))))
    (is (= [:packaged-jvm-cli-state :selected-sh07-command-arguments] (:artifact-inputs contract)))
    (is (= [:bootstrap-version-record :bootstrap-help-text
            :bootstrap-usage-text :sh07-core-input]
           (:artifact-outputs contract)))
    (is (= ['clojure.core]
           (get-in contract [:dependency-direction :requires])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (is (some #{:diagnostic-authenticity}
              (get-in contract [:ownership :does-not-own])))
    (is (some #{:command-dispatch}
              (get-in contract [:ownership :does-not-own])))
    (is (empty? (ns-aliases 'gravity.cli)))
    (is (true? (:bootstrap-hosted? contract)))
    (is (false? (:production-t1-cli-conformance? contract)))
    (is (false? (:full-t1-command-surface? contract)))
    (is (false? (:seedless-release? contract)))
    (is (false? (:self-hosted? contract)))))

(deftest selected-core-arguments-preserve-values-and-refuse-before-source-io
  (let [wrapper (ns-resolve 'gravity.bootstrap 'sh07-public-core-cli-input)
        reads (atom 0)
        rejection (fn [parse arguments]
                    (try (parse arguments) nil
                         (catch clojure.lang.ExceptionInfo error
                           {:message (.getMessage error) :data (ex-data error)})))]
    (is (= '([arguments]) (:arglists (meta #'cli/sh07-core-input))))
    (is (= :already-selected (get-in (:gravity/namespace-contract (meta #'cli/sh07-core-input))
                                     [:public-api 'sh07-core-input :command-selection])))
    (with-redefs [bootstrap/read-gravity-source-text
                  (fn [_] (swap! reads inc) (throw (AssertionError. "Unexpected target read")))]
      (doseq [[arguments expected] [[["sh07-core" "module.gravity"] ["module.gravity" {}]]
                                    [["sh07-core" "module.qst" "--development-current-reader"]
                                     ["module.qst" {:reader :development-current}]]
                                    [["sh07-core" ""] ["" {}]]]
              parse [cli/sh07-core-input wrapper]]
        (is (= expected (parse arguments))))
      (doseq [arguments [["sh07-core"] ["sh07-core" nil] ["sh07-core" 0]
                        ["sh07-core" "--unknown"] ["sh07-core" "module.gravity" "--unknown"]
                        ["sh07-core" "module.gravity" "extra.gravity"]
                        ["sh07-core" "module.gravity" "--development-current-reader" "extra"]
                        ["sh07-core" "module.gravity" "--development-current-reader" "--development-current-reader"]]
              parse [cli/sh07-core-input wrapper]]
        (is (= {:message "usage: gravity sh07-core FILE [--development-current-reader]"
                :data {:id "SH07-CLI-ARGS" :stage :command-line :severity :error}}
               (rejection parse arguments)))))
    (is (zero? @reads))))

(deftest diagnostic-presentation-is-sanitized-and-bootstrap-free
  (let [writer (java.io.StringWriter.)
        exception (ex-info "hidden host detail"
                           {:id "C15-TEST"
                            :severity :error
                            :cause-message "stable"
                            :secret "must-not-render"})]
    (binding [*err* writer]
      (diagnostic-presentation/print-diagnostic!
       (fn [_] {:facts {:authenticated true}})
       exception))
    (let [rendered (str writer)
          projection (edn/read-string (str/trim rendered))]
      (is (= "C15-TEST" (:id projection)))
      (is (= :error (:severity projection)))
      (is (= "stable" (:cause-message projection)))
      (is (= {:authenticated true} (:facts projection)))
      (is (not (str/includes? rendered "must-not-render")))))
  (doseq [namespace ['gravity.cli.diagnostic-presentation
                     'gravity.cli.dispatch]]
    (is (not-any? #{'gravity.bootstrap}
                  (map ns-name (vals (ns-aliases namespace)))))))

(deftest public-compile-diagnostics-preserve-command-facts
  (let [source "never-created.gravity"
        output "target/public-compile-options"
        prefix ["compile" source "--target" "js"]]
    (doseq [[arguments expected]
            [[(into prefix ["--unknown" "value" "-o" output])
              {:id "P18T04002" :unsupported-option "--unknown"}]
             [["compile" source "--target"]
              {:id "P18T04002" :option "--target" :missing-fields [:target]}]
             [(into prefix ["-o"])
              {:id "P18T04002" :option "-o" :missing-fields [:output-path]}]
             [(into prefix ["--lowering"])
              {:id "P18T04002" :option "--lowering" :missing-fields [:lowering-mode]}]
             [(into prefix ["--target" "js-ts" "-o" output])
              {:id "P18T04002" :option "--target" :missing-fields [:duplicate-target-option]}]
             [(into prefix ["-o" output "--output" output])
              {:id "P18T04002" :option "--output" :missing-fields [:duplicate-output-option]}]
             [(into prefix ["--lowering" "runtime-derived" "--lowering" "runtime-derived" "-o" output])
              {:id "P18T04002" :option "--lowering" :missing-fields [:duplicate-lowering-option]}]
             [(into prefix ["--lowering" "not-supported" "-o" output])
              {:id "B6-TARGET" :option "--lowering"
               :source-span {:source "bin/gravity"}
               :lowering-mode :not-supported
               :supported-lowering-modes [:runtime-derived]}]]]
      (let [rejected (try
                       (dispatch/dispatch! #(ns-resolve 'gravity.bootstrap %) arguments)
                       nil
                       (catch clojure.lang.ExceptionInfo exception exception))]
        (is (instance? clojure.lang.ExceptionInfo rejected) (str arguments))
        (when rejected
          (let [writer (java.io.StringWriter.)]
            (binding [*err* writer]
              (diagnostic-presentation/print-diagnostic! (constantly nil) rejected))
            (let [projection (edn/read-string (str writer))]
              (doseq [[key value] expected]
                (is (= value (get projection key)) (str arguments " " key)))
              (is (empty? (select-keys (:source-span projection)
                                      [:line :column :start :end :byte-start :byte-end]))
                  "Command-option errors do not invent source coordinates"))))))))

(defn- fake-resolver [operation]
  (case operation
    p18-cli-help-text (fn [] "injected help\n")
    p18-cli-version-record (fn [] {:version "injected"})
    compiler-c2-reader-file-artifact (fn [path] {:artifact :reader :path path})
    check-file-artifact (fn [path] {:module path})
    check-artifact-module-name :module
    run-file (fn [path] (str "ran " path "\n"))
    run-compiled-file (fn [path] (str "compiled " path "\n"))
    (throw (ex-info "unexpected fake operation" {:operation operation}))))

(deftest extracted-dispatch-uses-injected-operations
  (let [result (atom nil)
        help-output (with-out-str
                      (reset! result (dispatch/dispatch! fake-resolver ["help"])))
        read-output (with-out-str
                      (reset! result (dispatch/dispatch! fake-resolver
                                                         ["read" "module.gravity"])))]
    (is (true? @result))
    (is (= "injected help\n" help-output))
    (is (= {:artifact :reader :path "module.gravity"}
           (edn/read-string read-output))))
  (let [result (atom nil)
        output (with-out-str
                 (reset! result (dispatch/dispatch! fake-resolver
                                                    ["not-a-command"])))]
    (is (false? @result))
    (is (empty? output))))

(deftest public-compile-preserves-operation-resolution-and-target-boundaries
  (let [source "module.gravity"
        output "target/public-compile-dispatch"]
    (doseq [[flags expected-operation expected-arguments]
            [[[] 'compile-file [source]]
             [["-o" output] 'p18-t04-compile-executable-file! [source output]]
             [["--target" "jvm"] 'compile-file [source]]
             [["--target" "jvm" "-o" output]
              'p18-t04-compile-executable-file! [source output]]
             [["--target" "jvm" "--lowering" "runtime-derived" "-o" output]
              'p18-t04-compile-jvm-target-file! [source output :jvm :runtime-derived]]
             [["--target" "c" "-o" output]
              'p18-t04-compile-c-target-file! [source output :c nil]]
             [["--target" "c" "--lowering" "verified-mir" "-o" output]
              'p18-t04-compile-experimental-verified-mir-c-target-file! :request]
             [["--target" "js" "-o" output]
              'p18-t04-compile-checked-js-target-file! :request]
             [["--target" "js-ts" "-o" output]
              'p18-t04-compile-checked-js-target-file! :request]
             [["--target" "js" "--lowering" "runtime-derived" "-o" output]
              'p18-t04-compile-js-ts-target-file! [source output :js-ts :runtime-derived]]
             [["--target" "js-ts" "--lowering" "runtime-derived" "-o" output]
              'p18-t04-compile-js-ts-target-file! [source output :js-ts :runtime-derived]]
             [["--target" "js" "--lowering" "verified-mir" "-o" output]
              'p18-t04-compile-checked-js-target-file! :request]
             [["--target" "js-ts" "--lowering" "verified-mir" "-o" output]
              'p18-t04-compile-checked-js-target-file! :request]
             [["--target" "js" "--lowering" "not-supported" "-o" output]
              'p18-t04-compile-checked-js-target-file! :request]]]
      (let [arguments (into ["compile" source] flags)
            request (bootstrap/p18-t04-parse-compile-request arguments)
            calls (atom [])
            selected? (atom nil)
            resolver
            (fn [operation]
              (case operation
                p18-t04-parse-compile-request #'bootstrap/p18-t04-parse-compile-request
                js-ts-backend-target #'bootstrap/js-ts-backend-target
                c-backend-supported-targets #'bootstrap/c-backend-supported-targets
                (fn [& values]
                  (swap! calls conj [operation (vec values)])
                  {:operation operation})))
            stdout (with-out-str
                     (reset! selected? (dispatch/dispatch! resolver arguments)))]
        (is (true? @selected?) (str arguments))
        (is (= [[expected-operation
                 (if (= :request expected-arguments) [request] expected-arguments)]]
               @calls)
            (str arguments))
        (is (= {:operation expected-operation} (edn/read-string stdout)))))))

(deftest extracted-entrypoint-owns-error-exits
  (let [statuses (atom [])
        stderr (java.io.StringWriter.)]
    (binding [*err* stderr]
      (entrypoint/run!
       ["not-a-command"]
       {:resolve-operation fake-resolver
        :print-diagnostic! (fn [_] (throw (AssertionError.)))
        :exit! #(swap! statuses conj %)}))
    (is (= [2] @statuses))
    (is (str/starts-with? (str stderr) "usage: clojure -M:gravity ")))
  (let [statuses (atom [])
        diagnostics (atom [])
        resolver (fn [operation]
                   (case operation
                     compiler-c2-reader-file-artifact
                     (fn [_] (throw (ex-info "rejected" {:id "TEST"})))))]
    (entrypoint/run!
     ["read" "rejected.gravity"]
     {:resolve-operation resolver
      :print-diagnostic! #(swap! diagnostics conj (ex-data %))
      :exit! #(swap! statuses conj %)})
    (is (= [{:id "TEST"}] @diagnostics))
    (is (= [1] @statuses))))

(deftest extracted-command-boundary-is-bootstrap-free
  (doseq [namespace ['gravity.cli.commands.bootstrap
                     'gravity.cli.commands.compiler
                     'gravity.cli.commands.platform
                     'gravity.cli.compile-command
                     'gravity.cli.dispatch
                     'gravity.cli.entrypoint]]
    (require namespace)
    (is (not-any? #{'gravity.bootstrap}
                  (map ns-name (vals (ns-aliases namespace)))))))
