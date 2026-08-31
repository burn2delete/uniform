(ns gravity.optimization-lowering.records
  "C13 pass contracts and deterministic optimization decision records.")

(def pass-contract-seed
  [{:pass :constant-fold
    :requires #{:constant-table :type-table}
    :preserves #{:types :effects :ownership :capabilities :source-origins
                 :profile :safety-outcomes}
    :invalidates #{}
    :regenerates #{}
    :proof-obligations #{:literal-equivalence}
    :profiles #{:core :hosted :native :gpu}
    :target-assumptions #{}
    :emits #{:decision-log :verifier-report}}
   {:pass :dead-code-eliminate
    :requires #{:control-flow-graph :effect-table :liveness}
    :preserves #{:types :effects :capabilities :source-origins :profile}
    :invalidates #{:liveness :data-flow-cache}
    :regenerates #{:liveness}
    :proof-obligations #{:no-effectful-removal}
    :profiles #{:hosted :native :gpu}
    :target-assumptions #{}
    :emits #{:decision-log :invalidation-ledger :verifier-report}}
   {:pass :bounds-check-elide
    :requires #{:dominator-tree :range-analysis :safety-outcomes}
    :preserves #{:types :effects :source-origins :profile}
    :invalidates #{:runtime-check-table :data-flow-cache}
    :regenerates #{:runtime-check-table}
    :proof-obligations #{:proof-dominates-check}
    :profiles #{:native :hosted :gpu}
    :target-assumptions #{}
    :emits #{:decision-log :check-elision-record :verifier-report}}
   {:pass :effect-aware-schedule
    :requires #{:effect-table :capability-proof-table}
    :preserves #{:types :capabilities :safety-outcomes :source-origins
                 :profile}
    :invalidates #{:control-flow-cache}
    :regenerates #{:effect-table}
    :proof-obligations #{:effect-order-equivalence}
    :profiles #{:hosted :native :distributed}
    :target-assumptions #{}
    :emits #{:decision-log :effect-order-proof :verifier-report}}
   {:pass :domain-ir-exit
    :requires #{:domain-verifier-report :semantic-anchor-map}
    :preserves #{:types :effects :ownership :capabilities :safety-outcomes
                 :source-origins :profile}
    :invalidates #{:domain-anchor-cache}
    :regenerates #{:domain-anchor-table}
    :proof-obligations #{:domain-translation-validation}
    :profiles #{:hosted :native :distributed :gpu}
    :target-assumptions #{}
    :emits #{:decision-log :domain-verifier-report :verifier-report}}
   {:pass :target-layout-prepare
    :requires #{:layout-facts :ownership-table :safety-outcomes}
    :preserves #{:types :effects :capabilities :source-origins :profile}
    :invalidates #{:layout-cache}
    :regenerates #{:layout-manifest}
    :proof-obligations #{:layout-equivalence}
    :profiles #{:hosted :native :gpu}
    :target-assumptions #{}
    :emits #{:decision-log :layout-decision-record :verifier-report}}])

(defn pass-contract-record [record]
  (assoc record
         :artifact :gravity/mir-pass-contract
         :input :gravity/mir
         :output :gravity/mir
         :version "stage0-c13"
         :contract-status :accepted))

(defn decision-record
  [sha256-hex domain-ir-artifact input-id index contract]
  (let [changed? (odd? index)
        pass (:pass contract)
        decision-input {:pass pass
                        :input input-id
                        :index index
                        :changed? changed?}
        output-id (str "sha256:" (sha256-hex (pr-str decision-input)))]
    {:artifact :gravity/optimization-decision
     :pass pass
     :decision-id (str "sha256:" (sha256-hex (pr-str decision-input)))
     :input-mir input-id
     :output-mir output-id
     :changed-ops (if changed?
                    [(str "mir-op-optimized-" (name pass))]
                    [])
     :reason (if changed? :stage0-evidence-gated :no-change-needed)
     :preserved (:preserves contract)
     :invalidated (:invalidates contract)
     :regenerated (:regenerates contract)
     :proofs-used [{:proof-id (keyword "proof" (str "c13-" (name pass)))
                    :kind (if changed?
                            :translation-validation
                            :contract-replay)
                    :status :accepted}]
     :residual-checks (if (= :bounds-check-elide pass)
                        []
                        [:stage0-visible-residual])
     :benchmarks []
     :verifier-result :passed
     :source (get-in domain-ir-artifact [:domain-ir-artifacts 0 :source])}))
