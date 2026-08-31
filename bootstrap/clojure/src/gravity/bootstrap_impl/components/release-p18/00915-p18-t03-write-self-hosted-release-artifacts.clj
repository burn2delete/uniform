

(defn p18-t03-write-self-hosted-release-artifacts!
  ([]
   (p18-t03-write-self-hosted-release-artifacts! p18-t03-compiler-source))
  ([compiler-source]
   (let [artifact (p18-t03-self-hosted-release-artifact! compiler-source)]
     (p18-t02-write-edn!
      (str p18-t03-artifact-dir
           "/p18-t03-self-hosted-release-artifact-proof.edn")
      artifact)
     (p18-t02-write-edn!
      (str p18-t03-artifact-dir
           "/p18-t03-release-artifact-candidate.edn")
      (:release-artifact-candidate artifact))
     (p18-t02-write-edn!
      (str p18-t03-artifact-dir "/p18-t03-compiler-path.edn")
      (:compiler-path-record artifact))
     (p18-t02-write-edn!
      (str p18-t03-artifact-dir "/p18-t03-runtime-boundary.edn")
      (:runtime-boundary-record artifact))
     (p18-t02-write-edn!
      (str p18-t03-artifact-dir "/p18-t03-seed-boundary.edn")
      (:seed-boundary-record artifact))
     (p18-t02-write-edn!
      (str p18-t03-artifact-dir "/p18-t03-source-debug-map.edn")
      (:source-debug-map artifact))
     (p18-t02-write-edn!
      (str p18-t03-artifact-dir "/p18-t03-provenance.edn")
      (:provenance-record artifact))
     (p18-t02-write-edn!
      (str p18-t03-artifact-dir "/p18-t03-rejected-fixtures.edn")
      (:rejected-fixtures artifact))
     artifact)))

(def p18-t04-build-root "target/phase-18/command")
(def p18-t04-artifact-dir "docs/artifacts/phase-18/command")

(def p18-t04-accepted-fixtures
  [{:fixture "examples/hello.gravity"
    :output-path (str p18-t04-build-root "/hello-app")
    :expected-stdout "Hello Gravity\n"}
   {:fixture "examples/core-app.gravity"
    :output-path "target/core-app"
    :expected-stdout "core-app\ngravity:19:2\n(:ok 19)\n"}
   {:fixture "examples/nontrivial-app.gravity"
    :output-path (str p18-t04-build-root "/nontrivial-app")
    :expected-stdout "nontrivial-app\ngravity:ready:2\n(:release 24)\n"}])

(def p18-t04-rejected-command-fixtures
  [{:fixture "bootstrap/clojure/fixtures/rejected/surface-syntax-l1-delimiter.gravity"
    :category :parser
    :output-path (str p18-t04-build-root "/rejected-parser")
    :expected-diagnostic "L1-DELIMITER"}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-function-arity.gravity"
    :category :semantic
    :output-path (str p18-t04-build-root "/rejected-semantic")
    :expected-diagnostic "L2-FUNCTION-ARITY"}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-profile-capability.gravity"
    :category :capability
    :output-path (str p18-t04-build-root "/rejected-capability")
    :expected-diagnostic "P4-HOST-CAPABILITY"}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-package-provenance.gravity"
    :category :package
    :output-path (str p18-t04-build-root "/rejected-package")
    :expected-diagnostic "PKG10001"}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-backend-release.gravity"
    :category :release-boundary
    :output-path (str p18-t04-build-root "/rejected-release-boundary")
    :expected-diagnostic "B13-RELEASE"}])

(def p18-t04-diagnostic-messages
  {"P18T04001" "public gravity executable command parity proof failed"
   "P18T04002" "compile executable output usage is invalid"
   "P18T04003" "compiled executable artifact is missing or not executable"
   "P18T04004" "compiled executable output does not match the checked run output"
   "P18T04005" "executable command contract cannot be claimed as final seedless release"
   "P18T04006" "bootstrap-hosted public test command cannot be claimed as full language conformance"
   "P18T04007" "public self-host verification is blocked by the active Clojure seed boundary"
   "P18T04008" "public self-host verification command usage is invalid"})

(def p18-t04-diagnostic-ids
  ["P18T04001" "P18T04002" "P18T04003" "P18T04004" "P18T04005"
   "P18T04006" "P18T04007" "P18T04008"])

(defn p18-t04-diagnostic-record
  [id fixture facts]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p18-t04-executable-command-contract
   :fixture fixture
   :source-span {:source (or (:source facts) "bin/gravity")}
   :message (get p18-t04-diagnostic-messages id)
   :facts facts
   :remediation :repair_p18_t04_executable_command_contract})

(defn p18-t04-fail!
  [id facts]
  (fail! id
         (get p18-t04-diagnostic-messages id)
         (merge {:source-span {:source (or (:source facts) "bin/gravity")}
                 :phase "P18-T04"
                 :diagnostic-family :p18-t04-executable-command-contract}
                facts)))

(defn p18-t04-path-segments
  [path]
  (vec (remove str/blank? (str/split (str path) #"/+"))))

(defn p18-t04-output-path-allowed?
  [output-path]
  (let [file (java.io.File. (str output-path))
        segments (p18-t04-path-segments output-path)]
    (and (string? output-path)
         (not (str/blank? output-path))
         (not (.isAbsolute file))
         (not-any? #{".."} segments)
         (or (str/starts-with? output-path "target/")
             (= 1 (count segments))))))