(let [p15-s23-b2-c17-gate-b-authority-token (nth __gravity_bootstrap_lexical_values_131964 0)
      p15-s23-b2-c17-gate-b-tool-state (nth __gravity_bootstrap_lexical_values_131964 1)]
(defn- p15-s23-b2-c17-gate-b-path-identity!
  [candidate source-path path expected-kind]
  (p15-s23-b2-c17-gate-b-require-authority!
   candidate source-path :bind-pinned-c17-tool-path)
  (let [path (java.nio.file.Paths/get path (make-array String 0))
        nofollow
        (into-array java.nio.file.LinkOption
                    [java.nio.file.LinkOption/NOFOLLOW_LINKS])
        attributes
        (try
          (java.nio.file.Files/readAttributes
           path java.nio.file.attribute.BasicFileAttributes nofollow)
          (catch Exception error
            (p15-s23-b2-c17-gate-b-rethrow-interrupt! error)
            nil))]
    (when-not
     (and attributes (some? (.fileKey attributes))
          (case expected-kind
            :file (and (.isRegularFile attributes)
                       (not (java.nio.file.Files/isSymbolicLink path)))
            :directory (and (.isDirectory attributes)
                            (not (java.nio.file.Files/isSymbolicLink path)))
            false))
      (p15-s23-c-backend-fail!
       "B2-DIALECT" source-path {}
       {:missing-fact :stable-pinned-c17-tool-path
        :bounded-reason expected-kind}))
    {:actual-path (.toString (.toAbsolutePath path))
     :kind expected-kind
     :file-key-hash
     (str "sha256:" (sha256-hex (str (.fileKey attributes))))
     :size (.size attributes)
     :last-modified-millis (.toMillis (.lastModifiedTime attributes))}))

(defn- p15-s23-b2-c17-gate-b-tool-file-identity!
  [candidate source-path path]
  (p15-s23-b2-c17-gate-b-require-authority!
   candidate source-path :content-bind-pinned-c17-tool-file)
  (let [path (java.nio.file.Paths/get path (make-array String 0))
        nofollow
        (into-array java.nio.file.LinkOption
                    [java.nio.file.LinkOption/NOFOLLOW_LINKS])
        before
        (try
          (java.nio.file.Files/readAttributes
           path java.nio.file.attribute.BasicFileAttributes nofollow)
          (catch Exception error
            (p15-s23-b2-c17-gate-b-rethrow-interrupt! error)
            nil))]
    (when-not (and before (.isRegularFile before)
                   (not (java.nio.file.Files/isSymbolicLink path))
                   (some? (.fileKey before))
                   (<= 0 (.size before) (* 512 1024 1024)))
      (p15-s23-c-backend-fail!
       "B2-DIALECT" source-path {}
       {:missing-fact :bounded-pinned-c17-tool-file
        :maximum-byte-count (* 512 1024 1024)
        :observed-byte-count (when before (.size before))}))
    (let [digest (java.security.MessageDigest/getInstance "SHA-256")
          byte-count
          (with-open
           [channel
            (java.nio.channels.FileChannel/open
             path
             (into-array java.nio.file.OpenOption
                         [java.nio.file.StandardOpenOption/READ
                          java.nio.file.LinkOption/NOFOLLOW_LINKS]))]
            (let [buffer (java.nio.ByteBuffer/allocate 65536)]
              (loop [total 0]
                (.clear buffer)
                (let [read (.read channel buffer)]
                  (if (neg? read)
                    total
                    (let [next-total (+ total read)]
                      (when (> next-total (* 512 1024 1024))
                        (p15-s23-c-backend-fail!
                         "B2-DIALECT" source-path {}
                         {:missing-fact :bounded-pinned-c17-tool-file-growth
                          :maximum-byte-count (* 512 1024 1024)
                          :observed-byte-count next-total}))
                      (.flip buffer)
                      (.update digest buffer)
                      (recur next-total)))))))
          after
          (java.nio.file.Files/readAttributes
           path java.nio.file.attribute.BasicFileAttributes nofollow)]
      (when-not (and (.isRegularFile after)
                     (= (.fileKey before) (.fileKey after))
                     (= (.size before) (.size after) byte-count)
                     (= (.lastModifiedTime before)
                        (.lastModifiedTime after)))
        (p15-s23-c-backend-fail!
         "B13-HASH" source-path {}
         {:missing-fact :stable-pinned-c17-tool-file-content}))
      {:actual-path (.toString (.toAbsolutePath path))
       :kind :file
       :byte-count byte-count
       :content-hash
       (str "sha256:"
            (apply str
                   (map #(format "%02x" (bit-and % 0xff))
                        (.digest digest))))
       :file-key-hash
       (str "sha256:" (sha256-hex (str (.fileKey after))))
       :last-modified-millis (.toMillis (.lastModifiedTime after))})))

(defn- p15-s23-b2-c17-gate-b-pinned-physical-toolchain!
  [candidate source-path]
  {:xcrun
   (p15-s23-b2-c17-gate-b-tool-file-identity!
    candidate source-path "/usr/bin/xcrun")
   :file
   (p15-s23-b2-c17-gate-b-tool-file-identity!
    candidate source-path "/usr/bin/file")
   :file-magic
   (p15-s23-b2-c17-gate-b-tool-file-identity!
    candidate source-path "/usr/share/file/magic.mgc")
   :clang
   (p15-s23-b2-c17-gate-b-tool-file-identity!
    candidate source-path p15-s23-b2-c17-gate-b-clang-path)
   :ld
   (p15-s23-b2-c17-gate-b-tool-file-identity!
    candidate source-path p15-s23-b2-c17-gate-b-ld-path)
   :otool
   (p15-s23-b2-c17-gate-b-tool-file-identity!
    candidate source-path p15-s23-b2-c17-gate-b-otool-real-path)
   :sdk
   (p15-s23-b2-c17-gate-b-path-identity!
    candidate source-path p15-s23-b2-c17-gate-b-sdk-path :directory)})

(declare ^:private p15-s23-b2-c17-gate-b-workspace-binding!))
