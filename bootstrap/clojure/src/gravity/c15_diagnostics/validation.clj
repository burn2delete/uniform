(ns gravity.c15-diagnostics.validation
  (:require [gravity.c15-diagnostics.operations :as operations]
            [gravity.c15-diagnostics.records :as records]
            [gravity.compiler-verification-shared :as shared]))
(defn fail! [configuration id source-path subject extra]
  (operations/invoke :c15-diagnostics-fail!
                     (fn [rule path item fields]
                       (operations/invoke :fail!
                                          (fn [r text payload] (throw (ex-info text (assoc (or payload {}) :id r))) )
                                          rule (get (:compiler-verification-diagnostic-messages configuration) rule
                                                    "compiler diagnostic validation failed")
                                          (merge {:source-span (or (:source-span item) (records/source-span path 0))
                                                  :diagnostic-family :compiler-diagnostics
                                                  :stage (or (:stage item) :c15-compiler-diagnostics)
                                                  :offending-diagnostic-id (:diagnostic-id item)
                                                  :schema-field (:schema-field item) :artifact-id (:artifact-id item)
                                                  :profile (:profile item) :target (:target item)
                                                  :remediation "Regenerate structured diagnostic artifacts with stable ids, primary spans, origin chains, facts, remediation, redaction, deterministic ordering, and golden fixtures."}
                                                 fields)))
                     id source-path subject extra))
(defn validate-source-overrides! [configuration source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (let [[id subject-kind] (get (:compiler-verification-override-diagnostics configuration) fail-kind)]
      (when (contains? (set (:c15-diagnostics-diagnostic-ids configuration)) id)
        (fail! configuration id source-path {:stage subject-kind :diagnostic-id (str "c15-invalid-" (name fail-kind))
                                              :schema-field fail-kind :artifact-id (str "c15-diagnostic-artifact-" (name fail-kind))
                                              :profile :hosted :target :jvm}
               {:missing-fields [fail-kind]})))))
(defn validate! [configuration source-path artifact]
  (let [required (set (:c15-diagnostic-required-fields configuration))
        schema-fields (set (get-in artifact [:diagnostic-schema :required-fields]))
        diagnostics (get-in artifact [:diagnostic-stream :diagnostics])
        catalog-rules (set (map :rule (get-in artifact [:diagnostic-catalog :rules])))
        golden (:golden-diagnostic-fixtures artifact)]
    (when-not (= required schema-fields) (fail! configuration "C15-SCHEMA" source-path (:diagnostic-schema artifact)
                                                 {:missing-fields (vec (remove schema-fields required))}))
    (when-not (= (count diagnostics) (count (distinct (map :diagnostic-id diagnostics))))
      (fail! configuration "C15-ID" source-path (first diagnostics) {:missing-fields [:diagnostic-id]}))
    (doseq [diagnostic diagnostics]
      (let [present (set (keys diagnostic))]
        (when-not (every? present required) (fail! configuration "C15-SCHEMA" source-path diagnostic
                                                       {:missing-fields (vec (remove present required))})))
      (when-not (= (:diagnostic-id diagnostic) (records/stable-id (dissoc diagnostic :diagnostic-id :ordering-key)))
        (fail! configuration "C15-ID" source-path diagnostic {:missing-fields [:stable-id]}))
      (when-not (every? #(get-in diagnostic [:primary %]) [:span :syntax-id :artifact])
        (fail! configuration "C15-SPAN" source-path diagnostic {:missing-fields [:primary]}))
      (when (and (:generated? diagnostic) (or (empty? (:origin-chain diagnostic))
                                               (not-any? #(= :generated-by (:role %)) (:related diagnostic))))
        (fail! configuration "C15-ORIGIN" source-path diagnostic {:missing-fields [:origin-chain]}))
      (when-not (and (map? (:facts diagnostic)) (seq (:facts diagnostic)))
        (fail! configuration "C15-FACTS" source-path diagnostic {:missing-fields [:facts]}))
      (when-not (seq (:remediation diagnostic))
        (fail! configuration "C15-REMEDIATION" source-path diagnostic {:missing-fields [:remediation]})))
    (when-not (true? (get-in artifact [:redaction-report :public-safe?]))
      (fail! configuration "C15-REDACTION" source-path (:redaction-report artifact) {:missing-fields [:public-safe]}))
    (when-not (= diagnostics (vec (sort-by :ordering-key diagnostics)))
      (fail! configuration "C15-ORDER" source-path (:diagnostic-stream artifact) {:missing-fields [:ordering-key]}))
    (when-not (= (set (:c15-diagnostics-diagnostic-ids configuration)) catalog-rules)
      (fail! configuration "C15-SCHEMA" source-path (:diagnostic-catalog artifact) {:missing-fields [:diagnostic-catalog]}))
    (when-not (and (= (set (:c15-diagnostics-diagnostic-ids configuration)) (set (map :rule golden)))
                   (every? #(= :matched (:status %)) golden))
      (fail! configuration "C15-GOLDEN" source-path (first golden) {:missing-fields [:golden-fixtures]})))
  :complete)
