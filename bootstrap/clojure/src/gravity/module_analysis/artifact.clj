(ns gravity.module-analysis.artifact)

(defn module-source-artifact-from-records
  [{:keys [validate-ns-syntax! parse-module syntax-object-stream
           assert-unique-aliases! assert-referred-names-unambiguous!
           assert-profile-boundaries! assert-qualified-symbols-resolve!
           infer-effects assert-namespace-effect-and-capability!
           definition-table sha256-hex required-capabilities-for-effects]}
   source-path source-text records]
  (let [forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        syntax (syntax-object-stream source-path records module)
        dependencies (vec (concat (:requires module) (:imports module)))
        _ (assert-unique-aliases! source-path dependencies)
        _ (assert-referred-names-unambiguous! source-path dependencies)
        _ (assert-profile-boundaries! source-path module dependencies)
        _ (assert-qualified-symbols-resolve! source-path (:forms module)
                                             module dependencies)
        inferred-effects (infer-effects (:forms module))
        _ (assert-namespace-effect-and-capability! source-path module
                                                   inferred-effects)
        definitions (definition-table syntax module)
        source-hash (str "sha256:" (sha256-hex source-text))
        definitions-hash (str "sha256:" (sha256-hex (pr-str definitions)))
        dependency-records
        (mapv #(select-keys % [:kind :module :alias :profile :boundary
                               :effects :capabilities])
              dependencies)
        public-api (filterv #(= :public (:visibility %)) definitions)]
    {:kind :gravity/stage0-module-artifact
     :pass {:name :namespace-analyzer
            :input :syntax-object-stream
            :output :module-artifact
            :requires [:reader]
            :preserves [:source-spans :profile :target :effects :capabilities]
            :rejects ["L3-NS-MISSING" "L3-PROFILE-MULTIPLE" "L3-UNKNOWN-ALIAS"
                      "L3-AMBIGUOUS-NAME" "L3-PRIVATE-IMPORT" "L3-CROSS-PROFILE"
                      "L3-EFFECT-WIDEN" "L3-CAPABILITY-MISSING"]}
     :namespace-table [{:name (:module module)
                        :package (get-in module [:metadata :package])
                        :profile (:profile module)
                        :target (:target module)
                        :source-path source-path
                        :safety (:safety module)
                        :metadata (:metadata module)}]
     :alias-table (mapv (fn [dependency]
                          {:alias (:alias dependency)
                           :module (:module dependency)
                           :kind (:kind dependency)
                           :profile (:profile dependency)})
                        (filter :alias dependencies))
     :import-export-table {:requires (:requires module)
                           :imports (:imports module)
                           :exports (:exports module)}
     :module-dependency-graph {:module (:module module)
                               :dependencies dependency-records
                               :acyclic true}
     :namespace-effect-summary {:declared (:effects module)
                                :inferred inferred-effects}
     :namespace-capability-summary
     {:declared (:capabilities module)
      :required (required-capabilities-for-effects inferred-effects)}
     :profile-boundary-records
     (mapv (fn [dependency]
             {:module (:module dependency)
              :from-profile (:profile module)
              :to-profile (:profile dependency)
              :boundary (or (:boundary dependency)
                            (when (= :core (:profile dependency)) :pure-core))})
           (filter #(or (:boundary %)
                        (and (:profile %)
                             (not= (:profile %) (:profile module))))
                   dependencies))
     :module-artifact {:module (:module module)
                       :package (get-in module [:metadata :package])
                       :profile (:profile module)
                       :target (:target module)
                       :exports (:exports module)
                       :requires (mapv #(select-keys % [:module :profile :effects])
                                       (:requires module))
                       :imports (mapv #(select-keys % [:module :profile :effects
                                                       :boundary])
                                      (:imports module))
                       :effects (:effects module)
                       :capabilities (:capabilities module)
                       :safety (:safety module)
                       :source-hash source-hash
                       :definitions definitions-hash}
     :public-api-manifest {:module (:module module) :exports public-api}
     :definitions definitions
     :syntax-object-stream syntax
     :diagnostics []}))
