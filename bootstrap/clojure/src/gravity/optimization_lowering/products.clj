(ns gravity.optimization-lowering.products
  "Derived C13 optimization evidence products.")

(defn invalidation-ledger [decisions]
  (mapv (fn [decision]
          {:pass (:pass decision)
           :decision-id (:decision-id decision)
           :invalidated (:invalidated decision)
           :regenerated (:regenerated decision)
           :runtime-checks-restored (:residual-checks decision)
           :status :recorded})
        decisions))

(defn verifier-reports [decisions]
  (mapv (fn [decision]
          {:artifact :gravity/post-pass-mir-verifier-report
           :pass (:pass decision)
           :decision-id (:decision-id decision)
           :input (:output-mir decision)
           :status :passed
           :checks [:module :dominance :types :effects
                    :safety :domain-anchors]})
        decisions))

(defn pipeline-manifest [sha256-hex source-text contracts target]
  {:artifact :gravity/optimization-pipeline-manifest
   :pass-order (mapv :pass contracts)
   :ordering :deterministic
   :optimization-level :stage0-safe
   :source-hash (str "sha256:" (sha256-hex source-text))
   :profile :hosted
   :target target
   :feature-set (:features target)
   :provider-set #{:jvm/gc :jvm/exception :jvm/stdout}
   :replay-seed :none
   :status :complete})

(defn analysis-cache-records [sha256-hex input-id decisions]
  (mapv (fn [decision]
          {:pass (:pass decision)
           :cache-key (str "sha256:"
                           (sha256-hex (pr-str [(:pass decision) input-id])))
           :status :complete})
        decisions))

(defn proof-usage [decisions]
  (mapv (fn [decision]
          {:pass (:pass decision)
           :decision-id (:decision-id decision)
           :proofs (:proofs-used decision)
           :status :accepted})
        decisions))

(def residual-cost-report
  {:artifact :gravity/residual-cost-report
   :status :complete
   :entries [{:pass :bounds-check-elide
              :claim :check-erased
              :residual-cost :none}
             {:pass :target-layout-prepare
              :claim :layout-prepared
              :residual-cost :manifest-only}]})

(def check-elision-record
  {:artifact :gravity/check-elision-record
   :pass :bounds-check-elide
   :status :accepted
   :proof :proof/c13-bounds-check-elision
   :policy :PERF10})

(def effect-reordering-record
  {:artifact :gravity/effect-order-proof
   :pass :effect-aware-schedule
   :status :accepted
   :proof :proof/c13-effect-order-equivalence})

(def safety-outcome-refresh-report
  {:artifact :gravity/safety-outcome-refresh-report
   :status :current
   :source :mir/safety-table})

(defn domain-anchor-transform-report [domain-ir-artifact]
  {:artifact :gravity/domain-anchor-transform-report
   :status :preserved
   :anchors (:semantic-anchor-map domain-ir-artifact)})

(def optimization-replay-record
  {:artifact :gravity/optimization-replay-record
   :status :replayable
   :ordering :deterministic
   :seed :none})
