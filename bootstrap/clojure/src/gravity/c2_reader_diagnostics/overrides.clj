(ns gravity.c2-reader-diagnostics.overrides)

(defn validate-overrides!
  [{:keys [override-diagnostics fail!]} source-path overrides source-unit token-stream]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get override-diagnostics fail-kind)]
      (let [failure-token (or (some #(when (= fail-kind (:decoded %)) %) token-stream)
                              (first token-stream))]
        (fail! id source-path
               {:source-id (:source-id source-unit)
                :source-span (:span failure-token)
                :token-id (:token-id failure-token)
                :raw (:raw failure-token)
                :reader-options (:reader-options source-unit)
                :extension-tag (:extension-tag overrides)}
               {:missing-fields [fail-kind]})))))
