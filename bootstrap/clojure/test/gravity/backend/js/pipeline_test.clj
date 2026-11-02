(ns gravity.backend.js.pipeline-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.backend.js :as js]
            [gravity.bootstrap :as bootstrap]
            [gravity.bootstrap-free-leaf-test-runner :as json]))

(defn- native [name & arguments]
  (apply (ns-resolve 'gravity.bootstrap (symbol (str "js-ts-backend-" name))) arguments))

(defn- literal [value] {:op :literal :value value})
(defn- print-values [& arguments] {:op :println :args (vec arguments)})
(defn- plan [instructions writes?]
  {:entrypoint 'main :functions {'main {:instructions instructions}}
   :instruction-summary {:println (if writes? 1 0)}})

(defn- fixtures []
  {"scalars" (plan [(print-values (literal "a\u0000\u03bb") (literal nil)
                                 (literal false) (literal true) (literal Long/MIN_VALUE)
                                 (literal Long/MAX_VALUE) {:op :quote :value "quoted"})] true)
   "control" (plan [{:op :do :body [(literal 1) (print-values)]}
                     {:op :let :bindings [{:name 'x :expr (literal "local")}
                                          {:name 'f :expr (literal false)}]
                      :body [{:op :local :name 'x}
                             {:op :if :test {:op :local :name 'f}
                              :then (print-values (literal "wrong"))
                              :else (print-values {:op :local :name 'x})}
                             (print-values {:op :if :test (literal 0)
                                            :then (literal "truthy") :else (literal "wrong")}
                                           {:op :let :bindings [{:name 'v :expr (literal "inner")}]
                                            :body [{:op :local :name 'v}]}
                                           {:op :builtin-call :function 'str
                                            :args [(literal "a") (literal "b")]})]}] true)
   "pure" (plan [(literal 7)] false)})

(defn- rejected [source input]
  (try (native "validate-plan!" source input) :unexpectedly-accepted
       (catch clojure.lang.ExceptionInfo ex (ex-data ex))))

(defn- capture-native []
  {:fixtures (into (sorted-map)
                   (for [[label input] (fixtures)
                         :let [javascript (native "source" input)
                               writes? (pos? (get-in input [:instruction-summary :println]))]]
                     [label {:input input :javascript javascript
                             :declaration bootstrap/js-ts-backend-declaration-source
                             :source-map (native "source-map-source" javascript "fixture\u0000\u03bb\n\"\\")
                             :package (native "package-source" writes?)
                             :paths (native "output-paths" "out/js")
                             :validation (native "validate-plan!" "js-source.gravity" input)}]))
   :refusals [(rejected "js-source.gravity" nil)
              (rejected nil (plan [] false))
              (rejected 7 (plan [] false))]})

(deftest real-esm-declaration-map-package-and-shape-rejection
  (let [{:keys [fixtures refusals]} (capture-native)]
    (doseq [[label {:keys [input javascript declaration source-map package paths validation]}] fixtures
            :let [map-data (json/read-strict-component-contract source-map)
                  package-data (json/read-strict-component-contract package)]]
      (is (= :passed validation) label)
      (is (str/starts-with? javascript "#!/usr/bin/env node\n"))
      (is (str/ends-with? javascript "//# sourceMappingURL=program.mjs.map\n"))
      (is (= ["fixture\u0000\u03bb\n\"\\"] (get map-data "sourcesContent")))
      (is (= false (get-in map-data ["x_gravity" "perFormOriginPreserved"])))
      (is (= "source-unit-only" (get-in map-data ["x_gravity" "coverage"])))
      (is (= "module" (get package-data "type")))
      (is (= (pos? (get-in input [:instruction-summary :println])) (get package-data "sideEffects")))
      (is (= "out/js/program.mjs" (:javascript paths)))
      (is (= "// Generated Gravity hosted entrypoint; no exported API in this slice.\nexport {};\n" declaration))
      (is (= javascript (native "source" input))))
    (is (str/includes? (get-in fixtures ["scalars" :javascript]) "97,0,206,187"))
    (is (str/includes? (get-in fixtures ["scalars" :javascript]) "import { stdout } from \"node:process\";"))
    (is (not (str/includes? (get-in fixtures ["pure" :javascript]) "node:process")))
    (is (every? #(= "C14-INPUT" (:id %)) refusals))
    (is (every? #(= :source-bound-stage2-plan (:missing-fact %)) refusals))
    (is (every? #(= :js-ts-backend-lowering (:stage %)) refusals))))

(deftest aliases-escaping-and-honest-emitter-boundary
  (is (= [:js-ts :js-ts :js-ts :js-ts :llvm nil]
         (mapv #(native "canonical-target" %) [:js :js-ts "JS" "JS-TS" :llvm nil])))
  (doseq [text ["" "\"\\\b\f\n\r\t\u0000\u001f" "\u03bb\ud83d\ude00"]]
    (is (= text (json/read-strict-component-contract (native "json-string" text)))))
  (is (= :passed (native "validate-plan!" "unknown.gravity" (plan [{:op :unsupported}] false))))
  (is (= "" (native "instruction-source" {:op :unsupported} (atom 0) 0)))
  (is (= "" (native "value-source" {:op :unsupported} (atom 0) 0 {}))))

(deftest public-contract-arities-and-ported-alias-values
  (let [expected {'canonical-target '([target]) 'validate-plan! '([source-path plan])
                  'bytes-source '([name bytes indent]) 'value-declaration '([instruction counter indent])
                  'write-source '([descriptor indent]) 'test-source '([instruction env])
                  'value-source '([instruction counter indent env])
                  'instruction-source '([instruction counter indent] [instruction counter indent env])
                  'source '([plan]) 'json-string '([value]) 'source-map-source '([javascript source-text])
                  'package-source '([writes-stdout?]) 'output-paths '([output-path])}
        contract (:gravity/namespace-contract (meta #'js/source))]
    (doseq [[name args] expected]
      (is (= args (:arglists (meta (ns-resolve 'gravity.bootstrap (symbol (str "js-ts-backend-" name)))))) name))
    (is (= (set (keys (ns-publics 'gravity.backend.js))) (set (keys (:public-api contract)))))
    (is (every? #(= :none (:admission %)) (vals (:public-api contract))))
    (is (false? (:authenticates-inputs? contract)))
    (is (false? (:issues-certificates? contract)))
    (is (= '[clojure.string] (get-in contract [:dependency-direction :requires])))
    (is (= '[gravity.bootstrap] (get-in contract [:dependency-direction :consumed-by])))
    (with-redefs [bootstrap/js-ts-backend-target-aliases #{:custom}
                  bootstrap/js-ts-backend-target :custom-target]
      (is (= :custom-target (native "canonical-target" "CUSTOM"))))))

(deftest recursive-ports-retain-callback-order-and-stage-interposition
  (let [input (plan [(print-values (literal "a") (literal "b"))] true)
        expected (native "source" input) events (atom [])
        bytes-var #'bootstrap/js-ts-backend-bytes-source write-var #'bootstrap/js-ts-backend-write-source
        original-bytes @bytes-var original-write @write-var]
    (with-redefs-fn {bytes-var (fn [name bytes indent]
                                (swap! events conj [:bytes name]) (original-bytes name bytes indent))
                    write-var (fn [descriptor indent]
                                (swap! events conj [:write (:name descriptor)]) (original-write descriptor indent))}
      #(is (= expected (native "source" input))))
    (is (= [[:bytes "gravityValue1"] [:write "gravityValue1"] [:bytes "gravitySpace2"]
            [:bytes "gravityValue3"] [:write "gravityValue3"] [:bytes "gravityNewline4"]] @events))
    (with-redefs [js/source (fn [instruction-source plan] [(var? instruction-source) plan])]
      (is (= [true input] (native "source" input))))))

(deftest source-map-hash-and-json-ports-retain-order
  (let [events (atom []) original-hash bootstrap/sha256-hex original-json bootstrap/js-ts-backend-json-string
        expected (native "source-map-source" "a\nb" "source")]
    (with-redefs [bootstrap/sha256-hex (fn [text] (swap! events conj [:hash text]) (original-hash text))
                  bootstrap/js-ts-backend-json-string (fn [text] (swap! events conj [:json text]) (original-json text))]
      (is (= expected (native "source-map-source" "a\nb" "source"))))
    (is (= [:hash :json :json :json] (mapv first @events)))
    (is (= ["source" "source" "AAAA;AAAA" (str "sha256:" (original-hash "source"))] (mapv second @events)))))

(defn- observe-toolchain [f]
  (try {:status :accepted :value (f)}
       (catch Exception ex
         {:status :rejected :class (.getName (class ex))
          :message (.getMessage ex) :data (ex-data ex)})))

(defn- file-bytes [file]
  (mapv #(bit-and (int %) 255)
        (java.nio.file.Files/readAllBytes (.toPath (io/file file)))))

(defn- toolchain-products [directory disposition extension]
  ;; Ordinary fixed-seed source and trusted output helpers only. This helper
  ;; creates no backend packet, authority token or conformance manifest.
  (let [source-path (.getCanonicalPath (io/file (str "bootstrap/clojure/fixtures/js-toolchain/declared-js" extension)))
        source-text (slurp source-path :encoding "UTF-8")
        source-intake (observe-toolchain
                       #(let [emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source-path :js-ts))]
                          (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source-path source-text)))
        fixture (get-in (capture-native) [:fixtures disposition])
        input (:input fixture)
        validation (:validation fixture)
        javascript (:javascript fixture)
        writes? (pos? (get-in input [:instruction-summary :println] 0))
        output (.getCanonicalPath (io/file directory "artifact"))
        paths (native "output-paths" output)
        contents (array-map :javascript javascript
                            :typescript-declarations bootstrap/js-ts-backend-declaration-source
                            :source-map (:source-map fixture)
                            :package-metadata (native "package-source" writes?))
        published (native "stage-files!" output paths contents source-path)
        _ (assert (instance? java.nio.file.Path published))
        _ (assert (= (.toPath (io/file output)) published))
        files (into (sorted-map)
                    (for [[kind _] contents :let [path (get paths kind)]]
                      [kind {:path path :bytes (file-bytes path)
                             :sha256 (str "sha256:" (bootstrap/sha256-bytes-hex
                                                       (java.nio.file.Files/readAllBytes (.toPath (io/file path)))))}]))
        process (native "run-node-process!" [(:javascript paths)] source-path "B6-TARGET" "JS test execution failed")
        syntax (native "run-node-process!" ["--check" (:javascript paths)] source-path "B6-TARGET" "JS syntax check failed")
        version-process (native "run-node-process!" ["--version"] source-path "B6-TARGET" "Node version probe failed")
        version (observe-toolchain #(native "node-version!" source-path))
        malformed (io/file directory "malformed.mjs")
        _ (spit malformed "export {" :encoding "UTF-8")
        syntax-refusal (observe-toolchain
                        #(native "run-node-process!" ["--check" (.getPath malformed)]
                                 source-path "B6-TARGET" "Node rejected malformed ESM"))
        nonzero (observe-toolchain
                 #(native "run-node-process!"
                          ["--eval" "process.stdout.write('before-refusal'); process.stderr.write('diagnostic'); process.exitCode=7"]
                          source-path "B6-TARGET" "Node process refused"))
        existing (observe-toolchain #(native "stage-files!" output paths contents source-path))
        escaped-output (.getCanonicalPath (io/file directory "escaped"))
        escaped-paths (assoc (native "output-paths" escaped-output)
                            :source-map (.getCanonicalPath (io/file directory "outside.map")))
        escaped (observe-toolchain #(native "stage-files!" escaped-output escaped-paths contents source-path))
        outside-output "/gravity-js-test-outside-declared-roots/artifact"
        outside (observe-toolchain #(native "stage-files!" outside-output
                                             (native "output-paths" outside-output) contents source-path))
        blocker (io/file directory "blocker")
        _ (spit blocker "file, not directory" :encoding "UTF-8")
        blocked-output (str (.getCanonicalPath blocker) "/artifact")
        blocked (observe-toolchain #(native "stage-files!" blocked-output
                                             (native "output-paths" blocked-output) contents source-path))
        scratch (io/file directory "cleanup/nested/file")
        _ (io/make-parents scratch)
        _ (spit scratch "owned temporary data" :encoding "UTF-8")
        cleanup (native "delete-tree!" (.toPath (io/file directory "cleanup")))
        fixture-id (str "sha256:" (bootstrap/sha256-hex
                                  (pr-str (bootstrap/c-backend-canonical-value input))))]
    (assert (not (.exists (io/file directory "cleanup"))))
    (assert (not (.exists (io/file escaped-output))))
    (assert (not (.exists (io/file outside-output))))
    (doseq [[kind {:keys [bytes]}] files]
      (assert (= bytes (file-bytes (get paths kind)))))
    {:boundary :explicit-native-plan-fixture-and-trusted-internal-esm-output
     :source {:path source-path :text source-text :sha256 (bootstrap/sha256-hex source-text)}
     :source-intake-refusal source-intake
     :fixture fixture :input input :fixture-data-digest fixture-id
     :compiled-source-provenance? false :shape-validation validation
     :paths paths :contents contents :publication-result-observation {:class (.getName (class published)) :path (str published)} :files files
     :executable? (.canExecute (io/file (:javascript paths)))
     :source-map (json/read-strict-component-contract (:source-map contents))
     :package (json/read-strict-component-contract (:package-metadata contents))
     :execution process :syntax syntax :version-process version-process :runtime-eligibility version
     :malformed-esm {:path (.getPath malformed) :text (slurp malformed :encoding "UTF-8")}
     :refusals {:syntax syntax-refusal :nonzero nonzero :existing existing
                :escaped escaped :outside outside :blocked-parent blocked}
     :cleanup-result cleanup :cleanup-path-absent? true
     :complete-manifest-or-node20-authority? false}))

(deftest native-fixture-esm-files-observed-node-and-real-refusals
  (let [directory (java.nio.file.Files/createTempDirectory
                    "gravity-js-toolchain-test-" (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (doseq [disposition ["scalars" "control" "pure"] extension [".gravity" ".qst"]
              :let [root (io/file (.toFile directory) (str disposition extension))
                    _ (.mkdirs root)
                    product (toolchain-products (.getPath root) disposition extension)
                    output (String. (byte-array (map unchecked-byte (get-in product [:execution :stdout-bytes])))
                                    java.nio.charset.StandardCharsets/UTF_8)
                    observed (str/trim (String. (byte-array (map unchecked-byte (get-in product [:version-process :stdout-bytes])))
                                               java.nio.charset.StandardCharsets/UTF_8))]]
        (is (= :passed (:shape-validation product)))
        (is (= :rejected (get-in product [:source-intake-refusal :status])))
        (is (= "B1-TARGET-UNSUPPORTED" (get-in product [:source-intake-refusal :data :id])))
        (is (= :js-ts (get-in product [:source-intake-refusal :data :target])))
        (is (= #{:jvm} (get-in product [:source-intake-refusal :data :supported])))
        (is (false? (:compiled-source-provenance? product)))
        (is (true? (:executable? product)))
        (is (= "fixture\u0000\u03bb\n\"\\" (first (get-in product [:source-map "sourcesContent"]))))
        (is (= (= disposition "scalars") (str/includes? (get-in product [:contents :javascript]) "97,0,206,187")))
        (is (= (not= disposition "pure") (get-in product [:package "sideEffects"])))
        (is (= (case disposition
                 "scalars" "a\u0000\u03bb  false true -9223372036854775808 9223372036854775807 quoted\n"
                 "control" "\nlocal\ntruthy inner ab\n"
                 "pure" "") output))
        (if (re-matches #"v20(?:\..*)?" observed)
          (is (= {:status :accepted :value observed} (:runtime-eligibility product)))
          (do (is (= :rejected (get-in product [:runtime-eligibility :status])))
              (is (= observed (get-in product [:runtime-eligibility :data :observed-node-version])))))
        (is (= 0 (get-in product [:execution :exit]) (get-in product [:syntax :exit])))
        (doseq [kind [:syntax :nonzero]]
          (is (= "B6-TARGET" (get-in product [:refusals kind :data :id])))
          (is (= :node20-esm-acceptance (get-in product [:refusals kind :data :missing-fact]))))
        (is (= 7 (get-in product [:refusals :nonzero :data :node-result :exit])))
        (is (= (mapv int "before-refusal") (get-in product [:refusals :nonzero :data :node-result :stdout-bytes])))
        (is (= "diagnostic" (get-in product [:refusals :nonzero :data :node-result :stderr])))
        (doseq [[kind fact] [[:existing :fresh-output-path] [:escaped :artifact-directory-containment]
                            [:outside :output-path-containment] [:blocked-parent :output-parent-directory]]]
          (is (= "C14-INPUT" (get-in product [:refusals kind :data :id])))
          (is (= fact (get-in product [:refusals kind :data :missing-fact])))))
      (finally (native "delete-tree!" directory)))))

(deftest output-api-wrapper-ports-and-opaque-return-identity
  (doseq [[old owner arguments port-count]
          [['run-node-process! #'js/run-node-process! '[arguments source-path diagnostic-id message] 2]
           ['node-version! #'js/node-version! '[source-path] 2]
           ['delete-tree! #'js/delete-tree! '[path] 0]
           ['stage-files! #'js/stage-files! '[output-path paths contents source-path] 3]]]
    (let [compatibility (ns-resolve 'gravity.bootstrap (symbol (str "js-ts-backend-" old)))
          values (vec (repeat (count arguments) (Object.)))
          marker (Object.) calls (atom nil)]
      (is (= (list arguments) (:arglists (meta compatibility))) old)
      (with-redefs-fn {owner (fn [& args] (reset! calls args) marker)}
        #(is (identical? marker (apply compatibility values)) old))
      (is (every? var? (take port-count @calls)) old)
      (is (= values (vec (drop port-count @calls))) old)))
  (let [contract (:gravity/namespace-contract (meta #'js/stage-files!))]
    (is (= (set (keys (ns-publics 'gravity.backend.js))) (set (keys (:public-api contract)))))
    (doseq [[name operation] (ns-publics 'gravity.backend.js)
            :when (not= :constant (get-in contract [:public-api name :kind]))]
      (is (= (:arglists (meta operation)) (get-in contract [:public-api name :arglists])) name)
      (is (= contract (:gravity/namespace-contract (meta operation)))))))

(deftest containment-and-version-failure-ports-remain-late
  (let [fail-var #'bootstrap/js-ts-backend-fail!
        original (ex-info "late refusal" {:id :late})
        events (atom [])
        late-failure (fn [& args] (swap! events conj [:failure (vec args)]) (throw original))]
    (with-redefs-fn
      {#'bootstrap/c-backend-output-path-allowed?
       (fn [path] (swap! events conj [:guard path])
         (alter-var-root fail-var (constantly late-failure)) false)
       fail-var (fn [& _] (throw (Exception. "eager failure selected")))}
      #(is (identical? original
             (try (native "stage-files!" "guard-only" nil nil "source.gravity")
                  nil (catch clojure.lang.ExceptionInfo ex ex)))))
    (is (= [:guard :failure] (mapv first @events)))
    (is (= :output-path-containment (get-in @events [1 1 4 :missing-fact])))
    (reset! events [])
    (with-redefs-fn
      {#'bootstrap/js-ts-backend-run-node-process!
       (fn [& args] (swap! events conj [:version-probe (vec args)])
         (alter-var-root fail-var (constantly late-failure))
         {:exit 0 :stdout-bytes (mapv int "v24.0.0\n") :stderr ""})
       fail-var (fn [& _] (throw (Exception. "eager failure selected")))}
      #(is (identical? original
             (try (native "node-version!" "source.gravity")
                  nil (catch clojure.lang.ExceptionInfo ex ex)))))
    (is (= [:version-probe :failure] (mapv first @events)))
    (is (= ["--version"] (get-in @events [0 1 0])))
    (is (= "v24.0.0" (get-in @events [1 1 4 :observed-node-version])))))

(deftest host-read-failure-preserves-runtime-lookup-and-cleanup
  (let [runtime-var #'bootstrap/js-ts-backend-runtime
        failure (ex-info "failure transport" {:id :transport})
        seen (atom nil) stderr-file (atom nil)]
    (with-redefs-fn
      {runtime-var :node20
       #'clojure.core/slurp
       (fn [file]
         (reset! stderr-file file)
         (alter-var-root runtime-var (constantly :observed-late-runtime))
         (throw (Exception. "host read failed")))
       #'bootstrap/js-ts-backend-fail!
       (fn [& args] (reset! seen args) (throw failure))}
      #(is (identical? failure
             (try (native "run-node-process!" ["--version"] "source.gravity"
                          "B6-TARGET" "version probe failed")
                  nil (catch clojure.lang.ExceptionInfo ex ex)))))
    (is (= "B6-TARGET" (first @seen)))
    (is (= :observed-late-runtime (get-in (vec @seen) [4 :runtime])))
    (is (= "host read failed" (get-in (vec @seen) [4 :cause-message])))
    (is (instance? java.io.File @stderr-file))
    (is (not (.exists ^java.io.File @stderr-file)))))

(deftest publication-finally-port-preserves-exception-after-real-commit
  (let [directory (java.nio.file.Files/createTempDirectory
                    "gravity-js-finally-test-" (make-array java.nio.file.attribute.FileAttribute 0))
        output (.getCanonicalPath (io/file (.toFile directory) "artifact"))
        paths (native "output-paths" output)
        old-delete bootstrap/js-ts-backend-delete-tree!
        seen (atom nil) failure (ex-info "cleanup transport" {:id :cleanup})]
    (try
      (with-redefs [bootstrap/js-ts-backend-delete-tree!
                    (fn [path] (reset! seen path) (old-delete path) (throw failure))]
        (is (identical? failure
               (try (native "stage-files!" output paths {:javascript "export {};\n"} "source.gravity")
                    nil (catch clojure.lang.ExceptionInfo ex ex)))))
      (is (instance? java.nio.file.Path @seen))
      (is (not (.exists (.toFile ^java.nio.file.Path @seen))))
      (is (= "export {};\n" (slurp (:javascript paths) :encoding "UTF-8")))
      (is (.canExecute (io/file (:javascript paths))))
      (finally (old-delete directory)))))
