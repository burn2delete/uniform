

(defn p15-s23-rejected-app-diagnostic-source-artifact
  [source-path]
  (p15-s23-context-artifact
   :rejected-app-diagnostic source-path
   (fn [] (let [source-data (p15-s23-compiler-source-form-record source-path)
        proof-contract
        (p15-s23-compiler-def-value
         source-path
         (:forms source-data)
         'p15-s23-rejected-app-diagnostic-proof)
        compiled-runner
        (resolve 'gravity.bootstrap/run-compiled-file)
        _ (when-not compiled-runner
            (p15-s23-rejected-app-fail!
             "P15S23E002" source-path nil
             {:missing-fields [:run-compiled-file]}))
        accepted-app-artifact
        (p15-s23-accepted-app-execution-source-artifact source-path)
        rejected-records
        (mapv #(p15-s23-run-rejected-app-fixture compiled-runner %)
              p15-s23-rejected-app-fixtures)
        preservation
        (p15-s23-rejected-app-diagnostic-preservation-record
         rejected-records)
        trusted-boundary
        (select-keys (:trusted-boundary-record accepted-app-artifact)
                     [:artifact :compiler :runtime :instruction-plan?
                      :direct-form-interpreter?
                      :clojure-instruction-runner?
                      :self-hosted-compiler?
                      :clojure-seed-retired?
                      :seed-boundary :retirement-condition :status])
        candidate {:proof-contract proof-contract
                   :rejected-app-diagnostic-records rejected-records
                   :diagnostic-preservation-record preservation
                   :accepted-app-execution-artifact accepted-app-artifact
                   :trusted-boundary-record trusted-boundary}
        diagnostics
        (p15-s23-rejected-app-proof-diagnostics source-path candidate)
        _ (when (seq diagnostics)
            (p15-s23-rejected-app-fail!
             (:diagnostic (first diagnostics)) source-path candidate
             {:diagnostics diagnostics}))
        proof-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :accepted-app-artifact
                       (:artifact-id accepted-app-artifact)
                       :rejected-diagnostics
                       (mapv :diagnostic rejected-records)
                       :proof-contract proof-contract})))
        rejected-candidate-records
        (p15-s23-rejected-app-rejected-records source-path candidate)
        artifact-base
        {:kind :gravity/p15-s23-rejected-app-diagnostic-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-rejected-app-diagnostic-proof
         :source-path source-path
         :proof-id proof-id
         :proof-contract proof-contract
         :accepted-app-execution-artifact
         (select-keys accepted-app-artifact
                      [:kind :artifact-id :proof-id
                       :accepted-app-artifact
                       :accepted-output-comparison
                       :runtime-capability-artifact
                       :trusted-boundary-record
                       :capability-based-proof])
         :rejected-app-diagnostic-records rejected-records
         :diagnostic-preservation-record preservation
         :trusted-boundary-record trusted-boundary
         :full-language-compiler-self-hosted?
         (get-in proof-contract
                 [:self-hosting-claims
                  :full-language-compiler-self-hosted?])
         :clojure-seed-retired?
         (get-in proof-contract
                 [:self-hosting-claims :clojure-seed-retired?])
         :verified-p15-s23-rejected-app-fixtures
         rejected-records
         :rejected-p15-s23-app-diagnostic-proof-fixtures
         rejected-candidate-records
         :p15-s23-rejected-app-diagnostic-stream
         (p15-s23-rejected-app-diagnostic-stream source-path proof-id)
         :p15-s23-rejected-app-diagnostic-results
         {:verified-rejected-fixtures (count rejected-records)
          :source-diagnostic-count
          (count (set (map :diagnostic rejected-records)))
          :diagnostic-count (count p15-s23-rejected-app-diagnostic-ids)
          :diagnostics (mapv :diagnostic rejected-records)
          :status :in-progress}
         :diagnostics []}
        proof (p15-s23-rejected-app-diagnostic-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))))

(defn p15-s23-rejected-app-diagnostic-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-rejected-app-fail!
     "P15S23E001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-with-artifact-build-context
   #(p15-s23-rejected-app-diagnostic-source-artifact path)))

(def p15-s23-reproducible-rebuild-stages
  [:compiler-source-inventory
   :compiler-pipeline-manifest
   :source-unit-and-syntax-serialization-proof
   :core-lowering-and-diagnostic-preservation-report
   :runtime-manifest-and-capability-enforcement-report
   :accepted-app-execution-proof
   :rejected-app-diagnostic-proof])

(def p15-s23-reproducible-rebuild-stage-commands
  {:compiler-source-inventory
   "p15-s23-compiler-source-inventory"
   :compiler-pipeline-manifest
   "p15-s23-compiler-pipeline-manifest"
   :source-unit-and-syntax-serialization-proof
   "p15-s23-source-syntax-serialization-proof"
   :core-lowering-and-diagnostic-preservation-report
   "p15-s23-core-lowering-diagnostic-preservation"
   :runtime-manifest-and-capability-enforcement-report
   "p15-s23-runtime-manifest-capability-enforcement"
   :accepted-app-execution-proof
   "p15-s23-accepted-app-execution"
   :rejected-app-diagnostic-proof
   "p15-s23-rejected-app-diagnostic"})

(def p15-s23-reproducible-rebuild-required-preserves
  #{:artifact-provenance :source-hash :proof-id :manifest-id
    :diagnostic-codes :rebuild-artifact-identity})

(def p15-s23-reproducible-rebuild-diagnostic-messages
  {"P15S23B001" "P15-S23 reproducible rebuild log is missing"
   "P15S23B002" "P15-S23 reproducible rebuild input set is incomplete"
   "P15S23B003" "P15-S23 rebuild produced nondeterministic artifact identity"
   "P15S23B004" "P15-S23 rebuild log is missing accepted/rejected app evidence links"
   "P15S23B005" "P15-S23 rebuild environment or provenance record is incomplete"
   "P15S23B006" "P15-S23 rebuild log makes an unsupported self-hosting or seed-retirement claim"})

(def p15-s23-reproducible-rebuild-diagnostic-ids
  ["P15S23B001" "P15S23B002" "P15S23B003"
   "P15S23B004" "P15S23B005" "P15S23B006"])

(defn p15-s23-reproducible-rebuild-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-reproducible-rebuild-diagnostic-messages
              id
              "P15-S23 reproducible rebuild proof failed")
         (merge {:source-span {:source source-path}
                 :stage :p15-s23-reproducible-rebuild-log
                 :diagnostic-family
                 :p15-s23-reproducible-rebuild-log
                 :value value
                 :remediation "Rebuild the current P15-S23 evidence bundle twice, compare artifact/proof/manifest identities, record the Clojure seed environment boundary, and keep self-hosting claims false until the full evidence bundle exists."}
                data)))

(defn p15-s23-reproducible-rebuild-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-reproducible-rebuild-log
   :source-span {:source source-path}
   :message (get p15-s23-reproducible-rebuild-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_reproducible_rebuild_log})

(defn p15-s23-rebuild-stage-artifact
  [source-path stage]
  (case stage
    :compiler-source-inventory
    (p15-s23-compiler-source-inventory-source-artifact source-path)
    :compiler-pipeline-manifest
    (p15-s23-compiler-pipeline-manifest-source-artifact source-path)
    :source-unit-and-syntax-serialization-proof
    (p15-s23-source-syntax-serialization-proof-source-artifact source-path)
    :core-lowering-and-diagnostic-preservation-report
    (p15-s23-core-lowering-diagnostic-preservation-source-artifact
     source-path)
    :runtime-manifest-and-capability-enforcement-report
    (p15-s23-runtime-manifest-capability-enforcement-source-artifact
     source-path)
    :accepted-app-execution-proof
    (p15-s23-accepted-app-execution-source-artifact source-path)
    :rejected-app-diagnostic-proof
    (p15-s23-rejected-app-diagnostic-source-artifact source-path)))

(defn p15-s23-rebuild-stage-record
  [source-path stage]
  (let [artifact (p15-s23-rebuild-stage-artifact source-path stage)]
    {:stage stage
     :kind (:kind artifact)
     :artifact-id (:artifact-id artifact)
     :proof-id (:proof-id artifact)
     :manifest-id (:manifest-id artifact)
     :serialization-id (get-in artifact
                               [:serialization-roundtrip-record
                                :serialization-id])
     :diagnostics
     (or (get-in artifact
                 [:p15-s23-rejected-app-diagnostic-results
                  :diagnostics])
         (mapv :diagnostic (:diagnostics artifact)))
     :full-language-compiler-self-hosted?
     (:full-language-compiler-self-hosted? artifact)
     :clojure-seed-retired? (:clojure-seed-retired? artifact)}))

(defn p15-s23-rebuild-record
  [source-path pass]
  {:artifact :gravity/p15-s23-rebuild-record
   :pass pass
   :source-path source-path
   :source-id (str "sha256:" (sha256-hex (slurp source-path)))
   :stages
   (mapv #(p15-s23-rebuild-stage-record source-path %)
         p15-s23-reproducible-rebuild-stages)
   :status :complete})