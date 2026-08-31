; Semantic decomposition of committed HEAD reader line 140134.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/let
 [semantic-late-c-backend-source-artifact-packet
  semantic-late-c-backend-source-artifact-packet
  semantic-late-c-backend-source-artifact-runtime
  semantic-late-c-backend-source-artifact-runtime
  semantic-late-c-backend-source-artifact-lower
  semantic-late-c-backend-source-artifact-lower
  semantic-late-c-backend-source-artifact-source-map
  semantic-late-c-backend-source-artifact-source-map
  semantic-late-c-backend-source-artifact-manifest
  semantic-late-c-backend-source-artifact-manifest
  semantic-late-c-backend-source-artifact-provenance
  semantic-late-c-backend-source-artifact-provenance
  semantic-late-c-backend-source-artifact-identity
  semantic-late-c-backend-source-artifact-identity
  semantic-late-c-backend-source-artifact-artifact-base
  semantic-late-c-backend-source-artifact-artifact-base
  semantic-late-c-backend-source-artifact-publish-records
  semantic-late-c-backend-source-artifact-publish-records]
 (defn
  c-backend-source-artifact
  "Lower a source unit through the genuine stage0 plan into a C artifact.\n\n  `:compile?` is intentionally opt-in.  Without it this function performs no\n  host compiler invocation and only returns deterministic lowering artifacts.\n  `:emit-dir` writes the C source and three EDN sidecars when requested."
  ([source-path source-text] (c-backend-source-artifact source-path source-text {}))
  ([source-path
    source-text
    {:keys
     [target
      dialect
      emit-dir
      compile?
      lowering-mode
      runtime-derived?
      executable-path
      c-source-path
      manifest-path
      source-map-path
      provenance-path],
     :or {target :c-hosted, dialect :c11, compile? false, lowering-mode :verified-stage0-output}}]
   (let
    [target
     (cond
      (keyword? target)
      target
      (string? target)
      (keyword (str/lower-case target))
      :else
      target)
     dialect
     (cond
      (keyword? dialect)
      dialect
      (string? dialect)
      (keyword (str/lower-case dialect))
      :else
      dialect)]
    (when-not
     (contains? c-backend-supported-dialects dialect)
     (c-backend-fail!
      "C14-TARGET"
      "C backend dialect is unsupported"
      source-path
      target
      nil
      {:dialect dialect,
       :supported-dialects (vec (sort c-backend-supported-dialects)),
       :missing-fact :c-dialect}))
    (clojure.core/let
     [state-0
      {:lowering-mode lowering-mode,
       :executable-path executable-path,
       :provenance-path provenance-path,
       :manifest-path manifest-path,
       :source-map-path source-map-path,
       :source-text source-text,
       :source-path source-path,
       :c-source-path c-source-path,
       :emit-dir emit-dir,
       :target target,
       :compile? compile?,
       :runtime-derived? runtime-derived?,
       :dialect dialect}
      state-1
      (semantic-late-c-backend-source-artifact-packet state-0)
      state-2
      (semantic-late-c-backend-source-artifact-runtime state-1)
      state-3
      (semantic-late-c-backend-source-artifact-lower state-2)
      state-4
      (semantic-late-c-backend-source-artifact-source-map state-3)
      state-5
      (semantic-late-c-backend-source-artifact-manifest state-4)
      state-6
      (semantic-late-c-backend-source-artifact-provenance state-5)
      state-7
      (semantic-late-c-backend-source-artifact-identity state-6)
      state-8
      (semantic-late-c-backend-source-artifact-artifact-base state-7)
      state-9
      (semantic-late-c-backend-source-artifact-publish-records state-8)]
     (clojure.core/let
      [{:keys
        [source-path
         source-text
         target
         dialect
         emit-dir
         compile?
         lowering-mode
         runtime-derived?
         executable-path
         c-source-path
         manifest-path
         source-map-path
         provenance-path
         shared-packet
         macro-artifact
         module
         stage2-rule
         stage2-compiler-artifact-record
         stage2-compiler-artifact-source-path
         stage2-runtime-rule
         stage2-driver-rule
         plan
         stage2-driver-run
         stage2-runtime-execution
         closed-plan-validation
         closed-plan-execution
         closed-plan-invocation
         closed-plan-target-record
         clojure-stage0-output
         stdout
         c-source
         source-hash
         plan-input
         plan-hash
         stage2-runtime-execution-record
         c-source-hash
         output-hash
         source-map
         source-map-hash
         manifest-input
         manifest-hash
         provenance
         provenance-hash
         identity-input
         artifact-base
         artifact]}
       state-9]
      (c-backend-validate-output-paths!
       source-path
       target
       [[:emit-dir emit-dir]
        [:c-source c-source-path]
        [:executable executable-path]
        [:manifest manifest-path]
        [:source-map source-map-path]
        [:provenance provenance-path]])
      (when emit-dir (p18-ensure-dir! emit-dir))
      (when c-source-path (spit c-source-path c-source))
      (when manifest-path (spit manifest-path (pr-str (:manifest artifact))))
      (when source-map-path (spit source-map-path (pr-str (:source-map artifact))))
      (when provenance-path (spit provenance-path (pr-str (:provenance artifact))))
      (let
       [compile-result
        (when
         compile?
         (when-not
          (and c-source-path executable-path)
          (c-backend-fail!
           "C14-INPUT"
           "C backend compilation requires explicit output paths"
           source-path
           target
           nil
           {:missing-fields [:c-source-path :executable-path]}))
         (c-backend-run-cc! c-source-path executable-path source-path target runtime-derived?))]
       (when
        (and
         runtime-derived?
         compile?
         (not= (:runtime-out compile-result) (:stdout stage2-runtime-execution)))
        (p15-s23-stage2-runtime-executor-fail!
         "P15S23X003"
         source-path
         compile-result
         {:requested-source source-path,
          :target target,
          :runtime-engine (:runtime-engine stage2-runtime-rule),
          :runtime-rule-hash (:runtime-rule-hash stage2-runtime-rule),
          :stage2-runtime-output (:stdout stage2-runtime-execution),
          :c-executable-output (:runtime-out compile-result),
          :p15-diagnostic "P15S23X003",
          :missing-fact :c-runtime-execution-equivalence}))
       (cond->
        (assoc
         artifact
         :emitted-files
         (cond->
          {}
          c-source-path
          (assoc :c-source c-source-path)
          manifest-path
          (assoc :manifest manifest-path)
          source-map-path
          (assoc :source-map source-map-path)
          provenance-path
          (assoc :provenance provenance-path))
         :compile-requested?
         (boolean compile?)
         :executable-path
         executable-path)
        compile?
        (assoc :compiled-executable? true)
        (and compile? runtime-derived?)
        (assoc :compiled-execution-output (:runtime-out compile-result))))))))))
