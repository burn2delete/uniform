

(defn p15-s23-final-seed-retirement-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"]
    (when (.isFile (java.io.File. source-path))
      (try
        (p15-s23-final-seed-retirement-evidence-summary
         (p15-s23-final-seed-retirement-file-artifact source-path))
        (catch Exception _
          nil)))))