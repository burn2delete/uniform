(ns gravity.backend.js
  "Internal ESM source and package data; no input admission or instruction legality."
  (:require [clojure.string :as str]))

(def ^:private namespace-contract
  (quote
{:authenticates-inputs? false,
 :public-api
 {bytes-source {:arglists ([name bytes indent]), :admission :none},
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
  :compatibility-var-interposition],
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
  :node-execution
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
  :explicit-compatibility-operations],
 :namespace gravity.backend.js,
 :diagnostic-ownership
 {:stage ["C14-INPUT" :source-path-and-plan-shape],
  :existing-owner
  [:scalar-legality
   :input-admission
   :manifest-authentication
   :diagnostic-construction
   :runtime-policy]},
 :artifact-outputs
 [:esm-source
  :empty-export-declaration
  :source-unit-only-v3-map
  :package-json
  :value-descriptor
  :output-path-data
  :canonical-target-or-original
  :passed-shape-or-existing-refusal],
 :issues-certificates? false,
 :contract-boundary :hosted-esm-source-and-package-data}
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
  "Apply the JS/TS-specific closed-surface rules after the shared stage2/C
  packet has validated instruction shape.  Variadic println remains bounded by
  the shared closed-plan node limit and lowers to ordered byte writes."
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


(doseq [[_ operation] (ns-publics *ns*)]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
