

(defn p18-t04-verified-mir-c-source-input!
  [source-path]
  (let [character-count (when (string? source-path) (count source-path))
        utf8-byte-count
        (when (and (string? source-path)
                   (<= 1 character-count 4096))
          (alength (.getBytes ^String source-path
                             java.nio.charset.StandardCharsets/UTF_8)))]
    (when-not
     (and (string? source-path)
          (<= 1 character-count 4096)
          (<= 1 (or utf8-byte-count 0) 4096)
          (not (str/blank? source-path))
          (not (str/includes? source-path "\u0000"))
          (not-any? #(Character/isISOControl ^char %) source-path))
      (p18-t04-verified-mir-c-source-snapshot-fail!
       (if (string? source-path) source-path
           "<verified-mir-c-source>")
       :bounded-public-gravity-source-path
       :invalid-source-path-spelling {})))
  (try
    (let [requested-path
          (java.nio.file.Paths/get source-path (make-array String 0))
          _
          (when (java.nio.file.Files/isSymbolicLink requested-path)
            (p18-t04-verified-mir-c-source-snapshot-fail!
             source-path :bounded-readable-regular-gravity-source
             :source-final-component-is-symbolic-link
             {:maximum-byte-count
              p18-t04-verified-mir-c-maximum-source-bytes}))
          requested-actual-path-before
          (.toString
           (.toRealPath requested-path
                        (make-array java.nio.file.LinkOption 0)))
          binding
          (p18-t04-verified-mir-c-source-native-binding! source-path)
          snapshot
          (with-open [arena (java.lang.foreign.Arena/ofConfined)]
            (let [state (.allocate arena (:state-layout binding))
                  path-stat-before
                  (p18-t04-verified-mir-c-source-native-path-stat!
                   binding arena state source-path requested-actual-path-before)]
              (p18-t04-verified-mir-c-with-source-descriptor!
               binding arena state source-path requested-actual-path-before
               (fn [file-descriptor]
                 (let [descriptor-stat-before
                       (p18-t04-verified-mir-c-source-native-fstat!
                        binding arena state source-path file-descriptor)
                       descriptor-path-before
                       (p18-t04-verified-mir-c-source-native-fd-path!
                        binding arena state source-path file-descriptor)
                       descriptor-path-stat-before
                       (p18-t04-verified-mir-c-source-native-path-stat!
                        binding arena state source-path
                        descriptor-path-before)
                       initial-byte-count (:byte-count descriptor-stat-before)
                       regular?
                       (= p18-t04-darwin-s-ifreg
                          (bit-and (:mode descriptor-stat-before)
                                   p18-t04-darwin-s-ifmt))
                       _
                       (when-not
                        (and regular?
                             (= path-stat-before descriptor-stat-before
                                descriptor-path-stat-before)
                             (<= 0 initial-byte-count
                                 p18-t04-verified-mir-c-maximum-source-bytes))
                         (p18-t04-verified-mir-c-source-snapshot-fail!
                          source-path
                          :bounded-readable-regular-gravity-source
                          :path-and-opened-descriptor-identity-mismatch
                          {:maximum-byte-count
                           p18-t04-verified-mir-c-maximum-source-bytes
                           :observed-byte-count initial-byte-count}))
                       bytes
                       (p18-t04-verified-mir-c-source-native-read-bytes!
                        binding arena state source-path file-descriptor
                        initial-byte-count)
                       descriptor-stat-after
                       (p18-t04-verified-mir-c-source-native-fstat!
                        binding arena state source-path file-descriptor)
                       descriptor-path-after
                       (p18-t04-verified-mir-c-source-native-fd-path!
                        binding arena state source-path file-descriptor)
                       descriptor-path-stat-after
                       (p18-t04-verified-mir-c-source-native-path-stat!
                        binding arena state source-path descriptor-path-after)
                       requested-path-stat-after
                       (p18-t04-verified-mir-c-source-native-path-stat!
                        binding arena state source-path
                        requested-actual-path-before)
                       requested-actual-path-after
                       (.toString
                        (.toRealPath
                         requested-path
                         (make-array java.nio.file.LinkOption 0)))]
                   (when-not
                    (and (= path-stat-before descriptor-stat-before
                            descriptor-path-stat-before
                            descriptor-stat-after
                            descriptor-path-stat-after
                            requested-path-stat-after)
                         (= requested-actual-path-before
                            requested-actual-path-after)
                         (not (java.nio.file.Files/isSymbolicLink
                               requested-path))
                         (= descriptor-path-before descriptor-path-after)
                         (= (alength bytes)
                            (:byte-count descriptor-stat-after)))
                     (p18-t04-verified-mir-c-source-snapshot-fail!
                      source-path :stable-bounded-source-snapshot
                      :source-path-or-descriptor-mutated-during-snapshot
                      {:observed-byte-count (alength bytes)
                       :after-byte-count
                       (:byte-count descriptor-stat-after)}))
                   {:actual-path descriptor-path-after
                    :identity descriptor-stat-after
                    :bytes bytes})))))
          bytes (:bytes snapshot)
          actual-path (:actual-path snapshot)
          _
          (when-not (qst-or-gravity-source? source-path)
            (source-path-policy-fail! source-path bytes))
          source-text (decode-gravity-source-bytes actual-path bytes)
          content-hash (str "sha256:" (sha256-bytes-hex bytes))]
      {:source-path actual-path
       :source-text source-text
       :source-snapshot-evidence
       {:kind :gravity/p18-t04-bounded-source-snapshot
        :schema-version 1
        :policy-id
        (p15-s23-c11-mir-digest
         (:source-snapshot-policy
          p18-t04-experimental-verified-mir-c-route-policy))
        :actual-path actual-path
        :file-key-hash
        (p15-s23-c11-mir-digest
         {:kind :gravity/p18-t04-source-file-key
          :schema-version 1
          :device (get-in snapshot [:identity :device])
          :inode (get-in snapshot [:identity :inode])})
        :byte-count (alength bytes)
        :content-hash content-hash
        :capture-provider :jdk26-ffm-darwin-libsystem
        :native-functions
        ["fstatat" "open" "fstat" "fcntl" "read" "close"]
        :identity-observation-phase-count
        (count
         (get-in p18-t04-experimental-verified-mir-c-route-policy
                 [:source-snapshot-policy :identity-observation-phases]))
        :source-byte-path-reopen-count 0
        :opened-handle-file-key-observation
        :native-fstat-device-and-inode
        :opened-handle-size-parity? true
        :path-and-descriptor-identity-parity? true
        :native-access-enabled?
        (.isNativeAccessEnabled (.getModule clojure.lang.RT))
        :status :captured}})
    (catch java.nio.file.NoSuchFileException _
      (p18-t04-verified-mir-c-source-snapshot-fail!
       source-path :bounded-readable-regular-gravity-source
       :missing-nonregular-unreadable-or-oversize-source
       {:maximum-byte-count
        p18-t04-verified-mir-c-maximum-source-bytes}))
    (catch java.nio.file.AccessDeniedException _
      (p18-t04-verified-mir-c-source-snapshot-fail!
       source-path :bounded-readable-regular-gravity-source
       :missing-nonregular-unreadable-or-oversize-source
       {:maximum-byte-count
        p18-t04-verified-mir-c-maximum-source-bytes}))
    (catch java.nio.file.InvalidPathException _
      (p18-t04-verified-mir-c-source-snapshot-fail!
       source-path :readable-gravity-source :invalid-source-path {}))
    (catch java.nio.channels.ClosedByInterruptException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch java.io.InterruptedIOException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch UnsupportedOperationException _
      (p18-t04-verified-mir-c-source-snapshot-fail!
       source-path :descriptor-bound-source-snapshot
       :source-provider-lacks-descriptor-bound-capture {}))
    (catch SecurityException _
      (p18-t04-verified-mir-c-source-snapshot-fail!
       source-path :readable-gravity-source :source-security-denial {}))
    (catch java.io.IOException _
      (p18-t04-verified-mir-c-source-snapshot-fail!
       source-path :readable-gravity-source :source-io-failure {}))
    (catch clojure.lang.ExceptionInfo exception
      (throw exception))
    (catch Exception _
      (p18-t04-verified-mir-c-source-snapshot-fail!
       source-path :descriptor-bound-source-snapshot
       :contained-source-snapshot-host-failure {}))))