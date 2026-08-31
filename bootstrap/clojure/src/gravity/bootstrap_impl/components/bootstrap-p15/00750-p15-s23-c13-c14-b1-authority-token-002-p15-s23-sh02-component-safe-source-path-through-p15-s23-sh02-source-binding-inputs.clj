(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn- p15-s23-sh02-component-safe-source-path!
  [request-source repository-root source-path]
  (let [repository-root (.normalize (.toAbsolutePath repository-root))
        source-path (.normalize (.toAbsolutePath source-path))
        nofollow
        (into-array java.nio.file.LinkOption
                    [java.nio.file.LinkOption/NOFOLLOW_LINKS])]
    (when-not (.startsWith source-path repository-root)
      (p15-s23-b3-llvm-fail!
       "B1-INPUT" request-source {}
       {:missing-fact :bounded-pinned-sh02-source-location
        :source-path (str source-path)}))
    (loop [path repository-root
           components (seq (iterator-seq
                            (.iterator
                             (.relativize repository-root source-path))))]
      (let [attributes
            (try
              (java.nio.file.Files/readAttributes
               path java.nio.file.attribute.BasicFileAttributes nofollow)
              (catch java.io.IOException _
                (p15-s23-b3-llvm-fail!
                 "B1-INPUT" request-source {}
                 {:missing-fact :component-safe-pinned-sh02-source
                  :source-path (str source-path)
                  :observed-component (str path)})))]
        (when (or (.isSymbolicLink attributes)
                  (and (seq components) (not (.isDirectory attributes)))
                  (and (nil? components) (not (.isRegularFile attributes))))
          (p15-s23-b3-llvm-fail!
           "B1-INPUT" request-source {}
           {:missing-fact :component-safe-pinned-sh02-source
            :source-path (str source-path)
            :observed-component (str path)
            :symbolic-link? (.isSymbolicLink attributes)
            :directory? (.isDirectory attributes)
            :regular-file? (.isRegularFile attributes)}))
        (if-let [component (first components)]
          (recur (.resolve path ^java.nio.file.Path component)
                 (next components))
          attributes)))))

(defn- p15-s23-sh02-source-snapshot!
  [request-source repository-root source-path]
  (let [before
        (p15-s23-sh02-component-safe-source-path!
         request-source repository-root source-path)
        maximum-byte-count (inc p15-s23-sh02-source-byte-count)
        buffer (java.nio.ByteBuffer/allocate maximum-byte-count)]
    (when-not (= (long p15-s23-sh02-source-byte-count) (.size before))
      (p15-s23-b3-llvm-fail!
       "B1-INPUT" request-source {}
       {:missing-fact :pinned-sh02-source-snapshot
        :source-path (str source-path)
        :expected-source-bytes p15-s23-sh02-source-byte-count
        :observed-source-bytes (.size before)}))
    (try
      (with-open
       [channel
        (java.nio.channels.FileChannel/open
         source-path
         (into-array java.nio.file.OpenOption
                     [java.nio.file.StandardOpenOption/READ
                      java.nio.file.LinkOption/NOFOLLOW_LINKS]))]
       (let [channel-size-before (.size channel)
             observed-byte-count
             (loop [zero-reads 0]
               (if-not (.hasRemaining buffer)
                 (.position buffer)
                 (let [read-count (.read channel buffer)]
                   (cond
                     (neg? read-count) (.position buffer)
                     (zero? read-count)
                     (if (= 8 zero-reads)
                       (p15-s23-b3-llvm-fail!
                        "B1-INPUT" request-source {}
                        {:missing-fact :bounded-pinned-sh02-source-read
                         :source-path (str source-path)})
                       (recur (inc zero-reads)))
                     :else (recur 0)))))
             channel-size-after (.size channel)
             after
             (p15-s23-sh02-component-safe-source-path!
              request-source repository-root source-path)
             bytes
             (java.util.Arrays/copyOf (.array buffer) observed-byte-count)
             observed-content-hash
             (str "sha256:" (sha256-bytes-hex bytes))]
         (when-not
          (and (= p15-s23-sh02-source-byte-count observed-byte-count)
               (= p15-s23-sh02-expected-source-content-hash
                  observed-content-hash)
               (= channel-size-before channel-size-after
                  (long observed-byte-count))
               (= (.fileKey before) (.fileKey after))
               (= (.lastModifiedTime before) (.lastModifiedTime after))
               (= (.size before) (.size after)
                  (long observed-byte-count)))
           (p15-s23-b3-llvm-fail!
            "B1-INPUT" request-source {}
            {:missing-fact :stable-pinned-sh02-source-snapshot
             :source-path (str source-path)
             :expected-source-bytes p15-s23-sh02-source-byte-count
             :observed-source-bytes observed-byte-count
             :expected-source-content-hash
             p15-s23-sh02-expected-source-content-hash
             :observed-source-content-hash observed-content-hash
             :stable-observed-path-snapshot?
             (and (= (.fileKey before) (.fileKey after))
                  (= (.lastModifiedTime before)
                     (.lastModifiedTime after))
                  (= (.size before) (.size after)))
             :stable-open-channel-size?
             (= channel-size-before channel-size-after
                (long observed-byte-count))}))
         {:source-path (str source-path)
          :source-byte-count observed-byte-count
          :source-content-hash observed-content-hash
          :source-bytes bytes}))
      (catch java.io.IOException _
        (p15-s23-b3-llvm-fail!
         "B1-INPUT" request-source {}
         {:missing-fact :stable-pinned-sh02-source-snapshot
          :source-path (str source-path)})))))

(defn- p15-s23-sh02-strict-source-text!
  [request-source source-path bytes]
  (try
    (let [decoder
          (doto (.newDecoder java.nio.charset.StandardCharsets/UTF_8)
            (.onMalformedInput java.nio.charset.CodingErrorAction/REPORT)
            (.onUnmappableCharacter
             java.nio.charset.CodingErrorAction/REPORT))]
      (.toString (.decode decoder (java.nio.ByteBuffer/wrap bytes))))
    (catch java.nio.charset.CharacterCodingException _
      (p15-s23-b3-llvm-fail!
       "B1-INPUT" request-source {}
       {:missing-fact :strict-utf8-pinned-sh02-source
        :source-path (str source-path)}))))

(defn- p15-s23-sh02-source-binding-inputs!
  [candidate source-path]
  (p15-s23-c13-c14-b1-require-authority!
   candidate source-path :load-pinned-sh02-source)
  (let [request-source source-path
        location (p15-s23-sh02-source-location! candidate request-source)
        repository-root (:repository-root location)
        sh02-source-path (:source-path location)
        snapshot
        (p15-s23-sh02-source-snapshot!
         request-source repository-root sh02-source-path)
        bytes (:source-bytes snapshot)
        source-text
        (p15-s23-sh02-strict-source-text!
         request-source sh02-source-path bytes)
        emitter-rule
        (c-backend-stage2-plan-emitter-source-rule! request-source :jvm)
        emitter-source-path
        (p15-s23-c6c10-canonical-file-path (:source-path emitter-rule))]
    {:inputs
     (merge
      (dissoc snapshot :source-bytes)
      {:emitter-target :jvm
       :emitter-source-path emitter-source-path
       :emitter-source-byte-count
       p15-s23-stage2-compiler-expected-source-byte-count
       :emitter-source-content-hash
       p15-s23-stage2-compiler-expected-source-content-hash
       :emitter-source-rule-hash (:source-rule-hash emitter-rule)})
     :source-text source-text
     :emitter-rule emitter-rule})))
