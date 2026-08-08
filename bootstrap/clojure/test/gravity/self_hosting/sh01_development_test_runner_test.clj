(ns gravity.self-hosting.sh01-development-test-runner-test
  (:require [clojure.string :as str]
            [clojure.test :as test :refer [deftest is testing]]
            [gravity.development-test-runner :as runner]))

(def ^:private c2-test-names
  '[c2-source-identity-compatibility-wrappers-preserve-interposition
    c2-reader-diagnostics-compatibility-wrappers-preserve-interposition
    c2-lexical-validation-compatibility-wrappers-preserve-interposition
    c2-artifact-identity-load-order-initializes-standard-reader-options
    c2-artifact-identity-compatibility-wrappers-preserve-interposition])

(def ^:private c3-test-names
  '[syntax-object-stream-compatibility-wrapper-preserves-arity-and-output
    c3-origin-chain-compatibility-wrapper-preserves-arity-and-output
    c3-syntax-evidence-compatibility-wrappers-preserve-output-and-interposition
    c3-syntax-construction-compatibility-wrappers-preserve-interposition
    c3-syntax-verification-compatibility-wrappers-preserve-interposition
    c3-syntax-diagnostics-compatibility-wrappers-preserve-interposition
    c3-reader-integrity-compatibility-wrappers-preserve-interposition
    c3-literal-projection-compatibility-wrappers-preserve-interposition
    c3-artifact-identity-compatibility-wrappers-preserve-interposition])

(def ^:private c4-c18-test-names
  {"c4_test.clj"
   '[c4-macro-evidence-compatibility-wrappers-preserve-output-and-interposition
     macro-expansion-compatibility-wrappers-preserve-output-and-interposition]
   "c5_test.clj" '[c5-resolution-compatibility-wrappers-preserve-arglists-and-interposition]
   "c6_test.clj" '[c6-lowering-compatibility-wrappers-preserve-arglists-and-interposition]
   "c7_test.clj" '[c7-type-checker-compatibility-wrappers-preserve-interposition]
   "c8_test.clj" '[c8-effect-checker-compatibility-wrappers-preserve-interposition]
   "c9_test.clj" '[c9-ownership-checker-compatibility-wrappers-preserve-interposition]
   "c10_test.clj" '[c10-safety-analysis-compatibility-wrappers-preserve-interposition]
   "c11_test.clj" '[c11-mir-compatibility-wrappers-preserve-interposition]
   "c12_test.clj" '[c12-domain-ir-compatibility-wrappers-preserve-interposition]
   "c13_test.clj" '[c13-optimization-compatibility-wrappers-preserve-interposition]
   "c14_test.clj" '[c14-lowering-compatibility-wrappers-preserve-interposition]
   "c15_test.clj" '[c15-diagnostics-compatibility-wrappers-preserve-interposition]
   "c16_test.clj" '[c16-incremental-compatibility-wrappers-preserve-interposition]
   "c17_test.clj" '[c17-plugin-compatibility-wrappers-preserve-interposition]
   "c18_test.clj" '[c18-verification-compatibility-wrappers-preserve-interposition]})

(defn- exception-data [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo exception
      (ex-data exception))))

(deftest catalog-is-static-exact-and-bootstrap-lazy
  (is (= [{:namespace 'gravity.bootstrap-test
           :path "bootstrap/clojure/test/gravity/bootstrap_test.clj"}
          {:namespace 'gravity.bootstrap-compatibility.c2-test
           :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c2_test.clj"}
          {:namespace 'gravity.bootstrap-compatibility.c3-test
           :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c3_test.clj"}
          {:namespace 'gravity.bootstrap-compatibility.c4-test :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c4_test.clj"}
          {:namespace 'gravity.bootstrap-compatibility.c5-test :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c5_test.clj"}
          {:namespace 'gravity.bootstrap-compatibility.c6-test :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c6_test.clj"}
          {:namespace 'gravity.bootstrap-compatibility.c7-test :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c7_test.clj"}
          {:namespace 'gravity.bootstrap-compatibility.c8-test :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c8_test.clj"}
          {:namespace 'gravity.bootstrap-compatibility.c9-test :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c9_test.clj"}
          {:namespace 'gravity.bootstrap-compatibility.c10-test :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c10_test.clj"}
          {:namespace 'gravity.bootstrap-compatibility.c11-test :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c11_test.clj"}
          {:namespace 'gravity.bootstrap-compatibility.c12-test :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c12_test.clj"}
          {:namespace 'gravity.bootstrap-compatibility.c13-test :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c13_test.clj"}
          {:namespace 'gravity.bootstrap-compatibility.c14-test :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c14_test.clj"}
          {:namespace 'gravity.bootstrap-compatibility.c15-test :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c15_test.clj"}
          {:namespace 'gravity.bootstrap-compatibility.c16-test :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c16_test.clj"}
          {:namespace 'gravity.bootstrap-compatibility.c17-test :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c17_test.clj"}
          {:namespace 'gravity.bootstrap-compatibility.c18-test :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c18_test.clj"}]
         runner/namespace-catalog))
  (let [loads (atom [])
        reject-load (fn [records]
                      (swap! loads into (map :namespace records))
                      (throw (ex-info "unexpected load" {})))]
    (with-redefs-fn {#'gravity.development-test-runner/load-selected-namespaces!
                     reject-load}
      #(do
         (is (zero? (runner/run-cli! ["--help"])))
         (is (zero? (runner/run-cli! ["--catalog"])))
         (is (= :gravity.development-test-runner/unknown-namespace
                (:type (exception-data
                        (fn []
                          (runner/run-cli!
                           ["--namespace" "gravity.not-in-static-catalog"]))))))))
    (is (empty? @loads))))

(deftest default-and-repeatable-namespace-selection-is-deterministic
  (let [parse-args @#'gravity.development-test-runner/parse-args
        selected-namespaces @#'gravity.development-test-runner/selected-namespace-records]
    (is (= ['gravity.bootstrap-test]
           (mapv :namespace (selected-namespaces (parse-args [])))))
    (is (= ['gravity.bootstrap-test
            'gravity.bootstrap-compatibility.c2-test
            'gravity.bootstrap-compatibility.c3-test]
           (mapv :namespace
                 (selected-namespaces
                  (parse-args
                   ["--namespace" "gravity.bootstrap-compatibility.c2-test"
                    "--namespace" "gravity.bootstrap-compatibility.c3-test"
                    "--namespace" "gravity.bootstrap-test"
                    "--namespace" "gravity.bootstrap-compatibility.c2-test"])))))))

(deftest qualified-selectors-resolve-only-in-selected-namespaces
  (let [select-vars @#'gravity.development-test-runner/select-test-vars
        bootstrap-record {:name "shared-test"
                          :qualified-name "gravity.bootstrap-test/shared-test"
                          :namespace 'gravity.bootstrap-test}
        c2-record {:name "shared-test"
                   :qualified-name "gravity.bootstrap-compatibility.c2-test/shared-test"
                   :namespace 'gravity.bootstrap-compatibility.c2-test}]
    (is (= [c2-record]
           (select-vars
            [bootstrap-record c2-record]
            {:exact ["gravity.bootstrap-compatibility.c2-test/shared-test"]
             :regex [] :prefix []})))
    (is (= :gravity.development-test-runner/unknown-selector
           (:type
            (exception-data
             #(select-vars
               [bootstrap-record]
               {:exact ["gravity.bootstrap-compatibility.c2-test/shared-test"]
                :regex [] :prefix []})))))))

(deftest multi-namespace-execution-preserves-once-and-each-fixtures
  (let [namespace-a (create-ns 'gravity.runner-fixture-a)
        namespace-b (create-ns 'gravity.runner-fixture-b)
        once-events (atom [])
        each-events (atom [])
        add-test!
        (fn [namespace-object test-name]
          (let [test-var (intern namespace-object test-name (fn [] (is true)))]
            (alter-meta! test-var assoc :test (fn [] ((var-get test-var))))
            test-var))]
    (try
      (doseq [[namespace-object marker] [[namespace-a :a] [namespace-b :b]]]
        (alter-meta! namespace-object assoc
                     ::test/once-fixtures
                     [(fn [body]
                        (swap! once-events conj [:begin marker])
                        (body)
                        (swap! once-events conj [:end marker]))]
                     ::test/each-fixtures
                     [(fn [body]
                        (swap! each-events conj marker)
                        (body))]))
      (let [var-a (add-test! namespace-a 'fixture-a-test)
            var-b (add-test! namespace-b 'fixture-b-test)
            namespace-records [{:namespace 'gravity.runner-fixture-a}
                               {:namespace 'gravity.runner-fixture-b}]
            selected-records [{:namespace 'gravity.runner-fixture-a :var var-a}
                              {:namespace 'gravity.runner-fixture-b :var var-b}]
            summary ((deref #'gravity.development-test-runner/run-selected-tests)
                     namespace-records selected-records false)]
        (is (= 2 (:test summary)))
        (is (zero? (:fail summary)))
        (is (zero? (:error summary)))
        (is (= [[:begin :a] [:end :a] [:begin :b] [:end :b]] @once-events))
        (is (= [:a :b] @each-events)))
      (finally
        (remove-ns 'gravity.runner-fixture-a)
        (remove-ns 'gravity.runner-fixture-b)))))

(deftest c2-compatibility-vars-moved-exactly-out-of-central-test
  (let [central (slurp "bootstrap/clojure/test/gravity/bootstrap_test.clj")
        compatibility
        (slurp "bootstrap/clojure/test/gravity/bootstrap_compatibility/c2_test.clj")]
    (doseq [test-name c2-test-names]
      (testing (str test-name)
        (is (not (str/includes? central (str "(deftest " test-name))))
        (is (str/includes? compatibility (str "(deftest " test-name))))))
  (is (= 5 (count c2-test-names))))

(deftest c3-compatibility-vars-moved-exactly-out-of-central-test
  (let [central (slurp "bootstrap/clojure/test/gravity/bootstrap_test.clj")
        compatibility
        (slurp "bootstrap/clojure/test/gravity/bootstrap_compatibility/c3_test.clj")]
    (doseq [test-name c3-test-names]
      (testing (str test-name)
        (is (not (str/includes? central (str "(deftest " test-name))))
        (is (str/includes? compatibility (str "(deftest " test-name))))))
    (is (= 9 (count c3-test-names)))))

(deftest c4-c18-compatibility-vars-moved-exactly-out-of-central-test
  (let [central (slurp "bootstrap/clojure/test/gravity/bootstrap_test.clj")
        directory "bootstrap/clojure/test/gravity/bootstrap_compatibility/"]
    (doseq [[file-name test-names] c4-c18-test-names
            test-name test-names]
      (testing (str file-name "/" test-name)
        (is (not (str/includes? central (str "(deftest " test-name))))
        (is (str/includes? (slurp (str directory file-name))
                           (str "(deftest " test-name))))))
    (is (= 16 (reduce + (map count (vals c4-c18-test-names)))))))
