

(defn compiler-c5-resolution-source-artifact
  ([source-path source-text]
   (sh06-resolution-source-artifact source-path source-text))
  ([source-path source-text module-inputs]
   (sh06-resolution-source-artifact source-path source-text module-inputs)))

(defn compiler-c5-resolution-file-artifact
  ([source-path] (sh06-resolution-file-artifact source-path))
  ([source-path module-inputs] (sh06-resolution-file-artifact source-path module-inputs)))
