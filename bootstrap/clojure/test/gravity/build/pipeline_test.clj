(ns gravity.build.pipeline-test
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.set :as set]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.build :as build]))

(defn- failure [operation]
  (try (operation) nil
       (catch clojure.lang.ExceptionInfo error
         {:message (.getMessage error) :data (ex-data error)})))

(defn- compile-fixture
  ([relative] (compile-fixture relative identity))
  ([relative transform-module]
   (let [path (.getCanonicalPath (io/file relative))
         text (slurp path :encoding "UTF-8")
         macro-artifact (b/macro-source-artifact path text)
         module (assoc (:module macro-artifact) :forms (:expanded-forms macro-artifact))]
     (b/stage0-compiled-core-plan path text (transform-module module)))))

(deftest current-uniform-metadata-predicates-execute-without-entering-build-gates
  ;; Development execution of current source; no migration pins are renewed.
  (let [path (.getCanonicalPath
              (io/file "bootstrap/gravity/src/gravity/self_hosting/hermetic_package_build.gravity"))
        gate (fn [& _] (throw (ex-info "Recursive compiled build gate" {})))
        execute b/p15-s23-stage2-runtime-execute-function
        calls (atom [])
        invoke (fn [callee arguments]
                 (with-redefs [b/validate-stage0-compiled-build-graph! gate
                               b/validate-stage0-compiled-reproducible-build! gate
                               b/p15-s23-stage2-runtime-execute-function
                               (fn [runtime plan predicate args]
                                 (swap! calls conj predicate)
                                 (is (= (str "sha256:" (b/sha256-hex (slurp path)))
                                        (get-in plan [:source :sha256])))
                                 (execute runtime plan predicate args))]
                   (b/execute-build-metadata-policy! callee arguments)))]
    (doseq [[declared nodes expected]
            [[#{} [] true]
             [#{} [#{}] true]
             [#{:build/read-file :build/write-artifact}
              [#{:build/read-file} #{} #{:build/write-artifact}] true]
             [#{:build/read-file}
              [#{:build/read-file} #{} #{:build/network}] false]]]
      (is (= expected (invoke 'build-graph-effects-declared? [declared nodes]))))
    (doseq [[network expected] [[:disabled true] [:enabled false] [nil false]
                              [false false] ["disabled" false]]]
      (is (= expected (invoke 'offline-build-recipe? [network]))))
    (is (= (concat (repeat 4 'build-graph-effects-declared?)
                   (repeat 5 'offline-build-recipe?)) @calls))))

(deftest native-contract-is-explicit-and-has-no-reverse-bootstrap-dependency
  (is (= #{'check-graph-effects! 'check-offline-recipe!}
         (set (keys (ns-publics 'gravity.build)))))
  (is (= #{} (set (keys (ns-aliases 'gravity.build)))))
  (doseq [[name operation] (ns-publics 'gravity.build)]
    (let [contract (:gravity/namespace-contract (meta operation))]
      (is (= 'gravity.build (:namespace contract)))
      (is (= (:arglists (meta operation)) (get-in contract [:public-api name :arglists])))
      (is (= :none (get-in contract [:public-api name :admission])))
      (is (false? (:authenticates-inputs? contract)))
      (is (false? (:performs-io? contract)))
      (is (false? (:self-hosted? contract))))))

(deftest transport-uses-only-supplied-policy-results
  (let [calls (atom [])
        execute (fn [predicate arguments]
                  (swap! calls conj [predicate arguments]) true)
        fail (fn [& _] (throw (ex-info "Host metadata decision survived" {})))]
    (with-redefs [set/subset? (fn [& _] (throw (ex-info "Host subset survived" {})))
                  b/compiled-package-fail! fail]
      ;; Acceptance of these subjects must come only from the supplied result.
      ;; The real Uniform executor rejects them in the diagnostic tests below.
      (is (nil? (build/check-graph-effects!
                 execute #'b/compiled-package-fail! {}
                 {:declared-effects #{} :nodes [{:effects #{:build/network}}]})))
      (is (nil? (build/check-offline-recipe!
                 execute #'b/compiled-package-fail! {} {:environment {:network :enabled}}))))
    (is (= [['build-graph-effects-declared? [#{} [#{:build/network}]]]
            ['offline-build-recipe? [:enabled]]] @calls))))

(deftest policy-results-fail-closed-and-preserve-execution-failures
  (doseq [gate [b/validate-stage0-compiled-build-graph!
                b/validate-stage0-compiled-reproducible-build!]
          result [nil :accepted 1 "true" [] {} {:status :accepted}]]
    (let [diagnostics (atom 0)
          observed (with-redefs [b/p15-s23-stage2-runtime-execute-function (fn [& _] result)
                                 b/compiled-package-fail! (fn [& _] (swap! diagnostics inc))]
                     (failure #(gate {} {})))]
      (is (= "BUILD-METADATA-POLICY" (get-in observed [:data :id])))
      (is (= :nonboolean-result (get-in observed [:data :reason])))
      (is (zero? @diagnostics))))
  (is (= :execution-unavailable
         (get-in (failure #(build/check-offline-recipe! nil #'b/compiled-package-fail! {} {}))
                 [:data :reason])))
  (let [denial (ex-info "original execution failure" {:id "ORIGINAL"})]
    (doseq [gate [b/validate-stage0-compiled-build-graph!
                  b/validate-stage0-compiled-reproducible-build!]]
      (is (identical? denial
                      (with-redefs [b/p15-s23-stage2-runtime-execute-function (fn [& _] (throw denial))]
                        (try (gate {} {}) (catch Throwable error error))))))))

(deftest current-policy-rejects-unavailable-stale-and-malformed-source-bindings
  (let [path (.getCanonicalPath
              (io/file "bootstrap/gravity/src/gravity/self_hosting/hermetic_package_build.gravity"))
        text (slurp path :encoding "UTF-8")
        emitter (:emitter (b/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (b/p15-s23-stage2-compiler-artifact-plan emitter path text)
        path-var (ns-resolve 'gravity.bootstrap 'build-metadata-policy-source-path)
        source-var (ns-resolve 'gravity.bootstrap 'build-metadata-policy-source!)]
    ;; The raw constructor's macro projection omits exports, even though the
    ;; source declares them. Its internal bindings use this exact transport ABI.
    (is (not (contains? (:module plan) :exports)))
    (doseq [predicate '[build-graph-effects-declared? offline-build-recipe?]]
      (is (= :stage2-local (get-in plan [:functions predicate :binding :visibility]))))
    (doseq [gate [b/validate-stage0-compiled-build-graph!
                  b/validate-stage0-compiled-reproducible-build!]]
      (doseq [[candidate reason]
              [[nil :stale-source-binding]
               [(assoc-in plan [:source :sha256] "sha256:stale") :stale-source-binding]
               [(assoc plan :kind :wrong) :predicate-abi]
               [(assoc-in plan [:module :effects] #{:build/network}) :predicate-abi]
               [(assoc-in plan [:module :exports] '[offline-build-recipe?]) :predicate-abi]
               [(update plan :functions dissoc 'offline-build-recipe?) :predicate-abi]
               [(assoc-in plan [:functions 'build-graph-effects-declared? :params] ['nodes]) :predicate-abi]
               [(assoc-in plan [:functions 'offline-build-recipe? :arity] 2) :predicate-abi]
               [(assoc-in plan [:functions 'offline-build-recipe? :binding :namespace] 'other) :predicate-abi]
               [(assoc-in plan [:functions 'offline-build-recipe? :binding :visibility] :private) :predicate-abi]
               [(assoc-in plan [:functions 'offline-build-recipe? :binding :visibility] :public) :predicate-abi]
               [(assoc-in plan [:functions 'offline-build-recipe? :instructions] []) :predicate-abi]]]
        (let [executions (atom 0)
              observed (with-redefs [b/p15-s23-stage2-compiler-artifact-plan (fn [& _] candidate)
                                     b/p15-s23-stage2-runtime-execute-function (fn [& _] (swap! executions inc) true)]
                         (failure #(gate {} {})))]
          (is (= "BUILD-METADATA-POLICY" (get-in observed [:data :id])))
          (is (= reason (get-in observed [:data :reason])))
          (is (zero? @executions))))
      (doseq [changed-text [(.replace text "sh23-verify-result])"
                                      "sh23-verify-result offline-build-recipe?])")
                           (.replace text "(:safety :safe)" "(:safety :audited-unsafe)")]]
        (let [executions (atom 0)
              observed (with-redefs-fn
                         {source-var (constantly changed-text)
                          #'b/p15-s23-stage2-runtime-execute-function
                          (fn [& _] (swap! executions inc) true)}
                         #(failure (fn [] (gate {} {}))))]
          (is (= :predicate-abi (get-in observed [:data :reason])))
          (is (zero? @executions))))
      (let [missing (str path ".missing-" (java.util.UUID/randomUUID))]
        (is (= :source-unavailable
               (get-in (with-redefs-fn {path-var (constantly missing)} #(failure (fn [] (gate {} {}))))
                       [:data :reason]))))
      (let [reads (atom 0)]
        (is (= :stale-source-binding
               (get-in (with-redefs-fn
                         {source-var (fn [_] (if (= 1 (swap! reads inc)) text (str text "\n")))
                          #'b/p15-s23-stage2-compiler-artifact-plan (fn [& _] plan)}
                         #(failure (fn [] (gate {} {}))))
                       [:data :reason])))))))

(deftest cold-loading-keeps-the-build-leaf-independent
  (let [result (shell/sh
                (str (io/file (System/getProperty "java.home") "bin" "java"))
                "-cp" (System/getProperty "java.class.path") "clojure.main" "-e"
                (str "(require 'gravity.build)"
                     "(assert (nil? (find-ns 'gravity.bootstrap)))"
                     "(require 'gravity.bootstrap)"
                     "(assert (nil? (gravity.bootstrap/validate-stage0-compiled-build-graph! {} {:declared-effects #{} :nodes []})))"
                     "(assert (nil? (gravity.bootstrap/validate-stage0-compiled-reproducible-build! {} {:environment {:network :disabled}})))"
                     "(println \"cold-build-policy-ok\")"))]
    (is (= 0 (:exit result)) (:err result))
    (is (= "cold-build-policy-ok\n" (:out result)))))

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
      (let [owned (failure #(direct b/execute-build-metadata-policy! #'b/compiled-package-fail! module subject))
            compatibility (failure #(wrapper module subject))]
        (is (= owned compatibility))
        (is (= expected (get-in owned [:data :id])))
        (is (= [field] (get-in owned [:data :missing-fields])))
        (is (= :stage0-compiled-package-gate (get-in owned [:data :stage])))
        (is (= {:source "metadata.gravity" :form-index 0}
               (get-in owned [:data :source-span])))))
    (is (nil? (build/check-graph-effects! b/execute-build-metadata-policy! #'b/compiled-package-fail! module
                                        (assoc-in graph [:nodes 0 :effects] #{:build/read-file}))))
    (is (nil? (build/check-offline-recipe! b/execute-build-metadata-policy! #'b/compiled-package-fail! module
                                         (assoc-in recipe [:environment :network] :disabled))))))

(deftest diagnostic-var-is-read-at-the-original-failure-position
  ;; Trusted host callback behavior only; neither callback issues authority.
  (doseq [operation [(fn [module graph]
                      (build/check-graph-effects! b/execute-build-metadata-policy! #'b/compiled-package-fail! module graph))
                    b/validate-stage0-compiled-build-graph!]]
    (let [calls (atom []) traversals (atom 0) module {:source-path "late-root.gravity"}
          final (fn [& arguments] (swap! calls conj (vec arguments)) :late-root)
          graph {:declared-effects #{}
                 :nodes (lazy-seq
                         (swap! traversals inc)
                         (alter-var-root #'b/compiled-package-fail! (constantly final))
                         (list {:effects #{:build/network}}))}]
      (with-redefs [b/compiled-package-fail! (fn [& _] (throw (ex-info "Premature diagnostic root" {})))]
        (is (= :late-root (operation module graph))))
      (is (= 1 @traversals))
      (is (= [["PKG2001" module graph {:missing-fields [:declared-effects]}]] @calls))))
  (let [denial (ex-info "original diagnostic failure" {:id "ORIGINAL"})]
    (doseq [[gate subject]
            [[b/validate-stage0-compiled-build-graph! {:declared-effects #{} :nodes [{:effects #{:build/network}}]}]
             [b/validate-stage0-compiled-reproducible-build! {}]]]
      (let [observed (with-redefs [b/compiled-package-fail! (fn [& _] (throw denial))]
                       (try (gate {} subject) (catch Throwable error error)))]
        (is (identical? denial observed))))))

(deftest complete-plan-gates-handle-empty-and-late-denied-graphs
  (doseq [extension [".gravity" ".qst"]
          [nodes rejected?] [[[] false]
                             [[{:effects #{:build/read-file}} {:effects #{}}
                               {:effects #{:build/write-artifact}}] false]
                             [[{:effects #{:build/read-file}} {:effects #{}}
                               {:effects #{:build/network}}] true]]]
    (let [relative (str "bootstrap/clojure/fixtures/build/accepted/metadata-checks" extension)
          graph {:declared-effects #{:build/read-file :build/write-artifact} :nodes nodes}
          execute b/p15-s23-stage2-runtime-execute-function
          calls (atom [])
          observed (with-redefs [b/p15-s23-stage2-runtime-execute-function
                                 (fn [runtime plan predicate arguments]
                                   (when (#{'build-graph-effects-declared? 'offline-build-recipe?} predicate)
                                     (swap! calls conj [predicate arguments]))
                                   (execute runtime plan predicate arguments))]
                     (try
                       (let [plan (compile-fixture
                                   relative #(assoc-in % [:metadata :package :compiled-gate :build-graphs] [graph]))]
                         {:output (b/execute-stage0-compiled-plan plan)})
                       (catch clojure.lang.ExceptionInfo error {:diagnostic (ex-data error)})))]
      (is (= ['build-graph-effects-declared? [(:declared-effects graph) (mapv :effects nodes)]]
             (first @calls)))
      (if rejected?
        (do (is (= "PKG2001" (get-in observed [:diagnostic :id])))
            (is (= 1 (count @calls))))
        (do (is (= "build-metadata\n" (:output observed)))
            (is (= ['offline-build-recipe? [:disabled]] (second @calls))))))))

(deftest complete-source-plans-emit-run-and-reject-before-execution
  (doseq [extension [".gravity" ".qst"]]
    (let [relative (str "bootstrap/clojure/fixtures/build/accepted/metadata-checks" extension)
          execute b/p15-s23-stage2-runtime-execute-function
          calls (atom [])
          plan (with-redefs [b/p15-s23-stage2-runtime-execute-function
                             (fn [runtime plan predicate arguments]
                               (when (#{'build-graph-effects-declared? 'offline-build-recipe?} predicate)
                                 (swap! calls conj [predicate arguments]))
                               (execute runtime plan predicate arguments))]
                 (compile-fixture relative))]
      (is (= [['build-graph-effects-declared? [#{:build/read-file} [#{:build/read-file}]]]
              ['offline-build-recipe? [:disabled]]] @calls))
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
