(ns gravity.c17-plugin.proof
  "Capability proof projection for C17 plugin artifacts."
  (:require [clojure.set :as set]))

(defn capability-proof [config artifact]
  (let [manifest (:plugin-manifest artifact)
        trust-levels (set (map :trust (:trust-grants artifact)))
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:plugin-diagnostic-stream
                                       :diagnostics])))]
    {:c16-incremental-input-verified?
     (= :complete (get-in artifact
                          [:c16-incremental-artifact
                           :capability-based-proof :status]))
     :manifest-loaded?
     (and (= :gravity/compiler-plugin (:artifact manifest))
          (set/subset? (set (:manifest-required-fields config))
                       (set (keys manifest))))
     :api-compatible?
     (= :compatible (get-in artifact [:api-compatibility-report :status]))
     :sandbox-and-trust-grants?
     (and (contains? trust-levels :sandboxed)
          (contains? trust-levels :trusted-package)
          (every? #(contains? #{:sandboxed :granted} (:status %))
                  (:trust-grants artifact)))
     :capabilities-scoped?
     (and (contains? (:capabilities manifest) :compiler/ir-transform)
          (set/subset?
           #{:read-mir :write-mir :register-pass :emit-artifacts}
           (get-in manifest [:capability-scopes :compiler/ir-transform])))
     :build-effect-denial-covered?
     (= :denied-ungranted-effects
        (get-in artifact [:hermetic-build-effect-report :status]))
     :pass-contracts-registered?
     (every? #(and (= :registered (:status %))
                   (set/subset?
                    (set (:pass-contract-required-fields config))
                    (set (keys (:contract %)))))
             (:plugin-pass-registration-records artifact))
     :output-artifacts-verified?
     (every? #(= :passed (:verifier-result %))
             (:plugin-output-artifacts artifact))
     :domain-and-facet-registrations-verified?
     (and (every? #(= :registered (:status %))
                  (:domain-registration-records artifact))
          (every? #(= :registered (:status %))
                  (:facet-registration-records artifact)))
     :execution-trace-cache-key-integrated?
     (and (every? #(and (= :passed (:verifier-result %)) (:cache-key %))
                  (:plugin-execution-traces artifact))
          (every? #(set/subset?
                    (set (:cache-key-required-fields config))
                    (set (keys %)))
                  (:plugin-cache-keys artifact)))
     :diagnostics-covered?
     (= (set (:diagnostic-ids config)) diagnostics)
     :conformance-passed?
     (= :passed (get-in artifact [:plugin-conformance-results :status]))
     :status :complete}))
