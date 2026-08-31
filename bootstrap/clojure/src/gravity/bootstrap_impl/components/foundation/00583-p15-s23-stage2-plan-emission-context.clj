

;; The C2/C3 ingress authenticates the same pinned SH-02 compiler source at
;; several sibling boundaries.  Keep one compiled binding only for the current
;; plan-emission request; source bytes and the emitter rule are still reloaded
;; and authenticated before every lookup, and no binding survives this dynamic
;; scope.
(def ^:private ^:dynamic *p15-s23-stage2-plan-emission-context* nil)