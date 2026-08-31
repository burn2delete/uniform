(let [p15-s23-b2-c17-gate-b-authority-token (nth __gravity_bootstrap_lexical_values_131964 0)
      p15-s23-b2-c17-gate-b-tool-state (nth __gravity_bootstrap_lexical_values_131964 1)]
(defn- p15-s23-b2-c17-gate-b-file-snapshot!
  [candidate root path source-path operation maximum-byte-count]
  (p15-s23-b2-c17-gate-b-require-authority!
   candidate source-path operation)
  (let [root (.normalize (.toAbsolutePath ^java.nio.file.Path root))
        path (.normalize (.toAbsolutePath ^java.nio.file.Path path))
        nofollow
        (into-array java.nio.file.LinkOption
                    [java.nio.file.LinkOption/NOFOLLOW_LINKS])]
    (when-not
     (and (java.nio.file.Files/isDirectory root nofollow)
          (not (java.nio.file.Files/isSymbolicLink root))
          (.startsWith path root)
          (not= root path)
          (= root (.getParent path))
          (integer? maximum-byte-count)
          (pos? maximum-byte-count)
          (<= maximum-byte-count (* 8 1024 1024))
          (not (java.nio.file.Files/isSymbolicLink path)))
      (p15-s23-c-backend-fail!
       "B2-MANIFEST" source-path {}
       {:missing-fact :contained-nofollow-c17-regular-file-snapshot
        :bounded-reason operation}))
    (let [before
          (try
            (java.nio.file.Files/readAttributes
             path java.nio.file.attribute.BasicFileAttributes nofollow)
            (catch Exception error
              (p15-s23-b2-c17-gate-b-rethrow-interrupt! error)
              nil))]
      (when-not (and before (.isRegularFile before)
                     (some? (.fileKey before))
                     (<= 0 (.size before) maximum-byte-count))
        (p15-s23-c-backend-fail!
         "B2-MANIFEST" source-path {}
         {:missing-fact :bounded-regular-c17-file
          :bounded-reason operation
          :maximum-byte-count maximum-byte-count
          :observed-byte-count (when before (.size before))}))
      (let [observed
            (with-open
             [channel
              (java.nio.channels.FileChannel/open
               path
               (into-array
                java.nio.file.OpenOption
                [java.nio.file.StandardOpenOption/READ
                 java.nio.file.LinkOption/NOFOLLOW_LINKS]))
              input (java.nio.channels.Channels/newInputStream channel)
              output (java.io.ByteArrayOutputStream.)]
              (let [buffer (byte-array 8192)
                    digest
                    (java.security.MessageDigest/getInstance "SHA-256")]
                (loop [total 0]
                  (let [read (.read input buffer)]
                    (if (neg? read)
                      (let [bytes (.toByteArray output)]
                        {:bytes bytes
                         :byte-count total
                         :content-hash
                         (str "sha256:"
                              (apply str
                                     (map #(format "%02x"
                                                   (bit-and % 0xff))
                                          (.digest digest))))})
                      (let [next-total (+ total read)]
                        (when (> next-total maximum-byte-count)
                          (p15-s23-c-backend-fail!
                           "B2-MANIFEST" source-path {}
                           {:missing-fact :bounded-emitted-c17-artifact-size
                            :bounded-reason operation
                            :maximum-byte-count maximum-byte-count
                            :observed-byte-count next-total}))
                        (.update digest buffer 0 read)
                        (.write output buffer 0 read)
                        (recur next-total)))))))
            after
            (java.nio.file.Files/readAttributes
             path java.nio.file.attribute.BasicFileAttributes nofollow)]
        (when-not (and (.isRegularFile after)
                       (not (.isSymbolicLink after))
                       (some? (.fileKey after))
                       (= (.fileKey before) (.fileKey after))
                       (= (.size before) (.size after)
                          (:byte-count observed))
                       (= (.lastModifiedTime before)
                          (.lastModifiedTime after)))
          (p15-s23-c-backend-fail!
           "B13-HASH" source-path {}
           {:missing-fact :stable-single-read-c17-file-snapshot
            :bounded-reason operation}))
        (assoc observed
               :file-key-hash
               (str "sha256:" (sha256-hex (str (.fileKey after))))
               :last-modified-millis
               (.toMillis (.lastModifiedTime after)))))))

(defn- p15-s23-b2-c17-gate-b-snapshot-content
  [snapshot]
  (select-keys snapshot [:byte-count :content-hash]))

(defn- p15-s23-b2-c17-gate-b-capped-directory-inventory!
  [candidate directory source-path expected-maximum]
  (p15-s23-b2-c17-gate-b-require-authority!
   candidate source-path :bounded-c17-directory-inventory)
  (let [nofollow
        (into-array java.nio.file.LinkOption
                    [java.nio.file.LinkOption/NOFOLLOW_LINKS])]
    (when-not
     (and (integer? expected-maximum) (<= 0 expected-maximum 16)
          (java.nio.file.Files/isDirectory directory nofollow)
          (not (java.nio.file.Files/isSymbolicLink directory)))
      (p15-s23-c-backend-fail!
       "B2-MANIFEST" source-path {}
       {:missing-fact :bounded-regular-c17-directory-inventory}))
    (with-open [stream (java.nio.file.Files/newDirectoryStream directory)]
      (loop [iterator (.iterator stream) names []]
        (if-not (.hasNext iterator)
          (set names)
          (let [path (.next iterator)
                next-names (conj names (str (.getFileName path)))]
            (when (> (count next-names) expected-maximum)
              (p15-s23-c-backend-fail!
               "B2-MANIFEST" source-path {}
               {:missing-fact :bounded-c17-directory-inventory-limit
                :maximum-byte-count expected-maximum
                :observed-byte-count (count next-names)}))
            (recur iterator next-names)))))))

(defn- p15-s23-b2-c17-gate-b-delete-tree!
  [candidate root source-path]
  (p15-s23-b2-c17-gate-b-require-authority!
   candidate source-path :delete-private-c17-staging-tree)
  (when (and root
             (java.nio.file.Files/exists
              root
              (into-array java.nio.file.LinkOption
                          [java.nio.file.LinkOption/NOFOLLOW_LINKS])))
    (with-open [stream
                (java.nio.file.Files/walk
                 root 2 (make-array java.nio.file.FileVisitOption 0))]
      (loop [iterator (.iterator stream) paths []]
        (if-not (.hasNext iterator)
          (doseq [path (reverse paths)]
            (java.nio.file.Files/deleteIfExists path))
          (let [next-paths (conj paths (.next iterator))]
            (when (> (count next-paths) 16)
              (p15-s23-c-backend-fail!
               "B2-MANIFEST" source-path {}
               {:missing-fact :bounded-private-c17-staging-cleanup}))
            (recur iterator next-paths)))))))

(defn- p15-s23-b2-c17-gate-b-write-bytes!
  [candidate workspace logical-path bytes executable? source-path]
  (p15-s23-b2-c17-gate-b-require-authority!
   candidate source-path :write-private-c17-artifact)
  (let [allowed
        #{"program.c" "program.h" "program.o" "program"
          "manifest.edn" "provenance.edn" "conformance.edn"}
        root (.normalize (.toAbsolutePath workspace))
        path (.normalize (.toAbsolutePath (.resolve workspace logical-path)))]
    (when-not
     (and (contains? allowed logical-path)
          (= root (.getParent path))
          (bytes? bytes)
          (<= 0 (alength ^bytes bytes) (* 8 1024 1024))
          (not (java.nio.file.Files/exists
                path
                (into-array java.nio.file.LinkOption
                            [java.nio.file.LinkOption/NOFOLLOW_LINKS]))))
      (p15-s23-c-backend-fail!
       "B2-MANIFEST" source-path {}
       {:missing-fact :closed-contained-c17-artifact-write
        :logical-path logical-path
        :observed-byte-count
        (when (bytes? bytes) (alength ^bytes bytes))}))
    (java.nio.file.Files/write
     path bytes
     (into-array java.nio.file.OpenOption
                 [java.nio.file.StandardOpenOption/CREATE_NEW
                  java.nio.file.StandardOpenOption/WRITE]))
    (java.nio.file.Files/setPosixFilePermissions
     path
     (if executable?
       p15-s23-b2-c17-gate-b-executable-permissions
       p15-s23-b2-c17-gate-b-nonexecutable-permissions))
    (let [snapshot
          (p15-s23-b2-c17-gate-b-file-snapshot!
           candidate workspace path source-path
           :verify-private-c17-artifact (* 8 1024 1024))]
      (when-not (java.util.Arrays/equals
                 ^bytes bytes ^bytes (:bytes snapshot))
        (p15-s23-c-backend-fail!
         "B13-HASH" source-path {}
         {:missing-fact :private-c17-artifact-roundtrip
          :logical-path logical-path}))
      snapshot)))

(defn- p15-s23-b2-c17-gate-b-write-text!
  [candidate workspace logical-path text source-path]
  (p15-s23-b2-c17-gate-b-write-bytes!
   candidate workspace logical-path
   (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8)
   false source-path)))
