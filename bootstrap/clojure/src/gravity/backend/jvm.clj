(ns gravity.backend.jvm
  "Internal Java source construction and plan rejection; no input admission."
  (:require [clojure.string :as str]))

(def ^:private namespace-contract
  (quote
{:authenticates-inputs? false,
 :public-api
 {byte-array-source
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
  :compatibility-var-interposition],
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
  :java-toolchain
  :classfile-emission
  :jar-publication
  :execution
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
  :explicit-compatibility-operations],
 :namespace gravity.backend.jvm,
 :diagnostic-ownership
 {:stage ["C14-UNSUPPORTED" :jvm-stage2-lowering-rule],
  :existing-owner
  [:scalar-validation :diagnostic-construction :input-admission]},
 :artifact-outputs
 [:program-java-source
  :module-info-java-source
  :value-descriptor
  :partial-source-unit-map
  :output-path-data
  :passed-or-existing-refusal],
 :issues-certificates? false,
 :contract-boundary :hosted-scalar-java-source-data}
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


(doseq [[_ operation] (ns-publics *ns*)]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
