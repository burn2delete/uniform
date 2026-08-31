

(defn p15-s23-accepted-app-execution-source-artifact
  [source-path]
  (p15-s23-context-artifact
   :accepted-app-execution source-path
   (fn [] (let [source-data (p15-s23-compiler-source-form-record source-path)
        proof-contract
        (p15-s23-compiler-def-value
         source-path
         (:forms source-data)
         'p15-s23-accepted-app-execution-proof)
        expected-stdout
        (or (get-in proof-contract [:accepted-fixture :expected-stdout])
            p15-s23-accepted-app-expected-stdout)
        compiled-app-fn
        (resolve 'gravity.bootstrap/hosted-core-compiled-app-proof-file-artifact)
        _ (when-not compiled-app-fn
            (p15-s23-accepted-app-fail!
             "P15S23A002" source-path nil
             {:missing-fields [:hosted-core-compiled-app-proof-file-artifact]}))
        accepted-app-artifact
        (p15-s23-context-artifact
         :hosted-core-compiled-app-proof
         p15-s23-accepted-app-source-path
         (fn [] (compiled-app-fn p15-s23-accepted-app-source-path)))
        runtime-artifact
        (p15-s23-runtime-manifest-capability-enforcement-source-artifact
         source-path)
        output-comparison
        (p15-s23-accepted-app-output-comparison accepted-app-artifact
                                                expected-stdout)
        execution-trace
        (p15-s23-compiled-plan-execution-trace accepted-app-artifact)
        runtime-use
        (p15-s23-accepted-app-runtime-capability-use-record
         accepted-app-artifact
         runtime-artifact)
        trusted-boundary
        (p15-s23-accepted-app-trusted-boundary-record accepted-app-artifact)
        candidate {:proof-contract proof-contract
                   :accepted-app-artifact accepted-app-artifact
                   :accepted-output-comparison output-comparison
                   :compiled-plan-execution-trace execution-trace
                   :runtime-capability-artifact runtime-artifact
                   :runtime-capability-use-record runtime-use
                   :trusted-boundary-record trusted-boundary}
        diagnostics
        (p15-s23-accepted-app-proof-diagnostics source-path candidate)
        _ (when (seq diagnostics)
            (p15-s23-accepted-app-fail!
             (:diagnostic (first diagnostics)) source-path candidate
             {:diagnostics diagnostics}))
        proof-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :accepted-app-artifact
                       (:artifact-id accepted-app-artifact)
                       :compiled-plan-id
                       (get-in accepted-app-artifact
                               [:compiled-plan :plan-id])
                       :runtime-artifact (:artifact-id runtime-artifact)
                       :expected-stdout expected-stdout
                       :proof-contract proof-contract})))
        rejected-records
        (p15-s23-accepted-app-rejected-records source-path candidate)
        artifact-base
        {:kind :gravity/p15-s23-accepted-app-execution-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-accepted-app-execution-proof
         :source-path source-path
         :proof-id proof-id
         :proof-contract proof-contract
         :accepted-app-path p15-s23-accepted-app-source-path
         :accepted-app-artifact
         (select-keys accepted-app-artifact
                      [:kind :artifact-id :phase :task :source :module
                       :compiled-plan :runtime-surface :accepted-run
                       :reference-run :trusted-boundary
                       :capability-based-proof])
         :runtime-capability-artifact
         (select-keys runtime-artifact
                      [:kind :artifact-id :proof-id
                       :runtime-manifest
                       :runtime-capability-manifest
                       :capability-enforcement-table
                       :core-diagnostic-artifact
                       :compiler-pipeline-manifest-artifact
                       :capability-based-proof])
         :compiled-plan-execution-trace execution-trace
         :accepted-output-comparison output-comparison
         :runtime-capability-use-record runtime-use
         :trusted-boundary-record trusted-boundary
         :full-language-compiler-self-hosted?
         (get-in proof-contract
                 [:self-hosting-claims
                  :full-language-compiler-self-hosted?])
         :clojure-seed-retired?
         (get-in proof-contract
                 [:self-hosting-claims :clojure-seed-retired?])
         :accepted-p15-s23-app-execution-fixtures
         [{:fixture p15-s23-accepted-app-source-path
           :status :accepted
           :profile (get-in accepted-app-artifact [:module :profile])
           :target (get-in accepted-app-artifact [:module :target])
           :effects (get-in accepted-app-artifact [:module :effects])
           :capabilities (get-in accepted-app-artifact
                                 [:module :capabilities])
           :user-functions
           (get-in accepted-app-artifact
                   [:runtime-surface :user-functions])
           :compiled-plan-id
           (get-in accepted-app-artifact [:compiled-plan :plan-id])
           :stdout (get-in output-comparison [:accepted-stdout])}]
         :rejected-p15-s23-app-execution-fixtures rejected-records
         :p15-s23-accepted-app-execution-diagnostic-stream
         (p15-s23-accepted-app-diagnostic-stream source-path proof-id)
         :p15-s23-accepted-app-execution-results
         {:accepted-fixtures 1
          :rejected-fixtures (count rejected-records)
          :diagnostic-count (count p15-s23-accepted-app-diagnostic-ids)
          :stdout (get-in output-comparison [:accepted-stdout])
          :compiled-plan-id
          (get-in accepted-app-artifact [:compiled-plan :plan-id])
          :status :in-progress}
         :diagnostics []}
        proof (p15-s23-accepted-app-execution-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))))

(defn p15-s23-accepted-app-execution-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-accepted-app-fail!
     "P15S23A001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-with-artifact-build-context
   #(p15-s23-accepted-app-execution-source-artifact path)))

(def p15-s23-rejected-app-fixtures
  [{:fixture
    "bootstrap/clojure/fixtures/rejected/core-app-function-arity.gravity"
    :expected-diagnostic "L2-FUNCTION-ARITY"
    :rejected-design :wrong-user-function-arity}
   {:fixture
    "bootstrap/clojure/fixtures/rejected/core-app-builtin-arity.gravity"
    :expected-diagnostic "L2-BUILTIN-ARITY"
    :rejected-design :wrong-builtin-arity}])

(def p15-s23-rejected-app-required-preserves
  #{:source-spans :diagnostic-codes :diagnostic-origin-chain :remediation
    :artifact-provenance :rejected-app-diagnostic-trace})

(def p15-s23-rejected-app-diagnostic-messages
  {"P15S23E001" "P15-S23 rejected app diagnostic proof is missing"
   "P15S23E002" "P15-S23 rejected app fixture manifest is incomplete"
   "P15S23E003" "P15-S23 rejected app fixture was accepted unexpectedly"
   "P15S23E004" "P15-S23 rejected app diagnostic is unstable or mismatched"
   "P15S23E005" "P15-S23 rejected app diagnostics are not linked to accepted app/compiler evidence"
   "P15S23E006" "P15-S23 rejected app diagnostic proof makes an unsupported self-hosting or seed-retirement claim"})

(def p15-s23-rejected-app-diagnostic-ids
  ["P15S23E001" "P15S23E002" "P15S23E003"
   "P15S23E004" "P15S23E005" "P15S23E006"])

(defn p15-s23-rejected-app-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-rejected-app-diagnostic-messages
              id
              "P15-S23 rejected app diagnostic proof failed")
         (merge {:source-span {:source source-path}
                 :stage :p15-s23-rejected-app-diagnostic-proof
                 :diagnostic-family
                 :p15-s23-rejected-app-diagnostic-proof
                 :value value
                 :remediation "Run invalid Gravity app fixtures through the current compiled path, prove they fail closed with stable diagnostics, link the result to accepted app/compiler evidence, and keep self-hosting claims false until the complete P15-S23 bundle exists."}
                data)))

(defn p15-s23-rejected-app-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-rejected-app-diagnostic-proof
   :source-span {:source source-path}
   :message (get p15-s23-rejected-app-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_rejected_app_diagnostic_proof})

(defn p15-s23-run-rejected-app-fixture
  [compiled-runner {:keys [fixture expected-diagnostic rejected-design]}]
  (try
    {:fixture fixture
     :rejected-design rejected-design
     :expected-diagnostic expected-diagnostic
     :status :accepted-unexpectedly
     :stdout (compiled-runner fixture)
     :diagnostic nil
     :matches-expected? false}
    (catch clojure.lang.ExceptionInfo ex
      (let [data (ex-data ex)
            diagnostic (:id data)]
        {:fixture fixture
         :rejected-design rejected-design
         :expected-diagnostic expected-diagnostic
         :status :rejected
         :diagnostic diagnostic
         :message (:message data)
         :diagnostic-data
         (select-keys data
                      [:id :message :source-span :callee :actual-arity
                       :expected-arities :builtin :required-capability
                       :declared-capabilities :remediation])
         :matches-expected? (= expected-diagnostic diagnostic)}))))