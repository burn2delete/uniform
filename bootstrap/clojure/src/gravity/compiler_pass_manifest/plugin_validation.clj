(ns gravity.compiler-pass-manifest.plugin-validation
  "Compiler plugin manifest, capability, and output validation."
  (:require [clojure.set :as set]
            [gravity.compiler-pass-manifest.contracts :as contracts]
            [gravity.compiler-pass-manifest.failures :as failures]))

(defn compiler-pass-validate-plugins!
  [source-path manifest suite]
  (let [plugin (:plugin-manifest suite)
        missing-fields (failures/compiler-pass-missing-fields
                        plugin
                        [:artifact :plugin :package :api-version
                         :compiler-compatibility :trust :profile
                         :build-effects :capabilities :capability-scopes
                         :passes :emits :conformance])]
    (when (seq missing-fields)
      (failures/compiler-pass-fail! "C17-MANIFEST" source-path manifest plugin
                           {:missing-fields missing-fields
                            :remediation "Load plugin manifests before code and reject missing identity, policy, authority, pass, or conformance fields."})))
  (let [plugin (:plugin-manifest suite)]
    (when-not (= "1" (:api-version plugin))
      (failures/compiler-pass-fail! "C17-API" source-path manifest plugin
                           {:remediation "Check compiler plugin API compatibility before loading plugin code."}))
    (let [granted (get-in plugin [:capability-scopes :compiler/ir-transform])
          requested (:requested-scopes plugin)]
      (when-not (set/subset? (set requested) (set granted))
        (failures/compiler-pass-fail! "C17-CAPABILITY" source-path manifest plugin
                             {:requested-capability :compiler/ir-transform
                              :scope requested
                              :remediation "Scope compiler capabilities to artifact kinds, pass phases, and package policy."}))))
  (doseq [contract (:plugin-pass-contracts suite)]
    (let [missing-fields (failures/compiler-pass-missing-fields
                          contract contracts/compiler-pass-contract-required-fields)]
      (when (seq missing-fields)
        (failures/compiler-pass-fail! "C17-PASS-CONTRACT" source-path manifest
                             contract
                             {:missing-fields missing-fields
                              :remediation "Plugin passes must declare the same contract fields as built-in compiler passes."}))))
  (doseq [trace (:plugin-execution-traces suite)]
    (when-not (= :passed (:verifier-result trace))
      (failures/compiler-pass-fail! "C17-OUTPUT" source-path manifest trace
                           {:remediation "Plugin output must pass the declared output verifier before the artifact can continue."})))
  :complete)
