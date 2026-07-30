(ns gravity.self-hosting.sh25-component-build-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh26-stage-rebuild-test])
  (:import [java.security MessageDigest]))

(defn- repository-root []
  (let [resource
        (io/resource "gravity/self_hosting/sh25_component_build_test.clj")]
    (loop [candidate
           (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root not found"
                        {:id "SH25-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(defn- path [relative] (str (.resolve @root relative)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-25")

(defn- compile-plan [relative]
  (let [source-path (path relative)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private engine-plan
  (delay (compile-plan (str fixture-root "/component_build_engine.gravity"))))
(def ^:private accepted-gravity-plan
  (delay (compile-plan (str fixture-root
                            "/accepted/component-builds.gravity"))))
(def ^:private accepted-qst-plan
  (delay (compile-plan (str fixture-root
                            "/accepted/component-builds.qst"))))
(def ^:private rejected-gravity-plan
  (delay (compile-plan (str fixture-root
                            "/rejected/invalid-component-builds.gravity"))))
(def ^:private rejected-qst-plan
  (delay (compile-plan (str fixture-root
                            "/rejected/invalid-component-builds.qst"))))
(def ^:private sh26-engine-plan
  (delay
    (compile-plan
     "bootstrap/clojure/fixtures/self-hosting/sh-26/stage_rebuild_engine.gravity")))
(def ^:private sh26-accepted-plan
  (delay
    (compile-plan
     "bootstrap/clojure/fixtures/self-hosting/sh-26/accepted/stage-rebuild.gravity")))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh25-component-build-leaf
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- request [plan function]
  (invoke plan function []))

(defn- sh26-test-value [symbol]
  (let [resolved
        (ns-resolve
         'gravity.self-hosting.sh26-stage-rebuild-test
         symbol)]
    (when-not resolved
      (throw
       (ex-info
        "Required SH-26 authenticated test harness value is absent"
        {:id "SH25-SH26-HARNESS-ABSENT"
         :symbol symbol})))
    (var-get resolved)))

(defn- build [request]
  (invoke engine-plan 'sh25-build-authoritative-components [request]))

(defn- nested-vector [depth value]
  (loop [remaining depth result value]
    (if (zero? remaining)
      result
      (recur (dec remaining) [result]))))

(defn- bounded-result [thunk]
  (deref
   (future
     (try
       {:value (thunk)}
       (catch Throwable throwable
         {:throwable throwable})))
   10000
   ::timeout))

(defn- sha256 [bytes]
  (str
   "sha256:"
   (apply str
          (map #(format "%02x" (bit-and 0xff %))
               (.digest (doto (MessageDigest/getInstance "SHA-256")
                          (.update bytes)))))))

(deftest sh25-engine-fixtures-and-policy-compile
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (let [policy
        (invoke engine-plan 'sh25-component-build-policy [])]
    (is (= 41 (:authoritative-owned-module-count policy)))
    (is (= 42 (:required-component-count policy)))
    (is (= :meta (:profile policy)))
    (is (= #{} (:effects policy)))
    (is (= #{} (:capabilities policy)))
    (is (= :provisional-leaf-source
           (get-in policy [:runtime-boundary :status])))
    (is (false? (:self-hosted? policy)))
    (is (:clojure-seed-boundary? policy))))

(deftest sh25-catalog-covers-the-current-authoritative-inventory
  (let [catalog
        (invoke engine-plan 'sh25-authoritative-component-catalog [])
        ownership
        (edn/read-string
         (slurp (path "docs/self-hosting-slice-ownership.edn")))
        owned-paths (set (keys (:module-owners ownership)))
        runtime-path
        "bootstrap/clojure/fixtures/self-hosting/sh-19/minimal_runtime_engine.gravity"
        catalog-paths (set (map :source-path catalog))
        components
        (:components
         (request accepted-gravity-plan 'sh25-component-build-request))]
    (is (= 42 (count catalog)))
    (is (= owned-paths (disj catalog-paths runtime-path)))
    (is (contains? catalog-paths runtime-path))
    (is (= 42 (count (set (map :component-id catalog)))))
    (is (= #{:reader :syntax :macro :analyzer :checked-core :mir
             :optimizer :lowering :backend :diagnostic :runtime
             :standard-library :package-build :compiler}
           (set (map :category catalog))))
    (is (every?
         #(.isFile (.toFile (.resolve @root (:source-path %))))
         catalog))
    (doseq [component components]
      (let [bytes
            (java.nio.file.Files/readAllBytes
             (.resolve @root (:actual-source-path component)))]
        (is (= (alength bytes)
               (get-in component [:source-identity :byte-count])))
        (is (= (sha256 bytes)
               (get-in component
                       [:source-identity :content-hash])))))))

(deftest sh25-builds-the-complete-ordered-component-set
  (let [gravity-request
        (request accepted-gravity-plan 'sh25-component-build-request)
        qst-request
        (request accepted-qst-plan 'sh25-component-build-request)
        gravity (build gravity-request)
        qst (build qst-request)
        verification
        (invoke engine-plan 'sh25-verify-component-build
                [gravity-request gravity])
        projection
        (invoke engine-plan 'sh25-project-verified-sh26-components
                [gravity-request gravity verification])]
    (is (= gravity-request qst-request))
    (is (= gravity qst))
    (is (= :accepted (:status gravity)))
    (is (= 42 (:component-count gravity)))
    (is (= 42 (count (:actions gravity))))
    (is (= 42 (count (:sh26-component-templates gravity))))
    (is (= (mapv :component-id (:components gravity-request))
           (mapv :component-id (:actions gravity))))
    (is (every? #(= :gravity-sh24-driver (:executor %))
                (:actions gravity)))
    (is (every? #(= :bound-supplied-product (:status %))
                (:actions gravity)))
    (is (every? #(= :pending (:sh25-verification-status %))
                (:sh26-component-templates gravity)))
    (is (= :passed (:status verification)))
    (is (= :accepted (:status projection)))
    (is (= 42 (count (:components projection))))
    (is (every? #(= :passed (:sh25-verification-status %))
                (:components projection)))))

(deftest sh25-identities-are-path-neutral-with-separate-provenance
  (let [left-request
        (request accepted-gravity-plan 'sh25-component-build-request)
        right-request
        (request accepted-gravity-plan
                 'sh25-component-build-alternate-path-request)
        provenance-request
        (-> right-request
            (assoc-in
             [:components 0 :authenticated-input :provenance-root]
             "sha256:alternate-provenance-root")
            (assoc-in
             [:components 0 :build-product :records :provenance
              :envelope-provenance-root]
             "sha256:alternate-provenance-root"))
        left (build left-request)
        right (build right-request)
        provenance-result (build provenance-request)]
    (is (= :accepted (:status left)))
    (is (= :accepted (:status right)))
    (is (= :accepted (:status provenance-result)))
    (is (= (:identity-input left) (:identity-input right)))
    (is (= (:identity-input left)
           (:identity-input provenance-result)))
    (is (not= (:provenance left) (:provenance right)))
    (is (not= (:provenance left)
              (:provenance provenance-result)))
    (is (= "/checkout-a/gravity"
           (get-in left [:provenance :physical :actual-build-root])))
    (is (= "/checkout-b/gravity"
           (get-in right [:provenance :physical :actual-build-root])))))

(deftest sh25-rejects-incomplete-or-illegal-component-inputs
  (let [base
        (request accepted-gravity-plan 'sh25-component-build-request)]
    (doseq [[function rule reason]
            [['sh25-incomplete-catalog-request
              "BOOT3001" :incomplete-catalog]
             ['sh25-forward-dependency-request
              "BOOT3001" :dependency-order]
             ['sh25-invalid-source-identity-request
              "BOOT1003" :source-identity]
             ['sh25-invalid-envelope-request
              "BOOT3004" :authenticated-input]
             ['sh25-incomplete-output-request
              "BOOT7005" :complete-output]
             ['sh25-illegal-meta-request
              "BOOT3002" :meta-legality]
             ['sh25-invalid-prerequisite-request
              "BOOT5003" :prerequisite]
             ['sh25-ambient-effect-request
              "BOOT3002" :ambient-authority]
             ['sh25-coherent-substitution-request
              "BOOT1003" :source-identity]]]
      (testing (str function)
        (let [gravity-request
              (invoke rejected-gravity-plan function [base])
              qst-request
              (invoke rejected-qst-plan function [base])
              gravity (build gravity-request)
              qst (build qst-request)]
          (is (= gravity-request qst-request))
          (is (= gravity qst))
          (is (= :rejected (:status gravity)))
          (is (= rule
                 (get-in gravity [:diagnostics 0 :rule])))
          (is (= reason
                 (get-in gravity [:diagnostics 0 :facts :reason]))))))
    (let [over-bound
          (assoc-in base [:components 1 :dependencies]
                    (vec (repeat 17 :authenticated-envelope)))]
      (is (= :rejected (:status (build over-bound)))))))

(deftest sh25-result-alteration-fails-exact-verification
  (let [request
        (request accepted-gravity-plan 'sh25-component-build-request)
        result (build request)
        alterations
        [(assoc-in result [:actions 0 :executor] :clojure)
         (assoc-in result
                   [:sh26-component-templates 0 :output-id] "changed")
         (assoc-in result [:identity-input :lock :content-id] "changed")
         (assoc-in result
                   [:provenance :physical :actual-build-root] "/changed")
         (assoc result :component-count 41)]]
    (doseq [candidate alterations]
      (is (= :rejected
             (:status
              (invoke engine-plan 'sh25-verify-component-build
                      [request candidate])))))))

(deftest sh25-total-carrier-preflight-and-closed-schemas-fail-closed
  (let [request
        (request accepted-gravity-plan 'sh25-component-build-request)
        result (build request)
        deep-request (nested-vector 140 request)
        deep-candidate (nested-vector 140 result)
        request-alterations
        [(assoc request :unexpected true)
         (assoc-in request [:components 0 :unexpected] true)
         (assoc-in request
                   [:components 0 :authenticated-input :unexpected] true)
         (assoc-in request
                   [:components 0 :meta-legality :unexpected] true)
         (assoc-in request
                   [:components 0 :build-product :unexpected] true)
         (assoc-in request [:sh24-driver :unexpected] true)
         (assoc-in request
                   [:sh24-driver :source-revision :content-hash]
                   "sha256:paired-prerequisite-change")
         (assoc-in request [:lock :unexpected] true)]]
    (is (= :rejected (:status (build deep-request))))
    (is (= :carrier-depth-bound
           (get-in (build deep-request)
                   [:diagnostics 0 :facts :reason])))
    (is (= :rejected
           (:status
            (invoke engine-plan 'sh25-verify-component-build
                    [request deep-candidate]))))
    (is (= :carrier-depth-bound
           (get-in
            (invoke engine-plan 'sh25-verify-component-build
                    [request deep-candidate])
            [:diagnostics 0 :facts :reason])))
    (doseq [altered request-alterations]
      (is (= :rejected (:status (build altered)))))
    (is (= :rejected
           (:status
            (invoke
             engine-plan 'sh25-verify-component-build
             [request (assoc result :unexpected true)]))))))

(deftest sh25-carrier-preflight-does-not-realize-unbounded-sequences
  (let [request
        (request accepted-gravity-plan 'sh25-component-build-request)
        infinite-request
        (assoc request :carrier-probe (iterate inc 0))
        throwing-request
        (assoc
         request
         :carrier-probe
         (lazy-seq
          (throw
           (ex-info "SH-25 must not realize this sequence"
                    {:id "SH25-LAZY-REALIZATION"}))))
        oversized-request
        (assoc request :carrier-probe
               (apply str (repeat 262145 "x")))]
    (doseq [[label carrier expected-reason]
            [[:infinite infinite-request
              :carrier-sequence-unsupported]
             [:throwing throwing-request
              :carrier-sequence-unsupported]
             [:oversized-scalar oversized-request
              :carrier-scalar-bound]]]
      (testing (str label " build boundary")
        (let [outcome (bounded-result #(build carrier))]
          (is (not= ::timeout outcome))
          (is (nil? (:throwable outcome)))
          (is (= :rejected (get-in outcome [:value :status])))
          (is (= expected-reason
                 (get-in outcome
                         [:value :diagnostics 0 :facts :reason])))))
      (testing (str label " verify boundary")
        (let [outcome
              (bounded-result
               #(invoke engine-plan 'sh25-verify-component-build
                        [request carrier]))]
          (is (not= ::timeout outcome))
          (is (nil? (:throwable outcome)))
          (is (= :rejected (get-in outcome [:value :status])))
          (is (= expected-reason
                 (get-in outcome
                         [:value :diagnostics 0 :facts :reason]))))))))

(deftest sh25-verification-gates-the-direct-sh26-consumer
  (let [base-request
        (request accepted-gravity-plan 'sh25-component-build-request)
        result (build base-request)
        verification
        (invoke
         engine-plan 'sh25-verify-component-build [base-request result])
        projection
        (invoke engine-plan 'sh25-project-verified-sh26-components
                [base-request result verification])
        ingress
        @(sh26-test-value 'authentic-ingress)
        trusted-context
        @(sh26-test-value 'trusted-context)
        sh26-request
        (invoke sh26-accepted-plan
                'sh26-stage-rebuild-request [ingress])
        sh26-result
        (invoke sh26-engine-plan 'sh26-build-next-stage
                [sh26-request trusted-context])]
    (is (= :passed (:status verification)))
    (is (= :accepted (:status projection)))
    (is (= base-request (:request ingress)))
    (is (= result (:complete-result ingress)))
    (is (= verification (:verification ingress)))
    (is (= projection (:projection ingress)))
    (is (= :accepted (:status sh26-result)))
    (is (= 42 (count (:actions sh26-result))))
    (is (= :passed
           (:status
            (invoke sh26-engine-plan 'sh26-verify-stage-rebuild
                    [sh26-request trusted-context sh26-result]))))
    (is (= :rejected
           (:status
            (invoke
             engine-plan 'sh25-project-verified-sh26-components
             [base-request result
              (assoc verification :component-count 41)]))))))

(deftest sh25-sh26-projection-has-the-exact-consumer-shape
  (let [result
        (build
         (request accepted-gravity-plan 'sh25-component-build-request))
        verification
        (invoke
         engine-plan 'sh25-verify-component-build
         [(request accepted-gravity-plan 'sh25-component-build-request)
          result])
        projection
        (invoke
         engine-plan 'sh25-project-verified-sh26-components
         [(request accepted-gravity-plan 'sh25-component-build-request)
          result verification])
        expected-keys
        #{:component-id :source-id :output-id :topological-ordinal
          :dependencies :profile :implementation-language
          :conformance-status :sh25-verification-status
          :actual-source-path}]
    (is (= :accepted (:status projection)))
    (is (every? #(= expected-keys (set (keys %)))
                (:components projection)))
    (is (= (range 42)
           (map :topological-ordinal (:components projection))))))

(deftest sh25-fixture-pairs-are-byte-identical
  (is (= (slurp (path (str fixture-root
                            "/accepted/component-builds.gravity")))
         (slurp (path (str fixture-root
                            "/accepted/component-builds.qst")))))
  (is (= (slurp (path (str fixture-root
                            "/rejected/invalid-component-builds.gravity")))
         (slurp (path (str fixture-root
                            "/rejected/invalid-component-builds.qst"))))))
