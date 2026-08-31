
(defn- c18-call [operation & args]
  (if *c18-leaf-call?*
    (apply operation args)
    (binding [*c18-leaf-call?* true]
      (c18/with-operations (c18-verification-ops)
        #(apply operation args)))))

(defn c18-verification-source-overrides [module]
  (c18-call c18/c18-verification-source-overrides module))

(defn c18-verification-fail! [id source-path subject extra]
  (c18-call c18/c18-verification-fail! id source-path subject extra))

(defn c18-verification-validate-source-overrides! [source-path overrides]
  (c18-call c18/c18-verification-validate-source-overrides!
            source-path overrides))

(defn c18-verification-diagnostic-stream [source-path input-id]
  (c18-call c18/c18-verification-diagnostic-stream source-path input-id))

(defn c18-pass-risk-records []
  (c18-call c18/c18-pass-risk-records))

(defn c18-verification-validate! [source-path artifact]
  (c18-call c18/c18-verification-validate! source-path artifact))

(defn c18-verification-capability-proof [artifact]
  (c18-call c18/c18-verification-capability-proof artifact))

(defn compiler-c18-verification-source-artifact [source-path source-text]
  (c18-call c18/compiler-c18-verification-source-artifact
            source-path source-text))

(defn compiler-c18-verification-file-artifact [path]
  (c18-call c18/compiler-c18-verification-file-artifact path))