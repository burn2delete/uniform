(ns gravity.reader.tokenizer-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.reader :as reader]))

(defn- operations []
  {:line-starts-var #'b/line-start-indices :digest-var #'b/sha256-hex
   :line-terminator-var #'b/line-terminator-char? :span-var #'b/stage1-reader-span
   :fail-var #'b/stage1-reader-fail! :decode-string-var #'b/stage1-reader-decode-string
   :token-kind-var #'b/stage1-reader-token-kind :decode-atom-var #'b/stage1-reader-decode-atom
   :decode-character-var #'b/stage1-reader-decode-character})

(defn- tokenize [path text table options]
  (reader/tokenize-compatibility-source path text table options (operations)))

(defn- rejection [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo ex ex)))

(deftest tokenizer-contract-is-explicit-and-unauthenticated
  (let [contract (:gravity/namespace-contract (meta #'reader/tokenize-compatibility-source))
        api (get-in contract [:public-api 'tokenize-compatibility-source])]
    (is (= :hosted-compatibility-source-tokenization (:contract-boundary api)))
    (is (= '([source-path source-text table reader-options operations]) (:arglists api)))
    (is (= (set (keys (operations))) (set (:operation-inputs api))))
    (is (= :none (:admission api)))
    (is (false? (:authenticates-inputs? api)))
    (is (false? (:opens-source? api)))
    (is (false? (:callback-type-checks? api)))
    (is (= 'gravity.reader.tokenizer-test (:test-owner api)))
    (is (= ['clojure.core] (get-in contract [:dependency-direction :requires])))))

(deftest real-paired-source-preserves-complete-token-streams-and-slices
  (let [table (b/stage1-reader-table)]
    (doseq [extension [".gravity" ".qst"] retain [false true]]
      (let [path (str "bootstrap/clojure/fixtures/accepted/surface-syntax" extension)
            text (slurp path) options {:retain-comments retain}
            result (tokenize path text table options)]
        (is (= result (b/stage1-reader-token-stream path text table options)))
        (is (= result (tokenize path text table options)))
        (is (= :gravity/stage1-reader-token-stream (:kind result)))
        (is (= :complete (:status result)))
        (is (= path (:source-path result)))
        (is (= (str "sha256:" (b/sha256-hex text)) (:source-id result)))
        (is (= (str "sha256:" (b/sha256-hex (pr-str table))) (:reader-table-id result)))
        (is (= (range (:token-count result)) (map :index (:tokens result))))
        (doseq [{:keys [raw span]} (:tokens result)]
          (is (= path (:source span)))
          (is (= raw (subs text (get-in span [:start :char]) (get-in span [:end :char]))))
          (is (= (:byte-start span) (b/utf8-byte-count (subs text 0 (get-in span [:start :char])))))
          (is (= (:byte-end span) (b/utf8-byte-count (subs text 0 (get-in span [:end :char]))))))))))

(deftest trivia-unicode-and-crlf-positions-survive
  (let [text (str "; note\r\n\"" (String. (Character/toChars 128578)) "\" x\r\n")
        table (b/stage1-reader-table)
        with-trivia (tokenize "unicode.qst" text table {:retain-comments true})
        without (tokenize "unicode.qst" text table {:retain-comments false})
        string-token (first (:tokens without))
        span (:span string-token)]
    (is (= [:comment :whitespace :string :whitespace :symbol :whitespace]
           (map :kind (:tokens with-trivia))))
    (is (= [:string :symbol] (map :kind (:tokens without))))
    (is (= (remove :trivia? (:tokens with-trivia))
           (map #(assoc % :index (:index %2)) (:tokens without)
                (remove :trivia? (:tokens with-trivia)))))
    (is (= (:source-id with-trivia) (:source-id without)))
    (is (= {:line 2 :column 1 :column-unit :unicode-scalar :char 8 :byte 8} (:start span)))
    (is (= {:line 2 :column 4 :column-unit :unicode-scalar :char 12 :byte 14} (:end span)))))

(deftest literal-classification-and-zero-denominator-deferral-stay-with-owners
  (let [result (tokenize "literal.gravity" "0x2a -0b11 1/2 1/0 2.5 nil true :a/b sym"
                         (b/stage1-reader-table) {})
        tokens (:tokens result)]
    (is (= [:integer :integer :ratio :ratio :decimal :nil :boolean :keyword :symbol]
           (map :kind tokens)))
    (is (= [42N -3N 1/2] (map :decoded (take 3 tokens))))
    (is (= :deferred (get-in tokens [3 :decoded :semantic-validation])))
    (is (= :zero-denominator (get-in tokens [3 :decoded :reason])))
    (is (= "1/0" (get-in tokens [3 :raw])))
    (is (= [2.5 nil true :a/b 'sym] (map :decoded (drop 4 tokens))))))

(deftest malformed-lexical-inputs-retain-original-diagnostics
  (let [table (b/stage1-reader-table)]
    (doseq [[text expected] [["\"unterminated" "STAGE1READER003"]
                           ["\"bad\\q\"" "STAGE1READER003"]
                           ["\\not-a-character" "STAGE1READER003"]
                           ["12xyz" "STAGE1READER007"]
                           ["#unknown payload" "STAGE1READER004"]]
            extension [".gravity" ".qst"]]
      (let [path (str "malformed" extension)
            direct (rejection #(tokenize path text table {:retain-comments true}))
            facade (rejection #(b/stage1-reader-token-stream path text table {:retain-comments true}))]
        (is (instance? clojure.lang.ExceptionInfo direct))
        (is (= expected (:id (ex-data direct))))
        (is (= (ex-data direct) (ex-data facade)))
        (is (= path (get-in (ex-data direct) [:source-span :source])))
        (is (keyword? (:token-id (ex-data direct))))))))

(deftest owner-vars-are-late-and-callback-order-is-preserved
  (let [table (b/stage1-reader-table) ports (operations) events (atom [])
        starts b/line-start-indices digest b/sha256-hex span b/stage1-reader-span
        classify b/stage1-reader-token-kind decode-atom b/stage1-reader-decode-atom]
    (with-redefs [b/line-start-indices (fn [s] (swap! events conj :line-starts) (starts s))
                  b/sha256-hex (fn [s] (swap! events conj :initial-digest) (digest s))
                  b/stage1-reader-token-kind (fn [s] (swap! events conj :classify) (classify s))
                  b/stage1-reader-decode-atom (fn [& args] (swap! events conj :atom) (apply decode-atom args))
                  b/stage1-reader-decode-string (fn [& _] (throw (ex-info "early decoder" {})))
                  b/stage1-reader-span
                  (fn [& args]
                    (swap! events conj :span)
                    (alter-var-root #'b/stage1-reader-decode-string
                                    (constantly (fn [& _]
                                                  (swap! events conj :late-string)
                                                  (alter-var-root #'b/sha256-hex
                                                                  (constantly (fn [s] (swap! events conj :late-digest) (digest s))))
                                                  "late-decoded")))
                    (apply span args))]
      (let [result (reader/tokenize-compatibility-source "late.qst" "\"x\" 2" table {} ports)]
        (is (= "late-decoded" (get-in result [:tokens 0 :decoded])))
        (is (= [:line-starts :initial-digest :span :late-string :classify :span :atom :late-digest]
               @events))))))

(deftest owner-exceptions-propagate-with-original-identity-and-before-source-digest
  (let [table (b/stage1-reader-table) sentinel (ex-info "owner refusal" {:owner :tokenizer})
        digest b/sha256-hex calls (atom [])]
    (with-redefs [b/sha256-hex (fn [s] (swap! calls conj :digest) (digest s))
                  b/stage1-reader-fail! (fn [& _] (swap! calls conj :fail) (throw sentinel))]
      (is (identical? sentinel (rejection #(tokenize "unclosed.gravity" "\"x" table {}))))
      (is (= [:digest :fail] @calls)))
    (with-redefs [b/stage1-reader-span (fn [& _] (throw sentinel))]
      (is (identical? sentinel (rejection #(tokenize "span.qst" "x" table {})))))))

(deftest supplied-table-and-text-need-no-source-intake-or-operation-discovery
  (let [table (b/stage1-reader-table) ports (assoc (operations) :next-token (fn [& _] (throw (ex-info "external internal operation" {}))))
        unexpected (fn [& _] (throw (ex-info "unexpected source discovery" {})))]
    (with-redefs [b/stage1-reader-table unexpected b/reader-project-context-for-source unexpected
                  b/read-source-form-records unexpected]
      (is (= :symbol (get-in (reader/tokenize-compatibility-source "supplied.qst" "value" table {} ports)
                            [:tokens 0 :kind]))))))

(deftest compatibility-facade-preserves-arities-and-default-options
  (let [facade b/stage1-reader-token-stream calls (atom []) table {:explicit :table}]
    (is (= '([source-path source-text table] [source-path source-text table reader-options])
           (:arglists (meta #'b/stage1-reader-token-stream))))
    (with-redefs [b/stage1-reader-token-stream (fn [& args] (swap! calls conj args) :delegated)]
      (is (= :delegated (facade "source.gravity" "text" table)))
      (is (= [["source.gravity" "text" table {:retain-comments false}]] @calls)))))
