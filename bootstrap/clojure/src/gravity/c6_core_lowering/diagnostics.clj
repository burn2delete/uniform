(ns gravity.c6-core-lowering.diagnostics
  "C6 override parsing and stable lowering diagnostics."
  (:require [gravity.c6-core-lowering.config :as config]
            [gravity.c6-core-lowering.context :as context]))

(defn c6-lowering-source-overrides [module]
  (get-in module [:metadata :compiler :c6-lowering] {}))

(defn c6-lowering-message [id]
  (case id
    "C6-LOWERING-GAP"
    "surface form cannot lower to core or a declared domain boundary"
    "C6-CORE-SHAPE" "core node is malformed"
    "C6-EVAL-ORDER" "lowering lost required evaluation-order facts"
    "C6-ORIGIN" "introduced core form lacks source or generated-origin links"
    "C6-EFFECT-DROP" "lowering erased effect or capability declarations"
    "C6-UNSAFE-DROP" "lowering erased unsafe metadata"
    "C6-DOMAIN-BOUNDARY" "domain IR boundary record is malformed"
    "C6-VERIFY" "core verifier rejected the lowered artifact"
    "AST and core lowering failed"))

(defn c6-lowering-fail! [id source-path subject extra]
  (context/fail!
   id
   ((context/op-fn :c6-lowering-message c6-lowering-message) id)
   (merge {:source-span (or (:source-span subject)
                            (:span subject)
                            (context/source-span source-path 0))
           :diagnostic-family :c6-ast-core-lowering
           :stage :core-lowering
           :document-id "C6"
           :expected-document
           (context/op-value :c6-lowering-governing-document
                             config/c6-lowering-governing-document)
           :syntax-id (:syntax-id subject)
           :core-node-id (:core-node-id subject)
           :generated-origin-chain (:generated-origin subject)
           :lowering-rule (:lowering-rule subject)
           :active-profile (:profile subject)
           :target (:target subject)
           :remediation
           "Lower expanded and resolved syntax into verified core nodes or declared domain IR boundary records while preserving source provenance, evaluation order, effects, capabilities, unsafe metadata, profile, and target facts."}
          extra)))

(defn c6-lowering-validate-overrides! [source-path module overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get (context/op-value
                        :c6-lowering-override-diagnostics
                        config/c6-lowering-override-diagnostics)
                       fail-kind)]
      ((context/op-fn :c6-lowering-fail! c6-lowering-fail!)
       id source-path
       {:source-span (context/source-span source-path 0)
        :syntax-id "fixture-override"
        :profile (:profile module)
        :target (:target module)
        :lowering-rule fail-kind}
       {:missing-fields [fail-kind]}))))
