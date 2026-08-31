(ns gravity.capability-validation.authority
  (:require [clojure.set :as set]))

(defn profile-capabilities [provider-specs profile invoke]
  (->> provider-specs
       (keep (fn [[capability spec]]
               (when (contains? (:profiles spec #{}) profile) capability)))
       (invoke :stable-set)))

(defn profile-effective-capabilities [profile-output grant-facts profile-capabilities]
  (let [source-capabilities (:declared-capabilities profile-output)
        profile-caps (profile-capabilities (:profile profile-output))
        package-grants (:package grant-facts)
        provider-grants (:provider grant-facts)
        deployment-grants (:deployment grant-facts)]
    {:source source-capabilities
     :required (:required-capabilities profile-output)
     :profile profile-caps
     :package (set (keys package-grants))
     :provider (set (keys provider-grants))
     :deployment (set (keys deployment-grants))
     :package-grants package-grants
     :provider-grants provider-grants
     :deployment-grants deployment-grants
     :effective (set/intersection source-capabilities profile-caps
                                  (set (keys package-grants))
                                  (set (keys provider-grants))
                                  (set (keys deployment-grants)))}))

(defn- permission-row [effective provider-facts provider-name capability]
  (let [provider-fact (get provider-facts capability)
        expected-provider (provider-name capability)
        package-grant (get-in effective [:package-grants capability])
        provider-grant (get-in effective [:provider-grants capability])
        deployment-grant (get-in effective [:deployment-grants capability])
        grants {:package package-grant :provider provider-grant
                :deployment deployment-grant}
        profile-allowed? (contains? (:profile effective) capability)
        package-allowed? (contains? (:package effective) capability)
        provider-granted? (contains? (:provider effective) capability)
        deployment-granted? (contains? (:deployment effective) capability)
        provider-selected? (and (= expected-provider (:provider provider-fact))
                                (every? #(or (nil? %) (= expected-provider (:provider %)))
                                        (vals grants))
                                (= :selected (:status provider-fact)))
        provider-trusted? (and provider-selected? (true? (:trusted? provider-fact)))
        missing-grant-layer (first (filter #(nil? (get grants %))
                                           [:package :provider :deployment]))
        scope-mismatch-layer (first (filter #(false? (:scope-satisfied? (get grants %)))
                                            [:package :provider :deployment]))
        phase-mismatch-layer (first (filter #(let [grant (get grants %)]
                                               (and grant (not= (:phase grant)
                                                               (:requested-phase grant))))
                                            [:package :provider :deployment]))
        effective? (contains? (:effective effective) capability)]
    {:capability capability :provider expected-provider :provider-fact provider-fact
     :declared? (contains? (:source effective) capability)
     :required? (contains? (:required effective) capability)
     :profile-allowed? profile-allowed? :package-allowed? package-allowed?
     :provider-granted? provider-granted? :deployment-granted? deployment-granted?
     :package-grant package-grant :provider-grant provider-grant
     :deployment-grant deployment-grant :missing-grant-layer missing-grant-layer
     :scope-mismatch-layer scope-mismatch-layer :phase-mismatch-layer phase-mismatch-layer
     :scope-valid? (nil? scope-mismatch-layer) :phase-valid? (nil? phase-mismatch-layer)
     :provider-selected? provider-selected? :provider-trusted? provider-trusted?
     :provider-trust-state (cond (not provider-selected?) :missing
                                 provider-trusted? :trusted :else :rejected)
     :effective? effective?
     :state (cond effective? :allowed
                  (not profile-allowed?) :rejected
                  (or (not package-allowed?) (not provider-granted?)
                      (not deployment-granted?)) :checked
                  :else :rejected)
     :policy-layer (cond (not profile-allowed?) :profile
                         (not package-allowed?) :package
                         (not provider-granted?) :provider
                         (not deployment-granted?) :deployment
                         :else :effective)}))

(defn capability-permission-table
  [_profile-output effective provider-facts provider-name invoke]
  (let [row-capabilities (set/union (:source effective) (:required effective))]
    (mapv #(permission-row effective provider-facts provider-name %)
          (invoke :stable-vec row-capabilities))))
