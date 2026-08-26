(ns gravity.bootstrap-compatibility.c11-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c11-mir :as c11]))

(defn fixture
  [name]
  (str "bootstrap/clojure/fixtures/" name))

(deftest c11-mir-compatibility-wrappers-preserve-interposition
  (is (= '([module span outcome-by-index index family])
         (:arglists (meta #'bootstrap/c11-mir-operation))))
  (is (= '([]) (:arglists (meta #'bootstrap/c11-domain-anchor-table))))
  (is (= '([source-path source-text])
         (:arglists (meta #'bootstrap/compiler-c11-mir-source-artifact))))
  (is (= '([path])
         (:arglists (meta #'bootstrap/compiler-c11-mir-file-artifact))))
  (let [calls (atom [])
        bindings (atom 0)
        with-operations c11/with-operations
        operation
        (with-redefs [bootstrap/c11-family-effects
                      (fn [family]
                        (swap! calls conj family)
                        #{:interposed/effect})
                      c11/with-operations
                      (fn [operations thunk]
                        (swap! bindings inc)
                        (with-operations operations thunk))]
          (bootstrap/c11-mir-operation
           {:profile :hosted}
           {:source "c11-probe.gravity" :form-index 0}
           [{:operation "op-safe" :proof "proof-safe"}]
           0 :constant))]
    (is (= #{:interposed/effect} (:effects operation)))
    (is (= [:constant] @calls))
    (is (= 1 @bindings)))
  (let [artifact
        (with-redefs [bootstrap/c11-mir-diagnostic-ids ["C11-SENTINEL"]
                      bootstrap/c11-mir-rejected-designs
                      [{:diagnostic "C11-SENTINEL"}]
                      bootstrap/c11-mir-governing-document
                      "docs/c11-sentinel.md"]
          (bootstrap/compiler-c11-mir-file-artifact
           (fixture "accepted/compiler-c11-mir-spec.gravity")))]
    (is (= "docs/c11-sentinel.md" (:governing-document artifact)))
    (is (= ["C11-SENTINEL"]
           (get-in artifact [:c11-mir-spec-results
                             :required-diagnostic-ids])))
    (is (= #{"C11-SENTINEL"}
           (set (map :diagnostic
                     (get-in artifact
                             [:mir-diagnostic-stream :diagnostics]))))))
  (is (= (:public-api (c11/c11-engine-contract)) c11/public-api)))
