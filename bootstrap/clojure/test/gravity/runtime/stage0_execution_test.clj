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
(def ^:private builtin-operations
  {:assert-min-arity! #'b/assert-min-arity! :assert-exact-arity! #'b/assert-exact-arity!
   :assert-between-arity! #'b/assert-between-arity! :assert-even-arity! #'b/assert-even-arity!
   :fail-call-arity! #'b/fail-call-arity! :readable-pr-str #'b/p15-s23-seed-readable-pr-str
   :normalized-rest #'b/p15-s23-seed-readable-normalized-rest :fail! #'b/fail!})
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
    (is (= 54 (count (ns-publics 'gravity.runtime)) (count (:public-api contract))))
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

(deftest ordinary-stage0-builtin-contract-has-its-own-eight-ports
  (let [contract (:gravity/namespace-contract (meta #'runtime/invoke-native-stage0-builtin))
        api (get-in contract [:public-api 'invoke-native-stage0-builtin])]
    (is (= '{:arglists ([operations module callee args]) :admission :none :hosted-seed-only? true
             :contract-boundary :stage0-native-ordinary-builtin-invocation
             :input-boundary :trusted-native-stage0-module-and-evaluated-arguments
             :operation-keys [:assert-min-arity! :assert-exact-arity! :assert-between-arity! :assert-even-arity!
                              :fail-call-arity! :readable-pr-str :normalized-rest :fail!]
             :operation-selection :late-owner-var-before-call-arguments
             :argument-evaluation-owner :existing-caller :failure-transport-owner :supplied-operation}
           api))
    (is (= (:arglists (meta #'runtime/invoke-native-stage0-builtin)) (:arglists api)))
    (is (= (set (:operation-keys api)) (set (keys builtin-operations))))
    (is (every? var? (vals builtin-operations)))
    (is (false? (:authenticates-plans? contract)))
    (is (false? (:grants-authority? contract)))
    (is (some #{:stage0-build-read-declared-input} (:unsupported contract)))))

(deftest ordinary-stage0-builtin-arms-preserve-native-values
  (let [module {:source-path "builtin-native.gravity"}
        cases [['+ [] 0] ['+ [1 2 3] 6] ['- [8] -8] ['* [] 1] ['/ [12 3] 4]
               ['= [nil nil] true] ['< [1 2 3] true] ['> [3 2] true]
               ['<= [1 1] true] ['>= [2 2] true] ['str [nil false "x"] "falsex"]
               ['pr-str ['(a b)] "(a b)"] ['hash-map [:a 1] {:a 1}]
               ['vector [:a nil] [:a nil]] ['list [1 2] '(1 2)] ['conj [[1] 2] [1 2]]
               ['assoc [{} :a 1] {:a 1}] ['get [{:a false} :a] false]
               ['get [{} :absent :missing] :missing] ['first [[1 2]] 1]
               ['second [[1 2]] 2] ['rest [[1 2 3]] '(2 3)] ['count [[1 2]] 2]]]
    (is (= '#{+ - * / = < > <= >= str pr-str hash-map vector list conj assoc get first second rest count}
           (set (map first cases))))
    (doseq [[callee args expected] cases]
      (is (= expected (runtime/invoke-native-stage0-builtin builtin-operations module callee args))
          (str callee " " args)))
    (is (= clojure.lang.PersistentList
           (class (runtime/invoke-native-stage0-builtin builtin-operations module 'rest [[1 2 3]]))))))

(deftest ordinary-stage0-builtin-arity-and-host-error-facts-stay-local
  (let [path (Object.) module {:source-path path}]
    (doseq [[callee args] [['- []] ['/ []] ['= []] ['< [1]] ['> [1]] ['<= [1]] ['>= [1]]
                           ['hash-map [:odd]] ['conj []] ['assoc [{} :key]] ['assoc [{} :x 1 :y]]
                           ['get []] ['get [{} :a :b :c]] ['first []] ['second []] ['rest []] ['count []]]]
      (let [error (observed-error #(runtime/invoke-native-stage0-builtin builtin-operations module callee args))]
        (is (= "L2-BUILTIN-ARITY" (:id (ex-data error))))
        (is (= callee (:function (ex-data error))))
        (is (identical? path (get-in (ex-data error) [:source-span :source])))))
    (doseq [[callee args] [['/ [1 0]] ['count [42]] ['build-read-declared-input []]]]
      (let [error (observed-error #(runtime/invoke-native-stage0-builtin builtin-operations module callee args))]
        (is (= "L2-BUILTIN-ERROR" (:id (ex-data error))))
        (is (= callee (:function (ex-data error))))
        (is (identical? path (get-in (ex-data error) [:source-span :source])))))))

(deftest ordinary-stage0-owner-exceptions-and-returning-failures-are-preserved
  (let [module {:source-path "builtin-errors.gravity"} sentinel (ex-info "owner" {:opaque :owner})
        fatal (Error. "fatal owner")]
    (doseq [error [sentinel fatal]]
      (with-redefs [b/assert-min-arity! (fn [& _] (throw error))
                    b/fail! (fn [& _] (throw (Exception. "unexpected remap")))]
        (is (identical? error
                       (observed-error #(runtime/invoke-native-stage0-builtin builtin-operations module '- [1]))))))
    (let [events (atom [])]
      (with-redefs [b/assert-min-arity! (fn [& _] (throw (Exception. "ordinary owner")))
                    b/fail! (fn [id message data] (swap! events conj [id message data]) (throw sentinel))]
        (is (identical? sentinel
                       (observed-error #(runtime/invoke-native-stage0-builtin builtin-operations module '- [1])))))
      (is (= 1 (count @events)))
      (is (= "L2-BUILTIN-ERROR" (ffirst @events)))
      (is (= "ordinary owner" (get-in @events [0 2 :cause-message]))))
    (let [events (atom []) token (Object.)]
      (with-redefs [b/fail-call-arity! (fn [& _] (swap! events conj :arity) false)
                    b/fail! (fn [id & _] (swap! events conj id) token)]
        (is (identical? token
                       (runtime/invoke-native-stage0-builtin builtin-operations module 'assoc [{} :x 1 :y])))
        (is (= [:arity "L2-BUILTIN-ERROR"] @events))))))

(deftest ordinary-stage0-formatter-is-selected-before-source-lookup
  (let [events (atom []) path (Object.) token (Object.)
        late (fn [& _] (swap! events conj :late) :late)
        source-lookup (fn [key not-found]
                        (if (= :source-path key)
                          (do (swap! events conj :source-path)
                              (alter-var-root #'b/p15-s23-seed-readable-pr-str (constantly late)) path)
                          not-found))
        module (reify clojure.lang.ILookup
                 (valAt [_ key] (source-lookup key nil))
                 (valAt [_ key not-found] (source-lookup key not-found)))
        lookup (fn [key not-found] (swap! events conj key) (get builtin-operations key not-found))
        ports (reify clojure.lang.ILookup
                (valAt [_ key] (lookup key nil))
                (valAt [_ key not-found] (lookup key not-found)))]
    (with-redefs [b/p15-s23-seed-readable-pr-str
                  (fn [input values]
                    (swap! events conj :selected)
                    (is (identical? path input)) (is (= [:value] values)) token)]
      (is (= 3 (runtime/invoke-native-stage0-builtin ports module '+ [1 2])))
      (is (empty? @events))
      (is (identical? token (runtime/invoke-native-stage0-builtin ports module 'pr-str [:value])))
      (is (= [:readable-pr-str :source-path :selected] @events))
      (reset! events [])
      (is (= :late (runtime/invoke-native-stage0-builtin ports module 'pr-str [:after])))
      (is (= [:readable-pr-str :source-path :late] @events)))))

(deftest ordinary-stage0-error-owner-is-selected-before-fact-construction
  (let [events (atom []) path (Object.) token (Object.)
        late (fn [& _] (swap! events conj :late) :late)
        lookup (fn [key not-found]
                 (if (= :source-path key)
                   (do (swap! events conj :source-path)
                       (alter-var-root #'b/fail! (constantly late)) path)
                   not-found))
        module (reify clojure.lang.ILookup
                 (valAt [_ key] (lookup key nil))
                 (valAt [_ key not-found] (lookup key not-found)))]
    (with-redefs [b/fail! (fn [id _ data]
                           (swap! events conj :selected)
                           (is (= "L2-BUILTIN-ERROR" id))
                           (is (identical? path (get-in data [:source-span :source]))) token)]
      (is (identical? token
                     (runtime/invoke-native-stage0-builtin builtin-operations module 'unknown-builtin [])))
      (is (= [:source-path :selected] @events))
      (reset! events [])
      (is (= :late (runtime/invoke-native-stage0-builtin builtin-operations module 'unknown-builtin [])))
      (is (= [:source-path :late] @events)))))

(deftest ^{:runtime :managed :host :temporary-clojure-bootstrap :profile :hosted :target :jvm
           :artifact :native-stage0-call-arity}
  native-stage0-arity-contract-is-explicit-and-distinct
  (let [contract (:gravity/namespace-contract (meta #'runtime/fail-native-stage0-call-arity!))]
    (doseq [[operation args port]
            [[#'runtime/fail-native-stage0-call-arity! '[fail-var id module callee args expected] 'fail-var]
             [#'runtime/assert-native-stage0-min-arity! '[fail-call-arity-var module callee args n] 'fail-call-arity-var]
             [#'runtime/assert-native-stage0-exact-arity! '[fail-call-arity-var module callee args n] 'fail-call-arity-var]
             [#'runtime/assert-native-stage0-between-arity! '[fail-call-arity-var module callee args min-n max-n] 'fail-call-arity-var]
             [#'runtime/assert-native-stage0-even-arity! '[fail-call-arity-var module callee args] 'fail-call-arity-var]]]
      (let [api (get-in contract [:public-api (:name (meta operation))])]
        (is (= (list args) (:arglists (meta operation)) (:arglists api)))
        (is (= :stage0-native-call-arity (:contract-boundary api)))
        (is (= :trusted-native-stage0-module-and-call-signature (:input-boundary api)))
        (is (= [port] (:operation-vars api)))
        (is (= :late-owner-var-before-call-arguments (:operation-selection api)))
        (is (= :nil-or-supplied-owner-return-or-unchanged-failure (:result-boundary api)))
        (is (= :none (:admission api)))
        (is (true? (:hosted-seed-only? api)))))
    (is (= :supplied-operation (get-in contract [:public-api 'fail-native-stage0-call-arity! :failure-transport-owner])))
    (is (false? (:authenticates-plans? contract)))
    (is (false? (:grants-authority? contract)))))

(deftest ^{:runtime :managed :host :temporary-clojure-bootstrap :profile :hosted :target :jvm
           :artifact :native-stage0-call-arity}
  native-stage0-arity-predicates-preserve-validity-and-returned-refusals
  (doseq [size (range 6)
          [guard parameters valid? expected]
          [[runtime/assert-native-stage0-min-arity! [2] (<= 2 size) "at least 2"]
           [runtime/assert-native-stage0-exact-arity! [2] (= 2 size) 2]
           [runtime/assert-native-stage0-between-arity! [2 3] (<= 2 size 3) "2 to 3"]
           [runtime/assert-native-stage0-even-arity! [] (even? size) "an even number of arguments"]]]
    (let [calls (atom []) token (Object.) module (Object.) args (vec (range size))
          failure (clojure.lang.Var/create (fn [& values] (swap! calls conj (vec values)) token))
          result (apply guard failure module 'native args parameters)]
      (if valid?
        (do (is (nil? result)) (is (empty? @calls))
            (is (nil? (apply guard nil module 'native args parameters))))
        (do (is (identical? token result))
            (is (= [["L2-BUILTIN-ARITY" module 'native args expected]] @calls)))))))

(deftest ^{:runtime :managed :host :temporary-clojure-bootstrap :profile :hosted :target :jvm
           :artifact :native-stage0-call-arity}
  native-stage0-arity-facts-use-the-module-path-and-exact-messages
  (let [path (Object.) expected (Object.) token (Object.) args [:a :b]
        module {:source-path path :source {:path :wrong-stage2-carrier}}]
    (doseq [[id message] [["L2-FUNCTION-ARITY" "stage0 function call has the wrong arity"]
                          ["L2-BUILTIN-ARITY" "stage0 builtin call has the wrong arity"]
                          ["other-id" "stage0 call has the wrong arity"]]]
      (let [calls (atom []) failure (clojure.lang.Var/create
                                    (fn [& values] (swap! calls conj (vec values)) token))]
        (is (identical? token (runtime/fail-native-stage0-call-arity! failure id module 'native args expected)))
        (is (= [[id message {:source-span {:source path} :function 'native
                            :expected-arity expected :actual-arity 2
                            :remediation "Call the function with the arity supported by the stage0 hosted subset."}]]
               @calls))
        (is (identical? path (get-in @calls [0 2 :source-span :source])))
        (is (identical? expected (get-in @calls [0 2 :expected-arity])))))))

(deftest ^{:runtime :managed :host :temporary-clojure-bootstrap :profile :hosted :target :jvm
           :artifact :native-stage0-call-arity}
  native-stage0-arity-ports-retain-count-and-fact-construction-order
  (let [events (atom []) failure (clojure.lang.Var/create (fn [& _] :old))
        args (reify clojure.lang.Counted
               (count [_]
                 (swap! events conj :count)
                 (alter-var-root failure (constantly (fn [& values] (swap! events conj values) :new))) 0))]
    (is (= :new (runtime/assert-native-stage0-min-arity! failure {} 'native args 1)))
    (is (= :count (first @events)))
    (is (= ["L2-BUILTIN-ARITY" {} 'native args "at least 1"] (vec (second @events)))))
  (let [events (atom []) token (Object.) failure (clojure.lang.Var/create nil)
        late (fn [& _] (swap! events conj :late) :late)
        lookup (fn [key not-found]
                 (if (= :source-path key)
                   (do (swap! events conj :source-path)
                       (alter-var-root failure (constantly late)) :retained-path) not-found))
        module (reify clojure.lang.ILookup
                 (valAt [_ key] (lookup key nil))
                 (valAt [_ key not-found] (lookup key not-found)))
        args (reify clojure.lang.Counted (count [_] (swap! events conj :count) 3))]
    (alter-var-root failure (constantly (fn [_ _ facts]
                                         (swap! events conj :selected)
                                         (is (= {:source :retained-path} (:source-span facts)))
                                         (is (= 3 (:actual-arity facts))) token)))
    (is (identical? token (runtime/fail-native-stage0-call-arity! failure "L2-BUILTIN-ARITY" module 'native args 2)))
    (is (= [:source-path :count :selected] @events))
    (reset! events [])
    (is (= :late (runtime/fail-native-stage0-call-arity! failure "L2-BUILTIN-ARITY" module 'native args 2)))
    (is (= [:source-path :count :late] @events))))

(deftest ^{:runtime :managed :host :temporary-clojure-bootstrap :profile :hosted :target :jvm
           :artifact :native-stage0-call-arity}
  native-stage0-arity-adapters-preserve-signatures-and-interposition
  (doseq [[legacy stage args owner]
          [[#'b/fail-call-arity! #'runtime/fail-native-stage0-call-arity! '[id module callee args expected] #'b/fail!]
           [#'b/assert-min-arity! #'runtime/assert-native-stage0-min-arity! '[module callee args n] #'b/fail-call-arity!]
           [#'b/assert-exact-arity! #'runtime/assert-native-stage0-exact-arity! '[module callee args n] #'b/fail-call-arity!]
           [#'b/assert-between-arity! #'runtime/assert-native-stage0-between-arity! '[module callee args min-n max-n] #'b/fail-call-arity!]
           [#'b/assert-even-arity! #'runtime/assert-native-stage0-even-arity! '[module callee args] #'b/fail-call-arity!]]]
    (let [values (mapv (fn [_] (Object.)) args) calls (atom []) token (Object.) error (ex-info "owner" {:id :owner})]
      (is (= (list args) (:arglists (meta legacy))))
      (is (not (:private (meta legacy))))
      (with-redefs-fn {stage (fn [& actual] (swap! calls conj (vec actual)) token)}
        #(is (identical? token (apply @legacy values))))
      (is (= [(into [owner] values)] @calls))
      (with-redefs-fn {stage (fn [& _] (throw error))}
        #(is (identical? error (observed-error (fn [] (apply @legacy values)))))))))

(deftest ^{:runtime :managed :host :temporary-clojure-bootstrap :profile :hosted :target :jvm
           :artifact :native-stage0-call-arity}
  native-stage0-arity-owner-failures-propagate-without-remapping
  (doseq [error [(ex-info "info" {:id :native-info}) (Exception. "host") (Error. "fatal")]]
    (let [calls (atom 0) failure (clojure.lang.Var/create (fn [& _] (swap! calls inc) (throw error)))]
      (doseq [[guard parameters]
              [[runtime/assert-native-stage0-min-arity! [1]]
               [runtime/assert-native-stage0-exact-arity! [1]]
               [runtime/assert-native-stage0-between-arity! [1 2]]
               [runtime/assert-native-stage0-even-arity! []]]]
        (let [args (if (= guard runtime/assert-native-stage0-even-arity!) [:odd] [])]
          (is (identical? error (observed-error #(apply guard failure {} 'native args parameters))))))
      (is (identical? error
                     (observed-error #(runtime/fail-native-stage0-call-arity! failure "L2-FUNCTION-ARITY" {} 'native [] 1))))
      (is (= 5 @calls)))))

(def ^:private interpreter-operations
  {:fail! #'b/fail! :validate-module-effects! #'b/validate-module-effects!
   :eval-expr #'b/eval-expr :eval-do #'b/eval-do :bind-let #'b/bind-let
   :stage0-builtin-functions #'b/stage0-builtin-functions
   :invoke-stage0-builtin #'b/invoke-stage0-builtin :invoke-stage0-function #'b/invoke-stage0-function
   :eval-collection #'b/eval-collection :join #'str/join})

(deftest ^{:runtime :managed :host :temporary-clojure-bootstrap :profile :hosted :target :jvm
           :artifact :native-stage0-interpretation}
  native-interpreter-contract-is-explicit
  (let [names '[eval-native-stage0-do bind-native-stage0-let invoke-native-stage0-function
                eval-native-stage0-collection eval-native-stage0-expr run-native-stage0-main]
        contract (:gravity/namespace-contract (meta #'runtime/eval-native-stage0-expr))]
    (is (= 54 (count (ns-publics 'gravity.runtime)) (count (:public-api contract))))
    (is (= (set (keys interpreter-operations))
           (set (get-in contract [:public-api 'eval-native-stage0-expr :operation-keys]))))
    (is (= [:stage0-builtin-functions]
           (get-in contract [:public-api 'eval-native-stage0-expr :data-operation-keys])))
    (is (every? var? (vals interpreter-operations)))
    (doseq [name names :let [operation (ns-resolve 'gravity.runtime name)
                            api (get-in contract [:public-api name])]]
      (is (= (:arglists (meta operation)) (:arglists api)))
      (is (= :none (:admission api)))
      (is (true? (:hosted-seed-only? api)))
      (is (= :stage0-native-interpretation (:contract-boundary api))))))

(deftest ^{:runtime :managed :host :temporary-clojure-bootstrap :profile :hosted :target :jvm
           :artifact :native-stage0-interpretation}
  native-interpreter-sequence-and-bindings-retain-late-roots-and-lexical-order
  (is (nil? (runtime/eval-native-stage0-do nil {} {} [])))
  (is (= {'x nil 'y false 'z 3}
         (b/bind-let {} {} '[x nil y false z (if y 0 3)])))
  (is (= {'x 4 'y 5} (b/bind-let {} {'x 1} '[x (+ x 3) y (+ x 1)])))
  (let [events (atom []) token (Object.) module (Object.) env (Object.)
        late (fn [m e form] (swap! events conj [:late m e form]) token)]
    (with-redefs [b/eval-expr (fn [m e form]
                              (swap! events conj [:first m e form])
                              (alter-var-root #'b/eval-expr (constantly late)) nil)]
      (is (identical? token (runtime/eval-native-stage0-do #'b/eval-expr module env [:a :b])))
      (is (= [[:first module env :a] [:late module env :b]] @events))))
  (doseq [bindings ['(x 1) '[x] '[1 2]]]
    (let [error (observed-error #(b/bind-let {:source-path "let.gravity"} {} bindings))]
      (is (= "L2-LET-BINDING" (:id (ex-data error))))
      (is (= "let.gravity" (get-in (ex-data error) [:source-span :source]))))))

(deftest ^{:runtime :managed :host :temporary-clojure-bootstrap :profile :hosted :target :jvm
           :artifact :native-stage0-interpretation}
  native-interpreter-collections-and-control-retain-native-results-and-order
  (let [events (atom []) module (Object.) env (Object.)
        evaluate (fn [m e value] (swap! events conj [m e value]) value)]
    (with-redefs [b/eval-expr evaluate]
      (is (= [:a :b] (b/eval-collection module env [:a :b])))
      (is (= [:a :b] (mapv #(nth % 2) @events)))
      (reset! events [])
      (is (= {:a 1 :b 2} (b/eval-collection module env (array-map :a 1 :b 2))))
      (is (= [:a 1 :b 2] (mapv #(nth % 2) @events)))
      (is (every? #(and (identical? module (first %)) (identical? env (second %))) @events))
      (is (= #{:a :b} (b/eval-collection module env #{:a :b}))))
    (is (identical? module (runtime/eval-native-stage0-collection nil {} {} module))))
  (is (nil? (b/eval-expr {} {'x nil} 'x)))
  (is (false? (b/eval-expr {} {'x false} 'x)))
  (is (= '(unresolved :data) (b/eval-expr {} {} '(quote (unresolved :data)))))
  (is (= :chosen (b/eval-expr {} {} '(if false (host-reflect) :chosen))))
  (is (= :chosen (b/eval-expr {} {} '(if true :chosen (/ 1 0)))))
  (is (= 3 (b/eval-expr {} {} '(do :ignored (let [a 1 b (+ a 2)] b))))))

(deftest ^{:runtime :managed :host :temporary-clojure-bootstrap :profile :hosted :target :jvm
           :artifact :native-stage0-interpretation}
  native-interpreter-call-catalog-and-println-ports-remain-late
  (let [events (atom []) token (Object.) module {:function-table {'chosen {}}}
        builtin (fn [m callee args] (swap! events conj [:builtin m callee args]) token)]
    (with-redefs [b/stage0-builtin-functions #{}
                  b/eval-expr (fn [_ _ value]
                                (swap! events conj [:argument value])
                                (alter-var-root #'b/stage0-builtin-functions (constantly #{'chosen}))
                                (alter-var-root #'b/invoke-stage0-builtin (constantly builtin)) value)
                  b/invoke-stage0-builtin (fn [& _] (throw (Exception. "early builtin")))
                  b/invoke-stage0-function (fn [& _] (throw (Exception. "wrong precedence")))]
      (is (identical? token (runtime/eval-native-stage0-expr interpreter-operations module {} '(chosen :a :b))))
      (is (= [[:argument :a] [:argument :b] [:builtin module 'chosen [:a :b]]] @events))))
  (let [events (atom []) module {:source-path "println.gravity"}]
    (with-redefs [b/validate-module-effects! (fn [m] (swap! events conj [:effects m]))
                  b/eval-expr (fn [_ _ value] (swap! events conj [:argument value]) value)]
      (is (= "a b\n" (with-out-str (runtime/eval-native-stage0-expr interpreter-operations module {} '(println "a" "b")))))
      (is (= [[:effects module] [:argument "a"] [:argument "b"]] @events)))))

(deftest ^{:runtime :managed :host :temporary-clojure-bootstrap :profile :hosted :target :jvm
           :artifact :native-stage0-interpretation}
  native-interpreter-entry-calls-and-failure-facts-preserve-identities
  (let [path (Object.) token (Object.) facts (atom nil)
        module {:source-path path :function-table {'f {:params '[x] :body [:body]}}}]
    (with-redefs [b/fail! (fn [id message data] (reset! facts [id message data]) token)]
      (doseq [[form id] [['missing "L2-UNKNOWN-SYMBOL"] ['(missing) "L2-UNKNOWN-CORE-FORM"]
                        ['(host-reflect) "P4-HOST-REFLECTION"]]]
        (is (identical? token (b/eval-expr module {} form)))
        (is (= id (first @facts)))
        (is (identical? path (get-in @facts [2 :source-span :source])))))
    (with-redefs [b/fail-call-arity! (fn [& args] (reset! facts args) :returned)
                  b/eval-do (fn [m env body]
                              (is (identical? module m)) (is (= {} env))
                              (is (= [:body] body)) token)]
      (is (identical? token (b/invoke-stage0-function module 'f [])))
      (is (= ["L2-FUNCTION-ARITY" module 'f [] 1] @facts))))
  (let [module {:source-path "main.gravity"} events (atom [])]
    (with-redefs [b/stage0-function-table (fn [m] (swap! events conj [:table m]) {'main {:params [] :body [:body]}})
                  b/eval-do (fn [m env body]
                              (swap! events conj [:body m env body]) (println "done"))]
      (is (= "done\n" (b/run-main module)))
      (is (= [[:table module] [:body (assoc module :function-table {'main {:params [] :body [:body]}}) {} [:body]]]
             @events)))
    (doseq [[table id] [[{} "L3-UNKNOWN-ALIAS"] [{'main {:params '[x] :body [1]}} "L2-MAIN-ARITY"]]]
      (with-redefs [b/stage0-function-table (fn [_] table)]
        (is (= id (:id (ex-data (observed-error #(b/run-main module))))))))))

(deftest ^{:runtime :managed :host :temporary-clojure-bootstrap :profile :hosted :target :jvm
           :artifact :native-stage0-interpretation}
  native-interpreter-owner-exceptions-propagate-without-remapping
  (doseq [error [(ex-info "info" {:id :owner}) (Exception. "host") (Error. "fatal")]
          :let [failure (clojure.lang.Var/create (fn [& _] (throw error)))]]
    (doseq [run [#(runtime/eval-native-stage0-do failure {} {} [1])
                 #(runtime/bind-native-stage0-let failure nil {} {} '[x])
                 #(runtime/invoke-native-stage0-function failure nil {:function-table {'f {:params '[x]}}} 'f [])
                 #(runtime/eval-native-stage0-collection failure {} {} [1])
                 #(runtime/eval-native-stage0-expr (assoc interpreter-operations :fail! failure) {} {} 'missing)
                 #(runtime/run-native-stage0-main failure nil nil {})]]
      (is (identical? error (observed-error run))))))

(deftest ^{:runtime :managed :host :temporary-clojure-bootstrap :profile :hosted :target :jvm
           :artifact :native-stage0-interpretation}
  native-interpreter-bootstrap-signatures-remain-thin-adapters
  (doseq [[legacy stage owners args]
          [[#'b/eval-do #'runtime/eval-native-stage0-do [#'b/eval-expr] '[module env forms]]
           [#'b/bind-let #'runtime/bind-native-stage0-let [#'b/fail! #'b/eval-expr] '[module env bindings]]
           [#'b/invoke-stage0-function #'runtime/invoke-native-stage0-function [#'b/fail-call-arity! #'b/eval-do] '[module callee args]]
           [#'b/eval-collection #'runtime/eval-native-stage0-collection [#'b/eval-expr] '[module env form]]
           [#'b/eval-expr #'runtime/eval-native-stage0-expr [interpreter-operations] '[module env form]]
           [#'b/run-main #'runtime/run-native-stage0-main [#'b/stage0-function-table #'b/fail! #'b/eval-do] '[module]]]
          :let [values (mapv (fn [_] (Object.)) args) token (Object.) calls (atom [])]]
    (is (= (list args) (:arglists (meta legacy))))
    (with-redefs-fn {stage (fn [& actual] (swap! calls conj (vec actual)) token)}
      #(is (identical? token (apply @legacy values))))
    (is (= [(into owners values)] @calls))))

(deftest ^{:runtime :managed :host :temporary-clojure-bootstrap :profile :hosted :target :jvm
           :artifact :native-stage0-interpretation}
  native-interpreter-real-source-compilation-and-refusal-retain-provenance
  (doseq [extension [".gravity" ".qst"]
          [body expected] [["(defn f [x] (+ x 2)) (defn main [] (println (let [a 1 b (f a)] (if false 0 b))))" "3\n"]
                           ["(defn f [x] x) (defn main [] (println (f)))" "L2-FUNCTION-ARITY"]]]
    (let [path (str "interpreter" extension)
          source (str "(ns interpreter (:profile :hosted) (:target :jvm) (:effects #{:io/write}) (:capabilities #{:io/stdout}))\n" body)
          _ (is (nil? (var-get #'b/*authenticated-source-form-records*)))
          artifact (b/macro-source-artifact path source)
          module (assoc (:module artifact) :forms (:expanded-forms artifact))
          plan (b/stage0-compiled-core-plan path source module)]
      (is (= path (get-in plan [:source :path])))
      (is (= (str "sha256:" (digest/sha256-hex source)) (get-in plan [:source :sha256])))
      (is (= (:plan-id plan) (str "sha256:" (digest/sha256-hex (pr-str (dissoc plan :plan-id))))))
      (if (= "3\n" expected)
        (is (= expected (b/run-main module) (b/execute-stage0-compiled-plan plan)))
        (let [error (observed-error #(b/run-main module))]
          (is (= expected (:id (ex-data error))))
          (is (= path (get-in (ex-data error) [:source-span :source]))))))))
