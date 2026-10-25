(declare ^:private p15-s23-c6c10-digest-validated-form)

(defn- sh03-reader-authenticated-envelope-options
  ([source-path invocation] (sh03-reader-authenticated-envelope-options source-path invocation nil))
  ([source-path invocation expected-binding]
  (when (some? invocation)
    (when-not
     (and (= :passed (:status (p15-s23-trusted-carrier-validation invocation :default-only)))
          (= #{:reader-source-path :authenticated-envelope-options} (set (keys invocation)))
          (string? (:reader-source-path invocation))
          (= {:source :development-current} (:authenticated-envelope-options invocation)))
      (sh03-reader-boundary-fail!
       source-path :explicit-sh03-reader-invocation-source-selection nil {}))
    (sh03-reader-invocation-source! source-path (:reader-source-path invocation) expected-binding)
    (:authenticated-envelope-options invocation))))

(defn- sh03-reader-validate-envelope-options! [source-path options]
  (when (some? options)
    (when-not (and (= :passed (:status (p15-s23-trusted-carrier-validation options :default-only)))
                   (= {:source :development-current} options))
      (sh03-reader-boundary-fail!
       source-path :explicit-sh03-authenticated-envelope-selection nil {})))
  options)

(defn- sh03-reader-verification-invocation! [source-path invocation]
  (if (nil? invocation)
    {:binding (sh03-reader-current-binding! source-path) :envelope-options nil}
    (do
      (sh03-reader-authenticated-envelope-options source-path invocation)
      {:binding (sh03-reader-current-binding! source-path (:reader-source-path invocation))
       :envelope-options (:authenticated-envelope-options invocation)})))

(defn sh03-reader-canonical-identity [source-path value]
  (p15-s23-c6c10-canonical-identity source-path value))

(defn sh03-reader-canonical-digest [source-path value]
  (sh03-reader-canonical-stream-digest source-path value))

(defn- sh03-reader-resolve-digest-request-sequence!
  [source-path raw-result source-bytes]
  (let [requests (:digest-requests raw-result)
        request-count (count requests)
        expected-names
        (if (= :accepted (:status raw-result))
          sh03-reader-accepted-digest-request-names
          sh03-reader-rejected-digest-request-names)]
    (when-not (= expected-names (mapv :key requests))
      (sh03-reader-boundary-fail!
       source-path :exact-sh03-reader-digest-request-set
       (mapv :key requests) {:expected expected-names}))
    (loop [ordinal 0
           digests []
           resolved-requests []]
      (if (= ordinal request-count)
        {:resolved-digests digests :resolved-requests resolved-requests}
        (let [request (get requests ordinal)
              expected-keys (if (zero? ordinal)
                              sh03-reader-source-digest-request-keys
                              sh03-reader-digest-request-keys)]
          (when-not
           (and (map? request)
                (= expected-keys (set (keys request)))
                (= ordinal (:ordinal request))
                (= (get expected-names ordinal) (:key request))
                (= :sha256 (:algorithm request))
                (vector? (:depends-on request)))
            (sh03-reader-boundary-fail!
             source-path :exact-sh03-reader-digest-request-schema
             (dissoc request :preimage) {:ordinal ordinal}))
          (let [references
                (p15-s23-c6c10-collect-digest-ref-ordinals!
                 source-path (:preimage request) request-count ordinal)
                dependencies (:depends-on request)]
            (when-not
             (and (= dependencies (vec (sort (distinct dependencies))))
                  (= (set dependencies) (set references))
                  (every? #(and (integer? %) (<= 0 %) (< % ordinal))
                          dependencies))
              (sh03-reader-boundary-fail!
               source-path :prior-only-sh03-reader-digest-dependencies
               request {:ordinal ordinal :references references}))
            (let [resolved-preimage
                  (p15-s23-c6c10-resolve-digest-references!
                   source-path (:preimage request) request-count ordinal digests)
                  raw-request? (zero? ordinal)
                  _
                  (when-not (= (if raw-request?
                                 :raw-byte-vector-v1
                                 :gravity-canonical-edn-v1)
                               (:encoding request))
                    (sh03-reader-boundary-fail!
                     source-path :declared-sh03-reader-digest-encoding
                     request {:ordinal ordinal}))
                  _
                  (when (and raw-request?
                             (not= (sh03-reader-byte-vector source-bytes)
                                   resolved-preimage))
                    (sh03-reader-boundary-fail!
                     source-path :source-bound-sh03-reader-digest-request
                     request {:ordinal ordinal}))
                  digest
                  (if raw-request?
                    (str "sha256:"
                         (sha256-bytes-hex
                          (sh03-reader-byte-array resolved-preimage)))
                    ;; Validate the complete original-order preimage, then
                    ;; stream the same canonical EDN v1 bytes for its digest.
                    (sh03-reader-canonical-digest source-path resolved-preimage))]
              ;; Equal canonical forms have identical fixed encoding and digest.
              ;; Digest uniqueness therefore also rejects equal forms; this
              ;; implication does not require the hash to be injective.
              (when (or (some #{digest} digests)
                        (and raw-request? (not= digest (:observed-id request))))
                (sh03-reader-boundary-fail!
                 source-path :unique-bound-sh03-reader-digest
                 request {:ordinal ordinal :digest digest}))
              (recur
               (inc ordinal)
               (conj digests digest)
               (conj resolved-requests
                     (assoc request
                            :preimage resolved-preimage
                            :digest digest))))))))))

(defn sh03-reader-resolve-digest-requests!
  [source-path raw-result source-bytes]
  ;; Complete the identity-checking frame before allocating the final result.
  (let [{:keys [resolved-digests resolved-requests]}
        (sh03-reader-resolve-digest-request-sequence! source-path raw-result source-bytes)
        resolved-result
        (p15-s23-c6c10-resolve-digest-references!
         source-path (assoc raw-result :digest-requests [])
         (count resolved-requests) nil resolved-digests)
        resolved-result
        (assoc resolved-result :digest-requests resolved-requests
               :incremental-reader-hashes
               (assoc (:incremental-reader-hashes resolved-result) :status
                      (if (= :accepted (:status raw-result)) :stable :rejected)))]
    {:result resolved-result :resolved-digests resolved-digests
     :resolved-requests resolved-requests :reader-result-id (last resolved-digests)}))

(defn sh03-reader-resolve-diagnostic-id
  [source-path diagnostic]
  (let [request (:diagnostic-id-request diagnostic)]
    (when-not (map? request)
      (sh03-reader-boundary-fail!
       source-path :sh03-reader-diagnostic-id-request
       diagnostic {}))
    (-> diagnostic
        (dissoc :diagnostic-id-request)
        (assoc :diagnostic-id
               (sh03-reader-canonical-digest source-path request)))))

(defn sh03-reader-resolved-result!
  ([source-path source-bytes project-context reader-options]
   (sh03-reader-resolved-result! source-path source-bytes project-context reader-options nil))
  ([source-path source-bytes project-context reader-options reader-source-path]
  (when-not (qst-or-gravity-source? source-path)
    (try
      (source-path-policy-fail! source-path source-bytes)
      (catch clojure.lang.ExceptionInfo ex
        (c2-reader-remap-exception! source-path ex))))
  (let [binding (if (nil? reader-source-path) (sh03-reader-current-binding! source-path)
                 (sh03-reader-current-binding! source-path reader-source-path))
        plan (:plan binding)
        source-unit
        (sh03-reader-input-source-unit
         source-path source-bytes project-context)
        reader-policy (sh03-reader-input-policy reader-options)
        source-byte-vector (sh03-reader-byte-vector source-bytes)
        arguments [source-unit source-byte-vector reader-policy]
        raw-result
        (sh03-reader-execute-plan!
         source-path plan sh03-reader-entrypoint arguments)
        _ (sh03-reader-result-preflight!
           source-path source-unit reader-policy raw-result)
        verifier-result
        (sh03-reader-execute-plan!
         source-path plan sh03-reader-verifier
         [source-unit source-byte-vector reader-policy raw-result])
        _ (sh03-reader-verifier-preflight!
           source-path raw-result verifier-result)
        resolved
        (sh03-reader-resolve-digest-requests!
         source-path raw-result source-bytes)
        result
        (update (:result resolved) :diagnostics
                #(mapv (partial sh03-reader-resolve-diagnostic-id source-path)
                       %))]
    (assoc resolved
           :result result
           :raw-result raw-result
           :verification-report verifier-result
           :invocation-context
           (when (some? reader-source-path)
             {:reader-source-path sh03-reader-source-relative-path
              :authenticated-envelope-options {:source :development-current}})
           :plan-binding (dissoc binding :plan)))))

(defn sh03-reader-decode-raw-bytes!
  [source-path raw bytes]
  (try
    (str
     (.decode
      (doto (.newDecoder java.nio.charset.StandardCharsets/UTF_8)
        (.onMalformedInput java.nio.charset.CodingErrorAction/REPORT)
        (.onUnmappableCharacter java.nio.charset.CodingErrorAction/REPORT))
      (java.nio.ByteBuffer/wrap bytes)))
    (catch java.nio.charset.CharacterCodingException error
      (sh03-reader-boundary-fail!
       source-path :valid-sh03-reader-raw-utf8 raw
       {:cause-message (.getMessage error)}))))

(defn sh03-reader-source-scalar-boundaries!
  [source-path source-text source-bytes]
  (loop [utf16-index 0
         byte-index 0
         scalar-index 0
         boundaries {0 0}]
    (if (= utf16-index (.length source-text))
      (do
        (when-not (= byte-index (alength source-bytes))
          (sh03-reader-boundary-fail!
           source-path :complete-sh03-reader-source-scalar-index
           {:observed-byte-count byte-index}
           {:expected-byte-count (alength source-bytes)}))
        boundaries)
      (let [codepoint (.codePointAt source-text utf16-index)
            utf16-width (Character/charCount codepoint)
            utf8-width (cond
                         (<= codepoint 0x7f) 1
                         (<= codepoint 0x7ff) 2
                         (<= codepoint 0xffff) 3
                         :else 4)
            next-byte (+ byte-index utf8-width)]
        (recur (+ utf16-index utf16-width)
               next-byte
               (inc scalar-index)
               (assoc boundaries next-byte (inc scalar-index)))))))
