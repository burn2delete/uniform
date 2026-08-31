

(defn p15-s23-runtime-manifest-capability-enforcement-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-runtime-manifest-capability-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat #(map :diagnostic (:diagnostics %))
                     (:rejected-p15-s23-runtime-capability-fixtures
                      artifact)))
        conformance (:capability-conformance-evidence artifact)]
    {:runtime-capability-proof-authored-in-gravity? true
     :status :in-progress
     :task "P15-S23"
     :core-diagnostic-proof-linked?
     (= :gravity/p15-s23-core-lowering-diagnostic-preservation-artifact
        (get-in artifact [:core-diagnostic-artifact :kind]))
     :compiler-pipeline-manifest-linked?
     (= :gravity/p15-s23-compiler-pipeline-manifest-artifact
        (get-in artifact [:compiler-pipeline-manifest-artifact :kind]))
     :runtime-manifest-complete?
     (= :complete (get-in artifact [:runtime-manifest :status]))
     :runtime-family-selection-explicit?
     (= :complete
        (get-in artifact
                [:runtime-manifest :selection-record :status]))
     :runtime-services-classified?
     (and (= :complete (:status (:runtime-service-table artifact)))
          (empty? (get-in artifact [:runtime-service-table
                                    :hidden-services])))
     :capability-manifest-deny-by-default?
     (true? (get-in artifact
                    [:runtime-capability-manifest
                     :deny-by-default?]))
     :grant-deny-delegate-revoke-covered?
     (:grant-deny-delegate-revoke-covered? conformance)
     :authority-families-covered?
     (:action-families-covered? conformance)
     :runtime-checks-do-not-grant-authority?
     (true? (get-in artifact
                    [:capability-enforcement-table
                     :runtime-checks-do-not-grant-authority?]))
     :ambient-authority-rejected?
     (true? (get-in artifact
                    [:capability-enforcement-table
                     :ambient-authority-rejected?]))
     :principal-identity-covered?
     (:principal-identity-covered? conformance)
     :audit-log-covered? (:audit-log-covered? conformance)
     :delegated-handles-scoped?
     (:delegated-handles-scoped? conformance)
     :revocation-covered? (:revocation-covered? conformance)
     :secret-redaction-covered? (:secret-redaction-covered? conformance)
     :does-not-claim-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :rejected-candidates-covered?
     (set/subset? (set p15-s23-runtime-diagnostic-ids)
                  rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-runtime-diagnostic-ids) diagnostics)
     :limitations
     {:full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
      :compiles-whole-claimed-subset? false
      :next-required-capability
      :implement_accepted_app_execution_proof}}))

(defn p15-s23-runtime-manifest-capability-enforcement-source-artifact
  [source-path]
  (p15-s23-context-artifact
   :runtime-manifest-capability-enforcement source-path
   (fn [] (let [source-data (p15-s23-compiler-source-form-record source-path)
        proof-contract
        (p15-s23-compiler-def-value
         source-path
         (:forms source-data)
         'p15-s23-runtime-manifest-capability-enforcement-report)
        core-artifact
        (p15-s23-core-lowering-diagnostic-preservation-source-artifact
         source-path)
        pipeline-artifact
        (p15-s23-compiler-pipeline-manifest-source-artifact source-path)
        runtime-manifest
        (p15-s23-runtime-manifest source-path (:artifact-id core-artifact))
        service-table (p15-s23-runtime-service-table runtime-manifest)
        capability-table
        (p15-s23-runtime-capability-table source-path
                                          (:artifact-id core-artifact))
        capability-manifest
        (p15-s23-runtime-capability-manifest capability-table)
        audit-records
        (p15-s23-runtime-identity-and-audit-records source-path
                                                    capability-table)
        conformance
        (p15-s23-runtime-conformance-evidence runtime-manifest
                                              service-table
                                              capability-manifest
                                              capability-table
                                              audit-records)
        candidate {:proof-contract proof-contract
                   :runtime-manifest runtime-manifest
                   :runtime-service-table service-table
                   :runtime-capability-manifest capability-manifest
                   :capability-enforcement-table capability-table
                   :audit-records audit-records
                   :capability-conformance-evidence conformance
                   :core-diagnostic-artifact core-artifact
                   :compiler-pipeline-manifest-artifact pipeline-artifact}
        diagnostics (p15-s23-runtime-proof-diagnostics source-path
                                                       candidate)
        _ (when (seq diagnostics)
            (p15-s23-runtime-fail!
             (:diagnostic (first diagnostics)) source-path candidate
             {:diagnostics diagnostics}))
        proof-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :core-artifact (:artifact-id core-artifact)
                       :pipeline-manifest (:artifact-id pipeline-artifact)
                       :runtime-manifest runtime-manifest
                       :capability-manifest capability-manifest
                       :capability-table
                       (:artifact capability-table)
                       :proof-contract proof-contract})))
        rejected-records (p15-s23-runtime-rejected-records source-path)
        artifact-base
        (merge
         {:kind
          :gravity/p15-s23-runtime-manifest-capability-enforcement-artifact
          :phase "15"
          :task "P15-S23"
          :stage
          :p15-s23-runtime-manifest-capability-enforcement-report
          :source-path source-path
          :proof-id proof-id
          :proof-contract proof-contract
          :core-diagnostic-artifact
          (select-keys core-artifact
                       [:kind :artifact-id :proof-id
                        :diagnostic-preservation-report
                        :capability-based-proof])
          :compiler-pipeline-manifest-artifact
          (select-keys pipeline-artifact
                       [:kind :artifact-id :manifest-id
                        :capability-based-proof])
          :runtime-manifest runtime-manifest
          :runtime-service-table service-table
          :runtime-capability-manifest capability-manifest
          :capability-enforcement-table capability-table
          :capability-conformance-evidence conformance
          :full-language-compiler-self-hosted?
          (get-in proof-contract
                  [:self-hosting-claims
                   :full-language-compiler-self-hosted?])
          :clojure-seed-retired?
          (get-in proof-contract
                  [:self-hosting-claims :clojure-seed-retired?])
          :accepted-p15-s23-runtime-capability-fixtures
          [{:fixture source-path
            :status :accepted
            :runtime-family (:family runtime-manifest)
            :runtime-consumers (:consumed-by runtime-manifest)
            :decision-count
            (count (:rows capability-table))
            :families-covered
            (count (:families-covered capability-table))}]
          :rejected-p15-s23-runtime-capability-fixtures
          rejected-records
          :p15-s23-runtime-manifest-capability-diagnostic-stream
          (p15-s23-runtime-diagnostic-stream source-path proof-id)
          :p15-s23-runtime-manifest-capability-results
          {:accepted-fixtures 1
           :rejected-fixtures (count rejected-records)
           :diagnostic-count (count p15-s23-runtime-diagnostic-ids)
           :decision-count (count (:rows capability-table))
           :authority-family-count
           (count (:families-covered capability-table))
           :status :in-progress}
          :diagnostics []}
         audit-records)
        proof
        (p15-s23-runtime-manifest-capability-enforcement-proof
         artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))))

(defn p15-s23-runtime-manifest-capability-enforcement-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-runtime-fail!
     "P15S23R001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-with-artifact-build-context
   #(p15-s23-runtime-manifest-capability-enforcement-source-artifact path)))

(def p15-s23-accepted-app-source-path
  "bootstrap/clojure/fixtures/accepted/core-app.gravity")

(def p15-s23-accepted-app-expected-stdout
  "core-app\ngravity:19:2\n(:ok 19)\n")

(def p15-s23-accepted-app-required-preserves
  #{:source-spans :diagnostic-codes :artifact-provenance :effects
    :capabilities :runtime-capability-manifest
    :compiled-plan-execution-trace :accepted-app-output})

(def p15-s23-accepted-app-diagnostic-messages
  {"P15S23A001" "P15-S23 accepted app execution proof is missing"
   "P15S23A002" "P15-S23 accepted app fixture or compiled execution record is incomplete"
   "P15S23A003" "P15-S23 accepted app output does not match the reference run and expected stdout"
   "P15S23A004" "P15-S23 accepted app execution is not linked to the compiler/runtime evidence bundle"
   "P15S23A005" "P15-S23 accepted app trusted boundary is incomplete or hidden"
   "P15S23A006" "P15-S23 accepted app proof makes an unsupported self-hosting or seed-retirement claim"})

(def p15-s23-accepted-app-diagnostic-ids
  ["P15S23A001" "P15S23A002" "P15S23A003"
   "P15S23A004" "P15S23A005" "P15S23A006"])

(defn p15-s23-accepted-app-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-accepted-app-diagnostic-messages
              id
              "P15-S23 accepted app execution proof failed")
         (merge {:source-span {:source source-path}
                 :stage :p15-s23-accepted-app-execution-proof
                 :diagnostic-family
                 :p15-s23-accepted-app-execution-proof
                 :value value
                 :remediation "Run a nontrivial accepted Gravity app through the current compiled-plan path, compare its output, link the result to P15-S23 compiler/runtime artifacts, expose the remaining Clojure runner boundary, and keep full self-hosting claims false."}
                data)))