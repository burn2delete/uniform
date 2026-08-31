; Semantic decomposition of committed HEAD reader line 142709.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/let
 [semantic-late-jvm-backend-source-artifact-output-tools
  semantic-late-jvm-backend-source-artifact-output-tools
  semantic-late-jvm-backend-source-artifact-packet
  semantic-late-jvm-backend-source-artifact-packet
  semantic-late-jvm-backend-source-artifact-lower
  semantic-late-jvm-backend-source-artifact-lower
  semantic-late-jvm-backend-source-artifact-stage
  semantic-late-jvm-backend-source-artifact-stage
  semantic-late-jvm-backend-source-artifact-compile
  semantic-late-jvm-backend-source-artifact-compile
  semantic-late-jvm-backend-source-artifact-class-jar
  semantic-late-jvm-backend-source-artifact-class-jar
  semantic-late-jvm-backend-source-artifact-execution-parity
  semantic-late-jvm-backend-source-artifact-execution-parity
  semantic-late-jvm-backend-source-artifact-source-input
  semantic-late-jvm-backend-source-artifact-source-input
  semantic-late-jvm-backend-source-artifact-manifest
  semantic-late-jvm-backend-source-artifact-manifest
  semantic-late-jvm-backend-source-artifact-provenance
  semantic-late-jvm-backend-source-artifact-provenance
  semantic-late-jvm-backend-source-artifact-artifact
  semantic-late-jvm-backend-source-artifact-artifact
  semantic-late-jvm-backend-source-artifact-publish
  semantic-late-jvm-backend-source-artifact-publish]
 (defn
  jvm-backend-source-artifact
  ([source-path source-text] (jvm-backend-source-artifact source-path source-text {}))
  ([source-path source-text {:keys [output-path emit?], :or {emit? false}}]
   (when
    (and emit? (str/blank? (str output-path)))
    (jvm-backend-fail!
     "C14-INPUT"
     "JVM target compilation requires an output directory"
     source-path
     nil
     {:missing-fields [:output-path]}))
   (clojure.core/let
    [state-0
     {:source-path source-path, :source-text source-text, :output-path output-path, :emit? emit?}
     state-1
     (semantic-late-jvm-backend-source-artifact-output-tools state-0)
     state-2
     (semantic-late-jvm-backend-source-artifact-packet state-1)
     state-3
     (semantic-late-jvm-backend-source-artifact-lower state-2)
     state-4
     (semantic-late-jvm-backend-source-artifact-stage state-3)]
    (try
     (clojure.core/let
      [action-state-0
       (semantic-late-jvm-backend-source-artifact-compile state-4)
       action-state-1
       (semantic-late-jvm-backend-source-artifact-class-jar action-state-0)
       action-state-2
       (semantic-late-jvm-backend-source-artifact-execution-parity action-state-1)
       action-state-3
       (semantic-late-jvm-backend-source-artifact-source-input action-state-2)
       action-state-4
       (semantic-late-jvm-backend-source-artifact-manifest action-state-3)
       action-state-5
       (semantic-late-jvm-backend-source-artifact-provenance action-state-4)
       action-state-6
       (semantic-late-jvm-backend-source-artifact-artifact action-state-5)]
      (semantic-late-jvm-backend-source-artifact-publish action-state-6))
     (catch clojure.lang.ExceptionInfo ex (throw ex))
     (catch
      Exception
      ex
      (jvm-backend-fail!
       "B5-MANIFEST"
       "JVM artifact construction failed closed"
       source-path
       nil
       {:cause-message (.getMessage ex), :missing-fact :transactional-jvm-artifact}))
     (finally
      (let [stage-directory (:stage-directory state-4)]
       (when (.exists (.toFile stage-directory))
        (js-ts-backend-delete-tree! stage-directory)))))))))
