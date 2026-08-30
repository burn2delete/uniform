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
     "java -cp target/phase-18/jvm-cli/gravity-jvm-cli.jar gravity.cli.Main"
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

(deftest cli-namespace-contract-is-narrow-and-acyclic
  (let [contract-var
        (get (ns-interns 'gravity.cli) 'namespace-contract)
        contract (var-get contract-var)]
    (is (= #{'p18-cli-version-record 'p18-cli-help-text}
           (set (keys (ns-publics 'gravity.cli)))))
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.cli (:namespace contract)))
    (is (= :bootstrap-cli-presentation-values
           (:contract-boundary contract)))
    (is (= #{'p18-cli-version-record 'p18-cli-help-text}
           (set (keys (:public-api contract)))))
    (is (= [:packaged-jvm-cli-state] (:artifact-inputs contract)))
    (is (= [:bootstrap-version-record :bootstrap-help-text]
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
