(defn- p15-s23-b3-llvm-output-preflight!
  [candidate output-directory source-path]
  (p15-s23-b3-llvm-require-authority!
   candidate source-path :exclusive-publication-output-preflight)
  (when output-directory
    ;; This check happens before Linker/nativeLinker can emit a restricted-
    ;; native-access warning and before any tool or publication filesystem work.
    (let [native-binding
          (p15-s23-b3-llvm-native-publication-preflight!
           candidate source-path)
          requested-destination
          (.normalize (.toAbsolutePath
                       (java.nio.file.Paths/get
                        output-directory
                        (make-array String 0))))
          parent (.getParent requested-destination)
          parent-real
          (when parent
            (try
              (.toRealPath parent
                           (make-array java.nio.file.LinkOption 0))
              (catch Exception _ nil)))
          destination
          (when parent-real
            (.resolve parent-real (.getFileName requested-destination)))
          parent-attributes
          (when parent-real
            (try
              (java.nio.file.Files/readAttributes
               parent-real java.nio.file.attribute.BasicFileAttributes
               (into-array
                java.nio.file.LinkOption
                [java.nio.file.LinkOption/NOFOLLOW_LINKS]))
              (catch Exception _ nil)))
          parent-file-key
          (when parent-attributes (.fileKey parent-attributes))]
      (loop [ancestor parent]
        (when ancestor
          (when (java.nio.file.Files/isSymbolicLink ancestor)
            (p15-s23-b3-llvm-fail!
             "B3-MANIFEST" source-path {}
             {:missing-fact :non-symlink-output-ancestor}))
          (recur (.getParent ancestor))))
      (when-not (and parent parent-real destination parent-attributes
                     (.isDirectory parent-attributes)
                     parent-file-key
                     (java.nio.file.Files/isDirectory
                      parent
                      (into-array java.nio.file.LinkOption
                                  [java.nio.file.LinkOption/NOFOLLOW_LINKS]))
                     (not (java.nio.file.Files/isSymbolicLink parent))
                     (not (java.nio.file.Files/exists
                           destination
                           (into-array java.nio.file.LinkOption
                                       [java.nio.file.LinkOption/NOFOLLOW_LINKS]))))
        (p15-s23-b3-llvm-fail!
         "B3-MANIFEST" source-path {}
         {:missing-fact :collision-free-regular-output-directory
          :output-collision?
          (boolean
           (and destination
                (java.nio.file.Files/exists
                 destination
                 (into-array java.nio.file.LinkOption
                             [java.nio.file.LinkOption/NOFOLLOW_LINKS]))))}))
      {:destination destination
       :requested-destination requested-destination
       :parent parent-real
       :parent-file-key parent-file-key
       :parent-file-key-hash
       (str "sha256:" (sha256-hex (str parent-file-key)))
       :native-binding native-binding})))
