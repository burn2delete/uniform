; Semantic decomposition of committed HEAD reader line 140134.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-c-backend-source-artifact-publish-records
 [state]
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
     artifact-base]}
   state
   artifact
   (assoc artifact-base :artifact-id (c4-artifact-id (c-backend-canonical-value identity-input)))
   emit-dir
   (when emit-dir (str emit-dir))
   c-source-path
   (or c-source-path (when emit-dir (str emit-dir "/program.c")))
   executable-path
   (or executable-path (when emit-dir (str emit-dir "/program")))
   manifest-path
   (or manifest-path (when emit-dir (str emit-dir "/manifest.edn")))
   source-map-path
   (or source-map-path (when emit-dir (str emit-dir "/source-map.edn")))
   provenance-path
   (or provenance-path (when emit-dir (str emit-dir "/provenance.edn")))]
  (clojure.core/assoc
   state
   :artifact
   artifact
   :emit-dir
   emit-dir
   :c-source-path
   c-source-path
   :executable-path
   executable-path
   :manifest-path
   manifest-path
   :source-map-path
   source-map-path
   :provenance-path
   provenance-path)))
