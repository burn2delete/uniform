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
        source-path (p15-s23-c11-mir-resolve-source-path)]
    (when-not (.isFile (java.io.File. source-path))
      (p15-s23-c11-mir-fail!
       "C11-MODULE" request-source {}
       {:missing-fact :pinned-gravity-c11-source
        :requested-target requested-target}))
    (let [source-file (java.io.File. source-path)
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
          raw-source (when (= 2 version)
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
            observed-shapes
            (into {}
                  (map (fn [[name _]]
                         [name (select-keys (get functions name)
                                            [:arity :params])]))
                  required-functions)
            plan-semantic-hash
            (p15-s23-c11-mir-digest
             (p15-s23-stage2-compiler-artifact-semantic-input plan))
            functions-semantic-hash
            (p15-s23-c11-mir-digest functions)
            builder-semantic-hash
            (p15-s23-c11-mir-digest
             (get functions p15-s23-c11-mir-builder-function))
            verifier-semantic-hash
            (p15-s23-c11-mir-digest
             (get functions p15-s23-c11-mir-verifier-function))]
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
        (cond-> {:source-path source-path
         :source-content-hash source-content-hash
         :source-byte-count source-bytes
         :plan-semantic-hash plan-semantic-hash
         :functions-semantic-hash functions-semantic-hash
         :builder-semantic-hash builder-semantic-hash
         :verifier-semantic-hash verifier-semantic-hash
         :function-shapes observed-shapes
         :plan plan}
          (= 2 version) (assoc :interface-version version)))))))
