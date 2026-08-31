

(def managed-runtime-public-check-basenames
  #{"runtime-managed-host.gravity"
    "runtime-managed-host.qst"
    "runtime-r4-host.gravity"
    "runtime-r4-host.qst"
    "runtime-r4-null.gravity"
    "runtime-r4-null.qst"
    "runtime-r4-exception.gravity"
    "runtime-r4-exception.qst"
    "runtime-r4-reflection.gravity"
    "runtime-r4-reflection.qst"
    "runtime-r4-collection.gravity"
    "runtime-r4-collection.qst"
    "runtime-r4-resource.gravity"
    "runtime-r4-resource.qst"
    "runtime-r4-sourcemap.gravity"
    "runtime-r4-sourcemap.qst"
    "runtime-r4-profile.gravity"
    "runtime-r4-profile.qst"
    "runtime-r4-manifest.gravity"
    "runtime-r4-manifest.qst"})

(defn check-artifact-module-name
  [artifact]
  (or (get-in artifact [:module :module])
      (get-in artifact [:module-artifact :module])
      (get-in artifact [:namespace-analysis :namespace])
      (get-in artifact [:namespace-table 0 :name])))