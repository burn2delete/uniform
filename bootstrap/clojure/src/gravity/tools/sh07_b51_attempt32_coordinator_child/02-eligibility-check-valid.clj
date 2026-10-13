
(defn- eligibility-check-valid?
  [expected-coordinate check]
  (and (exact-keys? check eligibility-check-keys)
       (= expected-coordinate (:coordinate check))
       (dense-observations? 5 (:observations check))
       (dense-observations? 209 (:pre-discharge-observations check))
       (= eligibility-check-counts
          (select-keys check (keys eligibility-check-counts)))
       (every? true? (map check eligibility-check-matches))))

(defn aggregate-eligibility-counters!
  "Validate the ordered L/M/A/B eligibility checks and return E4's exact
  aggregate counter carrier."
  [H participant-results]
  (let [common-coordinate
        (get-in participant-results [0 :eligibility-check :coordinate])]
    (when-not
     (and (ifn? H)
          (vector? participant-results)
          (= eligibility-participant-order
             (mapv :participant participant-results))
          (every?
           (fn [{:keys [eligibility-check eligibility-check-root] :as result}]
             (and (exact-keys?
                   result
                   #{:participant :eligibility-check :eligibility-check-root})
                  (eligibility-check-valid? common-coordinate
                                            eligibility-check)
                  (= eligibility-check-root
                     (H [:gravity/sh07-b51-eligibility-check-root-v1
                         eligibility-check]))))
           participant-results))
      (reject! :eligibility-aggregate-invalid))
    (assoc aggregate-eligibility-counter-values
           :participants eligibility-participant-order)))

(defn- nonnegative-long! [value option]
  (try
    (let [parsed (Long/parseLong value)]
      (when (neg? parsed) (reject! :argv-value {:option option}))
      parsed)
    (catch NumberFormatException _
      (reject! :argv-value {:option option}))))

(defn parse-coordinator-arguments!
  "Parse the exact 24-string M option vector without accepting duplicates."
  [arguments]
  (when-not (and (vector? arguments)
                 (= (* 2 (count coordinator-option-names))
                    (count arguments)))
    (reject! :argv {:actual-count (count arguments)}))
  (let [pairs (mapv vec (partition 2 arguments))
        names (mapv first pairs)]
    (when-not (= coordinator-option-names names)
      (reject! :argv-order {:actual names}))
    (let [options (into {} pairs)
          commit (get options "--snapshot-commit")
          tree (get options "--snapshot-tree")
          eligibility
          {:schema :gravity/sh07-b51-external-review-object-v1
           :commit (get options "--eligibility-review-commit")
           :tree (get options "--eligibility-review-tree")
           :parents [commit]
           :path (get options "--eligibility-receipt-path")
           :mode "100644"
           :type "blob"
           :blob-oid (get options "--eligibility-receipt-blob")
           :byte-count
           (nonnegative-long!
            (get options "--eligibility-receipt-byte-count")
            "--eligibility-receipt-byte-count")
           :raw-sha256
           (get options "--eligibility-receipt-raw-sha256")
           :receipt-id (get options "--eligibility-receipt-id")}
          stage (get options "--staging-root")]
      (when-not
       (and (git-id? commit)
            (git-id? tree)
            (= completed-at (get options "--completed-at"))
            (= evidence-relative-root
               (get options "--evidence-relative-root"))
            (string? stage)
            (.isAbsolute (Paths/get stage (make-array String 0)))
            (git-id? (:commit eligibility))
            (git-id? (:tree eligibility))
            (= eligibility-path (:path eligibility))
            (git-id? (:blob-oid eligibility))
            (sha256-id? (:raw-sha256 eligibility))
            (sha256-id? (:receipt-id eligibility)))
        (reject! :argv-value))
      {:arguments arguments
       :content {:commit commit :tree tree}
       :completed-at completed-at
       :evidence-relative-root evidence-relative-root
       :staging-root stage
       :eligibility eligibility})))

(defn validate-closed-environment! [environment]
  (when-not (= closed-environment environment)
    (reject! :environment
             {:expected-keys (set (keys closed-environment))
              :actual-keys (set (keys environment))}))
  environment)

(defn- classpath-entries [classpath]
  (if (string? classpath)
    (str/split classpath
               (re-pattern (java.util.regex.Pattern/quote File/pathSeparator))
               -1)
    []))

(defn validate-literal-classpath! [classpath]
  (let [entries (classpath-entries classpath)]
    (when-not (and (= 4 (count entries))
                   (= 4 (count (distinct entries)))
                   (every? #(and (string? %) (not (str/blank? %))) entries)
                   (every? #(.isAbsolute
                             (Paths/get % (make-array String 0)))
                           entries))
      (reject! :classpath {:entry-count (count entries)}))
    entries))

(defn- canonical-java-path []
  (str (.normalize
        (.toAbsolutePath
         (Paths/get (System/getProperty "java.home")
                    (into-array String ["bin" "java"]))))))

(defn- expected-m-argv [classpath arguments]
  (into [(canonical-java-path) "-Xms64m" "-Xmx8g"
         "-Dfile.encoding=UTF-8" "-Duser.language=en" "-Duser.country=US"
         "-Duser.timezone=UTC" "-Djava.awt.headless=true"
         "-Dclojure.main.report=stderr" "-Duser.home=/nonexistent"
         "-cp" classpath "clojure.main" "-m" coordinator-main-token]
        arguments))

(defn validate-entry-preflight!
  "Run M's project-free argv/environment/working-directory/literal-CP gate."
  [{:keys [arguments environment working-directory classpath process-argv]}]
  (when-not (string? working-directory)
    (reject! :working-directory {:actual working-directory}))
  (let [options (parse-coordinator-arguments! arguments)
        entries (validate-literal-classpath! classpath)
        cwd (Paths/get working-directory (make-array String 0))]
    (validate-closed-environment! environment)
    (when-not (and (.isAbsolute cwd)
                   (Files/isDirectory cwd (make-array LinkOption 0)))
      (reject! :working-directory {:actual working-directory}))
    (let [expected (expected-m-argv classpath arguments)]
      (when-not (= 39 (count expected))
        (reject! :m-argv-count {:actual (count expected)}))
      (when (and process-argv (not= expected process-argv))
        (reject! :m-argv-self-report))
      (assoc options
             :working-directory working-directory
             :environment {:working-directory working-directory
                           :variables environment}
             :classpath entries
             :classpath-string classpath
             :m-argv (or process-argv expected)))))

(defn- sha256-bytes-core [bytes]
  (let [digest (.digest (MessageDigest/getInstance "SHA-256") ^bytes bytes)
        out (StringBuilder. 71)]
    (.append out "sha256:")
    (doseq [value digest]
      (let [unsigned (bit-and 0xff value)]
        (.append out (Character/forDigit (bit-shift-right unsigned 4) 16))
        (.append out (Character/forDigit (bit-and unsigned 0x0f) 16))))
    (str out)))

(defn- strict-utf8-core! [bytes reason]
  (let [decoder (doto (.newDecoder StandardCharsets/UTF_8)
                  (.onMalformedInput CodingErrorAction/REPORT)
                  (.onUnmappableCharacter CodingErrorAction/REPORT))]
    (try
      (.toString (.decode decoder (ByteBuffer/wrap ^bytes bytes)))
      (catch java.nio.charset.CharacterCodingException _
        (reject! reason)))))

(defn- read-bounded-core! [^InputStream input maximum reason]
  (let [out (ByteArrayOutputStream.)
        buffer (byte-array 8192)]
    (loop [total 0]
      (let [read (.read input buffer)]
        (if (neg? read)
          (.toByteArray out)
          (let [next-total (+ total read)]
            (when (> next-total maximum)
              (reject! reason {:maximum maximum :actual next-total}))
            (.write out buffer 0 read)
            (recur next-total)))))))

(defn- completed-future-value! [^Future future]
  (try
    (.get future)
    (catch ExecutionException error
      (throw (or (.getCause error) error)))))

(declare remaining-millis close-quietly! terminate-process!)
