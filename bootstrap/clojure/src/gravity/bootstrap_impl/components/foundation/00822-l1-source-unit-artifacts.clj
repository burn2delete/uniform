

(defn l1-source-unit-artifacts
  [source-path source-text reader-options project-context]
  {:source-unit-record
   (c2-source-unit-record source-path source-text reader-options
                          project-context)
   :reader-options reader-options})