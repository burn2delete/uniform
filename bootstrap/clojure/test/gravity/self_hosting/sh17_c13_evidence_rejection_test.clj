(ns gravity.self-hosting.sh17-c13-evidence-rejection-test
  "Stage10a's explicit C13-evidence-to-C14 rejection boundary.

  This namespace reuses the genuine Stage9 C11 -> C12 -> C13 evidence chain,
  but it never treats that evidence-only carrier as verified optimized MIR.
  The test is intentionally a non-fixture, non-authoritative Stage2 route: it
  compiles the C14 source, calls the existing bounded C14 ingress/builders,
  and requires their stable fail-closed C14-INPUT result for all three target
  labels."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh16-c12-domain-evidence-boundary-test]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh17_c13_evidence_rejection_test.clj")]
    (when-not resource
      (throw (ex-info "Stage10a test source is not on the classpath"
                      {:id "SH17-C13-REJECTION-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH17-C13-REJECTION-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private c14-relative-path
  "bootstrap/gravity/src/gravity/compiler/c14_target_lowering_architecture.gravity")

(defn- path
  [relative]
  (str (.resolve @root relative)))

(defn- compile-c14-plan
  []
  (let [source-path (path c14-relative-path)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private c14-plan
  (delay (compile-c14-plan)))

(defn- invoke-c14
  [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh17-c13-evidence-rejection
    :compiler-artifact-plan? true}
   @c14-plan function arguments))

(defn- sh16-value
  [name]
  (var-get
   (ns-resolve
    'gravity.self-hosting.sh16-c12-domain-evidence-boundary-test name)))

(defn- actual-stage9-boundary
  []
  (let [{:keys [evidence verification]}
        @(sh16-value 'prepared-c12)
        request ((sh16-value 'c13-request) evidence verification)
        boundary
        ((sh16-value 'invoke-c13)
         'sh16-build-c13-evidence-boundary
         [request])
        boundary-verification
        ((sh16-value 'invoke-c13)
         'sh16-verify-c13-evidence-boundary
         [request boundary])]
    {:boundary boundary
     :request request
     :verification boundary-verification}))

(def ^:private target-builders
  [['c14-build-bounded-llvm-lowering-record :llvm]
   ['c14-build-bounded-c-lowering-record :c]
   ['c14-build-bounded-wasm-lowering-record :wasm]])

(defn- c14-rejection-artifact
  [backend]
  (case backend
    :llvm :gravity/c14-bounded-llvm-lowering-record
    :c :gravity/c14-bounded-c-lowering-record
    :wasm :gravity/c14-bounded-wasm-lowering-record))

(defn- expected-c14-input-rejection
  [artifact]
  {:artifact artifact
   :schema-version 1
   :status :rejected
   :diagnostic "C14-INPUT"
   :missing-fact :verified-optimized-mir
   :diagnostics ["C14-INPUT"]})

(deftest sh17-c14-rejects-genuine-stage9-evidence-boundary
  (let [{:keys [boundary verification]} (actual-stage9-boundary)]
    ;; The upstream carrier is genuine Stage9 evidence, but it is explicitly
    ;; not an optimized MIR artifact and cannot be promoted by relabeling.
    (is (= :passed (:status verification)))
    (is (= boundary (:expected verification)))
    (is (= boundary (:candidate verification)))
    (is (= :gravity/sh16-c13-evidence-boundary (:artifact boundary)))
    (is (= :accepted (:status boundary)))
    (is (= :rejected (:lowering-status boundary)))
    (is (false? (:executable-load? boundary)))
    (is (= :none (:semantic-authority boundary)))
    (is (= :unbound (:identity-binding-status boundary)))
    (is (= :coordinator-digest-required (:identity-resolution boundary)))
    (is (true? (:target-independent? boundary)))
    (is (false? (:self-hosted? boundary)))
    (is (false? (:seed-retired? boundary)))
    (is (= :incomplete-evidence-only
           (get-in boundary [:residual-check-report :status])))
    (is (some #{:exact-c12-evidence-validation}
              (get-in boundary
                      [:residual-check-report :open-proof-obligations])))
    (is (some #{:backend-lowering} (:nonclaims boundary)))
    (is (some #{:executable-load} (:nonclaims boundary)))
    (is (not= :gravity/c13-bounded-identity-optimized-mir
              (:artifact boundary)))
    (is (not (contains? boundary :optimized-mir)))

    (doseq [[builder backend] target-builders]
      (testing (name backend)
        (let [result (invoke-c14 builder [boundary {}])]
          (is (= (expected-c14-input-rejection
                  (c14-rejection-artifact backend))
                 result))
          (is (not (contains? result :target-program)))
          (is (not (contains? result :emission)))
          (is (not (contains? result :semantic-authority)))
          (is (not (contains? result :executable-load?))))))))

(deftest sh17-c14-rejection-survives-stage9-carrier-substitution
  (let [{:keys [boundary verification]} (actual-stage9-boundary)
        relabeled (assoc boundary
                         :artifact :gravity/c13-bounded-identity-optimized-mir)
        injected (assoc boundary
                        :optimized-mir {:status :accepted
                                        :artifact
                                        :gravity/verified-target-independent-mir})
        relabeled-and-injected
        (assoc relabeled :optimized-mir (:optimized-mir injected))]
    ;; Keep this mutation test independently meaningful if test selection
    ;; skips the preceding positive test: the fresh upstream verification must
    ;; pass before any carrier substitutions are attempted.
    (is (= :passed (:status verification)))
    (is (= (:expected verification) (:candidate verification)))
    (is (= boundary (:expected verification)))
    (doseq [[builder backend] target-builders]
      (testing (str (name backend) " relabeled")
        (is (= (expected-c14-input-rejection
                (c14-rejection-artifact backend))
               (invoke-c14 builder [relabeled {}]))))
      (testing (str (name backend) " injected")
        (is (= (expected-c14-input-rejection
                (c14-rejection-artifact backend))
               (invoke-c14 builder [injected {}]))))
      (testing (str (name backend) " relabeled-and-injected")
        (is (= (expected-c14-input-rejection
                (c14-rejection-artifact backend))
               (invoke-c14 builder [relabeled-and-injected {}])))))))
