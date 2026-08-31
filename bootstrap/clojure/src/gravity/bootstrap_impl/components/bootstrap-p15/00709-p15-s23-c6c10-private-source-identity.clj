

(defn p15-s23-c6c10-private-source-identity
  [source-path source-unit]
  (let [identity-inputs
        {:domain :gravity/c6-c10-private-source-identity-v1
         :bytes-hash (:bytes-hash source-unit)
         :encoding (:encoding source-unit)
         :reader-options (:reader-options source-unit)
         :enabled-features (:enabled-features source-unit)
         :extension-policy (:extension-policy source-unit)}
        private-source-id
        (p15-s23-c6c10-canonical-digest source-path identity-inputs)]
    {:identity-inputs identity-inputs
     :private-source-id private-source-id}))