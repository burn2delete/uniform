(ns gravity.backend.jvm.pipeline-test
  (:require [clojure.string :as str]
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

