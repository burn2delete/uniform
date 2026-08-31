

;; ---------------------------------------------------------------------------
;; Hosted C target (bounded stage0 instruction-plan slice)
;;
;; This backend is deliberately small.  It consumes the verified stage0 plan
;; produced above and lowers only the closed, side-effecting hosted subset to
;; a deterministic C11 translation unit.  The subset is constant-output for
;; now: evaluation is performed by the verified stage0 plan, then the resulting
;; stdout is represented by a C fputs call.  This is a real executable target
;; boundary (with optional host cc invocation), while the artifact records the
;; seed boundary and does not claim final self-hosting.

(def c-backend-supported-targets #{:c :c-hosted :c11})
(def c-backend-supported-dialects #{:c11})
(def c-backend-supported-instructions
  #{:literal :quote :local :println :do :if :let :builtin-call
    :function-call :vector-literal :map-literal :set-literal})

;; The runtime-derived mode is intentionally narrower than the historical
;; stage0-output fallback below.  Each instruction in this set is emitted as
;; executable C statements; no precomputed stdout buffer is embedded in the
;; generated translation unit.  More ambitious instructions continue to use
;; the reviewed stage0 fallback until their runtime semantics have a closed
;; C lowering rule.  `str` is admitted only as a direct println value after
;; its operands have passed the typed byte-string check below.
(def c-backend-runtime-derived-instructions
  #{:literal :quote :local :println :do :if :let :builtin-call})