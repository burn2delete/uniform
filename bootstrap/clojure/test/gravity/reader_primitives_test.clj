(ns gravity.reader-primitives-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.reader-primitives :as reader-primitives]))

(deftest form-kind-preserves-reader-classification-order-and-boundaries
  (doseq [[form expected]
          [[nil :nil]
           [true :boolean]
           [false :boolean]
           [0 :integer]
           [42N :integer]
           [1/3 :ratio]
           [0.0 :decimal]
           [Double/POSITIVE_INFINITY :decimal]
           ["" :string]
           [\newline :character]
           ['gravity.reader/value :symbol]
           [:gravity/value :keyword]
           [(list) :list]
           [(list 'quote 'x) :list]
           [[] :vector]
           [{} :map]
           [#{} :set]
           [(Object.) :unknown]]]
    (is (= expected (reader-primitives/form-kind form)) (pr-str form))
    (is (= expected (bootstrap/form-kind form)) (pr-str form))))

(deftest safe-excerpt-preserves-default-clamp-and-length-boundaries
  (testing "missing span coordinates use the historical zero/zero defaults"
    (doseq [span [nil {} {:start {}} {:start {:char 2}}]]
      (is (= "" (reader-primitives/safe-excerpt "abcd" span)))
      (is (= "" (bootstrap/safe-excerpt "abcd" span)))))
  (testing "end coordinates clamp to source length and preserve UTF-16 indexing"
    (doseq [[source span expected]
            [["abcdef" {:start {:char 1} :end {:char 4}} "bcd"]
             ["abcdef" {:start {:char 3} :end {:char 99}} "def"]
             ["a🙂b" {:start {:char 1} :end {:char 3}} "🙂"]
             ["" {:start {:char 0} :end {:char 0}} ""]]]
      (is (= expected (reader-primitives/safe-excerpt source span)))
      (is (= expected (bootstrap/safe-excerpt source span)))))
  (testing "160 characters are retained and longer excerpts gain an ellipsis"
    (let [exact (apply str (repeat 160 "x"))
          long (apply str (repeat 161 "x"))
          exact-span {:start {:char 0} :end {:char 160}}
          long-span {:start {:char 0} :end {:char 161}}]
      (is (= exact (reader-primitives/safe-excerpt exact exact-span)))
      (is (= (str exact "...")
             (reader-primitives/safe-excerpt long long-span)))
      (is (= (reader-primitives/safe-excerpt long long-span)
             (bootstrap/safe-excerpt long long-span)))))
  (testing "invalid non-clamped boundaries retain subs exception behavior"
    (doseq [span [{:start {:char -1} :end {:char 1}}
                  {:start {:char 3} :end {:char 2}}
                  {:start {:char 9} :end {:char 10}}]]
      (is (thrown? IndexOutOfBoundsException
                   (reader-primitives/safe-excerpt "abcd" span)))
      (is (thrown? IndexOutOfBoundsException
                   (bootstrap/safe-excerpt "abcd" span))))))

(deftest abbreviation-kind-preserves-longest-prefix-precedence
  (doseq [[excerpt expected]
          [["~@values" :splice-unquote]
           ["~value" :unquote]
           ["'value" :quote]
           ["`value" :syntax-quote]
           ["^:private value" :metadata]
           ["@value" :deref]
           ["" nil]
           [" value" nil]
           ["~~value" :unquote]
           ["#'value" nil]]]
    (is (= expected (reader-primitives/abbreviation-kind excerpt)) excerpt)
    (is (= expected (bootstrap/abbreviation-kind excerpt)) excerpt)))

(deftest source-metadata-removes-only-reader-location-fields
  (doseq [[form expected]
          [[nil {}]
           ['plain-symbol {}]
           [(with-meta 'located
              {:line 1 :column 2 :end-line 3 :end-column 4
               :doc "kept" :gravity/custom false})
            {:doc "kept" :gravity/custom false}]
           [(with-meta [] {:line nil :column nil :private true})
            {:private true}]]]
    (is (= expected (reader-primitives/source-metadata form)))
    (is (= expected (bootstrap/source-metadata form)))))

(deftest public-api-arglists-remain-compatible-with-bootstrap-wrappers
  (doseq [name '[form-kind safe-excerpt abbreviation-kind source-metadata]]
    (is (= (:arglists (meta (ns-resolve 'gravity.reader-primitives name)))
           (:arglists (meta (ns-resolve 'gravity.bootstrap name))))
        name)))

(deftest reader-primitives-contract-is-narrow-and-acyclic
  (let [contract-var
        (get (ns-interns 'gravity.reader-primitives) 'namespace-contract)
        contract (var-get contract-var)]
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.reader-primitives (:namespace contract)))
    (is (= :stage0-reader-metadata-primitives (:contract-boundary contract)))
    (is (= #{'form-kind 'safe-excerpt 'abbreviation-kind 'source-metadata}
           (set (keys (:public-api contract)))))
    (is (= ['clojure.string]
           (get-in contract [:dependency-direction :requires])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (is (= #{'str} (set (keys (ns-aliases 'gravity.reader-primitives)))))
    (is (= #{'form-kind 'safe-excerpt 'abbreviation-kind 'source-metadata}
           (set (keys (ns-publics 'gravity.reader-primitives)))))
    (is (some #{:diagnostic-construction}
              (get-in contract [:ownership :does-not-own])))
    (is (some #{:bootstrap-orchestration}
              (get-in contract [:ownership :does-not-own])))
    (is (true? (:bootstrap-hosted? contract)))
    (is (true? (:clojure-seed-boundary? contract)))
    (is (false? (:self-hosted? contract)))))
