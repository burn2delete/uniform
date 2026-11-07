(ns gravity.types.pipeline-test
  "Native annotation premise checks; compatibility is not canonical type proof."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.types :as types]))

(def ^:private node
  (with-meta {:node-id "annotation-node"
              :source-span {:source "annotations.gravity" :start {:line 3 :column 2}}
              :generated-origin [{:kind :macro :parent "annotation-parent"}]
              :unsafe-island :preserved :runtime-checked true :ai/human-review :preserved}
    {:source-owner :preserved}))

(defn- ports []
  {:diagnostic! #'b/typed-diagnostic! :type-keywords #'b/type-keywords
   :type-token-name #'b/type-token-name :literal-type #'b/literal-type
   :compatible-value-type? #'b/compatible-value-type?})

(defn- annotation [type] {:type "Keyword" :value type})
(defn- fact [type value] {:type type :value value})
(defn- diagnostic [operation]
  (try (operation) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))
(defn- thrown [operation]
  (try (operation) nil (catch Throwable error error)))

(deftest native-contract-declares-checks-without-canonical-type-authority
  (let [contract (:gravity/namespace-contract (meta #'types/check-native-assert!))]
    (is (= 32 (count (ns-publics 'gravity.types)) (count (:public-api contract))))
    (doseq [name '[check-native-assert! check-native-value! check-native-return!]]
      (let [operation (ns-resolve 'gravity.types name) api (get-in contract [:public-api name])]
        (is (= (:arglists (meta operation)) (:arglists api)))
        (is (= contract (:gravity/namespace-contract (meta operation))))
        (is (= :none (:admission api)))
        (is (false? (:seed-compatibility-data-only? api)))
        (is (true? (:seed-compatibility-checks-only? api)))
        (is (false? (:authenticates-inputs? api)))
        (is (false? (:proves-canonical-types? api)))
        (is (false? (:grants-authority? api)))
        (is (every? (set (keys (ports))) (:operation-inputs api)))))
    (is (every? var? (vals (ports))))
    (is (some #{:integer-width-proof} (:unsupported contract)))
    (is (= [] (get-in contract [:dependency-direction :requires])))))

(deftest real-typed-core-and-all-three-native-source-checks-still-run
  (let [calls (atom {})
        original-assert b/check-typed-assert!
        original-value b/check-typed-value!
        original-return b/check-typed-return!
        path "bootstrap/clojure/fixtures/accepted/typed-core.gravity"
        text (str "(ns typed.annotations (:profile :hosted) (:target :jvm)"
                  " (:effects #{}) (:capabilities #{}) (:safety :safe))\n"
                  "(def asserted (typed/assert :Integer 42))\n"
                  "(def valued (typed/value :Integer 42))\n"
                  "(def returned (typed/return (quote Integer) 42))\n")
        products
        (with-redefs [b/check-typed-assert! (fn [& args]
                                              (swap! calls update :assert (fnil inc 0))
                                              (apply original-assert args))
                      b/check-typed-value! (fn [& args]
                                             (swap! calls update :value (fnil inc 0))
                                             (apply original-value args))
                      b/check-typed-return! (fn [& args]
                                              (swap! calls update :return (fnil inc 0))
                                              (apply original-return args))]
          [(b/typed-file-artifact path)
           (b/typed-source-artifact "annotations.gravity" text)
           (b/typed-source-artifact "annotations.qst" text)])]
    (is (every? pos? (map @calls [:assert :value :return])))
    (doseq [artifact products]
      (is (= :gravity/stage0-typed-core-artifact (:kind artifact)))
      (is (= :type-effect-capability-check (get-in artifact [:pass :name])))
      (is (seq (:type-facts artifact)))
      (is (every? :source-span (:type-facts artifact))))
    (is (= 'typed.core (get-in (first products) [:module :module])))
    (is (= (mapv :type (:type-facts (second products)))
           (mapv :type (:type-facts (nth products 2)))))
    (doseq [[artifact source] (map vector products [path "annotations.gravity" "annotations.qst"])]
      (is (every? #(= source (get-in % [:source-span :source])) (:type-facts artifact))))))

(deftest actual-source-refusals-retain-l5-identity-and-source-spans
  (let [path "bootstrap/clojure/fixtures/rejected/typed-type-mismatch.gravity"
        result (diagnostic #(b/typed-file-artifact path))]
    (is (= "L5-TYPE-MISMATCH" (:id result)))
    (is (= path (get-in result [:source-span :source])))
    (is (= "Integer" (:expected-type result)))
    (is (= "String" (:actual-type result))))
  (doseq [[expression expected]
          [["(typed/assert :Unknown 1)" "L5-ANNOTATION-REQUIRED"]
           ["(typed/value :Integer \"bad\")" "L5-TYPE-MISMATCH"]
           ["(typed/return (quote Integer) \"bad\")" "L5-TYPE-MISMATCH"]
           ["(typed/value :Integer)" "L5-ANNOTATION-REQUIRED"]
           ["(typed/return (quote Integer))" "L5-ANNOTATION-REQUIRED"]]]
    (let [text (str "(ns annotations.bad (:profile :hosted) (:target :jvm)"
                    " (:effects #{}) (:capabilities #{}) (:safety :safe))\n(def bad " expression ")\n")
          result (diagnostic #(b/typed-source-artifact "annotations-bad.qst" text))]
      (is (= expected (:id result)))
      (is (= :typed-effected-core (:analyzer-stage result)))
      (is (= "annotations-bad.qst" (get-in result [:source-span :source])))
      (is (string? (:remediation result))))))

(deftest paired-real-plans-retain-native-type-facts-and-deterministic-c-text
  (let [products
        (mapv (fn [extension]
                (let [path (.getCanonicalPath (io/file (str "examples/hello" extension)))
                      text (slurp path :encoding "UTF-8")
                      typed (b/typed-source-artifact path text)
                      actual (first (filter #(= "String" (:type %)) (:type-facts typed)))
                      emitter (:emitter (b/c-backend-stage2-plan-emitter-source-rule! path :jvm))
                      plan (b/p15-s23-stage2-compiler-artifact-plan emitter path text)
                      source-node (assoc node :source-span (:source-span actual))]
                  (is (some? actual))
                  (is (nil? (b/check-typed-assert! source-node [(annotation :String) actual])))
                  (is (= "L5-TYPE-MISMATCH"
                         (:id (diagnostic #(b/check-typed-assert!
                                            source-node [(annotation :Integer) actual])))))
                  (is (= :passed (b/c-backend-validate-runtime-plan! path :c plan)))
                  (let [source (b/c-backend-runtime-source plan)]
                    (is (str/includes? source "fwrite("))
                    (is (= source (b/c-backend-runtime-source plan)))
                    {:plan-id (:plan-id plan) :source source})))
              [".gravity" ".qst"])]
    (is (= (first products) (second products)))))

(deftest native-assert-value-and-return-retain-their-distinct-compatibility
  (let [operations (ports) before (binding [*print-meta* true] (pr-str node))]
    (is (nil? (types/check-native-assert! operations node [(annotation :Integer) (fact "Integer" 1)])))
    (is (= "Integer" (types/check-native-value! operations node [(annotation :Integer) (fact "QuotedData" 1)])))
    (is (= "Integer" (types/check-native-return! operations node [(fact "QuotedData" 'Integer) (fact "Integer" 1)])))
    (is (= "L5-TYPE-MISMATCH"
           (:id (diagnostic #(types/check-native-return!
                              operations node [(annotation :Integer) (fact "QuotedData" 1)])))))
    (is (= "L5-TYPE-MISMATCH"
           (:id (diagnostic #(types/check-native-assert!
                              operations node [(annotation :I64) (fact "Integer" 1)])))))
    ;; Existing compatibility accepts these categories without proving width,
    ;; non-returning behavior or nominal structure. Preserve, never certify it.
    (is (= "I8" (types/check-native-value! operations node [(annotation :I8) (fact "Integer" 100000N)])))
    (is (= "Bottom" (types/check-native-value! operations node [(annotation :Bottom) (fact "Nil" nil)])))
    (is (= "Struct[Vec3]" (types/check-native-value! operations node [(annotation :Struct) (fact "String" "not-a-struct")])))
    (is (= before (binding [*print-meta* true] (pr-str node))))))

(deftest compatibility-adapters-borrow-inputs-and-pass-live-owner-vars
  (doseq [[wrapper-var stage-var required]
          [[#'b/check-typed-assert! #'types/check-native-assert! #{:diagnostic! :type-keywords}]
           [#'b/check-typed-value! #'types/check-native-value! #{:diagnostic! :type-token-name :literal-type :compatible-value-type?}]
           [#'b/check-typed-return! #'types/check-native-return! #{:diagnostic! :type-token-name :compatible-value-type?}]]]
    (let [args (with-meta [(annotation :Integer) (fact "Integer" 1)] {:owner :borrowed})
          result (Object.)]
      (with-redefs-fn {stage-var (fn [operations actual-node actual-args]
                                   (is (= required (set (keys operations))))
                                   (is (every? var? (vals operations)))
                                   (is (identical? node actual-node))
                                   (is (identical? args actual-args))
                                   result)}
        #(is (identical? result (wrapper-var node args)))))
    (is (= '([node args]) (:arglists (meta wrapper-var))))))

(deftest native-checks-observe-late-catalog-and-function-roots
  (let [operations (ports)]
    (with-redefs [b/type-keywords {:Custom "String"}]
      (is (nil? (types/check-native-assert! operations node [(annotation :Custom) (fact "String" "s")]))))
    (with-redefs [b/type-token-name (constantly "Borrowed")
                  b/compatible-value-type? (fn [expected actual]
                                             (is (= ["Borrowed" "String"] [expected actual])) true)]
      (is (= "Borrowed" (types/check-native-value! operations node [(annotation :Integer) (fact "String" "s")]))))
    (with-redefs [b/literal-type (constantly "String")]
      (is (= "String" (types/check-native-value! operations node [(annotation :String) (fact "QuotedData" 42)]))))))

(deftest callable-and-catalog-selection-precede-argument-lookups
  (let [calls (atom [])
        value (reify clojure.lang.ILookup
                (valAt [_ key]
                  (case key
                    :type "QuotedData"
                    :value (do (swap! calls conj :value-lookup)
                               (alter-var-root #'b/literal-type (constantly (constantly "String"))) 1)
                    nil))
                (valAt [this key _] (.valAt this key)))]
    (with-redefs [b/literal-type (fn [_] (swap! calls conj :selected-original) "Integer")]
      (is (= "Integer" (types/check-native-value! (ports) node [(annotation :Integer) value]))))
    (is (= [:value-lookup :selected-original] @calls)))
  (let [token (reify clojure.lang.ILookup
                (valAt [_ key]
                  (case key
                    :type "Keyword"
                    :value (do (alter-var-root #'b/type-keywords (constantly {:Integer "String"})) :Integer)
                    nil))
                (valAt [this key _] (.valAt this key)))]
    (with-redefs [b/type-keywords {:Integer "Integer"}]
      (is (nil? (types/check-native-assert! (ports) node [token (fact "Integer" 1)]))))))

(deftest returning-diagnostics-do-not-change-original-continuations
  (doseq [check [types/check-native-assert! types/check-native-value! types/check-native-return!]]
    (let [events (atom []) operations (assoc (ports) :diagnostic! (fn [& args] (swap! events conj args) :returned))]
      (is (nil? (check operations node [])))
      (is (= ["L5-ANNOTATION-REQUIRED" "L5-ANNOTATION-REQUIRED"] (mapv first @events)))
      (is (every? #(identical? node (nth % 2)) @events))))
  (let [result (Object.) operations (assoc (ports) :diagnostic! (fn [& _] result))
        args [(annotation :Integer) (fact "String" "bad")]]
    (is (identical? result (types/check-native-assert! operations node args)))
    (is (= "Integer" (types/check-native-value! operations node args)))
    (is (= "Integer" (types/check-native-return! operations node args)))))

(deftest owner-exceptions-and-native-source-metadata-are-preserved
  (let [error (ex-info "owner exception" {:private-evidence (Object.)})
        operations (assoc (ports) :diagnostic! (fn [& _] (throw error)))
        args (with-meta [(annotation :Integer) (fact "String" "bad")] {:source :preserved})
        before (binding [*print-meta* true] (pr-str [node args]))]
    (doseq [check [types/check-native-assert! types/check-native-value! types/check-native-return!]]
      (is (identical? error (thrown #(check operations node args)))))
    (is (= before (binding [*print-meta* true] (pr-str [node args]))))
    (let [result (diagnostic #(types/check-native-value! (ports) node args))]
      (is (= "L5-TYPE-MISMATCH" (:id result)))
      (is (= (:source-span node) (:source-span result)))
      (is (= ["Integer" "String"] [(:expected-type result) (:actual-type result)])))))
