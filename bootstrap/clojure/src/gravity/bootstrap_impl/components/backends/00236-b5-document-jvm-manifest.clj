

(defn b5-document-jvm-manifest
  [input-id]
  (let [source-hash (c4-artifact-id b5-document-java-source)
        module-hash (c4-artifact-id b5-document-module-info)]
    {:artifact :gravity/jvm-backend-manifest
     :backend :gravity.backend/jvm
     :target {:classfile 65
              :runtime :jvm-21
              :module-system :named
              :status :pinned}
     :emits #{:class-files :jar :interop-descriptors :debug-map}
     :requires #{:hosted-profile :exception-map :nullability-map
                 :runtime-provider-manifest}
     :rejects #{:unchecked-null-flow :undeclared-reflection
                :untranslated-host-exception :hidden-classloading}
     :classfile-and-jvm-target-record
     {:classfile 65
      :jvm-version-range [:jvm-21 :jvm-25]
      :module-system :named
      :classloader-policy :single-declared-loader
      :packaging-mode :modular-jar
      :reflection-policy :declared-denied-by-default
      :native-image-constraints :generated-from-dynamic-use
      :status :pinned}
     :class-and-module-model
     {:package "gravity.stage0"
      :module "gravity.stage"
      :generated-classes ["gravity.stage0.Hosted"
                          "gravity.stage0.Hosted$GravityPanic"
                          "gravity.stage0.Hosted$Resource"]
      :symbol-to-method {"gravity/entry" "gravity.stage0.Hosted.entry(J)J"
                         "gravity/nullable" "gravity.stage0.Hosted.nullable(Ljava/lang/String;)Ljava/util/Optional;"}
      :static-initializer :none
      :field-layout []
      :interface-protocol-mapping []
      :closure-representation :static-method-or-runtime-handle
      :generic-signature-strategy :bridge-when-required
      :annotations-emitted []
      :visibility-and-access-flags :public-final
      :classloader-assumptions :single-declared-loader
      :incremental-name-stability :stable
      :status :complete}
     :java-source-files [{:path "gravity/stage0/Hosted.java"
                          :content b5-document-java-source
                          :hash source-hash}]
     :module-descriptors [{:path "module-info.java"
                           :content b5-document-module-info
                           :hash module-hash}]
     :class-files [{:path "gravity/stage0/Hosted.class"
                    :logical-source "gravity/stage0/Hosted.java"
                    :classfile 65
                    :status :requires-proof-command}]
     :jar-or-module-artifact
     {:path "gravity-stage0-jvm.jar"
      :module "gravity.stage"
      :entries ["module-info.class"
                "gravity/stage0/Hosted.class"
                "gravity/stage0/Hosted$GravityPanic.class"
                "gravity/stage0/Hosted$Resource.class"]
      :status :requires-proof-command}
     :value-representation-record
     {:representations [{:gravity-type :I64
                         :jvm-descriptor "J"
                         :nullability :non-null-primitive
                         :boxing :specialized
                         :equality :primitive
                         :mutability :immutable
                         :ownership-rooting :stack}
                        {:gravity-type :String?
                         :jvm-descriptor "Ljava/util/Optional;"
                         :nullability :option-wrapper
                         :boxing :boxed
                         :equality :object-equals
                         :mutability :immutable
                         :ownership-rooting :gc-root}]
      :bridge-methods :when-required
      :status :complete}
     :java-interop-descriptor
     {:descriptors [{:id :gravity.stage0/entry
                     :class "gravity.stage0.Hosted"
                     :method "entry"
                     :jvm-descriptor "(J)J"
                     :generic-signature nil
                     :gravity-type [:fn [:I64] :I64]
                     :nullability {:params [:non-null-primitive]
                                   :return :non-null-primitive}
                     :exception-mapping :none
                     :thread-affinity :none
                     :reflection-required? false
                     :effects #{}
                     :capabilities #{}
                     :taint-policy :not-applicable}
                    {:id :gravity.stage0/translate-exception
                     :class "gravity.stage0.Hosted"
                     :method "translateException"
                     :jvm-descriptor "(Ljava/util/concurrent/Callable;)Ljava/lang/String;"
                     :gravity-type [:fn [:host-callable] :String]
                     :nullability {:params [:nonnull] :return :nonnull}
                     :exception-mapping :gravity-panic
                     :thread-affinity :caller
                     :reflection-required? false
                     :effects #{:error/throw}
                     :capabilities #{}
                     :taint-policy :validated}]
      :status :complete}
     :nullability-and-exception-translation-map
     {:nullability {:java-null-flow :option-result-checked-or-opaque
                    :safe-gravity-null-entry :rejected
                    :wrappers [:Option :Result :opaque-foreign]}
      :exceptions {:java-checked :gravity-error
                   :java-unchecked :gravity-panic
                   :fatal-jvm :runtime-record-when-catchable
                   :catch-all-preserves-source-and-host-identity true}
      :status :complete}
     :reflection-and-dynamic-use-manifest
     {:reflection []
      :dynamic-proxies []
      :method-handles []
      :invokedynamic []
      :service-loaders []
      :serialization-frameworks []
      :native-image-reflection-metadata []
      :policy :declared-denied-by-default
      :status :declared}
     :classloading-policy-record
     {:dynamic-loading :denied
      :hidden-classloading []
      :accepted-loaders [:single-declared-loader]
      :status :complete}
     :native-image-configuration
     {:reflection-config []
      :resource-config []
      :jni-config []
      :proxy-config []
      :dynamic-use-correspondence :exact
      :status :consistent}
     :runtime-helper-manifest
     {:gc-assumptions :jvm-gc
      :scheduler :runtime-provider-declared
      :threads :declared-effect-only
      :monitors :declared-effect-only
      :atomics :varhandle-record-required
      :executor-integration :manifested
      :classloader-lifecycle-hooks :none
      :shutdown-behavior :declared
      :deterministic-cleanup-helpers [:safe5-auto-close]
      :logging-tracing-hooks :declared
      :status :complete}
     :resource-cleanup-record
     {:linear-resources [:resource/file :resource/socket
                        :resource/lock :resource/transaction
                        :resource/native-handle]
      :cleanup-path :safe5-deterministic
      :gc-finalization-only :rejected
      :status :complete}
     :thread-monitor-executor-atomic-effect-record
     {:threads {:creation :requires-effect-and-capability}
      :monitors {:synchronized :requires-effect}
      :executors {:integration :runtime-provider}
      :atomics {:varhandle :requires-ordering-record}
      :blocking {:policy :declared}
      :status :complete}
     :profile-boundary-record
     {:hosted-behavior-exported-to-lower-profiles :rejected
      :lower-profile-contracts [:core :native :firmware :kernel :hardware]
      :status :complete}
     :source-debug-map
     {:source input-id
      :generated-origin-chain [:mir :c14-target-lowering
                               :b1-interface :b5-jvm-backend]
      :generated-files ["module-info.java" "gravity/stage0/Hosted.java"]
      :status :preserved}
     :bootstrap-self-hosting-record
     {:applies? true
      :stage :stage0-hosted-jvm-bootstrap-support
      :replacement-policy :retire-when-gravity-can-compile-subset
      :status :recorded}
     :javac-compilation-record
     {:declared-command "javac --release 21 -d /tmp/gravity-p07-b5-classes /tmp/gravity-p07-b5-src/module-info.java /tmp/gravity-p07-b5-src/gravity/stage0/Hosted.java"
      :proof-artifact "docs/artifacts/phase-07/reports/p07-d102-b5-jvm-backend-report.md"
      :status :requires-proof-command}
     :jar-creation-record
     {:declared-command "jar --create --file /tmp/gravity-p07-b5.jar -C /tmp/gravity-p07-b5-classes ."
      :proof-artifact "docs/artifacts/phase-07/reports/p07-d102-b5-jvm-backend-report.md"
      :status :requires-proof-command}
     :input-artifact input-id
     :status :complete}))