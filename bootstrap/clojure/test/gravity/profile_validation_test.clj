(ns gravity.profile-validation-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.profile-validation :as profile]))

(def effect-registry
  {:io/write {:profiles #{:hosted :meta} :family :io
              :requires-capability true :capability :io/stdout}
   :memory/raw {:profiles #{:native} :family :memory
                :requires-capability true :capability :memory/raw}
   :build/exec {:profiles #{} :family :build :requires-build-grant true}})

(def base-operations
  {:effect-registry effect-registry
   :provider-specs
   {:io/stdout {:provider 'gravity.io/stdout-host
                :profiles #{:hosted :native}}
    :memory/raw {:provider 'gravity.memory/raw-unsafe
                 :profiles #{:native}}}
   :core-forms #{'if 'do}
   :supported-targets #{:jvm :llvm}})

(defn module-for [profile-name target effects]
  {:module 'demo/main :profile profile-name :target target
   :effects effects :capabilities #{:io/stdout}
   :metadata {:package-allowed-effects effects
              :provider-effect-grants effects
              :deployment-allowed-effects effects}})

(def typed-artifact
  {:effected-core {:forms []}
   :inferred-effects #{:io/write}
   :required-capabilities #{:io/stdout}
   :source-spans [{:line 1}]})

(def module-artifact {:module-dependency-graph {:nodes ['demo/main]}})

(defn facts [module typed]
  (profile/with-operations
   base-operations
   #(profile/profile-validation-facts module typed module-artifact)))

(deftest profile-contract-and-public-boundary
  (let [contract (profile/profile-validation-contract)
        publics (set (keys (ns-publics 'gravity.profile-validation)))]
    (is (= publics (set (keys profile/public-api))))
    (is (= '([module typed-artifact module-artifact])
           (get-in profile/public-api
                   ['profile-validation-facts :arglists])))
    (is (= '([]) (get-in profile/public-api
                         ['all-registered-effects :arglists])))
    (is (= '([effect]) (get-in profile/public-api
                               ['effect-registry-entry :arglists])))
    (is (= ['clojure.core 'clojure.set]
           (get-in contract [:dependency-direction :requires])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (is (:compatibility-only? contract))
    (is (false? (:canonical-p1-authority? contract)))
    (is (false? (:capability-grant-authority? contract)))
    (is (every? (set (:does-not-own contract))
                [:source-reading :typed-core-construction
                 :backend-execution :proof-authority :release-authority]))))

(deftest profile-input-and-operation-shapes-are-strict
  (doseq [operations [nil {:unknown identity} {:stable-set :not-a-function}
                      {:all-registered-effects #{:io/write}}
                      {:effect-registry-entry :not-a-function}
                      {:effect-registry {:bad {:profiles [:hosted]}}}
                      {:provider-specs {:io/stdout {:profiles [:hosted]}}}
                      {:core-forms #{:not-a-symbol}}
                      {:supported-targets [:jvm]}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (profile/with-operations operations (fn [] :unreachable)))))
  (is (thrown? clojure.lang.ExceptionInfo
               (profile/with-operations base-operations :not-a-function)))
  (is (thrown? clojure.lang.ExceptionInfo
               (facts (assoc (module-for :hosted :jvm #{:io/write})
                             :effects [:io/write])
                      typed-artifact)))
  (is (thrown? clojure.lang.ExceptionInfo
               (facts (module-for :hosted :jvm #{:io/write})
                      (assoc typed-artifact :inferred-effects [:io/write]))))
  (is (thrown? clojure.lang.ExceptionInfo
               (facts (module-for :hosted :jvm #{:io/write})
                      (assoc typed-artifact
                             :required-capabilities [:io/stdout])))))

(deftest profile-positive-and-negative-policy-matrix
  (let [hosted (facts (module-for :hosted :jvm #{:io/write}) typed-artifact)
        native-typed (assoc typed-artifact :inferred-effects #{:memory/raw})
        native (facts (assoc (module-for :native :llvm #{:memory/raw})
                             :capabilities #{:memory/raw}) native-typed)
        denied (facts (module-for :hosted :jvm #{:memory/raw}) native-typed)
        target (facts (module-for :hosted :wasm #{:io/write}) typed-artifact)]
    (is (= hosted (facts (module-for :hosted :jvm #{:io/write})
                         typed-artifact)))
    (is (= :accepted (:status hosted)))
    (is (= :allowed (get-in hosted
                            [:profile-validation-report
                             :effect-permission-table 0 :state])))
    (is (= :accepted (:status native)))
    (is (= ["P1-EFFECT"]
           (mapv :diagnostic (:profile-diagnostics denied))))
    (is (= ["P1-BACKEND"]
           (mapv :diagnostic (:profile-diagnostics target))))
    (is (= #{:io/stdout}
           (get-in hosted [:profile-valid-core :declared-capabilities])))
    (is (= #{:io/stdout}
           (get-in hosted [:profile-valid-core :required-capabilities])))
    (is (= #{:io/stdout}
           (get-in hosted [:profile-validation-report
                           :profile-contract :capabilities])))
    (is (every? #(every? (set (keys (:facts %)))
                         [:profile :target :source-span :producing-pass
                          :consuming-pass :remediation])
                (concat (:profile-diagnostics denied)
                        (:profile-diagnostics target))))
    (is (every? #(some? (get-in % [:facts :source-span]))
                (concat (:profile-diagnostics denied)
                        (:profile-diagnostics target))))
    (is (nil? (get-in hosted [:profile-valid-core
                              :effective-capabilities])))))

(deftest profile-legacy-policy-parity-matrix
  (let [base (module-for :hosted :jvm #{:io/write})
        row #(get-in (facts %1 %2)
                     [:profile-validation-report
                      :effect-permission-table 0])
        allowed (row base typed-artifact)
        profile-denied
        (row (module-for :hosted :jvm #{:memory/raw})
             (assoc typed-artifact :inferred-effects #{:memory/raw}))
        package-denied (row (assoc-in base
                                      [:metadata :package-allowed-effects] #{})
                            typed-artifact)
        provider-denied (row (assoc-in base
                                       [:metadata :provider-effect-grants] #{})
                             typed-artifact)
        deployment-denied
        (row (assoc-in base [:metadata :deployment-allowed-effects] #{})
             typed-artifact)]
    (is (= [[:allowed :effective] [:rejected :profile]
            [:checked :package] [:checked :provider]
            [:checked :deployment]]
           (mapv (juxt :state :policy-layer)
                 [allowed profile-denied package-denied provider-denied
                  deployment-denied])))
    (is (= #{:io/stdout} (:capabilities
                          (profile/with-operations
                           base-operations
                           #(profile/profile-contract :hosted)))))))

(deftest profile-pass-contract-is-exact
  (is (= {:name :profile-validation
          :input :effected-core :output :profile-valid-core
          :requires [:type-facts :effect-facts :profile-declaration
                     :module-facts :module-dependency-graph
                     :profile-effect-policy :profile-capability-policy]
          :preserves [:source-spans :types :effects :profile-context
                      :capability-requirements]
          :invalidates [:unchecked-profile-assumptions]
          :regenerates []
          :emits [:profile-facts :effect-permission-table
                  :profile-validation-report :profile-diagnostics
                  :input-provenance]
          :rejects ["P1-MISSING-PROFILE" "P1-AMBIGUOUS-PROFILE"
                    "P1-EFFECT" "P1-CAPABILITY" "P1-MEMORY"
                    "P1-RUNTIME" "P1-CROSS-IMPORT" "P1-MACRO"
                    "P1-FACET" "P1-BACKEND"]}
         (:pass (facts (module-for :hosted :jvm #{:io/write})
                       typed-artifact)))))

(deftest profile-captured-original-is-one-shot
  (let [original profile/profile-allowed-effects
        calls (atom 0)
        observed
        (profile/with-operations
         (assoc base-operations
                :profile-allowed-effects
                (fn [profile-name]
                  (swap! calls inc)
                  (conj (profile/call-entrypoint-body
                         :profile-allowed-effects original [profile-name])
                        :test/extra)))
         #(profile/profile-contract :hosted))]
    (is (= 1 @calls))
    (is (contains? (:allowed-effects observed) :test/extra))))

(deftest profile-registry-helper-seams-are-dynamic-and-one-shot
  (let [all-original profile/all-registered-effects
        all-calls (atom 0)
        entry-calls (atom 0)
        contract
        (profile/with-operations
         (assoc (assoc base-operations
                       :effect-registry
                       (assoc effect-registry
                              :test/checked
                              {:profiles #{:hosted}
                               :requires-capability true}))
                :all-registered-effects
                (fn []
                  (swap! all-calls inc)
                  (disj (profile/call-entrypoint-body
                         :all-registered-effects all-original [])
                        :test/checked))
                :effect-registry-entry
                (fn [effect]
                  (swap! entry-calls inc)
                  (throw (ex-info "profile-contract must not call entry-registry seam"
                                  {:effect effect}))))
         #(profile/profile-contract :hosted))]
    (is (= 1 @all-calls))
    (is (zero? @entry-calls))
    (is (contains? (:checked-effects contract) :test/checked))
    (is (not (contains? (:forbidden-effects contract) :test/checked)))))

(deftest effect-permission-table-registry-entry-seam-is-one-shot
  (let [original profile/effect-registry-entry
        calls (atom 0)
        module (module-for :hosted :jvm #{:io/write})
        authority (profile/with-operations
                   base-operations
                   #(profile/profile-effective-effects module #{:io/write}))
        table
        (profile/with-operations
         (assoc base-operations
                :effect-registry-entry
                (fn [effect]
                  (swap! calls inc)
                  (assoc (profile/call-entrypoint-body
                          :effect-registry-entry original [effect])
                         :family :test/interposed)))
         #(profile/effect-permission-table module #{:io/write} authority))]
    (is (= 1 @calls))
    (is (= :test/interposed (:family (first table))))))
