(defn p15-s23-c11-mir-source-binding!*
  "Compile and authenticate the exact Gravity-authored C11 builder source.
  The executable plan is retained only for the opaque constructor/replay
  boundary; public artifacts receive hashes and function shapes, not a mutable
  plan that can be substituted after authentication."
  ([request-source requested-target]
   (p15-s23-c11-mir-source-binding!* request-source requested-target 1))
  ([request-source requested-target version]
  (let [contract (p15-s23-c11-mir-contract version)
        required-functions (:required-functions contract)
        builder-function (p15-s23-c11-mir-selected-builder-function version)
        verifier-function (p15-s23-c11-mir-selected-verifier-function version)
        source-path (p15-s23-c11-mir-resolve-source-path)]
    (when-not (.isFile (java.io.File. source-path))
      (p15-s23-c11-mir-fail!
       "C11-MODULE" request-source {}
       {:missing-fact :pinned-gravity-c11-source
        :requested-target requested-target}))
    (let [source-file (java.io.File. source-path)
          nofollow (into-array java.nio.file.LinkOption [java.nio.file.LinkOption/NOFOLLOW_LINKS])
          ^java.nio.file.attribute.BasicFileAttributes source-before (when (= 3 version)
                          (java.nio.file.Files/readAttributes
                           (.toPath source-file) java.nio.file.attribute.BasicFileAttributes nofollow))
          _source-shape
          (when (and source-before
                     (not (and (.isRegularFile source-before)
                               (not (.isSymbolicLink source-before)))))
            (p15-s23-c11-mir-fail! "C11-VERIFY" request-source {}
              {:missing-fact :stable-selected-c11-source}))
          observed-file-bytes
          (java.nio.file.Files/size (.toPath source-file))
          _
          (when-not (= (:source-byte-count contract)
                       observed-file-bytes)
            (p15-s23-c11-mir-fail!
             "C11-VERIFY" request-source {}
             {:missing-fact :pinned-gravity-c11-source-byte-size
              :expected-source-bytes (:source-byte-count contract)
              :observed-source-bytes observed-file-bytes
              :requested-target requested-target}))
          raw-source (when (not= 1 version)
                       (java.nio.file.Files/readAllBytes (.toPath source-file)))
          source-text (if raw-source
                        (str (.decode (doto (.newDecoder java.nio.charset.StandardCharsets/UTF_8)
                                        (.onMalformedInput java.nio.charset.CodingErrorAction/REPORT)
                                        (.onUnmappableCharacter java.nio.charset.CodingErrorAction/REPORT))
                                      (java.nio.ByteBuffer/wrap raw-source)))
                        (slurp source-file))
          source-bytes (alength (.getBytes
                                 source-text
                                 java.nio.charset.StandardCharsets/UTF_8))
          source-content-hash (str "sha256:" (sha256-hex source-text))]
      (when-not (and (or (nil? raw-source)
                        (java.util.Arrays/equals
                         ^bytes raw-source
                         ^bytes (.getBytes source-text java.nio.charset.StandardCharsets/UTF_8)))
                     (= (:source-byte-count contract) source-bytes)
                     (= (:source-content-hash contract)
                        source-content-hash))
        (p15-s23-c11-mir-fail!
         "C11-VERIFY" request-source {}
         {:missing-fact :pinned-gravity-c11-source-identity
          :expected-source-bytes (:source-byte-count contract)
          :observed-source-bytes source-bytes
          :expected-source-content-hash
          (:source-content-hash contract)
          :observed-source-content-hash source-content-hash}))
      (let [emitter-rule
            (c-backend-stage2-plan-emitter-source-rule!
             request-source requested-target)
            plan
            (try
              (p15-s23-stage2-compiler-artifact-plan
               (:emitter emitter-rule) source-path source-text)
              (catch clojure.lang.ExceptionInfo ex
                (p15-s23-c11-mir-fail!
                 "C11-VERIFY" request-source {}
                 {:missing-fact :gravity-c11-source-compilation
                  :cause-diagnostic (:id (ex-data ex))})))
            functions (:functions plan)
            _ (when (and (= 3 version)
                         (not= source-content-hash (get-in plan [:source :sha256])))
                (p15-s23-c11-mir-fail! "C11-VERIFY" request-source {}
                  {:missing-fact :selected-c11-plan-source-identity}))
            observed-shapes
            (into {}
                  (map (fn [[name _]]
                         [name (select-keys (get functions name)
                                            [:arity :params])]))
                  required-functions)
            _selected-shapes
            (when (and (= 3 version) (not= required-functions observed-shapes))
              (p15-s23-c11-mir-fail! "C11-MODULE" request-source {}
                {:missing-fact :pinned-gravity-c11-function-shapes
                 :observed-function-shapes observed-shapes}))
            plan-semantic-hash
            (p15-s23-c11-mir-digest
             (p15-s23-stage2-compiler-artifact-semantic-input plan))
            functions-semantic-hash
            (p15-s23-c11-mir-digest functions)
            builder-semantic-hash
            (p15-s23-c11-mir-digest
             (get functions builder-function))
            verifier-semantic-hash
            (p15-s23-c11-mir-digest
             (get functions verifier-function))]
        (when-not (= required-functions observed-shapes)
          (p15-s23-c11-mir-fail!
           "C11-MODULE" request-source {}
           {:missing-fact :pinned-gravity-c11-function-shapes
            :observed-function-shapes observed-shapes}))
        (when-not
         (and (= (:plan-semantic-hash contract)
                 plan-semantic-hash)
              (= (:functions-semantic-hash contract)
                 functions-semantic-hash)
              (= (:builder-semantic-hash contract)
                 builder-semantic-hash)
              (= (:verifier-semantic-hash contract)
                 verifier-semantic-hash))
          (p15-s23-c11-mir-fail!
           "C11-VERIFY" request-source {}
           {:missing-fact :pinned-gravity-c11-function-identity
            :observed-plan-semantic-hash plan-semantic-hash
            :observed-functions-semantic-hash functions-semantic-hash
            :observed-builder-semantic-hash builder-semantic-hash
            :observed-verifier-semantic-hash verifier-semantic-hash}))
        (when (= 3 version)
          (let [after-bytes (java.nio.file.Files/readAllBytes (.toPath source-file))
                ^java.nio.file.attribute.BasicFileAttributes source-after (java.nio.file.Files/readAttributes
                              (.toPath source-file) java.nio.file.attribute.BasicFileAttributes nofollow)]
            (when-not (and (.isRegularFile source-after) (not (.isSymbolicLink source-after))
                           (= (.fileKey source-before) (.fileKey source-after))
                           (= (.size source-before) (.size source-after) (:source-byte-count contract))
                           (= (.lastModifiedTime source-before) (.lastModifiedTime source-after))
                           (java.util.Arrays/equals ^bytes raw-source ^bytes after-bytes))
              (p15-s23-c11-mir-fail! "C11-VERIFY" request-source {}
                {:missing-fact :stable-selected-c11-source}))))
        (cond-> {:source-path source-path
         :source-content-hash source-content-hash
         :source-byte-count source-bytes
         :plan-semantic-hash plan-semantic-hash
         :functions-semantic-hash functions-semantic-hash
         :builder-semantic-hash builder-semantic-hash
         :verifier-semantic-hash verifier-semantic-hash
         :function-shapes observed-shapes
         :plan plan}
          (not= 1 version) (assoc :interface-version version)
          (= 3 version) (assoc :semantic-family (:semantic-family contract)
                              :builder-function builder-function
                              :verifier-function verifier-function)))))))
