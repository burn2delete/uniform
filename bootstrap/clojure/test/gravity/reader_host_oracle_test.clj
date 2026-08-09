(ns gravity.reader-host-oracle-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.diagnostics :as diagnostics]
            [gravity.reader-cursor :as reader-cursor]
            [gravity.reader-diagnostic-policy :as reader-diagnostic-policy]
            [gravity.reader-host-oracle :as reader-host-oracle]
            [gravity.reader-primitives :as reader-primitives]
            [gravity.source-span :as source-span]))

(defn- operations
  []
  {:line-start-indices source-span/line-start-indices
   :skip-ignored! reader-cursor/skip-ignored!
   :source-span source-span/source-span
   :safe-excerpt reader-primitives/safe-excerpt
   :abbreviation-kind reader-primitives/abbreviation-kind
   :source-metadata reader-primitives/source-metadata
   :form-kind reader-primitives/form-kind
   :classify-reader-diagnostic
   reader-diagnostic-policy/classify-reader-diagnostic
   :fail! diagnostics/fail!})

(defn- diagnostic-data
  [source]
  (try
    (reader-host-oracle/read-source-form-records-host-oracle
     "oracle.gravity" source)
    nil
    (catch clojure.lang.ExceptionInfo ex
      (ex-data ex))))

(deftest hosted-oracle-preserves-forms-order-spans-and-reader-origins
  (let [source (str "; comment with unicode λ\n"
                    "alpha\n"
                    "'β\n"
                    "^:private [γ]\n"
                    "@delta ")
        records
        (reader-host-oracle/read-source-form-records-host-oracle
         "unicode.gravity" source)]
    (is (= ['alpha (list 'quote 'β) ['γ]
            (list 'clojure.core/deref 'delta)]
           (mapv :form records)))
    (is (= [0 1 2 3] (mapv #(get-in % [:span :form-index]) records)))
    (is (= [2 3 4 5] (mapv #(get-in % [:span :start :line]) records)))
    (is (= ["alpha\n" "'β\n" "^:private [γ]" "@delta"]
           (mapv #(get-in % [:reader-origin :raw-excerpt]) records)))
    (is (= [:symbol :list :vector :list]
           (mapv #(get-in % [:reader-origin :raw-form-kind]) records)))
    (is (= [nil :quote :metadata :deref]
           (mapv #(get-in % [:reader-origin :abbreviation]) records)))
    (is (= {} (:metadata (first records))))
    (is (= {:private true} (:metadata (nth records 2))))
    (is (< (get-in records [1 :span :start :char])
           (get-in records [1 :span :start :byte]))
        "the preceding Unicode comment makes UTF-8 byte and UTF-16 offsets diverge")
    (doseq [record records]
      (is (= "unicode.gravity" (get-in record [:span :source])))
      (is (<= (get-in record [:span :byte-start])
              (get-in record [:span :byte-end]))))
    (is (= [] (:generated-origin (first records))))
    (doseq [record (rest records)]
      (is (= 1 (count (:generated-origin record))))
      (is (= (:span record)
             (get-in record [:generated-origin 0 :from]))))
    (testing "LF, CR, and CRLF comments retain forms and source spans"
      (doseq [terminator ["\n" "\r" "\r\n"]]
        (let [prefix (str "; λ" terminator)
              [record]
              (reader-host-oracle/read-source-form-records-host-oracle
               "terminator.gravity" (str prefix "value "))]
          (is (= 'value (:form record)) (pr-str terminator))
          (is (= "value" (get-in record [:reader-origin :raw-excerpt]))
              (pr-str terminator))
          (is (= 2 (get-in record [:span :start :line]))
              (pr-str terminator))
          (is (= (count prefix) (get-in record [:span :start :char]))
              (pr-str terminator)))))))

(deftest malformed-input-retains-classified-diagnostic-payload
  (doseq [[source expected-id]
          [["(" "L1-DELIMITER"]
           ["{:a}" "L1-MAP-ARITY"]
           ["^" "L1-METADATA"]
           ["#gravity/unknown 1" "L1-READER-EXTENSION"]]]
    (let [data (diagnostic-data source)]
      (is (= expected-id (:id data)) source)
      (is (= {:source "oracle.gravity"} (:source-span data)) source)
      (is (= {:stage :read-source-forms} (:reader-state data)) source)
      (is (string? (:cause-message data)) source)
      (is (= "Fix delimiter, string, collection, metadata, or reader-extension syntax before compilation."
             (:remediation data))
          source))))

(deftest clojure-read-eval-is-denied-and-not-interposable
  (let [property "gravity.reader-host-oracle-test.read-eval"]
    (System/clearProperty property)
    (try
      (let [data
            (diagnostic-data
             (str "#=(System/setProperty \"" property "\" \"executed\")"))]
        (is (= "C2-READER" (:id data)))
        (is (re-find #"read-eval" (:cause-message data)))
        (is (nil? (System/getProperty property))))
      (finally
        (System/clearProperty property)))))

(deftest explicit-operations-interpose-only-extracted-helpers
  (let [calls (atom [])
        wrapped
        (into {}
              (map (fn [[operation f]]
                     [operation
                      (fn [& args]
                        (swap! calls conj operation)
                        (apply f args))]))
              (operations))
        records
        (reader-host-oracle/read-source-form-records-host-oracle
         "interposed.gravity" "'value" wrapped)]
    (is (= [(list 'quote 'value)] (mapv :form records)))
    (is (= #{:line-start-indices :skip-ignored! :source-span :safe-excerpt
             :abbreviation-kind :source-metadata :form-kind}
           (set @calls)))
    (is (not-any? #{:classify-reader-diagnostic :fail!} @calls))
    (let [failure-calls (atom [])
          failing-operations
          (assoc (operations)
                 :classify-reader-diagnostic
                 (fn [_ _]
                   (swap! failure-calls conj :classified)
                   ["TEST-READER" "interposed reader failure"])
                 :fail!
                 (fn [id message data]
                   (swap! failure-calls conj :failed)
                   (throw (ex-info message (assoc data :id id)))))]
      (try
        (reader-host-oracle/read-source-form-records-host-oracle
         "interposed.gravity" "(" failing-operations)
        (is false "malformed input must fail")
        (catch clojure.lang.ExceptionInfo ex
          (is (= "TEST-READER" (:id (ex-data ex))))
          (is (= [:classified :failed] @failure-calls)))))))

(deftest operations-validation-and-api-contract-fail-closed
  (let [valid (operations)]
    (doseq [[candidate expected]
            [[(dissoc valid :fail!)
              {:missing-operation-keys [:fail!]}]
             [(assoc valid :form-kind :not-a-function)
              {:non-function-operation-keys [:form-kind]}]
             [(assoc valid :read read)
              {:unexpected-operation-keys [:read]}]
             [nil
              {:provided-operations nil}]]]
      (try
        (reader-host-oracle/read-source-form-records-host-oracle
         "invalid-operations.gravity" "value" candidate)
        (is false (pr-str candidate))
        (catch clojure.lang.ExceptionInfo ex
          (is (= "STAGE0-READER-HOST-ORACLE-OPERATIONS"
                 (:id (ex-data ex))))
          (doseq [[key value] expected]
            (is (= value (get (ex-data ex) key)) key))))))
  (let [contract-var
        (get (ns-interns 'gravity.reader-host-oracle) 'namespace-contract)
        contract (var-get contract-var)]
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.reader-host-oracle (:namespace contract)))
    (is (= :stage0-clojure-host-reference-reader-oracle
           (:contract-boundary contract)))
    (is (= #{'read-source-form-records-host-oracle}
           (set (keys (ns-publics 'gravity.reader-host-oracle)))))
    (is (= '([source-path source-text]
             [source-path source-text operations])
           (:arglists
            (meta #'reader-host-oracle/read-source-form-records-host-oracle))))
    (is (= #{'diagnostics 'reader-cursor 'reader-diagnostic-policy
             'reader-primitives 'source-span}
           (set (keys (ns-aliases 'gravity.reader-host-oracle)))))
    (is (some #{'gravity.bootstrap}
              (get-in contract [:dependency-direction :forbids])))
    (doseq [nonclaim [:canonical-c2-reader-authority
                      :authenticated-source-form-records
                      :self-hosted-reader
                      :clojure-read-function
                      :reader-eval-policy
                      :eof-identity]]
      (is (some #{nonclaim} (get-in contract [:ownership :does-not-own]))))
    (is (false? (:canonical-c2-reader? contract)))
    (is (false? (:authenticated-reader? contract)))
    (is (false? (:self-hosted? contract)))))
