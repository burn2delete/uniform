(ns gravity.runtime.stage0-execution-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as b]
            [gravity.digest :as digest]
            [gravity.runtime :as runtime]))

(def ^:private operations
  {:execute-instructions #'b/execute-stage0-instructions
   :execute-instruction #'b/execute-stage0-instruction
   :execute-function #'b/execute-stage0-compiled-function
   :validate-effects! #'b/validate-module-effects!
   :invoke-builtin #'b/invoke-stage0-builtin
   :fail-call-arity! #'b/fail-call-arity! :fail! #'b/fail! :join #'str/join})
(def ^:private native-plan {:source {:path "execution.gravity"}
                           :module {:source-path "execution.gravity"}
                           :entrypoint 'main})
(defn- literal [value] {:op :literal :value value})
(defn- call [name & values] {:op :builtin-call :function name :args (mapv literal values)})
(defn- observed-error [run] (try (run) nil (catch Throwable error error)))
(defn- compile-source [path source]
  (is (nil? (var-get #'b/*authenticated-source-form-records*)))
  (let [expanded (b/macro-source-artifact path source)
        module (assoc (:module expanded) :forms (:expanded-forms expanded))]
    (b/stage0-compiled-core-plan path source module)))

(deftest native-execution-contract-is-explicit-and-non-authorizing
  (let [names '[execute-native-stage0-instructions execute-native-stage0-instruction
                execute-native-stage0-function execute-native-stage0-plan]
        contract (:gravity/namespace-contract (meta #'runtime/execute-native-stage0-plan))]
    (is (= 42 (count (ns-publics 'gravity.runtime)) (count (:public-api contract))))
    (is (= (set (keys operations))
           (set (mapcat :operation-keys (vals (select-keys (:public-api contract) names))))))
    (is (every? var? (vals operations)))
    (is (empty? (ns-aliases 'gravity.runtime)))
    (is (false? (:authenticates-plans? contract)))
    (is (false? (:grants-authority? contract)))
    (is (false? (:canonical-runtime-manifest? contract)))
    (is (every? (set (:governing-contracts contract)) '[D0 D1 D2 D3 D6 D8 D9 L2 L9 C1 C6 C15 SAFE9 R1 R4 R11 R12 BOOT1 BOOT8 TEST2]))
    (doseq [name names :let [operation (ns-resolve 'gravity.runtime name)
                            api (get-in contract [:public-api name])]]
      (is (= (:arglists (meta operation)) (:arglists api)))
      (is (= :none (:admission api)))
      (is (true? (:hosted-seed-only? api)))
      (is (= :stage0-native-instruction-execution (:contract-boundary api))))))

(deftest sequence-retains-last-value-and-late-recursive-var-selection
  (is (nil? (b/execute-stage0-instructions native-plan {} [])))
  (is (= false (b/execute-stage0-instructions native-plan {} [(literal 1) (literal nil) (literal false)])))
  (let [events (atom []) token (Object.) env {'x token}
        late (fn [plan bindings instruction]
               (swap! events conj [:late plan bindings instruction]) token)]
    (with-redefs [b/execute-stage0-instruction
                  (fn [plan bindings instruction]
                    (swap! events conj [:first plan bindings instruction])
                    (alter-var-root #'b/execute-stage0-instruction (constantly late)) nil)]
      (is (identical? token (runtime/execute-native-stage0-instructions operations native-plan env [:a :b])))
      (is (= [[:first native-plan env :a] [:late native-plan env :b]] @events)))))

(deftest literal-quote-local-and-failure-identities-are-preserved
  (let [token (Object.) path (Object.) plan (assoc-in native-plan [:source :path] path)]
    (doseq [instruction [{:op :literal :value token} {:op :quote :value token} {:op :local :name 'x}]]
      (is (identical? token (b/execute-stage0-instruction plan {'x token} instruction))))
    (is (nil? (b/execute-stage0-instruction plan {'x nil} {:op :local :name 'x})))
    (is (= false (b/execute-stage0-instruction plan {'x false} {:op :local :name 'x})))
    (doseq [[instruction expected] [[{:op :local :name 'missing} "L2-UNKNOWN-SYMBOL"]
                                   [{:op :not-an-instruction} "L2-UNKNOWN-CORE-FORM"]]]
      (let [sentinel (ex-info "selected failure" {}) facts (atom nil)]
        (with-redefs [b/fail! (fn [id message data] (reset! facts [id message data]) (throw sentinel))]
          (is (identical? sentinel (observed-error #(b/execute-stage0-instruction plan {} instruction)))))
        (is (= expected (first @facts)))
        (is (identical? path (get-in @facts [2 :source-span :source])))))))

(deftest collection-traversal-preserves-order-and-host-results
  (let [events (atom []) trace (fn [_ function args]
                                (swap! events conj [function args]) (first args))]
    (with-redefs [b/invoke-stage0-builtin trace]
      (is (= [:a :b] (b/execute-stage0-instruction native-plan {} {:op :vector-literal :items [(call 'a :a) (call 'b :b)]})))
      (is (= [[:a [:a]] [:b [:b]]] (mapv (fn [[name args]] [(keyword name) args]) @events)))
      (reset! events [])
      (is (= #{:same} (b/execute-stage0-instruction native-plan {} {:op :set-literal :items [(call 'a :same) (call 'b :same)]})))
      (is (= ['a 'b] (mapv first @events)))
      (reset! events [])
      (is (= {:key :last}
             (b/execute-stage0-instruction native-plan {}
               {:op :map-literal :entries [{:key (call 'key1 :key) :value (call 'value1 :first)}
                                          {:key (call 'key2 :key) :value (call 'value2 :last)}]})))
      (is (= ['key1 'value1 'key2 'value2] (mapv first @events))))))

(deftest binding-and-selected-branch-order-are-preserved
  (let [events (atom []) instruction
        {:op :let :bindings [{:name 'x :expr (call 'first 4)}
                             {:name 'y :expr {:op :builtin-call :function 'second :args [{:op :local :name 'x}]}}]
         :body [{:op :do :body [(literal nil) {:op :local :name 'y}]}]}]
    (with-redefs [b/invoke-stage0-builtin (fn [_ name args] (swap! events conj [name args]) (inc (first args)))]
      (is (= 6 (b/execute-stage0-instruction native-plan {} instruction)))
      (is (= [['first [4]] ['second [5]]] @events)))
    (doseq [[condition expected] [[nil :else] [false :else] [0 :then] [[] :then]]]
      (is (= expected (b/execute-stage0-instruction native-plan {}
                       {:op :if :test (literal condition) :then (literal :then) :else (literal :else)}))))
    (is (= :chosen (b/execute-stage0-instruction native-plan {}
                    {:op :if :test (literal true) :then (literal :chosen) :else {:op :unknown}})))))

(deftest builtin-callee-is-selected-after-argument-evaluation
  (let [events (atom []) late (fn [_ name args] (swap! events conj [:late name args]) :result)
        original b/execute-stage0-instruction]
    (with-redefs [b/invoke-stage0-builtin (fn [& _] (throw (Exception. "stale builtin")))
                  b/execute-stage0-instruction
                  (fn [plan env instruction]
                    (if (= :argument (:op instruction))
                      (do (swap! events conj :argument)
                          (alter-var-root #'b/invoke-stage0-builtin (constantly late)) :value)
                      (original plan env instruction)))]
      (is (= :result (runtime/execute-native-stage0-instruction operations native-plan {}
                      {:op :builtin-call :function 'selected :args [{:op :argument}]})))
      (is (= [:argument [:late 'selected [:value]]] @events)))))

(deftest function-callee-is-selected-after-argument-evaluation
  (let [events (atom []) late (fn [plan name args] (swap! events conj [plan name args]) :result)]
    (with-redefs [b/execute-stage0-compiled-function (fn [& _] (throw (Exception. "stale function")))
                  b/execute-stage0-instruction
                  (fn [_ _ _] (swap! events conj :argument)
                    (alter-var-root #'b/execute-stage0-compiled-function (constantly late)) :value)]
      (is (= :result (runtime/execute-native-stage0-instruction operations native-plan {}
                      {:op :function-call :function 'selected :args [:argument]})))
      (is (= [:argument [native-plan 'selected [:value]]] @events)))))

(deftest println-evaluates-arguments-before-policy-and-preserves-denial
  (let [events (atom []) sentinel (ex-info "effect denied" {})]
    (with-redefs [b/execute-stage0-instruction (fn [_ _ instruction] (swap! events conj instruction) :value)
                  b/validate-module-effects! (fn [module] (swap! events conj [:effects module]) (throw sentinel))]
      (is (= "" (with-out-str
                   (is (identical? sentinel
                         (observed-error #(runtime/execute-native-stage0-instruction operations native-plan {}
                                            {:op :println :args [:a :b]})))))))
      (is (= [:a :b [:effects (:module native-plan)]] @events)))))

(deftest println-retains-join-selection-and-existing-writer-capture
  (let [events (atom []) late (fn [& _] (swap! events conj :late) "late")
        carrier (proxy [Object] [] (toString [] (alter-var-root #'str/join (constantly late)) "carrier"))
        original str/join]
    (with-redefs [b/validate-module-effects! (fn [_] nil)
                  str/join (fn [separator values] (swap! events conj :selected) (original separator values))]
      (is (= "carrier\nlate\n"
             (with-out-str
               (b/execute-stage0-instruction native-plan {} {:op :println :args [(literal carrier)]})
               (b/execute-stage0-instruction native-plan {} {:op :println :args [(literal "after")]}))))
      (is (= [:selected :late] @events)))))

(deftest function-binding-arity-and-sequence-port-are-preserved
  (let [token (Object.) plan (assoc native-plan :functions {'identity {:params ['x] :instructions [{:op :local :name 'x}]}})]
    (is (identical? token (b/execute-stage0-compiled-function plan 'identity [token])))
    (is (= "L2-FUNCTION-ARITY" (:id (ex-data (observed-error #(b/execute-stage0-compiled-function plan 'identity []))))))
    (is (= "L2-UNKNOWN-CORE-FORM" (:id (ex-data (observed-error #(b/execute-stage0-compiled-function plan 'missing []))))))
    (with-redefs [b/execute-stage0-instructions (fn [input env instructions]
                                               (is (identical? plan input))
                                               (is (identical? token (get env 'x)))
                                               (is (= [{:op :local :name 'x}] instructions)) :selected)]
      (is (= :selected (runtime/execute-native-stage0-function operations plan 'identity [token]))))))

(deftest malformed-native-entry-and-returning-failures-remain-compatibility-data
  (doseq [[plan expected] [[native-plan "L3-UNKNOWN-ALIAS"]
                           [(assoc native-plan :functions {'main {:params ['x] :instructions []}}) "L2-MAIN-ARITY"]]]
    (is (= expected (:id (ex-data (observed-error #(b/execute-stage0-compiled-plan plan))))))
    (let [ids (atom [])]
      (with-redefs [b/fail! (fn [id & _] (swap! ids conj id) false)]
        (is (= "" (b/execute-stage0-compiled-plan plan)))
        (is (= [expected] @ids)))))
  (let [ids (atom [])]
    (with-redefs [b/fail-call-arity! (fn [id & _] (swap! ids conj id) false)]
      (is (= :first (b/execute-stage0-compiled-function
                     (assoc native-plan :functions {'f {:params ['x] :instructions [{:op :local :name 'x}]}})
                     'f [:first :extra])))
      (is (= ["L2-FUNCTION-ARITY"] @ids)))))

(deftest real-source-execution-preserves-plan-provenance-and-stdout
  (doseq [path ["examples/hello.gravity" "examples/hello.qst" "examples/core-app.gravity" "examples/core-app.qst"]]
    (testing path
      (let [source (slurp path :encoding "UTF-8") plan (compile-source path source)
            original-id (:plan-id plan)]
        (is (= path (get-in plan [:source :path])))
        (is (= (str "sha256:" (digest/sha256-hex source)) (get-in plan [:source :sha256])))
        (is (= (if (.contains path "core-app") "core-app\ngravity:19:2\n(:ok 19)\n" "Hello Gravity\n")
               (b/execute-stage0-compiled-plan plan)))
        (is (= original-id (:plan-id plan)))
        (is (= original-id (str "sha256:" (digest/sha256-hex (pr-str (dissoc plan :plan-id))))))))))

(deftest real-arity-refusals-follow-genuine-plan-emission
  (doseq [[path expected] [["bootstrap/clojure/fixtures/rejected/core-app-function-arity.gravity" "L2-FUNCTION-ARITY"]
                           ["bootstrap/clojure/fixtures/rejected/core-app-builtin-arity.gravity" "L2-BUILTIN-ARITY"]]]
    (let [plan (compile-source path (slurp path :encoding "UTF-8"))
          error (observed-error #(b/execute-stage0-compiled-plan plan))]
      (is (= :gravity/stage0-hosted-core-compiled-plan (:kind plan)))
      (is (= expected (:id (ex-data error))))
      (is (= path (get-in (ex-data error) [:source-span :source]))))))
