(ns gravity.self-hosting.w5-c16-incremental-executor-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/w5_c16_incremental_executor_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "W5 C16 test source is not on the classpath"
        {:id "W5-C16-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "W5-C16-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private engine-path
  "bootstrap/gravity/src/gravity/compiler/w5_c16_incremental_executor.gravity")
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/w5-c16")

(defn- path [relative]
  (str (.resolve @root relative)))

(defn- compile-plan [relative]
  (let [source-path (path relative)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private engine-plan (delay (compile-plan engine-path)))
(def ^:private accepted-gravity-plan
  (delay (compile-plan
          (str fixture-root "/accepted/incremental-execution.gravity"))))
(def ^:private accepted-qst-plan
  (delay (compile-plan
          (str fixture-root "/accepted/incremental-execution.qst"))))
(def ^:private rejected-gravity-plan
  (delay (compile-plan
          (str fixture-root "/rejected/invalid-incremental-execution.gravity"))))
(def ^:private rejected-qst-plan
  (delay (compile-plan
          (str fixture-root "/rejected/invalid-incremental-execution.qst"))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-w5-c16-incremental-executor
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- request [plan function checkout extension kind]
  (invoke plan function [checkout extension kind]))

(defn- run-request [request-value]
  (invoke engine-plan 'w5-c16-incremental-execute [request-value]))

(def ^:private rejected-cases
  {'w5-c16-invalid-key-request
   ["C16-KEY" :malformed-or-incomplete-cache-key]
   'w5-c16-invalid-entry-request
   ["C16-ENTRY" :malformed-cache-entry]
   'w5-c16-stale-entry-request
   ["C16-STALE" :cache-key-mismatch]
   'w5-c16-stale-proof-request
   ["C16-PROOF" :stale-proof-or-certificate]
   'w5-c16-speculative-request
   ["C16-SPECULATIVE" :speculative-publication]
   'w5-c16-missing-replay-request
   ["C16-REPLAY" :missing-build-effect-replay]
   'w5-c16-policy-mismatch-request
   ["C16-POLICY" :incompatible-profile-or-target-policy]
   'w5-c16-stale-diagnostic-request
   ["C16-DIAGNOSTIC" :stale-diagnostic-stream]
   'w5-c16-graph-inconsistency-request
   ["C16-GRAPH" :dependency-graph-inconsistency]
   'w5-c16-invalid-provenance-request
   ["C16-STALE" :provenance-path-mismatch]
   'w5-c16-invalid-replay-input-request
   ["C16-REPLAY" :missing-build-effect-replay]
   'w5-c16-missing-source-span-request
   ["C16-KEY" :malformed-source-record]
   'w5-c16-revoked-trust-request
   ["C16-ENTRY" :malformed-cache-entry]
   'w5-c16-rejected-revalidation-request
   ["C16-ENTRY" :malformed-cache-entry]
   'w5-c16-producer-substitution-request
   ["C16-ENTRY" :malformed-cache-entry]
   'w5-c16-coherent-input-substitution-request
   ["C16-ENTRY" :cache-entry-request-binding-mismatch]
   'w5-c16-coherent-existing-root-substitution-request
   ["C16-GRAPH" :dependency-graph-inconsistency]})

(deftest w5-c16-engine-and-fixtures-compile-through-stage2-plan
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[w5-c16-policy
            w5-c16-cache-key-valid?
            w5-c16-cache-entry-valid?
            w5-c16-dependency-graph-valid?
            w5-c16-incremental-execute
            w5-c16-verify-result]]
    (is (map? (get-in @engine-plan [:functions function])) function))
  (doseq [[family basename]
          [["accepted" "incremental-execution"]
           ["rejected" "invalid-incremental-execution"]]]
    (is (= (slurp (path (str fixture-root "/" family "/" basename ".gravity")))
           (slurp (path (str fixture-root "/" family "/" basename ".qst")))))))

(deftest w5-c16-policy-keeps-the-leaf-nonauthoritative
  (let [policy (invoke engine-plan 'w5-c16-policy [])]
    (is (= :gravity/w5-c16-incremental-execution-policy
           (:artifact policy)))
    (is (= :type-check (:stage policy)))
    (is (= :meta (:profile policy)))
    (is (= :llvm-x86_64-linux (:target policy)))
    (is (= :jvm (:stage2-target policy)))
    (is (= :llvm
           (get-in policy [:candidate-platform :backend])))
    (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
           (:unsupported-targets policy)))
    (is (every? #(and (= :unsupported (:support %))
                      (false? (:invokes-clojure? %))
                      (false? (:links-jvm? %))
                      (false? (:fallback? %)))
                (:unsupported-target-policies policy)))
    (is (= 14 (count (:invalidation-causes policy))))
    (is (= 10 (count (:diagnostics policy))))
    (is (true? (:clojure-seed-boundary? policy)))
    (is (false? (:self-hosted? policy)))
    (is (false? (:release? policy)))
    (is (true? (get-in policy [:non-authority :clojure-seed-boundary?])))
    (is (false? (get-in policy [:non-authority :self-hosted?])))
    (is (false? (get-in policy [:non-authority :release?])))
    (is (false? (get-in policy [:non-authority :public-authority?])))
    (is (= :clojure-bootstrap
           (get-in policy [:residual-boundary :stage2-compiler-plan])))
    (is (= :jvm (get-in policy [:residual-boundary :runtime])))))

(deftest w5-c16-accepted-request-reuses-and-invalidates-real-inputs
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [extension (if (= plan accepted-gravity-plan) ".gravity" ".qst")
          kind (if (= plan accepted-gravity-plan) :gravity :qst)
          request-value
          (request plan 'w5-c16-incremental-execution-request
                   "/checkout-a" extension kind)
          result (run-request request-value)
          operations (:operations result)
          changed (rest operations)]
      (is (= :accepted (:status result)))
      (is (= 15 (count operations)))
      (is (= :unchanged-reuse (:operation-id (first operations))))
      (is (= :reused (:decision (first operations))))
      (is (= :accepted (:proof-reuse (first operations))))
      (is (= :accepted (:diagnostic-reuse (first operations))))
      (is (true? (:publishable? (first operations))))
      (is (= 14 (count (:invalidation-traces result))))
      (is (= 15 (count (:reuse-reports result))))
      (is (empty? (:diagnostics result)))
      (is (true? (:publishable? result)))
      (is (true? (:clojure-seed-boundary? result)))
      (is (false? (:self-hosted? result)))
      (is (false? (:release? result)))
      (is (str/ends-with?
           (get-in result [:provenance :source-path]) extension))
      (is (= :accepted (:revalidation result)))
      (is (= #{:source-change :macro-change :namespace-change :type-change
               :effect-change :profile-change :capability-change
               :safety-change :proof-change :dependency-change
               :target-change :backend-change :plugin-change
               :diagnostic-change}
             (set (map :cause changed))))
      (is (every? #(= :invalidated (:decision %)) changed))
      (is (every? #(= :rejected (:proof-reuse %)) changed))
      (is (every? #(= :rejected (:diagnostic-reuse %)) changed))
      (is (every? true? (map :publishable? changed)))
      (is (every? #(true? (get-in % [:invalidation-trace
                                     :proofs-invalidated]))
                  changed))
      (is (every? #(true? (get-in % [:invalidation-trace
                                     :diagnostics-invalidated]))
                  changed)))))

(deftest w5-c16-cache-artifacts-are-stage-bound-and-path-neutral
  (let [left-request
        (request accepted-gravity-plan
                 'w5-c16-incremental-execution-request
                 "/checkout-a" ".gravity" :gravity)
        right-request
        (request accepted-gravity-plan
                 'w5-c16-incremental-execution-alternate-path-request
                 "/checkout-b" ".gravity" :gravity)
        left (run-request left-request)
        right (run-request right-request)]
    (is (true? (invoke engine-plan 'w5-c16-cache-key-valid?
                        [(get left-request :cache-key)])))
    (is (true? (invoke engine-plan 'w5-c16-cache-entry-valid?
                        [(get left-request :cache-entry)])))
    (is (true? (invoke engine-plan 'w5-c16-dependency-graph-valid?
                        [(get left-request :dependency-graph)])))
    (is (= :source-unit
           (get-in left-request [:dependency-graph :root])))
    (is (false?
         (invoke engine-plan 'w5-c16-dependency-graph-valid?
                 [(assoc-in (get left-request :dependency-graph)
                            [:root] :typed-core)])))
    (is (= (:identity-input left) (:identity-input right)))
    (is (not= (:provenance left) (:provenance right)))
    (is (not= (get-in left [:provenance :source-path])
              (get-in right [:provenance :source-path])))
    (is (not (str/includes? (pr-str (:identity-input left))
                            "/checkout-a/")))
    (is (not (str/includes? (pr-str (:identity-input right))
                            "/checkout-b/")))
    (is (true? (get-in left [:non-authority :clojure-seed-boundary?])))
    (is (false? (get-in left [:non-authority :self-hosted?])))
    (is (false? (get-in left [:non-authority :release?])))))

(deftest w5-c16-rejected-fixture-covers-every-stable-family
  (doseq [[gravity-function [rule reason]] rejected-cases]
    (testing (str gravity-function)
      (let [gravity-request
            (request accepted-gravity-plan
                     'w5-c16-incremental-execution-request
                     "/checkout-a" ".gravity" :gravity)
            qst-request
            (request accepted-qst-plan
                     'w5-c16-incremental-execution-request
                     "/checkout-a" ".qst" :qst)
            gravity-invalid
            (invoke rejected-gravity-plan gravity-function [gravity-request])
            qst-invalid
            (invoke rejected-qst-plan gravity-function [qst-request])
            gravity-result (run-request gravity-invalid)
            qst-result (run-request qst-invalid)
            diagnostic (first (:diagnostics gravity-result))]
        (is (not= gravity-invalid qst-invalid))
        (is (not= (get-in gravity-invalid [:source :actual-source-path])
                  (get-in qst-invalid [:source :actual-source-path])))
        (is (= (get-in gravity-result [:diagnostics 0 :rule])
               (get-in qst-result [:diagnostics 0 :rule])))
        (is (= :rejected (:status gravity-result)))
        (is (false? (:publishable? gravity-result)))
        (is (false? (:publishable? qst-result)))
        (is (= 1 (count (:diagnostics gravity-result))))
        (is (= rule (:rule diagnostic) (:diagnostic-id diagnostic)))
        (is (= reason (get-in diagnostic [:facts :reason])))
        (is (= :type-check (:stage diagnostic)))
        (is (= :error (:severity diagnostic)))
        (is (map? (:source-span diagnostic)))
        (if (= gravity-function 'w5-c16-missing-source-span-request)
          (is (= {:source-id "gravity/w5-c16-diagnostic"
                  :start-byte 0
                  :end-byte 0}
                 (:source-span diagnostic)))
          (is (not= {:source-id "gravity/w5-c16-diagnostic"
                     :start-byte 0
                     :end-byte 0}
                    (:source-span diagnostic))))
        (is (string? (:remediation diagnostic)))
        (is (some? (:manifest-entry diagnostic)))))))

(deftest w5-c16-verifier-recomputes-and-rejects-substitution
  (let [request-value
        (request accepted-gravity-plan
                 'w5-c16-incremental-execution-request
                 "/checkout-a" ".gravity" :gravity)
        result (run-request request-value)
        verification
        (invoke engine-plan 'w5-c16-verify-result
                [request-value result])
        altered-result
        (assoc-in result [:operations 1 :decision] :reused)
        altered-verification
        (invoke engine-plan 'w5-c16-verify-result
                [request-value altered-result])
        altered-request
        (assoc-in request-value [:cache-key :source]
                  "sha256:w5-c16-substituted-source")
        recomputed
        (run-request altered-request)]
    (is (= :passed (:status verification)))
    (is (true? (:recomputed verification)))
    (is (= :rejected (:status altered-verification)))
    (is (= "C16-VERIFY"
           (get-in altered-verification [:diagnostics 0 :rule])))
    (is (= :rejected (:status recomputed)))
    (is (= "C16-STALE" (get-in recomputed [:diagnostics 0 :rule])))))
