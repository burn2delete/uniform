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
    (is (= 65536 (:maximum-combined-lexical-loop-records bounds)))))

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
