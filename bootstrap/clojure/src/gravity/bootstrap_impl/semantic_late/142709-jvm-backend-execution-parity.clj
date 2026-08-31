; Semantic decomposition of committed HEAD reader line 142709.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-jvm-backend-source-artifact-execution-parity
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
     expected-entries]}
   state
   execution
   (jvm-backend-run-process!
    [*jvm-backend-java-command* "-jar" (:jar staged-paths)]
    source-path
    "B14-DIFFERENTIAL"
    "generated JVM artifact execution failed")
   expected-output
   (get-in packet [:stage2-runtime-execution-record :stdout])
   expected-bytes
   (c-backend-runtime-bytes expected-output)
   _
   (when-not
    (= expected-bytes (:stdout-bytes execution))
    (jvm-backend-fail!
     "B14-DIFFERENTIAL"
     "JVM execution differs from the authoritative stage2 runtime"
     source-path
     nil
     {:expected-stdout-hash (str "sha256:" (sha256-hex expected-output)),
      :actual-stdout-hash
      (str "sha256:" (sha256-bytes-hex (byte-array (map byte (:stdout-bytes execution))))),
      :missing-fact :stage2-jvm-execution-equivalence}))]
  (clojure.core/assoc
   state
   :execution
   execution
   :expected-output
   expected-output
   :expected-bytes
   expected-bytes)))
