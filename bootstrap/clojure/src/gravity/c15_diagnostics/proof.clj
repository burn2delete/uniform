(ns gravity.c15-diagnostics.proof
  (:require [gravity.c15-diagnostics.records :as records]))
(defn capability-proof [configuration artifact]
  (let [diagnostics (get-in artifact [:diagnostic-stream :diagnostics])]
    {:c14-lowering-input-verified? (= :complete (get-in artifact [:c14-lowering-artifact :capability-based-proof :status]))
     :diagnostic-schema-complete? (= :complete (get-in artifact [:diagnostic-schema :status]))
     :diagnostic-stream-deterministic? (= diagnostics (vec (sort-by :ordering-key diagnostics)))
     :stable-ids? (every? #(= (:diagnostic-id %) (records/stable-id (dissoc % :diagnostic-id :ordering-key))) diagnostics)
     :locations-and-origins-linked?
     (every? #(and (get-in % [:primary :span]) (get-in % [:primary :syntax-id]) (get-in % [:primary :artifact])
                    (or (not (:generated? %)) (and (seq (:origin-chain %))
                                                    (some (fn [related] (= :generated-by (:role related))) (:related %))))) diagnostics)
     :facts-structured? (every? #(and (map? (:facts %)) (seq (:facts %))) diagnostics)
     :remediation-and-quick-fixes? (and (every? #(seq (:remediation %)) diagnostics)
                                        (every? #(= :available (:status %)) (:remediation-and-quick-fix-records artifact)))
     :redaction-public-safe? (true? (get-in artifact [:redaction-report :public-safe?]))
     :renderers-covered? (= #{:cli :ide :ci :safety-report :package-report}
                             (set (map :renderer (:rendering-records artifact))))
     :golden-fixtures-matched? (every? #(= :matched (:status %)) (:golden-diagnostic-fixtures artifact))
     :diagnostics-covered? (= (set (:c15-diagnostics-diagnostic-ids configuration))
                               (set (map :rule (:golden-diagnostic-fixtures artifact))))
     :status :complete}))
