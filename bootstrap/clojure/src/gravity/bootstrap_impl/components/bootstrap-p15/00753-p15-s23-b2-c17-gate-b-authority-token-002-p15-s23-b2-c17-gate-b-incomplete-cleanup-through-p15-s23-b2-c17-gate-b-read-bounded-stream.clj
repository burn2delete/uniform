(let [p15-s23-b2-c17-gate-b-authority-token (nth __gravity_bootstrap_lexical_values_131964 0)
      p15-s23-b2-c17-gate-b-tool-state (nth __gravity_bootstrap_lexical_values_131964 1)]
(defn- p15-s23-b2-c17-gate-b-incomplete-cleanup!
  [source-path primary cleanup-error]
  (let [data
        (p15-s23-backend-trusted-exception-data primary 65536 128)
        primary-rule
        (if (and (map? data)
                 (contains? p15-s23-c-backend-diagnostic-rules
                            (:id data)))
          (:id data)
          :contained-host-failure)
        primary-diagnostic-id
        (when (and (map? data)
                   (string? (:diagnostic-id data))
                   (re-matches #"diag-[0-9a-f]{64}"
                               (:diagnostic-id data)))
          (:diagnostic-id data))
        facts
        (cond->
         {:missing-fact
          :complete-descriptor-relative-c17-publication-cleanup
          :cleanup-complete? false :residue-possible? true
          :primary-failure-rule primary-rule}
          primary-diagnostic-id
          (assoc :primary-diagnostic-id primary-diagnostic-id))
        diagnostic
        (try
          (p15-s23-c-backend-fail!
           "B13-PROVENANCE" source-path {} facts)
          (catch clojure.lang.ExceptionInfo diagnostic
            diagnostic))]
    (.addSuppressed ^Throwable diagnostic ^Throwable primary)
    (when cleanup-error
      (.addSuppressed ^Throwable diagnostic ^Throwable cleanup-error))
    (throw diagnostic)))

(defn- p15-s23-b2-c17-gate-b-canonical-abort-result?
  [cleanup]
  (and
   (map? cleanup)
   (contains? #{:aborted :already-aborted} (:status cleanup))
   (= (if (= :aborted (:status cleanup))
        #{:status :published? :cleanup-complete? :residue-possible?}
        #{:status :published? :cleanup-complete? :residue-possible?
          :native-calls})
      (set (keys cleanup)))
   (false? (:published? cleanup))
   (boolean? (:cleanup-complete? cleanup))
   (boolean? (:residue-possible? cleanup))
   (= (:cleanup-complete? cleanup)
      (not (:residue-possible? cleanup)))
   (or (= :aborted (:status cleanup))
       (and (integer? (:native-calls cleanup))
            (zero? (:native-calls cleanup))))))

(defn- p15-s23-b2-c17-gate-b-abort-after-failure!
  [publication-context source-path error]
  (let [incomplete? (atom false)
        cleanup-error (atom nil)]
    (try
      (let [cleanup
            (darwin-publication/abort-staged-bundle! publication-context)]
        (when-not
         (and (p15-s23-b2-c17-gate-b-canonical-abort-result? cleanup)
              (true? (:cleanup-complete? cleanup)))
          (reset! incomplete? true)))
      (catch Throwable cleanup
        (p15-s23-b2-c17-gate-b-restore-interrupt! error)
        (p15-s23-b2-c17-gate-b-restore-interrupt! cleanup)
        (cond
          (instance? Error error)
          (.addSuppressed ^Throwable error ^Throwable cleanup)

          (instance? Error cleanup)
          (do
            (.addSuppressed ^Throwable cleanup ^Throwable error)
            (throw cleanup))

          (p15-s23-b2-c17-gate-b-interrupt-like? error)
          (.addSuppressed ^Throwable error ^Throwable cleanup)

          (p15-s23-b2-c17-gate-b-interrupt-like? cleanup)
          (do
            (.addSuppressed ^Throwable cleanup ^Throwable error)
            (throw cleanup))

          :else
          (do
            (reset! incomplete? true)
            (reset! cleanup-error cleanup)))))
    (when @incomplete?
      (if (or (instance? Error error)
              (p15-s23-b2-c17-gate-b-interrupt-like? error))
        (.addSuppressed
         ^Throwable error
         (ex-info
          "C17 descriptor publication cleanup was incomplete"
          {:cleanup-complete? false :residue-possible? true}))
        (p15-s23-b2-c17-gate-b-incomplete-cleanup!
         source-path error @cleanup-error)))
    (p15-s23-b2-c17-gate-b-restore-interrupt! error)
    (throw error)))

(defn- p15-s23-b2-c17-gate-b-host-runtime-preflight!
  [candidate source-path]
  (p15-s23-b2-c17-gate-b-require-authority!
   candidate source-path :pinned-c17-host-runtime-preflight)
  (let [record
        {:artifact :gravity/b2-c17-gate-b-host-runtime
         :schema-version 1
         :java-vendor (System/getProperty "java.vendor")
         :java-vm-name (System/getProperty "java.vm.name")
         :java-version (System/getProperty "java.version")
         :java-feature (.feature (Runtime/version))
         :os-name (System/getProperty "os.name")
         :os-arch (System/getProperty "os.arch")}]
    (when-not
     (= {:artifact :gravity/b2-c17-gate-b-host-runtime
         :schema-version 1
         :java-vendor "Homebrew"
         :java-vm-name "OpenJDK 64-Bit Server VM"
         :java-version "26.0.1" :java-feature 26
         :os-name "Mac OS X" :os-arch "aarch64"}
        record)
      (p15-s23-c-backend-fail!
       "B2-MANIFEST" source-path {}
       {:missing-fact :pinned-jdk26-macos-aarch64-c17-host-runtime
        :bounded-reason :host-runtime-preflight}))
    record))

(defn- p15-s23-b2-c17-gate-b-sha256-bytes
  [bytes]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")]
    (.update digest ^bytes bytes)
    (str "sha256:"
         (apply str
                (map #(format "%02x" (bit-and % 0xff))
                     (.digest digest))))))

(defn- p15-s23-b2-c17-gate-b-read-bounded-stream
  [candidate stream source-path]
  (p15-s23-b2-c17-gate-b-require-authority!
   candidate source-path :read-bounded-c-tool-stream)
  (with-open [input stream
              output (java.io.ByteArrayOutputStream.)]
    (let [buffer (byte-array 4096)
          digest (java.security.MessageDigest/getInstance "SHA-256")]
      (loop [total 0]
        (let [read (.read input buffer)]
          (if (neg? read)
            (let [bytes (.toByteArray output)]
              {:bytes bytes
               :text (String. ^bytes bytes
                              java.nio.charset.StandardCharsets/UTF_8)
               :stream-read-complete? true
               :total-byte-count total
               :retained-byte-count total
               :truncated? false
               :hash
               (str "sha256:"
                    (apply str
                           (map #(format "%02x" (bit-and % 0xff))
                                (.digest digest))))})
            (let [next-total (+ total read)
                  remaining (- 65536 total)
                  keep-count (max 0 (min remaining read))]
              (when (pos? keep-count)
                (.update digest buffer 0 keep-count)
                (.write output buffer 0 keep-count))
              (if (> next-total 65536)
                (let [bytes (.toByteArray output)]
                  {:bytes bytes
                   :text (String. ^bytes bytes
                                  java.nio.charset.StandardCharsets/UTF_8)
                   :stream-read-complete? false
                   :limit-exceeded? true
                   :total-byte-count next-total
                   :retained-byte-count (+ total keep-count)
                   :truncated? true
                   :hash
                   (str "sha256:"
                        (apply str
                               (map #(format "%02x" (bit-and % 0xff))
                                    (.digest digest))))})
                (recur next-total))))))))))
