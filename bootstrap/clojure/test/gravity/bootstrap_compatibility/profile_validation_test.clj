(ns gravity.bootstrap-compatibility.profile-validation-test
  (:require [clojure.set :as set]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.profile-validation :as profile]))

(def effect-registry
  {:io/write {:profiles #{:hosted :meta}
              :family :io
              :requires-capability true
              :capability :io/stdout}
   :memory/raw {:profiles #{:native}
                :family :memory
                :requires-capability true
                :capability :memory/raw}
   ;; Build effects have no profile set in the legacy registry.  The facade
   ;; projects them as an empty set and preserves :requires-build-grant.
   :build/exec {:family :build
                :requires-build-grant true}})

(def provider-specs
  {:io/stdout {:provider 'gravity.io/stdout-host
               :profiles #{:hosted :native}}
   :memory/raw {:provider 'gravity.memory/raw-unsafe
                :profiles #{:native}}})

(def profile-memory-regimes
  {:hosted {:managed true :ownership false :regions false
            :hidden-allocation :declared :raw-memory :unsafe-only}
   :native {:managed false :ownership true :regions true
            :hidden-allocation :declared :raw-memory :unsafe-only}})

(def profile-runtime-assumptions
  {:hosted {:required true :providers #{:host}}
   :native {:required false :providers #{:allocator}}})

(def profile-unsafe-policies
  {:hosted :audited :native :reviewed})

(def profile-artifact-boundaries
  {:hosted #{:schema :host-object}
   :native #{:schema :native-object}})

(def profile-order [:hosted :native])
(def diagnostic-ids ["P1-MISSING-PROFILE" "P1-AMBIGUOUS-PROFILE"
                    "P1-EFFECT" "P1-CAPABILITY" "P1-MEMORY"
                    "P1-RUNTIME" "P1-CROSS-IMPORT" "P1-MACRO"
                    "P1-FACET" "P1-BACKEND"])
(def core-forms '#{if do let fn})

(defn module-for
  [profile-name target effects]
  {:module 'demo/profile
   :profile profile-name
   :target target
   :effects effects
   :capabilities #{:io/stdout}
   :metadata {:package-allowed-effects effects
              :provider-effect-grants effects
              :deployment-allowed-effects effects}})

(def typed-artifact
  {:effected-core {:forms []}
   :inferred-effects #{:io/write}
   :required-capabilities #{:io/stdout}
   :source-spans [{:source "synthetic/profile.gravity"
                   :line 1}]})

(def module-artifact
  {:module-dependency-graph {:nodes ['demo/profile]}})

(defn bootstrap-policy-redefs
  []
  {#'bootstrap/effect-registry effect-registry
   #'bootstrap/provider-specs provider-specs
   #'bootstrap/standard-profile-order profile-order
   #'bootstrap/p1-diagnostic-ids diagnostic-ids
   #'bootstrap/profile-memory-regimes profile-memory-regimes
   #'bootstrap/profile-runtime-assumptions profile-runtime-assumptions
   #'bootstrap/profile-unsafe-policies profile-unsafe-policies
   #'bootstrap/profile-artifact-boundaries profile-artifact-boundaries
   #'bootstrap/core-forms core-forms
   #'bootstrap/supported-targets #{:jvm}})

(defn leaf-operations
  []
  {:stable-set bootstrap/stable-set
   :stable-vec bootstrap/stable-vec
   :all-registered-effects (fn [] (set (keys effect-registry)))
   :effect-registry-entry (fn [effect] (get effect-registry effect))
   :effect-registry
   (reduce-kv (fn [registry effect entry]
                (assoc registry effect
                       (if (contains? entry :profiles)
                         entry
                         (assoc entry :profiles #{}))))
              {}
              effect-registry)
   :provider-specs provider-specs
   :core-forms core-forms
   :supported-targets #{:jvm}
   :standard-profile-order profile-order
   :profile-diagnostic-ids diagnostic-ids
   :profile-memory-regimes profile-memory-regimes
   :profile-runtime-assumptions profile-runtime-assumptions
   :profile-unsafe-policies profile-unsafe-policies
   :profile-artifact-boundaries profile-artifact-boundaries
   :profile-policy-layer bootstrap/profile-policy-layer})

(defn leaf-facts
  [module]
  (profile/with-operations
   (leaf-operations)
   #(profile/profile-validation-facts module typed-artifact module-artifact)))

;; Independent copies of the profile projection bodies from HEAD
;; 4921fbc.  These deliberately do not call either compatibility leaf; they
;; provide a table-driven reference for the facade's old public behavior.
(defn head-profile-allowed-effects
  [registry profile-name stable-set]
  (->> registry
       (keep (fn [[effect entry]]
               (when (or (contains? (:profiles entry #{}) profile-name)
                         (and (:requires-build-grant entry)
                              (contains? #{:meta :hosted} profile-name)))
                 effect)))
       stable-set))

(defn head-profile-capabilities
  [specs profile-name stable-set]
  (->> specs
       (keep (fn [[capability spec]]
               (when (contains? (:profiles spec #{}) profile-name)
                 capability)))
       stable-set))

(defn head-profile-contract
  [registry specs profile-name forms memory runtime unsafe boundaries stable-set]
  (let [allowed-effects (head-profile-allowed-effects
                         registry profile-name stable-set)]
    {:profile profile-name
     :allowed-forms forms
     :allowed-effects allowed-effects
     :checked-effects
     (stable-set (for [[effect entry] registry
                       :when (and (contains? allowed-effects effect)
                                  (or (:requires-capability entry)
                                      (:requires-build-grant entry)))]
                   effect))
     :forbidden-effects (set/difference (set (keys registry)) allowed-effects)
     :capabilities (head-profile-capabilities specs profile-name stable-set)
     :memory (memory profile-name)
     :runtime (runtime profile-name)
     :nondeterminism (if (contains? #{:distributed :ai} profile-name)
                       :recorded-when-effectful
                       :profile-specific)
     :unsafe-policy (unsafe profile-name)
     :artifact-boundaries (boundaries profile-name)}))

(defn head-profile-policy-layer
  [module metadata-key source-key default-value]
  (or (get-in module [:metadata metadata-key])
      (source-key module)
      default-value))

(defn head-profile-effective-effects
  [module inferred-effects profile-allowed policy-layer]
  (let [source-effects (:effects module)
        package-effects (policy-layer module :package-allowed-effects :effects #{})
        provider-effects (policy-layer module :provider-effect-grants :effects #{})
        deployment-effects (policy-layer module :deployment-allowed-effects :effects #{})]
    {:source source-effects
     :inferred inferred-effects
     :profile profile-allowed
     :package package-effects
     :provider provider-effects
     :deployment deployment-effects
     :effective (set/intersection source-effects profile-allowed
                                  package-effects provider-effects
                                  deployment-effects)}))

(defn head-effect-permission-table
  [registry module inferred-effects effective stable-vec]
  (let [source-effects (:source effective)
        row-effects (set/union source-effects inferred-effects)]
    (mapv (fn [effect]
            (let [entry (get registry effect)
                  profile-allowed? (contains? (:profile effective) effect)
                  package-allowed? (contains? (:package effective) effect)
                  provider-granted? (contains? (:provider effective) effect)
                  deployment-granted? (contains? (:deployment effective) effect)
                  effective? (contains? (:effective effective) effect)]
              {:effect effect
               :family (:family entry)
               :requires-capability (boolean (:requires-capability entry))
               :capability (:capability entry)
               :declared? (contains? source-effects effect)
               :inferred? (contains? inferred-effects effect)
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
          (stable-vec row-effects))))

(deftest profile-facades-match-head-4921fbc-reference-table
  (with-redefs-fn (bootstrap-policy-redefs)
    #(let [module (module-for :hosted :jvm #{:io/write :build/exec})
           inferred #{:io/write :build/exec}
           allowed-reference (head-profile-allowed-effects
                              effect-registry :hosted bootstrap/stable-set)
           capabilities-reference (head-profile-capabilities
                                   provider-specs :hosted bootstrap/stable-set)
           contract-reference (head-profile-contract
                               effect-registry provider-specs :hosted
                               core-forms
                               (constantly (profile-memory-regimes :hosted))
                               (constantly (profile-runtime-assumptions :hosted))
                               (constantly (profile-unsafe-policies :hosted))
                               (constantly (profile-artifact-boundaries :hosted))
                               bootstrap/stable-set)
           authority-reference (head-profile-effective-effects
                                module inferred allowed-reference
                                head-profile-policy-layer)
           table-reference (head-effect-permission-table
                            effect-registry module inferred
                            authority-reference bootstrap/stable-vec)]
       (is (= allowed-reference
              (bootstrap/profile-allowed-effects :hosted)))
       (is (= capabilities-reference
              (bootstrap/profile-capabilities :hosted)))
       (is (= contract-reference
              (bootstrap/profile-contract :hosted)))
       (is (= authority-reference
              (bootstrap/profile-effective-effects module inferred)))
       (is (= table-reference
              (bootstrap/effect-permission-table
               module inferred authority-reference))))))

(deftest profile-head-reference-policy-denial-matrix-and-dynamic-seams
  (with-redefs-fn (bootstrap-policy-redefs)
    #(let [module (module-for :hosted :jvm #{:io/write})
           inferred #{:io/write}
           cases [[:package :package-allowed-effects]
                  [:provider :provider-effect-grants]
                  [:deployment :deployment-allowed-effects]]]
       (doseq [[layer metadata-key] cases]
         (let [denied-module (assoc-in module [:metadata metadata-key] #{})
               reference-authority
               (head-profile-effective-effects
                denied-module inferred
                (head-profile-allowed-effects
                 effect-registry :hosted bootstrap/stable-set)
                head-profile-policy-layer)
               reference-row (first (head-effect-permission-table
                                     effect-registry denied-module inferred
                                     reference-authority bootstrap/stable-vec))
               facade-authority (bootstrap/profile-effective-effects
                                 denied-module inferred)
               facade-row (first (bootstrap/effect-permission-table
                                  denied-module inferred facade-authority))]
           (is (= reference-authority facade-authority)
               (str layer " authority parity"))
           (is (= reference-row facade-row)
               (str layer " permission-row parity"))
           (is (= [:checked layer]
                  [(:state facade-row) (:policy-layer facade-row)]))))
       ;; The old body accepted dynamic stable-set/function seams.  Verify the
       ;; facade still observes them, independently of the leaf implementation.
       (with-redefs [bootstrap/stable-set
                     (fn [values]
                       (conj (set values) :test/stable))
                     bootstrap/profile-policy-layer
                     (fn [_ metadata-key _ default-value]
                       (if (contains?
                            #{:package-allowed-effects
                              :provider-effect-grants
                              :deployment-allowed-effects}
                            metadata-key)
                         #{:io/write}
                         default-value))]
         (is (= (head-profile-allowed-effects
                 effect-registry :hosted
                 (fn [values] (conj (set values) :test/stable)))
                (bootstrap/profile-allowed-effects :hosted)))
         (is (= #{:io/write}
                (:effective (bootstrap/profile-effective-effects
                             module inferred))))))))

(deftest profile-downstream-caller-artifacts-retain-head-shape
  (with-redefs-fn (bootstrap-policy-redefs)
    #(let [module (module-for :hosted :jvm #{:io/write})
           inferred #{:io/write}
           authority (bootstrap/profile-effective-effects module inferred)
           table (bootstrap/effect-permission-table module inferred authority)
           contract (bootstrap/profile-contract :hosted)
           manifest {:profile :hosted
                     :target :jvm
                     :source-effects (:source authority)
                     :inferred-effects inferred
                     :effective-effects (:effective authority)
                     :source-capabilities #{:io/stdout}
                     :required-capabilities #{:io/stdout}
                     :effective-capabilities #{:io/stdout}
                     :memory-regime (:memory contract)
                     :runtime-assumptions (:runtime contract)
                     :unsafe-policy (:unsafe-policy contract)
                     :dependencies {:acyclic true}
                     :provider-selections []}
           backend (bootstrap/backend-eligibility-report
                    module (assoc manifest :profile-contract contract))
           conformance (bootstrap/profile-conformance-fixture
                        manifest table [] {:acyclic true} backend)]
       (is (= :clojure-stage0-jvm (:backend backend)))
       (is (true? (:eligible? backend)))
       (is (= :P1 (:document conformance)))
       (is (= :complete (:status conformance)))
       (is (= 1 (:effect-permission-rows conformance))))))

(deftest profile-facades-preserve-public-arglists-and-exact-leaf-parity
  (doseq [[wrapper-var expected]
          [[#'bootstrap/profile-allowed-effects '([profile])]
           [#'bootstrap/profile-capabilities '([profile])]
           [#'bootstrap/profile-contract '([profile])]
           [#'bootstrap/profile-policy-layer
            '([module metadata-key source-key default-value])]
           [#'bootstrap/profile-effective-effects
            '([module inferred-effects])]
           [#'bootstrap/effect-permission-table
            '([module inferred-effects effective])]
           [#'bootstrap/profile-validation-facts
            '([module typed-artifact module-artifact])]]]
    (is (= expected (:arglists (meta wrapper-var)))))
  (with-redefs-fn (bootstrap-policy-redefs)
    #(let [module (module-for :hosted :jvm #{:io/write})
           wrapped (bootstrap/profile-validation-facts
                    module typed-artifact module-artifact)
           direct (leaf-facts module)]
       (is (= direct wrapped))
       (is (= :accepted (:status wrapped)))
       (is (= #{:io/stdout}
              (get-in wrapped [:profile-valid-core
                               :declared-capabilities]))))))

(deftest profile-policy-map-redefs-reach-the-leaf-through-the-central-seam
  (with-redefs-fn (bootstrap-policy-redefs)
    #(let [module (module-for :hosted :jvm #{:io/write})
           calls (atom [])]
       (with-redefs [bootstrap/profile-policy-layer
                     (fn [candidate metadata-key source-key default-value]
                       (swap! calls conj [metadata-key source-key])
                       (case metadata-key
                         :package-allowed-effects #{:io/write}
                         :provider-effect-grants #{:io/write}
                         :deployment-allowed-effects #{:io/write}
                         default-value))]
         (let [authority (bootstrap/profile-effective-effects
                          module #{:io/write})]
           (is (= #{:io/write} (:effective authority)))
           (is (= [[:package-allowed-effects :effects]
                   [:provider-effect-grants :effects]
                   [:deployment-allowed-effects :effects]]
                  @calls)))))))

(deftest profile-registry-function-seams-match-head-4921fbc-ownership
  ;; HEAD 4921fbc gave these functions deliberately disjoint ownership:
  ;; all-registered-effects supplied only the profile contract's forbidden
  ;; complement, while effect-registry-entry supplied only permission-row
  ;; metadata.
  (with-redefs-fn
    (bootstrap-policy-redefs)
    #(let [module (module-for :hosted :jvm #{:io/write})
           inferred #{:io/write}
           effective (bootstrap/profile-effective-effects module inferred)
           baseline-contract (bootstrap/profile-contract :hosted)
           baseline-table
           (bootstrap/effect-permission-table module inferred effective)
           registered-contract
           (with-redefs [bootstrap/all-registered-effects
                         (fn [] #{:io/write})]
             (bootstrap/profile-contract :hosted))
           entry-contract
           (with-redefs [bootstrap/effect-registry-entry
                         (fn [_]
                           {:family :test/registry-entry
                            :requires-capability false
                            :requires-build-grant false})]
             (bootstrap/profile-contract :hosted))
           entry-table
           (with-redefs [bootstrap/effect-registry-entry
                         (fn [_]
                           {:family :test/registry-entry
                            :requires-capability false
                            :requires-build-grant false})]
             (bootstrap/effect-permission-table
              module inferred effective))]
       (testing "all-registered-effects changes only forbidden-effects"
         (is (= (dissoc baseline-contract :forbidden-effects)
                (dissoc registered-contract :forbidden-effects)))
         (is (not= (:forbidden-effects baseline-contract)
                   (:forbidden-effects registered-contract)))
         (is (= #{} (:forbidden-effects registered-contract))))
       (testing "effect-registry-entry leaves profile-contract unchanged"
         (is (= baseline-contract entry-contract)))
       (testing "effect-registry-entry changes permission-table metadata only"
         (is (not= baseline-table entry-table))
         (is (= (mapv (fn [row]
                        (dissoc row :family :requires-capability :capability))
                      baseline-table)
                (mapv (fn [row]
                        (dissoc row :family :requires-capability :capability))
                      entry-table)))
         (is (= [{:family :test/registry-entry
                  :requires-capability false
                  :capability nil}]
                (mapv (fn [row]
                        (select-keys
                         row [:family :requires-capability :capability]))
                      entry-table)))))))

(deftest profile-validation-facade-preserves-central-diagnostics-and-target-gates
  (with-redefs-fn (bootstrap-policy-redefs)
    #(let [denied (bootstrap/profile-validation-facts
                   (module-for :hosted :jvm #{:memory/raw})
                   (assoc typed-artifact :inferred-effects #{:memory/raw})
                   module-artifact)
           bad-target (bootstrap/profile-validation-facts
                       (module-for :hosted :wasm #{:io/write})
                       typed-artifact module-artifact)]
       (is (= :rejected (:status denied)))
       (is (= ["P1-EFFECT"]
              (mapv :diagnostic (:profile-diagnostics denied))))
       (is (= ["P1-BACKEND"]
              (mapv :diagnostic (:profile-diagnostics bad-target))))
       (is (every? (fn [diagnostic]
                     (some? (get-in diagnostic [:facts :source-span])))
                   (concat (:profile-diagnostics denied)
                           (:profile-diagnostics bad-target)))))))

(deftest profile-captured-original-interposition-is-one-shot
  (with-redefs-fn (bootstrap-policy-redefs)
    #(let [original bootstrap/profile-allowed-effects
           calls (atom 0)
           contract
           (with-redefs [bootstrap/profile-allowed-effects
                         (fn [profile-name]
                           (swap! calls inc)
                           (conj (profile/call-entrypoint-body
                                  :profile-allowed-effects
                                  original [profile-name])
                                 :test/extra))]
             (bootstrap/profile-contract :hosted))]
       (is (= 1 @calls))
       (is (contains? (:allowed-effects contract) :test/extra)))))

(deftest profile-leaf-operation-interposition-is-observable-through-facade
  (let [bindings (atom 0)
        original profile/with-operations]
    (with-redefs [profile/with-operations
                  (fn [operations thunk]
                    (swap! bindings inc)
                    (original operations thunk))]
      (with-redefs-fn (bootstrap-policy-redefs)
        #(bootstrap/profile-allowed-effects :hosted)))
    (is (= 1 @bindings))))
