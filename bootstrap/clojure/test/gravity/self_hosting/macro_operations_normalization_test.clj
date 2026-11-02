(ns gravity.self-hosting.macro-operations-normalization-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.macro-expansion :as macro-expansion]))

(def ^:private fixture-paths
  [{:path
    "bootstrap/clojure/fixtures/self-hosting/sh-07/accepted/control-flow-order.gravity"
    :baseline-normalizations 121}
   {:path
    "bootstrap/clojure/fixtures/self-hosting/sh-07/accepted/latent-function-order.gravity"
    :baseline-normalizations 173}
   {:path
    "bootstrap/clojure/fixtures/self-hosting/sh-07/accepted/quoted-carrier-payloads.gravity"
    :baseline-normalizations 198}
   {:path
    "bootstrap/clojure/fixtures/self-hosting/sh-07/b47-function-call-recursion/accepted/function-call-recursion.gravity"
    :baseline-normalizations 412}
   {:path
    "bootstrap/clojure/fixtures/self-hosting/sh-07/b49-set-mutation-execution/accepted/set-mutation-execution.gravity"
    :baseline-normalizations 620}])

(defn- private-var
  [namespace-symbol symbol]
  (or (ns-resolve namespace-symbol symbol)
      (throw (ex-info "Required normalization seam is unavailable"
                      {:id "SH07-MACRO-NORMALIZATION-SEAM"
                       :namespace namespace-symbol
                       :symbol symbol}))))

(defn- fixture-input
  [source-path]
  (let [source-text (slurp source-path)]
    {:source-text source-text
     :records (bootstrap/read-source-form-records source-path source-text)}))

(defn- run-macro-artifact
  [source-path]
  (let [{:keys [source-text records]} (fixture-input source-path)]
    (bootstrap/macro-source-artifact-from-records
     source-path source-text records)))

(defn- counted-operation
  [operation]
  (let [uncached-var (private-var 'gravity.macro-expansion
                                  'normalize-ops-uncached)
        original @uncached-var
        calls (atom 0)
        value
        (with-redefs-fn
         {uncached-var
          (fn [operations]
            (swap! calls inc)
            (original operations))}
         operation)]
    {:value value :uncached-calls @calls}))

(defn- failure-data
  [operation]
  (try
    (operation)
    nil
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))))

(deftest authentic-fixtures-normalize-operations-once
  (testing "the SH-05 macro product no longer validates the same ops map per child"
    (doseq [{:keys [path baseline-normalizations]} fixture-paths]
      (let [helper-var (private-var 'gravity.macro-expansion
                                    'with-normalized-operations)
            helper-original @helper-var
            baseline
            (counted-operation
             (fn []
               (with-redefs-fn
                {helper-var (fn [operations operation]
                              (operation operations))}
                #(run-macro-artifact path))))
            candidate (counted-operation #(run-macro-artifact path))]
        (is (= baseline-normalizations
               (:uncached-calls baseline))
            (str "baseline count for " path))
        (is (= 1 (:uncached-calls candidate))
            (str "candidate count for " path))
        (is (= (:value baseline) (:value candidate))
            (str "artifact parity for " path))
        (is (identical? helper-original @helper-var)
            (str "normalization helper restored for " path))))))

(deftest changed-operations-start-a-fresh-normalization
  (let [uncached-var (private-var 'gravity.macro-expansion
                                  'normalize-ops-uncached)
        original @uncached-var
        calls (atom 0)
        values
        (with-redefs-fn
         {uncached-var
          (fn [operations]
            (swap! calls inc)
            (original operations))}
         #(mapv
           (fn [maximum-depth]
             (macro-expansion/with-normalized-operations
              {:max-macro-expansion-depth maximum-depth}
              (fn [operations]
                [(get operations :max-macro-expansion-depth)
                 (:fixed (macro-expansion/parse-param-list '[value]
                                                            operations))])))
           [7 11]))]
    (is (= [[7 '[value]] [11 '[value]]] values))
    (is (= 2 @calls))))

(deftest forged-normalization-context-does-not-bypass-validation
  (let [context-var (private-var 'gravity.macro-expansion
                                 '*normalized-ops-context*)
        normalize-var (private-var 'gravity.macro-expansion 'normalize-ops)
        forged-context {:token (Object.) :ops {}}
        data
        (with-bindings
          {context-var forged-context}
          (failure-data
           #(normalize-var {:unexpected-operation true})))]
    (is (= "STAGE0-MACRO-EXPANSION-OPERATIONS" (:id data)))
    (is (= #{:unexpected-operation}
           (:unexpected-operation-keys data)))))

(deftest concurrent-normalization-contexts-are-isolated
  (let [uncached-var (private-var 'gravity.macro-expansion
                                  'normalize-ops-uncached)
        original @uncached-var
        calls (atom 0)
        values
        (with-redefs-fn
         {uncached-var
          (fn [operations]
            (swap! calls inc)
            (original operations))}
         #(let [first-future
                (future
                  (macro-expansion/with-normalized-operations
                   {:max-macro-expansion-depth 7}
                   (fn [operations]
                     [(get operations :max-macro-expansion-depth)
                      (:fixed (macro-expansion/parse-param-list '[x]
                                                                 operations))])))
                second-future
                (future
                  (macro-expansion/with-normalized-operations
                   {:max-macro-expansion-depth 11}
                   (fn [operations]
                     [(get operations :max-macro-expansion-depth)
                      (:fixed (macro-expansion/parse-param-list '[y]
                                                                 operations))])))]
            [@first-future @second-future]))]
    (is (= [[7 '[x]] [11 '[y]]] values))
    (is (= 2 @calls))))

(deftest normalization-context-is-discarded-after-request
  (let [context-var (private-var 'gravity.macro-expansion
                                 '*normalized-ops-context*)]
    (is (nil? @context-var))
    (is (= :completed
           (macro-expansion/with-normalized-operations
            {}
            (fn [_] :completed))))
    (is (nil? @context-var))))

(deftest public-invalid-operations-diagnostic-remains-stable
  (let [invalid {:unexpected-operation identity}
        direct (failure-data
                #(macro-expansion/parse-param-list '[value] invalid))
        wrapped (failure-data
                 #(macro-expansion/with-normalized-operations
                    invalid
                    (fn [_]
                      :unreachable)))]
    (is (= direct wrapped))
    (is (= "STAGE0-MACRO-EXPANSION-OPERATIONS" (:id direct)))
    (is (= #{:unexpected-operation}
           (:unexpected-operation-keys direct)))))
