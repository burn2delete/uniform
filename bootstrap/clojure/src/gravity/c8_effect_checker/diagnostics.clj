(ns gravity.c8-effect-checker.diagnostics
  "C8 source overrides and diagnostic payload projection.")

(defn source-overrides [module]
  (get-in module [:metadata :compiler :c8-effect-check] {}))

(defn fail!
  [fail-operation effect-message source-span governing-document
   id source-path subject extra]
  (fail-operation
   id
   (effect-message id)
   (merge {:source-span (or (:source-span subject)
                            (get-in subject [:source :span])
                            (:span subject)
                            (source-span source-path 0))
           :diagnostic-family :c8-effect-checker
           :stage :effect-check
           :document-id "C8"
           :expected-document governing-document
           :core-node-id (or (:core-node-id subject) (:core-node subject))
           :generated-origin-chain (or (:generated-origin subject)
                                       (get-in subject [:source :origin-chain]))
           :function (:function subject)
           :namespace (:namespace subject)
           :effect (or (:effect subject) :unknown/effect)
           :capability (:capability subject)
           :profile (:profile subject)
           :target (:target subject)
           :provider (:provider subject)
           :grant (:grant subject)
           :remediation
           "Emit effect graph facts, legality intersection records, capability proofs, build/replay obligations, ordering constraints, residual effect records, and verifier-accepted diagnostics before MIR construction."}
          extra)))

(defn validate-overrides!
  [effect-fail! source-span override-diagnostics effect-capability
   source-path module overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get override-diagnostics fail-kind)]
      (effect-fail! id source-path
                    {:source-span (source-span source-path 0)
                     :core-node "fixture-override"
                     :function "fixture"
                     :namespace (:module module)
                     :effect fail-kind
                     :capability (get effect-capability fail-kind)
                     :profile (:profile module)
                     :target (:target module)
                     :provider :fixture/provider
                     :grant :fixture/grant
                     :generated-origin []}
                    {:missing-fields [fail-kind]}))))

(defn effect-diagnostics
  [source-span diagnostic-ids rejected-designs effect-capability
   source-path type-facts]
  {:artifact :gravity/c8-effect-diagnostic-registry
   :required-diagnostic-ids diagnostic-ids
   :diagnostics
   (mapv (fn [design]
           (let [fact (first type-facts)
                 effect (keyword "fixture" (:diagnostic design))]
             {:diagnostic (:diagnostic design)
              :fixture (:fixture design)
              :core-node-id (:core-node fact)
              :source-span (get-in fact [:source :span]
                                   (source-span source-path 0))
              :generated-origin-chain (get-in fact [:source :origin-chain])
              :function :fixture
              :namespace :fixture
              :effect effect
              :capability (get effect-capability effect)
              :profile (:profile fact)
              :target (:target fact)
              :provider :fixture/provider
              :grant :fixture/grant
              :remediation
              "Keep effect legality explicit before MIR construction."}))
         rejected-designs)
   :status :complete})
