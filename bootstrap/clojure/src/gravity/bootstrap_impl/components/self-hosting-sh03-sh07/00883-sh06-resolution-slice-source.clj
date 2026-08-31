

(defn sh06-resolution-slice-source?
  [source-path source-text]
  (try
    (let [c2 (compiler-c2-reader-source-artifact source-path source-text)
          forms (:parsed-semantic-values c2)
          module (parse-module source-path forms)]
      (= :SH-06 (get-in module [:metadata :slice])))
    (catch clojure.lang.ExceptionInfo _ false)))