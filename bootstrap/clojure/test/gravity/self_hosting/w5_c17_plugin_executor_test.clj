(ns gravity.self-hosting.w5-c17-plugin-executor-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/w5_c17_plugin_executor_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "W5 C17 executor test is not on the classpath"
        {:id "W5-C17-TEST-SOURCE"})))
    (loop [candidate
           (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "W5-C17-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path [relative]
  (str (.resolve @root relative)))

(def ^:private engine-source
  "bootstrap/gravity/src/gravity/compiler/w5_c17_plugin_executor.gravity")
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/w5-c17")

(defn- fixture [family basename extension]
  (str fixture-root "/" family "/" basename extension))

(defn- compile-plan [relative]
  (let [source-path (path relative)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private engine-plan (delay (compile-plan engine-source)))
(def ^:private accepted-gravity-plan
  (delay (compile-plan (fixture "accepted" "plugin-execution" ".gravity"))))
(def ^:private accepted-qst-plan
  (delay (compile-plan (fixture "accepted" "plugin-execution" ".qst"))))
(def ^:private rejected-gravity-plan
  (delay (compile-plan
          (fixture "rejected" "invalid-plugin-execution" ".gravity"))))
(def ^:private rejected-qst-plan
  (delay (compile-plan
          (fixture "rejected" "invalid-plugin-execution" ".qst"))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-w5-c17-plugin-executor
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- accepted-request [plan function actual-source-path]
  (invoke plan function [actual-source-path]))

(defn- execute [request]
  (invoke engine-plan 'w5-c17-execute [request]))

(defn- verify [request result]
  (invoke engine-plan 'w5-c17-verify [request result]))

(def ^:private rejected-cases
  {'w5-c17-invalid-manifest-request "C17-MANIFEST"
   'w5-c17-incompatible-api-request "C17-API"
   'w5-c17-unversioned-api-request "C17-API"
   'w5-c17-missing-capability-request "C17-CAPABILITY"
   'w5-c17-excess-capability-request "C17-CAPABILITY"
   'w5-c17-hidden-authority-request "C17-SANDBOX"
   'w5-c17-build-effect-request "C17-BUILD-EFFECT"
   'w5-c17-sandbox-request "C17-SANDBOX"
   'w5-c17-invalid-pass-contract-request "C17-PASS-CONTRACT"
   'w5-c17-invalid-preservation-request "C17-PASS-CONTRACT"
   'w5-c17-opaque-domain-request "C17-DOMAIN"
   'w5-c17-opaque-facet-request "C17-FACET"
   'w5-c17-unverifiable-output-request "C17-OUTPUT"
   'w5-c17-output-substitution-request "C17-OUTPUT"
   'w5-c17-trust-policy-request "C17-TRUST"
   'w5-c17-invalid-signature-request "C17-TRUST"
   'w5-c17-revoked-signature-request "C17-TRUST"
   'w5-c17-invalid-provenance-request "C17-MANIFEST"
   'w5-c17-invalid-source-span-request "C17-MANIFEST"
   'w5-c17-invalid-pass-identity-request "C17-PASS-CONTRACT"
   'w5-c17-invalid-pass-registration-request "C17-PASS-CONTRACT"
   'w5-c17-coherent-registration-plugin-substitution-request "C17-DOMAIN"})

(deftest w5-c17-engine-and-fixtures-compile-as-gravity
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[w5-c17-policy
            w5-c17-plugin-manifest
            w5-c17-pass-contract
            w5-c17-sandbox-grant
            w5-c17-domain-registration
            w5-c17-facet-registration
            w5-c17-cache-key-inputs
            w5-c17-identity-input
            w5-c17-diagnostic
            w5-c17-execute
            w5-c17-verify
            w5-c17-recompute]]
    (is (map? (get-in @engine-plan [:functions function])) function))
  (doseq [function
          '[w5-c17-sandboxed-request
            w5-c17-trusted-request
            w5-c17-sandboxed-alternate-path-request]]
    (is (map? (get-in @accepted-gravity-plan [:functions function])) function))
  (doseq [[family basename]
          [["accepted" "plugin-execution"]
           ["rejected" "invalid-plugin-execution"]]]
    (is (= (slurp (path (fixture family basename ".gravity")))
           (slurp (path (fixture family basename ".qst")))))))

(deftest w5-c17-policy-is-stage-owned-and-nonauthoritative
  (let [policy (invoke engine-plan 'w5-c17-policy [])]
    (is (= :gravity/w5-c17-plugin-execution-policy (:artifact policy)))
    (is (= 1 (:schema-version policy)))
    (is (= :meta (:plugin-profile policy)))
    (is (= :llvm-x86_64-linux (:target policy)))
    (is (= :jvm (:stage2-target policy)))
    (is (= {:os :linux :arch :x86_64 :backend :llvm
            :artifact-format :elf :abi :sysv-amd64}
           (:candidate-platform policy)))
    (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
           (:unsupported-targets policy)))
    (is (= #{:read-mir :write-mir :register-pass
             :emit-diagnostics :emit-artifacts}
           (:required-scopes policy)))
    (is (some #{"C17-MANIFEST"} (:diagnostics policy)))
    (is (some #{"C17-TRUST"} (:diagnostics policy)))
    (is (true? (get-in policy [:flags :clojure-seed-boundary?])))
    (is (false? (get-in policy [:flags :self-hosted?])))
    (is (false? (get-in policy [:flags :release?])))
    (is (true? (get-in policy [:flags :non-authoritative?])))))

(deftest w5-c17-accepted-sandboxed-and-trusted-execution
  (let [sandboxed
        (accepted-request accepted-gravity-plan
                          'w5-c17-sandboxed-request
                          "/checkout-a/self-hosting/w5-c17/accepted/plugin-execution.gravity")
        trusted
        (accepted-request accepted-gravity-plan
                          'w5-c17-trusted-request
                          "/checkout-a/self-hosting/w5-c17/accepted/plugin-execution.gravity")
        sandboxed-result (execute sandboxed)
        trusted-result (execute trusted)]
    (is (= :accepted (:status sandboxed-result)))
    (is (= :accepted (:status trusted-result)))
    (is (= :sandboxed
           (get-in sandboxed-result [:sandbox-grant :trust])))
    (is (= :trusted-package
           (get-in trusted-result [:sandbox-grant :trust])))
    (is (= :passed
           (get-in sandboxed-result [:execution-trace :verifier-result])))
    (is (= :passed
           (get-in trusted-result [:verifier-report :verifier-result])))
    (is (= [] (:diagnostics sandboxed-result)))
    (is (= (get-in sandboxed-result [:execution-trace :cache-key])
           (:cache-key sandboxed-result)))
    (is (= #{:read-mir :write-mir :register-pass
             :emit-diagnostics :emit-artifacts}
           (get-in sandboxed-result
                  [:execution-trace :grants :scopes])))
    (is (= :passed (:status (verify sandboxed sandboxed-result))))
    (is (= :passed (:status (verify trusted trusted-result))))
    (is (true? (:clojure-seed-boundary? sandboxed-result)))
    (is (false? (:self-hosted? sandboxed-result)))
    (is (false? (:release? sandboxed-result)))
    (is (true? (:non-authoritative? sandboxed-result)))
    (is (= :incomplete (:completion sandboxed-result)))))

(deftest w5-c17-qst-request-retains-qst-provenance
  (let [request (accepted-request
                 accepted-qst-plan
                 'w5-c17-sandboxed-request
                 "/checkout-a/self-hosting/w5-c17/accepted/plugin-execution.qst")
        result (execute request)]
    (is (= :accepted (:status result)))
    (is (str/ends-with?
         (get-in result [:provenance :actual-source-path]) ".qst"))
    (is (= (:identity-input result)
           (:identity-input (execute
                             (accepted-request
                              accepted-gravity-plan
                              'w5-c17-sandboxed-request
                              "/checkout-a/self-hosting/w5-c17/accepted/plugin-execution.gravity")))))))

(deftest w5-c17-identity-is-path-neutral-and-provenance-retains-paths
  (let [left-request
        (accepted-request accepted-gravity-plan
                          'w5-c17-sandboxed-request
                          "/checkout-a/self-hosting/w5-c17/accepted/plugin-execution.gravity")
        right-request
        (accepted-request accepted-gravity-plan
                          'w5-c17-sandboxed-alternate-path-request
                          "/checkout-b/self-hosting/w5-c17/accepted/plugin-execution.gravity")
        left (execute left-request)
        right (execute right-request)]
    (is (= (:identity-input left) (:identity-input right)))
    (is (= (:cache-key left) (:cache-key right)))
    (is (not= (:provenance left) (:provenance right)))
    (is (not (str/includes? (pr-str (:identity-input left)) "/checkout-a/")))
    (is (not (str/includes? (pr-str (:identity-input right)) "/checkout-b/")))
    (is (str/includes? (pr-str (:provenance left)) "/checkout-a/"))
    (is (str/includes? (pr-str (:provenance right)) "/checkout-b/"))))

(deftest w5-c17-rejected-families-are-stable-and-structured
  (doseq [[function expected-rule] rejected-cases]
    (testing (str function)
      (let [gravity-request
            (accepted-request rejected-gravity-plan function
                              "/checkout-a/self-hosting/w5-c17/rejected/invalid-plugin-execution.gravity")
            qst-request
            (accepted-request rejected-qst-plan function
                              "/checkout-a/self-hosting/w5-c17/rejected/invalid-plugin-execution.qst")
            gravity-result (execute gravity-request)
            qst-result (execute qst-request)
            diagnostic (first (:diagnostics gravity-result))]
        (is (not= gravity-request qst-request))
        (is (not= (get-in gravity-request [:provenance :actual-source-path])
                  (get-in qst-request [:provenance :actual-source-path])))
        (is (= (get-in gravity-result [:diagnostics 0 :diagnostic-id])
               (get-in qst-result [:diagnostics 0 :diagnostic-id])))
        (is (= :rejected (:status gravity-result)))
        (is (= expected-rule (:diagnostic-id diagnostic)))
        (is (= expected-rule (:rule diagnostic)))
        (is (= :error (:severity diagnostic)))
        (is (= :w5/plugin-execution (:pass-id diagnostic)))
        (is (= :meta (:profile diagnostic)))
        (is (= :llvm-x86_64-linux (:target diagnostic)))
        (is (keyword? (:remediation diagnostic)))
        (is (= true (get-in diagnostic [:facts :source-span-preserved])))
        (is (= true
               (get-in diagnostic [:facts :actual-path-in-provenance-only])))))))

(deftest w5-c17-verifier-recomputes-and-rejects-substitution
  (let [request (accepted-request accepted-gravity-plan
                                  'w5-c17-sandboxed-request
                                  "/checkout-a/self-hosting/w5-c17/accepted/plugin-execution.gravity")
        result (execute request)
        substituted
        (assoc-in result [:output-artifact :semantic-value] :substituted)
        cache-substituted
        (assoc-in result [:cache-key :manifest-hash]
                  "sha256:9999999999999999999999999999999999999999999999999999999999999999")]
    (is (= :accepted (:status result)))
    (is (= :rejected (:status (verify request substituted))))
    (is (= "C17-OUTPUT"
           (get-in (verify request substituted) [:diagnostics 0 :rule])))
    (is (= :execution-result-substitution
           (get-in (verify request substituted) [:diagnostics 0 :reason])))
    (is (= :w5/plugin-execution
           (get-in (verify request substituted) [:diagnostics 0 :pass-id])))
    (is (= :rejected (:status (verify request cache-substituted))))
    (is (= "C17-OUTPUT"
           (get-in (verify request cache-substituted)
                   [:diagnostics 0 :diagnostic-id])))
    (is (= result (invoke engine-plan 'w5-c17-recompute [request])))))

(deftest w5-c17-registration-plugin-identity-is-cross-bound
  (doseq [[plan suffix]
          [[rejected-gravity-plan ".gravity"]
           [rejected-qst-plan ".qst"]]]
    (let [request
          (accepted-request
           plan 'w5-c17-coherent-registration-plugin-substitution-request
           (str "/checkout-a/self-hosting/w5-c17/rejected/invalid-plugin-execution"
                suffix))
          result (execute request)
          diagnostic (first (:diagnostics result))]
      (is (= :rejected (:status result)))
      (is (= "C17-DOMAIN" (:rule diagnostic)))
      (is (= :domain-registration-binding-mismatch (:reason diagnostic)))
      (is (= :bind-domain-registration-to-manifest-pass-and-request
             (:remediation diagnostic))))))

(deftest w5-c17-rejected-fixture-requests-are-not-completion-authority
  (let [request (accepted-request rejected-gravity-plan
                                  'w5-c17-invalid-manifest-request
                                  "/checkout-a/self-hosting/w5-c17/rejected/invalid-plugin-execution.gravity")
        result (execute request)]
    (is (= :rejected (:status result)))
    (is (true? (:clojure-seed-boundary? result)))
    (is (false? (:self-hosted? result)))
    (is (false? (:release? result)))
    (is (true? (:non-authoritative? result)))
    (is (= :w5-c17-executable-leaf (:scope result)))))
