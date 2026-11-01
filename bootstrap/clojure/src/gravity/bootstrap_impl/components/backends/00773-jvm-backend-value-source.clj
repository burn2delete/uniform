

(defn jvm-backend-value-source
  [instruction counter indent env]
  (gravity.backend.jvm/value-source #'jvm-backend-value-declaration #'jvm-backend-write-source #'jvm-backend-test-source #'jvm-backend-value-source instruction counter indent env))

(defn jvm-backend-instruction-source
  ([instruction counter indent]
   (gravity.backend.jvm/instruction-source #'jvm-backend-byte-array-source #'jvm-backend-write-source #'jvm-backend-value-source #'jvm-backend-test-source #'jvm-backend-value-declaration #'jvm-backend-instruction-source instruction counter indent))
  ([instruction counter indent env]
   (gravity.backend.jvm/instruction-source #'jvm-backend-byte-array-source #'jvm-backend-write-source #'jvm-backend-value-source #'jvm-backend-test-source #'jvm-backend-value-declaration #'jvm-backend-instruction-source instruction counter indent env)))

(defn jvm-backend-java-source
  [plan]
  (gravity.backend.jvm/java-source #'jvm-backend-instruction-source plan))

(def jvm-backend-module-source (gravity.backend.jvm/module-source jvm-backend-module-name))

(defn jvm-backend-source-map
  [source-text java-source]
  (gravity.backend.jvm/source-map #'sha256-hex source-text java-source))

(defn jvm-backend-output-paths
  [output-path]
  (gravity.backend.jvm/output-paths output-path))

(defn jvm-backend-run-process!
  [command source-path diagnostic-id message]
  (let [stdout-file (java.io.File/createTempFile "gravity-jvm-out-" ".bin")
        stderr-file (java.io.File/createTempFile "gravity-jvm-err-" ".txt")]
    (try
      (let [pb (ProcessBuilder. ^java.util.List (vec command))]
        (.redirectOutput pb stdout-file)
        (.redirectError pb stderr-file)
        (let [process (.start pb)
              finished? (.waitFor process 60000
                                  java.util.concurrent.TimeUnit/MILLISECONDS)]
          (when-not finished?
            (.destroyForcibly process)
            (jvm-backend-fail!
             diagnostic-id message source-path nil
             {:command (vec command)
              :missing-fact :jvm-target-process-completion}))
          (let [result
                {:exit (.exitValue process)
                 :stdout-bytes
                 (vec (map #(bit-and (int %) 0xff)
                           (java.nio.file.Files/readAllBytes
                            (.toPath stdout-file))))
                 :stderr (if (.exists stderr-file) (slurp stderr-file) "")}]
            (when-not (zero? (:exit result))
              (jvm-backend-fail!
               diagnostic-id message source-path nil
               {:command (vec command)
                :process-result result
                :missing-fact :jvm-target-tool-acceptance}))
            result)))
      (catch clojure.lang.ExceptionInfo ex
        (throw ex))
      (catch Exception ex
        (jvm-backend-fail!
         "B5-TARGET" "JVM target tool is unavailable"
         source-path nil
         {:command (vec command)
          :cause-message (.getMessage ex)
          :missing-fact :java21-target-toolchain}))
      (finally
        (.delete stdout-file)
        (.delete stderr-file)))))

(defn jvm-backend-tool-version!
  [command source-path]
  (let [result (jvm-backend-run-process!
                [command "-version"] source-path "B5-TARGET"
                "JVM target tool version check failed")
        stdout (String. (byte-array (map byte (:stdout-bytes result)))
                        java.nio.charset.StandardCharsets/UTF_8)
        output (str/trim (str stdout "\n" (:stderr result)))
        major (some-> (re-find #"(?:version )?\"?([0-9]+)" output)
                      second Long/parseLong)]
    (when-not (and major (>= major jvm-backend-target-release))
      (jvm-backend-fail!
       "B5-TARGET" "JVM target tool cannot produce or run Java 21 artifacts"
       source-path nil
       {:command command
        :observed-version output
        :required-major jvm-backend-target-release
        :missing-fact :java21-target-toolchain}))
    {:command command :version output :major major}))

(defn jvm-backend-write-file!
  [path content]
  (java.nio.file.Files/createDirectories (.getParent path)
                                          (make-array
                                           java.nio.file.attribute.FileAttribute
                                           0))
  (java.nio.file.Files/write
   path (.getBytes (str content) java.nio.charset.StandardCharsets/UTF_8)
   (into-array java.nio.file.OpenOption
               [java.nio.file.StandardOpenOption/CREATE_NEW
                java.nio.file.StandardOpenOption/WRITE])))