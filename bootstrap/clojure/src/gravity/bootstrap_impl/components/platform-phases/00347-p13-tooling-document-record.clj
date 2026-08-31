

(defn p13-tooling-document-record
  [document]
  (let [summary (p13-tooling-document-summaries document)]
    (merge
     {:document document
      :task-id (p13-task-id document)
      :governing-doc (p13-tooling-phase-governing-documents document)
      :tool-id (get-in summary [:owned-surface])
      :diagnostics (p13-tooling-diagnostics-by-document document)
      :evidence (into {}
                      (map (fn [diagnostic]
                             [(keyword (str/lower-case diagnostic))
                              {:diagnostic diagnostic
                               :source :governing-document
                               :status :present}])
                           (p13-tooling-diagnostics-by-document document)))
      :conformance
      {:accepted-fixture "bootstrap/clojure/fixtures/accepted/tooling-experience.gravity"
       :rejected-fixture (str "bootstrap/clojure/fixtures/rejected/"
                              (p13-tooling-rejected-fixture-names document))
       :artifact-evidence :tooling-experience
       :validation-command "clojure -M:test"
       :status :complete}}
     summary)))

(defn p13-tooling-document-records
  []
  (into {} (map (fn [document]
                  [document (p13-tooling-document-record document)])
                p13-tooling-documents)))

(defn p13-accepted-tooling-fixtures
  []
  (mapv (fn [document]
          {:document document
           :fixture "bootstrap/clojure/fixtures/accepted/tooling-experience.gravity"
           :artifact (get-in (p13-tooling-document-summaries document)
                             [:owned-surface])
           :evidence [(p13-task-id document)
                      (p13-tooling-phase-governing-documents document)]
           :status :accepted})
        p13-tooling-documents))

(defn p13-rejected-tooling-fixtures
  []
  (mapv (fn [document]
          {:document document
           :fixture (str "bootstrap/clojure/fixtures/rejected/"
                         (p13-tooling-rejected-fixture-names document))
           :artifact :stable-tooling-diagnostic
           :diagnostic (p13-tooling-rejected-diagnostics document)
           :evidence [(p13-task-id document)
                      (p13-tooling-phase-governing-documents document)]
           :status :rejected})
        p13-tooling-documents))

(defn p13-tooling-conformance-evidence
  []
  (mapv (fn [document]
          {:document document
           :accepted-behavior (get-in (p13-tooling-document-summaries document)
                                      [:accepted-behavior])
           :rejected-behavior (p13-tooling-rejected-diagnostics document)
           :artifacts (get-in (p13-tooling-document-summaries document)
                              [:artifact-keys])
           :validation ["clojure -M:test"
                        "clojure -M:gravity tooling-experience bootstrap/clojure/fixtures/accepted/tooling-experience.gravity"]
           :status :complete})
        p13-tooling-documents))

(defn p13-tooling-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/phase13-tooling-diagnostic-stream
   :stage :tooling-experience
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           (let [document (p13-tooling-diagnostic-document id)]
             {:artifact :gravity/diagnostic
              :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
              :diagnostic id
              :rule id
              :severity :error
              :stage :tooling-experience
              :document-id document
              :task (when document (p13-task-id document))
              :primary {:span (source-span source-path index)
                        :syntax-id (str "p13-tooling-syntax-" index)
                        :artifact input-id}
              :missing-fact (keyword (str/lower-case id))
              :remediation [{:kind :surface-compiler-truth}
                            {:kind :preserve-source-artifact-lineage}
                            {:kind :enforce-authority-and-redaction}
                            {:kind :emit-structured-tooling-evidence}]
              :ordering-key [id :tooling-experience]}))
         p13-tooling-diagnostic-ids
         (range))
   :status :complete})

(defn p13-present?
  [value]
  (if (coll? value)
    (seq value)
    (some? value)))