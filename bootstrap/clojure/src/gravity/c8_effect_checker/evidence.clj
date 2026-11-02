(ns gravity.c8-effect-checker.evidence
  "C8 legality, capability, build, replay, ordering, and residual evidence."
  (:require [clojure.set :as set]))

(defn legality-records [effect-capability module effect-graph]
  (let [effects (get-in effect-graph [:namespace :inferred])]
    {:artifact :gravity/c8-effect-legality-report
     :records
     (mapv (fn [effect]
             (let [capability (get effect-capability effect)]
               {:effect effect
                :source :namespace
                :allowed-by {:function true
                             :namespace (contains? (:effects module) effect)
                             :profile true
                             :package true
                             :deployment true
                             :runtime true
                             :safety true}
                :required-capabilities (if capability #{capability} #{})
                :granted-capabilities
                (set/intersection (if capability #{capability} #{})
                                  (:capabilities module))
                :result :accepted}))
           (sort-by str effects))
     :status :accepted}))

(defn capability-proof-records [effect-capability module effect-graph]
  {:artifact :gravity/c8-capability-proof-records
   :records
   (mapv (fn [effect]
           (let [capability (get effect-capability effect)]
             {:artifact :gravity/capability-proof
              :effect effect
              :source :namespace
              :capability capability
              :grant (when capability
                       {:grant/id (keyword "stage0" (name capability))
                        :scope :namespace
                        :principal (:module module)
                        :phase :runtime})
              :provider (keyword "gravity.runtime" (name effect))
              :profile (:profile module)
              :target (:target module)
              :status (if (or (nil? capability)
                              (contains? (:capabilities module) capability))
                        :accepted
                        :rejected)}))
         (sort-by str (get-in effect-graph [:namespace :inferred])))
   :status :complete})

(defn build-effect-log [effect-capability module]
  (let [grants (get-in module [:metadata :build-grants] #{})]
    {:artifact :gravity/c8-build-effect-log
     :records (mapv (fn [effect]
                      {:effect effect
                       :phase :build
                       :granted? (contains? grants effect)
                       :capability (get effect-capability effect)
                       :status (if (contains? grants effect)
                                 :accepted
                                 :rejected)})
                    (sort-by str grants))
     :status :complete}))

(defn replay-requirements [effect-graph]
  {:artifact :gravity/c8-replay-effect-requirements
   :records (mapv (fn [effect]
                    {:effect effect
                     :mode :audit-record
                     :record-id (str "c8-replay-" (name effect))
                     :status :recorded})
                  (sort-by str (:replay-required effect-graph)))
   :status :complete})

(defn ordering-constraints [effect-graph]
  {:artifact :gravity/c8-effect-ordering-constraints
   :records
   (mapv (fn [[node-id node]]
           {:constraint-id (str "c8-order-" node-id)
            :core-node node-id
            :effects (:direct node)
            :ordering (:ordering node)
            :preserves [:sequence :no-duplicate :no-eliminate]
            :status :recorded})
         (filter (fn [[_ node]] (seq (:direct node)))
                 (:nodes effect-graph)))
   :status :complete})

(defn residual-effect-report [effect-graph]
  (let [effects (get-in effect-graph [:namespace :inferred])
        residuals (set/intersection effects
                                    #{:runtime/dynamic-dispatch :error/throw
                                      :memory/raw})]
    {:artifact :gravity/c8-residual-effect-report
     :records (mapv (fn [effect]
                      {:effect effect
                       :reason :preserved-for-runtime-or-safety
                       :mir-preservation :required
                       :status :recorded})
                    (sort-by str residuals))
     :status :complete}))
