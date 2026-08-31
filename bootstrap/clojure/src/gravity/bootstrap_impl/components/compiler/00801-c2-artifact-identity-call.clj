

(defn- c2-artifact-identity-call
  [operation-key operation & args]
  (if *c2-artifact-identity-leaf-call?*
    (c2-artifact-identity/call-entrypoint-body
     operation-key operation args)
    (binding [*c2-artifact-identity-leaf-call?* true]
      (c2-artifact-identity/with-operations
       (c2-artifact-identity-ops)
       #(c2-artifact-identity/call-entrypoint-body
         operation-key operation args)))))