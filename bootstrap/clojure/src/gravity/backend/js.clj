(ns gravity.backend.js
  "Internal ESM source, process and file output; no input admission or instruction legality."
  (:require [clojure.string :as str]))

(def ^:private namespace-contract
  (quote
{:authenticates-inputs? false,
 :public-api
 {delete-tree! {:arglists ([path]), :admission :none},
 node-version!
 {:arglists ([run-node-process! fail! source-path]), :admission :none},
 run-node-process!
 {:arglists
  ([fail! runtime arguments source-path diagnostic-id message]),
  :admission :none},
 stage-files!
 {:arglists
  ([output-path-allowed?
    fail!
    delete-tree!
    output-path
    paths
    contents
    source-path]),
  :admission :none}
 bytes-source {:arglists ([name bytes indent]), :admission :none},
  canonical-target
  {:arglists ([target-aliases target-kind target]), :admission :none},
  declaration-source {:kind :constant, :admission :none},
  instruction-source
  {:arglists
   ([_bytes-source
     _value-source
     _test-source
     _value-declaration
     instruction-source
     instruction
     counter
     indent]
    [bytes-source
     value-source
     test-source
     value-declaration
     instruction-source
     instruction
     counter
     indent
     env]),
   :admission :none},
  json-string {:arglists ([value]), :admission :none},
  output-paths {:arglists ([output-path]), :admission :none},
  package-source {:arglists ([writes-stdout?]), :admission :none},
  source {:arglists ([instruction-source plan]), :admission :none},
  source-map-source
  {:arglists ([sha256-hex json-string javascript source-text]),
   :admission :none},
  test-source {:arglists ([instruction env]), :admission :none},
  validate-plan!
  {:arglists ([fail! source-path plan]), :admission :none},
  value-declaration
  {:arglists ([bytes-source runtime-bytes instruction counter indent]),
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
 [:existing-esm-byte-emission
  :json-escaping
  :partial-source-map
  :package-data
  :target-aliases
  :plan-shape-adapter
  :compatibility-var-interposition
  :existing-node-process-invocation
  :existing-node20-version-rule
  :host-tree-cleanup
  :staged-utf8-artifact-publication],
 :dependency-direction
 {:requires [clojure.string],
  :consumed-by [gravity.bootstrap],
  :forbids [gravity.bootstrap gravity.cli gravity.release]},
 :governing-contracts
 [D1 D2 D3 D6 D8 D9 C1 C14 C15 B1 B6 B13 B14 P4 TEST2],
 :unsupported
 [:untrusted-input-admission
  :instruction-legality
  :full-source-map
  :canonical-node20-target-admission
  :compiled-gravity-js-source-provenance
  :typescript-checking
  :exported-api-declarations
  :general-numeric-computation
  :async-nullish-exception-host-policy
  :full-b6-conformance
  :gravity-b6-execution-parity
  :migration
  :release
  :self-hosting
  :seed-retirement],
 :artifact-inputs
 [:caller-owned-scalar-control-flow-plan
  :instruction-summary
  :counter
  :lexical-environment
  :source-text
  :output-prefix
  :explicit-compatibility-operations
  :caller-owned-node-arguments
  :caller-owned-output-paths-and-contents
  :late-runtime-data-var],
 :namespace gravity.backend.js,
 :diagnostic-ownership
 {:stage ["C14-INPUT" "B6-TARGET" "B6-MANIFEST"
           :source-path-and-plan-shape :node20-runtime
           :node-process-completion :node20-esm-acceptance
           :output-path-containment :fresh-output-path
           :artifact-directory-containment :output-parent-directory
           :output-staging-directory :executable-module-permission
           :atomic-artifact-directory-commit],
  :existing-owner
  [:scalar-legality
   :input-admission
   :manifest-authentication
   :diagnostic-construction
   :runtime-manifest-and-provider-policy
   :output-root-policy]},
 :artifact-outputs
 [:esm-source
  :empty-export-declaration
  :source-unit-only-v3-map
  :package-json
  :value-descriptor
  :output-path-data
  :canonical-target-or-original
  :passed-or-existing-refusal
  :node-stdout-octets-and-stderr
  :observed-node-version
  :published-utf8-files
  :opaque-publication-path
  :host-tree-cleanup],
 :issues-certificates? false,
 :contract-boundary :internal-hosted-esm-source-and-output}
))

(defn canonical-target
  [target-aliases target-kind target]
  (let [target (cond
                 (keyword? target) target
                 (string? target) (keyword (str/lower-case target))
                 :else target)]
    (if (contains? @target-aliases target)
      @target-kind
      target)))

(defn validate-plan!
  "Validate only the existing source-path/string and plan/map shape."
  [fail! source-path plan]
  ;; Source-path and plan remain explicit arguments so future target-specific
  ;; representation checks can fail at this boundary without changing callers.
  (when-not (and (string? source-path) (map? plan))
    (fail!
     "C14-INPUT" "JS/TS backend requires a source-bound stage2 plan"
     source-path plan {:missing-fact :source-bound-stage2-plan}))
  :passed)

(defn bytes-source
  [name bytes indent]
  (let [padding (apply str (repeat indent "  "))]
    (str padding "const " name " = new Uint8Array(["
         (str/join "," bytes) "]);\n")))

(defn value-declaration
  [bytes-source runtime-bytes instruction counter indent]
  (let [name (str "gravityValue" (swap! counter inc))
        value (:value instruction)
        bytes (runtime-bytes value)]
    {:source (bytes-source name bytes indent)
     :descriptor {:name name
                  :truth (not (or (nil? value) (false? value)))}}))

(defn write-source
  [descriptor indent]
  (let [padding (apply str (repeat indent "  "))]
    (str padding "stdout.write(" (:name descriptor) ");\n")))

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
                            (value-declaration expr counter indent)]
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
  ([_bytes-source _value-source _test-source _value-declaration instruction-source instruction counter indent]
   (instruction-source instruction counter indent {}))
  ([bytes-source value-source test-source value-declaration instruction-source instruction counter indent env]
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
                               (str (bytes-source
                                     name [32] indent)
                                    padding "stdout.write(" name ");\n")))
                           (value-source
                            argument counter indent env)))
                    (:args instruction)))
            (let [name (str "gravityNewline" (swap! counter inc))]
              (str (bytes-source name [10] indent)
                   padding "stdout.write(" name ");\n")))

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

(defn source
  [instruction-source plan]
  (let [counter (atom 0)
        main (get-in plan [:functions (:entrypoint plan)])
        writes-stdout? (pos? (get-in plan [:instruction-summary :println] 0))]
    (str "#!/usr/bin/env node\n"
         (when writes-stdout?
           "import { stdout } from \"node:process\";\n")
         "\n"
         (apply str
                (map #(instruction-source % counter 0 {})
                     (:instructions main)))
         "//# sourceMappingURL=program.mjs.map\n")))

(def declaration-source
  "// Generated Gravity hosted entrypoint; no exported API in this slice.\nexport {};\n")(defn json-string
  [value]
  (str "\""
       (apply str
              (map (fn [ch]
                     (case ch
                       \" "\\\""
                       \\ "\\\\"
                       \backspace "\\b"
                       \formfeed "\\f"
                       \newline "\\n"
                       \return "\\r"
                       \tab "\\t"
                       (if (< (int ch) 32)
                         (format "\\u%04x" (int ch))
                         (str ch))))
                   (str value)))
       "\""))

(defn source-map-source
  "Emit a deterministic v3 source map.  Generated lines are conservatively
  anchored at the beginning of the source unit, while the Gravity extension
  records the path-neutral content identity and explicit stage2 origin chain.
  The source text itself is embedded so consumers can recover exact line and
  Unicode content without consulting a checkout path."
  [sha256-hex json-string javascript source-text]
  (let [generated-lines (max 1 (count (str/split javascript #"\n" -1)))
        mappings (str/join ";" (repeat generated-lines "AAAA"))
        source-hash (str "sha256:" (sha256-hex source-text))
        entries
        (str/join
         ","
         (map (fn [line]
                (str "{\"generatedLine\":" line
                     ",\"generatedColumn\":0,\"sourceLine\":1"
                     ",\"sourceColumn\":0,\"originChain\":["
                     "\"source-unit\",\"stage2-plan-emitter\","
                     "\"stage2-compiler-driver\",\"js-ts-lowering\"]}"))
              (range 1 (inc generated-lines))))]
    (str "{\"version\":3,\"file\":\"program.mjs\","
         "\"sources\":[\"gravity-source\"],\"sourcesContent\":["
         (json-string source-text)
         "],\"names\":[],\"mappings\":"
         (json-string mappings)
         ",\"x_gravity\":{\"sourceContentHash\":"
         (json-string source-hash)
         ",\"sourceKind\":\"co-canonical-gravity-source\","
         "\"coverage\":\"source-unit-only\","
         "\"perFormOriginPreserved\":false,"
         "\"generatedOrigins\":[" entries "]}}\n")))

(defn package-source
  [writes-stdout?]
  (str "{\"name\":\"gravity-js-ts-artifact\",\"private\":true,"
       "\"type\":\"module\",\"sideEffects\":"
       (if writes-stdout? "true" "false")
       ",\"engines\":{\"node\":\">=20 <21\"}}\n"))(defn output-paths
  [output-path]
  ;; `-o` names an artifact directory for the multi-file JS/TS target.  Fixed
  ;; logical filenames keep sourceMappingURL and every emitted byte independent
  ;; of the checkout and caller-selected output path.
  {:javascript (str output-path "/program.mjs")
   :typescript-declarations (str output-path "/program.d.ts")
   :source-map (str output-path "/program.mjs.map")
   :package-metadata (str output-path "/package.json")
   :manifest (str output-path "/manifest.edn")
   :provenance (str output-path "/provenance.edn")})


(defn run-node-process!
  [fail! runtime arguments source-path diagnostic-id message]
  (let [stdout-file (java.io.File/createTempFile "gravity-js-node-out-" ".bin")
        stderr-file (java.io.File/createTempFile "gravity-js-node-err-" ".txt")
        pb (ProcessBuilder. ^java.util.List (into ["node"] arguments))]
    (try
      (.redirectOutput pb stdout-file)
      (.redirectError pb stderr-file)
      (let [process (.start pb)
            finished? (.waitFor process 60000
                                java.util.concurrent.TimeUnit/MILLISECONDS)]
        (when-not finished?
          (.destroyForcibly process)
          (fail!
           diagnostic-id message source-path nil
           {:node-command (into ["node"] arguments)
            :missing-fact :node-process-completion}))
        (let [result {:exit (.exitValue process)
                      :stdout-bytes
                      (vec (map #(bit-and (int %) 0xff)
                                (java.nio.file.Files/readAllBytes
                                 (.toPath stdout-file))))
                      :stderr (if (.exists stderr-file)
                                (slurp stderr-file) "")}]
          (when-not (zero? (:exit result))
            (fail!
             diagnostic-id message source-path nil
             {:node-command (into ["node"] arguments)
              :node-result result
              :missing-fact :node20-esm-acceptance}))
          result))
      (catch clojure.lang.ExceptionInfo ex
        (throw ex))
      (catch Exception ex
        (fail!
         "B6-TARGET" "Node 20 runtime is unavailable"
         source-path nil
         {:runtime (deref runtime)
          :cause-message (.getMessage ex)
          :missing-fact :node20-runtime}))
      (finally
        (.delete stdout-file)
        (.delete stderr-file)))))
(defn node-version!
  [run-node-process! fail! source-path]
  (let [result (run-node-process!
                ["--version"] source-path "B6-TARGET"
                "Node runtime version check failed")
        version (str/trim
                 (String. (byte-array (map byte (:stdout-bytes result)))
                          java.nio.charset.StandardCharsets/UTF_8))]
    (when-not (re-matches #"v20(?:\..*)?" version)
      (fail!
       "B6-TARGET" "JS/TS backend requires Node 20"
       source-path nil
       {:observed-node-version version
        :required-node-version "20.x"
        :missing-fact :node20-runtime}))
    version))

(defn delete-tree!
  [path]
  (when (and path (.exists (.toFile path)))
    (doseq [file (reverse (file-seq (.toFile path)))]
      (.delete file))))

(defn stage-files!
  [output-path-allowed? fail! delete-tree! output-path paths contents source-path]
  (when-not (output-path-allowed? output-path)
    (fail!
     "C14-INPUT" "JS/TS artifact directory is outside declared roots"
     source-path nil
     {:output-path output-path
      :missing-fact :output-path-containment}))
  (let [output-directory (java.io.File. output-path)
        expected-parent (.getCanonicalPath output-directory)]
    (when (.exists output-directory)
      (fail!
       "C14-INPUT" "JS/TS backend requires a fresh artifact directory"
       source-path nil
       {:output-path output-path
        :missing-fact :fresh-output-path}))
    (doseq [[kind path] paths]
      (when-not (= expected-parent
                   (.getCanonicalPath (.getParentFile (java.io.File. path))))
        (fail!
         "C14-INPUT" "JS/TS sidecar escaped its artifact directory"
         source-path nil
         {:output-kind kind :output-path path
          :missing-fact :artifact-directory-containment})))
    (let [parent-file (or (.getParentFile output-directory)
                          (java.io.File. "."))]
      (when (and (.exists parent-file) (not (.isDirectory parent-file)))
        (fail!
         "C14-INPUT" "JS/TS artifact parent is not a directory"
         source-path nil
         {:output-path output-path
          :output-parent (.getPath parent-file)
          :missing-fact :output-parent-directory}))
      (when (and (not (.exists parent-file))
                 (not (.mkdirs parent-file)))
        (fail!
         "C14-INPUT" "JS/TS artifact parent directory is unavailable"
         source-path nil
         {:output-path output-path
          :output-parent (.getPath parent-file)
          :missing-fact :output-parent-directory}))
      (let [stage-directory
            (try
              (java.nio.file.Files/createTempDirectory
               (.toPath parent-file)
               ".gravity-js-ts-stage-"
               (make-array java.nio.file.attribute.FileAttribute 0))
              (catch Exception ex
                (fail!
                 "C14-INPUT"
                 "JS/TS staging directory could not be created"
                 source-path nil
                 {:output-path output-path
                  :output-parent (.getPath parent-file)
                  :cause-message (.getMessage ex)
                  :missing-fact :output-staging-directory})))]
        (try
        (doseq [[kind value] contents]
          (let [target-name (.getName (java.io.File. (get paths kind)))
                staged-path (.resolve stage-directory target-name)]
            (java.nio.file.Files/write
             staged-path
             (.getBytes (str value)
                        java.nio.charset.StandardCharsets/UTF_8)
             (into-array java.nio.file.OpenOption
                          [java.nio.file.StandardOpenOption/CREATE_NEW
                           java.nio.file.StandardOpenOption/WRITE]))))
        (let [staged-javascript
              (.toFile
               (.resolve stage-directory
                         (.getName (java.io.File. (:javascript paths)))))]
          (when-not (.setExecutable staged-javascript true false)
            (fail!
             "B6-MANIFEST"
             "staged JS/TS entrypoint could not be made executable"
             source-path nil
             {:missing-fact :executable-module-permission})))
        (try
          (java.nio.file.Files/move
           stage-directory
           (.toPath output-directory)
           (into-array java.nio.file.CopyOption
                       [java.nio.file.StandardCopyOption/ATOMIC_MOVE]))
          (catch Exception ex
            (fail!
             "B6-MANIFEST"
             "JS/TS artifact directory could not be atomically committed"
             source-path nil
             {:cause-message (.getMessage ex)
              :missing-fact :atomic-artifact-directory-commit})))
          (finally
            (delete-tree! stage-directory)))))))
(doseq [[_ operation] (ns-publics *ns*)]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
