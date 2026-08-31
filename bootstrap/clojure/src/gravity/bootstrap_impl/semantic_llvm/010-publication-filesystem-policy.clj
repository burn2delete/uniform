(def ^:private p15-s23-b3-llvm-nonexecutable-permissions
  #{java.nio.file.attribute.PosixFilePermission/OWNER_READ
    java.nio.file.attribute.PosixFilePermission/OWNER_WRITE
    java.nio.file.attribute.PosixFilePermission/GROUP_READ
    java.nio.file.attribute.PosixFilePermission/OTHERS_READ})

(def ^:private p15-s23-b3-llvm-executable-permissions
  #{java.nio.file.attribute.PosixFilePermission/OWNER_READ
    java.nio.file.attribute.PosixFilePermission/OWNER_WRITE
    java.nio.file.attribute.PosixFilePermission/OWNER_EXECUTE
    java.nio.file.attribute.PosixFilePermission/GROUP_READ
    java.nio.file.attribute.PosixFilePermission/GROUP_EXECUTE
    java.nio.file.attribute.PosixFilePermission/OTHERS_READ
    java.nio.file.attribute.PosixFilePermission/OTHERS_EXECUTE})

(def ^:private p15-s23-b3-llvm-directory-permissions
  p15-s23-b3-llvm-executable-permissions)

(def ^:private p15-s23-b3-llvm-private-directory-permissions
  #{java.nio.file.attribute.PosixFilePermission/OWNER_READ
    java.nio.file.attribute.PosixFilePermission/OWNER_WRITE
    java.nio.file.attribute.PosixFilePermission/OWNER_EXECUTE})

(defn- p15-s23-b3-llvm-snapshot-content
  [snapshot]
  (select-keys snapshot [:byte-count :content-hash]))

(defn- p15-s23-b3-llvm-capped-directory-inventory!
  [candidate directory source-path expected-maximum]
  (p15-s23-b3-llvm-require-authority!
   candidate source-path :bounded-directory-inventory)
  (when-not (and (integer? expected-maximum) (<= 0 expected-maximum 16)
                 (java.nio.file.Files/isDirectory
                  directory
                  (into-array java.nio.file.LinkOption
                              [java.nio.file.LinkOption/NOFOLLOW_LINKS]))
                 (not (java.nio.file.Files/isSymbolicLink directory)))
    (p15-s23-b3-llvm-fail!
     "B3-MANIFEST" source-path {}
     {:missing-fact :bounded-regular-directory-inventory}))
  (with-open [stream (java.nio.file.Files/newDirectoryStream directory)]
    (loop [iterator (.iterator stream)
           names []]
      (if-not (.hasNext iterator)
        (set names)
        (let [path (.next iterator)
              next-names (conj names (str (.getFileName path)))]
          (when (> (count next-names) expected-maximum)
            (p15-s23-b3-llvm-fail!
             "B3-MANIFEST" source-path {}
             {:missing-fact :bounded-directory-inventory-limit
              :maximum-byte-count expected-maximum
              :observed-byte-count (count next-names)}))
          (recur iterator next-names))))))

(defn- p15-s23-b3-llvm-delete-tree!
  [candidate root source-path]
  (p15-s23-b3-llvm-require-authority!
   candidate source-path :delete-private-staging-tree)
  (when (and root (java.nio.file.Files/exists
                   root
                   (into-array java.nio.file.LinkOption
                               [java.nio.file.LinkOption/NOFOLLOW_LINKS])))
    (with-open [stream (java.nio.file.Files/walk
                       root 2
                       (make-array java.nio.file.FileVisitOption 0))]
      (loop [iterator (.iterator stream)
             paths []]
        (if-not (.hasNext iterator)
          (doseq [path (reverse paths)]
            (java.nio.file.Files/deleteIfExists path))
          (let [next-paths (conj paths (.next iterator))]
            (when (> (count next-paths) 16)
              (p15-s23-b3-llvm-fail!
               "B3-MANIFEST" source-path {}
               {:missing-fact :bounded-private-staging-cleanup}))
            (recur iterator next-paths)))))))
