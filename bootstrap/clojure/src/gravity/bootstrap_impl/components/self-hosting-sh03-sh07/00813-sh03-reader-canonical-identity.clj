

(defn sh03-reader-canonical-identity
  [source-path value]
  (binding [p15-s23-c6c10-max-carrier-nodes
            sh03-reader-canonical-maximum-nodes
            p15-s23-c6c10-max-carrier-depth
            sh03-reader-canonical-maximum-depth
            p15-s23-c6c10-max-container-width
            sh03-reader-canonical-maximum-width
            p15-s23-c6c10-max-scalar-bytes
            sh03-reader-canonical-maximum-scalar-bytes
            p15-s23-c6c10-max-total-scalar-bytes
            sh03-reader-canonical-maximum-total-scalar-bytes]
    (p15-s23-c6c10-canonical-identity source-path value)))

(defn sh03-reader-canonical-digest
  [source-path value]
  (binding [p15-s23-c6c10-max-carrier-nodes
            sh03-reader-canonical-maximum-nodes
            p15-s23-c6c10-max-carrier-depth
            sh03-reader-canonical-maximum-depth
            p15-s23-c6c10-max-container-width
            sh03-reader-canonical-maximum-width
            p15-s23-c6c10-max-scalar-bytes
            sh03-reader-canonical-maximum-scalar-bytes
            p15-s23-c6c10-max-total-scalar-bytes
            sh03-reader-canonical-maximum-total-scalar-bytes]
    (p15-s23-c6c10-canonical-digest source-path value)))

(defn sh03-reader-resolve-digest-requests!
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
           resolved-identities []
           resolved-requests []]
      (if (= ordinal request-count)
        (let [resolve-complete
              (fn [value]
                (p15-s23-c6c10-resolve-digest-references!
                 source-path value request-count nil digests))
              resolved-result (resolve-complete raw-result)
              resolved-result
              (assoc resolved-result
                     :digest-requests resolved-requests
                     :incremental-reader-hashes
                     (assoc (:incremental-reader-hashes resolved-result)
                            :status (if (= :accepted (:status raw-result))
                                      :stable :rejected)))]
          {:result resolved-result
           :resolved-digests digests
           :resolved-requests resolved-requests
           :reader-result-id (last digests)})
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
                  resolved-identity
                  (if raw-request?
                    [:raw-byte-vector-v1 resolved-preimage]
                    (sh03-reader-canonical-identity
                     source-path resolved-preimage))
                  digest
                  (if raw-request?
                    (str "sha256:"
                         (sha256-bytes-hex
                          (sh03-reader-byte-array resolved-preimage)))
                    (sh03-reader-canonical-digest
                     source-path resolved-preimage))]
              (when (or (some #{resolved-identity} resolved-identities)
                        (some #{digest} digests)
                        (and raw-request? (not= digest (:observed-id request))))
                (sh03-reader-boundary-fail!
                 source-path :unique-bound-sh03-reader-digest
                 request {:ordinal ordinal :digest digest}))
              (recur
               (inc ordinal)
               (conj digests digest)
               (conj resolved-identities resolved-identity)
               (conj resolved-requests
                     (assoc request
                            :preimage resolved-preimage
                            :digest digest))))))))))

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
  [source-path source-bytes project-context reader-options]
  (when-not (qst-or-gravity-source? source-path)
    (try
      (source-path-policy-fail! source-path source-bytes)
      (catch clojure.lang.ExceptionInfo ex
        (c2-reader-remap-exception! source-path ex))))
  (when (> (alength source-bytes) 1048576)
    (sh03-reader-boundary-fail!
     source-path :bounded-sh03-reader-source-bytes
     (alength source-bytes) {:maximum-source-bytes 1048576}))
  (let [binding (sh03-reader-current-binding! source-path)
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
           :plan-binding (dissoc binding :plan))))

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