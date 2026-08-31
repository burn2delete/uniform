

(defn p15-s23-reproducible-rebuild-log-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-reproducible-rebuild-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat #(map :diagnostic (:diagnostics %))
                     (:rejected-p15-s23-rebuild-proof-fixtures
                      artifact)))
        comparison (:artifact-identity-comparison artifact)]
    {:reproducible-rebuild-log-authored-in-gravity? true
     :status :in-progress
     :task "P15-S23"
     :rebuild-inputs-complete?
     (= (set p15-s23-reproducible-rebuild-stages)
        (set (map :stage (get-in artifact
                                  [:first-rebuild-record :stages]))))
     :second-rebuild-inputs-complete?
     (= (set p15-s23-reproducible-rebuild-stages)
        (set (map :stage (get-in artifact
                                  [:second-rebuild-record :stages]))))
     :artifact-identities-reproducible?
     (true? (:all-artifact-identities-match? comparison))
     :accepted-app-proof-linked?
     (boolean
      (some #(= :accepted-app-execution-proof (:stage %))
            (get-in artifact [:first-rebuild-record :stages])))
     :rejected-app-proof-linked?
     (boolean
      (some #(= :rejected-app-diagnostic-proof (:stage %))
            (get-in artifact [:first-rebuild-record :stages])))
     :environment-provenance-recorded?
     (= :complete (get-in artifact
                          [:environment-provenance-record :status]))
     :does-not-claim-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :rejected-candidates-covered?
     (set/subset? (set p15-s23-reproducible-rebuild-diagnostic-ids)
                  rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-reproducible-rebuild-diagnostic-ids) diagnostics)
     :limitations
     {:full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
      :clojure-stage0-rebuild? true
      :full-self-hosted-toolchain? false
      :next-required-capability
      :implement_stage_comparison_report}}))

(defn p15-s23-reproducible-rebuild-log-source-artifact
  [source-path]
  (p15-s23-context-artifact
   :reproducible-rebuild-log source-path
   (fn [] (let [source-data (p15-s23-compiler-source-form-record source-path)
        proof-contract
        (p15-s23-compiler-def-value
         source-path
         (:forms source-data)
         'p15-s23-reproducible-rebuild-log)
        first-rebuild (p15-s23-rebuild-record source-path :first)
        second-rebuild (p15-s23-rebuild-record source-path :second)
        comparison
        (p15-s23-artifact-identity-comparison first-rebuild
                                              second-rebuild)
        environment
        (p15-s23-rebuild-environment-provenance-record source-path
                                                       proof-contract)
        candidate {:proof-contract proof-contract
                   :first-rebuild-record first-rebuild
                   :second-rebuild-record second-rebuild
                   :artifact-identity-comparison comparison
                   :environment-provenance-record environment}
        diagnostics
        (p15-s23-reproducible-rebuild-proof-diagnostics source-path
                                                        candidate)
        _ (when (seq diagnostics)
            (p15-s23-reproducible-rebuild-fail!
             (:diagnostic (first diagnostics)) source-path candidate
             {:diagnostics diagnostics}))
        proof-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :first (mapv #(select-keys %
                                                   [:stage :artifact-id
                                                    :proof-id
                                                    :manifest-id
                                                    :serialization-id])
                                     (:stages first-rebuild))
                       :second (mapv #(select-keys %
                                                    [:stage :artifact-id
                                                     :proof-id
                                                     :manifest-id
                                                     :serialization-id])
                                      (:stages second-rebuild))
                       :proof-contract proof-contract})))
        rejected-records
        (p15-s23-reproducible-rebuild-rejected-records
         source-path candidate)
        artifact-base
        {:kind :gravity/p15-s23-reproducible-rebuild-log-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-reproducible-rebuild-log
         :source-path source-path
         :proof-id proof-id
         :proof-contract proof-contract
         :first-rebuild-record first-rebuild
         :second-rebuild-record second-rebuild
         :artifact-identity-comparison comparison
         :environment-provenance-record environment
         :full-language-compiler-self-hosted?
         (get-in proof-contract
                 [:self-hosting-claims
                  :full-language-compiler-self-hosted?])
         :clojure-seed-retired?
         (get-in proof-contract
                 [:self-hosting-claims :clojure-seed-retired?])
         :accepted-p15-s23-rebuild-fixtures
         [{:fixture source-path
           :status :accepted
           :rebuild-stage-count
           (count p15-s23-reproducible-rebuild-stages)
           :all-artifact-identities-match?
           (:all-artifact-identities-match? comparison)}]
         :rejected-p15-s23-rebuild-proof-fixtures rejected-records
         :p15-s23-reproducible-rebuild-diagnostic-stream
         (p15-s23-reproducible-rebuild-diagnostic-stream source-path
                                                         proof-id)
         :p15-s23-reproducible-rebuild-results
         {:accepted-fixtures 1
          :rejected-fixtures (count rejected-records)
          :diagnostic-count
          (count p15-s23-reproducible-rebuild-diagnostic-ids)
          :rebuild-stage-count
          (count p15-s23-reproducible-rebuild-stages)
          :all-artifact-identities-match?
          (:all-artifact-identities-match? comparison)
          :status :in-progress}
         :diagnostics []}
        proof (p15-s23-reproducible-rebuild-log-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))))

(defn p15-s23-reproducible-rebuild-log-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-reproducible-rebuild-fail!
     "P15S23B001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-with-artifact-build-context
   #(p15-s23-reproducible-rebuild-log-source-artifact path)))

(def p15-s23-stage-comparison-scope
  [:compiler-pipeline-manifest
   :accepted-app-execution-proof
   :rejected-app-diagnostic-proof
   :reproducible-rebuild-log])

(def p15-s23-stage-comparison-required-preserves
  #{:artifact-provenance :accepted-app-output
    :rejected-app-diagnostic-trace :diagnostic-codes
    :stage-equivalence-record})

(def p15-s23-stage-comparison-diagnostic-messages
  {"P15S23G001" "P15-S23 stage comparison report is missing"
   "P15S23G002" "P15-S23 stage comparison candidate or scope is incomplete"
   "P15S23G003" "P15-S23 accepted output differs between compared stages"
   "P15S23G004" "P15-S23 rejected diagnostics differ between compared stages"
   "P15S23G005" "P15-S23 stage comparison is not linked to a reproducible rebuild log"
   "P15S23G006" "P15-S23 stage comparison makes an unsupported self-hosting or seed-retirement claim"})

(def p15-s23-stage-comparison-diagnostic-ids
  ["P15S23G001" "P15S23G002" "P15S23G003"
   "P15S23G004" "P15S23G005" "P15S23G006"])

(defn p15-s23-stage-comparison-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-stage-comparison-diagnostic-messages
              id
              "P15-S23 stage comparison proof failed")
         (merge {:source-span {:source source-path}
                 :stage :p15-s23-stage-comparison-report
                 :diagnostic-family :p15-s23-stage-comparison-report
                 :value value
                 :remediation "Compare the current P15-S23 candidate with the seed-stage behavior, preserve accepted output and rejected diagnostics, link the reproducible rebuild log, and keep self-hosting claims false until the full evidence bundle exists."}
                data)))

(defn p15-s23-stage-comparison-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-stage-comparison-report
   :source-span {:source source-path}
   :message (get p15-s23-stage-comparison-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_stage_comparison_report})

(defn p15-s23-stage-boundary-record
  [source-path]
  {:artifact :gravity/p15-s23-stage-boundary-record
   :source-path source-path
   :seed-stage :clojure-stage0
   :candidate-stage :p15-s23-current-clojure-seed-candidate
   :candidate-is-self-hosted? false
   :full-self-hosted-equivalence? false
   :clojure-seed-boundary? true
   :full-self-hosted-toolchain? false
   :retirement-condition :complete-p15-s23-evidence-bundle
   :status :complete})