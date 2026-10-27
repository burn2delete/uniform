(ns gravity.self-hosting.sh03-decode-reference-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.self-hosting.sh03-decode-reference :as reference]))

(deftest unicode-and-mixed-newlines-have-explicit-reference-positions
  (let [positions (reference/positions "a\r\nλ🙂\rb\nc")]
    (is (= {:byte 0 :scalar 0 :utf16 0 :line 1 :column 1} (positions 0)))
    (is (= {:byte 3 :scalar 3 :utf16 3 :line 2 :column 1} (positions 3)))
    (is (= {:byte 9 :scalar 5 :utf16 6 :line 2 :column 3} (positions 9)))
    (is (= {:byte 10 :scalar 6 :utf16 7 :line 3 :column 1} (positions 10)))
    (is (= {:byte 12 :scalar 8 :utf16 9 :line 4 :column 1} (positions 12)))
    (is (= {:byte 13 :scalar 9 :utf16 10 :line 4 :column 2} (positions 13)))
    (is (nil? (positions 6)))))

(deftest strict-decoding-refuses-each-declared-malformed-family
  (doseq [values [[0xc0 0xaf] [0xe2 0x82] [0xed 0xa0 0x80]
                  [0xf4 0x90 0x80 0x80] [10 0x80]]]
    (is (thrown? java.nio.charset.CharacterCodingException
                 (reference/strict-text (byte-array (map unchecked-byte values))))))
  (is (thrown? clojure.lang.ExceptionInfo (reference/positions "\uD800"))))

(deftest exact-source-bytes-and-slices-have-a-nonauthoritative-oracle
  (let [bytes (.getBytes "λ🙂" java.nio.charset.StandardCharsets/UTF_8)]
    (is (= "🙂" (reference/slice bytes {:byte-start 2 :byte-end 6})))
    (is (= "λ🙂" (reference/strict-text bytes)))
    (is (= "sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
           (reference/sha256 (byte-array 0))))
    (is (nil? (find-ns 'gravity.bootstrap)))))
