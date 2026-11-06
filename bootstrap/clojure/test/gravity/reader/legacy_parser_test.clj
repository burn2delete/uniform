(ns gravity.reader.legacy-parser-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [gravity.bootstrap :as b]
            [gravity.reader :as reader]))

(defn- operations []
  {:line-starts-var #'b/line-start-indices :line-terminator-var #'b/line-terminator-char?
   :fail-var #'b/stage1-reader-fail! :span-var #'b/stage1-reader-span
   :starts-with-var #'str/starts-with? :form-kind-var #'b/form-kind})

(defn- parse [path text table]
  (reader/parse-compatibility-records path text table (operations)))

(defn- rejection [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo ex ex)))

(defn- with-paired-file [original f]
  (let [copy (java.io.File/createTempFile "gravity-legacy-parser-" ".qst")]
    (try
      (spit copy (slurp original))
      (doseq [file [(io/file original) copy]]
        (f (.getCanonicalPath file) (slurp file)))
      (finally (.delete copy)))))

(deftest historical-contract-is-explicit-and-unauthenticated
  (let [contract (:gravity/namespace-contract (meta #'reader/parse-compatibility-records))
        api (get-in contract [:public-api 'parse-compatibility-records])]
    (is (= :historical-p15-table-driven-parsing (:contract-boundary api)))
    (is (= :historical-p15-form-records (:returns api)))
    (is (= :none (:admission api)))
    (is (false? (:authenticates-inputs? api)))
    (is (false? (:opens-source? api)))
    (is (false? (:callback-type-checks? api)))
    (is (= (set (keys (operations))) (set (:operation-inputs api))))
    (is (= :per-call-var-root-before-arguments (:owner-call-resolution contract)))
    (is (= ['clojure.core] (get-in contract [:dependency-direction :requires]))))
  (is (= '([source-path source-text table])
         (:arglists (meta #'b/stage1-reader-table-driven-records)))))

(deftest real-paired-p15-fixture-retains-records-and-source-spans
  (let [table (b/stage1-reader-table)]
    (with-paired-file
      (:accepted-fixture table)
      (fn [path text]
        (let [records (parse path text table)]
          (is (= records (b/stage1-reader-table-driven-records path text table)))
          (is (= 4 (count records)))
          (is (= '(def answer 42) (:form (second records))))
          (is (= {:name "stage1-reader" :items [1 2 3] :flags #{:source-spans :syntax-identity}
                  :nested {:ok true :missing nil}}
                 (nth (:form (nth records 2)) 2)))
          (doseq [[index {:keys [span]}] (map-indexed vector records)]
            (is (= path (:source span)))
            (is (= index (:form-index span)))
            (is (= (:byte-start span) (b/utf8-byte-count (subs text 0 (get-in span [:start :char])))))
            (is (= (:byte-end span) (b/utf8-byte-count (subs text 0 (get-in span [:end :char])))))))))))

(deftest actual-paired-rejected-fixtures-retain-owned-diagnostics
  (let [table (b/stage1-reader-table)]
    (doseq [[file expected] (:rejected-fixtures table)]
      (with-paired-file
        file
        (fn [path text]
          (let [direct (rejection #(parse path text table))
                facade (rejection #(b/stage1-reader-table-driven-records path text table))]
            (is (= expected (:id (ex-data direct))))
            (is (= (ex-data facade) (ex-data direct)))
            (is (= path (get-in (ex-data direct) [:source-span :source])))))))))

(deftest unicode-mixed-lines-and-table-fields-retain-native-behavior
  (let [table (b/stage1-reader-table)
        text (str "; trace\r\n[\"" (String. (Character/toChars 128578)) "\"]\r:next\n")
        records (parse "unicode.qst" text table)]
    (is (= [[(String. (Character/toChars 128578))] :next] (mapv :form records)))
    (is (= [2 3] (mapv #(get-in % [:span :start :line]) records)))
    (is (= :unicode-scalar (get-in records [0 :span :end :column-unit])))
    (is (> (get-in records [0 :span :byte-end]) (get-in records [0 :span :end :char])))
    (is (= [] (parse "empty.gravity" ";only\r\n" table)))
    (is (= [1 2] (mapv :form (parse "table.qst" "!skip\n1_2"
                                    (assoc table :line-comment "!" :whitespace ["_" "\n"])))))))

(deftest failure-operator-is-captured-before-span-but-later-calls-stay-late
  (let [table (b/stage1-reader-table)]
    (with-redefs [b/stage1-reader-fail! (fn [& _] (throw (ex-info "original" {:owner :original})))
                  b/stage1-reader-span
                  (fn [& _]
                    (alter-var-root #'b/stage1-reader-fail!
                                    (constantly (fn [& _] (throw (ex-info "later" {:owner :later})))))
                    {:source "owner.qst"})]
      (is (= :original (:owner (ex-data (rejection #(parse "owner.qst" "(" table))))))
      (is (= :later (:owner (ex-data (rejection #(parse "owner.qst" "(" table)))))))))

(deftest line-index-owner-is-first-and-exceptions-remain-identical
  (let [table (b/stage1-reader-table) sentinel (ex-info "line owner" {})]
    (with-redefs [b/line-start-indices (fn [& _] (throw sentinel))
                  str/starts-with? (fn [& _] (throw (AssertionError. "Line owner must run first")))]
      (is (identical? sentinel (rejection #(parse "first.gravity" "x" table))))))
  (let [table (b/stage1-reader-table) sentinel (ex-info "terminator owner" {})]
    (with-redefs [b/line-terminator-char? (fn [& _] (throw sentinel))]
      (is (identical? sentinel (rejection #(parse "comment.qst" ";x\n1" table)))))))

(deftest atom-and-top-level-kind-owners-remain-late
  (let [table (b/stage1-reader-table) starts str/starts-with? calls (atom [])]
    (with-redefs [str/starts-with?
                  (fn [token prefix]
                    (swap! calls conj :first)
                    (alter-var-root #'str/starts-with? (constantly (fn [s p] (swap! calls conj :later) (starts s p))))
                    (starts token prefix))]
      (is (= ['a 'b] (mapv :form (parse "late.qst" "a b" table))))
      (is (= [:first :later] @calls))))
  (let [table (b/stage1-reader-table) calls (atom [])]
    (with-redefs [b/form-kind
                  (fn [form]
                    (swap! calls conj form)
                    (alter-var-root #'b/form-kind (constantly (fn [later] (swap! calls conj later) :later)))
                    :first)]
      (is (= [:first :later] (mapv :kind (parse "kind.qst" "(a) b" table))))
      (is (= ['(a) 'b] @calls)))))

(deftest historical-limitations-do-not-become-normative-gravity-rules
  (let [table (b/stage1-reader-table)
        unavailable (fn [& _] (throw (AssertionError. "No source or table discovery")))]
    (with-redefs [b/stage1-reader-table unavailable b/read-source-form-records unavailable]
      (is (= [(symbol "2.5") (symbol "1/0") "an" #{1}]
             (mapv :form (parse "seed.qst" "2.5 1/0 \"a\\n\" #{1 1}" table))))
      (is (thrown? NumberFormatException (parse "overflow.qst" "999999999999999999999999999" table))))))

(deftest span-and-terminator-rebinding-and-owner-exception-identities-survive
  (let [table (b/stage1-reader-table)
        result (with-redefs [b/stage1-reader-span
                             (fn [& _]
                               (alter-var-root #'b/stage1-reader-span
                                               (constantly (fn [& _] {:owner :later})))
                               {:owner :first})]
                 (parse "span.qst" "1 2" table))]
    (is (= [:first :later] (mapv #(get-in % [:span :owner]) result))))
  (let [table (b/stage1-reader-table) terminator b/line-terminator-char? calls (atom [])
        result (with-redefs [b/line-terminator-char?
                             (fn [ch]
                               (swap! calls conj [:first ch])
                               (alter-var-root #'b/line-terminator-char?
                                               (constantly (fn [later] (swap! calls conj [:later later]) (terminator later))))
                               (terminator ch))]
                 (parse "terminator.qst" ";x\r\n1" table))]
    (is (= [1] (mapv :form result)))
    (is (= [[:first \;] [:later \x] [:later \return]] @calls)))
  (let [table (b/stage1-reader-table) sentinel (ex-info "exact owner" {})]
    (doseq [owner [#'b/stage1-reader-span #'b/stage1-reader-fail!]]
      (with-redefs-fn {owner (fn [& _] (throw sentinel))}
        (fn []
          (let [caught (rejection #(parse "identity.qst" "]" table))]
            (is (identical? sentinel caught))))))))
