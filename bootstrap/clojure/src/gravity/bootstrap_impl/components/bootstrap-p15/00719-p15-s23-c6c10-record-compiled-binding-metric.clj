

(defn- p15-s23-c6c10-record-compiled-binding-metric!
  [event]
  (when (instance? clojure.lang.IAtom
                   *p15-s23-c6c10-compiled-binding-metrics*)
    (swap! *p15-s23-c6c10-compiled-binding-metrics*
           update event (fnil inc 0))))

(defn- p15-s23-c6c10-canonical-file-path
  [source-path]
  (try
    (.getCanonicalPath (java.io.File. source-path))
    (catch java.io.IOException _
      (.getPath
       (.normalize
        (.toAbsolutePath (.toPath (java.io.File. source-path))))))))

(defn- p15-s23-c6c10-read-pinned-source-snapshot!
  [request-source]
  (let [source-path (p15-s23-c6c10-resolve-source-path)
        source-file (java.io.File. source-path)
        path (.toPath source-file)
        nofollow
        (into-array java.nio.file.LinkOption
                    [java.nio.file.LinkOption/NOFOLLOW_LINKS])
        attributes
        (try
          (java.nio.file.Files/readAttributes
           path java.nio.file.attribute.BasicFileAttributes nofollow)
          (catch Exception _
            (p15-s23-c6c10-host-fail!
             "C6-VERIFY" request-source
             :pinned-gravity-c6-c10-source-present
             {:builder-source source-path})))]
    (when-not (and (.isRegularFile attributes)
                   (not (.isSymbolicLink attributes))
                   (= (long p15-s23-c6c10-source-byte-count)
                      (.size attributes)))
      (p15-s23-c6c10-host-fail!
       "C6-VERIFY" request-source
       :pinned-gravity-c6-c10-source-bytes
       {:builder-source source-path
        :regular-file? (.isRegularFile attributes)
        :symbolic-link? (.isSymbolicLink attributes)
        :expected-byte-count p15-s23-c6c10-source-byte-count
        :actual-byte-count (.size attributes)
        :expected-source-content-hash
        p15-s23-c6c10-expected-source-content-hash}))
    (let [maximum-byte-count (inc p15-s23-c6c10-source-byte-count)
          buffer (byte-array maximum-byte-count)
          observed-byte-count
          (try
            (with-open
             [input
              (java.nio.file.Files/newInputStream
               path
               (into-array
                java.nio.file.OpenOption
                [java.nio.file.StandardOpenOption/READ
                 java.nio.file.LinkOption/NOFOLLOW_LINKS]))]
              (loop [offset 0]
                (if (= offset maximum-byte-count)
                  offset
                  (let [read-count
                        (.read input buffer offset
                               (- maximum-byte-count offset))]
                    (cond
                      (neg? read-count) offset
                      (zero? read-count) (recur offset)
                      :else (recur (+ offset read-count)))))))
            (catch Exception _
              (p15-s23-c6c10-host-fail!
               "C6-VERIFY" request-source
               :pinned-gravity-c6-c10-source-bytes
               {:builder-source source-path
                :expected-byte-count p15-s23-c6c10-source-byte-count})))
          after
          (try
            (java.nio.file.Files/readAttributes
             path java.nio.file.attribute.BasicFileAttributes nofollow)
            (catch Exception _
              (p15-s23-c6c10-host-fail!
               "C6-VERIFY" request-source
               :pinned-gravity-c6-c10-source-bytes
               {:builder-source source-path
                :expected-byte-count p15-s23-c6c10-source-byte-count})))
          source-bytes
          (java.util.Arrays/copyOf buffer observed-byte-count)
          source-content-hash
          (str "sha256:" (sha256-bytes-hex source-bytes))]
      (when-not
       (and (= p15-s23-c6c10-source-byte-count observed-byte-count)
            (= p15-s23-c6c10-expected-source-content-hash
               source-content-hash)
            (.isRegularFile after)
            (not (.isSymbolicLink after))
            (= (.fileKey attributes) (.fileKey after))
            (= (.size attributes) (.size after)
               (long observed-byte-count))
            (= (.lastModifiedTime attributes)
               (.lastModifiedTime after)))
        (p15-s23-c6c10-host-fail!
         "C6-VERIFY" request-source
         :pinned-gravity-c6-c10-source-bytes
         {:builder-source source-path
          :regular-file? (.isRegularFile after)
          :symbolic-link? (.isSymbolicLink after)
          :stable-file-key?
          (= (.fileKey attributes) (.fileKey after))
          :stable-last-modified-time?
          (= (.lastModifiedTime attributes)
             (.lastModifiedTime after))
          :expected-byte-count p15-s23-c6c10-source-byte-count
          :actual-byte-count observed-byte-count
          :expected-source-content-hash
          p15-s23-c6c10-expected-source-content-hash
          :actual-source-content-hash source-content-hash}))
      (let [source-text
            (try
              (let [decoder
                    (doto (.newDecoder
                           java.nio.charset.StandardCharsets/UTF_8)
                      (.onMalformedInput
                       java.nio.charset.CodingErrorAction/REPORT)
                      (.onUnmappableCharacter
                       java.nio.charset.CodingErrorAction/REPORT))]
                (.toString
                 (.decode decoder
                          (java.nio.ByteBuffer/wrap source-bytes))))
              (catch java.nio.charset.CharacterCodingException ex
                (p15-s23-c6c10-host-fail!
                 "C6-VERIFY" request-source
                 :strict-utf8-pinned-gravity-c6-c10-source
                 {:builder-source source-path
                  :cause-message (.getMessage ex)})))]
        (p15-s23-c6c10-record-compiled-binding-metric!
         :source-authenticated)
        {:source-path source-path
         :canonical-source-path
         (p15-s23-c6c10-canonical-file-path source-path)
         :source-byte-count observed-byte-count
         :source-content-hash source-content-hash
         :source-text source-text}))))

(defn- p15-s23-c6c10-authenticated-source-binding-inputs!
  [request-source]
  (let [source-snapshot
        (p15-s23-c6c10-read-pinned-source-snapshot! request-source)
        emitter-source-path
        (c-backend-resolve-p15-s23-compiler-source-path)
        emitter-path (.toPath (java.io.File. emitter-source-path))
        nofollow
        (into-array java.nio.file.LinkOption
                    [java.nio.file.LinkOption/NOFOLLOW_LINKS])
        emitter-before
        (try
          (java.nio.file.Files/readAttributes
           emitter-path java.nio.file.attribute.BasicFileAttributes nofollow)
          (catch Exception _
            (p15-s23-stage2-plan-emitter-fail!
             "P15S23Q001" emitter-source-path nil
             {:requested-source request-source
              :target :jvm
              :missing-fact :stage2-compiler-source-regular-file})))
        _
        (when-not
         (and (.isRegularFile emitter-before)
              (not (.isSymbolicLink emitter-before))
              (= (long p15-s23-stage2-compiler-expected-source-byte-count)
                 (.size emitter-before)))
          (p15-s23-stage2-plan-emitter-fail!
           "P15S23Q001" emitter-source-path nil
           {:requested-source request-source
            :target :jvm
            :missing-fact :stage2-compiler-source-regular-file
            :regular-file? (.isRegularFile emitter-before)
            :symbolic-link? (.isSymbolicLink emitter-before)
            :expected-byte-count
            p15-s23-stage2-compiler-expected-source-byte-count
            :observed-byte-count (.size emitter-before)}))
        emitter-rule
        (c-backend-stage2-plan-emitter-source-rule!
         request-source :jvm)
        emitter-after
        (try
          (java.nio.file.Files/readAttributes
           emitter-path java.nio.file.attribute.BasicFileAttributes nofollow)
          (catch Exception _
            (p15-s23-stage2-plan-emitter-fail!
             "P15S23Q001" emitter-source-path nil
             {:requested-source request-source
              :target :jvm
              :missing-fact :stage2-compiler-source-stable-snapshot})))
        returned-emitter-path
        (p15-s23-c6c10-canonical-file-path (:source-path emitter-rule))
        expected-emitter-path
        (p15-s23-c6c10-canonical-file-path emitter-source-path)]
    (when-not
     (and (.isRegularFile emitter-after)
          (not (.isSymbolicLink emitter-after))
          (= (.fileKey emitter-before) (.fileKey emitter-after))
          (= (.size emitter-before) (.size emitter-after)
             (long p15-s23-stage2-compiler-expected-source-byte-count))
          (= (.lastModifiedTime emitter-before)
             (.lastModifiedTime emitter-after))
          (= expected-emitter-path returned-emitter-path))
      (p15-s23-stage2-plan-emitter-fail!
       "P15S23Q001" emitter-source-path nil
       {:requested-source request-source
        :target :jvm
        :missing-fact :stage2-compiler-source-stable-snapshot
        :regular-file? (.isRegularFile emitter-after)
        :symbolic-link? (.isSymbolicLink emitter-after)
        :stable-file-key?
        (= (.fileKey emitter-before) (.fileKey emitter-after))
        :stable-last-modified-time?
        (= (.lastModifiedTime emitter-before)
           (.lastModifiedTime emitter-after))
        :expected-byte-count
        p15-s23-stage2-compiler-expected-source-byte-count
        :observed-byte-count (.size emitter-after)
        :expected-source-path expected-emitter-path
        :observed-source-path returned-emitter-path}))
    (p15-s23-c6c10-record-compiled-binding-metric!
     :emitter-authenticated)
    {:source-snapshot source-snapshot
     :emitter-rule emitter-rule}))