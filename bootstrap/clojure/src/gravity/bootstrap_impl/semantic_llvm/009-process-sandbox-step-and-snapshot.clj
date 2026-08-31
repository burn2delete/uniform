(defn- p15-s23-b3-llvm-run-step!
  ([candidate directory source-path step command]
   (p15-s23-b3-llvm-run-step!
    candidate directory source-path step command #{0}))
  ([candidate directory source-path step command accepted-exit-codes]
   (p15-s23-b3-llvm-require-authority!
    candidate source-path :run-closed-tool-step)
   (when-not (= "docker" (first command))
     (p15-s23-b3-llvm-fail!
      "B3-TARGET" source-path {}
      {:missing-fact :host-tool-launcher-forbidden
       :tool-step step}))
   (when-not (and (p15-s23-b3-llvm-closed-tool-command? step command)
                  (set? accepted-exit-codes)
                  (= 1 (count accepted-exit-codes))
                  (every? #(and (integer? %) (<= 0 % 255))
                          accepted-exit-codes))
     (p15-s23-b3-llvm-fail!
      "B3-TARGET" source-path {}
      {:missing-fact :closed-enumerated-b3-tool-command
       :tool-step step}))
   (swap! p15-s23-b3-llvm-tool-observation-state
          (fn [state]
            (-> state
                (update :total inc)
                (update-in [:steps step] (fnil inc 0)))))
   (let [result
        (try
          (p15-s23-b3-llvm-run-process
           candidate directory command p15-s23-b3-llvm-tool-timeout-ms
           source-path)
          (catch InterruptedException interrupted
            (.interrupt (Thread/currentThread))
            (throw interrupted))
          (catch Exception exception
            (p15-s23-b3-llvm-fail!
             "B3-TARGET" source-path {}
             {:missing-fact :bounded-tool-process-start
              :tool-step step
              :stderr-hash
              (str "sha256:"
                   (sha256-hex (.getName (class exception))))})))
        record (p15-s23-b3-llvm-tool-record step command result)]
    (when-not (and (:finished? result)
                   (not (:timed-out? result))
                   (contains? accepted-exit-codes (:exit-code result))
                   (not (get-in result [:stdout :truncated?]))
                   (not (get-in result [:stderr :truncated?])))
      (p15-s23-b3-llvm-fail!
       (case step
         :llvm-to-object "B3-PASS"
         :link "B3-ABI"
         :run "B14-DIFFERENTIAL"
         :runtime-providers "B3-RUNTIME"
         "B3-TARGET")
       source-path {}
       {:missing-fact :bounded-successful-tool-step
        :tool-step step
        :exit-code (:exit-code result)
        :stdout-byte-count (get-in result [:stdout :total-byte-count])
        :stderr-byte-count (get-in result [:stderr :total-byte-count])
        :stdout-hash (get-in result [:stdout :hash])
        :stderr-hash (get-in result [:stderr :hash])
        :timed-out? (:timed-out? result)}))
    {:record record :result result})))

(defn- p15-s23-b3-llvm-file-snapshot!
  "Read one stable regular-file snapshot.  The returned bytes are the only
  bytes consumers may parse, hash, or publish for this observation."
  [candidate root path source-path operation maximum-byte-count]
  (p15-s23-b3-llvm-require-authority!
   candidate source-path operation)
  (let [root (.normalize (.toAbsolutePath ^java.nio.file.Path root))
        path (.normalize (.toAbsolutePath ^java.nio.file.Path path))
        nofollow
        (into-array java.nio.file.LinkOption
                    [java.nio.file.LinkOption/NOFOLLOW_LINKS])]
    (when-not (and (java.nio.file.Files/isDirectory root nofollow)
                   (not (java.nio.file.Files/isSymbolicLink root))
                   (.startsWith path root)
                   (not= root path)
                   (= root (.getParent path))
                   (integer? maximum-byte-count)
                   (pos? maximum-byte-count)
                   (<= maximum-byte-count
                       p15-s23-b3-llvm-max-emitted-file-bytes)
                   (not (java.nio.file.Files/isSymbolicLink path)))
      (p15-s23-b3-llvm-fail!
       "B3-MANIFEST" source-path {}
       {:missing-fact :contained-nofollow-regular-file-snapshot
        :bounded-reason operation}))
    (let [before
          (try
            (java.nio.file.Files/readAttributes
             path java.nio.file.attribute.BasicFileAttributes nofollow)
            (catch Exception exception
              (p15-s23-b3-llvm-fail!
               "B3-MANIFEST" source-path {}
               {:missing-fact :fresh-regular-contained-tool-output
                :bounded-reason operation})))
          _
          (when-not (and (.isRegularFile before)
                         (not (.isSymbolicLink before))
                         (some? (.fileKey before))
                         (<= (.size before) maximum-byte-count))
            (p15-s23-b3-llvm-fail!
             "B3-MANIFEST" source-path {}
             {:missing-fact :bounded-nofollow-regular-file
              :bounded-reason operation
              :maximum-byte-count maximum-byte-count
              :observed-byte-count (.size before)}))
          observed
          (with-open [channel
                      (java.nio.channels.FileChannel/open
                       path
                       (into-array
                        java.nio.file.OpenOption
                        [java.nio.file.StandardOpenOption/READ
                         java.nio.file.LinkOption/NOFOLLOW_LINKS]))
                      input (java.nio.channels.Channels/newInputStream channel)
                      output (java.io.ByteArrayOutputStream.)]
            (let [buffer (byte-array 4096)
                  digest
                  (java.security.MessageDigest/getInstance "SHA-256")]
              (loop [total 0]
                (let [read (.read input buffer)]
                  (if (neg? read)
                    {:bytes (.toByteArray output)
                     :byte-count total
                     :content-hash (str "sha256:"
                                        (apply str
                                         (map #(format "%02x"
                                                       (bit-and % 0xff))
                                              (.digest digest))))}
                    (let [next-total (+ total read)]
                      (when (> next-total maximum-byte-count)
                        (p15-s23-b3-llvm-fail!
                         "B3-MANIFEST" source-path {}
                         {:missing-fact :bounded-emitted-artifact-size
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
        (p15-s23-b3-llvm-fail!
         "B13-HASH" source-path {}
         {:missing-fact :stable-single-read-file-snapshot
          :bounded-reason operation}))
      (assoc observed
             :file-key-hash
             (str "sha256:" (sha256-hex (str (.fileKey after))))
             :last-modified-millis (.toMillis (.lastModifiedTime after))))))
