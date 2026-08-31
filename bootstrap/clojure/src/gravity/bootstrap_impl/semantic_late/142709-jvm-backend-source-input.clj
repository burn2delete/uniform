; Semantic decomposition of committed HEAD reader line 142709.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-jvm-backend-source-artifact-source-input
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
     expected-bytes]}
   state
   source-map-text
   (pr-str (c-backend-canonical-value source-map))
   _
   (jvm-backend-write-file! (.toPath (java.io.File. (:source-map staged-paths))) source-map-text)
   content-hashes
   (into
    {}
    (map
     (fn
      [[kind path]]
      [kind
       (str
        "sha256:"
        (sha256-bytes-hex (java.nio.file.Files/readAllBytes (.toPath (java.io.File. path)))))]))
    (select-keys
     staged-paths
     [:java-source :module-source :class-file :module-class :jar :source-map]))
   expected-input
   {:source-content-hash source-hash,
    :plan-assembly-invoked? (:plan-assembly-invoked? compiler-record),
    :source-declared-target (get-in packet [:target-eligibility :source-declared-target]),
    :plan-assembly-artifact-hash (:plan-assembly-artifact-hash compiler-record),
    :expression-lowering-artifact-hash (:artifact-hash compiler-record),
    :compiler-driver-rule-hash (get-in packet [:stage2-compiler-driver-rule :driver-rule-hash]),
    :runtime-rule-hash (get-in packet [:stage2-runtime-rule :runtime-rule-hash]),
    :expression-lowering-semantic-hash (:semantic-hash compiler-record),
    :expression-lowering-generic-bridge-residual? (:generic-bridge-residual? compiler-record),
    :plan-assembly-generic-bridge-residual?
    (:plan-assembly-generic-bridge-residual? compiler-record),
    :instruction-summary (:instruction-summary plan),
    :plan-emitter-source-rule-hash (get-in packet [:stage2-plan-emitter-rule :source-rule-hash]),
    :stage2-plan-hash plan-hash,
    :requested-backend-target :jvm,
    :plan-assembly-function (:plan-assembly-function compiler-record),
    :expression-lowering-invoked? (:invoked? compiler-record),
    :runtime-artifact-hash (get-in packet [:stage2-runtime-rule :runtime-artifact-hash]),
    :expression-lowering-source-content-hash (:source-content-hash compiler-record),
    :plan-assembly-semantic-hash (:plan-assembly-semantic-hash compiler-record),
    :plan-assembly-source-content-hash (:plan-assembly-source-content-hash compiler-record),
    :target-eligibility (:target-eligibility packet)}
   expected-effects
   (:effect-summary plan)
   expected-capabilities
   (get-in plan [:module :capabilities])]
  (clojure.core/assoc
   state
   :source-map-text
   source-map-text
   :content-hashes
   content-hashes
   :expected-input
   expected-input
   :expected-effects
   expected-effects
   :expected-capabilities
   expected-capabilities)))
