(ns gravity.core-ast-lowering.artifact
  "Projection of lowered roots into the legacy Stage0 core artifact.")

(defn core-source-artifact
  [ops source-path source-text]
  (let [{:keys [macro-source-artifact lower-core-expr flatten-core]} ops
        macro-artifact (macro-source-artifact source-path source-text)
        module (:module macro-artifact)
        expanded-syntax (:expanded-syntax-object-stream macro-artifact)
        ;; The seed treats the first expanded form as the namespace form.
        body-syntax (subvec expanded-syntax 1)
        counter (atom 0)
        roots (mapv #(lower-core-expr counter module % (:form %) {})
                    body-syntax)
        flat (vec (mapcat flatten-core roots))
        source-map (mapv #(select-keys % [:node-id :kind :source-span
                                          :generated-origin])
                         flat)
        form-kinds (mapv #(select-keys % [:node-id :kind :profile :namespace
                                          :effects :capabilities])
                         flat)
        evaluation (mapv #(select-keys % [:node-id :kind :evaluation-order])
                         (filter :evaluation-order flat))
        latent (mapv #(select-keys % [:node-id :params :latent-effects])
                     (filter #(= :fn (:kind %)) flat))
        calls (mapv #(select-keys % [:node-id :operator :arguments :effects])
                    (filter #(= :call (:kind %)) flat))]
    {:kind :gravity/stage0-core-artifact
     :pass {:name :core-lowering
            :input :expanded-syntax
            :output :core-ast
            :requires [:reader :namespace-analyzer]
            :preserves [:source-spans :generated-origin :profile :effects
                        :capabilities]
            :rejects ["L2-UNKNOWN-CORE-FORM" "L2-EVAL-ORDER"
                      "L2-RECUR-TARGET" "L2-SET-ILLEGAL" "L2-THROW-ILLEGAL"
                      "L2-HOST-SEMANTICS" "L2-LOWERING-GAP"]}
     :module (select-keys module [:module :source-path :profile :target :effects
                                  :capabilities :safety :metadata])
     :macro-expansion-trace (:macro-expansion-trace macro-artifact)
     :expanded-syntax-object-stream expanded-syntax
     :expanded-core-ast roots
     :core-node-source-map source-map
     :core-form-kind-records form-kinds
     :evaluation-order-metadata evaluation
     :latent-function-effect-records latent
     :call-records calls
     :diagnostics []}))
