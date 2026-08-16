(ns gravity.self-hosting.sh28-trust-provenance-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh28_trust_provenance_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-28 trust-provenance test is not on the classpath"
        {:id "SH28-TEST-SOURCE"})))
    (loop [candidate
           (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH28-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path [relative]
  (str (.resolve @root relative)))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-28")

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
  (delay
   (compile-plan
    (str fixture-root "/trust_provenance_engine.gravity"))))

(def ^:private accepted-gravity-plan
  (delay
   (compile-plan
    (str fixture-root "/accepted/trust-evidence.gravity"))))

(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (str fixture-root "/accepted/trust-evidence.qst"))))

(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (str fixture-root "/rejected/invalid-trust-evidence.gravity"))))

(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (str fixture-root "/rejected/invalid-trust-evidence.qst"))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh28-trust-provenance-leaf
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- request [plan function]
  (invoke plan function []))

(defn- build [value]
  (invoke engine-plan 'sh28-build-trust-evidence [value]))

(defn- verify [request result]
  (invoke engine-plan 'sh28-verify-trust-evidence [request result]))

(defn- accepted-request []
  (request accepted-gravity-plan 'sh28-trust-evidence-request))

(deftest sh28-engine-and-co-canonical-fixtures-compile
  (doseq [plan
          [engine-plan
           accepted-gravity-plan
           accepted-qst-plan
           rejected-gravity-plan
           rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[sh28-trust-provenance-policy
            sh28-build-trust-evidence
            sh28-verify-trust-evidence]]
    (is (map? (get-in @engine-plan [:functions function]))))
  (let [policy
        (invoke engine-plan 'sh28-trust-provenance-policy [])]
    (is (= [:D9 :PKG7 :PKG10 :PKG12 :BOOT6 :BOOT8]
           (:governing-documents policy)))
    (is (some #{:authentic-sh27-equivalence-ingress}
              (:pending policy)))
    (is (some #{:independent-trust-anchor-verification}
              (:pending policy)))
    (is (false? (:release-trust-closed? policy)))
    (is (false? (:clojure-seed-retired? policy)))
    (is (true? (:clojure-seed-boundary? policy)))))

(deftest sh28-accepts-bounded-traversable-descriptor-set
  (let [gravity-request
        (request accepted-gravity-plan 'sh28-trust-evidence-request)
        qst-request
        (request accepted-qst-plan 'sh28-trust-evidence-request)
        gravity (build gravity-request)
        qst (build qst-request)]
    (is (= gravity-request qst-request))
    (is (= gravity qst))
    (is (= :accepted (:status gravity)))
    (is (= :acyclic-and-traversable (:lineage-status gravity)))
    (is (= :descriptor-consistent (:diverse-build-status gravity)))
    (is (= :external-verification-pending
           (:trust-anchor-status gravity)))
    (is (= :clear-at-declared-sources (:revocation-status gravity)))
    (is (= :complete-for-declared-islands
           (:unsafe-audit-status gravity)))
    (is (= :explicit-pending-independent-validation
           (:tcb-delta-status gravity)))
    (is (empty? (:diagnostics gravity)))
    (is (false? (:release-trust-closed? gravity)))
    (is (false? (:clojure-seed-retired? gravity)))
    (is (true? (:clojure-seed-boundary? gravity)))
    (is (= :passed (:status (verify gravity-request gravity))))))

(deftest sh28-semantic-identity-is-recursively-path-neutral
  (let [left-request
        (request accepted-gravity-plan 'sh28-trust-evidence-request)
        right-request
        (request
         accepted-gravity-plan
         'sh28-trust-evidence-alternate-path-request)
        left (build left-request)
        right (build right-request)]
    (is (= :accepted (:status left)))
    (is (= :accepted (:status right)))
    (is (= (:identity-input left) (:identity-input right)))
    (is (not= (:provenance left) (:provenance right)))
    (is (not-any?
         #(re-find #"/checkout|/different|/other|/opt|/secure|/external"
                   (str %))
         (tree-seq coll? seq (:identity-input left))))
    (is (some
         #(= "/checkout-a/release" %)
         (tree-seq coll? seq (:provenance left))))))

(deftest sh28-rejects-lineage-gaps-cycles-and-unreachable-nodes
  (let [base (accepted-request)
        cycle
        (assoc-in
         base
         [:lineage 1 :rebuild-parent-ids]
         ["artifact:release-candidate"])
        wrong-compiler
        (assoc-in
         base
         [:lineage 2 :compiler-artifact-id]
         "artifact:seed-descriptor")
        unreachable
        (assoc-in
         base
         [:lineage 3 :rebuild-parent-ids]
         ["artifact:stage-n-plus-one"])]
    (doseq [candidate [cycle wrong-compiler]]
      (let [result (build candidate)]
        (is (= :rejected (:status result)))
        (is (= "BOOT8002" (get-in result [:diagnostics 0 :rule])))))
    ;; Stage N remains reachable through stage N+1, so deleting only the direct
    ;; edge does not manufacture a false lineage gap.
    (is (= :accepted (:status (build unreachable))))))

(deftest sh28-rejects-evidence-provenance-and-tcb-gaps
  (let [base (accepted-request)
        cases
        [[(assoc-in base [:environments 0 :network-policy] :enabled)
          "BOOT8007"]
         [(assoc-in base [:locks 0 :complete] false)
          "BOOT8007"]
         [(assoc-in base [:dependencies 0 :revocation-status] :revoked)
          "BOOT8007"]
         [(assoc-in base [:toolchains 0 :identity-status] :unknown)
          "BOOT8007"]
         [(assoc-in base [:diverse-builds 1 :toolchain-id]
                    "toolchain:gravity-native-a")
          "BOOT6005"]
         [(assoc-in base [:trust-anchors 1 :operator-id] "operator:a")
          "BOOT6005"]
         [(assoc-in base [:evidence :sbom-id] "")
          "BOOT8004"]
         [(assoc-in base [:evidence :sh27-equivalence-status] :verified)
          "BOOT8004"]
         [(assoc-in base [:revocations 0 :status] :revoked)
          "BOOT8006"]
         [(assoc-in base [:revocations 2 :subject-id] "build:unrelated")
          "BOOT8006"]
         [(assoc-in base [:unsafe-audits 0 :review-status] :pending)
          "BOOT8004"]
         [(assoc-in base [:tcb-delta :seed-retirement-proven] true)
          "SH28-TCB"]]]
    (doseq [[candidate rule] cases]
      (testing rule
        (let [result (build candidate)]
          (is (= :rejected (:status result)))
          (is (= rule (get-in result [:diagnostics 0 :rule])))
          (is (false? (:release-trust-closed? result)))
          (is (true? (:clojure-seed-boundary? result))))))))

(deftest sh28-rejects-paired-product-and-lineage-inconsistencies
  (let [base (accepted-request)
        cases
        [[(assoc-in base
                    [:dependency-graphs 0 :dependency-content-hashes]
                    ["sha256:other"])
          "BOOT8004"]
         [(assoc-in base
                    [:dependency-graphs 0 :dependency-provenance-ids]
                    ["provenance:other"])
          "BOOT8004"]
         [(assoc-in base
                    [:locks 0 :dependency-content-hashes]
                    ["sha256:other"])
          "BOOT8004"]
         [(assoc-in base
                    [:build-recipes 0 :environment-id]
                    "env:clean-b")
          "BOOT8004"]
         [(-> base
              (assoc-in [:build-recipes 0 :compiler-artifact-id]
                        "artifact:stage-n")
              (assoc-in [:diverse-builds 0 :compiler-artifact-id]
                        "artifact:stage-n"))
          "BOOT8004"]
         [(-> base
              (assoc-in [:build-recipes 1 :compiler-artifact-id]
                        "artifact:stage-n")
              (assoc-in [:diverse-builds 1 :compiler-artifact-id]
                        "artifact:stage-n"))
          "BOOT8004"]
         [(assoc-in base
                    [:diverse-builds 0 :content-hash]
                    "sha256:unrelated-artifact")
          "BOOT6005"]
         [(assoc-in base
                    [:diverse-builds 1 :build-id]
                    "build:diverse-a")
          "BOOT6005"]
         [(assoc-in base
                    [:attestations 0 :builder-id]
                    "builder:diverse-b")
          "BOOT8004"]
         [(assoc-in base
                    [:sboms 0 :subject-artifact-id]
                    "artifact:stage-n")
          "BOOT8004"]
         [(assoc-in base
                    [:sboms 0 :subject-content-hash]
                    "sha256:other")
          "BOOT8004"]
         [(assoc-in base
                    [:sboms 0 :source-graph-id]
                    "sha256:source-stage-n")
          "BOOT8004"]
         [(assoc-in base
                    [:sboms 0 :dependency-content-hashes]
                    ["sha256:other"])
          "BOOT8004"]
         [(update-in
           base
           [:sboms 0 :capability-inventory]
           conj
           {:effect :build/read
            :capability :compiler/source-read
                  :provider-id "provider:hermetic-source"})
          "BOOT8004"]
         [(update-in
           base
           [:sboms 0 :capability-inventory]
           conj
           {:effect :build/write
            :capability :compiler/artifact-write
            :provider-id "provider:hermetic-output"})
          "BOOT8004"]
         [(assoc-in base [:sboms 0 :capability-inventory] [])
          "BOOT8004"]
         [(assoc-in
           base
           [:sboms 0 :capability-inventory 0]
           {:effect :build/write
            :capability :compiler/artifact-write
            :provider-id "provider:hermetic-output"})
          "BOOT8004"]
         [(update-in
           base
           [:sboms 0 :generated-sources]
           conj
           (get-in base [:sboms 0 :generated-sources 0]))
          "BOOT8004"]
         [(update-in
           base
           [:sboms 0 :generated-sources]
           conj
           {:generated-source-id "generated:other-table"
            :content-hash "sha256:other-table"
            :source-graph-id "sha256:release-source"
            :generator-id "generator:other-table"})
          "BOOT8004"]
         [(assoc-in base [:sboms 0 :generated-sources] [])
          "BOOT8004"]
         [(assoc-in
           base
           [:sboms 0 :generated-sources 0]
           {:generated-source-id "generated:other-table"
            :content-hash "sha256:other-table"
            :source-graph-id "sha256:release-source"
            :generator-id "generator:other-table"})
          "BOOT8004"]
         [(assoc-in base
                    [:sboms 0 :generated-sources 0 :source-graph-id]
                    "sha256:source-stage-n")
          "BOOT8004"]
         [(update-in
           base
           [:sboms 0 :binary-blobs]
           conj
           (get-in base [:sboms 0 :binary-blobs 0]))
          "BOOT8004"]
         [(update-in
           base
           [:sboms 0 :binary-blobs]
           conj
           {:binary-blob-id "blob:other"
            :content-hash "sha256:other"
            :source-id "artifact:release-candidate"
            :policy-id "policy:release"})
          "BOOT8004"]
         [(assoc-in base [:sboms 0 :binary-blobs] [])
          "BOOT8004"]
         [(assoc-in
           base
           [:sboms 0 :binary-blobs 0]
           {:binary-blob-id "blob:other"
            :content-hash "sha256:other"
            :source-id "artifact:release-candidate"
            :policy-id "policy:release"})
          "BOOT8004"]
         [(assoc-in base
                    [:sboms 0 :binary-blobs 0 :source-id]
                    "artifact:stage-n")
          "BOOT8004"]
         [(assoc-in base
                    [:signatures 0 :subject-artifact-id]
                    "artifact:stage-n")
          "BOOT8004"]
         [(assoc-in base
                    [:signatures 0 :subject-content-hash]
                    "sha256:other")
          "BOOT8004"]
         [(assoc-in base
                    [:signatures 0 :artifact-manifest-id]
                    "manifest:other")
          "BOOT8004"]
         [(assoc-in base
                    [:signatures 0 :sbom-id]
                    "sbom:other")
          "BOOT8004"]
         [(assoc-in base
                    [:signatures 0 :safety-summary-id]
                    "safety:other")
          "BOOT8004"]
         [(assoc-in base
                    [:signatures 0 :build-recipe-id]
                    "recipe:diverse-b")
          "BOOT8004"]
         [(assoc-in base
                    [:signatures 0 :signed-payload-id]
                    "payload:other")
          "BOOT8004"]
         [(assoc-in base
                    [:signatures 0 :root-metadata-id]
                    "sha256:root-b")
          "BOOT8004"]
         [(-> base
              (assoc-in [:signatures 0 :root-metadata-id]
                        "sha256:root-b")
              (assoc-in
               [:signing-policies 0 :allowed-root-metadata-ids]
               ["sha256:root-b"]))
          "BOOT8004"]
         [(assoc-in base
                    [:signing-policies 0 :allowed-key-ids]
                    ["key:unrelated"])
          "BOOT8004"]
         [(assoc-in base
                    [:signing-policies 0 :allowed-root-metadata-ids]
                    ["sha256:root-b"])
          "BOOT8004"]
         [(assoc-in base
                    [:unsafe-audit-index :audit-ids]
                    ["audit:unrelated"])
          "BOOT8004"]
         [(assoc-in base
                    [:unsafe-audit-index :evidence-ids]
                    ["test:safe-wrapper" "test:safe-wrapper"])
          "BOOT8004"]
         [(assoc-in base
                    [:unsafe-audit-index :verification-status]
                    :verified)
          "BOOT8004"]
         [(assoc-in base
                    [:unsafe-audit-index :canonical]
                    false)
          "BOOT8004"]
         [(assoc-in base
                    [:bootstrap-inputs 0 :content-hash]
                    "sha256:unrelated-seed")
          "BOOT8004"]
         [(assoc-in base
                    [:bootstrap-inputs 0 :anchor-descriptor-id]
                    "anchor:absent")
          "BOOT8004"]
         [(assoc-in base
                    [:revocations 10 :subject-id]
                    "env:unrelated")
          "BOOT8006"]
         [(assoc-in base
                    [:revocations 11 :subject-id]
                    "signature:unrelated")
          "BOOT8006"]
         [(-> base
              (assoc-in [:runtime-artifacts 0 :artifact-id]
                        "artifact:other-runtime")
              (update
               :revocations
               (fn [records]
                 (mapv
                  (fn [record]
                    (if (= :runtime-artifact
                           (:subject-kind record))
                      (assoc record
                             :subject-id
                             "artifact:other-runtime")
                      record))
                  records))))
          "SH28-TCB"]
         [(-> base
              (assoc-in [:recovery-artifacts 0 :artifact-id]
                        "artifact:other-recovery")
              (update
               :revocations
               (fn [records]
                 (mapv
                  (fn [record]
                    (if (= :recovery-artifact
                           (:subject-kind record))
                      (assoc record
                             :subject-id
                             "artifact:other-recovery")
                      record))
                  records))))
          "SH28-TCB"]
         [(assoc-in base
                    [:tcb-delta :retained-components]
                    [:hardware])
          "SH28-TCB"]
         [(update-in base
                     [:tcb-delta :candidate-components]
                     conj
                     :hardware)
          "SH28-TCB"]
         [(assoc-in base
                    [:tcb-delta :release-boundary-exclusions]
                    ["artifact:other-recovery"])
          "SH28-TCB"]
         [(assoc-in base [:tcb-delta :review-id] "")
          "SH28-TCB"]]]
    (doseq [[candidate rule] cases]
      (testing rule
        (let [result (build candidate)]
          (is (= :rejected (:status result)))
          (is (= rule (get-in result [:diagnostics 0 :rule]))))))))

(deftest sh28-exact-result-alterations-fail-closed
  (let [request (accepted-request)
        result (build request)
        candidates
        [(assoc result :release-trust-closed? true)
         (assoc result :clojure-seed-retired? true)
         (assoc-in result [:identity-input :release-id] "altered")
         (assoc-in result [:provenance :actual-release-root] "/altered")
         (assoc-in result [:carrier-census :nodes] 1)
         (assoc result :unexpected-field :unexpected)]]
    (doseq [candidate candidates]
      (let [verification (verify request candidate)]
        (is (= :rejected (:status verification)))
        (is (= "SH28-VERIFY"
               (get-in verification [:diagnostics 0 :rule])))))))

(deftest sh28-carriers-are-preflighted-before-validation-and-comparison
  (let [overdeep
        (reduce (fn [value _] [value]) :leaf (range 100))
        too-wide (vec (range 2050))
        unbounded-lazy (map identity (range))
        exceptional-lazy
        (lazy-seq
         (throw (ex-info "must not realize" {:id "SH28-LAZY"})))
        cyclic-host-list (java.util.ArrayList.)
        _ (.add cyclic-host-list cyclic-host-list)
        accepted (accepted-request)
        accepted-result (build accepted)]
    (doseq [candidate
            [overdeep
             too-wide
             unbounded-lazy
             exceptional-lazy
             cyclic-host-list]]
      (let [result (build candidate)]
        (is (= :rejected (:status result)))
        (is (= "SH28-SCHEMA" (get-in result [:diagnostics 0 :rule])))))
    (is (= :rejected (:status (verify accepted overdeep))))
    (is (= :rejected (:status (verify accepted too-wide))))
    (is (= :rejected (:status (verify accepted exceptional-lazy))))
    (is (= :rejected (:status (verify accepted cyclic-host-list))))
    (is (= :passed (:status (verify accepted accepted-result))))))

(deftest sh28-rejected-fixtures-and-extension-pairs
  (let [gravity
        (request rejected-gravity-plan 'sh28-invalid-schema-request)
        qst (request rejected-qst-plan 'sh28-invalid-schema-request)]
    (is (= gravity qst))
    (is (= :rejected (:status (build gravity))))
    (is (= "SH28-SCHEMA"
           (get-in (build gravity) [:diagnostics 0 :rule]))))
  (is (= (slurp (path (str fixture-root
                          "/accepted/trust-evidence.gravity")))
         (slurp (path (str fixture-root
                          "/accepted/trust-evidence.qst")))))
  (is (= (slurp (path (str fixture-root
                          "/rejected/invalid-trust-evidence.gravity")))
         (slurp (path (str fixture-root
                          "/rejected/invalid-trust-evidence.qst"))))))
