(ns gravity.c3-artifact-identity-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.c3-artifact-identity :as identity]))

(def external-operations
  {:c2-token-hash-input identity
   :c2-form-hash-input identity
   :c2-syntax-seed-hash-input identity
   :c2-extension-hash-input identity
   :c2-path-neutral-span (fn [span]
                           (if (map? span) (dissoc span :source) span))
   :c3-path-neutral-origin
   (fn [origin]
     (cond-> origin
       (contains? origin :span) (update :span dissoc :source)))
   :reader-canonical-hash (constantly "sha256:artifact")})

(def c2-view
  {:artifact-id "sha256:c2"
   :sh03-reader-authentication
   {:reader-result-id "sha256:reader"
    :provenance-binding-id "sha256:path-bound"}
   :source-unit-record
   {:path "/checkout/source.gravity"
    :project-root-record {:path "/checkout" :project-root-id "root"}}
   :token-stream [{:token-id :tok-0 :source-path "/checkout/source.gravity"}]
   :form-tree [{:form-id :form-0}]
   :syntax-seed-stream [{:syntax-id :seed}]
   :reader-extension-invocation-records []
   :reader-source-map [{:span {:source "/checkout/source.gravity"
                               :byte-start 0 :byte-end 1}}]
   :literal-decoding-records [{:span {:source "/checkout/source.gravity"
                                      :byte-start 0 :byte-end 1}}]
   :semantic-error-deferment-record
   {:deferred-literal-records
    [{:span {:source "/checkout/source.gravity" :byte-start 0 :byte-end 1}}]}})

(def syntax-object
  {:syntax/id "sha256:syntax"
   :span {:primary {:source "/checkout/source.gravity" :form-index 0}
          :all [{:source "/checkout/source.gravity" :form-index 0}]}
   :origin [{:kind :source
             :span {:source "/checkout/source.gravity" :form-index 0}}]})

(def boundary
  {:slice :SH-04
   :owner :gravity-source
   :adapter-contract :gravity/sh04
   :plan-binding
   {:artifact :binding
    :status :accepted
    :semantic-authority :gravity
    :source-byte-count 10
    :source-content-hash "sha256:source"
    :plan-semantic-hash "sha256:plan"
    :functions-semantic-hash "sha256:functions"
    :function-count 1
    :function-names-hash "sha256:names"
    :function-shapes-hash "sha256:shapes"
    :public-function-hashes {'f "sha256:f"}
    :public-function-shapes {'f '(x)}}
   :reader-semantic-binding {:reader "sha256:reader"}
   :reader-source-revision "sha256:revision"
   :resolved-syntax-result
   {:artifact :syntax-result
    :kind :syntax
    :schema-version 1
    :status :accepted
    :artifact-id "sha256:syntax-result"
    :semantic-source-id "sha256:source"
    :reader-binding {:reader "sha256:reader"}
    :root-syntax-ids ["sha256:syntax"]
    :graph-verification-report {:status :passed}
    :syntax-serialization {:status :accepted}
    :authority :gravity
    :trusted-boundary true
    :ignored :value}
   :authenticated-envelope {:semantic-envelope-id "sha256:envelope"}
   :resolved-stream-verification
   {:artifact :stream-verification :schema-version 1
    :status :passed :checks {:shape true} :ignored :value}
   :stream-digest-requests [:request]
   :stream-resolved-digests [:digest]
   :gravity-syntax-serialization
   {:artifact :serialization :schema-version 1 :status :accepted
    :encoding :edn :payload-id-request :payload :ignored :value}
   :gravity-syntax-deserialization
   {:artifact :deserialization :schema-version 1 :status :accepted
    :encoding :edn :ignored :value}
   :uncredited-compatibility-facade true
   :target-source-reread? false
   :clojure-adapter-residual? true
   :self-hosted? false})

(deftest path-neutral-reader-and-syntax-projections-remove-checkout-provenance
  (identity/with-operations
   external-operations
   #(let [reader (identity/c3-path-neutral-reader-artifact-view c2-view)
          syntax (identity/c3-path-neutral-syntax-object syntax-object)]
      (is (not (contains? (:source-unit-record reader) :path)))
      (is (not (contains? (get-in reader [:source-unit-record
                                           :project-root-record]) :path)))
      (is (not (contains? (:sh03-reader-authentication reader)
                          :provenance-binding-id)))
      (is (not (contains? (get-in reader [:reader-source-map 0 :span])
                          :source)))
      (is (not (contains? (get-in reader [:literal-decoding-records 0 :span])
                          :source)))
      (is (not (contains? (get-in reader
                                  [:semantic-error-deferment-record
                                   :deferred-literal-records 0 :span])
                          :source)))
      (is (not (contains? (get-in syntax [:span :primary]) :source)))
      (is (not (contains? (get-in syntax [:span :all 0]) :source)))
      (is (not (contains? (get-in syntax [:origin 0 :span]) :source))))))

(deftest boundary-and-artifact-identity-preimage-are-exactly_projected
  (identity/with-operations
   external-operations
   #(let [view (identity/c3-gravity-syntax-boundary-identity-view boundary)
          artifact {:artifact-id "sha256:old"
                    :c2-reader-artifact c2-view
                    :syntax-object-stream [syntax-object]
                    :origin-chain-graph
                    {:nodes [{:origin (:origin syntax-object)}]}
                    :gravity-origin-chain-graph
                    {:nodes [{:origin (:origin syntax-object)}]}
                    :gravity-syntax-boundary boundary}
          preimage (identity/c3-artifact-identity-input artifact)]
      (is (= :SH-04 (:slice view)))
      (is (= "sha256:envelope" (:semantic-envelope-id view)))
      (is (not (contains? (:resolved-syntax-result view) :ignored)))
      (is (not (contains? (:gravity-syntax-serialization view) :ignored)))
      (is (not (contains? preimage :artifact-id)))
      (is (= (:reader-semantic-binding boundary)
             (:c2-reader-artifact preimage)))
      (is (= view (:gravity-syntax-boundary preimage)))
      (is (= "sha256:artifact" (identity/c3-artifact-id artifact))))))

(deftest operation-map-is-strict_and_nested_interposition_is_preserved
  (doseq [operations [nil {:unknown identity} {:reader-canonical-hash :hash}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (identity/with-operations operations (fn [] :unused)))))
  (is (thrown? clojure.lang.ExceptionInfo
               (identity/with-operations {} :not-a-function)))
  (let [calls (atom [])]
    (identity/with-operations
     (merge external-operations
            {:c3-path-neutral-reader-artifact-view
             (fn [value] (swap! calls conj [:reader value]) :reader-view)
             :c3-path-neutral-syntax-object
             (fn [value] (swap! calls conj [:syntax value]) :syntax-view)
             :c3-gravity-syntax-boundary-identity-view
             (fn [value] (swap! calls conj [:boundary value]) :boundary-view)})
     #(let [preimage
            (identity/c3-artifact-identity-input
             {:artifact-id :old
              :c2-reader-artifact :reader
              :syntax-object-stream [:syntax]
              :origin-chain-graph {:nodes []}
              :gravity-origin-chain-graph {:nodes []}
              :gravity-syntax-boundary {:reader-semantic-binding :binding}})]
        (is (= :binding (:c2-reader-artifact preimage)))
        (is (= :boundary-view (:gravity-syntax-boundary preimage)))))
    (is (= #{:reader :syntax :boundary} (set (map first @calls))))))

(deftest contract-is-path-neutral_bootstrap_free_and_nonauthoritative
  (let [contract-var (get (ns-interns 'gravity.c3-artifact-identity)
                          'namespace-contract)
        contract (var-get contract-var)]
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.c3-artifact-identity (:namespace contract)))
    (is (= (set (keys (:public-api contract)))
           (set (keys (ns-publics 'gravity.c3-artifact-identity)))))
    (is (true? (get-in contract
                       [:operation-interposition :unknown-keys-rejected?])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (doseq [claim [:canonical-c3-syntax-object-authority
                   :c2-reader-product-authentication
                   :sh04-boundary-authentication
                   :canonical-encoding
                   :signature-or-trust-root
                   :proof-authority
                   :self-hosted-authority
                   :release-authority]]
      (is (some #{claim} (get-in contract [:ownership :does-not-own]))))
    (is (nil? (find-ns 'gravity.bootstrap)))
    (is (false? (:canonical-c3-authority? contract)))
    (is (false? (:self-hosted? contract)))
    (is (false? (:release-authority? contract)))))
