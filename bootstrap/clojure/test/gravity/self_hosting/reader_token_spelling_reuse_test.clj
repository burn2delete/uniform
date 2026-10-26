(ns gravity.self-hosting.reader-token-spelling-reuse-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh03-phase-lifetime-test]
            [gravity.self-hosting.sh03-compact-character-storage-test]))

;; Direct current-source semantic regressions, not authenticated C2/C5 or C14
;; admission. Existing owner helpers compile the actual Reader without repins.
(defn- owner [name]
  (var-get (ns-resolve 'gravity.self-hosting.sh03-phase-lifetime-test name)))
(defn- execute [function arguments]
  ((owner 'execute-plan) @(owner 'current-plan) function arguments))
(defn- input [text trivia?] ((owner 'input) text trivia?))
(defn- utf8-bytes [text]
  (mapv #(bit-and 255 %) (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8)))
(defn- observe [calls operation]
  (let [dispatch (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped)
        original @dispatch]
    (with-redefs-fn
      {dispatch (fn [runtime plan callee arguments scope]
                  (swap! calls update callee (fnil inc 0))
                  (original runtime plan callee arguments scope))}
      operation)))
(defn- scan [text]
  (let [arguments (input text true)
        decoded (execute 'sh03-decode-source-bytes (subvec arguments 0 2))]
    (execute 'sh03-scan-tokens [(:characters decoded)])))

(deftest scanner-reuses-one-spelling-per-raw-token
  (let [pieces ["(" "foo" " " ":ns/key" " " "\"\u03bb\uD83D\uDE42\\n\"" " "
                "\\newline" " " "0x10" " " "2/3" " " "-1.5" " " "true" " " "nil"
                ")" "\r\n" ";tail" "\n"]
        kinds [:list-open :symbol :whitespace :keyword :whitespace :string :whitespace
               :character :whitespace :integer :whitespace :ratio :whitespace :decimal
               :whitespace :boolean :whitespace :nil :close :whitespace :comment :whitespace]
        text (apply str pieces) calls (atom {})
        result (observe calls #(scan text)) tokens (:tokens result)
        reference ((var-get (ns-resolve 'gravity.self-hosting.sh03-compact-character-storage-test
                                        'reference-decoded)) text)
        characters (:characters reference)]
    (is (= :accepted (:status result)))
    (is (= kinds (mapv :kind tokens)))
    (is (= (count pieces) (count tokens) (get @calls 'sh03-range-raw-spelling)))
    (is (= (count tokens) (get @calls 'sh03-character-range-codepoints)))
    (loop [index 0 scalar 0 byte 0]
      (when (< index (count pieces))
        (let [piece (pieces index) token (tokens index)
              points (vec (.toArray (.codePoints ^String piece)))
              end (+ scalar (count points)) raw (:raw token)
              first-span (:span (characters scalar))
              last-span (:span (characters (dec end)))]
          (is (= {:encoding :utf-8 :bytes (utf8-bytes piece) :unicode-scalars points} raw))
          (is (= {:start (:start first-span) :end (:end last-span)
                  :byte-start byte :byte-end (+ byte (count (utf8-bytes piece)))} (:span token)))
          (is (= [scalar end] ((juxt :scalar-start :scalar-end) token)))
          (when (contains? (:descriptor token) :raw)
            (is (= raw (get-in token [:descriptor :raw]))))
          (recur (inc index) end (+ byte (count (utf8-bytes piece)))))))
    (is (= [955 128578 10] (get-in tokens [5 :descriptor :decoded-codepoints])))
    (is (= 10 (get-in tokens [7 :descriptor :codepoint])))
    (is (= 16 (get-in tokens [9 :descriptor :value])))
    (is (true? (get-in tokens [15 :descriptor :value])))
    (is (nil? (get-in tokens [17 :descriptor :value])))))

(deftest existing-token-constructor-signature-and-extra-fields-remain-compatible
  (let [args (input "word" false)
        chars (:characters (execute 'sh03-decode-source-bytes (subvec args 0 2)))
        descriptor {:kind :symbol :name-codepoints [119 111 114 100]}
        token (execute 'sh03-make-token [chars 3 :symbol 0 4 descriptor {:custom :retained}])]
    (is (= '[characters index kind start end descriptor extra]
           (get-in @(owner 'current-plan) [:functions 'sh03-make-token :params])))
    (is (= ["token/3" 3 :symbol :retained descriptor]
           ((juxt :token-id :index :kind :custom :descriptor) token)))
    (is (= (utf8-bytes "word") (get-in token [:raw :bytes])))))

(deftest scanner-preserves-numeric-and-malformed-input-diagnostics
  (doseq [[text rule reason] [["12abc" "C2-NUMERIC" :malformed-numeric]
                             [":a/b/c" "C2-IDENTIFIER" :malformed-keyword]
                             ["\"bad\\q\"" "C2-STRING" :invalid-string-escape]
                             ["\"unfinished" "C2-STRING" :unclosed-string]]]
    (testing text
      (let [result (scan text)]
        (is (= [:rejected rule reason] ((juxt :status :rule :reason) result)))
        (is (map? (:span result)))
        (is (vector? (get-in result [:raw :bytes])))
        (when (contains? #{"12abc" ":a/b/c"} text)
          (let [size (count text)]
            (is (= {:encoding :utf-8 :bytes (utf8-bytes text)
                    :unicode-scalars (vec (.toArray (.codePoints ^String text)))} (:raw result)))
            (is (= {:start {:line 1 :column 1 :column-unit :unicode-scalar :char 0 :byte 0}
                    :end {:line 1 :column (inc size) :column-unit :unicode-scalar :char size :byte size}
                    :byte-start 0 :byte-end size} (:span result)))
            (is (= (when (= text "12abc") "STAGE1READER007")
                   (:engine result))))))))
  (doseq [[text kind] [["42" :integer] ["-3" :integer] ["0b10" :integer]
                       ["2/3" :ratio] [".5" :decimal] ["1e2" :decimal]
                       ["+" :symbol] ["-" :symbol] ["." :symbol]
                       ["\"\\u03bb\"" :string] ["\\u03bb" :character]]]
    (is (= [:accepted kind]
           ((juxt :status #(get-in % [:tokens 0 :kind])) (scan text))))))

(deftest spelling-reuse-keeps-fresh-replay-and-rejects-tampering
  (doseq [trivia? [false true]]
    (let [args (input "(foo :key \"\u03bb\uD83D\uDE42\")\r\n" trivia?)
          calls (atom {})
          result (observe calls #(execute 'sh03-read-source-unit args))
          report (observe calls #(execute 'sh03-verify-reader-result (conj args result)))
          phases '[sh03-decode-source-bytes sh03-scan-tokens sh03-parse-tokens
                   sh03-construct-parsed-source-unit]]
      (is (= :accepted (:status result) (:status report)))
      (is (true? (:verified? report)))
      (is (= (zipmap phases (repeat 2)) (select-keys @calls phases)))
      (doseq [path [[:token-stream 0 :raw :byte-end]
                    [:token-stream 0 :span :byte-start]
                    [:semantic-value-table 0 :descriptor :name-codepoints]
                    [:digest-requests 1 :preimage :logical-source-id]]]
        (let [bad (assoc-in result path :substituted)
              rejected (execute 'sh03-verify-reader-result (conj args bad))]
          (is (= :rejected (:status rejected)))
          (is (false? (:verified? rejected)))
          (is (= ["C2-HASH"] (mapv :id (:diagnostics rejected))))
          (is (= [:reader-result-replay-mismatch]
                 (mapv #(get-in % [:reader-state :reason]) (:diagnostics rejected)))))))))
