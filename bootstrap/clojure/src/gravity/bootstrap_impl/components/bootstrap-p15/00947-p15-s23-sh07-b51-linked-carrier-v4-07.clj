
(defn p15-s23-sh07-b51-execute-linked-raw-v4-public-root
  [raw root-ordinal args runtime]
  (if-let [{:keys [authority route]}
           (p15-s23-sh07-b51-v4-admission-proof raw root-ordinal args)]
    (let [{:keys [runtime context-lease]}
          (p15-s23-sh07-b51-v4-attach-runtime-token
           authority raw route args runtime)
          admitted-runtime runtime]
      (try
        ;; Retain the exact private context strongly for this synchronous
        ;; execution; the identity registry itself holds only weak references.
        (when-not context-lease
          (throw (ex-info "missing admitted SH-07 context lease"
                          {:id "L2-UNKNOWN-CORE-FORM"
                           :reason :unauthenticated-b51-linked-carrier})))
        (let [public-invocation-scope
              (when (contains? #{1 6} root-ordinal)
                (p15-s23-sh07-b51-top-level-public-invocation-scope
                 admitted-runtime))]
          (p15-s23-stage2-runtime-execute-function-scoped
           admitted-runtime (:plan route)
           (get-in route [:root :function]) args public-invocation-scope))
        (finally
          (p15-s23-sh07-b51-unregister-admitted-runtime-context!
           admitted-runtime)
          (java.lang.ref.Reference/reachabilityFence context-lease))))
    (throw (ex-info "unauthenticated Attempt32 public-root invocation"
                    {:id "L2-UNKNOWN-CORE-FORM"
                     :reason :unauthenticated-b51-linked-carrier
                     :result-committed? false
                     :output-committed? false}))))
