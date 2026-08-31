; Semantic decomposition of committed HEAD reader line 142709.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-jvm-backend-source-artifact-publish
 [state]
 (clojure.core/let
  [{:keys
    [source-path
     source-text
     output-path
     emit?
     output
     parent
     javac-version
     java-version
     packet
     trusted-emitter-rule
     trusted-driver-rule
     trusted-runtime-rule
     trusted-plan
     plan
     writes-stdout?
     java-source
     module-source
     source-map
     plan-hash
     source-hash
     compiler-record
     closed-plan-runtime
     stage-directory
     staged-paths
     class-path
     module-class-path
     class-major
     jar-record
     expected-entries
     execution
     expected-output
     expected-bytes
     source-map-text
     content-hashes
     expected-input
     expected-effects
     expected-capabilities
     manifest-input
     manifest-hash
     manifest
     validation-context
     provenance-input
     provenance-hash
     final-paths
     provenance
     identity
     artifact]}
   state]
  (when
   emit?
   (try
    (java.nio.file.Files/move
     stage-directory
     (.toPath output)
     (into-array java.nio.file.CopyOption [java.nio.file.StandardCopyOption/ATOMIC_MOVE]))
    (catch
     Exception
     ex
     (jvm-backend-fail!
      "B5-MANIFEST"
      "JVM artifact directory could not be atomically committed"
      source-path
      nil
      {:cause-message (.getMessage ex), :missing-fact :atomic-artifact-directory-commit}))))
  artifact))
