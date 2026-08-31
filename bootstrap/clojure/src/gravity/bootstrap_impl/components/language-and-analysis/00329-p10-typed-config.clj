

(defn p10-typed-config
  [source-schema]
  {:artifact :gravity/config-schema
   :schema-id "AppConfig"
   :schema-version 1
   :schema-hash (:schema-hash source-schema)
   :config-id :ticket-service-config
   :sources [:env :secrets :file]
   :source-precedence [:secrets :env :file]
   :defaults {:classification-threshold 0.7}
   :required-fields [:database-url :classification-threshold]
   :secret-fields #{:database-url}
   :effects #{:build/env :secrets/read :filesystem/read}
   :capabilities #{:env/read :secret/read :fs/read}
   :artifact-policy :redacted
   :redaction-report {:database-url :redacted
                      :diagnostics :redacted-marker-only}
   :build-reproducibility-record {:captured-inputs [:classification-threshold]
                                  :hermetic-build-compatible? true}
   :runtime-reload-policy {:compatibility :compatible-values-only
                           :lifecycle :reload-with-validation}
   :status :complete})