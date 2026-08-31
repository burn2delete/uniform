(ns gravity.profile-validation.policy
  (:require [clojure.set :as set]))

(defn profile-allowed-effects [effect-registry profile invoke]
  (->> effect-registry
       (keep (fn [[effect entry]]
               (when (or (contains? (:profiles entry #{}) profile)
                         (and (:requires-build-grant entry)
                              (contains? #{:meta :hosted} profile)))
                 effect)))
       (invoke :stable-set)))

(defn profile-capabilities [provider-specs profile invoke]
  (->> provider-specs
       (keep (fn [[capability spec]]
               (when (contains? (:profiles spec #{}) profile)
                 capability)))
       (invoke :stable-set)))

(defn profile-contract
  [profile allowed-effects-fn all-effects-fn capabilities-fn operation-value
   invoke]
  (let [allowed-effects (allowed-effects-fn profile)
        effect-registry (operation-value :effect-registry)
        registered-effects (all-effects-fn)]
    {:profile profile
     :allowed-forms (operation-value :core-forms)
     :allowed-effects allowed-effects
     :checked-effects
     (invoke :stable-set
             (for [[effect entry] effect-registry
                   :when (and (contains? allowed-effects effect)
                              (or (:requires-capability entry)
                                  (:requires-build-grant entry)))]
               effect))
     :forbidden-effects
     (set/difference registered-effects (set allowed-effects))
     ;; Profile legality is not a package, provider, or deployment grant.
     :capabilities (capabilities-fn profile)
     :memory ((operation-value :profile-memory-regimes) profile)
     :runtime ((operation-value :profile-runtime-assumptions) profile)
     :nondeterminism (if (contains? #{:distributed :ai} profile)
                       :recorded-when-effectful
                       :profile-specific)
     :unsafe-policy ((operation-value :profile-unsafe-policies) profile)
     :artifact-boundaries
     ((operation-value :profile-artifact-boundaries) profile)}))

(defn profile-policy-layer
  [module metadata-key source-key default-value]
  (or (get-in module [:metadata metadata-key])
      (source-key module)
      default-value))

(defn profile-effective-effects
  [module inferred-effects allowed-effects-fn policy-layer-fn]
  (let [source-effects (:effects module)
        profile-effects (allowed-effects-fn (:profile module))
        package-effects
        (policy-layer-fn module :package-allowed-effects :effects #{})
        provider-effects
        (policy-layer-fn module :provider-effect-grants :effects #{})
        deployment-effects
        (policy-layer-fn module :deployment-allowed-effects :effects #{})]
    {:source source-effects
     :inferred inferred-effects
     :profile profile-effects
     :package package-effects
     :provider provider-effects
     :deployment deployment-effects
     :effective (set/intersection source-effects profile-effects
                                  package-effects provider-effects
                                  deployment-effects)}))

(defn- permission-row [effective registry-entry effect]
  (let [source-effects (:source effective)
        profile-allowed? (contains? (:profile effective) effect)
        package-allowed? (contains? (:package effective) effect)
        provider-granted? (contains? (:provider effective) effect)
        deployment-granted? (contains? (:deployment effective) effect)
        effective? (contains? (:effective effective) effect)]
    {:effect effect
     :family (:family registry-entry)
     :requires-capability (boolean (:requires-capability registry-entry))
     :capability (:capability registry-entry)
     :declared? (contains? source-effects effect)
     :inferred? (contains? (:inferred effective) effect)
     :profile-allowed? profile-allowed?
     :package-allowed? package-allowed?
     :provider-granted? provider-granted?
     :deployment-granted? deployment-granted?
     :effective? effective?
     :state (cond
              effective? :allowed
              (not profile-allowed?) :rejected
              (or (not package-allowed?)
                  (not provider-granted?)
                  (not deployment-granted?)) :checked
              :else :rejected)
     :policy-layer (cond
                     (not profile-allowed?) :profile
                     (not package-allowed?) :package
                     (not provider-granted?) :provider
                     (not deployment-granted?) :deployment
                     :else :effective)}))

(defn effect-permission-table
  [inferred-effects effective registry-entry-fn invoke]
  (let [row-effects (set/union (:source effective) inferred-effects)
        effective (assoc effective :inferred inferred-effects)]
    (mapv (fn [effect]
            (permission-row effective (or (registry-entry-fn effect) {}) effect))
          (invoke :stable-vec row-effects))))
