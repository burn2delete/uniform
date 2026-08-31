

(defn- sh07-proof-transaction-report
  [stage mode verifier-epoch artifact verify]
  (if-let [context (sh07-proof-transaction-context!)]
    (let [state @context
          phase (:phase state)
          root (sh07-proof-transaction-artifact-root stage artifact)
          catalog-key [stage mode]
          expected-catalog (get-in state [:check-catalogs catalog-key])
          receipt
          (some
           (fn [candidate]
             (when (and (= phase (:phase candidate))
                        (= stage (:stage candidate))
                        (= mode (:mode candidate))
                        (identical?
                         (:verifier-root verifier-epoch)
                         (get-in candidate [:verifier-epoch :verifier-root]))
                        (= (dissoc verifier-epoch :verifier-root)
                           (dissoc (:verifier-epoch candidate)
                                   :verifier-root))
                        (= expected-catalog
                           {:check-catalog (:check-catalog candidate)
                            :check-catalog-hash
                            (:check-catalog-hash candidate)})
                        (identical? artifact (:artifact candidate))
                        (= root (:artifact-root candidate)))
               candidate))
           (:receipts state))]
      (if receipt
        (do
          (swap! context update-in [:reuses stage] (fnil inc 0))
          (:report receipt))
        (let [report (verify)
              check-catalog (set (keys (:checks report)))
              check-catalog-hash
              (reader-canonical-hash
               {:domain (:check-catalog-domain verifier-epoch)
                :schema-version (:report-schema-version verifier-epoch)
                :checks (vec (sort check-catalog))})
              observed-catalog
              {:check-catalog check-catalog
               :check-catalog-hash check-catalog-hash}]
          (swap! context update-in [:executions stage] (fnil inc 0))
          (when (and expected-catalog
                     (not= expected-catalog observed-catalog))
            (throw
             (ex-info "SH-07 proof transaction verifier catalog changed"
                      {:id "C6-VERIFY"
                       :stage :sh07-proof-transaction
                       :reason :check-catalog-changed
                       :verifier-stage stage
                       :verifier-mode mode})))
          (swap! context update :check-catalogs
                 #(if (contains? % catalog-key)
                    %
                    (assoc % catalog-key observed-catalog)))
          (when-not (= :passed (:status report))
            (swap! context update :failed-report-executions (fnil inc 0)))
          (when (= :passed (:status report))
            (let [completed (java.util.IdentityHashMap.)]
              (when (and
                     (sh07-proof-transaction-immutable-carrier?
                      completed artifact)
                     (sh07-proof-transaction-immutable-carrier?
                      completed report))
                (swap!
                 context
                 (fn [current]
                   (when (>= (count (:receipts current))
                             (:maximum-receipts current))
                     (throw
                      (ex-info
                       "SH-07 proof transaction receipt bound exceeded"
                       {:id "C6-VERIFY"
                        :stage :sh07-proof-transaction
                        :reason :maximum-receipts
                        :maximum (:maximum-receipts current)})))
                   (update current :receipts conj
                           {:phase phase
                            :stage stage
                            :mode mode
                            :verifier-epoch verifier-epoch
                            :artifact artifact
                            :artifact-root root
                            :check-catalog check-catalog
                            :check-catalog-hash check-catalog-hash
                            :report report}))))))
          report)))
    (verify)))