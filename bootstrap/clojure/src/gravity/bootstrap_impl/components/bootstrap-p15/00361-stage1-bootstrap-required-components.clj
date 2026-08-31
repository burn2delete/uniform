

(def stage1-bootstrap-required-components
  #{:reader :syntax :diagnostics :source-frontend :syntax-object-model
    :macro-expansion :name-resolution
    :core-semantics
    :core-lowering
    :type-checker
    :effect-checker
    :ownership-checker
    :safety-analysis
    :mir-specification
    :domain-ir-architecture
    :mir-optimization
    :target-lowering
    :compiler-diagnostics
    :incremental-compilation
    :compiler-plugin-pass-api
    :compiler-verification
    :backend-interface
    :c-backend
    :llvm-backend
    :wasm-backend
    :jvm-backend
    :js-ts-backend
    :mlir-backend
    :gpu-backend
    :hdl-backend
    :workflow-backend
    :query-backend
    :mobile-backend})

(def stage1-bootstrap-required-preserved-facts
  #{:source-spans :syntax-identity :diagnostic-codes
    :artifact-provenance})

(def stage1-bootstrap-documents
  ["BOOT1" "BOOT2" "BOOT3" "BOOT4" "BOOT5" "BOOT6" "BOOT7" "BOOT8"
   "L1" "L2" "C2" "C3" "C4" "C5" "C6" "C7" "C8" "C9" "C10"
   "C11" "C12" "C13" "C14" "C15" "C16" "C17" "C18"
   "B1" "B2" "B3" "B4" "B5" "B6" "B7" "B8" "B9" "B10" "B11"
   "B12"])

(def stage1-bootstrap-governing-documents
  {"BOOT1" "docs/phase-15-bootstrap-and-self-hosting/203-boot1-bootstrap-strategy.md"
   "BOOT2" "docs/phase-15-bootstrap-and-self-hosting/204-boot2-seed-compiler-design.md"
   "BOOT3" "docs/phase-15-bootstrap-and-self-hosting/205-boot3-self-hosted-compiler-plan.md"
   "BOOT4" "docs/phase-15-bootstrap-and-self-hosting/206-boot4-compiler-in-gravity-coding-standard.md"
   "BOOT5" "docs/phase-15-bootstrap-and-self-hosting/207-boot5-stage-compatibility-matrix.md"
   "BOOT6" "docs/phase-15-bootstrap-and-self-hosting/208-boot6-trusting-trust-and-reproducible-bootstrap-plan.md"
   "BOOT7" "docs/phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md"
   "BOOT8" "docs/phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md"
   "L1" "docs/phase-01-core-language/011-l1-surface-syntax-specification.md"
   "L2" "docs/phase-01-core-language/012-l2-core-language-semantics.md"
   "C2" "docs/phase-06-compiler-architecture/081-c2-reader-implementation-design.md"
   "C3" "docs/phase-06-compiler-architecture/082-c3-syntax-object-model.md"
   "C4" "docs/phase-06-compiler-architecture/083-c4-macro-expansion-engine-design.md"
   "C5" "docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md"
   "C6" "docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md"
   "C7" "docs/phase-06-compiler-architecture/086-c7-type-checker-design.md"
   "C8" "docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md"
   "C9" "docs/phase-06-compiler-architecture/088-c9-ownership-lifetime-and-region-checker-design.md"
   "C10" "docs/phase-06-compiler-architecture/089-c10-safety-analysis-pipeline-design.md"
   "C11" "docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md"
   "C12" "docs/phase-06-compiler-architecture/091-c12-domain-ir-architecture.md"
   "C13" "docs/phase-06-compiler-architecture/092-c13-mir-optimization-passes-design.md"
   "C14" "docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md"
   "C15" "docs/phase-06-compiler-architecture/094-c15-compiler-diagnostics-specification.md"
   "C16" "docs/phase-06-compiler-architecture/095-c16-incremental-compilation-design.md"
   "C17" "docs/phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md"
   "C18" "docs/phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md"
   "B1" "docs/phase-07-backend-architecture/098-b1-backend-interface-specification.md"
   "B2" "docs/phase-07-backend-architecture/099-b2-c-backend-design.md"
   "B3" "docs/phase-07-backend-architecture/100-b3-llvm-backend-design.md"
   "B4" "docs/phase-07-backend-architecture/101-b4-wasm-backend-design.md"
   "B5" "docs/phase-07-backend-architecture/102-b5-jvm-backend-design.md"
   "B6" "docs/phase-07-backend-architecture/103-b6-javascript-typescript-backend-design.md"
   "B7" "docs/phase-07-backend-architecture/104-b7-mlir-backend-design.md"
   "B8" "docs/phase-07-backend-architecture/105-b8-gpu-backend-design.md"
   "B9" "docs/phase-07-backend-architecture/106-b9-hdl-backend-design.md"
   "B10" "docs/phase-07-backend-architecture/107-b10-workflow-graph-backend-design.md"
   "B11" "docs/phase-07-backend-architecture/108-b11-query-relational-backend-design.md"
   "B12" "docs/phase-07-backend-architecture/109-b12-mobile-backend-design.md"})

(def stage1-bootstrap-diagnostic-messages
  {"STAGE1001" "stage1 Gravity bootstrap source must declare Gravity ownership"
   "STAGE1002" "stage1 Gravity bootstrap source must use the meta profile and stage1 marker"
   "STAGE1003" "stage1 Gravity bootstrap source must deny ambient authority"
   "STAGE1004" "stage1 Gravity bootstrap source must record seed and stage lineage"
   "STAGE1005" "stage1 Gravity bootstrap source must preserve required compiler facts"
   "STAGE1006" "stage1 Gravity bootstrap source set is incomplete"})

(def stage1-bootstrap-diagnostic-ids
  ["STAGE1001" "STAGE1002" "STAGE1003"
   "STAGE1004" "STAGE1005" "STAGE1006"])

(def stage1-bootstrap-rejected-fixture-records
  [{:fixture "bootstrap/clojure/fixtures/rejected/stage1-bootstrap-missing-owner.gravity"
    :diagnostic "STAGE1001"
    :rejected-behavior :missing-gravity-source-owner}
   {:fixture "bootstrap/clojure/fixtures/rejected/stage1-bootstrap-wrong-profile.gravity"
    :diagnostic "STAGE1002"
    :rejected-behavior :wrong-bootstrap-profile}
   {:fixture "bootstrap/clojure/fixtures/rejected/stage1-bootstrap-ambient-authority.gravity"
    :diagnostic "STAGE1003"
    :rejected-behavior :ambient-authority}
   {:fixture "bootstrap/clojure/fixtures/rejected/stage1-bootstrap-missing-lineage.gravity"
    :diagnostic "STAGE1004"
    :rejected-behavior :missing-bootstrap-lineage}
   {:fixture "bootstrap/clojure/fixtures/rejected/stage1-bootstrap-missing-preserved-fact.gravity"
    :diagnostic "STAGE1005"
    :rejected-behavior :missing-preserved-fact}
   {:fixture "bootstrap/clojure/fixtures/rejected/stage1-bootstrap-incomplete-source-set.gravity"
    :diagnostic "STAGE1006"
    :rejected-behavior :incomplete-source-set}])

(defn stage1-bootstrap-fail!
  [id source-path value data]
  (fail! id
         (get stage1-bootstrap-diagnostic-messages
              id
              "stage1 Gravity bootstrap source is invalid")
         (merge {:source-span {:source source-path}
                 :stage :stage1-bootstrap-source
                 :diagnostic-family :stage1-bootstrap-source
                 :value value
                 :remediation "Keep stage1 bootstrap source in Gravity, keep the Clojure seed explicit, deny ambient authority, preserve compiler facts, and include the reader, syntax, diagnostics, source-frontend, syntax-object-model, and compiler-diagnostics components."}
                data)))

(defn stage1-bootstrap-source-files
  [path]
  (let [root (java.io.File. path)]
    (cond
      (.isDirectory root)
      (let [files (->> (file-seq root)
                       (filter #(.isFile %))
                       (filter #(qst-or-gravity-source? (.getName %)))
                       (sort-by #(.getPath %))
                       (mapv #(.getPath %)))]
        (when-not (seq files)
          (stage1-bootstrap-fail! "STAGE1006" path nil
                                  {:missing-fields [:gravity-source-files]}))
        files)

      (.isFile root) [(.getPath root)]

      :else
      (stage1-bootstrap-fail! "STAGE1006" path nil
                              {:missing-fields [:gravity-source-root]}))))

(defn stage1-bootstrap-module-metadata
  [source-path module]
  (let [metadata (get-in module [:metadata :bootstrap])]
    (when-not (map? metadata)
      (stage1-bootstrap-fail! "STAGE1001" source-path metadata
                              {:module (:module module)
                               :missing-fields [:metadata :bootstrap]}))
    metadata))

(defn stage1-bootstrap-validate-module!
  [source-path module metadata]
  (when-not (= :gravity-source (:owner metadata))
    (stage1-bootstrap-fail! "STAGE1001" source-path metadata
                            {:module (:module module)
                             :missing-fields [:owner]}))
  (when-not (and (= :meta (:profile module))
                 (= :stage1 (:stage metadata)))
    (stage1-bootstrap-fail! "STAGE1002" source-path metadata
                            {:module (:module module)
                             :profile (:profile module)
                             :expected-profile :meta
                             :missing-fields [:stage]}))
  (when-not (and (empty? (:effects module))
                 (empty? (:capabilities module))
                 (true? (:ambient-authority-denied metadata)))
    (stage1-bootstrap-fail! "STAGE1003" source-path metadata
                            {:module (:module module)
                             :denied-effects (:effects module)
                             :denied-capabilities (:capabilities module)
                             :missing-fields [:ambient-authority-denied]}))
  (when-not (and (= :gravity (:source-language metadata))
                 (= :clojure-stage0 (:seed metadata))
                 (= :replace-clojure-seed
                    (:retirement-objective metadata))
                 (= :clojure-stage0 (get-in metadata
                                             [:lineage :verified-by]))
                 (= :stage1 (get-in metadata [:lineage :next-stage])))
    (stage1-bootstrap-fail! "STAGE1004" source-path metadata
                            {:module (:module module)
                             :missing-fields [:source-language :seed
                                              :retirement-objective
                                              :lineage]}))
  (when-not (set/subset? stage1-bootstrap-required-preserved-facts
                         (set (:preserves metadata)))
    (stage1-bootstrap-fail! "STAGE1005" source-path metadata
                            {:module (:module module)
                             :missing-fields [:preserves]
                             :missing-fact
                             (vec (sort (set/difference
                                          stage1-bootstrap-required-preserved-facts
                                          (set (:preserves metadata)))))}))
  :complete)

(defn stage1-bootstrap-module-record
  [source-path]
  (let [source-text (slurp source-path)
        records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        syntax (syntax-object-stream source-path records module)
        metadata (stage1-bootstrap-module-metadata source-path module)
        _ (stage1-bootstrap-validate-module! source-path module metadata)]
    {:module (:module module)
     :source-path source-path
     :source-language (:source-language metadata)
     :profile (:profile module)
     :target (:target module)
     :effects (:effects module)
     :capabilities (:capabilities module)
     :safety (:safety module)
     :component (:component metadata)
     :implements (vec (:implements metadata))
     :preserves (vec (sort (:preserves metadata)))
     :documents (vec (:documents metadata))
     :lineage (:lineage metadata)
     :definitions (definition-table syntax module)
     :form-count (count forms)
     :syntax-object-count (count syntax)
     :source-hash (str "sha256:" (sha256-hex source-text))
     :metadata metadata}))