(ns gravity.reader-diagnostic-policy-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.reader-diagnostic-policy :as reader-diagnostic-policy]))

(defn- exception
  [message]
  (Exception. message))

(def expected-results
  {:extension
   ["L1-READER-EXTENSION"
    "reader extension tag is not registered for the stage0 build policy"]
   :metadata
   ["L1-METADATA"
    "metadata form is malformed or unattached"]
   :map
   ["L1-MAP-ARITY"
    "map literal contains an odd number of forms"]
   :numeric
   ["L1-NUMERIC"
    "numeric candidate fails every enabled numeric literal grammar"]
   :identifier
   ["L1-IDENTIFIER"
    "symbol or keyword has an invalid surface spelling"]
   :string
   ["L1-STRING"
    "string or character literal is malformed"]
   :delimiter
   ["L1-DELIMITER"
    "source has an unbalanced or mismatched delimiter"]
   :fallback
   ["C2-READER"
    "source could not be read by the stage0 bootstrap reader"]})

(deftest classifier-covers-every-message-spelling-and-fallback
  (doseq [[message expected-key]
          [["No reader function for tag gravity/value" :extension]
           ["UNKNOWN READER TAG gravity/value" :extension]
           ["Metadata must be Symbol,Keyword,String or Map" :metadata]
           ["Map literal must contain an even number of forms" :map]
           ["Map literal contains duplicate key" :map]
           ["Invalid number: 1e" :numeric]
           ["Invalid numeric literal" :numeric]
           ["Number format failure" :numeric]
           ["Invalid token: :bad/name/again" :identifier]
           ["Unsupported escape character: \\q" :string]
           ["Invalid unicode escape: \\u12" :string]
           ["String literal did not terminate" :string]
           ["EOF while reading" :delimiter]
           ["some other reader failure" :fallback]
           ["" :fallback]]]
    (is (= (expected-results expected-key)
           (reader-diagnostic-policy/classify-reader-diagnostic
            "(source)" (exception message)))
        message))
  (testing "a nil exception message uses the historical empty-string fallback"
    (is (= (:fallback expected-results)
           (reader-diagnostic-policy/classify-reader-diagnostic
            "(source)" (exception nil))))))

(deftest metadata-eof-classification-trims-source-before-prefix-check
  (doseq [source ["^" "  ^:private value" "\n\t^String value"]]
    (is (= (:metadata expected-results)
           (reader-diagnostic-policy/classify-reader-diagnostic
            source (exception "EOF while reading")))
        (pr-str source)))
  (doseq [source ["" "  " "value ^metadata" "(unterminated"]]
    (is (= (:delimiter expected-results)
           (reader-diagnostic-policy/classify-reader-diagnostic
            source (exception "EOF while reading")))
        (pr-str source))))

(deftest classifier-preserves-cond-branch-precedence
  (doseq [[message source expected-key]
          [["reader function for tag; metadata" "^" :extension]
           ["metadata; map literal contains" "" :metadata]
           ["map literal contains; invalid number" "" :map]
           ["invalid number; invalid token" "" :numeric]
           ["invalid token; string" "" :identifier]
           ["string; eof while reading" "" :string]
           ["eof while reading; otherwise unknown" "(" :delimiter]]]
    (is (= (expected-results expected-key)
           (reader-diagnostic-policy/classify-reader-diagnostic
            source (exception message)))
        message)))

(deftest exception-and-source-access-semantics-remain-visible
  (testing "the classifier accesses the exception through Throwable.getMessage"
    (is (thrown? NullPointerException
                 (reader-diagnostic-policy/classify-reader-diagnostic "" nil))))
  (testing "source trimming retains nil rejection"
    (is (thrown? NullPointerException
                 (reader-diagnostic-policy/classify-reader-diagnostic
                  nil (exception "failure"))))))

(deftest reader-diagnostic-policy-contract-is-narrow-and-private
  (let [contract-var
        (get (ns-interns 'gravity.reader-diagnostic-policy) 'namespace-contract)
        contract (var-get contract-var)]
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.reader-diagnostic-policy (:namespace contract)))
    (is (= :stage0-reader-diagnostic-policy (:contract-boundary contract)))
    (is (= #{'classify-reader-diagnostic}
           (set (keys (:public-api contract)))))
    (is (= '([source-text ex])
           (:arglists (meta #'reader-diagnostic-policy/classify-reader-diagnostic))))
    (is (= ['clojure.string]
           (get-in contract [:dependency-direction :requires])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (is (= #{'str}
           (set (keys (ns-aliases 'gravity.reader-diagnostic-policy)))))
    (is (= #{'classify-reader-diagnostic}
           (set (keys (ns-publics 'gravity.reader-diagnostic-policy)))))
    (doseq [nonclaim [:source-reading
                      :diagnostic-construction
                      :diagnostic-throwing
                      :canonical-c2-reader-authority
                      :bootstrap-orchestration]]
      (is (some #{nonclaim} (get-in contract [:ownership :does-not-own]))))
    (is (true? (:bootstrap-hosted? contract)))
    (is (true? (:clojure-seed-boundary? contract)))
    (is (false? (:self-hosted? contract)))))
