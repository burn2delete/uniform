(ns gravity.c2-source-identity.identity)

(defn explicit-project-context
  [normalize-relative-path valid-project-relative-path? project-context]
  (let [project-root-id (:project-root-id project-context)
        project-relative-path (:project-relative-path project-context)
        normalized-path (when (string? project-relative-path)
                          (normalize-relative-path project-relative-path))]
    (when-not (and (string? project-root-id)
                   (re-matches #"sha256:[0-9a-f]{64}" project-root-id)
                   (string? normalized-path)
                   (valid-project-relative-path? project-relative-path))
      (throw
       (ex-info
        "reader project context requires a project-root id and relative path"
        {:id "C2-HASH"
         :project-context project-context
         :normalized-project-relative-path normalized-path
         :missing-fields
         (vec (remove #(get project-context %)
                      [:project-root-id :project-relative-path]))})))
    (assoc project-context :project-relative-path normalized-path)))

(defn valid-options?
  [reader-options]
  (and (map? reader-options)
       (boolean? (:retain-comments reader-options))
       (set? (:enabled-features reader-options))
       (string? (:extension-policy reader-options))
       (boolean
        (re-matches #"sha256:[0-9a-f]{64}"
                    (:extension-policy reader-options)))))

(defn validate-options!
  [valid-options? reader-options]
  (when-not (valid-options? reader-options)
    (throw
     (ex-info
      "reader options must be deterministic and content-addressed"
      {:id "C2-HASH"
       :reader-options reader-options
       :required-fields
       {:retain-comments :boolean
        :enabled-features :set
        :extension-policy :sha256-lowercase-hex}})))
  reader-options)

(defn project-root-record
  [explicit-project-context project-context]
  (let [context (explicit-project-context project-context)]
    {:path (:project-root-path context)
     :project-root-id (:project-root-id context)}))

(defn source-identity-inputs
  [explicit-project-context validate-options! sha256-hex source-text
   reader-options project-context]
  (let [context (explicit-project-context project-context)
        options (validate-options! reader-options)]
    {:project-root-id (:project-root-id context)
     :project-relative-path (:project-relative-path context)
     :encoding :utf-8
     :bytes-hash (str "sha256:" (sha256-hex source-text))
     :reader-options options
     :enabled-features (:enabled-features options)
     :extension-policy (:extension-policy options)}))
