

(def perf1-required-claim-fields
  [:claim-id :profile :target :target-features :input-shape :layout
   :safety-mode :benchmark])

(defn perf-present?
  [value]
  (and (some? value)
       (not (and (coll? value) (empty? value)))))

(defn perf1-normalize-claim
  [claim]
  (let [claim-id (or (:claim-id claim) (:claim claim))
        input-shape (or (:input-shape claim) (:input-domain claim))]
    (assoc claim
           :claim-id claim-id
           :input-shape input-shape)))

(defn perf1-missing-claim-fields
  [claim]
  (vec (remove #(perf-present? (get claim %))
               perf1-required-claim-fields)))

(defn perf1-fail!
  [id source-path manifest claim extra]
  (fail! id
         (case id
           "PERF1-CLAIM" "performance claim is incomplete"
           "PERF1-EVIDENCE" "performance claim is missing required evidence"
           "PERF1-SAFETY" "optimization loses required safety evidence"
           "PERF1-PROFILE" "optimization introduces profile-illegal behavior"
           "PERF1-EFFECT" "optimization hides or expands effects"
           "PERF1-CAPABILITY" "optimization hides or expands capability authority"
           "PERF1-NUMERIC" "optimization changes numeric mode without evidence"
           "PERF1-TARGET" "performance claim lacks target feature or fingerprint evidence"
           "PERF1-VARIANT" "generated variant lacks guard predicate evidence"
           "performance claim is invalid")
         (merge {:source-span {:source source-path}
                 :profile (:profile manifest)
                 :target (:target manifest)
                 :claim-id (:claim-id claim)
                 :target-request (:target claim)
                 :diagnostic-family :performance-claim-validation}
                extra)))

(defn perf1-target-fingerprint
  [claim]
  (or (get-in claim [:benchmark :target-fingerprint])
      (get-in claim [:benchmark :environment-fingerprint])))

(defn perf1-validate-claim!
  [source-path manifest performance claim]
  (let [missing-fields (perf1-missing-claim-fields claim)
        target-features (set (:target-features claim))
        target-fingerprint (perf1-target-fingerprint claim)
        semantic-proof (set (:semantic-proof claim))
        safety-proof (set (:safety-proof claim))
        artifacts (set (:artifacts claim))
        lost-safety-facts (set (:lost-safety-facts performance))
        expanded-effects (set (:expanded-effects performance))
        expanded-capabilities (set (:expanded-capabilities performance))
        numeric-change (:numeric-mode-change performance)
        variants (vec (:generated-variants performance))]
    (when (seq missing-fields)
      (perf1-fail! "PERF1-CLAIM" source-path manifest claim
                   {:missing-fields missing-fields
                    :remediation "Record profile, target, feature, input, layout, safety, and benchmark fields in the performance claim."}))
    (when-not (= (:profile manifest) (:profile claim))
      (perf1-fail! "PERF1-PROFILE" source-path manifest claim
                   {:active-profile (:profile manifest)
                    :requested-profile (:profile claim)
                    :remediation "The performance claim profile must match the already validated source profile."}))
    (when (or (empty? target-features)
              (not (perf-present? target-fingerprint)))
      (perf1-fail! "PERF1-TARGET" source-path manifest claim
                   {:target-features target-features
                    :target-fingerprint target-fingerprint
                    :remediation "Record target features and benchmark target fingerprint evidence."}))
    (when (or (empty? semantic-proof)
              (empty? safety-proof)
              (not (contains? artifacts :optimized-mir))
              (not (contains? artifacts :proof-index))
              (not (contains? artifacts :benchmark-report)))
      (perf1-fail! "PERF1-EVIDENCE" source-path manifest claim
                   {:missing-evidence
                    (cond-> #{}
                      (empty? semantic-proof) (conj :semantic-proof)
                      (empty? safety-proof) (conj :safety-proof)
                      (not (contains? artifacts :optimized-mir))
                      (conj :optimized-mir)
                      (not (contains? artifacts :proof-index))
                      (conj :proof-index)
                      (not (contains? artifacts :benchmark-report))
                      (conj :benchmark-report))
                    :remediation "Attach semantic proof, safety proof, optimized MIR, proof index, and benchmark report evidence."}))
    (when (seq lost-safety-facts)
      (perf1-fail! "PERF1-SAFETY" source-path manifest claim
                   {:lost-safety-facts lost-safety-facts
                    :remediation "Preserve, regenerate, or retain runtime checks for every safety fact."}))
    (when (seq expanded-effects)
      (perf1-fail! "PERF1-EFFECT" source-path manifest claim
                   {:expanded-effects expanded-effects
                    :declared-effects (:source-effects manifest)
                    :remediation "Optimization must not hide or expand effects outside the validated manifest."}))
    (when (seq expanded-capabilities)
      (perf1-fail! "PERF1-CAPABILITY" source-path manifest claim
                   {:expanded-capabilities expanded-capabilities
                    :declared-capabilities (:source-capabilities manifest)
                    :remediation "Optimization must not hide or expand authority outside the validated manifest."}))
    (when (and (:source-mode numeric-change)
               (:optimized-mode numeric-change)
               (not= (:source-mode numeric-change)
                     (:optimized-mode numeric-change))
               (not (perf-present? (:certificate numeric-change))))
      (perf1-fail! "PERF1-NUMERIC" source-path manifest claim
                   {:source-numeric-mode (:source-mode numeric-change)
                    :optimized-numeric-mode (:optimized-mode numeric-change)
                    :certificate-id (:certificate numeric-change)
                    :remediation "Attach an explicit math or numeric certificate for mode changes."}))
    (when-let [bad-variant (first (filter #(not (perf-present?
                                                 (:guard-predicate %)))
                                          variants))]
      (perf1-fail! "PERF1-VARIANT" source-path manifest claim
                   {:generated-variant (:variant-id bad-variant)
                    :missing-fact :guard-predicate
                    :remediation "Generated fast paths and variants require guard predicates and compatibility evidence."}))
    :complete))