(ns gravity.capability-validation-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.capability-validation :as capability]))

(def provider-specs
  {:io/stdout {:provider 'gravity.io/stdout-host
               :profiles #{:hosted :native}}
   :memory/raw {:provider 'gravity.memory/raw-unsafe
                :profiles #{:native}}})
(def base-operations {:provider-specs provider-specs})
(def profile-output
  {:artifact :gravity/profile-valid-core
   :effected-core {:forms []} :profile :hosted :target :jvm
   :declared-capabilities #{:io/stdout}
   :required-capabilities #{:io/stdout}
   :source-spans [{:line 1}] :status :accepted})
(defn grant-record
  ([grant-id provider]
   (grant-record grant-id provider :stdout :stdout :runtime :runtime true))
  ([grant-id provider actual-scope requested-scope phase requested-phase
    scope-satisfied?]
   {:grant-id grant-id
    :provider provider
    :actual-scope actual-scope
    :requested-scope requested-scope
    :phase phase
    :requested-phase requested-phase
    :scope-satisfied? scope-satisfied?}))

(defn grants-for [capability provider]
  {:package {capability (grant-record :grant/package provider)}
   :provider {capability (grant-record :grant/provider provider)}
   :deployment {capability (grant-record :grant/deployment provider)}})

(def all-grants (grants-for :io/stdout 'gravity.io/stdout-host))
(def trusted-provider
  {:io/stdout {:provider 'gravity.io/stdout-host
               :trusted? true :trust-level :trusted-package
               :status :selected}})
(def profile-report
  {:artifact :gravity/profile-validation-report
   :profile :hosted :target :jvm :diagnostics [] :status :accepted})

(defn linked-report [output]
  (assoc profile-report :profile (:profile output) :target (:target output)
         :status (:status output)))

(defn facts
  ([output grants providers]
   (capability/with-operations
    base-operations
    #(capability/capability-validation-facts
      output (linked-report output) grants providers))))

(deftest capability-contract-and-public-boundary
  (let [contract (capability/capability-validation-contract)]
    (is (= (set (keys (ns-publics 'gravity.capability-validation)))
           (set (keys capability/public-api))))
    (is (= '([profile-output profile-report grant-facts provider-facts])
           (get-in capability/public-api
                   ['capability-validation-facts :arglists])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (is (:compatibility-only? contract))
    (is (false? (:canonical-l15-authority? contract)))
    (is (false? (:grant-authority? contract)))
    (is (false? (:provider-trust-authority? contract)))
    (is (every? (set (:does-not-own contract))
                [:profile-validation :package-grant-authority
                 :provider-selection-authority :backend-execution
                 :proof-authority :release-authority]))))

(deftest capability-input-and-operation-shapes-are-strict
  (doseq [operations [nil {:unknown identity} {:stable-set :bad}
                      {:provider-specs {:io/stdout {:provider :not-symbol
                                                   :profiles #{:hosted}}}}
                      {:capability-diagnostic-ids []}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (capability/with-operations operations (fn [] :no)))))
  (is (thrown? clojure.lang.ExceptionInfo
               (facts (assoc profile-output :declared-capabilities [:io/stdout])
                      all-grants trusted-provider)))
  (is (thrown? clojure.lang.ExceptionInfo
               (facts profile-output (assoc all-grants :extra {})
                      trusted-provider)))
  (is (thrown? clojure.lang.ExceptionInfo
               (facts profile-output
                      (assoc-in all-grants
                                [:provider :io/stdout :scope-satisfied?]
                                :yes)
                      trusted-provider)))
  (is (thrown? clojure.lang.ExceptionInfo
               (capability/with-operations
                base-operations
                #(capability/capability-validation-facts
                  profile-output (assoc profile-report :target :llvm)
                  all-grants trusted-provider))))
  (is (thrown? clojure.lang.ExceptionInfo
               (facts profile-output all-grants
                      {:io/stdout {:provider :bad :trusted? true
                                   :trust-level :trusted-package
                                   :status :selected}}))))

(deftest capability-positive-and-negative-matrix
  (let [accepted (facts profile-output all-grants trusted-provider)
        missing (facts (assoc profile-output :declared-capabilities #{})
                       all-grants trusted-provider)
        profile-denied
        (facts (assoc profile-output :profile :hosted
                      :declared-capabilities #{:memory/raw}
                      :required-capabilities #{:memory/raw})
               (grants-for :memory/raw 'gravity.memory/raw-unsafe)
               {:memory/raw {:provider 'gravity.memory/raw-unsafe
                             :trusted? true
                             :trust-level :trusted-package
                             :status :selected}})
        scope-denied
        (facts profile-output
               (assoc-in all-grants
                         [:package :io/stdout]
                         (grant-record :grant/package
                                       'gravity.io/stdout-host
                                       {:streams #{:stdout}}
                                       {:streams #{:stderr}}
                                       :runtime :runtime false))
               trusted-provider)
        grant-denied (facts profile-output
                            (update all-grants :provider dissoc :io/stdout)
                            trusted-provider)
        provider-missing (facts profile-output all-grants {})
        provider-unselected
        (facts profile-output all-grants
               (assoc-in trusted-provider [:io/stdout :status] :unselected))
        phase-denied
        (facts profile-output
               (assoc-in all-grants [:provider :io/stdout :phase] :build)
               trusted-provider)
        trust-denied (facts profile-output all-grants
                            (assoc-in trusted-provider
                                      [:io/stdout :trusted?] false))
        mismatch-denied
        (facts profile-output all-grants
               (assoc-in trusted-provider [:io/stdout :provider]
                         'gravity.io/not-the-selected-provider))
        combined-denied
        (facts profile-output (update all-grants :deployment
                                      dissoc :io/stdout)
               (assoc-in trusted-provider [:io/stdout :trusted?] false))]
    (is (= accepted (facts profile-output all-grants trusted-provider)))
    (is (= :accepted (:status accepted)))
    (is (= #{:io/stdout}
           (get-in accepted [:capability-valid-core
                             :effective-capabilities])))
    (is (= ["L15-CAPABILITY-MISSING"]
           (mapv :diagnostic (:capability-diagnostics missing))))
    (is (= ["L15-PROFILE"]
           (mapv :diagnostic (:capability-diagnostics profile-denied))))
    (is (= ["L15-SCOPE"]
           (mapv :diagnostic (:capability-diagnostics scope-denied))))
    (is (= ["L15-PROVIDER-MISSING"]
           (mapv :diagnostic (:capability-diagnostics provider-missing))))
    (is (= {:requested-capability :io/stdout
            :selected-or-missing-provider nil
            :nearest-provider 'gravity.io/stdout-host
            :grant-id :grant/provider
            :actual-scope :stdout
            :requested-scope :stdout
            :grant-phase :runtime
            :phase :runtime}
           (select-keys
            (get-in provider-missing [:capability-diagnostics 0 :facts])
            [:requested-capability :selected-or-missing-provider
             :nearest-provider :grant-id :actual-scope :requested-scope
             :grant-phase :phase])))
    (is (= ["L15-PROVIDER-MISSING"]
           (mapv :diagnostic (:capability-diagnostics provider-unselected))))
    (is (= ["L15-PHASE"]
           (mapv :diagnostic (:capability-diagnostics phase-denied))))
    (is (= ["L15-CAPABILITY-MISSING"]
           (mapv :diagnostic (:capability-diagnostics grant-denied))))
    (is (= {:capability :io/stdout
            :provider 'gravity.io/stdout-host
            :grant :provider
            :grant-id nil}
           (select-keys
            (get-in grant-denied [:capability-diagnostics 0 :facts])
            [:capability :provider :grant :grant-id])))
    (is (= ["L15-TRUST"]
           (mapv :diagnostic (:capability-diagnostics trust-denied))))
    (is (= ["L15-PROVIDER-MISSING"]
           (mapv :diagnostic (:capability-diagnostics mismatch-denied))))
    (is (= ["L15-CAPABILITY-MISSING"]
           (mapv :diagnostic (:capability-diagnostics combined-denied))))
    (is (= :deployment
           (get-in combined-denied
                   [:capability-diagnostics 0 :facts :grant])))
    (is (= :missing
           (get-in provider-missing
                   [:capability-validation-report
                    :capability-permission-table 0 :provider-trust-state])))
    (is (every? #(every? (set (keys (:facts %)))
                         [:profile :target :source-span :producing-pass
                          :consuming-pass :remediation :grant :scope
                          :actual-scope :requested-scope :phase :grant-phase
                          :grant-id
                          :requested-capability
                          :selected-or-missing-provider :nearest-provider
                          :nearest-grant])
                (concat (:capability-diagnostics missing)
                        (:capability-diagnostics profile-denied)
                        (:capability-diagnostics scope-denied)
                        (:capability-diagnostics grant-denied)
                        (:capability-diagnostics provider-missing)
                        (:capability-diagnostics provider-unselected)
                        (:capability-diagnostics phase-denied)
                        (:capability-diagnostics trust-denied)
                        (:capability-diagnostics mismatch-denied)
                        (:capability-diagnostics combined-denied))))
    (is (every? #(some? (get-in % [:facts :source-span]))
                (concat (:capability-diagnostics missing)
                        (:capability-diagnostics profile-denied)
                        (:capability-diagnostics scope-denied)
                        (:capability-diagnostics grant-denied)
                        (:capability-diagnostics provider-missing)
                        (:capability-diagnostics provider-unselected)
                        (:capability-diagnostics phase-denied)
                        (:capability-diagnostics trust-denied)
                        (:capability-diagnostics mismatch-denied)
                        (:capability-diagnostics combined-denied))))
    (is (= {:provider 'gravity.io/stdout-host
            :trusted? false
            :trust-level :trusted-package
            :status :selected}
           (get-in trust-denied
                   [:capability-diagnostics 0 :facts :provider-fact])))
    (is (= 'gravity.io/not-the-selected-provider
           (get-in mismatch-denied
                   [:capability-diagnostics 0 :facts
                    :provider-fact :provider])))
    (is (= {:streams #{:stdout}}
           (get-in scope-denied
                   [:capability-diagnostics 0 :facts :actual-scope])))
    (is (= {:streams #{:stderr}}
           (get-in scope-denied
                   [:capability-diagnostics 0 :facts :requested-scope])))
    (is (= [:build :runtime]
           ((juxt :grant-phase :phase)
            (get-in phase-denied [:capability-diagnostics 0 :facts]))))
    (is (every? #(empty? (get-in % [:capability-valid-core
                                     :effective-capabilities]))
                [provider-missing provider-unselected scope-denied
                 phase-denied trust-denied mismatch-denied]))
    (is (every? #(= [:allowed true]
                    ((juxt :state :effective?)
                     (get-in % [:capability-validation-report
                                :capability-permission-table 0])))
                [provider-missing provider-unselected scope-denied
                 phase-denied trust-denied mismatch-denied]))
    ;; Trust rejection is downstream from, and does not rewrite, the legacy
    ;; grant intersection or permission state.
    (is (empty?
           (get-in trust-denied
                   [:capability-valid-core :effective-capabilities])))
    (is (= #{:io/stdout}
           (get-in trust-denied
                   [:capability-valid-core
                    :grant-effective-capabilities])))
    (is (= #{:io/stdout}
           (get-in trust-denied
                   [:capability-validation-report
                    :grant-effective-capabilities])))
    (is (empty?
         (get-in trust-denied
                 [:capability-validation-report
                  :effective-capabilities])))
    (is (= :allowed
           (get-in trust-denied
                   [:capability-validation-report
                    :capability-permission-table 0 :state])))
    (is (true?
         (get-in trust-denied
                 [:capability-validation-report
                  :capability-permission-table 0 :effective?])))
    (is (empty? (get-in grant-denied
                        [:capability-valid-core :effective-capabilities])))))

(deftest capability-pass-contract-is-exact
  (is (= {:name :capability-validation
          :input :profile-valid-core :output :capability-valid-core
          :requires [:capability-requirements :profile-validation-report
                     :package-capability-grants
                     :provider-capability-grants
                     :deployment-capability-grants :provider-registry
                     :provider-trust-facts]
          :preserves [:source-spans :types :effects :profile-context
                      :target :capability-requirements]
          :invalidates [:unscoped-provider-cache]
          :regenerates []
          :emits [:capability-facts :capability-permission-table
                  :capability-validation-report :capability-diagnostics]
          :rejects ["L15-CAPABILITY-MISSING" "L15-PROVIDER-MISSING"
                    "L15-PROFILE" "L15-SCOPE"
                    "L15-PHASE" "L15-TRUST"]}
         (:pass (facts profile-output all-grants trusted-provider)))))

(deftest capability-legacy-grant-row-parity-matrix
  (let [row (fn [output grants providers]
              (get-in (facts output grants providers)
                      [:capability-validation-report
                       :capability-permission-table 0]))
        allowed (row profile-output all-grants trusted-provider)
        profile-denied
        (row (assoc profile-output :profile :hosted
                    :declared-capabilities #{:memory/raw}
                    :required-capabilities #{:memory/raw})
             (grants-for :memory/raw 'gravity.memory/raw-unsafe)
             {:memory/raw {:provider 'gravity.memory/raw-unsafe
                           :trusted? true
                           :trust-level :trusted-package
                           :status :selected}})
        package-denied (row profile-output
                            (update all-grants :package dissoc :io/stdout)
                            trusted-provider)
        provider-denied (row profile-output
                             (update all-grants :provider dissoc :io/stdout)
                             trusted-provider)
        deployment-denied
        (row profile-output
             (update all-grants :deployment dissoc :io/stdout)
             trusted-provider)
        trust-denied
        (row profile-output all-grants
             (assoc-in trusted-provider [:io/stdout :trusted?] false))]
    (is (= [[:allowed :effective] [:rejected :profile]
            [:checked :package] [:checked :provider]
            [:checked :deployment] [:allowed :effective]]
           (mapv (juxt :state :policy-layer)
                 [allowed profile-denied package-denied provider-denied
                  deployment-denied trust-denied])))
    (is (= :rejected (:provider-trust-state trust-denied)))))

(deftest capability-captured-original-is-one-shot
  (let [original capability/profile-capabilities
        calls (atom 0)
        result
        (capability/with-operations
         {:provider-specs provider-specs
          :profile-capabilities
          (fn [profile-name]
            (swap! calls inc)
            (conj (capability/call-entrypoint-body
                   :profile-capabilities original [profile-name])
                  :test/extra))}
         #(capability/profile-effective-capabilities
           profile-output all-grants))]
    (is (= 1 @calls))
    (is (contains? (:profile result) :test/extra))))
