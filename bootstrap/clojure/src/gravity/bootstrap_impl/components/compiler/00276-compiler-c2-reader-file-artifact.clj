

(declare compiler-c2-reader-file-artifact c2-reader-fail!
         compile-source-from-records)

(defn c2-reader-artifact-source-text
  [path artifact]
  (let [source-text (apply str (map :raw (:token-stream artifact)))
        expected-hash (get-in artifact [:source-unit-record :bytes-hash])
        observed-hash (str "sha256:" (sha256-hex source-text))]
    (when-not (= expected-hash observed-hash)
      (c2-reader-fail!
       "C2-HASH" path
       {:source-id (get-in artifact [:source-unit-record :source-id])
        :source-span (source-span path 0)
        :reader-options standard-reader-options}
       {:missing-fields [:complete-token-source-reconstruction]
        :facts {:expected-bytes-hash expected-hash
                :observed-bytes-hash observed-hash}}))
    source-text))

(defn c2-reader-artifact-top-level-records
  [artifact]
  (let [forms-by-id (into {} (map (juxt :form-id identity)
                                  (:form-tree artifact)))]
    (mapv
     (fn [root-index form-id]
       (let [form (forms-by-id form-id)]
         {:form (:value form)
          :kind (:kind form)
          :form-id form-id
          :span (assoc (:span form) :form-index root-index)
          :metadata (:metadata form)
          :reader-origin
          {:kind :source
           :raw-form-kind (:kind form)
           :raw-excerpt (:raw form)
           :abbreviation (:abbrev form)}
          :generated-origin (:generated-origin form)}))
     (range) (:top-level-form-ids artifact))))