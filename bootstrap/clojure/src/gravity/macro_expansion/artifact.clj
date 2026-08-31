(ns gravity.macro-expansion.artifact
  (:require [gravity.macro-expansion.engine :as engine]
            [gravity.macro-expansion.operations :as operations]
            [gravity.macro-expansion.registry :as registry]))

(defn from-records
  [source-path source-text records module syntax builtin-macros ops]
  (let [registry-op
        (operations/operation
         ops :macro-registry
         (fn [m s] (registry/macro-registry m s builtin-macros ops)))
        form-op (operations/operation ops :form-op? operations/form-op?)
        expand-syntax
        (operations/operation
         ops :expand-syntax-object
         (fn [r m syn tr depth]
           (engine/expand-syntax-object r m syn tr depth ops)))
        trace (atom [])
        macro-registry (registry-op module syntax)
        raw-expanded-syntax
        (->> syntax
             (remove #(form-op 'defmacro (:form %)))
             (mapv #(expand-syntax macro-registry module % trace 0)))
        trace-records @trace
        origins-by-syntax-id (group-by :input-syntax-id trace-records)
        hygiene-by-syntax-id
        (reduce (fn [acc record]
                  (update acc (:input-syntax-id record) (fnil conj [])
                          {:macro (:macro record)
                           :policy (:hygiene-policy record)}))
                {}
                trace-records)
        distinct-op (operations/operation ops :distinct-by-pr-str
                                          engine/distinct-by-pr-str)
        expanded-syntax
        (mapv (fn [syntax-object]
                (let [trace-origins
                      (mapcat :generated-origin
                              (get origins-by-syntax-id
                                   (:syntax-id syntax-object)))
                      trace-hygiene
                      (get hygiene-by-syntax-id (:syntax-id syntax-object))]
                  (cond-> syntax-object
                    (seq trace-origins)
                    (assoc :generated-origin
                           (distinct-op
                            (concat (:generated-origin syntax-object)
                                    trace-origins)))
                    (seq trace-hygiene)
                    (assoc :hygiene
                           (distinct-op
                            (concat (:hygiene syntax-object)
                                    trace-hygiene))))))
              raw-expanded-syntax)
        body-forms (mapv :form (subvec expanded-syntax 1))
        macro-definitions (->> macro-registry vals distinct
                               (sort-by (comp str :identity)) vec)
        macro-entry (operations/operation ops :macro-namespace-entry
                                          registry/namespace-entry)
        build-record (operations/operation ops :macro-build-effect-record
                                           registry/build-effect-record)
        macro-entries (mapv macro-entry macro-definitions)]
    {:kind :gravity/stage0-macro-artifact
     :pass {:name :macro-expansion
            :input :syntax-object-stream
            :output :expanded-syntax
            :requires [:reader :namespace-analyzer]
            :preserves [:source-spans :metadata :hygiene :profile
                        :generated-origin]
            :rejects ["L4-MACRO-NOT-SYNTAX" "L4-HYGIENE-CAPTURE"
                      "L4-BUILD-EFFECT" "L4-EXPANSION-DEPTH"
                      "L4-GENERATED-PROFILE" "L4-GENERATED-UNSAFE"
                      "L4-PROVENANCE-MISSING"]}
     :module (select-keys module [:module :source-path :profile :target :effects
                                  :capabilities :safety :metadata])
     :macro-namespace-entries macro-entries
     :macro-build-effect-records (mapv build-record macro-definitions)
     :macro-expansion-trace trace-records
     :generated-origin-source-map
     (mapv #(select-keys % [:syntax-id :span :generated-origin])
           expanded-syntax)
     :hygiene-marks
     (mapv #(select-keys % [:syntax-id :hygiene]) expanded-syntax)
     :expanded-syntax-object-stream expanded-syntax
     :expanded-forms body-forms
     :diagnostics []}))
