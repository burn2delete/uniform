(ns gravity.bootstrap-compatibility.capability-validation-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.capability-validation :as capability]))

(def provider-specs
  {:io/stdout {:provider 'gravity.io/stdout-host
               :profiles #{:hosted :native}}
   :memory/raw {:provider 'gravity.memory/raw-unsafe
                :profiles #{:native}}})

(def profile-output
  {:artifact :gravity/profile-valid-core
   :effected-core {:forms []}
   :profile :hosted
   :target :jvm
   :declared-capabilities #{:io/stdout}
   :required-capabilities #{:io/stdout}
   :source-spans [{:source "synthetic/capability.gravity"
                   :line 1}]
   :status :accepted})

(def profile-report
  {:artifact :gravity/profile-validation-report
   :profile :hosted
   :target :jvm
   :diagnostics []
   :status :accepted})

(def stdout-grant
  {:grant-id :grant/io-stdout
   :provider 'gravity.io/stdout-host
   :actual-scope #{:stdout}
   :requested-scope #{:stdout}
   :phase :runtime
   :requested-phase :runtime
   :scope-satisfied? true})

(def all-grants
  {:package {:io/stdout stdout-grant}
   :provider {:io/stdout stdout-grant}
   :deployment {:io/stdout stdout-grant}})

(def trusted-provider
  {:io/stdout {:provider 'gravity.io/stdout-host
               :trusted? true
               :trust-level :trusted
               :status :selected}})

(defn linked-report
  [output]
  (assoc profile-report
         :profile (:profile output)
         :target (:target output)
         :status (:status output)))

(defn bootstrap-policy-redefs
  []
  {#'bootstrap/provider-specs provider-specs})

(defn leaf-operations
  []
  {:stable-set bootstrap/stable-set
   :stable-vec bootstrap/stable-vec
   :diagnostic-record
   (fn [id facts]
     {:artifact :gravity/capability-diagnostic
      :diagnostic id
      :stage :capability-validation
      :facts facts
      :status :rejected})
   :capability-diagnostic-ids bootstrap/capability-diagnostic-ids
   :provider-specs provider-specs})

(defn direct-facts
  [output grants providers]
  (capability/with-operations
   (leaf-operations)
   #(capability/capability-validation-facts
     output (linked-report output) grants providers)))

(deftest capability-facades-preserve-arglists-and-explicit-pass-parity
  (is (= '([capability])
         (:arglists (meta #'bootstrap/provider-name))))
  (is (= '([profile-output profile-report grant-facts provider-facts])
         (:arglists (meta #'bootstrap/capability-validation-facts))))
  (with-redefs-fn (bootstrap-policy-redefs)
    #(let [wrapped (bootstrap/capability-validation-facts
                    profile-output profile-report all-grants trusted-provider)
           direct (direct-facts profile-output all-grants trusted-provider)]
       (is (= direct wrapped))
       (is (= :accepted (:status wrapped)))
       (is (= #{:io/stdout}
              (get-in wrapped [:capability-valid-core
                               :effective-capabilities]))))))

(deftest capability-final-authority-narrows-trust-without-rewriting-legacy-row
  (with-redefs-fn (bootstrap-policy-redefs)
    #(let [untrusted (assoc-in trusted-provider [:io/stdout :trusted?] false)
           narrowed (bootstrap/capability-validation-facts
                     profile-output profile-report all-grants untrusted)
           legacy-effective {:source #{:io/stdout}
                             :required #{:io/stdout}
                             :profile #{:io/stdout}
                             :package #{:io/stdout}
                             :provider #{:io/stdout}
                             :deployment #{:io/stdout}
                             :effective #{:io/stdout}}
           legacy-row (first (bootstrap/capability-permission-table
                              {:capabilities #{:io/stdout}}
                              #{:io/stdout}
                              legacy-effective))]
       ;; Trust narrows the final authority only.
       (is (empty? (get-in narrowed
                           [:capability-valid-core :effective-capabilities])))
       (is (= #{:io/stdout}
              (get-in narrowed
                      [:capability-valid-core :grant-effective-capabilities])))
       (is (= :rejected
              (get-in narrowed
                      [:capability-validation-report
                       :capability-permission-table 0
                       :provider-trust-state])))
       (is (= :allowed (:state legacy-row)))
       (is (= :effective (:policy-layer legacy-row)))
       (is (not (contains? legacy-row :provider-trust-state))))))

(deftest capability-policy-and-provider-seams-remain-interposable
  (with-redefs-fn (bootstrap-policy-redefs)
    #(let [profile-calls (atom [])
           provider-calls (atom [])
           original-profile-capabilities bootstrap/profile-capabilities
           original-provider-name bootstrap/provider-name]
       (with-redefs [bootstrap/profile-capabilities
                     (fn [profile-name]
                       (swap! profile-calls conj profile-name)
                       (original-profile-capabilities profile-name))
                     bootstrap/provider-name
                     (fn [capability]
                       (swap! provider-calls conj capability)
                       (original-provider-name capability))]
         (let [result (bootstrap/capability-validation-facts
                       profile-output profile-report
                       all-grants trusted-provider)]
           (is (= :accepted (:status result)))
           (is (seq @profile-calls))
           (is (seq @provider-calls)))))))

(deftest capability-diagnostic-policy-scalar-reaches-leaf-pass-contract
  (is (= ["L15-CAPABILITY-MISSING" "L15-PROVIDER-MISSING"
          "L15-PROFILE" "L15-SCOPE" "L15-PHASE" "L15-TRUST"]
         bootstrap/capability-diagnostic-ids))
  (with-redefs-fn (bootstrap-policy-redefs)
    #(with-redefs [bootstrap/capability-diagnostic-ids
                   ["L15-CUSTOM-MISSING" "L15-CUSTOM-PROFILE"]]
       (let [result (bootstrap/capability-validation-facts
                     profile-output profile-report all-grants
                     trusted-provider)]
         (is (= ["L15-CUSTOM-MISSING" "L15-CUSTOM-PROFILE"]
                (get-in result [:pass :rejects])))))))

(deftest capability-diagnostics-preserve-source-context-and-stable-ids
  (with-redefs-fn (bootstrap-policy-redefs)
    #(let [scope-denied
           (bootstrap/capability-validation-facts
            profile-output profile-report
            (assoc-in all-grants [:package :io/stdout :scope-satisfied?]
                      false)
            trusted-provider)
           phase-denied
           (bootstrap/capability-validation-facts
            profile-output profile-report
            (assoc-in all-grants [:package :io/stdout :phase] :build)
            trusted-provider)
           trust-denied
           (bootstrap/capability-validation-facts
            profile-output profile-report all-grants
            (assoc-in trusted-provider [:io/stdout :trusted?] false))]
       (is (= ["L15-SCOPE"]
              (mapv :diagnostic (:capability-diagnostics scope-denied))))
       (is (= ["L15-PHASE"]
              (mapv :diagnostic (:capability-diagnostics phase-denied))))
       (is (= ["L15-TRUST"]
              (mapv :diagnostic (:capability-diagnostics trust-denied))))
       (let [scope-facts (-> scope-denied :capability-diagnostics first :facts)
             phase-facts (-> phase-denied :capability-diagnostics first :facts)
             trust-facts (-> trust-denied :capability-diagnostics first :facts)]
         (is (= :io/stdout (:requested-capability scope-facts)))
         (is (= :grant/io-stdout (:grant-id scope-facts)))
         (is (= #{:stdout} (:scope scope-facts)))
         (is (= #{:stdout} (:requested-scope scope-facts)))
         (is (= :runtime (:phase scope-facts)))
         (is (= :runtime (:grant-phase scope-facts)))
         (is (= :build (:grant-phase phase-facts)))
         (is (= 'gravity.io/stdout-host
                (:selected-or-missing-provider trust-facts)))
         (is (= :grant/io-stdout (:nearest-grant trust-facts))))
       (is (every? (fn [diagnostic]
                     (some? (get-in diagnostic [:facts :source-span])))
                   (concat (:capability-diagnostics scope-denied)
                           (:capability-diagnostics phase-denied)
                           (:capability-diagnostics trust-denied)))))))

(defn head-provider-name
  [specs capability]
  (get-in specs [capability :provider]))

(deftest capability-provider-name-matches-head-4921fbc-reference-table
  ;; Keep this parity table independent of the leaf: it captures the exact
  ;; legacy provider-name projection from HEAD 4921fbc.
  (with-redefs-fn (bootstrap-policy-redefs)
    #(doseq [[capability _] provider-specs]
       (is (= (head-provider-name provider-specs capability)
              (bootstrap/provider-name capability))))))

(deftest capability-captured-original-provider-interposition-is-one-shot
  (with-redefs-fn (bootstrap-policy-redefs)
    #(let [original bootstrap/provider-name
           calls (atom 0)
           result
           (with-redefs [bootstrap/provider-name
                         (fn [capability]
                           (swap! calls inc)
                           (original capability))]
             (bootstrap/capability-validation-facts
              profile-output profile-report all-grants trusted-provider))]
       (is (= :accepted (:status result)))
       (is (pos? @calls))
       ;; The wrapper's operation guard prevents recursive interposition from
       ;; multiplying a captured original call.
       (is (= 1 @calls)))))
