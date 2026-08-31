

(defn p12-package-safety
  []
  {:artifact :gravity/package-safety-manifest
   :artifact-id "package-safety:support-agent-001"
   :package-id "acme/support-agent"
   :unsafe-islands ["ffi.clock/read-monotonic"]
   :unsafe-audit-metadata {:unsafe-island-id "ffi.clock/read-monotonic"
                           :owner "runtime-team"
                           :invariant "monotonic clock is read-only"
                           :review-state :reviewed}
   :safe-wrappers ["time/monotonic-now"]
   :ffi-boundaries [{:symbol "clock_gettime"
                     :abi :posix
                     :ownership :borrowed
                     :lifetime :call
                     :errors :errno}]
   :privileged-effects #{:ffi/call}
   :capabilities #{:ffi/c}
   :taint-sinks [{:sink "ffi.load/library" :data :path}]
   :proof-claims [{:proof-id "proof:time-wrapper"
                   :checker "gravity.safety/package-check"
                   :revocation-status :valid}]
   :review-state :reviewed
   :vulnerability-state :none-known
   :schema-validated true
   :status :complete})