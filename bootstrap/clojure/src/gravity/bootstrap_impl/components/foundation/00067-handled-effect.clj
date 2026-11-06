

(defn handled-effect?
  [ctx effect]
  (gravity.effects/native-handled-effect? ctx effect))

(defn handled-capability?
  [ctx capability]
  (contains? (:handler-covered-capabilities @ctx) capability))

(defn check-effect-registry!
  [node effect]
  (gravity.effects/check-native-registry!
   {:registry-entry #'effect-registry-entry
    :diagnostic! #'typed-diagnostic!} node effect))

(defn check-build-effects!
  [checker ctx node effects opts]
  (gravity.effects/check-native-build-effects!
   {:build-effect? #'build-effect?
    :diagnostic! #'typed-diagnostic!
    :record! #'record-checker!} checker ctx node effects opts))

(defn check-replay-effects!
  [checker ctx node effects]
  (gravity.effects/check-native-replay-effects!
   {:replay-sensitive-effect? #'replay-sensitive-effect?
    :needs-replay? #'active-profile-needs-replay?
    :diagnostic! #'typed-diagnostic!
    :record! #'record-checker!} checker ctx node effects))

(defn check-effects-and-capabilities!
  ([checker ctx node effects capabilities]
   (check-effects-and-capabilities! checker ctx node effects capabilities {}))
  ([checker ctx node effects capabilities opts]
  (gravity.effects/check-native-effect-legality!
   {:check-registry! #'check-effect-registry!
    :profile-denied-effects #'profile-denied-effects
    :diagnostic! #'typed-diagnostic!
    :handled-effect? #'handled-effect?
    :internal-effects #'typed-internal-effects
    :check-build-effects! #'check-build-effects!
    :check-replay-effects! #'check-replay-effects!}
   checker ctx node effects opts)
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