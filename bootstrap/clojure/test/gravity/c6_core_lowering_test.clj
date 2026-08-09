(ns gravity.c6-core-lowering-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c6-core-lowering :as c6]))

(def module
  {:module 'gravity.c6-test
   :source-path "c6-test.gravity"
   :profile :hosted
   :target :jvm
   :effects #{}
   :capabilities #{}
   :safety :safe
   :metadata {}})

(def c5-artifact
  {:kind :gravity/stage0-c5-name-resolution-artifact
   :artifact-id "sha256:c5"
   :namespace-analysis {:namespace 'gravity.c6-test}
   :binding-table {:bindings [{:binding-id "binding-0"}]}
   :alias-table {}
   :dependency-graph {:nodes [] :edges []}})

(defn syntax
  [id form]
  {:syntax-id id
   :form form
   :span {:source "c6-test.gravity" :form-index id}
   :origin :source
   :generated-origin []
   :metadata {}})

(deftest contract-is-hosted-compatible-and-nonauthoritative
  (let [contract (c6/c6-engine-contract)
        publics (set (keys (ns-publics 'gravity.c6-core-lowering)))]
    (is (= :hosted-stage0-c6-core-lowering-engine
           (:contract-boundary contract)))
    (is (true? (:compatibility-only? contract)))
    (is (false? (:canonical-sh07-authority? contract)))
    (is (= :gravity.bootstrap-sh06-adapter (:authority-boundary contract)))
    (is (= ['c6-lowering-artifact] (:leaf-only-api contract)))
    (is (some #{:proof-authority} (:does-not-own contract)))
    (is (some #{:release} (:does-not-own contract)))
    (is (= #{'clojure.set 'gravity.digest}
           (set (get-in contract [:dependency-direction :requires]))))
    (is (= #{'gravity.bootstrap 'gravity.diagnostics}
           (set (get-in contract [:dependency-direction :forbids]))))
    (is (= publics (set (keys (:public-api contract)))))
    (doseq [[name spec] c6/public-api
            :when (:arglists spec)]
      (is (= (:arglists spec)
             (:arglists (meta (get (ns-publics 'gravity.c6-core-lowering)
                                   name))))))
    (is (not (contains? (set (keys (ns-aliases
                                    'gravity.c6-core-lowering)))
                        'gravity.bootstrap)))
    (is (not (contains? (set (keys (ns-aliases
                                    'gravity.c6-core-lowering)))
                        'gravity.diagnostics)))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest operation-map-is-validated-and-supports-interposition
  (doseq [operations [nil
                      {:unknown identity}
                      {:c6-lower-form :callable-keyword}
                      {:core-forms #{:not-a-symbol}}
                      {:known-source-profiles #{}}
                      {:c6-lowering-diagnostic-ids [:not-a-string]}
                      {:c6-lowering-governing-document ""}
                      {:c6-lowering-rejected-designs [:not-a-map]}
                      {:c6-lowering-override-diagnostics {:gap :not-a-string}}
                      {:c6-domain-boundary-operators #{:not-a-symbol}}
                      {:c6-core-node-forms #{42}}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (c6/with-operations operations (constantly :unreachable)))))
  (let [calls (atom [])
        sentinel {:artifact :gravity/core-node :node-id "sentinel"}]
    (is (= [sentinel]
           (c6/with-operations
             {:c6-lower-form (fn [& args]
                               (swap! calls conj args)
                               sentinel)}
             #(c6/c6-lower-children (atom 0) module (syntax 0 '(do)) [1]))))
    (is (= 1 (count @calls))))
  (is (= :wasm
         (:target
          (c6/with-operations
            {:supported-targets #{:jvm :wasm}}
            #(c6/c6-core-node "node" :literal (syntax 0 1)
                              (assoc module :target :wasm)
                              {:surface-form 1}))))))

(deftest lowering-engine-emits-core-products
  (let [forms ['(quote value)
               '(if condition then else)
               '(do first second)
               '(let [x 1] x)
               '(fn [x] x)
               '(loop [x 1] (recur x))
               '(recur x)
               '(def answer 42)
               '(var answer)
               '(set! answer 43)
               '(try body (catch Error e e))
               '(throw error)
               '(match value pattern result)]
        roots (mapv (fn [[index form]]
                      (c6/c6-lower-form (atom 0) module
                                         (syntax index form) form))
                    (map-indexed vector forms))
        stream (vec (concat [(syntax "ns" '(ns gravity.c6-test))]
                            (map-indexed syntax forms)
                            [(syntax "domain" '(defschema User {}))]))
        artifact (c6/c6-lowering-artifact "c6-test.gravity" module
                                           c5-artifact stream)]
    (is (= '[quote if do let fn loop recur def var set! try throw match]
           (mapv :form roots)))
    (is (= :gravity/stage0-c6-core-lowering-artifact (:kind artifact)))
    (is (= :passed (get-in artifact [:core-verifier-report :status])))
    (is (= :complete (get-in artifact [:surface-to-core-map :status])))
    (is (= :complete (get-in artifact [:desugaring-trace :status])))
    (is (= :complete (get-in artifact [:evaluation-order-records :status])))
    (is (= [:schema-ir] (mapv :domain (:domain-boundary-records artifact))))
    (is (= (set c6/c6-lowering-diagnostic-ids)
           (set (map :diagnostic (:rejected-design-coverage artifact)))))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id artifact)))))

(deftest diagnostic-overrides-retain-c6-identities
  (doseq [[failure expected-id]
          [[:gap "C6-LOWERING-GAP"]
           [:core-shape "C6-CORE-SHAPE"]
           [:eval-order "C6-EVAL-ORDER"]
           [:origin "C6-ORIGIN"]
           [:effect-drop "C6-EFFECT-DROP"]
           [:unsafe-drop "C6-UNSAFE-DROP"]
           [:domain-boundary "C6-DOMAIN-BOUNDARY"]
           [:verify "C6-VERIFY"]]]
    (testing expected-id
      (let [failed-module (assoc-in module
                                    [:metadata :compiler :c6-lowering :fail]
                                    failure)
            error (try
                    (c6/c6-lowering-artifact
                     "c6-test.gravity" failed-module c5-artifact
                     [(syntax 0 '(ns gravity.c6-test)) (syntax 1 42)])
                    nil
                    (catch clojure.lang.ExceptionInfo exception exception))]
        (is (some? error))
        (is (= expected-id (:id (ex-data error))))
        (is (= "C6" (:document-id (ex-data error))))))))
