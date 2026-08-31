; Semantic decomposition of committed HEAD reader line 142709.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-jvm-backend-source-artifact-lower
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
     writes-stdout?]}
   state
   java-source
   (jvm-backend-java-source plan)
   module-source
   jvm-backend-module-source
   source-map
   (jvm-backend-source-map source-text java-source)
   plan-hash
   (c4-artifact-id
    (c-backend-canonical-value
     (select-keys plan [:kind :entrypoint :functions :instruction-summary :effect-summary])))
   source-hash
   (str "sha256:" (sha256-hex source-text))
   compiler-record
   (:stage2-compiler-artifact-record packet)
   closed-plan-runtime
   (p15-s23-closed-runtime-target-record packet)
   _
   (when-not
    (p15-s23-closed-runtime-target-record-authentic?
     closed-plan-runtime
     (p15-s23-closed-runtime-target-context packet))
    (jvm-backend-fail!
     "C14-INPUT"
     "JVM backend received an unauthenticated closed runtime target record"
     source-path
     closed-plan-runtime
     {:missing-fact :authenticated-closed-runtime-target-record}))]
  (clojure.core/assoc
   state
   :java-source
   java-source
   :module-source
   module-source
   :source-map
   source-map
   :plan-hash
   plan-hash
   :source-hash
   source-hash
   :compiler-record
   compiler-record
   :closed-plan-runtime
   closed-plan-runtime)))
