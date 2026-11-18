; Semantic decomposition of committed HEAD reader line 142709.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-jvm-backend-source-artifact-packet
 [state]
 (clojure.core/let
  [{:keys [source-path source-text output-path emit? output parent javac-version java-version current-runtime-development?]}
   state
   candidate-packet
   (stage2-runtime-derived-packet
    source-path
    source-text
    jvm-backend-target
    {:validate-plan!
     (fn [candidate-plan] (jvm-backend-validate-plan! source-path candidate-plan))
     :current-runtime-development? (true? current-runtime-development?)})
   current-runtime-development?
   (c-backend-current-runtime-development-packet? candidate-packet)
   packet-context
   (when current-runtime-development?
     {:source-path source-path
      :source-text source-text
      :source-content-hash (str "sha256:" (sha256-hex source-text))
      :requested-target jvm-backend-target})
   current-runtime-development-authenticated-reference
   (when current-runtime-development?
     (or (current-runtime-development-packet-authenticated-reference
          candidate-packet packet-context)
         (jvm-backend-fail!
          "C14-INPUT"
          "JVM backend received an unauthenticated stage2 packet"
          source-path candidate-packet
          {:missing-fact :authenticated-target-neutral-stage2-packet})))
   packet
   (if current-runtime-development?
     current-runtime-development-authenticated-reference
     candidate-packet)
   trusted-emitter-rule
   (if current-runtime-development?
     (:stage2-plan-emitter-rule packet)
     (c-backend-stage2-plan-emitter-source-rule!
      source-path jvm-backend-target))
   trusted-driver-rule
   (when-not current-runtime-development?
     (c-backend-stage2-compiler-driver-source-rule!
      source-path jvm-backend-target))
   trusted-runtime-rule
   (if current-runtime-development?
     (:stage2-runtime-rule packet)
     (c-backend-stage2-runtime-source-rule! source-path jvm-backend-target))
   trusted-plan
   (if current-runtime-development?
     (:plan packet)
     (p15-s23-stage2-plan-emitter-compile-source
      (:emitter trusted-emitter-rule)
      source-path
      source-text))
   _
   (if current-runtime-development?
     (jvm-backend-validate-current-runtime-development-packet!
      source-path candidate-packet current-runtime-development-authenticated-reference
      trusted-plan)
     (jvm-backend-validate-packet!
      source-path packet trusted-emitter-rule trusted-driver-rule
      trusted-runtime-rule trusted-plan
      (p15-s23-closed-runtime-packet-context
       source-path source-text jvm-backend-target)))
   plan
   (:plan packet)
   _
   (when-not
    (= :hosted (get-in plan [:module :profile]))
    (jvm-backend-fail!
     "C14-PROFILE"
     "JVM target requires the hosted profile"
     source-path
     plan
     {:observed-profile (get-in plan [:module :profile]), :missing-fact :hosted-jvm-profile}))
   _
   (when-not
    (= :safe (get-in plan [:module :safety]))
    (jvm-backend-fail!
     "C14-PROFILE"
     "bounded JVM target does not lower unsafe modules"
     source-path
     plan
     {:observed-safety-mode (get-in plan [:module :safety]), :missing-fact :safe-jvm-module}))
   _
   (jvm-backend-validate-plan! source-path plan)
   writes-stdout?
   (pos? (get-in plan [:instruction-summary :println] 0))
   _
   (when
    (and
     writes-stdout?
     (not
      (and
       (contains? (get-in plan [:effect-summary :declared] #{}) :io/write)
       (contains? (get-in plan [:effect-summary :inferred] #{}) :io/write)
       (contains? (set (get-in plan [:module :capabilities])) :io/stdout))))
    (jvm-backend-fail!
     "B5-INTEROP"
     "JVM stdout lowering lacks effect or capability authority"
     source-path
     plan
     {:missing-fact :stdout-effect-capability-adapter}))]
   (cond->
    (clojure.core/assoc
     state
     :packet packet
     :trusted-emitter-rule trusted-emitter-rule
     :trusted-driver-rule trusted-driver-rule
     :trusted-runtime-rule trusted-runtime-rule
     :trusted-plan trusted-plan
     :plan plan
     :writes-stdout? writes-stdout?)
     current-runtime-development?
     (assoc :current-runtime-development-authenticated-reference
            current-runtime-development-authenticated-reference))))
