

(defn compiler-pass-file-artifact
  [path]
  (compiler-pass-source-artifact path (slurp path)))

(defn checked-core-file-artifact
  [path]
  (checked-core-source-artifact path (slurp path)))

(defn mir-file-artifact
  [path]
  (mir-source-artifact path (slurp path)))

(defn domain-ir-file-artifact
  [path]
  (domain-ir-source-artifact path (slurp path)))