(ns gravity.c9-ownership-checker.verification
  "Diagnostic, verifier, proof, and validation projections for hosted C9."
  (:require [clojure.set :as set]))

(defn ownership-diagnostics [source-span diagnostic-ids rejected-designs source-path ownership]
  {:artifact :gravity/c9-ownership-diagnostic-registry
   :required-diagnostic-ids diagnostic-ids
   :diagnostics
   (mapv (fn [design]
           {:diagnostic (:diagnostic design)
            :fixture (:fixture design)
            :value-id (or (some-> ownership :owners keys first) :fixture/value)
            :owner-id :fixture/owner :borrow-id :fixture/borrow
            :region-id :fixture/region :arena-generation :fixture/generation
            :resource-id :fixture/resource :control-path :fixture/path
            :source-span (source-span source-path 0)
            :generated-origin-chain [] :profile :fixture/profile :target :fixture/target
            :remediation "Keep ownership, lifetime, region, arena, and linear-resource facts explicit before safety analysis."})
         rejected-designs)
   :status :complete})

(defn linear-paths-exact? [linear]
  (every? (fn [[_ resource]]
            (every? #(= 1 (:terminal-count %)) (:terminal-paths resource)))
          (:resources linear)))

(defn verifier-report [linear-paths-exact? diagnostic-ids c8-artifact ownership borrow
                      lifetimes moves escape region arena linear transfer runtime unsafe diagnostics]
  (let [diagnostics? (= (set diagnostic-ids)
                        (set (map :diagnostic (:diagnostics diagnostics))))
        borrow-kinds (set (map :edge (:edges borrow)))
        transfer-boundaries (set (map :boundary (:records transfer)))
        c8-complete? (= :complete (get-in c8-artifact [:capability-based-proof :status]))
        graph-complete? (and (seq (:owners ownership)) (= :complete (:status ownership)))
        borrow-proven? (and (contains? borrow-kinds :immutable-borrow)
                            (contains? borrow-kinds :mutable-borrow)
                            (= :complete (:status borrow)))
        lifetimes-recorded? (and (seq (:intervals lifetimes)) (= :complete (:status lifetimes)))
        moves-recorded? (boolean (and (seq (:moves moves)) (seq (:consumes moves))))
        region-recorded? (and (seq (:regions region)) (seq (:arenas arena))
                              (= :complete (:status region)) (= :complete (:status arena)))
        linear-complete? (and (linear-paths-exact? linear) (= :complete (:status linear)))
        transfers-explicit? (set/subset? #{:function :actor :task :ffi} transfer-boundaries)
        runtime-gated? (every? :profile-legal? (:records runtime))]
    {:artifact :gravity/c9-ownership-verifier-report
     :c8-proof-complete? c8-complete?
     :ownership-graph-complete? graph-complete?
     :borrow-rules-proven? borrow-proven?
     :lifetime-intervals-recorded? lifetimes-recorded?
     :move-and-consume-recorded? moves-recorded?
     :escape-analysis-recorded? (= :complete (:status escape))
     :region-and-arena-recorded? region-recorded?
     :linear-flow-complete? linear-complete?
     :transfer-boundaries-explicit? transfers-explicit?
     :runtime-checks-profile-gated? runtime-gated?
     :unsafe-audit-references-recorded? (boolean (seq (:records unsafe)))
     :diagnostics-covered? diagnostics?
     :status (if (and c8-complete? (seq (:owners ownership))
                      (contains? borrow-kinds :immutable-borrow)
                      (contains? borrow-kinds :mutable-borrow)
                      (seq (:intervals lifetimes)) (seq (:moves moves))
                      (seq (:consumes moves)) (= :complete (:status escape))
                      (seq (:regions region)) (seq (:arenas arena))
                      (linear-paths-exact? linear) transfers-explicit? runtime-gated?
                      (seq (:records unsafe)) diagnostics?)
               :passed :failed)}))

(defn ownership-capability-proof [artifact]
  (let [verifier (:ownership-verifier-report artifact)]
    {:ownership-graph-complete? (:ownership-graph-complete? verifier)
     :borrow-rules-proven? (:borrow-rules-proven? verifier)
     :lifetime-intervals-recorded? (:lifetime-intervals-recorded? verifier)
     :move-and-consume-recorded? (:move-and-consume-recorded? verifier)
     :region-and-arena-recorded? (:region-and-arena-recorded? verifier)
     :linear-flow-complete? (:linear-flow-complete? verifier)
     :transfer-boundaries-explicit? (:transfer-boundaries-explicit? verifier)
     :runtime-checks-profile-gated? (:runtime-checks-profile-gated? verifier)
     :unsafe-audit-references-recorded? (:unsafe-audit-references-recorded? verifier)
     :diagnostics-covered? (:diagnostics-covered? verifier)
     :verifier-passed? (= :passed (:status verifier))
     :status :complete}))

(defn validate! [capability-proof fail! source-path artifact]
  (let [proof (capability-proof artifact)]
    (doseq [[field id] [[:ownership-graph-complete? "C9-USE-AFTER-MOVE"]
                        [:borrow-rules-proven? "C9-MUT-ALIAS"]
                        [:lifetime-intervals-recorded? "C9-BORROW-ESCAPE"]
                        [:move-and-consume-recorded? "C9-USE-AFTER-CONSUME"]
                        [:region-and-arena-recorded? "C9-REGION-ESCAPE"]
                        [:linear-flow-complete? "C9-LINEAR-LEAK"]
                        [:transfer-boundaries-explicit? "C9-TRANSFER"]
                        [:runtime-checks-profile-gated? "C9-RUNTIME-CHECK"]
                        [:unsafe-audit-references-recorded? "C9-UNSAFE"]
                        [:diagnostics-covered? "C9-UNSAFE"]
                        [:verifier-passed? "C9-UNSAFE"]]]
      (when-not (get proof field)
        (fail! id source-path {:stage :ownership-lifetime-region-check}
               {:missing-fields [field]}))))
  :complete)
