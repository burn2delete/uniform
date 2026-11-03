(ns gravity.bootstrap.rebuild.component
  "Build one persisted Gravity compiler component through the pinned seed.

  This boundary intentionally produces a serialized, executable plan for the
  native-plan emitter.  It does not claim to rebuild the whole compiler or
  retire the Clojure seed."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [gravity.bootstrap.rebuild.inputs :as inputs]
            [gravity.digest :as digest])
  (:import [java.io File]
           [java.nio.charset StandardCharsets]
           [java.nio.file Files LinkOption StandardOpenOption]))

(def ^:private component-id
  :gravity/p15-s23-native-plan-c-emitter-plan)
(def ^:private helper-source-path
  "bootstrap/gravity/p15_s23/native_plan_c_emitter.gravity")
(def ^:private helper-source-sha256
  "sha256:04645a82e66d024c6505ea3ec80c9789a7d0545ef8e7222c5d78cad68fc92adc")
(def ^:private accepted-fixture
  {:path "bootstrap/clojure/fixtures/accepted/hello.gravity"
   :sha256 "sha256:eb28603def64689a2e4d7018c1f356ea0cffd840600588843f29b1b30574b219"
   :expect :accepted
   :expected-output "Hello Gravity\n"
   :expected-source-fragment "Hello Gravity"})
(def ^:private rejected-fixture
  {:path "bootstrap/clojure/fixtures/jvm-toolchain/rejected.gravity"
   :sha256 "sha256:4ecd02f22528ae06c63a4210c1b7c9a65a9e8f57ff7db73d40c46bb80c179e3d"
   :expect :rejected
   :diagnostic "P15GCE002"})
(def ^:private default-component
  {:component-id component-id
   :source-path helper-source-path
   :source-sha256 helper-source-sha256
   :fixtures [accepted-fixture rejected-fixture]
   :plan-output-path "target/bootstrap-rebuild/native-plan-c-emitter.plan.edn"
   :source-output-path "target/bootstrap-rebuild/native-plan-c-emitter.c"
   :record-output-path "target/bootstrap-rebuild/native-plan-c-emitter.edn"})

(defn seed-built-component
  "Return the fixed declaration for the persisted emitter-plan component."
  []
  default-component)

(defn- fail!
  [message data]
  (throw (ex-info message
                  (merge {:diagnostic "BOOT-COMPONENT-BUILD"
                          :component-id component-id}
                         data))))

(defn- private-input-fn
  [name]
  (or (some-> (ns-resolve 'gravity.bootstrap.rebuild.inputs name) deref)
      (throw (IllegalStateException.
              (str "bootstrap input helper is unavailable: " name)))))

(defn- sha256-id
  [^bytes bytes]
  (str "sha256:" (digest/sha256-bytes-hex bytes)))

(defn- file-sha256
  [^File file]
  (sha256-id (Files/readAllBytes (.toPath file))))

(defn- bounded-file
  [^File root relative]
  (let [root (.getCanonicalFile root)
        file (.getCanonicalFile (io/file root relative))
        prefix (str (.getPath root) File/separator)]
    (when-not (and (not (.isAbsolute (io/file relative)))
                   (or (= (.getPath root) (.getPath file))
                       (str/starts-with? (.getPath file) prefix)))
      (fail! "component path escaped its root"
             {:path relative :reason :path-boundary}))
    file))

(defn- regular-file
  [^File root relative]
  (let [file (bounded-file root relative)]
    (when-not (Files/isRegularFile (.toPath file)
                                   (make-array LinkOption 0))
      (fail! "component input is not a regular file"
             {:path relative :reason :missing-input}))
    file))

(defn- read-single-edn
  [^bytes bytes label]
  (try
    (with-open [reader (java.io.PushbackReader.
                        (java.io.StringReader.
                         (String. bytes StandardCharsets/UTF_8)))]
      (let [eof (Object.)
            value (edn/read {:eof eof} reader)
            trailing (edn/read {:eof eof} reader)]
        (when (or (identical? eof value)
                  (not (identical? eof trailing)))
          (fail! (str label " is not one EDN value")
                 {:reason :edn-shape}))
        value))
    (catch clojure.lang.ExceptionInfo error
      (throw error))
    (catch Exception error
      (fail! (str label " is not valid EDN")
             {:reason :edn-parse :cause (.getMessage error)}))))

(defn- write-new!
  [^File file value]
  (when (.exists file)
    (fail! "component output already exists"
           {:path (.getPath file) :reason :output-exists}))
  (when-let [parent (.getParentFile file)]
    (Files/createDirectories (.toPath parent)
                             (make-array java.nio.file.attribute.FileAttribute 0)))
  (let [bytes (if (instance? (Class/forName "[B") value)
                ^bytes value
                (.getBytes (if (string? value) value (pr-str value))
                           StandardCharsets/UTF_8))]
    (Files/write (.toPath file)
                 bytes
                 (into-array StandardOpenOption [StandardOpenOption/CREATE_NEW
                                                 StandardOpenOption/WRITE])))
  file)

(defn- component-declaration!
  [component]
  (when-not (= default-component component)
    (fail! "component declaration is not the fixed producer contract"
           {:reason :component-declaration :component component}))
  component)

(defn- check-fixture!
  [^File snapshot {:keys [path sha256]}]
  (let [file (regular-file snapshot path)
        actual (file-sha256 file)]
    (when-not (= sha256 actual)
      (fail! "component fixture digest does not match its declaration"
             {:path path :expected sha256 :actual actual
              :reason :fixture-digest}))
    {:path path :sha256 actual}))

(defn- expression-literal
  [value]
  (pr-str value))

(defn- compile-expression
  [plan-path]
  (str
   "(do "
   "(require '[clojure.java.io :as io] '[gravity.bootstrap :as b]) "
   "(let [source-path " (expression-literal helper-source-path)
   " source-text (slurp source-path)"
   " macro-artifact (b/macro-source-artifact source-path source-text)"
   " module (assoc (:module macro-artifact)"
   " :forms (:expanded-forms macro-artifact))"
   " plan (b/stage0-compiled-core-plan source-path source-text module)"
   " plan-text (pr-str plan)]"
   " (io/make-parents " (expression-literal plan-path) ")"
   " (spit " (expression-literal plan-path) " plan-text)"
   " (println (pr-str {:status :complete"
   " :plan-id (:plan-id plan)"
   " :plan-sha256 (str \"sha256:\" (b/sha256-hex plan-text))"
   " :source-content-hash (get-in plan [:source :sha256])}))))"))

(defn- execution-expression
  [plan-path accepted-path rejected-path record-path]
  (str
   "(do "
   "(require '[clojure.edn :as edn] '[clojure.java.io :as io] "
   "'[clojure.string :as str] "
   "'[gravity.bootstrap :as b] "
   "'[gravity.p15-native-plan-specialization.contract :as c]) "
   "(let [plan-text (slurp " (expression-literal plan-path) ")"
   " helper-plan (edn/read-string plan-text)"
   " accepted-path " (expression-literal accepted-path)
   " accepted-text (slurp accepted-path)"
   " accepted-macro (b/macro-source-artifact accepted-path accepted-text)"
   " accepted-module (assoc (:module accepted-macro)"
   " :forms (:expanded-forms accepted-macro))"
   " accepted-plan (b/stage0-compiled-core-plan accepted-path accepted-text accepted-module)"
   " accepted-output (b/execute-stage0-compiled-plan accepted-plan)"
   " safety (c/helper-safety-proof accepted-plan c/helper-contract)"
   " request {:plan accepted-plan"
   " :safe-printable-ascii? (:safe? safety)"
   " :safety-facts (:facts safety)}"
   " invoke (fn [] (b/execute-stage0-compiled-function"
   " helper-plan c/helper-function [request]))"
   " first-result (invoke) second-result (invoke)"
   " rejected-path " (expression-literal rejected-path)
   " rejected-text (slurp rejected-path)"
   " rejected-macro (b/macro-source-artifact rejected-path rejected-text)"
   " rejected-module (assoc (:module rejected-macro)"
   " :forms (:expanded-forms rejected-macro))"
   " rejected-plan (b/stage0-compiled-core-plan rejected-path rejected-text rejected-module)"
   " rejected-safety (c/helper-safety-proof rejected-plan c/helper-contract)"
   " rejected-request {:plan rejected-plan"
   " :safe-printable-ascii? (:safe? rejected-safety)"
   " :safety-facts (:facts rejected-safety)}"
   " rejected-result (b/execute-stage0-compiled-function"
   " helper-plan c/helper-function [rejected-request])]"
   " (io/make-parents " (expression-literal record-path) ")"
   " (spit " (expression-literal record-path) ""
   " (pr-str {:plan-id (:plan-id helper-plan)"
   " :plan-sha256 (str \"sha256:\" (b/sha256-hex plan-text))"
   " :accepted {:status (:status first-result)"
   " :same-invocation? (= first-result second-result)"
   " :contract (:contract first-result)"
   " :implementation (:implementation first-result)"
   " :source (:source first-result)"
   " :source-sha256 (str \"sha256:\" (b/sha256-hex (:source first-result)))"
   " :expected-output accepted-output"
   " :expected-source-fragment? (str/includes? (:source first-result) \"Hello Gravity\")}"
   " :rejected {:status (:status rejected-result)"
   " :diagnostic (:diagnostic rejected-result)"
   " :missing-fact (get-in rejected-result [:facts :missing-fact])}}))"
   " (println (pr-str {:status :complete :record-path "
   (expression-literal record-path) "}))))"))

(defn- run-child!
  [snapshot expression]
  (let [command-result! (private-input-fn 'command-result)
        result (command-result! snapshot
                                ["clojure" "-Srepro" "-M" "-e"
                                 expression])]
    (when-not (zero? (:exit-code result))
      (fail! "pinned seed child process failed"
             {:reason :child-process
              :command (:command result)
              :exit-code (:exit-code result)
              :stdout-sha256 (:stdout-sha256 result)
              :stderr-sha256 (:stderr-sha256 result)
              :stdout (String. ^bytes (:stdout-bytes result)
                               StandardCharsets/UTF_8)
              :stderr (String. ^bytes (:stderr-bytes result)
                               StandardCharsets/UTF_8)}))
    (assoc result :summary
           (read-single-edn (:stdout-bytes result)
                            "pinned seed child summary"))))

(defn- valid-plan?
  [plan]
  (and (map? plan)
       (= :gravity/stage0-hosted-core-compiled-plan (:kind plan))
       (= :hosted (get-in plan [:module :profile]))
       (= :jvm (get-in plan [:module :target]))
       (= 'main (:entrypoint plan))
       (map? (get-in plan [:functions 'p15-s23-native-c-emit-plan]))))

(defn run-seed-built-component!
  "Persist and execute the fixed Gravity emitter plan from the pinned seed.

  The compiler child creates the plan in the authenticated source snapshot.
  A second child process reloads that exact EDN file and invokes its exported
  function on accepted and rejected stage0-compiled compiler plans.  The
  output is seed-hosted component evidence, not a whole-compiler rebuild."
  ([project-root manifest-path component]
   (run-seed-built-component! project-root manifest-path component project-root))
  ([project-root manifest-path component output-root]
   (let [component (component-declaration! component)
         manifest (inputs/read-manifest manifest-path)
         extra-paths (mapv :path (:fixtures component))
         materialize! (private-input-fn 'materialize-source!)
         delete-tree! (private-input-fn 'delete-tree!)
         load-inputs! (private-input-fn 'load-inputs-from-manifest!)
         toolchain! (private-input-fn 'toolchain-record!)
         output-root (.getCanonicalFile (io/file output-root))
         plan-destination (bounded-file output-root (:plan-output-path component))
         source-destination (bounded-file output-root (:source-output-path component))
         record-destination (bounded-file output-root (:record-output-path component))]
     (when (or (.exists plan-destination)
               (.exists source-destination)
               (.exists record-destination))
       (fail! "seed-built component output already exists"
              {:reason :output-exists
               :paths (filter #(.exists ^File %)
                              [plan-destination source-destination record-destination])}))
     (let [snapshot (atom nil)
           output-files [plan-destination source-destination record-destination]]
       (try
         (let [materialized (materialize! project-root manifest extra-paths)
               _ (reset! snapshot materialized)
               snapshot-root (.getCanonicalFile (.toFile ^java.nio.file.Path
                                                 (:root materialized)))
               loaded (load-inputs! snapshot-root manifest)
               toolchain (toolchain! snapshot-root manifest)
               fixture-records (mapv #(check-fixture! snapshot-root %) (:fixtures component))
               source-file (regular-file snapshot-root (:source-path component))
               source-sha256 (file-sha256 source-file)
               _ (when-not (= (:source-sha256 component) source-sha256)
                   (fail! "component source digest does not match its declaration"
                          {:reason :source-digest
                           :path (:source-path component)
                           :expected (:source-sha256 component)
                           :actual source-sha256}))
               plan-path (str (.getPath snapshot-root) "/target/bootstrap-component.plan.edn")
               child-record-path (str (.getPath snapshot-root) "/target/bootstrap-component.result.edn")
               compile-run (run-child! snapshot-root (compile-expression plan-path))
               plan-file (io/file plan-path)
               _ (when-not (.isFile plan-file)
                   (fail! "seed child did not persist a compiler plan"
                          {:reason :plan-missing :path plan-path}))
               plan-bytes (Files/readAllBytes (.toPath plan-file))
               plan (read-single-edn plan-bytes "persisted compiler plan")
               plan-sha256 (sha256-id plan-bytes)
               _ (when-not (valid-plan? plan)
                   (fail! "persisted compiler plan has the wrong executable shape"
                          {:reason :plan-shape :plan-kind (:kind plan)
                           :entrypoint (:entrypoint plan)}))
               _ (when-not (= (get-in compile-run [:summary :plan-id])
                              (:plan-id plan))
                   (fail! "persisted compiler plan identity differs from child output"
                          {:reason :plan-identity
                           :summary (get-in compile-run [:summary :plan-id])
                           :plan (:plan-id plan)}))
               _ (when-not (= source-sha256
                              (get-in compile-run [:summary :source-content-hash]))
                   (fail! "compiler child source identity differs from the authenticated source"
                          {:reason :source-plan-binding
                           :expected source-sha256
                           :actual (get-in compile-run [:summary :source-content-hash])}))
               _ (when-not (= source-sha256 (get-in plan [:source :sha256]))
                   (fail! "persisted compiler plan source identity differs"
                          {:reason :plan-source-binding
                           :expected source-sha256
                           :actual (get-in plan [:source :sha256])}))
               _ (when-not (= (get-in compile-run [:summary :plan-sha256])
                              plan-sha256)
                   (fail! "persisted compiler plan hash differs from child output"
                          {:reason :plan-digest
                           :summary (get-in compile-run [:summary :plan-sha256])
                           :actual plan-sha256}))
               execute-run (run-child! snapshot-root
                                       (execution-expression
                                        plan-path
                                        (str (.getPath snapshot-root) "/"
                                             (:path accepted-fixture))
                                        (str (.getPath snapshot-root) "/"
                                             (:path rejected-fixture))
                                        child-record-path))
               observation-file (io/file child-record-path)
               observation-bytes (Files/readAllBytes (.toPath observation-file))
               observation (read-single-edn observation-bytes
                                            "persisted-plan execution observation")
               accepted (:accepted observation)
               rejected (:rejected observation)
               _ (when-not (and (= (:plan-id observation) (:plan-id plan))
                                (= (:plan-sha256 observation) plan-sha256))
                   (fail! "execution child observed a different persisted plan"
                          {:reason :execution-plan-identity
                           :expected-id (:plan-id plan)
                           :actual-id (:plan-id observation)
                           :expected-sha256 plan-sha256
                           :actual-sha256 (:plan-sha256 observation)}))
               _ (when-not (and (= :complete (:status accepted))
                                (true? (:same-invocation? accepted))
                                (= :p15-s23-native-plan-c-emitter-v1
                                   (:contract accepted))
                                (= :gravity-source (:implementation accepted))
                                (string? (:source accepted))
                                (re-matches #"sha256:[0-9a-f]{64}"
                                            (:source-sha256 accepted))
                                (= "Hello Gravity\n" (:expected-output accepted))
                                (true? (:expected-source-fragment? accepted)))
                   (fail! "persisted compiler plan did not emit the accepted C artifact"
                          {:reason :accepted-execution :accepted accepted}))
               _ (when-not (and (= :rejected (:status rejected))
                                (= "P15GCE002" (:diagnostic rejected)))
                   (fail! "persisted compiler plan did not preserve the rejected diagnostic"
                          {:reason :rejected-execution :rejected rejected}))
               source (:source accepted)
               source-bytes (.getBytes ^String source StandardCharsets/UTF_8)
               record
               {:artifact :gravity/bootstrap-seed-built-component
                :schema-version 1
                :status :complete-for-seed-built-plan
                :component component-id
                :source {:path (:source-path component)
                         :sha256 (:source-sha256 component)}
                :manifest-sha256 (:manifest-sha256 loaded)
                :source-graph-sha256 (get-in loaded [:source-graph :sha256])
                :source-revision (:revision materialized)
                :source-archive-sha256 (:archive-sha256 materialized)
                :toolchain toolchain
                :compiler-child {:command (:command compile-run)
                                 :exit-code (:exit-code compile-run)
                                 :stdout-sha256 (:stdout-sha256 compile-run)
                                 :stderr-sha256 (:stderr-sha256 compile-run)
                                 :source-content-sha256
                                 (get-in compile-run [:summary :source-content-hash])
                                 :plan-id (:plan-id plan)
                                 :plan-sha256 plan-sha256}
                :execution-child {:command (:command execute-run)
                                  :exit-code (:exit-code execute-run)
                                  :stdout-sha256 (:stdout-sha256 execute-run)
                                  :stderr-sha256 (:stderr-sha256 execute-run)
                                  :plan-id (:plan-id observation)
                                  :plan-sha256 (:plan-sha256 observation)
                                  :observation-sha256 (sha256-id observation-bytes)
                                  :reloaded-plan? true}
                :inputs {:fixtures fixture-records
                         :accepted (:path accepted-fixture)
                         :rejected (:path rejected-fixture)}
                :plan {:path (:plan-output-path component)
                       :kind (:kind plan)
                       :id (:plan-id plan)
                       :source-sha256 (get-in plan [:source :sha256])
                       :sha256 plan-sha256}
                :generated-c {:path (:source-output-path component)
                              :dialect :c11
                              :sha256 (sha256-id source-bytes)
                              :bytes (alength source-bytes)
                              :expected-output (:expected-output accepted)}
                :diagnostics {:rejected-fixture (:path rejected-fixture)
                              :code (:diagnostic rejected)}
                :boundary {:build-language :clojure
                           :source-language :gravity
                           :seed-hosted? true
                           :component-scope :native-plan-emitter
                           :input-profile :hosted
                           :input-target :jvm
                           :output-target :c11
                           :supported-source-subset
                           [:ns-hosted-jvm :defn-main :println-string]
                           :output-conformance {:c11-source :generated-only
                                                :hosted-stdout :observed
                                                :native-c11-execution :unrun}
                           :whole-compiler-rebuild? false
                           :self-hosted? false
                           :seed-retired? false}}
               _ (write-new! plan-destination plan-bytes)
               _ (write-new! source-destination source)
               _ (write-new! record-destination record)
               public-plan-bytes (Files/readAllBytes (.toPath plan-destination))
               public-source-bytes (Files/readAllBytes (.toPath source-destination))
               public-record-bytes (Files/readAllBytes (.toPath record-destination))
               public-plan (read-single-edn public-plan-bytes
                                            "persisted compiler plan readback")
               public-record (read-single-edn public-record-bytes
                                              "persisted component record")
               public-plan-sha256 (sha256-id public-plan-bytes)
               public-source-sha256 (sha256-id public-source-bytes)
               public-record-sha256 (sha256-id public-record-bytes)
               _ (when-not (valid-plan? public-plan)
                   (fail! "persisted plan readback has the wrong executable shape"
                          {:reason :plan-readback-shape
                           :plan-kind (:kind public-plan)
                           :entrypoint (:entrypoint public-plan)}))
               _ (when-not (= (:plan-id plan) (:plan-id public-plan))
                   (fail! "persisted plan readback identity differs"
                          {:reason :plan-readback-identity
                           :expected (:plan-id plan)
                           :actual (:plan-id public-plan)}))
               _ (when-not (= source-sha256 (get-in public-plan [:source :sha256]))
                   (fail! "persisted plan readback source identity differs"
                          {:reason :plan-readback-source-binding
                           :expected source-sha256
                           :actual (get-in public-plan [:source :sha256])}))
               _ (when-not (= plan-sha256 public-plan-sha256)
                   (fail! "persisted plan readback hash differs"
                          {:reason :plan-readback
                           :expected plan-sha256
                           :actual public-plan-sha256}))
               _ (when-not (= (get-in record [:generated-c :sha256])
                              public-source-sha256)
                   (fail! "persisted generated source readback hash differs"
                          {:reason :source-readback
                           :expected (get-in record [:generated-c :sha256])
                           :actual public-source-sha256}))
               _ (when-not (= record public-record)
                   (fail! "persisted component record readback differs"
                          {:reason :record-readback}))]
           {:record record
            :plan-path (.getPath plan-destination)
            :source-path (.getPath source-destination)
            :record-path (.getPath record-destination)
            :output-sha256 {:plan public-plan-sha256
                            :source public-source-sha256
                            :record public-record-sha256}})
         (catch Throwable error
           (doseq [^File file output-files]
             (Files/deleteIfExists (.toPath file)))
           (throw error))
         (finally
           (when-let [materialized @snapshot]
             (delete-tree! (:root materialized))
             (Files/deleteIfExists (:archive materialized)))))))))
