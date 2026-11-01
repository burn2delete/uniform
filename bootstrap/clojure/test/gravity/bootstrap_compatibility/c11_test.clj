(ns gravity.bootstrap-compatibility.c11-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c11-mir :as c11]
            [gravity.c11-mir.artifact :as artifact]))

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
  ;; Facade/configuration oracle only: the real wrapper and operation bindings
  ;; run, but no C11 product or upstream authenticated artifact is constructed.
  (let [calls (atom [])
        sentinel (Object.)
        source-path (fixture "accepted/compiler-c11-mir-spec.gravity")
        result
        (with-redefs [bootstrap/c11-mir-diagnostic-ids ["C11-SENTINEL"]
                      bootstrap/c11-mir-rejected-designs
                      [{:diagnostic "C11-SENTINEL"}]
                      bootstrap/c11-mir-governing-document
                      "docs/c11-sentinel.md"
                      artifact/file-artifact
                      (fn [configuration path]
                        (swap! calls conj [configuration path])
                        sentinel)]
          (bootstrap/compiler-c11-mir-file-artifact source-path))
        [configuration path] (first @calls)]
    (is (identical? sentinel result))
    (is (= 1 (count @calls)))
    (is (= source-path path))
    (is (= "docs/c11-sentinel.md" (:c11-mir-governing-document configuration)))
    (is (= ["C11-SENTINEL"] (:c11-mir-diagnostic-ids configuration)))
    (is (= [{:diagnostic "C11-SENTINEL"}] (:c11-mir-rejected-designs configuration))))
  (is (= (:public-api (c11/c11-engine-contract)) c11/public-api)))
