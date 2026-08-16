(ns gravity.bootstrap-compatibility.c6-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c6-core-lowering :as c6]))

(defn fixture
  [name]
  (str "bootstrap/clojure/fixtures/" name))

(deftest c6-lowering-compatibility-wrappers-preserve-arglists-and-interposition
  (is (= '([id source-path subject extra])
         (:arglists (meta #'bootstrap/c6-lowering-fail!))))
  (is (= '([counter module syntax form])
         (:arglists (meta #'bootstrap/c6-lower-form))))
  (is (= '([source-path source-text])
         (:arglists (meta #'bootstrap/compiler-c6-lowering-source-artifact))))
  (is (= '([path])
         (:arglists (meta #'bootstrap/compiler-c6-lowering-file-artifact))))
  (let [calls (atom [])
        sentinel {:artifact :gravity/core-node :node-id "sentinel"}]
    (is (= [sentinel]
           (with-redefs [bootstrap/c6-lower-form
                         (fn [& args]
                           (swap! calls conj args)
                           sentinel)]
             (bootstrap/c6-lower-children
              (atom 0)
              {:module 'gravity.c6-test :profile :hosted :target :jvm}
              {:syntax-id "syntax" :form '(do 1)}
              [1]))))
    (is (= 1 (count @calls))))
  (let [bindings (atom 0)
        with-operations c6/with-operations]
    (with-redefs [c6/with-operations
                  (fn [operations thunk]
                    (swap! bindings inc)
                    (with-operations operations thunk))]
      (bootstrap/c6-lower-form
       (atom 0)
       {:module 'gravity.c6-test :source-path "probe.gravity"
        :profile :hosted :target :jvm :capabilities #{}}
       {:syntax-id "syntax" :form '(do 1 2)
        :span {:source "probe.gravity" :form-index 0}}
       '(do 1 2)))
    (is (= 1 @bindings)))
  (let [error (with-redefs [bootstrap/c6-lowering-message
                            (constantly "interposed C6 message")]
                (try
                  (bootstrap/c6-lowering-fail!
                   "C6-VERIFY" "probe.gravity" {:syntax-id "probe"} {})
                  nil
                  (catch clojure.lang.ExceptionInfo exception exception)))]
    (is (= "interposed C6 message" (.getMessage error)))
    (is (= "C6-VERIFY" (:id (ex-data error)))))
  (let [artifact
        (with-redefs [bootstrap/c6-lowering-diagnostic-ids ["C6-SENTINEL"]
                      bootstrap/c6-lowering-rejected-designs
                      [{:diagnostic "C6-SENTINEL"}]
                      bootstrap/c6-lowering-governing-document
                      "docs/c6-sentinel.md"]
          (bootstrap/compiler-c6-lowering-file-artifact
           (fixture "accepted/compiler-c6-core-lowering.gravity")))]
    (is (= "docs/c6-sentinel.md" (:governing-document artifact)))
    (is (= ["C6-SENTINEL"]
           (get-in artifact [:c6-lowering-results
                             :required-diagnostic-ids])))
    (is (= [{:diagnostic "C6-SENTINEL"}]
           (:rejected-design-coverage artifact))))
  (is (= (:public-api (c6/c6-engine-contract)) c6/public-api)))
