(ns gravity.c3-syntax-diagnostics-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c3-syntax-diagnostics :as diagnostics]))

(def expected-messages
  {"C3-SHAPE" "syntax object fields are malformed or incomplete"
   "C3-ID" "syntax object identity is unstable or inconsistent"
   "C3-SPAN" "syntax object span is not resolvable"
   "C3-ORIGIN" "syntax object origin chain is missing or broken"
   "C3-HYGIENE" "syntax object hygiene context is malformed or hidden"
   "C3-CAPTURE" "syntax object captures a binding without explicit capture"
   "C3-METADATA" "syntax object metadata is invalid or lost"
   "C3-FACT-STALE" "syntax object facts are stale after transformation"
   "C3-SERIALIZE" "syntax object artifact does not round-trip"})

(deftest diagnostic-catalog-and-source-overrides-are-exact
  (is (= (set (keys expected-messages))
         (set diagnostics/c3-syntax-diagnostic-ids)))
  (is (= (set diagnostics/c3-syntax-diagnostic-ids)
         (set (map :diagnostic diagnostics/c3-syntax-rejected-designs))))
  (doseq [[id message] expected-messages]
    (is (= message (diagnostics/c3-syntax-message id))))
  (is (= "syntax object model failed"
         (diagnostics/c3-syntax-message "C3-UNKNOWN")))
  (let [overrides {:fail :origin}
        module {:metadata {:compiler {:c3-syntax overrides}}}
        forms [(list 'ns 'demo.core
                     (list :metadata {:compiler {:c3-syntax overrides}}))]]
    (is (= overrides (diagnostics/c3-syntax-source-overrides module)))
    (is (= overrides (diagnostics/c3-syntax-overrides-from-forms forms)))
    (is (= {} (diagnostics/c3-syntax-source-overrides {})))
    (is (= {} (diagnostics/c3-syntax-overrides-from-forms [])))))

(deftest failure-payload-preserves-c3-diagnostic-fields
  (let [failure (atom nil)
        fallback-span {:source "source.gravity" :form-index 0}
        subject {:syntax/id "sha256:syntax"
                 :form-kind :generated-form
                 :phase :macro-expanded
                 :producer 'demo.macro/expand
                 :origin [{:kind :generated}]
                 :hygiene {:marks [:m] :captures []}}
        extra {:missing-fields [:origin]}]
    (diagnostics/with-operations
     {:fail! (fn [& args] (reset! failure args) :failed)
      :source-span (fn [path index]
                     (is (= ["source.gravity" 0] [path index]))
                     fallback-span)}
     #(is (= :failed
             (diagnostics/c3-syntax-fail!
              "C3-ORIGIN" "source.gravity" subject extra))))
    (let [[id message payload] @failure]
      (is (= "C3-ORIGIN" id))
      (is (= (expected-messages id) message))
      (is (= fallback-span (:source-span payload)))
      (is (= :c3-syntax-object (:diagnostic-family payload)))
      (is (= :syntax-object-model (:stage payload)))
      (is (= "C3" (:document-id payload)))
      (is (= diagnostics/c3-syntax-governing-document
             (:expected-document payload)))
      (is (= "sha256:syntax" (:syntax-id payload)))
      (is (= :generated-form (:form-kind payload)))
      (is (= :macro-expanded (:phase payload)))
      (is (= 'demo.macro/expand (:producer payload)))
      (is (= [{:kind :generated}] (:origin-chain payload)))
      (is (= {:marks [:m] :captures []} (:hygiene-summary payload)))
      (is (= [:origin] (:missing-fields payload))))))

(deftest fixture-override-routing-retains-all-nine-identities
  (let [routes diagnostics/c3-syntax-override-diagnostics]
    (doseq [[fail-kind expected-id] routes]
      (let [failures (atom [])
            span {:source "fixture.gravity" :form-index 0}]
        (diagnostics/with-operations
         {:fail! (fn [& args] (swap! failures conj args))
          :source-span (fn [& _] span)}
         #(diagnostics/c3-syntax-validate-overrides!
           "fixture.gravity" {:fail fail-kind}))
        (is (= 1 (count @failures)))
        (let [[id _ payload] (first @failures)]
          (is (= expected-id id))
          (is (= span (:source-span payload)))
          (is (= :fixture-override (:producer payload)))
          (is (= fail-kind (:form-kind payload)))
          (is (= {:marks [] :captures []} (:hygiene-summary payload)))
          (is (= [fail-kind] (:missing-fields payload))))))
    (let [failures (atom [])]
      (diagnostics/with-operations
       {:fail! (fn [& args] (swap! failures conj args))
        :source-span (fn [& _] :span)}
       #(do
          (diagnostics/c3-syntax-validate-overrides! "fixture.gravity" {})
          (diagnostics/c3-syntax-validate-overrides!
           "fixture.gravity" {:fail :unknown})))
      (is (empty? @failures)))))

(deftest operation-map-is-strict-and-preserves-nested-interposition
  (doseq [operations
          [nil
           {:unknown identity}
           {:fail! :not-a-function}
           {:c3-syntax-diagnostic-ids []}
           {:c3-syntax-governing-document ""}
           {:c3-syntax-rejected-designs [:not-a-map]}
           {:c3-syntax-override-diagnostics {:shape :not-a-string}}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (diagnostics/with-operations operations (fn [] :unused)))))
  (is (thrown? clojure.lang.ExceptionInfo
               (diagnostics/with-operations {} :not-a-function)))
  (let [failure (atom nil)]
    (diagnostics/with-operations
     {:c3-syntax-message (constantly "interposed message")
      :c3-syntax-governing-document "docs/interposed.md"
      :fail! (fn [& args] (reset! failure args))
      :source-span (fn [& _] :span)}
     #(diagnostics/c3-syntax-fail! "C3-ID" "source.gravity" {} {}))
    (is (= "interposed message" (second @failure)))
    (is (= "docs/interposed.md"
           (:expected-document (nth @failure 2))))))

(deftest contract-is-hosted-bootstrap-free-and-nonauthoritative
  (let [contract-var
        (get (ns-interns 'gravity.c3-syntax-diagnostics) 'namespace-contract)
        contract (var-get contract-var)]
    (is (true? (:private (meta contract-var))))
    (is (= (set (keys (:public-api contract)))
           (set (keys (ns-publics 'gravity.c3-syntax-diagnostics)))))
    (is (true? (get-in contract
                       [:operation-interposition :unknown-keys-rejected?])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (doseq [claim [:canonical-c3-syntax-object-authority
                   :c2-reader-product-authentication
                   :sh04-boundary-authentication
                   :syntax-object-construction
                   :syntax-verification-authority
                   :proof-authority
                   :attestation-authority
                   :self-hosted-authority
                   :release-authority]]
      (is (some #{claim} (get-in contract [:ownership :does-not-own]))))
    (is (nil? (find-ns 'gravity.bootstrap)))
    (is (false? (:canonical-c3-authority? contract)))
    (is (false? (:proof-authority? contract)))
    (is (false? (:self-hosted? contract)))
    (is (false? (:release-authority? contract)))))
