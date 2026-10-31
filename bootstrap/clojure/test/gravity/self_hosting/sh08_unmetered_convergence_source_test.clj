(ns gravity.self-hosting.sh08-unmetered-convergence-source-test
  "Inert C7 source contracts; no Bootstrap, harness or Gravity evaluation."
  (:require [clojure.test :refer [deftest is]]))

(def ^:private source-path
  "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity")

(defn- read-forms [text]
  (binding [*read-eval* false]
    (with-open [reader (java.io.PushbackReader. (java.io.StringReader. text))]
      (loop [out []]
        (let [form (read {:eof ::end} reader)]
          (if (= ::end form) out (recur (conj out form))))))))

(defn- definition [forms name]
  (first (filter #(and (seq? %) (= 'defn (first %)) (= name (second %))) forms)))

(defn- binding-value [form name]
  (first (for [node (tree-seq coll? seq form)
               :when (and (seq? node) (contains? '#{let loop} (first node)))
               [key value] (partition 2 (second node))
               :when (= name key)]
           value)))

(def ^:private observed-method
  '{:status (get inference :convergence-status)
    :method (if recursive? :checked-recursive-rule :exact-inference-state-fixed-point)})

(def ^:private state-equality
  '(if (= next-functions current-functions)
     (if (= next-bindings current-bindings)
       (if (= current-types (get pass :type-table))
         (if (= current-shapes (get pass :binding-shapes))
           (= current-loop-products (get pass :loop-member-products)) false) false) false) false))

(defn- source-view [forms]
  (let [infer (definition forms 'sh08-ft-infer-acyclic-with-context)
        finish (definition forms 'sh08-ft-finish-function-type-core)
        ingress (definition forms 'sh08-ft-function-type-core-artifact-with-imports)]
    {:arguments (nth infer 2)
     :convergence (binding-value finish 'convergence)
     :stable (binding-value infer 'stable?)
     :recursive (binding-value ingress 'recursive?)}))

(defn- contract-errors [view]
  (cond-> #{}
    (not= '[node-sequence node-table lexical-bindings definitions calls edges
            function-types binding-types context] (:arguments view)) (conj :unmetered-interface)
    (not= observed-method (:convergence view)) (conj :descriptive-method)
    (not= state-equality (:stable view)) (conj :observed-equality)
    (not= '(= :accepted (get recursive-proof :status)) (:recursive view)) (conj :checked-recursive-rule)))

(deftest source-records-describe-only-the-observed-method
  (let [forms (read-forms (slurp source-path))
        view (source-view forms)]
    (is (= #{} (contract-errors view)))
    (is (= #{:status :method} (set (keys (:convergence view)))))
    (is (not-any? #{:monotone-finite-type-fact-propagation}
                  (tree-seq coll? seq forms)))
    (doseq [name '[sh08-ft-effective-work-policy sh08-ft-invalid-work-policy
                  sh08-ft-work-allowance sh08-ft-inference-round-bound]]
      (is (nil? (definition forms name))))
    (is (nil? (find-ns 'gravity.bootstrap)))
    (is (nil? (find-ns 'gravity.self-hosting.sh08-function-call-type-test)))))

(deftest source-counterexamples-cannot-satisfy-the-contract
  (let [view (source-view (read-forms (slurp source-path)))]
    (doseq [[changed expected]
            [[(assoc-in view [:convergence :proof] :monotone-finite-type-fact-propagation)
              :descriptive-method]
             [(assoc-in view [:convergence :method] :exact-inference-state-fixed-point)
              :descriptive-method]
             [(assoc-in view [:convergence :method]
                        '(if recursive? :exact-inference-state-fixed-point :checked-recursive-rule))
              :descriptive-method]
             [(update view :arguments conj 'maximum-rounds) :unmetered-interface]
             [(assoc view :stable '(= next-functions current-functions)) :observed-equality]
             [(assoc view :recursive true) :checked-recursive-rule]]]
      (is (= #{expected} (contract-errors changed))))))
