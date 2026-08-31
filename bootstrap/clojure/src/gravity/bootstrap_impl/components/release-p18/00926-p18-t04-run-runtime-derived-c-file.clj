

(defn p18-t04-run-runtime-derived-c-file!
  "Execute one explicitly requested source unit through the real C runtime.

  Compilation and execution happen in a private temporary directory.  The
  returned record distinguishes the native application runtime from the
  still-bootstrap-hosted compiler, verifier, comparison, and process/IO
  boundaries.  The default `run <source>` command never calls this function."
  [request]
  (when-not
   (and (map? request)
        (= "run" (:command request))
        (string? (:source-path request))
        (= :c (:target request))
        (= "c" (:target-argument request))
        (= :runtime-derived (:lowering-mode request))
        (= "runtime-derived" (:lowering-argument request))
        (true? (:target-requested? request))
        (true? (:lowering-requested? request))
        (true? (:runtime-derived-requested? request))
        (nil? (:output-path request)))
    (p18-t04-fail!
     "P18T04002"
     {:source (or (:source-path request) "bin/gravity")
      :request request
      :missing-fields [:exact-runtime-derived-c-run-request]
      :remediation
      "Use p18-t04-parse-run-request on run <source> --target c --lowering runtime-derived."}))
  ;; Java's ProcessBuilder cannot execute descriptor-relative to the retained
  ;; SecureDirectoryStream, and ProcessHandle does not establish an OS process
  ;; group/job.  Do not perform source I/O, staging, compilation, or native
  ;; execution until both path identity and whole-tree containment are backed
  ;; by OS primitives.
  (p18-t04-fail!
   "P18T04002"
   {:source (:source-path request)
    :request (select-keys request
                          [:source-path :source-extension :source-kind
                           :target :target-argument
                           :lowering-mode :lowering-argument])
    :missing-fields [:descriptor-relative-native-execution
                     :os-process-tree-containment]
    :missing-fact :contained-public-native-run
    :native-executable-run? false
    :clojure-seed-boundary? true
    :self-hosted? false
    :public-release? false
    :seedless-release? false
    :remediation
    "Provide an OS-contained native launcher with descriptor-relative execution before enabling this route."})
  (let [source-path (:source-path request)
        directory (p18-t04-run-private-directory! source-path)
        output-path (str (.toString ^java.nio.file.Path (:path directory))
                        "/program")
        primary-failure (atom nil)
        cleanup-failure (atom nil)
        cleanup-record (atom nil)
        result (atom nil)]
    (try
      (try
        (let [artifact
              (p18-t04-compile-c-target-file!
               source-path output-path :c :runtime-derived)
              executable (java.io.File. output-path)
              stdout (:compiled-execution-output artifact)
              source-record (:source artifact)
              target-record (:target artifact)
              emitted-files (:emitted-files artifact)]
          (when-not (and (true? (:compiled-executable? artifact))
                         (.isFile executable)
                         (.canExecute executable)
                         (= :c (:target target-record))
                         (= :runtime-derived
                            (:lowering-mode target-record))
                         (string? stdout)
                         (= stdout (:stdout artifact)))
            (p18-t04-fail!
             "P18T04002"
             {:source source-path
              :target target-record
              :missing-fields [:real-compiled-executable-execution]
              :missing-fact :compiled-executable-execution-result}))
          (when-not (and (map? source-record)
                         (= source-path (:path source-record))
                         (= (gravity-source-extension source-path)
                            (:extension source-record))
                         (= (gravity-source-kind source-path)
                            (:kind source-record)))
            (p18-t04-fail!
             "P18T04002"
             {:source source-path
              :observed-source source-record
              :missing-fields [:actual-source-path-extension-provenance]
              :missing-fact :source-path-extension-provenance}))
          (reset!
           result
           {:artifact :gravity/p18-t04-runtime-derived-c-application-execution
            :schema-version 1
            :task "P18-T04"
            :status :complete
            :command (:command request)
            :request (select-keys request
                                  [:source-path :source-extension :source-kind
                                   :target :target-argument
                                   :lowering-mode :lowering-argument])
            :source source-record
            :target {:backend :c
                     :target :c
                     :lowering-mode :runtime-derived
                     :runtime :hosted-libc-stdout}
            :stdout stdout
            :execution-result
            {:source :compiled-executable
             :exit-code 0
             :stdout stdout
             :stdout-hash (str "sha256:" (sha256-hex stdout))
             :stderr :not-retained
             :compiled-executable? true}
            :application-runtime
            {:component :native-compiled-c-executable
             :execution-source :p18-t04-compile-c-target-file!
             :compiled-executable? true
             :native-executable-run? true
             :clojure-seed-boundary? false
             :self-hosted? false
             :evidence
             {:executable-path output-path
              :artifact-kind (:kind artifact)
              :compiled-execution-output-field :compiled-execution-output
              :observed-stdout-hash (str "sha256:" (sha256-hex stdout))}}
            :seed-boundary
            {:application-runtime
             {:clojure-seed-boundary? false
              :self-hosted? false
              :evidence :compiled-executable-output}
             :compiler
             {:owner :clojure-bootstrap
              :clojure-seed-boundary? true
              :self-hosted? false}
             :verifier
             {:owner :clojure-bootstrap
              :clojure-seed-boundary? true
              :self-hosted? false}
             :comparison
             {:owner :clojure-bootstrap
              :authoritative? false
              :clojure-seed-boundary? true
              :self-hosted? false}
             :process-file-io
             {:owner :clojure-bootstrap
              :role :temporary-staging-and-process-launch
              :clojure-seed-boundary? true
              :self-hosted? false}
             :public-command
             {:owner :clojure-bootstrap
              :clojure-seed-boundary? true
              :self-hosted? false
              :public-release? false}}
            :runtime-gravity-source
            {:artifact-source (:runtime-artifact-source artifact)
             :artifact-source-path (:runtime-artifact-source-path artifact)
             :rule-source (:runtime-rule-source artifact)
             :rule-source-path (:runtime-rule-source-path artifact)}
            :provenance
            {:backend (:provenance artifact)
             :actual-paths (get-in artifact [:provenance :actual-paths])
             :source source-record
             :runtime-gravity-source
             {:artifact-source-path (:runtime-artifact-source-path artifact)
              :rule-source-path (:runtime-rule-source-path artifact)}}
            :temporary-artifacts
            {:root (.toString ^java.nio.file.Path (:path directory))
             :executable output-path
             :emitted-files emitted-files
             :retention :ephemeral
             :cleanup :pending}
            :diagnostics []
            :native-executable-run? true
            :clojure-seed-boundary? true
            :self-hosted? false
            :public-release? false
            :seedless-release? false}))
        (catch clojure.lang.ExceptionInfo error
          (let [facts (ex-data error)
                enriched
                (if (or (:source-path facts)
                        (:source facts)
                        (get-in facts [:source :path]))
                  error
                  (ex-info
                   (.getMessage error)
                   (assoc facts
                          :source-path source-path
                          :source
                          {:path source-path
                           :extension (gravity-source-extension source-path)
                           :kind (gravity-source-kind source-path)})
                   error))]
            (reset! primary-failure enriched)))
        (catch Throwable error
          (reset! primary-failure error)))
      (try
        (reset! cleanup-record
                (p18-t04-run-delete-private-tree!
                 directory source-path))
        (catch Throwable cleanup
          (reset! cleanup-failure cleanup)
          (when-let [error @primary-failure]
            (.addSuppressed ^Throwable error ^Throwable cleanup))))
      (when-let [error @primary-failure]
        (throw ^Throwable error))
      (when-let [error @cleanup-failure]
        (throw ^Throwable error))
      (when-not @result
        (p18-t04-fail!
         "P18T04002"
         {:source source-path
          :missing-fields [:runtime-derived-c-run-record]
          :missing-fact :runtime-derived-c-run-record}))
      (assoc @result :temporary-artifacts
             (assoc (:temporary-artifacts @result)
                    :cleanup @cleanup-record
                    :root-removed? (true? (:root-removed? @cleanup-record))))
      (catch Throwable error
        (reset! primary-failure error)
        (throw error)))))