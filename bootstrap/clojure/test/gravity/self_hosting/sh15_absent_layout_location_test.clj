(ns gravity.self-hosting.sh15-absent-layout-location-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private root
  (delay
    (loop [path (.getParent
                 (.toPath (io/file (.toURI
                                   (io/resource "gravity/self_hosting/sh15_absent_layout_location_test.clj")))))]
      (cond
        (nil? path) (throw (ex-info "repository root missing" {}))
        (.isFile (.toFile (.resolve path "deps.edn"))) path
        :else (recur (.getParent path))))))

(def ^:private c15-path
  "bootstrap/gravity/src/gravity/compiler/c15_compiler_diagnostics.gravity")
(def ^:private c12-path
  "bootstrap/gravity/src/gravity/compiler/c12_domain_ir_architecture.gravity")
(defn- fixture-path [family extension]
  (str "bootstrap/clojure/fixtures/self-hosting/sh-15/" family
       "/coordinator-absent-layout-location." extension))

(defn- compile-plan [relative]
  (let [path (str (.resolve @root relative))
        emitter (:emitter
                 (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))))

(def ^:private c15-plan (delay (compile-plan c15-path)))
(def ^:private c12-plan (delay (compile-plan c12-path)))
(def ^:private accepted-plan (delay (compile-plan (fixture-path "accepted" "gravity"))))
(def ^:private rejected-plan (delay (compile-plan (fixture-path "rejected" "gravity"))))
(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh15-absent-location-development :compiler-artifact-plan? true}
   plan function arguments))
(defn- c15 [function arguments] (invoke @c15-plan function arguments))
(defn- fixture [function] (invoke @accepted-plan function []))
(defn- source-request [] (fixture 'coordinator-absent-layout-location-request))
(defn- original-rejection []
  (invoke @c12-plan 'sh14-build-authenticated-layout [(source-request)]))
(defn- original-row [] (first (:diagnostics (original-rejection))))

(deftest sh15-original-early-layout-rejection-has-explicit-absence
  (let [rejection (original-rejection) row (first (:diagnostics rejection))]
    (is (= :rejected (:status rejection)))
    (is (= "S7-LAYOUT" (:diagnostic-id row) (:rule row)))
    (is (= :request-schema-mismatch (:reason row)))
    (is (contains? row :source-span))
    (is (nil? (:source-span row)))
    (is (= [] (:generated-origin-chain row)))
    (is (= :authenticated-fixed-slots (:value-id row)))
    (is (= :tuple (:kind row)))
    (is (= "C12" (:owner-document row)))
    (is (= :safe (:profile row)))
    (is (= :portable (:target row)))
    (is (= #{:diagnostic-id :rule :family :reason :missing-fact :stage :domain
             :artifact-id :semantic-anchor :owner-document :verifier :value-id
             :kind :profile :target :source-span :generated-origin-chain :remediation}
           (set (keys row))))))

(defn- boundary-request [row]
  ;; Shapes are supplied test inputs, not projections of authenticated builds.
  (let [{domain :domain-reference layout :layout-reference}
        (fixture 'coordinator-absent-layout-location-references)]
    {:artifact :gravity/sh15-diagnostic-boundary-request :schema-version 1
     :domain-reference domain :layout-reference layout
     :domain-verification-reference
     {:artifact :gravity/sh15-external-sh13-verification-reference
      :schema-version 1 :status :external-reference-shape-and-equality-only
      :source-verifier-artifact :gravity/sh13-c11-domain-evidence-verification
      :source-verifier-schema-version 1 :source-verifier-status :passed
      :scope :proof-reference-shape-only
      :checks (c15 'sh15-domain-checks [])
      :nonclaims (c15 'sh15-domain-nonclaims [])
      :expected-reference domain :candidate-reference domain}
     :layout-verification-reference
     {:artifact :gravity/sh15-external-sh14-verification-reference
      :schema-version 1 :status :external-reference-shape-and-equality-only
      :source-verifier-artifact :gravity/sh14-authenticated-layout-verification
      :source-verifier-schema-version 1 :source-verifier-status :passed
      :nonclaims (c15 'sh15-layout-nonclaims [])
      :expected-reference layout :candidate-reference layout}
     :upstream-diagnostics [row] :profile :safe :target :portable
     :provenance {:test-input :unverified-reference-shapes}}))

(deftest sh15-absent-location-paired-fixtures-use-original-produced-rows
  (let [row (original-row)]
    (doseq [family ["accepted" "rejected"]]
      (let [g (fixture-path family "gravity") q (fixture-path family "qst")
            g-plan (if (= "accepted" family) @accepted-plan @rejected-plan)
            q-plan (compile-plan q)
            functions (if (= "accepted" family)
                        '[coordinator-absent-layout-location-request
                          coordinator-absent-layout-location-references]
                        '[coordinator-absent-layout-location-rejected-rows])
            arguments (if (= "accepted" family) [] [row])]
        (is (= (slurp (str (.resolve @root g))) (slurp (str (.resolve @root q)))))
        (doseq [function functions]
          (is (= (invoke g-plan function arguments)
                 (invoke q-plan function arguments))))))))

(deftest sh15-preserves-original-absent-location-through-boundary-and-view
  (let [row (original-row) before (pr-str row) request (boundary-request row)
        candidate (c15 'sh15-build-diagnostic-boundary [request])
        preserved (get-in candidate [:diagnostic-stream :diagnostics 0])
        identity (get-in candidate [:identity-input :diagnostics 0])
        rendered (c15 'sh15-render-bounded-diagnostic-text [preserved])]
    (is (true? (c15 'sh15-diagnostic-input-valid? [request])))
    (is (= :accepted (:status candidate)))
    (is (= "S7-LAYOUT" (:rule preserved) (:upstream-diagnostic-id preserved)))
    (is (= :request-schema-mismatch (get-in preserved [:facts :reason])))
    (is (= :authenticated-layout-contract (get-in preserved [:facts :missing-fact])))
    (is (= (:remediation row) (first (:remediation preserved))))
    (is (contains? (:primary preserved) :span))
    (is (nil? (get-in preserved [:primary :span])))
    (is (nil? (get-in identity [:primary :span])))
    (is (= {:span nil :artifact nil} (:primary identity)))
    (is (= {:span nil :artifact nil} (get (:diagnostic-id identity) 2)))
    (is (= [] (:origin-chain preserved)))
    (is (= :not-evaluated (:redactions preserved)))
    (is (= :accepted (:status rendered)))
    (is (= "code=\"S7-LAYOUT\" severity=:error location=<redacted> redactions=:not-evaluated"
           (:text rendered)))
    (is (= before (pr-str row)))
    (is (= row (original-row)))
    (is (= :none (:semantic-authority candidate)))
    (is (false? (:executable? candidate)))
    (is (false? (:self-hosted? candidate)))
    (is (= :external-reference-shape-and-equality-only (:verification-status candidate)))
    (is (= :external-unverified-preservation-input (:diagnostic-input-status candidate)))
    (is (= :pending (get-in candidate [:diagnostic-stream :rendering-version])))
    (is (= {:error 1 :warning 0 :info 0 :hint 0 :internal-error 0}
           (get-in candidate [:diagnostic-stream :summary-counts])))
    (is (some #{:upstream-semantic-authentication} (:nonclaims candidate)))
    (is (= :passed
           (:status (c15 'sh15-verify-diagnostic-boundary [request candidate]))))
    (let [fresh (compile-plan c15-path)]
      (is (= candidate (invoke fresh 'sh15-build-diagnostic-boundary [request])))
      (is (= :passed
             (:status (invoke fresh 'sh15-verify-diagnostic-boundary [request candidate]))))
      (is (= rendered
             (invoke fresh 'sh15-render-bounded-diagnostic-text [preserved]))))
    (doseq [path [[:diagnostic-stream :diagnostics 0 :primary :span]
                  [:identity-input :diagnostics 0 :primary :span]]]
      (let [forged (assoc-in candidate path {:source-id nil :start nil :end nil})
            verification (c15 'sh15-verify-diagnostic-boundary [request forged])]
        (is (= :rejected (:status verification)))
        (is (= :candidate-substitution
               (get-in verification [:diagnostics 0 :facts :reason])))))))

(deftest sh15-absent-location-keeps-the-other-input-gates-closed
  (let [row (original-row)
        fixture-rows (invoke @rejected-plan
                            'coordinator-absent-layout-location-rejected-rows [row])
        mutations (concat fixture-rows
                          [(dissoc row :source-span) (assoc row :extra :forbidden)
                           (assoc row :generated-origin-chain (vec (repeat 65 {})))])]
    (doseq [mutation mutations]
      (let [request (boundary-request mutation)
            result (c15 'sh15-build-diagnostic-boundary [request])]
        (is (false? (c15 'sh15-diagnostic-input-valid? [request])))
        (is (= :rejected (:status result)))
        (is (= "C15-SCHEMA" (get-in result [:diagnostics 0 :rule])))
        (is (= :upstream-diagnostic-schema
               (get-in result [:diagnostics 0 :facts :reason])))))
    (is (= row (original-row)))))

(deftest sh15-located-diagnostic-control-retains-existing-domain-and-identity
  ;; Supplied row mutation controls the unchanged located domain; not a claim
  ;; that the producer emitted this secondary reason/rule for the early failure.
  (let [span {:source-id :fixture :actual-source-path "/source.gravity"
              :start {:byte 0 :line 1 :column 1} :end {:byte 1 :line 1 :column 2}}
        row (assoc (original-row) :source-span span
                   :reason :candidate-substitution
                   :rule "C12-VERIFY" :diagnostic-id "C12-VERIFY")
        request (boundary-request row)
        candidate (c15 'sh15-build-diagnostic-boundary [request])]
    (is (= :accepted (:status candidate)))
    (is (= span (get-in candidate [:diagnostic-stream :diagnostics 0 :primary :span])))
    (is (= (dissoc span :actual-source-path)
           (get-in candidate [:identity-input :diagnostics 0 :primary :span])))
    (is (= :passed
           (:status (c15 'sh15-verify-diagnostic-boundary [request candidate]))))))
