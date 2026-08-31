; Semantic decomposition of committed HEAD reader line 142709.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-jvm-backend-source-artifact-provenance
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
     validation-context]}
   state
   provenance-input
   {:source-content-hash source-hash,
    :seed-boundary {:clojure-seed-boundary? true, :self-hosted? false},
    :closed-plan-runtime closed-plan-runtime,
    :schema-version 1,
    :compiler-driver-rule-hash (get-in packet [:stage2-compiler-driver-rule :driver-rule-hash]),
    :runtime-rule-hash (get-in packet [:stage2-runtime-rule :runtime-rule-hash]),
    :stage2-plan-hash plan-hash,
    :manifest-hash manifest-hash,
    :stage2-expression-lowering-artifact (dissoc compiler-record :source-path),
    :pass-history
    [:c2-reader
     :stage2-source-front-end
     :stage2-plan-emitter
     :stage2-compiler-driver
     :stage2-runtime-executor
     :jvm-lowering
     :javac-21
     :deterministic-jar],
    :artifact :gravity/jvm-provenance,
    :stage2-plan-emitter-source-rule-hash
    (get-in packet [:stage2-plan-emitter-rule :source-rule-hash]),
    :backend :gravity.backend/jvm,
    :target-eligibility (:target-eligibility packet)}
   provenance-hash
   (str
    "sha256:"
    (sha256-hex
     (pr-str
      (c-backend-canonical-value
       (update
        provenance-input
        :closed-plan-runtime
        p15-s23-closed-runtime-target-semantic-record)))))
   final-paths
   (when emit? (jvm-backend-output-paths (str output-path)))
   provenance
   (assoc
    provenance-input
    :provenance-hash
    provenance-hash
    :actual-paths
    {:source source-path,
     :outputs final-paths,
     :stage2-expression-lowering-source
     (get-in packet [:provenance :actual-paths :stage2-expression-lowering-source]),
     :stage2-runtime-artifact-source
     (get-in packet [:provenance :actual-paths :stage2-runtime-artifact-source]),
     :validation-toolchain {:javac javac-version, :java java-version}})
   _
   (jvm-backend-write-file! (.toPath (java.io.File. (:manifest staged-paths))) (pr-str manifest))
   _
   (jvm-backend-write-file!
    (.toPath (java.io.File. (:provenance staged-paths)))
    (pr-str provenance))]
  (clojure.core/assoc
   state
   :provenance-input
   provenance-input
   :provenance-hash
   provenance-hash
   :final-paths
   final-paths
   :provenance
   provenance)))
