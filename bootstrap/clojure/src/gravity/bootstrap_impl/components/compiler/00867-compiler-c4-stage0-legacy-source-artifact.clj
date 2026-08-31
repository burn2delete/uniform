

;; Preserve the broader stage0 user-macro oracle outside SH-05 credit.  The
;; public C4 compatibility entrypoint uses Gravity for the compiler-required
;; defn subset and falls back only for source that actually defines user macros.
(def compiler-c4-stage0-legacy-source-artifact
  compiler-c4-macro-source-artifact)