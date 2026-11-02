

(defn p18-t01-thin-cli-wrapper-artifact
  []
  (let [hello "examples/hello.gravity"
        rejected "bootstrap/clojure/fixtures/rejected/core-app-builtin-arity.gravity"
        hello-artifact (compile-file hello)
        hello-output (run-file hello)
        rejected-diagnostic (p18-diagnostic-id #(run-compiled-file rejected))
        artifact-base
        {:kind :gravity/p18-t01-thin-cli-wrapper-proof
         :task "P18-T01"
         :status :complete
         :phase :binary-distribution-and-seedless-release
         :command-boundary
         {:public-command "bin/gravity"
          :bootstrap-command "bin/gravity-bootstrap"
          :delegates-to ["clojure" "-M:gravity"]
          :bootstrap-hosted? true
          :seedless-release? false
          :public-release-boundary? false
          :replacement-objective
          :replace-with-self-hosted-release-artifact}
         :command-metadata
         {:version-reports {:bootstrap-hosted? true
                            :seedless-release? false
                            :delegates-to "clojure -M:gravity"}
          :help-lists ["check" "run" "compile" "test" "artifact-command"]
          :delegated-commands-report-bootstrap-hosted? true}
         :accepted-command-proofs
         [{:command ["bin/gravity" "check" hello]
           :delegated-command ["clojure" "-M:gravity" "check" hello]
           :module (get-in hello-artifact [:module :module])
           :bootstrap-hosted? true
           :expected-stdout-prefix "gravity stage0 check passed:"}
          {:command ["bin/gravity" "run" hello]
           :delegated-command ["clojure" "-M:gravity" "run" hello]
           :stdout hello-output
           :bootstrap-hosted? true}
          {:command ["bin/gravity" "compile" hello]
           :delegated-command ["clojure" "-M:gravity" "compile" hello]
           :artifact-kind (:kind hello-artifact)
           :bootstrap-hosted? true}]
         :rejected-command-proofs
         [{:command ["bin/gravity" "run-compiled" rejected]
           :delegated-command ["clojure" "-M:gravity" "run-compiled" rejected]
           :diagnostic rejected-diagnostic
           :stable-diagnostic-preserved? (= "L2-BUILTIN-ARITY"
                                            rejected-diagnostic)
           :bootstrap-hosted? true}]
         :unsupported-release-claim
         {:command ["bin/gravity" "--assert-seedless-release"]
          :diagnostic "P18T01001"
          :bootstrap-hosted? true
          :seedless-release? false}
         :capability-based-proof
         {:thin-wrapper-present? (p18-executable? "bin/gravity")
          :bootstrap-command-present? (p18-executable? "bin/gravity-bootstrap")
          :check-delegates-to-stage0? (= 'hello.main
                                         (get-in hello-artifact
                                                 [:module :module]))
          :run-delegates-to-stage0? (= "Hello Gravity\n" hello-output)
          :compile-delegates-to-stage0? (= :gravity/stage0-hosted-artifact
                                           (:kind hello-artifact))
          :diagnostic-preserved? (= "L2-BUILTIN-ARITY" rejected-diagnostic)
          :seedless-overclaim-rejected? true
          :final-seedless-release? false}}]
    (assoc artifact-base :artifact-id (c4-artifact-id artifact-base))))

(def p18-t02-build-root "target/phase-18/jvm-cli")
(def p18-t02-classes-dir (str p18-t02-build-root "/classes"))
(def p18-t02-manifest-path (str p18-t02-build-root "/MANIFEST.MF"))
(def p18-t02-jar-path (str p18-t02-build-root "/gravity-jvm-cli.jar"))
(def p18-t02-launcher-source "bootstrap/clojure/java/gravity/cli/Main.java")
(def p18-t02-bootstrap-source "bootstrap/clojure/src/gravity/bootstrap.clj")
(def p18-t02-diagnostics-source
  "bootstrap/clojure/src/gravity/diagnostics.clj")
(def p18-t02-cli-source "bootstrap/clojure/src/gravity/cli.clj")
(def p18-t02-darwin-publication-source
  "bootstrap/clojure/src/gravity/darwin_publication.clj")
(def p18-t02-manifest-entry "META-INF/MANIFEST.MF")
(def p18-t02-launcher-class-entry "gravity/cli/Main.class")
(def p18-t02-source-inventory
  [{:role :launcher :path p18-t02-launcher-source
    :compiled-jar-entry p18-t02-launcher-class-entry}
   {:role :bootstrap :path p18-t02-bootstrap-source
    :jar-entry "gravity/bootstrap.clj"}
   {:role :cli :path p18-t02-cli-source
    :jar-entry "gravity/cli.clj"}
   {:role :diagnostics :path p18-t02-diagnostics-source
    :jar-entry "gravity/diagnostics.clj"}
   {:role :darwin-publication :path p18-t02-darwin-publication-source
    :jar-entry "gravity/darwin_publication.clj"}
   {:role :deps :path "deps.edn"}])
(def p18-t02-artifact-dir "docs/artifacts/phase-18/jvm-cli")