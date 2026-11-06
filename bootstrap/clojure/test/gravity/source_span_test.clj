(ns gravity.source-span-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.source-span :as source-span]))

(deftest line-terminators-and-line-starts-preserve-bootstrap-boundaries
  (testing "only LF and CR classify as line terminator characters"
    (doseq [[value expected]
            [[\newline true]
             [\return true]
             [\space false]
             ["\n" false]
             [nil false]]]
      (is (= expected (source-span/line-terminator-char? value)) value)))
  (testing "LF, CR, and CRLF produce deterministic UTF-16 line starts"
    (doseq [[source expected]
            [["" [0]]
             ["a" [0]]
             ["\n" [0 1]]
             ["\r" [0 1]]
             ["\r\n" [0 2]]
             ["a\nb" [0 2]]
             ["a\rb" [0 2]]
             ["a\r\nb" [0 3]]
             ["\n\r\r\n" [0 1 2 4]]
             ["🙂\nβ" [0 3]]]]
      (is (= expected (source-span/line-start-indices source)) source)
      (is (= expected (bootstrap/line-start-indices source)) source)))
    (is (= [0] (source-span/line-start-indices nil)))
    (is (= [0] (bootstrap/line-start-indices nil))))

(deftest character-indices-and-byte-counts-preserve-bootstrap-behavior
  (testing "line/column lookup keeps one-based coordinates and boundary clamps"
    (let [starts [0 2 5]]
      (doseq [[line column expected]
              [[1 1 0]
               [1 0 0]
               [1 -3 0]
               [1 3 2]
               [2 1 2]
               [2 2 3]
               [3 1 5]
               [4 1 0]
               [0 4 3]]]
        (is (= expected (source-span/char-index-at starts line column))
            [line column]))
      (is (= 0 (source-span/char-index-at nil 1 1)))
      (is (= 0 (bootstrap/char-index-at nil 1 1)))
      (is (= (mapv (fn [[line column]]
                     (source-span/char-index-at starts line column))
                   [[1 1] [1 0] [1 -3] [1 3] [2 1] [2 2]
                    [3 1] [4 1] [0 4]])
             (mapv (fn [[line column]]
                     (bootstrap/char-index-at starts line column))
                   [[1 1] [1 0] [1 -3] [1 3] [2 1] [2 2]
                    [3 1] [4 1] [0 4]])))))
  (testing "UTF-8 counts differ from UTF-16 indices for non-ASCII text"
    (doseq [[text expected]
            [["" 0]
             ["ascii" 5]
             ["é" 2]
             ["λ" 2]
             ["🙂" 4]
             ["a🙂β" 7]
             ["\r\n" 2]]]
      (is (= expected (source-span/utf8-byte-count text)) text)
      (is (= expected (bootstrap/utf8-byte-count text)) text))))

(deftest source-locations-and-spans-preserve-unicode-byte-offsets
  (let [source "α🙂\r\nβ"
        starts (source-span/line-start-indices source)]
    (is (= [0 5] starts))
    (doseq [[line column expected]
            [[1 1 {:line 1 :column 1 :char 0 :byte 0}]
             [1 2 {:line 1 :column 2 :char 1 :byte 2}]
             [1 3 {:line 1 :column 3 :char 2 :byte 3}]
             [2 1 {:line 2 :column 1 :char 5 :byte 8}]
             [2 2 {:line 2 :column 2 :char 6 :byte 10}]
             [3 1 {:line 3 :column 1 :char 0 :byte 0}]
             [3 9 {:line 3 :column 9 :char 6 :byte 10}]]]
      (is (= expected
             (source-span/source-location source starts line column))
          [line column])
      (is (= expected
             (bootstrap/source-location source starts line column))
          [line column]))
    (let [expected
          {:source "unicode.gravity"
           :form-index 7
           :start {:line 1 :column 2 :char 1 :byte 2}
           :end {:line 2 :column 2 :char 6 :byte 10}
           :byte-start 2
           :byte-end 10}]
      (is (= expected
             (source-span/source-span
              "unicode.gravity" source starts 7 1 2 2 2)))
      (is (= expected
             (bootstrap/source-span
              "unicode.gravity" source starts 7 1 2 2 2))))
  (testing "the compact form reference remains nil/empty-safe"
    (doseq [[source form-index]
            [[nil nil]
             ["" 0]
             ["source.gravity" nil]]]
      (let [expected {:source source :form-index form-index}]
        (is (= expected (source-span/source-span source form-index)))
        (is (= expected (bootstrap/source-span source form-index))))))))

(deftest source-position-functions-are-deterministic
  (let [cases [["" [0] 1 1]
               ["a\r\nb🙂" [0 3] 2 3]
               ["λ\n🙂" [0 2] 3 2]]
        snapshot (mapv (fn [[source starts line column]]
                         [(source-span/line-start-indices source)
                          (source-span/char-index-at starts line column)
                          (source-span/source-location source starts line column)])
                       cases)]
    (is (= snapshot
           (mapv (fn [[source starts line column]]
                   [(source-span/line-start-indices source)
                    (source-span/char-index-at starts line column)
                    (source-span/source-location source starts line column)])
                 cases)))))

(deftest source-prefix-offsets-match-java-utf8-for-every-utf16-boundary
  (doseq [text ["" "ascii\r\nend" "\u03bb\r\u03b2\n\u4e00"
                "A\uD83D\uDE42Z" "\uD800x\uDC00" "\uD800\uD800\uDC00\uDC00"
                "\u007f\u0080\u07ff\u0800\uD7ff\uE000\uffff\uDBff\uDfff"]
          index (range (inc (count text)))]
    (let [expected (alength (.getBytes ^String (subs text 0 index)
                                      java.nio.charset.StandardCharsets/UTF_8))]
      (is (= expected (:byte (source-span/source-location text [0] 1 (inc index)))))))
  (doseq [text ["a\r\nb" "\u03bb\n\uD83D\uDE42" "\uD800\r\uDC00"]
          :let [starts (source-span/line-start-indices text)]
          line (range 1 (+ 2 (count starts)))
          column (range 0 (+ 3 (count text)))]
    (let [index (min (count text) (source-span/char-index-at starts line column))
          expected (source-span/utf8-byte-count (subs text 0 index))]
      (is (= expected (:byte (source-span/source-location text starts line column))))))
  (is (thrown? NullPointerException (source-span/source-location nil [0] 1 1)))
  (is (thrown? StringIndexOutOfBoundsException
               (source-span/source-location "abc" [-1] 1 1)))
  (is (= {:line 1 :column 2.5 :char 1.5 :byte 1}
         (source-span/source-location "abc" [0] 1 2.5))))

(deftest source-position-namespace-contract-is-narrow-and-acyclic
  (let [contract-var
        (get (ns-interns 'gravity.source-span) 'namespace-contract)
        contract (var-get contract-var)]
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.source-span (:namespace contract)))
    (is (= :hosted-source-position-metadata
           (:contract-boundary contract)))
    (is (= #{'indexed-source-location 'indexed-source-span 'line-terminator-char?
             'line-start-indices
             'char-index-at
             'utf8-byte-count
             'source-location
             'source-span}
           (set (keys (:public-api contract)))))
    (is (= [:source-text :line-start-indices :line-column-coordinates
            :source-path :form-index :indexed-reader-coordinates :byte-count-var :location-var]
           (:artifact-inputs contract)))
    (is (= [:line-start-indices :source-location :source-span
            :indexed-reader-source-location :indexed-reader-source-span]
           (:artifact-outputs contract)))
    (is (= ['clojure.core 'java.lang.String
            'java.nio.charset.StandardCharsets]
           (get-in contract [:dependency-direction :requires])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (is (some #{:diagnostic-construction}
              (get-in contract [:ownership :does-not-own])))
    (is (some #{:source-unit-identity}
              (get-in contract [:ownership :does-not-own])))
    (is (empty? (ns-aliases 'gravity.source-span)))
    (is (= #{'indexed-source-location 'indexed-source-span 'line-terminator-char?
             'line-start-indices
             'char-index-at
             'utf8-byte-count
             'source-location
             'source-span}
           (set (keys (ns-publics 'gravity.source-span)))))
    (is (true? (:bootstrap-hosted? contract)))
    (is (true? (:clojure-seed-boundary? contract)))
    (is (false? (:self-hosted? contract)))))

(deftest indexed-reader-locations-retain-separate-coordinate-conventions
  (let [text (str "\u03b1" (String. (Character/toChars 128578)) "\r\n\u03b2")
        starts (source-span/line-start-indices text)]
    (doseq [[index line column byte] [[0 1 1 0] [1 1 2 2] [2 1 3 3] [3 1 3 6]
                                     [4 1 4 7] [5 2 1 8] [6 2 2 10] [20 2 2 10]]]
      (let [expected {:line line :column column :column-unit :unicode-scalar
                      :char (min index (count text)) :byte byte}]
        (is (= expected (source-span/indexed-source-location #'bootstrap/utf8-byte-count text starts index)))
        (is (= expected (bootstrap/stage1-reader-location text starts index)))))
    (is (thrown? IndexOutOfBoundsException
                 (source-span/indexed-source-location #'bootstrap/utf8-byte-count text starts -1))))
  (doseq [unit [0xd800 0xdc00]]
    (let [text (String. (char-array [(char unit)]))]
      (is (= {:line 1 :column 2 :column-unit :unicode-scalar :char 1 :byte 1}
             (source-span/indexed-source-location #'bootstrap/utf8-byte-count text [0] 1)))))
  (is (= '([source-text line-starts idx]) (:arglists (meta #'bootstrap/stage1-reader-location))))
  (is (= '([source-path source-text line-starts start end]) (:arglists (meta #'bootstrap/stage1-reader-span)))))

(deftest indexed-span-captures-each-late-location-owner-and-preserves-references
  (let [path (String. "native.qst") first-location {:byte 2 :owner :first}
        later-location {:byte 8 :owner :later} calls (atom [])
        result
        (with-redefs [bootstrap/stage1-reader-location
                      (fn [_ _ index]
                        (swap! calls conj [:first index])
                        (alter-var-root #'bootstrap/stage1-reader-location
                                        (constantly (fn [_ _ later] (swap! calls conj [:later later]) later-location)))
                        first-location)]
          (source-span/indexed-source-span #'bootstrap/stage1-reader-location path "ab" [0] 0 1))]
    (is (identical? path (:source result)))
    (is (identical? first-location (:start result)))
    (is (identical? later-location (:end result)))
    (is (= [2 8] [(:byte-start result) (:byte-end result)]))
    (is (= [[:first 0] [:later 1]] @calls))))

(deftest indexed-byte-owner-remains-late-and-owner-exceptions-remain-identical
  (let [seen (atom [])]
    (with-redefs [bootstrap/utf8-byte-count
                  (fn [text]
                    (swap! seen conj [:first text])
                    (alter-var-root #'bootstrap/utf8-byte-count
                                    (constantly (fn [later] (swap! seen conj [:later later]) 202)))
                    101)]
      (is (= [101 202]
             (mapv :byte [(source-span/indexed-source-location #'bootstrap/utf8-byte-count "ab" [0] 1)
                          (source-span/indexed-source-location #'bootstrap/utf8-byte-count "ab" [0] 2)])))
      (is (= [[:first "a"] [:later "ab"]] @seen))))
  (let [sentinel (ex-info "position owner" {})]
    (doseq [[owner call] [[#'bootstrap/utf8-byte-count
                           #(source-span/indexed-source-location #'bootstrap/utf8-byte-count "ab" [0] 1)]
                          [#'bootstrap/stage1-reader-location
                           #(source-span/indexed-source-span #'bootstrap/stage1-reader-location "path.qst" "ab" [0] 0 1)]]]
      (with-redefs-fn {owner (fn [& _] (throw sentinel))}
        (fn []
          (let [caught (try (call) nil (catch clojure.lang.ExceptionInfo ex ex))]
            (is (identical? sentinel caught))))))))

(deftest indexed-position-contracts-do-not-own-source-admission-or-diagnostics
  (let [contract @(get (ns-interns 'gravity.source-span) 'namespace-contract)]
    (doseq [name '[indexed-source-location indexed-source-span]]
      (let [api (get-in contract [:public-api name])]
        (is (= :none (:admission api)))
        (is (false? (:authenticates-inputs? api)))
        (is (false? (:opens-source? api)))
        (is (false? (:callback-type-checks? api)))
        (is (= 'gravity.source-span-test (:test-owner api)))))
    (is (= :per-call-var-root-before-arguments (:owner-call-resolution contract)))))
