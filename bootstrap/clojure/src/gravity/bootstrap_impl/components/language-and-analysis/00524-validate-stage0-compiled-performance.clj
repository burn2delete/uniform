

(defn validate-stage0-compiled-performance!
  [module]
  (let [source-path (:source-path module)
        performance (get-in module [:metadata :performance] {})
        manifest (stage0-compiled-performance-manifest module)
        claim (:claim performance)]
    (when (map? claim)
      (perf1-validate-claim! source-path
                             manifest
                             performance
                             (perf1-normalize-claim claim)))
    (doseq [record (:check-elision performance)]
      (validate-stage0-compiled-check-elision! module record))))