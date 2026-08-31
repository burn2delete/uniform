

;; This binding is non-nil only while the C11 bridge calls the genuine
;; checked-core verifier.  The opaque value is created inside the C11 lexical
;; boundary below; ordinary checked-core callers and public substitutions
;; never receive an owned upstream marker.
(def ^:dynamic *p15-s23-c11-upstream-diagnostic-owner* nil)