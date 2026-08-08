(ns gravity.c3-syntax-evidence-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c3-syntax-evidence :as evidence]))

(def source-syntax
  {:syntax/id "syntax-source"
   :form {:kind :symbol}
   :profile :hosted
   :metadata {:doc "source"}
   :hygiene {:marks [:source-mark]
             :lexical-scopes [:scope]
             :renames {'x 'x__1}
             :captures []
             :introduced-identifiers ['x__1]
             :macro-definition-namespace 'macro.ns
             :macro-call-site-namespace 'call.ns}
   :origin [{:kind :source :span :source-span}]
   :prior-syntax-ids []})

(def generated-syntax
  {:syntax/id "syntax-generated"
   :form {:kind :generated-form}
   :profile :hosted
   :metadata {:generated true}
   :hygiene {:marks [:generated-mark] :captures []}
   :origin [{:kind :generated
             :producer {:kind :macro :name 'demo/m}
             :inputs ["syntax-source"]
             :generated-span "generated:demo/m:1"}]
   :prior-syntax-ids ["syntax-source"]})

(deftest schema-and-stream-projections-preserve-exact-c3-shapes
  (let [stream [source-syntax generated-syntax]]
    (is (= evidence/c3-required-form-kinds
           (:form-kinds (evidence/c3-syntax-schema))))
    (is (= [:only] (:form-kinds (evidence/c3-syntax-schema [:only]))))
    (is (= [{:syntax-id "syntax-source"
             :marks [:source-mark]
             :lexical-scopes [:scope]
             :renames {'x 'x__1}
             :captures []
             :introduced-identifiers ['x__1]
             :macro-definition-namespace 'macro.ns
             :macro-call-site-namespace 'call.ns}
            {:syntax-id "syntax-generated"
             :marks [:generated-mark]
             :lexical-scopes nil
             :renames nil
             :captures []
             :introduced-identifiers nil
             :macro-definition-namespace nil
             :macro-call-site-namespace nil}]
           (:contexts (evidence/c3-hygiene-context-map stream))))
    (is (= [{:syntax-id "syntax-source"
             :origin (:origin source-syntax)
             :prior-syntax-ids []}
            {:syntax-id "syntax-generated"
             :origin (:origin generated-syntax)
             :prior-syntax-ids ["syntax-source"]}]
           (:nodes (evidence/c3-origin-chain-graph stream))))
    (is (= [{:syntax-id "syntax-source"
             :action :preserved
             :metadata {:doc "source"}}]
           (:source-metadata (evidence/c3-metadata-ledger stream))))
    (is (= 'compiler.c3/with-capture-demo
           (get-in (evidence/c3-metadata-ledger stream)
                   [:explicit-changes 0 :producer])))
    (is (= "syntax-generated"
           (get-in (evidence/c3-fact-ledger stream)
                   [:invalidated 0 :successor-syntax-id])))
    (is (= [{:syntax-id "syntax-generated"
             :producer {:kind :macro :name 'demo/m}
             :input-syntax-ids ["syntax-source"]
             :expansion-step 1
             :generated-span "generated:demo/m:1"
             :caller-profile :hosted
             :hygiene (:hygiene generated-syntax)}]
           (:generated (evidence/c3-generated-syntax-report stream))))))

(deftest projection-boundaries-retain-nil-empty-and-eager-behavior
  (testing "empty and nil streams keep legacy projection shapes"
    (doseq [stream [nil [] '()]]
      (is (= [] (:contexts (evidence/c3-hygiene-context-map stream))))
      (is (= [] (:nodes (evidence/c3-origin-chain-graph stream))))
      (is (= [] (:generated (evidence/c3-generated-syntax-report stream))))
      (is (= [] (:source-metadata (evidence/c3-metadata-ledger stream))))
      (is (= nil (get-in (evidence/c3-fact-ledger stream)
                         [:attached 0 :syntax-id])))))
  (testing "mapv projections realize ordered lazy streams before returning"
    (let [seen (atom [])
          stream (map (fn [syntax]
                        (swap! seen conj (:syntax/id syntax))
                        syntax)
                      [source-syntax generated-syntax])
          result (evidence/c3-origin-chain-graph stream)]
      (is (= ["syntax-source" "syntax-generated"] @seen))
      (is (vector? (:nodes result))))))

(deftest reference-values-and-generated-fallbacks-are-not-normalized
  (let [metadata (Object.)
        hygiene (Object.)
        syntax {:syntax/id :generated
                :form {:kind :generated-form}
                :metadata metadata
                :hygiene hygiene
                :origin [{:producer :producer
                          :input-syntax-ids [:preferred]
                          :inputs [:fallback]
                          :span :preferred-span
                          :generated-span :fallback-span}]}
        report (first (:generated
                       (evidence/c3-generated-syntax-report [syntax])))
        ledger (evidence/c3-metadata-ledger [syntax])]
    (is (= [:preferred] (:input-syntax-ids report)))
    (is (= :preferred-span (:generated-span report)))
    (is (identical? hygiene (:hygiene report)))
    (is (identical? metadata
                    (get-in ledger [:explicit-changes 0 :metadata])))
    (is (identical? (:origin syntax)
                    (:origin (first (:nodes
                                     (evidence/c3-origin-chain-graph
                                      [syntax]))))))))

(deftest contract-is-narrow-bootstrap-free-and-nonauthoritative
  (let [contract-var (get (ns-interns 'gravity.c3-syntax-evidence)
                          'namespace-contract)
        contract (var-get contract-var)]
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.c3-syntax-evidence (:namespace contract)))
    (is (= #{'c3-required-form-kinds
             'c3-syntax-schema
             'c3-hygiene-context-map
             'c3-origin-chain-graph
             'c3-metadata-ledger
             'c3-fact-ledger
             'c3-generated-syntax-report}
           (set (keys (:public-api contract)))))
    (is (= (set (keys (:public-api contract)))
           (set (keys (ns-publics 'gravity.c3-syntax-evidence)))))
    (is (= ['clojure.core]
           (get-in contract [:dependency-direction :requires])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (doseq [claim [:canonical-c3-syntax-object-authority
                   :c2-reader-product-authentication
                   :syntax-object-construction
                   :syntax-identity
                   :syntax-validation
                   :hygiene-authority
                   :proof-authority
                   :self-hosted-authority
                   :release-authority]]
      (is (some #{claim} (get-in contract [:ownership :does-not-own]))))
    (is (empty? (ns-aliases 'gravity.c3-syntax-evidence)))
    (is (nil? (find-ns 'gravity.bootstrap)))
    (is (false? (:canonical-c3-authority? contract)))
    (is (false? (:self-hosted? contract)))
    (is (false? (:release-authority? contract)))))
