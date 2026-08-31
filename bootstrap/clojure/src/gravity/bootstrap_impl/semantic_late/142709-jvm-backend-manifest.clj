; Semantic decomposition of committed HEAD reader line 142709.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-jvm-backend-source-artifact-manifest
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
     expected-capabilities]}
   state
   manifest-input
   {:release-grade? false,
    :capabilities expected-capabilities,
    :diagnostics [],
    :closed-plan-runtime closed-plan-runtime,
    :conformance
    {:jar-entries :passed,
     :b5-conforming? false,
     :source-map-coverage :source-unit-only,
     :stdout-byte-exact? true,
     :stage2-differential :passed,
     :verified-mir-input? false,
     :per-form-origin-preserved? false,
     :main-class :passed,
     :source-map :partial,
     :classfile-major :passed},
    :managed-runtime
    {:family :managed,
     :host :jvm-21,
     :delegated #{:classloading :startup :gc},
     :generated #{:byte-array-values :entrypoint},
     :linked #{},
     :forbidden #{:monitors :native-access :threads :dynamic-loading :reflection}},
    :emits [:java-sources :class-files :modular-executable-jar :source-map :manifest :provenance],
    :schema-version 1,
    :content-hashes content-hashes,
    :toolchain
    {:target-release 21,
     :encoding :utf8,
     :debug [:source :lines],
     :annotation-processing :disabled},
    :self-hosted? false,
    :module
    {:name jvm-backend-module-name,
     :main-class jvm-backend-main-class,
     :side-effects writes-stdout?},
    :host-boundaries
    (if
     writes-stdout?
     [{:class "java.lang.System",
       :member "out",
       :operation :write-byte-array,
       :effect :io/write,
       :capability :io/stdout,
       :representation :byte-array}]
     []),
    :effects expected-effects,
    :safety {:mode :safe, :unsafe-islands [], :status :preserved},
    :artifact :gravity/jvm-backend-manifest,
    :input expected-input,
    :target
    {:classfile 65, :runtime :jvm-21, :module-system :named, :packaging :modular-executable-jar},
    :backend :gravity.backend/jvm,
    :profile :hosted,
    :clojure-seed-boundary? true}
   manifest-hash
   (str
    "sha256:"
    (sha256-hex
     (pr-str
      (c-backend-canonical-value
       (update
        manifest-input
        :closed-plan-runtime
        p15-s23-closed-runtime-target-semantic-record)))))
   manifest
   (assoc manifest-input :manifest-hash manifest-hash)
   validation-context
   {:input expected-input,
    :effects expected-effects,
    :capabilities expected-capabilities,
    :content-hashes content-hashes,
    :closed-runtime-context (p15-s23-closed-runtime-target-context packet)}
   _
   (jvm-backend-validate-manifest! source-path manifest validation-context)
   _
   (jvm-backend-validate-content-hashes! source-path manifest staged-paths)]
  (clojure.core/assoc
   state
   :manifest-input
   manifest-input
   :manifest-hash
   manifest-hash
   :manifest
   manifest
   :validation-context
   validation-context)))
