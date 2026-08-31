

(defn safety-conformance-file-artifact
  [path]
  (safety-conformance-source-artifact path (slurp path)))

(defn profile-manifest-file-artifact
  [path]
  (profile-manifest-source-artifact path (slurp path)))

(defn profile-set-file-artifact
  [path]
  (profile-set-source-artifact path (slurp path)))

(defn profile-validation-file-artifact
  [path]
  (constrained-profile-source-artifact path (slurp path)))

(defn profile-distributed-ai-file-artifact
  [path]
  (distributed-ai-profile-source-artifact path (slurp path)))

(defn profile-compatibility-file-artifact
  [path]
  (profile-compatibility-source-artifact path (slurp path)))

(defn profile-compliance-file-artifact
  [path]
  (profile-compliance-source-artifact path (slurp path)))

(defn performance-file-artifact
  [path]
  (performance-source-artifact path (slurp path)))