(ns gravity.backend.js-checked-mir-test
  "B1/C14/B6 target evidence from ordinary current source and real Node 20."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.backend.js :as js]
            [gravity.backend.js-checked-mir :as backend]
            [gravity.bootstrap :as bootstrap]
            [gravity.bootstrap-free-leaf-test-runner :as json]
            [gravity.compiler.application :as application])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files Path StandardCopyOption]
           [java.nio.file.attribute FileAttribute]
           [java.security MessageDigest]))

(def ^:private scalar-policy
  {:profile :hosted :target :js-ts :effects #{:error/raise}
   :capabilities #{} :safety :safe})

(def ^:private boolean-policy (assoc scalar-policy :effects #{}))

(defn- module-source [module exports effects definitions]
  (str "(ns " module "\n"
       "  (:profile :hosted)\n  (:target :js-ts)\n"
       "  (:effects " (pr-str effects) ")\n"
       "  (:capabilities #{})\n  (:safety :safe)\n"
       "  (:exports " (pr-str exports) "))\n\n" definitions "\n"))

(def ^:private scalar-source
  (module-source 'acceptance.scalar '[score] #{:error/raise}
    (str "(defn add [left :- I64 right :- I64]\n"
         "  (+ left right))\n\n"
         "(defn score [choose-sum :- Bool left :- I64 right :- I64]\n"
         "  (let [sum (add left right)]\n"
         "    (if choose-sum\n      sum\n      left)))")))

(def ^:private composed-source
  (module-source 'renamed.scalar.composition '[evaluate] #{:error/raise}
    (str "(defn evaluate [enabled :- Bool first-value :- I64 second-value :- I64]\n"
         "  (let [combined (combine first-value second-value)\n"
         "        computed (relay combined first-value)]\n"
         "    (if enabled\n"
         "      (+ computed (if enabled second-value first-value))\n"
         "      (relay second-value first-value))))\n\n"
         "(defn relay [item :- I64 extra :- I64]\n  (combine item extra))\n\n"
         "(defn combine [augend :- I64 addend :- I64]\n  (+ augend addend))")))

(defn- options [output]
  {:output-path (str output) :target :js-ts
   :runtime :node20 :language :es2022 :module-format :esm})

(defn- sha256 [^bytes bytes]
  (str "sha256:"
       (apply str (map #(format "%02x" (bit-and 255 %))
                      (.digest (MessageDigest/getInstance "SHA-256") bytes)))))

(defn- content-id? [value]
  (and (string? value) (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- with-source [filename text operation]
  (let [directory (Files/createTempDirectory "uniform-js-checked-" (make-array FileAttribute 0))
        source (.resolve directory filename)]
    (try
      (spit (.toFile source) text :encoding "UTF-8")
      (operation (.getCanonicalPath (.toFile source)) directory)
      (finally (js/delete-tree! directory)))))

(defn- failure [operation]
  (try (operation) nil
       (catch clojure.lang.ExceptionInfo error (ex-data error))))

(defn- diagnostic-source [data]
  (or (get-in data [:source-span :source])
      (get-in data [:source-span :file])
      (get-in data [:primary :span :source])
      (get-in data [:primary :span :file])
      (:actual-source-path data)
      (get-in data [:provenance :actual-source-path])
      (:source-path data)))

(defn- node-output [source-path module-path script]
  (let [result (bootstrap/js-ts-backend-run-node-process!
                 ["--input-type=module" "--eval"
                  (str "process.chdir(process.argv[2]);\n"
                       "const m = await import(process.argv[1]);\n" script)
                  (str (.toURI (io/file module-path)))
                  (.getCanonicalPath (.getParentFile (io/file module-path)))]
                 source-path "B6-TARGET" "Node rejected a checked scalar module")]
    (is (zero? (:exit result)))
    (is (str/blank? (:stderr result)) (:stderr result))
    (String. (byte-array (map unchecked-byte (:stdout-bytes result))) StandardCharsets/UTF_8)))

(defn- node-report [source-path module-path script]
  (json/read-strict-component-contract (node-output source-path module-path script)))

(defn- reference-invoker [source-path]
  ;; :jvm selects the compiler implementation host, not the application's
  ;; source/artifact target. The supplied Core and MIR remain :js-ts products.
  (let [binding (bootstrap/p15-s23-c11-mir-source-binding!
                  source-path :jvm 2 :development-current)]
    (fn [function arguments]
      (bootstrap/p15-s23-stage2-runtime-execute-function
        {:engine :gravity-js-checked-mir-reference-test :compiler-artifact-plan? true}
        (:plan binding) function arguments))))

(defn- reference-result [invoke result exported-function arguments]
  (let [inputs [(:checked-core result) (:mir-module result) exported-function arguments]
        execution (invoke 'sh13-run-scalar-functions-mir inputs)]
    (is (= :passed (:status (invoke 'sh13-verify-scalar-functions-execution
                                    (conj inputs execution)))))
    execution))

(defn- function-operations [mir name]
  (let [function (get-in mir [:functions name])]
    (vec (mapcat #(get-in function [:blocks % :instructions]) (:block-order function)))))

(defn- mutate-operation [mir opcode transform]
  (let [path (first
               (for [[name function] (:functions mir)
                     block-id (:block-order function)
                     [index operation] (map-indexed vector (get-in function [:blocks block-id :instructions]))
                     :when (= opcode (:opcode operation))]
                 [:functions name :blocks block-id :instructions index]))]
    (is (some? path) (str "Genuine MIR must contain " opcode))
    (if path (update-in mir path transform) mir)))

(defn- target-owner-invoker [source-path relative-path]
  (let [project-root (bootstrap/reader-project-root-path
                       (bootstrap/p15-s23-c11-mir-resolve-source-path))
        path (.getCanonicalPath (io/file project-root relative-path))
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source-path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path :encoding "UTF-8"))]
    (fn [function arguments]
      (bootstrap/p15-s23-stage2-runtime-execute-function
       {:engine :gravity-js-target-rules-test :compiler-artifact-plan? true}
       plan function arguments))))

(defn- assert-owned-rejection [path expected-id result]
  (is (= :rejected (:status result)) (pr-str result))
  (is (= expected-id (get-in result [:diagnostics 0 :diagnostic-id])))
  (is (= path (diagnostic-source (first (:diagnostics result)))))
  (println "checked scalar Uniform target rule rejection" (pr-str {:id expected-id
                                                                   :missing-fact (get-in result [:diagnostics 0 :missing-fact])})))

(defn- assert-target-owner-rejections [path product]
  ;; These are actual current Uniform owners executed on genuine production
  ;; products. Pure target-rule tests do not confer construction authority.
  (let [invoke-c14 (target-owner-invoker path "language/compiler/src/gravity/compiler/c14_target_lowering_architecture.gravity")
        invoke-b1 (target-owner-invoker path "language/compiler/src/gravity/backend/b1_backend_interface_specification.gravity")
        invoke-b6 (target-owner-invoker path "language/compiler/src/gravity/backend/b6_javascript_typescript_backend_design.gravity")
        request {:target :js-ts :runtime :node20 :language :es2022 :module-format :esm}
        lowering (invoke-c14 'c14-build-checked-js-lowering [product request])
        packet (invoke-b1 'b1-build-checked-js-input [product lowering])
        program (invoke-b6 'b6-build-checked-js-program [product lowering packet])
        mir (:mir-module product)
        addition (first (filter #(= :integer-add (:opcode %)) (function-operations mir 'add)))
        effect-id (get-in addition [:facts :effect-fact-id])
        proof-operation (first (filter #(string? (get-in % [:facts :safety-proof-id]))
                                       (function-operations mir 'add)))
        proof-id (get-in proof-operation [:facts :safety-proof-id])
        entry (get-in mir [:functions 'add :entry])]
    (is (= :accepted (:status lowering) (:status packet) (:status program)))
    (is (= :passed (:status (invoke-c14 'c14-verify-checked-js-lowering [product request lowering]))))
    (is (= :passed (:status (invoke-b1 'b1-verify-checked-js-input [product lowering packet]))))
    (is (= :passed (:status (invoke-b6 'b6-verify-checked-js-program [product lowering packet program]))))
    (is (contains? (:effect-table mir) effect-id))
    (is (contains? (get-in mir [:proof-certificate-table :safety-proofs]) proof-id))
    (doseq [[expected candidate]
            [["C14-UNSUPPORTED" (update product :mir-module mutate-operation :integer-add
                                          #(assoc % :opcode :dynamic-eval))]
             ["C14-PROOF-METADATA"
              (update-in product [:mir-module :functions 'add :blocks entry :instructions]
                         #(filterv (fn [operation] (not= :runtime-check (:opcode operation))) %))]
             ["C14-PROOF-METADATA" (update product :mir-module mutate-operation :integer-add
                                              #(update % :operands (fn [values] (into [] (rest values)))))]
             ["C14-MANIFEST" (update-in product [:mir-module :effect-table] dissoc effect-id)]
             ["C14-MANIFEST" (update-in product [:mir-module :proof-certificate-table :safety-proofs] dissoc proof-id)]
             ["C14-ABI" (update product :mir-module mutate-operation :call
                                 #(assoc-in % [:signature :params 0] :gravity/bool))]
             ["C14-INPUT" (assoc-in product [:source :content-hash] "sha256:changed")]
             ["C14-INPUT" (dissoc product :mir-verification)]]]
      (is (not= product candidate))
      (assert-owned-rejection path expected
                              (invoke-c14 'c14-build-checked-js-lowering [candidate request])))
    (assert-owned-rejection path "B6-TARGET"
                            (invoke-c14 'c14-build-checked-js-lowering [product (assoc request :runtime :browser)]))
    (doseq [candidate [(assoc-in lowering [:abi :exports 0 :parameters 0] :gravity/i64)
                       (assoc-in lowering [:runtime :host-globals] [])
                       (update lowering :evidence dissoc :proof-certificate-table)]]
      (is (not= lowering candidate))
      (assert-owned-rejection path "B1-INPUT" (invoke-b1 'b1-build-checked-js-input [product candidate])))
    (assert-owned-rejection path "B1-INPUT"
                            (invoke-b6 'b6-build-checked-js-program
                                       [product lowering (assoc-in packet [:abi :exports 0 :parameters 0] :gravity/i64)]))
    (assert-owned-rejection path "B6-MANIFEST"
                            (invoke-b6 'b6-verify-checked-js-program
                                       [product lowering packet (assoc program :exports [])]))))

(defn- copy-artifacts! [source destination]
  (doseq [file (.listFiles (io/file (str source)))]
    (Files/copy (.toPath file) (.resolve ^Path destination (.getName file))
                (make-array StandardCopyOption 0))))

(defn- decode-vlq [segment]
  (let [alphabet "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"]
    (loop [characters (seq segment) bits 0 shift 0 values []]
      (if-let [character (first characters)]
        (let [digit (.indexOf alphabet (int character))
              next-bits (bit-or bits (bit-shift-left (bit-and digit 31) shift))]
          (is (not (neg? digit)) "Source-map segments use base64 VLQ")
          (if (zero? (bit-and digit 32))
            (recur (next characters) 0 0
                   (conj values (* (if (odd? next-bits) -1 1) (unsigned-bit-shift-right next-bits 1))))
            (recur (next characters) next-bits (+ shift 5) values)))
        (do (is (zero? shift) "Source-map VLQ segments terminate") values)))))

(defn- decoded-source-map [mappings]
  (let [state (atom {:source 0 :line 0 :column 0})]
    (into {}
          (map-indexed
           (fn [index line]
             [(inc index)
              (loop [segments (remove empty? (str/split line #",")) column 0 result []]
                (if-let [segment (first segments)]
                  (let [[generated-delta source-delta line-delta column-delta :as values] (decode-vlq segment)
                        generated-column (+ column generated-delta)]
                    (if (< (count values) 4)
                      (recur (next segments) generated-column result)
                      (let [origin (swap! state #(-> % (update :source + source-delta)
                                                    (update :line + line-delta)
                                                    (update :column + column-delta)))]
                        (recur (next segments) generated-column
                               (conj result {:generated-column generated-column
                                             :source (:source origin) :source-line (inc (:line origin))
                                             :source-column (:column origin)})))))
                  result))])
           (str/split mappings #";" -1)))))

(def ^:private scalar-execution-script
  (str "const inputs = [[true,3n,7n],[false,7n,3n],"
       "[true,9007199254740993n,9007199254740994n],"
       "[true,9223372036854775807n,0n],[true,-9223372036854775808n,0n],"
       "[true,9223372036854775807n,-9223372036854775808n]];\n"
       "console.log(JSON.stringify({exports:Object.keys(m),"
       "results:inputs.map(args => m.score(...args).toString())}));"))

(def ^:private error-execution-script
  (str "const cases = [[false,9223372036854775807n,1n],"
       "[true,-9223372036854775808n,-1n],"
       "[null,3n],[true,3n,7n,1n],[true,3,7n],[true,null,7n],"
       "[true,undefined,7n],[1,3n,7n],[null,3n,7n],[undefined,3n,7n],"
       "[true,9223372036854775808n,0n],[true,-9223372036854775809n,0n]];\n"
       "console.log(JSON.stringify(cases.map(args => {try {"
       "return {returned:m.score(...args).toString()};"
       "} catch (e) {return {isError:e instanceof Error,name:e.name,type:e.type,kind:e.kind,"
       "numericMode:e.numericMode,width:e.width,signed:e.signed,operation:e.operation,"
       "checkId:e.checkId,operationId:e.operationId,"
       "operands:Array.isArray(e.operands)?e.operands.map(value=>value.toString()):null,"
       "operandTypes:Array.isArray(e.operands)?e.operands.map(value=>typeof value):null,"
       "function:e.function,argumentIndex:e.argumentIndex,expected:e.expected,"
       "actualType:e.actualType,actualArity:e.actualArity,source:e.source};}})));"))

(defn- assert-artifacts [result path text]
  (let [paths (:paths result)
        artifact-directory (.getParentFile (io/file (:javascript paths)))
        source-map (json/read-strict-component-contract (slurp (:source-map paths)))
        package-data (json/read-strict-component-contract (slurp (:package-metadata paths)))
        application (:application result)]
    (is (= #{"program.mjs" "program.d.ts" "program.mjs.map" "package.json"
             "manifest.edn" "provenance.edn"}
           (set (map #(.getName %) (.listFiles artifact-directory)))))
    (is (= (:manifest result) (edn/read-string (slurp (:manifest paths)))))
    (is (= (:provenance result) (edn/read-string (slurp (:provenance paths)))))
    (is (= #{:javascript :typescript-declarations :source-map :package-metadata :provenance}
           (set (keys (get-in result [:manifest :artifacts])))))
    (doseq [[kind record] (get-in result [:manifest :artifacts])]
      (let [file (io/file (get paths kind)) bytes (Files/readAllBytes (.toPath file))]
        (is (= (.getName file) (:file record)))
        (is (= (alength bytes) (:byte-count record)))
        (is (= (sha256 bytes) (:sha256 record)))))
    (is (= :js-ts (:source-target application)
           (get-in application [:checked-core :source-target])
           (get-in application [:mir-module :source-target])))
    (is (= :pending (get-in application [:mir-module :verification-status]))
        "The raw MIR flag is not rewritten into an acceptance certificate")
    (is (= :passed (get-in application [:mir-verification :status])))
    (is (= :jvm (get-in application [:compiler-host :target])))
    (is (= (sha256 (.getBytes ^String text StandardCharsets/UTF_8))
           (get-in application [:source :content-hash])))
    (is (= path (get-in application [:source :path])))
    (is (= "module" (get package-data "type")))
    (is (= ">=20 <21" (get-in package-data ["engines" "node"])))
    (is (= 3 (get source-map "version")))
    (is (= [text] (get source-map "sourcesContent")))
    (is (seq (get source-map "mappings")))
    (is (not= "source-unit-only" (get-in source-map ["x_gravity" "coverage"])))
    (let [origins (get-in source-map ["x_gravity" "generatedOrigins"])
          decoded (decoded-source-map (get source-map "mappings"))
          core-ids (set (map :node-id (get-in application [:checked-core :core-nodes])))
          function-ids (set (map :fn-id (vals (get-in application [:mir-module :functions]))))
          addition (first (filter #(= :integer-add (:opcode %))
                                  (function-operations (:mir-module application) 'add)))
          addition-origins (filter #(= (:op-id addition) (get % "mirInstructionId")) origins)]
      (is (seq origins))
      (is (seq addition-origins) "A numeric failure maps to its original addition")
      (is (every? #(= 10 (get % "sourceLine")) addition-origins))
      (is (> (count (set (map #(get % "sourceLine") origins))) 1))
      (doseq [origin origins]
        (is (vector? (get origin "originChain")))
        (is (contains? core-ids (get origin "coreNodeId")))
        (is (contains? function-ids (get origin "functionId")))
        (is (content-id? (get origin "syntaxId")))
        (is (some #{(select-keys
                     {:generated-column (get origin "generatedColumn")
                      :source 0 :source-line (get origin "sourceLine")
                      :source-column (get origin "sourceColumn")}
                     [:generated-column :source :source-line :source-column])}
                  (get decoded (get origin "generatedLine")))
            "Generated-origin coordinates agree with the actual standard v3 mappings")))
    (is (str/includes? (slurp (:typescript-declarations paths)) "bigint"))
    (is (str/includes? (slurp (:typescript-declarations paths)) "boolean"))
    (is (str/includes? (slurp (:typescript-declarations paths)) "score"))
    (is (re-find #"\([^)]*:\s*boolean\s*,[^)]*:\s*bigint\s*,[^)]*:\s*bigint\s*\):\s*bigint"
                 (slurp (:typescript-declarations paths))))
    (is (not (str/includes? (slurp (:typescript-declarations paths)) "export {};")))))

(deftest scalar-module-target-execution
  (with-source "acceptance.gravity" scalar-source
    (fn [path directory]
      (let [output (.resolve directory "acceptance-js")
            result (application/current-source-to-js-module path scalar-policy (options output))
            product (:application result)
            invoke (reference-invoker path)
            node (node-report path (get-in result [:paths :javascript]) scalar-execution-script)
            arguments [[true 3 7] [false 7 3]
                       [true 9007199254740993 9007199254740994]
                       [true Long/MAX_VALUE 0] [true Long/MIN_VALUE 0]
                       [true Long/MAX_VALUE Long/MIN_VALUE]]
            expected ["10" "7" "18014398509481987" "9223372036854775807"
                      "-9223372036854775808" "-1"]]
        (assert-artifacts result path scalar-source)
        (is (= ["score"] (get node "exports")) "Private add is not exported")
        (is (= expected (get node "results")))
        (doseq [[args actual] (map vector arguments (get node "results"))]
          (let [execution (reference-result invoke product 'score args)]
            (is (= :returned (get-in execution [:execution :status])))
            (is (= actual (str (get-in execution [:execution :value]))))))
        (let [outside (Files/createTempDirectory "uniform-js-standalone-" (make-array FileAttribute 0))]
          (try
            (copy-artifacts! output outside)
            (is (= node (node-report path (str (.resolve outside "program.mjs")) scalar-execution-script)))
            (finally (js/delete-tree! outside))))
        (println "checked scalar Node20 execution" (pr-str {:results expected :exports (get node "exports")
                                                           :standalone true}))))))

(deftest scalar-module-runtime-rejections
  (with-source "runtime-rejections.gravity" scalar-source
    (fn [path directory]
      (let [result (application/current-source-to-js-module path scalar-policy
                     (options (.resolve directory "module")))
            product (:application result)
            errors (node-report path (get-in result [:paths :javascript]) error-execution-script)
            invoke (reference-invoker path)
            addition (first (filter #(= :integer-add (:opcode %))
                                    (function-operations (:mir-module product) 'add)))
            expected-types [":error/numeric" ":error/numeric" ":error/abi" ":error/abi"
                            ":error/abi" ":error/abi" ":error/abi" ":error/abi" ":error/abi"
                            ":error/abi" ":error/abi" ":error/abi"]
            expected-kinds [":overflow" ":overflow" ":arity" ":arity" ":type" ":type"
                            ":type" ":type" ":type" ":type" ":range" ":range"]]
        (is (= expected-types (mapv #(get % "type") errors)))
        (is (= expected-kinds (mapv #(get % "kind") errors)))
        (doseq [error errors]
          (is (not (contains? error "returned")))
          (is (= true (get error "isError")))
          (is (= path (get-in error ["source" "path"])))
          (is (= (get-in product [:source :content-hash]) (get-in error ["source" "contentHash"])))
          (is (map? (get-in error ["source" "span"])))
          (is (content-id? (get-in error ["source" "coreNodeId"]))))
        (doseq [[args error] (map vector [[false Long/MAX_VALUE 1] [true Long/MIN_VALUE -1]] (take 2 errors))]
          (let [reference (reference-result invoke product 'score args)]
            (is (= :raised (get-in reference [:execution :status])))
            (is (= "UniformNumericError" (get error "name")))
            (is (= ":checked" (get error "numericMode")))
            (is (= 64 (get error "width")))
            (is (= true (get error "signed")))
            (is (= ":add" (get error "operation")))
            (is (= (get-in reference [:execution :error :check-id])
                   (get-in addition [:facts :runtime-check-id])
                   (get error "checkId")))
            (is (= (get-in reference [:execution :error :operation-id])
                   (:op-id addition) (get error "operationId")))
            (is (= ["bigint" "bigint"] (get error "operandTypes"))
                "Numeric error operands retain native BigInt values before JSON transport")
            (is (= (mapv str (get-in reference [:execution :error :operands]))
                   (mapv str (rest args)) (get error "operands"))
                "The target error preserves the actual evaluated operands in source order")
            (is (= (:op-id addition) (get-in error ["source" "mirInstructionId"])))
            (is (= (:op-id addition) (get-in error ["source" "coreNodeId"])))
            (is (= 10 (get-in error ["source" "span" "start" "line"])))
            (is (= (str (get-in reference [:execution :error :type])) (get error "type")))
            (is (= (str (get-in reference [:execution :error :kind])) (get error "kind")))))
        (doseq [[index error] (map-indexed vector (drop 2 errors))]
          (is (= "UniformABIError" (get error "name")))
          (is (= "score" (get error "function")))
          (is (= (nth [nil nil 1 1 1 0 0 0 1 1] index) (get error "argumentIndex")))
          (is (= (nth [3 3 "bigint" "bigint" "bigint" "boolean" "boolean" "boolean"
                       "signed I64" "signed I64"] index) (get error "expected")))
          (is (= (nth [nil nil "number" "null" "undefined" "number" "null" "undefined"
                       "bigint" "bigint"] index) (get error "actualType")))
          (when (< index 2)
            (is (= (nth [2 4] index) (get error "actualArity")))))
        (println "checked scalar runtime rejection kinds" (pr-str expected-kinds))))))

(deftest scalar-module-boolean-execution
  (let [text (module-source 'acceptance.boolean '[choose flag exact] #{}
               (str "(defn choose [enabled𝒙 :- Bool] (if enabled𝒙 true false))\n"
                    "(defn flag [] (do true false))\n"
                    "(defn exact [] 9007199254740993)"))]
    (with-source "boolean.gravity" text
      (fn [path directory]
        (let [result (application/current-source-to-js-module path boolean-policy
                       (options (.resolve directory "module")))
              report (node-report path (get-in result [:paths :javascript])
                       (str "console.log(JSON.stringify({exports:Object.keys(m),"
                            "results:[m.choose(true),m.choose(false),m.flag()],exact:m.exact().toString()}));"))
              source-map (json/read-strict-component-contract
                           (slurp (get-in result [:paths :source-map])))
              origins (get-in source-map ["x_gravity" "generatedOrigins"])
              lines (str/split text #"\n" -1)
              invoke (reference-invoker path)]
          (is (= ["choose" "exact" "flag"] (get report "exports")))
          (is (= [true false false] (get report "results")))
          (is (= "9007199254740993" (get report "exact")))
          (doseq [[name arguments expected] [['choose [true] true] ['choose [false] false]
                                            ['flag [] false]]]
            (let [reference (reference-result invoke (:application result) name arguments)]
              (is (= :returned (get-in reference [:execution :status])))
              (is (= expected (get-in reference [:execution :value])))))
          (let [reference (reference-result invoke (:application result) 'exact [])]
            (is (= :returned (get-in reference [:execution :status])))
            (is (= "9007199254740993" (str (get-in reference [:execution :value])))))
          (is (some #(> (get % "sourceColumn")
                        (dec (get-in % ["sourceSpan" "start" "column"]))) origins)
              "The supplementary Unicode parameter forces a real scalar-to-UTF16 column conversion")
          (doseq [origin origins]
            (let [line (get lines (dec (get origin "sourceLine")))
                  scalar-column (dec (get-in origin ["sourceSpan" "start" "column"]))]
              (is (= "utf16-code-unit" (get origin "sourceColumnUnit")))
              (is (= (.offsetByCodePoints ^String line 0 scalar-column)
                     (get origin "sourceColumn")))))
          (is (empty? (get-in result [:application :mir-module :runtime-check-table])))
          (println "checked Boolean Node20 execution" (pr-str (get report "results"))))))))

(deftest scalar-module-composed-qst-and-shadowed-call
  (doseq [[filename text policy exported inputs script expected]
          [["renamed.qst" composed-source scalar-policy 'evaluate [[true 3 7] [false 3 7]]
            "console.log(JSON.stringify({exports:Object.keys(m),results:[m.evaluate(true,3n,7n).toString(),m.evaluate(false,3n,7n).toString()]}));"
            ["20" "10"]]
           ["shadowed.gravity"
            (module-source 'acceptance.shadowed '[select-value] #{}
              (str "(defn + [selected :- I64 unused :- I64] selected)\n\n"
                   "(defn select-value [first-value :- I64 second-value :- I64]\n"
                   "  (+ second-value first-value))"))
            boolean-policy 'select-value [[3 7] [Long/MAX_VALUE 1] [1 Long/MAX_VALUE]]
            (str "console.log(JSON.stringify({exports:Object.keys(m),results:[m['select-value'](3n,7n).toString(),"
                 "m['select-value'](9223372036854775807n,1n).toString(),"
                 "m['select-value'](1n,9223372036854775807n).toString()]}));")
            ["7" "1" "9223372036854775807"]]]]
    (with-source filename text
      (fn [path directory]
        (let [emission (cond-> (options (.resolve directory "module"))
                         (= filename "shadowed.gravity") (assoc :target :js))
              result (application/current-source-to-js-module path policy emission)
              node (node-report path (get-in result [:paths :javascript]) script)
              invoke (reference-invoker path)]
          (is (= [(str exported)] (get node "exports")))
          (is (= expected (get node "results")))
          (doseq [[arguments value] (map vector inputs expected)]
            (let [reference (reference-result invoke (:application result) exported arguments)]
              (is (= :returned (get-in reference [:execution :status])))
              (is (= value (str (get-in reference [:execution :value]))))))
          (when (= filename "shadowed.gravity")
            (is (empty? (get-in result [:application :mir-module :runtime-check-table])))
            (is (= :js (get-in result [:provenance :requested-target])))
            (is (= :js-ts (get-in result [:provenance :canonical-target])
                   (get-in result [:application :source-policy-receipt :policy :target]))))
          (println "checked scalar alternate module" (pr-str {:file filename :results expected})))))))

(deftest scalar-module-rejects-source-and-target-mismatch
  (doseq [[label text policy emission expected-id]
          [["source-policy" scalar-source (assoc scalar-policy :target :jvm) {} "C6-ORIGIN"]
           ["unsupported-runtime" scalar-source scalar-policy {:runtime :browser} "B6-TARGET"]
           ["unsupported-language" scalar-source scalar-policy {:language :es2019} "B6-TARGET"]
           ["unsupported-format" scalar-source scalar-policy {:module-format :commonjs} "B6-TARGET"]
           ["unsupported-target" scalar-source scalar-policy {:target :llvm} "B6-TARGET"]
           ["supplied-mir-option" scalar-source scalar-policy {:mir-module {:status :accepted}} "B6-TARGET"]
           ["supplied-verification-option" scalar-source scalar-policy
            {:mir-verification {:status :passed}} "B6-TARGET"]
           ["malformed" (str scalar-source "[") scalar-policy {} "C2-DELIMITER"]
           ["argument-type" (str/replace scalar-source "(add left right)" "(add true right)")
            scalar-policy {} "C7-TYPE-MISMATCH"]
           ["call-arity" (str/replace scalar-source "(add left right)" "(add left)")
            scalar-policy {} "L2-BUILTIN-ARITY"]
           ["undeclared-error" (str/replace scalar-source "#{:error/raise}" "#{}")
            boolean-policy {} "C8-UNDECLARED"]
           ["unsupported-recursion" (str/replace scalar-source "(+ left right)" "(add left right)")
            scalar-policy {} "C6-LOWERING-GAP"]]]
    (with-source (str label ".gravity") text
      (fn [path directory]
        (let [output (.resolve directory "rejected")
              data (failure #(application/current-source-to-js-module path policy
                               (merge (options output) emission)))]
          (is (= expected-id (:id data)) (pr-str data))
          (is (= path (diagnostic-source data)) (pr-str data))
          (is (not (.exists (.toFile output))) "Rejection cannot publish partial artifacts")
          (println "checked scalar source/target rejection" (pr-str {:case label :id (:id data)})))))))

(deftest scalar-module-rejects-genuine-product-tampering
  (with-source "tamper.gravity" scalar-source
    (fn [path directory]
      (let [captured (atom nil)
            replaced-constructor? (atom false)
            refuse! (fn [label authenticate! candidate]
                      (let [output (.resolve directory label)
                            data (failure #(backend/emit-verified-module! authenticate! candidate (options output)))]
                        (is (some? data) (str label " unexpectedly accepted"))
                        (is (string? (:id data)) (pr-str data))
                        (is (= path (diagnostic-source data)) (pr-str data))
                        (is (not (.exists (.toFile output))))
                        (println "checked scalar product rejection" (pr-str {:case label :id (:id data)}))
                        data))]
        (with-redefs [bootstrap/p15-s23-current-source-to-verified-mir
                      (fn [& _]
                        (reset! replaced-constructor? true)
                        {:artifact :caller-substituted-product :status :accepted})]
         (bootstrap/p15-s23-call-with-current-source-verified-mir
         path scalar-policy
         (fn [product authenticate!]
           (reset! captured [product authenticate!])
           (assert-target-owner-rejections path product)
           (let [execute bootstrap/p15-s23-stage2-runtime-execute-function]
             (doseq [[label verifier change]
                     [["c14-verifier-application" 'c14-verify-checked-js-lowering
                       #(assoc % :application-id "sha256:substituted")]
                      ["b1-verifier-artifact" 'b1-verify-checked-js-input
                       #(assoc % :artifact :caller/substituted-verifier)]
                      ["b6-verifier-checks" 'b6-verify-checked-js-program
                       #(assoc % :checks [])]]]
               (let [substituted? (atom false)]
                 (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                               (fn [runtime plan function arguments]
                                 (let [report (execute runtime plan function arguments)]
                                   (if (= verifier function)
                                     (do (is (= :passed (:status report)))
                                         (reset! substituted? true)
                                         (change report))
                                     report)))]
                   (let [data (refuse! label authenticate! product)]
                     (is (= :current-target-verifier-result-binding (:missing-fact data))
                         "The host binds the genuine target owner's exact fresh verifier report")))
                 (is @substituted? "The mutation must reach the actual target verifier result"))))
           (let [core (:checked-core product) mir (:mir-module product)
                 caller (get-in core [:functions 'score])
                 addition (first (filter #(= :integer-add (:opcode %)) (function-operations mir 'add)))
                 cases
                 [["missing-verification" #(dissoc % :mir-verification)]
                  ["substituted-verification" #(assoc-in % [:mir-verification :checks] [])]
                  ["changed-source" #(assoc-in % [:source :content-hash] "sha256:changed")]
                  ["changed-core" #(assoc-in % [:checked-core :type-facts caller :type :return] :gravity/bool)]
                  ["compiler-owner" #(assoc-in % [:compiler-bindings :c11 :source-content-hash] "sha256:changed")]
                  ["call-signature" #(update % :mir-module mutate-operation :call
                                             (fn [op] (assoc-in op [:signature :params 0] :gravity/bool)))]
                  ["exported-abi" #(assoc-in % [:mir-module :functions 'score :param-types 0] :gravity/i64)]
                  ["missing-overflow-guard" #(assoc-in % [:mir-module :runtime-check-table] {})]
                  ["guard-token" #(update % :mir-module mutate-operation :integer-add
                                          (fn [op] (update op :operands (fn [values] (into [] (rest values))))))]
                  ["missing-effects" #(assoc-in % [:mir-module :effect-table] {})]
                  ["missing-ownership" #(assoc-in % [:mir-module :ownership-table] {})]
                  ["missing-safety" #(assoc-in % [:mir-module :safety-table] {})]
                  ["missing-proof" #(assoc-in % [:mir-module :proof-certificate-table] {})]
                  ["missing-source-map" #(assoc-in % [:mir-module :source-map] {})]
                  ["missing-provenance" #(dissoc % :source-policy-receipt)]
                  ["unsupported-operation" #(update % :mir-module mutate-operation :integer-add
                                                    (fn [op] (assoc op :opcode :dynamic-eval)))]]]
             (is (some? addition))
             (doseq [[label change] cases]
               (testing label
                 (let [candidate (change product)]
                   (is (not= product candidate) "Every tamper changes a genuine product")
                   (refuse! label authenticate! candidate))))
             (let [replaced-authenticator? (atom false)]
               (with-redefs [bootstrap/p15-s23-authenticate-current-source-mir-handoff!
                             (fn [& _] (reset! replaced-authenticator? true) :passed)]
                 (refuse! "substituted-authority-entrypoint" (constantly :passed) product))
               (is (false? @replaced-authenticator?)
                   "Emitter authentication must use the original authority rather than a replaceable public Var"))
             (refuse! "substituted-authority-callback" (constantly :passed) product)
             (try
               (spit path (str scalar-source "\n") :encoding "UTF-8")
               (refuse! "changed-source-bytes" authenticate! product)
               (finally (spit path scalar-source :encoding "UTF-8")))))))
        (is (false? @replaced-constructor?)
            "Backend authority must capture the genuine constructor rather than a replaceable public Var")
        (let [[product authenticate!] @captured]
          (refuse! "expired-authority-callback" authenticate! product))))))
