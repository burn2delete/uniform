

(defn b5-document-fail!
  [id source-path subject extra]
  (fail! id
         "B5 JVM backend design document coverage validation failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :b5-jvm-backend-document
                 :stage (or (:stage subject)
                            :b5-jvm-backend-document-coverage)
                 :backend :gravity.backend/jvm
                 :profile (or (:profile subject) :hosted)
                 :classfile-target (or (:classfile-target subject) 65)
                 :artifact-id (:artifact-id subject)
                 :mir-op (or (:mir-op subject) :host-call)
                 :domain-anchor (:domain-anchor subject)
                 :jvm-symbol (or (:jvm-symbol subject)
                                 (b5-document-jvm-symbol id))
                 :missing-type-effect-capability-cleanup-fact
                 (or (:missing-type-effect-capability-cleanup-fact subject)
                     (b5-document-missing-fact id))
                 :selected-adapter-or-rejection
                 (or (:selected-adapter-or-rejection subject)
                     (b5-document-selected-adapter id))
                 :fallback-status (or (:fallback-status subject) :rejected)
                 :remediation "Emit JVM artifacts only from verified hosted backend input with pinned classfile/JVM/module/package policy, nullability and exception maps, explicit reflection/classloading/thread/native-image capabilities, deterministic resource cleanup, profile boundary checks, source maps, and complete JVM manifests."}
                extra)))

(defn b5-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get b5-document-override-diagnostics fail-kind)]
      (b5-document-fail!
       id source-path
       {:stage :b5-jvm-backend-document-coverage
        :artifact-id (str "b5-document-" (name fail-kind))
        :missing-type-effect-capability-cleanup-fact fail-kind
        :jvm-symbol (name fail-kind)}
       {:missing-fields [fail-kind]}))))

(def b5-document-java-source
  (str "package gravity.stage0;\n\n"
       "import java.util.Optional;\n"
       "import java.util.concurrent.Callable;\n\n"
       "public final class Hosted {\n"
       "  private Hosted() {}\n\n"
       "  public static long entry(long x) {\n"
       "    return x;\n"
       "  }\n\n"
       "  public static Optional<String> nullable(String value) {\n"
       "    return Optional.ofNullable(value);\n"
       "  }\n\n"
       "  public static String translateException(Callable<String> thunk) {\n"
       "    try {\n"
       "      return thunk.call();\n"
       "    } catch (Exception ex) {\n"
       "      throw new GravityPanic(ex);\n"
       "    }\n"
       "  }\n\n"
       "  public static Resource openResource() {\n"
       "    return new Resource();\n"
       "  }\n\n"
       "  public static final class GravityPanic extends RuntimeException {\n"
       "    public GravityPanic(Throwable cause) {\n"
       "      super(cause);\n"
       "    }\n"
       "  }\n\n"
       "  public static final class Resource implements AutoCloseable {\n"
       "    private boolean closed;\n\n"
       "    @Override\n"
       "    public void close() {\n"
       "      closed = true;\n"
       "    }\n\n"
       "    public boolean isClosed() {\n"
       "      return closed;\n"
       "    }\n"
       "  }\n"
       "}\n"))

(def b5-document-module-info
  (str "module gravity.stage {\n"
       "  exports gravity.stage0;\n"
       "}\n"))

(defn b5-document-java-structurally-valid?
  [source]
  (and (str/includes? source "package gravity.stage0;")
       (str/includes? source "public final class Hosted")
       (str/includes? source "Optional<String>")
       (str/includes? source "GravityPanic")
       (str/includes? source "implements AutoCloseable")
       (str/includes? source "Callable<String>")))

(defn b5-document-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/b5-jvm-backend-diagnostic-stream
   :stage :b5-jvm-backend-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :b5-jvm-backend-document-coverage
            :backend :gravity.backend/jvm
            :message-key (keyword "backend-jvm" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "b5-document-syntax-" index)
                      :artifact input-id}
            :mir-op (case id
                      "B5-NULL" :host-null-boundary
                      "B5-EXCEPTION" :host-exception-boundary
                      "B5-RESOURCE" :linear-resource-cleanup
                      "B5-THREAD" :host-thread-call
                      :host-call)
            :domain-anchor (when (= id "B5-INTEROP") :java-interop)
            :jvm-symbol (b5-document-jvm-symbol id)
            :classfile-target 65
            :profile :hosted
            :missing-type-effect-capability-cleanup-fact
            (b5-document-missing-fact id)
            :selected-adapter-or-rejection (b5-document-selected-adapter id)
            :fallback-status :rejected
            :facts {:java-null-policy :option-result-checked-or-opaque
                    :host-exception-policy :translate
                    :hidden-dynamic-behavior :rejected}
            :remediation [{:kind :pin-jvm-target-record}
                          {:kind :attach-java-interop-descriptor}
                          {:kind :translate-null-exception-resource-boundary}]
            :redactions []
            :ordering-key [id :b5-jvm-backend-document-coverage
                           :jvm-21]})
         b5-document-diagnostic-ids
         (range))
   :status :complete})