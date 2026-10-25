(ns gravity.native-syntax-invocation-test
  "Explicit invocation routing; no artifact field grants source authority."
  (:require [clojure.test :refer [deftest is]] [gravity.bootstrap :as b]))
(defn- route-failure [f] (try (f) nil (catch clojure.lang.ExceptionInfo e (ex-data e))))
(deftest native-c3-literal-authentication-forwards-only-explicit-invocation
  ;; Routing unit fixtures, not authentic source products or admission evidence.
  (let [invocation {:reader-source-path "selected-reader.gravity"
                    :authenticated-envelope-options {:source :development-current}}
        proof {:status :complete}
        c2 {:top-level-form-ids []}
        c3 {:c2-reader-artifact c2 :syntax-object-stream [] :artifact-id "source-id"
            :gravity-syntax-boundary {} :capability-based-proof proof}
        front {:c2-reader-artifact c2 :top-level-form-ids []
               :authenticated-c3-source-artifact c3 :c3-artifact-id "source-id"
               :c3-capability-proof proof :c3-syntax-object-stream [] :records [] :forms []}
        calls (atom [])]
    (with-redefs [b/c3-c2-reader-integrity-report (fn [_] {:authentic? true})
                  b/c3-artifact-id (fn [_] "source-id")
                  b/c3-syntax-capability-proof
                  (fn [_ actual] (swap! calls conj [:proof actual])
                    (if (= invocation actual) proof {:status :incomplete}))
                  b/c3-syntax-stream-reader-products-authentic?
                  (fn [_ _ _ actual] (swap! calls conj [:stream actual])
                    (= invocation actual))]
      (is (map? (b/p15-s23-c6c10-literal-authentication "test.gravity" front invocation)))
      (is (= [[:proof invocation] [:stream invocation]] @calls))
      (reset! calls [])
      (is (= :fresh-authentic-embedded-c2-c3-products
             (:missing-fact (route-failure #(b/p15-s23-c6c10-literal-authentication
                                            "test.gravity" (assoc front :invocation invocation))))))
      (is (= [[:proof nil] [:stream nil]] @calls))
      (is (= :fresh-authentic-embedded-c2-c3-products
             (:missing-fact (route-failure #(b/p15-s23-c6c10-literal-authentication
                                            "test.gravity" (assoc-in front [:c3-capability-proof :status] :forged)
                                            invocation))))))))
(deftest native-c3-projection-forwards-invocation-and-keeps-historical-default
  (with-redefs [b/p15-s23-c6c10-literal-authentication
                (fn [_ _ invocation] (throw (ex-info "routing stop" {:invocation invocation})))]
    (let [invocation {:reader-source-path "selected-reader.gravity"
                      :authenticated-envelope-options {:source :development-current}}]
      (is (= invocation (:invocation (route-failure #(b/p15-s23-c6c10-project-front-end
                                                      "test.gravity" {} invocation)))))
      (is (nil? (:invocation (route-failure #(b/p15-s23-c6c10-project-front-end
                                            "test.gravity" {:invocation invocation}))))))))
(deftest native-c3-ingress-uses-host-selection-not-artifact-fields
  (let [ingress (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-private-ingress-products)
        forged {:reader-source-path "forged.gravity"
                :authenticated-envelope-options {:source :development-current}}
        calls (atom [])]
    (with-redefs [b/reader-project-context-for-source (fn [_] {})
                  b/p15-s23-stage2-c2-c3-front-end-products (fn [& _] {:invocation forged})
                  b/p15-s23-c6c10-literal-authentication
                  (fn [_ _ invocation] (swap! calls conj invocation) {})
                  b/p15-s23-c6c10-project-front-end
                  (fn [_ _ invocation] (throw (ex-info "routing stop" {:invocation invocation})))]
      (let [expected {:reader-source-path "selected-reader.gravity"
                      :authenticated-envelope-options {:source :development-current}}]
        (is (= expected (:invocation (route-failure #(ingress "test.gravity" "source" "selected-reader.gravity")))))
        (is (= [expected] @calls)))
      (reset! calls [])
      (is (nil? (:invocation (route-failure #(ingress "test.gravity" "source")))))
      (is (= [nil] @calls)))))

(deftest native-c3-current-products-require-explicit-host-invocation
  ;; Real current C2/C3 products and fresh verification, not the routing mocks.
  ;; This is development evidence, not historical source admission or C6 proof.
  (let [canonical #(.getCanonicalPath (java.io.File. %))
        path (canonical "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/zero_arity_bool_true_source.gravity")
        reader (canonical "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
        invocation {:reader-source-path reader
                    :authenticated-envelope-options {:source :development-current}}
        raw (b/p15-s23-stage2-c2-c3-front-end-products
             path (slurp path) (b/reader-project-context-for-source path) true reader)
        authentication (b/p15-s23-c6c10-literal-authentication path raw invocation)
        projection (b/p15-s23-c6c10-project-front-end path raw invocation)]
    (is (vector? (:records authentication)))
    (is (seq (:forms-by-id authentication)))
    (is (= (:forms raw) (:forms projection)))
    (is (= :fresh-authentic-embedded-c2-c3-products
           (:missing-fact
            (route-failure #(b/p15-s23-c6c10-literal-authentication
                              path (assoc raw :invocation invocation))))))
    (is (= :fresh-authentic-embedded-c2-c3-products
           (:missing-fact
            (route-failure #(b/p15-s23-c6c10-literal-authentication
                              path (assoc-in raw [:c3-capability-proof :status] :forged)
                              invocation)))))))
