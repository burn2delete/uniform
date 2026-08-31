

;; A fresh authoritative P15 run must rebuild from an empty context, but it
;; should not rebuild the same immutable evidence node for every downstream
;; proof that consumes it.  This dynamic context is deliberately scoped to a
;; single top-level artifact request: it is not persisted, serialized, or
;; consulted by the developer result cache.  Artifact identities therefore
;; continue to be computed from the same values while sibling evidence shares
;; the already-built node in that request.
(def ^:dynamic *p15-s23-artifact-build-context* nil)