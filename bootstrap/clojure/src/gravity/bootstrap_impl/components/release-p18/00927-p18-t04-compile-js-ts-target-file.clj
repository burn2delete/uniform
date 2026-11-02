

(defn p18-t04-compile-js-ts-target-file!
  "Compile a current source unit to the bounded Node 20 ES2022 ESM target.

  Both public aliases are canonicalized before this boundary.  The emitted
  JavaScript, declarations, source map, package metadata, manifest, and
  provenance are validated and differentially executed before becoming
  caller-visible."
  [source-path output-path target lowering-mode]
  (let [target (js-ts-backend-canonical-target target)]
    (when-not (= js-ts-backend-target target)
      (js-ts-backend-fail!
       "B6-TARGET" "public JS/TS target is unsupported"
       source-path nil
       {:requested-target target
        :supported-targets (vec (sort js-ts-backend-target-aliases))
        :missing-fact :supported-target}))
    (when (and lowering-mode (not= :runtime-derived lowering-mode))
      (js-ts-backend-fail!
       "B6-TARGET" "requested JS/TS lowering mode is unsupported"
       source-path nil
       {:lowering-mode lowering-mode
        :supported-lowering-modes [:runtime-derived]
        :missing-fact :js-ts-lowering-mode}))
    (when-not output-path
      (js-ts-backend-fail!
       "C14-INPUT" "JS/TS target compilation requires an explicit output"
       source-path nil
       {:missing-fields [:output-path]
        :remediation "Use --target js -o <module-base>."}))
    (let [artifact
          (js-ts-backend-file-artifact
           source-path
           {:target target :output-path output-path :emit? true})]
      (assoc artifact
             :command-boundary
             {:compile-command
              (cond-> ["gravity" "compile" source-path
                       "--target" (name target)]
                lowering-mode
                (into ["--lowering" (name lowering-mode)])
                true
                (into ["-o" output-path]))
              :run-command ["node"
                            (get-in artifact
                                    [:provenance :actual-paths :outputs
                                     :javascript])]
              :public-command "gravity"
              :bootstrap-hosted? true
              :clojure-seed-boundary? true
              :self-hosted? false
              :seedless-release? false}
             :target-requested? true
             :target-selection :explicit
             :public-current-source? true
             :lowering-mode :runtime-derived
             :lowering-requested? (some? lowering-mode)
             :compiled-executable? true))))

(defn p18-t04-compile-jvm-target-file!
  "Compile a current source unit to the bounded Java 21 modular JAR target.

  This entrypoint is intentionally reachable only for the explicit
  runtime-derived lowering mode.  The historical JVM compile behavior remains
  the default seed/release compatibility route."
  [source-path output-path target lowering-mode]
  (when-not (= jvm-backend-target target)
    (jvm-backend-fail!
     "B5-TARGET" "public JVM target is unsupported"
     source-path nil
     {:requested-target target :supported-targets [:jvm]
      :missing-fact :supported-target}))
  (when-not (= :runtime-derived lowering-mode)
    (jvm-backend-fail!
     "B5-TARGET" "new JVM backend requires the runtime-derived lowering mode"
     source-path nil
     {:lowering-mode lowering-mode
      :supported-lowering-modes [:runtime-derived]
      :missing-fact :jvm-lowering-mode}))
  (when-not output-path
    (jvm-backend-fail!
     "C14-INPUT" "JVM target compilation requires an explicit output"
     source-path nil
     {:missing-fields [:output-path]
      :remediation
      "Use --target jvm --lowering runtime-derived -o <artifact-directory>."}))
  (let [artifact
        (jvm-backend-file-artifact
         source-path {:output-path output-path :emit? true})]
    (assoc artifact
           :command-boundary
           {:compile-command
            ["gravity" "compile" source-path "--target" "jvm"
             "--lowering" "runtime-derived" "-o" output-path]
            :run-command
            ["java" "-jar"
             (get-in artifact [:provenance :actual-paths :outputs :jar])]
            :public-command "gravity"
            :bootstrap-hosted? true
            :clojure-seed-boundary? true
            :self-hosted? false
            :seedless-release? false}
           :target-requested? true
           :target-selection :explicit
           :public-current-source? true
           :lowering-mode :runtime-derived
           :lowering-requested? true
           :compiled-executable? true)))

(defn p18-t04-shell
  [& args]
  (if (= "bin/gravity" (first args))
    (p18-shell-run {"GRAVITY_PACKAGED_CLI_ONLY" "1"} args)
    (p18-shell-run args)))

(defn p18-t04-read-edn-stdout
  [result]
  (when (zero? (:exit result))
    (edn/read-string (:out result))))

(def p18-t04-public-test-accepted-fixtures
  [{:fixture "examples/hello.gravity"
    :expected-stdout "Hello Gravity\n"}
   {:fixture "examples/hello.qst"
    :expected-stdout "Hello Gravity\n"}
   {:fixture "examples/core-app.gravity"
    :expected-stdout "core-app\ngravity:19:2\n(:ok 19)\n"}
   {:fixture "examples/core-app.qst"
    :expected-stdout "core-app\ngravity:19:2\n(:ok 19)\n"}
   {:fixture "examples/nontrivial-app.gravity"
    :expected-stdout "nontrivial-app\ngravity:ready:2\n(:release 24)\n"}])

(def p18-t04-public-test-rejected-fixtures
  [{:fixture "bootstrap/clojure/fixtures/rejected/surface-syntax-l1-delimiter.gravity"
    :command :check
    :expected-diagnostic "L1-DELIMITER"}
   {:fixture "bootstrap/clojure/fixtures/rejected/surface-syntax-l1-delimiter.qst"
    :command :check
    :expected-diagnostic "L1-DELIMITER"}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-function-arity.gravity"
    :command :compile
    :expected-diagnostic "L2-FUNCTION-ARITY"}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-function-arity.qst"
    :command :compile
    :expected-diagnostic "L2-FUNCTION-ARITY"}
   {:fixture "bootstrap/clojure/fixtures/rejected/host-semantics.gravity"
    :command :check
    :expected-diagnostic "L2-HOST-SEMANTICS"}
   {:fixture "bootstrap/clojure/fixtures/rejected/host-semantics.qst"
    :command :check
    :expected-diagnostic "L2-HOST-SEMANTICS"}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-profile-capability.gravity"
    :command :compile
    :expected-diagnostic "P4-HOST-CAPABILITY"}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-package-provenance.gravity"
    :command :compile
    :expected-diagnostic "PKG10001"}
   {:fixture "bootstrap/clojure/fixtures/rejected/runtime-r1-selection.gravity"
    :command :check
    :expected-diagnostic "R1-SELECTION"}
   {:fixture "bootstrap/clojure/fixtures/rejected/runtime-r1-selection.qst"
    :command :check
    :expected-diagnostic "R1-SELECTION"}
   {:fixture "bootstrap/clojure/fixtures/rejected/runtime-r2-hidden-service.gravity"
    :command :check
    :expected-diagnostic "R2-HIDDEN-SERVICE"}
   {:fixture "bootstrap/clojure/fixtures/rejected/runtime-r2-hidden-service.qst"
    :command :check
    :expected-diagnostic "R2-HIDDEN-SERVICE"}
   {:fixture "bootstrap/clojure/fixtures/rejected/runtime-r4-host.gravity"
    :command :check
    :expected-diagnostic "R4-HOST"}
   {:fixture "bootstrap/clojure/fixtures/rejected/runtime-r4-host.qst"
    :command :check
    :expected-diagnostic "R4-HOST"}
   {:fixture "bootstrap/clojure/fixtures/rejected/runtime-r4-null.gravity"
    :command :check
    :expected-diagnostic "R4-NULL"}
   {:fixture "bootstrap/clojure/fixtures/rejected/runtime-r4-null.qst"
    :command :check
    :expected-diagnostic "R4-NULL"}
   {:fixture "bootstrap/clojure/fixtures/rejected/runtime-r4-exception.gravity"
    :command :check
    :expected-diagnostic "R4-EXCEPTION"}
   {:fixture "bootstrap/clojure/fixtures/rejected/runtime-r4-exception.qst"
    :command :check
    :expected-diagnostic "R4-EXCEPTION"}
   {:fixture "bootstrap/clojure/fixtures/rejected/runtime-r4-reflection.gravity"
    :command :check
    :expected-diagnostic "R4-REFLECTION"}
   {:fixture "bootstrap/clojure/fixtures/rejected/runtime-r4-reflection.qst"
    :command :check
    :expected-diagnostic "R4-REFLECTION"}
   {:fixture "bootstrap/clojure/fixtures/rejected/runtime-r4-collection.gravity"
    :command :check
    :expected-diagnostic "R4-COLLECTION"}
   {:fixture "bootstrap/clojure/fixtures/rejected/runtime-r4-collection.qst"
    :command :check
    :expected-diagnostic "R4-COLLECTION"}
   {:fixture "bootstrap/clojure/fixtures/rejected/runtime-r4-resource.gravity"
    :command :check
    :expected-diagnostic "R4-RESOURCE"}
   {:fixture "bootstrap/clojure/fixtures/rejected/runtime-r4-resource.qst"
    :command :check
    :expected-diagnostic "R4-RESOURCE"}
   {:fixture "bootstrap/clojure/fixtures/rejected/runtime-r4-sourcemap.gravity"
    :command :check
    :expected-diagnostic "R4-SOURCEMAP"}
   {:fixture "bootstrap/clojure/fixtures/rejected/runtime-r4-sourcemap.qst"
    :command :check
    :expected-diagnostic "R4-SOURCEMAP"}
   {:fixture "bootstrap/clojure/fixtures/rejected/runtime-r4-profile.gravity"
    :command :check
    :expected-diagnostic "R4-PROFILE"}
   {:fixture "bootstrap/clojure/fixtures/rejected/runtime-r4-profile.qst"
    :command :check
    :expected-diagnostic "R4-PROFILE"}
   {:fixture "bootstrap/clojure/fixtures/rejected/runtime-r4-manifest.gravity"
    :command :check
    :expected-diagnostic "R4-MANIFEST"}
   {:fixture "bootstrap/clojure/fixtures/rejected/runtime-r4-manifest.qst"
    :command :check
    :expected-diagnostic "R4-MANIFEST"}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-backend-release.gravity"
    :command :compile
    :expected-diagnostic "B13-RELEASE"}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-backend-release.qst"
    :command :compile
    :expected-diagnostic "B13-RELEASE"}])

(defn p18-t04-public-test-output-path
  [fixture]
  (let [basename (.getName (java.io.File. fixture))
        safe-name (str/replace basename #"[^A-Za-z0-9_.-]" "_")]
    (str p18-t04-build-root "/public-test/" safe-name)))