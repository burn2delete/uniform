(ns gravity.effects
  "Existing hosted seed effect/graph builders; Gravity retains effect semantics."
  (:require [clojure.set :as set]))

(def ^:private namespace-contract
  '{:namespace gravity.effects
    :contract-boundary :hosted-seed-effect-graph-data-and-native-reports
    :governing-contracts [D1 D2 D3 D8 D9 L6 L15 C1 C7 C8 C15 BOOT8 TEST2]
    :public-api
    {
     native-verifier-report
     {:arglists ([known-effects diagnostic-ids module effect-graph legality capability-proof build-log replay ordering residual diagnostics])
      :admission :none :seed-compatibility-data-only? true :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     native-capability-proof
     {:arglists ([artifact]) :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     validate-native-report!
     {:arglists ([capability-proof effect-fail! source-path artifact]) :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false :trusted-callbacks? true}
     fact-direct-effects
     {:arglists ([fact]) :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     effectful-facts
     {:arglists ([direct-effects type-facts]) :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}
     effect-graph
     {:arglists ([effectful-facts direct-effects replay-sensitive-effects module type-facts functions])
      :admission :none :seed-compatibility-data-only? true
      :authenticates-inputs? false :proves-effect-legality? false :grants-authority? false}}
    :dependency-direction
    {:requires [clojure.set] :consumed-by [gravity.c8-effect-checker.facts gravity.c8-effect-checker.verification]
     :forbids [gravity.bootstrap gravity.cli gravity.release gravity.c8-effect-checker]}
    :artifact-inputs [:native-seed-type-facts :native-function-records :native-module-declarations
                      :explicit-classifier-and-selection-callbacks :replay-sensitive-effect-catalog :native-effect-catalog-and-diagnostic-ids :native-legality-capability-build-replay-ordering-residual-and-diagnostic-records :trusted-projection-and-failure-callbacks]
    :artifact-outputs [:seed-direct-effect-sets :lazy-seed-effectful-fact-view :seed-effect-graph-data :native-verifier-report :legacy-capability-projection :complete-marker-or-native-report-refusal]
    :diagnostic-ownership
    {:stage [C8-VERIFY C8-UNDECLARED C8-PROFILE C8-CAPABILITY C8-BUILD C8-REPLAY C8-ORDER C8-RUNTIME] :upstream [:type-function-and-source-data]
     :existing-c8-owner [:effect-rules :legality :capabilities :replay :ordering :source-admission-and-diagnostic-publication]}
    :legacy-status-labels-authoritative? false
    :legacy-build-grants-prove-performed-effects? false
    :unsupported [:native-ingress-authentication :callback-validation :canonical-c8-legality
                  :pure-native-graph-ordering-gap :canonical-diagnostic-coverage :untrusted-report-authentication :call-effect-fixed-point :latent-body-inference :handlers-and-continuations
                  :performed-build-effects :package-deployment-provider-authority :profile-and-safety-legality
                  :full-source-pipeline :authority-admission :migration :release :self-hosting :seed-retirement]})

(defn fact-direct-effects [fact]
  (set/union (set (:effects fact))
             (case (:type fact)
               "CheckedCast[String]" #{:runtime/dynamic-dispatch}
               "ProtocolValue" #{:runtime/dynamic-dispatch}
               "SchemaDerived" #{:runtime/dynamic-dispatch}
               "UnsafeIsland[Dynamic]" #{:memory/raw}
               "Never" #{:error/throw}
               #{})))

(defn effectful-facts [direct-effects type-facts]
  (filter #(seq (direct-effects %)) type-facts))

(defn effect-graph
  [effectful-facts direct-effects replay-sensitive-effects
   module type-facts functions]
  (let [effectful (effectful-facts type-facts)]
    {:artifact :gravity/c8-effect-graph
     :module (:module module)
     :nodes (into (sorted-map)
                  (map (fn [fact]
                         (let [direct (direct-effects fact)]
                           [(:core-node fact)
                            {:direct direct
                             :latent #{}
                             :transitive direct
                             :ordering (if (seq direct) :sequence :pure)
                             :source (:source fact)}]))
                       type-facts))
     :functions (into (sorted-map)
                      (map (fn [fn-record]
                             [(:fn-id fn-record)
                              {:declared (set (:latent-effects fn-record))
                               :inferred (set (:latent-effects fn-record))
                               :latent (set (:latent-effects fn-record))
                               :throws (:throws fn-record)}])
                           (:functions functions)))
     :namespace {:declared (:effects module)
                 :inferred (set (mapcat direct-effects type-facts))}
     :build-effects (vec (sort-by str
                                  (get-in module
                                          [:metadata :build-grants] #{})))
     :replay-required
     (set/intersection replay-sensitive-effects
                       (set (mapcat direct-effects effectful)))
     :diagnostics []
     :status :complete}))

(defn native-verifier-report
  [known-effects diagnostic-ids module effect-graph legality capability-proof
   build-log replay ordering residual diagnostics]
  (let [inferred (get-in effect-graph [:namespace :inferred])
        declared (:effects module)
        known? (set/subset? inferred known-effects)
        declared? (set/subset? inferred declared)
        legality? (every? #(= :accepted (:result %)) (:records legality))
        capabilities? (every? #(= :accepted (:status %))
                              (:records capability-proof))
        build? (every? #(= :accepted (:status %)) (:records build-log))
        replay? (or (empty? (:replay-required effect-graph))
                    (seq (:records replay)))
        order? (seq (:records ordering))
        residual? (= :complete (:status residual))
        diagnostics? (= (set diagnostic-ids)
                        (set (map :diagnostic (:diagnostics diagnostics))))]
    {:artifact :gravity/c8-effect-verifier-report
     :every-effectful-node-recorded? (boolean (seq (:nodes effect-graph)))
     :known-effects? known?
     :declarations-cover-inferred-effects? declared?
     :legality-intersections-accepted? legality?
     :capability-proofs-accepted? capabilities?
     :build-effects-authorized? build?
     :replay-obligations-recorded? (boolean replay?)
     :ordering-constraints-recorded? (boolean order?)
     :residual-effects-recorded? residual?
     :diagnostics-covered? diagnostics?
     :status (if (and known? declared? legality? capabilities? build?
                      replay? order? residual? diagnostics?)
               :passed
               :failed)}))

(defn native-capability-proof [artifact]
  (let [verifier (:effect-verifier-report artifact)]
    {:effect-graph-complete?
     (:every-effectful-node-recorded? verifier)
     :declared-effect-allowance-checked?
     (:declarations-cover-inferred-effects? verifier)
     :legality-intersection-recorded?
     (:legality-intersections-accepted? verifier)
     :capability-proofs-accepted?
     (:capability-proofs-accepted? verifier)
     :build-effects-separated-and-authorized?
     (:build-effects-authorized? verifier)
     :replay-obligations-recorded?
     (:replay-obligations-recorded? verifier)
     :ordering-constraints-recorded?
     (:ordering-constraints-recorded? verifier)
     :residual-effects-recorded?
     (:residual-effects-recorded? verifier)
     :diagnostics-covered?
     (:diagnostics-covered? verifier)
     :verifier-passed?
     (= :passed (:status verifier))
     :status :complete}))

(defn validate-native-report! [capability-proof effect-fail! source-path artifact]
  (let [proof (capability-proof artifact)]
    (doseq [[field id] [[:effect-graph-complete? "C8-VERIFY"]
                        [:declared-effect-allowance-checked? "C8-UNDECLARED"]
                        [:legality-intersection-recorded? "C8-PROFILE"]
                        [:capability-proofs-accepted? "C8-CAPABILITY"]
                        [:build-effects-separated-and-authorized? "C8-BUILD"]
                        [:replay-obligations-recorded? "C8-REPLAY"]
                        [:ordering-constraints-recorded? "C8-ORDER"]
                        [:residual-effects-recorded? "C8-RUNTIME"]
                        [:diagnostics-covered? "C8-VERIFY"]
                        [:verifier-passed? "C8-VERIFY"]]]
      (when-not (get proof field)
        (effect-fail! id source-path {:stage :effect-check}
                      {:missing-fields [field]}))))
  :complete)

(doseq [operation [#'native-verifier-report #'native-capability-proof #'validate-native-report! #'fact-direct-effects #'effectful-facts #'effect-graph]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
