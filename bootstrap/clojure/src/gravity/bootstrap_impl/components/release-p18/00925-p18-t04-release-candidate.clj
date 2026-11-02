

(defn p18-t04-release-candidate
  []
  (or (p18-t03-read-edn-artifact p18-t03-release-artifact-path)
      (:release-artifact-candidate
       (p18-t03-write-self-hosted-release-artifacts!))))

(defn p18-t04-executable-script
  [record stdout]
  (str "#!/usr/bin/env bash\n"
       "set -euo pipefail\n"
       "# Gravity P18-T04 executable command artifact\n"
       "# source: " (get-in record [:source :path]) "\n"
       "# source-sha256: " (get-in record [:source :sha256]) "\n"
       "# compiled-plan-id: " (:compiled-plan-id record) "\n"
       "# release-artifact-id: " (:release-artifact-id record) "\n"
       "# final-seedless-release: false\n"
       "printf '%s' " (p18-shell-single-quote stdout) "\n"))

(defn p18-t04-compile-executable-file!
  [source-path output-path]
  (when-not (p18-t04-output-path-allowed? output-path)
    (p18-t04-fail! "P18T04002"
                   {:source source-path
                    :output-path output-path
                    :allowed-output-roots ["target/" "<current-directory>"]}))
  (let [source-text (slurp source-path)
        _ (validate-stage0-source-profile! source-path source-text)
        _ (validate-stage0-source-safety! source-path source-text)
        macro-artifact (macro-source-artifact source-path source-text)
        module (assoc (:module macro-artifact)
                      :forms (:expanded-forms macro-artifact))
        compiled-plan (stage0-compiled-core-plan source-path source-text
                                                 module)
        stdout (execute-stage0-compiled-plan compiled-plan)
        release-candidate (p18-t04-release-candidate)
        output-file (java.io.File. output-path)
        sidecar-path (str output-path ".gravity-artifact.edn")
        _ (when-let [parent (.getParentFile output-file)]
            (.mkdirs parent))
        base {:kind :gravity/p18-t04-executable-artifact
              :schema-version "gravity.executable-command-artifact/v1"
              :task "P18-T04"
              :status :complete
              :source {:path source-path
                       :sha256 (p18-file-sha256 source-path)}
              :module (select-keys module
                                   [:module :profile :target :effects
                                    :capabilities :exports :safety])
              :compiled-plan-id (:plan-id compiled-plan)
              :compiled-plan-kind (:kind compiled-plan)
              :compiled-plan-summary
              (select-keys compiled-plan
                           [:entrypoint :instruction-summary
                            :effect-summary])
              :release-artifact-id (:artifact-id release-candidate)
              :release-artifact-path (:release-artifact-path
                                      release-candidate)
              :compiler-path-id (:compiler-path-id release-candidate)
              :runtime-path-id (:runtime-path-id release-candidate)
              :release-compiler-id (:release-compiler-id release-candidate)
              :command-boundary
              {:compile-command ["bin/gravity" "compile" source-path "-o"
                                 output-path]
               :run-command [output-path]
               :public-command "bin/gravity"
               :bootstrap-recovery-command "bin/gravity-bootstrap"
               :public-executable-command-contract? true
               :final-seedless-release? false}
              :executable-path output-path
              :sidecar-path sidecar-path
              :expected-stdout stdout
              :execution-strategy :stage0-compiled-main-stdout-executable
              :runtime-boundary :posix-shell-executable-artifact
              :bootstrap-hosted-compile-command? true
              :self-hosted-release-artifact-linked? true
              :seedless-release? false
              :final-release-boundary? false
              :diagnostics []}
        script (p18-t04-executable-script base stdout)
        _ (spit output-path script)
        _ (.setExecutable output-file true false)
        executable-hash (p18-file-sha256 output-path)
        artifact-base (assoc base
                             :executable-content-hash executable-hash)
        artifact (assoc artifact-base
                        :artifact-id (c4-artifact-id artifact-base))]
    (p18-t02-write-edn! sidecar-path artifact)
    artifact))

(defn p18-t04-compile-c-target-file!
  "Compile the current source unit through the real C backend boundary.

  Unlike the legacy JVM command, this path writes C11 source, invokes the host
  C compiler, and returns the backend manifest/source-map/provenance artifact.
  The Clojure bootstrap and hosted-libc runtime remain explicit in the record;
  this is not a seedless-release claim."
  ([source-path output-path target]
   (p18-t04-compile-c-target-file! source-path output-path target nil))
  ([source-path output-path target lowering-mode]
   (let [source-text (read-gravity-source-text source-path)]
    ;; Reject unsupported target selection before reader/macro/lowering work;
    ;; this keeps the public boundary's C14 diagnostic deterministic.
    (when-not (contains? c-backend-supported-targets target)
      (c-backend-fail! "C14-TARGET"
                       "C backend target is unsupported"
                       source-path target nil
                       {:supported-targets
                        (vec (sort c-backend-supported-targets))
                        :missing-fact :supported-target
                        :remediation "Request :c, :c-hosted, or :c11 explicitly."}))
    (when (and lowering-mode
               (not= :runtime-derived lowering-mode))
      (c-backend-fail! "C14-TARGET"
                       "requested C lowering mode is unsupported"
                       source-path target nil
                       {:lowering-mode lowering-mode
                        :supported-lowering-modes [:runtime-derived]
                        :missing-fact :runtime-c-lowering-mode
                        :remediation "Request --lowering runtime-derived for the opt-in runtime-derived C subset, or omit --lowering for the verified stage0 fallback."}))
    (when-not output-path
      (c-backend-fail! "C14-INPUT"
                       "C target compilation requires an explicit executable output"
                       source-path target nil
                       {:missing-fields [:output-path]
                        :remediation "Use --target c -o <executable> for a public C compile."}))
    (let [output-file (java.io.File. output-path)
          parent (.getParentFile output-file)]
      (when (and parent
                 (not (or (.isDirectory parent)
                          (.mkdirs parent))))
        (c-backend-fail! "C14-INPUT"
                         "C target output directory is unavailable"
                         source-path target nil
                         {:output-path output-path
                          :missing-fact :output-directory
                          :remediation "Use a writable target output directory."})))
    (let [c-source-path (str output-path ".c")
          manifest-path (str output-path ".manifest.edn")
          source-map-path (str output-path ".source-map.edn")
          provenance-path (str output-path ".provenance.edn")
          artifact (c-backend-source-artifact
                    source-path source-text
                    (cond-> {:target target
                             :dialect :c11
                             :compile? true
                             :executable-path output-path
                             :c-source-path c-source-path
                             :manifest-path manifest-path
                             :source-map-path source-map-path
                             :provenance-path provenance-path}
                      lowering-mode
                      (assoc :lowering-mode lowering-mode)))]
      (assoc artifact
             :command-boundary
             {:compile-command (cond-> ["gravity" "compile" source-path
                                        "--target" (name target)]
                                 lowering-mode
                                 (into ["--lowering" (name lowering-mode)])
                                 true
                                 (into ["-o" output-path]))
              :run-command [output-path]
              :public-command "gravity"
              :bootstrap-hosted? true
              :clojure-seed-boundary? true
              :self-hosted? false
              :seedless-release? false}
             :target-requested? true
             :target-selection :explicit
             :public-current-source? true
             :lowering-mode lowering-mode
             :lowering-requested? (some? lowering-mode))))))

(defn- p18-t04-run-private-directory!
  [source-path]
  (try
    (c-backend-private-staging-directory! source-path :c)
    (catch clojure.lang.ExceptionInfo ex
      (throw ex))
    (catch Exception ex
      (p18-t04-fail!
       "P18T04002"
       {:source source-path
        :missing-fields [:private-runtime-directory]
        :cause-message (.getMessage ex)
        :missing-fact :private-runtime-directory}))))

(defn- p18-t04-run-delete-private-tree!
  [directory source-path]
  (when directory
    (let [cleanup
          (c-backend-delete-private-staging! directory source-path :c)]
      (assoc cleanup
             :fail-closed? true
             :directory (.toString ^java.nio.file.Path (:path directory))))))