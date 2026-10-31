(ns gravity.effects
  "Existing hosted seed effect/graph builders; Gravity retains effect semantics."
  (:require [clojure.set :as set]))

(def ^:private namespace-contract
  '{:namespace gravity.effects
    :contract-boundary :hosted-seed-effect-graph-data
    :governing-contracts [D1 D2 D3 D8 D9 L6 L15 C1 C7 C8 C15 BOOT8 TEST2]
    :public-api
    {fact-direct-effects
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
    {:requires [clojure.set] :consumed-by [gravity.c8-effect-checker.facts]
     :forbids [gravity.bootstrap gravity.cli gravity.release gravity.c8-effect-checker]}
    :artifact-inputs [:native-seed-type-facts :native-function-records :native-module-declarations
                      :explicit-classifier-and-selection-callbacks :replay-sensitive-effect-catalog]
    :artifact-outputs [:seed-direct-effect-sets :lazy-seed-effectful-fact-view :seed-effect-graph-data]
    :diagnostic-ownership
    {:stage [] :upstream [:type-function-and-source-data]
     :existing-c8-owner [:effect-rules :legality :capabilities :replay :ordering :verification]}
    :legacy-status-labels-authoritative? false
    :legacy-build-grants-prove-performed-effects? false
    :unsupported [:native-ingress-authentication :callback-validation :canonical-c8-legality
                  :call-effect-fixed-point :latent-body-inference :handlers-and-continuations
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

(doseq [operation [#'fact-direct-effects #'effectful-facts #'effect-graph]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
