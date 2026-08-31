

(defn p15-s23-compiler-source-inventory-source-artifact
  [source-path]
  (p15-s23-context-artifact
   :compiler-source-inventory source-path
   (fn [] (let [source-data (p15-s23-compiler-source-form-record source-path)
        compiler-stage
        (p15-s23-compiler-def-value source-path
                                     (:forms source-data)
                                     'p15-s23-compiler-stage)
        module-record
        (p15-s23-compiler-source-module-record source-path source-data
                                               compiler-stage)
        source-inventory (p15-s23-source-module-records compiler-stage)
        inventory-id
        (str "sha256:"
             (sha256-hex (pr-str {:compiler-source source-path
                                  :compiler-stage compiler-stage
                                  :source-inventory source-inventory})))
        rejected-records
        (p15-s23-compiler-source-rejected-records source-path)
        artifact-base
        {:kind :gravity/p15-s23-compiler-source-inventory-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-compiler-source-inventory
         :source-path source-path
         :inventory-id inventory-id
         :compiler-module module-record
         :compiler-stage compiler-stage
         :source-inventory source-inventory
         :full-language-compiler-self-hosted?
         (get-in compiler-stage
                 [:self-hosting-claims
                  :full-language-compiler-self-hosted?])
         :clojure-seed-retired?
         (get-in compiler-stage
                 [:self-hosting-claims :clojure-seed-retired?])
         :accepted-p15-s23-compiler-source-fixtures
         [{:fixture source-path
           :status :accepted
           :source-components (vec (sort p15-s23-compiler-source-components))
           :canonical-pipeline p15-s23-canonical-compiler-pipeline}]
         :rejected-p15-s23-compiler-source-fixtures rejected-records
         :p15-s23-compiler-source-inventory-diagnostic-stream
         (p15-s23-compiler-source-diagnostic-stream source-path
                                                    inventory-id)
         :p15-s23-compiler-source-inventory-results
         {:accepted-fixtures 1
          :rejected-fixtures (count rejected-records)
          :diagnostic-count
          (count p15-s23-compiler-source-inventory-diagnostic-ids)
          :source-component-count (count source-inventory)
          :status :in-progress}
         :diagnostics []}
        proof (p15-s23-compiler-source-inventory-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))))

(defn p15-s23-compiler-source-inventory-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-compiler-source-inventory-fail!
     "P15S23C001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-with-artifact-build-context
   #(p15-s23-compiler-source-inventory-source-artifact path)))

(def p15-s23-compiler-pipeline-manifest-required-preserves
  #{:source-spans :syntax-identity :diagnostic-codes
    :artifact-provenance :pipeline-stage-contracts
    :runtime-capability-manifest})

(def p15-s23-compiler-pipeline-manifest-diagnostic-messages
  {"P15S23M001" "P15-S23 compiler pipeline manifest is missing"
   "P15S23M002" "P15-S23 compiler pipeline manifest does not match the C1 canonical pipeline"
   "P15S23M003" "P15-S23 compiler pipeline manifest has incomplete pass contracts"
   "P15S23M004" "P15-S23 compiler pipeline manifest drops required preservation facts"
   "P15S23M005" "P15-S23 compiler pipeline manifest makes an unsupported self-hosting or seed-retirement claim"})

(def p15-s23-compiler-pipeline-manifest-diagnostic-ids
  ["P15S23M001" "P15S23M002" "P15S23M003" "P15S23M004"
   "P15S23M005"])

(defn p15-s23-compiler-pipeline-manifest-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-compiler-pipeline-manifest-diagnostic-messages
              id
              "P15-S23 compiler pipeline manifest failed")
         (merge {:source-span {:source source-path}
                 :stage :p15-s23-compiler-pipeline-manifest
                 :diagnostic-family
                 :p15-s23-compiler-pipeline-manifest
                 :value value
                 :remediation "Keep the P15-S23 compiler pipeline manifest in Gravity-owned source, preserve the C1 canonical stage order, provide pass contracts for every stage, and keep self-hosting claims false until the complete evidence bundle exists."}
                data)))

(defn p15-s23-compiler-pipeline-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-compiler-pipeline-manifest
   :source-span {:source source-path}
   :message (get p15-s23-compiler-pipeline-manifest-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_gravity_compiler_pipeline_manifest})

(defn p15-s23-pass-contract-complete?
  [contract]
  (and (keyword? (:stage contract))
       (contains? contract :input)
       (contains? contract :output)
       (seq (:preserves contract))
       (seq (:emits contract))))

(defn p15-s23-compiler-pipeline-manifest-diagnostics
  [source-path manifest]
  (let [pass-contracts (:pass-contracts manifest)
        pass-stages (mapv :stage pass-contracts)
        incomplete-contracts
        (vec (remove p15-s23-pass-contract-complete? pass-contracts))
        missing-preserves
        (set/difference
         p15-s23-compiler-pipeline-manifest-required-preserves
         (set (:preserves manifest)))
        claims (:self-hosting-claims manifest)]
    (vec
     (concat
      (when-not (= :gravity/compiler-pipeline-manifest
                   (:artifact manifest))
        [(p15-s23-compiler-pipeline-diagnostic-record
          source-path "P15S23M001" manifest
          {:missing-fields [:artifact]})])
      (when-not (= p15-s23-canonical-compiler-pipeline
                   (:pipeline manifest))
        [(p15-s23-compiler-pipeline-diagnostic-record
          source-path "P15S23M002" (:pipeline manifest)
          {:expected p15-s23-canonical-compiler-pipeline})])
      (when (or (not= p15-s23-canonical-compiler-pipeline pass-stages)
                (seq incomplete-contracts))
        [(p15-s23-compiler-pipeline-diagnostic-record
          source-path "P15S23M003" pass-contracts
          {:expected-stages p15-s23-canonical-compiler-pipeline
           :actual-stages pass-stages
           :incomplete-contracts incomplete-contracts})])
      (when (seq missing-preserves)
        [(p15-s23-compiler-pipeline-diagnostic-record
          source-path "P15S23M004" (:preserves manifest)
          {:missing-preserves (vec (sort missing-preserves))})])
      (when (or (true? (:full-language-compiler-self-hosted? claims))
                (true? (:clojure-seed-retired? claims)))
        [(p15-s23-compiler-pipeline-diagnostic-record
          source-path "P15S23M005" claims
          {:full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? claims)
           :clojure-seed-retired? (:clojure-seed-retired? claims)})])))))

(def p15-s23-compiler-pipeline-rejected-candidates
  [{:fixture :internal-p15-s23-pipeline-missing-manifest
    :candidate {}
    :expected-diagnostic "P15S23M001"}
   {:fixture :internal-p15-s23-pipeline-mismatch
    :candidate {:artifact :gravity/compiler-pipeline-manifest
                :pipeline [:read-source]
                :pass-contracts []
                :preserves p15-s23-compiler-pipeline-manifest-required-preserves
                :self-hosting-claims
                {:full-language-compiler-self-hosted? false
                 :clojure-seed-retired? false}}
    :expected-diagnostic "P15S23M002"}
   {:fixture :internal-p15-s23-pipeline-pass-contract-gap
    :candidate {:artifact :gravity/compiler-pipeline-manifest
                :pipeline p15-s23-canonical-compiler-pipeline
                :pass-contracts
                [{:stage :read-source
                  :input :source-bytes}]
                :preserves p15-s23-compiler-pipeline-manifest-required-preserves
                :self-hosting-claims
                {:full-language-compiler-self-hosted? false
                 :clojure-seed-retired? false}}
    :expected-diagnostic "P15S23M003"}
   {:fixture :internal-p15-s23-pipeline-preservation-gap
    :candidate {:artifact :gravity/compiler-pipeline-manifest
                :pipeline p15-s23-canonical-compiler-pipeline
                :pass-contracts
                (mapv (fn [stage]
                        {:stage stage
                         :input :input
                         :output :output
                         :preserves [:source-spans]
                         :emits [:diagnostics]})
                      p15-s23-canonical-compiler-pipeline)
                :preserves [:source-spans]
                :self-hosting-claims
                {:full-language-compiler-self-hosted? false
                 :clojure-seed-retired? false}}
    :expected-diagnostic "P15S23M004"}
   {:fixture :internal-p15-s23-pipeline-overclaim
    :candidate {:artifact :gravity/compiler-pipeline-manifest
                :pipeline p15-s23-canonical-compiler-pipeline
                :pass-contracts
                (mapv (fn [stage]
                        {:stage stage
                         :input :input
                         :output :output
                         :preserves [:source-spans]
                         :emits [:diagnostics]})
                      p15-s23-canonical-compiler-pipeline)
                :preserves
                p15-s23-compiler-pipeline-manifest-required-preserves
                :self-hosting-claims
                {:full-language-compiler-self-hosted? true
                 :clojure-seed-retired? true}}
    :expected-diagnostic "P15S23M005"}])

(defn p15-s23-compiler-pipeline-rejected-records
  [source-path]
  (mapv (fn [{:keys [fixture candidate expected-diagnostic]}]
          {:fixture fixture
           :status :rejected
           :expected-diagnostic expected-diagnostic
           :diagnostics
           (p15-s23-compiler-pipeline-manifest-diagnostics
            source-path candidate)})
        p15-s23-compiler-pipeline-rejected-candidates))