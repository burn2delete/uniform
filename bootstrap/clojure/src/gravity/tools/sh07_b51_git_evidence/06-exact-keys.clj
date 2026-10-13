
(defn- exact-keys!
  [value expected id reason]
  (when-not (and (map? value) (= expected (set (keys value))))
    (fail! id reason
           {:expected-keys expected
            :actual-keys (when (map? value) (set (keys value)))}))
  value)

(defn- git-id?
  [value]
  (and (string? value) (boolean (re-matches #"[0-9a-f]{40}" value))))

(defn- sha256-id?
  [value]
  (and (string? value)
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- safe-relative-path?
  [value]
  (and (string? value)
       (not (str/blank? value))
       (not (str/starts-with? value "/"))
       (not (str/includes? value "\\"))
       (not-any? #{"" "." ".."} (str/split value #"/" -1))))

(defn- require-git-id!
  [value field id]
  (when-not (git-id? value)
    (fail! id :git-id {:field field :value value}))
  value)

(defn- canonical-directory!
  [value field id]
  (when-not (instance? Path value)
    (fail! id :path-type {:field field :actual (some-> value class .getName)}))
  (let [^Path path (.normalize (.toAbsolutePath ^Path value))]
    (when-not (and (Files/isDirectory path no-link-options)
                   (not (Files/isSymbolicLink path)))
      (fail! id :directory {:field field :path (str path)}))
    path))

(defn- file-key
  [^Path path id]
  (let [attributes
        (Files/readAttributes path BasicFileAttributes no-link-options)
        key (.fileKey ^BasicFileAttributes attributes)]
    (when (nil? key)
      (fail! id :missing-file-key {:path (str path)}))
    (str key)))

(defn- decode-utf8!
  [bytes id reason]
  (let [decoder (doto (.newDecoder StandardCharsets/UTF_8)
                  (.onMalformedInput CodingErrorAction/REPORT)
                  (.onUnmappableCharacter CodingErrorAction/REPORT))]
    (try
      (.toString (.decode decoder (ByteBuffer/wrap ^bytes bytes)))
      (catch java.nio.charset.CharacterCodingException _
        (fail! id reason)))))

;; This parser is deliberately local to the authenticated Git-object seam.
;; Adding a tooling JSON namespace to the checker classpath would enlarge the
;; source-authority closure.  Duplicate keys, malformed grammar, and trailing
;; input all reject before any governance field is consulted.
(defn- json-skip-whitespace! [^PushbackReader reader]
  (loop [value (.read reader)]
    (if (and (not= -1 value)
             (contains? #{\space \tab \newline \return} (char value)))
      (recur (.read reader))
      value)))

(defn- json-unread! [^PushbackReader reader value]
  (when-not (= -1 value) (.unread reader value)))

(defn- json-read-string! [^PushbackReader reader]
  (let [output (StringBuilder.)]
    (loop []
      (let [value (.read reader)]
        (cond
          (= -1 value) (fail! "SH07-B51-GIT-SNAPSHOT" :json-string)
          (= \" (char value)) (str output)
          (= \\ (char value))
          (let [escaped (.read reader)]
            (when (= -1 escaped)
              (fail! "SH07-B51-GIT-SNAPSHOT" :json-escape))
            (case (char escaped)
              \" (.append output \" )
              \\ (.append output \\)
              \/ (.append output \/)
              \b (.append output \backspace)
              \f (.append output \formfeed)
              \n (.append output \newline)
              \r (.append output \return)
              \t (.append output \tab)
              \u (let [values (repeatedly 4 #(.read reader))]
                   (when (some #(= -1 %) values)
                     (fail! "SH07-B51-GIT-SNAPSHOT" :json-unicode))
                   (let [digits (apply str (map char values))]
                     (when-not (re-matches #"[0-9A-Fa-f]{4}" digits)
                       (fail! "SH07-B51-GIT-SNAPSHOT" :json-unicode))
                     (.append output (char (Integer/parseInt digits 16)))))
              (fail! "SH07-B51-GIT-SNAPSHOT" :json-escape))
            (recur))
          (< value 0x20)
          (fail! "SH07-B51-GIT-SNAPSHOT" :json-control)
          :else (do (.append output (char value)) (recur)))))))

(declare json-read-value!)

(defn- json-read-literal!
  [^PushbackReader reader suffix value]
  (doseq [expected suffix]
    (let [actual (.read reader)]
      (when (or (= -1 actual) (not= expected (char actual)))
        (fail! "SH07-B51-GIT-SNAPSHOT" :json-literal))))
  value)

(defn- json-read-number! [^PushbackReader reader first-character]
  (let [token
        (loop [output (StringBuilder. (str first-character))]
          (let [value (.read reader)]
            (if (and (not= -1 value)
                     (re-matches #"[0-9eE+\-.]" (str (char value))))
              (recur (.append output (char value)))
              (do (json-unread! reader value) (str output)))))]
    (when-not
     (re-matches #"-?(?:0|[1-9][0-9]*)(?:\.[0-9]+)?(?:[eE][+-]?[0-9]+)?"
                 token)
      (fail! "SH07-B51-GIT-SNAPSHOT" :json-number))
    (try
      (if (re-find #"[.eE]" token) (bigdec token) (bigint token))
      (catch NumberFormatException _
        (fail! "SH07-B51-GIT-SNAPSHOT" :json-number)))))

(defn- json-read-array! [^PushbackReader reader]
  (let [first-value (json-skip-whitespace! reader)]
    (if (= (int \]) first-value)
      []
      (do
        (json-unread! reader first-value)
        (loop [values []]
          (let [value (json-read-value! reader)
                delimiter (json-skip-whitespace! reader)]
            (cond
              (= (int \,) delimiter) (recur (conj values value))
              (= (int \]) delimiter) (conj values value)
              :else (fail! "SH07-B51-GIT-SNAPSHOT" :json-array))))))))

(defn- json-read-object! [^PushbackReader reader]
  (let [first-key (json-skip-whitespace! reader)]
    (if (= (int \}) first-key)
      {}
      (do
        (json-unread! reader first-key)
        (loop [result {}]
          (when-not (= (int \") (json-skip-whitespace! reader))
            (fail! "SH07-B51-GIT-SNAPSHOT" :json-object-key))
          (let [key (json-read-string! reader)
                colon (json-skip-whitespace! reader)]
            (when (contains? result key)
              (fail! "SH07-B51-GIT-SNAPSHOT" :json-duplicate-key
                     {:key key}))
            (when-not (= (int \:) colon)
              (fail! "SH07-B51-GIT-SNAPSHOT" :json-object-colon))
            (let [updated (assoc result key (json-read-value! reader))
                  delimiter (json-skip-whitespace! reader)]
              (cond
                (= (int \,) delimiter) (recur updated)
                (= (int \}) delimiter) updated
                :else (fail! "SH07-B51-GIT-SNAPSHOT" :json-object)))))))))

(defn- json-read-value! [^PushbackReader reader]
  (let [value (json-skip-whitespace! reader)]
    (when (= -1 value) (fail! "SH07-B51-GIT-SNAPSHOT" :json-value))
    (let [character (char value)]
      (case character
        \{ (json-read-object! reader)
        \[ (json-read-array! reader)
        \" (json-read-string! reader)
        \t (json-read-literal! reader "rue" true)
        \f (json-read-literal! reader "alse" false)
        \n (json-read-literal! reader "ull" nil)
        (if (or (= character \-) (Character/isDigit character))
          (json-read-number! reader character)
          (fail! "SH07-B51-GIT-SNAPSHOT" :json-value))))))

(defn- read-strict-json! [text]
  (with-open [reader (PushbackReader. (StringReader. text))]
    (let [value (json-read-value! reader)]
      (when-not (= -1 (json-skip-whitespace! reader))
        (fail! "SH07-B51-GIT-SNAPSHOT" :json-trailing))
      value)))

(defn- remaining-millis
  [deadline-nanos]
  (max 0 (long (Math/ceil
                (/ (double (- deadline-nanos (System/nanoTime)))
                   1000000.0)))))

(defn- close-quietly!
  [value]
  (when (instance? Closeable value)
    (try (.close ^Closeable value) (catch Throwable _ nil))))

(defn- terminate-process!
  [^Process process]
  (let [destroy-called (boolean (and (.isAlive process)
                                     (do (.destroy process) true)))
        destroy-forcibly-called
        (boolean
         (and (.isAlive process)
              (not (.waitFor process 250 TimeUnit/MILLISECONDS))
              (do (.destroyForcibly process) true)))]
    (when (.isAlive process)
      (.waitFor process 1000 TimeUnit/MILLISECONDS))
    {:destroy-called destroy-called
     :destroy-forcibly-called destroy-forcibly-called}))

(defn- completed-value!
  [^Future future]
  (try
    (.get future)
    (catch ExecutionException error
      (throw (or (.getCause error) error)))))
