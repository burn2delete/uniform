

(defn check-file-artifact
  [path]
  (let [reader-artifact (compiler-c2-reader-file-artifact path)
        source-text (c2-reader-artifact-source-text path reader-artifact)
        records (c2-reader-artifact-top-level-records reader-artifact)
        forms (mapv :form records)
        module (when (ns-form? (first forms))
                 (parse-module path forms))
        bootstrap-metadata (get-in module [:metadata :bootstrap])
        sh06-self-hosting-fixture?
        (= :SH-06 (get-in module [:metadata :slice]))
        gravity-owned-module?
        (and (= :gravity-source (:owner bootstrap-metadata))
             (= :gravity (:source-language bootstrap-metadata)))
        basename (.getName (java.io.File. path))]
    (binding [*authenticated-source-form-records*
              {:source-path path :source-text source-text :records records}]
     (cond
      sh06-self-hosting-fixture?
      (sh06-resolution-source-artifact path source-text)

      gravity-owned-module?
      (module-source-artifact-from-records path source-text records)

      (contains? b14-public-check-basenames basename)
      (b14-document-source-artifact path source-text)

      (contains? core-public-check-basenames basename)
      (core-source-artifact path source-text)

      (contains? runtime-selection-public-check-basenames basename)
      (runtime-selection-source-artifact path source-text)

      (contains? managed-runtime-public-check-basenames basename)
      (managed-runtime-source-artifact path source-text)

      :else
      (compile-source-from-records path source-text records)))))