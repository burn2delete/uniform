(ns gravity.reader-cursor-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.reader-cursor :as reader-cursor])
  (:import [clojure.lang LineNumberingPushbackReader]
           [java.io StringReader]))

(defn- cursor
  [source]
  (LineNumberingPushbackReader. (StringReader. source)))

(deftest line-comment-skipping-stops-after-one-terminator-or-eof
  (testing "LF, CR, and CRLF retain the seed reader's cursor behavior"
    (doseq [[source expected-next]
            [["comment\nform" \f]
             ["comment\rform" \f]
             ["comment\r\nform" \f]]]
      (let [rdr (cursor source)]
        (is (nil? (reader-cursor/skip-line-comment! rdr)) source)
        (is (= expected-next (char (.read rdr))) source))))
  (testing "EOF is consumed without attempting unread"
    (let [rdr (cursor "comment")]
      (is (nil? (reader-cursor/skip-line-comment! rdr)))
      (is (= -1 (.read rdr))))))

(deftest ignored-input-skipping-preserves-read-unread-and-eof-boundaries
  (doseq [[source expected]
          [["" :eof]
           [" \t\n\r" :eof]
           ["; comment" :eof]
           ["; first\n; second\r\n  " :eof]
           ["form" :form]
           [" \t; comment\r\n form" :form]
           ["; comment\rform" :form]]]
    (let [rdr (cursor source)]
      (is (= expected (reader-cursor/skip-ignored! rdr)) source)
      (when (= :form expected)
        (is (= \f (char (.read rdr))) source)))))

(deftest public-api-and-contract-remain-narrow-and-acyclic
  (let [contract-var
        (get (ns-interns 'gravity.reader-cursor) 'namespace-contract)
        contract (var-get contract-var)]
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.reader-cursor (:namespace contract)))
    (is (= :stage0-reader-ignored-input-cursor
           (:contract-boundary contract)))
    (is (= #{'skip-line-comment! 'skip-ignored!}
           (set (keys (:public-api contract)))))
    (is (= ['clojure.core
            'clojure.lang.LineNumberingPushbackReader
            'gravity.source-span]
           (get-in contract [:dependency-direction :requires])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (is (= #{'source-span}
           (set (keys (ns-aliases 'gravity.reader-cursor)))))
    (is (= #{'skip-line-comment! 'skip-ignored!}
           (set (keys (ns-publics 'gravity.reader-cursor)))))
    (is (some #{:reader-diagnostic-classification}
              (get-in contract [:ownership :does-not-own])))
    (is (some #{:bootstrap-orchestration}
              (get-in contract [:ownership :does-not-own])))
    (doseq [name '[skip-line-comment! skip-ignored!]]
      (is (= '([rdr]) (:arglists (meta (ns-resolve 'gravity.reader-cursor name))))
          name))
    (is (true? (:bootstrap-hosted? contract)))
    (is (true? (:clojure-seed-boundary? contract)))
    (is (false? (:self-hosted? contract)))))
