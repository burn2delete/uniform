;; Semantic C2 pass-cache component loaded by gravity.c2-pass-cache.
;; It deliberately evaluates in the facade namespace to retain private test seams.
(defn bounded-source-snapshot!
  "Read a no-follow regular source file into one bounded immutable snapshot.

  The returned byte array is an execution input, not part of the key record;
  only its byte count and SHA-256 identity enter the semantic preimage."
  ([path]
   (bounded-source-snapshot! path maximum-source-bytes))
  ([path byte-limit]
   (when-not (and (integer? byte-limit)
                  (pos? byte-limit)
                  (<= byte-limit maximum-source-bytes))
     (cache-fail! "C16-KEY" "source snapshot bound is invalid"
                  {:maximum-source-bytes maximum-source-bytes
                   :requested byte-limit}))
   (with-contained-host-errors
    "C16-KEY" :bounded-source-snapshot
    (fn []
      (let [source-path (normalized-absolute-path! path)
            parent (.getParent source-path)
            relative (.getFileName source-path)
            parent-identity (directory-identity parent false)]
        (with-open [raw-parent (Files/newDirectoryStream parent)]
          (let [parent-stream
                (require-secure-directory-stream!
                 raw-parent :bounded-source-snapshot)
                _ (verify-secure-directory-handle! parent-stream
                                                   parent-identity)
                {:keys [^BasicFileAttributes basic
                        ^PosixFileAttributes posix]}
                (secure-child-attributes parent-stream relative)
                links (correlated-unix-link-count! source-path basic
                                                   "C16-KEY")]
            (when-not (and (.isRegularFile basic)
                           (not (.isSymbolicLink basic))
                           (= 1 links)
                           (= (current-owner-name) (.getName (.owner posix)))
                           (<= 0 (.size basic) byte-limit))
              (cache-fail!
               "C16-KEY" "source snapshot is not a bounded secure file"
               {:path (str source-path)
                :regular-file? (.isRegularFile basic)
                :symbolic-link? (.isSymbolicLink basic)
                :link-count links
                :owner (.getName (.owner posix))
                :required-owner (current-owner-name)
                :observed-bytes (.size basic)
                :maximum-bytes byte-limit}))
            (let [expected-size (long (.size basic))
                  raw-channel
                  (.newByteChannel ^SecureDirectoryStream parent-stream relative
                                   (HashSet. [StandardOpenOption/READ
                                              LinkOption/NOFOLLOW_LINKS])
                                   (make-array FileAttribute 0))
                  channel
                  (require-file-channel! raw-channel "C16-KEY"
                                         :bounded-source-snapshot)]
              (with-open [channel channel]
                (let [first-bytes
                      (read-channel-exact! channel expected-size "C16-KEY"
                                           "source snapshot")
                      _ (.position channel 0)
                      second-bytes
                      (read-channel-exact! channel expected-size "C16-KEY"
                                           "source snapshot recheck")
                      after-attributes
                      (secure-child-attributes parent-stream relative)
                      ^BasicFileAttributes after (:basic after-attributes)
                      ^PosixFileAttributes after-posix
                      (:posix after-attributes)
                      after-links
                      (correlated-unix-link-count! source-path after "C16-KEY")]
                  (when-not (and (= expected-size (.size channel))
                                 (same-basic-file? basic after)
                                 (= 1 after-links)
                                 (= (.getName (.owner posix))
                                    (.getName (.owner after-posix)))
                                 (= (.permissions posix)
                                    (.permissions after-posix))
                                 (java.util.Arrays/equals
                                  ^bytes first-bytes ^bytes second-bytes)
                                 (= (digest/sha256-bytes-hex first-bytes)
                                    (digest/sha256-bytes-hex second-bytes)))
                    (cache-fail! "C16-KEY"
                                 "source changed during stable double snapshot"
                                 {:path (str source-path)}))
                  (verify-secure-directory-handle! parent-stream
                                                   parent-identity)
                  (verify-directory-identity! parent-identity)
                  {:artifact :gravity/bounded-source-snapshot
                   :schema-version 1
                   :canonical-path (str source-path)
                   :byte-count (alength first-bytes)
                   :bytes-hash
                   (str "sha256:"
                        (digest/sha256-bytes-hex first-bytes))
                   :bytes first-bytes
                   :maximum-source-bytes byte-limit}))))))))))
