

(defn- c2-source-identity-call
  [operation-key operation & args]
  (if *c2-source-identity-leaf-call?*
    (c2-source-identity/call-entrypoint-body
     operation-key operation args)
    (binding [*c2-source-identity-leaf-call?* true]
      (c2-source-identity/with-operations
       (c2-source-identity-ops)
       #(c2-source-identity/call-entrypoint-body
         operation-key operation args)))))