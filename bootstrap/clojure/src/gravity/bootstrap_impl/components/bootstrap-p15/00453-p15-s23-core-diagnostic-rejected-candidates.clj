

(def p15-s23-core-diagnostic-rejected-candidates
  [{:fixture :internal-p15-s23-core-diagnostic-missing-report
    :candidate {}
    :expected-diagnostic "P15S23D001"}
   {:fixture :internal-p15-s23-core-diagnostic-core-gap
    :candidate {:proof-contract
                {:artifact
                 :gravity/core-lowering-and-diagnostic-preservation-report
                 :preserves p15-s23-core-diagnostic-required-preserves
                 :self-hosting-claims
                 {:full-language-compiler-self-hosted? false
                  :clojure-seed-retired? false}}
                :c6-core-lowering-artifact
                {:kind :gravity/stage0-c6-core-lowering-artifact
                 :core-verifier-report {:status :failed}
                 :core-node-table []}}
    :expected-diagnostic "P15S23D002"}
   {:fixture :internal-p15-s23-core-diagnostic-preservation-gap
    :candidate {:proof-contract
                {:artifact
                 :gravity/core-lowering-and-diagnostic-preservation-report
                 :preserves p15-s23-core-diagnostic-required-preserves
                 :self-hosting-claims
                 {:full-language-compiler-self-hosted? false
                  :clojure-seed-retired? false}}
                :c15-diagnostics-artifact
                {:kind :gravity/stage0-c15-compiler-diagnostics-artifact}
                :diagnostic-preservation-report
                {:status :failed
                 :stable-ids? false
                 :diagnostic-rules []}}
    :expected-diagnostic "P15S23D003"}
   {:fixture :internal-p15-s23-core-diagnostic-link-gap
    :candidate {:proof-contract
                {:artifact
                 :gravity/core-lowering-and-diagnostic-preservation-report
                 :preserves p15-s23-core-diagnostic-required-preserves
                 :self-hosting-claims
                 {:full-language-compiler-self-hosted? false
                  :clojure-seed-retired? false}}
                :c6-core-lowering-artifact {:artifact-id "c6"}
                :c15-diagnostics-artifact
                {:c6-core-lowering-artifact {:artifact-id "other"}}}
    :expected-diagnostic "P15S23D004"}
   {:fixture :internal-p15-s23-core-diagnostic-overclaim
    :candidate {:proof-contract
                {:artifact
                 :gravity/core-lowering-and-diagnostic-preservation-report
                 :preserves p15-s23-core-diagnostic-required-preserves
                 :self-hosting-claims
                 {:full-language-compiler-self-hosted? true
                  :clojure-seed-retired? true}}
                :c6-core-lowering-artifact
                {:kind :gravity/stage0-c6-core-lowering-artifact
                 :core-verifier-report {:status :passed}
                 :core-node-table [{:node-id "core"}]
                 :capability-based-proof
                 {:source-spans-preserved? true
                  :origin-chains-preserved? true}}
                :c15-diagnostics-artifact
                {:kind :gravity/stage0-c15-compiler-diagnostics-artifact}
                :diagnostic-preservation-report
                {:status :complete
                 :stable-ids? true
                 :source-spans-preserved? true
                 :syntax-identities-preserved? true
                 :origin-chains-preserved? true
                 :remediation-preserved? true
                 :diagnostic-rules p15-s23-core-diagnostic-ids}}
    :expected-diagnostic "P15S23D005"}])

(defn p15-s23-core-diagnostic-rejected-records
  [source-path]
  (mapv (fn [{:keys [fixture candidate expected-diagnostic]}]
          {:fixture fixture
           :status :rejected
           :expected-diagnostic expected-diagnostic
           :diagnostics
           (p15-s23-core-diagnostic-proof-diagnostics source-path
                                                      candidate)})
        p15-s23-core-diagnostic-rejected-candidates))

(defn p15-s23-core-diagnostic-stream
  [source-path proof-id]
  {:artifact :gravity/p15-s23-core-diagnostic-preservation-diagnostic-stream
   :stage :p15-s23-core-lowering-diagnostic-preservation-report
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage
            :p15-s23-core-lowering-diagnostic-preservation-report
            :message (get p15-s23-core-diagnostic-messages id)})
         p15-s23-core-diagnostic-ids)
   :status :complete})

(defn p15-s23-core-lowering-diagnostic-preservation-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-core-diagnostic-preservation-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat #(map :diagnostic (:diagnostics %))
                     (:rejected-p15-s23-core-diagnostic-fixtures artifact)))
        preservation (:diagnostic-preservation-report artifact)]
    {:core-diagnostic-proof-authored-in-gravity? true
     :status :in-progress
     :task "P15-S23"
     :source-syntax-proof-linked?
     (= :gravity/p15-s23-source-syntax-serialization-proof-artifact
        (get-in artifact [:source-syntax-artifact :kind]))
     :compiler-pipeline-manifest-linked?
     (= :gravity/p15-s23-compiler-pipeline-manifest-artifact
        (get-in artifact [:compiler-pipeline-manifest-artifact :kind]))
     :c6-core-lowering-artifact-linked?
     (= :gravity/stage0-c6-core-lowering-artifact
        (get-in artifact [:c6-core-lowering-artifact :kind]))
     :c15-diagnostics-artifact-linked?
     (= :gravity/stage0-c15-compiler-diagnostics-artifact
        (get-in artifact [:c15-diagnostics-artifact :kind]))
     :core-verifier-passed?
     (= :passed (get-in artifact
                        [:c6-core-lowering-artifact
                         :core-verifier-report :status]))
     :source-spans-preserved? (:source-spans-preserved? preservation)
     :syntax-identities-preserved?
     (:syntax-identities-preserved? preservation)
     :origin-chains-preserved? (:origin-chains-preserved? preservation)
     :stable-diagnostic-ids? (:stable-ids? preservation)
     :diagnostic-remediation-preserved?
     (:remediation-preserved? preservation)
     :does-not-claim-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :rejected-candidates-covered?
     (set/subset? (set p15-s23-core-diagnostic-ids)
                  rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-core-diagnostic-ids) diagnostics)
     :limitations
     {:full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
      :compiles-whole-claimed-subset? false
      :next-required-capability
      :implement_runtime_manifest_and_capability_enforcement_report}}))