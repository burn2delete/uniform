(ns gravity.self-hosting.sh16-c12-domain-evidence-boundary-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh13-c11-domain-evidence-adapter-test]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh16_c12_domain_evidence_boundary_test.clj")]
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "repository root not found"
                        {:id "SH16-C12-EVIDENCE-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- compile-plan [relative-path]
  (let [source-path (str (.resolve @root relative-path))
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private c13-plan
  (delay
    (compile-plan
     "bootstrap/gravity/src/gravity/compiler/c13_mir_optimization_passes.gravity")))

(defn- invoke-c13 [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh16-c12-domain-evidence-boundary-test
    :compiler-artifact-plan? true}
   @c13-plan function arguments))

(defn- sh13-value [name]
  (var-get
   (ns-resolve
    'gravity.self-hosting.sh13-c11-domain-evidence-adapter-test name)))

(defn- invoke-c12 [function arguments]
  ((sh13-value 'invoke-c12) function arguments))

(defn- digest [ordinal]
  (format "sha256:%064x" (long ordinal)))

(def ^:private prepared-c12
  (delay
    (let [prepared-c11 @(sh13-value 'prepared-c11)
          candidate (:candidate prepared-c11)
          c11-verification (:result prepared-c11)
          template
          (invoke-c12 'sh13-build-c11-domain-evidence-template
                      [candidate c11-verification])
          identity-request
          (invoke-c12 'sh13-c11-domain-evidence-identity-request
                      [candidate c11-verification template])
          resolution {:request (:request identity-request)
                      :digest (digest 916001)}
          evidence
          (invoke-c12 'sh13-bind-c11-domain-evidence
                      [candidate c11-verification template resolution])
          verification
          (invoke-c12 'sh13-verify-c11-domain-evidence
                      [candidate c11-verification template resolution evidence])]
      {:evidence evidence
       :verification verification})))

(defn- c13-request [evidence verification]
  (let [semantic (:identity-input evidence)]
    {:artifact :gravity/sh16-c13-evidence-boundary-request
     :schema-version 1
     :status :requested
     :c12-evidence evidence
     :c12-verification verification
     :profile (:profile semantic)
     :target (:target semantic)
     :target-independent? true
     :executable-load? false
     :identity-input
     (invoke-c13 'sh16-c13-evidence-identity-input [evidence])
     :provenance (:provenance evidence)
     :scope :c12-evidence-preservation-only
     :lowering-status :rejected
     :fallback {:status :pending
                :kind :c13-bounded-identity-optimized-mir-pending}
     :b1-preflight {:status :blocked
                    :reason :evidence-only-no-executable-load
                    :executable-load? false}
     :pending (invoke-c13 'sh16-c13-evidence-pending [])
     :nonclaims (invoke-c13 'sh16-c13-evidence-nonclaims [])}))

(deftest sh16-c13-evidence-boundary-surface
  (let [functions (:functions @c13-plan)
        representative (vec (repeat 64 (vec (range 64))))
        preflight
        (invoke-c13 'sh16-c13-evidence-preflight [representative])]
    (is (= 1 (get-in functions ['sh16-c13-evidence-input-valid? :arity])))
    (is (= 1 (get-in functions ['sh16-build-c13-evidence-boundary :arity])))
    (is (= 2 (get-in functions ['sh16-verify-c13-evidence-boundary :arity])))
    (is (= :accepted (:status preflight)))
    (is (= 4161 (:nodes preflight)))))

(deftest sh16-c13-evidence-boundary-positive
  (let [{:keys [evidence verification]} @prepared-c12
        request (c13-request evidence verification)
        boundary
        (invoke-c13 'sh16-build-c13-evidence-boundary [request])
        result
        (invoke-c13 'sh16-verify-c13-evidence-boundary [request boundary])
        boundary-preflight
        (invoke-c13 'sh16-c13-evidence-boundary-preflight [boundary])]
    (is (= :accepted (:status evidence)))
    (is (= :passed (:status verification)))
    (is (true?
         (invoke-c13 'sh16-c13-evidence-input-valid? [request])))
    (is (= :accepted (:status boundary)))
    (is (= :accepted (:status boundary-preflight)))
    (is (= :passed (:status result)))
    (is (= evidence (:c12-evidence boundary)))
    (is (= verification (:c12-verification boundary)))
    (is (= (:semantic-anchor evidence) (:semantic-anchor boundary)))
    (is (= (:facts evidence) (:facts boundary)))
    (is (= (:payload evidence) (:payload boundary)))
    (is (= (:source evidence) (:source boundary)))
    (is (= (:provenance evidence) (:provenance boundary)))
    (is (= [:opaque-c12-evidence-value
            :opaque-c12-verification-value
            :physical-provenance-separation]
           (:preserved boundary)))
    (is (false? (get-in boundary [:decision-record :changed?])))
    (is (empty? (get-in boundary
                        [:decision-record :changed-operations])))
    (is (empty? (get-in boundary
                        [:invalidation-ledger :facts-invalidated])))
    (is (empty? (get-in boundary
                        [:residual-check-report :elided-runtime-checks])))
    (is (= :incomplete-evidence-only
           (get-in boundary [:residual-check-report :status])))
    (is (= :not-carried
           (get-in boundary
                   [:residual-check-report :retained-runtime-checks])))
    (is (some #{:exact-c12-evidence-validation}
              (get-in boundary
                      [:residual-check-report :open-proof-obligations])))
    (is (= :rejected (:lowering-status boundary)))
    (is (= :unbound (:identity-binding-status boundary)))
    (is (= :none (:semantic-authority boundary)))
    (is (false? (:executable-load? boundary)))
    (is (not (contains? boundary :artifact-id)))
    (is (not (contains? boundary :optimized-mir)))
    (is (some #{:exact-c12-evidence-validation}
              (:nonclaims boundary)))
    (is (= boundary (:expected result)))
    (is (= boundary (:candidate result)))))

(deftest sh16-c13-evidence-boundary-rejects-substitution-and-hostile-carriers
  (let [{:keys [evidence verification]} @prepared-c12
        request (c13-request evidence verification)
        boundary
        (invoke-c13 'sh16-build-c13-evidence-boundary [request])
        extra-request (assoc request :unexpected true)
        missing-request (dissoc request :identity-input)
        wrong-artifact-evidence
        (assoc evidence :artifact :gravity/substituted)
        wrong-artifact-verification
        (assoc (assoc verification :expected wrong-artifact-evidence)
               :candidate wrong-artifact-evidence)
        wrong-artifact-request
        (c13-request wrong-artifact-evidence wrong-artifact-verification)
        wrong-anchor-evidence
        (assoc-in evidence [:semantic-anchor :artifact]
                  :gravity/substituted)
        wrong-anchor-verification
        (assoc (assoc verification :expected wrong-anchor-evidence)
               :candidate wrong-anchor-evidence)
        wrong-anchor-request
        (c13-request wrong-anchor-evidence wrong-anchor-verification)
        wrong-payload-evidence
        (assoc-in evidence [:payload :identity-id] (digest 916101))
        wrong-payload-verification
        (assoc (assoc verification :expected wrong-payload-evidence)
               :candidate wrong-payload-evidence)
        wrong-payload-request
        (c13-request wrong-payload-evidence wrong-payload-verification)
        verification-substitution
        (assoc verification :candidate (assoc evidence :status :rejected))
        wrong-identity-request
        (assoc request :identity-input {})
        wrong-profile-request (assoc request :profile :substituted)
        wrong-target-request (assoc request :target :substituted)
        executable-request (assoc request :executable-load? true)
        tampered-boundary (assoc boundary :optimization-status :optimized)
        over-boundary-member
        (assoc boundary :source (vec (range 1025)))
        over-width (vec (range 1025))
        over-depth
        (loop [remaining 257 value :leaf]
          (if (zero? remaining)
            value
            (recur (dec remaining) [value])))
        arbitrary-sequence (lazy-seq (cons :leaf nil))]
    (doseq [[label candidate]
            [[:extra extra-request]
             [:missing missing-request]
             [:artifact wrong-artifact-request]
             [:anchor wrong-anchor-request]
             [:payload wrong-payload-request]
             [:verification
              (c13-request evidence verification-substitution)]
             [:identity wrong-identity-request]
             [:profile wrong-profile-request]
             [:target wrong-target-request]
             [:executable executable-request]]]
      (is (false?
           (invoke-c13 'sh16-c13-evidence-input-valid? [candidate]))
          label)
      (is (= :rejected
             (:status
              (invoke-c13 'sh16-build-c13-evidence-boundary
                          [candidate])))
          label))
    (is (= :rejected
           (:status
            (invoke-c13 'sh16-verify-c13-evidence-boundary
                        [request tampered-boundary]))))
    (is (= :source-carrier-bound
           (:reason
            (invoke-c13 'sh16-c13-evidence-boundary-preflight
                        [over-boundary-member]))))
    (is (= :rejected
           (:status
            (invoke-c13 'sh16-verify-c13-evidence-boundary
                        [request over-boundary-member]))))
    (is (= :carrier-vector-width
           (:reason
            (invoke-c13 'sh16-c13-evidence-preflight [over-width]))))
    (is (= :carrier-depth-bound
           (:reason
            (invoke-c13 'sh16-c13-evidence-preflight [over-depth]))))
    (is (= :carrier-sequence
           (:reason
            (invoke-c13 'sh16-c13-evidence-preflight
                        [arbitrary-sequence]))))))

(deftest sh16-c13-evidence-boundary-separates-top-level-provenance
  (let [{:keys [evidence verification]} @prepared-c12
        evidence-b
        (assoc evidence :provenance
                (assoc (:provenance evidence)
                       :actual-source-path
                       "/checkout-b/function.gravity"))
        verification-b
        (assoc (assoc verification :expected evidence-b)
               :candidate evidence-b)
        request-a (c13-request evidence verification)
        request-b (c13-request evidence-b verification-b)
        boundary-a
        (invoke-c13 'sh16-build-c13-evidence-boundary [request-a])
        boundary-b
        (invoke-c13 'sh16-build-c13-evidence-boundary [request-b])]
    (is (= :accepted (:status boundary-a)))
    (is (= :accepted (:status boundary-b)))
    (is (= (:identity-input request-a) (:identity-input request-b)))
    (is (not= (:provenance request-a) (:provenance request-b)))
    (is (not= (:provenance boundary-a) (:provenance boundary-b)))
    (is (some #{:full-path-neutral-identity}
              (:nonclaims boundary-a)))
    (is (not (contains? boundary-a :artifact-id)))
    (is (not (contains? boundary-b :artifact-id)))))
