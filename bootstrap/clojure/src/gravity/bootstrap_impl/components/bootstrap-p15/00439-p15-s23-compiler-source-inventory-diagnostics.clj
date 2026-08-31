

(defn p15-s23-compiler-source-inventory-diagnostics
  [source-path compiler-stage]
  (let [source-modules (:source-modules compiler-stage)
        source-components (set (map :component source-modules))
        source-paths (set (map :path source-modules))
        missing-source-components
        (set/difference p15-s23-compiler-source-components
                        source-components)
        missing-source-files
        (vec (remove #(p15-s23-source-module-present? %)
                     source-modules))
        evidence-keys (set (:required-evidence compiler-stage))
        missing-evidence
        (set/difference (p15-s23-required-evidence-keys) evidence-keys)
        claims (:self-hosting-claims compiler-stage)]
    (vec
     (concat
      (when-not (= :gravity-p15-s23-compiler-source-inventory-v1
                   (:engine compiler-stage))
        [(p15-s23-compiler-source-diagnostic-record
          source-path "P15S23C001" compiler-stage
          {:missing-fields [:engine]})])
      (when-not (= p15-s23-canonical-compiler-pipeline
                   (:canonical-pipeline compiler-stage))
        [(p15-s23-compiler-source-diagnostic-record
          source-path "P15S23C002" (:canonical-pipeline compiler-stage)
          {:expected p15-s23-canonical-compiler-pipeline})])
      (when (or (seq missing-source-components)
                (seq missing-source-files)
                (not (contains? source-paths p15-s23-compiler-source-path)))
        [(p15-s23-compiler-source-diagnostic-record
          source-path "P15S23C003" source-modules
          {:missing-components (vec (sort missing-source-components))
           :missing-files missing-source-files
           :required-compiler-source p15-s23-compiler-source-path})])
      (when (seq missing-evidence)
        [(p15-s23-compiler-source-diagnostic-record
          source-path "P15S23C004" (:required-evidence compiler-stage)
          {:missing-evidence (vec (sort missing-evidence))})])
      (when (or (true? (:full-language-compiler-self-hosted? claims))
                (true? (:clojure-seed-retired? claims))
                (false? (get-in compiler-stage
                                [:seed-boundary :still-trusted?])))
        [(p15-s23-compiler-source-diagnostic-record
          source-path "P15S23C005" claims
          {:full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? claims)
           :clojure-seed-retired? (:clojure-seed-retired? claims)
           :seed-boundary (:seed-boundary compiler-stage)})])))))

(defn p15-s23-compiler-source-module-record
  [source-path source-data compiler-stage]
  (let [{:keys [source-text records forms module]} source-data
        metadata (stage1-bootstrap-module-metadata source-path module)]
    (when-not (and (= :gravity-source (:owner metadata))
                   (= :gravity (:source-language metadata))
                   (= :clojure-stage0 (:seed metadata))
                   (= :p15-s23 (:stage metadata))
                   (= :compiler-source-inventory (:component metadata))
                   (= :meta (:profile module))
                   (empty? (:effects module))
                   (empty? (:capabilities module))
                   (true? (:ambient-authority-denied metadata)))
      (p15-s23-compiler-source-inventory-fail!
       "P15S23C001" source-path metadata
       {:missing-fields [:metadata :bootstrap]}))
    (let [diagnostics
          (p15-s23-compiler-source-inventory-diagnostics
           source-path compiler-stage)]
      (when (seq diagnostics)
        (p15-s23-compiler-source-inventory-fail!
         (:diagnostic (first diagnostics)) source-path compiler-stage
         {:diagnostics diagnostics}))
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
       :definitions (definition-table
                     (syntax-object-stream source-path records module)
                     module)
       :form-count (count forms)
       :source-hash (str "sha256:" (sha256-hex source-text))
       :metadata metadata})))

(defn p15-s23-source-module-records
  [compiler-stage]
  (mapv (fn [{:keys [component path]}]
          (let [source-text (slurp path)]
            {:component component
             :path path
             :source-hash (str "sha256:" (sha256-hex source-text))
             :status :present}))
        (:source-modules compiler-stage)))

(defn p15-s23-compiler-source-diagnostic-stream
  [source-path inventory-id]
  {:artifact :gravity/p15-s23-compiler-source-inventory-diagnostic-stream
   :stage :p15-s23-compiler-source-inventory
   :source-path source-path
   :inventory-id inventory-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p15-s23-compiler-source-inventory
            :message
            (get p15-s23-compiler-source-inventory-diagnostic-messages id)})
         p15-s23-compiler-source-inventory-diagnostic-ids)
   :status :complete})