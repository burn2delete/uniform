

(defn validate-stage0-compiled-package-safety!
  [module package-safety]
  (when-not (p12-present? (:unsafe-audit-metadata package-safety))
    (compiled-package-fail!
     "PKG8001" module package-safety
     {:missing-fields [:unsafe-audit-metadata]})))