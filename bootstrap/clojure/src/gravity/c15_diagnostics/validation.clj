(ns gravity.c15-diagnostics.validation
  (:require [gravity.diagnostics :as diagnostics]
            [gravity.c15-diagnostics.operations :as operations]
            [gravity.c15-diagnostics.records :as records]))
(defn context-valid? [diagnostic]
  (diagnostics/native-context-valid? diagnostic))
(defn primary-valid? [diagnostic]
  (diagnostics/native-primary-valid? diagnostic))
(defn fail! [configuration id source-path subject extra]
  (operations/invoke :c15-diagnostics-fail!
                     (fn [rule path item fields]
                       (operations/invoke :fail!
                                          (fn [r text payload] (throw (ex-info text (assoc (or payload {}) :id r))) )
                                          rule (get (:compiler-verification-diagnostic-messages configuration) rule
                                                    "compiler diagnostic validation failed")
                                          (merge {:source-span (or (:source-span item) (records/source-span path 0))
                                                  :diagnostic-family :compiler-diagnostics
                                                  :stage (or (:stage item) :compiler-diagnostics)
                                                  :offending-diagnostic-id (:diagnostic-id item)
                                                  :schema-field (:schema-field item) :artifact-id (:artifact-id item)
                                                  :profile (:profile item) :target (:target item)
                                                  :syntax-id (get-in item [:primary :syntax-id])
                                                  :remediation "Regenerate structured diagnostic artifacts with stable ids, primary spans, origin chains, facts, remediation, redaction, deterministic ordering, and golden fixtures."}
                                                 fields)))
                     id source-path subject extra))
(defn validate-source-overrides! [configuration source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (let [[id subject-kind] (get (:compiler-verification-override-diagnostics configuration) fail-kind)]
      (when (contains? (set (:c15-diagnostics-diagnostic-ids configuration)) id)
        (fail! configuration id source-path {:stage :compiler-diagnostics
                                              :diagnostic-id (str "c15-invalid-" (name fail-kind))
                                              :schema-field fail-kind :artifact-id (str "c15-diagnostic-artifact-" (name fail-kind))
                                              :profile nil :target nil}
               {:missing-fields [fail-kind] :subject-kind subject-kind})))))
(defn validate! [configuration source-path artifact]
  (diagnostics/validate-native!
   {:fail! #'fail! :stable-id #'records/stable-id
    :context-valid? #'context-valid? :primary-valid? #'primary-valid?}
   configuration source-path artifact))
