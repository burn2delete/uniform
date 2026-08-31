

(def p15-s23-stage2-compiler-nucleus-required-preserves
  #{:source-spans :diagnostic-codes :effects :capabilities :profile
    :compiler-lineage :artifact-provenance})

(def p15-s23-stage2-compiler-nucleus-required-emits
  #{:compiled-instruction-plan :accepted-output-comparison
    :rejected-diagnostic-comparison :stage2-boundary-record})

(def p15-s23-stage2-compiler-nucleus-diagnostic-messages
  {"P15S23N001" "P15-S23 stage2 compiler nucleus contract is missing"
   "P15S23N002" "P15-S23 stage2 compiler nucleus does not match the accepted compiled app plan"
   "P15S23N003" "P15-S23 stage2 compiler nucleus does not preserve rejected app diagnostics"
   "P15S23N004" "P15-S23 stage2 compiler nucleus is missing required evidence links"
   "P15S23N005" "P15-S23 stage2 compiler nucleus preservation or emission contract is incomplete"
   "P15S23N006" "P15-S23 stage2 compiler nucleus residual Clojure seed boundary is incomplete"
   "P15S23N007" "P15-S23 stage2 compiler nucleus makes an unsupported self-hosting or seed-retirement claim"})

(def p15-s23-stage2-compiler-nucleus-diagnostic-ids
  ["P15S23N001" "P15S23N002" "P15S23N003" "P15S23N004"
   "P15S23N005" "P15S23N006" "P15S23N007"])

(defn p15-s23-stage2-compiler-nucleus-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-stage2-compiler-nucleus-diagnostic-messages
              id
              "P15-S23 stage2 compiler nucleus proof failed")
         (merge {:source-span {:source source-path}
                 :stage :p15-s23-stage2-compiler-nucleus
                 :diagnostic-family
                 :p15-s23-stage2-compiler-nucleus
                 :value value
                 :remediation "Keep the compiler nucleus authored in Gravity source, prove it against the accepted compiled-plan fixture and rejected diagnostics, record the remaining Clojure seed boundary, and keep full self-hosting claims false."}
                data)))

(defn p15-s23-stage2-compiler-nucleus-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-stage2-compiler-nucleus
   :source-span {:source source-path}
   :message (get p15-s23-stage2-compiler-nucleus-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_stage2_compiler_nucleus_contract})

(defn p15-s23-stage2-sort-values
  [values]
  (vec (sort-by str values)))

(defn p15-s23-stage2-compiler-nucleus-accepted-plan-record
  [nucleus compiled-app-artifact]
  (let [contract (:accepted-plan-contract nucleus)
        plan (:compiled-plan compiled-app-artifact)
        summary (:instruction-summary plan)
        required-ops (set (:required-op-families contract))
        present-ops (set (keys summary))
        missing-ops (set/difference required-ops present-ops)
        binding-names (set (map :name (:binding-table plan)))
        required-users (set (:required-user-functions contract))
        missing-users (set/difference required-users binding-names)
        required-effects (set (:required-effects contract))
        required-capabilities (set (:required-capabilities contract))
        declared-effects (set (get-in plan [:effect-summary :declared]))
        declared-capabilities (set (get-in plan
                                           [:effect-summary :capabilities]))
        stdout (get-in compiled-app-artifact [:accepted-run :stdout])
        expected-stdout (:expected-stdout contract)
        matches?
        (and (= :gravity/stage0-hosted-core-compiled-app-proof
                (:kind compiled-app-artifact))
             (= p15-s23-accepted-app-source-path (:fixture contract))
             (= p15-s23-accepted-app-source-path
                (get-in compiled-app-artifact [:source :path]))
             (= (:module contract)
                (get-in compiled-app-artifact [:module :module]))
             (= (:profile contract)
                (get-in compiled-app-artifact [:module :profile]))
             (= (:target contract)
                (get-in compiled-app-artifact [:module :target]))
             (= :gravity/stage0-hosted-core-compiled-plan (:kind plan))
             (= (:entrypoint contract) (:entrypoint plan))
             (empty? missing-ops)
             (empty? missing-users)
             (= required-effects declared-effects)
             (= required-capabilities declared-capabilities)
             (= stdout expected-stdout)
             (true? (get-in compiled-app-artifact
                             [:capability-based-proof
                              :compiled-plan-executed?])))]
    {:artifact :gravity/p15-s23-stage2-accepted-plan-record
     :fixture (:fixture contract)
     :compiled-plan-id (:plan-id plan)
     :compiled-plan-kind (:kind plan)
     :entrypoint (:entrypoint plan)
     :required-op-families (p15-s23-stage2-sort-values required-ops)
     :present-op-families (p15-s23-stage2-sort-values present-ops)
     :missing-op-families (p15-s23-stage2-sort-values missing-ops)
     :required-user-functions (p15-s23-stage2-sort-values required-users)
     :binding-names (p15-s23-stage2-sort-values binding-names)
     :missing-user-functions (p15-s23-stage2-sort-values missing-users)
     :required-effects required-effects
     :declared-effects declared-effects
     :required-capabilities required-capabilities
     :declared-capabilities declared-capabilities
     :stdout stdout
     :expected-stdout expected-stdout
     :output-matches? (= stdout expected-stdout)
     :compiled-plan-executed?
     (true? (get-in compiled-app-artifact
                    [:capability-based-proof :compiled-plan-executed?]))
     :status (if matches? :complete :failed)}))

(defn p15-s23-stage2-compiler-nucleus-rejected-diagnostic-record
  [nucleus rejected-artifact]
  (let [contract-rows
        (set (map (juxt :fixture :expected-diagnostic)
                  (:rejected-diagnostic-contract nucleus)))
        verified-rows
        (set (map (juxt :fixture :expected-diagnostic)
                  (:verified-p15-s23-rejected-app-fixtures
                   rejected-artifact)))
        expected-diagnostics
        (set (map :expected-diagnostic
                  (:rejected-diagnostic-contract nucleus)))
        observed-diagnostics
        (set (map :diagnostic
                  (:verified-p15-s23-rejected-app-fixtures
                   rejected-artifact)))
        mismatches
        (remove :matches-expected?
                (:verified-p15-s23-rejected-app-fixtures
                 rejected-artifact))
        matches?
        (and (= :gravity/p15-s23-rejected-app-diagnostic-artifact
                (:kind rejected-artifact))
             (= contract-rows verified-rows)
             (= expected-diagnostics observed-diagnostics)
             (empty? mismatches))]
    {:artifact :gravity/p15-s23-stage2-rejected-diagnostic-record
     :expected-fixture-count (count contract-rows)
     :verified-fixture-count (count verified-rows)
     :expected-diagnostics (p15-s23-stage2-sort-values
                            expected-diagnostics)
     :observed-diagnostics (p15-s23-stage2-sort-values
                            observed-diagnostics)
     :missing-fixtures
     (p15-s23-stage2-sort-values
      (set/difference contract-rows verified-rows))
     :unexpected-fixtures
     (p15-s23-stage2-sort-values
      (set/difference verified-rows contract-rows))
     :mismatch-count (count mismatches)
     :status (if matches? :complete :failed)}))

(defn p15-s23-stage2-compiler-nucleus-evidence-link-record
  [nucleus pipeline-artifact accepted-artifact rejected-artifact]
  (let [required-links (set (:required-evidence-links nucleus))
        links {:compiler-pipeline-manifest
               {:artifact (:kind pipeline-artifact)
                :artifact-id (:artifact-id pipeline-artifact)
                :present?
                (= :gravity/p15-s23-compiler-pipeline-manifest-artifact
                   (:kind pipeline-artifact))}
               :accepted-app-execution-proof
               {:artifact (:kind accepted-artifact)
                :artifact-id (:artifact-id accepted-artifact)
                :proof-id (:proof-id accepted-artifact)
                :present?
                (= :gravity/p15-s23-accepted-app-execution-artifact
                   (:kind accepted-artifact))}
               :rejected-app-diagnostic-proof
               {:artifact (:kind rejected-artifact)
                :artifact-id (:artifact-id rejected-artifact)
                :proof-id (:proof-id rejected-artifact)
                :present?
                (= :gravity/p15-s23-rejected-app-diagnostic-artifact
                   (:kind rejected-artifact))}}
        present-links (set (for [[k v] links :when (:present? v)] k))
        missing-links (set/difference required-links present-links)]
    {:artifact :gravity/p15-s23-stage2-evidence-link-record
     :required-links (p15-s23-stage2-sort-values required-links)
     :present-links links
     :missing-links (p15-s23-stage2-sort-values missing-links)
     :all-required-links-present? (empty? missing-links)
     :status (if (empty? missing-links) :complete :failed)}))

(defn p15-s23-stage2-compiler-nucleus-boundary-record
  [nucleus]
  (let [claims (:self-hosting-claims nucleus)]
    {:artifact :gravity/p15-s23-stage2-boundary-record
     :implemented-by (:implemented-by nucleus)
     :verified-by (:verified-by nucleus)
     :compiled-by (get-in nucleus [:lineage :compiled-by])
     :source-language (get-in nucleus [:lineage :source-language])
     :clojure-stage0-verifier?
     (= :clojure-stage0 (:verified-by nucleus))
     :clojure-stage0-compiler?
     (= :clojure-stage0 (get-in nucleus [:lineage :compiled-by]))
     :clojure-instruction-runner?
     (= :clojure-instruction-runner
        (get-in nucleus [:seed-boundary :stage0-executor-boundary]))
     :self-hosted-compiler? false
     :full-language-compiler-self-hosted?
     (:full-language-compiler-self-hosted? claims)
     :clojure-seed-retired? (:clojure-seed-retired? claims)
     :seed-boundary (:seed-boundary nucleus)
     :next-required-capability (:next-required-capability nucleus)
     :status :complete}))