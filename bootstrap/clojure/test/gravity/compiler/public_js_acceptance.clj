(ns gravity.compiler.public-js-acceptance
  "Public CLI process evidence for the hosted scalar JavaScript compiler.
   Every compilation below starts the real launcher in a new process."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [is testing]]
            [gravity.bootstrap-free-leaf-test-runner :as json])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files Path]
           [java.nio.file.attribute FileAttribute]
           [java.security MessageDigest]
           [java.util UUID]
           [java.util.concurrent TimeUnit]
           [java.util.jar JarEntry JarOutputStream]))

(def ^:private artifact-names
  #{"program.mjs" "program.d.ts" "program.mjs.map" "package.json"
    "manifest.edn" "provenance.edn"})

(defn- root [] (.getCanonicalFile (io/file ".")))
(defn- absolute [path] (.getCanonicalPath (io/file (str path))))
(defn- resolve-file [directory path]
  (let [file (io/file path)]
    (if (.isAbsolute file) file (io/file (str directory) path))))
(defn- content-id? [value]
  (and (string? value) (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- sha256 [file]
  (str "sha256:"
       (apply str
              (map #(format "%02x" (bit-and 255 %))
                   (.digest (MessageDigest/getInstance "SHA-256")
                            (Files/readAllBytes (.toPath (io/file file))))))))

(defn- delete-tree! [^Path directory]
  ;; Delete only this fixture tree, without following any symbolic links.
  (when (Files/exists directory (make-array java.nio.file.LinkOption 0))
    (with-open [entries (Files/walk directory (make-array java.nio.file.FileVisitOption 0))]
      (doseq [entry (reverse (iterator-seq (.iterator entries)))]
        (Files/deleteIfExists entry)))))

(defn- write-source! [directory name text]
  (let [file (io/file (str directory) name)]
    (io/make-parents file)
    (spit file text :encoding "UTF-8")
    (.getCanonicalPath file)))

(defn- executables [name]
  (distinct
    (keep (fn [directory]
            (let [file (io/file directory name)]
              (when (and (.isFile file) (.canExecute file))
                (.getCanonicalPath file))))
          (str/split (or (System/getenv "PATH") "")
                     (re-pattern java.io.File/pathSeparator)))))

(defn- executable [name]
  (or (first (executables name))
      (throw (ex-info (str "Required acceptance tool is unavailable: " name)
                      {:tool name}))))

(defn- without-java-banner [text]
  (let [options (System/getenv "JAVA_TOOL_OPTIONS")
        banner (when (seq options) (str "Picked up JAVA_TOOL_OPTIONS: " options "\n"))]
    (loop [text text]
      (if (and banner (str/starts-with? text banner))
        (recur (subs text (count banner)))
        text))))

(defn- process! [directory command & [environment clear-environment?]]
  (let [stdout (java.io.File/createTempFile "uniform-public-js-out-" ".txt")
        stderr (java.io.File/createTempFile "uniform-public-js-err-" ".txt")
        builder (doto (ProcessBuilder. ^java.util.List (mapv str command))
                  (.directory (io/file (str directory)))
                  (.redirectOutput stdout)
                  (.redirectError stderr))
        env (.environment builder)]
    (when clear-environment? (.clear env))
    (doseq [[key value] environment]
      (if (nil? value) (.remove env key) (.put env key value)))
    (try
      (let [started (System/nanoTime)
            process (.start builder)
            finished? (.waitFor process 900 TimeUnit/SECONDS)]
        (when-not finished?
          (.destroyForcibly process)
          (.waitFor process))
        {:exit (if finished? (.exitValue process) 124)
         :out (slurp stdout)
         :err (without-java-banner (slurp stderr))
         :elapsed-ms (quot (- (System/nanoTime) started) 1000000)})
      (finally (.delete stdout) (.delete stderr)))))

(defn- parallel-pairs! [operation inputs]
  ;; Fresh compiler processes are independent, but each needs its own heap.
  ;; Wait for both outcomes before propagating an exception or removing files.
  (reduce
    (fn [results pair]
      (let [pending (mapv (fn [input]
                            (future
                              (try {:value (operation input)}
                                   (catch Throwable error {:error error})))) pair)
            completed (mapv deref pending)]
        (when-let [error (some :error completed)] (throw error))
        (into results (map :value completed))))
    [] (partition-all 2 inputs)))

(defn- edn-output [text]
  (when-not (str/blank? text)
    (try (edn/read-string text)
         (catch Exception _ nil))))

(defn- diagnostic-source [data]
  (or (get-in data [:source-span :source])
      (get-in data [:source-span :file])
      (get-in data [:primary :span :source])
      (:actual-source-path data) (:source-path data)))

(defn- diagnostic-span [data]
  (or (:source-span data) (get-in data [:primary :span])))

(defn- diagnostic-facts [data]
  (select-keys (merge data (:facts data))
               [:missing-fact :function :expected-type :actual-type
                :expected-arity :actual-arity :declared-effects :inferred-effects]))

(defn- compile! [launcher directory arguments & [environment]]
  (let [command (into launcher arguments)
        result (process! directory command environment)
        data (edn-output (if (zero? (:exit result)) (:out result) (:err result)))]
    (locking *out*
      (println "public-js-process"
               (pr-str {:arguments arguments :exit (:exit result)
                        :elapsed-ms (:elapsed-ms result) :diagnostic (:id data)
                        :diagnostic-source (diagnostic-source data)
                        :source-position (select-keys (:start (diagnostic-span data))
                                                      [:line :column :column-unit :byte])
                        :facts (diagnostic-facts data)
                        :reader-reason (get-in data [:reader-state :reason])}))
      (flush))
    (assoc result :data data)))

(defn- assert-rejected! [result expected-id source output & [missing-fact]]
  (is (= 1 (:exit result)) (pr-str result))
  (is (str/blank? (:out result)) (:out result))
  (is (= expected-id (get-in result [:data :id])) (pr-str result))
  (when source
    (is (= (absolute source) (diagnostic-source (:data result))) (pr-str (:data result))))
  (when missing-fact
    (is (= missing-fact (get-in result [:data :missing-fact])) (pr-str (:data result))))
  (when output
    (is (not (.exists (io/file output))) "Rejected compilation cannot publish a target directory")))

(defn- assert-diagnostic-detail! [result {:keys [position facts reader-reason]}]
  (let [data (:data result)
        start (:start (diagnostic-span data))]
    (when position
      (is (= position [(:line start) (:column start)]) (pr-str data))
      (is (= :unicode-scalar (:column-unit start)) (pr-str data)))
    (doseq [[key expected] facts]
      (is (= expected (get (diagnostic-facts data) key)) (str key " " (pr-str data))))
    (when reader-reason
      (is (= reader-reason (get-in data [:reader-state :reason])) (pr-str data)))))

(defn- assert-artifacts! [directory output source requested-target effects result]
  (let [output-file (resolve-file directory output)
        manifest (edn/read-string (slurp (io/file output-file "manifest.edn")))
        provenance (edn/read-string (slurp (io/file output-file "provenance.edn")))]
    (is (= 0 (:exit result)) (pr-str result))
    (is (str/blank? (:err result)) (:err result))
    (is (= :emitted (get-in result [:data :status])))
    (is (= requested-target (get-in result [:data :requested-target])))
    (is (= :js-ts (get-in result [:data :canonical-target])
           (get-in result [:data :source-target])))
    (is (= :importable-javascript-module
           (get-in result [:data :command-boundary :artifact-kind])))
    (is (= :node-esm-import-and-call-exports
           (get-in result [:data :command-boundary :execution])))
    (is (= artifact-names (set (map #(.getName %) (.listFiles output-file)))))
    (is (= :js-ts (:source-target manifest) (:source-target provenance)
           (get-in manifest [:target :target]) (:canonical-target provenance)))
    (is (= requested-target (:requested-target provenance)))
    (is (= effects (get-in provenance [:source-policy-receipt :policy :effects])))
    (is (= #{} (get-in provenance [:source-policy-receipt :policy :capabilities])))
    (is (= :jvm (get-in manifest [:compiler-host :target])))
    (is (= :clojure-stage0-seed (get-in manifest [:compiler-host :implementation])))
    (is (false? (get-in manifest [:compiler-host :self-hosted?])))
    (is (= :syntax-only (get-in provenance [:runtime-observation :validation])))
    (is (= {:node-validation :syntax-check
            :export-execution :not-performed
            :clean-checkout-acceptance :separate-development-evidence}
           (:validation-scope manifest) (:validation-scope provenance)))
    (doseq [record [manifest provenance]]
      (is (not-any? #{:public-compile-cli :clean-checkout-acceptance} (:nonclaims record)))
      (is (every? (set (:nonclaims record)) [:full-language :release :self-hosting :seed-retirement])))
    (is (= (absolute source) (get-in manifest [:source :path])))
    (is (= (sha256 source) (get-in manifest [:source :content-hash])))
    (doseq [[kind record] (:artifacts manifest)]
      (let [file (io/file output-file (:file record))]
        (is (.isFile file) (str kind))
        (is (= (:sha256 record) (sha256 file)))
        (is (= (:byte-count record) (.length file)))))
    (is (= artifact-names
           (set (map #(.getName (io/file %))
                     (vals (get-in result [:data :paths]))))))
    (is (= (set (keys (get-in result [:data :paths])))
           (set (keys (get-in result [:data :artifact-inventory])))))
    (doseq [[kind record] (get-in result [:data :artifact-inventory])]
      (let [expected (io/file output-file (:file record))
            reported (get-in result [:data :paths kind])
            actual (resolve-file directory reported)]
        (is (= (absolute expected) (absolute actual)))
        (is (.isFile actual) (str kind " reported path"))
        (is (= (:sha256 record) (sha256 actual)))
        (is (= (:byte-count record) (.length actual)))))
    (is (= (get-in result [:data :paths :javascript])
           (get-in result [:data :command-boundary :module-path])))
    {:manifest manifest :provenance provenance :directory output-file}))

(def ^:private success-script
  (str "const inputs=[[true,3n,7n],[false,7n,3n],"
       "[true,9007199254740993n,9007199254740994n],"
       "[true,-10n,25n],[false,100n,-50n],"
       "[true,9223372036854775807n,0n],[true,-9223372036854775808n,0n]];"
       "const results=inputs.map(args=>m.score(...args));"
       "console.log(JSON.stringify({exports:Object.keys(m),"
       "results:results.map(value=>value.toString()),"
       "resultTypes:results.map(value=>typeof value)}));"))

(def ^:private errors-script
  (str "const cases=[[false,9223372036854775807n,1n],"
       "[true,-9223372036854775808n,-1n],[null,3n],[true,3n,7n,1n],"
       "[true,3,7n],[true,null,7n],[true,undefined,7n],"
       "[1,3n,7n],[null,3n,7n],[undefined,3n,7n],"
       "[true,9223372036854775808n,0n],[true,-9223372036854775809n,0n]];"
       "console.log(JSON.stringify(cases.map(args=>{try {return {returned:m.score(...args).toString()};}"
       "catch(e){return {isError:e instanceof Error,name:e.name,type:e.type,kind:e.kind,"
       "numericMode:e.numericMode,width:e.width,signed:e.signed,operation:e.operation,"
       "checkId:e.checkId,operationId:e.operationId,"
       "operands:Array.isArray(e.operands)?e.operands.map(x=>x.toString()):null,"
       "operandTypes:Array.isArray(e.operands)?e.operands.map(x=>typeof x):null,"
       "function:e.function,argumentIndex:e.argumentIndex,expected:e.expected,"
       "actualType:e.actualType,actualArity:e.actualArity,source:e.source};}})));"))

(defn- node-report! [directory script]
  (let [node (executable "node")
        result (process! directory
                         [node "--input-type=module" "--eval"
                          (str "const m=await import('./program.mjs');" script)]
                         {"PATH" (.getParent (io/file node))} true)]
    (is (= 0 (:exit result)) (pr-str result))
    (is (str/blank? (:err result)) (:err result))
    (when (zero? (:exit result)) (json/read-strict-component-contract (:out result)))))

(defn- assert-scalar-results! [directory]
  (let [success (node-report! directory success-script)]
    (is (= ["score"] (get success "exports")))
    (is (= ["10" "7" "18014398509481987" "15" "100"
            "9223372036854775807" "-9223372036854775808"]
           (get success "results")))
    (is (= (vec (repeat 7 "bigint")) (get success "resultTypes")))
    success))

(defn run-smoke! []
  (let [output (str "target/development-loop-batches/public-js-smoke-" (UUID/randomUUID))
        directory (.toPath (io/file (root) output))
        source (absolute "examples/checked-js/acceptance.gravity")]
    (try
      (let [result (compile! [(absolute "bin/gravity")] (root)
                             ["compile" source "--target" "js" "-o" output])]
        (is (= 0 (:exit result)) (pr-str result))
        (is (= :emitted (get-in result [:data :status])))
        (when (zero? (:exit result))
          (assert-scalar-results! (.toFile directory))))
      (finally (delete-tree! directory)))))

(defn- assert-runtime! [directory source]
  (let [success (assert-scalar-results! directory)
        errors (node-report! directory errors-script)
        manifest (edn/read-string (slurp (io/file directory "manifest.edn")))
        checks (get-in manifest [:evidence :runtime-check-table])
        source-map (json/read-strict-component-contract
                    (slurp (io/file directory "program.mjs.map")))
        program (slurp (io/file directory "program.mjs"))
        addition-ids (set (map #(get % "mirInstructionId")
                             (filter #(= 10 (get % "sourceLine"))
                                     (get-in source-map ["x_gravity" "generatedOrigins"]))))]
    (is (= [":overflow" ":overflow" ":arity" ":arity" ":type" ":type"
            ":type" ":type" ":type" ":type" ":range" ":range"]
           (mapv #(get % "kind") errors)))
    (doseq [error errors]
      (is (= true (get error "isError")))
      (is (not (contains? error "returned")))
      (is (= (absolute source) (get-in error ["source" "path"])))
      (is (= (sha256 source) (get-in error ["source" "contentHash"])))
      (is (content-id? (get-in error ["source" "coreNodeId"]))))
    (doseq [[error operands] (map vector (take 2 errors)
                                 [["9223372036854775807" "1"]
                                  ["-9223372036854775808" "-1"]])]
      (is (= "UniformNumericError" (get error "name")))
      (is (= ":error/numeric" (get error "type")))
      (is (= ":checked" (get error "numericMode")))
      (is (= 64 (get error "width")))
      (is (= true (get error "signed")))
      (is (= ":add" (get error "operation")))
      (is (= operands (get error "operands")))
      (is (= ["bigint" "bigint"] (get error "operandTypes")))
      (is (content-id? (get error "checkId")))
      (is (str/includes? program (get error "checkId")))
      (is (= (get error "checkId") (:check-id (get checks (get error "checkId")))))
      (is (= (get error "operationId")
             (:operation-id (get checks (get error "checkId")))
             (:guarded-operation-id (get checks (get error "checkId")))))
      (is (contains? addition-ids (get error "operationId")))
      (is (= (get error "operationId") (get-in error ["source" "mirInstructionId"])
             (get-in error ["source" "coreNodeId"])))
      (is (= 10 (get-in error ["source" "span" "start" "line"]))))
    (is (= (get-in errors [0 "checkId"]) (get-in errors [1 "checkId"])))
    (doseq [[index error] (map-indexed vector (drop 2 errors))]
      (is (= "UniformABIError" (get error "name")))
      (is (= ":error/abi" (get error "type")))
      (is (= "score" (get error "function")))
      (is (= (nth [nil nil 1 1 1 0 0 0 1 1] index) (get error "argumentIndex")))
      (is (= (nth [3 3 "bigint" "bigint" "bigint" "boolean" "boolean" "boolean"
                   "signed I64" "signed I64"] index) (get error "expected")))
      (is (= (nth [nil nil "number" "null" "undefined" "number" "null" "undefined"
                   "bigint" "bigint"] index) (get error "actualType")))
      (when (< index 2) (is (= (nth [2 4] index) (get error "actualArity")))))
    {:success success :errors errors}))

(defn- copy-source-checkout! [^Path directory]
  ;; Copy the current bytes of tracked source, including uncommitted edits.
  ;; Current-source owner binding rejects symlink components. No target tree,
  ;; generated artifact, dependency cache, or stale packaged JAR is borrowed.
  (let [tracked (process! (root) [(executable "git") "ls-files" "-z"])]
    (when-not (zero? (:exit tracked))
      (throw (ex-info "Could not enumerate tracked source for the isolated launcher fixture" tracked)))
    (doseq [relative (str/split (:out tracked) #"\u0000")
            :when (and (not (str/blank? relative))
                       (not (str/starts-with? relative "target/")))]
      (let [source (.toPath (io/file (root) relative))
            destination (.resolve directory relative)]
        (Files/createDirectories (.getParent destination) (make-array FileAttribute 0))
        (Files/copy source destination (make-array java.nio.file.CopyOption 0))))))

(defn- with-stale-jar-root [operation]
  (let [directory (Files/createTempDirectory "uniform-public-stale-jar-"
                                             (make-array FileAttribute 0))
        wrapper (.resolve directory "bin/gravity")
        jar (.resolve directory "target/phase-18/jvm-cli/gravity-jvm-cli.jar")]
    (try
      (copy-source-checkout! directory)
      (.setExecutable (.toFile wrapper) true)
      (Files/createDirectories (.getParent jar) (make-array FileAttribute 0))
      ;; This real archive has no current CLI entrypoint. Selecting it instead
      ;; of the current-source target route must fail the subprocess test.
      (with-open [archive (JarOutputStream. (io/output-stream (.toFile jar)))]
        (.putNextEntry archive (JarEntry. "obsolete-cli.txt"))
        (.write archive (.getBytes "obsolete packaged CLI fixture\n" StandardCharsets/UTF_8))
        (.closeEntry archive))
      (operation directory wrapper jar)
      (finally (delete-tree! directory)))))

(defn run-acceptance! []
  (let [relative-root (str "target/development-loop-batches/public-js-" (UUID/randomUUID))
        directory (.toPath (io/file (root) relative-root))
        source (absolute "examples/checked-js/acceptance.gravity")
        source-text (slurp source)
        wrapper [(absolute "bin/gravity")]
        current-source [(executable "clojure") "-J-Xmx2g" "-M:gravity"]
        output (str relative-root "/module-js")
        canonical-output (str relative-root "/module-js-ts")]
    (Files/createDirectories directory (make-array FileAttribute 0))
    (try
      (testing "ordinary wrapper and explicit source launcher compile both aliases"
        (let [[first-result second-result]
              (parallel-pairs!
                (fn [[launcher alias output]]
                  (compile! launcher (root) ["compile" source "--target" alias "-o" output]))
                [[wrapper "js" output] [current-source "js-ts" canonical-output]])]
          (is (= 0 (:exit first-result)) (pr-str first-result))
          (is (= 0 (:exit second-result)) (pr-str second-result))
          (when (zero? (:exit first-result))
            (let [artifact (assert-artifacts! (root) output source :js #{:error/raise} first-result)
                  observation (assert-runtime! (:directory artifact) source)]
              (when (zero? (:exit second-result))
                (let [canonical (assert-artifacts! (root) canonical-output source :js-ts #{:error/raise} second-result)]
                  (is (= (:success observation) (node-report! (:directory canonical) success-script)))
                  (is (= (slurp (io/file (:directory artifact) "program.mjs"))
                         (slurp (io/file (:directory canonical) "program.mjs"))))))
              (let [outside (Files/createTempDirectory "uniform-public-node-only-" (make-array FileAttribute 0))]
                (try
                  (doseq [name artifact-names]
                    (Files/copy (.toPath (io/file (:directory artifact) name)) (.resolve outside name)
                                (make-array java.nio.file.CopyOption 0)))
                  (is (= (:success observation) (node-report! outside success-script)))
                  (is (= (:errors observation) (node-report! outside errors-script)))
                  (finally (delete-tree! outside))))))))
      (testing "empty effects, renamed qst source, and a stale packaged JAR"
        (let [text (str "(ns renamed.public.logic\n  (:profile :hosted)\n  (:target :js-ts)\n"
                        "  (:effects #{})\n  (:capabilities #{})\n  (:safety :safe)\n  (:exports [decide]))\n"
                        "(defn invert [answer :- Bool] (if answer false true))\n"
                        "(defn decide [signal :- Bool]\n"
                        "  (let [opposite (invert signal)] (if signal opposite true)))\n")]
          (with-stale-jar-root
            (fn [isolated wrapper-path jar]
              (let [renamed (write-source! isolated "examples/renamed-logic.qst" text)
                    jar-hash (sha256 (.toFile jar))
                    output "target/renamed-boolean"
                    result (compile! [(str wrapper-path)] isolated
                                     ["compile" renamed "--target" "js" "-o" output]
                                     {"GRAVITY_BOOTSTRAP_ONLY" "0" "GRAVITY_PACKAGED_CLI_ONLY" "0"})]
                (is (= 0 (:exit result)) (pr-str result))
                (when (zero? (:exit result))
                  (let [artifact (assert-artifacts! isolated output renamed :js #{} result)]
                    (is (= {"exports" ["decide"] "values" [false true]}
                           (node-report! (:directory artifact)
                                         "console.log(JSON.stringify({exports:Object.keys(m),values:[m.decide(true),m.decide(false)]}));")))))
                (is (= jar-hash (sha256 (.toFile jar))) "The stale package fixture is untouched"))))))
      (testing "owned semantic failures survive the public process boundary"
        (parallel-pairs!
          (fn [[label text expected-id detail]]
            (let [path (write-source! directory (str label ".gravity") text)
                  rejected-output (str relative-root "/" label "-output")
                  result (compile! current-source (root)
                                   ["compile" path "--target" "js" "-o" rejected-output])]
              (assert-rejected! result expected-id path rejected-output)
              (assert-diagnostic-detail! result detail)))
          [["malformed" (str source-text "[") "C2-DELIMITER"
            {:position [17 2] :reader-reason :unexpected-end-of-source}]
           ["wrong-call-type" (str/replace source-text "(add left right)" "(add true right)") "C7-TYPE-MISMATCH"
            {:position [13 13]
             :facts {:missing-fact :scalar-call-argument-types :function 'add
                     :expected-type [:gravity/i64 :gravity/i64]
                     :actual-type [:gravity/bool :gravity/i64]}}]
           ["wrong-call-arity" (str/replace source-text "(add left right)" "(add left)") "L2-BUILTIN-ARITY"
            {:position [13 13]
             :facts {:missing-fact :fixed-arity-scalar-call :function 'add
                     :expected-arity 2 :actual-arity 1}}]
           ["unused-invalid" (str source-text "\n(defn unused [flag :- Bool] (+ flag flag))\n") "C7-TYPE-MISMATCH"
            {:position [18 29]
             :facts {:missing-fact :binary-i64-add-operands
                     :expected-type [:gravity/i64 :gravity/i64]
                     :actual-type [:gravity/bool :gravity/bool]}}]
           ["undeclared-error" (str/replace source-text "#{:error/raise}" "#{}") "C8-UNDECLARED"
            {:facts {:missing-fact :declared-numeric-error-effect
                     :declared-effects #{} :inferred-effects #{:error/raise}}}]
           ["unsupported-recursion" (str/replace source-text "(+ left right)" "(add left right)") "C6-LOWERING-GAP"]
           ["unsupported-form" (str/replace source-text "(+ left right)" "[left right]") "C6-LOWERING-GAP"]
           ["source-target-mismatch" (str/replace source-text "(:target :js-ts)" "(:target :jvm)") "C14-INPUT"]
           ["unsupported-source-profile" (str/replace source-text "(:profile :hosted)" "(:profile :core)")
            "B1-TARGET-UNSUPPORTED"
            {:facts {:missing-fact :hosted-safe-scalar-js-source-policy}}]
           ["unsupported-source-policy" (str/replace source-text "(:capabilities #{})" "(:capabilities #{:fs/read})") "B1-TARGET-UNSUPPORTED"]]))
      (testing "unsupported JavaScript lowering cannot select the native route or fall back"
        (parallel-pairs!
          (fn [[target lowering]]
            (let [rejected-output (str relative-root "/" target "-" lowering)
                  result (compile! current-source (root)
                                   ["compile" source "--target" target "--lowering" lowering
                                    "-o" rejected-output])]
              (assert-rejected! result "B6-TARGET" nil rejected-output :js-ts-lowering-mode)
              (is (= "--lowering" (get-in result [:data :option])))
              (is (= (keyword lowering) (get-in result [:data :lowering-mode])))
              (is (= [:runtime-derived] (get-in result [:data :supported-lowering-modes])))
              (is (empty? (select-keys (get-in result [:data :source-span])
                                      [:form-index :line :column :start :end :byte-start :byte-end]))
                  "Lowering option failures do not invent a source location")))
          [["js" "not-supported"] ["js" "verified-mir"] ["js-ts" "verified-mir"]]))
      (testing "command shape errors precede attempts to read nonexistent source"
        (let [missing-source (str directory "/never-created.gravity")
              rejected-output (str relative-root "/options-output")]
          (parallel-pairs!
            (fn [[arguments expected-option]]
              (let [result (compile! current-source (root) arguments)]
                (assert-rejected! result "P18T04002" nil rejected-output)
                (when expected-option
                  (is (= expected-option
                         (select-keys (:data result) (keys expected-option)))
                      "CLI option failures identify the actual offending option"))
                (is (empty? (select-keys (get-in result [:data :source-span])
                                        [:form-index :line :column :start :end :byte-start :byte-end]))
                    "CLI shape errors do not invent a source location")))
            [[["compile"] nil]
             [["compile" missing-source "--target"] {:option "--target"}]
             [["compile" missing-source "--target" "js" "-o"] {:option "-o"}]
             [["compile" missing-source "--target" "js" "--lowering"] {:option "--lowering"}]
             [["compile" missing-source "--target" "js" "--target" "js-ts" "-o" rejected-output]
              {:option "--target"}]
             [["compile" missing-source "--target" "js" "-o" rejected-output "--output" rejected-output]
              {:option "--output"}]
             [["compile" missing-source "--target" "js" "--lowering" "runtime-derived" "--lowering" "runtime-derived" "-o" rejected-output]
              {:option "--lowering"}]
             [["compile" missing-source "--target" "js" "--unknown" "value" "-o" rejected-output]
              {:unsupported-option "--unknown"}]])))
      (testing "output destination and Node availability failures publish nothing"
        (parallel-pairs!
         (fn [check] (check))
         [(fn []
            (let [existing (str relative-root "/existing")
                  marker (write-source! existing "keep.txt" "caller-owned artifact\n")
                  result (compile! current-source (root) ["compile" source "--target" "js" "-o" existing])]
              (assert-rejected! result "C14-INPUT" source nil :fresh-output-path)
              (is (= "caller-owned artifact\n" (slurp marker)))
              (is (= #{"keep.txt"} (set (map #(.getName %) (.listFiles (io/file existing))))))))
          (fn []
            (let [parent (write-source! directory "not-a-directory" "retained\n")
                  invalid-output (str relative-root "/not-a-directory/child")
                  result (compile! current-source (root) ["compile" source "--target" "js" "-o" invalid-output])]
              (assert-rejected! result "C14-INPUT" source invalid-output :output-parent-directory)
              (is (= "retained\n" (slurp parent)))))
          (fn []
            (let [missing-output (compile! current-source (root) ["compile" source "--target" "js"])]
              (assert-rejected! missing-output "P18T04002" nil nil)
              (is (= "-o" (get-in missing-output [:data :option])))))
          (fn []
            (let [result (compile! current-source (root) ["compile" source "--target" "js" "-o" "../outside-output"])]
              (assert-rejected! result "P18T04002" nil nil)
              (is (= "-o" (get-in result [:data :option])))))
          (fn []
            (let [no-node-output (str relative-root "/without-node")
                  launcher [(absolute (io/file (System/getProperty "java.home") "bin/java"))
                            "-Xmx2g" "-cp" (System/getProperty "java.class.path")
                            "clojure.main" "-m" "gravity.cli.launcher"]
                  result (compile! launcher (root)
                                   ["compile" source "--target" "js" "-o" no-node-output]
                                   {"PATH" ""})]
              (assert-rejected! result "B6-TARGET" source no-node-output :node20-runtime)))
          ;; A second installed Node is optional, while the absence case above
          ;; always runs. Never fabricate a version response as runtime evidence.
          (fn []
            (if-let [[node version]
                     (some (fn [node]
                             (let [result (process! (root) [node "--version"])
                                   version (str/trim (:out result))]
                               (when (and (zero? (:exit result))
                                          (re-matches #"v[0-9]+(?:\..*)?" version)
                                          (not (re-matches #"v20(?:\..*)?" version)))
                                 [node version])))
                           (executables "node"))]
              (let [rejected-output (str relative-root "/unsupported-node")
                    result (compile! current-source (root)
                                     ["compile" source "--target" "js" "-o" rejected-output]
                                     {"PATH" (str (.getParent (io/file node))
                                                  java.io.File/pathSeparator (System/getenv "PATH"))})]
                (println "public-js-unsupported-node" (pr-str {:path node :version version}))
                (assert-rejected! result "B6-TARGET" source rejected-output :node20-runtime)
                (is (= version (get-in result [:data :observed-node-version])))
                (is (= "20.x" (get-in result [:data :required-node-version]))))
              (println "public-js-unsupported-node" (pr-str {:observation :no-additional-node-version-on-path}))))]))
      (finally (delete-tree! directory)))))
