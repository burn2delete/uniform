(ns gravity.self-hosting.c2-canonical-default-dispatch-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c2-artifact-identity :as identity]))

(defn- operation-map []
  ((ns-resolve 'gravity.bootstrap 'c2-artifact-identity-ops)))

(deftest default-canonical-recursion-uses-only-the-entrypoint-trampoline
  (let [value {:nested [1 (list :a #{"b" "c"})]}
        expected (identity/reader-canonical-value value)
        entry identity/call-entrypoint-body calls (atom [])]
    (is (not (contains? (operation-map) :reader-canonical-value)))
    (with-redefs [identity/call-entrypoint-body
                  (fn [key operation args]
                    (swap! calls conj key) (entry key operation args))]
      (is (= expected (bootstrap/reader-canonical-value value))))
    (is (= [:reader-canonical-value] @calls))
    (is (= (identity/with-operations {:sha256-hex bootstrap/sha256-hex}
             #(identity/reader-canonical-hash value))
           (bootstrap/reader-canonical-hash value)))))

(deftest bootstrap-canonical-var-override-retains-every-recursive-observation
  (let [original bootstrap/reader-canonical-value
        value [1 [2]] visits (atom [])]
    (with-redefs [bootstrap/reader-canonical-value
                  (fn [item]
                    (swap! visits conj item)
                    (let [result (original item)] (if (= item 2) :interposed result)))]
      (is (identical? bootstrap/reader-canonical-value
                      (:reader-canonical-value (operation-map))))
      (is (= [:vector [1 [:vector [:interposed]]]]
             (bootstrap/reader-canonical-value value))))
    (is (= [value 1 [2] 2] @visits))))

(deftest leaf-canonical-var-override-keeps-the-compatibility-operation
  (let [original identity/reader-canonical-value visits (atom [])]
    (with-redefs [identity/reader-canonical-value
                  (fn [item] (swap! visits conj item) (original item))]
      (is (identical? bootstrap/reader-canonical-value
                      (:reader-canonical-value (operation-map))))
      (is (= [:vector [1]] (bootstrap/reader-canonical-value [1]))))
    (is (seq @visits))))
