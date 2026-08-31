
(defn- c14-call [operation & args]
  (if *c14-leaf-call?*
    (apply operation args)
    (binding [*c14-leaf-call?* true]
      (c14/with-operations (c14-lowering-ops)
        #(apply operation args)))))

(defn c14-lowering-source-overrides [module]
  (c14-call c14/c14-lowering-source-overrides module))

(defn c14-lowering-validate-source-overrides! [source-path overrides]
  (c14-call c14/c14-lowering-validate-source-overrides!
            source-path overrides))

(defn c14-lowering-diagnostic-catalog [source-path input-id]
  (c14-call c14/c14-lowering-diagnostic-catalog source-path input-id))

(defn c14-lowering-validate! [source-path artifact]
  (c14-call c14/c14-lowering-validate! source-path artifact))

(defn c14-lowering-capability-proof [artifact]
  (c14-call c14/c14-lowering-capability-proof artifact))

(defn compiler-c14-lowering-source-artifact [source-path source-text]
  (c14-call c14/compiler-c14-lowering-source-artifact
            source-path source-text))

(defn compiler-c14-lowering-file-artifact [path]
  (c14-call c14/compiler-c14-lowering-file-artifact path))