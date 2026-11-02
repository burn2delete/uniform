(ns gravity.p15-native-plan-specialization.artifact
  (:require [gravity.bootstrap :as bootstrap]))

(defn build
  [packet context emission helper-function helper-contract]
  (let [{:keys [source source-bytes source-content-hash expected-output bounds
                helper helper-safety helper-result]} emission
        compiler-record (:stage2-compiler-artifact-record packet)]
    {:artifact :gravity/p15-native-plan-specialization
     :schema-version 1
     :status :complete-for-internal-plan-specialized-native-child
     :input {:kind (:kind packet)
             :status (:status packet)
             :requested-target (:requested-target packet)
             :target-eligibility (:target-eligibility packet)
             :source-path (:source-path context)
             :source-content-hash (:source-content-hash context)}
     :authentication {:status :authenticated
                      :authenticator
                      'gravity.bootstrap/p15-s23-closed-runtime-packet-authentic?
                      :authenticator-arity 2
                      :contextual? true}
     :plan {:plan-id (get-in packet [:plan :plan-id])
            :content-hash
            (str "sha256:"
                 (bootstrap/sha256-hex
                  (pr-str
                   (bootstrap/c-backend-canonical-value
                    (:plan packet)))))
            :instruction-count (:instruction-count bounds)
            :validation-status :passed
            :validator 'gravity.bootstrap/c-backend-validate-runtime-plan!}
     :compiler {:artifact (:artifact compiler-record)
                :artifact-hash (:artifact-hash compiler-record)
                :source-content-hash (:source-content-hash compiler-record)
                :semantic-hash (:semantic-hash compiler-record)}
     :emitter {:emitter
               'gravity.p15-native-plan-specialization/gravity-source-c-emitter
               :source-rule-hash
               (get-in packet [:stage2-plan-emitter-rule :source-rule-hash])
               :status :emitted
               :semantic-owner :gravity-source
               :source-language :gravity
               :helper-source-path
               (get-in helper [:snapshot :source-path])
               :helper-source-content-hash
               (get-in helper [:snapshot :source-content-hash])
               :helper-function helper-function
               :helper-function-semantic-hash
               (:function-semantic-hash helper)
               :helper-contract helper-contract
               :helper-contract-hash (:contract-hash helper)
               :helper-safety-proof (:facts helper-safety)
               :helper-result-contract
               (select-keys helper-result
                            [:status :contract :implementation])}
     :driver {:driver-rule-hash
              (get-in packet [:stage2-compiler-driver-rule :driver-rule-hash])
              :record-status
              (get-in packet [:stage2-compiler-driver-record :status])}
     :runtime {:runtime-rule-hash
               (get-in packet [:stage2-runtime-rule :runtime-rule-hash])
               :runtime-artifact-hash
               (get-in packet [:stage2-runtime-rule :runtime-artifact-hash])
               :execution-status
               (get-in packet [:stage2-runtime-execution-record :status])}
     :generated-c {:dialect :c11
                   :source source
                   :bytes source-bytes
                   :content-hash source-content-hash
                   :implementation :gravity-source-emitted-plan-specialized-c
                   :provider-kind :host-c
                   :execution :not-run
                   :execution-evidence :external-focused-test-only}
     :expected-output expected-output
     :runner {:status :not-exposed
              :reason
              :public-c-backend-process-staging-not-reused-by-production-wrapper
              :compiler-and-process-boundary :clojure-bootstrap}
     :provenance {:selected-generated-child-clojure-seed-boundary? false
                  :selected-generated-child-jvm-available? false
                  :selected-runtime-clojure-seed-boundary? false
                  :selected-child-clojure-seed-boundary? false
                  :generic-host-c-packet-interpreter-used? false
                  :compiler-clojure-seed-boundary? true
                  :authentication-clojure-seed-boundary? true
                  :validator-clojure-seed-boundary? true
                  :c-emitter-clojure-seed-boundary? true
                  :c-emitter-semantic-owner :gravity-source
                  :c-emitter-source-language :gravity
                  :c-emitter-helper-executed? true
                  :c-emitter-pr-str-primitive-boundary? true
                  :artifact-clojure-seed-boundary? true
                  :artifact-construction-clojure-seed-boundary? true
                  :process-clojure-seed-boundary? true
                  :file-io-clojure-seed-boundary? true
                  :process-and-file-io-clojure-seed-boundary? true
                  :public-clojure-seed-boundary? true
                  :public-wrapper-clojure-seed-boundary? true
                  :global-clojure-seed-boundary? true}
     :claims {:provider-authored-in-gravity? false
              :compiler-authored-in-gravity? false
              :public-command-route? false
              :self-hosted? false
              :release-ready? false
              :backend-complete? false
              :full-language? false
              :formal-language-complete? false
              :source-content-hash-verified-by-provider? false}}))
