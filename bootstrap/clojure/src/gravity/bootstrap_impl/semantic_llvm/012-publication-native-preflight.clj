(defn- p15-s23-b3-llvm-native-publication-preflight!
  [candidate source-path]
  (p15-s23-b3-llvm-require-authority!
   candidate source-path :native-exclusive-publication-preflight)
  (let [enabled?
        (.isNativeAccessEnabled (.getModule clojure.lang.RT))
        java-version (System/getProperty "java.version")
        feature (.feature (Runtime/version))]
    (when-not (and enabled? (= 26 feature) (= "26.0.1" java-version))
      (p15-s23-b3-llvm-fail!
       "B3-MANIFEST" source-path {}
       {:missing-fact :jdk26-native-access-required-for-exclusive-publication
        :native-access-enabled? enabled?}))
    (let [binding
          (try
            (let [linker (java.lang.foreign.Linker/nativeLinker)
                  optional-symbol
                  (.find (.defaultLookup linker) "renamex_np")]
              (when (.isEmpty optional-symbol)
                (p15-s23-b3-llvm-fail!
                 "B3-MANIFEST" source-path {}
                 {:missing-fact
                  :darwin-libsystem-renamex-np-symbol-required}))
              (let [descriptor
                    (java.lang.foreign.FunctionDescriptor/of
                     java.lang.foreign.ValueLayout/JAVA_INT
                     (into-array
                      java.lang.foreign.MemoryLayout
                      [java.lang.foreign.ValueLayout/ADDRESS
                       java.lang.foreign.ValueLayout/ADDRESS
                       java.lang.foreign.ValueLayout/JAVA_INT]))
                    capture-option
                    (java.lang.foreign.Linker$Option/captureCallState
                     (into-array String ["errno"]))
                    handle
                    (.downcallHandle
                     linker (.get optional-symbol) descriptor
                     (into-array java.lang.foreign.Linker$Option
                                 [capture-option]))
                    state-layout
                    (java.lang.foreign.Linker$Option/captureStateLayout)
                    errno-handle
                    (.toMethodHandle
                     (.varHandle
                      state-layout
                      (into-array
                       java.lang.foreign.MemoryLayout$PathElement
                       [(java.lang.foreign.MemoryLayout$PathElement/groupElement
                         "errno")]))
                     java.lang.invoke.VarHandle$AccessMode/GET)]
                {:linker linker
                 :symbol (.get optional-symbol)
                 :handle handle
                 :state-layout state-layout
                 :errno-handle errno-handle}))
            (catch clojure.lang.ExceptionInfo exception
              (throw exception))
            (catch Exception _ :unavailable))]
      (when (= :unavailable binding)
        (p15-s23-b3-llvm-fail!
         "B3-MANIFEST" source-path {}
         {:missing-fact
          :jdk26-darwin-exclusive-publication-ffi-binding}))
      {:evidence
       {:jdk-version java-version
        :jdk-feature feature
        :native-access-enabled? true
        :ffi-provider :jdk-26-foreign-function-and-memory
        :native-library :darwin-libsystem
        :symbol "renamex_np"
        :errno-read-policy :failure-only
        :guarantee-scope
        #{:exclusive-destination :no-symlink-traversal}
        :path-identity-linearization
        :precommit-file-key-checked-not-fd-relative
        :flags {:rename-excl 4 :rename-nofollow-any 16 :combined 20}}
       :runtime binding})))
