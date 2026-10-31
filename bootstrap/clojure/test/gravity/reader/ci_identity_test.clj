(ns gravity.reader.ci-identity-test
  "Compiler-free source projections for current Reader identity dependencies."
  (:require [clojure.test :refer [deftest is]]
            [clojure.walk :as walk]))

(defn- source-forms [path]
  (binding [*read-eval* false]
    (with-open [reader (java.io.PushbackReader. (java.io.StringReader. (slurp path)))]
      (loop [result []]
        (let [form (read {:eof ::end} reader)]
          (if (= ::end form) result (recur (conj result form))))))))

(defn- named-form [forms name]
  (or (some #(when (and (seq? %) (= name (second %))) %) forms)
      (throw (ex-info "Missing inspected source form" {:name name}))))

(defn- model-function [form ports]
  (let [model (create-ns (gensym "reader-ci-identity-model-"))]
    (try
      (binding [*ns* model]
        (clojure.core/refer 'clojure.core)
        (doseq [[name function] ports] (intern model name function))
        (eval form)
        (var-get (ns-resolve model (second form))))
      (finally (remove-ns (ns-name model))))))

(deftest syntax-native-admission-dependency-is-explicit-and-acyclic
  (let [forms (source-forms "bootstrap/clojure/src/gravity/syntax.clj")
        requires (->> (first forms) (filter seq?) (filter #(= :require (first %)))
                      first rest (mapv first))
        contract (second (nth (named-form forms 'namespace-contract) 2))
        dependency (source-forms "bootstrap/clojure/src/gravity/reader/pure_text.clj")
        leaf-requires (->> (first dependency) (filter seq?) (filter #(= :require (first %)))
                           first rest (mapv first))]
    (is (= '[clojure.string gravity.reader.pure-text gravity.c3-reader-expansions gravity.c3-syntax-verification]
           requires))
    (is (= (vec (cons 'clojure.core requires)) (get-in contract [:dependency-direction :requires])))
    (is (= '[gravity.c2-artifact-identity.canonical gravity.digest] leaf-requires))
    (is (not-any? #{'gravity.bootstrap 'gravity.cli 'gravity.release} leaf-requires))
    (is (not= (vec (cons 'clojure.core (remove #{'gravity.reader.pure-text} requires)))
              (get-in contract [:dependency-direction :requires])))))

(deftest macro-parent-and-implementation-identities-survive-the-actual-projection
  (let [forms (source-forms "bootstrap/clojure/src/gravity/macro.clj")
        form (named-form forms 'artifact-payload)
        operations {:sh05-macro-run-semantic-summary identity
                    :sh05-ordered-identity-chunks (fn [_ _] [])
                    :sh05-path-neutral-semantic-value identity}
        project (model-function form {})
        fixture {:gravity-macro-boundary {:plan-binding {:source-content-hash "Macro-implementation-A"}}
                 :sh04-syntax-artifact {:artifact-id "C3-A" :syntax-stream-id "syntax-A"}}
        expected (:sh04-syntax-artifact fixture)
        preserves? (fn [f] (= expected (:sh04-syntax-artifact (f fixture operations))))]
    (is (preserves? project))
    (doseq [path [[:sh04-syntax-artifact :artifact-id]
                  [:sh04-syntax-artifact :syntax-stream-id]
                  [:gravity-macro-boundary :plan-binding :source-content-hash]]]
      (is (not= (project fixture operations) (project (assoc-in fixture path "changed") operations))))
    ;; A production mutation that drops the authenticated C3 parent is detected.
    (let [mutant (walk/postwalk #(if (map? %) (dissoc % :sh04-syntax-artifact) %) form)]
      (is (false? (preserves? (model-function mutant {})))))
    (let [mutant (walk/postwalk #(if (= :source-content-hash %) :lost-source-hash %) form)
          actual ((model-function mutant {}) fixture operations)]
      (is (not= "Macro-implementation-A" (get-in actual [:plan-binding :source-content-hash]))))))

(deftest resolution-retains-every-upstream-join-in-the-actual-preimage
  (let [forms (source-forms "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00877-sh06-resolution-product-projections.clj")
        form (named-form forms 'sh06-resolution-artifact-identity-input-with-chunks)
        ;; Identity instead of hashing exposes the complete dependency DATA.
        ports {'reader-canonical-hash identity}
        project (model-function form ports)
        fixture {:gravity-resolution-boundary
                 {:resolved-analysis {:artifact-id "analysis-A"}
                  :authenticated-resolution-request {:module {:source-revision-id "source-A"}}}
                 :sh05-macro-artifact {:artifact-id "macro-A" :expanded-syntax-stream-id "syntax-A"
                                      :macro-expansion-trace-id "trace-A"}}
        expected {:analysis-artifact-id "analysis-A" :source-revision-id "source-A"
                  :upstream-artifact-id "macro-A" :upstream-syntax-stream-id "syntax-A"
                  :upstream-trace-id "trace-A"}
        fields (set (keys expected))
        preserves? (fn [f] (= expected (select-keys (f fixture {}) fields)))]
    (is (preserves? project))
    (doseq [field fields]
      (let [mutant (walk/postwalk #(if (map? %) (dissoc % field) %) form)]
        (is (false? (preserves? (model-function mutant ports))) (str "lost join " field))))))
