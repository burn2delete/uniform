; Semantic decomposition of committed HEAD reader line 142709.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-jvm-backend-source-artifact-stage
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
     closed-plan-runtime]}
   state
   stage-directory
   (if
    emit?
    (try
     (java.nio.file.Files/createTempDirectory
      (.toPath parent)
      ".gravity-jvm-stage-"
      (make-array java.nio.file.attribute.FileAttribute 0))
     (catch
      Exception
      ex
      (jvm-backend-fail!
       "C14-INPUT"
       "JVM staging directory could not be created"
       source-path
       nil
       {:cause-message (.getMessage ex), :missing-fact :output-staging-directory})))
    (java.nio.file.Files/createTempDirectory
     "gravity-jvm-validate-"
     (make-array java.nio.file.attribute.FileAttribute 0)))
   staged-paths
   (jvm-backend-output-paths (.toString stage-directory))]
  (clojure.core/assoc state :stage-directory stage-directory :staged-paths staged-paths)))
