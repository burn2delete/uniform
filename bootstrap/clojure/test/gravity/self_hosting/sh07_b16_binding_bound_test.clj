(ns gravity.self-hosting.sh07-b16-binding-bound-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_b16_binding_bound_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-07 B16 binding-bound test source is absent"
                {:id "SH07-B16-BINDING-BOUND-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH07-B16-BINDING-BOUND-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(defn- compile-plan
  [relative-path]
  (let [source-path (path relative-path)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private checked-core-plan
  (delay
   (compile-plan
    "bootstrap/gravity/src/gravity/checked_core.gravity")))

(defn- invoke
  [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh07-b16-binding-bound-test
    :compiler-artifact-plan? true}
   @checked-core-plan function arguments))

(defn- ordinal-sha
  [ordinal]
  (str "sha256:" (format "%064x" (biginteger ordinal))))

(deftest sh07-b16-aggregate-binding-bound-is-isolated
  (let [bounds (invoke 'sh07-bounds-value [])]
    (is (= 2048 (:maximum-bindings bounds)))
    (is (= 1024 (:maximum-lexical-binding-records bounds)))
    (is (= 1024 (:maximum-loop-binding-records bounds)))
    (is (= 65536 (:maximum-combined-lexical-loop-records bounds)))
    (is (= 8388608 (:maximum-carrier-nodes bounds)))
    (is (= 268435456 (:maximum-scalar-bytes bounds)))
    (is (= 16777216 (:maximum-template-carrier-nodes bounds)))
    (is (= 256 (:maximum-template-carrier-depth bounds)))
    (is (= 65536 (:maximum-template-carrier-width bounds)))
    (is (= 536870912 (:maximum-template-scalar-bytes bounds)))
    (is (= 16777216 (:maximum-resolved-core-carrier-nodes bounds)))
    (is (= 256 (:maximum-resolved-core-carrier-depth bounds)))
    (is (= 65536 (:maximum-resolved-core-carrier-width bounds)))
    (is (= 536870912
           (:maximum-resolved-core-scalar-bytes bounds)))
    (is (= 8388608 (:maximum-generated-digest-carrier-nodes bounds)))
    (is (= 256 (:maximum-generated-digest-carrier-depth bounds)))
    (is (= 65536 (:maximum-generated-digest-carrier-width bounds)))
    (is (= 536870912
           (:maximum-generated-digest-scalar-bytes bounds)))))

(deftest sh07-b16-binding-preflight-is-inclusive-and-fails-closed
  (let [at-bound (vec (repeat 2048 nil))
        over-bound (conj at-bound nil)
        accepted (invoke 'sh07-binding-count-preflight [at-bound])
        rejected (invoke 'sh07-binding-count-preflight [over-bound])
        malformed (invoke 'sh07-binding-count-preflight [:not-a-vector])]
    (testing "the aggregate request binding table accepts its exact ceiling"
      (is (= {:status :accepted
              :bound :maximum-bindings
              :maximum 2048
              :observed 2048}
             accepted)))
    (testing "boundary plus one is a structured fail-closed rejection"
      (is (= {:status :rejected
              :reason :maximum-bindings
              :bound :maximum-bindings
              :maximum 2048
              :observed 2049}
             rejected)))
    (testing "malformed ingress is rejected without host traversal failure"
      (is (= {:status :rejected
              :reason :binding-vector-required
              :bound :maximum-bindings
              :maximum 2048
              :observed nil}
             malformed)))))

(deftest sh07-b16-fragment-binding-id-bound-is-inclusive
  (let [at-bound (mapv ordinal-sha (range 2048))
        over-bound (conj at-bound (ordinal-sha 2048))]
    (is (true? (invoke 'sh07-binding-id-vector? [at-bound])))
    (is (false? (invoke 'sh07-binding-id-vector? [over-bound])))
    (is (false? (invoke 'sh07-binding-id-vector? [[:not-a-sha]])))))

(deftest sh07-source-local-bindings-follow-authenticated-definition-syntax
  (let [local-a (ordinal-sha 1)
        local-b (ordinal-sha 2)
        external (ordinal-sha 3)
        syntax-a (ordinal-sha 11)
        syntax-b (ordinal-sha 12)
        external-syntax (ordinal-sha 13)
        request
        {:module {:namespace 'gravity.bootstrap.reader}
         :forms [{:syntax-id syntax-a} {:syntax-id syntax-b}]
         :binding-table
         [{:binding-id local-a
           :namespace 'gravity.bootstrap.reader
           :definition-syntax-id syntax-a}
          {:binding-id local-b
           :namespace 'reader
           :definition-syntax-id syntax-b}
          {:binding-id external
           :namespace 'gravity.core
           :definition-syntax-id external-syntax}]}]
    (testing "qualified definitions remain source-local after name normalization"
      (is (= [local-a local-b]
             (invoke 'sh07-b13-local-binding-ids [request]))))
    (testing "binding-table order remains authoritative"
      (is (= [local-b local-a]
             (invoke
              'sh07-b13-local-binding-ids
              [(update request :binding-table
                       #(vec [(second %) (first %) (nth % 2)]))]))))))

(deftest sh07-template-carrier-limits-are-separate-and-inclusive
  (let [preflight
        #(invoke
          'sh07-carrier-preflight-with-bounds
          [%1 %2 %3 %4 %5])]
    (is (= :accepted (:status (preflight [:a] 2 1 1 8))))
    (is (= :carrier-node-bound
           (:reason (preflight [:a] 1 1 1 8))))
    (is (= :carrier-depth-bound
           (:reason (preflight [:a] 2 0 1 8))))
    (is (= :carrier-width-bound
           (:reason (preflight [:a] 2 1 0 8))))
    (is (= :carrier-scalar-byte-bound
           (:reason (preflight [:a] 2 1 1 7))))))

(deftest sh07-template-and-resolved-output-preflights-are-explicit
  (let [value [:a]
        template (invoke 'sh07-template-carrier-preflight [value])
        resolved (invoke 'sh07-resolved-core-carrier-preflight [value])
        digest
        (invoke 'sh07-generated-digest-carrier-preflight [value])]
    (is (= :accepted (:status template)))
    (is (= :accepted (:status resolved)))
    (is (= :accepted (:status digest)))
    (is (= (select-keys template
                        [:nodes :aggregate-nodes :component-nodes
                         :scalar-bytes :maximum-depth :maximum-width])
           (select-keys resolved
                        [:nodes :aggregate-nodes :component-nodes
                         :scalar-bytes :maximum-depth :maximum-width])
           (select-keys digest
                        [:nodes :aggregate-nodes :component-nodes
                         :scalar-bytes :maximum-depth :maximum-width])))))

(deftest sh07-b16-public-export-shapes-remain-stable
  (is (= :gravity/stage2-compiler-artifact-plan
         (:kind @checked-core-plan)))
  (doseq [[function parameters]
          {'sh07-build-core-template '[request]
           'sh07-verify-core-template
           '[request template digest-requests]
           'sh07-verify-core-resolved
           '[request resolved-core digest-requests resolved-digests]}]
    (let [compiled (get-in @checked-core-plan [:functions function])]
      (is (map? compiled) function)
      (is (= parameters (:params compiled)) function))))
