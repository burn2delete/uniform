

(defn jvm-backend-value-source
  [instruction counter indent env]
  (let [padding (apply str (repeat indent "  "))
        op (:op instruction)]
    (cond
      (#{:literal :quote} op)
      (let [{:keys [source descriptor]}
            (jvm-backend-value-declaration instruction counter indent)]
        (str source (jvm-backend-write-source descriptor indent)))

      (= :local op)
      (jvm-backend-write-source (get env (:name instruction)) indent)

      (= :builtin-call op)
      (apply str
             (map #(jvm-backend-value-source % counter indent env)
                  (:args instruction)))

      (= :if op)
      (str padding "if ("
           (jvm-backend-test-source (:test instruction) env)
           ") {\n"
           (jvm-backend-value-source (:then instruction) counter
                                     (inc indent) env)
           padding "} else {\n"
           (jvm-backend-value-source (:else instruction) counter
                                     (inc indent) env)
           padding "}\n")

      (= :let op)
      (let [binding-state
            (reduce (fn [state {:keys [name expr]}]
                      (let [{:keys [source descriptor]}
                            (jvm-backend-value-declaration
                             expr counter indent)]
                        {:source (str (:source state) source)
                         :env (assoc (:env state) name descriptor)}))
                    {:source "" :env env}
                    (:bindings instruction))]
        (str (:source binding-state)
             (jvm-backend-value-source
              (first (:body instruction)) counter indent
              (:env binding-state))))

      :else "")))

(defn jvm-backend-instruction-source
  ([instruction counter indent]
   (jvm-backend-instruction-source instruction counter indent {}))
  ([instruction counter indent env]
   (let [padding (apply str (repeat indent "  "))
         op (:op instruction)]
     (cond
       (#{:literal :quote :local} op) ""

       (= :println op)
       (str (apply str
                   (map-indexed
                    (fn [index argument]
                      (str (when (pos? index)
                             (let [name (str "gravitySpace"
                                             (swap! counter inc))]
                               (str (jvm-backend-byte-array-source
                                     name [32] indent)
                                    (jvm-backend-write-source
                                     {:name name} indent))))
                           (jvm-backend-value-source
                            argument counter indent env)))
                    (:args instruction)))
            (let [name (str "gravityNewline" (swap! counter inc))]
              (str (jvm-backend-byte-array-source name [10] indent)
                   (jvm-backend-write-source {:name name} indent))))

       (= :do op)
       (apply str
              (map #(jvm-backend-instruction-source % counter indent env)
                   (:body instruction)))

       (= :if op)
       (str padding "if ("
            (jvm-backend-test-source (:test instruction) env)
            ") {\n"
            (jvm-backend-instruction-source (:then instruction) counter
                                            (inc indent) env)
            padding "} else {\n"
            (jvm-backend-instruction-source (:else instruction) counter
                                            (inc indent) env)
            padding "}\n")

       (= :let op)
       (let [binding-state
             (reduce (fn [state {:keys [name expr]}]
                       (let [{:keys [source descriptor]}
                             (jvm-backend-value-declaration
                              expr counter indent)]
                         {:source (str (:source state) source)
                          :env (assoc (:env state) name descriptor)}))
                     {:source "" :env env}
                     (:bindings instruction))]
         (str (:source binding-state)
              (apply str
                     (map #(jvm-backend-instruction-source
                            % counter indent (:env binding-state))
                          (:body instruction)))))

       :else ""))))

(defn jvm-backend-java-source
  [plan]
  (let [counter (atom 0)
        main (get-in plan [:functions (:entrypoint plan)])]
    (str "package gravity.stage2;\n\n"
         "public final class Program {\n"
         "  private Program() {}\n\n"
         "  public static void main(String[] args) {\n"
         (apply str
                (map #(jvm-backend-instruction-source % counter 2 {})
                     (:instructions main)))
         "  }\n"
         "}\n")))

(def jvm-backend-module-source
  (str "module " jvm-backend-module-name " {\n"
       "  exports gravity.stage2;\n"
       "}\n"))

(defn jvm-backend-source-map
  [source-text java-source]
  {:artifact :gravity/jvm-source-map
   :schema-version 1
   :source {:kind :co-canonical-gravity-source
            :content-hash (str "sha256:" (sha256-hex source-text))
            :content source-text}
   :generated {:file "sources/gravity/stage2/Program.java"
               :line-count (count (str/split-lines java-source))}
   :origin-chain [:source-unit :c2-reader :stage2-source-front-end
                  :stage2-plan-emitter :stage2-compiler-driver
                  :jvm-lowering]
   :coverage :source-unit-only
   :per-form-origin-preserved? false
   :status :partial})

(defn jvm-backend-output-paths
  [output-path]
  {:java-source (str output-path "/sources/gravity/stage2/Program.java")
   :module-source (str output-path "/sources/module-info.java")
   :class-file (str output-path "/classes/gravity/stage2/Program.class")
   :module-class (str output-path "/classes/module-info.class")
   :jar (str output-path "/program.jar")
   :source-map (str output-path "/source-map.edn")
   :manifest (str output-path "/manifest.edn")
   :provenance (str output-path "/provenance.edn")})

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