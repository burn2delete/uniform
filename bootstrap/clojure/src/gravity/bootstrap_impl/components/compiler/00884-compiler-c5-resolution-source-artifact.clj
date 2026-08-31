

(defn compiler-c5-resolution-source-artifact
  [source-path source-text]
  (if (sh06-resolution-slice-source? source-path source-text)
    (sh06-resolution-source-artifact source-path source-text)
    (compiler-c5-stage0-legacy-source-artifact source-path source-text)))