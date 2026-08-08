(ns gravity.c3-syntax-construction-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c3-syntax-construction :as construction]))

(def seed
  {:syntax-id "stage0-syntax-0"
   :form '(demo value)
   :form-id :form-0
   :span {:source "/checkout/demo.gravity"
          :file "sha256:source"
          :form-index 0
          :byte-start 0
          :byte-end 12}
   :namespace 'demo.core
   :phase :read
   :profile :hosted
   :metadata {:doc "demo"}
   :reader-origin {:raw-excerpt "(demo value)"}
   :generated-origin []})

(def form-record
  {:form-id :form-0
   :kind :list
   :open-token :tok-0
   :close-token :tok-2})

(def token-record {:token-id :tok-0})
(def source-unit {:source-id "sha256:source"})

(deftest path-neutral-identity-and-source-object-shape-are-preserved
  (let [origin [{:kind :source
                 :span {:source "/checkout/demo.gravity" :byte-start 0}
                 :source-span {:source "/checkout/demo.gravity" :byte-end 12}
                 :from {:source "/checkout/demo.gravity" :form-index 0}}]
        neutral (mapv construction/c3-path-neutral-origin origin)
        identity (construction/c3-identity-input
                  seed neutral {:current 'demo.core} {:marks []} :list)
        object
        (construction/with-operations
         {:c3-origin-chain (fn [_ _] origin)
          :c3-source-form-kind (fn [& _] :list)
          :c3-source-facts (fn [& _] {:reader-fact :preserved})
          :sha256-hex (constantly "identity")}
         #(construction/c3-syntax-object
           seed form-record token-record source-unit {} {:authentic? true}))]
    (is (= [{:kind :source
             :span {:byte-start 0}
             :source-span {:byte-end 12}
             :from {:form-index 0}}]
           neutral))
    (is (= {:file "sha256:source"
            :form-index 0
            :byte-start 0
            :byte-end 12}
           (:span identity)))
    (is (= "sha256:identity" (:syntax/id object)))
    (is (= "sha256:identity" (get-in object [:identity :input-hash])))
    (is (= {:kind :list :value '(demo value) :raw "(demo value)"}
           (:form object)))
    (is (= {:source-id "sha256:source"
            :form-id :form-0
            :token-range [:tok-0 :tok-2]
            :token-id :tok-0}
           (:source object)))
    (is (= {:current 'demo.core :aliases {} :imports []}
           (:namespace object)))
    (is (= {:reader-fact :preserved} (:facts object)))
    (is (= origin (:origin object)))
    (is (= [] (:prior-syntax-ids object)))
    (is (true? (:immutable? object)))))

(deftest generated-object-preserves-origin-hygiene-and-prior-identity
  (let [base {:syntax/id "sha256:base"
              :profile :hosted
              :namespace {:current 'demo.core :aliases {} :imports []}
              :span {:primary {:form-index 0}}
              :source {:source-id "sha256:source"}
              :metadata {:doc "base"}}
        object (construction/with-operations
                {:sha256-hex (constantly "generated")}
                #(construction/c3-generated-syntax-object base))]
    (is (= "sha256:generated" (:syntax/id object)))
    (is (= :generated-form (get-in object [:form :kind])))
    (is (= :macro-expanded (:phase object)))
    (is (= ["sha256:base"] (:prior-syntax-ids object)))
    (is (= "sha256:source" (get-in object [:source :source-id])))
    (is (= 'demo.core
           (get-in object [:hygiene :macro-call-site-namespace])))
    (is (= 'gravity.syntax/capture
           (get-in object [:hygiene :captures 0 :macro-api])))
    (is (true? (get-in object [:hygiene :captures 0 :intentional?])))
    (is (false? (get-in object
                        [:hygiene :captures 0 :authority-bearing?])))
    (is (= "generated:compiler.c3/with-capture-demo:1"
           (get-in object [:origin 0 :generated-span])))
    (is (= {:generated true :source-metadata {:doc "base"}}
           (:metadata object)))))

(deftest operation-map-is-strict-and-preserves-nested-interposition
  (doseq [operations [nil
                      {:unknown identity}
                      {:sha256-hex :keyword-is-invokable-but-not-function}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (construction/with-operations operations (fn [] :unused)))))
  (is (thrown? clojure.lang.ExceptionInfo
               (construction/with-operations {} :not-a-function)))
  (let [observed (atom [])
        identity {:interposed :identity}
        object
        (construction/with-operations
         {:c3-origin-chain (fn [& args]
                             (swap! observed conj [:origin args])
                             [:interposed-origin])
          :c3-source-form-kind (fn [& args]
                                (swap! observed conj [:kind args])
                                :interposed-kind)
          :c3-source-facts (fn [& args]
                            (swap! observed conj [:facts args])
                            {:interposed true})
          :c3-identity-input (fn [& args]
                              (swap! observed conj [:identity args])
                              identity)
          :c3-stable-syntax-id (fn [value]
                                (swap! observed conj [:stable value])
                                "syntax:interposed")
          :sha256-hex (fn [value]
                       (swap! observed conj [:hash value])
                       "input")}
         #(construction/c3-syntax-object
           seed form-record token-record source-unit {} {}))]
    (is (= "syntax:interposed" (:syntax/id object)))
    (is (= "sha256:input" (get-in object [:identity :input-hash])))
    (is (= :interposed-kind (get-in object [:form :kind])))
    (is (= {:interposed true} (:facts object)))
    (is (= [:interposed-origin] (:origin object)))
    (is (some #(= [:stable identity] %) @observed))
    (is (= #{:origin :kind :facts :identity :stable :hash}
           (set (map first @observed))))))

(deftest contract-is-hosted-bootstrap-free-and-nonauthoritative
  (let [contract-var (get (ns-interns 'gravity.c3-syntax-construction)
                          'namespace-contract)
        contract (var-get contract-var)]
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.c3-syntax-construction (:namespace contract)))
    (is (= (set (keys (:public-api contract)))
           (set (keys (ns-publics 'gravity.c3-syntax-construction)))))
    (is (= #{:c2-path-neutral-span
             :sha256-hex
             :c3-origin-chain
             :c3-source-form-kind
             :c3-source-facts
             :c3-path-neutral-origin
             :c3-identity-input
             :c3-stable-syntax-id
             :c3-syntax-object
             :c3-generated-syntax-object}
           (get-in contract [:operation-interposition :accepted-keys])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (doseq [claim [:canonical-c3-syntax-object-authority
                   :c2-reader-product-authentication
                   :literal-decoding
                   :syntax-stream-validation
                   :hygiene-verification
                   :macro-expansion
                   :diagnostic-policy
                   :proof-authority
                   :self-hosted-authority
                   :release-authority]]
      (is (some #{claim} (get-in contract [:ownership :does-not-own]))))
    (is (nil? (find-ns 'gravity.bootstrap)))
    (is (false? (:canonical-c3-authority? contract)))
    (is (false? (:self-hosted? contract)))
    (is (false? (:release-authority? contract)))))
