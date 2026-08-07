(ns gravity.project-structure-test-runner-test
  (:require [clojure.test :as test :refer [deftest is testing]]
            [gravity.project-structure-test-runner :as runner]))

(def ^:private compatibility-selectors
  ["gravity.bootstrap-test/hosted-hello-runs"
   "gravity.bootstrap-test/reader-source-unit-identity-preserves-path-extension-and-options"
   "gravity.bootstrap-test/reader-file-policy-rejects-extension-and-malformed-utf8"
   "gravity.bootstrap-test/c2-reader-treats-cr-lf-and-crlf-as-line-terminators"])

(defn- exact-args
  [selectors]
  (mapcat (fn [selector] ["--exact" selector]) selectors))

(defn- synthetic-vars
  [namespace-symbol count]
  (let [namespace-object (or (find-ns namespace-symbol)
                             (create-ns namespace-symbol))]
    (mapv (fn [index]
            (let [name (symbol (str "synthetic-test-" index))
                  test-var (intern namespace-object name (fn [] nil))]
              (alter-meta! test-var assoc :test (fn [] nil))
              test-var))
          (range count))))

(defn- raises-message?
  [pattern thunk]
  (try
    (thunk)
    false
    (catch clojure.lang.ExceptionInfo ex
      (boolean (re-find pattern (.getMessage ex))))))

(deftest runner-unit-does-not-eagerly-load-production-namespaces
  (is (nil? (find-ns 'gravity.bootstrap)))
  (is (nil? (find-ns 'gravity.bootstrap-test)))
  (is (nil? (find-ns 'gravity.source-unit-test)))
  (is (nil? (find-ns 'gravity.source-span-test)))
  (is (nil? (find-ns 'gravity.digest-test))))

(deftest exact-selection-is-strict
  (testing "the required set is exact, with no duplicates, omissions, or unknown vars"
    (is (raises-message?
         #"duplicate"
         #(runner/run-cli! (concat (exact-args compatibility-selectors)
                                   ["--exact" (first compatibility-selectors)]))))
    (is (raises-message?
         #"exactly the four"
         #(runner/run-cli! (exact-args (butlast compatibility-selectors)))))
    (is (raises-message?
         #"unknown exact"
         #(runner/run-cli! (concat (exact-args compatibility-selectors)
                                   ["--exact" "gravity.bootstrap-test/no-such-test"]))))))

(deftest aggregate-summary-covers-fifteen-leaf-and-four-compatibility-tests
  (let [leaf-plan [['gravity.synthetic-source-unit-test 4]
                   ['gravity.synthetic-source-span-test 5]
                   ['gravity.synthetic-digest-test 6]]
        leaf-namespaces (mapv first leaf-plan)
        _ (doseq [[namespace-symbol count] leaf-plan]
            (synthetic-vars namespace-symbol count))
        bootstrap-vars (synthetic-vars 'gravity.synthetic-bootstrap-test 4)
        seen (atom [])]
    (with-redefs-fn
      {(ns-resolve 'gravity.project-structure-test-runner 'run-selected-vars)
       (fn [namespace-symbol selected-vars _fail-fast?]
         (swap! seen conj [namespace-symbol (count selected-vars)])
         {:test (count selected-vars)
          :pass (* 2 (count selected-vars))
          :fail 0
          :error 0})}
      (fn []
        (let [result (runner/run-suite!
                    {:leaf-namespaces leaf-namespaces
                     :leaf-loader (fn [_] nil)
                     :bootstrap-namespace 'gravity.synthetic-bootstrap-test
                       :bootstrap-vars bootstrap-vars
                       :fail-fast? true})]
          (is (= 19 (:expected-tests result)))
          (is (= 19 (get-in result [:summary :test])))
          (is (= 0 (:exit-code result)))
          (is (= (conj (mapv (fn [[namespace-symbol count]] [namespace-symbol count]) leaf-plan)
                       ['gravity.synthetic-bootstrap-test 4])
                 @seen)))))))

(deftest leaf-failure-stops-before-bootstrap
  (let [leaf-namespaces ['gravity.synthetic-failing-leaf-test]
        bootstrap-namespace 'gravity.synthetic-unreached-bootstrap-test
        _ (synthetic-vars (first leaf-namespaces) 1)
        seen (atom [])
        required (atom [])
        resolved (atom [])]
    (with-redefs-fn
      {(ns-resolve 'gravity.project-structure-test-runner 'run-selected-vars)
       (fn [namespace-symbol selected-vars _fail-fast?]
         (swap! seen conj namespace-symbol)
         {:test 1 :pass 0 :fail 1 :error 0})}
      (fn []
        (let [result (runner/run-suite!
                      {:leaf-namespaces leaf-namespaces
                       :leaf-loader (fn [_] nil)
                       :bootstrap-namespace bootstrap-namespace
                       :bootstrap-selectors compatibility-selectors
                       :bootstrap-loader #(swap! required conj %)
                       :bootstrap-resolver (fn [namespace-symbol _]
                                             (swap! resolved conj namespace-symbol)
                                             [])
                       :fail-fast? true})]
          (is (= 1 (:exit-code result)))
          (is (= leaf-namespaces (:ran-namespaces result)))
          (is (= leaf-namespaces @seen))
          (is (not-any? #{'gravity.bootstrap-test} @required))
          (is (empty? @resolved)))))))

(deftest full-signal-mode-reports-leaf-failure-and-still-runs-bootstrap
  (let [leaf-namespaces ['gravity.synthetic-full-signal-leaf-test]
        bootstrap-namespace 'gravity.synthetic-full-signal-bootstrap-test
        _ (synthetic-vars (first leaf-namespaces) 1)
        bootstrap-vars (synthetic-vars bootstrap-namespace 1)
        seen (atom [])]
    (with-redefs-fn
      {(ns-resolve 'gravity.project-structure-test-runner 'run-selected-vars)
       (fn [namespace-symbol selected-vars _fail-fast?]
         (swap! seen conj namespace-symbol)
         (if (= namespace-symbol (first leaf-namespaces))
           {:test 1 :pass 0 :fail 1 :error 0}
           {:test 1 :pass 1 :fail 0 :error 0}))}
      (fn []
        (let [result (runner/run-suite!
                      {:leaf-namespaces leaf-namespaces
                       :leaf-loader (fn [_] nil)
                       :bootstrap-namespace bootstrap-namespace
                       :bootstrap-vars bootstrap-vars
                       :fail-fast? false})]
          (is (= 1 (:exit-code result)))
          (is (= ["gravity.synthetic-full-signal-leaf-test"
                  "gravity.synthetic-full-signal-bootstrap-test"]
                 (mapv str @seen)))
          (is (= 2 (get-in result [:summary :test]))))))))

(deftest green-leaves-load-bootstrap-only-after-leaf-phase
  (let [leaf-namespaces ['gravity.synthetic-green-leaf-test]
        _ (synthetic-vars (first leaf-namespaces) 1)
        bootstrap-vars (synthetic-vars 'gravity.synthetic-green-bootstrap-test 4)
        seen (atom [])
        required (atom [])]
    (with-redefs-fn
      {(ns-resolve 'gravity.project-structure-test-runner 'run-selected-vars)
       (fn [namespace-symbol selected-vars _fail-fast?]
         (swap! seen conj namespace-symbol)
         {:test (count selected-vars) :pass 1 :fail 0 :error 0})}
      (fn []
        (let [result (runner/run-suite!
                      {:leaf-namespaces leaf-namespaces
                       :leaf-loader (fn [_] nil)
                       :bootstrap-namespace 'gravity.synthetic-green-bootstrap-test
                       :bootstrap-selectors compatibility-selectors
                       :bootstrap-loader #(swap! required conj %)
                       :bootstrap-resolver
                       (fn [_ _]
                         (mapv (fn [test-var] {:var test-var}) bootstrap-vars))
                       :fail-fast? true})]
          (is (= 0 (:exit-code result)))
          (is (= 'gravity.synthetic-green-bootstrap-test (last @seen)))
          (is (some #{'gravity.synthetic-green-bootstrap-test} @required)))))))

(deftest fixture-errors-are-reported-and-lifecycle-is-explicit
  (let [namespace-symbol 'gravity.synthetic-fixture-error-test
        namespace-object (or (find-ns namespace-symbol) (create-ns namespace-symbol))
        events (atom [])
        test-var (intern namespace-object 'passes (fn [] nil))]
    (alter-meta! test-var assoc :test (fn [] nil))
    (alter-meta! namespace-object assoc
                 ::test/once-fixtures
                 [(fn [f]
                    (swap! events conj :once-before)
                    (try
                      (f)
                      (finally
                        (swap! events conj :once-after))))]
                 ::test/each-fixtures
                 [(fn [f]
                    (swap! events conj :each-before)
                    (throw (ex-info "fixture boom" {})))])
    (let [run-selected (ns-resolve 'gravity.project-structure-test-runner 'run-selected-vars)
          thrown (try
                   (run-selected namespace-symbol [test-var] true)
                   nil
                   (catch clojure.lang.ExceptionInfo ex ex))]
      (is (= "fixture boom" (.getMessage thrown)))
      (is (= [:once-before :each-before :once-after] @events)))
    (let [cleanup (ns-resolve 'gravity.project-structure-test-runner 'cleanup!)
          events (atom [])]
      (with-redefs [clojure.core/flush #(swap! events conj :flush)
                    clojure.core/shutdown-agents #(swap! events conj :shutdown)]
        (cleanup))
      (is (= [:flush :shutdown :flush] @events)))))

(deftest metadata-added-inside-once-fixture-is-observed
  (let [namespace-symbol 'gravity.synthetic-metadata-fixture-test
        namespace-object (or (find-ns namespace-symbol) (create-ns namespace-symbol))
        events (atom [])
        first-var (intern namespace-object 'first-test (fn [] nil))
        added-var (intern namespace-object 'added-test (fn [] nil))]
    (alter-meta! first-var assoc :test (fn [] (swap! events conj :first)))
    (alter-meta! namespace-object assoc
                 ::test/once-fixtures
                 [(fn [f]
                    (alter-meta! added-var assoc :test (fn [] (swap! events conj :added)))
                    (f))])
    ((ns-resolve 'gravity.project-structure-test-runner 'run-selected-vars)
     namespace-symbol [first-var added-var] true)
    (is (= [:first :added] @events))))

(deftest test-ns-hook-is-rejected-fail-closed
  (let [namespace-symbol 'gravity.synthetic-hook-test
        namespace-object (or (find-ns namespace-symbol) (create-ns namespace-symbol))
        test-var (intern namespace-object 'test-var (fn [] nil))]
    (alter-meta! test-var assoc :test (fn [] nil))
    (intern namespace-object 'test-ns-hook (fn [] nil))
    (is (try
          ((ns-resolve 'gravity.project-structure-test-runner 'run-selected-vars)
           namespace-symbol [test-var] true)
          false
          (catch clojure.lang.ExceptionInfo ex
            (= :gravity.project-structure-test-runner/unsupported-test-hook
               (:type (ex-data ex))))))))
