

(defn run-file
  [path]
  (let [source (slurp path)
        macro-artifact (macro-source-artifact path source)
        module (assoc (:module macro-artifact) :forms (:expanded-forms macro-artifact))
        _ (executable-profile! path module (:forms module))]
    (run-main module)))

(defn p18-diagnostic-id
  [f]
  (try
    (f)
    nil
    (catch clojure.lang.ExceptionInfo ex
      (:id (ex-data ex)))))

(defn p18-executable?
  [path]
  (let [file (java.io.File. path)]
    (and (.isFile file) (.canExecute file))))