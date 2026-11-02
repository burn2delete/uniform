(ns gravity.c8-effect-checker.verification
  "C8 artifact verification and capability-proof projection."
  (:require [clojure.set :as set]))

(defn verifier-report
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

(defn capability-proof [artifact]
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

(defn validate! [capability-proof effect-fail! source-path artifact]
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
