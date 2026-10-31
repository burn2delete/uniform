(ns gravity.reader.plan-selection-test
  "Source/DATA function-selection regression. No Reader plan is built and no
  Gravity definition, handler, provider or integration fixture is evaluated."
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as strings]
            [clojure.test :refer [deftest is]]
            [clojure.walk :as walk]))

(alias 'str (ns-name (:ns (meta #'strings/starts-with?))))

(def ^:private component-root
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/")

(defn- source-forms [path]
  (with-open [reader (java.io.PushbackReader. (io/reader path))]
    (binding [*read-eval* false]
      (loop [result []]
        (let [form (read {:eof ::end} reader)]
          (if (= ::end form) result (recur (conj result form))))))))

(defn- named-form [forms name]
  (or (some #(when (and (seq? %) (= name (second %))) %) forms)
      (throw (ex-info "Required production definition missing" {:name name}))))

(defn- selector []
  (let [builder (named-form
                 (source-forms (str component-root "00809-sh03-reader-plan-instruction-summary.clj"))
                 'sh03-reader-build-plan!)
        ;; Read the actual let binding, including its predicate and prefix use.
        bindings (second (nth builder 3))
        expression (get (into {} (map vec (partition 2 bindings))) 'selected-functions)
        prefix (nth (named-form
                      (source-forms (str component-root "00808-sh03-reader-expected-source-byte-count.clj"))
                      'sh03-reader-function-prefix) 2)]
    (assert (= '(into (sorted-map)) (take 2 expression)))
    (assert (= 'all-functions (last expression)))
    {:prefix prefix
     ;; Evaluate only the production collection selection expression on DATA.
     ;; Never evaluate the builder, compiler or any Gravity function body.
     :select (binding [*ns* (the-ns 'gravity.reader.plan-selection-test)]
               (eval (list 'fn '[all-functions sh03-reader-function-prefix] expression)))}))

(defn- function-table []
  (into (sorted-map)
        (for [form (source-forms "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
              :when (and (seq? form) (= 'defn (first form)))]
          [(second form) form])))

(defn- calls [table definition]
  (into #{}
        (keep #(when (and (seq? %) (symbol? (first %))
                          (contains? table (first %))) (first %)))
        (tree-seq coll? seq (drop 3 definition))))

(defn- missing-selected-edges [table selected root]
  ;; Check the transitive defined-function call closure, not a duplicated
  ;; starts-with rule or an assertion that the new spelling looks correct.
  (loop [pending #{root} visited #{} missing #{}]
    (if (empty? pending)
      missing
      (let [caller (first pending)
            callees (calls table (get table caller))
            absent (set/difference callees (set (keys selected)))]
        (recur (into (disj pending caller) (set/difference callees visited #{caller}))
               (conj visited caller)
               (into missing (map #(vector caller %) absent)))))))

(deftest policy-helper-survives-production-plan-selection
  (let [{:keys [select prefix]} (selector)
        table (function-table)
        selected (select table prefix)]
    (is (contains? table 'sh03-reader-pure-tags?))
    (is (contains? selected 'sh03-reader-policy-valid?))
    (is (contains? (calls table (get table 'sh03-reader-policy-valid?))
                   'sh03-reader-pure-tags?))
    (is (contains? selected 'sh03-reader-pure-tags?))
    (is (empty? (missing-selected-edges table selected 'sh03-reader-policy-valid?)))))

(deftest dropped-prefix-exposes-the-missing-call-edge
  (let [{:keys [select prefix]} (selector)
        helper 'sh03-reader-pure-tags?
        dropped (symbol (subs (name helper) (count prefix)))
        ;; Change both DEF and CALL as DATA to reproduce the reviewed defect.
        table (walk/postwalk-replace {helper dropped} (function-table))
        selected (select table prefix)]
    (is (contains? table dropped))
    (is (not (contains? selected dropped)))
    (is (= #{['sh03-reader-policy-valid? dropped]}
           (missing-selected-edges table selected 'sh03-reader-policy-valid?)))))

(deftest sealed-producer-and-fresh-verifier-closures-are-selected
  (let [{:keys [select prefix]} (selector)
        table (function-table)
        selected (select table prefix)]
    (doseq [name '[sh03-reader-read-text sh03-reader-sealed-text-record
                  sh03-reader-sealed-text-forms-valid?]]
      (is (contains? selected name)))
    (doseq [root '[sh03-read-source-unit sh03-verify-reader-result]]
      (is (empty? (missing-selected-edges table selected root))))))
