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
    (is (= :stage0-source-position-metadata
           (:contract-boundary contract)))
    (is (= #{'line-terminator-char?
             'line-start-indices
             'char-index-at
             'utf8-byte-count
             'source-location
             'source-span}
           (set (keys (:public-api contract)))))
    (is (= [:source-text :line-start-indices :line-column-coordinates
            :source-path :form-index]
           (:artifact-inputs contract)))
    (is (= [:line-start-indices :source-location :source-span]
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
    (is (= #{'line-terminator-char?
             'line-start-indices
             'char-index-at
             'utf8-byte-count
             'source-location
             'source-span}
           (set (keys (ns-publics 'gravity.source-span)))))
    (is (true? (:bootstrap-hosted? contract)))
    (is (true? (:clojure-seed-boundary? contract)))
    (is (false? (:self-hosted? contract)))))
