

(defn c-backend-stage2-runtime-artifact-source-path
  [compiler-source]
  (let [compiler-file (java.io.File. compiler-source)
        sibling (java.io.File.
                 (or (.getParentFile compiler-file)
                     (java.io.File. "."))
                 "runtime.gravity")]
    (if (.isFile sibling)
      (.getPath sibling)
      p15-s23-stage2-runtime-artifact-source-relative-path)))

(defn c-backend-stage2-runtime-artifact-hash-input
  [plan authoritative-module contract-definitions function-hashes
   derived-contract-facts]
  {:kind :gravity/p15-s23-reference-runtime-contract-bound-artifact
   :entrypoint (:entrypoint plan)
   :module (select-keys authoritative-module
                        [:module :profile :target :effects :capabilities
                         :providers :exports :safety])
   :functions (c-backend-canonical-value (:functions plan))
   :instruction-summary (:instruction-summary plan)
   :effect-summary (:effect-summary plan)
   :contract-definitions
   (c-backend-canonical-value contract-definitions)
   :function-semantic-hashes function-hashes
   :independently-derived-contract-facts
   (c-backend-canonical-value derived-contract-facts)})