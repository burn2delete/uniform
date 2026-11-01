(ns gravity.backend.jvm
  "Internal Java source, toolchain and output operations; no input admission."
  (:require [clojure.string :as str]))

(def ^:private namespace-contract
  (quote
{:authenticates-inputs? false,
 :public-api
 {classfile-major {:arglists ([path]), :admission :none},
 jar-record {:arglists ([jar-path]), :admission :none},
 run-process!
 {:arglists ([fail! command source-path diagnostic-id message]),
  :admission :none},
 stored-jar-entry!
 {:arglists ([jar-output entry-name bytes]), :admission :none},
 tool-version!
 {:arglists ([run-process! fail! target-release command source-path]),
  :admission :none},
 validate-content-hashes!
 {:arglists ([sha256-bytes-hex fail! source-path manifest paths]),
  :admission :none},
 write-file! {:arglists ([path content]), :admission :none},
 write-jar!
 {:arglists ([stored-jar-entry! main-class jar-path class-directory]),
  :admission :none}
 byte-array-source
  {:arglists ([name bytes indent]), :admission :none},
  instruction-source
  {:arglists
   ([_byte-array-source
     _write-source
     _value-source
     _test-source
     _value-declaration
     instruction-source
     instruction
     counter
     indent]
    [byte-array-source
     write-source
     value-source
     test-source
     value-declaration
     instruction-source
     instruction
     counter
     indent
     env]),
   :admission :none},
  java-source
  {:arglists ([instruction-source plan]), :admission :none},
  module-source {:arglists ([module-name]), :admission :none},
  output-paths {:arglists ([output-path]), :admission :none},
  source-map
  {:arglists ([sha256-hex source-text java-source]), :admission :none},
  test-source {:arglists ([instruction env]), :admission :none},
  validate-plan!
  {:arglists ([validate-runtime-plan! fail! target source-path plan]),
   :admission :none},
  value-declaration
  {:arglists
   ([byte-array-source runtime-bytes instruction counter indent]),
   :admission :none},
  value-source
  {:arglists
   ([value-declaration
     write-source
     test-source
     value-source
     instruction
     counter
     indent
     env]),
   :admission :none},
  write-source {:arglists ([descriptor indent]), :admission :none}},
 :supported
 [:existing-java-source-emission
  :scalar-control-flow-data
  :partial-source-map
  :plan-rejection-adapter
  :compatibility-var-interposition
  :existing-host-process-invocation
  :tool-version-records
  :create-new-utf8-file-output
  :classfile-header-inspection
  :deterministic-stored-jar-output
  :jar-structure-inspection
  :supplied-content-hash-comparison],
 :dependency-direction
 {:requires [clojure.string],
  :consumed-by [gravity.bootstrap],
  :forbids [gravity.bootstrap gravity.cli gravity.release]},
 :governing-contracts
 [D1 D2 D3 D6 D8 D9 C1 C14 C15 B1 B5 B13 B14 P4 TEST2],
 :unsupported
 [:untrusted-input-admission
  :b1-mir-verification
  :full-source-map
  :artifact-publication-eligibility
  :full-b13-manifest-verification
  :null-interop-reflection-exception-and-resource-policy
  :full-b5-conformance
  :gravity-b5-execution-parity
  :migration
  :release
  :self-hosting
  :seed-retirement],
 :artifact-inputs
 [:caller-owned-scalar-control-flow-plan
  :counter
  :lexical-environment
  :source-text
  :output-prefix
  :explicit-compatibility-operations
  :caller-owned-commands-paths-and-class-bytes
  :supplied-content-hashes
  :late-target-data-vars],
 :namespace gravity.backend.jvm,
 :diagnostic-ownership
 {:stage ["C14-UNSUPPORTED" "B5-TARGET" "B13-HASH"
           :jvm-stage2-lowering-rule :java21-target-toolchain
           :jvm-target-process-completion :jvm-target-tool-acceptance
           :content-hash-integrity],
  :existing-owner
  [:scalar-validation :diagnostic-construction :input-admission]},
 :artifact-outputs
 [:program-java-source
  :module-info-java-source
  :value-descriptor
  :partial-source-unit-map
  :output-path-data
  :passed-or-existing-refusal
  :process-stdout-octets-and-stderr
  :tool-version-record
  :utf8-file
  :classfile-major
  :deterministic-stored-jar
  :jar-structure-record],
 :issues-certificates? false,
 :contract-boundary :internal-hosted-java-source-and-output}
))

(defn validate-plan!
  [validate-runtime-plan! fail! target source-path plan]
  (try
    ;; Reuse the shared iterative scalar/control-flow validator, but remap its
    ;; historical C diagnostic at this concrete backend boundary.
    (validate-runtime-plan! source-path target plan)
    (catch clojure.lang.ExceptionInfo ex
      (if (= "B2-UNSUPPORTED" (:id (ex-data ex)))
        (fail!
         "C14-UNSUPPORTED"
         "JVM backend cannot lower this stage2 instruction plan"
         source-path plan
         {:unsupported-op (:unsupported-op (ex-data ex))
          :missing-fact :jvm-stage2-lowering-rule})
        (throw ex))))
  :passed)

(defn byte-array-source
  [name bytes indent]
  (let [padding (apply str (repeat indent "  "))]
    (str padding "byte[] " name " = new byte[]{"
         (str/join "," (map #(str "(byte)" %) bytes)) "};\n")))

(defn value-declaration
  [byte-array-source runtime-bytes instruction counter indent]
  (let [name (str "gravityValue" (swap! counter inc))
        value (:value instruction)]
    {:source (byte-array-source
              name (runtime-bytes value) indent)
     :descriptor {:name name
                  :truth (not (or (nil? value) (false? value)))}}))

(defn write-source
  [descriptor indent]
  (let [padding (apply str (repeat indent "  "))
        name (:name descriptor)]
    (str padding "System.out.write(" name ", 0, " name ".length);\n")))

(defn test-source
  [instruction env]
  (case (:op instruction)
    :literal (if (or (nil? (:value instruction))
                     (false? (:value instruction))) "false" "true")
    :quote (if (or (nil? (:value instruction))
                   (false? (:value instruction))) "false" "true")
    :local (if (get-in env [(:name instruction) :truth]) "true" "false")
    "false"))

(defn value-source
  [value-declaration write-source test-source value-source instruction counter indent env]
  (let [padding (apply str (repeat indent "  "))
        op (:op instruction)]
    (cond
      (#{:literal :quote} op)
      (let [{:keys [source descriptor]}
            (value-declaration instruction counter indent)]
        (str source (write-source descriptor indent)))

      (= :local op)
      (write-source (get env (:name instruction)) indent)

      (= :builtin-call op)
      (apply str
             (map #(value-source % counter indent env)
                  (:args instruction)))

      (= :if op)
      (str padding "if ("
           (test-source (:test instruction) env)
           ") {\n"
           (value-source (:then instruction) counter
                                     (inc indent) env)
           padding "} else {\n"
           (value-source (:else instruction) counter
                                     (inc indent) env)
           padding "}\n")

      (= :let op)
      (let [binding-state
            (reduce (fn [state {:keys [name expr]}]
                      (let [{:keys [source descriptor]}
                            (value-declaration
                             expr counter indent)]
                        {:source (str (:source state) source)
                         :env (assoc (:env state) name descriptor)}))
                    {:source "" :env env}
                    (:bindings instruction))]
        (str (:source binding-state)
             (value-source
              (first (:body instruction)) counter indent
              (:env binding-state))))

      :else "")))

(defn instruction-source
  ([_byte-array-source _write-source _value-source _test-source _value-declaration instruction-source instruction counter indent]
   (instruction-source instruction counter indent {}))
  ([byte-array-source write-source value-source test-source value-declaration instruction-source instruction counter indent env]
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
                               (str (byte-array-source
                                     name [32] indent)
                                    (write-source
                                     {:name name} indent))))
                           (value-source
                            argument counter indent env)))
                    (:args instruction)))
            (let [name (str "gravityNewline" (swap! counter inc))]
              (str (byte-array-source name [10] indent)
                   (write-source {:name name} indent))))

       (= :do op)
       (apply str
              (map #(instruction-source % counter indent env)
                   (:body instruction)))

       (= :if op)
       (str padding "if ("
            (test-source (:test instruction) env)
            ") {\n"
            (instruction-source (:then instruction) counter
                                            (inc indent) env)
            padding "} else {\n"
            (instruction-source (:else instruction) counter
                                            (inc indent) env)
            padding "}\n")

       (= :let op)
       (let [binding-state
             (reduce (fn [state {:keys [name expr]}]
                       (let [{:keys [source descriptor]}
                             (value-declaration
                              expr counter indent)]
                         {:source (str (:source state) source)
                          :env (assoc (:env state) name descriptor)}))
                     {:source "" :env env}
                     (:bindings instruction))]
         (str (:source binding-state)
              (apply str
                     (map #(instruction-source
                            % counter indent (:env binding-state))
                          (:body instruction)))))

       :else ""))))

(defn java-source
  [instruction-source plan]
  (let [counter (atom 0)
        main (get-in plan [:functions (:entrypoint plan)])]
    (str "package gravity.stage2;\n\n"
         "public final class Program {\n"
         "  private Program() {}\n\n"
         "  public static void main(String[] args) {\n"
         (apply str
                (map #(instruction-source % counter 2 {})
                     (:instructions main)))
         "  }\n"
         "}\n")))

(defn module-source
  [module-name]
  (str "module " module-name " {\n" "  exports gravity.stage2;\n" "}\n"))

(defn source-map
  [sha256-hex source-text java-source]
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

(defn output-paths
  [output-path]
  {:java-source (str output-path "/sources/gravity/stage2/Program.java")
   :module-source (str output-path "/sources/module-info.java")
   :class-file (str output-path "/classes/gravity/stage2/Program.class")
   :module-class (str output-path "/classes/module-info.class")
   :jar (str output-path "/program.jar")
   :source-map (str output-path "/source-map.edn")
   :manifest (str output-path "/manifest.edn")
   :provenance (str output-path "/provenance.edn")})


(defn
 run-process!
 [fail! command source-path diagnostic-id message]
 (let
  [stdout-file
   (java.io.File/createTempFile "gravity-jvm-out-" ".bin")
   stderr-file
   (java.io.File/createTempFile "gravity-jvm-err-" ".txt")]
  (try
   (let
    [pb (ProcessBuilder. (vec command))]
    (.redirectOutput pb stdout-file)
    (.redirectError pb stderr-file)
    (let
     [process
      (.start pb)
      finished?
      (.waitFor
       process
       60000
       java.util.concurrent.TimeUnit/MILLISECONDS)]
     (when-not
      finished?
      (.destroyForcibly process)
      (fail!
       diagnostic-id
       message
       source-path
       nil
       {:command (vec command),
        :missing-fact :jvm-target-process-completion}))
     (let
      [result
       {:exit (.exitValue process),
        :stdout-bytes
        (vec
         (map
          (fn* [p1__154#] (bit-and (int p1__154#) 255))
          (java.nio.file.Files/readAllBytes (.toPath stdout-file)))),
        :stderr (if (.exists stderr-file) (slurp stderr-file) "")}]
      (when-not
       (zero? (:exit result))
       (fail!
        diagnostic-id
        message
        source-path
        nil
        {:command (vec command),
         :process-result result,
         :missing-fact :jvm-target-tool-acceptance}))
      result)))
   (catch clojure.lang.ExceptionInfo ex (throw ex))
   (catch
    Exception
    ex
    (fail!
     "B5-TARGET"
     "JVM target tool is unavailable"
     source-path
     nil
     {:command (vec command),
      :cause-message (.getMessage ex),
      :missing-fact :java21-target-toolchain}))
   (finally (.delete stdout-file) (.delete stderr-file)))))

(defn
 tool-version!
 [run-process! fail! target-release command source-path]
 (let
  [result
   (run-process!
    [command "-version"]
    source-path
    "B5-TARGET"
    "JVM target tool version check failed")
   stdout
   (String.
    (byte-array (map byte (:stdout-bytes result)))
    java.nio.charset.StandardCharsets/UTF_8)
   output
   (str/trim (str stdout "\n" (:stderr result)))
   major
   (some->
    (re-find #"(?:version )?\"?([0-9]+)" output)
    second
    Long/parseLong)]
  (when-not
   (and major (>= major (deref target-release)))
   (fail!
    "B5-TARGET"
    "JVM target tool cannot produce or run Java 21 artifacts"
    source-path
    nil
    {:command command,
     :observed-version output,
     :required-major (deref target-release),
     :missing-fact :java21-target-toolchain}))
  {:command command, :version output, :major major}))

(defn
 write-file!
 [path content]
 (java.nio.file.Files/createDirectories
  (.getParent path)
  (make-array java.nio.file.attribute.FileAttribute 0))
 (java.nio.file.Files/write
  path
  (.getBytes (str content) java.nio.charset.StandardCharsets/UTF_8)
  (into-array
   java.nio.file.OpenOption
   [java.nio.file.StandardOpenOption/CREATE_NEW
    java.nio.file.StandardOpenOption/WRITE])))

(defn
 stored-jar-entry!
 [jar-output entry-name bytes]
 (let
  [crc
   (java.util.zip.CRC32.)
   entry
   (java.util.jar.JarEntry. entry-name)]
  (.update crc bytes)
  (.setTime entry 0)
  (.setMethod entry java.util.zip.ZipEntry/STORED)
  (.setSize entry (alength bytes))
  (.setCompressedSize entry (alength bytes))
  (.setCrc entry (.getValue crc))
  (.putNextEntry jar-output entry)
  (.write jar-output bytes 0 (alength bytes))
  (.closeEntry jar-output)))

(defn
 write-jar!
 [stored-jar-entry! main-class jar-path class-directory]
 (let
  [manifest-bytes
   (.getBytes
    (str
     "Manifest-Version: 1.0\r\n"
     "Main-Class: "
     (deref main-class)
     "\r\n\r\n")
    java.nio.charset.StandardCharsets/UTF_8)
   entries
   [["META-INF/MANIFEST.MF" manifest-bytes]
    ["gravity/stage2/Program.class"
     (java.nio.file.Files/readAllBytes
      (.resolve class-directory "gravity/stage2/Program.class"))]
    ["module-info.class"
     (java.nio.file.Files/readAllBytes
      (.resolve class-directory "module-info.class"))]]]
  (with-open
   [output
    (java.util.jar.JarOutputStream.
     (java.nio.file.Files/newOutputStream
      jar-path
      (into-array
       java.nio.file.OpenOption
       [java.nio.file.StandardOpenOption/CREATE_NEW
        java.nio.file.StandardOpenOption/WRITE])))]
   (doseq
    [[entry-name bytes] entries]
    (stored-jar-entry! output entry-name bytes)))))

(defn
 classfile-major
 [path]
 (let
  [bytes (java.nio.file.Files/readAllBytes path)]
  (when
   (and
    (>= (alength bytes) 8)
    (=
     [202 254 186 190]
     (mapv
      (fn* [p1__155#] (bit-and (int (aget bytes p1__155#)) 255))
      (range 4))))
   (+
    (bit-shift-left (bit-and (int (aget bytes 6)) 255) 8)
    (bit-and (int (aget bytes 7)) 255)))))

(defn
 jar-record
 [jar-path]
 (with-open
  [jar (java.util.jar.JarFile. (.toFile jar-path))]
  (let
   [module-entry
    (.getJarEntry jar "module-info.class")
    module-name
    (when
     module-entry
     (with-open
      [input (.getInputStream jar module-entry)]
      (.name (java.lang.module.ModuleDescriptor/read input))))]
   {:entries
    (->>
     (enumeration-seq (.entries jar))
     (map (fn* [p1__156#] (.getName p1__156#)))
     sort
     vec),
    :main-class
    (some->
     jar
     .getManifest
     .getMainAttributes
     (.getValue java.util.jar.Attributes$Name/MAIN_CLASS)),
    :module-name module-name})))

(defn
 validate-content-hashes!
 [sha256-bytes-hex fail! source-path manifest paths]
 (doseq
  [[kind expected] (:content-hashes manifest)]
  (let
   [path
    (get paths kind)
    actual
    (when
     (and path (.isFile (java.io.File. path)))
     (str
      "sha256:"
      (sha256-bytes-hex
       (java.nio.file.Files/readAllBytes
        (.toPath (java.io.File. path))))))]
   (when-not
    (= expected actual)
    (fail!
     "B13-HASH"
     "JVM emitted artifact content hash is stale or mismatched"
     source-path
     nil
     {:artifact-kind kind,
      :artifact-path path,
      :expected-content-hash expected,
      :actual-content-hash actual,
      :missing-fact :content-hash-integrity}))))
 :passed)

(doseq [[_ operation] (ns-publics *ns*)]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
