

(defn- c3-artifact-identity-call
  [operation & args]
  (if *c3-artifact-identity-leaf-call?*
    (apply operation args)
    (binding [*c3-artifact-identity-leaf-call?* true]
      (c3-artifact-identity/with-operations
       (c3-artifact-identity-ops)
       #(apply operation args)))))

(defn c3-path-neutral-reader-artifact-view
  [c2-view]
  (c3-artifact-identity-call
   c3-artifact-identity/c3-path-neutral-reader-artifact-view c2-view))

(defn c3-path-neutral-syntax-object
  [syntax]
  (c3-artifact-identity-call
   c3-artifact-identity/c3-path-neutral-syntax-object syntax))

(defn c3-gravity-syntax-boundary-identity-view
  [boundary]
  (c3-artifact-identity-call
   c3-artifact-identity/c3-gravity-syntax-boundary-identity-view boundary))

(defn c3-artifact-identity-input
  [artifact]
  (c3-artifact-identity-call
   c3-artifact-identity/c3-artifact-identity-input artifact))

(defn c3-artifact-id
  [artifact]
  (c3-artifact-identity-call c3-artifact-identity/c3-artifact-id artifact))