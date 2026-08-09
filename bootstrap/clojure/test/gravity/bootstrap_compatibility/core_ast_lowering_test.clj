(ns gravity.bootstrap-compatibility.core-ast-lowering-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.core-ast-lowering :as core-ast-lowering]))

(def source-path "synthetic/core-ast.gravity")
(def source-text "synthetic core AST input")

(def module
  {:module 'demo.core-ast
   :source-path source-path
   :profile :hosted
   :target :jvm
   :effects #{:io/write :error/throw}
   :capabilities #{:io/stdout}
   :safety :safe
   :metadata {}})

(defn syntax
  [index form]
  {:syntax-id (str "syntax-" index)
   :form form
   :span {:source source-path :form-index index}
   :profile :hosted
   :namespace 'demo.core-ast
   :generated-origin [{:kind :source :index index}]
   :metadata nil})

(def macro-artifact
  {:module module
   :macro-expansion-trace [{:kind :synthetic}]
   :expanded-syntax-object-stream
   [(syntax 0 '(ns demo.core-ast))
    (syntax 1 '(def answer 42))
    (syntax 2 '(do (println answer) (if answer 1 0)))]})

(defn core-ast-lowering-operations
  []
  {:fail! bootstrap/fail!
   :macro-source-artifact bootstrap/macro-source-artifact
   :uses-println? bootstrap/uses-println?
   :core-forms bootstrap/core-forms
   :lowering-gap-forms bootstrap/lowering-gap-forms
   :unknown-reserved-core-forms bootstrap/unknown-reserved-core-forms
   :form-effect bootstrap/form-effect
   :combine-effects bootstrap/combine-effects
   :core-node bootstrap/core-node
   :lower-sequential-body bootstrap/lower-sequential-body
   :extract-pattern-guard bootstrap/extract-pattern-guard
   :lower-match-clauses bootstrap/lower-match-clauses
   :next-node-id bootstrap/next-node-id
   :assert-recur-target! bootstrap/assert-recur-target!
   :assert-set-target! bootstrap/assert-set-target!
   :assert-throw-legal! bootstrap/assert-throw-legal!
   :assert-core-operator! bootstrap/assert-core-operator!
   :lower-core-expr bootstrap/lower-core-expr
   :flatten-core bootstrap/flatten-core
   :core-source-artifact bootstrap/core-source-artifact})

(deftest core-ast-lowering-compatibility-wrappers-preserve-arglists-output-and-interposition
  (doseq [[wrapper-var expected]
          [[#'bootstrap/form-effect '([form])]
           [#'bootstrap/combine-effects '([& effect-sets])]
           [#'bootstrap/core-node '([node-id kind syntax form data])]
           [#'bootstrap/lower-sequential-body
            '([counter module syntax forms context])]
           [#'bootstrap/extract-pattern-guard '([pattern])]
           [#'bootstrap/lower-match-clauses
            '([counter module syntax clauses context])]
           [#'bootstrap/next-node-id '([counter])]
           [#'bootstrap/assert-recur-target!
            '([module syntax form context])]
           [#'bootstrap/assert-set-target! '([module syntax form])]
           [#'bootstrap/assert-throw-legal! '([module syntax form])]
           [#'bootstrap/assert-core-operator! '([module syntax form])]
           [#'bootstrap/lower-core-expr
            '([counter module syntax form context])]
           [#'bootstrap/flatten-core '([node])]
           [#'bootstrap/core-source-artifact '([source-path source-text])]]]
    (is (= expected (:arglists (meta wrapper-var)))))

  (is (= core-ast-lowering/core-forms bootstrap/core-forms))
  (is (= core-ast-lowering/lowering-gap-forms
         bootstrap/lowering-gap-forms))
  (is (= core-ast-lowering/unknown-reserved-core-forms
         bootstrap/unknown-reserved-core-forms))

  (with-redefs [bootstrap/macro-source-artifact
                (fn [_source-path _source-text] macro-artifact)]
    (let [wrapper-artifact
          (bootstrap/core-source-artifact source-path source-text)
          direct-artifact
          (core-ast-lowering/with-operations
           (core-ast-lowering-operations)
           #(core-ast-lowering/call-entrypoint-body
             :core-source-artifact
             core-ast-lowering/core-source-artifact
             [source-path source-text]))]
      (is (= wrapper-artifact direct-artifact))))

  (let [forms-seen (atom [])
        captured-lower-core-expr bootstrap/lower-core-expr
        form '(do 1 2)]
    (with-redefs [bootstrap/lower-core-expr
                  (fn [counter active-module active-syntax active-form context]
                    (swap! forms-seen conj active-form)
                    (captured-lower-core-expr
                     counter active-module active-syntax active-form context))]
      (bootstrap/lower-core-expr
       (atom 0) module (syntax 3 form) form {}))
    (is (= [form 1 2] @forms-seen)))

  (let [node-calls (atom [])
        captured-core-node bootstrap/core-node]
    (with-redefs [bootstrap/core-node
                  (fn [node-id kind active-syntax form data]
                    (swap! node-calls conj [node-id kind])
                    (captured-core-node
                     node-id kind active-syntax form data))]
      (bootstrap/lower-core-expr
       (atom 0) module (syntax 4 '(if true 1 0)) '(if true 1 0) {}))
    (is (= [[1 :literal] [2 :literal] [3 :literal] [0 :if]]
           @node-calls)))

  (let [operation-bindings (atom 0)
        captured-with-operations core-ast-lowering/with-operations]
    (with-redefs [core-ast-lowering/with-operations
                  (fn [operations thunk]
                    (swap! operation-bindings inc)
                    (captured-with-operations operations thunk))]
      (bootstrap/lower-core-expr
       (atom 0) module (syntax 5 '(do 1 2)) '(do 1 2) {}))
    (is (= 1 @operation-bindings))))
