(ns gravity.darwin-publication.native-runtime
  "Internal Darwin publication native runtime operations."
  (:require [clojure.string :as str]
            [gravity.darwin-publication.contract :refer :all]
            [gravity.darwin-publication.failure :refer :all])
  (:import [java.lang.foreign Arena FunctionDescriptor Linker
            Linker$Option MemoryLayout MemoryLayout$PathElement MemorySegment
            ValueLayout]
           [java.lang.invoke VarHandle$AccessMode]
           [java.nio ByteBuffer]
           [java.nio.charset CharacterCodingException CodingErrorAction
            StandardCharsets]
           [java.nio.file InvalidPathException Paths]
           [java.security MessageDigest SecureRandom]
           [java.util Collections WeakHashMap]))

(defn function-descriptor
  [return-layout argument-layouts]
  (FunctionDescriptor/of
   return-layout
   (into-array MemoryLayout argument-layouts)))

(defn symbol!
  [lookup name]
  (let [candidate (.find lookup name)]
    (when (.isEmpty candidate)
      (failure! :native-preflight :missing-native-symbol
                {:missing-symbol name}))
    (.get candidate)))

(defn bind-captured!
  [linker lookup capture name return-layout argument-layouts
   & linker-options]
  (.downcallHandle
   linker (symbol! lookup name)
   (function-descriptor return-layout argument-layouts)
   (into-array Linker$Option (into [capture] linker-options))))

(defn bind-direct!
  [linker lookup name return-layout argument-layouts
   & linker-options]
  (.downcallHandle
   linker (symbol! lookup name)
   (function-descriptor return-layout argument-layouts)
   (into-array Linker$Option linker-options)))

(defn native-runtime!
  []
  (let [native-access-enabled?
        (.isNativeAccessEnabled (.getModule clojure.lang.RT))]
    (when-not
     (and (= 26 (.feature (Runtime/version)))
          (= "26.0.1" (System/getProperty "java.version"))
          (= "Mac OS X" (System/getProperty "os.name"))
          (= "aarch64" (System/getProperty "os.arch")))
      (failure! :native-preflight :unsupported-host-runtime))
    (when-not native-access-enabled?
      (failure! :native-preflight :native-access-disabled
                {:native-access-enabled? false}))
    (try
      (let [linker (Linker/nativeLinker)
            lookup (.defaultLookup linker)
            capture (Linker$Option/captureCallState
                     (into-array String ["errno"]))
            state-layout (Linker$Option/captureStateLayout)
            errno-handle
            (.toMethodHandle
             (.varHandle
              state-layout
              (into-array
               MemoryLayout$PathElement
               [(MemoryLayout$PathElement/groupElement "errno")]))
             VarHandle$AccessMode/GET)
            variadic2 (Linker$Option/firstVariadicArg 2)
            variadic3 (Linker$Option/firstVariadicArg 3)]
        {:state-layout state-layout
         :errno-handle errno-handle
         :open
         (bind-captured!
          linker lookup capture "open" ValueLayout/JAVA_INT
          [ValueLayout/ADDRESS ValueLayout/JAVA_INT ValueLayout/JAVA_INT]
          variadic2)
         :openat
         (bind-captured!
          linker lookup capture "openat" ValueLayout/JAVA_INT
          [ValueLayout/JAVA_INT ValueLayout/ADDRESS
           ValueLayout/JAVA_INT ValueLayout/JAVA_INT]
          variadic3)
         :mkdirat
         (bind-captured!
          linker lookup capture "mkdirat" ValueLayout/JAVA_INT
          [ValueLayout/JAVA_INT ValueLayout/ADDRESS ValueLayout/JAVA_SHORT])
         :fstat
         (bind-captured!
          linker lookup capture "fstat" ValueLayout/JAVA_INT
          [ValueLayout/JAVA_INT ValueLayout/ADDRESS])
         :fstatat
         (bind-captured!
          linker lookup capture "fstatat" ValueLayout/JAVA_INT
          [ValueLayout/JAVA_INT ValueLayout/ADDRESS ValueLayout/ADDRESS
           ValueLayout/JAVA_INT])
         :fcntl-address
         (bind-captured!
          linker lookup capture "fcntl" ValueLayout/JAVA_INT
          [ValueLayout/JAVA_INT ValueLayout/JAVA_INT ValueLayout/ADDRESS]
          variadic2)
         :write
         (bind-captured!
          linker lookup capture "write" ValueLayout/JAVA_LONG
          [ValueLayout/JAVA_INT ValueLayout/ADDRESS ValueLayout/JAVA_LONG])
         :pread
         (bind-captured!
          linker lookup capture "pread" ValueLayout/JAVA_LONG
          [ValueLayout/JAVA_INT ValueLayout/ADDRESS ValueLayout/JAVA_LONG
           ValueLayout/JAVA_LONG])
         :fchmod
         (bind-captured!
          linker lookup capture "fchmod" ValueLayout/JAVA_INT
          [ValueLayout/JAVA_INT ValueLayout/JAVA_SHORT])
         :fsync
         (bind-captured!
          linker lookup capture "fsync" ValueLayout/JAVA_INT
          [ValueLayout/JAVA_INT])
         :fdopendir
         (bind-captured!
          linker lookup capture "fdopendir" ValueLayout/ADDRESS
          [ValueLayout/JAVA_INT])
         :readdir
         (bind-captured!
          linker lookup capture "readdir" ValueLayout/ADDRESS
          [ValueLayout/ADDRESS])
         :closedir
         (bind-captured!
          linker lookup capture "closedir" ValueLayout/JAVA_INT
          [ValueLayout/ADDRESS])
         :unlinkat
         (bind-captured!
          linker lookup capture "unlinkat" ValueLayout/JAVA_INT
          [ValueLayout/JAVA_INT ValueLayout/ADDRESS ValueLayout/JAVA_INT])
         :renameatx-np
         (bind-captured!
          linker lookup capture "renameatx_np" ValueLayout/JAVA_INT
          [ValueLayout/JAVA_INT ValueLayout/ADDRESS ValueLayout/JAVA_INT
           ValueLayout/ADDRESS ValueLayout/JAVA_INT])
         :close
         (bind-captured!
          linker lookup capture "close" ValueLayout/JAVA_INT
          [ValueLayout/JAVA_INT])
         :geteuid
         (bind-captured!
          linker lookup capture "geteuid" ValueLayout/JAVA_INT [])
         :acl-get-fd-np
         (bind-captured!
          linker lookup capture "acl_get_fd_np" ValueLayout/ADDRESS
          [ValueLayout/JAVA_INT ValueLayout/JAVA_INT])
         :acl-free
         (bind-captured!
          linker lookup capture "acl_free" ValueLayout/JAVA_INT
          [ValueLayout/ADDRESS])
         :error-pointer
         (bind-direct!
          linker lookup "__error" ValueLayout/ADDRESS [])})
      (catch clojure.lang.ExceptionInfo error
        (throw error))
      (catch Exception error
        (rethrow-interrupt! error)
        (failure! :native-preflight :ffi-binding-unavailable)))))
