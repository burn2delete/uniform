

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

(def p18-t02-jar-path packaged-launcher/packaged-jar-path)
(def p18-t02-build-root
  (.getPath (.getParentFile (java.io.File. p18-t02-jar-path))))
(def p18-t02-classes-dir (str p18-t02-build-root "/classes"))
(def p18-t02-manifest-path (str p18-t02-build-root "/MANIFEST.MF"))
(def p18-t02-launcher-source "bootstrap/clojure/java/gravity/cli/Main.java")
(def p18-t02-source-root "bootstrap/clojure/src")
(def p18-t02-manifest-entry "META-INF/MANIFEST.MF")
(def p18-t02-launcher-class-entry "gravity/cli/Main.class")

(defn p18-t02-production-source-paths
  ([] (p18-t02-production-source-paths p18-t02-source-root))
  ([source-root]
   (let [root (java.nio.file.Paths/get source-root (make-array String 0))
         paths
         (with-open [stream
                     (java.nio.file.Files/walk
                      root
                      (make-array java.nio.file.FileVisitOption 0))]
           (vec (iterator-seq (.iterator stream))))]
     (when-let [symbolic-link
                (some #(when (java.nio.file.Files/isSymbolicLink %) %)
                      paths)]
       (throw (ex-info "packaged source inventory contains a symbolic link"
                       {:path (.toString symbolic-link)})))
     (->> paths
          (filter #(java.nio.file.Files/isRegularFile
                    %
                    (make-array java.nio.file.LinkOption 0)))
          (map #(.replace (.toString ^java.nio.file.Path %)
                          (str java.io.File/separator)
                          "/"))
          (filter #(or (str/ends-with? % ".clj")
                       (str/ends-with? % ".cljc")
                       (str/ends-with? % ".edn")))
          sort
          vec))))

(defn p18-t02-production-source-entry
  [path]
  {:role :runtime-source
   :path path
   :jar-entry (subs path (inc (count p18-t02-source-root)))})

(defn p18-t02-source-inventory
  "Discover the exact production source inventory for one package operation.
  This must remain an operation-time function: requiring the bootstrap must not
  walk the production source tree or retain a cross-operation snapshot."
  []
  (vec
   (concat
    [{:role :launcher :path p18-t02-launcher-source
      :compiled-jar-entry p18-t02-launcher-class-entry}]
    (map p18-t02-production-source-entry
         (p18-t02-production-source-paths))
    [{:role :deps :path "deps.edn"}])))
(def p18-t02-artifact-dir
  "target/development-loop-batches/phase-18/jvm-cli/artifacts")
