

(def p15-s23-stage2-runtime-artifact-source-relative-path
  "bootstrap/gravity/p15_s23/runtime.gravity")

(def p15-s23-stage2-runtime-artifact-function
  'p15-s23-runtime-format-value)

(def p15-s23-stage2-runtime-artifact-concat-function
  'p15-s23-runtime-concat)

(def p15-s23-stage2-runtime-artifact-println-function
  'p15-s23-runtime-println-value)

(def p15-s23-stage2-runtime-artifact-println-two-function
  'p15-s23-runtime-println-two)

(def p15-s23-stage2-runtime-artifact-closed-plan-function
  'p15-s23-runtime-execute-closed-plan)

(def p15-s23-stage2-runtime-artifact-closed-plan-helper-functions
  '#{p15-s23-runtime-evaluate-arguments
     p15-s23-runtime-println-output
     p15-s23-runtime-evaluate-bindings
     p15-s23-runtime-evaluate-sequence
     p15-s23-runtime-evaluate-closed-instruction})

(def p15-s23-reference-runtime-function-set
  '#{main
     p15-s23-runtime-format-value
     p15-s23-runtime-concat
     p15-s23-runtime-println-value
     p15-s23-runtime-println-two
     p15-s23-runtime-evaluate-arguments
     p15-s23-runtime-println-output
     p15-s23-runtime-evaluate-bindings
     p15-s23-runtime-evaluate-sequence
     p15-s23-runtime-evaluate-closed-instruction
     p15-s23-runtime-execute-closed-plan})

(def p15-s23-reference-runtime-contract-definition-names
  '#{p15-s23-reference-runtime-contract
     p15-s23-checked-core-program-authority-policy
     p15-s23-checked-core-verification-replay-policy
     p15-s23-checked-core-verification-replay-audit-policy
     p15-s23-reference-runtime-function-graph
     p15-s23-reference-runtime-function-effects
     p15-s23-reference-runtime-effect-graph
     p15-s23-reference-managed-allocator-provider
     p15-s23-reference-transcript-capture-provider
     p15-s23-reference-transcript-capture-handler
     p15-s23-reference-runtime-provider-selections
     p15-s23-reference-runtime-capability-proofs
     p15-s23-reference-stdout-deployment-requirement
     p15-s23-reference-runtime-service-manifest
     p15-s23-reference-managed-runtime-adapter
     p15-s23-reference-memory-provider-manifest
     p15-s23-reference-runtime-failure-policy
     p15-s23-reference-runtime-audit-policy
     p15-s23-reference-runtime-capability-manifest
     p15-s23-reference-runtime-capability-table
     p15-s23-reference-runtime-observability-manifest
     p15-s23-reference-runtime-grant-records})

(def p15-s23-stage2-runtime-artifact-expected-source-content-hash
  "sha256:ec1a94a979d8464492a904587913d6d1634161e5cefaf6e0bc4db977365d7230")

(def p15-s23-stage2-compiler-expected-source-content-hash
  "sha256:f0c5f30518dc8ddb9979bea3344b6b29c512a9a325e6c496bbe6be29ef55a673")

(def p15-s23-stage2-compiler-expected-source-byte-count 116505)
(def p15-s23-stage2-runtime-artifact-expected-source-byte-count 75905)

(defn p15-s23-stage2-compiler-pinned-source!
  "Read and authenticate compiler.gravity before any of its bytes are parsed,
  extracted, or interpreted.  The caller supplies its own diagnostic family;
  hostile source text is never copied into the rejection payload."
  [compiler-source requested-source target diagnostic-id fail-fn]
  (let [path (.toPath (java.io.File. compiler-source))
        nofollow
        (into-array java.nio.file.LinkOption
                    [java.nio.file.LinkOption/NOFOLLOW_LINKS])
        before
        (try
          (java.nio.file.Files/readAttributes
           path java.nio.file.attribute.BasicFileAttributes nofollow)
          (catch Exception _
            (fail-fn
             diagnostic-id compiler-source nil
             {:requested-source requested-source
              :target target
              :missing-fact :stage2-compiler-source-byte-count
              :expected-byte-count
              p15-s23-stage2-compiler-expected-source-byte-count})))
        byte-count (.size before)]
    (when-not
     (and (.isRegularFile before)
          (not (.isSymbolicLink before))
          (= (long p15-s23-stage2-compiler-expected-source-byte-count)
             byte-count))
      (fail-fn
       diagnostic-id compiler-source nil
       {:requested-source requested-source
        :target target
        :missing-fact :stage2-compiler-source-byte-count
        :regular-file? (.isRegularFile before)
        :symbolic-link? (.isSymbolicLink before)
        :expected-byte-count
        p15-s23-stage2-compiler-expected-source-byte-count
        :observed-byte-count byte-count}))
    (let [maximum-byte-count
          (inc p15-s23-stage2-compiler-expected-source-byte-count)
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
              (fail-fn
               diagnostic-id compiler-source nil
               {:requested-source requested-source
                :target target
                :missing-fact :stage2-compiler-source-bytes
                :maximum-byte-count maximum-byte-count})))
          after
          (try
            (java.nio.file.Files/readAttributes
             path java.nio.file.attribute.BasicFileAttributes nofollow)
            (catch Exception _
              (fail-fn
               diagnostic-id compiler-source nil
               {:requested-source requested-source
                :target target
                :missing-fact :stage2-compiler-source-stable-snapshot})))
          bytes (java.util.Arrays/copyOf buffer observed-byte-count)
          content-hash (str "sha256:" (sha256-bytes-hex bytes))]
      (when-not
       (= p15-s23-stage2-compiler-expected-source-byte-count
          observed-byte-count)
        (fail-fn
         diagnostic-id compiler-source nil
         {:requested-source requested-source
          :target target
          :missing-fact :stage2-compiler-source-byte-count
          :expected-byte-count
          p15-s23-stage2-compiler-expected-source-byte-count
          :observed-byte-count observed-byte-count}))
      (when-not (= p15-s23-stage2-compiler-expected-source-content-hash
                   content-hash)
        (fail-fn
         diagnostic-id compiler-source nil
         {:requested-source requested-source
          :target target
          :missing-fact :stage2-compiler-source-content-hash
          :expected-source-content-hash
          p15-s23-stage2-compiler-expected-source-content-hash
          :observed-source-content-hash content-hash}))
      (when-not
       (and (.isRegularFile after)
            (not (.isSymbolicLink after))
            (= (.fileKey before) (.fileKey after))
            (= (.size before) (.size after)
               (long observed-byte-count))
            (= (.lastModifiedTime before)
               (.lastModifiedTime after)))
        (fail-fn
         diagnostic-id compiler-source nil
         {:requested-source requested-source
          :target target
          :missing-fact :stage2-compiler-source-stable-snapshot
          :regular-file? (.isRegularFile after)
          :symbolic-link? (.isSymbolicLink after)
          :stable-file-key? (= (.fileKey before) (.fileKey after))
          :stable-last-modified-time?
          (= (.lastModifiedTime before)
             (.lastModifiedTime after))
          :expected-byte-count
          p15-s23-stage2-compiler-expected-source-byte-count
          :observed-byte-count (.size after)}))
      {:source-text
       (try
         (let [decoder
               (doto (.newDecoder java.nio.charset.StandardCharsets/UTF_8)
                 (.onMalformedInput
                  java.nio.charset.CodingErrorAction/REPORT)
                 (.onUnmappableCharacter
                  java.nio.charset.CodingErrorAction/REPORT))]
           (.toString
            (.decode decoder (java.nio.ByteBuffer/wrap bytes))))
         (catch java.nio.charset.CharacterCodingException _
           (fail-fn
            diagnostic-id compiler-source nil
            {:requested-source requested-source
             :target target
             :missing-fact :stage2-compiler-source-utf8})))
       :source-byte-count observed-byte-count
       :source-content-hash content-hash})))

(defn p15-s23-compiler-source-form-record-from-text
  [source-path source-text]
  (let [records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)]
    {:source-text source-text
     :records records
     :forms forms
     :module module}))

(def p15-s23-stage2-runtime-artifact-expected-artifact-hash
  "sha256:6072cf2cb0d7d45ab4ece8e48190f5caed6597f882a585ebb0cdef1272939c11")

(def p15-s23-reference-runtime-expected-contract-definition-hash
  "sha256:e29579c1c229e5633f93b9054a9b4d5af4ad0ef449b6a61601df97e4a94ad8e6")