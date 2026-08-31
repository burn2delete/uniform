; Semantic decomposition of HEAD reader line 24702.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-hosted-lowering-jvm-backend
 [source-path state]
 (let
  [{:keys [jvm-manifest jvm-class]} state]
  (assoc
   {}
   :jvm-backend
   {:classfile-target-record
    {:classfile 65,
     :runtime :jvm-21,
     :module-system :named,
     :status :pinned},
    :nullability-map
    {:entries
     [{:symbol "entry", :params [:nonnull], :returns :nonnull}],
     :status :checked},
    :jar-artifact
    {:path "gravity-stage0-hosted.jar",
     :entries ["gravity/stage0/Hosted.class"],
     :hash (:content-hash jvm-manifest),
     :status :complete},
    :class-files
    [{:path "gravity/stage0/Hosted.class",
      :logical-content jvm-class,
      :hash (:content-hash jvm-manifest)}],
    :java-interop-descriptor
    {:symbols
     [{:class "gravity.stage0.Hosted",
       :method "entry",
       :descriptor "(J)J",
       :effects #{},
       :capabilities #{},
       :exceptions []}],
     :status :complete},
    :status :complete,
    :reflection-dynamic-use-manifest
    {:reflection [],
     :method-handles [],
     :classloading :denied,
     :status :declared},
    :native-image-configuration
    {:reflection-config [], :resource-config [], :status :consistent},
    :artifact :gravity/jvm-backend-manifest,
    :backend :gravity.backend/jvm,
    :exception-translation-map
    {:host-exceptions [],
     :panic-mapping :gravity/panic,
     :status :translated},
    :runtime-helper-manifest
    {:gc :jvm,
     :linear-cleanup :deterministic,
     :thread-provider :declared,
     :status :complete}})))
