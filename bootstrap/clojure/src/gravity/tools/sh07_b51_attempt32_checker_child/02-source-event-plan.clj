
(def ^:private source-event-plan
  (vec
   (concat
    [{:ordinal 0 :domain :l1-contract :source-ordinal 0
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-l1-contract}
     {:ordinal 1 :domain :l2-contract :source-ordinal 1
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-l2-contract}
     {:ordinal 2 :domain :checked-core-revision :source-ordinal 2
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-checked-core-contract}]
    (for [module-ordinal (range 7)]
      {:ordinal (+ 3 module-ordinal) :domain :module-validation
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer 'gravity.bootstrap/p15-s23-sh07-b51-module-source-valid*})
    (for [module-ordinal (range 7)]
      {:ordinal (+ 10 module-ordinal) :domain :callsite-census
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer 'gravity.bootstrap/p15-s23-sh07-b51-derive-callsite-census})
    (for [module-ordinal (range 7)]
      {:ordinal (+ 17 module-ordinal) :domain :link-authority
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer
       'gravity.bootstrap/p15-s23-sh07-b51-build-link-authority-from-census})
    [{:ordinal 24 :domain :compiler-emitter :source-ordinal 10
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-current-emitter}]
    (for [module-ordinal (range 7)]
      {:ordinal (+ 25 module-ordinal) :domain :source-authority
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer
       'gravity.bootstrap/p15-s23-sh07-b51-current-module-source-authority})
    (for [module-ordinal (range 7)]
      {:ordinal (+ 32 module-ordinal) :domain :compiler-artifact-binding
       :source-ordinal 11 :module-ordinal module-ordinal
       :consumer
       'gravity.bootstrap/p15-s23-stage2-compiler-artifact-binding})
    [{:ordinal 39 :domain :sh06-resolution-binding :source-ordinal 12
      :module-ordinal nil
      :consumer
      'gravity.bootstrap/p15-s23-sh07-b51-current-sh06-resolution-binding}])))

(defn- reject!
  ([reason] (reject! reason {}))
  ([reason details]
   (throw (ex-info "SH-07 B51 checker child rejected"
                   {:id "SH07-B51-CHECKER-CHILD"
                    :reason reason
                    :details details}))))

(defn- exact-keys? [value expected-keys]
  (and (map? value) (= expected-keys (set (keys value)))))

(defn- git-id? [value]
  (and (string? value) (boolean (re-matches #"[0-9a-f]{40}" value))))

(defn- sha256-id? [value]
  (and (string? value)
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn role-from-cli!
  "Apply the only admitted CLI-string/EDN-keyword role bijection."
  [role-name]
  (or (some (fn [[role spec]] (when (= role-name (:cli spec)) role))
            role-specs)
      (reject! :role {:actual role-name})))

(defn parse-role-arguments!
  "Accept exactly --role followed by one canonical role spelling."
  [arguments]
  (when-not (and (vector? arguments)
                 (= 2 (count arguments))
                 (= "--role" (first arguments)))
    (reject! :argv {:actual arguments}))
  (role-from-cli! (second arguments)))

(defn validate-closed-environment!
  "Reject inherited, injected, or omitted process environment entries."
  [environment]
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

(defn validate-literal-classpath!
  "Perform the project-free literal four-entry classpath preflight."
  [classpath]
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

(defn- expected-child-argv [classpath role]
  [(canonical-java-path) "-Xms64m" "-Xmx8g" "-Dfile.encoding=UTF-8"
   "-Duser.language=en" "-Duser.country=US" "-Duser.timezone=UTC"
   "-Djava.awt.headless=true" "-Dclojure.main.report=stderr"
   "-Duser.home=/nonexistent" "-cp" classpath "clojure.main" "-m"
   child-main-token "--role" (get-in role-specs [role :cli])])

(defn validate-entry-preflight!
  "Validate argv, the closed environment, working directory, and literal CP.

  This function intentionally uses no project namespace.  The full C/T
  manifest and codec TCB validation belongs to the launcher-provided preflight
  seam; no semantic source is decoded here."
  [{:keys [arguments environment working-directory classpath process-argv]}]
  (when-not (string? working-directory)
    (reject! :working-directory {:actual working-directory}))
  (let [role (parse-role-arguments! arguments)
        entries (validate-literal-classpath! classpath)
        cwd (Paths/get working-directory (make-array String 0))]
    (validate-closed-environment! environment)
    (when-not (and (string? working-directory)
                   (.isAbsolute cwd)
                   (Files/isDirectory cwd (make-array LinkOption 0)))
      (reject! :working-directory {:actual working-directory}))
    (let [expected (expected-child-argv classpath role)]
      (when-not (= 17 (count expected))
        (reject! :child-argv-count {:actual (count expected)}))
      (when (and process-argv (not= expected process-argv))
        (reject! :child-argv-self-report))
      {:role role
       :arguments arguments
       :environment {:working-directory working-directory
                     :variables environment}
       :classpath entries
       :classpath-string classpath
       :child-argv (or process-argv expected)})))

(defn- sha256-bytes-core [bytes]
  (let [digest (.digest (MessageDigest/getInstance "SHA-256") ^bytes bytes)
        out (StringBuilder. 71)]
    (.append out "sha256:")
    (doseq [value digest]
      (let [unsigned (bit-and 0xff value)]
        (.append out (Character/forDigit (bit-shift-right unsigned 4) 16))
        (.append out (Character/forDigit (bit-and unsigned 15) 16))))
    (str out)))

(defn- strict-utf8-core! [bytes reason]
  (let [decoder (doto (.newDecoder StandardCharsets/UTF_8)
                  (.onMalformedInput CodingErrorAction/REPORT)
                  (.onUnmappableCharacter CodingErrorAction/REPORT))]
    (try
      (.toString (.decode decoder (ByteBuffer/wrap ^bytes bytes)))
      (catch java.nio.charset.CharacterCodingException _ (reject! reason)))))

(defn- read-bounded-core! [^InputStream input maximum reason]
  (let [out (ByteArrayOutputStream.) buffer (byte-array 8192)]
    (loop [total 0]
      (let [read (.read input buffer)]
        (if (neg? read)
          (.toByteArray out)
          (let [next-total (+ total read)]
            (when (> next-total maximum)
              (reject! reason {:maximum maximum :actual next-total}))
            (.write out buffer 0 read)
            (recur next-total)))))))

(defn- remaining-ms [deadline]
  (max 0 (long (Math/ceil
                (/ (double (- deadline (System/nanoTime))) 1000000.0)))))

(defn- close-quietly-core! [value]
  (when (instance? Closeable value)
    (try (.close ^Closeable value) (catch Throwable _ nil))))

(defn- run-project-free-process!
  [working-directory argv stdout-maximum operation]
  (let [builder (doto (ProcessBuilder. ^java.util.List argv)
                  (.directory (.toFile
                               (Paths/get working-directory
                                          (make-array String 0)))))
        _ (doto (.environment builder)
            (.clear) (.putAll closed-environment))
        process (.start builder)
        stdout (.getInputStream process) stderr (.getErrorStream process)
        stdin (.getOutputStream process)
        out-task (future (read-bounded-core! stdout stdout-maximum
                                             :git-stdout-overflow))
        err-task (future (read-bounded-core!
                          stderr (:stderr-max-bytes transport-limits)
                          :git-stderr-overflow))
        deadline (+ (System/nanoTime)
                    (* 1000000 (:deadline-ms transport-limits)))]
    (try
      (.close ^OutputStream stdin)
      (when-not (.waitFor process (remaining-ms deadline)
                          TimeUnit/MILLISECONDS)
        (throw (TimeoutException. "project-free process")))
      (let [timeout (Object.)
            out (deref out-task (remaining-ms deadline) timeout)
            err (deref err-task (remaining-ms deadline) timeout)]
        (when (or (identical? timeout out) (identical? timeout err))
          (throw (TimeoutException. "project-free drains")))
        (when-not (and (zero? (.exitValue process))
                       (zero? (alength ^bytes err)))
          (reject! :git-object-operation
                   {:operation operation :exit-code (.exitValue process)
                    :stderr-byte-count (alength ^bytes err)}))
        {:operation operation :argv argv :stdin-byte-count 0
         :stdin-raw-sha256 empty-raw-sha256 :exit-code 0
         :stdout-bytes out :stdout-byte-count (alength ^bytes out)
         :stdout-raw-sha256 (sha256-bytes-core out)
         :stderr-byte-count 0 :stderr-raw-sha256 empty-raw-sha256})
      (catch Throwable error
        (future-cancel out-task) (future-cancel err-task)
        (when (.isAlive process)
          (.destroy process)
          (when-not (.waitFor process 250 TimeUnit/MILLISECONDS)
            (.destroyForcibly process)
            (.waitFor process 1000 TimeUnit/MILLISECONDS)))
        (if (= "SH07-B51-CHECKER-CHILD" (:id (ex-data error)))
          (throw error)
          (reject! :git-object-operation
                   {:operation operation
                    :cause (.getName (class error))})))
      (finally
        (close-quietly-core! stdin)
        (close-quietly-core! stdout)
        (close-quietly-core! stderr)))))
