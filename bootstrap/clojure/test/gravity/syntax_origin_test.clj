(ns gravity.syntax-origin-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.syntax-origin :as syntax-origin]))

(deftest source-and-generated-record-shapes-are-exact-and-ordered
  (let [span {:form-index 17 :source "module.gravity"}
        first-source-span (Object.)
        second-source-span (Object.)
        seed {:syntax-id "syntax-17"
              :span span
              :generated-origin
              [{:reader-abbreviation :quote :from first-source-span}
               {:reader-abbreviation 'unquote-splicing
                :from second-source-span}]}
        result (syntax-origin/c3-origin-chain
                seed {:source-id "sha256:source"})]
    (is (= [{:kind :source
             :producer {:kind :reader
                        :name 'gravity.stage0/reader
                        :version "stage0"}
             :source-id "sha256:source"
             :span span
             :input-syntax-ids []
             :reason :source-read
             :build-effects []}
            {:kind :generated
             :producer {:kind :reader
                        :name 'gravity.stage0/reader-abbreviation
                        :version "stage0"}
             :inputs ["syntax-17"]
             :generated-span "generated:reader:quote:17"
             :source-span first-source-span
             :reason :quote
             :build-effects []}
            {:kind :generated
             :producer {:kind :reader
                        :name 'gravity.stage0/reader-abbreviation
                        :version "stage0"}
             :inputs ["syntax-17"]
             :generated-span "generated:reader:unquote-splicing:17"
             :source-span second-source-span
             :reason 'unquote-splicing
             :build-effects []}]
           result))
    (is (vector? result))
    (is (identical? span (:span (first result))))
    (is (identical? first-source-span (:source-span (second result))))
    (is (identical? second-source-span (:source-span (nth result 2))))))

(deftest absent-fields-and-abbreviation-fallback-match-seed-behavior
  (let [result (syntax-origin/c3-origin-chain
                {:generated-origin [nil
                                    {:reader-abbreviation false :from nil}
                                    {:reader-abbreviation "dispatch"}]}
                nil)]
    (is (= 4 (count result)))
    (is (= {:kind :source
            :producer {:kind :reader
                       :name 'gravity.stage0/reader
                       :version "stage0"}
            :source-id nil
            :span nil
            :input-syntax-ids []
            :reason :source-read
            :build-effects []}
           (first result)))
    (is (= ["generated:reader:abbreviation:"
            "generated:reader:abbreviation:"
            "generated:reader:dispatch:"]
           (mapv :generated-span (rest result))))
    (is (= [nil false "dispatch"]
           (mapv :reason (rest result))))
    (is (= [[nil] [nil] [nil]]
           (mapv :inputs (rest result))))
    (is (every? #(contains? % :source-span) (rest result)))))

(deftest nil-empty-and-sequential-generated-origins-preserve-boundaries
  (testing "nil and empty generated origins retain the mandatory source entry"
    (doseq [generated-origin [nil [] '()]]
      (let [result (syntax-origin/c3-origin-chain
                    {:span {:form-index 0}
                     :generated-origin generated-origin}
                    {:source-id :source})]
        (is (vector? result))
        (is (= 1 (count result)))
        (is (= :source (:kind (first result)))))))
  (testing "list and lazy inputs retain encounter order and are realized"
    (let [observed (atom [])
          origins (map (fn [abbreviation]
                         (swap! observed conj abbreviation)
                         {:reader-abbreviation abbreviation})
                       [:quote :deref :var])
          result (syntax-origin/c3-origin-chain
                  {:syntax-id :seed
                   :span {:form-index 4}
                   :generated-origin origins}
                  {})]
      (is (= [:quote :deref :var] @observed))
      (is (= [:source :generated :generated :generated]
             (mapv :kind result)))
      (is (= [:quote :deref :var]
             (mapv :reason (rest result)))))))

(deftest source-id-lookup-and-unrelated-fields-retain-exact-semantics
  (let [lookup-count (atom 0)
        source-unit (reify clojure.lang.ILookup
                      (valAt [_ key]
                        (swap! lookup-count inc)
                        (when (= key :source-id) :looked-up))
                      (valAt [_ key not-found]
                        (swap! lookup-count inc)
                        (if (= key :source-id) :looked-up not-found)))
        seed {:syntax-id :seed
              :span {:form-index 2}
              :generated-origin []
              :ignored (Object.)}
        result (syntax-origin/c3-origin-chain seed source-unit)]
    (is (= :looked-up (:source-id (first result))))
    (is (= 1 @lookup-count))
    (is (= 1 (count result)))
    (is (not (contains? (first result) :ignored)))))

(deftest syntax-origin-contract-is-narrow-hosted-and-nonauthoritative
  (let [contract-var (get (ns-interns 'gravity.syntax-origin)
                          'namespace-contract)
        contract (var-get contract-var)]
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.syntax-origin (:namespace contract)))
    (is (= :hosted-reader-derived-syntax-origin-projection
           (:contract-boundary contract)))
    (is (= #{'c3-origin-chain} (set (keys (:public-api contract)))))
    (is (= '([seed source-unit])
           (:arglists (meta #'syntax-origin/c3-origin-chain))))
    (is (= ['clojure.core]
           (get-in contract [:dependency-direction :requires])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (is (= #{:hosted-reader-derived-syntax-origin-projection}
           (set (get-in contract [:ownership :owns]))))
    (doseq [nonclaim [:canonical-c3-syntax-object-authority
                      :source-reading
                      :source-authentication
                      :macro-expansion
                      :macro-provenance
                      :hygiene-semantics
                      :diagnostic-construction
                      :bootstrap-orchestration
                      :self-hosted-authority]]
      (is (some #{nonclaim} (get-in contract [:ownership :does-not-own]))
          nonclaim))
    (is (empty? (ns-aliases 'gravity.syntax-origin)))
    (is (= #{'c3-origin-chain}
           (set (keys (ns-publics 'gravity.syntax-origin)))))
    (is (nil? (find-ns 'gravity.bootstrap)))
    (is (true? (:bootstrap-hosted? contract)))
    (is (true? (:clojure-seed-boundary? contract)))
    (is (false? (:canonical-c3-authority? contract)))
    (is (false? (:self-hosted? contract)))))
