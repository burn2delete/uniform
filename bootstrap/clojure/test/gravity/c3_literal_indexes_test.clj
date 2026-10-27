(ns gravity.c3-literal-indexes-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.c3-literal-projection :as l]
            [gravity.c3-literal-projection.indexes :as indexes]))
(deftest effective-operations-compare-identity-not-function-results
  (let [callback (fn [_] (throw (Exception. "must not call")))
        defaults @(ns-resolve 'gravity.c3-literal-projection 'default-operations)
        expected (merge defaults {:c3-form-index callback})]
    (l/with-operations {:c3-form-index callback}
      #(do (is (l/c3-index-operations-current? expected))
           (is (false? (l/c3-index-operations-current? (assoc expected :c3-form-index identity))))
           (is (false? (l/c3-index-operations-current? (dissoc expected :form-kind))))))))
(deftest opaque-keys-are-rejected-before-lookup-hash-or-equality
  (let [calls (atom 0) opaque (proxy [Object] []
                              (hashCode [] (swap! calls inc) 1)
                              (equals [_] (swap! calls inc) false)
                              (toString [] (swap! calls inc) "opaque"))
        bad (assoc (zipmap (map #(keyword (str "k" %)) (range 20)) (repeat identity)) opaque identity)]
    (reset! calls 0)
    (is (false? (indexes/operations-current? bad {:form-kind identity})))
    (is (false? (indexes/operations-current? {:form-kind identity} bad)))
    (is (zero? @calls))))
(deftest index-factory-callbacks-remain-explicit-and-ordered
  (let [calls (atom []) error (ex-info "tokens reject" {})]
    (is (identical? error
          (try (l/with-operations
                {:c3-form-index (fn [_] (swap! calls conj :forms) {})
                 :c3-token-index (fn [_] (swap! calls conj :tokens) (throw error))}
                #(l/c3-lossless-literal-descriptor {} {:kind :list} {} {:authentic? false}))
               nil (catch Throwable t t))))
    (is (= [:forms :tokens] @calls))))
