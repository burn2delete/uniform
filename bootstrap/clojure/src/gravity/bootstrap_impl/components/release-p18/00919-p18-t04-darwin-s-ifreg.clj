
(def ^:private p18-t04-darwin-s-ifreg 0x8000)

(defn- p18-t04-verified-mir-c-source-native-binding!
  [source-path]
  (when-not
   (and (= 26 (.feature (Runtime/version)))
        (= "Mac OS X" (System/getProperty "os.name"))
        (= "aarch64" (System/getProperty "os.arch")))
    (p18-t04-verified-mir-c-source-snapshot-fail!
     source-path :descriptor-bound-source-snapshot
     :jdk26-darwin-arm64-source-snapshot-provider-required {}))
  (try
    (let [linker (java.lang.foreign.Linker/nativeLinker)
          lookup (.defaultLookup linker)
          capture
          (java.lang.foreign.Linker$Option/captureCallState
           (into-array String ["errno"]))
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
           java.lang.invoke.VarHandle$AccessMode/GET)
          symbol
          (fn [name]
            (let [candidate (.find lookup name)]
              (when (.isEmpty candidate)
                (p18-t04-verified-mir-c-source-snapshot-fail!
                 source-path :descriptor-bound-source-snapshot
                 :missing-darwin-source-snapshot-symbol
                 {:missing-fields [(keyword name)]}))
              (.get candidate)))
          bind
          (fn [name return-layout argument-layouts & linker-options]
            (.downcallHandle
             linker (symbol name)
             (java.lang.foreign.FunctionDescriptor/of
              return-layout
              (into-array java.lang.foreign.MemoryLayout argument-layouts))
             (into-array
              java.lang.foreign.Linker$Option
              (into [capture] linker-options))))]
      {:state-layout state-layout
       :errno-handle errno-handle
       :fstatat
       (bind "fstatat" java.lang.foreign.ValueLayout/JAVA_INT
             [java.lang.foreign.ValueLayout/JAVA_INT
              java.lang.foreign.ValueLayout/ADDRESS
              java.lang.foreign.ValueLayout/ADDRESS
              java.lang.foreign.ValueLayout/JAVA_INT])
       :open
       (bind "open" java.lang.foreign.ValueLayout/JAVA_INT
             [java.lang.foreign.ValueLayout/ADDRESS
              java.lang.foreign.ValueLayout/JAVA_INT
              java.lang.foreign.ValueLayout/JAVA_INT]
             (java.lang.foreign.Linker$Option/firstVariadicArg 2))
       :fstat
       (bind "fstat" java.lang.foreign.ValueLayout/JAVA_INT
             [java.lang.foreign.ValueLayout/JAVA_INT
              java.lang.foreign.ValueLayout/ADDRESS])
       :fcntl
       (bind "fcntl" java.lang.foreign.ValueLayout/JAVA_INT
             [java.lang.foreign.ValueLayout/JAVA_INT
              java.lang.foreign.ValueLayout/JAVA_INT
              java.lang.foreign.ValueLayout/ADDRESS]
             (java.lang.foreign.Linker$Option/firstVariadicArg 2))
       :read
       (bind "read" java.lang.foreign.ValueLayout/JAVA_LONG
             [java.lang.foreign.ValueLayout/JAVA_INT
              java.lang.foreign.ValueLayout/ADDRESS
              java.lang.foreign.ValueLayout/JAVA_LONG])
       :close
       (bind "close" java.lang.foreign.ValueLayout/JAVA_INT
             [java.lang.foreign.ValueLayout/JAVA_INT])})
    (catch clojure.lang.ExceptionInfo exception
      (throw exception))
    (catch Exception _
      (p18-t04-verified-mir-c-source-snapshot-fail!
       source-path :descriptor-bound-source-snapshot
       :jdk26-darwin-source-snapshot-ffi-binding-unavailable {}))))

(defn- p18-t04-verified-mir-c-source-native-errno
  [binding state]
  (int
   (.invokeWithArguments
    (:errno-handle binding)
    (object-array [state (long 0)]))))

(defn- p18-t04-verified-mir-c-source-native-int-call
  [binding operation state arguments]
  (int
   (.invokeWithArguments
    (get binding operation)
    (object-array (into [state] arguments)))))

(defn- p18-t04-verified-mir-c-source-native-long-call
  [binding operation state arguments]
  (long
   (.invokeWithArguments
    (get binding operation)
    (object-array (into [state] arguments)))))

(defn- p18-t04-verified-mir-c-source-native-stat-record
  [segment]
  (let [mode (bit-and 0xffff
                      (int (.get segment
                                 java.lang.foreign.ValueLayout/JAVA_SHORT
                                 (long 4))))]
    {:device (int (.get segment java.lang.foreign.ValueLayout/JAVA_INT
                        (long 0)))
     :inode (long (.get segment java.lang.foreign.ValueLayout/JAVA_LONG
                        (long 8)))
     :mode mode
     :byte-count
     (long (.get segment java.lang.foreign.ValueLayout/JAVA_LONG
                 (long 96)))
     :modified-time
     [(long (.get segment java.lang.foreign.ValueLayout/JAVA_LONG
                  (long 48)))
      (long (.get segment java.lang.foreign.ValueLayout/JAVA_LONG
                  (long 56)))]
     :changed-time
     [(long (.get segment java.lang.foreign.ValueLayout/JAVA_LONG
                  (long 64)))
      (long (.get segment java.lang.foreign.ValueLayout/JAVA_LONG
                  (long 72)))]
     :birth-time
     [(long (.get segment java.lang.foreign.ValueLayout/JAVA_LONG
                  (long 80)))
      (long (.get segment java.lang.foreign.ValueLayout/JAVA_LONG
                  (long 88)))]}))

(defn- p18-t04-verified-mir-c-source-native-path-stat!
  [binding arena state source-path actual-path]
  (let [path-segment (.allocateFrom arena actual-path)
        stat-segment
        (.allocate arena (long p18-t04-darwin-stat-byte-count) (long 8))
        return-code
        (p18-t04-verified-mir-c-source-native-int-call
         binding :fstatat state
         [(int p18-t04-darwin-at-fdcwd) path-segment stat-segment
          (int p18-t04-darwin-at-symlink-nofollow-any)])]
    (when (neg? return-code)
      (p18-t04-verified-mir-c-source-snapshot-fail!
       source-path :descriptor-bound-source-snapshot
       :nofollow-source-path-stat-failed
       {:captured-errno
        (p18-t04-verified-mir-c-source-native-errno binding state)}))
    (p18-t04-verified-mir-c-source-native-stat-record stat-segment)))

(defn- p18-t04-verified-mir-c-source-native-fstat!
  [binding arena state source-path file-descriptor]
  (let [stat-segment
        (.allocate arena (long p18-t04-darwin-stat-byte-count) (long 8))
        return-code
        (p18-t04-verified-mir-c-source-native-int-call
         binding :fstat state [file-descriptor stat-segment])]
    (when (neg? return-code)
      (p18-t04-verified-mir-c-source-snapshot-fail!
       source-path :descriptor-bound-source-snapshot
       :opened-source-fstat-failed
       {:captured-errno
        (p18-t04-verified-mir-c-source-native-errno binding state)}))
    (p18-t04-verified-mir-c-source-native-stat-record stat-segment)))

(defn- p18-t04-verified-mir-c-source-native-open!
  [binding arena state source-path actual-path]
  (let [path-segment (.allocateFrom arena actual-path)
        file-descriptor
        (p18-t04-verified-mir-c-source-native-int-call
         binding :open state
         [path-segment
          (int p18-t04-darwin-open-read-nofollow-flags)
          (int 0)])]
    (when (neg? file-descriptor)
      (p18-t04-verified-mir-c-source-snapshot-fail!
       source-path :bounded-readable-regular-gravity-source
       :descriptor-open-read-nofollow-failed
       {:maximum-byte-count p18-t04-verified-mir-c-maximum-source-bytes
        :captured-errno
        (p18-t04-verified-mir-c-source-native-errno binding state)}))
    file-descriptor))

(defn- p18-t04-verified-mir-c-source-native-fd-path!
  [binding arena state source-path file-descriptor]
  (let [path-buffer (.allocate arena (long 4096) (long 1))
        return-code
        (p18-t04-verified-mir-c-source-native-int-call
         binding :fcntl state
         [file-descriptor (int p18-t04-darwin-f-getpath) path-buffer])]
    (when (neg? return-code)
      (p18-t04-verified-mir-c-source-snapshot-fail!
       source-path :descriptor-bound-source-snapshot
       :opened-source-f-getpath-failed
       {:captured-errno
        (p18-t04-verified-mir-c-source-native-errno binding state)}))
    (.getString path-buffer (long 0)
                java.nio.charset.StandardCharsets/UTF_8)))

(defn- p18-t04-verified-mir-c-source-native-read-call
  [binding state file-descriptor buffer maximum-byte-count]
  (p18-t04-verified-mir-c-source-native-long-call
   binding :read state
   [file-descriptor buffer (long maximum-byte-count)]))