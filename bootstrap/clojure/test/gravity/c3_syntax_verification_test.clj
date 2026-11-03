(ns gravity.c3-syntax-verification-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c3-syntax-verification :as verification]))

(def diagnostic-ids
  ["C3-SHAPE" "C3-ID" "C3-SPAN" "C3-ORIGIN" "C3-HYGIENE"
   "C3-CAPTURE" "C3-METADATA" "C3-FACT-STALE" "C3-SERIALIZE"])

(def source-syntax
  {:syntax/id "sha256:source"
   :form {:kind :symbol}
   :span {:primary {:source "source.gravity" :byte-start 0 :byte-end 1}}
   :namespace {:module 'demo.core}
   :phase :read
   :metadata {:doc "preserved"}
   :hygiene {:marks [] :captures []}
   :origin [{:kind :source}]
   :ownership {:kind :source}})

(def generated-syntax
  {:syntax/id "sha256:generated"
   :form {:kind :generated-form}
   :span {:primary "generated:demo:1"}
   :namespace {:module 'demo.core}
   :phase :macro-expanded
   :metadata {:generated true}
   :hygiene {:marks [:macro-mark] :captures ['captured]}
   :origin [{:kind :generated
             :producer 'demo.macro/expand
             :input-syntax-ids ["sha256:source"]
             :generated-span "generated:demo:1"}]
   :ownership {:kind :generated}})

(def syntax-stream [source-syntax generated-syntax])

(def serialization
  {:artifact :gravity/syntax-serialization-fixture
   :roundtrip? true
   :hash "sha256:serialization"})

(def required-fields
  [:syntax/id :form :span :namespace :phase :metadata :hygiene :origin])

(def base-operations
  {:c3-syntax-schema (fn [] {:required-fields required-fields})
   :c3-resolvable-span? (constantly true)
   :c3-syntax-serialization-fixture (constantly serialization)
   :c3-syntax-stream-reader-products-authentic? (constantly true)
   :c3-syntax-fail! (fn [& _] (throw (ex-info "unexpected failure" {})))
   :c3-syntax-diagnostic-ids diagnostic-ids})

(defn- production-marked-operation
  [operation]
  ((deref (ns-resolve 'gravity.c3-syntax-verification
                      'mark-production-default-serialization-operation))
   operation))

(deftest verification-report-recomputes-shape-and-authentication
  (let [authentication-calls (atom [])
        operations
        (assoc base-operations
               :c3-syntax-stream-reader-products-authentic?
               (fn [stream c2 boundary]
                 (swap! authentication-calls conj [stream c2 boundary])
                 true))]
    (verification/with-operations
     operations
     #(do
        (let [without-reader
              (verification/c3-syntax-verification-report
               syntax-stream serialization)]
          (is (= :gravity/syntax-verification-report
                 (:artifact without-reader)))
          (is (= :passed (:status without-reader)))
          (is (true? (:reader-products-authentic? without-reader)))
          (is (empty? @authentication-calls)))
        (let [c2 {:artifact-id "sha256:c2"}
              boundary {:slice :SH-04}
              with-reader
              (verification/c3-syntax-verification-report
               syntax-stream serialization c2 boundary)]
          (is (= :passed (:status with-reader)))
          (is (= [[syntax-stream c2 boundary]] @authentication-calls)))))))

(deftest capability-proof-recomputes-every-hosted-check
  (let [serialization-calls (atom 0)
        report-calls (atom [])
        marked-serialization
        (production-marked-operation
         (fn [_]
           (swap! serialization-calls inc)
           serialization))
        operations (assoc base-operations
                           :c3-syntax-serialization-fixture
                           marked-serialization
                           :c3-syntax-verification-report
                           (fn [stream supplied-serialization c2 boundary]
                             (swap! report-calls conj
                                    [stream supplied-serialization c2 boundary])
                             (verification/c3-syntax-verification-report
                              stream supplied-serialization c2 boundary)))]
    (verification/with-operations
     operations
     #(let [gravity-ownership
          {:owner :gravity-source
           :module 'gravity.bootstrap.syntax
           :syntax-ownership (mapv :ownership syntax-stream)}
          gravity-result
          {:hygiene-context-map {:artifact :gravity/hygiene}
           :metadata-ledger {:artifact :gravity/metadata}
           :fact-invalidation-ledger {:artifact :gravity/facts}
           :origin-chain-graph {:artifact :gravity/origins}
           :ownership-product gravity-ownership
           :syntax-object-stream syntax-stream}
          boundary {:slice :SH-04 :resolved-syntax-result gravity-result}
          c2 {:syntax-seed-stream [{:syntax-id "sha256:seed"}]}
          verifier
          (verification/c3-syntax-verification-report
           syntax-stream serialization c2 boundary)
          artifact
          {:syntax-object-stream syntax-stream
           :c2-reader-artifact c2
           :gravity-syntax-boundary boundary
           :gravity-syntax-ownership-product gravity-ownership
           :gravity-hygiene-context-map (:hygiene-context-map gravity-result)
           :gravity-metadata-ledger (:metadata-ledger gravity-result)
           :gravity-fact-invalidation-ledger
           (:fact-invalidation-ledger gravity-result)
           :gravity-origin-chain-graph (:origin-chain-graph gravity-result)
           :hygiene-context-map {:status :complete}
           :metadata-ledger
           {:source-metadata [{:syntax-id "sha256:source"}]
            :explicit-changes [{:syntax-id "sha256:generated"}]}
           :fact-ledger {:attached [{}] :invalidated [{}]}
           :generated-syntax-report
           {:generated [{:syntax-id "sha256:generated"}]}
           :syntax-serialization-fixture serialization
           :syntax-verification-report verifier
           :rejected-design-coverage
           (mapv (fn [id] {:diagnostic id}) diagnostic-ids)}
          _ (do (reset! serialization-calls 0)
                (reset! report-calls []))
          proof (verification/c3-syntax-capability-proof artifact)]
       (doseq [[field value] proof
               :when (not= field :status)]
         (is (true? value) (str field " should be recomputed as true")))
       (is (= :complete (:status proof)))
       (is (= 1 @serialization-calls))
       (is (= [[syntax-stream serialization c2 boundary]] @report-calls))
       (testing "unmarked injected callbacks retain both observations"
         (let [fallback-calls (atom 0)
               fallback-operations
               (assoc base-operations
                      :c3-syntax-serialization-fixture
                      (fn [_]
                        (swap! fallback-calls inc)
                        serialization))
               fallback-proof
               (verification/with-operations
                fallback-operations
                (fn [] (verification/c3-syntax-capability-proof artifact)))]
           (is (= :complete (:status fallback-proof)))
           (is (= 2 @fallback-calls))))
       (testing "forged marker metadata cannot opt a callback into reuse"
         (let [forged-calls (atom 0)
               forged-operations
               (assoc base-operations
                      :c3-syntax-serialization-fixture
                      (with-meta
                        (fn [_]
                          (swap! forged-calls inc)
                          serialization)
                        {:gravity.c3-syntax-verification/default-serialization-token
                         true}))
               forged-proof
               (verification/with-operations
                 forged-operations
                 (fn [] (verification/c3-syntax-capability-proof artifact)))]
           (is (= :complete (:status forged-proof)))
           (is (= 2 @forged-calls))))
       (testing "a different operation binding cannot borrow the handoff"
         (let [nested-calls (atom 0)
               nested-operations
               (assoc base-operations
                      :c3-syntax-serialization-fixture
                      (fn [_]
                        (swap! nested-calls inc)
                        serialization))
               rebound-operations
               (assoc base-operations
                      :c3-syntax-serialization-fixture
                      marked-serialization
                      :c3-syntax-verification-report
                      (fn [stream supplied-serialization c2 boundary]
                        (verification/with-operations
                          nested-operations
                          (fn []
                            (verification/c3-syntax-verification-report
                             stream supplied-serialization c2 boundary)))))
               rebound-proof
               (verification/with-operations
                 rebound-operations
                 (fn [] (verification/c3-syntax-capability-proof artifact)))]
           (is (= :complete (:status rebound-proof)))
           (is (= 1 @nested-calls))))
       (testing "a different syntax stream cannot borrow the handoff"
         (let [stream-calls (atom 0)
               stream-operations
               (assoc base-operations
                      :c3-syntax-serialization-fixture
                      (fn [_]
                        (swap! stream-calls inc)
                        serialization)
                      :c3-syntax-verification-report
                      (fn [stream supplied-serialization c2 boundary]
                        (verification/c3-syntax-verification-report
                         (mapv identity stream)
                         supplied-serialization c2 boundary)))
               stream-proof
               (verification/with-operations
                 (assoc stream-operations
                        :c3-syntax-serialization-fixture
                        (production-marked-operation
                         (fn [_]
                           (swap! stream-calls inc)
                           serialization)))
                 (fn [] (verification/c3-syntax-capability-proof artifact)))]
           (is (= :complete (:status stream-proof)))
           (is (= 2 @stream-calls))))
       (testing "stale artifact serialization remains rejected"
         (let [stale-proof
               (verification/c3-syntax-capability-proof
                (assoc artifact :syntax-serialization-fixture
                       (assoc serialization :hash "sha256:stale")))]
           (is (= :failed (:status stale-proof)))
           (is (false? (:serialization-round-trips? stale-proof)))))
       (testing "forged stored verification remains rejected"
         (let [forged-proof
               (verification/c3-syntax-capability-proof
                (assoc artifact :syntax-verification-report
                       {:artifact :forged-c3-report}))]
           (is (= :failed (:status forged-proof)))
           (is (false? (:syntax-verifier-current? forged-proof)))))
       (testing "stateful callback divergence remains observable"
         (let [divergent-calls (atom 0)
               divergent-operations
               (assoc base-operations
                      :c3-syntax-serialization-fixture
                      (fn [_]
                        (if (= 1 (swap! divergent-calls inc))
                          serialization
                          (assoc serialization :hash "sha256:divergent"))))
               divergent-proof
               (verification/with-operations
                divergent-operations
                (fn [] (verification/c3-syntax-capability-proof artifact)))]
           (is (= :failed (:status divergent-proof)))
           (is (= 2 @divergent-calls))
           (is (false? (:syntax-verifier-passed? divergent-proof)))))))))

(deftest validation-routing-retains-exact-diagnostic-identities
  (let [routes
        [[:construction-from-reader-seeds? "C3-SHAPE"]
         [:stable-syntax-ids? "C3-ID"]
         [:source-and-generated-origins? "C3-ORIGIN"]
         [:hygiene-propagated? "C3-HYGIENE"]
         [:intentional-capture-recorded? "C3-CAPTURE"]
         [:metadata-preservation-and-change? "C3-METADATA"]
         [:fact-invalidation-recorded? "C3-FACT-STALE"]
         [:serialization-round-trips? "C3-SERIALIZE"]
         [:reader-products-authentic? "C3-FACT-STALE"]
         [:syntax-verifier-current? "C3-FACT-STALE"]
         [:syntax-verifier-passed? "C3-SHAPE"]
         [:gravity-authoritative-products-current? "C3-FACT-STALE"]
         [:diagnostics-covered? "C3-SHAPE"]]
        complete-proof (into {:status :complete}
                             (map (fn [[field _]] [field true]) routes))]
    (doseq [[missing-field expected-id] routes]
      (let [failures (atom [])]
        (verification/with-operations
         {:c3-syntax-capability-proof
          (constantly (assoc complete-proof missing-field false))
          :c3-syntax-fail! (fn [& args] (swap! failures conj args))}
         #(is (= :complete
                 (verification/c3-syntax-validate! "source.gravity" {}))))
        (is (= [[expected-id
                 "source.gravity"
                 {:stage :syntax-object-model}
                 {:missing-fields [missing-field]}]]
               @failures))))))

(deftest operation-contract-is-strict-bootstrap-free-and-nonauthoritative
  (testing "operation maps and thunks fail closed"
    (doseq [operations
            [nil
             {:unknown identity}
             {:c3-syntax-schema :not-a-function}
             {:c3-syntax-diagnostic-ids []}
             {:c3-syntax-diagnostic-ids [:not-a-string]}]]
      (is (thrown? clojure.lang.ExceptionInfo
                   (verification/with-operations operations (fn [] :unused)))))
    (is (thrown? clojure.lang.ExceptionInfo
                 (verification/with-operations {} :not-a-function))))
  (testing "public API and dependency/authority ceiling are machine-readable"
    (let [contract-var
          (get (ns-interns 'gravity.c3-syntax-verification)
               'namespace-contract)
          contract (var-get contract-var)]
      (is (true? (:private (meta contract-var))))
      (is (= (set (keys (:public-api contract)))
             (set (keys (ns-publics 'gravity.c3-syntax-verification)))))
      (is (true? (get-in contract
                         [:operation-interposition :unknown-keys-rejected?])))
      (is (= ['gravity.bootstrap 'gravity.diagnostics]
             (get-in contract [:dependency-direction :forbids])))
      (doseq [claim [:canonical-c3-syntax-object-authority
                     :c2-reader-product-authentication
                     :sh04-boundary-authentication
                     :diagnostic-construction
                     :proof-authority
                     :attestation-authority
                     :self-hosted-authority
                     :release-authority]]
        (is (some #{claim} (get-in contract [:ownership :does-not-own]))))
      (is (nil? (find-ns 'gravity.bootstrap)))
      (is (false? (:canonical-c3-authority? contract)))
      (is (false? (:proof-authority? contract)))
      (is (false? (:self-hosted? contract)))
      (is (false? (:release-authority? contract))))))
