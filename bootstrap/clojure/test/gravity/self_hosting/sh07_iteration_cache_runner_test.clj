(ns gravity.self-hosting.sh07-iteration-cache-runner-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh07-iteration-cache-runner :as runner]))

(deftest iteration-cache-reuses-equal-source-and-identical-artifacts
  (let [sh06-calls (atom 0)
        core-calls (atom 0)
        verification-calls (atom 0)]
    (with-redefs
      [bootstrap/sh06-resolution-source-artifact
       (fn [path source]
         (swap! sh06-calls inc)
         {:artifact-id (str path ":" source)
          :provenance {:source-path path}
          :gravity-resolution-boundary
          {:resolved-analysis {:semantic-projection-id source}}})
       bootstrap/sh07-core-from-resolution-artifact
       (fn [resolution]
         (swap! core-calls inc)
         {:artifact-id (:artifact-id resolution)})
       bootstrap/sh07-core-artifact-verification
       (fn [artifact]
         (swap! verification-calls inc)
         {:verified (:artifact-id artifact)})]
      (let [result
            (runner/with-iteration-cache
             {:maximum-entries 2}
             (fn []
               (let [left (bootstrap/sh06-resolution-source-artifact "a" "x")
                     right (bootstrap/sh06-resolution-source-artifact "a" "x")
                     core-left
                     (bootstrap/sh07-core-from-resolution-artifact left)
                     core-right
                     (bootstrap/sh07-core-from-resolution-artifact right)]
                 [(bootstrap/sh07-core-artifact-verification core-left)
                  (bootstrap/sh07-core-artifact-verification core-right)])))]
        (is (= 1 @sh06-calls @core-calls @verification-calls))
        (is (= {:sh06-hits 1 :sh06-misses 1
                :core-hits 1 :core-misses 1
                :verification-hits 1 :verification-misses 1}
               (:cache result)))
        (is (= :non-authoritative (:authority result)))
        (is (false? (:authoritative? result)))
        (is (false? (:cache-authoritative? result)))
        (is (true? (:fresh-authoritative-run-required? result)))))))

(deftest iteration-cache-is-content-keyed-and-bounded
  (let [calls (atom 0)]
    (with-redefs
      [bootstrap/sh06-resolution-source-artifact
       (fn [path source]
         (swap! calls inc)
         [path source])]
      (let [result
            (runner/with-iteration-cache
             {:maximum-entries 1}
             (fn []
               [(bootstrap/sh06-resolution-source-artifact "a" "one")
                (bootstrap/sh06-resolution-source-artifact "a" "two")
                (bootstrap/sh06-resolution-source-artifact "a" "one")]))]
        (is (= 3 @calls))
        (is (= 3 (get-in result [:cache :sh06-misses])))
        (is (zero? (get-in result [:cache :sh06-hits])))))))

(deftest iteration-cache-serializes-concurrent-misses
  (let [calls (atom 0)]
    (with-redefs
      [bootstrap/sh06-resolution-source-artifact
       (fn [path source]
         (swap! calls inc)
         (Thread/sleep 25)
         [path source])]
      (let [result
            (runner/with-iteration-cache
             {:maximum-entries 1}
             (fn []
               (mapv deref
                     (repeatedly
                      8
                      #(future
                         (bootstrap/sh06-resolution-source-artifact
                          "same" "content"))))))]
        (is (= 1 @calls))
        (is (= 1 (get-in result [:cache :sh06-misses])))
        (is (= 7 (get-in result [:cache :sh06-hits])))
        (is (apply = (:value result)))))))

(deftest iteration-cache-reports-per-namespace-time-and-cache-deltas
  (let [calls (atom 0)
        tested-namespaces (atom [])
        output (java.io.StringWriter.)]
    (with-redefs
      [bootstrap/sh06-resolution-source-artifact
       (fn [path source]
         (swap! calls inc)
         [path source])
       clojure.test/test-ns
       (fn [namespace]
         (swap! tested-namespaces conj namespace)
         (case namespace
           example.one
           (let [nested (Thread.
                         #(clojure.test/test-ns 'unrelated.namespace))]
             (.start nested)
             (.join nested))

           unrelated.namespace
           (bootstrap/sh06-resolution-source-artifact "same" "content")

           example.two
           (bootstrap/sh06-resolution-source-artifact "same" "content"))
         {:test 0 :pass 0 :fail 0 :error 0})]
      (let [result
            (binding [*out* output]
              (runner/with-iteration-cache
               {:maximum-entries 1}
               #(#'runner/run-tests-with-telemetry
                  '[example.one example.two] false)))
            namespace-results (get-in result [:value :namespace-results])]
        (is (= 1 @calls))
        (is (= '[example.one unrelated.namespace example.two]
               @tested-namespaces))
        (is (= '[example.one example.two]
               (mapv :namespace namespace-results)))
        (is (every? #(and (integer? (:elapsed-ms %))
                          (not (neg? (:elapsed-ms %))))
                    namespace-results))
        (is (= 1 (get-in namespace-results [0 :cache :sh06-misses])))
        (is (= 1 (get-in namespace-results [1 :cache :sh06-hits])))
        (is (every? #(= :non-authoritative (:authority %))
                    namespace-results))
        (is (= (:cache result)
               (apply merge-with + (map :cache namespace-results))))
        (is (= 2
               (count
                (re-seq #":gravity/sh07-iteration-namespace-result"
                        (str output)))))))))

(deftest iteration-cache-fail-fast-skips-downstream-namespaces
  (let [test-namespace-var
        (ns-resolve
         'gravity.self-hosting.sh07-iteration-cache-runner
         'test-namespace)
        tested-namespaces (atom [])
        output (java.io.StringWriter.)]
    (with-redefs-fn
      {test-namespace-var
       (fn [namespace _fail-fast?]
         (swap! tested-namespaces conj namespace)
         {:summary
          (if (= 'example.failing namespace)
            {:test 1 :pass 0 :fail 1 :error 0}
            {:test 1 :pass 1 :fail 0 :error 0})
          :skipped-test-vars []})}
      (fn []
        (let [result
              (binding [*out* output
                        clojure.test/*test-out* output]
                (#'runner/run-tests-with-telemetry
                 '[example.failing example.skipped] true))]
        (is (= '[example.failing] @tested-namespaces))
        (is (= '[example.failing]
               (mapv :namespace (:namespace-results result))))
        (is (true? (:stopped-early? result)))
        (is (= '[example.skipped] (:skipped-namespaces result)))
        (is (= [] (:skipped-test-vars result)))
        (is (= {:test 1 :pass 0 :fail 1 :error 0 :type :summary}
               (:test-result result)))
        (is (= 1
               (count
                (re-seq #":gravity/sh07-iteration-namespace-result"
                        (str output)))))
        (reset! tested-namespaces [])
        (let [full-result
              (binding [*out* (java.io.StringWriter.)
                        clojure.test/*test-out* (java.io.StringWriter.)]
                (#'runner/run-tests-with-telemetry
                 '[example.failing example.skipped] false))]
          (is (= '[example.failing example.skipped] @tested-namespaces))
          (is (false? (:stopped-early? full-result)))
          (is (= [] (:skipped-namespaces full-result)))
          (is (= {:test 2 :pass 1 :fail 1 :error 0 :type :summary}
                 (:test-result full-result)))))))))

(deftest iteration-cache-fail-fast-stops-inside-ordinary-namespace
  (let [namespace-symbol 'gravity.self-hosting.synthetic-fail-fast-test
        namespace-object (create-ns namespace-symbol)
        once-calls (atom 0)
        each-calls (atom 0)
        executed (atom [])
        output (java.io.StringWriter.)]
    (try
      (let [test-vars
            (mapv
             (fn [var-name]
               (let [test-var (intern namespace-object var-name)]
                 (alter-meta! test-var assoc :test (fn []))
                 test-var))
             '[alpha-test beta-test gamma-test])
            ordered-vars
            (vec
             (filter (comp :test meta)
                     (vals (ns-interns namespace-object))))
            failing-var (first ordered-vars)
            passing-vars (subvec ordered-vars 1)
            qualified
            (fn [test-var]
              (symbol (str namespace-symbol)
                      (str (:name (meta test-var)))))]
        (alter-meta!
         failing-var assoc :test
         #(do
            (swap! executed conj (:name (meta failing-var)))
            (clojure.test/do-report
             {:type :fail
              :message "synthetic fail-fast failure"
              :expected true
              :actual false})))
        (doseq [test-var passing-vars]
          (alter-meta!
           test-var assoc :test
           #(do
              (swap! executed conj (:name (meta test-var)))
              (clojure.test/do-report
               {:type :pass :expected true :actual true}))))
        (alter-meta!
         namespace-object assoc
         :clojure.test/once-fixtures
         [(fn [operation] (swap! once-calls inc) (operation))]
         :clojure.test/each-fixtures
         [(fn [operation] (swap! each-calls inc) (operation))])
        (let [result
              (binding [*out* output
                        clojure.test/*test-out* output]
                (#'runner/run-tests-with-telemetry
                 [namespace-symbol] true))]
          (is (= [(:name (meta failing-var))] @executed))
          (is (= 1 @once-calls @each-calls))
          (is (= (mapv qualified passing-vars)
                 (:skipped-test-vars result)))
          (is (= (:skipped-test-vars result)
                 (get-in result [:namespace-results 0
                                 :skipped-test-vars])))
          (is (true? (:stopped-early? result)))
          (is (= {:test 1 :pass 0 :fail 1 :error 0 :type :summary}
                 (:test-result result))))
        (reset! executed [])
        (reset! once-calls 0)
        (reset! each-calls 0)
        (let [result
              (binding [*out* (java.io.StringWriter.)
                        clojure.test/*test-out* (java.io.StringWriter.)]
                (#'runner/run-tests-with-telemetry
                 [namespace-symbol] false))]
          (is (= (set (map (comp :name meta) test-vars))
                 (set @executed)))
          (is (= 1 @once-calls))
          (is (= 3 @each-calls))
          (is (false? (:stopped-early? result)))
          (is (= [] (:skipped-test-vars result)))
          (is (= {:test 3 :pass 2 :fail 1 :error 0 :type :summary}
                 (:test-result result)))))
      (finally
        (remove-ns namespace-symbol)))))

(deftest iteration-cache-fail-fast-preserves-dynamic-fixture-selection
  (let [namespace-symbol 'gravity.self-hosting.synthetic-fixture-selection-test
        namespace-object (create-ns namespace-symbol)
        added-var (intern namespace-object 'added-by-once-fixture)
        removed-var (intern namespace-object 'removed-by-once-fixture)
        empty-namespace-symbol
        'gravity.self-hosting.synthetic-empty-fixture-test
        empty-namespace (create-ns empty-namespace-symbol)
        executed (atom [])
        each-calls (atom 0)
        empty-once-calls (atom 0)]
    (try
      (alter-meta!
       removed-var assoc :test
       #(swap! executed conj 'removed-by-once-fixture))
      (alter-meta!
       namespace-object assoc
       :clojure.test/once-fixtures
       [(fn [operation]
          (alter-meta!
           added-var assoc :test
           #(do
              (swap! executed conj 'added-by-once-fixture)
              (clojure.test/do-report
               {:type :pass :expected true :actual true})))
          (alter-meta! removed-var dissoc :test)
          (operation))]
       :clojure.test/each-fixtures
       [(fn [operation] (swap! each-calls inc) (operation))])
      (alter-meta!
       empty-namespace assoc
       :clojure.test/once-fixtures
       [(fn [operation] (swap! empty-once-calls inc) (operation))])
      (let [selection-result
            (binding [*out* (java.io.StringWriter.)
                      clojure.test/*test-out* (java.io.StringWriter.)]
              (#'runner/fail-fast-test-ns namespace-symbol))
            empty-result
            (binding [*out* (java.io.StringWriter.)
                      clojure.test/*test-out* (java.io.StringWriter.)]
              (#'runner/fail-fast-test-ns empty-namespace-symbol))]
        (is (= '[added-by-once-fixture] @executed))
        (is (= 1 @each-calls))
        (is (= {:test 1 :pass 1 :fail 0 :error 0}
               (:summary selection-result)))
        (is (zero? @empty-once-calls))
        (is (= {:test 0 :pass 0 :fail 0 :error 0}
               (:summary empty-result))))
      (finally
        (remove-ns namespace-symbol)
        (remove-ns empty-namespace-symbol)))))

(deftest iteration-cache-retains-telemetry-when-fixture-throws
  (let [namespace-symbol 'gravity.self-hosting.synthetic-fixture-error-test
        namespace-object (create-ns namespace-symbol)
        test-var (intern namespace-object 'never-runs-test)]
    (try
      (alter-meta!
       test-var assoc :test
       #(clojure.test/do-report
         {:type :pass :expected true :actual true}))
      (alter-meta!
       namespace-object assoc
       :clojure.test/once-fixtures
       [(fn [_operation]
          (throw (ex-info "fixture failed" {:id "FIXTURE-FAILED"})))])
      (doseq [fail-fast? [false true]]
        (let [output (java.io.StringWriter.)
              data
              (binding [*out* output
                        clojure.test/*test-out* output]
                (try
                  (#'runner/run-tests-with-telemetry
                   [namespace-symbol] fail-fast?)
                  nil
                  (catch clojure.lang.ExceptionInfo error
                    (ex-data error))))
              rendered (str output)]
          (is (= "FIXTURE-FAILED" (:id data)))
          (is (re-find #":gravity/sh07-iteration-namespace-result"
                       rendered))
          (is (re-find #":completed\? false" rendered))
          (is (re-find #":authority :non-authoritative" rendered))))
      (finally
        (remove-ns namespace-symbol)))))

(deftest iteration-cache-fail-fast-preserves-test-ns-hook-boundary
  (let [hook-namespace-symbol
        'gravity.self-hosting.synthetic-test-ns-hook-test
        later-namespace-symbol
        'gravity.self-hosting.synthetic-after-test-ns-hook-test
        hook-namespace (create-ns hook-namespace-symbol)
        later-namespace (create-ns later-namespace-symbol)
        hook-calls (atom 0)
        later-calls (atom 0)]
    (try
      (intern
       hook-namespace 'test-ns-hook
       #(do
          (swap! hook-calls inc)
          (clojure.test/do-report
           {:type :fail
            :message "synthetic hook failure"
            :expected true
            :actual false})))
      (let [later-var (intern later-namespace 'later-test)]
        (alter-meta!
         later-var assoc :test
         #(do
            (swap! later-calls inc)
            (clojure.test/do-report
             {:type :pass :expected true :actual true}))))
      (let [result
            (binding [*out* (java.io.StringWriter.)
                      clojure.test/*test-out* (java.io.StringWriter.)]
              (#'runner/run-tests-with-telemetry
               [hook-namespace-symbol later-namespace-symbol] true))]
        (is (= 1 @hook-calls))
        (is (zero? @later-calls))
        (is (true? (:stopped-early? result)))
        (is (= [later-namespace-symbol] (:skipped-namespaces result)))
        (is (= [] (:skipped-test-vars result)))
        (is (= {:test 0 :pass 0 :fail 1 :error 0 :type :summary}
               (:test-result result))))
      (finally
        (remove-ns hook-namespace-symbol)
        (remove-ns later-namespace-symbol)))))

(deftest iteration-cache-runs-one-owned-test-var-with-honest-authority
  (require 'gravity.cli-test)
  (let [selection
        (runner/parse-arguments
         ["--test-var"
          "gravity.cli-test/presentation-values-are-extracted-with-bootstrap-parity"
          "--max-cache-entries" "1"])
        namespace-object (find-ns 'gravity.cli-test)
        original-namespace-meta (meta namespace-object)
        once-calls (atom 0)
        each-calls (atom 0)
        report-events (atom [])
        original-report clojure.test/report
        output (java.io.StringWriter.)
        result
        (try
          (alter-meta!
           namespace-object assoc
           :clojure.test/once-fixtures
           [(fn [operation] (swap! once-calls inc) (operation))]
           :clojure.test/each-fixtures
           [(fn [operation] (swap! each-calls inc) (operation))])
          (with-redefs
            [clojure.test/report
             (fn [event]
               (swap! report-events conj (:type event))
               (original-report event))]
            (binding [*out* output
                      clojure.test/*test-out* output]
              (runner/run-selection selection)))
          (finally
            (reset-meta! namespace-object original-namespace-meta)))]
    (is (true? (:ok? result)))
    (is (= :gravity/sh07-iteration-cache-run (:artifact result)))
    (is (= 1 (get-in result [:test-result :test])))
    (is (= 40 (get-in result [:test-result :pass])))
    (is (zero? (get-in result [:test-result :fail])))
    (is (= :non-authoritative (:authority result)))
    (is (false? (:authoritative? result)))
    (is (true? (:fresh-authoritative-run-required? result)))
    (is (= :non-authoritative
           (get-in result [:test-var-result :authority])))
    (is (re-find #":gravity/sh07-iteration-test-var-result" (str output)))
    (is (= 1 @once-calls @each-calls))
    (is (= :begin-test-ns (first @report-events)))
    (is (= [:end-test-ns :summary] (vec (take-last 2 @report-events)))))
  (is (= "SH07-ITERATION-CACHE-TEST-VAR"
         (:id
          (ex-data
           (try
             (runner/run-selection
              (runner/parse-arguments
               ["--test-var" "gravity.cli-test/not-a-test"]))
             (catch clojure.lang.ExceptionInfo error error)))))))

(deftest iteration-cache-test-var-rejects-refers-and-reports-failures
  (require 'gravity.cli-test 'gravity.diagnostics-test)
  (let [cli-namespace (find-ns 'gravity.cli-test)]
    (binding [*ns* cli-namespace]
      (refer 'gravity.diagnostics-test
             :only '[base-exception-info-carrier-is-extracted-with-bootstrap-parity]
             :rename
             '{base-exception-info-carrier-is-extracted-with-bootstrap-parity
               borrowed-cross-owner-test}))
    (try
      (is (= "SH07-ITERATION-CACHE-TEST-VAR"
             (:id
              (ex-data
               (try
                 (runner/run-selection
                  (runner/parse-arguments
                   ["--test-var"
                    "gravity.cli-test/borrowed-cross-owner-test"]))
                 (catch clojure.lang.ExceptionInfo error error))))))
      (finally
        (ns-unmap cli-namespace 'borrowed-cross-owner-test)))
    (let [failing-var (intern cli-namespace 'synthetic-failing-test)]
      (alter-meta!
       failing-var assoc :test
       #(clojure.test/do-report
         {:type :fail
          :message "synthetic failure"
          :expected true
          :actual false}))
      (try
        (let [output (java.io.StringWriter.)
              result
              (binding [*out* output
                        clojure.test/*test-out* output]
                (runner/run-selection
                 (runner/parse-arguments
                  ["--test-var"
                   "gravity.cli-test/synthetic-failing-test"])))]
          (is (false? (:ok? result)))
          (is (= 1 (get-in result [:test-result :fail])))
          (is (zero? (get-in result [:test-result :error])))
          (is (= :non-authoritative (:authority result))))
        (finally
          (ns-unmap cli-namespace 'synthetic-failing-test))))))

(deftest argument-selection-is-explicit-and-owned
  (testing "repeatable namespaces and cache bound"
    (is (= {:namespaces '[gravity.diagnostics-test gravity.cli-test]
            :maximum-entries 2
            :fail-fast? false
            :test-var nil}
           (runner/parse-arguments
            ["--namespace" "gravity.diagnostics-test"
             "--max-cache-entries" "2"
             "--namespace" "gravity.cli-test"]))))
  (testing "fail-fast is explicit and order independent"
    (is (= true
           (:fail-fast?
            (runner/parse-arguments
             ["--fail-fast"
              "--namespace" "gravity.cli-test"])))))
  (testing "one owned namespace-qualified test var is accepted"
    (is (= 'gravity.cli-test/presentation-values-are-extracted-with-bootstrap-parity
           (:test-var
            (runner/parse-arguments
             ["--test-var"
              "gravity.cli-test/presentation-values-are-extracted-with-bootstrap-parity"]))))
    (is (= "SH07-ITERATION-CACHE-SELECTION-CONFLICT"
           (:id
            (ex-data
             (try
               (runner/parse-arguments
               ["--namespace" "gravity.cli-test"
                 "--test-var"
                 "gravity.cli-test/presentation-values-are-extracted-with-bootstrap-parity"])
               (catch clojure.lang.ExceptionInfo error error))))))
    (doseq [[arguments expected-id]
            [[["--test-var" "unqualified-test"]
              "SH07-ITERATION-CACHE-TEST-VAR"]
             [["--test-var"
               "gravity.cli-test/presentation-values-are-extracted-with-bootstrap-parity"
               "--test-var"
               "gravity.cli-test/cli-namespace-contract-is-narrow-and-acyclic"]
              "SH07-ITERATION-CACHE-DUPLICATE-TEST-VAR"]
             [["--fail-fast"
               "--test-var"
               "gravity.cli-test/presentation-values-are-extracted-with-bootstrap-parity"]
              "SH07-ITERATION-CACHE-FAIL-FAST-SELECTION"]]]
      (is (= expected-id
             (:id
              (ex-data
               (try
                 (runner/parse-arguments arguments)
                 (catch clojure.lang.ExceptionInfo error error))))))))
  (testing "zero work and invalid bounds fail closed"
    (is (= "SH07-ITERATION-CACHE-SELECTION"
           (:id (ex-data (try (runner/parse-arguments [])
                              (catch clojure.lang.ExceptionInfo error
                                error))))))
    (is (= "SH07-ITERATION-CACHE-ARGUMENT"
           (:id (ex-data
                 (try
                   (runner/parse-arguments
                    ["--namespace" "gravity.cli-test"
                     "--max-cache-entries" "0"])
                   (catch clojure.lang.ExceptionInfo error error)))))))
  (testing "unowned namespaces are rejected by the coordinator"
    (is (= "SH01-TEST-NAMESPACE"
           (:id (ex-data
                 (try
                   (runner/parse-arguments
                   ["--namespace" "gravity.not-owned-test"])
                   (catch clojure.lang.ExceptionInfo error error)))))))
  (testing "duplicate namespace work is rejected"
    (is (= "SH07-ITERATION-CACHE-DUPLICATE-NAMESPACE"
           (:id (ex-data
                 (try
                   (runner/parse-arguments
                    ["--namespace" "gravity.cli-test"
                     "--namespace" "gravity.cli-test"])
                   (catch clojure.lang.ExceptionInfo error error))))))))
