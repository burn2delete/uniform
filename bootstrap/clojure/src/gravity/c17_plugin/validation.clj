(ns gravity.c17-plugin.validation
  "Verifier-backed validation of C17 plugin evidence."
  (:require [clojure.set :as set]))

(defn validate! [config source-path artifact]
  (let [manifest (:plugin-manifest artifact)
        manifest-fields (set (keys manifest))
        pass-contract-fields (set (:pass-contract-required-fields config))
        diagnostics (get-in artifact [:plugin-diagnostic-stream :diagnostics])
        diagnostic-ids (set (map :diagnostic diagnostics))
        trust-levels (set (map :trust (:trust-grants artifact)))
        fail! (:plugin-fail! config)]
    (when-not (set/subset? (set (:manifest-required-fields config))
                           manifest-fields)
      (fail! "C17-MANIFEST" source-path manifest
             {:missing-fields
              (vec (remove manifest-fields
                           (:manifest-required-fields config)))}))
    (when-not (= :compatible (get-in artifact
                                     [:api-compatibility-report :status]))
      (fail! "C17-API" source-path (:api-compatibility-report artifact)
             {:missing-fields [:api-compatibility-report]}))
    (when-not (and (contains? (:capabilities manifest)
                              :compiler/ir-transform)
                   (set/subset?
                    #{:read-mir :write-mir :register-pass :emit-artifacts}
                    (get-in manifest
                            [:capability-scopes :compiler/ir-transform])))
      (fail! "C17-CAPABILITY" source-path manifest
             {:missing-fields [:capability-scopes]}))
    (when-not (= :denied-ungranted-effects
                 (get-in artifact [:hermetic-build-effect-report :status]))
      (fail! "C17-BUILD-EFFECT" source-path
             (:hermetic-build-effect-report artifact)
             {:missing-fields [:hermetic-build-effect-report]}))
    (when-not (and (contains? trust-levels :sandboxed)
                   (contains? trust-levels :trusted-package)
                   (every? #(contains? #{:sandboxed :granted} (:status %))
                           (:trust-grants artifact)))
      (fail! "C17-SANDBOX" source-path (first (:trust-grants artifact))
             {:missing-fields [:trust-grants]}))
    (doseq [registration (:plugin-pass-registration-records artifact)]
      (when-not (set/subset? pass-contract-fields
                             (set (keys (:contract registration))))
        (fail! "C17-PASS-CONTRACT" source-path registration
               {:missing-fields
                (vec (remove (set (keys (:contract registration)))
                             pass-contract-fields))})))
    (when-not (every? #(= :passed (:verifier-result %))
                      (:plugin-output-artifacts artifact))
      (fail! "C17-OUTPUT" source-path
             (first (:plugin-output-artifacts artifact))
             {:missing-fields [:verifier-result]}))
    (when-not (every? #(and (:schema %) (:verifier %)
                            (seq (:supported-profiles %))
                            (seq (:lowering-paths %)))
                      (:domain-registration-records artifact))
      (fail! "C17-DOMAIN" source-path
             (first (:domain-registration-records artifact))
             {:missing-fields [:schema :verifier
                               :supported-profiles :lowering-paths]}))
    (when-not (every? #(and (:schema %) (:verifier %)
                            (seq (:supported-profiles %))
                            (seq (:conformance-fixtures %)))
                      (:facet-registration-records artifact))
      (fail! "C17-FACET" source-path
             (first (:facet-registration-records artifact))
             {:missing-fields [:schema :verifier
                               :supported-profiles :conformance-fixtures]}))
    (when-not (= :accepted (get-in artifact [:package-trust-report :status]))
      (fail! "C17-TRUST" source-path (:package-trust-report artifact)
             {:missing-fields [:package-trust-report]}))
    (when-not (= (set (:diagnostic-ids config)) diagnostic-ids)
      (fail! "C17-MANIFEST" source-path (:plugin-diagnostic-stream artifact)
             {:missing-fields [:plugin-diagnostics]})))
  :complete)
