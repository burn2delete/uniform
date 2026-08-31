
(defn- c12-call [operation & args]
  (if *c12-leaf-call?*
    (apply operation args)
    (binding [*c12-leaf-call?* true]
      (c12/with-operations (c12-domain-ir-ops)
        #(apply operation args)))))

(defn c12-domain-ir-source-overrides
  [module]
  (c12-call c12/c12-domain-ir-source-overrides module))

(defn c12-domain-ir-validate-source-overrides!
  [source-path overrides]
  (c12-call c12/c12-domain-ir-validate-source-overrides! source-path overrides))

(defn c12-domain-ir-diagnostic-catalog
  [source-path]
  (c12-call c12/c12-domain-ir-diagnostic-catalog source-path))

(defn compiler-c12-domain-ir-source-artifact
  [source-path source-text]
  (c12-call c12/compiler-c12-domain-ir-source-artifact source-path source-text))

(defn compiler-c12-domain-ir-file-artifact
  [path]
  (c12-call c12/compiler-c12-domain-ir-file-artifact path))

(def c13-optimization-diagnostic-ids optimization-lowering/c13-optimization-diagnostic-ids)
(def c14-lowering-diagnostic-ids optimization-lowering/c14-lowering-diagnostic-ids)