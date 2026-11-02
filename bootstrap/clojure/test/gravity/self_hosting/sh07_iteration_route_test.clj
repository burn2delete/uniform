(ns gravity.self-hosting.sh07-iteration-route-test
  "Additive route selection; BOOT7 parity without compiler proof execution."
  (:require [clojure.string :as str]
            [clojure.test :as test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh07-iteration-cache-runner :as runner]
            [gravity.self-hosting.unified-execution-cli :as cli]
            [gravity.self-hosting.unified-execution-plan :as plan]))

;; Independent oracle from production exact selections, not replacement claims.
(def expected-routes
  [{:id "sh07-b13-accepted" :label "b13-accepted"
    :namespace 'gravity.self-hosting.sh07-module-fragment-test
    :selectors '[sh07-b13-direct-and-public-routing-use-v14
                 sh07-b13-fragment-manifest-is-exact-root-aligned-and-exhaustive
                 sh07-b13-fragment-coverage-is-exact-ordered-and-complete]}
   {:id "sh07-conformance-table" :label "conformance-table"
    :namespace 'gravity.self-hosting.sh07-conformance-table-test
    :selectors '[reviewed-table-has-exact-scope authenticated-pairs-match-census
                 manifest-coverage-is-derived-from-rows static-validation-fails-closed
                 preserved-bespoke-boundaries-remain-nonauthoritative]}])

(defn qualified [{:keys [namespace selectors]}]
  (mapv #(symbol (str namespace) (str %)) selectors))
(defn explicit-args [route]
  (vec (mapcat #(vector "--test-var" (str %)) (qualified route))))
(defn error-id [operation]
  (try (operation) nil
       (catch clojure.lang.ExceptionInfo error (:id (ex-data error)))))

(deftest declaration-and-production-resolution-have-independent-oracles
  (let [projection (plan/legacy-mechanism-projection :iteration-cache-runner)]
    (is (= (mapv :id expected-routes) (:route-ids projection)))
    (is (= (mapv :label expected-routes) (:labels projection)))
    (is (= :explicit-catalog-selection (:membership-semantics projection)))
    (is (= {:kind :strip-prefix :prefix "sh07-"} (:label-policy projection)))
    (is (= 9 (count (:route-ids (plan/legacy-mechanism-projection :parallel-shard-runner)))))
    (is (= 11 (count (get (plan/legacy-route-catalog) :parallel-shard-runner)))))
  (doseq [route expected-routes]
    (let [selected (runner/parse-arguments ["--route" (:label route)])
          resolved (runner/resolve-route (:label route))
          cli-resolved (cli/resolve-legacy-shard :iteration-cache-runner (:label route))]
      (is (= (runner/parse-arguments (explicit-args route))
             (dissoc selected :route-selection)))
      (is (= (qualified route) (:test-vars selected)))
      (is (= resolved cli-resolved))
      (is (= (:id route) (:route-id resolved)))
      (is (= (:namespace route) (:namespace resolved)))
      (is (= (mapv #(ns-resolve (:namespace route) %) (:selectors route))
             (:test-vars resolved)))
      (is (every? #(identical? (find-ns (:namespace route)) (:ns (meta %)))
                  (:test-vars resolved))))))

(deftest route-parser-keeps-controls-and-rejects-ambiguous-selections
  (let [selected (runner/parse-arguments ["--fail-fast" "--max-cache-entries" "1"
                                         "--route" "b13-accepted"])]
    (is (= 1 (:maximum-entries selected)))
    (is (true? (:fail-fast? selected))))
  (doseq [[args id]
          [[["--route"] "SH07-ITERATION-CACHE-USAGE"]
           [["--route" "unknown"] "UEP-LEGACY-LABEL"]
           [["--route" "b13-accepted" "--route" "b13-accepted"]
            "SH07-ITERATION-CACHE-DUPLICATE-ROUTE"]
           [["--route" "b13-accepted" "--route" "conformance-table"]
            "SH07-ITERATION-CACHE-DUPLICATE-ROUTE"]
           [["--namespace" "gravity.cli-test" "--route" "b13-accepted"]
            "SH07-ITERATION-CACHE-SELECTION-CONFLICT"]
           [["--route" "b13-accepted" "--test-var" "gravity.cli-test/absent"]
            "SH07-ITERATION-CACHE-SELECTION-CONFLICT"]
           [["--route" "b13-accepted" "--max-cache-entries" "0"]
            "SH07-ITERATION-CACHE-ARGUMENT"]
           [["--route" "b13-accepted" "--lane" "fresh"]
            "SH07-ITERATION-CACHE-USAGE"]]]
    (is (= id (error-id #(runner/parse-arguments args)))))
  (is (= "UEP-LEGACY-LABEL"
         (error-id #(plan/legacy-route-selection :iteration-cache-runner :b13-accepted)))))

(deftest malformed-declarations-and-policies-fail-closed
  (let [path [:legacy-mechanisms :iteration-cache-runner]
        ids (mapv :id expected-routes)]
    (doseq [[suffix value id]
            [[[:route-ids] [] "UEP-CATALOG-LEGACY-ROUTES"]
             [[:route-ids] ["sh07-absent"] "UEP-CATALOG-LEGACY-ROUTE"]
             [[:route-ids] [(first ids) (first ids)] "UEP-CATALOG-LEGACY-ROUTES"]
             [[:route-ids] (vec (reverse ids)) "UEP-CATALOG-LEGACY-ROUTES"]
             [[:route-ids] (vec (sort (conj ids "sh07-b11-contract"))) "UEP-CATALOG-LEGACY-ROUTE"]
             [[:membership-semantics] :exact-historical-membership "UEP-CATALOG-LEGACY-MEMBERSHIP"]
             [[:label-policy :kind] :unknown "UEP-CATALOG-LEGACY-LABEL"]
             [[:label-policy :prefix] "" "UEP-CATALOG-LEGACY-LABEL"]
             [[:label-policy :prefix] nil "UEP-CATALOG-LEGACY-LABEL"]
             [[:label-policy :extra] true "UEP-CATALOG-LEGACY-LABEL-KEYS"]
             [[:extra] true "UEP-CATALOG-LEGACY-MECHANISM-KEYS"]]]
      (is (= id (error-id #(plan/validate-catalog (assoc-in plan/catalog (into path suffix) value)))))))
  (doseq [[suffix value]
          [[[:authority-ceiling] :acceptance-only]
           [[:cache-policy :authoritative?] true]
           [[:selection :kind] :unknown]
           [[:selection :selectors] ["duplicate" "duplicate"]]
           [[:selection :selectors] [42]]]]
    (is (string? (error-id #(plan/validate-catalog
                           (assoc-in plan/catalog (into [:routes "sh07-b13-accepted"] suffix) value)))))))

(deftest stale-catalog-and-altered-selection-cannot-reach-execution
  (let [selected (runner/parse-arguments ["--route" "b13-accepted"])
        path [:legacy-mechanisms :iteration-cache-runner :route-ids]
        catalogs [(assoc-in plan/catalog path ["sh07-b13-accepted"])
                  (assoc-in plan/catalog [:legacy-mechanisms :iteration-cache-runner :label-policy :prefix] "sh07-b")
                  (update-in plan/catalog [:routes "sh07-b13-accepted" :selection :selectors] #(vec (reverse %)))]]
    (doseq [catalog catalogs]
      (with-redefs [plan/catalog catalog]
        (is (= "UEP-PLAN-CATALOG" (error-id #(runner/run-selection selected))))))
    (doseq [altered [(assoc selected :test-vars (vec (reverse (:test-vars selected))))
                     (update selected :test-vars pop)
                     (update selected :test-vars conj (first (:test-vars selected)))
                     (assoc selected :namespaces ['gravity.cli-test])]]
      (is (= "SH07-ITERATION-CACHE-ROUTE-SELECTION" (error-id #(runner/run-selection altered)))))
    (is (= "UEP-LEGACY-SELECTION-KEYS"
           (error-id #(runner/run-selection (assoc-in selected [:route-selection :extra] true)))))
    (is (= "UEP-LEGACY-MECHANISM"
           (error-id #(runner/run-selection (assoc-in selected [:route-selection :mechanism] :unknown)))))))

(defn catalog-with-namespace [namespace]
  (-> plan/catalog
      (assoc-in [:routes "sh07-b13-accepted" :selection :namespace] namespace)
      (update-in [:routes "sh07-b13-accepted" :dependency-closure :paths]
                 #(mapv (fn [path]
                          (if (= path "bootstrap/clojure/test/gravity/self_hosting/sh07_module_fragment_test.clj")
                            (str "bootstrap/clojure/test/" (-> namespace (str/replace "." "/")
                                                              (str/replace "-" "_")) ".clj") path)) %))))

(deftest namespace-and-test-var-ownership-remain-fail-closed
  (with-redefs [plan/catalog (catalog-with-namespace "gravity.not-owned-test")]
    (is (= "SH01-TEST-NAMESPACE" (error-id #(runner/parse-arguments ["--route" "b13-accepted"])))))
  (doseq [selector ["absent-route-test" "" "clojure.core/eval"]]
    (with-redefs [plan/catalog (assoc-in plan/catalog [:routes "sh07-b13-accepted" :selection :selectors]
                                       [selector "another-absent-test" "third-absent-test"])]
      (is (= "SH07-ITERATION-CACHE-TEST-VAR" (error-id #(runner/resolve-route "b13-accepted")))))))

(defn normalize-result [result]
  (-> result (dissoc :elapsed-ms)
      (update :test-var-results #(mapv (fn [value] (dissoc value :elapsed-ms)) %))))

(deftest real-route-execution-preserves-cache-fixtures-receipts-and-skipped-tails
  (doseq [route expected-routes]
    (let [resolved (:test-vars (runner/resolve-route (:label route)))
          original-meta (mapv meta resolved)
          namespace (find-ns (:namespace route)) namespace-meta (meta namespace)
          calls (atom []) fixtures (atom []) misses (atom 0) fail-first? (atom false)]
      (try
        (doseq [[index var] (map-indexed vector resolved)]
          (alter-meta! var assoc :test
                       #(do (swap! calls conj index)
                            (bootstrap/sh06-resolution-source-artifact "same" "content")
                            (test/do-report {:type (if (and @fail-first? (zero? index)) :fail :pass)
                                             :expected true :actual (not @fail-first?)}))))
        (alter-meta! namespace assoc
                     :clojure.test/once-fixtures [(fn [op] (swap! fixtures conj :once) (op))]
                     :clojure.test/each-fixtures [(fn [op] (swap! fixtures conj :each) (op))])
        (doseq [fail-fast? [false true]]
          (reset! fail-first? fail-fast?)
          (let [controls (cond-> ["--max-cache-entries" "1"] fail-fast? (conj "--fail-fast"))
                invoke (fn [args]
                         (reset! calls []) (reset! fixtures []) (reset! misses 0)
                         (let [out (java.io.StringWriter.)
                               result (with-redefs [bootstrap/sh06-resolution-source-artifact
                                                    (fn [path source] (swap! misses inc) [path source])]
                                        (binding [*out* out test/*test-out* out]
                                          (runner/run-selection (runner/parse-arguments args))))]
                           {:result (normalize-result result) :calls @calls
                            :fixtures @fixtures :misses @misses}))
                explicit (invoke (into controls (explicit-args route)))
                routed (invoke (into controls ["--route" (:label route)]))
                result (:result routed)]
            (is (= explicit routed))
            (is (= 1 (:misses routed)))
            (is (= (if fail-fast? [0] (vec (range (count resolved)))) (:calls routed)))
            (is (= (vec (mapcat (constantly [:once :each]) (:calls routed))) (:fixtures routed)))
            (is (= (if fail-fast? (subvec (qualified route) 1) []) (:skipped-test-vars result)))
            (is (= :non-authoritative (:authority result)))
            (is (false? (:authoritative? result)))
            (is (true? (:fresh-authoritative-run-required? result)))))
        (finally
          (reset-meta! namespace namespace-meta)
          (doseq [[var metadata] (map vector resolved original-meta)] (reset-meta! var metadata)))))))
