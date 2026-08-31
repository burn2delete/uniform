(ns gravity.module-analysis.module)

(defn parse-module
  [{:keys [require-ns parse-clause single-clause-value clause-args
           parse-dependencies fail! source-span known-source-profiles
           supported-profiles supported-targets bootstrap-target-supported?]}
   source-path forms]
  (let [ns-form (require-ns source-path forms)
        module-name (second ns-form)
        clauses (map #(parse-clause source-path %) (drop 2 ns-form))
        clause-map (reduce (fn [acc [k v]] (update acc k (fnil conj []) v))
                           {} clauses)
        active-profile-values (get clause-map :profile)
        library-profile-values (get clause-map :profiles)
        profile (first (single-clause-value source-path clause-map :profile true))
        target (or (first (single-clause-value source-path clause-map :target false))
                   :jvm)
        effects (or (first (single-clause-value source-path clause-map :effects false))
                    #{})
        capabilities (or (first (single-clause-value source-path clause-map
                                                     :capabilities false))
                         #{})
        requires (parse-dependencies source-path :require
                                     (clause-args clause-map :requires))
        imports (parse-dependencies source-path :import
                                    (clause-args clause-map :imports))
        exports (or (first (single-clause-value source-path clause-map :exports false))
                    [])
        safety (or (first (single-clause-value source-path clause-map :safety false))
                   :safe)
        providers (or (first (single-clause-value source-path clause-map
                                                  :providers false))
                      [])
        metadata (or (first (single-clause-value source-path clause-map :metadata false))
                     {})
        docs (or (first (single-clause-value source-path clause-map :doc false)) nil)]
    (when (or (> (count active-profile-values) 1)
              (and (seq active-profile-values) (seq library-profile-values))
              (seq library-profile-values))
      (fail! "L3-PROFILE-MULTIPLE"
             "stage0 implementation namespaces must declare exactly one active profile"
             {:source-span (source-span source-path 0)
              :profile-clauses active-profile-values
              :profiles-clauses library-profile-values
              :remediation "Use one (:profile p) clause for stage0 implementation modules."}))
    (when-not (symbol? module-name)
      (fail! "L3-NS-MISSING" "namespace name must be a symbol"
             {:source-span (source-span source-path 0)
              :remediation "Use a symbolic namespace name, for example hello.main."}))
    (when-not (contains? known-source-profiles profile)
      (fail! "P1-PROFILE-UNSUPPORTED"
             "stage0 bootstrap does not know this source profile"
             {:source-span (source-span source-path 0)
              :profile profile :known known-source-profiles
              :supported supported-profiles
              :remediation "Use a known source profile such as :hosted, :core, or :kernel."}))
    (when-not (bootstrap-target-supported? target)
      (fail! "B1-TARGET-UNSUPPORTED"
             "stage0 bootstrap does not support this requested target"
             {:source-span (source-span source-path 0)
              :target target :supported supported-targets
              :remediation "Use a target enabled by the selected bootstrap backend."}))
    {:module module-name :source-path source-path :profile profile :target target
     :effects effects :capabilities capabilities :requires requires
     :imports imports :exports exports :safety safety :providers providers
     :metadata metadata :doc docs :forms (vec (rest forms))}))
