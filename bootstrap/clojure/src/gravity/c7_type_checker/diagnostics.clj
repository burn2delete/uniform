(ns gravity.c7-type-checker.diagnostics
  "Stable C7 failure payloads and fixture override handling.")

(defn type-fail!
  [fail! source-span type-message governing-document
   id source-path subject extra]
  (fail! id
         (type-message id)
         (merge {:source-span (or (:source-span subject)
                                  (get-in subject [:source :span])
                                  (:span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :c7-type-checker
                 :stage :type-check
                 :document-id "C7"
                 :expected-document governing-document
                 :core-node-id (or (:core-node-id subject) (:node-id subject))
                 :syntax-id (or (:syntax-id subject)
                                (get-in subject [:source :syntax-id]))
                 :expected-type (or (:expected-type subject) "Typed")
                 :actual-type (or (:actual-type subject) "Dynamic")
                 :active-profile (:profile subject)
                 :target (:target subject)
                 :relevant-binding-id (:binding-ref subject)
                 :generated-origin-chain (or (:generated-origin subject)
                                             (get-in subject
                                                     [:source :origin-chain]))
                 :remediation "Emit typed-core facts, solved constraints, checked casts, dynamic boundary records, schema/layout links, generic and protocol evidence, and verifier-accepted diagnostics before effect checking."}
                extra)))

(defn validate-overrides!
  [source-span type-fail! override-diagnostics source-path module overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get override-diagnostics fail-kind)]
      (type-fail! id source-path
                  {:source-span (source-span source-path 0)
                   :syntax-id "fixture-override"
                   :core-node-id "fixture-override"
                   :expected-type "Expected"
                   :actual-type "Actual"
                   :profile (:profile module)
                   :target (:target module)
                   :generated-origin []}
                  {:missing-fields [fail-kind]}))))

(defn type-diagnostics
  [source-span diagnostic-ids rejected-designs source-path nodes]
  {:artifact :gravity/c7-type-diagnostic-registry
   :required-diagnostic-ids diagnostic-ids
   :diagnostics
   (mapv (fn [design]
           (let [node (first nodes)]
             {:diagnostic (:diagnostic design)
              :fixture (:fixture design)
              :core-node-id (:node-id node)
              :syntax-id (get-in node [:source :syntax-id])
              :source-span (get-in node [:source :span]
                                   (source-span source-path 0))
              :expected-type "Expected"
              :actual-type "Actual"
              :active-profile (:profile node)
              :target (:target node)
              :relevant-binding-id (:node-id node)
              :generated-origin-chain (get-in node [:source :origin-chain])
              :remediation "Keep C7 type facts explicit and profile-gated."}))
         rejected-designs)
   :status :complete})
