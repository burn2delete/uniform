(ns gravity.capability-validation
  "Pure hosted Stage0 capability-validation compatibility projection.

  This leaf consumes accepted profile output plus explicit package, provider,
  and deployment grant facts.  It intersects those facts; it never creates a
  grant, selects a provider, or establishes provider trust."
  (:require [clojure.set :as set]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private ^:dynamic *bypass-next-operation-keys* #{})

(def ^:private function-operation-keys
  #{:stable-set :stable-vec :diagnostic-record :provider-name
    :profile-capabilities :profile-effective-capabilities
    :capability-permission-table :capability-validation-facts})
(def ^:private scalar-operation-keys
  #{:provider-specs :capability-diagnostic-ids})
(def ^:private operation-keys
  (into function-operation-keys scalar-operation-keys))

(def ^:private capability-diagnostic-ids-default
  ["L15-CAPABILITY-MISSING" "L15-PROVIDER-MISSING" "L15-PROFILE"
   "L15-SCOPE" "L15-PHASE" "L15-TRUST"])
(def ^:private default-scalars
  {:capability-diagnostic-ids capability-diagnostic-ids-default})

(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key)
    (get *operations* key)))

(defn- operation-value [key]
  (if (contains? *operations* key)
    (get *operations* key)
    (if (contains? default-scalars key)
      (get default-scalars key)
      (throw (ex-info "Capability validation requires an injected operation"
                      {:operation key})))))

(defn- default-stable-vec [values]
  (->> values (sort-by pr-str) vec))
(defn- default-stable-set [values]
  (into (sorted-set-by #(compare (pr-str %1) (pr-str %2))) values))

(defn- invoke [key & args]
  (if-let [operation (current-operation key)]
    (apply operation args)
    (case key
      :stable-set (apply default-stable-set args)
      :stable-vec (apply default-stable-vec args)
      :diagnostic-record
      (let [[id facts] args]
        {:artifact :gravity/capability-diagnostic
         :diagnostic id
         :stage :capability-validation
         :facts facts
         :status :rejected})
      (throw (ex-info "Capability validation requires a function operation"
                      {:operation key})))))

(defmacro ^:private definterposable [name key arguments & body]
  `(defn ~name ~arguments
     (if (contains? *bypass-next-operation-keys* ~key)
       (binding [*bypass-next-operation-keys*
                 (disj *bypass-next-operation-keys* ~key)]
         (do ~@body))
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys*
                   (conj *active-operation-keys* ~key)]
           (operation# ~@arguments))
         (do ~@body)))))

(definterposable provider-name :provider-name
  [capability]
  (get-in (operation-value :provider-specs) [capability :provider]))

(definterposable profile-capabilities :profile-capabilities
  [profile]
  (->> (operation-value :provider-specs)
       (keep (fn [[capability spec]]
               (when (contains? (:profiles spec #{}) profile)
                 capability)))
       (invoke :stable-set)))

(definterposable profile-effective-capabilities
  :profile-effective-capabilities
  [profile-output grant-facts]
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

(definterposable capability-permission-table
  :capability-permission-table
  [profile-output effective provider-facts]
  (let [source-capabilities (:source effective)
        required-capabilities (:required effective)
        row-capabilities (set/union source-capabilities required-capabilities)]
    (mapv
     (fn [capability]
       (let [provider-fact (get provider-facts capability)
             expected-provider (provider-name capability)
             package-grant (get-in effective [:package-grants capability])
             provider-grant (get-in effective [:provider-grants capability])
             deployment-grant
             (get-in effective [:deployment-grants capability])
             grants {:package package-grant
                     :provider provider-grant
                     :deployment deployment-grant}
             profile-allowed? (contains? (:profile effective) capability)
             package-allowed? (contains? (:package effective) capability)
             provider-granted? (contains? (:provider effective) capability)
             deployment-granted? (contains? (:deployment effective) capability)
             provider-selected? (and (= expected-provider
                                        (:provider provider-fact))
                                     (every? #(or (nil? %)
                                                  (= expected-provider
                                                     (:provider %)))
                                             (vals grants))
                                     (= :selected (:status provider-fact)))
             provider-trusted? (and provider-selected?
                                    (true? (:trusted? provider-fact)))
             missing-grant-layer
             (first (filter #(nil? (get grants %))
                            [:package :provider :deployment]))
             scope-mismatch-layer
             (first (filter #(false? (:scope-satisfied? (get grants %)))
                            [:package :provider :deployment]))
             phase-mismatch-layer
             (first (filter #(let [grant (get grants %)]
                               (and grant
                                    (not= (:phase grant)
                                          (:requested-phase grant))))
                            [:package :provider :deployment]))
             effective? (contains? (:effective effective) capability)]
         {:capability capability
          :provider expected-provider
          :provider-fact provider-fact
          :declared? (contains? source-capabilities capability)
          :required? (contains? required-capabilities capability)
          :profile-allowed? profile-allowed?
          :package-allowed? package-allowed?
          :provider-granted? provider-granted?
          :deployment-granted? deployment-granted?
          :package-grant package-grant
          :provider-grant provider-grant
          :deployment-grant deployment-grant
          :missing-grant-layer missing-grant-layer
          :scope-mismatch-layer scope-mismatch-layer
          :phase-mismatch-layer phase-mismatch-layer
          :scope-valid? (nil? scope-mismatch-layer)
          :phase-valid? (nil? phase-mismatch-layer)
          :provider-selected? provider-selected?
          :provider-trusted? provider-trusted?
          :provider-trust-state (cond
                                  (not provider-selected?) :missing
                                  provider-trusted? :trusted
                                  :else :rejected)
          :effective? effective?
          ;; Preserve the legacy P1 grant-row decision. Provider trust is a
          ;; distinct downstream L15 decision and does not rewrite this row.
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
     (invoke :stable-vec row-capabilities))))

(defn- keyword-set? [value]
  (and (set? value) (every? keyword? value)))

(defn- valid-profile-output? [value]
  (and (map? value)
       (= :gravity/profile-valid-core (:artifact value))
       (keyword? (:profile value))
       (keyword? (:target value))
       (keyword-set? (:declared-capabilities value))
       (keyword-set? (:required-capabilities value))
       (contains? #{:accepted :rejected} (:status value))))

(defn- valid-grants? [value]
  (and (map? value)
       (= #{:package :provider :deployment} (set (keys value)))
       (every?
        (fn [grants]
          (and (map? grants)
               (every? keyword? (keys grants))
               (every?
                (fn [grant]
                  (and (map? grant)
                       (= #{:grant-id :provider :actual-scope
                            :requested-scope :phase :requested-phase
                            :scope-satisfied?}
                          (set (keys grant)))
                       (keyword? (:grant-id grant))
                       (symbol? (:provider grant))
                       (some? (:actual-scope grant))
                       (some? (:requested-scope grant))
                       (keyword? (:phase grant))
                       (keyword? (:requested-phase grant))
                       (boolean? (:scope-satisfied? grant))))
                (vals grants))))
        (vals value))))

(defn- valid-provider-facts? [value]
  (and (map? value)
       (every? keyword? (keys value))
       (every? (fn [fact]
                 (and (map? fact)
                      (symbol? (:provider fact))
                      (boolean? (:trusted? fact))
                      (contains? #{:selected :unselected} (:status fact))
                      (keyword? (:trust-level fact))))
               (vals value))))

(defn- valid-profile-report? [profile-output value]
  (and (map? value)
       (= :gravity/profile-validation-report (:artifact value))
       (= (:profile profile-output) (:profile value))
       (= (:target profile-output) (:target value))
       (= (:status profile-output) (:status value))
       (vector? (:diagnostics value))))

(defn- row-grant [row layer]
  (get row (keyword (str (name layer) "-grant"))))

(defn- nearest-grant [row]
  (some #(row-grant row %) [:provider :package :deployment]))

(defn- diagnostic-context
  ([profile-output row]
   (diagnostic-context profile-output row nil))
  ([profile-output row layer]
   (let [grant (when layer (row-grant row layer))
         nearby (or grant (nearest-grant row))
         provider-fact (:provider-fact row)]
     {:profile (:profile profile-output)
      :target (:target profile-output)
      :source-span (first (:source-spans profile-output))
      :producing-pass :profile-validation
      :consuming-pass :capability-validation
      :requested-capability (:capability row)
      :capability (:capability row)
      :selected-or-missing-provider (:provider provider-fact)
      :provider (:provider row)
      :nearest-provider (:provider row)
      :grant-id (:grant-id grant)
      :nearest-grant (:grant-id nearby)
      :actual-scope (:actual-scope grant)
      :scope (:actual-scope grant)
      :requested-scope (:requested-scope (or grant nearby))
      :phase (:requested-phase (or grant nearby))
      :grant-phase (:phase grant)})))

(definterposable capability-validation-facts
  :capability-validation-facts
  [profile-output profile-report grant-facts provider-facts]
  (when-not (valid-profile-output? profile-output)
    (throw (ex-info "Capability validation profile output is malformed"
                    {:profile-output profile-output})))
  (when-not (valid-grants? grant-facts)
    (throw (ex-info "Capability validation grant facts are malformed"
                    {:grant-facts grant-facts})))
  (when-not (valid-profile-report? profile-output profile-report)
    (throw (ex-info "Capability validation profile report is malformed or unlinked"
                    {:profile-output profile-output
                     :profile-report profile-report})))
  (when-not (valid-provider-facts? provider-facts)
    (throw (ex-info "Capability validation provider facts are malformed"
                    {:provider-facts provider-facts})))
  (let [authority (profile-effective-capabilities profile-output grant-facts)
        table (capability-permission-table profile-output authority provider-facts)
        diagnostics
        (vec
         (concat
          (when (= :rejected (:status profile-output))
            [(invoke :diagnostic-record "L15-PROFILE"
                     (merge (diagnostic-context profile-output {})
                            {:profile-status (:status profile-output)
                             :grant :profile-legality
                             :scope :profile
                             :remediation
                             :resolve-profile-validation-diagnostics}))])
          (mapcat
           (fn [row]
             (let [missing-layer (:missing-grant-layer row)
                   scope-layer (:scope-mismatch-layer row)
                   phase-layer (:phase-mismatch-layer row)]
               (cond
               (and (:required? row) (not (:declared? row)))
               [(invoke :diagnostic-record "L15-CAPABILITY-MISSING"
                        (merge (diagnostic-context profile-output row
                                                   :provider)
                               {:grant :source-declaration :scope :module
                                :remediation :declare-required-capability}))]
               (not (:profile-allowed? row))
               [(invoke :diagnostic-record "L15-PROFILE"
                        (merge (diagnostic-context profile-output row
                                                   :provider)
                               {:grant :profile-legality :scope :profile
                                :remediation
                                :select-capability-compatible-profile}))]
               missing-layer
               [(invoke :diagnostic-record "L15-CAPABILITY-MISSING"
                        (merge (diagnostic-context profile-output row
                                                   missing-layer)
                               {:grant missing-layer
                                :remediation
                                :add-capability-grant}))]
               (not (:provider-selected? row))
               [(invoke :diagnostic-record "L15-PROVIDER-MISSING"
                        (merge (diagnostic-context profile-output row :provider)
                               {:grant :provider
                                :provider-fact (:provider-fact row)
                                :remediation :select-required-provider}))]
               scope-layer
               [(invoke :diagnostic-record "L15-SCOPE"
                        (merge (diagnostic-context profile-output row
                                                   scope-layer)
                               {:grant scope-layer
                                :remediation
                                :attenuate-request-or-expand-grant-scope}))]
               phase-layer
               [(invoke :diagnostic-record "L15-PHASE"
                        (merge (diagnostic-context profile-output row
                                                   phase-layer)
                               {:grant phase-layer
                                :remediation
                                :use-separate-matching-phase-grant}))]
               (not (:provider-trusted? row))
               [(invoke :diagnostic-record "L15-TRUST"
                        (merge (diagnostic-context profile-output row
                                                   :provider)
                               (select-keys row
                                            [:capability :provider
                                             :provider-fact])
                               {:grant :provider
                                :trust-level
                                (get-in row [:provider-fact :trust-level])
                                :remediation :select-trusted-provider}))]
               :else [])))
           table)))
        accepted? (and (= :accepted (:status profile-output))
                       (empty? diagnostics))
        pass {:name :capability-validation
              :input :profile-valid-core
              :output :capability-valid-core
              :requires [:capability-requirements :profile-validation-report
                         :package-capability-grants
                         :provider-capability-grants
                         :deployment-capability-grants :provider-registry
                         :provider-trust-facts]
              :preserves [:source-spans :types :effects :profile-context
                          :target :capability-requirements]
              :invalidates [:unscoped-provider-cache]
              ;; Selection and usage artifacts are supplied inputs or future
              ;; outputs; this compatibility projection does not regenerate
              ;; either one.
              :regenerates []
              :emits [:capability-facts :capability-permission-table
                      :capability-validation-report
                      :capability-diagnostics]
              :rejects (operation-value :capability-diagnostic-ids)}
        grant-effective-capabilities (:effective authority)
        effective-capabilities
        (invoke :stable-set
                (for [row table
                      :when (and (:effective? row)
                                 (:provider-selected? row)
                                 (:scope-valid? row)
                                 (:phase-valid? row)
                                 (:provider-trusted? row))]
                  (:capability row)))
        report {:artifact :gravity/capability-validation-report
                :profile (:profile profile-output)
                :target (:target profile-output)
                :capability-permission-table table
                :grant-effective-capabilities grant-effective-capabilities
                :effective-capabilities effective-capabilities
                :grant-facts grant-facts
                :provider-facts provider-facts
                :profile-validation-report profile-report
                :diagnostics diagnostics
                :status (if accepted? :accepted :rejected)}]
    {:kind :gravity/stage0-capability-validation-facts
     :pass pass
     :capability-valid-core
     {:artifact :gravity/capability-valid-core
      :profile-valid-core profile-output
      :profile (:profile profile-output)
      :target (:target profile-output)
      :grant-effective-capabilities grant-effective-capabilities
      :effective-capabilities effective-capabilities
      :source-spans (:source-spans profile-output)
      :status (if accepted? :accepted :rejected)}
     :capability-validation-report report
     :capability-diagnostics diagnostics
     :input-provenance
     {:profile-validation-report profile-report
      :grant-facts grant-facts
      :provider-facts provider-facts}
     :status (if accepted? :accepted :rejected)}))

(defn- provider-specs? [value]
  (and (map? value) (seq value) (every? keyword? (keys value))
       (every? (fn [spec]
                 (and (map? spec) (symbol? (:provider spec))
                      (set? (:profiles spec)) (seq (:profiles spec))
                      (every? keyword? (:profiles spec))))
               (vals value))))
(defn- non-empty-string-vector? [value]
  (and (vector? value) (seq value)
       (every? #(and (string? %) (seq %)) value)))

(defn- validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "Capability validation operation map must be a map"
                    {:operations operations})))
  (let [unknown (vec (remove operation-keys (keys operations)))
        invalid (vec (for [[key value]
                           (select-keys operations function-operation-keys)
                           :when (not (fn? value))] key))]
    (when (seq unknown)
      (throw (ex-info "Capability validation operation map has unknown keys"
                      {:unknown-keys unknown})))
    (when (seq invalid)
      (throw (ex-info "Capability validation function operation is not a function"
                      {:operation-keys invalid}))))
  (when (and (contains? operations :provider-specs)
             (not (provider-specs? (:provider-specs operations))))
    (throw (ex-info "Capability validation provider specs are malformed"
                    {:provider-specs (:provider-specs operations)})))
  (when (and (contains? operations :capability-diagnostic-ids)
             (not (non-empty-string-vector?
                   (:capability-diagnostic-ids operations))))
    (throw (ex-info "Capability validation diagnostic IDs are malformed"
                    {:capability-diagnostic-ids
                     (:capability-diagnostic-ids operations)})))
  operations)

(defn with-operations [operations thunk]
  (validate-operations! operations)
  (when-not (fn? thunk)
    (throw (ex-info "Capability validation thunk must be a function"
                    {:thunk thunk})))
  (binding [*operations* (merge *operations* operations)] (thunk)))

(defn call-entrypoint-body [operation-key operation args]
  (when-not (contains? function-operation-keys operation-key)
    (throw (ex-info "Capability validation entrypoint key is unknown"
                    {:operation operation-key})))
  (when-not (fn? operation)
    (throw (ex-info "Capability validation entrypoint must be a function"
                    {:operation operation-key :value operation})))
  (when-not (sequential? args)
    (throw (ex-info "Capability validation entrypoint args must be sequential"
                    {:operation operation-key :args args})))
  (binding [*active-operation-keys*
            (disj *active-operation-keys* operation-key)
            *bypass-next-operation-keys*
            (conj *bypass-next-operation-keys* operation-key)]
    (apply operation args)))

(def ^:private namespace-contract
  {:namespace 'gravity.capability-validation
   :contract-boundary :hosted-stage0-capability-validation-projection
   :artifact-inputs [:profile-valid-core :profile-validation-report
                     :explicit-grant-facts
                     :explicit-provider-facts]
   :artifact-outputs [:capability-valid-core
                      :capability-validation-report
                      :capability-diagnostics]
   :owns [:hosted-capability-policy-intersection
          :hosted-capability-validation-facts
          :hosted-capability-validation-report]
   :does-not-own [:source-reading :typed-core-construction
                  :profile-validation :effect-checking-authority
                  :package-grant-authority :deployment-grant-authority
                  :provider-selection-authority :provider-trust-authority
                  :backend-execution :canonical-l15-authority
                  :proof-authority :attestation-authority :self-hosting
                  :seed-retirement :release-authority]
   :dependency-direction
   {:requires ['clojure.core 'clojure.set]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :compatibility-only? true
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-l15-authority? false
   :grant-authority? false
   :provider-trust-authority? false
   :proof-authority? false
   :self-hosted? false
   :release-authority? false
   :operation-interposition
   {:accepted-keys operation-keys :partial-overrides? true
    :unknown-keys-rejected? true :function-values-must-satisfy :fn?
    :captured-original-one-shot? true}})

(def public-api
  {'public-api {:kind :contract}
   'capability-validation-contract {:arglists '([])}
   'with-operations {:arglists '([operations thunk])}
   'call-entrypoint-body {:arglists '([operation-key operation args])}
   'provider-name {:arglists '([capability])}
   'profile-capabilities {:arglists '([profile])}
   'profile-effective-capabilities
   {:arglists '([profile-output grant-facts])}
   'capability-permission-table
   {:arglists '([profile-output effective provider-facts])}
   'capability-validation-facts
   {:arglists '([profile-output profile-report grant-facts provider-facts])}})

(defn capability-validation-contract []
  (assoc namespace-contract :public-api public-api))
