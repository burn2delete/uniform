(ns gravity.backend.jvm.pipeline-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.backend.jvm :as jvm]
            [gravity.bootstrap :as bootstrap]))

(defn- native [name & arguments]
  (apply (ns-resolve 'gravity.bootstrap (symbol (str "jvm-backend-" name))) arguments))

(defn- literal [value] {:op :literal :value value})
(defn- print-values [& arguments] {:op :println :args (vec arguments)})
(defn- plan [instructions]
  {:entrypoint 'main :functions {'main {:instructions instructions}}})

(defn- fixtures []
  {"scalars" (plan [(print-values (literal "a\u0000\u03bb")
                                 (literal nil) (literal false) (literal true)
                                 (literal Long/MIN_VALUE) (literal Long/MAX_VALUE)
                                 {:op :quote :value "quoted"})])
   "control" (plan [{:op :do :body [(literal 1) {:op :quote :value "unused"}
                                   (print-values)]}
                     {:op :let
                      :bindings [{:name 'x :expr (literal "local")}
                                 {:name 'f :expr (literal false)}]
                      :body [{:op :local :name 'x}
                             {:op :if :test {:op :local :name 'f}
                              :then (print-values (literal "wrong"))
                              :else (print-values {:op :local :name 'x})}
                             (print-values
                              {:op :let :bindings [{:name 'v :expr (literal "nested")}]
                               :body [{:op :local :name 'v}]}
                              {:op :if :test (literal 0)
                               :then (literal "truthy") :else (literal "wrong")}
                              {:op :builtin-call :function 'str
                               :args [(literal "a") (literal "b")]})]}])})

(defn- rejected [input]
  (try (native "validate-plan!" "jvm-source.gravity" input)
       :unexpectedly-accepted
       (catch clojure.lang.ExceptionInfo ex (ex-data ex))))

(defn- capture-native []
  {:fixtures
   (into (sorted-map)
         (for [[label input] (fixtures)
               :let [java (native "java-source" input)]]
           [label {:input input :validation (native "validate-plan!" "jvm-source.gravity" input)
                   :java java :module bootstrap/jvm-backend-module-source
                   :source-map (native "source-map" "(println :fixture)" java)
                   :paths (native "output-paths" "out/jvm")}]))
   :refusals (mapv #(rejected (plan [%]))
                   [{:op :unsupported :source-span {:source "original.gravity" :start {:line 7 :column 3}}}
                    {:op :println :args nil}
                    (print-values {:op :local :name 'missing})
                    (print-values {:op :builtin-call :function 'str :args []})
                    {:op :let :bindings nil :body []}])})

(deftest real-java-source-and-plan-refusals
  (let [{:keys [fixtures refusals]} (capture-native)]
    (doseq [[label {:keys [validation java module source-map paths]}] fixtures]
      (is (= :passed validation) label)
      (is (str/includes? java "package gravity.stage2;"))
      (is (str/includes? java "public static void main(String[] args)"))
      (is (str/includes? java "System.out.write("))
      (is (= "module gravity.stage {\n  exports gravity.stage2;\n}\n" module))
      (is (= :partial (:status source-map)))
      (is (= :source-unit-only (:coverage source-map)))
      (is (false? (:per-form-origin-preserved? source-map)))
      (is (= "(println :fixture)" (get-in source-map [:source :content])))
      (is (= "out/jvm/sources/gravity/stage2/Program.java" (:java-source paths)))
      (is (= java (native "java-source" (get-in fixtures [label :input])))))
    (is (str/includes? (get-in fixtures ["scalars" :java]) "(byte)0,(byte)206,(byte)187"))
    (is (str/includes? (get-in fixtures ["control" :java]) "if (false)"))
    (is (every? #(= "C14-UNSUPPORTED" (:id %)) refusals))
    (is (every? #(= :jvm-stage2-lowering-rule (:missing-fact %)) refusals))
    (is (every? #(= :jvm-backend-lowering (:stage %)) refusals))
    (is (every? #(= :rejected (:fallback-status %)) refusals))))

(deftest explicit-api-and-bootstrap-arities
  (let [expected {'validate-plan! '([source-path plan])
                  'byte-array-source '([name bytes indent])
                  'value-declaration '([instruction counter indent])
                  'write-source '([descriptor indent])
                  'test-source '([instruction env])
                  'value-source '([instruction counter indent env])
                  'instruction-source '([instruction counter indent] [instruction counter indent env])
                  'java-source '([plan]) 'source-map '([source-text java-source])
                  'output-paths '([output-path])}
        contract (:gravity/namespace-contract (meta #'jvm/java-source))]
    (doseq [[name args] expected]
      (is (= args (:arglists (meta (ns-resolve 'gravity.bootstrap (symbol (str "jvm-backend-" name)))))) name))
    (is (= (set (keys (ns-publics 'gravity.backend.jvm))) (set (keys (:public-api contract)))))
    (is (every? #(= :none (:admission %)) (vals (:public-api contract))))
    (is (false? (:authenticates-inputs? contract)))
    (is (false? (:issues-certificates? contract)))
    (is (= '[clojure.string] (get-in contract [:dependency-direction :requires])))
    (is (= '[gravity.bootstrap] (get-in contract [:dependency-direction :consumed-by])))
    (is (= bootstrap/jvm-backend-module-source (jvm/module-source bootstrap/jvm-backend-module-name)))
    (is (= "" (native "value-source" {:op :unsupported} (atom 0) 0 {})))
    (is (= "" (native "instruction-source" {:op :unsupported} (atom 0) 0)))))

(deftest recursive-ports-preserve-order-and-late-interposition
  (let [input (plan [(print-values (literal "a") (literal "b"))])
        expected (native "java-source" input)
        byte-var #'bootstrap/jvm-backend-byte-array-source
        write-var #'bootstrap/jvm-backend-write-source
        old-byte @byte-var old-write @write-var events (atom [])]
    (with-redefs-fn {byte-var (fn [name bytes indent]
                               (swap! events conj [:byte name bytes indent])
                               (old-byte name bytes indent))
                    write-var (fn [descriptor indent]
                                (swap! events conj [:write (:name descriptor) indent])
                                (old-write descriptor indent))}
      #(is (= expected (native "java-source" input))))
    (is (= [:byte :write :byte :write :byte :write :byte :write] (mapv first @events)))
    (is (= ["gravityValue1" "gravityValue1" "gravitySpace2" "gravitySpace2"
            "gravityValue3" "gravityValue3" "gravityNewline4" "gravityNewline4"]
           (mapv second @events)))
    (with-redefs [jvm/java-source (fn [instruction-source plan]
                                  [(var? instruction-source) plan])]
      (is (= [true input] (native "java-source" input))))))

(deftest validator-keeps-original-unrelated-exception-and-target-data
  (let [ex (ex-info "unrelated validator exception" {:id "TEST-EXCEPTION"})
        seen (atom []) input (plan [])]
    (is (identical? ex
          (try (jvm/validate-plan! (fn [& args] (swap! seen conj args) (throw ex))
                                   (fn [& _] (throw (Exception. "unexpected remapping")))
                                   :test-target "subject.gravity" input)
               nil (catch clojure.lang.ExceptionInfo observed observed))))
    (is (= [["subject.gravity" :test-target input]] @seen))))

(defn- observe-toolchain [f]
  (try {:status :accepted :value (f)}
       (catch Exception ex
         {:status :rejected :class (.getName (class ex))
          :message (.getMessage ex) :data (ex-data ex)})))

(defn- file-bytes [file]
  (mapv #(bit-and (int %) 255)
        (java.nio.file.Files/readAllBytes (.toPath (io/file file)))))

(defn- remove-test-directory! [directory]
  (doseq [file (reverse (file-seq (io/file directory)))]
    (java.nio.file.Files/deleteIfExists (.toPath file))))

(defn- toolchain-products [directory extension]
  ;; Only ordinary fixed-seed source compilation and trusted internal output
  ;; helpers are used here. No packet, authority token or B1 admission is minted.
  (let [source-path (.getCanonicalPath
                     (io/file
                      (str "bootstrap/clojure/fixtures/jvm-toolchain/accepted" extension)))
        source-text (slurp source-path :encoding "UTF-8")
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source-path :jvm))
        input (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source-path source-text)
        java-source (native "java-source" input)
        source-map (native "source-map" source-text java-source)
        work (io/file directory "work")
        paths (native "output-paths" (.getPath work))
        _ (native "write-file!" (.toPath (io/file (:java-source paths))) java-source)
        _ (native "write-file!" (.toPath (io/file (:module-source paths))) bootstrap/jvm-backend-module-source)
        classes (io/file work "classes")
        _ (.mkdirs classes)
        command ["javac" "--release" "21" "-encoding" "UTF-8" "-g:source,lines"
                 "-proc:none" "-implicit:none" "-Werror" "-d" (.getPath classes)
                 (:module-source paths) (:java-source paths)]
        compiler-result (native "run-process!" command source-path "B5-TARGET"
                                "javac rejected generated Java 21 source")
        _ (native "write-deterministic-jar!" (.toPath (io/file (:jar paths))) (.toPath classes))
        jar-record (native "jar-record" (.toPath (io/file (:jar paths))))
        class-major (native "classfile-major" (.toPath (io/file (:class-file paths))))
        duplicate (io/file directory "second.jar")
        _ (native "write-deterministic-jar!" (.toPath duplicate)
                  (.toPath (io/file work "classes")))
        artifacts (into (sorted-map)
                        (for [kind [:java-source :module-source :class-file :module-class :jar]]
                          [kind {:path (get paths kind) :bytes (file-bytes (get paths kind))}]))
        hashes (into (sorted-map)
                     (for [[kind {:keys [bytes]}] artifacts]
                       [kind (str "sha256:" (bootstrap/sha256-bytes-hex
                                             (byte-array (map unchecked-byte bytes))))]))
        manifest {:content-hashes hashes}
        verified (native "validate-content-hashes!" source-path manifest paths)
        repeated? (= (file-bytes duplicate) (file-bytes (:jar paths)))
        validation (native "validate-plan!" source-path input)
        plan-id (str "sha256:" (bootstrap/sha256-hex
                               (pr-str (bootstrap/c-backend-canonical-value (dissoc input :plan-id)))))
        _ (assert (= plan-id (:plan-id input)))
        _ (assert (= (str "sha256:" (bootstrap/sha256-hex source-text))
                     (get-in source-map [:source :content-hash])))
        execution (native "run-process!" ["java" "-jar" (:jar paths)] source-path
                          "B5-TARGET" "JVM test execution failed")
        _ (spit (:java-source paths) "this is not Java")
        invalid-java (observe-toolchain
                      #(native "run-process!" command source-path "B5-TARGET"
                               "javac rejected generated Java 21 source"))
        _ (spit (:java-source paths) java-source)
        wrong-class (let [path (.toPath (io/file (:class-file paths)))
                          bytes (java.nio.file.Files/readAllBytes path)]
                      (aset-byte bytes 7 (byte 64))
                      (java.nio.file.Files/write path bytes (make-array java.nio.file.OpenOption 0))
                      (native "classfile-major" path))
        tamper (observe-toolchain #(native "validate-content-hashes!" source-path manifest paths))
        existing (observe-toolchain #(native "write-file!" (.toPath (io/file (:java-source paths))) "replacement"))
        rejected-path (.getCanonicalPath
                       (io/file
                        (str "bootstrap/clojure/fixtures/jvm-toolchain/rejected" extension)))
        rejected-source (slurp rejected-path :encoding "UTF-8")
        rejected-emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! rejected-path :jvm))
        rejected-plan (bootstrap/p15-s23-stage2-compiler-artifact-plan rejected-emitter rejected-path rejected-source)
        source-refusal (observe-toolchain #(native "validate-plan!" rejected-path rejected-plan))]
    {:boundary :ordinary-fixed-seed-source-and-internal-java-output
     :source {:path source-path :text source-text :sha256 (bootstrap/sha256-hex source-text)}
     :emitter emitter :plan input :recomputed-plan-id plan-id :validation validation
     :java-source java-source :module-source bootstrap/jvm-backend-module-source
     :source-map source-map :tool-versions (mapv #(native "tool-version!" % source-path) ["javac" "java"])
     :artifacts artifacts :content-hashes hashes :hash-validation verified
     :jar-record jar-record :class-major class-major :altered-class-major wrong-class
     :compiler-command command :compiler-result compiler-result
     :repeated-jar-bytes-equal? repeated? :execution execution
     :rejected-source {:path rejected-path :text rejected-source
                       :sha256 (bootstrap/sha256-hex rejected-source) :plan rejected-plan}
     :refusals {:source source-refusal :javac invalid-java
                :tamper tamper :existing-file existing}}))

(deftest real-source-java21-jar-and-refusals
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "gravity-jvm-toolchain-test-"
                            (make-array java.nio.file.attribute.FileAttribute 0)))]
    (try
      (doseq [extension [".gravity" ".qst"]
              :let [root (io/file directory (subs extension 1))
                    _ (.mkdirs root)
                    product (toolchain-products (.getPath root) extension)]]
        (is (= :passed (:validation product) (:hash-validation product)))
        (is (= 65 (:class-major product)))
        (is (= 64 (:altered-class-major product)))
        (is (true? (:repeated-jar-bytes-equal? product)))
        (is (= {:entries ["META-INF/MANIFEST.MF" "gravity/stage2/Program.class" "module-info.class"]
                :main-class "gravity.stage2.Program" :module-name "gravity.stage"}
               (:jar-record product)))
        (is (= 0 (get-in product [:execution :exit]) (get-in product [:compiler-result :exit])))
        (is (= "gravity 42  false\nlocal\n"
               (String. (byte-array (map unchecked-byte (get-in product [:execution :stdout-bytes])))
                        java.nio.charset.StandardCharsets/UTF_8)))
        (is (= "C14-UNSUPPORTED" (get-in product [:refusals :source :data :id])))
        (is (= :vector-literal (get-in product [:refusals :source :data :unsupported-op])))
        (is (= "B5-TARGET" (get-in product [:refusals :javac :data :id])))
        (is (= :jvm-target-tool-acceptance (get-in product [:refusals :javac :data :missing-fact])))
        (is (= "B13-HASH" (get-in product [:refusals :tamper :data :id])))
        (is (= :class-file (get-in product [:refusals :tamper :data :artifact-kind])))
        (is (= "java.nio.file.FileAlreadyExistsException" (get-in product [:refusals :existing-file :class]))))
      (finally (remove-test-directory! directory)))))

(deftest toolchain-api-and-late-wrapper-ports
  (doseq [[old owner arguments port-count]
          [['run-process! #'jvm/run-process! '[command source-path diagnostic-id message] 1]
           ['tool-version! #'jvm/tool-version! '[command source-path] 3]
           ['write-file! #'jvm/write-file! '[path content] 0]
           ['stored-jar-entry! #'jvm/stored-jar-entry! '[jar-output entry-name bytes] 0]
           ['write-deterministic-jar! #'jvm/write-jar! '[jar-path class-directory] 2]
           ['classfile-major #'jvm/classfile-major '[path] 0]
           ['jar-record #'jvm/jar-record '[jar-path] 0]
           ['validate-content-hashes! #'jvm/validate-content-hashes! '[source-path manifest paths] 2]]]
    (let [compatibility (ns-resolve 'gravity.bootstrap (symbol (str "jvm-backend-" old)))
          values (vec (repeat (count arguments) (Object.)))
          calls (atom nil)]
      (is (= (list arguments) (:arglists (meta compatibility))) old)
      (with-redefs-fn {owner (fn [& args] (reset! calls args) :observed)}
        #(is (= :observed (apply compatibility values)) old))
      (is (every? var? (take port-count @calls)) old)
      (is (= values (vec (drop port-count @calls))) old)))
  (let [contract (:gravity/namespace-contract (meta #'jvm/run-process!))]
    (is (= (set (keys (ns-publics 'gravity.backend.jvm))) (set (keys (:public-api contract)))))
    (doseq [[name operation] (ns-publics 'gravity.backend.jvm)]
      (is (= (:arglists (meta operation)) (get-in contract [:public-api name :arglists])) name)
      (is (= contract (:gravity/namespace-contract (meta operation)))))))

(deftest tool-versions-and-hashes-preserve-late-data-and-failure
  (let [target (with-local-vars [release 21] release)
        calls (atom [])
        run (fn [& args]
              (swap! calls conj args)
              (alter-var-root target (constantly 22))
              {:exit 0 :stdout-bytes (mapv int "javac 21.0.1\n") :stderr ""})
        failure (ex-info "original refusal" {:id :original})
        observed (atom nil)]
    (is (identical? failure
                   (try (jvm/tool-version! run
                          (fn [& args] (reset! observed args) (throw failure))
                          target "compiler" "source.gravity")
                        nil (catch clojure.lang.ExceptionInfo ex ex))))
    (is (= [[ ["compiler" "-version"] "source.gravity" "B5-TARGET"
              "JVM target tool version check failed"]] @calls))
    (is (= 22 (get-in (vec @observed) [4 :required-major])))
    (is (= "javac 21.0.1" (get-in (vec @observed) [4 :observed-version]))))
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "gravity-jvm-hash-test-" (make-array java.nio.file.attribute.FileAttribute 0)))
        file (io/file directory "content")
        failure (ex-info "late hash refusal" {:id :late})
        fail-var #'bootstrap/jvm-backend-fail!
        hash-var #'bootstrap/sha256-bytes-hex
        observed (atom nil)]
    (try
      (spit file "actual")
      (with-redefs-fn {hash-var (fn [_]
                                 (alter-var-root fail-var
                                   (constantly (fn [& args] (reset! observed args) (throw failure))))
                                 "changed")}
        #(with-redefs-fn {fail-var (fn [& _] (throw (Exception. "eager failure selected")))}
           (fn []
             (is (identical? failure
                    (try (native "validate-content-hashes!" "source.gravity"
                                 {:content-hashes {:jar "sha256:expected"}} {:jar (.getPath file)})
                         nil (catch clojure.lang.ExceptionInfo ex ex)))))))
      (is (= "B13-HASH" (first @observed)))
      (is (= "sha256:changed" (get-in (vec @observed) [4 :actual-content-hash])))
      (is (= :passed (native "validate-content-hashes!" "source.gravity" {} {})))
      (is (= :passed (native "validate-content-hashes!" "source.gravity" nil nil)))
      (let [missing (observe-toolchain
                     #(native "validate-content-hashes!" "source.gravity"
                              {:content-hashes {:jar "sha256:missing"}} {}))]
        (is (= "B13-HASH" (get-in missing [:data :id])))
        (is (nil? (get-in missing [:data :actual-content-hash]))))
      (doseq [content ["short" "invalid-header"]]
        (spit file content)
        (is (nil? (native "classfile-major" (.toPath file)))))
      (is (= "java.util.zip.ZipException"
             (:class (observe-toolchain #(native "jar-record" (.toPath file))))))
      (finally (remove-test-directory! directory)))))

(deftest jar-entry-order-late-interposition-and-create-new
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "gravity-jvm-jar-test-" (make-array java.nio.file.attribute.FileAttribute 0)))
        classes (io/file directory "classes")
        file (io/file directory "result.jar")
        stored-var #'bootstrap/jvm-backend-stored-jar-entry!
        old-stored @stored-var
        entries (atom [])]
    (try
      (doseq [name ["gravity/stage2/Program.class" "module-info.class"]]
        (native "write-file!" (.toPath (io/file classes name)) "opaque class bytes"))
      (with-redefs-fn {stored-var
                      (fn [output name bytes]
                        (swap! entries conj [:first name])
                        (alter-var-root stored-var
                          (constantly (fn [next-output next-name next-bytes]
                                        (swap! entries conj [:late next-name])
                                        (old-stored next-output next-name next-bytes))))
                        (old-stored output name bytes))}
        #(native "write-deterministic-jar!" (.toPath file) (.toPath classes)))
      (is (= [[:first "META-INF/MANIFEST.MF"] [:late "gravity/stage2/Program.class"]
              [:late "module-info.class"]] @entries))
      (with-open [jar (java.util.jar.JarFile. file)]
        (doseq [entry (enumeration-seq (.entries jar))]
          (is (= java.util.zip.ZipEntry/STORED (.getMethod ^java.util.jar.JarEntry entry)))
          (is (= 0 (.getTime ^java.util.jar.JarEntry entry)))
          (is (= (.getSize ^java.util.jar.JarEntry entry)
                 (.getCompressedSize ^java.util.jar.JarEntry entry)))))
      (let [before (file-bytes file)]
        (is (= "java.nio.file.FileAlreadyExistsException"
               (:class (observe-toolchain #(native "write-deterministic-jar!" (.toPath file) (.toPath classes))))))
        (is (= before (file-bytes file))))
      (finally (remove-test-directory! directory)))))

(deftest real-process-unavailable-tool-and-temporary-output-cleanup
  (let [unavailable (observe-toolchain #(native "run-process!"
                                         ["/gravity-test-no-such-tool"] "source.gravity"
                                         "B5-TARGET" "test tool failed"))
        old-slurp slurp
        observed-files (atom [])
        observe-stderr (fn [file & args]
                         (when (and (instance? java.io.File file)
                                    (str/starts-with? (.getName ^java.io.File file) "gravity-jvm-err-"))
                           (swap! observed-files conj file))
                         (apply old-slurp file args))]
    (is (= "B5-TARGET" (get-in unavailable [:data :id])))
    (is (= :java21-target-toolchain (get-in unavailable [:data :missing-fact])))
    (with-redefs [clojure.core/slurp observe-stderr]
      (is (= 0 (:exit (native "run-process!" ["java" "-version"]
                              "source.gravity" "B5-TARGET" "version probe failed")))))
    (is (= 1 (count @observed-files)))
    (is (every? #(not (.exists ^java.io.File %)) @observed-files))
    (let [failure (ex-info "original process failure" {:id :original-process})]
      (with-redefs [bootstrap/jvm-backend-fail! (fn [& _] (throw failure))
                    clojure.core/slurp observe-stderr]
        (is (identical? failure
               (try (native "run-process!" ["java" "--gravity-unknown-option"]
                            "source.gravity" "B5-TARGET" "test process refused")
                    nil (catch clojure.lang.ExceptionInfo ex ex)))))
      (is (= 2 (count @observed-files)))
      (is (every? #(not (.exists ^java.io.File %)) @observed-files)))))
