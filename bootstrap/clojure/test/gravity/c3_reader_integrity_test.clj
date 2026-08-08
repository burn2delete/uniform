(ns gravity.c3-reader-integrity-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c3-reader-integrity :as integrity]))

(def source-path "source.gravity")
(def source-id "sha256:source")
(def source-span
  {:file source-id :source source-path :form-index 0})
(def incremental-hashes {:form-tree "sha256:forms"})
(def integrity-record {:artifact :gravity/reader-product-integrity})

(def accepted-artifact
  {:kind :gravity/stage0-c2-reader-document-artifact
   :artifact-id "sha256:artifact"
   :source-unit-record
   {:source-id source-id
    :path source-path
    :bytes-hash "sha256:bytes"
    :identity-inputs {:semantic :source}}
   :token-stream
   [{:token-id :tok-0
     :raw "x"
     :source-id source-id
     :source-path source-path
     :span source-span}]
   :form-tree
   [{:form-id :form-0
     :kind :symbol
     :children []
     :value 'x
     :source-id source-id
     :source-path source-path
     :span source-span
     :origin {:source-id source-id :source-path source-path}}]
   :top-level-form-ids [:form-0]
   :syntax-seed-stream
   [{:syntax-id "stage0-syntax-0"
     :form-id :form-0
     :form 'x
     :span source-span}]
   :reader-extension-invocation-records []
   :reader-diagnostics []
   :reader-source-map
   [{:syntax-id "stage0-syntax-0" :form-id :form-0 :span source-span}]
   :parsed-semantic-values ['x]
   :incremental-reader-hashes incremental-hashes
   :literal-decoding-records []
   :semantic-error-deferment-record
   {:deferred? true
    :semantic-analysis-in-reader? false
    :deferred-literal-records []}
   :reader-product-integrity integrity-record})

(def base-operations
  {:c2-lexical-product-validation
   (fn [& _] {:graph-valid? true :max-form-depth 1})
   :c2-incremental-hashes (fn [& _] incremental-hashes)
   :c2-literal-records (constantly [])
   :c2-deferred-semantic-literals (constantly [])
   :c3-deferred-ratio-descriptor-from-raw (constantly nil)
   :c2-reader-product-integrity-record (fn [& _] integrity-record)
   :reader-canonical-hash (constantly source-id)
   :sha256-hex (constantly "bytes")
   :c2-reader-artifact-id (constantly "sha256:artifact")
   :c3-syntax-fail! (fn [& _] (throw (ex-info "unexpected failure" {})))
   :source-span (fn [& _] source-span)
   :max-reader-form-graph-depth 32})

(deftest integrity-report-recomputes-every-reader-product-check
  (integrity/with-operations
   base-operations
   #(let [report
          (integrity/c3-c2-reader-integrity-report accepted-artifact)]
      (doseq [[field value] report
              :when (not (#{:failures :authentic?} field))]
        (is (true? value) (str field " should be true")))
      (is (true? (:authentic? report)))
      (is (= [] (:failures report)))))
  (integrity/with-operations
   base-operations
   #(let [report
          (integrity/c3-c2-reader-integrity-report
           (assoc-in accepted-artifact [:token-stream 0 :token-id]
                     :unstable-token))]
      (is (false? (:authentic? report)))
      (is (false? (:stable-token-ids? report)))
      (is (some #{:stable-token-ids?} (:failures report)))))
  (let [descriptor {:artifact :gravity/deferred-ratio-literal
                    :numerator "1" :denominator "2"}
        calls (atom [])
        ratio-artifact
        (-> accepted-artifact
            (assoc-in [:token-stream 0 :raw] "1/2")
            (assoc-in [:form-tree 0 :kind] :ratio)
            (assoc-in [:form-tree 0 :raw] "1/2")
            (assoc-in [:form-tree 0 :value] descriptor)
            (assoc-in [:syntax-seed-stream 0 :form] descriptor)
            (assoc :parsed-semantic-values [descriptor]))]
    (integrity/with-operations
     (assoc base-operations
            :c3-deferred-ratio-descriptor-from-raw
            (fn [raw] (swap! calls conj raw) descriptor))
     #(is (true?
           (:authentic?
            (integrity/c3-c2-reader-integrity-report ratio-artifact)))))
    (is (= ["1/2"] @calls))))

(deftest validation-binds-the-requested-source-and-routes-stale-facts
  (let [failures (atom [])
        operations
        (assoc base-operations
               :c3-syntax-fail! (fn [& args] (swap! failures conj args)))]
    (integrity/with-operations
     operations
     #(let [report
            (integrity/c3-validate-c2-reader-artifact!
             source-path accepted-artifact)]
        (is (true? (:authentic? report)))
        (is (true? (:source-path-binding-valid? report)))
        (is (empty? @failures))))
    (integrity/with-operations
     operations
     #(let [report
            (integrity/c3-validate-c2-reader-artifact!
             "different.gravity" accepted-artifact)]
        (is (false? (:authentic? report)))
        (is (false? (:source-path-binding-valid? report)))
        (is (= [:source-path-binding-valid?] (:failures report)))))
    (let [[id path subject extra] (first @failures)
          report (get-in extra [:facts :reader-product-integrity])]
      (is (= "C3-FACT-STALE" id))
      (is (= "different.gravity" path))
      (is (= :c2-reader-artifact (:producer subject)))
      (is (= :symbol (:form-kind subject)))
      (is (= [:source-path-binding-valid?] (:missing-fields extra)))
      (is (= report (:reader-product-integrity (:facts extra)))))))

(deftest validation-exceptions-and-depth-overflow-are-contained
  (integrity/with-operations
   (assoc base-operations
          :c2-lexical-product-validation
          (fn [& _] (throw (StackOverflowError. "depth"))))
   #(is (= {:authentic? false
            :failures [:reader-depth-stack-overflow-contained?]}
           (integrity/c3-c2-reader-integrity-report accepted-artifact))))
  (integrity/with-operations
   (assoc base-operations
          :c2-lexical-product-validation
          (fn [& _] (throw (IllegalStateException. "invalid"))))
   #(is (= {:authentic? false
            :failures [:reader-product-validation-exception]
            :cause-class "java.lang.IllegalStateException"}
           (integrity/c3-c2-reader-integrity-report accepted-artifact)))))

(deftest operation-map-is-strict-and-supports-nested-interposition
  (doseq [operations
          [nil
           {:unknown identity}
           {:c2-incremental-hashes :not-a-function}
           {:max-reader-form-graph-depth 0}
           {:max-reader-form-graph-depth :unbounded}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (integrity/with-operations operations (fn [] :unused)))))
  (is (thrown? clojure.lang.ExceptionInfo
               (integrity/with-operations {} :not-a-function)))
  (let [calls (atom [])]
    (integrity/with-operations
     {:c3-c2-reader-integrity-report
      (fn [artifact]
        (swap! calls conj artifact)
        {:authentic? true :failures []})
      :c3-syntax-fail! (fn [& _] (throw (ex-info "unexpected" {})))
      :source-span (fn [& _] source-span)}
     #(is (true?
           (:authentic?
            (integrity/c3-validate-c2-reader-artifact!
             source-path accepted-artifact)))))
    (is (= [accepted-artifact] @calls))))

(deftest contract-is-hosted-bootstrap-free-and-nonauthoritative
  (let [contract-var
        (get (ns-interns 'gravity.c3-reader-integrity) 'namespace-contract)
        contract (var-get contract-var)]
    (is (true? (:private (meta contract-var))))
    (is (= (set (keys (:public-api contract)))
           (set (keys (ns-publics 'gravity.c3-reader-integrity)))))
    (is (true? (get-in contract
                       [:operation-interposition :unknown-keys-rejected?])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (doseq [claim [:canonical-c2-reader-authority
                   :sh03-reader-product-authentication
                   :sh04-syntax-boundary-authentication
                   :source-reading
                   :diagnostic-construction
                   :canonical-c3-syntax-object-authority
                   :proof-authority
                   :attestation-authority
                   :self-hosted-authority
                   :release-authority]]
      (is (some #{claim} (get-in contract [:ownership :does-not-own]))))
    (is (nil? (find-ns 'gravity.bootstrap)))
    (is (false? (:canonical-c2-authority? contract)))
    (is (false? (:canonical-c3-authority? contract)))
    (is (false? (:proof-authority? contract)))
    (is (false? (:self-hosted? contract)))
    (is (false? (:release-authority? contract)))))
