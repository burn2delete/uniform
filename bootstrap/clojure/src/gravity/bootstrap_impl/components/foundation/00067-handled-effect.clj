

(defn handled-effect?
  [ctx effect]
  (contains? (:handler-covered-effects @ctx) effect))

(defn handled-capability?
  [ctx capability]
  (contains? (:handler-covered-capabilities @ctx) capability))

(defn check-effect-registry!
  [node effect]
  (when-not (effect-registry-entry effect)
    (typed-diagnostic! "L6-EFFECT-UNKNOWN"
                       "effect kind is unknown or lacks governance registration"
                       node
                       "Register the effect with profile legality, capability requirements, ordering, and artifact representation."
                       {:effect effect})))

(defn check-build-effects!
  [checker ctx node effects opts]
  (doseq [effect effects
          :when (build-effect? effect)]
    (when-not (contains? (:build-grants @ctx) effect)
      (typed-diagnostic! (or (:build-grant-diagnostic opts)
                             "L6-BUILD-EFFECT")
                         "build-time effect is not granted by build policy"
                         node
                         "Grant the build effect in metadata or move the operation behind an approved build provider."
                         {:effect effect
                          :granted-build-effects (:build-grants @ctx)}))
    (record-checker! checker :build-effect-log
                     {:node-id (:node-id node)
                      :effect effect
                      :phase :build
                      :grant :metadata-build-grant
                      :status :granted})))

(defn check-replay-effects!
  [checker ctx node effects]
  (doseq [effect effects
          :when (and (replay-sensitive-effect? effect)
                     (active-profile-needs-replay? ctx))]
    (when-not (contains? (:replay-records @ctx) effect)
      (typed-diagnostic! "L6-REPLAY-EFFECT"
                         "replay-sensitive effect lacks a replay record"
                         node
                         "Record replay evidence for this nondeterministic effect or use a replay-aware handler."
                         {:effect effect
                          :profile (:profile @ctx)}))
    (record-checker! checker :replay-effect-log
                     {:node-id (:node-id node)
                      :effect effect
                      :profile (:profile @ctx)
                      :replay-record :metadata-replay-record
                      :mode :recorded})))

(defn check-effects-and-capabilities!
  ([checker ctx node effects capabilities]
   (check-effects-and-capabilities! checker ctx node effects capabilities {}))
  ([checker ctx node effects capabilities opts]
  (doseq [effect effects]
    (check-effect-registry! node effect)
    (when (contains? (get profile-denied-effects (:profile @ctx) #{}) effect)
      (typed-diagnostic! "L6-EFFECT-PROFILE"
                         "active profile rejects an inferred effect"
                         node
                         "Move the operation behind a profile-supported provider or remove the effect."
                         {:effect effect
                          :active-profile (:profile @ctx)}))
    (when (and (not (handled-effect? ctx effect))
               (not (contains? typed-internal-effects effect))
               (not (contains? (:declared-effects @ctx) effect)))
      (typed-diagnostic! "L6-EFFECT-UNDECLARED"
                         "inferred effect exceeds namespace declaration"
                         node
                         "Declare the effect in the namespace, function, package, and runtime policy or remove the operation."
                          {:effect effect
                           :declared-effects (:declared-effects @ctx)})))
  (check-build-effects! checker ctx node effects opts)
  (check-replay-effects! checker ctx node effects)
	  (doseq [capability capabilities]
	    (when (and (not (handled-capability? ctx capability))
	               (not (contains? (:declared-capabilities @ctx) capability)))
	      (typed-diagnostic! (or (:capability-diagnostic opts)
	                             "L15-CAPABILITY-MISSING")
                         "effect requires a capability that is not granted"
                         node
                         "Declare an explicit capability grant or use a handler with a structured fixture/replay capability."
                         {:requested-capability capability
                          :selected-or-missing-provider (provider-name capability)
                          :grant-id nil
                          :scope nil
                          :phase :runtime
	                          :active-profile (:profile @ctx)
	                          :target (:target @ctx)}))
	    (when (and (not (handled-capability? ctx capability))
	               (contains? (:declared-capabilities @ctx) capability)
	               (nil? (provider-name capability)))
	      (typed-diagnostic! "L15-PROVIDER-MISSING"
	                         "no provider implements the requested capability"
	                         node
	                         "Install or declare a provider that implements this capability for the active profile and target."
	                         {:requested-capability capability
	                          :selected-or-missing-provider nil
	                          :grant-id nil
	                          :scope nil
	                          :phase :runtime
	                          :active-profile (:profile @ctx)
	                          :target (:target @ctx)}))
	    (when-let [provider (provider-name capability)]
	      (when-not (contains? (get-in provider-specs [capability :profiles] #{})
	                           (:profile @ctx))
	        (when (true? (get-in @ctx [:provider-policy :strict-profile?]))
	          (typed-diagnostic! "L15-PROFILE"
	                             "selected provider is unsupported by the active profile or target"
	                             node
	                             "Select a provider whose declaration covers the active profile and target or reject the operation."
	                             {:requested-capability capability
	                              :selected-or-missing-provider provider
	                              :provider-profiles (get-in provider-specs [capability :profiles])
	                              :grant-id nil
	                              :scope nil
	                              :phase :runtime
	                              :active-profile (:profile @ctx)
	                              :target (:target @ctx)}))))
	    (when-not (handled-capability? ctx capability)
	      (record-checker! checker :provider-selection-records
	                       {:capability capability
	                        :provider (provider-name capability)
	                        :version (provider-version capability)
	                        :phase :runtime
	                        :selection :profile-default
	                        :selection-deterministic? true
	                        :scope :namespace
	                        :scope-kind (provider-scope-kind capability)
	                        :trust-level (provider-trust-level capability)
	                        :artifact-schema (provider-artifact-schema capability)
	                        :contracts (provider-contracts capability)
	                        :conformance-suite (provider-conformance-suite capability)
	                        :active-profile (:profile @ctx)
	                        :target (:target @ctx)})
	      (record-checker! checker :capability-records
	                       {:node-id (:node-id node)
	                        :capability capability
	                        :provider (provider-name capability)
	                        :provider-version (provider-version capability)
	                        :scope-kind (provider-scope-kind capability)
	                        :trust-level (provider-trust-level capability)
	                        :phase :runtime
	                        :status :granted})))))