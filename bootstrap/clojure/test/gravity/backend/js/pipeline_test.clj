(ns gravity.backend.js.pipeline-test
  (:require [clojure.string :as str]
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
