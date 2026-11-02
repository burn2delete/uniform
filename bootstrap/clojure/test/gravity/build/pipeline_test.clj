(ns gravity.build.pipeline-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.build :as build]))

(defn- failure [operation]
  (try (operation) nil
       (catch clojure.lang.ExceptionInfo error
         {:message (.getMessage error) :data (ex-data error)})))

(defn- compile-fixture [relative]
  (let [path (.getCanonicalPath (io/file relative))
        text (slurp path :encoding "UTF-8")
        macro-artifact (b/macro-source-artifact path text)
        module (assoc (:module macro-artifact) :forms (:expanded-forms macro-artifact))]
    (b/stage0-compiled-core-plan path text module)))

(deftest native-contract-is-explicit-and-has-no-reverse-bootstrap-dependency
  (is (= #{'check-graph-effects! 'check-offline-recipe!}
         (set (keys (ns-publics 'gravity.build)))))
  (is (= #{'set} (set (keys (ns-aliases 'gravity.build)))))
  (doseq [[name operation] (ns-publics 'gravity.build)]
    (let [contract (:gravity/namespace-contract (meta operation))]
      (is (= 'gravity.build (:namespace contract)))
      (is (= (:arglists (meta operation)) (get-in contract [:public-api name :arglists])))
      (is (= :none (get-in contract [:public-api name :admission])))
      (is (false? (:authenticates-inputs? contract)))
      (is (false? (:performs-io? contract)))
      (is (false? (:self-hosted? contract))))))

(deftest metadata-checks-preserve-diagnostics-and-surrounding-data
  (let [module {:source-path "metadata.gravity" :profile :hosted :target :jvm}
        graph {:artifact-id "graph:unbound-data" :declared-effects #{:build/read-file}
               :nodes [{:node-id :read :effects #{:build/network}}]
               :unsafe-island {:audit :required} :ai/human-review :required}
        recipe {:artifact-id "recipe:unbound-data" :environment {:network :enabled}
                :runtime-checked [:retained-check]}]
    (doseq [[direct wrapper subject expected field]
            [[build/check-graph-effects! b/validate-stage0-compiled-build-graph!
              graph "PKG2001" :declared-effects]
             [build/check-offline-recipe! b/validate-stage0-compiled-reproducible-build!
              recipe "PKG7003" :controlled-network]]]
      (let [owned (failure #(direct #'b/compiled-package-fail! module subject))
            compatibility (failure #(wrapper module subject))]
        (is (= owned compatibility))
        (is (= expected (get-in owned [:data :id])))
        (is (= [field] (get-in owned [:data :missing-fields])))
        (is (= :stage0-compiled-package-gate (get-in owned [:data :stage])))
        (is (= {:source "metadata.gravity" :form-index 0}
               (get-in owned [:data :source-span])))))
    (is (nil? (build/check-graph-effects! #'b/compiled-package-fail! module
                                        (assoc-in graph [:nodes 0 :effects] #{:build/read-file}))))
    (is (nil? (build/check-offline-recipe! #'b/compiled-package-fail! module
                                         (assoc-in recipe [:environment :network] :disabled))))))

(deftest diagnostic-var-is-read-at-the-original-failure-position
  ;; Trusted host callback behavior only; neither callback issues authority.
  (doseq [operation [(fn [module graph]
                      (build/check-graph-effects! #'b/compiled-package-fail! module graph))
                    b/validate-stage0-compiled-build-graph!]]
    (let [calls (atom []) module {:source-path "late-root.gravity"}
          final (fn [& arguments] (swap! calls conj (vec arguments)) :late-root)
          graph {:declared-effects #{}
                 :nodes (lazy-seq
                         (alter-var-root #'b/compiled-package-fail! (constantly final))
                         (list {:effects #{:build/network}}))}]
      (with-redefs [b/compiled-package-fail! (fn [& _] (throw (ex-info "Premature diagnostic root" {})))]
        (is (= :late-root (operation module graph))))
      (is (= [["PKG2001" module graph {:missing-fields [:declared-effects]}]] @calls))))
  (let [denial (ex-info "original diagnostic failure" {:id "ORIGINAL"})
        observed (with-redefs [b/compiled-package-fail! (fn [& _] (throw denial))]
                   (try (build/check-offline-recipe! #'b/compiled-package-fail! {} {})
                        (catch Throwable error error)))]
    (is (identical? denial observed))))

(deftest complete-source-plans-emit-run-and-reject-before-execution
  (doseq [extension [".gravity" ".qst"]]
    (let [relative (str "bootstrap/clojure/fixtures/build/accepted/metadata-checks" extension)
          plan (compile-fixture relative)]
      (is (= :gravity/stage0-hosted-core-compiled-plan (:kind plan)))
      (is (= (:plan-id plan) (str "sha256:" (b/sha256-hex (pr-str (dissoc plan :plan-id))))))
      (is (= (get-in plan [:source :sha256]) (str "sha256:" (b/sha256-hex (slurp relative)))))
      (is (= (.getCanonicalPath (io/file relative)) (get-in plan [:source :path])))
      (is (= "build-metadata\n" (b/execute-stage0-compiled-plan plan))))
    (doseq [[name expected] [["undeclared-effect" "PKG2001"] ["uncontrolled-network" "PKG7003"]]]
      (let [relative (str "bootstrap/clojure/fixtures/build/rejected/" name extension)
            executions (atom 0)
            rejected (with-redefs [b/execute-stage0-compiled-plan (fn [_] (swap! executions inc))]
                       (failure #(b/run-compiled-file relative)))]
        (is (= expected (get-in rejected [:data :id])))
        (is (= :stage0-compiled-package-gate (get-in rejected [:data :stage])))
        (is (zero? @executions))))))
