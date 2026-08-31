(ns gravity.compiler-pass-manifest.support
  "Shared predicates for compiler pass manifest validation.")

(defn present?
  [value]
  (and (some? value)
       (not (and (coll? value) (empty? value)))))
