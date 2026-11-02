

(defn compiler-c4-macro-source-artifact
  [source-path source-text]
  (let [c2 (compiler-c2-reader-source-artifact source-path source-text)
        forms (:parsed-semantic-values c2)
        module (parse-module source-path forms)]
    (if (sh05-bounded-authoritative-source? source-path module forms)
      (sh05-c4-compatibility-artifact
       source-path (sh05-macro-source-artifact source-path source-text))
      (compiler-c4-stage0-legacy-source-artifact source-path source-text))))