(ns gravity.capability-validation.validation)

(defn- keyword-set? [value] (and (set? value) (every? keyword? value)))

(defn- valid-profile-output? [value]
  (and (map? value) (= :gravity/profile-valid-core (:artifact value))
       (keyword? (:profile value)) (keyword? (:target value))
       (keyword-set? (:declared-capabilities value))
       (keyword-set? (:required-capabilities value))
       (contains? #{:accepted :rejected} (:status value))))

(defn- valid-grants? [value]
  (and (map? value) (= #{:package :provider :deployment} (set (keys value)))
       (every? (fn [grants]
                 (and (map? grants) (every? keyword? (keys grants))
                      (every? (fn [grant]
                                (and (map? grant)
                                     (= #{:grant-id :provider :actual-scope :requested-scope
                                          :phase :requested-phase :scope-satisfied?}
                                        (set (keys grant)))
                                     (keyword? (:grant-id grant)) (symbol? (:provider grant))
                                     (some? (:actual-scope grant)) (some? (:requested-scope grant))
                                     (keyword? (:phase grant)) (keyword? (:requested-phase grant))
                                     (boolean? (:scope-satisfied? grant))))
                              (vals grants))))
               (vals value))))

(defn- valid-provider-facts? [value]
  (and (map? value) (every? keyword? (keys value))
       (every? #(and (map? %) (symbol? (:provider %)) (boolean? (:trusted? %))
                     (contains? #{:selected :unselected} (:status %))
                     (keyword? (:trust-level %)))
               (vals value))))

(defn- valid-profile-report? [profile-output value]
  (and (map? value) (= :gravity/profile-validation-report (:artifact value))
       (= (:profile profile-output) (:profile value))
       (= (:target profile-output) (:target value))
       (= (:status profile-output) (:status value)) (vector? (:diagnostics value))))

(defn validate-inputs! [profile-output profile-report grant-facts provider-facts]
  (when-not (valid-profile-output? profile-output)
    (throw (ex-info "Capability validation profile output is malformed"
                    {:profile-output profile-output})))
  (when-not (valid-grants? grant-facts)
    (throw (ex-info "Capability validation grant facts are malformed"
                    {:grant-facts grant-facts})))
  (when-not (valid-profile-report? profile-output profile-report)
    (throw (ex-info "Capability validation profile report is malformed or unlinked"
                    {:profile-output profile-output :profile-report profile-report})))
  (when-not (valid-provider-facts? provider-facts)
    (throw (ex-info "Capability validation provider facts are malformed"
                    {:provider-facts provider-facts}))))
