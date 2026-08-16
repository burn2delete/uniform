(ns gravity.c2-lexical-validation-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c2-lexical-validation :as validation]))

(def source-text "(a)")
(def source-id "sha256:source")
(def source-path "source.gravity")

(defn span [start end start-column end-column]
  {:file source-id
   :source source-path
   :byte-start start
   :byte-end end
   :start {:line 1 :column start-column}
   :end {:line 1 :column end-column}})

(def token-stream
  [{:token-id :tok-0 :kind :list-open :raw "("
    :source-id source-id :source-path source-path :span (span 0 1 1 2)}
   {:token-id :tok-1 :kind :symbol :raw "a"
    :source-id source-id :source-path source-path :span (span 1 2 2 3)}
   {:token-id :tok-2 :kind :close :raw ")"
    :source-id source-id :source-path source-path :span (span 2 3 3 4)}])

(def form-tree
  [{:form-id :form-0
    :kind :list
    :collection-kind :list
    :raw "(a)"
    :span (span 0 3 1 4)
    :open-token :tok-0
    :close-token :tok-2
    :parent-form-id nil
    :children [:form-1]}
   {:form-id :form-1
    :kind :symbol
    :raw "a"
    :span (span 1 2 2 3)
    :open-token :tok-1
    :close-token :tok-1
    :parent-form-id :form-0
    :children []}])

(deftest utf8-span-and-graph-primitives-are-exact
  (let [bytes (.getBytes "λx" java.nio.charset.StandardCharsets/UTF_8)]
    (is (= "λ" (validation/c2-utf8-slice bytes 0 2)))
    (is (= "x" (validation/c2-utf8-slice bytes 2 3))))
  (is (true? (validation/c2-span-encloses? (span 0 3 1 4)
                                            (span 1 2 2 3))))
  (is (false? (validation/c2-span-encloses? (span 1 2 2 3)
                                             (span 0 3 1 4))))
  (is (true? (validation/c2-spans-source-ordered?
              [(span 0 1 1 2) (span 1 2 2 3)])))
  (is (false? (validation/c2-spans-source-ordered?
               [(span 1 2 2 3) (span 0 1 1 2)])))
  (is (= {:acyclic? true :processed-form-count 2 :max-form-depth 2}
         (validation/c2-form-graph-metrics form-tree)))
  (is (= {:acyclic? false :processed-form-count 0 :max-form-depth 0}
         (validation/c2-form-graph-metrics
          [{:form-id :form-0 :children [:form-1]}
           {:form-id :form-1 :children [:form-0]}]))))

(deftest lexical-product-validation-accepts-real-nested-products
  (let [report
        (validation/c2-lexical-product-validation
         source-text token-stream form-tree [:form-0])]
    (is (= :gravity/c2-lexical-product-validation (:artifact report)))
    (is (= :passed (:status report)))
    (is (true? (:graph-valid? report)))
    (is (true? (:ordered-token-ids-unique? report)))
    (is (true? (:token-raw-slices-exact? report)))
    (is (true? (:form-raw-slices-exact? report)))
    (is (true? (:token-provenance-complete? report)))
    (is (true? (:form-links-resolve? report)))
    (is (true? (:parent-child-bidirectional? report)))
    (is (true? (:parent-spans-enclose-children? report)))
    (is (true? (:collection-delimiters-resolve? report)))
    (is (= 2 (:max-form-depth report)))
    (is (false? (:nested-depth-at-least-three? report)))))

(deftest lexical-product-validation-rejects-graph-and-slice-drift
  (let [wrong-parent
        (assoc-in form-tree [1 :parent-form-id] nil)
        parent-report
        (validation/c2-lexical-product-validation
         source-text token-stream wrong-parent [:form-0])
        wrong-raw (assoc-in token-stream [1 :raw] "b")
        raw-report
        (validation/c2-lexical-product-validation
         source-text wrong-raw form-tree [:form-0])]
    (is (= :failed (:status parent-report)))
    (is (false? (:graph-valid? parent-report)))
    (is (false? (:parent-child-bidirectional? parent-report)))
    ;; The hosted compatibility report exposes token slice drift as a fact;
    ;; its legacy graph status is driven by form/graph checks and remains
    ;; passed. The downstream C2 capability proof rejects the false fact.
    (is (= :passed (:status raw-report)))
    (is (false? (:token-raw-slices-exact? raw-report))))
  (let [odd-map (-> form-tree
                    (assoc-in [0 :collection-kind] :map)
                    (assoc-in [0 :raw] "{a}"))
        odd-tokens (-> token-stream
                       (assoc-in [0 :raw] "{")
                       (assoc-in [0 :kind] :map-open)
                       (assoc-in [2 :raw] "}"))
        report
        (validation/c2-lexical-product-validation
         "{a}" odd-tokens odd-map [:form-0])]
    (is (= :failed (:status report)))
    (is (false? (:maps-even-logical-children? report)))))

(deftest operation-map-is-strict-and-preserves-nested-interposition
  (doseq [operations
          [nil {:unknown identity} {:c2-utf8-slice :not-a-function}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (validation/with-operations operations (fn [] :unused)))))
  (is (thrown? clojure.lang.ExceptionInfo
               (validation/with-operations {} :not-a-function)))
  (let [calls (atom [])]
    (validation/with-operations
     {:c2-form-graph-metrics
      (fn [forms]
        (swap! calls conj [:graph forms])
        {:acyclic? false :processed-form-count 0 :max-form-depth 0})
      :c2-span-encloses?
      (fn [parent child]
        (swap! calls conj [:span parent child])
        false)}
     #(let [report
            (validation/c2-lexical-product-validation
             source-text token-stream form-tree [:form-0])]
        (is (= :failed (:status report)))
        (is (false? (:acyclic? report)))
        (is (false? (:parent-spans-enclose-children? report)))))
    (is (some #(= :graph (first %)) @calls))
    (is (some #(= :span (first %)) @calls))))

(deftest contract-is-hosted-bootstrap-free-and-nonauthoritative
  (let [contract-var
        (get (ns-interns 'gravity.c2-lexical-validation) 'namespace-contract)
        contract (var-get contract-var)]
    (is (true? (:private (meta contract-var))))
    (is (= (set (keys (:public-api contract)))
           (set (keys (ns-publics 'gravity.c2-lexical-validation)))))
    (is (true? (get-in contract
                       [:operation-interposition :unknown-keys-rejected?])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (doseq [claim [:canonical-c2-reader-authority
                   :sh03-reader-product-authentication
                   :source-reading
                   :tokenization
                   :form-construction
                   :complete-lexical-conformance-authority
                   :diagnostic-policy
                   :proof-authority
                   :attestation-authority
                   :self-hosted-authority
                   :release-authority]]
      (is (some #{claim} (get-in contract [:ownership :does-not-own]))))
    (is (nil? (find-ns 'gravity.bootstrap)))
    (is (false? (:canonical-c2-authority? contract)))
    (is (false? (:proof-authority? contract)))
    (is (false? (:self-hosted? contract)))
    (is (false? (:release-authority? contract)))))
