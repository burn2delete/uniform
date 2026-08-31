(ns gravity.c2-reader-diagnostics.payload)

(defn fail!
  [{:keys [source-span reader-canonical-hash governing-document message terminal-fail!]}
   id source-path subject extra]
  (let [raw-span (or (:source-span subject)
                     (:source-span extra)
                     (source-span source-path 0))
        source-id (or (:source-id subject)
                      (:source-id extra)
                      (get-in subject [:primary :artifact])
                      (get-in extra [:primary :artifact]))
        span (cond-> raw-span
               (and source-id (not (:file raw-span)))
               (assoc :file source-id))
        raw (or (:raw subject) (:raw-spelling subject)
                (:raw extra) (:raw-spelling extra))
        token-id (or (:token-id subject) (:token-id extra))
        form-id (or (:form-id subject) (:form-id extra))
        facts (merge (or (:facts subject) {})
                     (or (:facts extra) {}))
        remediation
        "Regenerate reader artifacts with deterministic decoding, spans, raw literal facts, extension policy, and stable incremental hashes."
        defaults
        {:artifact :gravity/diagnostic
         :diagnostic-id
         (reader-canonical-hash
          {:rule (keyword id)
           :primary-artifact source-id
           :stage :read-source
           :span (dissoc span :source)
           :token-id token-id
           :form-id form-id
           :facts facts})
         :rule (keyword id)
         :severity :error
         :source-id source-id
         :source-span span
         :primary {:span span :artifact source-id}
         :related []
         :origin-chain [{:kind :source :source-id source-id :path source-path}]
         :profile nil
         :target nil
         :facts facts
         :diagnostic-family :c2-reader
         :stage :read-source
         :document-id "C2"
         :expected-document governing-document
         :involved-artifacts (cond-> [] source-id (conj source-id))
         :token-id token-id
         :form-id form-id
         :raw-spelling raw
         :reader-options (or (:reader-options subject) (:reader-options extra))
         :extension-tag (or (:extension-tag subject) (:extension-tag extra))
         :reader-state {:artifact :gravity/reader-state
                        :stage :read-source
                        :byte-offset (:byte-start span)
                        :line (get-in span [:start :line])
                        :column (get-in span [:start :column])
                        :token-id token-id
                        :form-id form-id}
         :redactions []
         :lifecycle :active
         :remediation remediation
         :remediation-records [{:kind :fix-reader-source}]}
        payload (-> (merge defaults extra)
                    (assoc :artifact (:artifact defaults)
                           :diagnostic-id (:diagnostic-id defaults)
                           :rule (:rule defaults)
                           :severity (:severity defaults)
                           :source-id source-id
                           :source-span span
                           :primary (:primary defaults)
                           :facts facts
                           :diagnostic-family :c2-reader
                           :stage :read-source
                           :document-id "C2"
                           :expected-document governing-document
                           :token-id (:token-id defaults)
                           :form-id (:form-id defaults)
                           :raw-spelling raw
                           :reader-options (:reader-options defaults)
                           :extension-tag (:extension-tag defaults)
                           :remediation remediation
                           :remediation-records [{:kind :fix-reader-source}]))]
    (terminal-fail! id (message id) payload)))
