

(defn validate-stage0-compiled-package-safety!
  [module package-safety]
  (gravity.package/check-package-safety! #'p12-present? #'compiled-package-fail! module package-safety))
