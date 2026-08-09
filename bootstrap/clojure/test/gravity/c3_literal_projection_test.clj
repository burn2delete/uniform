(ns gravity.c3-literal-projection-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c3-literal-projection :as literals]))

(def ratio-span {:source "ratio.gravity" :byte-start 0 :byte-end 3})
(def ratio-value 1/2)
(def ratio-form
  {:form-id :form-0
   :kind :ratio
   :open-token :tok-0
   :close-token :tok-0
   :raw "1/2"
   :value ratio-value
   :span ratio-span})
(def ratio-token
  {:token-id :tok-0
   :kind :ratio
   :raw "1/2"
   :lexeme "1/2"
   :decoded ratio-value
   :span ratio-span})
(def ratio-seed
  {:syntax-id "stage0-syntax-0"
   :form-id :form-0
   :form ratio-value
   :span (assoc ratio-span :form-index 0)
   :reader-origin {:raw-excerpt "1/2" :raw-form-kind :ratio}
   :generated-origin []})
(def ratio-artifact
  {:form-tree [ratio-form]
   :token-stream [ratio-token]
   :literal-decoding-records
   [{:literal-id :lit-0
     :form-id :form-0
     :decoded ratio-value
     :raw "1/2"
     :span ratio-span
     :facts {:numerator-spelling "1"
             :denominator-spelling "2"
             :exact? true}}]
   :semantic-error-deferment-record {:deferred-literal-records []}
   :reader-product-integrity {:integrity-hash "sha256:integrity"}
   :source-unit-record {:source-id "sha256:source"}})

(deftest ratio-and-deferred-ratio-descriptors-preserve-lossless-products
  (let [accepted (literals/c3-ratio-descriptor-from-raw "1/2")
        deferred (literals/c3-ratio-descriptor-from-raw "9/0")]
    (is (= :accepted (:semantic-validation accepted)))
    (is (= 1N (:numerator accepted)))
    (is (= 2N (:denominator accepted)))
    (is (= :deferred (:semantic-validation deferred)))
    (is (= :zero-denominator (:reason deferred)))
    (is (= deferred
           (literals/c3-deferred-ratio-descriptor-from-raw "9/0")))
    (is (nil? (literals/c3-deferred-ratio-descriptor-from-raw "1/2")))
    (doseq [raw [nil false "ratio" "1/" "/2" "1/2/3"]]
      (is (nil? (literals/c3-ratio-descriptor-from-raw raw)))))
  (let [descriptor (literals/c3-lossless-literal-descriptor
                    ratio-seed ratio-form ratio-artifact {:authentic? true})
        facts (literals/c3-source-facts
               ratio-seed ratio-form ratio-artifact {:authentic? true})]
    (is (= :gravity/ratio-literal (:artifact descriptor)))
    (is (= ratio-value (:form ratio-seed)))
    (is (= :ratio
           (literals/c3-source-form-kind
            ratio-seed ratio-form ratio-artifact {:authentic? true})))
    (is (= descriptor (:reader-literal-descriptor facts)))
    (is (= "sha256:integrity" (:reader-product-integrity-hash facts)))
    (is (= "sha256:source" (:reader-source-id facts)))
    (is (= :form-0 (:reader-form-id facts)))
    (is (= "stage0-syntax-0" (:reader-seed-id facts)))
    (is (nil? (:reader-container-kind facts)))))

(deftest tagged-literal-projection-and-fallback-kind-are-exact
  (let [span {:source "tag.gravity" :byte-start 0 :byte-end 12}
        form {:form-id :form-0
              :kind :tagged-literal
              :tag 'gravity/schema
              :children [:form-1]
              :raw "#gravity/schema \"x\""
              :span span}
        payload {:form-id :form-1 :kind :string :value "x"}
        seed {:syntax-id :seed :form-id :form-0 :form '(tagged)}
        artifact {:form-tree [form payload]
                  :literal-decoding-records
                  [{:form-id :form-0
                    :raw (:raw form)
                    :span span
                    :facts {:tag 'gravity/schema}}]
                  :reader-product-integrity {:integrity-hash :integrity}
                  :source-unit-record {:source-id :source}}
        descriptor (literals/c3-tagged-literal-descriptor
                    seed form artifact {:authentic? true})]
    (is (= {:artifact :gravity/tagged-literal-descriptor
            :kind :tagged-literal
            :tag 'gravity/schema
            :raw "#gravity/schema \"x\""
            :payload "x"
            :semantic-validation :accepted}
           descriptor))
    (is (= :tagged-literal
           (literals/c3-source-form-kind
            seed form artifact {:authentic? true})))
    (is (= "x" (get-in (literals/c3-source-facts
                         seed form artifact {:authentic? true})
                        [:reader-literal-facts :payload]))))
  (is (= :vector
         (literals/c3-source-form-kind
          {:form [1 2]} {:kind :list} {} {:authentic? false})))
  (is (= {}
         (literals/c3-source-facts
          {:form [1 2]} {:kind :list} {} {:authentic? false}))))

(deftest operation-map-is-strict-and-preserves-nested-interposition
  (doseq [operations [nil {:unknown identity} {:form-kind :vector}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (literals/with-operations operations (fn [] :unused)))))
  (is (thrown? clojure.lang.ExceptionInfo
               (literals/with-operations {} :not-a-function)))
  (let [descriptor {:kind :ratio :raw "sentinel"}
        calls (atom [])]
    (literals/with-operations
     {:c3-lossless-literal-descriptor
      (fn [& args]
        (swap! calls conj args)
        descriptor)}
     #(do
        (is (= :sentinel-kind
               (literals/c3-source-form-kind
                {} {:kind :sentinel-kind} {} {})))
        (is (= descriptor
               (:reader-literal-descriptor
                (literals/c3-source-facts
                 {:syntax-id :seed} {:form-id :form} {} {}))))))
    (is (= 2 (count @calls))))
  (let [calls (atom 0)]
    (is (= :list
           (literals/with-operations
            {:form-kind (fn [_] (swap! calls inc) :list)}
            #(literals/c3-source-form-kind
              {:form '(x)} {:kind :other} {} {:authentic? false}))))
    (is (= 1 @calls))))

(deftest contract-is-bootstrap-free-and-denies-semantic-authority
  (let [contract-var (get (ns-interns 'gravity.c3-literal-projection)
                          'namespace-contract)
        contract (var-get contract-var)]
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.c3-literal-projection (:namespace contract)))
    (is (= (set (keys (:public-api contract)))
           (set (keys (ns-publics 'gravity.c3-literal-projection)))))
    (is (true? (get-in contract
                       [:operation-interposition :unknown-keys-rejected?])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (doseq [claim [:canonical-c3-syntax-object-authority
                   :c2-reader-product-authentication
                   :numeric-semantics
                   :tagged-literal-execution
                   :reader-extension-authority
                   :syntax-object-identity
                   :diagnostic-policy
                   :proof-authority
                   :self-hosted-authority
                   :release-authority]]
      (is (some #{claim} (get-in contract [:ownership :does-not-own]))))
    (is (nil? (find-ns 'gravity.bootstrap)))
    (is (false? (:canonical-c3-authority? contract)))
    (is (false? (:self-hosted? contract)))
    (is (false? (:release-authority? contract)))))
