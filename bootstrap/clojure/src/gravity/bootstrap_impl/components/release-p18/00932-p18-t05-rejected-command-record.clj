

(defn p18-t05-rejected-command-record
  [{:keys [fixture output-path expected-diagnostic]}]
  (let [result (p18-t04-shell p18-t05-release-binary-path "compile"
                              fixture "-o" output-path)
        diagnostic-present? (str/includes? (:err result)
                                           expected-diagnostic)]
    {:fixture fixture
     :command [p18-t05-release-binary-path "compile" fixture "-o"
               output-path]
     :status :rejected
     :expected-diagnostic expected-diagnostic
     :result (select-keys result [:exit :out :err])
     :stable-diagnostic-through-seedless-binary? diagnostic-present?
     :matches-expected? (and (= 1 (:exit result))
                             diagnostic-present?)}))

(defn p18-t05-rejected-boundary-records
  [boundary]
  (let [fixtures
        [{:fixture :p18-t05-clojure-in-release-boundary
          :expected-diagnostic "P18T05001"
          :boundary
          (p18-t05-update-component
           boundary :gravity-binary
           #(assoc % :clojure-seed-boundary? true
                   :implementation-family :clojure-jvm))}
         {:fixture :p18-t05-missing-seed-facts
          :expected-diagnostic "P18T05002"
          :boundary
          (p18-t05-update-component
           boundary :runtime-path
           #(dissoc % :clojure-seed-boundary?))}
         {:fixture :p18-t05-seed-boundary-regression
          :expected-diagnostic "P18T05003"
          :boundary
          (assoc-in boundary
                    [:seed-boundary-facts
                     :runtime-path-clojure-seed-boundary?]
                    true)}
         {:fixture :p18-t05-bootstrap-command-confusion
          :expected-diagnostic "P18T05004"
          :boundary
          (-> boundary
              (update :release-boundary-components conj
                      {:component :bootstrap-recovery-path
                       :path "bin/gravity-bootstrap"
                       :clojure-seed-boundary? true
                       :included-in-public-release-boundary? true
                       :status :rejected})
              (assoc-in [:bootstrap-recovery-boundary
                         :included-in-public-release-boundary?]
                        true))}
         {:fixture :p18-t05-release-compiler-ambiguity
          :expected-diagnostic "P18T05005"
          :boundary
          (p18-t05-update-component
           boundary :release-compiler-path
           #(dissoc % :release-compiler-id))}]]
    (mapv
     (fn [{:keys [fixture expected-diagnostic boundary]}]
       (let [diagnostics (p18-t05-boundary-diagnostics boundary)
             observed (set (map :diagnostic diagnostics))]
         {:fixture fixture
          :status :rejected
          :expected-diagnostic expected-diagnostic
          :diagnostics diagnostics
          :matches-expected? (contains? observed expected-diagnostic)}))
     fixtures)))

(defn p18-t05-tcb-delta-record
  [boundary]
  (let [base {:artifact :gravity/p18-t05-tcb-delta-record
              :schema-version "gravity.release-tcb-delta/v1"
              :task "P18-T05"
              :baseline-components
              [{:component :bin-gravity-bootstrap-proof-runner
                :path "bin/gravity"
                :clojure-seed-boundary? true
                :included-in-public-release-boundary? false}
               {:component :gravity-bootstrap-recovery
                :path "bin/gravity-bootstrap"
                :clojure-seed-boundary? true
                :included-in-public-release-boundary? false}]
              :release-boundary-components
              (mapv #(select-keys % [:component :path :artifact-id
                                     :clojure-seed-boundary?])
                    (:release-boundary-components boundary))
              :removed-from-public-release-boundary
              [:clojure-stage0 :jvm-cli-packaging :bin-gravity-bootstrap]
              :residual-trusted-components
              [:operating-system-process-loader :filesystem-artifact-loader]
              :unaccounted-trusted-components []
              :status (:status boundary)}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t05-provenance-attestation-record
  [boundary p18-t03-artifact p18-t04-artifact]
  (let [base {:artifact :gravity/p18-t05-provenance-attestation
              :schema-version "gravity.release-boundary-provenance/v1"
              :task "P18-T05"
              :subject {:path p18-t05-release-binary-path
                        :content-hash
                        (get-in boundary
                                [:release-boundary-components 0
                                 :content-hash])
                        :clojure-seed-boundary?
                        (:clojure-seed-boundary? boundary)}
              :builder-identity
              :gravity-stage3-release-compiler-boundary-proof
              :compiler-lineage
              [{:stage :p15-final-seed-retirement
                :artifact-id
                (get-in boundary
                        [:seed-boundary-facts
                         :p15-final-seed-retirement-proof-id])}
               {:stage :p18-t03-self-hosted-release-artifact
                :artifact-id (:artifact-id p18-t03-artifact)}
               {:stage :p18-t04-executable-command-contract
                :artifact-id (:artifact-id p18-t04-artifact)}
               {:stage :p18-t05-seedless-release-boundary
                :artifact-id (:artifact-id boundary)}]
              :required-contracts ["BOOT7" "BOOT8" "PKG10" "PKG12" "D9"]
              :required-links-present? true
              :canonicalized-before-signing? true
              :sbom-and-signing-finalization-required? true
              :next-required-capability
              :p18-t06-reproducibility-provenance-sbom-signing-governance
              :status (:status boundary)}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t05-bootstrap-audit-record
  [boundary]
  (let [base {:artifact :gravity/p18-t05-bootstrap-audit-record
              :schema-version "gravity.bootstrap-audit-boundary/v1"
              :task "P18-T05"
              :public-release-boundary
              (mapv :component (:release-boundary-components boundary))
              :audit-and-recovery-commands
              [(:bootstrap-hosted-command-boundary boundary)
               (:bootstrap-recovery-boundary boundary)]
              :bootstrap-recovery-explicit? true
              :bootstrap-excluded-from-public-release-boundary?
              (and (false? (get-in boundary
                                    [:bootstrap-hosted-command-boundary
                                     :included-in-public-release-boundary?]))
                   (false? (get-in boundary
                                    [:bootstrap-recovery-boundary
                                     :included-in-public-release-boundary?])))
              :status :complete}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t05-release-eligibility-report
  [boundary]
  (let [base {:artifact :gravity/p18-t05-release-eligibility-report
              :schema-version "gravity.release-eligibility/v1"
              :task "P18-T05"
              :seedless-release-boundary-eligible?
              (p18-t05-seed-boundary-retired? boundary)
              :final-release-eligible? false
              :remaining-gates (if (p18-t05-seed-boundary-retired? boundary)
                                 ["P18-T06"]
                                 ["P15-S23" "P18-T06"])
              :blocked-on
              (if (p18-t05-seed-boundary-retired? boundary)
                [:reproducible-binary-evidence :complete-provenance
                 :complete-sbom :release-signing-records
                 :governance-release-approval]
                [:p15-final-seed-retirement :reproducible-binary-evidence
                 :complete-provenance :complete-sbom
                 :release-signing-records :governance-release-approval])
              :status (:status boundary)}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t05-diagnostic-stream
  [proof-id]
  {:artifact :gravity/p18-t05-diagnostic-stream
   :stage :p18-t05-seedless-release-boundary
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p18-t05-seedless-release-boundary
            :message (get p18-t05-diagnostic-messages id)
            :stable? true})
         p18-t05-diagnostic-ids)
   :status :complete})