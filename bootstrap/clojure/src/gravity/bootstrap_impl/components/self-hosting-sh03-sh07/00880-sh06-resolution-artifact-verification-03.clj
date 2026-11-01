(defn-
 sh06-resolution-diagnostic-origin
 "Recover only the existing origin of an exact failed reference.\n  The caller has already authenticated the SH05 parent and request."
 [source-path sh05-artifact request diagnostic]
 (gravity.resolution/diagnostic-origin
  source-path
  sh05-artifact
  request
  diagnostic
  (resolution-stage-operations)))

(defn-
 sh06-resolution-source-artifact-candidate
 "Construct the authentic internal aggregate before its mandatory proof gate.\n\n  This private value is available to bounded diagnostics and tests only.  It is\n  not an accepted artifact and must never cross the public SH-06 boundary\n  without sh06-resolution-capability-based-proof-for-construction."
 ([source-path sh05-artifact legacy?]
  (sh06-resolution-source-artifact-candidate
   source-path
   sh05-artifact
   legacy?
   :gravity.bootstrap/legacy))
 ([source-path sh05-artifact legacy? context]
  (sh06-resolution-source-artifact-candidate
   source-path
   sh05-artifact
   legacy?
   context
   {:artifact :gravity/sh06-module-inputs,
    :schema-version 1,
    :module-artifacts []}))
 ([source-path sh05-artifact legacy? context module-inputs]
  (with-sh06-resolution-transport-bounds
   (let
    [contextual?
     (not= :gravity.bootstrap/legacy context)
     _
     (when
      (and contextual? (nil? context))
      (sh06-resolution-c5-fail! source-path :missing-context {}))
     prepared
     (when
      (and (not contextual?) (resolution-construction-eligible?))
      (resolution-construction-inputs
       source-path
       sh05-artifact
       legacy?))
     _
     (when-not
      prepared
      (sh06-authenticated-sh05-parent! source-path sh05-artifact))
     c2-artifact
     (get-in
      sh05-artifact
      [:gravity-macro-boundary
       :authenticated-sh04-artifact
       :c2-reader-artifact])
     source-before
     (if
      prepared
      (:source-before prepared)
      (if
       contextual?
       (sh06-authenticated-source-snapshot!
        source-path
        c2-artifact
        :source-authentication-before-c5
        legacy?
        context)
       (sh06-authenticated-source-snapshot!
        source-path
        c2-artifact
        :source-authentication-before-c5
        legacy?)))
     request
     (if
      prepared
      (resolution-module-request-from-base
       source-path
       sh05-artifact
       module-inputs
       (:base prepared))
      (if
       contextual?
       (sh06-resolution-request source-path sh05-artifact context)
       (sh06-resolution-module-request
        source-path
        sh05-artifact
        module-inputs)))
     binding
     (if
      contextual?
      (sh06-resolution-context-plan-binding! source-path context)
      (sh06-resolution-current-binding! source-path))
     run
     (try
      (sh06-resolution-run-request! source-path binding request)
      (catch
       clojure.lang.ExceptionInfo
       error
       (if-let
        [origin
         (sh06-resolution-diagnostic-origin
          source-path
          sh05-artifact
          request
          (ex-data error))]
        (throw
         (ex-info
          (.getMessage error)
          (assoc (ex-data error) :origin-chain origin)
          error))
        (throw error))))
     assembled
     (gravity.resolution/assemble-candidate
      source-path
      sh05-artifact
      contextual?
      module-inputs
      request
      binding
      run
      (resolution-stage-operations))
     artifact-with-id
     (:artifact assembled)
     descriptor
     (:descriptor assembled)
     envelope
     (p15-s23-stage2-sh02-descriptor-envelope
      sh06-resolution-envelope-stage
      sh06-resolution-sealed-artifact-kind
      descriptor
      source-path
      (sh06-resolution-envelope-source-options binding))
     _
     (p15-s23-stage2-sh02-descriptor-envelope-verify!
      envelope
      sh06-resolution-envelope-stage
      sh06-resolution-sealed-artifact-kind
      descriptor
      source-path
      (sh06-resolution-envelope-source-options binding))
     artifact-with-envelope
     (->
      artifact-with-id
      (assoc-in
       [:gravity-resolution-boundary
        :authenticated-envelope-descriptor]
       descriptor)
      (assoc-in
       [:gravity-resolution-boundary :authenticated-envelope]
       envelope))]
    (if
     contextual?
     (sh06-source-snapshot-stable!
      source-path
      source-before
      c2-artifact
      legacy?
      context)
     (sh06-source-snapshot-stable!
      source-path
      source-before
      c2-artifact
      legacy?))
    artifact-with-envelope))))
